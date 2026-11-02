package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.device;
import static org.mockito.Mockito.when;

/**
 * A workbench view stack: the selected view's toolbar and the view menu sit in a
 * {@code RIGHT | WRAP} topRight composite, filled after the folder was first laid out. When the
 * tabs leave no room beside them, the composite moves to a row of its own under the tab row.
 */
@ExtendWith(Mocks.class)
class CTabFolderWrapViewToolbarTest {

    private static final String[] VIEWS = {"Outline", "Properties", "Assistant", "Favorites"};

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
    void aViewToolbarTooWideForTheTabRowWrapsUnderIt() {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setBounds(0, 0, 300, 400);
        for (String view : VIEWS) {
            new CTabItem(folder, SWT.CLOSE).setText(view);
        }
        folder.setSelection(1);

        Composite topRight = new Composite(folder, SWT.NONE);
        RowLayout rowLayout = new RowLayout();
        rowLayout.spacing = rowLayout.marginLeft = rowLayout.marginRight = rowLayout.marginTop = rowLayout.marginBottom = 0;
        topRight.setLayout(rowLayout);
        folder.setTopRight(topRight, SWT.RIGHT | SWT.WRAP);
        folder.layout(true);

        Image icon = new Image(device(), 16, 16);
        ToolBar viewToolbar = new ToolBar(topRight, SWT.FLAT | SWT.RIGHT);
        for (int i = 0; i < 5; i++) {
            new ToolItem(viewToolbar, SWT.PUSH).setImage(icon);
        }
        ToolBar viewMenu = new ToolBar(topRight, SWT.FLAT | SWT.RIGHT);
        viewMenu.setLayoutData(new RowData());
        new ToolItem(viewMenu, SWT.PUSH).setImage(icon);
        topRight.pack();
        ((DartCTabFolder) folder.getImpl()).runUpdate();
        ((DartCTabFolder) folder.getImpl()).updateItems();
        folder.layout(true);

        Rectangle bounds = topRight.getBounds();
        int tabHeight = folder.getTabHeight();
        assertThat(bounds.width).as("the toolbar has its packed width").isGreaterThan(0);
        assertThat(bounds.y).as("the toolbar is on a row under the tab row").isGreaterThanOrEqualTo(tabHeight);
        assertThat(folder.getClientArea().y).isGreaterThanOrEqualTo(bounds.y + bounds.height);
    }
}
