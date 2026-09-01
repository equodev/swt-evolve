package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.device;

class GCDrawOpenImageTest extends SerializeTestBase {

    // A GC opened on an Image renders only on an explicit signal, so an image blitted while its own
    // GC is still open would put its untouched backing buffer on the wire. draw2d's
    // BufferedGraphicsSource does exactly that -- it paints a figure tree into an off-screen Image
    // and blits it, and SWTGraphics#dispose() leaves the image's GC open -- so nothing else would
    // ever trigger the render and the blit came out blank. copyImageForDraw closes that gap.
    //
    // The open-GC branch itself needs a live off-screen Flutter engine (new GC(Image) starts one),
    // so it is covered by the draw2d snippets rather than here; these lock the pass-through
    // behaviour so the added branch cannot change what a plain image draw sends.
    @Test
    void an_image_with_no_open_gc_is_passed_through_untouched() {
        Image image = new Image(device(), 8, 8);

        assertThat(((DartImage) image.getImpl())._memGC()).isNull();
        assertThat(GraphicsUtils.copyImageForDraw(null, image)).isSameAs(image);
    }

    @Test
    void a_null_image_is_still_a_no_op() {
        assertThat(GraphicsUtils.copyImageForDraw(null, null)).isNull();
    }
}
