package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.FocusTrackingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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

    @Test
    @DisplayName("clicking an unselected tab selects it and leaves the focus where it was")
    void clickOnUnselectedTabTakesNoFocus() {
        withToolBarsLaidOut();
        clickTab(1);

        assertThat(folder.getSelectionIndex()).isEqualTo(1);
        assertThat(focusedInFolder())
                .as("a native click on an unselected tab only selects it: nothing in the folder is focused")
                .isNull();
    }

    @Test
    @DisplayName("clicking the selected tab of a folder without a page focuses the folder, not its toolbar")
    void clickOnSelectedTabWithoutPageFocusesTheFolder() {
        withToolBarsLaidOut();
        clickTab(1);

        clickTab(1);

        assertThat(focusedInFolder())
                .as("upstream forces the focus onto the folder itself; setFocus would hand it to the min/max ToolBar")
                .isSameAs(folder);
    }

    @Test
    @DisplayName("the second press of a double click on a tab focuses the folder before the first one's relayout has run")
    void secondPressFocusesTheFolderAheadOfTheDeferredRelayout() {
        withToolBarsLaidOut();
        while (display.readAndDispatch()) {
            // the initial layout
        }

        Rectangle tab = folder.getItem(1).getBounds();
        press(tab);
        press(tab);

        assertThat(focusedInFolder())
                .as("natively the strip relayout the first press deferred runs before the second press, which then focuses the folder")
                .isSameAs(folder);
    }

    /**
     * A primary press at the centre of {@code tab}, without running anything the press defers. The
     * bounds are read beforehand: reading them runs the pending layout.
     */
    private void press(Rectangle tab) {
        Event down = new Event();
        down.button = 1;
        down.x = tab.x + tab.width / 2;
        down.y = tab.y + tab.height / 2;
        folder.notifyListeners(SWT.MouseDown, down);
    }

    /**
     * The min/max ToolBar a setFocus could fall to, and the folder laid out, so upstream's own press
     * handling finds the tab under the press as it does on screen.
     */
    private void withToolBarsLaidOut() {
        folder.setMinimizeVisible(true);
        folder.setMaximizeVisible(true);
        folder.setBounds(0, 0, 300, 100);
    }

    /** The folder, or the control inside it, that holds the focus; null when none does. */
    private Control focusedInFolder() {
        if (folder.isFocusControl()) return folder;
        for (Control child : folder.getChildren()) {
            if (child.isFocusControl()) return child;
        }
        return null;
    }

    /**
     * A click as the render side reports it: the press, which reaches upstream's own mouse handling
     * first, then the folder's Dart→Java Selection handler; drains the asyncExec it hops through.
     */
    private void clickTab(int index) {
        Rectangle tab = folder.getItem(index).getBounds();
        Event down = new Event();
        down.button = 1;
        down.x = tab.x + tab.width / 2;
        down.y = tab.y + tab.height / 2;
        folder.notifyListeners(SWT.MouseDown, down);
        String channel = FlutterBridge.event((DartWidget) folder.getImpl(), "Selection", "Selection");
        Event e = new Event();
        e.index = index;
        bridge.comm.fireContaining(channel, e);
        while (display.readAndDispatch()) {
            // drain queued asyncExec runnables
        }
    }
}
