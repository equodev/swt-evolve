package org.eclipse.swt.internal.cloudready;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Pluggable capture of the calling thread's context, for work that later runs on a thread the
 * caller does not own: a host that ties state to threads installs a capturer, and what is captured
 * runs a task under that context on whichever thread calls it.
 *
 * <p>When unset (the default), {@link #capture()} returns {@code null} and the task runs as it is.
 * Desktop usage is unaffected.
 */
public final class CallerContext {

    private static volatile Supplier<Executor> capturer;

    private CallerContext() {}

    /**
     * Install a capturer. Pass {@code null} to clear.
     */
    public static void set(Supplier<Executor> c) {
        capturer = c;
    }

    /**
     * The calling thread's context as an {@link Executor} that runs a task under it on the thread
     * that calls {@code execute}, or {@code null} when no capturer is installed or the calling
     * thread has no context to carry.
     */
    public static Executor capture() {
        Supplier<Executor> c = capturer;
        return c != null ? c.get() : null;
    }

    /**
     * {@code task}, to run under the calling thread's context on whichever thread later runs it; as
     * it is when there is no context to carry.
     */
    public static Runnable wrap(Runnable task) {
        Executor context = capture();
        return context == null ? task : () -> context.execute(task);
    }

    /**
     * {@code target}, running each task under the context of the thread that hands it over.
     */
    public static Executor bind(Executor target) {
        return task -> target.execute(wrap(task));
    }
}
