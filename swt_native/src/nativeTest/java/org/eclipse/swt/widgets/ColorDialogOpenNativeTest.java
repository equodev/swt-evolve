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
import static org.mockito.Mockito.verify;

/**
 * Java half of "Color dialog (color wheel) does not open": {@code DartColorDialog.open()} used to be
 * an empty stub that returned null without ever handing the dialog to Flutter, so an app calling it
 * (an Eclipse {@code ColorSelector}, for one) simply saw nothing happen.
 *
 * <p>It must now go through {@link DartDialog#openDialogWithFlutter} — parking itself on the owning
 * Shell so Flutter renders it — and decode the picked colour from the close channel's 0xRRGGBB
 * payload. The byte order matters: the win32 dialog's own struct is 0x00BBGGRR, and reusing that
 * packing here would silently swap red and blue.
 */
@Tag("native-unit")
class ColorDialogOpenNativeTest {

    /** Stands in for Flutter answering the {@code ColorDialog/<id>/close} channel. */
    private static Shell shellAnswering(String closePayload) {
        Shell shell = DartMocks.dartShell();
        DisplayBridge bridge = mock(DisplayBridge.class);
        doAnswer(invocation -> {
            Consumer<String> handler = invocation.getArgument(1);
            handler.accept(closePayload);
            return null;
        }).when(bridge).onChannel(anyString(), any());
        ((DartDisplay) shell.getDisplay().getImpl()).displayBridge = bridge;
        return shell;
    }

    @Test
    void openReturnsThePickedColourAndShowsTheDialog() {
        Shell shell = shellAnswering(String.valueOf(0x123456));
        ColorDialog dialog = new ColorDialog(shell);
        dialog.setRGB(new RGB(255, 255, 255));

        RGB picked = dialog.open();

        assertThat(picked).isEqualTo(new RGB(0x12, 0x34, 0x56));
        assertThat(dialog.getRGB()).isEqualTo(picked);
        // Rendering is what "the dialog opens" means: the Shell must have carried it to Flutter.
        verify((DartShell) shell.getImpl()).addDialog((DartDialog) dialog.getImpl());
        verify((DartShell) shell.getImpl()).removeDialog((DartDialog) dialog.getImpl());
    }

    @Test
    void cancelReturnsNullAndKeepsThePreviouslySetColour() {
        Shell shell = shellAnswering("-1");
        ColorDialog dialog = new ColorDialog(shell);
        dialog.setRGB(new RGB(10, 20, 30));

        assertThat(dialog.open()).isNull();
        assertThat(dialog.getRGB()).isEqualTo(new RGB(10, 20, 30));
    }
}
