package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
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

/**
 * A height the application fixes with {@code setTabHeight} is the height the tabs are laid out at
 * (upstream adds one line for the top edge), not the render side's default.
 */
@ExtendWith(Mocks.class)
class CTabFolderTabHeightTest {

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

    private CTabFolder folder(int style) {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, style);
        folder.setBounds(0, 0, 300, 200);
        return folder;
    }

    private CTabItem tab(CTabFolder folder) {
        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText("a");
        return item;
    }

    @Test
    @DisplayName("a fixed tab height is the height of every tab, plus the line on top")
    void aFixedHeightIsTheHeightOfTheTabs() {
        CTabFolder folder = folder(SWT.NONE);
        folder.setTabHeight(20);
        CTabItem item = tab(folder);

        assertThat(item.getBounds().height).isEqualTo(21);
    }

    @Test
    @DisplayName("tabs at the bottom start that far above the bottom edge")
    void tabsAtTheBottomStartOneFixedHeightAboveTheEdge() {
        CTabFolder folder = folder(SWT.BOTTOM);
        folder.setTabHeight(20);
        CTabItem item = tab(folder);

        assertThat(item.getBounds().y).isEqualTo(200 - 21);
    }

    @Test
    @DisplayName("a fixed height of zero leaves the tabs no height")
    void aFixedHeightOfZeroLeavesNoHeight() {
        CTabFolder folder = folder(SWT.NONE);
        folder.setTabHeight(0);
        CTabItem item = tab(folder);

        assertThat(item.getBounds().height).isZero();
    }

    @Test
    @DisplayName("the default height comes back once the fixed one is cleared")
    void clearingTheFixedHeightGoesBackToTheDefault() {
        CTabFolder folder = folder(SWT.NONE);
        CTabItem item = tab(folder);
        int natural = item.getBounds().height;
        folder.setTabHeight(40);
        assertThat(item.getBounds().height).isEqualTo(41);

        folder.setTabHeight(SWT.DEFAULT);

        assertThat(item.getBounds().height).isEqualTo(natural);
    }
}
