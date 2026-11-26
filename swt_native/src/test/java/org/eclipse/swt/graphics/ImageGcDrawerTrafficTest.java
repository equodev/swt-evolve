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

        int released = flushUntilSent("Image/forget", 5000);
        assertThat(released).as("the disposed image was never released on the client").isNotNegative();
        synchronized (comm) {
            assertThat(comm.forgotten).containsExactly(id);
            assertThat(comm.sent.subList(0, released))
                    .as("the release overtook the draw that names the image")
                    .anyMatch(e -> e.endsWith("/drawImageImageintint"));
        }
        canvas.dispose();
    }

    @Test
    void an_image_let_go_of_between_flushes_is_released_in_the_next_flush_message() throws InterruptedException {
        // A form's title gradient is made anew and the last one disposed on each resize of a drag,
        // outside any flush: a message for each would outnumber the step's own.
        Image frame = new Image(device(), 16, 16);
        Image canvas = new Image(device(), 64, 64);
        GC gc = new GC(canvas);
        gc.drawImage(frame, 0, 0);
        gc.dispose();
        long id = FlutterBridge.id(frame.getImpl());
        frame.dispose();

        assertThat(comm.awaitSent("Image/forget", 300)).as("released before the next flush, on its own").isNegative();
        int before = comm.messages;
        Image[] drawn = new Image[1];
        FlutterBridge.inOneMessage(() -> drawn[0] = gcGradient(1, 30, new Color(10, 20, 30), new Color(200, 210, 220)));

        synchronized (comm) {
            assertThat(comm.forgotten).as("released by the next flush").containsExactly(id);
            assertThat(comm.messages - before).as("in that flush's message, with what it drew").isEqualTo(1);
        }
        drawn[0].dispose();
        canvas.dispose();
    }

    @Test
    void an_image_the_client_never_saw_is_not_released_there() throws InterruptedException {
        Image scratch = new Image(device(), 16, 16);
        scratch.dispose();

        assertThat(flushUntilSent("Image/forget", 500)).isNegative();
    }

    /**
     * Flushes, as each turn of the event loop does, until {@code eventName} has gone or
     * {@code timeoutMs} has passed: its index in what was sent, or -1. The release of an image is
     * enrolled for the next flush once every send already deferred has gone, which may be on
     * another thread, so the flush that carries it may not be the first.
     */
    private int flushUntilSent(String eventName, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (true) {
            FlutterBridge.inOneMessage(() -> {
            });
            int at = comm.awaitSent(eventName, 20);
            if (at >= 0 || System.currentTimeMillis() >= deadline) return at;
        }
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

    @Test
    void images_drawn_alike_are_named_alike() {
        // The client takes the pixels of a render named like one it already rasterised.
        Long first = contentKeyOf(gradient(1, 30, new Color(10, 20, 30), new Color(200, 210, 220)));
        Long second = contentKeyOf(gradient(1, 30, new Color(10, 20, 30), new Color(200, 210, 220)));

        assertThat(first).as("a small image drawn on a blank one is named").isNotNull().isNotZero();
        assertThat(second).isEqualTo(first);
    }

    @Test
    void images_a_gc_draws_alike_are_named_alike() {
        Long first = contentKeyOf(gcGradient(1, 30, new Color(10, 20, 30), new Color(200, 210, 220)));
        Long second = contentKeyOf(gcGradient(1, 30, new Color(10, 20, 30), new Color(200, 210, 220)));

        assertThat(first).as("a small image a GC drew on is named").isNotNull().isNotZero();
        assertThat(second).isEqualTo(first);
    }

    @Test
    void images_drawn_alike_while_a_flush_runs_are_named_alike() {
        // As a paint pass draws them, while a flush runs.
        Image[] drawn = new Image[1];
        comm.payloads.clear();
        dev.equo.swt.FlutterBridge.inOneMessage(() -> drawn[0] = gcGradient(1, 30, new Color(10, 20, 30), new Color(200, 210, 220)));
        Long first = contentKeyOf(drawn[0]);
        comm.payloads.clear();
        dev.equo.swt.FlutterBridge.inOneMessage(() -> drawn[0] = gcGradient(1, 30, new Color(10, 20, 30), new Color(200, 210, 220)));
        Long second = contentKeyOf(drawn[0]);

        assertThat(first).as("a render drawn during a flush is named").isNotNull().isNotZero();
        assertThat(second).isEqualTo(first);
    }

    @Test
    void an_image_drawn_while_a_flush_runs_travels_in_the_flush_message() {
        // Sent on its own, the render reached the client after the paint that fills with it, which
        // then found no render and decoded the image's data instead.
        boolean[] outBeforeTheFlushEnded = new boolean[1];
        Image[] drawn = new Image[1];
        int before = comm.messages;
        dev.equo.swt.FlutterBridge.inOneMessage(() -> {
            drawn[0] = gcGradient(1, 30, new Color(10, 20, 30), new Color(200, 210, 220));
            outBeforeTheFlushEnded[0] = comm.sent.stream().anyMatch(e -> e.endsWith("/gcDispose"));
        });

        assertThat(outBeforeTheFlushEnded[0]).as("the render's gcDispose waited for the flush's message").isFalse();
        assertThat(comm.sent).as("and went in it").anyMatch(e -> e.endsWith("/gcDispose"));
        assertThat(comm.messages).isGreaterThan(before);
        drawn[0].dispose();
    }

    @Test
    void an_image_drawn_while_a_flush_runs_is_one_message_with_it() {
        // GC/create and imageInit too, ahead of the ops they set up: a paint that makes a gradient
        // anew on every move of a drag costs no messages of its own.
        Image[] drawn = new Image[1];
        int before = comm.messages;
        dev.equo.swt.FlutterBridge.inOneMessage(
                () -> drawn[0] = gcGradient(1, 30, new Color(10, 20, 30), new Color(200, 210, 220)));

        assertThat(comm.messages - before).as("the render, created and drawn, in one message").isEqualTo(1);
        int created = comm.sent.indexOf("GC/create");
        int disposed = java.util.stream.IntStream.range(0, comm.sent.size())
                .filter(i -> comm.sent.get(i).endsWith("/gcDispose")).findFirst().orElse(-1);
        assertThat(created).as("GC/create is in it").isNotNegative();
        assertThat(disposed).as("ahead of the gcDispose that ends the render").isGreaterThan(created);
        drawn[0].dispose();
    }

    @Test
    void pixels_read_while_a_flush_runs_are_asked_for_after_the_render_they_read() {
        // A view whose Paint draws an image and then copies it, as a trace viewer's histogram does: the
        // copy reads the render's pixels back. The paint pass holds what it sends for one message, so
        // the render has to go out first, or the client is asked for a render it does not have and Java
        // waits out the read's timeout, three times, on the UI thread.
        Image image = new Image(device(), 16, 16);
        Image[] copy = new Image[1];
        dev.equo.swt.FlutterBridge.inOneMessage(() -> {
            GC gc = new GC(image);
            gc.fillRectangle(0, 0, 16, 16);
            gc.dispose();
            copy[0] = new Image(device(), image, SWT.IMAGE_COPY);
        });

        assertThat(comm.pixelsAskedAfterARender).as("the copy read the render's pixels back").isNotEmpty();
        assertThat(comm.pixelsAskedAfterARender).as("the client was asked for a render it had not been sent")
                .containsOnly(true);
        copy[0].dispose();
        image.dispose();
    }

    @Test
    void images_drawn_with_other_colors_or_sizes_are_named_apart() {
        Long gradient = contentKeyOf(gradient(1, 30, new Color(10, 20, 30), new Color(200, 210, 220)));

        assertThat(contentKeyOf(gradient(1, 30, new Color(10, 20, 30), new Color(200, 210, 221)))).isNotEqualTo(gradient);
        assertThat(contentKeyOf(gradient(1, 28, new Color(10, 20, 30), new Color(200, 210, 220)))).isNotEqualTo(gradient);
        assertThat(contentKeyOf(gradient(2, 30, new Color(10, 20, 30), new Color(200, 210, 220)))).isNotEqualTo(gradient);
    }

    @Test
    void an_image_drawn_over_its_last_render_is_not_named() {
        // What it shows depends on that render, whose pixels this side does not hold.
        Image image = new Image(device(), 16, 16);
        GC first = new GC(image);
        first.fillRectangle(0, 0, 16, 16);
        first.dispose();
        comm.payloads.clear();

        GC second = new GC(image);
        second.drawLine(0, 0, 15, 15);
        second.dispose();

        assertThat(contentKeys()).containsOnlyNulls();
        image.dispose();
    }

    @Test
    void a_large_image_is_not_named() {
        // Hashing its pixels would cost more than a read-back saves.
        assertThat(contentKeyOf(gradient(200, 200, new Color(10, 20, 30), new Color(200, 210, 220)))).isNull();
    }

    /** An image of {@code width} by {@code height} filled with a vertical gradient, as Eclipse Forms makes its title bars'. */
    private Image gradient(int width, int height, Color from, Color to) {
        comm.payloads.clear();
        return new Image(device(), (gc, w, h) -> {
            gc.setForeground(from);
            gc.setBackground(to);
            gc.fillGradientRectangle(0, 0, w, h, true);
        }, width, height);
    }

    /** {@link #gradient}, drawn by a GC on a new image, as Eclipse Forms' FormImages does. */
    private Image gcGradient(int width, int height, Color from, Color to) {
        comm.payloads.clear();
        Image image = new Image(device(), width, height);
        GC gc = new GC(image);
        gc.setForeground(from);
        gc.setBackground(to);
        gc.fillGradientRectangle(0, 0, width, height, true);
        gc.dispose();
        return image;
    }

    /** The content key the render of {@code image}, just drawn, was named by; null when unnamed. */
    private Long contentKeyOf(Image image) {
        List<Long> keys = contentKeys();
        image.dispose();
        assertThat(keys).as("the render's gcDispose").hasSize(1);
        return keys.get(0);
    }

    /** The content key of each gcDispose sent since the payloads were last cleared, null for one without. */
    private List<Long> contentKeys() {
        List<Long> keys = new ArrayList<>();
        for (Map<String, Object> dispose : WireValues.find(comm.carried(), m -> m.containsKey("retain") && m.containsKey("ref"))) {
            Object key = dispose.get("contentKey");
            keys.add(key == null ? null : ((Number) key).longValue());
        }
        return keys;
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
        /** The id each Image/forget names, whether it travelled on its own or in a batch. */
        final List<Long> forgotten = new ArrayList<>();
        int messages;
        final Map<String, Consumer<?>> handlers = new LinkedHashMap<>();
        /** For each copyArea request, whether Java was listening on the exact channel the client answers on. */
        final List<Boolean> copyAreaAnswerHeard = new ArrayList<>();
        /**
         * For each request for a render's pixels, whether a render had reached the client before it:
         * the client can only read back a render it was given.
         */
        final List<Boolean> pixelsAskedAfterARender = new ArrayList<>();

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
            List<byte[]> forgets = MessageBatch.EVENT.equals(eventName) ? GcFrames.payloadsOn(payload, "Image/forget")
                    : "Image/forget".equals(frame.event()) ? List.of(payload) : List.of();
            for (byte[] forget : forgets) forgotten.add(ByteBuffer.wrap(forget).getLong());
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
            } else if (eventName.equals("Image/requestPixels")) {
                pixelsAskedAfterARender.add(sent.stream().anyMatch(e -> e.endsWith("/gcDispose")));
                // Answered either way, so a test that gets the order wrong fails rather than waits.
                answer("/pixelsResult", png(16, 16));
            }
        }

        private static byte[] png(int width, int height) {
            ImageLoader loader = new ImageLoader();
            loader.data = new ImageData[] {
                    new ImageData(width, height, 32, new PaletteData(0xFF0000, 0xFF00, 0xFF)) };
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            loader.save(png, SWT.IMAGE_PNG);
            return png.toByteArray();
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
