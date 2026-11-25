package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An image an application decodes from a resource stream and keeps in its own cache keyed by the
 * resource's location is still named after that resource, so the icon pack can replace it.
 */
class CachedResourceImageNameTest {

    private static final byte[] PNG_1x1 = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==");

    private ConfigFlags savedFlags;
    private Display display;

    @BeforeEach
    void setUp() {
        savedFlags = Config.getConfigFlags();
        Config.setConfigFlags(new ConfigFlags());
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
    }

    @AfterEach
    void tearDown() {
        ResourceManager.disposeImages();
        Config.setConfigFlags(savedFlags);
        FlutterBridge.set(null);
    }

    private static String nameShown(Display display, Image image) {
        return ((DartImage) GraphicsUtils.copyImage(display, image).getImpl())._filename();
    }

    @Test
    void anImageCachedUnderItsPluginUrlIsNamedAfterTheFile() {
        Image image = ResourceManager.getPluginImage(
                "bundleresource://42.fwk1/resources/ui/tick_small.png", PNG_1x1);

        assertThat(nameShown(display, image)).isEqualTo("tick_small");
    }

    @Test
    void anImageCachedUnderAPathIsNamedAfterTheFile() {
        Image image = SWTResourceManager.getImage("/resources/icons/Icon_SaveDeviceListView.png", PNG_1x1);

        assertThat(nameShown(display, image)).isEqualTo("Icon_SaveDeviceListView");
    }

    @Test
    void anImageWithATransparentPixelIsNamedToo() throws Exception {
        Image image = ResourceManager.getPluginImage(
                "bundleresource://42.fwk1/icons/extract.gif", gifWithTransparentPixel());

        assertThat(nameShown(display, image)).isEqualTo("extract");
    }

    @Test
    void anImageBuiltFromPixelsNoCacheHoldsStaysUnnamed() {
        ImageData data = new ImageData(1, 1, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));

        assertThat(nameShown(display, new Image(display, data))).isNull();
    }

    @Test
    void noNameIsLookedUpWhenIconReplacementIsOff() {
        ConfigFlags flags = new ConfigFlags();
        flags.image_disable_icons_replacement = true;
        Config.setConfigFlags(flags);

        Image image = ResourceManager.getPluginImage(
                "bundleresource://42.fwk1/resources/ui/doc_co.png", PNG_1x1);

        assertThat(nameShown(display, image)).isNull();
    }

    private static byte[] gifWithTransparentPixel() throws Exception {
        byte[] reds = {0, (byte) 255};
        byte[] none = {0, 0};
        IndexColorModel palette = new IndexColorModel(1, 2, reds, none, none, 1);
        BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_BYTE_BINARY, palette);
        image.getRaster().setSample(1, 0, 0, 1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "gif", out);
        return out.toByteArray();
    }
}
