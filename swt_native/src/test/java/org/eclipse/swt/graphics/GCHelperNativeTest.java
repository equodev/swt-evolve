package org.eclipse.swt.graphics;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/** The helpers {@link DartGC} and {@link DartImage} lean on, driven with the inputs their callers can hand them. */
@Tag("native-unit")
class GCHelperNativeTest {

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

    private static byte[] png(int width, int height, int pixel) {
        ImageData data = new ImageData(width, height, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                data.setPixel(x, y, pixel);
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[] {data};
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        loader.save(out, SWT.IMAGE_PNG);
        return out.toByteArray();
    }

    /** A comm that answers each pixel request at once with the given bytes. */
    private static class AnsweringComm extends RecordingComm {
        private final byte[] answer;

        AnsweringComm(byte[] answer) {
            this.answer = answer;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void send(String eventName, byte[] payload) {
            super.send(eventName, payload);
            long ref = ByteBuffer.wrap(payload).getLong();
            Consumer<byte[]> handler = (Consumer<byte[]>) handlers.get("Image/" + ref + "/pixelsResult");
            if (handler != null)
                handler.accept(answer);
        }
    }

    @Test
    @DisplayName("nothing to read from a missing GC")
    void missingGc() {
        assertThat(GCHelper.drawableOf(null)).isNull();
        assertThat(GCHelper.stateKeyOf(null)).isNull();
        Image image = new Image(display, 2, 2);
        GC gc = new GC(image);
        assertThat(GCHelper.drawableOf((DartGC) gc.getImpl())).isSameAs(image);
        gc.dispose();
        image.dispose();
    }

    @Test
    @DisplayName("off the UI thread there is no system font, so a metric for no font has nothing to measure")
    void metricsWithoutAFont() throws Exception {
        FontMetrics[] metrics = new FontMetrics[1];
        Thread other = new Thread(() -> metrics[0] = GCHelper.createFontMetrics(null));
        other.start();
        other.join(5000);

        assertThat(metrics[0]).isNotNull();
        assertThat(metrics[0].getHeight()).isZero();
    }

    @Test
    @DisplayName("only a live transform other than the identity transforms")
    void whatTransforms() {
        Transform identity = new Transform(display);
        Transform scale = new Transform(display, 2, 0, 0, 2, 0, 0);
        Transform disposed = new Transform(display, 2, 0, 0, 2, 0, 0);
        disposed.dispose();

        assertThat(GCHelper.isTransforming(null)).isFalse();
        assertThat(GCHelper.isTransforming(disposed)).isFalse();
        assertThat(GCHelper.isTransforming(identity)).isFalse();
        assertThat(GCHelper.isTransforming(scale)).isTrue();
        assertThat(GCHelper.transformationScale(null)).isEqualTo(1f);
        assertThat(GCHelper.transformationScale(disposed)).isEqualTo(1f);
        assertThat(GCHelper.transformationScale(new Transform(display, 2, 0, 0, 3, 7, 7))).isEqualTo(3f);
        assertThat(GCHelper.transformationScale(new Transform(display, 0, 1, -1, 0, 0, 0))).isEqualTo(1f);
        identity.dispose();
        scale.dispose();
    }

    @Test
    @DisplayName("a singular transform has no inverse, so a rectangle goes back unchanged")
    void singularInverse() {
        Transform flat = new Transform(display, 0, 0, 0, 0, 5, 5);

        assertThat(GCHelper.transformBounds(flat, new Rectangle(1, 2, 3, 4), true)).isEqualTo(new Rectangle(1, 2, 3, 4));
        flat.dispose();
    }

    @Test
    @DisplayName("rectangles laid end to end are bounded, and none bound nothing")
    void boundsOfRects() {
        assertThat(GCHelper.boundsOfRects(new int[0])).isEqualTo(new Rectangle(0, 0, 0, 0));
        assertThat(GCHelper.boundsOfRects(new int[] {0, 0, 2, 2, 5, 5, 1, 1})).isEqualTo(new Rectangle(0, 0, 6, 6));
    }

    @Test
    @DisplayName("disposing an image's GC tolerates no GC and a disposed one")
    void disposeImageGcTolerance() {
        GCHelper.disposeImageGC(null);
        Shell shell = DartMocks.dartShell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        GC gc = new GC(canvas);
        gc.dispose();

        GCHelper.disposeImageGC(gc);

        assertThat(gc.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("an image takes the pixels a render sends back, from bytes, a buffer or base64")
    void imageTakesRenderedPixels() {
        Image image = new Image(display, 3, 3);
        byte[] red = png(3, 3, 0xFF0000);

        GCHelper.updateImageFromPngBytes(image, null, (byte[]) null);
        GCHelper.updateImageFromPngBytes(image, null, new byte[0]);
        GCHelper.updateImageFromPngBytes(image, null, (ByteBuffer) null);
        GCHelper.updateImageFromPngBytes(image, null, ByteBuffer.allocate(0));
        GCHelper.updateImageFromPng(image, null, null);
        assertThat(image.getImageData().getPixel(1, 1)).as("nothing arrived, nothing changed").isNotEqualTo(0xFF0000);

        ByteBuffer direct = ByteBuffer.allocateDirect(red.length);
        direct.put(red).flip();
        GCHelper.updateImageFromPngBytes(image, null, direct);
        assertThat(image.getImageData().palette.getRGB(image.getImageData().getPixel(1, 1))).isEqualTo(new RGB(255, 0, 0));

        GCHelper.updateImageFromPng(image, null, Base64.getEncoder().encodeToString(png(3, 3, 0x0000FF)));
        assertThat(image.getImageData().palette.getRGB(image.getImageData().getPixel(1, 1))).isEqualTo(new RGB(0, 0, 255));

        GCHelper.updateImageFromPngBytes(image, null, new byte[] {1, 2, 3});
        assertThat(image.getImageData().palette.getRGB(image.getImageData().getPixel(1, 1)))
                .as("bytes that are not an image are dropped").isEqualTo(new RGB(0, 0, 255));
        image.dispose();
    }

    @Test
    @DisplayName("an image GC context is a value")
    void imageGcContextIsAValue() {
        Image image = new Image(display, 2, 2);
        CompletableFuture<Void> future = new CompletableFuture<>();
        GCHelper.ImageGCContext a = new GCHelper.ImageGCContext(image, null, image, future);
        GCHelper.ImageGCContext b = new GCHelper.ImageGCContext(image, null, image, future);

        assertThat(a).isEqualTo(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo("context");
        assertThat(a).isNotEqualTo(new GCHelper.ImageGCContext(null, null, image, future));
        assertThat(a).isNotEqualTo(new GCHelper.ImageGCContext(image, image, image, future));
        assertThat(a).isNotEqualTo(new GCHelper.ImageGCContext(image, null, null, future));
        assertThat(a).isNotEqualTo(new GCHelper.ImageGCContext(image, null, image, new CompletableFuture<>()));
        assertThat(a.toString()).startsWith("ImageGCContext[");
        image.dispose();
    }

    @Test
    @DisplayName("a pixel read with no comm, or one nobody answers, comes back empty after a second try")
    void unansweredPixelRead() {
        RecordingComm comm = new RecordingComm();

        assertThat(GCHelper.fetchRemotePixels(null, null, 7, 1)).isNull();
        assertThat(GCHelper.fetchRemotePixels(null, comm, 7, 1)).isNull();

        assertThat(comm.sent.stream().filter(f -> f.event.equals("Image/requestPixels")).count()).isEqualTo(2);
        assertThat(comm.handlers).isEmpty();
    }

    @Test
    @DisplayName("off the UI thread a pixel read with no device has no display to wait on")
    void pixelReadOffTheUiThread() throws Exception {
        byte[][] read = {new byte[] {1}};
        Thread other = new Thread(() -> read[0] = GCHelper.fetchRemotePixels(null, new RecordingComm(), 14, 1000));
        other.start();
        other.join(5000);

        assertThat(read[0]).isNull();
    }

    @Test
    @DisplayName("an answered pixel read returns the bytes the render side sent")
    void answeredPixelRead() {
        byte[] bytes = png(1, 1, 0);

        assertThat(GCHelper.fetchRemotePixels(display, new AnsweringComm(bytes), 8, 1000)).isEqualTo(bytes);
        assertThat(GCHelper.fetchRemotePixels(null, new AnsweringComm(bytes), 12, 1000)).as("with no display to wake").isEqualTo(bytes);
        assertThat(GCHelper.fetchRemotePixels(display, new AnsweringComm(new byte[0]), 13, 1000)).as("an empty answer is asked again").isEmpty();
    }

    @Test
    @DisplayName("an interrupted wait for pixels gives up and keeps the interrupt")
    void interruptedPixelRead() {
        Thread.currentThread().interrupt();
        try {
            assertThat(GCHelper.fetchRemotePixels(display, new RecordingComm(), 9, 5000)).isNull();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    @DisplayName("on the desktop the wait pumps the display until the answer or the deadline")
    void desktopPixelRead() {
        String mode = ConfigFlags.mode();
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        try {
            byte[] bytes = png(1, 1, 0);
            assertThat(GCHelper.fetchRemotePixels(display, new AnsweringComm(bytes), 10, 1000)).isEqualTo(bytes);
            assertThat(GCHelper.fetchRemotePixels(display, new RecordingComm(), 11, 20)).isNull();
        } finally {
            if (mode == null)
                System.clearProperty(ConfigFlags.MODE_PROPERTY);
            else
                ConfigFlags.setMode(mode);
        }
    }

    @Test
    @DisplayName("a request that waits for an answer gives up at its deadline, whoever it is for")
    void unansweredRequest() {
        Shell shell = DartMocks.dartShell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        Image image = new Image(display, 2, 2);
        GC imageGc = new GC(image);
        boolean[] answered = new boolean[1];

        GCHelper.callOnDisplayBytes(canvas.getImpl(), dev.equo.swt.WireTable.CHANNEL_GET_ITEM_HEIGHT, dev.equo.swt.WireTable.CHANNEL_GET_ITEM_HEIGHT_RESPONSE, null, bytes -> answered[0] = true, 5);
        GCHelper.callOnDisplayBytes(imageGc.getImpl(), dev.equo.swt.WireTable.CHANNEL_GET_ITEM_HEIGHT, dev.equo.swt.WireTable.CHANNEL_GET_ITEM_HEIGHT_RESPONSE, null, bytes -> answered[0] = true, 5);

        assertThat(answered[0]).isFalse();
        imageGc.dispose();
        image.dispose();
    }

    @Test
    @DisplayName("off the UI thread an image's GC has no display to pump, and its request still gives up at its deadline")
    void unansweredRequestOffTheUiThread() throws Exception {
        Image image = new Image(display, 2, 2);
        GC imageGc = new GC(image);
        boolean[] answered = new boolean[1];
        Throwable[] failed = new Throwable[1];

        Thread other = new Thread(() -> {
            try {
                GCHelper.callOnDisplayBytes(imageGc.getImpl(), dev.equo.swt.WireTable.CHANNEL_GET_ITEM_HEIGHT, dev.equo.swt.WireTable.CHANNEL_GET_ITEM_HEIGHT_RESPONSE, null, bytes -> answered[0] = true, 20);
            } catch (Throwable t) {
                failed[0] = t;
            }
        });
        other.start();
        other.join(5000);

        assertThat(other.isAlive()).isFalse();
        assertThat(failed[0]).isNull();
        assertThat(answered[0]).isFalse();
        imageGc.dispose();
        image.dispose();
    }

    @Test
    @DisplayName("text flags decide what is measured: mnemonics, an empty string, a disposed font")
    void textExtentFlags() {
        Font font = new Font(display, new FontData("Helvetica", 12, SWT.NORMAL));

        assertThat(GCHelper.textExtent("a&&b", SWT.DRAW_MNEMONIC, font)).isEqualTo(GCHelper.textExtent("a&b", 0, font));
        assertThat(GCHelper.textExtent("a&b", SWT.DRAW_MNEMONIC, font)).isEqualTo(GCHelper.textExtent("ab", 0, font));
        assertThat(GCHelper.textExtent("", 0, font).x).isZero();
        assertThat(GCHelper.textExtent("", 0, font).y).isPositive();
        assertThat(GCHelper.textExtent("a&", SWT.DRAW_MNEMONIC, font)).as("a trailing & is dropped too, as on Win32 and GTK").isEqualTo(GCHelper.textExtent("a", 0, font));
        assertThat(GCHelper.textExtent("", SWT.DRAW_DELIMITER, font)).isEqualTo(GCHelper.textExtent("", 0, font));
        Font disposed = new Font(display, new FontData("Helvetica", 30, SWT.NORMAL));
        disposed.dispose();
        assertThat(GCHelper.computeLineExtent("Wg", disposed)).isEqualTo(GCHelper.computeLineExtent("Wg", display.getSystemFont()));
        font.dispose();
    }

    @Test
    @DisplayName("a control with no font of its own has its text measured in the system font")
    void textExtentWithoutAFontUsesTheSystemFont() {
        assertThat(GCHelper.textExtent("a&b", SWT.DRAW_MNEMONIC, null))
                .isEqualTo(GCHelper.textExtent("a&b", SWT.DRAW_MNEMONIC, display.getSystemFont()));
    }
}
