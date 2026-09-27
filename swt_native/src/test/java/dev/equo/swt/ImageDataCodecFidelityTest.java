package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/** The encoded PNG is all Flutter sees of an ImageData, so anything the encode drops is lost silently. */
public class ImageDataCodecFidelityTest {

    private static ImageData decoded(ImageData source) {
        byte[] png = ImageDataCodec.encode(source);
        assertThat(png).as("nothing was encoded").isNotNull();
        return new ImageLoader().load(new ByteArrayInputStream(png))[0];
    }

    private static RGB rgbAt(ImageData data, int x, int y) {
        return data.palette.getRGB(data.getPixel(x, y));
    }

    @Test
    public void a_channel_narrower_than_a_byte_is_encoded_at_full_range() {
        // Native SWT scales a 2-bit channel to 0xFF; PaletteData#getRGB only shifts it, to 0xC0.
        ImageData source = new ImageData(4, 4, 8, new PaletteData(0x30, 0x0C, 0x03));
        source.setPixel(1, 2, 0x30);

        assertThat(rgbAt(decoded(source), 1, 2))
                .as("the encoded red is dimmer than the one native SWT shows")
                .isEqualTo(new RGB(255, 0, 0));
    }

    @Test
    public void a_full_width_channel_is_left_exactly_as_it_is() {
        ImageData source = new ImageData(4, 4, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));
        source.setPixel(0, 0, 0x336699);

        assertThat(rgbAt(decoded(source), 0, 0)).isEqualTo(new RGB(0x33, 0x66, 0x99));
    }

    @Test
    public void a_separate_transparency_mask_survives_as_alpha() {
        // new Image(device, source, mask) keeps the mask in maskData, which the PNG encoder ignores.
        // The image then reaches Flutter fully opaque and paints over whatever it is blitted onto.
        ImageData pixels = new ImageData(4, 4, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));
        ImageData mask = new ImageData(4, 4, 1,
                new PaletteData(new RGB(0, 0, 0), new RGB(255, 255, 255)));
        mask.setPixel(3, 3, 1);
        ImageData combined = new ImageData(pixels.width, pixels.height, pixels.depth,
                pixels.palette, pixels.scanlinePad, pixels.data);
        combined.maskPad = mask.scanlinePad;
        combined.maskData = mask.data;
        assertThat(combined.getTransparencyType()).isEqualTo(SWT.TRANSPARENCY_MASK);

        ImageData out = decoded(combined);
        assertThat(out.alphaData).as("the mask was dropped, so the whole image is opaque").isNotNull();
        assertThat(out.getAlpha(3, 3)).as("the one masked-in pixel should be opaque").isEqualTo(255);
        assertThat(out.getAlpha(0, 0)).as("everything else should be transparent").isZero();
    }

    @Test
    public void a_transparent_palette_index_stays_transparent() {
        // A 1-bit stipple: one index drawn, the other see-through.
        ImageData source = new ImageData(4, 4, 1,
                new PaletteData(new RGB(0x1B, 0x76, 0x99), new RGB(255, 255, 255)));
        source.setPixel(0, 0, 0);
        source.setPixel(1, 0, 1);
        source.transparentPixel = 1;

        ImageData out = decoded(source);
        assertThat(isTransparent(out, 1, 0))
                .as("the see-through index arrived opaque, painting its colour over the background")
                .isTrue();
        assertThat(isTransparent(out, 0, 0)).as("the drawn index should stay opaque").isFalse();
        assertThat(rgbAt(out, 0, 0)).isEqualTo(new RGB(0x1B, 0x76, 0x99));
    }

    private static boolean isTransparent(ImageData data, int x, int y) {
        return switch (data.getTransparencyType()) {
            case SWT.TRANSPARENCY_PIXEL -> data.getPixel(x, y) == data.transparentPixel;
            case SWT.TRANSPARENCY_ALPHA -> data.getAlpha(x, y) == 0;
            default -> false;
        };
    }

    @Test
    public void two_images_differing_only_by_their_mask_do_not_share_a_cache_entry() {
        ImageData pixels = new ImageData(4, 4, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));

        byte[] first = ImageDataCodec.encode(withMask(pixels, 0, 0));
        byte[] second = ImageDataCodec.encode(withMask(pixels, 3, 3));

        assertThat(second).isNotSameAs(first);
        assertThat(second).isNotEqualTo(first);
    }

    private static ImageData withMask(ImageData pixels, int opaqueX, int opaqueY) {
        ImageData mask = new ImageData(pixels.width, pixels.height, 1,
                new PaletteData(new RGB(0, 0, 0), new RGB(255, 255, 255)));
        mask.setPixel(opaqueX, opaqueY, 1);
        ImageData combined = new ImageData(pixels.width, pixels.height, pixels.depth,
                pixels.palette, pixels.scanlinePad, pixels.data);
        combined.maskPad = mask.scanlinePad;
        combined.maskData = mask.data;
        return combined;
    }
}
