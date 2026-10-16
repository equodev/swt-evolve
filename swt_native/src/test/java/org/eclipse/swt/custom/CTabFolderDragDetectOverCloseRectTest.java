package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
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
 * A DragDetect on a tab reaches the application even where this side's own layout puts a close
 * button.
 *
 * <p>The render side draws the tabs and their close buttons, and does not raise a DragDetect for a
 * drag that starts on one. This side's layout does not match the drawing: in a narrow folder it
 * shrinks the selected tab and moves its close button under the middle of the tab as drawn, which
 * is the point a DragDetect reports. Cancelling there left the workbench unable to move the view.
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
        CTabFolder folder = narrowFolder();
        // Where this side put the selected tab's close button in a 254-wide workbench stack.
        Rectangle close = ((DartCTabItem) folder.getItem(0).getImpl()).closeRect;
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

    private CTabFolder narrowFolder() {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.CLOSE);
        folder.setBounds(0, 0, 254, 300);
        for (String label : new String[]{"Stratigraphic Modeling", "Facies Trend Modeling", "Variogram Modeling"}) {
            new CTabItem(folder, SWT.CLOSE).setText(label);
        }
        folder.setSelection(0);
        return folder;
    }
}
