package dev.equo.swt.comm;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Maps, lists and scalars in binary, what a payload the wire has no class encoding for travels as.
 * Every value carries a tag; a map's keys go by the number both sides know them by
 * ({@link dev.equo.swt.WireTable}, generated), and only one nothing numbers is spelled out, once per payload. Text is the only
 * string left.
 *
 * <p>A payload opens with {@link #MARK}, which UTF-8 text cannot start with.
 */
public final class TaggedValues {

    public static final int MARK = 0xF5;

    static final int NULL = 0;
    static final int TRUE = 1;
    static final int FALSE = 2;
    static final int INT = 3;
    static final int DOUBLE = 4;
    static final int TEXT = 5;
    static final int BYTES = 6;
    static final int LIST = 7;
    static final int MAP = 8;

    private static final Map<String, Integer> KEYS = new HashMap<>();
    private static final String[] keyNames = dev.equo.swt.WireTable.KEYS;

    static {
        for (int i = 1; i < keyNames.length; i++) KEYS.put(keyNames[i], i);
    }

    /** A buffer kept past this size is let go once lent, so one large payload is not held for good. */
    private static final int KEPT_BYTES = 1 << 20;
    private static final ThreadLocal<TaggedValues> POOL = ThreadLocal.withInitial(TaggedValues::new);

    private byte[] buf = new byte[64];
    private int size;
    private boolean lent;
    private final Map<String, Integer> spelled = new HashMap<>();
    /** How a value of no other kind is made one of the kinds above. */
    private Function<Object, Object> tree;

    private TaggedValues() {
    }

    /** Whether {@code v} is made of maps, lists and scalars only, which need no conversion to be sent. */
    public static boolean isPlain(Object v) {
        if (v == null || v instanceof Boolean || v instanceof Number || v instanceof CharSequence
                || v instanceof Character || v instanceof Enum<?> || v instanceof byte[]) {
            return true;
        }
        if (v instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) v;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!isPlain(e.getValue())) return false;
            }
            return true;
        }
        if (v instanceof Collection<?>) {
            Collection<?> list = (Collection<?>) v;
            for (Object item : list) {
                if (!isPlain(item)) return false;
            }
            return true;
        }
        if (v.getClass().isArray()) {
            if (v.getClass().getComponentType().isPrimitive()) return true;
            for (int i = 0, n = Array.getLength(v); i < n; i++) {
                if (!isPlain(Array.get(v, i))) return false;
            }
            return true;
        }
        return false;
    }

    /** Whether {@code data[offset, offset + length)} holds tagged values rather than text. */
    public static boolean isTagged(byte[] data, int offset, int length) {
        return length > 0 && (data[offset] & 0xFF) == MARK;
    }

    /** Takes a written frame, valid only until it returns. */
    public interface Lent {
        void accept(byte[] buffer, int length);
    }

    /** {@code value} as tagged values; {@code tree} turns any other object into maps, lists and scalars. */
    public static byte[] encode(Object value, Function<Object, Object> tree) {
        byte[][] out = new byte[1][];
        frame(new byte[0], value, tree, (buffer, length) -> out[0] = Arrays.copyOf(buffer, length));
        return out[0];
    }

    /**
     * Writes {@code header} and then {@code value} as tagged values into this thread's buffer and
     * lends it to {@code sink}, so a caller that sends at once copies nothing.
     */
    public static void frame(byte[] header, Object value, Function<Object, Object> tree, Lent sink) {
        TaggedValues w = lend(header, value, tree);
        try {
            sink.accept(w.buf, w.size);
        } finally {
            w.release();
        }
    }

    /**
     * This thread's writer, holding {@code header} and then {@code value} as tagged values in
     * {@link #buffer()}{@code [0, }{@link #size()}{@code )} until {@link #release()}.
     */
    public static TaggedValues lend(byte[] header, Object value, Function<Object, Object> tree) {
        TaggedValues w = borrow(tree, header.length);
        System.arraycopy(header, 0, w.buf, 0, header.length);
        return w.close(header.length, value);
    }

    /**
     * {@link #lend(byte[], Object, Function)} behind the header of the channel numbered {@code channel}
     * of the widget of type {@code type} numbered {@code id}, written from those numbers.
     */
    public static TaggedValues lend(int type, long id, int channel, Object value) {
        TaggedValues w = borrow(null, ChannelHeader.MAX_NUMBERED);
        return w.close(ChannelHeader.write(w.buf, 0, type, id, channel), value);
    }

    private static TaggedValues borrow(Function<Object, Object> tree, int header) {
        TaggedValues w = POOL.get();
        if (w.lent) w = new TaggedValues();
        w.lent = true;
        w.tree = tree;
        w.spelled.clear();
        w.size = 0;
        w.ensure(header + 1);
        return w;
    }

    private TaggedValues close(int header, Object value) {
        try {
            size = header;
            payloadStart = header;
            u8(MARK);
            value(value);
            return this;
        } catch (RuntimeException | Error e) {
            release();
            throw e;
        }
    }

    private int payloadStart;

    /** The lent frame: its header, then the payload from {@link #payloadStart()}. */
    public byte[] buffer() {
        return buf;
    }

    public int size() {
        return size;
    }

    public int payloadStart() {
        return payloadStart;
    }

    /** Ends the lend: the buffer is this thread's again. */
    public void release() {
        lent = false;
        tree = null;
        if (buf.length > KEPT_BYTES) buf = new byte[64];
    }

    private void value(Object v) {
        if (v == null) {
            u8(NULL);
        } else if (v instanceof Boolean) {
            Boolean b = (Boolean) v;
            u8(b ? TRUE : FALSE);
        } else if (v instanceof Integer || v instanceof Long || v instanceof Short || v instanceof Byte) {
            u8(INT);
            long n = ((Number) v).longValue();
            varint((n << 1) ^ (n >> 63));
        } else if (v instanceof Number) {
            Number n = (Number) v;
            u8(DOUBLE);
            long bits = Double.doubleToRawLongBits(n.doubleValue());
            ensure(8);
            for (int i = 0; i < 8; i++) buf[size++] = (byte) (bits >>> (8 * i));
        } else if (v instanceof CharSequence || v instanceof Character || v instanceof Enum<?>) {
            u8(TEXT);
            text(v instanceof Enum<?> ? ((Enum<?>) v).name() : v.toString());
        } else if (v instanceof byte[]) {
            byte[] bytes = (byte[]) v;
            u8(BYTES);
            varint(bytes.length);
            ensure(bytes.length);
            System.arraycopy(bytes, 0, buf, size, bytes.length);
            size += bytes.length;
        } else if (v instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) v;
            u8(MAP);
            varint(map.size());
            for (Map.Entry<?, ?> e : map.entrySet()) {
                key(String.valueOf(e.getKey()));
                value(e.getValue());
            }
        } else if (v instanceof Collection<?>) {
            Collection<?> list = (Collection<?>) v;
            u8(LIST);
            varint(list.size());
            for (Object item : list) value(item);
        } else if (v.getClass().isArray()) {
            int n = Array.getLength(v);
            u8(LIST);
            varint(n);
            for (int i = 0; i < n; i++) value(Array.get(v, i));
        } else if (tree != null) {
            value(tree.apply(v));
        } else {
            throw new IllegalArgumentException("no tagged form for " + v.getClass().getName());
        }
    }

    /** A key: odd, its number; 0, spelled out; otherwise one spelled out earlier, by its place. */
    private void key(String name) {
        Integer number = KEYS.get(name);
        if (number != null) {
            varint((long) number << 1 | 1);
            return;
        }
        Integer place = spelled.get(name);
        if (place != null) {
            varint((long) (place + 1) << 1);
            return;
        }
        spelled.put(name, spelled.size());
        varint(0);
        text(name);
    }

    /**
     * Text as the widget wire writes a string, so neither end transcodes it: {@code varint(n << 1 |
     * wide) + 1}, then {@code n} Latin-1 bytes, or, when a char is past 0xFF, {@code n} UTF-16 code
     * units little endian.
     */
    @SuppressWarnings("deprecation")
    private void text(String s) {
        int n = s.length();
        int latin1 = 0;
        while (latin1 < n && s.charAt(latin1) <= 0xFF) latin1++;
        if (latin1 == n) {
            varint((n << 1) + 1);
            ensure(n);
            // The low byte of each char: a plain copy of a Latin-1 string's bytes.
            s.getBytes(0, n, buf, size);
            size += n;
            return;
        }
        varint(((n << 1) | 1) + 1);
        ensure(2 * n);
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            buf[size++] = (byte) c;
            buf[size++] = (byte) (c >> 8);
        }
    }

    private void u8(int b) {
        ensure(1);
        buf[size++] = (byte) b;
    }

    private void varint(long v) {
        ensure(10);
        while ((v & ~0x7FL) != 0) {
            buf[size++] = (byte) ((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        buf[size++] = (byte) v;
    }

    private void ensure(int n) {
        if (size + n > buf.length) buf = Arrays.copyOf(buf, Math.max(size + n, buf.length * 2));
    }

    // --- reading ---

    /** The value in {@code data[offset, offset + length)}, which opens with {@link #MARK}. */
    public static Object decode(byte[] data, int offset, int length) {
        Reader r = new Reader(data, offset + 1, offset + length);
        return r.value();
    }

    private static final class Reader {
        final byte[] b;
        int pos;
        final int end;
        final List<String> spelled = new ArrayList<>();

        Reader(byte[] b, int pos, int end) {
            this.b = b;
            this.pos = pos;
            this.end = end;
        }

        Object value() {
            int tag = b[pos++] & 0xFF;
            switch (tag) {
                case NULL:
                    return null;
                case TRUE:
                    return Boolean.TRUE;
                case FALSE:
                    return Boolean.FALSE;
                case INT: {
                    long z = varint();
                    return (z >>> 1) ^ -(z & 1);
                }
                case DOUBLE: {
                    long bits = 0;
                    for (int i = 0; i < 8; i++) bits |= (long) (b[pos++] & 0xFF) << (8 * i);
                    return Double.longBitsToDouble(bits);
                }
                case TEXT:
                    return text();
                case BYTES: {
                    int n = (int) varint();
                    byte[] out = Arrays.copyOfRange(b, pos, pos + n);
                    pos += n;
                    return out;
                }
                case LIST: {
                    int n = (int) varint();
                    List<Object> out = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) out.add(value());
                    return out;
                }
                case MAP: {
                    int n = (int) varint();
                    Map<String, Object> out = new LinkedHashMap<>();
                    for (int i = 0; i < n; i++) {
                        String key = key();
                        out.put(key, value());
                    }
                    return out;
                }
                default:
                    throw new IllegalArgumentException("not a tagged value: " + tag);
            }
        }

        String key() {
            long ref = varint();
            if ((ref & 1) == 1) return keyNames[(int) (ref >>> 1)];
            if (ref != 0) return spelled.get((int) (ref >>> 1) - 1);
            String name = text();
            spelled.add(name);
            return name;
        }

        @SuppressWarnings("deprecation")
        String text() {
            int header = (int) varint();
            int n = (header - 1) >>> 1;
            if (((header - 1) & 1) == 0) {
                // Each byte is a char's low byte, its high byte 0: a Latin-1 copy, nothing decoded.
                String s = new String(b, 0, pos, n);
                pos += n;
                return s;
            }
            char[] chars = new char[n];
            for (int i = 0; i < n; i++, pos += 2) chars[i] = (char) ((b[pos] & 0xFF) | (b[pos + 1] & 0xFF) << 8);
            return new String(chars);
        }

        long varint() {
            long v = 0;
            for (int shift = 0; ; shift += 7) {
                int x = b[pos++] & 0xFF;
                v |= (long) (x & 0x7F) << shift;
                if ((x & 0x80) == 0) return v;
            }
        }
    }
}
