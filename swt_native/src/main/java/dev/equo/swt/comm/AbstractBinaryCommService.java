package dev.equo.swt.comm;

import dev.equo.swt.Serializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Transport-agnostic core of the binary comm protocol shared by every {@link CommService}
 * implementation. Subclasses provide only the WebSocket transport (server lifecycle, session
 * tracking, per-session send and {@link #getPort()}); everything wire-format related lives here.
 *
 * <p>Wire format: {@code [2-byte name length BE][name UTF-8][payload]}. Frames sent before the
 * first client connects are buffered and flushed on connect (see {@link #onClientConnected}).
 */
public abstract class AbstractBinaryCommService implements CommService {

    protected final Serializer serializer = new Serializer();
    private final Map<String, TypedHandler<?>> typedHandlers = new ConcurrentHashMap<>();
    private final List<byte[]> pendingFrames = new ArrayList<>();
    private volatile boolean firstClientConnected = false;

    /**
     * The client frames are being written for. Starts at 1, not 0: frames sent before anyone
     * connects are buffered and handed to the first client on connect, so they genuinely reach it
     * and are rightly credited to it. Each session after that gets a fresh id, which is what makes
     * a reconnecting client be described in full rather than referred to by name.
     */
    private final java.util.concurrent.atomic.AtomicInteger connection =
            new java.util.concurrent.atomic.AtomicInteger(1);

    @Override
    public int connectionId() {
        return connection.get();
    }

    @Override
    public void send(String eventName) {
        send(eventName, (byte[]) null);
    }

    @Override
    public void send(String eventName, byte[] payload) {
        byte[] frame = encodeFrame(eventName, payload);
        if (heldForFirstClient(frame, 0, frame.length, false)) return;
        broadcast(frame, 0, frame.length);
    }

    @Override
    public void sendFrame(byte[] frame, int offset, int length) {
        if (heldForFirstClient(frame, offset, length, true)) return;
        broadcast(frame, offset, length);
    }

    /** Keeps a frame until the first client connects; a lent one is copied, since it outlives the call. */
    private boolean heldForFirstClient(byte[] frame, int offset, int length, boolean lent) {
        if (firstClientConnected) return false;
        synchronized (pendingFrames) {
            if (firstClientConnected) return false;
            pendingFrames.add(lent ? Arrays.copyOfRange(frame, offset, offset + length) : frame);
            return true;
        }
    }

    private static byte[] encodeFrame(String eventName, byte[] payload) {
        byte[] header = CommService.frameHeader(eventName);
        int payloadLength = payload != null ? payload.length : 0;
        byte[] frame = Arrays.copyOf(header, header.length + payloadLength);
        if (payloadLength > 0) System.arraycopy(payload, 0, frame, header.length, payloadLength);
        return frame;
    }

    @Override
    public <T> void on(String eventName, Class<T> cls, Consumer<T> callback) {
        typedHandlers.put(eventName, new TypedHandler<>(cls, callback));
    }

    @Override
    public void remove(String eventName) {
        typedHandlers.remove(eventName);
    }

    /**
     * Subclasses call this once per established session. The first call drains any frames buffered
     * before a client was connected, delivering them to the just-connected session.
     */
    protected void onClientConnected(Consumer<byte[]> sendToSession) {
        if (firstClientConnected) {
            // A different client from the one everything so far was written for. It holds none of
            // it, so it is given a new id and every widget is described to it in full again.
            connection.incrementAndGet();
            return;
        }
        List<byte[]> drain;
        synchronized (pendingFrames) {
            if (firstClientConnected) return;
            firstClientConnected = true;
            drain = new ArrayList<>(pendingFrames);
            pendingFrames.clear();
        }
        for (byte[] frame : drain) {
            sendToSession.accept(frame);
        }
    }

    /**
     * Subclasses call this with each received binary frame. {@code data[0, offset + length)} is
     * handed over: decoding may overwrite it, so it must not be read again or shared.
     */
    protected void onBinaryMessage(byte[] data, int offset, int length) {
        if (length < NAME_LENGTH_BYTES) return;
        int nameLen = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
        if (length < NAME_LENGTH_BYTES + nameLen) return;
        String eventName = new String(data, offset + NAME_LENGTH_BYTES, nameLen, StandardCharsets.UTF_8);
        int payloadStart = offset + NAME_LENGTH_BYTES + nameLen;
        int payloadLen = length - NAME_LENGTH_BYTES - nameLen;
        dispatch(eventName, data, payloadStart, payloadLen);
    }

    private void dispatch(String eventName, byte[] data, int offset, int length) {
        TypedHandler<?> typed = typedHandlers.get(eventName);
        if (typed != null) {
            typed.handle(data, offset, length, serializer, logTag());
            return;
        }
        System.err.println(logTag() + " No handler for: " + eventName);
        // Show which same-type handlers ARE registered here: distinguishes "wrong widget id"
        // (siblings listed) from "this widget type never registered" (nothing listed) at a glance.
        String typePrefix = eventName.substring(0, eventName.indexOf('/') + 1);
        typedHandlers.keySet().stream().filter(k -> k.startsWith(typePrefix)).sorted()
                .forEach(k -> System.err.println(logTag() + "   registered: " + k));
    }

    /**
     * Sends {@code frame[offset, offset + length)} to every currently-open session. Lent, as in
     * {@link #sendFrame}: a transport that finishes the send after returning must copy it first.
     */
    protected abstract void broadcast(byte[] frame, int offset, int length);

    /** Prefix for diagnostic logging; defaults to the concrete class' simple name. */
    protected String logTag() {
        return "[" + getClass().getSimpleName() + "]";
    }

    private static final class TypedHandler<T> {
        final Class<T> cls;
        final Consumer<T> callback;

        TypedHandler(Class<T> cls, Consumer<T> callback) {
            this.cls = cls;
            this.callback = callback;
        }

        @SuppressWarnings("unchecked")
        void handle(byte[] data, int offset, int length, Serializer serializer, String tag) {
            // ByteBuffer.class is the passthrough that copies nothing: the frame is ours for the
            // length of the dispatch, so a handler that reads it here and keeps nothing takes a view.
            if (cls == java.nio.ByteBuffer.class) {
                callback.accept((T) java.nio.ByteBuffer.wrap(data, offset, length));
                return;
            }
            // byte[].class is a passthrough: deliver the raw frame bytes (a copy, since the handler
            // may outlive this dispatch) with no deserialization — used for already-binary payloads.
            if (cls == byte[].class) {
                callback.accept((T) (length > 0 ? Arrays.copyOfRange(data, offset, offset + length) : null));
                return;
            }
            if (length <= 0 || cls == null || cls == Void.class || cls == void.class) {
                callback.accept(null);
                return;
            }
            try {
                // dsl-json's byte reader only starts at index 0. The frame is ours and its name has
                // been read, so blanking everything before the body lets the parser skip it as
                // whitespace instead of the body being copied out.
                Arrays.fill(data, 0, offset, (byte) ' ');
                T value = serializer.from(cls, data, 0, offset + length);
                callback.accept(value);
            } catch (IOException e) {
                System.err.println(tag + " Deserialization failed: " + e.getMessage());
                callback.accept(null);
            }
        }
    }
}
