package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
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
 * A DragDetect on a tab reaches the application even where this side's own layout puts a close
 * button: the render side, which draws the close buttons, withholds a DragDetect that starts on one.
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
    @DisplayName("a DragDetect inside this side's close button rectangle is not cancelled")
    void dragDetectOverTheCloseRectIsNotCancelled() {
        CTabFolder folder = new CTabFolder(Mocks.shell(), SWT.CLOSE);
        CTabItem item = new CTabItem(folder, SWT.CLOSE);
        Rectangle close = ((DartCTabItem) item.getImpl()).closeRect;
        close.x = 105;
        close.y = 4;
        close.width = close.height = 18;

        Event drag = new Event();
        drag.type = SWT.DragDetect;
        drag.x = 107;
        drag.y = 16;
        ((DartCTabFolder) folder.getImpl()).onDragDetect(drag);

        assertThat(drag.type)
                .as("left a DragDetect, so the listeners after the folder's own - the workbench's - get it")
                .isEqualTo(SWT.DragDetect);
    }
}
