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

    private final dev.equo.swt.MoveCompression moves = new dev.equo.swt.MoveCompression();

    @Override
    public dev.equo.swt.MoveCompression moves() {
        return moves;
    }

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
    /** The messages sent, each counted once however many frames {@link #sent} records for it. */
    public final java.util.concurrent.atomic.AtomicInteger messages = new java.util.concurrent.atomic.AtomicInteger();

    @Override
    public void send(int type, long id, int channel, byte[] payload) {
        messages.incrementAndGet();
        send(Channels.name(type, id, channel), payload);
    }

    /** The socket messages sent, however many frames each carries. */
    public int messages() {
        return messages.get();
    }

    @Override
    public void sendFrame(byte[] frame, int offset, int length) {
        messages.incrementAndGet();
        dev.equo.swt.comm.ChannelHeader.Decoded header = dev.equo.swt.comm.ChannelHeader.decode(frame, offset, offset + length);
        send(header.channel(), java.util.Arrays.copyOfRange(frame, header.payloadStart(), offset + length));
    }

    /** Records a message with no payload on {@code eventName}. */
    public void send(String eventName) {
        sent.add(new Frame(eventName, null, 0, null, 0, List.of()));
    }

    /**
     * Records a message on {@code eventName}, by the frames it stands for: every send and frame comes
     * through here by its channel's name, for a test that watches the traffic to override.
     */
    public void send(String eventName, byte[] payload) {
        if (payload == null) {
            send(eventName);
            return;
        }
        // A binary frame of widget records: each record recorded as the frame it replaced.
        if (dev.equo.swt.WidgetWire.CHANNEL.equals(eventName)) {
            int wire = ++wireFramesSent;
            for (WireFrames.Record r : WireFrames.records(payload)) sent.add(new Frame(r.channel(), r.state(), wire, null, r.size(), List.of()));
            return;
        }
        // A flush's records travel in its batch, after the frames of what it painted: recorded as the
        // frame of records it would have been on its own, between the runs of the batch's other entries.
        if (dev.equo.swt.comm.MessageBatch.EVENT.equals(eventName) && carriesRecords(payload)) {
            int pos = 0;
            int runStart = 0;
            while (pos < payload.length) {
                int[] entry = entryAt(payload, pos);
                if (entry == null) break;
                if (entry[0] == 1) {
                    if (pos > runStart) send(eventName, java.util.Arrays.copyOfRange(payload, runStart, pos));
                    send(dev.equo.swt.WidgetWire.CHANNEL, java.util.Arrays.copyOfRange(payload, entry[1], entry[2]));
                    runStart = entry[2];
                }
                pos = entry[2];
            }
            if (payload.length > runStart) send(eventName, java.util.Arrays.copyOfRange(payload, runStart, payload.length));
            return;
        }
        GcFrames.Decoded frame = GcFrames.decode(eventName, payload);
        sent.add(new Frame(frame.event(), frame.value(), 0, payload, payload.length, GcFrames.opsNamedIn(eventName, payload)));
    }

    /** Whether the batch {@code payload} has a frame of widget records among its entries. */
    private static boolean carriesRecords(byte[] payload) {
        for (int pos = 0; pos < payload.length; ) {
            int[] entry = entryAt(payload, pos);
            if (entry == null) return false;
            if (entry[0] == 1) return true;
            pos = entry[2];
        }
        return false;
    }

    /** The batch entry at {@code pos}: 1 when it is a frame of widget records, else 0; its payload's start and end. */
    private static int[] entryAt(byte[] batch, int pos) {
        dev.equo.swt.comm.ChannelHeader.Decoded entry = dev.equo.swt.comm.ChannelHeader.decode(batch, pos, batch.length);
        if (entry == null) return null;
        int at = entry.payloadStart();
        int length = 0;
        for (int shift = 0; ; shift += 7) {
            int b = batch[at++] & 0xFF;
            length |= (b & 0x7F) << shift;
            if (b < 0x80) break;
        }
        return new int[] {dev.equo.swt.WidgetWire.CHANNEL.equals(entry.channel()) ? 1 : 0, at, at + length};
    }

    /** How many binary frames of widget records carried what is in {@link #sent}. */
    public long wireFrames() {
        return sent.stream().filter(f -> f.wire != 0).mapToInt(f -> f.wire).distinct().count();
    }

    @Override
    public <T> void on(int type, long id, int channel, Class<T> cls, Consumer<T> callback) {
        handlers.put(Channels.name(type, id, channel), callback);
    }

    @Override
    public void remove(int type, long id, int channel) {
        handlers.remove(Channels.name(type, id, channel));
    }

    @Override
    public void removeOwner(int type, long id) {
        String owner = dev.equo.swt.comm.ChannelHeader.typeName(type) + "/" + id;
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
