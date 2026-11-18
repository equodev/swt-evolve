package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.jface.resource.CompositeImageDescriptor;
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
import java.net.URLStreamHandler;
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

    /**
     * A navigator item is drawn as a nested composite image descriptor (a decoration wrapping another
     * decoration wrapping the base icon). This stub stands in for JFace's {@code DecorationOverlayIcon}
     * with only the stable {@link CompositeImageDescriptor} contract, so the test compiles against every
     * SWT/JFace version in the matrix (older builds lack the descriptor-base constructor).
     */
    private static final class NestedDecoration extends CompositeImageDescriptor {
        private final Object base; // an Image, or a nested ImageDescriptor

        NestedDecoration(Object base) {
            this.base = base;
        }

        @Override
        protected Point getSize() {
            return new Point(1, 1);
        }

        @Override
        protected void drawCompositeImage(int width, int height) {
            // No overlay to draw; the base name is what matters here.
        }
    }

    // A provider that holds a two-level composite over a named base image — the shape JFace hands a
    // decorated navigator icon to the backend. Tests getFilename directly, so it does not depend on how
    // a given SWT/JFace version builds the image from the descriptor.
    private ImageDataProvider nestedCompositeProvider() throws Exception {
        Path base = tmp.resolve("blank_folder.png");
        Files.write(base, PNG_1x1);
        Image baseImage = new Image(display, base.toString()); // DartImage names itself "blank_folder"
        ImageDescriptor nested = new NestedDecoration(new NestedDecoration(baseImage));
        // The provider only has to carry the descriptor: getFilename reads it by reflection and never
        // renders it, so the body returns nothing and avoids getImageData, whose signature differs
        // across the SWT/JFace version matrix.
        return zoom -> nested != null ? null : null;
    }

    @Test
    void getFilenameRecoversANestedCompositesBaseNameWhenGcReplacementIsOn() throws Exception {
        Config.getConfigFlags().gc_icons_replacement = true;

        // The composite does not adapt to a URL; the name is recovered from the base at the bottom.
        assertThat(GraphicsUtils.getFilename(nestedCompositeProvider())).isEqualTo("blank_folder");
    }

    /** A descriptor that adapts to an application URL, the way a JFace URL descriptor does. */
    private static final class UrlDescriptor extends ImageDescriptor {
        private final URL url;

        UrlDescriptor(URL url) {
            this.url = url;
        }

        @SuppressWarnings("unchecked")
        public <T> T getAdapter(Class<T> adapter) {
            return adapter == URL.class ? (T) url : null;
        }

        @Override
        public ImageData getImageData() {
            return null;
        }
    }

    /** A provider whose descriptor adapts to {@code icon://<host><path>}, a protocol of the application's own. */
    private static ImageDataProvider iconUrlProvider(String host, String path) throws Exception {
        URLStreamHandler handler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                throw new UnsupportedOperationException();
            }
        };
        ImageDescriptor descriptor = new UrlDescriptor(new URL("icon", host, -1, path, handler));
        return zoom -> descriptor != null ? null : null;
    }

    @Test
    void getFilenameNamesAnIconWhoseUrlKeepsItsKeyInTheHost() throws Exception {
        assertThat(GraphicsUtils.getFilename(iconUrlProvider("IMG_EDIT", ""))).isEqualTo("edit");
    }

    @Test
    void getFilenameNamesAnIconByAHostKeyWithoutThePrefix() throws Exception {
        assertThat(GraphicsUtils.getFilename(iconUrlProvider("Refresh", ""))).isEqualTo("refresh");
        assertThat(GraphicsUtils.getFilename(iconUrlProvider("IMG_EDIT", "/"))).isEqualTo("edit");
    }

    @Test
    void getFilenamePrefersTheUrlPathOverItsHost() throws Exception {
        assertThat(GraphicsUtils.getFilename(iconUrlProvider("IMG_EDIT", "/icons/save.png"))).isEqualTo("save");
    }

    @Test
    void getFilenameIgnoresACompositeWhenGcReplacementIsOff() throws Exception {
        // gc_icons_replacement defaults to false: a decorated image stays unnamed and keeps its pixels
        // (overlay included) rather than collapsing to the base pack icon.
        assertThat(GraphicsUtils.getFilename(nestedCompositeProvider())).isNull();
    }
}
