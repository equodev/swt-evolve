package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.size.CTabFolderSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** SWT.CLOSE on the folder puts a close button on every tab, and a tab leaves room for it. */
@ExtendWith(Mocks.class)
class CTabFolderCloseStyleTest {

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

    private int widthOfTheTab(int folderStyle, int tabStyle) {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, folderStyle);
        folder.setBounds(0, 0, 400, 200);
        CTabItem item = new CTabItem(folder, tabStyle);
        item.setText("a tab");
        return item.getBounds().width;
    }

    @Test
    @DisplayName("a tab of a folder with SWT.CLOSE is as wide as one that asks for the button itself")
    void theFoldersCloseStyleReachesTheTabs() {
        int plain = widthOfTheTab(SWT.NONE, SWT.NONE);

        assertThat(widthOfTheTab(SWT.CLOSE, SWT.NONE) - plain).isEqualTo(CTabFolderSizes.TAB_CLOSE_EXTRA);
        assertThat(widthOfTheTab(SWT.NONE, SWT.CLOSE)).isEqualTo(widthOfTheTab(SWT.CLOSE, SWT.NONE));
    }
}
