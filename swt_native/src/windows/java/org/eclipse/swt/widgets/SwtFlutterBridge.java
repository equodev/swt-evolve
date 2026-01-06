package org.eclipse.swt.widgets;

import org.eclipse.swt.internal.win32.OS;

public class SwtFlutterBridge extends SwtFlutterBridgeBase {
    private DartControl focused = null;
    public SwtFlutterBridge(DartWidget widget) {
        super(widget);
    }

    protected long getHandle(Control control) {
        return control.handle;
    }

    @Override
    protected void setHandle(DartControl control, long view) {
        control.getApi().handle = view;
        ((SwtDisplay) control.display.getImpl()).addControl(control.getApi().handle, control.getApi());
    }

    @Override
    public Object container(DartComposite parent) {
        return parent.getApi().handle;
    }

    @Override
    public void destroy(DartWidget control) {
        StackWalker walker = StackWalker.getInstance();

        StackWalker.StackFrame frame = walker.walk(stream -> stream.filter(f -> f.getMethodName().equals("WM_CLOSE")).findFirst().orElse(null));
        if (frame != null) {
            if (control instanceof DartControl dartControl && control == forWidget) {
                context = 0;
                destroyHandle(dartControl);
            }
            return;
        }
        super.destroy(control);
    }

    @Override
    protected void destroyHandle(DartControl control) {
        ((SwtDisplay) control.display.getImpl()).removeControl(control.getApi().handle);
        control.getApi().handle = 0;
    }

    @Override
    public void setFocus(DartControl control) {
        focused = control;
        long handle = control.getApi().handle;

        if (handle == 0) {
            Composite parent = control.getParent();
            while (parent != null && handle == 0) {
                handle = parent.handle;
                if (handle == 0) {
                    parent = parent.getParent();
                }
            }
        }

        if (handle != 0) {
            OS.SetFocus(handle);
        }
    }

    /*@Override
    public boolean hasFocus(DartControl widget) {
        long hwndFocus = OS.GetFocus();
        while (hwndFocus != 0) {
            if (hwndFocus == widget.getApi().handle)
                return true;
            if (((SwtDisplay) widget.getDisplay().getImpl()).getControl(hwndFocus) != null) {
                return false;
            }
            hwndFocus = OS.GetParent(hwndFocus);
        }
        return false;
    }*/
    /*@Override
    public boolean hasFocus(DartControl widget) {
        /*
         * If a non-SWT child of the control has focus,
         * then this control is considered to have focus
         * even though it does not have focus in Windows.
         *
        if (widget != focused) {
            return false;
        }

        long hwndFocus = OS.GetFocus();
        while (hwndFocus != 0) {
            long widgetHandle = widget.getApi().handle;
            if (hwndFocus == widgetHandle) {
                return true;
            }

            Composite parent = widget.getParent();
            while (parent != null) {
                if (hwndFocus == parent.handle) {
                    return true;
                }
                parent = parent.getParent();
            }

            if (((SwtDisplay) widget.getDisplay().getImpl()).getControl(hwndFocus) != null) {
                return false;
            }
            hwndFocus = OS.GetParent(hwndFocus);
        }
        return false;
    }*/
    @Override
    public boolean hasFocus(DartControl widget) {
        /*
         * If a non-SWT child of the control has focus,
         * then this control is considered to have focus
         * even though it does not have focus in Windows.
         */

        if (widget != focused) {
            return false;
        }

        long hwndFocus = OS.GetFocus();
        while (hwndFocus != 0) {
            long widgetHandle = widget.getApi().handle;
            if (hwndFocus == widgetHandle) {
                return true;
            }

            Composite parent = widget.getParent();
            while (parent != null) {
                if (hwndFocus == parent.handle) {
                    return true;
                }
                parent = parent.getParent();
            }

            if (((SwtDisplay) widget.getDisplay().getImpl()).getControl(hwndFocus) != null) {
                return false;
            }
            hwndFocus = OS.GetParent(hwndFocus);
        }
        return false;
    }

    @Override
    public void reparent(DartControl control, Composite newParent) {
        long controlHandle = control.getApi().handle;

        if (controlHandle == 0) {
            return;
        }

        OS.SetParent(controlHandle, newParent.handle);

        // Also set the window position to the bottom after reparenting
        int flags = OS.SWP_NOSIZE | OS.SWP_NOMOVE | OS.SWP_NOACTIVATE;
        OS.SetWindowPos(controlHandle, OS.HWND_BOTTOM, 0, 0, 0, 0, flags);
    }
}

