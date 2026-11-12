package dev.equo.swt.comm;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The client sends what it sent in one turn as one batch, laid out as {@link MessageBatch} lays one
 * out: each frame in it is dispatched as if it had come on its own, in the order it was sent.
 */
class ClientBatchDispatchTest {

    private final Feeder comm = new Feeder();
    private final List<String> seen = new ArrayList<>();

    private CommService.TypeCallback<Object> recording() {
        return new CommService.TypeCallback<>() {
            @Override
            public Object target(String type, long id, int channel, String rest) {
                return type + " " + id;
            }

            @Override
            public void accept(Object target, String rest, Object value) {
                seen.add(target + " " + rest);
            }
        };
    }

    @Test
    void everyFrameOfABatchReachesItsHandlerInOrder() {
        comm.onType(ChannelHeader.requireType("Button"), Object.class, recording());
        comm.onType(ChannelHeader.requireType("TreeItem"), Object.class, recording());
        MessageBatch batch = new MessageBatch();
        dev.equo.swt.harness.Channels.add(batch, "Button/1/Selection/Selection", new byte[0]);
        dev.equo.swt.harness.Channels.add(batch, "TreeItem/12/Selection/Selection", new byte[0]);
        dev.equo.swt.harness.Channels.add(batch, "Button/-2/Selection/Selection", new byte[0]);

        comm.deliver(sent(batch));

        assertThat(seen).containsExactly(
                "Button 1 Selection/Selection", "TreeItem 12 Selection/Selection", "Button -2 Selection/Selection");
    }

    @Test
    void aBatchWhoseEntryOverrunsItDispatchesWhatCameBefore() {
        comm.onType(ChannelHeader.requireType("Button"), Object.class, recording());
        MessageBatch batch = new MessageBatch();
        dev.equo.swt.harness.Channels.add(batch, "Button/1/Selection/Selection", new byte[0]);
        dev.equo.swt.harness.Channels.add(batch, "Button/2/Selection/Selection", new byte[] { 1, 2, 3 });
        byte[] frame = sent(batch);

        comm.deliver(Arrays.copyOf(frame, frame.length - 1));

        assertThat(seen).containsExactly("Button 1 Selection/Selection");
    }

    @Test
    void anEntryWhoseHandlerThrowsLeavesTheRestToTheirHandlersAndThrowsAfterThem() {
        comm.onType(ChannelHeader.requireType("Button"), Object.class, new CommService.TypeCallback<>() {
            @Override
            public Object target(String type, long id, int channel, String rest) {
                return type + " " + id;
            }

            @Override
            public void accept(Object target, String rest, Object value) {
                if (target.equals("Button 1")) throw new IllegalStateException("handler of Button 1");
                seen.add(target + " " + rest);
            }
        });
        MessageBatch batch = new MessageBatch();
        dev.equo.swt.harness.Channels.add(batch, "Button/1/Selection/Selection", new byte[0]);
        dev.equo.swt.harness.Channels.add(batch, "Button/2/Selection/Selection", new byte[0]);
        dev.equo.swt.harness.Channels.add(batch, "Button/3/Selection/Selection", new byte[0]);
        byte[] frame = sent(batch);

        assertThatThrownBy(() -> comm.deliver(frame)).hasMessage("handler of Button 1");

        assertThat(seen).containsExactly("Button 2 Selection/Selection", "Button 3 Selection/Selection");
    }

    /** What one batch put on the wire. */
    private static byte[] sent(MessageBatch batch) {
        List<byte[]> frames = new ArrayList<>();
        batch.sendTo(new dev.equo.swt.harness.RecordingComm() {
            @Override
            public void sendFrame(byte[] frame, int offset, int length) {
                frames.add(Arrays.copyOfRange(frame, offset, offset + length));
            }
        });
        assertThat(frames).hasSize(1);
        return frames.get(0);
    }

    /** The real dispatcher, fed frames directly instead of over a socket. */
    private static final class Feeder extends AbstractBinaryCommService {
        void deliver(byte[] frame) {
            onBinaryMessage(frame, 0, frame.length);
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
