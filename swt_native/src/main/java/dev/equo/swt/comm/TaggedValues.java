package dev.equo.swt.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
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
 * Every value carries a tag; a map's keys go by the number both sides know them by (wire-keys.txt,
 * generated), and only one nothing numbers is spelled out, once per payload. Text is the only
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
    private static String[] keyNames = new String[0];

    static {
        int max = 0;
        try (InputStream s = TaggedValues.class.getResourceAsStream("/dev/equo/swt/wire-keys.txt")) {
            if (s != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(s, StandardCharsets.UTF_8));
                for (String line; (line = reader.readLine()) != null; ) {
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int space = line.indexOf(' ');
                    int number = Integer.parseInt(line.substring(0, space));
                    KEYS.put(line.substring(space + 1), number);
                    max = Math.max(max, number);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        keyNames = new String[max + 1];
        KEYS.forEach((name, number) -> keyNames[number] = name);
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
        TaggedValues w = POOL.get();
        if (w.lent) w = new TaggedValues();
        w.lent = true;
        w.tree = tree;
        w.spelled.clear();
        try {
            w.size = 0;
            w.ensure(header.length + 1);
            System.arraycopy(header, 0, w.buf, 0, header.length);
            w.size = header.length;
            w.u8(MARK);
            w.value(value);
            sink.accept(w.buf, w.size);
        } finally {
            w.lent = false;
            w.tree = null;
            if (w.buf.length > KEPT_BYTES) w.buf = new byte[64];
        }
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

    /** Text as its UTF-8 length and bytes, encoded straight into the buffer. */
    private void text(String s) {
        int n = s.length();
        int length = n;
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (c < 0x80) continue;
            if (c < 0x800) {
                length += 1;
            } else if (Character.isHighSurrogate(c) && i + 1 < n && Character.isLowSurrogate(s.charAt(i + 1))) {
                length += 2;
                i++;
            } else if (!Character.isSurrogate(c)) {
                length += 2;
            }
        }
        varint(length);
        ensure(length);
        byte[] b = buf;
        int p = size;
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (c < 0x80) {
                b[p++] = (byte) c;
            } else if (c < 0x800) {
                b[p++] = (byte) (0xC0 | c >> 6);
                b[p++] = (byte) (0x80 | c & 0x3F);
            } else if (Character.isHighSurrogate(c) && i + 1 < n && Character.isLowSurrogate(s.charAt(i + 1))) {
                int cp = Character.toCodePoint(c, s.charAt(++i));
                b[p++] = (byte) (0xF0 | cp >> 18);
                b[p++] = (byte) (0x80 | cp >> 12 & 0x3F);
                b[p++] = (byte) (0x80 | cp >> 6 & 0x3F);
                b[p++] = (byte) (0x80 | cp & 0x3F);
            } else if (Character.isSurrogate(c)) {
                // An unpaired surrogate is written as '?', as String.getBytes does.
                b[p++] = '?';
            } else {
                b[p++] = (byte) (0xE0 | c >> 12);
                b[p++] = (byte) (0x80 | c >> 6 & 0x3F);
                b[p++] = (byte) (0x80 | c & 0x3F);
            }
        }
        size = p;
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

        String text() {
            int n = (int) varint();
            String s = new String(b, pos, n, StandardCharsets.UTF_8);
            pos += n;
            return s;
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
