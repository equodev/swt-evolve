package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.custom.CTabFolderStripReports.reportsStrip;
import static org.mockito.Mockito.when;

/**
 * The workbench opens a folder's chevron list at the bottom-right corner of the last tab whose
 * {@code isShowing()} is true, read from {@code getBounds()}. With the tab scroll off the render side
 * draws the selected tab and then the tabs from the left that fit, next to a chevron as wide as it
 * draws it, so the list lands under the chevron only when this side lays the tabs and the chevron
 * out where they are drawn.
 */
@ExtendWith(Mocks.class)
class CTabFolderChevronListAnchorTest {

    /** A view stack as the workbench builds it, the last view selected. */
    private static final String[] LABELS = {"Problems", "Target Platform State", "Notification", "Notification", "Notification"};
    private static final int SELECTED = 4;
    private static final int FOLDER_WIDTH = 270;
    /** The render side's chevron: its 14-px icon with 2 px on each side. */
    private static final int DRAWN_CHEVRON_WIDTH = 18;
    /** Upstream's {@code CTabFolder.SPACING}, between the last showing tab and the chevron. */
    private static final int CHEVRON_GAP = 3;
    /** The part of the strip the tabs are drawn in. */
    private static final int SHOWN_STRIP_WIDTH = 193;
    /**
     * The strip as it is drawn in a 270-px view stack: the first three tabs and the selected one side
     * by side, ending at 193, and the one that does not fit parked past them.
     */
    private static final int[] DRAWN_TABS = {0, 42, 42, 43, 85, 44, 216, 44, 129, 64};

    /** A folder whose strip fits three of its six tabs. */
    private static final int MRU_FOLDER_WIDTH = 260;

    private boolean tabScroll;
    private boolean chevronAfterTabs;

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
        tabScroll = Config.getConfigFlags().ctabfolder_tab_scroll;
        Config.getConfigFlags().ctabfolder_tab_scroll = false;
        chevronAfterTabs = Config.getConfigFlags().ctabfolder_chevron_after_tabs;
        Config.getConfigFlags().ctabfolder_chevron_after_tabs = true;
    }

    @AfterEach
    void tearDown() {
        Config.getConfigFlags().ctabfolder_tab_scroll = tabScroll;
        Config.getConfigFlags().ctabfolder_chevron_after_tabs = chevronAfterTabs;
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("without ctabfolder_chevron_after_tabs, the strip report leaves the tabs where Java laid them out")
    void withoutTheFlagTheReportLeavesTheLayout() {
        Config.getConfigFlags().ctabfolder_chevron_after_tabs = false;
        CTabFolder folder = viewStack();
        Rectangle[] before = new Rectangle[folder.getItemCount()];
        for (int i = 0; i < before.length; i++) before[i] = folder.getItem(i).getBounds();

        reportsDrawnStrip(folder);

        for (int i = 0; i < before.length; i++) {
            assertThat(folder.getItem(i).getBounds()).as(folder.getItem(i).getText()).isEqualTo(before[i]);
        }
        ToolBar chevron = ((DartCTabFolder) folder.getImpl())._chevronTb();
        assertThat(chevron.getBounds().width).isNotEqualTo(DRAWN_CHEVRON_WIDTH);
    }

    @Test
    @DisplayName("a showing tab is laid out where the strip draws it, so the list opens at the drawn last tab")
    void aShowingTabIsLaidOutWhereItIsDrawn() {
        CTabFolder folder = viewStack();
        reportsDrawnStrip(folder);

        CTabItem lastShowing = null;
        for (int i = 0; i < folder.getItemCount(); i++) {
            CTabItem item = folder.getItem(i);
            if (!item.isShowing()) continue;
            lastShowing = item;
            assertThat(item.getBounds())
                    .as("%s is drawn at x %d, width %d", item.getText(), DRAWN_TABS[2 * i], DRAWN_TABS[2 * i + 1])
                    .extracting(b -> b.x, b -> b.width)
                    .containsExactly(DRAWN_TABS[2 * i], DRAWN_TABS[2 * i + 1]);
        }
        assertThat(lastShowing).as("the premise: the selected tab is drawn").isNotNull();
        Rectangle anchor = lastShowing.getBounds();
        assertThat(anchor.x + anchor.width)
                .as("the list's anchor, the right edge of %s", lastShowing.getText())
                .isEqualTo(DRAWN_TABS[2 * SELECTED] + DRAWN_TABS[2 * SELECTED + 1]);
    }

    @Test
    @DisplayName("the chevron is laid out as wide as it is drawn")
    void theChevronIsAsWideAsItIsDrawn() {
        CTabFolder folder = viewStack();
        reportsDrawnStrip(folder);

        ToolBar chevron = ((DartCTabFolder) folder.getImpl())._chevronTb();
        assertThat(chevron).as("the premise: the tabs overflow, so there is a chevron").isNotNull();
        assertThat(chevron.getVisible()).as("the premise: the chevron shows").isTrue();
        assertThat(chevron.getBounds().width).isEqualTo(DRAWN_CHEVRON_WIDTH);
    }

    @Test
    @DisplayName("with the tab scroll on, the chevron is laid out as wide as it is drawn there too")
    void theChevronIsAsWideAsItIsDrawnWithTabScroll() {
        Config.getConfigFlags().ctabfolder_tab_scroll = true;
        CTabFolder folder = viewStack();

        ToolBar chevron = ((DartCTabFolder) folder.getImpl())._chevronTb();
        assertThat(chevron).as("the premise: the tabs overflow, so there is a chevron").isNotNull();
        assertThat(chevron.getVisible()).as("the premise: the chevron shows").isTrue();
        assertThat(chevron.getBounds().width).isEqualTo(DRAWN_CHEVRON_WIDTH);
    }

    @Test
    @DisplayName("the chevron sits right after the last drawn tab, as upstream places it, so the list opens under it")
    void theChevronSitsRightAfterTheLastShowingTab() {
        CTabFolder folder = viewStack();
        reportsDrawnStrip(folder);

        ToolBar chevron = ((DartCTabFolder) folder.getImpl())._chevronTb();
        assertThat(chevron).as("the premise: the tabs overflow, so there is a chevron").isNotNull();
        int lastShowingRight = DRAWN_TABS[2 * SELECTED] + DRAWN_TABS[2 * SELECTED + 1];
        assertThat(chevron.getBounds().x).isEqualTo(lastShowingRight + CHEVRON_GAP);
    }

    @Test
    @DisplayName("with MRU on, closing the selected tab selects the tab used before it, even when that one is not drawn")
    void mruCloseSelectsTheTabUsedBefore() {
        CTabFolder folder = mruFolder();
        folder.setSelection(5);
        folder.setSelection(4);
        reportsMruStrip(folder, 0, 1, 4);

        folder.getItem(4).dispose();

        // Native SWT 3.135 on cocoa, same folder: Tab 5.
        assertThat(folder.getSelection().getText()).isEqualTo("Tab 5");
    }

    @Test
    @DisplayName("with MRU on, closing a tab picked from the list selects the tab used before it")
    void mruCloseOfAListedTabSelectsTheTabUsedBefore() {
        CTabFolder folder = mruFolder();
        folder.setSelection(5);
        folder.setSelection(4);
        reportsMruStrip(folder, 0, 1, 4);
        folder.getItem(4).dispose();
        reportsMruStrip(folder, 0, 1, 4);
        CTabItem listed = folder.getItem(2);
        assertThat(listed.isShowing()).as("the premise: Tab 2 is in the chevron's list").isFalse();

        folder.setSelection(listed);
        reportsMruStrip(folder, 0, 1, 2);
        listed.dispose();

        // Native SWT 3.135 on cocoa, same folder: Tab 5.
        assertThat(folder.getSelection().getText()).isEqualTo("Tab 5");
    }

    @Test
    @DisplayName("before the strip is reported, the tabs keep the layout this side gave them")
    void withNoReportTheTabsKeepTheirLayout() {
        CTabFolder folder = viewStack();
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        Rectangle before = folder.getItem(0).getBounds();

        assertThat(CTabFolderHelper.followDrawnStrip(impl)).isFalse();
        assertThat(folder.getItem(0).getBounds()).isEqualTo(before);
    }

    @Test
    @DisplayName("a tab the reported strip does not reach keeps the layout this side gave it")
    void aTabPastTheReportKeepsItsLayout() {
        CTabFolder folder = viewStack();
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        DartCTabItem last = (DartCTabItem) folder.getItem(LABELS.length - 1).getImpl();
        int[] laidOut = {last.x, last.width};
        int seq = impl.stripLayoutSeq;
        int[] firstFour = Arrays.copyOf(DRAWN_TABS, DRAWN_TABS.length - 2);
        StringBuilder tabs = new StringBuilder();
        for (int value : firstFour) {
            if (tabs.length() > 0) tabs.append(',');
            tabs.append(value);
        }
        reportsStrip(folder, tabs + ";;0," + SHOWN_STRIP_WIDTH + ";1,1,1,1;" + FOLDER_WIDTH + ";" + seq);

        CTabFolderHelper.followDrawnStrip(impl);

        assertThat(new int[] {last.x, last.width}).isEqualTo(laidOut);
    }

    @Test
    @DisplayName("a tab already where the strip draws it is not laid out again")
    void aTabWhereItIsDrawnIsLeftAlone() {
        CTabFolder folder = viewStack();
        reportsDrawnStrip(folder);
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        CTabFolderHelper.followDrawnStrip(impl);

        assertThat(CTabFolderHelper.followDrawnStrip(impl)).isFalse();
    }

    @Test
    @DisplayName("a showing tab laid out wider than it is drawn is put back to its drawn width")
    void aShowingTabTakesItsDrawnWidth() {
        CTabFolder folder = viewStack();
        reportsDrawnStrip(folder);
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        CTabFolderHelper.followDrawnStrip(impl);
        DartCTabItem first = (DartCTabItem) folder.getItem(0).getImpl();
        first.width += 5;

        assertThat(CTabFolderHelper.followDrawnStrip(impl)).isTrue();
        assertThat(first.width).isEqualTo(DRAWN_TABS[1]);
    }

    @Test
    @DisplayName("a tab marked showing that the strip does not draw is marked hidden")
    void aTabTheStripDoesNotDrawIsHidden() {
        CTabFolder folder = viewStack();
        reportsDrawnStrip(folder);
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        CTabFolderHelper.followDrawnStrip(impl);
        DartCTabItem hidden = null;
        for (CTabItem item : folder.getItems()) {
            if (!item.isShowing()) hidden = (DartCTabItem) item.getImpl();
        }
        assertThat(hidden).as("the premise: a tab does not fit").isNotNull();
        hidden.showing = true;

        assertThat(CTabFolderHelper.followDrawnStrip(impl)).isTrue();
        assertThat(hidden.showing).isFalse();
    }

    @Test
    @DisplayName("only the folder's chevron is sized as the drawn chevron, not a bar on a shell or in the folder's corner")
    void onlyTheChevronIsTheChevronBar() {
        CTabFolder folder = viewStack();
        ToolBar corner = new ToolBar(folder, SWT.FLAT);
        folder.setTopRight(corner);
        ToolBar onShell = new ToolBar(folder.getShell(), SWT.FLAT);

        assertThat(CTabFolderHelper.isChevronBar(((DartCTabFolder) folder.getImpl())._chevronTb())).isTrue();
        assertThat(CTabFolderHelper.isChevronBar(corner)).isFalse();
        assertThat(CTabFolderHelper.isChevronBar(onShell)).isFalse();
    }

    /** Reports the strip with the given tabs drawn side by side, 64 px each, and the rest past it. */
    private void reportsMruStrip(CTabFolder folder, int... drawn) {
        StringBuilder tabs = new StringBuilder();
        StringBuilder shortened = new StringBuilder();
        int x = 0;
        int parked = MRU_FOLDER_WIDTH;
        for (int i = 0; i < folder.getItemCount(); i++) {
            int index = i;
            boolean isDrawn = Arrays.stream(drawn).anyMatch(d -> d == index);
            if (i > 0) {
                tabs.append(',');
                shortened.append(',');
            }
            tabs.append(isDrawn ? x : parked).append(",64");
            shortened.append('0');
            if (isDrawn) x += 64;
            else parked += 64;
        }
        int seq = ((DartCTabFolder) folder.getImpl()).stripLayoutSeq;
        reportsStrip(folder, tabs + ";;0," + x + ";" + shortened + ";" + MRU_FOLDER_WIDTH + ";" + seq);
    }

    private CTabFolder mruFolder() {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER | SWT.MULTI | SWT.FLAT);
        folder.setMRUVisible(true);
        for (int i = 0; i < 6; i++) new CTabItem(folder, SWT.CLOSE).setText("Tab " + i);
        resize(folder, MRU_FOLDER_WIDTH);
        return folder;
    }

    private void reportsDrawnStrip(CTabFolder folder) {
        StringBuilder tabs = new StringBuilder();
        for (int value : DRAWN_TABS) {
            if (tabs.length() > 0) tabs.append(',');
            tabs.append(value);
        }
        int seq = ((DartCTabFolder) folder.getImpl()).stripLayoutSeq;
        reportsStrip(folder, tabs + ";;0," + SHOWN_STRIP_WIDTH + ";1,1,1,1,0;" + FOLDER_WIDTH + ";" + seq);
    }

    private void resize(CTabFolder folder, int width) {
        folder.setBounds(0, 0, width, 442);
        ((DartCTabFolder) folder.getImpl()).onResize(new Event());
        // Lays out whatever the resize left pending, so a strip report sent next is for this layout.
        folder.getItem(new Point(0, 0));
    }

    private CTabFolder viewStack() {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER | SWT.MULTI | SWT.FLAT);
        folder.setMinimumCharacters(1);
        folder.setMinimizeVisible(true);
        folder.setMaximizeVisible(true);
        for (String label : LABELS) {
            CTabItem item = new CTabItem(folder, SWT.CLOSE);
            item.setText(label);
            item.setImage(new Image(shell.getDisplay(), new ImageData(16, 16, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF))));
        }
        folder.setSelection(SELECTED);
        resize(folder, FOLDER_WIDTH);
        return folder;
    }
}
