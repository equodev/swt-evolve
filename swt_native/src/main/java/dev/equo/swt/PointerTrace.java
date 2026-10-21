package dev.equo.swt;

import java.util.ArrayList;
import java.util.List;

/**
 * The raw pointer events the client received, and the mouse events that reached Java from it, in
 * arrival order: what a test reads to tell input that never reached the client from input the client
 * received and did not report. Off unless {@code -Ddev.equo.swt.tracePointer=true}.
 */
public final class PointerTrace {

    private static final boolean ENABLED = Boolean.getBoolean("dev.equo.swt.tracePointer");
    private static final List<String> lines = new ArrayList<>();

    private PointerTrace() {
    }

    public static boolean enabled() {
        return ENABLED;
    }

    public static void record(String line) {
        if (!ENABLED) return;
        synchronized (lines) {
            lines.add(System.currentTimeMillis() % 100_000 + " " + line);
        }
    }

    /** Everything recorded since the last call. */
    public static List<String> drain() {
        synchronized (lines) {
            List<String> out = new ArrayList<>(lines);
            lines.clear();
            return out;
        }
    }
}
