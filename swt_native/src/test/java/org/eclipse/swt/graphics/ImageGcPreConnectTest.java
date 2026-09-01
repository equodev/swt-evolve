package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.SerializeTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.device;

class ImageGcPreConnectTest extends SerializeTestBase {

    // An application that builds its content during startup -- draw2d's LightweightSystem, or a GEF
    // viewer populated from setContents() -- opens a GC on an off-screen Image before Flutter has
    // connected. Gating the wiring on that connection left the GC with no id, so DartGC#hashCode()
    // stayed 0 and every op it published went to the dead "GC/0" channel, and with no memGC, so the
    // image could never be rendered before something drew it. Neither needs a connected client: the
    // comm layer buffers pre-connect sends, and GCImageDrawer#onReady gates what actually does.
    @Test
    void an_image_gc_is_wired_even_before_the_flutter_client_connects() {
        boolean bootstrapped = FlutterBridge.displayBootstrapped;
        FlutterBridge.displayBootstrapped = false;
        try {
            Image image = new Image(device(), 8, 8);
            GCData data = new GCData();

            GCHelper.ImageGCContext context = GCHelper.setupImageGC(image, data, null);

            assertThat(context).isNotNull();
            assertThat(context.dartImage()).isSameAs(image);
            assertThat(context.renderFuture()).isNotNull();
            assertThat(((DartImage) image.getImpl()).pendingRenderFuture)
                    .isSameAs(context.renderFuture());
            assertThat(data.image).isSameAs(image);
        } finally {
            FlutterBridge.displayBootstrapped = bootstrapped;
        }
    }

    @Test
    void a_drawable_that_is_not_image_backed_is_still_rejected() {
        assertThat(GCHelper.setupImageGC(null, new GCData(), null)).isNull();
    }
}
