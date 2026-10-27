package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.FocusTrackingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Clicking a control in a part that is not the active one activates that part, and a workbench
 * reacts to the activation by focusing the part's default control (e4 calls the view's setFocus()).
 * Natively the activation comes first, from the press itself, and the click then moves the focus to
 * the control under the pointer, so the clicked control ends up holding it. A search field above a
 * tree is the common case: the view focuses its tree, the click focuses the field.
 */
@Tag("flutter-it")
class ClickFocusAfterPartActivationFlutterTest {

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
    @DisplayName("clicking a field in an inactive part leaves the focus in the field, not in the part's default control")
    void clickedFieldKeepsFocusOverThePartsDefaultControl() {
        Shell shell = new Shell(display);
        Composite editorPart = new Composite(shell, SWT.NONE);
        Canvas editor = new Canvas(editorPart, SWT.NONE);
        Composite viewPart = new Composite(shell, SWT.NONE);
        Text search = new Text(viewPart, SWT.SINGLE | SWT.SEARCH);
        Tree tree = new Tree(viewPart, SWT.NONE);
        shell.open();
        assertThat(editor.forceFocus()).isTrue();

        // What the workbench does when the view's part becomes active: focus its default control.
        boolean[] viewActive = {false};
        viewPart.addListener(SWT.Activate, e -> {
            if (viewActive[0]) return;
            viewActive[0] = true;
            tree.setFocus();
        });

        reportFocusIn(search);

        assertThat(viewActive[0]).as("the click activates the view's part").isTrue();
        assertThat(bridge.hasFocus((DartControl) search.getImpl()))
                .as("the clicked field holds the focus, as natively, not the tree the part activation focused")
                .isTrue();
    }

    @Test
    @DisplayName("pressing a field that still holds the client's focus takes Java's focus back to it")
    void pressOnAFieldJavaFocusedAwayFromRefocusesIt() {
        Shell shell = new Shell(display);
        Text search = new Text(shell, SWT.SINGLE | SWT.SEARCH);
        Tree tree = new Tree(shell, SWT.NONE);
        shell.open();
        reportFocusIn(search);
        // A part activation focuses its default control; the client's focus stays in the field.
        tree.setFocus();
        drain();

        Event press = new Event();
        press.button = 1;
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) search.getImpl(), "Mouse", "MouseDown"), press);
        drain();

        assertThat(bridge.hasFocus((DartControl) search.getImpl()))
                .as("a press focuses a field natively, whoever Java last gave the focus to")
                .isTrue();
    }

    /** The focus the client reports when a press focuses a field. */
    private void reportFocusIn(Control control) {
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) control.getImpl(), "Focus", "FocusIn"), new Event());
        drain();
    }

    private void drain() {
        while (display.readAndDispatch()) {
            // drain queued asyncExec runnables
        }
    }
}
