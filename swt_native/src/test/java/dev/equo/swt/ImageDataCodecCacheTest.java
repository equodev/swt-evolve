package dev.equo.swt;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Image.getImageData() hands back a fresh ImageData (and pixel array) on every call, so re-encoding
 * an unchanged icon on every serialization did a full PngEncoder/Deflater pass each time. Multiplied
 * across a Tree with many icons, on every dirty-flush, that stalled the UI thread long enough for
 * the OS to flag the process as not responding. encode() must recognize repeated content and skip
 * re-compressing it.
 */
public class ImageDataCodecCacheTest {

    private static ImageData solidImage(int width, int height, int rgb) {
        PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
        ImageData data = new ImageData(width, height, 24, palette);
        int pixel = palette.getPixel(new org.eclipse.swt.graphics.RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data.setPixel(x, y, pixel);
            }
        }
        return data;
    }

    @Test
    public void encoding_the_same_content_twice_reuses_the_cached_bytes() {
        byte[] first = ImageDataCodec.encode(solidImage(12, 12, 0x336699));
        byte[] second = ImageDataCodec.encode(solidImage(12, 12, 0x336699));

        assertThat(second).isSameAs(first);
    }

    @Test
    public void different_content_is_not_served_from_another_images_cache_entry() {
        byte[] blue = ImageDataCodec.encode(solidImage(12, 12, 0x0000FF));
        byte[] red = ImageDataCodec.encode(solidImage(12, 12, 0xFF0000));

        assertThat(red).isNotSameAs(blue);
        assertThat(red).isNotEqualTo(blue);
    }
}
