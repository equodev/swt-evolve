package dev.equo.swt;

/**
 * The slice of a stack trace Config actually reads, behind an interface so the JDK 9+ StackWalker
 * implementation can be left out of a fragment built for an older SWT release.
 *
 * <p>Only class name, method name and file name are ever needed — all three have been on
 * {@link StackTraceElement} since 1.4, which is what makes the Java 8 fallback possible at all.
 * (Config used to ask StackWalker for {@code RETAIN_CLASS_REFERENCE} and never call
 * {@code getDeclaringClass()}; that request is gone.)
 */
public interface StackFrames {

    /** The frame {@code skip} levels above the caller of this method, or null past the stack top. */
    StackTraceElement at(int skip);

    /** Whether any frame matches, with a null {@code methodName} matching any method. */
    boolean anyMatch(String className, String methodName);
}
