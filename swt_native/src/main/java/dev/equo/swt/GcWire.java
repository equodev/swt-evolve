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
 * <li>{@link #OP_NUMBERED}: an op by the number both sides know it by (wire-channels.txt), then its
 * arguments. It takes the next op number in the frame.</li>
 * <li>{@link #OP_FIRST} and up: an op number given earlier in the frame, then only the arguments.</li>
 * <li>{@link #STATE}: a change to the GC's state, and {@link #WHOLE}: the whole of it, replacing what
 * the client holds. Either is a length and then properties as the widget wire writes them (each a
 * key delta and a value, closed by a 0).</li>
 * <li>{@link #ESCAPE}: a channel and a payload, delivered as if it had come on its own: the frames
 * that end a paint.</li>
 * </ul>
 *
 * <p>Arguments by signature letter: {@code i} zigzag varint, {@code I} an {@code int[]} (varint length
 * plus one, 0 for null, then zigzag varints), {@code b} a byte, {@code s} a string as the widget wire
 * writes one (Latin-1 or UTF-16, so neither end transcodes it), {@code f} a float and {@code d} a
 * double (little endian), {@code w} any other value as the widget wire encodes its type (varint byte
 * length plus one, 0 for null, then the bytes). An image a
 * value names goes by id; what the client lacks of it is sent ahead of the frame
 * ({@link #images(WidgetWire.Names)}).
 */
public final class GcWire {

    /** An op's arguments in the order its signature names them. */
    public interface Op {
        /** The number both sides know this op by: its channel's in wire-channels.txt. */
        int wireNumber();

        String signature();

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

    /** Above any channel's number. */
    private static final int MAX_OPS = 4096;

    /** Each op's signature by number, as written: what {@link #entries} reads a numbered op with. */
    private static final java.util.concurrent.atomic.AtomicReferenceArray<String> SIGNATURES =
            new java.util.concurrent.atomic.AtomicReferenceArray<>(MAX_OPS);

    /** Each op's {@link Op#wireKinds()} by number, for {@link #entries}. */
    private static final java.util.concurrent.atomic.AtomicReferenceArray<String[]> KINDS =
            new java.util.concurrent.atomic.AtomicReferenceArray<>(MAX_OPS);

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
    private String channel;
    /** Per op number, its number in this frame; valid where {@link #opFrame} holds {@link #frame}. */
    private final int[] opInFrame = new int[MAX_OPS];
    private final int[] opFrame = new int[MAX_OPS];
    private int frame;
    private int opsInFrame;
    private final WidgetWire scratch = new WidgetWire();
    private WidgetWire.Names images;

    /** Starts an empty frame for the GC on {@code channel}. */
    public void begin(String channel) {
        this.channel = channel;
        byte[] header = CommService.frameHeader(frameEvent(channel));
        ensure(header.length);
        System.arraycopy(header, 0, buf, 0, header.length);
        size = header.length;
        records = 0;
        frame++;
        opsInFrame = 0;
    }

    /** Where the images that values name are reported, so what the client lacks is sent first. */
    public void images(WidgetWire.Names names) {
        this.images = names;
    }

    public String channel() {
        return channel;
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
        return entries(channel, buf, start, size - start);
    }

    /** Writes {@code op}'s record: its number, then its arguments. */
    public void op(Op op) {
        records++;
        int number = op.wireNumber();
        if (opFrame[number] == frame) {
            varint(OP_FIRST + opInFrame[number]);
        } else {
            opFrame[number] = frame;
            opInFrame[number] = opsInFrame++;
            if (SIGNATURES.get(number) == null) {
                SIGNATURES.set(number, op.signature());
                KINDS.set(number, op.wireKinds());
            }
            varint(OP_NUMBERED);
            varint(number);
        }
        op.writeBinary(this);
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

    /** A channel and its payload, as it would have travelled on its own. */
    public void escape(String channel, byte[] payload, int offset, int length) {
        records++;
        varint(ESCAPE);
        byte[] header = dev.equo.swt.comm.ChannelHeader.encode(channel);
        ensure(size + header.length);
        System.arraycopy(header, 0, buf, size, header.length);
        size += header.length;
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
        List<Integer> opNumbers = new ArrayList<>();
        while (r.pos < r.end) {
            int tag = r.varint();
            if (tag == ESCAPE) {
                dev.equo.swt.comm.ChannelHeader.Decoded header = dev.equo.swt.comm.ChannelHeader.decode(r.b, r.pos, r.end);
                r.pos = header.payloadStart();
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
                int wire = r.varint();
                opNumbers.add(wire);
                number = opNumbers.size() - 1;
            } else {
                number = tag - OP_FIRST;
            }
            int wire = opNumbers.get(number);
            String known = dev.equo.swt.comm.ChannelHeader.name(wire);
            String name = known != null ? known : "op" + wire;
            String signature = SIGNATURES.get(wire) == null ? "" : SIGNATURES.get(wire);
            String[] kinds = KINDS.get(wire);
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
