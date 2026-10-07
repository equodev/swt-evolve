package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.BinaryCommService;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;

/**
 * A control redrawn from its own paint loop gets one Paint per frame its client shows, as a native
 * toolkit gives it one per display refresh, instead of one per turn of the event loop.
 *
 * <p>Lives in org.eclipse.swt.widgets so the mocked display's package-private
 * {@code sendEvent(EventTable, Event)} can be wired to really dispatch.
 */
@ExtendWith(Mocks.class)
class PaintPacingTest {

    private BinaryCommService comm;
    private Client client;
    private final ConcurrentLinkedDeque<Runnable> asyncQueue = new ConcurrentLinkedDeque<>();
    private final List<Runnable> timers = new CopyOnWriteArrayList<>();
    private final AtomicInteger paints = new AtomicInteger();
    /** Whether each paint draws something new, so none is dropped as a repeat of the last. */
    private boolean varies = true;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() throws Exception {
        comm = new BinaryCommService();
        client = new Client(URI.create("ws://localhost:" + comm.getPort()));
        assertThat(client.connectBlocking(5, TimeUnit.SECONDS)).isTrue();
        waitFor(comm::hasClient);
        FlutterBridge.set(new RecordingBridge(comm));
    }

    @AfterEach
    void tearDown() throws Exception {
        FlutterBridge.set(null);
        client.closeBlocking();
        comm.stop();
    }

    @Test
    @DisplayName("a paint beyond the ones in flight waits for the frame that takes the oldest")
    void aThirdPaintWaitsForTheFrame() {
        Canvas canvas = inFlightFull();

        canvas.redraw();
        pumpAsync();
        assertThat(paints).as("paints before the client took the first one into a frame").hasValue(PaintPacing.IN_FLIGHT);

        client.inFrame(canvas);
        waitFor(() -> !asyncQueue.isEmpty());
        pumpAsync();
        assertThat(paints).as("paints once it did").hasValue(PaintPacing.IN_FLIGHT + 1);
    }

    @Test
    @DisplayName("redraws made while waiting become one paint")
    void redrawsWhileWaitingMerge() {
        Canvas canvas = inFlightFull();

        for (int i = 0; i < 10; i++) {
            canvas.redraw();
            pumpAsync();
        }
        client.inFrame(canvas);
        waitFor(() -> !asyncQueue.isEmpty());
        pumpAsync();
        assertThat(paints).hasValue(PaintPacing.IN_FLIGHT + 1);
    }

    @Test
    @DisplayName("a client that does not answer slows painting down, never stops it")
    void theTimeoutSendsTheHeldPaint() {
        Canvas canvas = inFlightFull();

        canvas.redraw();
        pumpAsync();
        assertThat(timers).as("a timeout armed for the held paint").hasSize(1);
        timers.remove(0).run();
        pumpAsync();
        assertThat(paints).hasValue(PaintPacing.IN_FLIGHT + 1);
    }

    @Test
    @DisplayName("a held paint goes out with any other state, so it is not a frame behind it")
    void otherStateReleasesAHeldPaint() {
        Canvas canvas = inFlightFull();
        Button button = new Button(canvas.getShell(), SWT.PUSH);
        pumpAsync();

        canvas.redraw();
        pumpAsync();
        assertThat(paints).hasValue(PaintPacing.IN_FLIGHT);

        button.setText("changed");
        FlutterBridge.update();
        pumpAsync();
        assertThat(paints).hasValue(PaintPacing.IN_FLIGHT + 1);
    }

    @Test
    @DisplayName("a repaint of what is already shown still waits for its frame, not the timeout")
    void aRepeatAsksForItsFrame() {
        varies = false;
        Canvas canvas = inFlightFull();
        canvas.redraw();
        pumpAsync();
        client.inFrame(canvas);
        waitFor(() -> !asyncQueue.isEmpty());
        pumpAsync();
        assertThat(paints).as("the repeat goes out once a frame took the first").hasValue(PaintPacing.IN_FLIGHT + 1);
        String nextFrame = "GC/" + canvas.hashCode() + "/nextFrame";
        waitFor(() -> client.received.contains(nextFrame));

        canvas.redraw();
        pumpAsync();
        assertThat(paints).as("the paint after the repeat waits for the repeat's frame").hasValue(PaintPacing.IN_FLIGHT + 1);
        client.inFrame(canvas);
        waitFor(() -> !asyncQueue.isEmpty());
        pumpAsync();
        assertThat(paints).hasValue(PaintPacing.IN_FLIGHT + 2);
    }

    @Test
    @DisplayName("a disposed control's timeout does nothing")
    void aDisposedControlIsNotPainted() {
        Canvas canvas = inFlightFull();
        canvas.redraw();
        pumpAsync();
        canvas.dispose();

        timers.forEach(Runnable::run);
        pumpAsync();
        assertThat(paints).hasValue(PaintPacing.IN_FLIGHT);
    }

    /** A canvas that has painted as often as it may without an answer from its client. */
    private Canvas inFlightFull() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        doAnswer(inv -> {
            Event ev = inv.getArgument(1);
            if (ev != null && (ev.type == SWT.Paint || ev.type == SWT.Resize)) {
                return inv.callRealMethod();
            }
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        doAnswer(inv -> {
            timers.add(inv.getArgument(1));
            return null;
        }).when(display).timerExec(anyInt(), any(Runnable.class));
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 100, 100);
        canvas.addPaintListener(e -> {
            int n = paints.incrementAndGet();
            e.gc.drawLine(0, 0, 10, varies ? n : 10);
        });
        pumpAsync();
        paints.set(0);
        for (int i = 0; i < PaintPacing.IN_FLIGHT; i++) {
            canvas.redraw();
            pumpAsync();
        }
        assertThat(paints).as("paints that go out without waiting").hasValue(PaintPacing.IN_FLIGHT);
        return canvas;
    }

    private void pumpAsync() {
        Runnable r;
        while ((r = asyncQueue.poll()) != null) {
            r.run();
        }
    }

    private static void waitFor(java.util.function.BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + 5000;
        while (!condition.getAsBoolean()) {
            assertThat(System.currentTimeMillis()).as("waited 5 s").isLessThan(deadline);
            Thread.onSpinWait();
        }
    }

    private static final class Client extends WebSocketClient {
        final List<String> received = new CopyOnWriteArrayList<>();

        Client(URI uri) {
            super(uri);
        }

        /** What the client says when the frame showing [control]'s oldest paint begins. */
        void inFrame(Control control) {
            byte[] header = CommService.frameHeader(PaintPacing.IN_FRAME);
            byte[] payload = ("{\"id\":" + control.hashCode() + "}").getBytes(StandardCharsets.UTF_8);
            send(ByteBuffer.allocate(header.length + payload.length).put(header).put(payload).array());
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
        }

        @Override
        public void onMessage(String message) {
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            int nameLength = ((bytes.get(0) & 0xFF) << 8) | (bytes.get(1) & 0xFF);
            byte[] name = new byte[nameLength];
            bytes.position(2);
            bytes.get(name);
            received.add(new String(name, StandardCharsets.UTF_8));
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
        }

        @Override
        public void onError(Exception ex) {
        }
    }
}
