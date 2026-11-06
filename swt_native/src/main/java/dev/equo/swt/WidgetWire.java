package dev.equo.swt;

import dev.equo.swt.comm.CommService;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GraphicsUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Everything one flush tells a client about its widgets, as a single binary frame on {@link #CHANNEL}:
 * a tape of records, read front to back.
 *
 * <pre>
 * frame    := u32 count, record{count}
 * record   := u8 kind, varint length, body        (length lets a reader skip a record whole)
 * WHOLE    := type id stamp property*             a widget described in full, replacing what is held
 * CHANGE   := type id stamp base property*        the properties changed since the state stamped base
 * IMAGE    := id property*                        an image, ahead of anything that names it
 * HOLDER   := type id stamp property*             something that holds widgets but is not one: the display
 * type     := varint 2n + 1                       a type both sides number, by its number n
 *           | varint 0, string                    any other, spelled out the first time in the frame
 *           | varint 2(k + 1)                     after that, by its place k among those spelled out
 * id       := zigzag varint
 * stamp    := varint, this record's write stamp less the previous record's
 * base     := varint, the stamp less the stamp of the state the change was computed from
 * property := key, value
 * key      := zigzag varint, the property's number less the previous number in the same record or
 *             object (the first counts from 0); a class's numbers are consecutive, so most are 1 byte
 * items    := varint count + 1 (0 for null), entry{count}    a list of the items a widget owns
 *           | change
 * entry    := varint 0                                        none
 *           | varint 2 zigzag(id) + 1                         an item the client holds, by id
 *           | varint 2 zigzag(id) + 2, step, property*, 0      one it lacks, described in place
 * change   := varint LIST_CHANGE, varint checksum, varint from, varint removed, varint count,
 *             element{count}                                  in a change record, the list the client
 *             holds with removed elements at from replaced by these: a list of widgets or of items
 * step     := varint, the item's write stamp less the one before it in the record: the record's own
 *             stamp, or the item described in place before it
 * </pre>
 *
 * A property is named by its number, the one the generator gave its name ({@link WireTable}), and both
 * sides know from the property what its value is, so the frame carries no types and no names. A
 * widget or an image is named by its id: anything named that the client does not hold is described
 * by its own record in the same frame, so a new subtree arrives as consecutive records, parents
 * before children; an item a list of items names is described in that list instead ({@link #items}). Strings and arrays are a varint length plus one (0 for null) and their contents;
 * ints and longs zigzag varints; floats and doubles little endian; an object or other nullable value
 * a presence byte first, then an object's fields as key and value, closed by a 0 (no key is 0:
 * numbers in one object are distinct).
 */
public final class WidgetWire {

    /** The channel the frame travels on. */
    public static final String CHANNEL = "swt.evolve.wire";

    public static final int WHOLE = 1;
    public static final int CHANGE = 2;
    public static final int IMAGE = 3;
    public static final int HOLDER = 4;

    /** Told about every widget and image a value names, so whatever the client lacks can follow. */
    public interface Names {
        /** A widget, or another value the client holds as one (a table editor). */
        void widget(Object widget);

        void image(Image image);

        /**
         * The id to write for {@code image}, after arranging for whatever the client lacks to
         * follow. Normally the image's own id, with its pixels sent under it; an implementation that
         * dedupes a repeated icon returns the id of the twin already in the frame instead, and sends
         * nothing more.
         */
        default long imageRef(Image image) {
            image(image);
            return image.hashCode();
        }

        /** Whether a list of widgets written now is told to {@link #listWritten}. */
        default boolean tracksLists() {
            return false;
        }

        /**
         * The ids of the list the client holds for the property {@code key} being written, when the
         * list may travel as a change to it; null otherwise.
         */
        default int[] heldList(int key) {
            return null;
        }

        /** The client holds {@code ids} for the property {@code key} once what is being written reaches it. */
        default void listWritten(int key, int[] ids) {
        }

        /** Whether the client holds {@code widget} as it was last described, so a list may keep it unnamed. */
        default boolean holds(Object widget) {
            return false;
        }

        /**
         * Describes {@code item} where the frame stands, when the client lacks it and nothing else in
         * the frame describes it: its {@link #inPlaceStamp step} and its properties, keyed from 0. False,
         * with nothing written, otherwise, or when its record type is not the one numbered {@code type}.
         */
        default boolean describeInPlace(Object item, int type) {
            return false;
        }
    }

    private static final Names IGNORE = new Names() {
        @Override
        public void widget(Object widget) {
        }

        @Override
        public void image(Image image) {
        }
    };

    private byte[] buf = new byte[8192];
    private int size;
    private int records;
    private int countAt;
    private long lastStamp;
    /** The previous property number in the record or object being written: keys are deltas from it. */
    private int lastKey;
    /** Where the last empty string, array or map was written, for {@link #isEmpty}. */
    private int emptyAt = -1;
    private Names names = IGNORE;

    /** Record types by name, numbered as the client knows them ({@link WireNames}). */
    private static final Map<String, Integer> TYPE_NUMBERS = new HashMap<>();

    static {
        String[] types = WireNames.types();
        for (int i = 1; i < types.length; i++) TYPE_NUMBERS.put(types[i], i);
    }

    private static final byte[] HEADER = dev.equo.swt.comm.ChannelHeader.encode(0, 0, WireTable.CHANNEL_SWT_EVOLVE_WIRE);

    /** Starts an empty frame. */
    public void begin() {
        byte[] header = HEADER;
        size = 0;
        ensure(header.length + 4);
        System.arraycopy(header, 0, buf, 0, header.length);
        countAt = header.length;
        size = countAt + 4;
        records = 0;
        lastStamp = 0;
        lastKey = 0;
        emptyAt = -1;
        listFrom = -1;
        objectDepth = 0;
    }

    /** Where the widgets and images that values name are reported. */
    public void names(Names names) {
        this.names = names == null ? IGNORE : names;
    }

    public boolean isEmpty() {
        return records == 0;
    }

    public int records() {
        return records;
    }

    public int byteSize() {
        return size;
    }

    public void sendTo(CommService comm) {
        if (records == 0) return;
        writeCount(buf, countAt);
        comm.sendFrame(buf, 0, size);
    }

    /** The frame's payload, for tests: what {@link #sendTo} would send after the frame header. */
    public byte[] payload() {
        byte[] out = Arrays.copyOfRange(buf, countAt, size);
        writeCount(out, 0);
        return out;
    }

    private void writeCount(byte[] into, int at) {
        into[at] = (byte) records;
        into[at + 1] = (byte) (records >> 8);
        into[at + 2] = (byte) (records >> 16);
        into[at + 3] = (byte) (records >> 24);
    }

    // --- records ---

    /** Opens a record; its properties follow, and {@link #endRecord} closes it with the position returned. */
    public int beginRecord(int kind, String type, long id) {
        return beginRecord(kind, type, type == null ? null : TYPE_NUMBERS.get(type), id);
    }

    /** The number the client knows {@code type} by, or null when it has none: {@link #beginRecord} takes it. */
    public static Integer typeNumber(String type) {
        return TYPE_NUMBERS.get(type);
    }

    /** {@link #beginRecord(int, String, long)}, with {@code type}'s number already looked up. */
    public int beginRecord(int kind, String type, Integer number, long id) {
        ensure(2);
        buf[size++] = (byte) kind;
        // The length is written once known; one byte is kept for it, which most records fit.
        size++;
        int start = size;
        lastKey = 0;
        if (type != null) {
            // Every type is numbered when the code is generated; the number goes odd.
            if (number == null) {
                throw new IllegalArgumentException("type \"" + type + "\" has no wire number: add it to the generator's types");
            }
            varint(number << 1 | 1);
        }
        varlong((id << 1) ^ (id >> 63));
        return start;
    }

    /** The record's write stamp, and for a change the stamp of the state it was computed from. */
    public void stamps(long stamp, long base, boolean change) {
        varlong(stamp - lastStamp);
        lastStamp = stamp;
        inPlaceStamp = stamp;
        if (change) varlong(stamp - base);
    }

    /** The stamp the next item described in place is written as a step from. */
    private long inPlaceStamp;

    /** Writes the step to {@code stamp}, an item's described in place; returns the stamp it stepped from. */
    public long inPlaceStamp(long stamp) {
        long previous = inPlaceStamp;
        varlong(stamp - previous);
        inPlaceStamp = stamp;
        return previous;
    }

    /** Steps the next item described in place from {@code stamp} again: the ones after it were taken back. */
    public void restoreInPlaceStamp(long stamp) {
        inPlaceStamp = stamp;
    }

    /** Closes the record opened at {@code start}, writing its length. */
    public void endRecord(int start) {
        int length = size - start;
        int lengthBytes = varintSize(length);
        if (lengthBytes > 1) {
            ensure(lengthBytes - 1);
            System.arraycopy(buf, start, buf, start + lengthBytes - 1, length);
            size += lengthBytes - 1;
        }
        int at = start - 1;
        int v = length;
        while ((v & ~0x7F) != 0) {
            buf[at++] = (byte) ((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        buf[at] = (byte) v;
        records++;
    }

    /** Appends a property: its number, then the value {@code value[from, to)}. */
    public void property(int key, WidgetWire value, int from, int to) {
        key(key);
        int length = to - from;
        ensure(length);
        System.arraycopy(value.buf, from, buf, size, length);
        size += length;
    }

    /** A field's number, ahead of its value, inside an object. */
    public void key(int key) {
        int d = key - lastKey;
        varint((d << 1) ^ (d >> 31));
        lastKey = key;
        valueStart = size;
    }

    /** Where the value of the property {@link #key} last opened starts. */
    public int valueStart() {
        return valueStart;
    }

    private int valueStart;

    /**
     * A value's {@code writeWireWhole}: no generated class up to the value's has a whole writer for
     * the property, nothing was written, and it is to be written as {@code writeWire} writes it. A
     * value class written by hand has only {@code writeWire}.
     */
    public static final int UNHANDLED = -1;
    /** A value's {@code writeWireWhole}: the property is at a default written as a single 0, and was not written. */
    public static final int AT_ZERO = 0;
    /** A value's {@code writeWireWhole}: the property is an empty string, array or map, and was not written. */
    public static final int AT_EMPTY = 1;
    /** A value's {@code writeWireWhole}: the property's key and value were written. */
    public static final int WRITTEN = 2;


    /** What {@link #hash} gives for a value written as a single 0: false, zero or null. */
    public static final long ZERO_HASH = hash(new byte[] { 0 }, 0, 1);
    /** What {@link #hash} gives for an empty string, array or map. */
    public static final long EMPTY_HASH = hash(new byte[] { 1 }, 0, 1);

    /** {@code a} followed by {@code b}: a class's property numbers after its base class's. */
    public static int[] concat(int[] a, int[] b) {
        int[] all = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, all, a.length, b.length);
        return all;
    }

    // --- scratch use: values written on their own, then copied into a frame ---

    /** Empties a scratch buffer. */
    public void reset() {
        size = 0;
        lastKey = 0;
        emptyAt = -1;
        listFrom = -1;
    }

    public int size() {
        return size;
    }

    /** What has been written so far: {@code buffer()[0, size())}. */
    public byte[] buffer() {
        return buf;
    }

    /** A copy of everything written so far. */
    byte[] written() {
        return Arrays.copyOf(buf, size);
    }

    public void truncate(int size) {
        this.size = size;
        if (emptyAt >= size) emptyAt = -1;
        if (listFrom >= size) listFrom = -1;
    }

    /** The number of the property last written in the open record or object. */
    public int lastKey() {
        return lastKey;
    }

    /** Takes back a property written from {@code at}, the record's last key having been {@code previousKey}. */
    public void rewind(int at, int previousKey) {
        truncate(at);
        lastKey = previousKey;
    }

    private int markSize;
    private long markStamp;

    /** Remembers where the frame stands, for {@link #rollback} to return to. */
    public void mark() {
        markSize = size;
        markStamp = lastStamp;
    }

    /** Takes back everything since {@link #mark}: a record that turned out to say nothing. */
    public void rollback() {
        truncate(markSize);
        lastStamp = markStamp;
        lastKey = 0;
    }

    // --- buffers, kept per thread: a flush's frame is built and sent on one thread ---

    private static final int KEPT_BUFFER_BYTES = 4 << 20;
    private static final ThreadLocal<java.util.ArrayDeque<WidgetWire>> spare =
            ThreadLocal.withInitial(java.util.ArrayDeque::new);

    /** A frame to write into, reusing the buffer of one given back on this thread. */
    public static WidgetWire borrow() {
        WidgetWire w = spare.get().poll();
        return w != null ? w : new WidgetWire();
    }

    /** Whether this buffer is small enough to keep for reuse. */
    public boolean keepable() {
        return buf.length <= KEPT_BUFFER_BYTES;
    }

    /** Returns {@code w}'s buffer for the next {@link #borrow}; {@code w} is not used after this. */
    public static void giveBack(WidgetWire w) {
        w.names = IGNORE;
        // A flush that described a whole window grew its buffer large; that one is not kept.
        if (w.buf.length <= KEPT_BUFFER_BYTES && spare.get().size() < 4) spare.get().push(w);
    }

    /** Whether {@code [from, to)} is the single zero byte that false, zero and null all write as. */
    public boolean isZero(int from, int to) {
        return to - from == 1 && buf[from] == 0;
    }

    /** Whether {@code [from, to)} is an empty string, array or map, written as such at {@code from}. */
    public boolean isEmpty(int from, int to) {
        return to - from == 1 && emptyAt == from;
    }

    /** A string's, an array's or a map's length, plus one: 0 is null. */
    void lengthPrefix(int length) {
        if (length == 0) emptyAt = size;
        varint(length + 1);
    }

    /** A hash of {@code [from, to)}, to tell a value from the one last sent without keeping it. */
    public long hash(int from, int to) {
        return hash(buf, from, to);
    }

    /** A 64-bit hash of {@code b[from, to)}, eight bytes a step: it runs on every property written. */
    static long hash(byte[] b, int from, int to) {
        // The length is mixed in before any byte, so a value cannot hash like a shorter one plus a tail.
        long h = mix(0x9E3779B97F4A7C15L + (to - from));
        int i = from;
        for (; i + 8 <= to; i += 8) h = mix(h ^ littleEndianLong(b, i));
        long tail = 0;
        for (int shift = 0; i < to; i++, shift += 8) tail |= (b[i] & 0xFFL) << shift;
        return mix(h ^ tail);
    }

    /** The eight bytes at {@code i}, least significant first. */
    private static long littleEndianLong(byte[] b, int i) {
        return (b[i] & 0xFFL) | (b[i + 1] & 0xFFL) << 8 | (b[i + 2] & 0xFFL) << 16 | (b[i + 3] & 0xFFL) << 24
                | (b[i + 4] & 0xFFL) << 32 | (b[i + 5] & 0xFFL) << 40 | (b[i + 6] & 0xFFL) << 48
                | (b[i + 7] & 0xFFL) << 56;
    }

    /** MurmurHash3's 64-bit finalizer: every bit of the result depends on every bit of {@code z}. */
    private static long mix(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return z ^ (z >>> 33);
    }

    // --- values ---

    public void write(boolean v) {
        ensure(1);
        buf[size++] = (byte) (v ? 1 : 0);
    }

    public void write(int v) {
        varint((v << 1) ^ (v >> 31));
    }

    public void write(long v) {
        varlong((v << 1) ^ (v >> 63));
    }

    public void write(char v) {
        varint(v);
    }

    public void write(float v) {
        int bits = Float.floatToRawIntBits(v);
        ensure(4);
        buf[size++] = (byte) bits;
        buf[size++] = (byte) (bits >> 8);
        buf[size++] = (byte) (bits >> 16);
        buf[size++] = (byte) (bits >> 24);
    }

    public void write(double v) {
        long bits = Double.doubleToRawLongBits(v);
        ensure(8);
        for (int i = 0; i < 8; i++) buf[size++] = (byte) (bits >> (8 * i));
    }

    public void write(String v) {
        writeString(v);
    }

    public void write(Boolean v) {
        ensure(1);
        buf[size++] = (byte) (v == null ? 0 : v ? 2 : 1);
    }

    public void write(Integer v) {
        if (present(v != null)) write(v.intValue());
    }

    public void write(Long v) {
        if (present(v != null)) write(v.longValue());
    }

    public void write(Float v) {
        if (present(v != null)) write(v.floatValue());
    }

    public void write(Double v) {
        if (present(v != null)) write(v.doubleValue());
    }

    public void write(int[] v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.length);
        for (int e : v) varint((e << 1) ^ (e >> 31));
    }

    /**
     * A style index: a palette name, then runs of {@code name, start, length}, each start written as
     * the step from the end of the run before. A run's name and length go as one number, {@code
     * length * names + name}, after the count of names it is made with; that number carries whether
     * the step follows, which is left out when the run touches the one before. With a negative name or
     * length, or too many names, the count is 0 and each run's name and length go on their own. A
     * change to an index (first element {@code Integer.MIN_VALUE}) writes its header the same way
     * before the runs it inserts. Anything not made of whole runs is written as {@link #write(int[])}
     * writes it.
     */
    public void runs(int[] v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.length);
        if (v.length == 0) return;
        int from = runsFrom(v[0], v.length);
        int header = from == 0 ? v.length : from;
        for (int i = 0; i < header; i++) varint((v[i] << 1) ^ (v[i] >> 31));
        if (from == 0) return;
        int names = runNames(v, from);
        varint(names);
        int end = 0;
        for (int i = from; i < v.length; i += 3) {
            int step = v[i + 1] - end;
            int stepped = step != 0 ? 1 : 0;
            if (names > 0) {
                varlong(((long) v[i + 2] * names + v[i]) << 1 | stepped);
            } else {
                varlong((((v[i] << 1) ^ (v[i] >> 31)) & 0xFFFFFFFFL) << 1 | stepped);
            }
            if (step != 0) varint((step << 1) ^ (step >> 31));
            if (names == 0) varint((v[i + 2] << 1) ^ (v[i + 2] >> 31));
            end = v[i + 1] + v[i + 2];
        }
    }

    /** The most names {@link #runs} packs with a length: keeps the packed number exact as a JavaScript double. */
    static final int MAX_RUN_NAMES = 1 << 16;

    /** One more than the largest name of the runs {@code v} holds from {@code from}, or 0 if they cannot be packed. */
    private static int runNames(int[] v, int from) {
        int names = 0;
        for (int i = from; i < v.length; i += 3) {
            if (v[i] < 0 || v[i + 2] < 0 || v[i] >= MAX_RUN_NAMES) return 0;
            names = Math.max(names, v[i] + 1);
        }
        return names;
    }

    /** The header a change to a style index carries before the runs it inserts. */
    public static final int RUNS_CHANGE_HEADER = 6;

    /**
     * Where the runs of a list of {@code length} starting with {@code first} begin as {@link #runs}
     * writes it: after a whole index's palette name, or after a change's header; 0 when it is not
     * made of whole runs.
     */
    public static int runsFrom(int first, int length) {
        int from = first == Integer.MIN_VALUE ? RUNS_CHANGE_HEADER : 1;
        return length >= from && (length - from) % 3 == 0 ? from : 0;
    }

    public void write(long[] v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.length);
        for (long e : v) varlong((e << 1) ^ (e >> 63));
    }

    public void write(char[] v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.length);
        for (char c : v) varint(c);
    }

    public void write(byte[] v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.length);
        ensure(v.length);
        System.arraycopy(v, 0, buf, size, v.length);
        size += v.length;
    }

    public void write(float[] v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.length);
        for (float f : v) write(f);
    }

    public void write(double[] v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.length);
        for (double d : v) write(d);
    }

    public void write(String[] v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.length);
        for (String s : v) writeString(s);
    }

    public void write(Color v) {
        if (v != null && v.isDisposed()) v = null;
        if (!present(v != null)) return;
        int argb = (GraphicsUtils.getAlpha(v) << 24) | (v.getRed() << 16) | (v.getGreen() << 8) | v.getBlue();
        ensure(4);
        buf[size++] = (byte) argb;
        buf[size++] = (byte) (argb >> 8);
        buf[size++] = (byte) (argb >> 16);
        buf[size++] = (byte) (argb >> 24);
    }

    public void write(Point v) {
        if (!present(v != null)) return;
        write(v.x);
        write(v.y);
    }

    public void write(Rectangle v) {
        if (!present(v != null)) return;
        write(v.x);
        write(v.y);
        write(v.width);
        write(v.height);
    }

    public void write(RGB v) {
        if (!present(v != null)) return;
        ensure(3);
        buf[size++] = (byte) v.red;
        buf[size++] = (byte) v.green;
        buf[size++] = (byte) v.blue;
    }

    /** A widget, or anything the client holds as one, by id; 0 for none. */
    public void ref(Object v) {
        if (v == null) {
            varint(0);
            return;
        }
        names.widget(v);
        long id = Serializer.wireId(v);
        varlong(((id << 1) ^ (id >> 63)) + 1);
    }

    public void refs(Object[] v) {
        listFrom = -1;
        int key = lastKey;
        int[] ids = listIds(v);
        int from = size;
        if (v == null) {
            varint(0);
        } else if (ids != null && listChange(v, ids, key, NOT_ITEMS)) {
            listWrittenAs(from, namesHash(v, NOT_ITEMS));
        } else {
            lengthPrefix(v.length);
            for (Object e : v) ref(e);
        }
        if (ids != null) names.listWritten(key, ids);
    }

    /**
     * The items a widget owns ({@code items} in the class comment): each one the client lacks is
     * described in place, as an item of the record type numbered {@code type}, rather than named here
     * and described by a record of its own.
     */
    public void items(Object[] v, int type) {
        listFrom = -1;
        int key = lastKey;
        int[] ids = listIds(v);
        int from = size;
        if (v == null) {
            varint(0);
        } else if (ids != null && listChange(v, ids, key, type)) {
            listWrittenAs(from, namesHash(v, type));
        } else {
            lengthPrefix(v.length);
            boolean described = false;
            for (Object e : v) described |= item(e, type);
            if (described) listWrittenAs(from, namesHash(v, type));
        }
        if (ids != null) names.listWritten(key, ids);
    }

    /** One entry of {@link #items}; true when it described the item in place. */
    private boolean item(Object e, int type) {
        if (e == null) {
            varint(0);
            return false;
        }
        long id = Serializer.wireId(e);
        long z = (id << 1) ^ (id >> 63);
        int at = size;
        varlong((z << 1 | 1) + 1);
        int outerKey = lastKey;
        int outerValue = valueStart;
        lastKey = 0;
        boolean inPlace = names.describeInPlace(e, type);
        lastKey = outerKey;
        valueStart = outerValue;
        if (inPlace) {
            varint(0);
            return true;
        }
        truncate(at);
        names.widget(e);
        varlong((z << 1) + 1);
        return false;
    }

    /** The header of a list that is a change to the one the client holds: no array's length plus one is this. */
    public static final long LIST_CHANGE = (1L << 31) + 1;

    /** {@link #items}' type for a list of widgets only named, {@link #refs}. */
    private static final int NOT_ITEMS = -1;

    private static final int[] NO_IDS = new int[0];

    /** The ids of {@code v} in order, when the list is to be told to {@link Names#listWritten}; else null. */
    private int[] listIds(Object[] v) {
        if (objectDepth > 0 || !names.tracksLists()) return null;
        if (v == null || v.length == 0) return NO_IDS;
        int[] ids = new int[v.length];
        int n = 0;
        for (Object e : v) if (e != null) ids[n++] = (int) Serializer.wireId(e);
        return n == ids.length ? ids : Arrays.copyOf(ids, n);
    }

    /**
     * Writes {@code v} as the change to the list the client holds for {@code key}, when it has one and
     * the change is the shorter: the common head and tail stay, what lies between is replaced.
     */
    private boolean listChange(Object[] v, int[] ids, int key, int type) {
        // A list with holes is held without them, so its positions are not the client's.
        if (ids.length != v.length) return false;
        int[] held = names.heldList(key);
        if (held == null) return false;
        int limit = Math.min(held.length, ids.length);
        int head = 0;
        while (head < limit && held[head] == ids[head]) head++;
        int tail = 0;
        while (tail < limit - head && held[held.length - 1 - tail] == ids[ids.length - 1 - tail]) tail++;
        int removed = held.length - head - tail;
        int inserted = ids.length - head - tail;
        int checksum = listChecksum(ids);
        int header = varlongSize(LIST_CHANGE) + varintSize(checksum) + varintSize(head) + varintSize(removed)
                + varintSize(inserted);
        // What the change leaves out: the length and the names of the elements it keeps.
        int kept = varintSize(v.length + 1);
        for (int i = 0; i < head; i++) kept += nameSize(ids[i], type);
        for (int i = ids.length - tail; i < ids.length; i++) kept += nameSize(ids[i], type);
        if (header >= kept) return false;
        // What the list keeps is not named, so nothing it keeps can be one the client is owed.
        for (int i = 0; i < head; i++) if (!names.holds(v[i])) return false;
        for (int i = ids.length - tail; i < ids.length; i++) if (!names.holds(v[i])) return false;
        varlong(LIST_CHANGE);
        varint(checksum);
        varint(head);
        varint(removed);
        varint(inserted);
        for (int i = head; i < head + inserted; i++) {
            if (type == NOT_ITEMS) ref(v[i]);
            else item(v[i], type);
        }
        return true;
    }

    /**
     * A number both sides compute alike over a list's ids, kept below 2^30 so every intermediate is
     * an exact integer in a JavaScript double as well.
     */
    public static int listChecksum(int[] ids) {
        long hash = ids.length;
        for (int id : ids) hash = (hash * 31 + id) & 0x3FFFFFFF;
        return (int) hash;
    }

    private static int nameSize(int id, int type) {
        long z = ((long) id << 1) ^ ((long) id >> 63);
        return varlongSize(type == NOT_ITEMS ? z + 1 : (z << 1) + 1);
    }

    /** The list written from {@code from} to here hashes as {@code hash}: see {@link #valueHash}. */
    private void listWrittenAs(int from, long hash) {
        listFrom = from;
        listTo = size;
        listHash = hash;
    }

    /** Where the last list that is not written as itself was written, or -1; and what it hashes as. */
    private int listFrom = -1;
    private int listTo;
    private long listHash;

    /** The hash of {@code v} written whole, its elements only named. */
    private static long namesHash(Object[] v, int type) {
        WidgetWire w = borrow();
        try {
            w.reset();
            w.lengthPrefix(v.length);
            for (Object e : v) {
                if (e == null) {
                    w.varint(0);
                    continue;
                }
                long id = Serializer.wireId(e);
                long z = (id << 1) ^ (id >> 63);
                w.varlong(type == NOT_ITEMS ? z + 1 : (z << 1) + 1);
            }
            return w.hash(0, w.size);
        } finally {
            giveBack(w);
        }
    }

    /**
     * {@link #hash} of the value written at {@code [from, to)}, except for a list written as a change or
     * describing items in place: that one hashes as the whole list will be written once the client
     * holds it, so a list that has not changed since is not sent again, however it travelled.
     */
    public long valueHash(int from, int to) {
        if (from == listFrom && to == listTo) {
            listFrom = -1;
            return listHash;
        }
        return hash(from, to);
    }

    /** An image, by id; 0 for none. */
    public void image(Image v) {
        if (v == null || v.isDisposed()) {
            varint(0);
            return;
        }
        long id = names.imageRef(v);
        varlong(((id << 1) ^ (id >> 63)) + 1);
    }

    public void images(Image[] v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.length);
        for (Image e : v) image(e);
    }

    /** An object: a presence byte, then its fields, closed by a 0. */
    public void object(WireObject v) {
        if (!present(v != null)) return;
        fields(v);
    }

    private void fields(WireObject v) {
        int outer = lastKey;
        lastKey = 0;
        objectDepth++;
        try {
            v.writeWireFields(this);
        } finally {
            objectDepth--;
        }
        varint(0);
        lastKey = outer;
    }

    /** How many objects the value being written sits inside: a list in one is never a change. */
    private int objectDepth;

    /**
     * Opens a frame in this buffer: the header of the channel numbered {@code channel} of the widget
     * of type {@code type} numbered {@code id}. Returns where the payload starts.
     */
    public int header(int type, long id, int channel) {
        ensure(dev.equo.swt.comm.ChannelHeader.MAX_NUMBERED);
        size = dev.equo.swt.comm.ChannelHeader.write(buf, size, type, id, channel);
        return size;
    }

    /** Writes {@code v}'s fields, closed by a 0, after what is already written. */
    public void writeFields(WireObject v) {
        fields(v);
    }

    /** {@code v}'s fields, closed by a 0: an object sent as a message of its own. */
    public static byte[] fieldsOf(WireObject v) {
        WidgetWire w = borrow();
        try {
            w.reset();
            w.fields(v);
            return Arrays.copyOf(w.buf, w.size);
        } finally {
            giveBack(w);
        }
    }

    /** An object whose class is only known now: a presence byte, its class's number, its fields, a 0. */
    public void poly(WireObject v) {
        if (!present(v != null)) return;
        varint(v.wireTypeId());
        fields(v);
    }

    public void objects(WireObject[] v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.length);
        for (WireObject e : v) object(e);
    }

    /** A map of strings to strings: {@code WireType.map(T_STRING, T_STRING)}. */
    public void writeStrings(java.util.Map<String, String> v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.size());
        for (java.util.Map.Entry<String, String> e : v.entrySet()) {
            writeString(e.getKey());
            writeString(e.getValue());
        }
    }

    /** A map of strings to booleans: {@code WireType.map(T_STRING, T_BOOL_BOXED)}. */
    public void writeBooleans(java.util.Map<String, Boolean> v) {
        if (v == null) {
            varint(0);
            return;
        }
        lengthPrefix(v.size());
        for (java.util.Map.Entry<String, Boolean> e : v.entrySet()) {
            writeString(e.getKey());
            write(e.getValue());
        }
    }

    /** A list whose elements the caller writes itself: their count, or -1 for null. */
    public void listSize(int count) {
        if (count < 0) varint(0);
        else lengthPrefix(count);
    }

    /**
     * In a list of objects, where a presence byte would be: the run of nulls that follows, its length
     * after it. A lone null keeps its single 0.
     */
    public static final int NULL_RUN = 2;

    /**
     * Writes the run of nulls in a list of objects that starts at {@code a[from]}, which is null, and
     * returns its length.
     */
    public int nulls(Object[] a, int from) {
        int run = 1;
        while (from + run < a.length && a[from + run] == null) run++;
        ensure(1);
        if (run == 1) {
            buf[size++] = 0;
        } else {
            buf[size++] = NULL_RUN;
            varint(run);
        }
        return run;
    }

    /** Writes the presence byte and returns {@code present}, so a writer can go on to the value. */
    public boolean present(boolean present) {
        ensure(1);
        buf[size++] = (byte) (present ? 1 : 0);
        return present;
    }

    // --- encoding ---

    /**
     * A string as both ends hold one, so neither encodes nor decodes: {@code varint(n << 1 | wide) + 1}
     * (0 for null), then {@code n} ASCII bytes, or, when a char is past 0x7F, {@code n} UTF-16 code
     * units little endian. ASCII rather than all of Latin-1, so a browser client can hand the bytes to
     * its own decoder as they are.
     */
    @SuppressWarnings("deprecation")
    void writeString(String s) {
        if (s == null) {
            varint(0);
            return;
        }
        int n = s.length();
        int ascii = 0;
        while (ascii < n && s.charAt(ascii) < 0x80) ascii++;
        if (ascii == n) {
            if (n == 0) emptyAt = size;
            ensure(5 + n);
            varint((n << 1) + 1);
            // The low byte of each char: a plain copy of an ASCII string's bytes.
            s.getBytes(0, n, buf, size);
            size += n;
            return;
        }
        ensure(5 + 2 * n);
        varint(((n << 1) | 1) + 1);
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            buf[size++] = (byte) c;
            buf[size++] = (byte) (c >> 8);
        }
    }

    void varint(int v) {
        ensure(5);
        while ((v & ~0x7F) != 0) {
            buf[size++] = (byte) ((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        buf[size++] = (byte) v;
    }

    void varlong(long v) {
        ensure(10);
        while ((v & ~0x7FL) != 0) {
            buf[size++] = (byte) ((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        buf[size++] = (byte) v;
    }

    private static int varlongSize(long v) {
        int n = 1;
        while ((v & ~0x7FL) != 0) {
            n++;
            v >>>= 7;
        }
        return n;
    }

    private static int varintSize(int v) {
        int n = 1;
        while ((v & ~0x7F) != 0) {
            n++;
            v >>>= 7;
        }
        return n;
    }

    private void ensure(int more) {
        if (size + more > buf.length) buf = Arrays.copyOf(buf, Math.max(buf.length * 2, size + more));
    }
}
