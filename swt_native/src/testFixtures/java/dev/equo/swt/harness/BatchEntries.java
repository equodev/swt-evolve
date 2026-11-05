package dev.equo.swt.harness;

import dev.equo.swt.comm.MessageBatch;

import java.util.ArrayList;
import java.util.List;

/** Recorded frames as their entries in order, batched or not. */
public final class BatchEntries {

    private BatchEntries() {
    }

    @SuppressWarnings("unchecked")
    public static List<GcFrames.Entry> of(List<RecordingComm.Frame> frames) {
        List<GcFrames.Entry> out = new ArrayList<>();
        for (RecordingComm.Frame frame : frames) {
            if (MessageBatch.EVENT.equals(frame.event) && frame.value instanceof List) {
                out.addAll((List<GcFrames.Entry>) frame.value);
            } else {
                out.add(new GcFrames.Entry(frame.event, frame.value));
            }
        }
        return out;
    }
}
