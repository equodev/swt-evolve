package dev.equo.swt.awt;

import java.awt.Component;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.equo.swt.FlutterNative;

/**
 * The frames an OpenGL heavyweight presents on Linux, read back so the host can show them.
 *
 * <p>A heavyweight hosted here renders into its own X window, a child of the frame's window, which
 * the {@code JLightweightFrame} peer never maps: the application's drawing reaches nothing on
 * screen and cannot be read afterwards. Every OpenGL application ends a frame with
 * {@code glXSwapBuffers}, the frame drawn and its context current, so the native side rebinds that
 * call and keeps the back buffer of each tracked window from just before the swap.
 *
 * <p>Off with {@code -Dswt.evolve.gl_present_hook=false}. An application that reaches
 * {@code glXSwapBuffers} through {@code glXGetProcAddress} or {@code dlsym} presents nothing here.
 */
final class GlPresentHook {

    static final String PROPERTY = "swt.evolve.gl_present_hook";

    /** What installing did, readable from outside the JVM with {@code jcmd <pid> VM.system_properties}. */
    static final String STATUS_PROPERTY = "dev.equo.swt.glPresentHook";

    private static final AtomicBoolean STARTED = new AtomicBoolean();

    private static volatile boolean installed;

    /** Each tracked heavyweight's X window, or 0 while it has none. */
    private static final Map<Component, Long> WINDOWS = Collections.synchronizedMap(new WeakHashMap<>());

    private GlPresentHook() {}

    static boolean enabled() {
        return enabled(System.getProperty("os.name", ""), System.getProperty(PROPERTY));
    }

    static boolean enabled(String osName, String property) {
        return osName.toLowerCase(Locale.ROOT).contains("linux") && !"false".equalsIgnoreCase(property);
    }

    /**
     * The last frame {@code heavy} presented, as {@code [width, height, argb...]} in window pixels, top
     * row first; null when there is none. Starts tracking the component on its first call and follows
     * it to a new window when its peer is recreated. Event dispatch thread.
     */
    static int[] frame(Component heavy) {
        if (!start()) return null;
        long window = Track(heavy);
        Long previous = window != 0 ? WINDOWS.put(heavy, window) : WINDOWS.remove(heavy);
        if (previous != null && previous != window) Untrack(previous);
        return window == 0 ? null : Frame(window);
    }

    private static boolean start() {
        if (STARTED.compareAndSet(false, true)) {
            int status;
            try {
                FlutterNative.ensureLoaded();
                status = Install();
            } catch (LinkageError | RuntimeException unavailable) {
                notInstalled("unavailable: " + unavailable);
                return false;
            }
            if (status != 0) {
                notInstalled("not installed, code " + status);
                return false;
            }
            installed = true;
            System.setProperty(STATUS_PROPERTY, "installed");
        }
        return installed;
    }

    private static void notInstalled(String status) {
        System.setProperty(STATUS_PROPERTY, status);
        System.err.println("[EvolveSwingHost] GL present hook " + status);
    }

    /** 0 once OpenGL is resolved; otherwise why not (see gl_present_hook.cc). */
    private static native int Install();

    /** The X window of {@code canvas}, now tracked; 0 when it has none. */
    private static native long Track(Component canvas);

    /** Stops tracking {@code window}, once its canvas has another window or none. */
    private static native void Untrack(long window);

    private static native int[] Frame(long window);
}
