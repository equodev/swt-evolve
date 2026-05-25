package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.gtk.GDK;
import org.eclipse.swt.internal.gtk.GTK;
import org.eclipse.swt.internal.gtk.OS;
import org.eclipse.swt.internal.gtk3.GTK3;
import org.eclipse.swt.internal.gtk4.GTK4;

public class SwtFlutterBridge extends SwtFlutterBridgeWeb {
    SwtFlutterBridge(DartDisplay display) {
        super(display);
        init();
    }

    SwtFlutterBridge(DartWidget widget) {
        super(widget);
    }

    private void init() {
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