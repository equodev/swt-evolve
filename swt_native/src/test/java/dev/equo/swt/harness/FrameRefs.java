package dev.equo.swt.harness;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The widgets a run of outbound frames describes, and those it names ({@code _r}) before any of them
 * described it, read in delivery order as the client applies them. A name the client cannot resolve
 * costs it one refresh request.
 */
public final class FrameRefs {

    /** Ids described in full (a {@code _s} payload that is not a diff against a {@code _b} base). */
    public final Set<Long> described = new HashSet<>();
    /** Ids named before anything described them, in the order they were first named. */
    public final Set<Long> unresolved = new LinkedHashSet<>();

    public static FrameRefs of(List<RecordingComm.Frame> frames) {
        FrameRefs refs = new FrameRefs();
        for (RecordingComm.Frame frame : frames) refs.walk(JsonParser.parseString(frame.json));
        return refs;
    }

    private void walk(JsonElement element) {
        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) walk(e);
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("id") && object.has("_r")) {
                long id = object.get("id").getAsLong();
                if (!described.contains(id)) unresolved.add(id);
            } else if (object.has("id") && object.has("_s") && !object.has("_b")) {
                described.add(object.get("id").getAsLong());
            }
            for (var entry : object.entrySet()) walk(entry.getValue());
        }
    }
}
