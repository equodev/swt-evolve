package dev.equo.swt;

import java.lang.reflect.Method;

/**
 * Reaches the parts of Evolve that need a JDK newer than the oldest SWT release we ship for.
 *
 * <p>{@code src/main} is compiled into every platform fragment, so it can only use the API of the
 * lowest {@code minJavaVersion} among the supported SWT releases — 8, for the 2020 ones. Two pieces
 * of Evolve genuinely cannot honour that: the Swing bridge is built on {@code jdk.swing.interop},
 * a package that did not exist before 9, and the caller lookup uses {@link java.lang.StackWalker},
 * added in 9. Neither has a Java 8 equivalent to compile against, so a bytecode rewrite cannot save
 * them either — on a Java 8 VM they simply cannot run.
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

    /** {@code EvolveSwingHost.newFrame}, or null on a fragment built without the Swing bridge. */
    static final Method SWING_HOST_NEW_FRAME = swingHostNewFrame();

    private static Method swingHostNewFrame() {
        Class<?> host = load("dev.equo.swt.awt.EvolveSwingHost");
        if (host == null) return null;
        try {
            return host.getMethod("newFrame", Class.forName("org.eclipse.swt.widgets.Composite"));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
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
