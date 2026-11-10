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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.custom.CTabFolderStripReports.reportsStrip;
import static org.mockito.Mockito.when;

/**
 * The workbench opens a folder's chevron list under the bottom-right corner of the last tab whose
 * {@code isShowing()} is true, taken from {@code getBounds()}. A tab that is showing must therefore
 * be laid out where it is drawn, inside the folder.
 */
@ExtendWith(Mocks.class)
class CTabFolderShowingTabBoundsTest {

    /** A view stack as the workbench builds it: four closable views with icons, the third one selected. */
    private static final String[] LABELS = {"Inventory", "Sessions", "Color Bar", "Legend"};
    private static final int SELECTED = 2;
    private static final int FOLDER_WIDTH = 379;
    /** The four tabs drawn shortened, side by side, left of the chevron. */
    private static final String DRAWN_TABS = "0,80,80,78,158,87,245,77";
    /** The part of the strip left of the chevron. */
    private static final String SHOWN_STRIP = "0,326";

    private boolean tabScroll;

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
    }

    @AfterEach
    void tearDown() {
        Config.getConfigFlags().ctabfolder_tab_scroll = tabScroll;
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a tab drawn inside the strip, left of the chevron, is laid out inside the folder")
    void aShowingTabIsLaidOutInsideTheFolder() {
        Config.getConfigFlags().ctabfolder_tab_scroll = false;
        CTabFolder folder = viewStack();
        int seq = ((DartCTabFolder) folder.getImpl()).stripLayoutSeq;
        reportsStrip(folder, DRAWN_TABS + ";;" + SHOWN_STRIP + ";1,1,1,1;" + FOLDER_WIDTH + ";" + seq);

        for (CTabItem item : folder.getItems()) {
            assertThat(item.isShowing()).as("the premise: %s is drawn inside the strip", item.getText()).isTrue();
            Rectangle bounds = item.getBounds();
            assertThat(bounds.x + bounds.width)
                    .as("%s's right edge, bounds %s", item.getText(), bounds)
                    .isBetween(1, FOLDER_WIDTH);
        }
    }

    @Test
    @DisplayName("a view stack shrunk after a widen shows the chevron again")
    void shrinkingAfterAWidenBringsTheChevronBack() {
        Config.getConfigFlags().ctabfolder_tab_scroll = false;
        CTabFolder folder = viewStack();

        resize(folder, 782);
        assertThat(chevron(folder)).as("the premise: wide, no chevron").isFalse();
        assertThat(showing(folder)).as("the premise: wide, every tab showing").containsOnly(true);

        resize(folder, 120);
        assertThat(chevron(folder)).isTrue();
        assertThat(showing(folder)).contains(false);
    }

    private void resize(CTabFolder folder, int width) {
        folder.setBounds(0, 0, width, 850);
        ((DartCTabFolder) folder.getImpl()).onResize(new Event());
        // Lays out whatever the resize left pending, so a strip report sent next is for this layout.
        folder.getItem(new Point(0, 0));
    }

    private boolean chevron(CTabFolder folder) {
        return ((DartCTabFolder) folder.getImpl())._showChevron();
    }

    private List<Boolean> showing(CTabFolder folder) {
        List<Boolean> showing = new ArrayList<>();
        for (CTabItem item : folder.getItems()) {
            showing.add(item.isShowing());
        }
        return showing;
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
