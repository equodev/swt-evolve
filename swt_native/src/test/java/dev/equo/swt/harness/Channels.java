package dev.equo.swt.harness;

import dev.equo.swt.comm.ChannelHeader;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.comm.MessageBatch;

/**
 * A channel's name, {@code Type/id/channel} or a global name, as the numbers the wire addresses it by,
 * and back: tests name what they send and expect by name, production code only by number.
 */
public final class Channels {

    private Channels() {
    }

    /** The numbers {@code name} stands for: type (0 for a global channel), id and channel. */
    public static long[] of(String name) {
        int slash = name.indexOf('/');
        if (slash > 0) {
            int second = name.indexOf('/', slash + 1);
            String id = name.substring(slash + 1, second < 0 ? name.length() : second);
            if (id.matches("-?[0-9]+")) {
                return new long[] {
                        ChannelHeader.requireType(name.substring(0, slash)),
                        Long.parseLong(id),
                        ChannelHeader.requireChannel(second < 0 ? "" : name.substring(second + 1)),
                };
            }
        }
        return new long[] {0, 0, ChannelHeader.requireChannel(name)};
    }

    /** The name the channel these numbers address has. */
    public static String name(int type, long id, int channel) {
        String rest = ChannelHeader.name(channel);
        if (type == 0) return rest;
        String owner = ChannelHeader.typeName(type) + "/" + id;
        return rest.isEmpty() ? owner : owner + "/" + rest;
    }

    /** The header a message on {@code name} opens with. */
    public static byte[] header(String name) {
        long[] n = of(name);
        return ChannelHeader.encode((int) n[0], n[1], (int) n[2]);
    }

    /** Sends {@code payload} on {@code name}. */
    public static void send(CommService comm, String name, byte[] payload) {
        long[] n = of(name);
        comm.send((int) n[0], n[1], (int) n[2], payload);
    }

    /** Adds {@code payload} to {@code batch} on {@code name}. */
    public static void add(MessageBatch batch, String name, byte[] payload) {
        long[] n = of(name);
        batch.add((int) n[0], n[1], (int) n[2], payload, 0, payload == null ? 0 : payload.length);
    }

    /** Registers {@code callback} on {@code name}. */
    public static <T> void on(CommService comm, String name, Class<T> cls, java.util.function.Consumer<T> callback) {
        long[] n = of(name);
        comm.on((int) n[0], n[1], (int) n[2], cls, callback);
    }

    /** {@link CommService#onOwner} for the owner {@code Type/id} and the channels {@code rests} name. */
    public static <T> void onOwner(CommService comm, String owner, String[] rests, Class<T> cls,
            java.util.function.BiConsumer<String, T> callback) {
        long[] n = of(owner);
        int[] channels = new int[rests.length];
        for (int i = 0; i < rests.length; i++) channels[i] = ChannelHeader.requireChannel(rests[i]);
        comm.onOwner((int) n[0], n[1], channels, cls, callback);
    }

    /** {@link CommService#removeOwner} for the owner {@code Type/id}. */
    public static void removeOwner(CommService comm, String owner) {
        long[] n = of(owner);
        comm.removeOwner((int) n[0], n[1]);
    }

    /** A frame on {@code name} carrying {@code payload}. */
    public static byte[] frame(String name, byte[] payload) {
        byte[] header = header(name);
        byte[] frame = java.util.Arrays.copyOf(header, header.length + payload.length);
        System.arraycopy(payload, 0, frame, header.length, payload.length);
        return frame;
    }
}
