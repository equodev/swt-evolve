package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.cocoa.NSView;
import org.eclipse.swt.internal.cocoa.OS;

public class SwtFlutterBridge extends SwtFlutterBridgeBase {
    public SwtFlutterBridge(DartWidget widget) {
        super(widget);
    }

    protected long getHandle(Control control) {
        return control.view.id;
    }

    @Override
    protected void setHandle(DartControl control, long view) {
        control.getApi().view = new NSView(view);
        control.jniRef = OS.NewGlobalRef(control.getApi());
        if (control.jniRef == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
        ((SwtDisplay) control.display.getImpl()).addWidget(control.getApi().view, control.getApi());
    }

    @Override
    public Object container(DartComposite parent) {
        return parent.getApi().view;
    }

    @Override
    protected void destroyHandle(DartControl control) {
        ((SwtDisplay) control.display.getImpl()).removeWidget(control.getApi().view);
        if (control.jniRef != 0)
            OS.DeleteGlobalRef(control.jniRef);
        control.jniRef = 0;
        control.getApi().view = null;
    }
}

