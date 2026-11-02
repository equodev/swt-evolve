package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.comm.MessageBatch;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.WireValues;
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
        assertThat(comm.copyAreaAnswerHeard)
                .as("Java waits for the answer on the channel the client sends it on")
                .containsExactly(true);

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
        assertThat(describesImageData(comm.carried()))
                .as("the icon was never described, so there is nothing to name later")
                .isTrue();

        comm.payloads.clear();
        GC second = new GC(canvas);
        second.drawImage(icon, 0, 0);
        second.dispose();

        assertThat(describesImageData(comm.carried()))
                .as("the icon was described again to a client that already held it")
                .isFalse();
        assertThat(comm.payloads)
                .as("and what replaces it has to say which image it is")
                .anyMatch(e -> e.channel().endsWith("drawImageImageintint") && e.value() instanceof List<?> args
                        && args.size() == 3 && names(args.get(0), icon.hashCode())
                        && WireValues.same(args.get(1), 0) && WireValues.same(args.get(2), 0));

        icon.dispose();
        canvas.dispose();
    }

    @Test
    void an_image_the_client_was_given_is_released_after_the_draw_that_names_it() throws InterruptedException {
        // A frame image made and disposed per paint would otherwise stay on the client forever.
        Image frame = new Image(device(), 16, 16);
        Image canvas = new Image(device(), 64, 64);
        GC gc = new GC(canvas);
        gc.drawImage(frame, 0, 0);
        long id = FlutterBridge.id(frame.getImpl());
        frame.dispose();
        gc.dispose();

        // Sent once every send already deferred has gone, which may be on another thread.
        int released = comm.awaitSent("Image/forget", 5000);
        assertThat(released).as("the disposed image was never released on the client").isNotNegative();
        synchronized (comm) {
            assertThat(ByteBuffer.wrap(comm.raw.get(released)).getLong()).isEqualTo(id);
            assertThat(comm.sent.subList(0, released))
                    .as("the release overtook the draw that names the image")
                    .anyMatch(e -> e.endsWith("/drawImageImageintint"));
        }
        canvas.dispose();
    }

    @Test
    void an_image_the_client_never_saw_is_not_released_there() throws InterruptedException {
        Image scratch = new Image(device(), 16, 16);
        scratch.dispose();

        assertThat(comm.awaitSent("Image/forget", 500)).isNegative();
    }

    @Test
    void an_image_drawn_by_its_own_gc_is_blitted_as_its_render_not_its_blank_buffer() {
        // Whatever the client was given while the image was still blank must not be what a later
        // blit names: the blank buffer paints as an opaque black square.
        Image avatar = new Image(device(), 16, 16);
        GC paint = new GC(avatar);
        paint.fillRectangle(0, 0, 16, 16);
        paint.dispose();
        Long ref = ((DartImage) avatar.getImpl()).remoteRef;
        assertThat(ref).as("the GC's drawing never became a render").isNotNull();

        Image target = new Image(device(), 64, 64);
        GC firstBlit = new GC(target);
        firstBlit.drawImage(avatar, 0, 0);
        firstBlit.dispose();
        // The next image's GC is what sends the first one's batch, crediting its blank description.
        Image next = new Image(device(), 16, 16);
        GC nextPaint = new GC(next);
        nextPaint.fillRectangle(0, 0, 16, 16);
        nextPaint.dispose();

        comm.payloads.clear();
        GC blit = new GC(target);
        blit.drawImage(avatar, 0, 0);
        blit.dispose();

        assertThat(WireValues.find(comm.carried(), m -> m.containsKey("remoteRef") && WireValues.same(m.get("remoteRef"), ref)))
                .as("the blit named a stale copy of the image instead of its render")
                .isNotEmpty();

        avatar.dispose();
        next.dispose();
        target.dispose();
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

            assertThat(WireValues.find(comm.carried(), m -> "#ff010203".equals(m.get("foreground"))))
                    .as("paint %d", paint).isNotEmpty();
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
    void a_batch_carries_raw_bytes_beside_other_frames() {
        // Each entry of a batch is applied as the frame it would have been, raw bytes included.
        MessageBatch batch = new MessageBatch();
        dev.equo.swt.harness.Channels.add(batch, "GC/1/fillRectangleintintintint", "{\"x\":0}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        dev.equo.swt.harness.Channels.add(batch, "GC/create", java.nio.ByteBuffer.allocate(8).putLong(1L).array());
        dev.equo.swt.harness.Channels.add(batch, "GC/1/gcDispose", "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        comm.send(batch);

        assertThat(comm.sent).containsSubsequence("GC/1/fillRectangleintintintint", "GC/create", "GC/1/gcDispose");
    }

    /** Whether some record in {@code carried} describes image data. */
    private static boolean describesImageData(List<Object> carried) {
        return !WireValues.find(carried, m -> m.get("imageData") instanceof Map).isEmpty();
    }

    /** Whether {@code argument} names the image {@code id}: by its id, or as a reference to it. */
    private static boolean names(Object argument, int id) {
        if (argument instanceof Number) return WireValues.same(argument, id);
        return argument instanceof Map<?, ?> ref && WireValues.same(ref.get("id"), id)
                && Boolean.TRUE.equals(ref.get("_r"));
    }

    /**
     * Records what crosses the comm, and answers the round trips the drawer waits on so the blocking
     * reads in {@code DartGC#destroy} and {@code GC#copyArea} complete instead of timing out.
     */
    private static final class RecordingComm implements CommService {
        final List<String> sent = new ArrayList<>();
        /** Every entry put on the wire, so a test can ask what a frame actually carried. */
        final List<GcFrames.Entry> payloads = new ArrayList<>();
        /** The payload of each entry in {@link #sent} that travelled on its own, by the same index. */
        final List<byte[]> raw = new ArrayList<>();
        int messages;
        final Map<String, Consumer<?>> handlers = new LinkedHashMap<>();
        /** For each copyArea request, whether Java was listening on the exact channel the client answers on. */
        final List<Boolean> copyAreaAnswerHeard = new ArrayList<>();

        @Override
        public void send(int type, long id, int channel, byte[] payload) {
            send(dev.equo.swt.harness.Channels.name(type, id, channel), payload == null ? new byte[0] : payload);
        }

        @Override
        public void sendFrame(byte[] frame, int offset, int length) {
            dev.equo.swt.comm.ChannelHeader.Decoded header = dev.equo.swt.comm.ChannelHeader.decode(frame, offset, offset + length);
            send(header.channel(), java.util.Arrays.copyOfRange(frame, header.payloadStart(), offset + length));
        }

        /** The index of the first {@code eventName} sent, waiting up to {@code timeoutMs} for it; -1 if none. */
        int awaitSent(String eventName, long timeoutMs) throws InterruptedException {
            long deadline = System.currentTimeMillis() + timeoutMs;
            while (true) {
                synchronized (this) {
                    int at = sent.indexOf(eventName);
                    if (at >= 0 || System.currentTimeMillis() >= deadline) return at;
                }
                Thread.sleep(10);
            }
        }

        synchronized void send(String eventName, byte[] payload) {
            GcFrames.Decoded frame = GcFrames.decode(eventName, payload);
            messages++;
            if (MessageBatch.EVENT.equals(frame.event()) && frame.value() instanceof List<?> entries) {
                for (Object entry : entries) {
                    payloads.add((GcFrames.Entry) entry);
                    raw.add(null);
                    record(((GcFrames.Entry) entry).channel());
                }
                return;
            }
            payloads.add(new GcFrames.Entry(frame.event(), frame.value()));
            raw.add(payload);
            record(frame.event());
        }

        /** What each entry put on the wire carried, in order. */
        List<Object> carried() {
            List<Object> values = new ArrayList<>();
            for (GcFrames.Entry entry : payloads) values.add(entry.value());
            return values;
        }


        private void record(String eventName) {
            sent.add(eventName);
            if (eventName.equals("GC/create")) {
                answer("/ClientReady", null);
            } else if (eventName.endsWith("/gcDispose")) {
                answer("/imageResult", renderResult());
            } else if (eventName.endsWith("/copyAreaImageintint")) {
                // The client answers on the channel the request came in on, plus "Response".
                copyAreaAnswerHeard.add(handlers.containsKey(eventName + "Response"));
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
        public <T> void on(int type, long id, int channel, Class<T> cls, Consumer<T> callback) {
            handlers.put(dev.equo.swt.harness.Channels.name(type, id, channel), callback);
        }

        @Override
        public void remove(int type, long id, int channel) {
            handlers.remove(dev.equo.swt.harness.Channels.name(type, id, channel));
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
