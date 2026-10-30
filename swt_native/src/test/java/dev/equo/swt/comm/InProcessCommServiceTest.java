package dev.equo.swt.comm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The in-process channel, as far as Java goes: the native bridge is replaced by what it would call.
 * Frames written before a client attaches reach it once it does, and what a client sends reaches
 * its handler off the thread that handed it over, in the order it was sent.
 */
class InProcessCommServiceTest {

    /** Records what would be posted to the attached clients. */
    private static final class Recording extends InProcessCommService {
        final List<String> posted = new CopyOnWriteArrayList<>();

        @Override
        protected void broadcast(byte[] frame, int offset, int length) {
            ChannelHeader.Decoded header = ChannelHeader.decode(frame, offset, offset + length);
            posted.add(header.channel());
        }
    }

    private final Recording comm = new Recording();

    @AfterEach
    void stop() {
        comm.stop();
    }

    @Test
    void itsPortIsNegativeSoTheClientAttachesInsteadOfConnecting() {
        assertThat(comm.getPort()).isNegative();
    }

    @Test
    void framesWrittenBeforeAClientAttachesReachItOnceItDoes() throws Exception {
        comm.send("first");
        comm.send("second");
        assertThat(comm.posted).isEmpty();

        InProcessCommService.clientsChanged(-comm.getPort(), 1);
        comm.send("third");

        waitFor(() -> comm.posted.size() == 3);
        assertThat(comm.posted).containsExactly("first", "second", "third");
        assertThat(comm.hasClient()).isTrue();
    }

    @Test
    void whatAClientSendsReachesItsHandlerOffTheSendingThreadInOrder() throws Exception {
        List<String> seen = new CopyOnWriteArrayList<>();
        List<Thread> threads = new CopyOnWriteArrayList<>();
        comm.on("Button/7/Selection/Selection", byte[].class, payload -> {
            seen.add(new String(payload, StandardCharsets.UTF_8));
            threads.add(Thread.currentThread());
        });

        for (String payload : new String[] { "a", "b", "c" }) {
            InProcessCommService.fromClient(-comm.getPort(), frame("Button/7/Selection/Selection", payload));
        }

        waitFor(() -> seen.size() == 3);
        assertThat(seen).containsExactly("a", "b", "c");
        assertThat(threads).doesNotContain(Thread.currentThread());
    }

    @Test
    void aStoppedChannelTakesNothingMore() throws Exception {
        List<String> seen = new ArrayList<>();
        comm.on("Shell/1/Shell/Close", byte[].class, payload -> seen.add("closed"));
        comm.stop();

        InProcessCommService.fromClient(-comm.getPort(), frame("Shell/1/Shell/Close", "x"));

        TimeUnit.MILLISECONDS.sleep(100);
        assertThat(seen).isEmpty();
    }

    private static byte[] frame(String channel, String payload) {
        byte[] header = CommService.frameHeader(channel);
        byte[] body = payload.getBytes(StandardCharsets.UTF_8);
        byte[] frame = Arrays.copyOf(header, header.length + body.length);
        System.arraycopy(body, 0, frame, header.length, body.length);
        return frame;
    }

    private static void waitFor(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) TimeUnit.MILLISECONDS.sleep(5);
    }
}
