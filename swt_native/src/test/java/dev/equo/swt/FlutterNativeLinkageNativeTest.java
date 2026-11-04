package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * Every native method of {@link FlutterNative} has to resolve in every runner. Nothing checks it at build
 * time: a native the runner does not export, or one the Windows or Linux runner defines without the
 * header's {@code extern "C"} (exported under its C++-decorated name), throws {@link UnsatisfiedLinkError}
 * on its first call, in the middle of whatever Java was doing.
 */
@Tag("native-unit")
@EnabledOnOs({WINDOWS, LINUX, MAC})
public class FlutterNativeLinkageNativeTest {

    /**
     * Not called: Initialize opens a window, the loader has already called SetBundleDir, Pump drains
     * the calling thread's event queue, and WaitEvents makes the calling thread the runner's waiter.
     */
    private static final Set<String> NOT_CALLED =
            new HashSet<>(Arrays.asList("Initialize", "SetBundleDir", "Pump", "WaitEvents"));

    @Test
    void everyNativeMethodIsBound() throws Exception {
        try {
            FlutterLibraryLoader.initialize();
        } catch (FlutterLibraryLoader.LibraryLoaderException e) {
            Assumptions.abort("no runner built here: " + e.getMessage());
        }
        List<String> unbound = new ArrayList<>();
        for (Method m : FlutterNative.class.getDeclaredMethods()) {
            // Every entry point called here ignores a null surface.
            if (!Modifier.isNative(m.getModifiers()) || NOT_CALLED.contains(m.getName())) continue;
            m.setAccessible(true);
            try {
                m.invoke(null, neutralArguments(m));
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof UnsatisfiedLinkError) unbound.add(m.getName());
            }
        }
        assertThat(unbound).as("native methods the runner does not export under their JNI name").isEmpty();
    }

    private static Object[] neutralArguments(Method m) {
        Class<?>[] types = m.getParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> t = types[i];
            if (t == long.class) args[i] = 0L;
            else if (t == int.class) args[i] = 0;
            else if (t == boolean.class) args[i] = false;
            else if (t == String.class) args[i] = "";
        }
        return args;
    }
}
