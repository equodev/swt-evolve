package org.eclipse.swt.widgets;

import org.eclipse.swt.internal.win32.OS;

public class SwtFlutterBridge extends SwtFlutterBridgeBase {
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
    protected void destroyHandle(DartControl control) {
        ((SwtDisplay) control.display.getImpl()).removeControl(control.getApi().handle);
        control.getApi().handle = 0;
    }

    @Override
    public void setFocus(DartControl control) {
        OS.SetFocus(control.getApi().handle);
    }

    @Override
    public boolean hasFocus(DartControl widget) {
        /*
         * If a non-SWT child of the control has focus,
         * then this control is considered to have focus
         * even though it does not have focus in Windows.
         */
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
    }
}

