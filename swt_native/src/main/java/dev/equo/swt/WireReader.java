package dev.equo.swt;

import java.nio.charset.StandardCharsets;

/**
 * Reads what the client sends in the binary wire's encoding (see {@link WidgetWire}): an object's
 * fields as key and value, each key the field's number less the previous one's, closed by a 0.
 */
public final class WireReader {

    private byte[] buf;
    private int pos;
    private int end;

    public WireReader(byte[] buf, int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.end = offset + length;
    }

    /**
     * This thread's reader over {@code buf[offset, offset + length)}: valid until the next call on
     * the thread, for a reader that reads and lets go, as a frame's handler does.
     */
    public static WireReader over(byte[] buf, int offset, int length) {
        WireReader r = LENT.get();
        r.buf = buf;
        r.pos = offset;
        r.end = offset + length;
        return r;
    }

    private static final ThreadLocal<WireReader> LENT = ThreadLocal.withInitial(() -> new WireReader(new byte[0], 0, 0));

    /** What a handler was lent: the frame's body. */
    public WireReader(java.nio.ByteBuffer body) {
        this(body.array(), body.arrayOffset() + body.position(), body.remaining());
    }

    /** Reads {@code target}'s fields until the 0 that closes them, or the end of the bytes. */
    public <T extends WireReadable> T fields(T target) {
        int key = 0;
        while (pos < end) {
            int delta = zigzag();
            if (delta == 0) break;
            key += delta;
            if (!target.readWire(key, this))
                throw new IllegalStateException(target.getClass().getSimpleName() + " has no field " + key);
        }
        return target;
    }

    public int u8() {
        return buf[pos++] & 0xFF;
    }

    /** An unsigned varint: a count or a length. */
    public int varint() {
        int b = buf[pos++];
        if (b >= 0) return b;
        int v = b & 0x7F;
        for (int shift = 7; ; shift += 7) {
            b = buf[pos++];
            v |= (b & 0x7F) << shift;
            if (b >= 0) return v;
        }
    }

    public boolean bool() {
        return buf[pos++] != 0;
    }

    public Boolean boolBoxed() {
        byte b = buf[pos++];
        return b == 0 ? null : b == 2;
    }

    public int int32() {
        return zigzag();
    }

    public long int64() {
        long v = varlong();
        return (v >>> 1) ^ -(v & 1);
    }

    public char char16() {
        return (char) varint();
    }

    public float float32() {
        return Float.intBitsToFloat(le32());
    }

    public double float64() {
        long lo = le32() & 0xFFFFFFFFL;
        long hi = le32() & 0xFFFFFFFFL;
        return Double.longBitsToDouble(lo | (hi << 32));
    }

    public Integer intBoxed() {
        return buf[pos++] == 0 ? null : int32();
    }

    public Long longBoxed() {
        return buf[pos++] == 0 ? null : int64();
    }

    public Float floatBoxed() {
        return buf[pos++] == 0 ? null : float32();
    }

    public Double doubleBoxed() {
        return buf[pos++] == 0 ? null : float64();
    }

    /** A string in {@link WidgetWire}'s form: Latin-1 bytes or UTF-16 code units. */
    @SuppressWarnings("deprecation")
    public String string() {
        int header = varint();
        if (header == 0) return null;
        int n = (header - 1) >>> 1;
        if (((header - 1) & 1) == 0) {
            need(n);
            String s = new String(buf, 0, pos, n);
            pos += n;
            return s;
        }
        need(2L * n);
        char[] chars = new char[n];
        for (int i = 0; i < n; i++, pos += 2) chars[i] = (char) ((buf[pos] & 0xFF) | (buf[pos + 1] & 0xFF) << 8);
        return new String(chars);
    }

    public int[] ints() {
        int count = varint();
        if (count == 0) return null;
        need(count - 1);
        int[] v = new int[count - 1];
        for (int i = 0; i < v.length; i++) v[i] = zigzag();
        return v;
    }

    public long[] longs() {
        int count = varint();
        if (count == 0) return null;
        need(count - 1);
        long[] v = new long[count - 1];
        for (int i = 0; i < v.length; i++) v[i] = int64();
        return v;
    }

    public char[] chars() {
        int count = varint();
        if (count == 0) return null;
        need(count - 1);
        char[] v = new char[count - 1];
        for (int i = 0; i < v.length; i++) v[i] = (char) varint();
        return v;
    }

    public String[] strings() {
        int count = varint();
        if (count == 0) return null;
        need(count - 1);
        String[] v = new String[count - 1];
        for (int i = 0; i < v.length; i++) v[i] = string();
        return v;
    }

    // --- encoding ---

    /**
     * Fails unless {@code bytes} more are left. The client sends the lengths, so an array is sized
     * only once its elements, at least a byte each, can be there.
     */
    private void need(long bytes) {
        if (bytes > end - pos)
            throw new IllegalStateException("wire: " + bytes + " bytes wanted, " + (end - pos) + " left");
    }

    private int zigzag() {
        int v = varint();
        return (v >>> 1) ^ -(v & 1);
    }

    private long varlong() {
        long v = 0;
        for (int shift = 0; ; shift += 7) {
            int b = buf[pos++];
            v |= (long) (b & 0x7F) << shift;
            if (b >= 0) return v;
        }
    }

    private int le32() {
        need(4);
        int v = (buf[pos] & 0xFF) | (buf[pos + 1] & 0xFF) << 8 | (buf[pos + 2] & 0xFF) << 16 | (buf[pos + 3] & 0xFF) << 24;
        pos += 4;
        return v;
    }
}
