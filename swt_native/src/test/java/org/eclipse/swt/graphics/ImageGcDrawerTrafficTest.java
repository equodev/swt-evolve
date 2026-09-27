package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.comm.MessageBatch;
import org.assertj.core.api.Condition;
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
 */
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


    @Test
    void an_image_blitted_again_travels_as_a_name() {
        // Buffered GC ops are serialized when made, so the write must be credited when the batch goes out.
        Image icon = new Image(device(), 16, 16);
        Image canvas = new Image(device(), 64, 64);

        GC first = new GC(canvas);
        first.drawImage(icon, 0, 0);
        first.dispose();
        assertThat(String.join("", comm.payloads))
                .as("the icon was never described, so there is nothing to name later")
                .contains("imageData");

        comm.payloads.clear();
        GC second = new GC(canvas);
        second.drawImage(icon, 0, 0);
        second.dispose();

        String repeat = String.join("", comm.payloads);
        assertThat(repeat)
                .as("the icon was described again to a client that already held it")
                .doesNotContain("imageData");
        assertThat(repeat)
                .as("and what replaces it has to say which image it is")
                .contains("\"_r\"");

        icon.dispose();
        canvas.dispose();
    }

    @Test
    void every_gc_on_an_image_describes_itself_however_alike_the_last_one_was() {
        // Each GC is a new, blank drawer on the client, so its state must never be withheld as already sent.
        Image buffer = new Image(device(), 30, 100);
        for (int paint = 0; paint < 3; paint++) {
            comm.payloads.clear();
            GC gc = new GC(buffer);
            gc.setForeground(new Color(1, 2, 3));
            gc.drawString("12", 2, 2, true);
            gc.dispose();

            assertThat(String.join("", comm.payloads)).as("paint %d", paint)
                    .contains("\"foreground\":{\"a\":255,\"b\":3,\"g\":2,\"r\":1}");
        }
        buffer.dispose();
    }

    @Test
    void a_paint_into_an_image_travels_as_one_message() {
        Image image = new Image(device(), 16, 400);
        GC gc = new GC(image);
        comm.messages = 0;

        for (int line = 0; line < 40; line++) {
            gc.fillRectangle(0, line * 10, 16, 10);
        }
        gc.dispose();

        assertThat(comm.sent).haveExactly(40, new Condition<>(
                e -> e.endsWith("/fillRectangleintintintint"), "a line drawn"));
        // GC/create and imageInit carry raw bytes and cannot join a batch (see MessageBatch); the
        // ops and the gcDispose that ends them are one message.
        assertThat(comm.messages).isEqualTo(3);
        image.dispose();
    }

    @Test
    void a_batch_may_not_carry_raw_bytes() {
        // The client decodes a batch as JSON, so one binary payload silently drops every frame in it.
        MessageBatch batch = new MessageBatch();
        batch.add("GC/1/fillRectangleintintintint", "{\"x\":0}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        batch.add("GC/create", java.nio.ByteBuffer.allocate(8).putLong(1L).array());

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> comm.send(batch))
                .withMessageContaining("raw-bytes payload");
    }

    /**
     * Records what crosses the comm, and answers the round trips the drawer waits on so the blocking
     * reads in {@code DartGC#destroy} and {@code GC#copyArea} complete instead of timing out.
     */
    private static final class RecordingComm implements CommService {
        final List<String> sent = new ArrayList<>();
        /** Every payload put on the wire, so a test can ask what a frame actually carried. */
        final List<String> payloads = new ArrayList<>();
        int messages;
        final Map<String, Consumer<?>> handlers = new LinkedHashMap<>();

        @Override
        public void send(String eventName) {
            send(eventName, new byte[0]);
        }

        @Override
        public void send(String eventName, byte[] payload) {
            messages++;
            payloads.add(new String(payload, java.nio.charset.StandardCharsets.UTF_8));
            if (MessageBatch.EVENT.equals(eventName)) {
                for (String framed : framesIn(payload)) {
                    record(framed);
                }
                return;
            }
            record(eventName);
        }

        /** The event names a batch payload, {@code [["channel",payload],…]}, carries; rejects one the client could not parse. */
        private static List<String> framesIn(byte[] payload) {
            List<String> names = new ArrayList<>();
            String text = new String(payload, java.nio.charset.StandardCharsets.UTF_8);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c < 0x20 && c != '\n' && c != '\r' && c != '\t') {
                    throw new AssertionError("a batch carried a raw-bytes payload at " + i
                            + "; the client parses a batch as JSON and would drop the whole message");
                }
            }
            // A repeated channel travels as an entry index (see MessageBatch). Only the token right
            // after an entry's '[' is a channel: payload values sit at the same depth.
            List<String> channels = new ArrayList<>();
            int depth = 0;
            boolean inString = false, escaped = false, expectChannel = false;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (inString) {
                    if (escaped) escaped = false;
                    else if (c == '\\') escaped = true;
                    else if (c == '"') inString = false;
                    continue;
                }
                if (c == '"') {
                    if (depth == 2) {
                        int end = text.indexOf('"', i + 1);
                        String name = text.substring(i + 1, end);
                        names.add(name);
                        if (expectChannel) {
                            channels.add(name);
                            expectChannel = false;
                        }
                        i = end;
                        continue;
                    }
                    inString = true;
                } else if (c == '[') {
                    depth++;
                    expectChannel = depth == 2;
                } else if (c == ']') {
                    depth--;
                    expectChannel = false;
                } else if (expectChannel && c >= '0' && c <= '9') {
                    int end = i;
                    while (end < text.length() && text.charAt(end) >= '0' && text.charAt(end) <= '9') end++;
                    int at = Integer.parseInt(text.substring(i, end));
                    String named = at >= 0 && at < channels.size() ? channels.get(at) : "?";
                    names.add(named);
                    channels.add(named);
                    expectChannel = false;
                    i = end - 1;
                }
            }
            return names;
        }

        private void record(String eventName) {
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
