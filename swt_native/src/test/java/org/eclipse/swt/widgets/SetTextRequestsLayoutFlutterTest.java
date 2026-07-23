package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code setText} on a text-sized control must re-lay-out its enclosing composite so a label whose
 * text is set after construction does not keep stale bounds — the "Start typing to search…" hint
 * that rendered at height 4 in "Find Actions" (#818).
 *
 * <p>The relayout is <em>scheduled</em> (runs on the next event-loop turn via {@code asyncExec}),
 * NOT {@code requestLayout()}. {@code requestLayout()} routes through {@code layout(control,
 * SWT.DEFER)}, which calls {@code setLayoutDeferred(true)} — raising the shell's {@code layoutCount}
 * that only {@code runDeferredLayouts()} (inside {@code readAndDispatch}) lowers again. Code that
 * never pumps the loop (e.g. the upstream {@code test_setVisibility_and_sizing}) then leaves the
 * shell permanently layout-suspended, so every later {@code layout()} is a no-op and {@code
 * getSize()} reads {@code 0x0}. Scheduling with {@code asyncExec} gets the same "relayout on the
 * next turn" without suspending the shell.
 *
 * <p>This test asserts both halves: (1) {@code setText} does NOT suspend the shell, and (2) after
 * pumping the loop the scheduled relayout actually sizes the composite — without the test laying it
 * out itself. Runs on the native backend (where the transform lives) via {@code nativeTest}.
 */
@Tag("flutter-it")
class SetTextRequestsLayoutFlutterTest {

    private RecordingBridge bridge;
    private Display display;

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
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    /** A 500x500 shell with a bordered, FillLayout composite — deliberately NOT laid out yet. */
    private Composite borderedComposite(Shell shell) {
        shell.setSize(500, 500);
        shell.setLayout(new FillLayout());
        Composite composite = new Composite(shell, SWT.BORDER);
        composite.setLayout(new FillLayout());
        return composite;
    }

    /**
     * {@code setText} must (1) not suspend the shell, and (2) have scheduled a relayout that, once
     * the loop is pumped, sizes the composite. The test itself never calls {@code layout()}, so a
     * non-zero size proves {@code setText} scheduled the relayout. With the old {@code
     * requestLayout()} the shell is left layout-deferred (assertion 1 fails); with no relayout at
     * all the composite stays {@code 0x0} (assertion 2 fails).
     */
    private void assertSchedulesRelayoutWithoutDeferring(Shell shell, Composite composite) {
        assertThat(shell.isLayoutDeferred())
                .as("setText must not leave the shell layout-deferred")
                .isFalse();
        // Drain the async queue, as a live event loop would, so the scheduled relayout runs.
        for (int i = 0; i < 100 && display.readAndDispatch(); i++) {
            // keep pumping until idle
        }
        Point size = composite.getSize();
        assertThat(size.x).as("composite width after the scheduled relayout").isGreaterThan(100);
        assertThat(size.y).as("composite height after the scheduled relayout").isGreaterThan(100);
    }

    @Test
    void labelSetText_schedulesRelayoutWithoutDeferring() {
        Shell shell = new Shell(display);
        Composite composite = borderedComposite(shell);
        Label label = new Label(composite, SWT.NONE);
        label.setText("a considerably longer label");
        assertSchedulesRelayoutWithoutDeferring(shell, composite);
    }

    @Test
    void buttonSetText_schedulesRelayoutWithoutDeferring() {
        Shell shell = new Shell(display);
        Composite composite = borderedComposite(shell);
        Button button = new Button(composite, SWT.PUSH);
        button.setText("a considerably longer button label");
        assertSchedulesRelayoutWithoutDeferring(shell, composite);
    }

    @Test
    void linkSetText_schedulesRelayoutWithoutDeferring() {
        Shell shell = new Shell(display);
        Composite composite = borderedComposite(shell);
        Link link = new Link(composite, SWT.NONE);
        link.setText("a considerably longer <a>link</a> text");
        assertSchedulesRelayoutWithoutDeferring(shell, composite);
    }

    @Test
    void cLabelSetText_schedulesRelayoutWithoutDeferring() {
        Shell shell = new Shell(display);
        Composite composite = borderedComposite(shell);
        CLabel cLabel = new CLabel(composite, SWT.NONE);
        cLabel.setText("a considerably longer clabel");
        assertSchedulesRelayoutWithoutDeferring(shell, composite);
    }
}
