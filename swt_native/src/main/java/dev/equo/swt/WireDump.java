package dev.equo.swt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a {@link WidgetWire} frame back into named values: for a test that asserts on what was sent,
 * and for a log. A property's number alone says how its value is encoded (wire-kinds.txt, written by
 * the generator; numbers are unique across classes), so nothing about the widget's class is needed.
 */
public final class WireDump {

    /** Record type names by number (wire-types.txt). */
    private static final Map<Integer, String> TYPE_NAMES = loadTypeNames();

    private static Map<Integer, String> loadTypeNames() {
        Map<Integer, String> names = new java.util.HashMap<>();
        try (InputStream s = WireDump.class.getResourceAsStream("wire-types.txt")) {
            if (s == null) return names;
            BufferedReader reader = new BufferedReader(new InputStreamReader(s, StandardCharsets.UTF_8));
            for (String line; (line = reader.readLine()) != null; ) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                int space = line.indexOf(' ');
                names.put(Integer.parseInt(line.substring(0, space)), line.substring(space + 1));
            }
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        return names;
    }

    /** One record: its kind ({@link WidgetWire#WHOLE} and so on), what it names, and its properties by name. */
    public static final class Record {
        public final int kind;
        public final String type;
        public final long id;
        public final long stamp;
        /** For a change, the stamp of the state it was computed from; otherwise 0. */
        public final long base;
        /** Bytes the record takes in its frame, its kind and length included. */
        public final int size;
        public final Map<String, Object> properties = new LinkedHashMap<>();

        Record(int kind, String type, long id, long stamp, long base, int size) {
            this.kind = kind;
            this.type = type;
            this.id = id;
            this.stamp = stamp;
            this.base = base;
            this.size = size;
        }

        /** The channel the record's widget was addressed by before the wire: {@code Type/id}. */
        public String channel() {
            return (type == null ? "Image" : type) + "/" + id;
        }

        @Override
        public String toString() {
            return channel() + " " + properties;
        }
    }

    /** A widget named by id; its type when this frame or an earlier one described it. */
    public static final class Ref {
        public final long id;
        public final String type;

        Ref(long id, String type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public String toString() {
            return (type == null ? "?" : type) + "/" + id;
        }
    }

    /** Each generated property's number by its {@code Class.name}, filled by {@link #load()}. */
    private static final Map<String, Integer> NUMBERS = new HashMap<>();

    private static final Map<Integer, String[]> KINDS = load();

    /** The type of every widget a frame described, so a later frame naming it can say what it is. */
    private static final Map<Long, String> typesById = new java.util.concurrent.ConcurrentHashMap<>();

    private WireDump() {
    }

    /** The records of a frame body ({@code body[offset, offset + length)}, after the channel). */
    public static List<Record> records(byte[] body, int offset, int length) {
        In in = new In(body, offset, offset + length);
        int count = in.le32();
        List<Record> records = new ArrayList<>(count);
        List<String> types = new ArrayList<>();
        long stamp = 0;
        for (int i = 0; i < count; i++) {
            int start = in.pos;
            int kind = in.u8();
            int recordLength = in.varint();
            int end = in.pos + recordLength;
            String type = null;
            if (kind != WidgetWire.IMAGE) {
                int ref = in.varint();
                if ((ref & 1) == 1) {
                    type = TYPE_NAMES.get(ref >> 1);
                } else if (ref == 0) {
                    type = in.string();
                    types.add(type);
                } else {
                    type = types.get((ref >> 1) - 1);
                }
            }
            long id = in.zigzagLong();
            long base = 0;
            if (kind != WidgetWire.IMAGE) {
                stamp += in.varlong();
                if (kind == WidgetWire.CHANGE) base = stamp - in.varlong();
            }
            Record r = new Record(kind, type, id, kind == WidgetWire.IMAGE ? 0 : stamp, base, end - start);
            if (type != null) typesById.put(id, type);
            int key = 0;
            try {
                while (in.pos < end) {
                    key += in.zigzag();
                    String[] k = kindOf(key, type);
                    r.properties.put(k[1], value(in, k[0]));
                }
            } catch (RuntimeException e) {
                r.properties.put("?" + key, "unreadable: " + e.getMessage());
            }
            in.pos = end;
            records.add(r);
        }
        return records;
    }

    /** Properties as a record or GC state carries them, key deltas and values closed by a 0, by name. */
    public static Map<String, Object> fields(byte[] b, int pos, int end) {
        In in = new In(b, pos, end);
        Map<String, Object> fields = new LinkedHashMap<>();
        int key = 0;
        try {
            while (in.pos < end) {
                int delta = in.zigzag();
                if (delta == 0) break;
                key += delta;
                String[] k = kindOf(key, null);
                fields.put(k[1], value(in, k[0]));
            }
        } catch (RuntimeException e) {
            fields.put("?" + key, "unreadable: " + e.getMessage());
        }
        return fields;
    }

    /** One value of the encoding {@code kind} at {@code b[pos, end)}. */
    public static Object value(byte[] b, int pos, int end, String kind) {
        return value(new In(b, pos, end), kind);
    }

    /** Bytes as text: a short run as its base64, a long one (pixels) as its length. */
    public static String describe(byte[] bytes) {
        return bytes.length <= 64 ? java.util.Base64.getEncoder().encodeToString(bytes) : bytes.length + " bytes";
    }

    /** The number of the generated property {@code classDotName} ({@code Decorations.ownMenuStrip}), or -1. */
    public static int number(String classDotName) {
        Integer n = NUMBERS.get(classDotName);
        return n == null ? -1 : n;
    }

    /** Whether property {@code key} travels as an image or a list of them. */
    static boolean isImage(int key) {
        String[] k = KINDS.get(key);
        return k != null && ("image".equals(k[0]) || "image[]".equals(k[0]));
    }

    private static String[] kindOf(int key, String type) {
        String[] k = KINDS.get(key);
        if (k == null) throw new IllegalStateException("no property " + key + (type == null ? "" : " on " + type));
        return k;
    }

    private static Object value(In in, String kind) {
        if (kind.equals("byte[]")) {
            int count = in.varint();
            if (count == 0) return null;
            byte[] bytes = java.util.Arrays.copyOfRange(in.buf, in.pos, in.pos + count - 1);
            in.pos += count - 1;
            return bytes;
        }
        if (kind.endsWith("[]")) {
            String element = kind.substring(0, kind.length() - 2);
            int count = in.varint();
            if (count == 0) return null;
            List<Object> list = new ArrayList<>(count - 1);
            for (int i = 0; i < count - 1; i++) list.add(value(in, element));
            return list;
        }
        if (kind.startsWith("object:")) return object(in, false);
        switch (kind) {
            case "bool": return in.u8() != 0;
            case "bool?": {
                int b = in.u8();
                return b == 0 ? null : b == 2;
            }
            case "int": return in.zigzag();
            case "long": return in.zigzagLong();
            case "char": return in.varint();
            case "float": return Float.intBitsToFloat(in.le32());
            case "double": return Double.longBitsToDouble((in.le32() & 0xFFFFFFFFL) | ((long) in.le32() << 32));
            case "int?": return in.u8() == 0 ? null : in.zigzag();
            case "long?": return in.u8() == 0 ? null : in.zigzagLong();
            case "float?": return in.u8() == 0 ? null : Float.intBitsToFloat(in.le32());
            case "double?":
                return in.u8() == 0 ? null : Double.longBitsToDouble((in.le32() & 0xFFFFFFFFL) | ((long) in.le32() << 32));
            case "string": return in.string();
            case "byte": return in.zigzag();
            case "color": {
                if (in.u8() == 0) return null;
                int argb = in.le32();
                return String.format(java.util.Locale.ROOT, "#%08x", argb);
            }
            case "point": return in.u8() == 0 ? null : named(new String[] {"x", "y"}, in.zigzag(), in.zigzag());
            case "rect":
                return in.u8() == 0 ? null : named(new String[] {"x", "y", "width", "height"},
                        in.zigzag(), in.zigzag(), in.zigzag(), in.zigzag());
            case "rgb": return in.u8() == 0 ? null : named(new String[] {"red", "green", "blue"}, in.u8(), in.u8(), in.u8());
            case "ref": {
                long v = in.varlong();
                if (v == 0) return null;
                long z = v - 1;
                long id = (z >>> 1) ^ -(z & 1);
                return new Ref(id, typesById.get(id));
            }
            case "image": {
                long v = in.varlong();
                if (v == 0) return null;
                long z = v - 1;
                return new Ref((z >>> 1) ^ -(z & 1), "Image");
            }
            case "poly": return object(in, true);
            case "stringMap": {
                int count = in.varint();
                if (count == 0) return null;
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < count - 1; i++) map.put(in.string(), in.string());
                return map;
            }
            case "boolMap": {
                int count = in.varint();
                if (count == 0) return null;
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < count - 1; i++) map.put(in.string(), value(in, "bool?"));
                return map;
            }
            default: throw new IllegalStateException("no encoding " + kind);
        }
    }

    private static Map<String, Object> named(String[] names, Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < names.length; i++) map.put(names[i], values[i]);
        return map;
    }

    private static Map<String, Object> object(In in, boolean poly) {
        if (in.u8() == 0) return null;
        Map<String, Object> fields = new LinkedHashMap<>();
        if (poly) fields.put("$type", in.varint());
        int key = 0;
        while (true) {
            int delta = in.zigzag();
            if (delta == 0) return fields;
            key += delta;
            String[] k = kindOf(key, null);
            fields.put(k[1], value(in, k[0]));
        }
    }

    private static Map<Integer, String[]> load() {
        Map<Integer, String[]> kinds = new HashMap<>();
        // The hand-written holders: the Display and the flags it carries.
        String[][] display = {
                {"1", "ref[]", "shells"}, {"2", "ref[]", "popups"}, {"3", "ref", "systemMenu"},
                {"4", "ref[]", "tooltips"}, {"5", "long", "activeShellId"}, {"6", "long", "mainShellId"},
                {"7", "long[]", "windowedShellIds"}, {"8", "object:ConfigFlags", "config"},
                {"1396", "long", "focusHolderId"},
        };
        for (String[] d : display) kinds.put(Integer.parseInt(d[0]), new String[] {d[1], d[2]});
        for (Map.Entry<Integer, String[]> e : ConfigFlags.wireKinds().entrySet()) kinds.put(e.getKey(), e.getValue());
        try (InputStream s = WireDump.class.getResourceAsStream("wire-kinds.txt")) {
            if (s == null) return kinds;
            BufferedReader reader = new BufferedReader(new InputStreamReader(s, StandardCharsets.UTF_8));
            for (String line; (line = reader.readLine()) != null; ) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(" ");
                String name = parts[2].substring(parts[2].lastIndexOf('.') + 1);
                kinds.put(Integer.parseInt(parts[0]), new String[] {parts[1], name});
                NUMBERS.put(parts[2], Integer.parseInt(parts[0]));
            }
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        return kinds;
    }

    private static final class In {
        final byte[] buf;
        int pos;
        final int end;

        In(byte[] buf, int pos, int end) {
            this.buf = buf;
            this.pos = pos;
            this.end = end;
        }

        int u8() {
            return buf[pos++] & 0xFF;
        }

        int le32() {
            int v = (buf[pos] & 0xFF) | (buf[pos + 1] & 0xFF) << 8 | (buf[pos + 2] & 0xFF) << 16 | (buf[pos + 3] & 0xFF) << 24;
            pos += 4;
            return v;
        }

        int varint() {
            return (int) varlong();
        }

        long varlong() {
            long v = 0;
            for (int shift = 0; ; shift += 7) {
                int b = buf[pos++];
                v |= (long) (b & 0x7F) << shift;
                if (b >= 0) return v;
            }
        }

        int zigzag() {
            int v = varint();
            return (v >>> 1) ^ -(v & 1);
        }

        long zigzagLong() {
            long v = varlong();
            return (v >>> 1) ^ -(v & 1);
        }

        @SuppressWarnings("deprecation")
        String string() {
            int header = varint();
            if (header == 0) return null;
            int n = (header - 1) >>> 1;
            if (((header - 1) & 1) == 0) {
                String s = new String(buf, 0, pos, n);
                pos += n;
                return s;
            }
            char[] chars = new char[n];
            for (int i = 0; i < n; i++, pos += 2) chars[i] = (char) ((buf[pos] & 0xFF) | (buf[pos + 1] & 0xFF) << 8);
            return new String(chars);
        }
    }
}
