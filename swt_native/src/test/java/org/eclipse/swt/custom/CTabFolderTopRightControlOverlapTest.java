package org.eclipse.swt.custom;

import dev.equo.swt.Config;
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
 * A top-right control with {@code SWT.WRAP} wraps below the tabs rather than being drawn over the
 * last one. The tabs leave no room for a wrapped control, so whether it fits on the tab row is
 * decided from what is left beside them, and that has to include the gap its own placement keeps
 * from the folder's right edge.
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

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @ParameterizedTest(name = "minimize and maximize shown: {0}")
    @ValueSource(booleans = {false, true})
    @DisplayName("a wrapped top-right control never overlaps a tab, at any folder width")
    void aWrappedTopRightControlNeverOverlapsATab(boolean minMax) {
        List<String> overlaps = new ArrayList<>();
        int tabRowRows = 0;
        for (int width = 260; width <= 560; width++) {
            CTabFolder folder = folderWithTopRight(width, SWT.RIGHT | SWT.WRAP, minMax);
            Rectangle control = folder.getTopRight().getBounds();
            for (CTabItem item : folder.getItems()) {
                if (item.isShowing() && item.getBounds().intersects(control)) {
                    overlaps.add("at " + width + ": " + control + " over " + item.getText() + item.getBounds());
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
