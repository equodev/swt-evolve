package dev.equo.swt.harness;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.equo.swt.comm.MessageBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Recorded frames as (channel, JSON body) pairs in order, batched or not. A batch spells a channel
 * out once and refers back to it by position, so a later entry on the same channel carries an index.
 */
public final class BatchEntries {

    private BatchEntries() {
    }

    public static List<String[]> of(List<RecordingComm.Frame> frames) {
        List<String[]> out = new ArrayList<>();
        for (RecordingComm.Frame frame : frames) {
            if (!MessageBatch.EVENT.equals(frame.event)) {
                out.add(new String[] { frame.event, frame.json });
                continue;
            }
            JsonArray entries = JsonParser.parseString(frame.json).getAsJsonArray();
            String[] names = new String[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                JsonArray entry = entries.get(i).getAsJsonArray();
                JsonElement first = entry.get(0);
                names[i] = first.getAsJsonPrimitive().isString() ? first.getAsString() : names[first.getAsInt()];
                out.add(new String[] { names[i], entry.get(1).toString() });
            }
        }
        return out;
    }
}
