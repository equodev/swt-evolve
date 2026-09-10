package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.gtk.GTK;

/**
 * Per-OS native init shared by the two Display surfaces on Linux — the web one ({@link WebDisplayBridge}) and the desktop-native one ({@link DeskDisplayBridge}). Initializes
 * GTK (and its SWT lock functions) once for the Display thread.
 */
final class DisplayBridgePlatform {
    private DisplayBridgePlatform() {
    }

    static void init() {
        // When the native SWT library isn't loaded (headless web/test, e.g. -Ddev.equo.swt.loadLibrary=false)
        // the GTK calls below would throw UnsatisfiedLinkError; pure-web/browser needs no GTK init anyway.
        if ("false".equals(System.getProperty("dev.equo.swt.loadLibrary")))
            return;
        if (!GTK.GTK4) {
            GTKWrapper.setupThreadLocking();
        }
        boolean init;
        if (GTK.GTK4) {
            init = GTKWrapper.gtk_init_check();
        } else {
            init = GTKWrapper.gtk_init_check(new long[] { 0 }, null);
        }
        if (!init)
            SWT.error(SWT.ERROR_NO_HANDLES, null, " [gtk_init_check() failed]");
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
