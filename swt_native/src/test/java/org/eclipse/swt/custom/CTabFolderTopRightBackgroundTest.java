package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
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
 * CTabFolder hands its header controls (topRight, the min/max bar) the folder's background, so
 * they read as part of the tab row. A folder with no background of its own hands them none: the
 * system default getBackground() answers is not a colour the application chose, and as an explicit
 * one it would be painted over the themed tab row.
 */
@ExtendWith(Mocks.class)
class CTabFolderTopRightBackgroundTest {

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
    @DisplayName("a folder with no background gives its topRight control, its tool bar and the min/max bar none")
    void noFolderBackgroundGivesTheHeaderControlsNone() {
        CTabFolder folder = folder();
        Composite topRight = topRight(folder);
        ToolBar bar = (ToolBar) topRight.getChildren()[0];

        assertThat(explicitBackground(topRight)).isNull();
        assertThat(explicitBackground(bar)).isNull();
        assertThat(explicitBackground(minMaxBar(folder))).isNull();
        assertThat(topRight.getBackground()).isEqualTo(folder.getBackground());
    }

    @Test
    @DisplayName("a folder's own background still reaches its topRight control and the min/max bar")
    void folderBackgroundReachesTheTopRightControl() {
        CTabFolder folder = folder();
        Color blue = new Color(folder.getDisplay(), 0x20, 0x40, 0xC0);
        folder.setBackground(blue);
        Composite topRight = topRight(folder);

        assertThat(explicitBackground(topRight)).isEqualTo(blue);
        assertThat(explicitBackground(topRight.getChildren()[0])).isEqualTo(blue);
        assertThat(explicitBackground(minMaxBar(folder))).isEqualTo(blue);
    }

    private static ToolBar minMaxBar(CTabFolder folder) {
        ToolBar minMaxTb = ((DartCTabFolder) folder.getImpl()).minMaxTb;
        assertThat(minMaxTb).isNotNull();
        return minMaxTb;
    }

    private static Color explicitBackground(Control control) {
        return ((DartControl) control.getImpl()).getExplicitBackground();
    }

    private static CTabFolder folder() {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setBounds(0, 0, 500, 300);
        new CTabItem(folder, SWT.CLOSE).setText("Item");
        folder.setSelection(0);
        folder.setMinimizeVisible(true);
        folder.setMaximizeVisible(true);
        return folder;
    }

    private static Composite topRight(CTabFolder folder) {
        Composite topRight = new Composite(folder, SWT.NONE);
        ToolBar bar = new ToolBar(topRight, SWT.FLAT | SWT.RIGHT | SWT.WRAP);
        new ToolItem(bar, SWT.PUSH).setText("T");
        folder.setTopRight(topRight, SWT.RIGHT | SWT.WRAP);
        // As the folder's own hit test does: runs the layout this side has pending.
        folder.getItem(new Point(0, 0));
        return topRight;
    }
}
