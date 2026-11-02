package dev.equo.swt.comm;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A run of frames sent as one. Payloads are carried verbatim, so a batched message is
 * byte-identical to the one it replaces and the far side dispatches it on its own channel.
 *
 * <p>The batch is written straight into its wire frame: the batch's header, then per entry the
 * entry's channel as a header names it ({@link ChannelHeader}), its payload's length and its payload.
 * Sending it copies nothing, and {@link #clear()} keeps the buffer for the next run.
 */
public final class MessageBatch {

    /** Channel a batched run travels on. */
    public static final String EVENT = "swt.evolve.batch";

    private static final byte[] HEADER = ChannelHeader.encode(0, 0, dev.equo.swt.WireTable.CHANNEL_SWT_EVOLVE_BATCH);

    private byte[] buffer = new byte[4096];
    private int size;
    private int count;
    private int firstHeaderStart;
    private int firstHeaderLength;
    private int firstPayloadStart;
    private int firstPayloadLength;

    public MessageBatch() {
        clear();
    }

    /**
     * Copies {@code payload[offset, offset + length)} in on the channel numbered {@code channel} of
     * the widget of type {@code type} numbered {@code id} (a global channel when {@code type} is 0),
     * so the caller may reuse its buffer after.
     */
    public void add(int type, long id, int channel, byte[] payload, int offset, int length) {
        ensure(size + ChannelHeader.MAX_NUMBERED + 5 + length);
        if (count == 0) firstHeaderStart = size;
        int end = ChannelHeader.write(buffer, size, type, id, channel);
        if (count == 0) firstHeaderLength = end - size;
        size = end;
        entry(payload, offset, length);
    }

    /** Copies in the frame {@code frame[0, length)}, its header ending at {@code payloadStart}. */
    public void addFrame(byte[] frame, int payloadStart, int length) {
        ensure(size + length + 5);
        if (count == 0) {
            firstHeaderStart = size;
            firstHeaderLength = payloadStart;
        }
        System.arraycopy(frame, 0, buffer, size, payloadStart);
        size += payloadStart;
        entry(frame, payloadStart, length - payloadStart);
    }

    /** An entry's length and payload, after its header. */
    private void entry(byte[] payload, int offset, int length) {
        int v = length;
        while ((v & ~0x7F) != 0) {
            buffer[size++] = (byte) ((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        buffer[size++] = (byte) v;
        if (count == 0) {
            firstPayloadStart = size;
            firstPayloadLength = length;
        }
        if (length > 0) {
            System.arraycopy(payload, offset, buffer, size, length);
            size += length;
        }
        count++;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public int size() {
        return count;
    }

    /** What this batch holds on the wire, so a caller can bound how much it buffers. */
    public int byteSize() {
        return size;
    }

    /** How much the batch can hold before it grows, so a caller can decide whether to keep it. */
    public int capacity() {
        return buffer.length;
    }

    /** Empties the batch, keeping its buffer. */
    public void clear() {
        System.arraycopy(HEADER, 0, buffer, 0, HEADER.length);
        size = HEADER.length;
        count = 0;
    }

    /**
     * Puts the batch on {@code comm} as one message, or a lone frame unwrapped. Either way the
     * buffer is lent for the call, and the batch has to be cleared before it is added to again.
     */
    void sendTo(CommService comm) {
        if (count == 0) return;
        if (count == 1) {
            // What precedes the payload - the batch header, the entry's header and its length - is
            // always longer than the frame's own header, so the lone frame is framed in place over it.
            int start = firstPayloadStart - firstHeaderLength;
            System.arraycopy(buffer, firstHeaderStart, buffer, start, firstHeaderLength);
            comm.sendFrame(buffer, start, firstHeaderLength + firstPayloadLength);
            return;
        }
        comm.sendFrame(buffer, 0, size);
    }

    private void ensure(int needed) {
        if (needed > buffer.length) buffer = Arrays.copyOf(buffer, Math.max(needed, buffer.length * 2));
    }
}
