package dev.equo.swt;

import java.lang.reflect.Field;

/** Reads a field another library declares privately, for presenting its objects; never writes one. */
public final class DeclaredFields {

    private DeclaredFields() {
    }

    /** The value of {@code name} as {@code declaringClass} declares it on {@code object}; null when there is none. */
    public static Object read(Object object, String declaringClass, String name) {
        for (Class<?> c = object == null ? null : object.getClass(); c != null; c = c.getSuperclass()) {
            if (!c.getName().equals(declaringClass)) continue;
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(object);
            } catch (ReflectiveOperationException | RuntimeException e) {
                return null;
            }
        }
        return null;
    }
}
