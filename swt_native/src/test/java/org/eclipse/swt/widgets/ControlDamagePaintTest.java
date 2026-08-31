package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Java owns when a control repaints. Every case but the first paint after mount has to reach the
 * application's PaintListener from the Java side alone, so that the render side never has to ask
 * for one: an explicit redraw() on any control (not only a Composite), a size change, and a change
 * to the state a GC created on the control inherits.
 *
 * Lives in org.eclipse.swt.widgets so the mocked display's package-private
 * {@code sendEvent(EventTable, Event)} can be wired to really dispatch; paint delivery is
 * coalesced through {@code asyncExec}, which the test captures and pumps.
 */
// The chain under test (ControlHelper.markDamaged/paint/firePaint and the generated redraw and
// setter bodies) is shared code, identical on every backend; one platform pins it.
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
@ExtendWith(Mocks.class)
class ControlDamagePaintTest {

    private final Deque<Runnable> asyncQueue = new ArrayDeque<>();
    private Shell shell;

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
        FlutterBridge.set(new RecordingBridge());
        shell = Mocks.swtShell();
        Display display = shell.getDisplay();
        SwtDisplay displayImpl = (SwtDisplay) display.getImpl();
        doAnswer(inv -> {
            Event ev = inv.getArgument(1);
            return ev != null && ev.type == SWT.Paint ? inv.callRealMethod() : null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        asyncQueue.clear();
    }

    private void pumpAsync() {
        Runnable r;
        while ((r = asyncQueue.poll()) != null) {
            r.run();
        }
    }

    /** A control sized and settled, so a later count only sees the event under test. */
    private Label label() {
        Label label = new Label(shell, SWT.NONE);
        label.setBounds(0, 0, 200, 40);
        pumpAsync();
        return label;
    }

    private AtomicInteger countPaints(Control control) {
        AtomicInteger paints = new AtomicInteger();
        control.addPaintListener(e -> paints.incrementAndGet());
        return paints;
    }

    @Test
    @DisplayName("redraw() delivers a Paint to a control that is not a Composite")
    void redrawOnPlainControlDeliversPaint() {
        Label label = label();
        AtomicInteger paints = countPaints(label);
        label.redraw();
        pumpAsync();
        assertThat(paints.get()).as("Paint events after redraw()").isPositive();
    }

    @Test
    @DisplayName("redraw(x,y,w,h,all) delivers a Paint to a control that is not a Composite")
    void rectRedrawOnPlainControlDeliversPaint() {
        Label label = label();
        AtomicInteger paints = countPaints(label);
        label.redraw(0, 0, 10, 10, false);
        pumpAsync();
        assertThat(paints.get()).as("Paint events after redraw(x,y,w,h,all)").isPositive();
    }

    @Test
    @DisplayName("a new size delivers a Paint")
    void resizeDeliversPaint() {
        Label label = label();
        AtomicInteger paints = countPaints(label);
        label.setBounds(0, 0, 300, 80);
        pumpAsync();
        assertThat(paints.get()).as("Paint events after a size change").isPositive();
    }

    @Test
    @DisplayName("a move alone delivers no Paint")
    void moveDeliversNoPaint() {
        Label label = label();
        AtomicInteger paints = countPaints(label);
        label.setLocation(30, 30);
        pumpAsync();
        assertThat(paints.get()).as("Paint events after a move").isZero();
    }

    @Test
    @DisplayName("a new background delivers a Paint")
    void backgroundChangeDeliversPaint() {
        Label label = label();
        AtomicInteger paints = countPaints(label);
        label.setBackground(new Color(shell.getDisplay(), 10, 20, 30));
        pumpAsync();
        assertThat(paints.get()).as("Paint events after a background change").isPositive();
    }

    @Test
    @DisplayName("a new foreground delivers a Paint")
    void foregroundChangeDeliversPaint() {
        Label label = label();
        AtomicInteger paints = countPaints(label);
        label.setForeground(new Color(shell.getDisplay(), 200, 100, 0));
        pumpAsync();
        assertThat(paints.get()).as("Paint events after a foreground change").isPositive();
    }

    // Applications batch bulk updates between setRedraw(false) and setRedraw(true), and
    // ControlHelper.paint() discards damage while drawCount > 0. Without a Paint when drawing is
    // turned back on, everything drawn during the batch is never painted.
    @Test
    @DisplayName("turning drawing back on delivers a Paint for what was batched")
    void setRedrawTrueDeliversPaint() {
        Label label = label();
        label.setRedraw(false);
        AtomicInteger paints = countPaints(label);
        label.redraw();
        pumpAsync();
        assertThat(paints.get()).as("Paint events while drawing is off").isZero();

        label.setRedraw(true);
        pumpAsync();
        assertThat(paints.get()).as("Paint events after drawing is turned back on").isPositive();
    }

    // Only the outermost setRedraw(true) ends the batch; an inner one still leaves drawing off.
    @Test
    @DisplayName("a nested setRedraw(true) does not end the batch")
    void nestedSetRedrawDeliversNoPaint() {
        Label label = label();
        label.setRedraw(false);
        label.setRedraw(false);
        AtomicInteger paints = countPaints(label);
        label.setRedraw(true);
        pumpAsync();
        assertThat(paints.get()).as("Paint events after an inner setRedraw(true)").isZero();
    }

    // Guards the counter's arithmetic end to end, now that queueing a paint no longer touches it.
    @Test
    @DisplayName("a paint queued across a setRedraw batch leaves the control still repainting")
    void queuedPaintLeavesTheControlRepainting() {
        Label label = label();
        AtomicInteger paints = countPaints(label);
        label.redraw();
        label.setRedraw(false);
        pumpAsync();
        label.setRedraw(true);
        pumpAsync();

        int afterBatch = paints.get();
        label.redraw();
        pumpAsync();
        assertThat(paints.get()).as("Paint events after a later redraw()").isGreaterThan(afterBatch);
    }

    // drawCount is SWT's setRedraw counter, and it was also the "a paint is already queued" latch.
    // Queueing therefore made the control look like drawing was suppressed to everything else, and
    // the queued paint went on to run after the application had actually turned drawing off --
    // native SWT paints nothing while redraw is suppressed.
    @Test
    @DisplayName("a paint queued before drawing is turned off does not run during the batch")
    void queuedPaintDoesNotRunWhileSuppressed() {
        Label label = label();
        AtomicInteger paints = countPaints(label);
        label.redraw();
        label.setRedraw(false);
        pumpAsync();
        assertThat(paints.get()).as("Paint events while drawing is off").isZero();
    }

    // The E4 CSS engine re-applies the same colors on every relayout pass, so an idempotent
    // re-apply must not count as damage.
    @Test
    @DisplayName("re-applying the same background delivers no further Paint")
    void unchangedBackgroundDeliversNoPaint() {
        Label label = label();
        Color color = new Color(shell.getDisplay(), 10, 20, 30);
        label.setBackground(color);
        pumpAsync();
        AtomicInteger paints = countPaints(label);
        label.setBackground(color);
        pumpAsync();
        assertThat(paints.get()).as("Paint events after re-applying the same background").isZero();
    }
}
