package dev.equo.swt.comm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What is sent before any client connects is held and handed to the first one. The client applies
 * frames in the order they arrive, and a later frame can be relative to an earlier one, so the held
 * frames have to reach it before anything sent once it has connected.
 */
class FirstClientOrderTest {

    /** A comm whose one client receives, in order, the first byte of every frame it is sent. */
    private static final class Comm extends AbstractBinaryCommService {
        final List<Integer> received = new ArrayList<>();

        @Override
        protected synchronized void broadcast(byte[] frame, int offset, int length) {
            received.add((int) frame[offset]);
        }

        void connect(Consumer<byte[]> toSession) {
            onClientConnected(toSession);
        }

        void send(int n) {
            sendFrame(new byte[] {(byte) n}, 0, 1);
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public void stop() {
        }
    }

    @Test
    @DisplayName("no two comms, nor two clients of one comm, share a client id")
    void clientIdsAreNeverShared() {
        Comm first = new Comm();
        Comm second = new Comm();
        assertThat(first.connectionId()).isNotEqualTo(second.connectionId());
        first.connect(frame -> {});
        int before = first.connectionId();
        first.connect(frame -> {});
        assertThat(first.connectionId()).isNotIn(before, second.connectionId());
    }

    @Test
    @DisplayName("a frame sent while the held ones are being handed over arrives after them")
    void heldFramesComeFirst() {
        Comm comm = new Comm();
        comm.send(1);
        comm.send(2);
        comm.send(3);

        comm.connect(frame -> {
            synchronized (comm) {
                comm.received.add((int) frame[0]);
            }
            // Sent the moment the first held frame is out, as the Display thread would be.
            if (frame[0] == 1) comm.send(4);
        });

        assertThat(comm.received).containsExactly(1, 2, 3, 4);
    }

    @Test
    @DisplayName("frames sent from another thread during the hand-over arrive after the held ones")
    void heldFramesComeFirstAcrossThreads() throws InterruptedException {
        Comm comm = new Comm();
        for (int i = 1; i <= 50; i++) comm.send(i);
        CountDownLatch handingOver = new CountDownLatch(1);
        CountDownLatch sent = new CountDownLatch(1);
        Thread sender = new Thread(() -> {
            try {
                handingOver.await();
                comm.send(51);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                sent.countDown();
            }
        });
        sender.start();

        comm.connect(frame -> {
            synchronized (comm) {
                comm.received.add((int) frame[0]);
            }
            if (frame[0] == 1) {
                handingOver.countDown();
                try {
                    // Long enough for the sender to have sent, when nothing makes it wait for the hand-over.
                    sent.await(200, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        sent.await(5, TimeUnit.SECONDS);
        sender.join(5000);

        List<Integer> expected = new ArrayList<>();
        for (int i = 1; i <= 51; i++) expected.add(i);
        assertThat(comm.received).containsExactlyElementsOf(expected);
    }
}
