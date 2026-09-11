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
 * Which stack has focus is carried by CTabFolder's {@code highlight} flag, which upstream toggles
 * on Activate/Deactivate and reads back through {@code shouldHighlight()}. It has to reach the
 * render side and be pushed when it changes: without it every open stack paints its selected tab
 * identically and the user cannot tell where keyboard input will land.
 */
@Tag("flutter-it")
class CTabFolderActiveHighlightFlutterTest {

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
    private CTabFolder left;
    private final java.util.Map<CTabFolder, Composite> contents = new java.util.HashMap<>();
    private CTabFolder right;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        bridge = new FocusTrackingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        shell = new Shell(display);
        shell.open();
        left = stack("left");
        right = stack("right");
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private CTabFolder stack(String label) {
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText(label);
        Composite content = new Composite(folder, SWT.NONE);
        item.setControl(content);
        folder.setSelection(0);
        contents.put(folder, content);
        return folder;
    }

    @Test
    @DisplayName("only the folder that holds focus reports itself as highlighted")
    void oneStackAtATimeIsHighlighted() {
        clickTab(left);

        assertThat(highlightOf(left))
                .as("the stack the user just clicked into is the active one")
                .isTrue();
        assertThat(highlightOf(right))
                .as("a stack that never had focus must not look active")
                .isFalse();

        clickTab(right);

        assertThat(highlightOf(right)).isTrue();
        assertThat(highlightOf(left))
                .as("Deactivate has to clear the flag, or both stacks look active")
                .isFalse();
    }

    /**
     * Focus moving between two stacks without touching a tab -- the user clicking inside a view.
     * Nothing else about either folder changes, so the new activation reaches the render side only
     * if activation itself pushes; upstream just redraws.
     */
    @Test
    @DisplayName("a change of active stack is pushed to the render side")
    void theChangeReachesTheWire() {
        focusInside(left);
        bridge.comm.sent.clear();

        focusInside(right);

        assertThat(lastPushOf(right))
                .as("the stack that gained focus has to announce it; wire was %s", bridge.comm.sent)
                .contains("\"highlight\":true");
        assertThat(lastPushOf(left))
                .as("the stack that lost focus has to be re-sent without the flag")
                .isNotEmpty()
                .doesNotContain("\"highlight\":true");
    }

    private boolean highlightOf(CTabFolder folder) {
        return ((DartCTabFolder) folder.getImpl()).getValue().getHighlight();
    }

    /** The last state this folder put on the wire, as JSON. */
    private String lastPushOf(CTabFolder folder) {
        String event = FlutterBridge.event((DartWidget) folder.getImpl());
        return bridge.comm.sent.stream()
                .filter(f -> f.event.equals(event))
                .map(f -> f.json)
                .reduce((first, second) -> second)
                .orElse("");
    }

    /**
     * What the render side reports for a click on a tab: the folder's Selection, then FocusIn on
     * the tab's content — the same two messages, in the same order, a real click produces.
     */
    private void clickTab(CTabFolder folder) {
        fire(FlutterBridge.event((DartWidget) folder.getImpl(), "Selection", "Selection"), 0);
        fire(FlutterBridge.event((DartWidget) contents.get(folder).getImpl(), "Focus", "FocusIn"), 0);
    }

    /** Focus lands on the stack's content, the way clicking anywhere inside the view reports it. */
    private void focusInside(CTabFolder folder) {
        fire(FlutterBridge.event((DartWidget) contents.get(folder).getImpl(), "Focus", "FocusIn"), 0);
    }

    /** Deliver one Dart→Java message and drain the asyncExec its handler hops through. */
    private void fire(String channel, int index) {
        Event e = new Event();
        e.index = index;
        bridge.comm.fireContaining(channel, e);
        while (display.readAndDispatch()) {
            // drain queued asyncExec runnables
        }
    }
}
