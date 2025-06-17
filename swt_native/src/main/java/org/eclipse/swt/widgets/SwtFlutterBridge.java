package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterLibraryLoader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.cocoa.NSView;
import org.eclipse.swt.internal.cocoa.OS;

import java.nio.file.Paths;
import java.util.Arrays;

public class SwtFlutterBridge extends FlutterBridge {
    private long context;

    static {
        FlutterLibraryLoader.initialize();
    }

    public static SwtFlutterBridge of(DartWidget widget) {
        if (widget instanceof DartControl dartControl && dartControl.parent.getImpl() instanceof SwtComposite) {
//            SwtComposite parentComposite = new SwtComposite(dartControl.parent, SWT.NONE, null);
            SwtFlutterBridge bridge = new SwtFlutterBridge();
            // TODO abstract view.id in getHandle()
//            bridge.initFlutterView(parentComposite.getApi().view.id, dartControl);
            bridge.initFlutterView(dartControl.parent, dartControl);
            return bridge;
        }
        if (widget instanceof DartControl dartControl && dartControl.parent.getImpl() instanceof DartComposite c) {
            Control[] newArray = c.children != null ? Arrays.copyOf(c.children, c.children.length + 1) : new Control[1];
            newArray[newArray.length - 1] = dartControl.getApi();
            c.children = newArray;
            FlutterBridge bridge = c.getBridge();
            bridge.dirty(c);
            return (SwtFlutterBridge) bridge;
        }
        return null;
    }

    void initFlutterView(Composite parent, DartControl control) {
        super.onReady(control);
        context = InitializeFlutterWindow(client.getPort(), parent.view.id, control.hashCode(), widgetName(control));
        long view = GetView(context);
        control.getApi().view = new NSView(view);
        control.jniRef = OS.NewGlobalRef(control.getApi());
        if (control.jniRef == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
        ((SwtDisplay) control.display.getImpl()).addWidget(control.getApi().view, control.getApi());
    }

    @Override
    public void destroy(DartWidget control) {
        if (control instanceof DartControl dartControl) {
            ((SwtDisplay) control.display.getImpl()).removeWidget(dartControl.getApi().view);
            if (dartControl.jniRef != 0) {
                OS.DeleteGlobalRef(dartControl.jniRef);
            }
            control.jniRef = 0;
            dartControl.getApi().view = null;
            Dispose(context);
            context = 0;
        }
    }

    @Override
    public void setBounds(DartControl dartControl, Rectangle bounds) {
        if (dartControl.bridge != null) {
//            System.out.println("SET BOUNDS: "+bounds);
            if (dartControl instanceof DartCTabFolder) {
                SetBounds(context, bounds.x, bounds.y, bounds.width, bounds.height,
                        0, 0, bounds.width, 28);
            } else {
                SetBounds(context, bounds.x, bounds.y, bounds.width, bounds.height,
                        bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }

    @Override
    public Object container(DartComposite parent) {
        return parent.getApi().view;
    }

    static native long InitializeFlutterWindow(int port, long parentHandle, long widgetId, String widgetName);

    static native long Dispose(long context);

    static native long SetBounds(long context, int x, int y, int w, int h, int vx, int vy, int vw, int vh);

    static native long GetView(long context);


}

