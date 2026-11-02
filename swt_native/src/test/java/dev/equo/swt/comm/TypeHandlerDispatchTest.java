package dev.equo.swt.comm;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every widget of a type takes its events through one handler for the type, told the id and the
 * rest of the channel, so a widget registers nothing of its own and leaves nothing behind. A channel
 * with a handler of its own, or whose owner has one, goes to that handler.
 */
class TypeHandlerDispatchTest {

    private final Feeder comm = new Feeder();
    private final List<String> seen = new ArrayList<>();

    private CommService.TypeCallback<Object> recording(String... rests) {
        return new CommService.TypeCallback<>() {
            @Override
            public Object target(String type, long id, int channel, String rest) {
                return Arrays.asList(rests).contains(rest) ? type + " " + id : null;
            }

            @Override
            public void accept(Object target, String rest, Object value) {
                seen.add(target + " " + rest);
            }
        };
    }

    @Test
    void anEventReachesItsTypesHandlerWithTheIdAndChannel() {
        comm.onType(dev.equo.swt.comm.ChannelHeader.requireType("TreeItem"), Object.class, recording("Selection/Selection"));

        comm.deliver("TreeItem/12/Selection/Selection");
        comm.deliver("TreeItem/-34/Selection/Selection");

        assertThat(seen).containsExactly("TreeItem 12 Selection/Selection", "TreeItem -34 Selection/Selection");
    }

    @Test
    void aChannelTheTypeDoesNotTakeIsNotHandedToIt() {
        comm.onType(dev.equo.swt.comm.ChannelHeader.requireType("TreeItem"), Object.class, recording("Selection/Selection"));

        comm.deliver("TreeItem/12/Key/KeyDown");

        assertThat(seen).isEmpty();
    }

    @Test
    void anOwnersOrAChannelsOwnHandlerComesFirst() {
        comm.onType(dev.equo.swt.comm.ChannelHeader.requireType("Table"), Object.class, recording("PaintItem/PaintItem", "Selection/Selection"));
        dev.equo.swt.harness.Channels.on(comm, "Table/3/PaintItem/PaintItem", Object.class, value -> seen.add("own"));
        dev.equo.swt.harness.Channels.onOwner(comm, "Table/3", new String[] { "Selection/Selection" }, Object.class, (rest, value) -> seen.add("owner"));

        comm.deliver("Table/3/PaintItem/PaintItem");
        comm.deliver("Table/3/Selection/Selection");
        comm.deliver("Table/4/Selection/Selection");

        assertThat(seen).containsExactly("own", "owner", "Table 4 Selection/Selection");
    }

    @Test
    void registeringATypeAgainKeepsTheFirstHandler() {
        assertThat(comm.onType(dev.equo.swt.comm.ChannelHeader.requireType("Button"), Object.class, recording("Selection/Selection"))).isTrue();
        comm.onType(dev.equo.swt.comm.ChannelHeader.requireType("Button"), Object.class, new CommService.TypeCallback<>() {
            @Override
            public Object target(String type, long id, int channel, String rest) {
                return type;
            }

            @Override
            public void accept(Object target, String rest, Object value) {
                seen.add("second");
            }
        });

        comm.deliver("Button/1/Selection/Selection");

        assertThat(seen).containsExactly("Button 1 Selection/Selection");
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
