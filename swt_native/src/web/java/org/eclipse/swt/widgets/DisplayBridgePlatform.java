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
}
