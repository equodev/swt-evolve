package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
class ColorDialogOpenNativeTest extends DialogBridgeTestBase {

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

    /** A cancel clears the colour on every OS, as Cocoa and GTK do. */
    @Test
    void cancelReturnsNullAndClearsTheColour() {
        Shell shell = shellAnswering("-1");
        ColorDialog dialog = new ColorDialog(shell);
        dialog.setRGB(new RGB(10, 20, 30));

        assertThat(dialog.open()).isNull();
        assertThat(dialog.getRGB()).isNull();
    }

    @Test
    void theColourAndTheCustomColoursAreSentToFlutter() {
        Shell shell = shellAnswering("-1");
        ColorDialog dialog = new ColorDialog(shell);
        dialog.setText("Pick");
        dialog.setRGB(new RGB(1, 2, 3));
        dialog.setRGBs(new RGB[] {new RGB(4, 5, 6), null});
        VColorDialog value = ((DartColorDialog) dialog.getImpl()).getValue();

        assertThat(value.getText()).isEqualTo("Pick");
        assertThat(value.getRGB()).isEqualTo(new RGB(1, 2, 3));
        assertThat(value.getRGBs()).containsExactly(new RGB(4, 5, 6));
        assertThat(((DartColorDialog) dialog.getImpl())._rgb()).isEqualTo(new RGB(1, 2, 3));
        assertThat(((DartColorDialog) dialog.getImpl())._rgbs()).hasSize(2);
        dialog.open();
        assertThat(((DartColorDialog) dialog.getImpl())._selected()).isFalse();
        assertThat(value.getSwt()).isEqualTo("ColorDialog");
    }

    @Test
    void anImplementationBuiltOnItsOwnMakesItsApi() {
        DartColorDialog dialog = new DartColorDialog(DartMocks.dartShell(), SWT.NONE, null);

        assertThat(dialog.getApi().getImpl()).isSameAs(dialog);
        assertThat(dialog.getApi()).isSameAs(dialog.getApi());
    }
}
