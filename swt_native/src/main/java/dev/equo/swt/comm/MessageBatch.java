package dev.equo.swt.comm;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A run of frames sent as one. Payloads are carried verbatim, so a batched message is
 * byte-identical to the one it replaces and the far side dispatches it on its own channel.
 */
public final class MessageBatch {

    /** Channel a batched run travels on. */
    public static final String EVENT = "swt.evolve.batch";

    private final List<String> events = new ArrayList<>();
    private final List<byte[]> payloads = new ArrayList<>();
    private int bytes;

    public void add(String event, byte[] payload) {
        events.add(event);
        payloads.add(payload);
        bytes += event.length() + (payload != null ? payload.length : 0);
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public int size() {
        return events.size();
    }

    /** Roughly what this batch holds, so a caller can bound how much it buffers. */
    public int byteSize() {
        return bytes;
    }

    public String event(int i) {
        return events.get(i);
    }

    public byte[] payload(int i) {
        return payloads.get(i);
    }

    /** {@code [["channel",payload],…]} — payloads spliced in as raw JSON, never re-encoded. */
    public byte[] encode() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(bytes + 8 * events.size() + 2);
        out.write('[');
        for (int i = 0; i < events.size(); i++) {
            if (i > 0) out.write(',');
            out.write('[');
            writeJsonString(out, events.get(i));
            out.write(',');
            byte[] payload = payloads.get(i);
            if (payload == null || payload.length == 0) {
                out.writeBytes("null".getBytes(StandardCharsets.UTF_8));
            } else {
                out.writeBytes(payload);
            }
            out.write(']');
        }
        out.write(']');
        return out.toByteArray();
    }

    private static void writeJsonString(ByteArrayOutputStream out, String s) {
        out.write('"');
        out.writeBytes(s.replace("\\", "\\\\").replace("\"", "\\\"").getBytes(StandardCharsets.UTF_8));
        out.write('"');
    }
}
