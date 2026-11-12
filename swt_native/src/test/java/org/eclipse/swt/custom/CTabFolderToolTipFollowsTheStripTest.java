package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * The folder's tooltip, which it sets from the tab under the pointer as the pointer moves, follows the
 * tabs and close buttons as the render side draws them. Taken from this side's own layout, an editor
 * tab's tooltip read "Close" over the tab's name, and its neighbour's path over its right half.
 */
@ExtendWith(Mocks.class)
class CTabFolderToolTipFollowsTheStripTest {

    /** Two tabs as a running workbench reported them: 134 and 209 wide, a close button on each. */
    private static final String STRIP = "0,134,134,209;112,8,16,17,321,8,16,17;0,882";

    private static final int Y = 13;

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
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("over a tab's name, where this side's layout puts its close button, the tooltip is the tab's")
    void theNameShowsTheTabsToolTip() {
        CTabFolder folder = twoEditorTabs();
        drawsStrip(folder, STRIP);

        assertThat(toolTipAt(folder, 70)).isEqualTo("src/Test123.java");
    }

    @Test
    @DisplayName("over the right half of a tab, the tooltip is that tab's, not the next one's")
    void theRightHalfIsStillTheSameTab() {
        CTabFolder folder = twoEditorTabs();
        drawsStrip(folder, STRIP);

        assertThat(toolTipAt(folder, 100)).isEqualTo("src/Test123.java");
        assertThat(toolTipAt(folder, 200)).isEqualTo("src/InventoryManager.java");
    }

    @Test
    @DisplayName("over a drawn close button, the tooltip is Close")
    void aDrawnCloseButtonSaysClose() {
        CTabFolder folder = twoEditorTabs();
        drawsStrip(folder, STRIP);

        assertThat(toolTipAt(folder, 120)).isEqualTo(SWT.getMessage("SWT_Close"));
        assertThat(toolTipAt(folder, 328)).isEqualTo(SWT.getMessage("SWT_Close"));
    }

    @Test
    @DisplayName("above or below a drawn close button, the tooltip is the tab's")
    void besideACloseButtonIsTheTab() {
        CTabFolder folder = twoEditorTabs();
        // The first tab's close button drawn 10 high, from 8 to 17.
        drawsStrip(folder, "0,134,134,209;112,8,16,10,321,8,16,17;0,882");

        assertThat(((DartCTabFolder) folder.getImpl())._getToolTip(120, 4)).isEqualTo("src/Test123.java");
        assertThat(((DartCTabFolder) folder.getImpl())._getToolTip(120, 20)).isEqualTo("src/Test123.java");
    }

    @Test
    @DisplayName("where the strip draws a tab without its close button, the tooltip is the tab's")
    void anUndrawnCloseButtonIsTheTab() {
        CTabFolder folder = twoEditorTabs();
        drawsStrip(folder, "0,134,134,209;112,8,0,0,321,8,16,17;0,882");

        assertThat(toolTipAt(folder, 120)).isEqualTo("src/Test123.java");
    }

    private CTabFolder twoEditorTabs() {
        Shell shell = Mocks.shell();
        // Laying out the tabs parks hidden ones past the display's width.
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.CLOSE);
        folder.setBounds(0, 0, 882, 600);
        CTabItem first = new CTabItem(folder, SWT.CLOSE);
        first.setToolTipText("src/Test123.java");
        CTabItem second = new CTabItem(folder, SWT.CLOSE);
        second.setToolTipText("src/InventoryManager.java");
        // Where this side's own layout put the first tab's close button: under the name as drawn.
        ((DartCTabFolder) folder.getImpl()).runUpdate();
        Rectangle close = ((DartCTabItem) first.getImpl()).closeRect;
        close.x = 60;
        close.y = 4;
        close.width = close.height = 18;
        return folder;
    }

    private void drawsStrip(CTabFolder folder, String report) {
        Event e = new Event();
        e.text = report;
        CTabFolderHelper.handleStripLaidOut((DartCTabFolder) folder.getImpl(), e);
    }

    private String toolTipAt(CTabFolder folder, int x) {
        return ((DartCTabFolder) folder.getImpl())._getToolTip(x, Y);
    }
}
