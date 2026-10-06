package dev.equo.swt;

import org.eclipse.swt.internal.DPIUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Runs a block under a given {@code swt.autoScale} policy, on every supported SWT version.
 *
 * <p>{@code DPIUtil.runWithAutoScaleValue} only exists in the newer releases. The older ones read
 * the policy once, into a static String, when {@code DPIUtil} loads, so the system property cannot
 * change it afterwards; the field is swapped instead. Either way, a {@code DPIUtil.setDeviceZoom}
 * inside the block applies the policy.
 */
public final class AutoScalePolicy {

    private AutoScalePolicy() {
    }

    public static void runWith(String policy, Runnable action) {
        Method runWith = runWithAutoScaleValue();
        if (runWith != null) {
            invoke(runWith, policy, action);
            return;
        }
        Field field = autoScaleField();
        Object saved = get(field);
        set(field, policy);
        try {
            action.run();
        } finally {
            set(field, saved);
        }
    }

    private static Method runWithAutoScaleValue() {
        try {
            return DPIUtil.class.getMethod("runWithAutoScaleValue", String.class, Runnable.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static void invoke(Method method, String policy, Runnable action) {
        try {
            method.invoke(null, policy, action);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException(cause);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Field autoScaleField() {
        try {
            Field field = DPIUtil.class.getDeclaredField("autoScaleValue");
            if (field.getType() != String.class) {
                throw new IllegalStateException("DPIUtil.autoScaleValue is a " + field.getType().getName());
            }
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("this SWT version keeps the autoscale policy elsewhere", e);
        }
    }

    private static Object get(Field field) {
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void set(Field field, Object value) {
        try {
            field.set(null, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
