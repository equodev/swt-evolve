package org.eclipse.swt.internal.cloudready;

import java.util.function.Supplier;

/**
 * Pluggable resolver for {@code Display.getDefault()}.
 *
 * <p>When unset (the default), {@link #get()} returns {@code null} and each
 * platform's {@code SwtDisplay.getDefault()} falls back to its legacy
 * {@code static Default} singleton. Desktop usage is unaffected.
 *
 * <p>Returns {@code Object} (not {@code Display}) so this shared class has no
 * dependency on the platform-specific {@code Display} type; each platform's
 * {@code SwtDisplay.getDefault()} performs the {@code instanceof Display} cast.
 */
public final class DefaultDisplayResolver {

    private static volatile Supplier<Object> resolver;

    private DefaultDisplayResolver() {}

    /**
     * Install a resolver. Pass {@code null} to clear.
     */
    public static void set(Supplier<Object> r) {
        resolver = r;
    }

    /**
     * Returns the currently-resolved Display object, or {@code null} if no
     * resolver is installed or the resolver itself returned {@code null}.
     */
    public static Object get() {
        Supplier<Object> r = resolver;
        return r != null ? r.get() : null;
    }

    /**
     * Whether a resolver is installed.
     */
    public static boolean isSet() {
        return resolver != null;
    }
}
