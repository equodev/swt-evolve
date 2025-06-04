package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterLibraryLoader;
import org.eclipse.swt.SWT;

import java.nio.file.Paths;
import java.util.Arrays;

public class SwtFlutterBridge extends FlutterBridge {
    private long context;

    static {
        FlutterLibraryLoader.initialize();
    }

    public static SwtFlutterBridge of(DartWidget widget) {
        if (widget instanceof DartControl dartControl && dartControl.parent.getImpl() instanceof SwtComposite) {
            SwtComposite parentComposite = new SwtComposite(dartControl.parent, SWT.NONE);
            SwtFlutterBridge bridge = new SwtFlutterBridge();
            // TODO abstract view.id in getHandle()
            bridge.initFlutterView(parentComposite.getApi().view.id, dartControl);
            return bridge;
        }
        if (widget instanceof DartControl dartControl && dartControl.parent.getImpl() instanceof DartComposite c) {
            Control[] newArray = c.children != null ? Arrays.copyOf(c.children, c.children.length + 1) : new Control[1];
            newArray[newArray.length - 1] = dartControl.getApi();
            c.children = newArray;
        }
        return null;
    }

    void initFlutterView(long parent, DartControl control) {
        super.onReady(control);
        context = InitializeFlutterWindow(client.getPort(), parent, control.hashCode(), widgetName(control));
    }

    static native long InitializeFlutterWindow(int port, long parentHandle, long widgetId, String widgetName);

}
