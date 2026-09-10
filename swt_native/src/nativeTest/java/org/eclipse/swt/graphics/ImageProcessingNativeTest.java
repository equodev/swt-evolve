package org.eclipse.swt.graphics;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The three transforms an application's image service is built on — scale, gray/disable and
 * decorate — over the desk/web backend, where {@code Image} is always Dart-backed.
 *
 * <p>Without a native image handle the transform has to land on the {@link ImageData} that
 * {@code VImage} puts on the wire, and the render side has to be left with no cheaper source to
 * prefer over it: {@code image_utils.dart} resolves svgContent, then filename (asset replacement
 * and icon map), and only then the pixels, so a transformed image that still carries the source's
 * name or providers is painted as the untransformed original.
 */
@Tag("native-unit")
public class ImageProcessingNativeTest {

    private static final int SIZE = 8;
    private static final RGB SOURCE_RGB = new RGB(200, 40, 10);

    private static ImageData colorData() {
        PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
        ImageData data = new ImageData(SIZE, SIZE, 24, palette);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                data.setPixel(x, y, palette.getPixel(SOURCE_RGB));
            }
        }
        return data;
    }

    private static RGB rgbAt(ImageData data, int x, int y) {
        return data.palette.getRGB(data.getPixel(x, y));
    }

    private static Image source(Display display) {
        return new Image(display, colorData());
    }

    @Test
    public void grayFlagProducesGrayPixels() {
        Display display = DartMocks.dartDisplay();
        ImageData data = new Image(display, source(display), SWT.IMAGE_GRAY).getImageData();

        RGB rgb = rgbAt(data, 0, 0);
        assertThat(rgb.red).isEqualTo(rgb.green).isEqualTo(rgb.blue);
    }

    @Test
    public void disableFlagChangesThePixels() {
        Display display = DartMocks.dartDisplay();
        ImageData data = new Image(display, source(display), SWT.IMAGE_DISABLE).getImageData();

        assertThat(rgbAt(data, 0, 0)).isNotEqualTo(SOURCE_RGB);
    }

    @Test
    public void aTransformedImageStopsCarryingTheSourceFilename() {
        Display display = DartMocks.dartDisplay();
        Image src = source(display);
        ((DartImage) src.getImpl())._filename("delete_obj");

        Image gray = new Image(display, src, SWT.IMAGE_GRAY);
        Image copy = new Image(display, src, SWT.IMAGE_COPY);

        assertThat(((DartImage) gray.getImpl())._filename()).isNull();
        assertThat(((DartImage) copy.getImpl())._filename()).isEqualTo("delete_obj");
    }

    @Test
    public void scalingAnImageKeepsItsPixels() {
        Display display = DartMocks.dartDisplay();
        Image scaled = new Image(display, source(display).getImageData().scaledTo(SIZE * 2, SIZE * 2));

        ImageData data = scaled.getImageData();
        assertThat(data.width).isEqualTo(SIZE * 2);
        assertThat(rgbAt(data, 0, 0)).isEqualTo(SOURCE_RGB);
    }

    @Test
    public void jfaceDecorationCompositesOverADartBackedImage() {
        Display display = DartMocks.dartDisplay();
        ImageData base = source(display).getImageData();
        RGB overlayRgb = new RGB(0, 0, 255);
        ImageData overlay = new ImageData(SIZE / 2, SIZE / 2, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        for (int y = 0; y < overlay.height; y++) {
            for (int x = 0; x < overlay.width; x++) {
                overlay.setPixel(x, y, overlay.palette.getPixel(overlayRgb));
            }
        }

        ImageData composite = new CompositeImageDescriptor() {
            @Override
            protected Point getSize() {
                return new Point(SIZE, SIZE);
            }

            @Override
            protected void drawCompositeImage(int width, int height) {
                drawImage(zoom -> zoom == 100 ? base : null, 0, 0);
                drawImage(zoom -> zoom == 100 ? overlay : null, SIZE / 2, SIZE / 2);
            }
        }.getImageData(100);

        assertThat(rgbAt(composite, 0, 0)).isEqualTo(SOURCE_RGB);
        assertThat(rgbAt(composite, SIZE - 1, SIZE - 1)).isEqualTo(overlayRgb);
    }
}
