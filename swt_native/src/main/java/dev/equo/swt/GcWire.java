package dev.equo.swt;

import dev.equo.swt.comm.CommService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * One paint of a GC as a single binary frame, on the GC's channel with {@code /b} appended.
 *
 * <p>The frame is a run of records, each opened by a varint tag:
 * <ul>
 * <li>{@link #OP_NUMBERED}: an op's number ({@link WireTable}), then its arguments. It takes the next
 * op number in the frame.</li>
 * <li>{@link #OP_FIRST} and up: an op number given earlier in the frame, then only the arguments.</li>
 * <li>{@link #STATE}: a change to the GC's state, and {@link #WHOLE}: the whole of it, replacing what
 * the client holds. Either is a length and then properties as the widget wire writes them (each a
 * key delta and a value, closed by a 0).</li>
 * <li>{@link #ESCAPE}: a channel and a payload, delivered as if it had come on its own: the frames
 * that end a paint.</li>
 * </ul>
 *
 * <p>Arguments by signature letter: {@code i} zigzag varint, {@code I} an {@code int[]} (varint length
 * plus one, 0 for null, then zigzag varints), {@code b} a byte, {@code s} a string (varint byte
 * length plus one, 0 for null, then UTF-8), {@code f} a float and {@code d} a double (little endian),
 * {@code w} any other value as the widget wire encodes its type (length as for a string). An image a
 * value names goes by id; what the client lacks of it is sent ahead of the frame
 * ({@link #images(WidgetWire.Names)}).
 */
public final class GcWire {

    /** An op's arguments in the order its signature names them. */
    public interface Op {
        String signature();

        /** The number both sides know this op's name by (see ChannelHeader), or 0 when it has none. */
        default int number() {
            return 0;
        }

        void writeBinary(GcWire w);

        /** The widget wire encoding of each {@code w} argument, in order, for {@link #describe}. */
        default String[] wireKinds() {
            return null;
        }
    }

    public static final int STATE = 1;
    public static final int ESCAPE = 2;
    public static final int OP_NUMBERED = 3;
    public static final int WHOLE = 4;
    public static final int OP_FIRST = 5;

    /** Each op's signature by name, as written: what {@link #describe} reads a numbered op with. */
    private static final Map<String, String> SIGNATURES = new java.util.concurrent.ConcurrentHashMap<>();

    /** Each op's {@link Op#wireKinds()} by name, for {@link #describe}. */
    private static final Map<String, String[]> KINDS = new java.util.concurrent.ConcurrentHashMap<>();

    /** What a GC's binary frame is called, given the channel its other frames are on. */
    public static String frameEvent(String channel) {
        return channel + "/b";
    }

    private Object owner;

    /** Whether this frame is still the one buffering {@code gc}'s ops. */
    public boolean ownedBy(Object gc) {
        return owner == gc;
    }

    /** Hands the frame to {@code gc}, or with null takes it back once it stops buffering. */
    public void own(Object gc) {
        owner = gc;
        if (gc != null) writer = Thread.currentThread();
    }

    /**
     * The thread writing this frame. A frame is written without locking, so only that thread may
     * send it from an event-loop flush: Displays sharing a JVM each flush on their own thread, and one
     * sending another's frame sends it mid-write and recycles it while it is still being filled.
     */
    public Thread writer() {
        return writer;
    }

    private Thread writer;

    private byte[] buf = new byte[4096];
    private int size;
    private int records;
    /** The type and number of what the GC draws as: the frame's channel, and every escaped message's. */
    private int type;
    private long id;
    /** How many ops the frame has numbered. */
    private int opCount;
    /** Each numbered op's number in the frame plus one, by its name's number; 0 for one not seen yet. */
    private int[] opIndex = new int[0];
    /** The names' numbers {@link #opIndex} holds an entry for, to clear at the next frame. */
    private int[] opsSeen = new int[16];
    private int opsSeenCount;
    /** The ops whose signature is recorded for {@link #describe}, by number. */
    private static volatile boolean[] described = new boolean[0];
    private final WidgetWire scratch = new WidgetWire();
    private WidgetWire.Names images;

    /** The header of the frames {@link #begin} last began: a GC paints as the same widget frame after frame. */
    private byte[] header;
    private int headerType;
    private long headerId;

    /** Starts an empty frame for the GC drawing as the widget of type {@code type} numbered {@code id}. */
    public void begin(int type, long id) {
        this.type = type;
        this.id = id;
        if (header == null || type != headerType || id != headerId) {
            header = dev.equo.swt.comm.ChannelHeader.encode(type, id, WireTable.CHANNEL_B);
            headerType = type;
            headerId = id;
        }
        ensure(header.length);
        System.arraycopy(header, 0, buf, 0, header.length);
        size = header.length;
        records = 0;
        opCount = 0;
        for (int i = 0; i < opsSeenCount; i++) opIndex[opsSeen[i]] = 0;
        opsSeenCount = 0;
    }

    /** Where the images that values name are reported, so what the client lacks is sent first. */
    public void images(WidgetWire.Names names) {
        this.images = names;
    }

    /** The channel the GC draws on, by name: for logs and tests. */
    public String channel() {
        return dev.equo.swt.comm.ChannelHeader.typeName(type) + "/" + id;
    }

    public boolean isEmpty() {
        return records == 0;
    }

    public int byteSize() {
        return size;
    }

    /** The frame so far; a record is {@code buffer()[from, byteSize())} for a {@code from} read before it. */
    public byte[] buffer() {
        return buf;
    }

    public void sendTo(CommService comm) {
        if (records > 0) comm.sendFrame(buf, 0, size);
    }

    /** This frame's records as {@link #entries(String, byte[], int, int)} reads them, past its channel header. */
    public List<Entry> entries() {
        int start = dev.equo.swt.comm.ChannelHeader.decode(buf, 0, size).payloadStart();
        return entries(channel(), buf, start, size - start);
    }

    /** Opens the record for {@code name}; its arguments follow. */
    /** Appends {@code op}, by the number both sides know it by, and its arguments. */
    public void op(Op op) {
        records++;
        int known = op.number();
        if (known <= 0) throw new IllegalArgumentException("the op " + op.signature() + " has no wire number");
        numbered(op, known);
        op.writeBinary(this);
    }

    /** Opens the record of an op numbered {@code known}. */
    private void numbered(Op op, int known) {
        if (known >= opIndex.length) opIndex = java.util.Arrays.copyOf(opIndex, Math.max(known + 1, opIndex.length * 2));
        int index = opIndex[known];
        if (index != 0) {
            varint(OP_FIRST + index - 1);
            return;
        }
        opIndex[known] = ++opCount;
        if (opsSeenCount == opsSeen.length) opsSeen = java.util.Arrays.copyOf(opsSeen, opsSeenCount * 2);
        opsSeen[opsSeenCount++] = known;
        boolean[] d = described;
        if (known >= d.length || !d[known]) describedOnce(dev.equo.swt.comm.ChannelHeader.name(known), op, known);
        varint(OP_NUMBERED);
        varint(known);
    }

    private static synchronized void describedOnce(String name, Op op, int known) {
        SIGNATURES.putIfAbsent(name, op.signature());
        String[] kinds = op.wireKinds();
        if (kinds != null) KINDS.putIfAbsent(name, kinds);
        boolean[] d = java.util.Arrays.copyOf(described, Math.max(described.length, known + 1));
        d[known] = true;
        described = d;
    }

    /**
     * An empty buffer for one value or one set of properties in the widget wire's encoding, which
     * {@link #state} or {@link #w} then copies into the frame. One per frame: the next call empties it.
     */
    public WidgetWire scratch() {
        scratch.reset();
        scratch.names(images);
        return scratch;
    }

    /** A {@link #STATE} or {@link #WHOLE} record of the properties written into {@code props}. */
    public void state(int tag, WidgetWire props) {
        records++;
        varint(tag);
        props.varint(0);
        bytes(props.buffer(), 0, props.size());
    }

    /**
     * What ends a GC's paint ({@code gcDispose}), positionally: a flags byte (bit 0, the paint replaces
     * what was drawn; bit 1, a damage rectangle follows; bit 2, the client sends the render's pixels
     * back; bit 3, the drawer keeps drawing on the render), the image's render ref, then the damage as
     * zigzag x, y, width and height.
     */
    public static final class Dispose {
        /** At most: the flags, a ref and four rectangle values. */
        static final int MAX_BYTES = 1 + 10 + 4 * 5;

        final boolean fullRepaint;
        final org.eclipse.swt.graphics.Rectangle damage;
        final long ref;
        final boolean pixels;
        final boolean retain;

        private Dispose(boolean fullRepaint, org.eclipse.swt.graphics.Rectangle damage, long ref, boolean pixels, boolean retain) {
            this.fullRepaint = fullRepaint;
            this.damage = damage;
            this.ref = ref;
            this.pixels = pixels;
            this.retain = retain;
        }

        /** A control's paint, replacing what it drew when {@code fullRepaint}, scoped to {@code damage} when not null. */
        public static Dispose ofPaint(boolean fullRepaint, org.eclipse.swt.graphics.Rectangle damage) {
            return new Dispose(fullRepaint, damage, 0, false, false);
        }

        /** An image's paint, rendered into the client's image {@code ref} (0: render nothing). */
        public static Dispose ofImage(long ref, boolean pixels, boolean retain) {
            return new Dispose(true, null, ref, pixels, retain);
        }

        /** Writes the fields into {@code out} at {@code at}; returns where they end. */
        int write(byte[] out, int at) {
            return write(out, at, fullRepaint, damage, ref, pixels, retain);
        }

        static int write(byte[] out, int at, boolean fullRepaint, org.eclipse.swt.graphics.Rectangle damage, long ref,
                boolean pixels, boolean retain) {
            boolean scoped = damage != null;
            out[at++] = (byte) ((fullRepaint ? 1 : 0) | (scoped ? 2 : 0) | (pixels ? 4 : 0) | (retain ? 8 : 0));
            at = varint(out, at, ref);
            if (scoped) {
                at = varint(out, at, zigzag(damage.x));
                at = varint(out, at, zigzag(damage.y));
                at = varint(out, at, zigzag(damage.width));
                at = varint(out, at, zigzag(damage.height));
            }
            return at;
        }

        /** The fields on their own, for a {@code gcDispose} sent outside a frame. */
        public byte[] encode() {
            byte[] out = new byte[MAX_BYTES];
            return Arrays.copyOf(out, write(out, 0));
        }

        /** {@code payload}'s fields by name, for a test or a log: what {@link #write} wrote. */
        public static Map<String, Object> fields(byte[] payload) {
            Reader r = new Reader(payload, 0, payload.length);
            int flags = payload[r.pos++] & 0xFF;
            Map<String, Object> out = new java.util.LinkedHashMap<>();
            out.put("fullRepaint", (flags & 1) != 0);
            out.put("pixels", (flags & 4) != 0);
            out.put("retain", (flags & 8) != 0);
            out.put("ref", r.varlong());
            if ((flags & 2) != 0) {
                Map<String, Object> damage = new java.util.LinkedHashMap<>();
                for (String key : new String[] {"x", "y", "width", "height"}) {
                    long z = r.varlong();
                    damage.put(key, (int) ((z >>> 1) ^ -(z & 1)));
                }
                out.put("damage", damage);
            }
            return out;
        }

        private static long zigzag(int v) {
            return ((long) v << 1) ^ (v >> 31);
        }

        private static int varint(byte[] out, int at, long v) {
            while ((v & ~0x7FL) != 0) {
                out[at++] = (byte) ((v & 0x7F) | 0x80);
                v >>>= 7;
            }
            out[at++] = (byte) v;
            return at;
        }
    }

    /** Ends the GC's paint on this frame: a {@code gcDispose} of {@code d}, written straight into it. */
    public void dispose(Dispose d) {
        dispose(d.fullRepaint, d.damage, d.ref, d.pixels, d.retain);
    }

    /** {@link #dispose(Dispose)} from the fields themselves. */
    public void dispose(boolean fullRepaint, org.eclipse.swt.graphics.Rectangle damage, long ref, boolean pixels, boolean retain) {
        records++;
        varint(ESCAPE);
        ensure(size + dev.equo.swt.comm.ChannelHeader.MAX_NUMBERED + 1 + Dispose.MAX_BYTES);
        size = dev.equo.swt.comm.ChannelHeader.write(buf, size, type, id, WireTable.CHANNEL_GC_DISPOSE);
        // The fields take fewer than 127 bytes, so their length (plus one) fits the one byte kept for it.
        int lengthAt = size++;
        size = Dispose.write(buf, size, fullRepaint, damage, ref, pixels, retain);
        buf[lengthAt] = (byte) (size - lengthAt);
    }

    /** A message on the GC's channel numbered {@code channel} and its payload, as it would have travelled on its own. */
    public void escape(int channel, byte[] payload, int offset, int length) {
        records++;
        varint(ESCAPE);
        ensure(size + dev.equo.swt.comm.ChannelHeader.MAX_NUMBERED);
        size = dev.equo.swt.comm.ChannelHeader.write(buf, size, type, id, channel);
        bytes(payload, offset, length);
    }

    // Arguments.

    public void i(int v) {
        varint((v << 1) ^ (v >> 31));
    }

    public void ints(int[] v) {
        if (v == null) {
            varint(0);
            return;
        }
        varint(v.length + 1);
        ensure(size + 5 * v.length);
        for (int x : v) i(x);
    }

    public void b(boolean v) {
        ensure(size + 1);
        buf[size++] = (byte) (v ? 1 : 0);
    }

    /** {@code varint(n << 1 | wide) + 1} (0 for null), then {@code n} Latin-1 bytes, or UTF-16 code units little endian. */
    @SuppressWarnings("deprecation")
    public void s(String v) {
        if (v == null) {
            varint(0);
            return;
        }
        int n = v.length();
        int latin1 = 0;
        while (latin1 < n && v.charAt(latin1) <= 0xFF) latin1++;
        if (latin1 == n) {
            varint((n << 1) + 1);
            ensure(size + n);
            // The low byte of each char: a plain copy of a Latin-1 string's bytes.
            v.getBytes(0, n, buf, size);
            size += n;
            return;
        }
        varint(((n << 1) | 1) + 1);
        ensure(size + 2 * n);
        for (int i = 0; i < n; i++) {
            char c = v.charAt(i);
            buf[size++] = (byte) c;
            buf[size++] = (byte) (c >> 8);
        }
    }

    public void f(float v) {
        int bits = Float.floatToRawIntBits(v);
        ensure(size + 4);
        for (int k = 0; k < 4; k++) buf[size++] = (byte) (bits >>> (8 * k));
    }

    public void d(double v) {
        long bits = Double.doubleToRawLongBits(v);
        ensure(size + 8);
        for (int k = 0; k < 8; k++) buf[size++] = (byte) (bits >>> (8 * k));
    }

    /** A {@code w} argument: the value written into {@link #scratch()}. */
    public void w(WidgetWire value) {
        bytes(value.buffer(), 0, value.size());
    }

    /** Length-prefixed bytes; length 0 is null, so an empty run is a length of 1. */
    private void bytes(byte[] b, int offset, int length) {
        varint(length + 1);
        ensure(size + length);
        System.arraycopy(b, offset, buf, size, length);
        size += length;
    }

    private void varint(int v) {
        ensure(size + 5);
        while ((v & ~0x7F) != 0) {
            buf[size++] = (byte) ((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        buf[size++] = (byte) v;
    }

    private void ensure(int needed) {
        if (needed > buf.length) buf = Arrays.copyOf(buf, Math.max(needed, buf.length * 2));
    }

    /** One record of a frame: the channel it is on and what it carries, decoded. */
    public static final class Entry {
        public final String channel;
        public final Object value;
        /** Whether this entry names its op, rather than referring to one named earlier in the frame. */
        public final boolean named;

        Entry(String channel, Object value) {
            this(channel, value, false);
        }

        Entry(String channel, Object value, boolean named) {
            this.channel = channel;
            this.value = value;
            this.named = named;
        }

        @Override
        public String toString() {
            return channel + " " + value;
        }
    }

    /**
     * The frame's records decoded, for tests and logs, which read the traffic rather than draw it: an
     * op as its arguments in order, a change as its properties under {@code _d}, a whole state under
     * {@code _whole}, and an entry on a channel of its own as its payload: tagged values decoded, any
     * other payload as its UTF-8 text.
     */
    public static List<Entry> entries(String channel, byte[] frame, int offset, int length) {
        Reader r = new Reader(frame, offset, offset + length);
        List<Entry> out = new ArrayList<>();
        List<String> opNames = new ArrayList<>();
        List<String> signatures = new ArrayList<>();
        while (r.pos < r.end) {
            int tag = r.varint();
            if (tag == ESCAPE) {
                dev.equo.swt.comm.ChannelHeader.Decoded header = dev.equo.swt.comm.ChannelHeader.decode(r.b, r.pos, r.end);
                r.pos = header.payloadStart();
                if (header.channel().endsWith("/gcDispose")) {
                    int fields = r.varint() - 1;
                    out.add(new Entry(header.channel(), Dispose.fields(Arrays.copyOfRange(r.b, r.pos, r.pos + fields))));
                    r.pos += fields;
                    continue;
                }
                out.add(new Entry(header.channel(), r.payload()));
                continue;
            }
            if (tag == STATE || tag == WHOLE) {
                int bytes = r.varint() - 1;
                Map<String, Object> props = new java.util.LinkedHashMap<>();
                props.put(tag == STATE ? "_d" : "_whole", tag == STATE ? (Object) 0 : (Object) true);
                props.putAll(WireDump.fields(r.b, r.pos, r.pos + bytes));
                r.pos += bytes;
                out.add(new Entry(channel, props));
                continue;
            }
            int number;
            if (tag == OP_NUMBERED) {
                String name = dev.equo.swt.comm.ChannelHeader.name(r.varint());
                opNames.add(name);
                signatures.add(SIGNATURES.getOrDefault(name, ""));
                number = opNames.size() - 1;
            } else if (tag >= OP_FIRST) {
                number = tag - OP_FIRST;
            } else {
                throw new IllegalStateException("GC frame tag " + tag + " is not one");
            }
            String name = opNames.get(number);
            String signature = signatures.get(number);
            String[] kinds = KINDS.get(name);
            List<Object> arguments = new ArrayList<>();
            int w = 0;
            for (int k = 0; k < signature.length(); k++) {
                char type = signature.charAt(k);
                arguments.add(type == 'w' ? r.wireValue(kinds == null || w >= kinds.length ? null : kinds[w++]) : r.argument(type));
            }
            out.add(new Entry(channel + "/" + name, arguments, tag == OP_NUMBERED));
        }
        return out;
    }

    private static final class Reader {
        final byte[] b;
        int pos;
        final int end;

        Reader(byte[] b, int pos, int end) {
            this.b = b;
            this.pos = pos;
            this.end = end;
        }

        int varint() {
            int v = 0;
            for (int shift = 0; ; shift += 7) {
                int x = b[pos++] & 0xFF;
                v |= (x & 0x7F) << shift;
                if ((x & 0x80) == 0) return v;
            }
        }

        int zigzag() {
            int v = varint();
            return (v >>> 1) ^ -(v & 1);
        }

        long varlong() {
            long v = 0;
            for (int shift = 0; ; shift += 7) {
                int x = b[pos++] & 0xFF;
                v |= (long) (x & 0x7F) << shift;
                if ((x & 0x80) == 0) return v;
            }
        }

        /** A payload: tagged values decoded, anything else as its UTF-8 text; null for none. */
        Object payload() {
            int length = varint();
            if (length == 0) return null;
            int start = pos;
            pos += length - 1;
            if (!dev.equo.swt.comm.TaggedValues.isTagged(b, start, length - 1)) {
                return new String(b, start, length - 1, StandardCharsets.UTF_8);
            }
            return dev.equo.swt.comm.TaggedValues.decode(b, start, length - 1);
        }

        /** A {@code w} argument of the widget wire encoding {@code kind}, decoded. */
        Object wireValue(String kind) {
            int length = varint();
            if (length == 0) return null;
            int start = pos;
            pos += length - 1;
            if (kind == null) return (length - 1) + " bytes";
            return WireDump.value(b, start, pos, kind);
        }

        String string() {
            int header = varint();
            if (header == 0) return null;
            int n = (header - 1) >> 1;
            char[] chars = new char[n];
            if (((header - 1) & 1) == 0) {
                for (int i = 0; i < n; i++) chars[i] = (char) (b[pos++] & 0xFF);
            } else {
                for (int i = 0; i < n; i++, pos += 2) chars[i] = (char) ((b[pos] & 0xFF) | (b[pos + 1] & 0xFF) << 8);
            }
            return new String(chars);
        }

        long bits(int count) {
            long v = 0;
            for (int k = 0; k < count; k++) v |= (long) (b[pos++] & 0xFF) << (8 * k);
            return v;
        }

        Object argument(char type) {
            switch (type) {
                case 'i':
                    return zigzag();
                case 'b':
                    return b[pos++] != 0;
                case 'f':
                    return Float.intBitsToFloat((int) bits(4));
                case 'd':
                    return Double.longBitsToDouble(bits(8));
                case 's':
                    return string();
                case 'I': {
                    int length = varint();
                    if (length == 0) return null;
                    List<Object> ints = new ArrayList<>();
                    for (int k = 0; k < length - 1; k++) ints.add(zigzag());
                    return ints;
                }
                default:
                    return payload();
            }
        }
    }
}
