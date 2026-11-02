package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code GC.drawImage(Image, x, y, width, height)} draws what an image has for the size it lands at: an
 * {@link ImageDataAtSizeProvider} is asked for that size, and a scalable file is loaded at it, instead of
 * the 100% pixels being stretched. The image keeps one such rendering and reuses it at the same size.
 *
 * <p>Split out because {@link ImageDataAtSizeProvider} and the at-size machinery only exist from SWT 3.132;
 * the build excludes this file below that (see swt_native/build.gradle.kts).
 */
@Tag("native-unit")
class ImageAtSizeDrawNativeTest {

    private RecordingBridge bridge;
    private Display display;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static ImageData direct(int width, int height) {
        return new ImageData(width, height, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
    }

    /** A size in points as the device's pixels: the provider is asked in pixels. */
    private static int px(int points) {
        return Math.round(points * org.eclipse.swt.internal.DPIUtil.getDeviceZoom() / 100f);
    }

    private static Image atSize(DartImage impl, int width, int height) {
        Image[] found = new Image[1];
        Consumer<Image> take = image -> found[0] = image;
        impl.executeOnImageAtSizeBestFittingSize(take, width, height);
        return found[0];
    }

    private static ImageDataAtSizeProvider provider(List<String> asked) {
        return new ImageDataAtSizeProvider() {
            @Override
            public ImageData getImageData(int zoom) {
                return direct(4 * zoom / 100, 4 * zoom / 100);
            }

            @Override
            public ImageData getImageData(int width, int height) {
                asked.add(width + "x" + height);
                return direct(width, height);
            }
        };
    }

    @Test
    @DisplayName("a draw at a size sends what the provider drew for it")
    void drawAtASizeSendsTheProvidersImage() {
        List<String> asked = new ArrayList<>();
        Image image = new Image(display, provider(asked));
        Shell shell = DartMocks.dartShell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 100, 100);
        GC gc = new GC(canvas);
        bridge.comm.sent.clear();

        gc.drawImage(image, 5, 6, 40, 30);
        gc.dispose();

        assertThat(asked).containsExactly(px(40) + "x" + px(30));
        assertThat(blitted(0, 0, px(40), px(30), 5, 6, 40, 30)).isTrue();
        assertThat(GcFrames.channels(bridge.comm.sent)).noneMatch(c -> c.endsWith("/drawImageImageintintintint"));
        image.dispose();
    }

    @Test
    @DisplayName("an owner-draw capture takes a draw at a size instead of the wire")
    void captureTakesADrawAtASize() {
        Image image = new Image(display, 8, 8);
        Shell shell = DartMocks.dartShell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        GC gc = new GC(canvas);
        List<Image> captured = new ArrayList<>();
        ((DartGC) gc.getImpl()).imageCapture = captured::add;
        bridge.comm.sent.clear();

        gc.drawImage(image, 0, 0, 4, 4);
        gc.dispose();

        assertThat(captured).containsExactly(image);
        assertThat(GcFrames.channels(bridge.comm.sent)).noneMatch(c -> c.contains("drawImage"));
        image.dispose();
    }

    @Test
    @DisplayName("the rendering for a size is kept and reused, replaced at another size, and freed with the image")
    void renderingIsCached() {
        List<String> asked = new ArrayList<>();
        Image image = new Image(display, provider(asked));
        DartImage impl = (DartImage) image.getImpl();

        Image first = atSize(impl, 10, 10);
        Image again = atSize(impl, 10, 10);
        Image wider = atSize(impl, 12, 10);
        Image other = atSize(impl, 12, 12);

        assertThat(again).isSameAs(first);
        assertThat(first.isDisposed()).as("replaced at another width").isTrue();
        assertThat(wider.isDisposed()).as("replaced at another height").isTrue();
        assertThat(asked).containsExactly(px(10) + "x" + px(10), px(12) + "x" + px(10), px(12) + "x" + px(12));
        image.dispose();
        assertThat(other.isDisposed()).as("freed with the image").isTrue();
    }

    @Test
    @DisplayName("an image with nothing for a size draws itself, and a provider that answers nothing is refused")
    void nothingForTheSize() {
        Image plain = new Image(display, 4, 4);
        assertThat(atSize((DartImage) plain.getImpl(), 10, 10)).isSameAs(plain);
        Image empty = new Image(display, new ImageDataAtSizeProvider() {
            @Override
            public ImageData getImageData(int zoom) {
                return direct(4, 4);
            }

            @Override
            public ImageData getImageData(int width, int height) {
                return null;
            }
        });
        assertThatThrownBy(() -> atSize((DartImage) empty.getImpl(), 10, 10)).isInstanceOf(IllegalArgumentException.class);
        plain.dispose();
        empty.dispose();
    }

    @Test
    @DisplayName("a file that does not scale is drawn as it is")
    void fixedFileDrawsItself() throws Exception {
        File png = File.createTempFile("atsize", ".png");
        png.deleteOnExit();
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[] {direct(4, 4)};
        loader.save(png.getAbsolutePath(), SWT.IMAGE_PNG);
        Image image = new Image(display, (ImageFileNameProvider) zoom -> zoom == 100 ? png.getAbsolutePath() : null);

        assertThat(atSize((DartImage) image.getImpl(), 16, 16)).isSameAs(image);
        image.dispose();
    }

    @Test
    @DisplayName("a scalable file is loaded at the size it is drawn at")
    void scalableFileAtASize() throws Exception {
        File svg = File.createTempFile("atsize", ".svg");
        svg.deleteOnExit();
        Files.write(svg.toPath(), "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"4\" height=\"4\"/>".getBytes());
        Image image = new Image(display, (ImageFileNameProvider) zoom -> zoom == 100 ? svg.getAbsolutePath() : null);

        Image found = atSize((DartImage) image.getImpl(), 16, 16);

        assertThat(found).isNotSameAs(image);
        assertThat(found.getBounds().width).isEqualTo(px(16));
        image.dispose();
    }

    /** Whether a nine-argument drawImage went out with {@code area} after its image. */
    private boolean blitted(Object... area) {
        return GcFrames.argsOf(bridge.comm.sent, "drawImageImageintintintintintintintint").stream()
                .anyMatch(args -> WireValues.same(args.subList(1, args.size()), java.util.Arrays.asList(area)));
    }
}
