package dev.equo.swt.comm;

import dev.equo.swt.harness.CarriedContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a client sends, and its connecting, are handled under the context of the thread that built
 * the comm, on a transport thread that has none; with no capturer installed they run as they are.
 */
class CallerContextDispatchTest {

    private final List<String> seen = new ArrayList<>();

    @AfterEach
    void clear() {
        CarriedContext.uninstall();
    }

    private Feeder commBuiltUnder(String context) {
        CarriedContext.set(context);
        Feeder comm = new Feeder();
        dev.equo.swt.harness.Channels.on(comm, "Button/1/Selection/Selection", Object.class, value -> seen.add("message under " + CarriedContext.get()));
        CarriedContext.remove();
        return comm;
    }

    private static void onATransportThread(Runnable work) throws InterruptedException {
        Thread transport = new Thread(work);
        transport.start();
        transport.join();
    }

    @Test
    void aMessageIsHandledUnderTheContextOfTheThreadThatBuiltTheComm() throws InterruptedException {
        CarriedContext.install();
        Feeder first = commBuiltUnder("first");
        Feeder second = commBuiltUnder("second");

        onATransportThread(() -> {
            first.deliver("Button/1/Selection/Selection");
            second.deliver("Button/1/Selection/Selection");
            seen.add("after under " + CarriedContext.get());
        });

        assertThat(seen).containsExactly("message under first", "message under second", "after under null");
    }

    @Test
    void aClientConnectsUnderTheContextOfTheThreadThatBuiltTheComm() throws InterruptedException {
        CarriedContext.install();
        CarriedContext.set("first");
        Feeder comm = new Feeder();
        comm.send(0, 0, 1, null);
        CarriedContext.remove();

        onATransportThread(() -> comm.connect(frame -> seen.add("held frame under " + CarriedContext.get())));

        assertThat(seen).containsExactly("held frame under first");
    }

    @Test
    void aCommBuiltOnAThreadWithNoContextHandlesMessagesAsTheyAre() throws InterruptedException {
        CarriedContext.install();
        Feeder comm = commBuiltUnder(null);

        onATransportThread(() -> {
            CarriedContext.set("transport");
            comm.deliver("Button/1/Selection/Selection");
        });

        assertThat(seen).containsExactly("message under transport");
    }

    @Test
    void withNoCapturerMessagesAreHandledAsTheyAre() throws InterruptedException {
        Feeder comm = commBuiltUnder("first");

        onATransportThread(() -> comm.deliver("Button/1/Selection/Selection"));

        assertThat(seen).containsExactly("message under null");
    }

    /** The real dispatcher, fed frames directly instead of over a socket. */
    private static final class Feeder extends AbstractBinaryCommService {
        void deliver(String channel) {
            byte[] header = dev.equo.swt.harness.Channels.header(channel);
            byte[] frame = Arrays.copyOf(header, header.length);
            onBinaryMessage(frame, 0, frame.length);
        }

        void connect(Consumer<byte[]> sendToSession) {
            onClientConnected(sendToSession);
        }

        @Override
        protected void broadcast(byte[] frame, int offset, int length) {
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
