package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A transformed image must not be repainted from the provider it inherited.
 *
 * <p>The provider describes the untransformed source, and {@code loadImageDataAtExactSize} reads it:
 * the transform lives in the image's own {@link ImageData} alone, so a provider left in place hands
 * the source pixels back on every at-size request.
 *
 * <p>Split out from {@code ImageProcessingNativeTest} because {@link ImageDataAtSizeProvider} and
 * the at-size machinery it drives only exist from SWT 3.132; the build excludes this file below
 * that (see swt_native/build.gradle.kts). The rest of the image-transform coverage is version
 * independent and stays there.
 */
@Tag("native-unit")
public class ImageAtSizeProviderNativeTest {

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

    @Test
    public void aTransformedImageIsNotRepaintedFromTheSourceProvider() {
        Display display = DartMocks.dartDisplay();
        RGB providerRgb = new RGB(10, 220, 30);
        ImageDataAtSizeProvider provider = new ImageDataAtSizeProvider() {
            @Override
            public ImageData getImageData(int zoom) {
                return colorData();
            }

            @Override
            public ImageData getImageData(int width, int height) {
                ImageData data = new ImageData(width, height, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        data.setPixel(x, y, data.palette.getPixel(providerRgb));
                    }
                }
                return data;
            }
        };
        Image gray = new Image(display, new Image(display, provider), SWT.IMAGE_GRAY);

        // The provider describes the untransformed source; left in place it re-loads those pixels
        // for every at-size request, and the enabled icon is what gets painted.
        Image[] atSize = new Image[1];
        ((DartImage) gray.getImpl()).executeOnImageAtSizeBestFittingSize(i -> atSize[0] = i, SIZE * 2, SIZE * 2);

        RGB rgb = rgbAt(atSize[0].getImageData(), 0, 0);
        assertThat(rgb).isNotEqualTo(providerRgb);
        assertThat(rgb.red).isEqualTo(rgb.green).isEqualTo(rgb.blue);
    }
}
