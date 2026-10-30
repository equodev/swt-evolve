package dev.equo.swt.comm;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A widget takes most of its events through one handler for all of its channels. A channel that also
 * has a handler of its own goes to that one, and both go when the widget does.
 */
class OwnerHandlerDispatchTest {

    private final Feeder comm = new Feeder();
    private final List<String> seen = new ArrayList<>();

    @Test
    void anEventReachesTheOwnersHandlerWithItsChannel() {
        dev.equo.swt.harness.Channels.onOwner(comm, "Button/7", new String[] { "Mouse/MouseDown", "Selection/Selection" }, Object.class,
                (channel, value) -> seen.add(channel));

        comm.deliver("Button/7/Selection/Selection");
        comm.deliver("Button/7/Mouse/MouseDown");

        assertThat(seen).containsExactly("Selection/Selection", "Mouse/MouseDown");
    }

    @Test
    void aChannelWithAHandlerOfItsOwnGoesToThatHandler() {
        dev.equo.swt.harness.Channels.onOwner(comm, "Table/3", new String[] { "PaintItem/PaintItem" }, Object.class, (channel, value) -> seen.add("owner"));
        dev.equo.swt.harness.Channels.on(comm, "Table/3/PaintItem/PaintItem", Object.class, value -> seen.add("own"));

        comm.deliver("Table/3/PaintItem/PaintItem");

        assertThat(seen).containsExactly("own");
    }

    @Test
    void aChannelTheOwnerDoesNotTakeIsNotHandedToIt() {
        dev.equo.swt.harness.Channels.onOwner(comm, "Label/5", new String[] { "Mouse/MouseDown" }, Object.class, (channel, value) -> seen.add(channel));

        comm.deliver("Label/5/Key/KeyDown");

        assertThat(seen).isEmpty();
    }

    @Test
    void removingTheOwnerRemovesBothKindsOfHandler() {
        dev.equo.swt.harness.Channels.onOwner(comm, "Shell/9", new String[] { "Shell/Close" }, Object.class, (channel, value) -> seen.add("owner"));
        dev.equo.swt.harness.Channels.on(comm, "Shell/9/Shell/SetBounds", Object.class, value -> seen.add("own"));

        dev.equo.swt.harness.Channels.removeOwner(comm, "Shell/9");
        comm.deliver("Shell/9/Shell/Close");
        comm.deliver("Shell/9/Shell/SetBounds");

        assertThat(seen).isEmpty();
    }

    @Test
    void removingAnOwnerByTypeAndIdRemovesItsHandlers() {
        dev.equo.swt.harness.Channels.onOwner(comm, "Shell/9", new String[] { "Shell/Close" }, Object.class, (channel, value) -> seen.add("owner"));
        dev.equo.swt.harness.Channels.on(comm, "Shell/9/Shell/SetBounds", Object.class, value -> seen.add("own"));
        dev.equo.swt.harness.Channels.on(comm, "Shell/10/Shell/SetBounds", Object.class, value -> seen.add("other"));

        comm.removeOwner(dev.equo.swt.comm.ChannelHeader.requireType("Shell"), 9);
        comm.deliver("Shell/9/Shell/Close");
        comm.deliver("Shell/9/Shell/SetBounds");
        comm.deliver("Shell/10/Shell/SetBounds");

        assertThat(seen).containsExactly("other");
    }

    @Test
    void theLastOwnerOfATypeGoesTooOnceItsOnlyHandlerIsRemoved() {
        dev.equo.swt.harness.Channels.on(comm, "Label/5/Mouse/MouseDown", Object.class, value -> seen.add("first"));
        { long[] n_ = dev.equo.swt.harness.Channels.of("Label/5/Mouse/MouseDown"); comm.remove((int) n_[0], n_[1], (int) n_[2]); }
        dev.equo.swt.harness.Channels.on(comm, "Label/6/Mouse/MouseDown", Object.class, value -> seen.add("second"));

        comm.removeOwner(dev.equo.swt.comm.ChannelHeader.requireType("Label"), 6);
        comm.deliver("Label/6/Mouse/MouseDown");

        assertThat(seen).isEmpty();
    }

    /** The real dispatcher, fed frames directly instead of over a socket. */
    private static final class Feeder extends AbstractBinaryCommService {
        void deliver(String channel) {
            byte[] header = dev.equo.swt.harness.Channels.header(channel);
            byte[] frame = Arrays.copyOf(header, header.length);
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
