package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.FocusTrackingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A press on a close button is no press on the tab: natively it only arms the button, so it neither
 * selects the tab nor focuses the folder. The render side draws the close buttons where its own
 * layout puts them, which is not this side's {@code closeRect}, so the press has to be tested against
 * the buttons as they are drawn.
 */
@Tag("flutter-it")
class CTabFolderDrawnCloseButtonPressFlutterTest {

    private FocusTrackingBridge bridge;
    private Display display;
    private CTabFolder folder;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        bridge = new FocusTrackingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        Shell shell = new Shell(display);
        shell.open();
        folder = new CTabFolder(shell, SWT.CLOSE);
        for (String text : new String[] {"One", "Second tab", "Three"}) new CTabItem(folder, SWT.NONE).setText(text);
        folder.setSelection(1);
        folder.setBounds(0, 0, 300, 100);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a press on the selected tab's drawn close button does not focus the folder")
    void pressOnDrawnCloseButtonTakesNoFocus() {
        Rectangle tab = folder.getItem(1).getBounds();
        Rectangle laidOut = ((DartCTabItem) folder.getItem(1).getImpl()).closeRect;
        // Drawn near the tab's left end, clear of where this side laid the button out.
        Rectangle drawn = new Rectangle(tab.x + 2, tab.y + 2, 10, 10);
        assertThat(laidOut.intersects(drawn)).as("the premise: drawn apart from this side's closeRect").isFalse();
        reportCloseButton(1, drawn);

        pressAt(drawn);

        assertThat(folder.isFocusControl())
                .as("natively a press on the close button only arms it; a press on the selected tab would focus the folder")
                .isFalse();
    }

    @Test
    @DisplayName("a drag that starts on the drawn close button selects its tab without focusing the folder")
    void dragFromDrawnCloseButtonTakesNoFocus() {
        Rectangle tab = folder.getItem(1).getBounds();
        Rectangle drawn = new Rectangle(tab.x + 2, tab.y + 2, 10, 10);
        reportCloseButton(1, drawn);
        pressAt(drawn);

        // The render side selects the tab a drag starts from, as a native press would.
        String channel = FlutterBridge.event((DartWidget) folder.getImpl(), "Selection", "Selection");
        Event selection = new Event();
        selection.index = 1;
        selection.detail = SWT.DRAG;
        bridge.comm.fireContaining(channel, selection);
        while (display.readAndDispatch()) {
            // drain queued asyncExec runnables
        }

        assertThat(folder.isFocusControl()).as("natively a drag moves no focus").isFalse();
    }

    private void pressAt(Rectangle r) {
        Event down = new Event();
        down.button = 1;
        down.x = r.x + r.width / 2;
        down.y = r.y + r.height / 2;
        folder.notifyListeners(SWT.MouseDown, down);
    }

    /** What the render side reports: every tab where this side has it, and tab {@code index}'s close button at {@code button}. */
    private void reportCloseButton(int index, Rectangle button) {
        StringBuilder strip = new StringBuilder();
        StringBuilder close = new StringBuilder();
        for (int i = 0; i < folder.getItemCount(); i++) {
            Rectangle b = folder.getItem(i).getBounds();
            if (i > 0) { strip.append(','); close.append(','); }
            strip.append(b.x).append(',').append(b.width);
            close.append(i == index ? button.x + "," + button.y + "," + button.width + "," + button.height : "0,0,0,0");
        }
        Event e = new Event();
        e.text = strip + ";" + close;
        CTabFolderHelper.handleStripLaidOut((DartCTabFolder) folder.getImpl(), e);
    }
}
