package dev.equo.swt;

import java.lang.reflect.Method;

/**
 * Reaches the parts of Evolve that need a JDK newer than the oldest SWT release we ship for.
 *
 * <p>{@code src/main} is compiled into every platform fragment, so it can only use the API of the
 * lowest {@code minJavaVersion} among the supported SWT releases — 8, for the 2020 ones. Two pieces
 * of Evolve genuinely cannot honour that: the Swing bridge opens the JDK internals it is built on
 * through {@code java.lang.Module}, which did not exist before 9, and the caller lookup uses
 * {@link java.lang.StackWalker}, added in 9. Neither has a Java 8 equivalent to compile against, so a
 * bytecode rewrite cannot save them either — on a Java 8 VM they simply cannot run.
 *
 * <p>So they live in packages a pre-9 build leaves out, and everything else reaches them through
 * this class: one lookup per entry point, resolved once at class-init and cached. A fragment built
 * for a newer release binds the fast implementation; one built for Java 8 gets the fallback.
 */
final class Jdk9 {

    private Jdk9() {}

    private static Class<?> load(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }

    /**
     * {@code EvolveSwingHost.newFrame}, or null when the Swing bridge cannot run: on a fragment built
     * without it, or on a JVM that cannot load it or reach the JDK internals it needs.
     */
    static final Method SWING_HOST_NEW_FRAME = swingHostNewFrame();

    private static Method swingHostNewFrame() {
        if (swingHostFailure(true) != null) return null;
        try {
            return Class.forName("dev.equo.swt.awt.EvolveSwingHost")
                    .getMethod("newFrame", Class.forName("org.eclipse.swt.widgets.Composite"));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    /**
     * Why the Swing bridge cannot run here, or null when it can. Recomputed per call instead of cached
     * in a static: a Throwable is mutable, and every session would share it.
     */
    static Throwable swingHostFailure(boolean explainLoadFailure) {
        Class<?> host;
        try {
            host = Class.forName("dev.equo.swt.awt.EvolveSwingHost");
        } catch (ClassNotFoundException builtWithout) {
            return builtWithout;
        } catch (LinkageError cannotLoad) {
            if (explainLoadFailure) {
                System.err.println("[EvolveSwingHost] Swing embedding disabled: SWT_AWT.new_Frame will fail with "
                        + "ERROR_NOT_IMPLEMENTED. The bridge cannot be loaded on this Java runtime: its image "
                        + "needs the java.desktop module. Cause: " + cannotLoad);
            }
            return cannotLoad;
        }
        try {
            // The sun.swing and sun.awt access checks fire at class definition, which loading the host
            // above did not reach; hostFailure() self-exports and forces those definitions.
            return (Throwable) host.getMethod("hostFailure").invoke(null);
        } catch (java.lang.reflect.InvocationTargetException e) {
            return e.getCause();
        } catch (ReflectiveOperationException | LinkageError e) {
            return e;
        }
    }

    /** The StackWalker-backed frame reader, or the Throwable-backed one below Java 9. */
    static final StackFrames STACK_FRAMES = stackFrames();

    private static StackFrames stackFrames() {
        Class<?> impl = load("dev.equo.swt.jdk9.StackWalkerFrames");
        if (impl != null) {
            try {
                return (StackFrames) impl.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException | LinkageError e) {
                // fall through to the Java 8 reader
            }
        }
        return new ThrowableFrames();
    }
}
