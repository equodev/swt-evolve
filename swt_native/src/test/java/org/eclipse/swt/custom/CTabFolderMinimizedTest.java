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

/** A minimized folder leaves its page no room: the client area keeps its origin and has no size. */
@ExtendWith(Mocks.class)
class CTabFolderMinimizedTest {

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
        new CTabItem(folder, SWT.NONE).setText("a");
        return folder;
    }

    @Test
    @DisplayName("a minimized folder has an empty client area at the same origin")
    void aMinimizedFolderHasNoClientArea() {
        CTabFolder folder = folder(SWT.NONE);
        Rectangle area = folder.getClientArea();

        folder.setMinimized(true);

        assertThat(folder.getClientArea()).isEqualTo(new Rectangle(area.x, area.y, 0, 0));
    }

    @Test
    @DisplayName("tabs at the bottom: the same, at the same origin")
    void aMinimizedFolderWithTabsAtTheBottomHasNoClientArea() {
        CTabFolder folder = folder(SWT.BOTTOM);
        Rectangle area = folder.getClientArea();

        folder.setMinimized(true);

        assertThat(folder.getClientArea()).isEqualTo(new Rectangle(area.x, area.y, 0, 0));
    }

    @Test
    @DisplayName("restoring gives the page its area back")
    void restoringGivesTheAreaBack() {
        CTabFolder folder = folder(SWT.NONE);
        Rectangle area = folder.getClientArea();
        folder.setMinimized(true);

        folder.setMinimized(false);

        assertThat(folder.getClientArea()).isEqualTo(area);
    }
}
