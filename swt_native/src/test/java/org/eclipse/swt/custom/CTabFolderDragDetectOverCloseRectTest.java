package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A drag that starts on a close button is not a drag, as upstream decides it, but against the close
 * buttons as the render side draws them. This side's own close rectangles come from a layout that
 * does not match the drawing: in a narrow folder one sits under the middle of a tab drawn without it.
 */
@ExtendWith(Mocks.class)
class CTabFolderDragDetectOverCloseRectTest {

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
    @DisplayName("a DragDetect where only this side's layout puts a close button is not cancelled")
    void thisSidesCloseRectDoesNotCancel() {
        CTabFolder folder = folderWithJavaCloseRectAt105();
        drawsStrip(folder, "0,212;0,0,0,0");

        assertThat(dragDetectAt(folder, 107, 16)).isEqualTo(SWT.DragDetect);
    }

    @Test
    @DisplayName("a DragDetect on a drawn close button is cancelled")
    void aDrawnCloseButtonCancels() {
        CTabFolder folder = folderWithJavaCloseRectAt105();
        drawsStrip(folder, "0,212;190,6,18,18");

        assertThat(dragDetectAt(folder, 198, 14))
                .as("upstream's rule: a drag that starts on a close button is not a drag")
                .isEqualTo(SWT.None);
        assertThat(dragDetectAt(folder, 107, 16)).isEqualTo(SWT.DragDetect);
    }

    @Test
    @DisplayName("the close buttons in the report leave the strip the tabs are found by unchanged")
    void theStripStillFindsTheTab() {
        CTabFolder folder = folderWithJavaCloseRectAt105();
        folder.setBounds(0, 0, 254, 300);
        drawsStrip(folder, "0,212;190,6,18,18");

        assertThat(folder.getItem(new Point(150, folder.getTabHeight() / 2))).isSameAs(folder.getItem(0));
    }

    /** As the VM trace showed it: this side's layout put the selected tab's close button at 105. */
    private CTabFolder folderWithJavaCloseRectAt105() {
        CTabFolder folder = new CTabFolder(Mocks.shell(), SWT.CLOSE);
        CTabItem item = new CTabItem(folder, SWT.CLOSE);
        Rectangle close = ((DartCTabItem) item.getImpl()).closeRect;
        close.x = 105;
        close.y = 4;
        close.width = close.height = 18;
        return folder;
    }

    private void drawsStrip(CTabFolder folder, String report) {
        Event e = new Event();
        e.text = report;
        CTabFolderHelper.handleStripLaidOut((DartCTabFolder) folder.getImpl(), e);
    }

    private int dragDetectAt(CTabFolder folder, int x, int y) {
        Event drag = new Event();
        drag.type = SWT.DragDetect;
        drag.x = x;
        drag.y = y;
        ((DartCTabFolder) folder.getImpl()).onDragDetect(drag);
        return drag.type;
    }
}
