package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.gtk.GDK;
import org.eclipse.swt.internal.gtk.GTK;
import org.eclipse.swt.internal.gtk.OS;
import org.eclipse.swt.internal.gtk3.GTK3;
import org.eclipse.swt.internal.gtk4.GTK4;

public class SwtFlutterBridge extends SwtFlutterBridgeBase {
    public SwtFlutterBridge(DartWidget widget) {
        super(widget);
    }

    @Override
    protected long getHandle(Control control) {
        return control.handle;
    }

    @Override
    protected void setHandle(DartControl control, long view) {
        // Create a proper SWT fixed container like other SWT widgets
        long fixedHandle = OS.g_object_new(((SwtDisplay) control.display.getImpl()).gtk_fixed_get_type(), 0);
        if (fixedHandle == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
        if (!GTK.GTK4)
            GTK3.gtk_widget_set_has_window(fixedHandle, true);
        
        long parentHandle = control.parent.handle;
        if (GTK.GTK4)
            OS.swt_fixed_add(parentHandle, fixedHandle);
        else
            GTK3.gtk_container_add(parentHandle, fixedHandle);

        if (GTK.GTK4)
            OS.swt_fixed_add(fixedHandle, view);
        else
            GTK3.gtk_container_add(fixedHandle, view);
        GTK.gtk_widget_show(view);

        control.getApi().handle = fixedHandle;
        ((SwtDisplay) control.display.getImpl()).addWidget(control.getApi().handle, control.getApi());
        
        // Show the container after everything is properly anchored
        GTK.gtk_widget_show(fixedHandle);
    }

    @Override
    public Object container(DartComposite parent) {
        return parent.getApi().handle;
    }

    @Override
    public void setBounds(DartControl dartControl, Rectangle bounds) {
        if (!(dartControl instanceof DartMainToolbar))
            super.setBounds(dartControl, bounds);
        if (dartControl.getApi().handle != 0 && bounds != null) {
            // Resize the fixed handle within its parent using SWT's method
            long parent = GTK.gtk_widget_get_parent(dartControl.getApi().handle);
            OS.swt_fixed_move(parent, dartControl.getApi().handle, bounds.x, bounds.y);
            OS.swt_fixed_resize(parent, dartControl.getApi().handle, bounds.width, bounds.height);
        }
        if (dartControl instanceof DartMainToolbar)
            super.setBounds(dartControl, bounds);
    }

    @Override
    public void setFocus(DartControl dartControl) {
        long focusHandle = GetView(context);
//        long focusHandle = dartControl.getApi().handle;
        if (GTK.gtk_widget_has_focus(focusHandle))
            return;
        /* When the control is zero sized it must be realized */
        GTK.gtk_widget_realize(focusHandle);
        GTK.gtk_widget_grab_focus(focusHandle);
    }

    @Override
    public boolean hasFocus(DartControl control) {
        return control.getApi() == control.display.getFocusControl();
    }

    @Override
    protected void destroyHandle(DartControl control) {
        if (control.getApi().handle != 0) {
            // Remove from SWT display first
            ((SwtDisplay) control.display.getImpl()).removeWidget(control.getApi().handle);
            // Destroy the SWT fixed container - GTK will automatically 
            if (GTK.GTK4) {
                GTK.gtk_widget_unparent(control.getApi().handle);
            } else {
                GTK3.gtk_widget_destroy(control.getApi().handle);
            }
            control.getApi().handle = 0;
        }
    }

    @Override
    public Point getWindowOrigin(DartControl control) {
        if (GTK.GTK4) {
            double[] originX = new double[1], originY = new double[1];
            boolean success = GTK4.gtk_widget_translate_coordinates(control.getApi().handle, ((SwtShell) control.getShell().getImpl()).shellHandle, 0, 0, originX, originY);
            return success ? new Point((int) originX[0], (int) originY[0]) : new Point(0, 0);
        } else {
            int[] x = new int[1];
            int[] y = new int[1];
            long window = eventWindow(control);
            GDK.gdk_window_get_origin(window, x, y);
            return new Point(x[0], y[0]);
        }
    }

    @Override
    public void setCursor(DartControl control, long cursor) {
        if (GTK.GTK4) {
            long eventHandle = eventWindow(control);
            GTK4.gtk_widget_set_cursor(eventHandle, cursor);
        } else {
            long window = eventWindow(control);
            if (window != 0) {
                GDK.gdk_window_set_cursor(window, cursor);
                control.update(false, true);
            }
        }
    }

    long eventWindow(DartControl control) {
        long eventHandle = control.getApi().handle;
        GTK.gtk_widget_realize(eventHandle);
        return GTK3.gtk_widget_get_window(eventHandle);
    }

}

