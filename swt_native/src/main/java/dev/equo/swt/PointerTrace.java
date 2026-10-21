package dev.equo.swt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * The raw pointer events the client received and the frames it built, the mouse events that reached
 * Java from it, and what Java sent it, in arrival order: what a test reads to tell input that never
 * reached the client from input the client received and did not report, and a client that lags the
 * widget tree from one that was never told. Off unless {@code -Ddev.equo.swt.tracePointer=true}.
 * <p>
 * One trace for the process, not per session: a debugging aid, read by a single-session test.
 */
public final class PointerTrace {

    private static final boolean ENABLED = Boolean.getBoolean("dev.equo.swt.tracePointer");
    /** Nothing may drain it (an app run with the flag): only the latest lines are kept. */
    private static final int CAPACITY = 5000;
    private static final ArrayDeque<String> lines = new ArrayDeque<>();

    private PointerTrace() {
    }

    /** Callers check it before building a line, so a trace that is off costs nothing. */
    public static boolean enabled() {
        return ENABLED;
    }

    public static void record(String line) {
        if (!ENABLED) return;
        synchronized (lines) {
            if (lines.size() == CAPACITY) lines.removeFirst();
            lines.addLast(System.currentTimeMillis() % 100_000 + " " + line);
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
