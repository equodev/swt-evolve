package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
        java.util.List<Integer> asked = new java.util.ArrayList<>();
        Image image = new Image(display, (ImageDataProvider) zoom -> { asked.add(zoom); return zoom == 100 ? direct(2) : null; });
        assertThat(image.getImageData(200)).as("the 100% pixels scaled").isNotNull();
        assertThat(asked).contains(200);
        image.dispose();
    }

    @Test
    void the_provider_data_is_copied() {
        ImageData held = direct(4);
        Image image = new Image(display, (ImageDataProvider) zoom -> zoom == 100 ? direct(2) : held);
        assertThat(image.getImageData(200)).isNotSameAs(held);
        image.dispose();
    }
}
