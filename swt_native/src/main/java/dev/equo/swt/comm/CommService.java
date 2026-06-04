package dev.equo.swt.comm;

import java.util.function.Consumer;

public interface CommService {
    void send(String eventName);

    void send(String eventName, byte[] payload);

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
