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
 * As in native SWT, a {@code RIGHT | WRAP} topRight control too wide to fit beside the tabs moves to
 * a row of its own under them, leaving the tab row to the tabs, and the client area starts below it.
 */
@ExtendWith(Mocks.class)
class CTabFolderWrapTopRightTest {

    private static final int FOLDER_WIDTH = 344;

    private static final int FOLDER_HEIGHT = 400;

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

    private CTabFolder folder;

    private Rectangle topRightBounds(int alignment) {
        return topRightBounds(alignment, 90);
    }

    private Rectangle topRightBounds(int alignment, int tabWidth) {
        return topRightBounds(alignment, tabWidth, SWT.TOP);
    }

    private Rectangle topRightBounds(int alignment, int tabWidth, int tabPosition) {
        Shell shell = Mocks.shell();
        folder = new CTabFolder(shell, SWT.BORDER);
        folder.setTabPosition(tabPosition);
        folder.setBounds(0, 0, FOLDER_WIDTH, FOLDER_HEIGHT);
        for (String name : new String[]{"Inventory", "Search", "Color Bar", "Layers"}) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText(name);
            DartCTabItem impl = (DartCTabItem) item.getImpl();
            impl.showing = true;
            impl.width = tabWidth;
        }
        Composite topRight = new Composite(folder, SWT.NONE);
        topRight.setLayout(new FixedSizeLayout());
        folder.setTopRight(topRight, alignment);
        ((DartCTabFolder) folder.getImpl()).setButtonBounds();
        return topRight.getBounds();
    }

    @Test
    void aWrapControlThatDoesNotFitWrapsUnderTheTabRow() {
        // Four showing tabs of 90px leave no room for the control beside them.
        Rectangle bounds = topRightBounds(SWT.RIGHT | SWT.WRAP);

        assertThat(bounds.y).as("the control is on the row under the tab strip").isEqualTo(CTabFolderSizes.TAB_STRIP_HEIGHT);
        assertThat(bounds.width).isEqualTo(CONTROL_WIDTH);
        assertThat(bounds.x + bounds.width).as("right-aligned in the folder").isGreaterThan(FOLDER_WIDTH - 10);
    }

    @Test
    void theClientAreaStartsBelowTheWrappedRow() {
        Rectangle bounds = topRightBounds(SWT.RIGHT | SWT.WRAP);

        assertThat(folder.getClientArea().y).isGreaterThanOrEqualTo(bounds.y + bounds.height);
    }

    @Test
    void withTabsAtTheBottomTheControlWrapsAboveTheTabRow() {
        Rectangle bounds = topRightBounds(SWT.RIGHT | SWT.WRAP, 90, SWT.BOTTOM);

        assertThat(bounds.y + bounds.height).as("the control is on the row over the tab strip")
                .isEqualTo(FOLDER_HEIGHT - CTabFolderSizes.TAB_STRIP_HEIGHT);
        Rectangle clientArea = folder.getClientArea();
        assertThat(clientArea.y + clientArea.height).isLessThanOrEqualTo(bounds.y);
    }

    @Test
    void aWrapControlThatFitsStaysInTheTabRow() {
        // Four 10px tabs leave room for the control beside them.
        assertThat(topRightBounds(SWT.RIGHT | SWT.WRAP, 10)).isEqualTo(topRightBounds(SWT.RIGHT, 10));
    }
}
