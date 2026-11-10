package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.device;
import static org.mockito.Mockito.when;

/**
 * A workbench view whose toolbar holds a search field: the {@code RIGHT | WRAP} topRight
 * composite stays on the tab row when it fits beside the single tab, and wraps below it when not.
 */
@ExtendWith(Mocks.class)
class CTabFolderViewToolbarWithSearchFieldTest {

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
    void aViewToolbarWithASearchFieldThatFitsStaysOnTheTabRow() {
        CTabFolder folder = folderWithViewToolbar(500);

        Rectangle bounds = folder.getTopRight().getBounds();
        int tabHeight = folder.getTabHeight();
        assertThat(bounds.width).as("the toolbar has its packed width").isGreaterThan(0);
        assertThat(bounds.y).as("the toolbar sits on the tab row").isLessThan(tabHeight);
    }

    @Test
    void aViewToolbarTooWideForTheTabRowStillWraps() {
        CTabFolder folder = folderWithViewToolbar(200);

        Rectangle bounds = folder.getTopRight().getBounds();
        assertThat(bounds.y).as("the toolbar moves below the tab row").isGreaterThanOrEqualTo(folder.getTabHeight());
    }

    private static CTabFolder folderWithViewToolbar(int folderWidth) {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        Image icon = new Image(device(), 16, 16);
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setBounds(0, 0, folderWidth, 600);
        CTabItem item = new CTabItem(folder, SWT.CLOSE);
        item.setText("Solution Explorer");
        item.setImage(icon);
        folder.setSelection(0);

        Composite topRight = new Composite(folder, SWT.NONE);
        RowLayout rowLayout = new RowLayout();
        rowLayout.spacing = rowLayout.marginLeft = rowLayout.marginRight = rowLayout.marginTop = rowLayout.marginBottom = 0;
        topRight.setLayout(rowLayout);
        folder.setTopRight(topRight, SWT.RIGHT | SWT.WRAP);

        ToolBar viewToolbar = new ToolBar(topRight, SWT.FLAT | SWT.RIGHT);
        for (int i = 0; i < 3; i++) {
            new ToolItem(viewToolbar, SWT.PUSH).setImage(icon);
        }
        new ToolItem(viewToolbar, SWT.SEPARATOR);
        ToolItem filterItem = new ToolItem(viewToolbar, SWT.SEPARATOR);
        Text filter = new Text(viewToolbar, SWT.SEARCH | SWT.ICON_CANCEL);
        filter.setMessage("Filter");
        Point filterSize = filter.computeSize(100, SWT.DEFAULT, true);
        filterItem.setWidth(filterSize.x);
        filter.setSize(filterSize);
        filterItem.setControl(filter);
        new ToolItem(viewToolbar, SWT.SEPARATOR);
        for (int i = 0; i < 2; i++) {
            new ToolItem(viewToolbar, SWT.PUSH).setImage(icon);
        }
        ToolBar viewMenu = new ToolBar(topRight, SWT.FLAT | SWT.RIGHT);
        new ToolItem(viewMenu, SWT.PUSH).setImage(icon);
        topRight.pack();
        ((DartCTabFolder) folder.getImpl()).runUpdate();
        ((DartCTabFolder) folder.getImpl()).updateItems();
        folder.layout(true);
        return folder;
    }
}
