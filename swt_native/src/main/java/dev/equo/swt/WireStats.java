package dev.equo.swt;

/**
 * Measurement-only counters for the widget state Java sends: how long flushes take and how much they
 * write. Off unless {@link #start} is called, so production pays one volatile read per flush.
 */
public final class WireStats {

    static volatile boolean on;
    static long flushes;
    static long flushNanos;
    static long sendNanos;
    static long frames;
    static long bytes;
    static long binaryFrames;

    private WireStats() {
    }

    /** Starts counting from zero. */
    public static synchronized void start() {
        flushes = flushNanos = sendNanos = frames = bytes = binaryFrames = 0;
        on = true;
    }

    /**
     * Stops counting and returns what was counted: {@code flushes, flushNanos, sendNanos, frames, bytes,
     * binaryFrames}.
     * {@code flushNanos} covers the whole flush, {@code sendNanos} the part of it spent handing the
     * written messages to the transport.
     */
    public static synchronized long[] stop() {
        on = false;
        return new long[] { flushes, flushNanos, sendNanos, frames, bytes, binaryFrames };
    }

    static void frame(int length) {
        frames++;
        bytes += length;
    }
}
