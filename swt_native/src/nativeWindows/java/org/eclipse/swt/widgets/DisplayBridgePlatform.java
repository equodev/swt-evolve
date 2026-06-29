package org.eclipse.swt.widgets;

/**
 * Per-OS native init shared by the two Display surfaces on Windows — the web one ({@link WebDisplayBridge}) and the desktop-native one ({@link DeskDisplayBridge}). Initializes
 * OLE/COM for the Display thread.
 */
final class DisplayBridgePlatform {
    private DisplayBridgePlatform() {
    }

    static void init() {
        // When the native SWT library isn't loaded (headless web/test, e.g. -Ddev.equo.swt.loadLibrary=false)
        // the native call below would throw UnsatisfiedLinkError; pure-web/browser needs no OLE init anyway.
        if ("false".equals(System.getProperty("dev.equo.swt.loadLibrary")))
            return;
        org.eclipse.swt.internal.win32.OS.OleInitialize(0);
    }
}
