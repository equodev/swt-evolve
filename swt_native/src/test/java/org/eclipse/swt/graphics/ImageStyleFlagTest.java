package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.device;

/**
 * {@code new Image(device, src, SWT.IMAGE_GRAY|IMAGE_DISABLE)} must hand back transformed pixels.
 *
 * <p>The Dart backend has no native image handle, so the transform has to happen on the
 * {@link ImageData} that {@code VImage} puts on the wire — that data is the only thing Flutter can
 * paint. A copy that keeps the source pixels renders the enabled icon where the application asked
 * for a disabled one.
 */
@DisabledIf(value = "constructorThrowsOnThisBaseline", disabledReason = "Image(Device, Image, flag) raises from getImageDataAtCurrentZoom on SWT 3.128 and older")
class ImageStyleFlagTest extends SerializeTestBase {

    private static final int W = 4;
    private static final int H = 4;

    /**
     * The generated copy-with-flag constructor reaches {@code getImageDataAtCurrentZoom()} on the
     * older baselines and raises there, so these three cases cannot run until that is fixed.
     * {@code SWT.getVersion()} is {@code major * 1000 + minor}: 4969 is 3.130, the oldest baseline
     * the constructor works on.
     */
    static boolean constructorThrowsOnThisBaseline() {
        return SWT.getVersion() < 4969;
    }

    private static ImageData colorData() {
        PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
        ImageData data = new ImageData(W, H, 24, palette);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                data.setPixel(x, y, palette.getPixel(new RGB(200, 40, 10)));
            }
        }
        return data;
    }

    private static RGB rgbAt(ImageData data, int x, int y) {
        return data.palette.getRGB(data.getPixel(x, y));
    }

    @Test
    void grayFlagProducesGrayPixels() {
        Image source = new Image(device(), colorData());
        Image gray = new Image(device(), source, SWT.IMAGE_GRAY);

        ImageData data = gray.getImageData();
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                RGB rgb = rgbAt(data, x, y);
                assertThat(rgb.red).as("pixel %d,%d is gray", x, y).isEqualTo(rgb.green).isEqualTo(rgb.blue);
            }
        }
    }

    @Test
    void disableFlagChangesThePixels() {
        Image source = new Image(device(), colorData());
        Image disabled = new Image(device(), source, SWT.IMAGE_DISABLE);

        assertThat(rgbAt(disabled.getImageData(), 0, 0)).isNotEqualTo(new RGB(200, 40, 10));
    }

    @Test
    void copyFlagLeavesThePixelsAlone() {
        Image source = new Image(device(), colorData());
        Image copy = new Image(device(), source, SWT.IMAGE_COPY);

        assertThat(rgbAt(copy.getImageData(), 0, 0)).isEqualTo(new RGB(200, 40, 10));
    }
}
