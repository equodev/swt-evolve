package dev.equo.swt;

import java.util.ArrayList;
import java.util.List;

/**
 * Where the start of an application goes: named, nested steps of Evolve's boot, from the native
 * libraries to the client's first frame. Off unless {@code -Ddev.equo.swt.bootTrace=true}; while off,
 * every call returns at once and records nothing.
 *
 * <p>One trace per process: it records the first Display's boot and stops at {@link #freeze()}, which
 * that Display calls once its first ClientReady is applied. A later Display, or another session under
 * Cloud Ready, records nothing, so the trace stays bounded and never mixes sessions.
 *
 * <p>Times are microseconds since the epoch, so steps recorded by the Flutter client (another process
 * on the web, the same one on the desktop) line up with the ones recorded here: both read the same
 * wall clock. {@link #toJson()} lists every step.
 */
public final class BootTrace {

    public static final String PROPERTY = "dev.equo.swt.bootTrace";
    private static final boolean ENABLED = Boolean.getBoolean(PROPERTY);

    private static final long EPOCH_BASE_MICROS = System.currentTimeMillis() * 1000L;
    private static final long NANO_BASE = System.nanoTime();

    private static final Trace TRACE = new Trace();

    private BootTrace() {
    }

    public static boolean enabled() {
        return ENABLED;
    }

    /** Microseconds since the epoch, at microsecond resolution. */
    public static long nowMicros() {
        return EPOCH_BASE_MICROS + (System.nanoTime() - NANO_BASE) / 1000L;
    }

    /**
     * Starts a step, ended by {@link Span#close()}; steps started before it closes are its children.
     * Use with try-with-resources. Null while tracing is off or frozen, which try-with-resources skips.
     */
    public static Span span(String name) {
        return ENABLED ? TRACE.span(name) : null;
    }

    /** A point in time rather than a step: the first time something happened. */
    public static void mark(String name) {
        if (ENABLED) TRACE.mark(name, false);
    }

    /** Like {@link #mark}, but only the first time {@code name} is marked. */
    public static void markOnce(String name) {
        if (ENABLED) TRACE.mark(name, true);
    }

    /** A step from {@code startMicros} to now, recorded only the first time {@code name} ends. */
    public static void endOnce(String name, long startMicros) {
        if (ENABLED) TRACE.endOnce(name, startMicros);
    }

    /** Ends the recording: the boot is complete. */
    public static void freeze() {
        if (ENABLED) TRACE.freeze();
    }

    /** {@code [{"name","source","depth","startUs","endUs"}, ...]} in the order the steps started. */
    public static String toJson() {
        return TRACE.toJson();
    }

    /** The recorded steps and whether recording has ended, guarded by the instance. */
    static final class Trace {
        private final List<Step> steps = new ArrayList<>();
        private final ThreadLocal<int[]> depth = ThreadLocal.withInitial(() -> new int[1]);
        private boolean frozen;

        synchronized Span span(String name) {
            if (frozen) return null;
            int[] level = depth.get();
            Step step = new Step(name, level[0]++, nowMicros(), -1);
            steps.add(step);
            return new Span(step, level);
        }

        synchronized void mark(String name, boolean once) {
            if (frozen || (once && contains(name))) return;
            long now = nowMicros();
            steps.add(new Step(name, depth.get()[0], now, now));
        }

        synchronized void endOnce(String name, long startMicros) {
            if (frozen || contains(name)) return;
            steps.add(new Step(name, 0, startMicros, nowMicros()));
        }

        synchronized void freeze() {
            frozen = true;
        }

        private boolean contains(String name) {
            for (Step s : steps) {
                if (s.name.equals(name)) return true;
            }
            return false;
        }

        synchronized String toJson() {
            StringBuilder out = new StringBuilder("[");
            for (int i = 0; i < steps.size(); i++) {
                Step s = steps.get(i);
                if (i > 0) out.append(',');
                out.append("{\"name\":");
                appendJsonString(out, s.name);
                out.append(",\"source\":\"java\",\"depth\":").append(s.depth)
                        .append(",\"startUs\":").append(s.start)
                        .append(",\"endUs\":").append(s.end).append('}');
            }
            return out.append(']').toString();
        }
    }

    private static void appendJsonString(StringBuilder out, String value) {
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                out.append('\\').append(c);
            } else if (c < 0x20) {
                out.append("\\u00").append(Character.forDigit(c >> 4, 16)).append(Character.forDigit(c & 0xF, 16));
            } else {
                out.append(c);
            }
        }
        out.append('"');
    }

    private static final class Step {
        final String name;
        final int depth;
        final long start;
        volatile long end;

        Step(String name, int depth, long start, long end) {
            this.name = name;
            this.depth = depth;
            this.start = start;
            this.end = end;
        }
    }

    /** An open step; closing it records its end. */
    public static final class Span implements AutoCloseable {
        private final Step step;
        private final int[] depth;

        Span(Step step, int[] depth) {
            this.step = step;
            this.depth = depth;
        }

        @Override
        public void close() {
            step.end = nowMicros();
            depth[0]--;
        }
    }
}
