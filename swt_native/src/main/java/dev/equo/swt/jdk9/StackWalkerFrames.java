package dev.equo.swt.jdk9;

import dev.equo.swt.StackFrames;

/**
 * StackWalker-backed frame reader — the fast path, and the reason StackFrames is an interface.
 *
 * <p>StackWalker only materialises the frames the walk actually consumes, so the {@code at(skip)}
 * lookups Config runs on every widget creation stay cheap. Java 9+ only, hence its own package:
 * a fragment built for an older SWT release leaves this out and {@code Jdk9} falls back.
 */
public final class StackWalkerFrames implements StackFrames {

    private static final StackWalker WALKER = StackWalker.getInstance();

    /** This class's own frame, so `skip` counts from the caller on both implementations. */
    private static final int OWN_FRAMES = 1;

    @Override
    public StackTraceElement at(int skip) {
        int drop = skip + OWN_FRAMES;
        return WALKER.walk(frames ->
                frames.skip(drop).findFirst().map(StackWalker.StackFrame::toStackTraceElement).orElse(null));
    }

    @Override
    public boolean anyMatch(String className, String methodName) {
        return WALKER.walk(frames -> frames.anyMatch(f ->
                className.equals(f.getClassName())
                        && (methodName == null || methodName.equals(f.getMethodName()))));
    }
}
