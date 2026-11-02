package dev.equo.swt.harness;

import dev.equo.swt.GcWire;
import dev.equo.swt.comm.MessageBatch;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Outbound frames as values a test reads (see {@link WireValues}): a batch, or a GC's binary frame,
 * as its entries in order, a lone GC entry unwrapped as a batch of one is. Lets a test read GC
 * traffic the way it reads every other frame.
 */
public final class GcFrames {

    private GcFrames() {
    }

    /** One entry of a batch: the channel it is on and what it carries. */
    public record Entry(String channel, Object value) {
    }

    /** A frame as a test reads it: the channel it is recorded under and its payload decoded. */
    public record Decoded(String event, Object value) {
    }

    /** {@code payload} sent on {@code event}, decoded. */
    public static Decoded decode(String event, byte[] payload) {
        if (MessageBatch.EVENT.equals(event)) {
            return new Decoded(event, batch(payload));
        }
        if ("swt.evolve.properties".equals(event) || (event.startsWith("GC/") && event.endsWith("/imageInit"))) {
            return new Decoded(event, WireValues.plain(dev.equo.swt.WireDump.fields(payload, 0, payload.length)));
        }
        if (dev.equo.swt.WidgetWire.CHANNEL.equals(event)) {
            return new Decoded(event, WireFrames.records(payload));
        }
        if (!event.startsWith("GC/") || !event.endsWith("/b")) {
            return new Decoded(event, value(payload));
        }
        String channel = event.substring(0, event.length() - 2);
        List<Entry> entries = new ArrayList<>();
        for (GcWire.Entry e : GcWire.entries(channel, payload, 0, payload.length)) {
            entries.add(new Entry(e.channel, WireValues.plain(e.value)));
        }
        if (entries.size() == 1) return new Decoded(entries.get(0).channel(), entries.get(0).value());
        return new Decoded(MessageBatch.EVENT, entries);
    }

    /** The arguments of every {@code op} {@code frames} carry, in order, a batch's entries included. */
    public static List<List<?>> argsOf(List<RecordingComm.Frame> frames, String op) {
        List<List<?>> out = new ArrayList<>();
        for (Entry e : BatchEntries.of(frames)) {
            if (e.channel().endsWith("/" + op) && e.value() instanceof List<?> args) out.add(args);
        }
        return out;
    }

    /** The channel of every entry {@code frames} carry, in order, a batch's entries included. */
    public static List<String> channels(List<RecordingComm.Frame> frames) {
        List<String> out = new ArrayList<>();
        for (Entry e : BatchEntries.of(frames)) out.add(e.channel());
        return out;
    }

    /** The ops {@code frames} name, once per naming: a frame refers to an op it named by number. */
    public static List<String> opsNamed(List<RecordingComm.Frame> frames) {
        List<String> out = new ArrayList<>();
        for (RecordingComm.Frame frame : frames) out.addAll(frame.opsNamed);
        return out;
    }

    /** The ops a frame sent on {@code event} names, a batch's entries included. */
    static List<String> opsNamedIn(String event, byte[] payload) {
        List<String> out = new ArrayList<>();
        if (!MessageBatch.EVENT.equals(event)) {
            named(event, payload, 0, payload.length, out);
            return out;
        }
        int pos = 0;
        while (pos < payload.length) {
            dev.equo.swt.comm.ChannelHeader.Decoded entry = dev.equo.swt.comm.ChannelHeader.decode(payload, pos, payload.length);
            int at = entry.payloadStart();
            int length = 0;
            for (int shift = 0; ; shift += 7) {
                int b = payload[at++] & 0xFF;
                length |= (b & 0x7F) << shift;
                if ((b & 0x80) == 0) break;
            }
            named(entry.channel(), payload, at, length, out);
            pos = at + length;
        }
        return out;
    }

    private static void named(String event, byte[] bytes, int offset, int length, List<String> out) {
        if (!event.startsWith("GC/") || !event.endsWith("/b")) return;
        String channel = event.substring(0, event.length() - 2);
        for (GcWire.Entry e : GcWire.entries(channel, bytes, offset, length)) {
            if (e.named) out.add(e.channel.substring(e.channel.lastIndexOf('/') + 1));
        }
    }

    /** A binary batch as its entries, each decoded as a frame of its own: a GC frame as its ops, in place. */
    public static List<Entry> batch(byte[] batch) {
        List<Entry> out = new ArrayList<>();
        int pos = 0;
        while (pos < batch.length) {
            dev.equo.swt.comm.ChannelHeader.Decoded entry = dev.equo.swt.comm.ChannelHeader.decode(batch, pos, batch.length);
            int at = entry.payloadStart();
            int length = 0;
            for (int shift = 0; ; shift += 7) {
                int b = batch[at++] & 0xFF;
                length |= (b & 0x7F) << shift;
                if ((b & 0x80) == 0) break;
            }
            Decoded decoded = decode(entry.channel(), java.util.Arrays.copyOfRange(batch, at, at + length));
            if (MessageBatch.EVENT.equals(decoded.event()) && decoded.value() instanceof List<?> ops) {
                for (Object op : ops) out.add((Entry) op);
            } else {
                out.add(new Entry(decoded.event(), decoded.value()));
            }
            pos = at + length;
        }
        return out;
    }

    /** A payload decoded: tagged values as the values they hold, anything else as its UTF-8 text; null for none. */
    public static Object value(byte[] payload) {
        if (payload == null || payload.length == 0) return null;
        if (dev.equo.swt.comm.TaggedValues.isTagged(payload, 0, payload.length)) {
            return WireValues.plain(dev.equo.swt.comm.TaggedValues.decode(payload, 0, payload.length));
        }
        return new String(payload, StandardCharsets.UTF_8);
    }
}
