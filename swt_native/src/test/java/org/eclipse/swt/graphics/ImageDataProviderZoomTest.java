package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.device;

@DisabledIf(value = "constructorThrowsOnThisBaseline", disabledReason = "Image(Device, ImageDataProvider) raises ERROR_NO_HANDLES on the GTK backend of SWT 3.131 and older")
class ImageDataProviderZoomTest extends SerializeTestBase {

    /**
     * On the GTK backend of the older baselines the provider constructor itself raises
     * ERROR_NO_HANDLES, before the provider's zooms can be observed. {@code SWT.getVersion()} is
     * {@code major * 1000 + minor}: 4971 is 3.132, the oldest GTK baseline the constructor works on.
     */
    static boolean constructorThrowsOnThisBaseline() {
        return "gtk".equals(SWT.getPlatform()) && SWT.getVersion() < 4971;
    }

    // A provider can be expensive: JFace's ColorSelector draws into a live GC and reads the pixels
    // back, a round trip to the render side per call. The image stores 100% data and scales it for
    // any other zoom, so asking the provider for more is work nobody uses.
    @Test
    void creating_an_image_asks_the_provider_only_for_the_zoom_it_stores() {
        List<Integer> zooms = new ArrayList<>();
        ImageDataProvider provider = zoom -> {
            zooms.add(zoom);
            return new ImageData(4, 4, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        };

        Image image = new Image(device(), provider);

        assertThat(zooms).containsExactly(100);
        image.dispose();
    }
}
