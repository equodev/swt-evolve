package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A tab click reported by the render side has to carry the same information a native click does.
 * setSelection(int, boolean) notifies only when the index actually changes, so a click on the
 * already-selected tab would otherwise reach Java as nothing at all — and an embedding workbench
 * that keys part activation off the folder's focus and the click's MouseUp (e4's StackRenderer) then
 * leaves the part inactive, which silently empties every menu contribution resolved from it.
 */
@Tag("flutter-it")
class CTabFolderTabClickActivationFlutterTest {

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
    private Shell shell;
    private CTabFolder folder;
    private Composite content;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        bridge = new FocusTrackingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        shell = new Shell(display);
        shell.open();
        folder = new CTabFolder(shell, SWT.NONE);
        content = new Composite(folder, SWT.NONE);
        CTabItem first = new CTabItem(folder, SWT.NONE);
        first.setText("first");
        first.setControl(content);
        new CTabItem(folder, SWT.NONE).setText("second");
        folder.setSelection(0);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("clicking the already-selected tab focuses its content and announces activation")
    void clickOnSelectedTabStillActivatesItsContent() {
        int[] activations = {0};
        content.addListener(SWT.Activate, e -> activations[0]++);
        int[] selections = {0};
        folder.addListener(SWT.Selection, e -> selections[0]++);

        clickTab(0);

        assertThat(selections[0])
                .as("re-selecting the same tab must not fire SWT.Selection (native setSelection semantics)")
                .isZero();
        assertThat(activations[0])
                .as("a click on the selected tab must still announce the content's activation")
                .isOne();
        assertThat(content.isFocusControl())
                .as("a native click on the selected tab leaves that tab's content focused")
                .isTrue();
    }

    @Test
    @DisplayName("clicking the selected tab again does not re-announce activation")
    void repeatedClickOnSelectedTabIsIdempotent() {
        clickTab(0);
        int[] activations = {0};
        content.addListener(SWT.Activate, e -> activations[0]++);

        clickTab(0);

        assertThat(activations[0])
                .as("content that already holds focus must not be re-activated (selection flash)")
                .isZero();
    }

    @Test
    @DisplayName("clicking another tab still selects it and notifies")
    void clickOnOtherTabSelectsAndNotifies() {
        int[] selections = {0};
        folder.addListener(SWT.Selection, e -> selections[0]++);

        clickTab(1);

        assertThat(folder.getSelectionIndex()).isEqualTo(1);
        assertThat(selections[0])
                .as("a tab click that changes the selection still fires SWT.Selection")
                .isOne();
    }

    /** Fire the folder's own Dart→Java Selection handler and drain the asyncExec it hops through. */
    private void clickTab(int index) {
        String channel = FlutterBridge.event((DartWidget) folder.getImpl(), "Selection", "Selection");
        Event e = new Event();
        e.index = index;
        bridge.comm.fireContaining(channel, e);
        while (display.readAndDispatch()) {
            // drain queued asyncExec runnables
        }
    }
}
