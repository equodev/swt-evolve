package org.eclipse.swt.graphics;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** What {@link DartImage} does when it is driven directly rather than through the dual-run API tests. */
@Tag("native-unit")
class ImageImplementationNativeTest {

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

    private static DartImage impl(Image image) {
        return (DartImage) image.getImpl();
    }

    private static ImageData direct(int width, int height) {
        return new ImageData(width, height, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
    }

    private static void completeLater(CompletableFuture<Void> future) {
        new Thread(() -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {
            }
            future.complete(null);
        }).start();
    }

    @Test
    @DisplayName("an image made without pixels is Dart-backed, has no size, and takes pixels later")
    void imageWithoutPixels() {
        Image image = new Image(display);

        assertThat(image.getImpl()).isInstanceOf(DartImage.class);
        assertThat(impl(image)._wireWidth()).isZero();
        assertThat(impl(image)._wireHeight()).isZero();
        assertThat(image.getBounds()).isEqualTo(new Rectangle(0, 0, -1, -1));
        assertThat(image.getBackground()).as("no pixels, no transparent pixel").isNull();
        image.setBackground(new Color(display, 1, 2, 3));
        assertThat(image.getBackground()).isNull();

        impl(image)._updateImageData(direct(3, 2));

        assertThat(impl(image)._wireWidth()).isEqualTo(3);
        assertThat(impl(image)._wireHeight()).isEqualTo(2);
        assertThat(image.getBounds()).isEqualTo(new Rectangle(0, 0, 3, 2));
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartImage impl = new DartImage(display, 3, 3, null);

        Image api = impl.getApi();

        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("a GC asks the image for its context without data, and a context handle is released")
    void gcContextWithoutData() {
        Image image = new Image(display, 3, 3);

        assertThat(image.internal_new_GC(null)).isZero();
        image.internal_dispose_GC(0, null);
        image.internal_dispose_GC(1, null);
        GCData data = new GCData();
        data.style = SWT.RIGHT_TO_LEFT;
        image.internal_new_GC(data);
        assertThat(data.style).isEqualTo(SWT.RIGHT_TO_LEFT);
        assertThat(data.image).isSameAs(image);
        image.dispose();
    }

    @Test
    @DisplayName("the remote render an image is drawn from is replaced, not duplicated, by a new one")
    void remoteRefIsReplaced() {
        Image image = new Image(display, 2, 2);

        impl(image)._setRemoteRef(1L);
        impl(image)._setRemoteRef(1L);
        assertThat(impl(image)._remoteRef()).isEqualTo(1L);
        impl(image)._setRemoteRef(2L);
        assertThat(impl(image)._remoteRef()).isEqualTo(2L);
        image.dispose();
    }

    @Test
    @DisplayName("a render that is not pending has nothing to cancel, and a pending one is completed")
    void cancelRender() {
        Image image = new Image(display, 2, 2);
        impl(image).cancelRenderFuture();
        CompletableFuture<Void> done = CompletableFuture.completedFuture(null);
        impl(image)._pendingRenderFuture(done);
        impl(image).cancelRenderFuture();
        CompletableFuture<Void> pending = new CompletableFuture<>();
        impl(image)._pendingRenderFuture(pending);

        impl(image).cancelRenderFuture();

        assertThat(pending).isDone();
        CompletableFuture<Void> elsewhere = new CompletableFuture<>();
        impl(image)._pendingRenderFuture(elsewhere);
        Thread other = new Thread(impl(image)::cancelRenderFuture);
        other.start();
        try {
            other.join(5000);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
        assertThat(elsewhere).as("cancelled off the UI thread, where there is no display to wake").isDone();
        image.dispose();
    }

    @Test
    @DisplayName("a render that fails while it is waited for ends the wait")
    void failedRenderEndsTheWait() {
        Image image = new Image(display, 2, 2);
        CompletableFuture<Void> failing = new CompletableFuture<>();
        impl(image)._pendingRenderFuture(failing);
        new Thread(() -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {
            }
            failing.completeExceptionally(new IllegalStateException("render failed"));
        }).start();

        assertThat(image.getImageData().width).isEqualTo(2);
        image.dispose();
    }

    @Test
    @DisplayName("a render that already finished is not waited for")
    void finishedRenderIsNotWaitedFor() {
        Image image = new Image(display, 2, 2);
        impl(image)._pendingRenderFuture(CompletableFuture.completedFuture(null));

        assertThat(image.getImageData().width).isEqualTo(2);
        image.dispose();
    }

    /**
     * Pixels are read back from {@code getImageData(int)}, an overload SWT has from 3.106; below it
     * {@code getImageData()} answers from the image's own buffer and asks the render side nothing.
     */
    private static boolean readsBackTheRenderSide() {
        try {
            Image.class.getMethod("getImageData", int.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Test
    @DisplayName("pixels the render side kept are fetched on read; an empty answer is an IO error, garbage is dropped")
    void remotePixels() {
        assumeTrue(readsBackTheRenderSide(), "this SWT release answers from the buffer only");
        Image image = new Image(display, 2, 2);
        impl(image)._adoptRemoteRender(5L, answering(new byte[] {1, 2, 3}));
        assertThat(image.getImageData().width).as("undecodable pixels leave the buffer as it was").isEqualTo(2);

        impl(image)._adoptRemoteRender(6L, answering(new byte[0]));
        assertThatThrownBy(image::getImageData)
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_IO));
        impl(image)._adoptRemoteRender(8L, answering(null));
        assertThatThrownBy(image::getImageData)
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_IO));
        impl(image)._adoptRemoteRender(9L, null);
        assertThat(image.getImageData().width).as("no comm to ask, the buffer stands").isEqualTo(2);
        impl(image)._adoptRemoteRender(null, answering(new byte[0]));
        assertThat(image.getImageData().width).as("no render to ask for").isEqualTo(2);
        image.dispose();
    }

    private static dev.equo.swt.comm.CommService answering(byte[] answer) {
        return new dev.equo.swt.harness.RecordingComm() {
            @Override
            @SuppressWarnings("unchecked")
            public void send(String eventName, byte[] payload) {
                super.send(eventName, payload);
                long ref = java.nio.ByteBuffer.wrap(payload).getLong();
                java.util.function.Consumer<byte[]> handler = (java.util.function.Consumer<byte[]>) handlers.get("Image/" + ref + "/pixelsResult");
                if (handler != null)
                    handler.accept(answer);
            }
        };
    }

    @Test
    @DisplayName("reading pixels waits for a render in flight, in a browser and on the desktop")
    void readWaitsForARenderInFlight() {
        assumeTrue(readsBackTheRenderSide(), "this SWT release answers from the buffer only");
        Image image = new Image(display, 2, 2);
        CompletableFuture<Void> web = new CompletableFuture<>();
        impl(image)._pendingRenderFuture(web);
        completeLater(web);
        assertThat(image.getImageData().width).isEqualTo(2);
        assertThat(web).isDone();

        String mode = ConfigFlags.mode();
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        try {
            CompletableFuture<Void> desktop = new CompletableFuture<>();
            impl(image)._pendingRenderFuture(desktop);
            completeLater(desktop);
            assertThat(image.getImageData().width).isEqualTo(2);
            assertThat(desktop).isDone();
        } finally {
            if (mode == null)
                System.clearProperty(ConfigFlags.MODE_PROPERTY);
            else
                ConfigFlags.setMode(mode);
        }
        image.dispose();
    }

    @Test
    @DisplayName("an interrupted read stops waiting and keeps the interrupt; off the UI thread it does not wait")
    void readThatDoesNotWait() throws Exception {
        Image image = new Image(display, 2, 2);
        impl(image)._pendingRenderFuture(new CompletableFuture<>());
        Thread.currentThread().interrupt();
        try {
            assertThat(image.getImageData().width).isEqualTo(2);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
        AtomicReference<ImageData> read = new AtomicReference<>();
        Thread other = new Thread(() -> read.set(image.getImageData()));
        other.start();
        other.join(5000);
        assertThat(read.get().width).isEqualTo(2);
        image.dispose();
    }

    @Test
    @DisplayName("images from two file providers are not equal, even of the same file")
    void equalityByFileProvider() throws Exception {
        File png = File.createTempFile("imageimpl", ".png");
        png.deleteOnExit();
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[] {direct(2, 2)};
        loader.save(png.getAbsolutePath(), SWT.IMAGE_PNG);
        Image fileA = new Image(display, (ImageFileNameProvider) zoom -> zoom == 100 ? png.getAbsolutePath() : null);
        Image fileB = new Image(display, (ImageFileNameProvider) zoom -> zoom == 100 ? png.getAbsolutePath() : null);

        assertThat(fileA).isNotEqualTo(fileB);
        fileA.dispose();
        fileB.dispose();
    }

    @Test
    @DisplayName("images built from different sources or flags are not equal")
    void equalityBySource() {
        ImageDataProvider provider = zoom -> direct(2, 2);
        Image fromProvider = new Image(display, provider);
        Image gray = new Image(display, fromProvider, SWT.IMAGE_GRAY);
        Image plain = new Image(display, 2, 2);

        assertThat(gray).isNotEqualTo(fromProvider);
        assertThat(fromProvider).isNotEqualTo(plain);
        assertThat(plain).isNotEqualTo(fromProvider);
        gray.dispose();
        plain.dispose();
        fromProvider.dispose();
    }
}
