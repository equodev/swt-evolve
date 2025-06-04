package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterLibraryLoader;
import org.eclipse.swt.SWT;

public class SwtFlutterBridge extends FlutterBridge {
    private long context;

    static {
        FlutterLibraryLoader.initialize();
    }

    public static SwtFlutterBridge of(DartControl dartControl) {
        if (dartControl.parent.getImpl() instanceof SwtComposite) {
            SwtComposite parentComposite = new SwtComposite(dartControl.parent, SWT.NONE);
            SwtFlutterBridge bridge = new SwtFlutterBridge();
            bridge.initFlutterView(parentComposite.getApi().view.id, dartControl);
            return bridge;
        }
        return null;
    }

    void initFlutterView(long parent, DartControl control) {
        super.onReady(control);
        context = InitializeFlutterWindow(client.getPort(), parent, control.hashCode(), widgetName(control));
    }

    static native long InitializeFlutterWindow(int port, long parentHandle, long widgetId, String widgetName);

}
