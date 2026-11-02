package dev.equo.swt.harness;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The widgets a run of outbound frames describes, and those it names ({@code _r}) before any of them
 * described it, read in delivery order as the client applies them. A name the client cannot resolve
 * costs it one refresh request.
 *
 * <p>The client reads a binary frame of widget records twice, entering every widget the frame
 * describes before it reads any record's properties, so a name resolves to a record anywhere in the
 * same frame, before or after it.
 */
public final class FrameRefs {

    /** Ids described in full (a {@code _s} record that is not a diff against a {@code _b} base). */
    public final Set<Long> described = new HashSet<>();
    /** Ids named before anything described them, in the order they were first named. */
    public final Set<Long> unresolved = new LinkedHashSet<>();

    public static FrameRefs of(List<RecordingComm.Frame> frames) {
        FrameRefs refs = new FrameRefs();
        for (int i = 0; i < frames.size(); ) {
            // The records one binary frame carried, applied together.
            List<Object> frame = new ArrayList<>();
            int wire = frames.get(i).wire;
            do {
                frame.add(WireValues.plain(content(frames.get(i).value)));
                i++;
            } while (wire != 0 && i < frames.size() && frames.get(i).wire == wire);
            refs.apply(frame);
        }
        return refs;
    }

    /** One frame as the client applies it: what it describes first, then what it names. */
    private void apply(Object frame) {
        collectDescribed(frame);
        collectNamed(frame);
    }

    /** What a decoded frame's parts hold: a widget record its state, a batch entry its value. */
    private static Object content(Object node) {
        if (node instanceof WireFrames.Record record) return record.state();
        if (node instanceof GcFrames.Entry entry) return entry.value();
        if (node instanceof List<?> list) {
            List<Object> out = new ArrayList<>();
            for (Object e : list) out.add(content(e));
            return out;
        }
        return node;
    }

    private static Long id(Map<?, ?> object) {
        return object.get("id") instanceof Number ? ((Number) object.get("id")).longValue() : null;
    }

    private void collectDescribed(Object node) {
        if (node instanceof List) {
            for (Object e : (List<?>) node) collectDescribed(e);
        } else if (node instanceof Map) {
            Map<?, ?> object = (Map<?, ?>) node;
            Long id = id(object);
            if (id != null && !object.containsKey("_r") && object.containsKey("_s") && !object.containsKey("_b")) {
                described.add(id);
            }
            for (Object value : object.values()) collectDescribed(value);
        }
    }

    private void collectNamed(Object node) {
        if (node instanceof List) {
            for (Object e : (List<?>) node) collectNamed(e);
        } else if (node instanceof Map) {
            Map<?, ?> object = (Map<?, ?>) node;
            Long id = id(object);
            if (id != null && object.containsKey("_r") && !described.contains(id)) unresolved.add(id);
            for (Object value : object.values()) collectNamed(value);
        }
    }
}
