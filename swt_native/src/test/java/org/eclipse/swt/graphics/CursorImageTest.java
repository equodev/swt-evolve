package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
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

        String json = serialize(cursor);

        assertThatJson(json).inPath("hotspotX").isEqualTo(3);
        assertThatJson(json).inPath("hotspotY").isEqualTo(1);
        assertThatJson(json).inPath("image.imageData.width").isEqualTo(8);
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

        assertThatJson(serialize(cursor)).inPath("image").isAbsent();
    }

    @Test
    void disposingTheCursorDisposesItsImage() {
        Cursor cursor = new Cursor(device(), pointer(), 3, 1);
        Image image = ((DartCursor) cursor.getImpl()).image;

        cursor.dispose();

        assertTrue(image.isDisposed());
    }
}
