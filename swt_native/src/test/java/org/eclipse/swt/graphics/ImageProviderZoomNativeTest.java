package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A provider image answers another zoom from its provider, for any zoom, as on GTK and Win32. Where the provider
 * has nothing for the zoom, the held pixels are scaled.
 */
@Tag("native-unit")
class ImageProviderZoomNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static ImageData direct(int size) {
        return new ImageData(size, size, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
    }

    private static final int RED = 0xFF0000;

    /** {@code size} square, black but for a red top-left pixel. */
    private static ImageData marked(int size) {
        ImageData data = direct(size);
        data.setPixel(0, 0, RED);
        return data;
    }

    private static String write(Path dir, String name, ImageData data) {
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[] {data};
        String path = dir.resolve(name).toString();
        loader.save(path, SWT.IMAGE_PNG);
        return path;
    }

    private static final ImageDataProvider PROVIDER = zoom -> direct(zoom / 50);

    @Test
    void the_provider_is_asked_for_any_zoom() {
        Image image = new Image(display, PROVIDER);
        assertThat(image.getImageData(150).width).isEqualTo(3);
        assertThat(image.getImageData(200).width).isEqualTo(4);
        image.dispose();
    }

    @Test
    void an_image_answers_a_zoom_it_is_not_held_at_from_its_provider() {
        Image image = new Image(display, (ImageDataProvider) zoom -> direct(zoom == 100 ? 2 : 5));
        assertThat(image.getImageData(150).width).as("the provider's, not the 100% pixels scaled").isEqualTo(5);
        image.dispose();
    }

    @Test
    void a_provider_with_nothing_for_the_zoom_leaves_it_to_scaling() {
        List<Integer> asked = new ArrayList<>();
        ImageData at100 = marked(2);
        Image image = new Image(display, (ImageDataProvider) zoom -> {
            asked.add(zoom);
            return zoom == 100 ? at100 : null;
        });
        ImageData answered = image.getImageData(200);
        assertThat(asked).contains(200);
        assertIsTheHeldDataUnscaled(image, answered);
        assertThat(answered).as("a copy, not the provider's 100% data").isNotSameAs(at100);
        image.dispose();
    }

    @Test
    void a_file_name_provider_is_asked_for_any_zoom(@TempDir Path dir) {
        String at100 = write(dir, "at100.png", marked(2));
        String at150 = write(dir, "at150.png", direct(5));
        Image image = new Image(display, (ImageFileNameProvider) zoom -> zoom == 100 ? at100 : zoom == 150 ? at150 : null);
        assertThat(image.getImageData(150).width).as("the provider's file, not the 100% pixels scaled").isEqualTo(5);
        assertIsTheHeldDataUnscaled(image, image.getImageData(200));
        image.dispose();
    }

    /**
     * The mocked display is not auto-scalable, so scaling the held 100% data to another zoom leaves it as it is,
     * as {@code DPIUtil.scaleImageData} does on a real non-scaling device.
     */
    private static void assertIsTheHeldDataUnscaled(Image image, ImageData answered) {
        assertThat(answered.width).as("the 100% data").isEqualTo(2);
        assertThat(answered.getPixel(0, 0)).as("the marked pixel").isEqualTo(RED);
        assertThat(answered.getPixel(1, 1)).isEqualTo(0);
        answered.setPixel(1, 1, RED);
        assertThat(image.getImageData(100).getPixel(1, 1)).as("changing the answer leaves the image as it was").isEqualTo(0);
    }

    @Test
    void the_provider_data_is_copied() {
        ImageData held = direct(4);
        Image image = new Image(display, (ImageDataProvider) zoom -> zoom == 100 ? direct(2) : held);
        assertThat(image.getImageData(200)).isNotSameAs(held);
        image.dispose();
    }
}
