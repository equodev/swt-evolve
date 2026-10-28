package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
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

/** What every platform's Image does on dispose, with its transparent pixel, and with what it was built from. */
@ExtendWith(Mocks.class)
class ImageDisposeAndBackgroundNativeTest {

    private Shell shell;

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
        shell = Mocks.shell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static ImageData direct(int width, int height) {
        return new ImageData(width, height, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
    }

    @Test
    @DisplayName("disposing an image disposes the GC drawing on it")
    void disposingTheImageDisposesItsGc() {
        Image image = new Image(shell.getDisplay(), 8, 8);
        GC gc = new GC(image);

        image.dispose();

        assertThat(gc.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("the background of an image with a transparent pixel is that pixel's colour")
    void backgroundIsTheTransparentPixel() {
        ImageData data = new ImageData(3, 3, 8, new PaletteData(new RGB[] {new RGB(255, 0, 0), new RGB(0, 0, 255)}));
        data.transparentPixel = 1;
        Image image = new Image(shell.getDisplay(), data);

        assertThat(image.getBackground()).isNotNull();
        assertThat(image.getBackground().getRGB()).isEqualTo(new RGB(0, 0, 255));
        image.setBackground(new Color(shell.getDisplay(), 0, 255, 0));
        assertThat(image.getBackground().getRGB()).isEqualTo(new RGB(0, 255, 0));
        image.dispose();
    }

    @Test
    @DisplayName("an image is not equal to something that is not an image")
    void notEqualToANonImage() {
        Image image = new Image(shell.getDisplay(), 3, 3);

        assertThat(image.equals("Image")).isFalse();
        assertThat(image.equals(null)).isFalse();
        image.dispose();
    }

    @Test
    @DisplayName("an image keeps its own copy of the data it was built from")
    void imageDataIsCopied() {
        ImageData data = direct(4, 4);
        Image image = new Image(shell.getDisplay(), data);

        data.setPixel(1, 1, 0xFF0000);

        assertThat(image.getImageData().getPixel(1, 1)).isZero();
        image.dispose();
    }

    @SuppressWarnings("deprecation")
    @Test
    @DisplayName("an image built from a rectangle has pixels of that size to read")
    void rectangleImageHasPixels() {
        Image image = new Image(shell.getDisplay(), new Rectangle(0, 0, 6, 4));

        ImageData data = image.getImageData();

        assertThat(data.width).isEqualTo(6);
        assertThat(data.height).isEqualTo(4);
        image.dispose();
    }

    @Test
    @DisplayName("an image built from a provider answers 200% with the provider's own data")
    void providerAnswersItsOwn200() {
        ImageDataProvider provider = zoom -> {
            ImageData data = direct(zoom / 25, zoom / 25);
            if (zoom == 200) data.setPixel(0, 0, 0xFF);
            return data;
        };
        Image image = new Image(shell.getDisplay(), provider);

        ImageData at200 = image.getImageData(200);

        assertThat(at200.width).isEqualTo(8);
        assertThat(at200.getPixel(0, 0)).as("the provider's pixels, not the 100% data scaled").isEqualTo(0xFF);
        image.dispose();
    }
}
