package dev.equo.swt.comm;

import java.util.function.Consumer;

/** What carries messages to and from the client. Every frame is {@code [channel, as ChannelHeader writes it][payload]}. */
public interface CommService {
    /**
     * Sends {@code payload} on the channel numbered {@code channel} of the widget of type {@code type}
     * numbered {@code id}, or on the global channel numbered {@code channel} when {@code type} is 0
     * (see {@link ChannelHeader}); null for none.
     */
    void send(int type, long id, int channel, byte[] payload);

    /**
     * Sends {@code frame[offset, offset + length)}, already in wire format. The bytes are lent: they
     * are read before this returns and may be overwritten as soon as it has.
     */
    void sendFrame(byte[] frame, int offset, int length);

    /** Sends a run of frames as one message; a batch of one goes unwrapped. */
    default void send(MessageBatch batch) {
        batch.sendTo(this);
    }

    /**
     * Registers a handler for the channel numbered {@code channel} of the widget of type {@code type}
     * numbered {@code id}, or for the global channel numbered {@code channel} when {@code type} is 0.
     * The frame payload is deserialized to {@code cls} before the callback is invoked. Pass
     * {@code byte[].class} to receive the raw frame bytes with no deserialization (used for
     * already-binary payloads such as rendered image data).
     */
    <T> void on(int type, long id, int channel, Class<T> cls, Consumer<T> callback);

    /**
     * {@link #on} for a handler that reads the payload where it lies, with no wrapper or copy made
     * for it.
     */
    default void onBytes(int type, long id, int channel, FrameBytes handler) {
        on(type, id, channel, java.nio.ByteBuffer.class,
                b -> handler.accept(b.array(), b.arrayOffset() + b.position(), b.remaining()));
    }

    /** Removes the handler {@link #on} registered for these numbers. */
    void remove(int type, long id, int channel);

    /**
     * Removes every handler registered for the widget of type {@code type} numbered {@code id}: what a
     * disposed widget leaves behind, which otherwise keeps it reachable from this comm for good.
     */
    default void removeOwner(int type, long id) {
    }

    /**
     * One handler for the channels numbered {@code channels} of the widget of type {@code type}
     * numbered {@code id}, told which channel a message came on, by name. A channel with a handler of
     * its own (see {@link #on}) goes to that handler instead.
     */
    default <T> void onOwner(int type, long id, int[] channels, Class<T> cls,
            java.util.function.BiConsumer<String, T> callback) {
        for (int channel : channels) {
            String name = ChannelHeader.name(channel);
            on(type, id, channel, cls, value -> callback.accept(name, value));
        }
    }

    /** What {@link #onType} hands a message on a channel of a widget of its type. */
    interface TypeCallback<T> {
        /**
         * What the message on the channel numbered {@code channel} (named {@code rest}) of the widget
         * of type {@code type} numbered {@code id} is for, or null when it is not this callback's:
         * read before its payload is decoded, and handed to {@link #accept} with it.
         */
        Object target(String type, long id, int channel, String rest);

        void accept(Object target, String rest, T value);
    }

    /**
     * One handler for every channel of every widget of {@code type}, finding the widget by the id
     * its channel names, so a widget registers nothing of its own and leaves nothing behind. A
     * channel with a handler of its own (see {@link #on}, {@link #onOwner}) goes to that handler
     * instead. Registering a type a second time keeps the first callback. False where this comm does
     * not dispatch by type; its caller registers each widget's handlers then.
     */
    default <T> boolean onType(int type, Class<T> cls, TypeCallback<T> callback) {
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
