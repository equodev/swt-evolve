package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.size.CTabFolderSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A {@code RIGHT | WRAP} topRight control too wide to fit beside the tabs is still drawn in the tab
 * row, so that is where the folder places it: a menu opened under one of its items with
 * {@code toDisplay} then appears under the item.
 */
@ExtendWith(Mocks.class)
class CTabFolderWrapTopRightInlineTest {

    private static final int FOLDER_WIDTH = 344;

    private static final int CONTROL_WIDTH = 268;

    static class FixedSizeLayout extends Layout {
        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean changed) {
            return new Point(CONTROL_WIDTH, 22);
        }

        @Override
        protected void layout(Composite composite, boolean changed) {
        }
    }

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

    private Rectangle topRightBounds(int alignment) {
        Shell shell = Mocks.shell();
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setBounds(0, 0, FOLDER_WIDTH, 400);
        for (String name : new String[]{"Inventory", "Search", "Color Bar", "Layers"}) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText(name);
            // Four showing tabs of 90px leave no room for the control beside them.
            DartCTabItem impl = (DartCTabItem) item.getImpl();
            impl.showing = true;
            impl.width = 90;
        }
        Composite topRight = new Composite(folder, SWT.NONE);
        topRight.setLayout(new FixedSizeLayout());
        folder.setTopRight(topRight, alignment);
        ((DartCTabFolder) folder.getImpl()).setButtonBounds();
        return topRight.getBounds();
    }

    @Test
    void aWrapControlThatDoesNotFitStaysInTheTabRow() {
        Rectangle bounds = topRightBounds(SWT.RIGHT | SWT.WRAP);

        assertThat(bounds.y + bounds.height)
                .as("the control sits inside the tab strip, not on a row under it")
                .isLessThanOrEqualTo(CTabFolderSizes.TAB_STRIP_HEIGHT);
        assertThat(bounds.width).isEqualTo(CONTROL_WIDTH);
    }

    @Test
    void itIsPlacedWhereANonWrappingControlWouldBe() {
        assertThat(topRightBounds(SWT.RIGHT | SWT.WRAP)).isEqualTo(topRightBounds(SWT.RIGHT));
    }
}
