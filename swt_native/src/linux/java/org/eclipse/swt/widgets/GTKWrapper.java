package org.eclipse.swt.widgets;

import org.eclipse.swt.internal.gtk3.GTK3;

/**
 * Version-agnostic wrapper for GTK calls.
 * Generated for SWT version 3.131.0.
 *
 * This class provides static methods that delegate to the correct GTK implementation
 * based on the SWT version being used.
 */
@SuppressWarnings("unused")
public class GTKWrapper {

    /**
     * Shows a GTK widget.
     */
    public static void gtk_widget_show(long widget) {
        GTK3.gtk_widget_show(widget);
    }
}
