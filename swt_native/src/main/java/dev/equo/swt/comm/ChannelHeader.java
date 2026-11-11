package dev.equo.swt.comm;

import java.util.HashMap;
import java.util.Map;

/**
 * What a message opens with: its channel, as numbers. Both sides number channels and widget types
 * alike ({@link dev.equo.swt.WireTable}, generated), so a channel travels as them rather than as its
 * name.
 *
 * <p>The first varint says which form follows:
 * <ul>
 * <li>odd: a channel addressed to one widget, {@code Type/id/name}: the name's number is the varint
 * shifted right, then the type's number and the id (zigzag) follow;</li>
 * <li>even: a global channel, its number shifted left one.</li>
 * </ul>
 * Every channel and type is numbered when the code is generated: a name without a number is a
 * mistake, reported where it is used, never sent spelled out.
 */
public final class ChannelHeader {

    private static final Map<String, Integer> CHANNELS = numbers(dev.equo.swt.WireTable.CHANNELS);
    private static final Map<String, Integer> TYPES = numbers(dev.equo.swt.WireTable.TYPES);
    private static final String[] channelNames = dev.equo.swt.WireTable.CHANNELS;
    private static final String[] typeNames = dev.equo.swt.WireTable.TYPES;

    private ChannelHeader() {
    }

    /** Each of {@code names} by its index, the number it stands for. */
    private static Map<String, Integer> numbers(String[] names) {
        Map<String, Integer> numbers = new HashMap<>();
        for (int i = 1; i < names.length; i++) numbers.put(names[i], i);
        return numbers;
    }

    /** The number both sides know the channel {@code name} by, or 0 when it has none. */
    public static int number(String name) {
        Integer number = CHANNELS.get(name);
        return number == null ? 0 : number;
    }

    /** The channel numbered {@code number}, or null. */
    public static String name(int number) {
        return number >= 0 && number < channelNames.length ? channelNames[number] : null;
    }

    /** The number of the type {@code name}, or 0 when it has none. */
    public static int type(String name) {
        Integer number = TYPES.get(name);
        return number == null ? 0 : number;
    }

    /**
     * The number of the type {@code name}.
     *
     * @throws IllegalArgumentException when it has none.
     */
    public static int requireType(String name) {
        Integer number = TYPES.get(name);
        if (number == null) throw unnumbered("type", name);
        return number;
    }

    /**
     * The number of the channel {@code name}, the part of a widget's channel after its id, or a global one.
     *
     * @throws IllegalArgumentException when it has none.
     */
    public static int requireChannel(String name) {
        Integer number = CHANNELS.get(name);
        if (number == null || number == 0) throw unnumbered("channel", name);
        return number;
    }

    private static IllegalArgumentException unnumbered(String what, String name) {
        return new IllegalArgumentException("the " + what + " \"" + name
                + "\" has no wire number: add it to the generator's channels or types");
    }

    /** The widget type numbered {@code number}, or null. */
    public static String typeName(int number) {
        return number >= 0 && number < typeNames.length ? typeNames[number] : null;
    }

    /**
     * Writes into {@code out} at {@code at} the header of the channel numbered {@code channel} of the
     * widget of type {@code type} numbered {@code id}, or of the global channel numbered
     * {@code channel} when {@code type} is 0, and returns where it ends: at most
     * {@link #MAX_NUMBERED} bytes.
     */
    public static int write(byte[] out, int at, int type, long id, int channel) {
        if (type == 0) return varint(out, at, (long) channel << 1);
        at = varint(out, at, ((long) channel << 1) | 1);
        at = varint(out, at, type);
        return varint(out, at, (id << 1) ^ (id >> 63));
    }

    /** How many bytes {@link #write} writes for these numbers. */
    public static int length(int type, long id, int channel) {
        if (type == 0) return varintLength((long) channel << 1);
        return varintLength(((long) channel << 1) | 1) + varintLength(type) + varintLength((id << 1) ^ (id >> 63));
    }

    private static int varintLength(long v) {
        int n = 1;
        while ((v & ~0x7FL) != 0) {
            v >>>= 7;
            n++;
        }
        return n;
    }

    /** The longest header {@link #write} writes. */
    public static final int MAX_NUMBERED = 3 + 3 + 10;

    /** The header of the channel numbered {@code channel} of the widget of type {@code type} numbered {@code id}. */
    public static byte[] encode(int type, long id, int channel) {
        byte[] out = new byte[length(type, id, channel)];
        write(out, 0, type, id, channel);
        return out;
    }

    private static int varint(byte[] out, int at, long v) {
        while ((v & ~0x7FL) != 0) {
            out[at++] = (byte) ((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out[at++] = (byte) v;
        return at;
    }

    /** A decoded header: the channel's name and where the payload starts. */
    public static final class Decoded {
        private final String channel;
        private final int payloadStart;

        Decoded(String channel, int payloadStart) {
            this.channel = channel;
            this.payloadStart = payloadStart;
        }

        public String channel() {
            return channel;
        }

        public int payloadStart() {
            return payloadStart;
        }
    }

    /**
     * Reads the header of the message in {@code data[offset, end)}, naming its channel, or null when
     * it is malformed: for logs and tests, which show a message by name. Messages are dispatched by
     * their numbers.
     */
    public static Decoded decode(byte[] data, int offset, int end) {
        int[] at = {offset};
        long first = readVarint(data, at, end);
        if (first <= 0) return null;
        int number = (int) (first >>> 1);
        if ((first & 1) == 0) {
            return number < channelNames.length && channelNames[number] != null
                    ? new Decoded(channelNames[number], at[0]) : null;
        }
        long type = readVarint(data, at, end);
        long zigzag = readVarint(data, at, end);
        if (type < 0 || zigzag < 0 || type >= typeNames.length || typeNames[(int) type] == null
                || number >= channelNames.length || channelNames[number] == null) {
            return null;
        }
        long id = (zigzag >>> 1) ^ -(zigzag & 1);
        String name = channelNames[number];
        String channel = typeNames[(int) type] + "/" + id + (name.isEmpty() ? "" : "/" + name);
        return new Decoded(channel, at[0]);
    }

    private static long readVarint(byte[] data, int[] at, int end) {
        long v = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            if (at[0] >= end) return -1;
            int b = data[at[0]++] & 0xFF;
            v |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return v;
        }
        return -1;
    }
}
