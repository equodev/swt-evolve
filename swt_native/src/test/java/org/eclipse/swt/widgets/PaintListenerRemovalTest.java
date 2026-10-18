package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.MessageBatch;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A control that stops painting keeps showing its last painted frame on the client until a Paint
 * replaces it. Native SWT repaints the control's own content on {@code redraw()} whether or not
 * anyone listens for Paint, so an overlay drawn by a listener that was since removed disappears.
 *
 * Lives in org.eclipse.swt.widgets so the mocked display's package-private
 * {@code sendEvent(EventTable, Event)} can be wired to really dispatch.
 */
@ExtendWith(Mocks.class)
class PaintListenerRemovalTest {

    private RecordingBridge bridge;
    private final Deque<Runnable> asyncQueue = new ArrayDeque<>();
    private boolean dispatching;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        dispatching = false;
        asyncQueue.clear();
    }

    private Composite composite() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        dispatching = true;
        doAnswer(inv -> {
            Event ev = inv.getArgument(1);
            if (dispatching && ev != null && (ev.type == SWT.Paint || ev.type == SWT.Resize)) {
                return inv.callRealMethod();
            }
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setBounds(0, 0, 400, 300);
        pumpAsync();
        return composite;
    }

    private void pumpAsync() {
        Runnable r;
        while ((r = asyncQueue.poll()) != null) {
            r.run();
        }
    }

    private String redrawAndCollectGcTraffic(Control control) {
        bridge.comm.sent.clear();
        control.redraw();
        pumpAsync();
        return bridge.comm.sent.stream()
                .filter(f -> f.event.startsWith("GC/") || f.event.equals(MessageBatch.EVENT))
                .map(f -> f.event + f.json)
                .reduce("", String::concat);
    }

    @Test
    @DisplayName("redraw after the last paint listener is removed blanks what it painted")
    void redrawAfterRemovingTheListenerClearsTheOverlay() {
        Composite composite = composite();
        PaintListener overlay = e -> e.gc.fillRectangle(100, 100, 330, 20);
        composite.addPaintListener(overlay);
        assertThat(redrawAndCollectGcTraffic(composite)).as("the overlay's paint").contains("gcDispose");

        composite.removePaintListener(overlay);

        assertThat(redrawAndCollectGcTraffic(composite))
                .as("the repaint that replaces the overlay's last frame")
                .contains("gcDispose")
                .doesNotContain("fillRectangle");
    }

    @Test
    @DisplayName("a control that never painted still costs no paint on redraw")
    void redrawOfAControlThatNeverPaintedSendsNothing() {
        Composite composite = composite();

        assertThat(redrawAndCollectGcTraffic(composite)).as("GC traffic").isEmpty();
    }

    @Test
    @DisplayName("only the first redraw after the removal pays for a blank paint")
    void theBlankPaintIsSentOnce() {
        Composite composite = composite();
        PaintListener overlay = e -> e.gc.fillRectangle(0, 0, 10, 10);
        composite.addPaintListener(overlay);
        redrawAndCollectGcTraffic(composite);
        composite.removePaintListener(overlay);
        redrawAndCollectGcTraffic(composite);

        assertThat(redrawAndCollectGcTraffic(composite)).as("GC traffic of the next redraw").isEmpty();
    }
}
