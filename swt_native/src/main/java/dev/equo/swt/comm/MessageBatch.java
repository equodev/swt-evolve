package dev.equo.swt.comm;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A run of frames sent as one. Payloads are carried verbatim, so a batched message is
 * byte-identical to the one it replaces and the far side dispatches it on its own channel.
 *
 * <p>The batch is written straight into its wire frame, {@code [header]["channel",payload],…]}, so
 * sending it copies nothing, and {@link #clear()} keeps the buffer for the next run.
 */
public final class MessageBatch {

    /** Channel a batched run travels on. */
    public static final String EVENT = "swt.evolve.batch";

    private static final byte[] HEADER = CommService.frameHeader(EVENT);
    private static final byte[] NULL = "null".getBytes(StandardCharsets.UTF_8);

    private byte[] buffer = new byte[4096];
    private int size;
    private int count;
    private String firstEvent;
    private int firstPayloadStart;
    private int firstPayloadLength;

    public MessageBatch() {
        clear();
    }

    public void add(String event, byte[] payload) {
        add(event, payload, 0, payload != null ? payload.length : 0);
    }

    /** Copies {@code payload[offset, offset + length)} in, so the caller may reuse its buffer after. */
    public void add(String event, byte[] payload, int offset, int length) {
        byte[] name = event.getBytes(StandardCharsets.UTF_8);
        // Separator, brackets, quotes, comma and the closing ']' of the whole batch, plus every name
        // byte escaped and a "null" in place of an empty payload.
        ensure(size + 2 * name.length + Math.max(length, NULL.length) + 8);
        if (count > 0) buffer[size++] = ',';
        buffer[size++] = '[';
        buffer[size++] = '"';
        for (byte b : name) {
            if (b == '"' || b == '\\') buffer[size++] = '\\';
            buffer[size++] = b;
        }
        buffer[size++] = '"';
        buffer[size++] = ',';
        if (count == 0) {
            firstEvent = event;
            firstPayloadStart = size;
            firstPayloadLength = length;
        }
        if (length == 0) {
            System.arraycopy(NULL, 0, buffer, size, NULL.length);
            size += NULL.length;
        } else {
            System.arraycopy(payload, offset, buffer, size, length);
            size += length;
        }
        buffer[size++] = ']';
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
        buffer[HEADER.length] = '[';
        size = HEADER.length + 1;
        count = 0;
        firstEvent = null;
    }

    /**
     * Puts the batch on {@code comm} as one message, or a lone frame unwrapped. Either way the
     * buffer is lent for the call, and the batch has to be cleared before it is added to again.
     */
    void sendTo(CommService comm) {
        if (count == 0) return;
        if (count == 1) {
            // What precedes the payload - the batch header and ["channel", - is always longer than
            // the frame's own header, so the lone frame is framed in place over it.
            byte[] header = CommService.frameHeader(firstEvent);
            int start = firstPayloadStart - header.length;
            System.arraycopy(header, 0, buffer, start, header.length);
            comm.sendFrame(buffer, start, header.length + firstPayloadLength);
            return;
        }
        buffer[size] = ']';
        comm.sendFrame(buffer, 0, size + 1);
    }

    private void ensure(int needed) {
        if (needed > buffer.length) buffer = Arrays.copyOf(buffer, Math.max(needed, buffer.length * 2));
    }
}
