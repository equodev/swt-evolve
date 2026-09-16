package dev.equo.swt;

/**
 * Java 8 fallback: the only public way to read the stack below 9.
 *
 * <p>Unlike StackWalker this cannot be lazy — {@code fillInStackTrace} walks the whole stack and
 * materialises the whole array even when only one frame is wanted. That cost is why the StackWalker
 * implementation still exists rather than being replaced outright: fragments for current SWT
 * releases keep it, and only the pre-9 ones pay this.
 */
final class ThrowableFrames implements StackFrames {

    /** This class's own frame, so `skip` counts from the caller on both implementations. */
    private static final int OWN_FRAMES = 1;

    @Override
    public StackTraceElement at(int skip) {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        int remaining = skip + OWN_FRAMES;
        for (StackTraceElement frame : trace) {
            if (isHidden(frame)) continue;
            if (remaining-- == 0) return frame;
        }
        return null;
    }

    @Override
    public boolean anyMatch(String className, String methodName) {
        for (StackTraceElement f : new Throwable().getStackTrace()) {
            if (isHidden(f)) continue;
            if (className.equals(f.getClassName())
                    && (methodName == null || methodName.equals(f.getMethodName()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * The frames StackWalker leaves out unless asked for SHOW_REFLECT_FRAMES / SHOW_HIDDEN_FRAMES.
     * Without this the two implementations disagree the moment anything up the stack was invoked
     * reflectively — which in an RCP is most of it — and `skip` would count different frames
     * depending on which implementation the fragment happens to carry.
     */
    private static boolean isHidden(StackTraceElement frame) {
        String c = frame.getClassName();
        return c.startsWith("jdk.internal.reflect.")
                || c.startsWith("sun.reflect.")
                || c.startsWith("java.lang.invoke.LambdaForm")
                || ("java.lang.reflect.Method".equals(c) && "invoke".equals(frame.getMethodName()))
                || ("java.lang.reflect.Constructor".equals(c) && "newInstance".equals(frame.getMethodName()));
    }
}
