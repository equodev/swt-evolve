package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Issue #509 — Java-side half: a Selection event arriving from Flutter must move the sash.
 *
 * {@code DartSash.checkStyle} forces {@code SWT.SMOOTH}, i.e. it claims the macOS smooth-sash
 * contract — and in native SWT that contract includes the sash applying its own move:
 * {@code SwtSash.mouseDragged}/{@code mouseUp} call {@code setBounds(event.x, event.y, ...)}
 * whenever the app's listeners leave {@code event.doit} true. Apps therefore do NOT have to
 * reposition a smooth sash themselves, and a recorder view doesn't. {@code DartSash}'s
 * Selection hook only forwards the event to listeners and never applies the move, so the sash
 * (and everything laid out around it) stays put until a window resize forces a fresh layout —
 * exactly the reported symptom.
 *
 * Uses a {@link RecordingBridge} whose {@code RecordingComm} captures the {@code comm().on(...)}
 * handler DartSash registers, so the Dart→Java Selection can be fired directly (same seam as
 * {@code DisplayWakeFlutterTest}). The mocked Display's {@code sendEvent} is stubbed to deliver
 * to the widget's EventTable and {@code asyncExec} to run inline, so the hook's dispatch hop
 * completes synchronously.
 */
@ExtendWith(Mocks.class)
class SashSelectionMovesSashTest {

    private RecordingBridge bridge;

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
    }

    private Sash sash() {
        Shell shell = Mocks.swtShell();
        Display display = shell.getDisplay();
        SwtDisplay displayImpl = (SwtDisplay) display.getImpl();
        // Deliver events to the widget's listeners, like the real display does.
        doAnswer(inv -> {
            EventTable table = inv.getArgument(0);
            Event event = inv.getArgument(1);
            if (table != null)
                table.sendEvent(event);
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        // The Selection hook hops through asyncExec; run it inline.
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));

        Composite parent = new Composite(shell, SWT.NONE);
        parent.setBounds(0, 0, 390, 260);
        Sash sash = new Sash(parent, SWT.VERTICAL);
        sash.setBounds(195, 0, 10, 260);
        return sash;
    }

    private void fireSelectionFromFlutter(int x, int y, int width, int height) {
        Event e = new Event();
        e.x = x;
        e.y = y;
        e.width = width;
        e.height = height;
        bridge.comm.fireContaining("/Selection/Selection", e);
    }

    @Test
    @DisplayName("a Selection from Flutter moves the sash when no listener vetoes (issue #509)")
    void selectionAppliesTheMove() {
        Sash sash = sash();

        fireSelectionFromFlutter(255, 0, 10, 260);

        assertThat(sash.getBounds().x)
                .as("a SMOOTH sash applies its own move on Selection (SwtSash.mouseDragged/" +
                        "mouseUp parity) — without it the sash never repositions until a window " +
                        "resize forces a layout (issue #509)")
                .isEqualTo(255);
    }

    @Test
    @DisplayName("a listener setting doit=false vetoes the move")
    void vetoedSelectionDoesNotMove() {
        Sash sash = sash();
        sash.addListener(SWT.Selection, e -> e.doit = false);

        fireSelectionFromFlutter(255, 0, 10, 260);

        assertThat(sash.getBounds().x)
                .as("event.doit=false must veto the move, like native SWT")
                .isEqualTo(195);
    }
}
