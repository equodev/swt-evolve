package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A control taking focus announces its activation up the parent chain so an embedding workbench can
 * pick the active part (there is no OS focus behind this backend). The render side reports focus on
 * every click that takes it — Java can move the focus holder on its own, so a later click may be the
 * only signal the control is focused again. That report is idempotent: re-reporting focus we already
 * hold must announce activation again <em>only</em> when the control did not already hold it.
 *
 * Regression: the activation walk used to run on every FocusIn report, so clicking around inside an
 * already-focused tree re-activated the owning part each time, flashing the selection.
 */
@Tag("flutter-it")
class ControlRefocusActivationFlutterTest {

    /** Tracks the focus holder like the whole-tree DisplayBridge does; the base RecordingBridge doesn't. */
    static final class FocusTrackingBridge extends RecordingBridge {
        private DartControl focused;

        @Override
        public boolean setFocus(DartControl control) {
            focused = control;
            return true;
        }

        @Override
        public boolean hasFocus(DartControl control) {
            return control == focused;
        }

        @Override
        public void clearFocus(DartControl control) {
            if (focused == control)
                focused = null;
        }
    }

    private FocusTrackingBridge bridge;
    private Display display;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        bridge = new FocusTrackingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("re-reporting focus already held does not re-announce activation up the chain")
    void refocusWhileAlreadyFocusedDoesNotReactivateAncestors() {
        Shell shell = new Shell(display);
        Composite parent = new Composite(shell, SWT.NONE);
        Tree tree = new Tree(parent, SWT.NONE);

        int[] parentActivations = {0};
        parent.addListener(SWT.Activate, e -> parentActivations[0]++);

        // First report: the tree genuinely takes focus, so the part is activated up the chain.
        reportFocusIn(tree);
        int afterAcquire = parentActivations[0];
        assertThat(afterAcquire)
                .as("a genuine focus acquisition announces activation up the parent chain")
                .isPositive();

        // Second report while the tree still holds focus (a later click on another row): no change.
        reportFocusIn(tree);
        assertThat(parentActivations[0])
                .as("re-reporting focus already held must not re-activate the owning part (selection flash)")
                .isEqualTo(afterAcquire);
    }

    @Test
    @DisplayName("a control that focuses itself announces activation, without waiting for a client report")
    void forceFocusAnnouncesActivationUpTheChain() {
        Shell shell = new Shell(display);
        Composite part = new Composite(shell, SWT.NONE);
        Canvas canvas = new Canvas(part, SWT.NONE);
        shell.open();

        int[] partActivations = {0};
        part.addListener(SWT.Activate, e -> partActivations[0]++);

        // What a custom-drawn grid does from its own mouse-down handler: focus itself. There is no
        // OS focus behind this backend, so nothing else announces it.
        assertThat(canvas.forceFocus()).isTrue();

        assertThat(partActivations[0])
                .as("taking the focus announces activation up the chain, so the workbench "
                        + "re-activates the owning part")
                .isPositive();

        // Already the focus holder: forceFocus() returns early, so nothing is announced twice.
        int afterAcquire = partActivations[0];
        assertThat(canvas.forceFocus()).isTrue();
        assertThat(partActivations[0]).isEqualTo(afterAcquire);
    }

    /** Fire the tree's own Dart→Java FocusIn handler and drain the asyncExec it hops through. */
    private void reportFocusIn(Tree tree) {
        String channel = FlutterBridge.event((DartWidget) tree.getImpl(), "Focus", "FocusIn");
        bridge.comm.fireContaining(channel, new Event());
        while (display.readAndDispatch()) {
            // drain queued asyncExec runnables
        }
    }
}
