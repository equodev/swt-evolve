package org.eclipse.swt.widgets;

import java.util.Arrays;

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
//        ((SwtDisplay) control.display.getImpl()).addWidget(control.getApi().handle, control.getApi());
    }

    @Override
    public Object container(DartComposite parent) {
        return parent.getApi().handle;
    }

    @Override
    protected void destroyHandle(DartWidget control, DartControl dartControl) {
//        ((SwtDisplay) control.display.getImpl()).removeWidget(dartControl.getApi().view);
//        if (dartControl.jniRef != 0)
//            OS.DeleteGlobalRef(dartControl.jniRef);
//        control.jniRef = 0;
        dartControl.getApi().handle = 0;
    }
}

