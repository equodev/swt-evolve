package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.swt.widgets.Mocks.device;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * A renderer that shares one surface between frames and read-backs can read an image back at the
 * view's size. Those pixels are not the image's: taken as a buffer held at another zoom, they would
 * be scaled to the image's width and come out at the wrong height.
 */
class ImageReadBackOfAnotherSizeTest extends SerializeTestBase {

    private boolean bootstrapped;
    private SizedReadsComm comm;

    @BeforeEach
    void renderSide() {
        bootstrapped = FlutterBridge.displayBootstrapped;
        FlutterBridge.displayBootstrapped = true;
        FlutterBridge.setDisplayGcCommResolver(display -> comm);
    }

    @AfterEach
    void restore() {
        FlutterBridge.setDisplayGcCommResolver(null);
        FlutterBridge.displayBootstrapped = bootstrapped;
    }

    @Test
    void a_read_back_at_the_views_size_is_read_again() {
        comm = new SizedReadsComm(new int[][] {{140, 60}, {20, 10}});
        Image image = drawn(20, 10);
        try {
            ImageData data = image.getImageData();

            assertThat(comm.reads).as("reads asked of the render side").isEqualTo(2);
            assertThat(data.width).isEqualTo(20);
            assertThat(data.height).isEqualTo(10);
            assertThat(data.palette.getRGB(data.getPixel(5, 5))).isEqualTo(SizedReadsComm.RENDERED);
        } finally {
            image.dispose();
        }
    }

    @Test
    void three_read_backs_at_no_zoom_of_the_image_are_an_io_error() {
        // A zoom that rounds to nothing, a width a pixel off 150%, and 200% one dimension only.
        comm = new SizedReadsComm(new int[][] {{1, 1}, {451, 15}, {600, 30}});
        Image image = drawn(300, 10);
        try {
            assertThatThrownBy(image::getImageData)
                    .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_IO));
            assertThat(comm.reads).as("reads asked of the render side").isEqualTo(3);
        } finally {
            image.dispose();
        }
    }

    private static Image drawn(int width, int height) {
        Image image = new Image(device(), width, height);
        GC gc = new GC(image);
        gc.fillRectangle(0, 0, width, height);
        gc.dispose();
        assumeTrue(((DartImage) image.getImpl()).remoteRef != null, "this release draws the image here, with nothing to read back");
        return image;
    }

    /** Answers the n-th pixel read at the n-th size it is given, and every read after those at the last. */
    private static final class SizedReadsComm extends RecordingComm {
        static final RGB RENDERED = new RGB(0x33, 0x66, 0xCC);
        private final int[][] sizes;
        int reads;

        SizedReadsComm(int[][] sizes) {
            this.sizes = sizes;
        }

        @Override
        public void send(String eventName, byte[] payload) {
            super.send(eventName, payload);
            if (eventName.equals("Image/requestPixels")) {
                long ref = ByteBuffer.wrap(payload).getLong();
                int[] size = sizes[Math.min(reads++, sizes.length - 1)];
                byte[] png = png(size[0], size[1]);
                answer("Image/" + ref + "/pixelsResult", png);
            }
        }

        private static byte[] png(int width, int height) {
            PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
            ImageData data = new ImageData(width, height, 32, palette);
            int pixel = palette.getPixel(RENDERED);
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) data.setPixel(x, y, pixel);
            }
            ImageLoader loader = new ImageLoader();
            loader.data = new ImageData[] {data};
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            loader.save(out, SWT.IMAGE_PNG);
            return out.toByteArray();
        }

        @SuppressWarnings("unchecked")
        private void answer(String event, Object payload) {
            Consumer<Object> handler = (Consumer<Object>) handlers.get(event);
            if (handler != null) handler.accept(payload);
        }
    }
}
