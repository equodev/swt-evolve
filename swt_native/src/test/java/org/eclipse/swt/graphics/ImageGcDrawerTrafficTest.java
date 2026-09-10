package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.comm.CommService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.swt.widgets.Mocks.device;

/**
 * {@code new GC(image)} talks to Flutter over the Display's shared comm — the channel every other
 * widget and event uses. Standing the off-screen drawer up eagerly, at GC creation, puts a
 * ClientReady handshake and the image's whole bitmap on that channel before a single drawing
 * operation has happened, so a caller that rebuilds an image GC per repaint (JFace's
 * {@code AnnotationRulerColumn.doubleBufferPaint}, whose buffer field stays null whenever the draw
 * throws) starves the rest of the UI. Nothing needs the drawer before the ops are flushed, so it
 * must not be started until something actually wants pixels back.
 *
 * <p>Not run on the GTK backend, for the reason given in {@link ImageGcDrawerFailureTest}: there the
 * image reports itself disposed and {@code new GC(image)} fails before the drawer is reached.
 */
@DisabledOnOs(OS.LINUX)
class ImageGcDrawerTrafficTest extends SerializeTestBase {

    private boolean bootstrapped;
    private RecordingComm comm;

    @BeforeEach
    void useARecordingDisplayComm() {
        bootstrapped = FlutterBridge.displayBootstrapped;
        FlutterBridge.displayBootstrapped = true;
        comm = new RecordingComm();
        FlutterBridge.setDisplayGcCommResolver(display -> comm);
    }

    @AfterEach
    void dropTheRecordingDisplayComm() {
        FlutterBridge.setDisplayGcCommResolver(null);
        FlutterBridge.displayBootstrapped = bootstrapped;
    }

    @Test
    void a_draw_that_throws_puts_nothing_on_the_display_comm() {
        ImageGcDrawer drawer = (gc, width, height) -> {
            gc.fillRectangle(0, 0, width, height);
            throw new IllegalStateException("assertion failed");
        };

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> new Image(device(), drawer, 16, 400));

        assertThat(comm.sent).isEmpty();
        assertThat(comm.handlers).isEmpty();
    }

    @Test
    void the_drawer_is_not_started_before_the_drawing_is_done() {
        List<String> sentDuringTheDraw = new ArrayList<>();
        ImageGcDrawer drawer = (gc, width, height) -> {
            gc.fillRectangle(0, 0, width, height);
            sentDuringTheDraw.addAll(comm.sent);
        };

        Image image = new Image(device(), drawer, 16, 400);

        assertThat(sentDuringTheDraw).isEmpty();
        assertThat(comm.sent).contains("GC/create");
        image.dispose();
    }

    @Test
    void an_op_whose_answer_the_caller_waits_for_starts_the_drawer() {
        Image image = new Image(device(), 16, 16);
        GC gc = new GC(image);

        // copyArea sends and then blocks pumping the Display until Flutter answers. The op is
        // buffered in the drawer, so unless the request itself starts the drawer it never leaves
        // Java and the caller waits out its whole timeout for a picture that was never asked for.
        gc.copyArea(image, 0, 0);

        assertThat(comm.sent).contains("GC/create");
        assertThat(comm.sent).anyMatch(e -> e.endsWith("/copyAreaImageintint"));

        gc.dispose();
        image.dispose();
    }

    /**
     * Records what crosses the comm, and answers the round trips the drawer waits on so the blocking
     * reads in {@code DartGC#destroy} and {@code GC#copyArea} complete instead of timing out.
     */
    private static final class RecordingComm implements CommService {
        final List<String> sent = new ArrayList<>();
        final Map<String, Consumer<?>> handlers = new LinkedHashMap<>();

        @Override
        public void send(String eventName) {
            send(eventName, new byte[0]);
        }

        @Override
        public void send(String eventName, byte[] payload) {
            sent.add(eventName);
            if (eventName.equals("GC/create")) {
                answer("/ClientReady", null);
            } else if (eventName.endsWith("/gcDispose")) {
                answer("/imageResult", renderResult());
            } else if (eventName.endsWith("/copyAreaImageintint")) {
                answer("/copyAreaImageintintResponse", renderResult());
            }
        }

        @SuppressWarnings("unchecked")
        private void answer(String suffix, Object payload) {
            handlers.entrySet().stream()
                    .filter(e -> e.getKey().endsWith(suffix))
                    .findFirst()
                    .ifPresent(e -> ((Consumer<Object>) e.getValue()).accept(payload));
        }

        /** 8-byte remote ref followed by the PNG, the shape {@code GCImageDrawer} unpacks. */
        private static byte[] renderResult() {
            ImageLoader loader = new ImageLoader();
            loader.data = new ImageData[] {
                    new ImageData(16, 400, 32, new PaletteData(0xFF0000, 0xFF00, 0xFF)) };
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            loader.save(png, SWT.IMAGE_PNG);
            return ByteBuffer.allocate(8 + png.size()).putLong(1L).put(png.toByteArray()).array();
        }

        @Override
        public <T> void on(String eventName, Class<T> cls, Consumer<T> callback) {
            handlers.put(eventName, callback);
        }

        @Override
        public void remove(String eventName) {
            handlers.remove(eventName);
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public void stop() {
        }
    }
}
