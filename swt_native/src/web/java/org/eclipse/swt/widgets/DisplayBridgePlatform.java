package org.eclipse.swt.widgets;

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
}
