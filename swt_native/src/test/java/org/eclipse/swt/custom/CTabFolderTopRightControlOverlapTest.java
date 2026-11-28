package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * A top-right control with {@code SWT.WRAP} wraps below the tabs rather than being drawn over them.
 * Shown only on hover, it may cover up to two thirds of the last tab, which stays readable, and
 * hovering any tab draws the tabs over it; it covers no other tab. Whether it fits on the tab row is decided from what is left beside the tabs, and
 * that has to include the gap its own placement keeps from the folder's right edge.
 */
@ExtendWith(Mocks.class)
class CTabFolderTopRightControlOverlapTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    private ConfigFlags savedFlags;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        savedFlags = Config.getConfigFlags();
        // Shown only on hover: the mode in which a control may cover part of the last tab.
        ConfigFlags flags = savedFlags.copy();
        flags.ctabfolder_topright_auto_hide = true;
        Config.setConfigFlags(flags);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        Config.setConfigFlags(savedFlags);
    }

    @ParameterizedTest(name = "minimize and maximize shown: {0}")
    @ValueSource(booleans = {false, true})
    @DisplayName("a wrap top-right control covers no tab but up to two thirds of the last one, at any folder width")
    void aWrappedTopRightControlNeverOverlapsATab(boolean minMax) {
        List<String> overlaps = new ArrayList<>();
        int tabRowRows = 0;
        for (int width = 260; width <= 560; width++) {
            CTabFolder folder = folderWithTopRight(width, SWT.RIGHT | SWT.WRAP, minMax);
            Rectangle control = folder.getTopRight().getBounds();
            CTabItem last = lastShowing(folder);
            for (CTabItem item : folder.getItems()) {
                if (!item.isShowing() || !item.getBounds().intersects(control)) continue;
                Rectangle tab = item.getBounds();
                int covered = tab.x + tab.width - control.x;
                if (item != last || covered > tab.width * 2 / 3) {
                    overlaps.add("at " + width + ": " + control + " over " + item.getText() + tab);
                }
            }
            if (control.y <= folder.getTabHeight() - control.height) tabRowRows++;
        }

        assertThat(overlaps).isEmpty();
        assertThat(tabRowRows).as("sanity: the widths swept cover both rows the control can take").isPositive();
    }

    @Test
    @DisplayName("a top-right control still shares the tab row when there is room for it beside the tabs")
    void aTopRightControlStillSharesTheTabRow() {
        CTabFolder folder = folderWithTopRight(560, SWT.RIGHT | SWT.WRAP, false);
        Rectangle control = folder.getTopRight().getBounds();

        assertThat(control.y).as("drawn on the tab row, not wrapped below it").isLessThan(folder.getTabHeight());
        assertThat(control.x + control.width).isLessThanOrEqualTo(folder.getSize().x);
    }

    private static CTabItem lastShowing(CTabFolder folder) {
        CTabItem last = null;
        for (CTabItem item : folder.getItems()) if (item.isShowing()) last = item;
        return last;
    }

    private CTabFolder folderWithTopRight(int width, int style, boolean minMax) {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.CLOSE);
        folder.setBounds(0, 0, width, 300);
        Label topRight = new Label(folder, SWT.BORDER);
        topRight.setText("TopRight");
        for (int i = 1; i <= 5; i++) new CTabItem(folder, SWT.NONE).setText("Item" + i);
        folder.setSelection(0);
        folder.setMinimizeVisible(minMax);
        folder.setMaximizeVisible(minMax);
        folder.setTopRight(topRight, style);
        // As the folder's own hit test does: runs the layout this side has pending.
        folder.getItem(new Point(0, 0));
        return folder;
    }
}
