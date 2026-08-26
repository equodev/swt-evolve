package dev.equo.swt.comm;

import java.util.function.Consumer;

public interface CommService {
    void send(String eventName);

    void send(String eventName, byte[] payload);

    /** Sends a run of frames as one message; a batch of one goes unwrapped. */
    default void send(MessageBatch batch) {
        if (batch.isEmpty()) return;
        if (batch.size() == 1) {
            send(batch.event(0), batch.payload(0));
            return;
        }
        send(MessageBatch.EVENT, batch.encode());
    }

    /**
     * Registers a handler for {@code eventName}. The frame payload is deserialized to {@code cls}
     * before the callback is invoked. Pass {@code byte[].class} to receive the raw frame bytes with
     * no deserialization (used for already-binary payloads such as rendered image data).
     */
    <T> void on(String eventName, Class<T> cls, Consumer<T> callback);

    void remove(String eventName);

    int getPort();

    void stop();
}
