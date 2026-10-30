package dev.equo.swt.comm;

import java.util.Arrays;
import java.util.function.Consumer;

public interface CommService {
    /** Every frame is {@code [channel, as ChannelHeader writes it][payload]}. */

    void send(String eventName);

    void send(String eventName, byte[] payload);

    /**
     * Sends {@code frame[offset, offset + length)}, already in wire format. The bytes are lent: they
     * are read before this returns and may be overwritten as soon as it has.
     */
    default void sendFrame(byte[] frame, int offset, int length) {
        ChannelHeader.Decoded header = ChannelHeader.decode(frame, offset, offset + length);
        if (header == null) return;
        send(header.channel(), Arrays.copyOfRange(frame, header.payloadStart(), offset + length));
    }

    /** Sends a run of frames as one message; a batch of one goes unwrapped. */
    default void send(MessageBatch batch) {
        batch.sendTo(this);
    }

    /** The bytes a frame for {@code eventName} opens with, ahead of its payload. */
    static byte[] frameHeader(String eventName) {
        return ChannelHeader.encode(eventName);
    }

    /**
     * Registers a handler for {@code eventName}. The frame payload is deserialized to {@code cls}
     * before the callback is invoked. Pass {@code byte[].class} to receive the raw frame bytes with
     * no deserialization (used for already-binary payloads such as rendered image data).
     */
    <T> void on(String eventName, Class<T> cls, Consumer<T> callback);

    void remove(String eventName);

    /**
     * Removes every handler registered under {@code owner} ({@code Type/id}): what a disposed widget
     * leaves behind, which otherwise keeps it reachable from this comm for good.
     */
    default void removeOwner(String owner) {
    }

    /** {@link #removeOwner(String)} for the owner {@code type + "/" + id}. */
    default void removeOwner(String type, long id) {
        removeOwner(type + "/" + id);
    }

    /**
     * One handler for every channel {@code owner + "/" + rest}, {@code rest} one of {@code rests},
     * told which rest a message came on. A channel with a handler of its own (see {@link #on}) goes
     * to that handler instead.
     */
    default <T> void onOwner(String owner, String[] rests, Class<T> cls,
            java.util.function.BiConsumer<String, T> callback) {
        for (String rest : rests) on(owner + "/" + rest, cls, value -> callback.accept(rest, value));
    }

    /** What {@link #onType} hands a message on {@code type + "/" + id + "/" + rest}. */
    interface TypeCallback<T> {
        /**
         * What the message is for, or null when it is not this callback's: read before its payload
         * is decoded, and handed to {@link #accept} with it.
         */
        Object target(String type, long id, String rest);

        void accept(Object target, String rest, T value);
    }

    /**
     * One handler for every channel of every widget of {@code type}, finding the widget by the id
     * its channel names, so a widget registers nothing of its own and leaves nothing behind. A
     * channel with a handler of its own (see {@link #on}, {@link #onOwner}) goes to that handler
     * instead. Registering a type a second time keeps the first callback. False where this comm does
     * not dispatch by type; its caller registers each widget's handlers then.
     */
    default <T> boolean onType(String type, Class<T> cls, TypeCallback<T> callback) {
        return false;
    }

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
