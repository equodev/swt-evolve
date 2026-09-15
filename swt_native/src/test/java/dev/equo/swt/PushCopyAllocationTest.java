package dev.equo.swt;

import dev.equo.swt.comm.BinaryCommService;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.comm.MessageBatch;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How many times a push's bytes are copied on the sending thread before they reach the socket.
 *
 * <p>Measured as the allocation that grows with the payload: the same push at two sizes, the
 * difference in bytes allocated over the difference in bytes a real WebSocket client received.
 * What every push costs whatever its size - walking a widget, naming its channel - cancels out.
 * The transport's own frame is one copy and cannot be avoided while Java-WebSocket is the
 * transport; everything above it is ours.
 */
@ExtendWith(Mocks.class)
class PushCopyAllocationTest {

    private static final int TEXT_CHARS = 64 * 1024;
    private static final int WARMUP = 30;
    private static final int RUNS = 30;

    private static final com.sun.management.ThreadMXBean THREADS =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    private BinaryCommService comm;
    private Client client;
    private WireBridge bridge;

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
        // The server registers the session on its own thread; a frame arriving proves it has.
        comm.send("probe");
        client.awaitBytes(1);
        bridge = new WireBridge(comm);
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() throws Exception {
        FlutterBridge.set(null);
        client.closeBlocking();
        comm.stop();
    }

    @Test
    void directPush_copiesThePayloadOnlyForTheTransport() {
        double copies = copiesPerWireByte(chars -> {
            String text = "x".repeat(chars);
            return () -> {
                try {
                    bridge.serializeAndSend("Probe/1", text);
                } catch (java.io.IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            };
        });
        assertThat(client.lastEvent()).isEqualTo("Probe/1");
        assertThat(client.lastPayload()).isEqualTo("\"" + "x".repeat(2 * TEXT_CHARS) + "\"");
        assertThat(copies).as("payload copies per push, direct").isLessThan(1.5);
    }

    @Test
    void flushOfSeveralWidgets_copiesThePayloadOnlyForTheTransport() {
        Composite parent = new Composite(Mocks.swtShell(), SWT.NONE);
        double copies = copiesPerWireByte(chars -> {
            Button first = button(parent, chars);
            Button second = button(parent, chars);
            return () -> {
                bridge.dirty((DartWidget) first.getImpl());
                bridge.dirty((DartWidget) second.getImpl());
                FlutterBridge.update();
            };
        });
        assertThat(client.lastEvent()).isEqualTo(MessageBatch.EVENT);
        assertThat(client.lastPayload()).startsWith("[[\"Button/").endsWith("}]]");
        assertThat(copies).as("payload copies per push, batched flush").isLessThan(1.5);
    }

    @Test
    void batchOfOwnedPayloads_isFramedOnce() {
        MessageBatch batch = new MessageBatch();
        double copies = copiesPerWireByte(chars -> {
            byte[] payload = ("\"" + "x".repeat(chars) + "\"").getBytes(StandardCharsets.UTF_8);
            return () -> {
                batch.add("GC/1/drawText", payload);
                batch.add("GC/1/drawText", payload);
                comm.send(batch);
                batch.clear();
            };
        });
        String json = "\"" + "x".repeat(2 * TEXT_CHARS) + "\"";
        assertThat(client.lastEvent()).isEqualTo(MessageBatch.EVENT);
        assertThat(client.lastPayload()).isEqualTo("[[\"GC/1/drawText\"," + json + "],[\"GC/1/drawText\"," + json + "]]");
        assertThat(copies).as("payload copies per push, batch of owned payloads").isLessThan(1.5);
    }

    @Test
    void loneFrameInABatch_travelsAsItself() {
        MessageBatch batch = new MessageBatch();
        batch.add("Button/7", "{\"text\":\"ok\"}".getBytes(StandardCharsets.UTF_8));
        long before = client.received.get();
        comm.send(batch);
        client.awaitBytes(before + 1);
        assertThat(client.lastEvent()).isEqualTo("Button/7");
        assertThat(client.lastPayload()).isEqualTo("{\"text\":\"ok\"}");
    }

    private static Button button(Composite parent, int chars) {
        Button b = new Button(parent, SWT.PUSH);
        b.setText("y".repeat(chars));
        b.setData("dev.equo.swt.new", false);
        return b;
    }

    /** Extra bytes allocated per extra byte on the wire, between a push and the same push twice as large. */
    private double copiesPerWireByte(IntFunction<Runnable> pushOfSize) {
        long[] small = fewestAllocated(pushOfSize.apply(TEXT_CHARS));
        long[] large = fewestAllocated(pushOfSize.apply(2 * TEXT_CHARS));
        assertThat(large[1] - small[1]).as("the larger push put more on the wire").isGreaterThan(TEXT_CHARS / 2);
        double copies = (large[0] - small[0]) / (double) (large[1] - small[1]);
        System.out.printf("[push-copies] allocated=%d/%d wire=%d/%d copies=%.2f%n",
                small[0], large[0], small[1], large[1], copies);
        return copies;
    }

    /** The fewest bytes one run allocated on this thread, and the bytes that run put on the wire. */
    private long[] fewestAllocated(Runnable push) {
        for (int i = 0; i < WARMUP; i++) measure(push);
        long[] best = {Long.MAX_VALUE, 0};
        for (int i = 0; i < RUNS; i++) {
            long[] m = measure(push);
            if (m[0] < best[0]) best = m;
        }
        return best;
    }

    private long[] measure(Runnable push) {
        long receivedBefore = client.received.get();
        long before = THREADS.getCurrentThreadAllocatedBytes();
        push.run();
        long allocated = THREADS.getCurrentThreadAllocatedBytes() - before;
        long wire = client.awaitBytes(receivedBefore + 1) - receivedBefore;
        return new long[]{allocated, wire};
    }

    /** A bridge whose comm is a real socket rather than a recording, so the transport's copy counts. */
    private static final class WireBridge extends FlutterBridge {
        private final CommService comm;

        WireBridge(CommService comm) {
            this.comm = comm;
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }

    private static final class Client extends WebSocketClient {
        final AtomicLong received = new AtomicLong();
        private volatile byte[] last;

        Client(URI uri) {
            super(uri);
        }

        /** Waits until at least {@code atLeast} bytes have arrived and nothing more is on its way. */
        long awaitBytes(long atLeast) {
            long deadline = System.currentTimeMillis() + 5000;
            long seen = -1;
            while (System.currentTimeMillis() < deadline) {
                long now = received.get();
                if (now >= atLeast && now == seen) return now;
                seen = now;
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return received.get();
        }

        String lastEvent() {
            byte[] frame = last;
            int nameLength = ((frame[0] & 0xFF) << 8) | (frame[1] & 0xFF);
            return new String(frame, 2, nameLength, StandardCharsets.UTF_8);
        }

        String lastPayload() {
            byte[] frame = last;
            int nameLength = ((frame[0] & 0xFF) << 8) | (frame[1] & 0xFF);
            return new String(frame, 2 + nameLength, frame.length - 2 - nameLength, StandardCharsets.UTF_8);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
        }

        @Override
        public void onMessage(String message) {
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            byte[] frame = new byte[bytes.remaining()];
            bytes.get(frame);
            last = frame;
            received.addAndGet(frame.length);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
        }

        @Override
        public void onError(Exception ex) {
        }
    }
}
