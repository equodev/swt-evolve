package dev.equo.swt;

/**
 * Steps of the UI thread's work for the trace file {@code dev.equo.swt.traceTap} names (see TraceTap):
 * a no-op unless that property is set. Callers outside the bridge's package mark through here.
 */
public final class Trace {

    private Trace() {
    }

    /** Whether steps are recorded; when not, callers need not take the time. */
    public static boolean on() {
        return FlutterBridge.TAP.tracing();
    }

    /** Marks {@code name} as happening now. */
    public static void mark(String name) {
        FlutterBridge.TAP.mark(name);
    }

    /** Records {@code name} as running from {@code startNanos} ({@link System#nanoTime}) to now. */
    public static void span(String name, long startNanos) {
        FlutterBridge.TAP.span(name, startNanos);
    }

    /** The start of a step for {@link #paintEnded}: now when steps are recorded, 0 otherwise. */
    public static long started() {
        return on() ? System.nanoTime() : 0;
    }

    /** Records the Paint of a {@code widget} control as running from {@code startNanos}, if it was timed. */
    public static void paintEnded(Class<?> widget, long startNanos) {
        if (startNanos != 0) span("Java.paint:" + widget.getSimpleName(), startNanos);
    }
}
