package dev.equo.swt.harness;

import dev.equo.swt.comm.CommService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A {@link CommService} test double that captures traffic in both directions without any real
 * transport: outbound frames are recorded in {@link #sent}; inbound handlers registered via
 * {@link #on} are recorded in {@link #handlers} and can be invoked with {@link #fireContaining} to
 * simulate a Dart&rarr;Java event. Used by {@code RadioGroupDeliveryTest} (which inspects outbound
 * frames) and {@code DisplayWakeFlutterTest} (which fires inbound handlers).
 */
public class RecordingComm implements CommService {

    /**
     * A recorded outbound frame: its channel ({@link #event}) and its payload decoded ({@link #value},
     * see {@link GcFrames#decode}); a widget record's value is its state.
     */
    public static final class Frame {
        public final String event;
        public final Object value;
        /** Which binary frame of widget records carried this record, from 1; 0 for any other frame. */
        public final int wire;
        /** The payload as sent, for a frame sent as bytes; null otherwise. */
        public final byte[] bytes;
        /** Bytes this frame took on the wire: a widget record's own, any other frame's payload. */
        public final int size;
        /** The GC ops this frame names, in order; see {@link GcFrames#opsNamed}. */
        public final List<String> opsNamed;

        Frame(String event, Object value, int wire, byte[] bytes, int size, List<String> opsNamed) {
            this.event = event;
            this.value = value;
            this.wire = wire;
            this.bytes = bytes;
            this.size = size;
            this.opsNamed = opsNamed;
        }

        /** The value as a record of properties; fails when it is not one. */
        @SuppressWarnings("unchecked")
        public java.util.Map<String, Object> state() {
            if (!(value instanceof java.util.Map)) throw new AssertionError(event + " carries no record: " + value);
            return (java.util.Map<String, Object>) value;
        }

        @Override
        public String toString() {
            return event + " " + value;
        }
    }

    public final List<Frame> sent = new CopyOnWriteArrayList<>();
    public final Map<String, Consumer<?>> handlers = new ConcurrentHashMap<>();
    private int wireFramesSent;

    @Override
    public void send(String eventName) {
        sent.add(new Frame(eventName, null, 0, null, 0, List.of()));
    }

    @Override
    public void send(String eventName, byte[] payload) {
        // A binary frame of widget records: each record recorded as the frame it replaced.
        if (dev.equo.swt.WidgetWire.CHANNEL.equals(eventName)) {
            int wire = ++wireFramesSent;
            for (WireFrames.Record r : WireFrames.records(payload)) sent.add(new Frame(r.channel(), r.state(), wire, null, r.size(), List.of()));
            return;
        }
        GcFrames.Decoded frame = GcFrames.decode(eventName, payload);
        sent.add(new Frame(frame.event(), frame.value(), 0, payload, payload.length, GcFrames.opsNamedIn(eventName, payload)));
    }

    /** How many binary frames of widget records carried what is in {@link #sent}. */
    public long wireFrames() {
        return sent.stream().filter(f -> f.wire != 0).mapToInt(f -> f.wire).distinct().count();
    }

    @Override
    public <T> void on(String eventName, Class<T> cls, Consumer<T> callback) {
        handlers.put(eventName, callback);
    }

    @Override
    public void remove(String eventName) {
        handlers.remove(eventName);
    }

    @Override
    public void removeOwner(String owner) {
        handlers.keySet().removeIf(name -> name.equals(owner) || name.startsWith(owner + "/"));
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public void stop() {
    }

    /** Fire the single recorded inbound handler whose channel name contains {@code needle}. */
    @SuppressWarnings("unchecked")
    public <T> void fireContaining(String needle, T value) {
        String key = handlers.keySet().stream()
                .filter(k -> k.contains(needle))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "no handler containing '" + needle + "'; registered: " + handlers.keySet()));
        ((Consumer<T>) handlers.get(key)).accept(value);
    }
}
