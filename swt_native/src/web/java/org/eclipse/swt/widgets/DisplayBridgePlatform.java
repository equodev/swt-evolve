package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Per-OS native init hook for the Display surfaces ({@link WebDisplayBridge} / {@link DeskDisplayBridge}).
 * The pure-web build has no native platform to bind to (no GTK/Cocoa/Win32), so this is a no-op; the
 * web-OS source sets (webMacos/webLinux/webWindows) supply their own GTK/OLE-initializing variant.
 */
final class DisplayBridgePlatform {
    private DisplayBridgePlatform() {
    }

    static void init() {
        // Pure web: no Java-side native init.
    }

    /** Nothing {@link #init()} set up outlives the Display on this platform. */
    static void release() {
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

    /** The pure-web build has no native library to read the OS pointer with. */
    static Point cursorLocation() {
        return null;
    }

    /** The pure-web build has no native library to move the OS pointer with. */
    static boolean moveCursor(int x, int y) {
        return false;
    }

    /** The pure-web build has no native library to read the mouse buttons with. */
    static boolean pointerButtonDown() {
        return false;
    }

    /**
     * The pure-web build has no native library to reach the launcher's window with.
     *
     * @return false, nothing is dismissed
     */
    static boolean dismissLauncherSplash() {
        return false;
    }

    /** A shell the application gives no size: 5/8 of the viewport each way, truncated, as GTK sizes its client area. */
    static Point defaultShellSize(Rectangle screen, DartShell shell) {
        return new Point(screen.width * 5 / 8, screen.height * 5 / 8);
    }
}
