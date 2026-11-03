package dev.equo.swt.awt;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.equo.swt.FlutterNative;

/**
 * Keeps OpenGL drawing into the front buffer of an off-screen heavyweight visible.
 *
 * <p>An immediate-mode overlay, such as an XOR tracking cursor, is drawn into the front buffer of
 * the canvas's own window or of the drawable its scene renders into, outside any paint, and asks
 * for no repaint. Off screen neither is ever presented, so the overlay is lost. With
 * {@code -Dswt.evolve.gl_front_redirect=true} the Windows runner keeps that drawing in a layer of its
 * own (the hidden window's back buffer, or the scene drawable's front buffer), reads the layer back on
 * the {@code glFlush} that ends it, and the host XORs it onto the canvas's last painted frame, which
 * is what the drawing would have left on screen.
 *
 * <p>A render of the canvas's scene wipes the overlay, as a repaint wipes front-buffer drawing on
 * screen; the application knows this and draws its overlay afresh instead of erasing the old one.
 * Renders are told apart by the drawable they go to, learnt while the host paints the canvas, since
 * the application also renders on its own.
 *
 * <p>The flush is reported on a thread of its own, not inside the application's GL call, and acted on
 * through {@link EventQueue#invokeLater}: the drawing happens on the event dispatch thread, so the
 * frame is pushed after the event that drew, never between an overlay's erase and its redraw.
 */
final class GlFrontBuffer {

    static final String PROPERTY = "swt.evolve.gl_front_redirect";

    /** What installing did, readable from outside the JVM with {@code jcmd <pid> VM.system_properties}. */
    static final String STATUS_PROPERTY = "dev.equo.swt.glFrontRedirect";

    private static final AtomicBoolean STARTED = new AtomicBoolean();

    private static final AtomicBoolean PUSH_PENDING = new AtomicBoolean();

    private static volatile boolean installed;

    /** Each tracked canvas's native window, or 0 when it has none to track. */
    private static final Map<Component, Long> WINDOWS = Collections.synchronizedMap(new WeakHashMap<>());

    private GlFrontBuffer() {}

    static boolean enabled() {
        return Boolean.getBoolean(PROPERTY);
    }

    /** Installs the redirect once per process; {@code showOverlays} runs on the event dispatch thread. */
    static void start(Runnable showOverlays) {
        if (!STARTED.compareAndSet(false, true)) return;
        int status;
        try {
            FlutterNative.ensureLoaded();
            status = Install();
        } catch (LinkageError | RuntimeException unavailable) {
            notInstalled("unavailable: " + unavailable);
            return;
        }
        if (status != 0) {
            notInstalled("not installed, code " + status);
            return;
        }
        installed = true;
        System.setProperty(STATUS_PROPERTY, "installed");
        Thread watcher = new Thread(() -> {
            while (AwaitFrontFlush()) {
                if (PUSH_PENDING.compareAndSet(false, true)) {
                    EventQueue.invokeLater(() -> {
                        PUSH_PENDING.set(false);
                        showOverlays.run();
                    });
                }
            }
        }, "Evolve GL front-buffer flush");
        watcher.setDaemon(true);
        watcher.start();
    }

    private static void notInstalled(String status) {
        System.setProperty(STATUS_PROPERTY, status);
        System.err.println("[EvolveSwingHost] GL front-buffer redirect " + status);
    }

    /**
     * Redirects the front-buffer drawing of {@code canvas}'s window, looked up again on every call
     * since the canvas gets a new window when its peer is recreated. Event dispatch thread.
     */
    static void track(Component canvas) {
        if (!installed) return;
        long window = Track(canvas);
        Long previous = window != 0 ? WINDOWS.put(canvas, window) : WINDOWS.remove(canvas);
        if (previous != null && previous != window) Untrack(previous);
    }

    /** Runs {@code paint} for {@code canvas}, learning where its scene renders go. */
    static void paint(Component canvas, Runnable paint) {
        long window = installed ? WINDOWS.getOrDefault(canvas, 0L) : 0;
        if (window == 0) {
            paint.run();
            return;
        }
        BeginPaint(window);
        try {
            paint.run();
        } finally {
            EndPaint();
        }
    }

    /** {@code canvas}'s overlay as {@code [width, height, rgb...]} in window pixels, top row first; null when empty. */
    static int[] overlay(Component canvas) {
        long window = installed ? WINDOWS.getOrDefault(canvas, 0L) : 0;
        return window == 0 ? null : Overlay(window);
    }

    /** 0 once opengl32 is patched; otherwise why not (see gl_front_buffer.cpp). */
    private static native int Install();

    /** Blocks until a flush ends a redirected draw; false when nothing is installed. */
    private static native boolean AwaitFrontFlush();

    /** The native window of {@code canvas}, now tracked; 0 when it has none. */
    private static native long Track(Component canvas);

    /** Stops tracking {@code window}, once its canvas has another window or none. */
    private static native void Untrack(long window);

    /** Until {@link #EndPaint}, a scene render on this thread shows where {@code window}'s canvas renders. */
    private static native void BeginPaint(long window);

    private static native void EndPaint();

    private static native int[] Overlay(long window);
}
