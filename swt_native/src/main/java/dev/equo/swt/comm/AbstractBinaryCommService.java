package dev.equo.swt.comm;

import dev.equo.swt.Serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    protected static final int HEADER_LEN = 2;

    protected final Serializer serializer = new Serializer();
    private final Map<String, TypedHandler<?>> typedHandlers = new ConcurrentHashMap<>();
    private final List<byte[]> pendingFrames = new ArrayList<>();
    private volatile boolean firstClientConnected = false;

    @Override
    public void send(String eventName) {
        send(eventName, (byte[]) null);
    }

    @Override
    public void send(String eventName, byte[] payload) {
        byte[] frame = encodeFrame(eventName, payload);
        if (!firstClientConnected) {
            synchronized (pendingFrames) {
                if (!firstClientConnected) {
                    pendingFrames.add(frame);
                    return;
                }
            }
        }
        broadcast(frame);
    }

    private static byte[] encodeFrame(String eventName, byte[] payload) {
        byte[] nameBytes = eventName.getBytes(StandardCharsets.UTF_8);
        int totalLen = HEADER_LEN + nameBytes.length + (payload != null ? payload.length : 0);
        ByteBuffer buf = ByteBuffer.allocate(totalLen);
        buf.putShort((short) nameBytes.length);
        buf.put(nameBytes);
        if (payload != null) {
            buf.put(payload);
        }
        return buf.array();
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
        if (firstClientConnected) return;
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

    /** Subclasses call this with each received binary frame (which may be a slice of a buffer). */
    protected void onBinaryMessage(byte[] data, int offset, int length) {
        if (length < HEADER_LEN) return;
        int nameLen = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
        if (length < HEADER_LEN + nameLen) return;
        String eventName = new String(data, offset + HEADER_LEN, nameLen, StandardCharsets.UTF_8);
        int payloadStart = offset + HEADER_LEN + nameLen;
        int payloadLen = length - HEADER_LEN - nameLen;
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

    /** Send an already-encoded frame to every currently-open session. */
    protected abstract void broadcast(byte[] frame);

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
                T value = serializer.from(cls, data, offset, length);
                callback.accept(value);
            } catch (IOException e) {
                System.err.println(tag + " Deserialization failed: " + e.getMessage());
                callback.accept(null);
            }
        }
    }
}
