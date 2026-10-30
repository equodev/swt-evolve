package dev.equo.swt.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * What a message opens with: its channel, as numbers. Both sides number channels and widget types
 * alike (wire-channels.txt and wire-types.txt, generated), so a channel travels as them rather than
 * as its name.
 *
 * <p>The first varint says which form follows:
 * <ul>
 * <li>odd: a channel addressed to one widget, {@code Type/id/name}: the name's number is the varint
 * shifted right, then the type's number and the id (zigzag) follow;</li>
 * <li>even and not 0: a global channel, its number shifted left one;</li>
 * <li>0: a name nothing numbers, spelled out as its length and its UTF-8 bytes.</li>
 * </ul>
 */
public final class ChannelHeader {

    private static final Map<String, Integer> CHANNELS = new HashMap<>();
    private static final Map<String, Integer> TYPES = new HashMap<>();
    private static String[] channelNames = new String[0];
    private static String[] typeNames = new String[0];

    static {
        channelNames = load("/dev/equo/swt/wire-channels.txt", CHANNELS);
        typeNames = load("/dev/equo/swt/wire-types.txt", TYPES);
    }

    private ChannelHeader() {
    }

    private static String[] load(String resource, Map<String, Integer> numbers) {
        int max = 0;
        try (InputStream s = ChannelHeader.class.getResourceAsStream(resource)) {
            if (s == null) return new String[0];
            BufferedReader reader = new BufferedReader(new InputStreamReader(s, StandardCharsets.UTF_8));
            for (String line; (line = reader.readLine()) != null; ) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                int space = line.indexOf(' ');
                int number = Integer.parseInt(space < 0 ? line : line.substring(0, space));
                numbers.put(space < 0 ? "" : line.substring(space + 1), number);
                max = Math.max(max, number);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String[] names = new String[max + 1];
        numbers.forEach((name, number) -> names[number] = name);
        return names;
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

    /** The bytes a message on {@code channel} opens with. */
    public static byte[] encode(String channel) {
        byte[] out = new byte[24];
        int size = 0;
        int slash = channel.indexOf('/');
        if (slash > 0) {
            int second = channel.indexOf('/', slash + 1);
            long id = parseId(channel, slash + 1, second < 0 ? channel.length() : second);
            if (id != Long.MIN_VALUE) {
                Integer type = TYPES.get(channel.substring(0, slash));
                Integer name = CHANNELS.get(second < 0 ? "" : channel.substring(second + 1));
                if (type != null && name != null) {
                    size = varint(out, size, ((long) name << 1) | 1);
                    size = varint(out, size, type);
                    size = varint(out, size, (id << 1) ^ (id >> 63));
                    return java.util.Arrays.copyOf(out, size);
                }
            }
        }
        Integer global = CHANNELS.get(channel);
        if (global != null && global != 0) {
            size = varint(out, size, (long) global << 1);
            return java.util.Arrays.copyOf(out, size);
        }
        byte[] name = channel.getBytes(StandardCharsets.UTF_8);
        byte[] literal = new byte[1 + 5 + name.length];
        size = varint(literal, 0, 0);
        size = varint(literal, size, name.length);
        System.arraycopy(name, 0, literal, size, name.length);
        return java.util.Arrays.copyOf(literal, size + name.length);
    }

    /** The decimal id in {@code s[from, to)}, or {@link Long#MIN_VALUE} when it is not one. */
    private static long parseId(String s, int from, int to) {
        if (from >= to || to - from > 20) return Long.MIN_VALUE;
        boolean negative = s.charAt(from) == '-';
        int i = negative ? from + 1 : from;
        if (i >= to) return Long.MIN_VALUE;
        long v = 0;
        for (; i < to; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return Long.MIN_VALUE;
            v = v * 10 + (c - '0');
        }
        return negative ? -v : v;
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

    /** Reads the header of the message in {@code data[offset, end)}, or null when it is malformed. */
    public static Decoded decode(byte[] data, int offset, int end) {
        int[] at = {offset};
        long first = readVarint(data, at, end);
        if (first < 0) return null;
        if (first == 0) {
            long length = readVarint(data, at, end);
            if (length < 0 || at[0] + length > end) return null;
            String name = new String(data, at[0], (int) length, StandardCharsets.UTF_8);
            return new Decoded(name, at[0] + (int) length);
        }
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
