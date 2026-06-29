package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.gtk.GDK;
import org.eclipse.swt.internal.gtk.GTK;
import org.eclipse.swt.internal.gtk.OS;
import org.eclipse.swt.internal.gtk3.GTK3;
import org.eclipse.swt.internal.gtk4.GTK4;

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
            OS.swt_set_lock_functions();
            GDK.gdk_threads_init();
            GDK.gdk_threads_enter();
        }
        boolean init;
        if (GTK.GTK4) {
            init = GTK4.gtk_init_check();
        } else {
            init = GTK3.gtk_init_check(new long[] { 0 }, null);
        }
        if (!init)
            SWT.error(SWT.ERROR_NO_HANDLES, null, " [gtk_init_check() failed]");
    }
}
