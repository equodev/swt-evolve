package dev.equo.swt;

/**
 * Measurement-only counters for the widget state Java sends: how long flushes take and how much they
 * write. Counted by {@link #TAP}, which is installed only under {@code dev.equo.swt.wireStats}, and
 * only between {@link #start} and {@link #stop}.
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
        if (!BridgeTap.installed(TAP))
            throw new IllegalStateException("wire stats are counted only with -Ddev.equo.swt.wireStats=true");
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

    static final BridgeTap TAP = new BridgeTap() {
        @Override
        long flushStarted() {
            return on ? System.nanoTime() : 0;
        }

        @Override
        long flushSending(long started) {
            return started != 0 ? System.nanoTime() : 0;
        }

        @Override
        void flushEnded(long started, long sending) {
            if (started == 0) return;
            long end = System.nanoTime();
            flushes++;
            flushNanos += end - started;
            sendNanos += end - sending;
        }

        @Override
        void widgetFrame(WidgetWire frame) {
            if (!on) return;
            frames++;
            bytes += frame.byteSize();
            binaryFrames++;
        }

        @Override
        void sent(byte[] buffer, int payloadStart, int length) {
            if (!on) return;
            frames++;
            bytes += length;
        }
    };
}
