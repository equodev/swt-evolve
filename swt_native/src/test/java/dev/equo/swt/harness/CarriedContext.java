package dev.equo.swt.harness;

import org.eclipse.swt.internal.cloudready.CallerContext;

/**
 * A context tied to the thread, as a host's would be, and the capturer that carries it: what a
 * test sets on one thread and reads on another to tell under which context a task ran.
 */
public final class CarriedContext {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    private CarriedContext() {}

    public static void set(String context) {
        CONTEXT.set(context);
    }

    public static String get() {
        return CONTEXT.get();
    }

    public static void remove() {
        CONTEXT.remove();
    }

    /** Installs the capturer; {@link #uninstall()} after the test. */
    public static void install() {
        CallerContext.set(() -> {
            String captured = CONTEXT.get();
            if (captured == null) return null;
            return task -> {
                String previous = CONTEXT.get();
                CONTEXT.set(captured);
                try {
                    task.run();
                } finally {
                    CONTEXT.set(previous);
                }
            };
        });
    }

    public static void uninstall() {
        CallerContext.set(null);
        CONTEXT.remove();
    }
}
