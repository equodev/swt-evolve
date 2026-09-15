package dev.equo.swt.comm;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

public interface CommService {
    /** Every frame is {@code [name length, 2 bytes big-endian][name UTF-8][payload]}. */
    int NAME_LENGTH_BYTES = 2;

    void send(String eventName);

    void send(String eventName, byte[] payload);

    /**
     * Sends {@code frame[offset, offset + length)}, already in wire format. The bytes are lent: they
     * are read before this returns and may be overwritten as soon as it has.
     */
    default void sendFrame(byte[] frame, int offset, int length) {
        int nameLength = ((frame[offset] & 0xFF) << 8) | (frame[offset + 1] & 0xFF);
        int payloadStart = offset + NAME_LENGTH_BYTES + nameLength;
        send(new String(frame, offset + NAME_LENGTH_BYTES, nameLength, StandardCharsets.UTF_8),
                Arrays.copyOfRange(frame, payloadStart, offset + length));
    }

    /** Sends a run of frames as one message; a batch of one goes unwrapped. */
    default void send(MessageBatch batch) {
        batch.sendTo(this);
    }

    /** The bytes a frame for {@code eventName} opens with, ahead of its payload. */
    static byte[] frameHeader(String eventName) {
        byte[] name = eventName.getBytes(StandardCharsets.UTF_8);
        byte[] header = new byte[NAME_LENGTH_BYTES + name.length];
        header[0] = (byte) (name.length >>> 8);
        header[1] = (byte) name.length;
        System.arraycopy(name, 0, header, NAME_LENGTH_BYTES, name.length);
        return header;
    }

    /**
     * Registers a handler for {@code eventName}. The frame payload is deserialized to {@code cls}
     * before the callback is invoked. Pass {@code byte[].class} to receive the raw frame bytes with
     * no deserialization (used for already-binary payloads such as rendered image data).
     */
    <T> void on(String eventName, Class<T> cls, Consumer<T> callback);

    void remove(String eventName);

    /**
     * Identifies the client that will receive whatever is sent now.
     *
     * <p>Delivery is recorded against this rather than against the widget, because "already sent"
     * is a fact about one client: a frame written before anyone connected, or written for another
     * engine, tells this client nothing. A frame buffered while nobody is connected still counts
     * for the client that drains it, so the id is stable across that wait and changes only when a
     * different client takes over - which is what makes a reconnecting client be told everything
     * again instead of being handed names it cannot resolve.
     *
     * <p>Transports that never serve more than one client can leave this at its default.
     */
    default int connectionId() {
        return 1;
    }

    int getPort();

    void stop();
}
