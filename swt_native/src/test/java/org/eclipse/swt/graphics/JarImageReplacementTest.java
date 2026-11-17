package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An icon pack in {@code assets_path} replaces an application icon of the same name, whether the
 * icon is a file on disk or a resource inside the application's jar.
 */
class JarImageReplacementTest {

    private static final byte[] PNG_1x1 = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==");
    private static final String SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>";

    @TempDir
    Path tmp;

    private ConfigFlags savedFlags;
    private Display display;

    @BeforeEach
    void setUp() throws Exception {
        savedFlags = Config.getConfigFlags();
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        Path pack = Files.createDirectories(tmp.resolve("pack"));
        Files.writeString(pack.resolve("blank_folder.svg"), SVG);
        ConfigFlags flags = new ConfigFlags();
        flags.assets_path = pack.toString();
        Config.setConfigFlags(flags);
        resetAssetsCache();
    }

    @AfterEach
    void tearDown() throws Exception {
        Config.setConfigFlags(savedFlags);
        resetAssetsCache();
        FlutterBridge.set(null);
    }

    private static void resetAssetsCache() throws Exception {
        Field f = GraphicsUtils.class.getDeclaredField("assetFilesCache");
        f.setAccessible(true);
        f.set(null, null);
    }

    private static DartImage impl(Image image) {
        return (DartImage) image.getImpl();
    }

    @Test
    void anIconOnDiskIsReplacedByThePacksIcon() throws Exception {
        Path png = tmp.resolve("blank_folder.png");
        Files.write(png, PNG_1x1);

        Image image = ImageDescriptor.createFromURL(png.toUri().toURL()).createImage(display);
        Image shown = GraphicsUtils.copyImage(display, image);

        assertThat(impl(shown)._svgContent()).isEqualTo(SVG);
    }

    @Test
    void anIconInsideTheApplicationsJarIsReplacedByThePacksIcon() throws Exception {
        Path jar = tmp.resolve("app.jar");
        try (OutputStream out = Files.newOutputStream(jar); JarOutputStream jos = new JarOutputStream(out)) {
            jos.putNextEntry(new JarEntry("icons/blank_folder.png"));
            jos.write(PNG_1x1);
            jos.closeEntry();
        }
        // What ImageDescriptor.createFromFile(SomeClass.class, "blank_folder.png") builds for a
        // class packed in a jar: a URL descriptor on a jar: URL.
        URL resource = new URL("jar:" + jar.toUri() + "!/icons/blank_folder.png");

        // A cached JarFile stays open, and Windows can't delete the temp dir holding an open file.
        boolean savedUseCaches = URLConnection.getDefaultUseCaches("jar");
        URLConnection.setDefaultUseCaches("jar", false);
        try {
            Image image = ImageDescriptor.createFromURL(resource).createImage(display);
            Image shown = GraphicsUtils.copyImage(display, image);

            assertThat(impl(shown)._svgContent()).isEqualTo(SVG);
        } finally {
            URLConnection.setDefaultUseCaches("jar", savedUseCaches);
        }
    }

    @Test
    void anImageFromAProviderWithNoDescriptorStaysUnnamed() {
        ImageData data = new ImageData(1, 1, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        ImageDataProvider provider = zoom -> zoom == 100 ? data : null;

        Image shown = GraphicsUtils.copyImage(display, new Image(display, provider));

        assertThat(impl(shown)._filename()).isNull();
        assertThat(impl(shown)._svgContent()).isNull();
    }

    @Test
    void aDisabledIconIsGreyedNotReplacedByThePacksIcon() throws Exception {
        Path png = tmp.resolve("blank_folder.png");
        BufferedImage red = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        red.setRGB(0, 0, 0xFF0000);
        ImageIO.write(red, "png", png.toFile());
        ImageDescriptor plain = ImageDescriptor.createFromURL(png.toUri().toURL());

        Image shown = GraphicsUtils.copyImage(display,
                ImageDescriptor.createWithFlags(plain, SWT.IMAGE_DISABLE).createImage(display));

        assertThat(impl(shown)._filename()).isNull();
        assertThat(impl(shown)._svgContent()).isNull();
        ImageData data = shown.getImageData();
        RGB rgb = data.palette.getRGB(data.getPixel(0, 0));
        assertThat(rgb.red).isEqualTo(rgb.green).isEqualTo(rgb.blue);
    }
}
