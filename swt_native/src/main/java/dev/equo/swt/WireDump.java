package dev.equo.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a {@link WidgetWire} frame back into named values: for a test that asserts on what was sent,
 * and for a log. A property's number alone says how its value is encoded ({@link WireTable}, generated;
 * numbers are unique across classes), so nothing about the widget's class is needed.
 */
public final class WireDump {

    private static final String[] TYPE_NAMES = WireNames.types();

    /** Record type names by number. */
    private static String typeName(int number) {
        return number > 0 && number < TYPE_NAMES.length ? TYPE_NAMES[number] : null;
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

        /** {@code read} as the {@code size} bytes it took. */
        Record(Record read, int size) {
            this(read.kind, read.type, read.id, read.stamp, read.base, size);
            properties.putAll(read.properties);
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

    private static final Map<Integer, String[]> KINDS = load();

    /** The type of every widget a frame described, so a later frame naming it can say what it is. */
    private static final Map<Long, String> typesById = new java.util.concurrent.ConcurrentHashMap<>();

    private WireDump() {
    }

    /**
     * The records of a frame body ({@code body[offset, offset + length)}, after the channel). An item
     * described in place inside a list of items ({@link WidgetWire#items}) is given as a whole record
     * of its own, after the record whose list holds it.
     */
    public static List<Record> records(byte[] body, int offset, int length) {
        In in = new In(body, offset, offset + length);
        in.inPlace = new ArrayList<>();
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
                    type = typeName(ref >> 1);
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
            in.inPlaceStamp = stamp;
            in.inPlace.clear();
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
            records.addAll(in.inPlace);
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
        if (kind.equals("runs")) {
            int count = in.varint();
            if (count == 0) return null;
            List<Object> list = new ArrayList<>(count - 1);
            if (count == 1) return list;
            int first = in.zigzag();
            list.add(first);
            int from = WidgetWire.runsFrom(first, count - 1);
            for (int i = 1; i < (from == 0 ? count - 1 : from); i++) list.add(in.zigzag());
            if (from == 0) return list;
            int names = in.varint();
            int end = 0;
            for (int i = from; i < count - 1; i += 3) {
                long run = in.varlong();
                long packed = run >>> 1;
                int name;
                if (names > 0) {
                    name = (int) (packed % names);
                } else {
                    int zigzag = (int) packed;
                    name = (zigzag >>> 1) ^ -(zigzag & 1);
                }
                int start = (run & 1) == 0 ? end : end + in.zigzag();
                int length = names > 0 ? (int) (packed / names) : in.zigzag();
                list.add(name);
                list.add(start);
                list.add(length);
                end = start + length;
            }
            return list;
        }
        if (kind.startsWith("refs:")) return items(in, kind.substring("refs:".length()));
        if (kind.equals("ref[]") && in.buf[in.pos] < 0 && peekVarlong(in) == WidgetWire.LIST_CHANGE) {
            return listChange(in, () -> value(in, "ref"));
        }
        if (kind.endsWith("[]")) {
            String element = kind.substring(0, kind.length() - 2);
            int count = in.varint();
            if (count == 0) return null;
            List<Object> list = new ArrayList<>(count - 1);
            boolean objects = element.startsWith("object:");
            while (list.size() < count - 1) {
                if (objects && in.buf[in.pos] == WidgetWire.NULL_RUN) {
                    in.pos++;
                    for (int run = in.varint(); run > 0; run--) list.add(null);
                } else {
                    list.add(value(in, element));
                }
            }
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

    /** The varint at {@code in}'s position, without moving past it. */
    private static long peekVarlong(In in) {
        int at = in.pos;
        long v = in.varlong();
        in.pos = at;
        return v;
    }

    /**
     * A list written as a change to the one the client holds ({@link WidgetWire#LIST_CHANGE}): where
     * it starts, how many it removes there, what it inserts, and the checksum of the list it makes.
     */
    private static Map<String, Object> listChange(In in, java.util.function.Supplier<Object> element) {
        in.varlong();
        Map<String, Object> change = new LinkedHashMap<>();
        change.put("checksum", in.varint());
        change.put("from", in.varint());
        change.put("removed", in.varint());
        int count = in.varint();
        List<Object> inserted = new ArrayList<>(count);
        for (int i = 0; i < count; i++) inserted.add(element.get());
        change.put("inserted", inserted);
        return change;
    }

    /** What {@link WidgetWire#items} wrote: each item by id, one described in place also as a record. */
    private static Object items(In in, String type) {
        if (in.buf[in.pos] < 0 && peekVarlong(in) == WidgetWire.LIST_CHANGE) return listChange(in, () -> item(in, type));
        int count = in.varint();
        if (count == 0) return null;
        List<Object> list = new ArrayList<>(count - 1);
        for (int i = 0; i < count - 1; i++) list.add(item(in, type));
        return list;
    }

    /** One entry of a list of items: the item by id, and the record it is described by in place, if it is. */
    private static Object item(In in, String type) {
        int start = in.pos;
        long v = in.varlong();
        if (v == 0) return null;
        long z = (v - 1) >>> 1;
        long id = (z >>> 1) ^ -(z & 1);
        Ref ref = new Ref(id, type);
        typesById.put(id, type);
        if (((v - 1) & 1) == 0) return ref;
        in.inPlaceStamp += in.varlong();
        Record r = new Record(WidgetWire.WHOLE, type, id, in.inPlaceStamp, 0, 0);
        List<Record> outer = in.inPlace;
        in.inPlace = new ArrayList<>();
        int key = 0;
        while (true) {
            int delta = in.zigzag();
            if (delta == 0) break;
            key += delta;
            String[] k = kindOf(key, type);
            r.properties.put(k[1], value(in, k[0]));
        }
        List<Record> inner = in.inPlace;
        in.inPlace = outer;
        if (outer != null) {
            outer.add(new Record(r, in.pos - start));
            outer.addAll(inner);
        }
        return ref;
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
        for (int i = 1; i < WireTable.PROPERTIES.length; i++) {
            if (WireTable.KINDS[i] == null) continue;
            String name = WireTable.PROPERTIES[i];
            kinds.put(i, new String[] {WireTable.KINDS[i], name.substring(name.lastIndexOf('.') + 1)});
        }
        return kinds;
    }

    private static final class In {
        final byte[] buf;
        int pos;
        final int end;
        /** The stamp the next item described in place steps from. */
        long inPlaceStamp;
        /** Where the items described in place in the record being read go, or null to drop them. */
        List<Record> inPlace;

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
