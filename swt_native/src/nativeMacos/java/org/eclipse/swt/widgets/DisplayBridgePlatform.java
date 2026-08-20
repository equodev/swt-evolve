package org.eclipse.swt.widgets;

/**
 * Per-OS native init shared by the two Display surfaces on macOS — the web one ({@link WebDisplayBridge}) and the desktop-native one ({@link DeskDisplayBridge}). Creating the
 * window itself needs nothing from Java (the desktop-native window sets up {@code NSApp} natively
 * in {@code bridge.swift}, and the web surface drives a browser/Chromium), but the application menu
 * in the system menu bar is owned by the OS and has to be built here — see
 * {@link MacApplicationMenu}.
 */
final class DisplayBridgePlatform {
    private DisplayBridgePlatform() {
    }

    static void init() {
        // When the native SWT library isn't loaded (headless web/test, e.g. -Ddev.equo.swt.loadLibrary=false)
        // the Cocoa calls below would throw UnsatisfiedLinkError, and a headless run has no menu bar anyway.
        if ("false".equals(System.getProperty("dev.equo.swt.loadLibrary")))
            return;
        MacApplicationMenu.install();
    }

    /** The application menu as SWT widgets, for the menu bar Evolve draws inside the window. */
    static Menu systemMenu(Display display) {
        if ("false".equals(System.getProperty("dev.equo.swt.loadLibrary")))
            return null;
        return MacApplicationMenu.systemMenu(display);
    }
}
