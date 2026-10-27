package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A dialog is drawn by the Flutter state of the Shell it is parked on, and Flutter never builds a
 * Shell that is not visible. A dialog whose parent Shell was never opened, as in the JFace-style
 * {@code new ColorDialog(new Shell(parent)).open()}, therefore has to be parked on the nearest
 * Shell that is actually on screen, or it never appears and {@code open()} never returns.
 */
@Tag("native-unit")
class DialogHiddenParentNativeTest {

    /** Stands in for Flutter answering the dialog's close channel once it is shown. */
    private static void answerClose(Display display, String closePayload) {
        DisplayBridge bridge = mock(DisplayBridge.class);
        doAnswer(invocation -> {
            Consumer<String> handler = invocation.getArgument(1);
            handler.accept(closePayload);
            return null;
        }).when(bridge).onChannel(anyString(), any());
        ((DartDisplay) display.getImpl()).displayBridge = bridge;
    }

    private static Shell shell(Display display, Shell parent, boolean visible) {
        Shell shell = DartMocks.dartShell(display);
        when(shell.getParent()).thenReturn(parent);
        when(shell.getVisible()).thenReturn(visible);
        return shell;
    }

    @Test
    void colorDialogOnANeverOpenedChildShellIsShownOnTheVisibleAncestor() {
        Display display = DartMocks.dartDisplay();
        Shell window = shell(display, null, true);
        Shell unopened = shell(display, window, false);
        answerClose(display, String.valueOf(0x123456));

        ColorDialog dialog = new ColorDialog(unopened);
        dialog.setRGB(new RGB(255, 255, 255));
        RGB picked = dialog.open();

        assertThat(picked).isEqualTo(new RGB(0x12, 0x34, 0x56));
        verify((DartShell) window.getImpl()).addDialog((DartDialog) dialog.getImpl());
        verify((DartShell) window.getImpl()).removeDialog((DartDialog) dialog.getImpl());
        verify((DartShell) unopened.getImpl(), never()).addDialog(any());
    }

    @Test
    void dialogOnANeverOpenedTopLevelShellIsShownOnTheActiveShell() {
        Display display = DartMocks.dartDisplay();
        Shell window = shell(display, null, true);
        Shell unopened = shell(display, null, false);
        answerClose(display, String.valueOf(0x123456));
        when(display.getActiveShell()).thenReturn(window);
        try {
            ColorDialog dialog = new ColorDialog(unopened);
            dialog.open();

            verify((DartShell) window.getImpl()).addDialog((DartDialog) dialog.getImpl());
            verify((DartShell) unopened.getImpl(), never()).addDialog(any());
        } finally {
            // The display mock is shared by the whole suite.
            when(display.getActiveShell()).thenReturn(null);
        }
    }
}
