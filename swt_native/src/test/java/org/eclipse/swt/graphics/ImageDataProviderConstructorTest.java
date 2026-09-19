package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.device;

/**
 * {@code new Image(device, provider)} must build the image from the provider's data on every
 * backend and baseline. JFace's {@code ImageDescriptor} goes through this constructor, so an image
 * that fails here takes every descriptor-based icon with it.
 */
class ImageDataProviderConstructorTest extends SerializeTestBase {

    @Test
    void buildsTheImageFromTheProvidersData() {
        ImageData data = new ImageData(3, 2, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        ImageDataProvider provider = zoom -> zoom == 100 ? data : null;

        Image image = new Image(device(), provider);

        assertThat(image.isDisposed()).isFalse();
        assertThat(image.getBounds()).isEqualTo(new Rectangle(0, 0, 3, 2));
    }
}
