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
