package org.eclipse.swt.widgets;

import org.eclipse.swt.internal.gtk.GTK;
import org.eclipse.swt.internal.gtk.GDK;
import org.eclipse.swt.internal.gtk.OS;
import org.eclipse.swt.internal.gtk3.GTK3;
import org.eclipse.swt.internal.gtk4.GTK4;

/**
 * Version-agnostic wrapper for GTK calls.
 * Generated for SWT version 3.131.0.
 *
 * This class provides static methods that delegate to the correct GTK implementation
 * based on the SWT version being used. SwtFlutterBridge uses this class exclusively
 * so it doesn't need version-conditional imports.
 */
@SuppressWarnings("unused")
public class GTKWrapper {

    public static final boolean IS_GTK4 = GTK.GTK4;

    public static void gtk_widget_show(long widget) {
        GTK3.gtk_widget_show(widget);
    }

    public static void gtk_widget_hide(long widget) {
        GTK3.gtk_widget_hide(widget);
    }

    public static long gtk_widget_get_parent(long widget) {
        return GTK.gtk_widget_get_parent(widget);
    }

    public static boolean gtk_widget_has_focus(long widget) {
        return GTK.gtk_widget_has_focus(widget);
    }

    public static void gtk_widget_realize(long widget) {
        GTK.gtk_widget_realize(widget);
    }

    public static void gtk_widget_grab_focus(long widget) {
        GTK.gtk_widget_grab_focus(widget);
    }

    public static void gtk_widget_unparent(long widget) {
        GTK.gtk_widget_unparent(widget);
    }

    public static void gtk_widget_set_has_window(long widget, boolean has_window) {
        GTK3.gtk_widget_set_has_window(widget, has_window);
    }

    public static void gtk_container_add(long container, long widget) {
        GTK3.gtk_container_add(container, widget);
    }

    public static void gtk_container_remove(long container, long widget) {
        GTK3.gtk_container_remove(container, widget);
    }

    public static void gtk_widget_destroy(long widget) {
        GTK3.gtk_widget_destroy(widget);
    }

    public static long gtk_widget_get_window(long widget) {
        return GTK3.gtk_widget_get_window(widget);
    }

    public static void swt_fixed_add(long container, long widget) {
        OS.swt_fixed_add(container, widget);
    }

    public static int gdk_window_get_origin(long window, int[] x, int[] y) {
        return GDK.gdk_window_get_origin(window, x, y);
    }

    public static void gdk_window_set_cursor(long window, long cursor) {
        GDK.gdk_window_set_cursor(window, cursor);
    }

    public static boolean gtk_widget_translate_coordinates(long src_widget, long dest_widget, double src_x, double src_y, double[] dest_x, double[] dest_y) {
        return GTK4.gtk_widget_translate_coordinates(src_widget, dest_widget, src_x, src_y, dest_x, dest_y);
    }

    public static void gtk_widget_set_cursor(long widget, long cursor) {
        GTK4.gtk_widget_set_cursor(widget, cursor);
    }
}
