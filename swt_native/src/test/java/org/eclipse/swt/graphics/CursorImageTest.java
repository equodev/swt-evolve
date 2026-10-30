package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

import dev.equo.swt.harness.WireValues;

import java.util.Map;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.eclipse.swt.widgets.Mocks.device;

/**
 * A cursor made from an image has no system cursor to name, so the render side can only show it if
 * the image and the hotspot travel with it.
 */
class CursorImageTest extends SerializeTestBase {

    private static ImageData pointer() {
        ImageData data = new ImageData(8, 8, 32, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        data.setPixel(3, 1, 0x112233);
        data.alphaData = new byte[64];
        data.alphaData[3 + 8] = (byte) 0xFF;
        return data;
    }

    @Test
    void anImageCursorCarriesItsImageAndHotspot() {
        Cursor cursor = new Cursor(device(), pointer(), 3, 1);

        Map<String, Object> state = serialize(cursor);

        assertThatWire(state).node("hotspotX").isEqualTo(3);
        assertThatWire(state).node("hotspotY").isEqualTo(1);
        // The image travels by id; its pixels go in an image record of their own.
        assertThatWire(state).node("image.id").isEqualTo(((DartCursor) cursor.getImpl()).image.hashCode());
    }

    @Test
    void theImageKeepsTheCursorsPixelsAndTransparency() {
        Cursor cursor = new Cursor(device(), pointer(), 3, 1);

        ImageData kept = ((DartCursor) cursor.getImpl()).image.getImageData();

        assertEquals(new RGB(0x11, 0x22, 0x33), kept.palette.getRGB(kept.getPixel(3, 1)));
        assertEquals(0xFF, kept.getAlpha(3, 1));
        assertEquals(0, kept.getAlpha(0, 0));
    }

    @Test
    void aSystemCursorCarriesNoImage() {
        Cursor cursor = new Cursor(device(), SWT.CURSOR_HAND);

        org.assertj.core.api.Assertions.assertThat(WireValues.find(serialize(cursor), m -> m.get("image") instanceof Map))
                .isEmpty();
    }

    @Test
    void disposingTheCursorDisposesItsImage() {
        Cursor cursor = new Cursor(device(), pointer(), 3, 1);
        Image image = ((DartCursor) cursor.getImpl()).image;

        cursor.dispose();

        assertTrue(image.isDisposed());
    }
}
