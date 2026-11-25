package dev.equo.swt;

/** The classes on the current stack, for code outside this package; see {@link StackFrames}. */
public final class CallerClasses {

    private CallerClasses() {}

    /**
     * The first {@code limit} distinct classes on the stack outside {@code excludedPrefixes}, or null
     * on a fragment built for Java 8, whose stack traces carry no class references.
     */
    public static Class<?>[] outside(String[] excludedPrefixes, int limit) {
        return Jdk9.STACK_FRAMES.classesOutside(excludedPrefixes, limit);
    }
}
