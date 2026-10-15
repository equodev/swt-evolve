package org.eclipse.swt.widgets;

import org.eclipse.swt.internal.win32.OS;

/**
 * Per-OS native init shared by the two Display surfaces on Windows — the web one ({@link WebDisplayBridge}) and the desktop-native one ({@link DeskDisplayBridge}). Initializes
 * OLE/COM for the Display thread.
 */
final class DisplayBridgePlatform {

    static final String LOAD_LIBRARY = "dev.equo.swt.loadLibrary";

    private DisplayBridgePlatform() {
    }

    static void init() {
        // When the native SWT library isn't loaded (headless web/test, e.g. -Ddev.equo.swt.loadLibrary=false)
        // the native call below would throw UnsatisfiedLinkError; pure-web/browser needs no OLE init anyway.
        if (nativeLibraryOff())
            return;
        org.eclipse.swt.internal.win32.OS.OleInitialize(0);
    }

    /** No application menu outside macOS: the platform has no such concept. */
    static Menu systemMenu(Display display) {
        return null;
    }

    /** The menu bar belongs in the window on this platform; only macOS has an OS-owned one. */
    static void setMenuBar(Menu menu) {
    }

    /** Nothing to keep in step: the menu bar is drawn inside the window on this platform. */
    static void syncMenuBar() {
    }

    /** Nothing to bind: a Shell here is drawn by Flutter and has no native view of its own. */
    static void bindWindowView(Control control, long view) {
    }

    /**
     * Destroys the launcher's splash window. It belongs to the Display thread, which never pumps
     * window messages while the Display renders in a browser, so Windows would report the window,
     * and the process with it, as not responding. Only the owning thread can destroy a window.
     *
     * @return whether the window was destroyed
     */
    static boolean dismissLauncherSplash() {
        long hwnd = Long.getLong("org.eclipse.equinox.launcher.splash.handle", 0);
        if (hwnd == 0 || nativeLibraryOff() || OS.GetWindowThreadProcessId(hwnd, new int[1]) != OS.GetCurrentThreadId())
            return false;
        return OS.DestroyWindow(hwnd);
    }

    /** Whether the native SWT library is switched off ({@code -Ddev.equo.swt.loadLibrary=false}). */
    private static boolean nativeLibraryOff() {
        return "false".equals(System.getProperty(LOAD_LIBRARY));
    }
}
