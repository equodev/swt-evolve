package dev.equo.swt.comm;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * What a widget channel's name costs on the Java side, per message: spelled out and parsed back
 * (sending), or built from the header's numbers and looked up (receiving), against working with the
 * numbers directly. A measurement, printed rather than asserted.
 */
@Tag("bench")
class ChannelNameCostBench {

    private static final int N = 2_000_000;
    private static final com.sun.management.ThreadMXBean THREADS =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    private static long sink;

    private interface Run {
        void run(int i);
    }

    private static void report(String name, Run run) {
        for (int i = 0; i < 200_000; i++) run.run(i);
        long[] times = new long[5];
        long bytes = 0;
        for (int r = 0; r < times.length; r++) {
            long before = THREADS.getCurrentThreadAllocatedBytes();
            long t0 = System.nanoTime();
            for (int i = 0; i < N; i++) run.run(i);
            times[r] = System.nanoTime() - t0;
            bytes = THREADS.getCurrentThreadAllocatedBytes() - before;
        }
        java.util.Arrays.sort(times);
        System.out.printf("[channel-name-bench] %s: %.0f ns, %.1f B per message%n", name,
                times[2] / (double) N, bytes / (double) N);
    }

    private static int varint(byte[] out, int at, long v) {
        while ((v & ~0x7FL) != 0) {
            out[at++] = (byte) ((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out[at++] = (byte) v;
        return at;
    }

    @Test
    void channelNames() {
        String paint = "Paint/Paint";
        int paintNumber = ChannelHeader.number(paint);
        byte[] sample = dev.equo.swt.harness.Channels.header("Button/1/" + paint);
        int[] pos = {0};
        read(sample, pos);
        int button = (int) read(sample, pos);
        long[] ids = new long[200];
        for (int i = 0; i < ids.length; i++) ids[i] = 700001 + i * 7L;

        // Sending: the name spelled out, as FlutterBridge.event builds it, then encoded back to numbers.
        report("send, spelled out and parsed", i -> {
            String channel = "Button" + "/" + ids[i % ids.length] + "/" + paint;
            sink += dev.equo.swt.harness.Channels.header(channel).length;
        });
        byte[] header = new byte[24];
        report("send, from numbers", i -> {
            int at = varint(header, 0, ((long) paintNumber << 1) | 1);
            at = varint(header, at, button);
            long id = ids[i % ids.length];
            sink += varint(header, at, (id << 1) ^ (id >> 63));
        });

        // Receiving: the header decoded into a name, then the handler looked up by it.
        byte[][] frames = new byte[ids.length][];
        Map<String, Object> byName = new HashMap<>();
        Map<Long, Object> byNumber = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            frames[i] = dev.equo.swt.harness.Channels.header("Button/" + ids[i] + "/" + paint);
            byName.put("Button/" + ids[i] + "/" + paint, new Object());
            byNumber.put(ids[i] * 4096 + paintNumber, new Object());
        }
        report("receive, decoded to a name and looked up", i -> {
            byte[] f = frames[i % frames.length];
            ChannelHeader.Decoded d = ChannelHeader.decode(f, 0, f.length);
            sink += byName.get(d.channel()).hashCode();
        });
        report("receive, by its numbers", i -> {
            byte[] f = frames[i % frames.length];
            int[] at = {0};
            long first = read(f, at);
            read(f, at);
            long z = read(f, at);
            long id = (z >>> 1) ^ -(z & 1);
            sink += byNumber.get(id * 4096 + (first >>> 1)).hashCode();
        });
        System.out.println("[channel-name-bench] (sink " + (sink & 1) + ")");
    }

    /** A comm with no transport: what it sends is dropped, what it is handed is dispatched. */
    private static final class Loopback extends AbstractBinaryCommService {
        Loopback() {
            onClientConnected(frame -> { });
        }

        @Override
        protected void broadcast(byte[] frame, int offset, int length) {
            sink += length;
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public void stop() {
        }

        void receive(byte[] frame) {
            onBinaryMessage(frame, 0, frame.length);
        }
    }

    /** The service's own paths: a received message to its handler, and a send by name. */
    @Test
    void service() {
        Loopback comm = new Loopback();
        String event = "Selection/Selection";
        long[] ids = new long[200];
        byte[][] frames = new byte[ids.length][];
        byte[][] typed = new byte[ids.length][];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = 700001 + i * 7L;
            dev.equo.swt.harness.Channels.on(comm, "Button/" + ids[i] + "/" + event, Void.class, v -> sink++);
            frames[i] = dev.equo.swt.harness.Channels.header("Button/" + ids[i] + "/" + event);
            typed[i] = dev.equo.swt.harness.Channels.header("Label/" + ids[i] + "/" + event);
        }
        comm.onType(ChannelHeader.requireType("Label"), Void.class, new CommService.TypeCallback<Void>() {
            @Override
            public Object target(String type, long id, int channel, String rest) {
                return rest.equals(event) ? id : null;
            }

            @Override
            public void accept(Object id, String rest, Void value) {
                sink += (Long) id;
            }
        });
        report("service receive, a widget's own handler", i -> comm.receive(frames[i % frames.length]));
        report("service receive, its type's handler", i -> comm.receive(typed[i % typed.length]));
        report("service send, by name", i -> dev.equo.swt.harness.Channels.send(comm, "Button/" + ids[i % ids.length] + "/" + event, null));
        int button = ChannelHeader.requireType("Button");
        int selection = ChannelHeader.number(event);
        report("service send, by numbers", i -> comm.send(button, ids[i % ids.length], selection, null));
        System.out.println("[channel-name-bench] (sink " + (sink & 1) + ")");
    }

    private static long read(byte[] data, int[] at) {
        long v = 0;
        for (int shift = 0; ; shift += 7) {
            int b = data[at[0]++] & 0xFF;
            v |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return v;
        }
    }
}
