package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterLibraryLoader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;

import java.util.Arrays;

public abstract class SwtFlutterBridgeBase extends FlutterBridge {
    private final DartWidget forWidget;
    private long context;

    static {
        FlutterLibraryLoader.initialize();
    }

    public SwtFlutterBridgeBase(DartWidget widget) {
        this.forWidget = widget;
    }

    public static SwtFlutterBridge of(DartWidget widget) {
        if (widget instanceof DartControl dartControl && dartControl.parent.getImpl() instanceof SwtComposite) {
//            SwtComposite parentComposite = new SwtComposite(dartControl.parent, SWT.NONE, null);
            SwtFlutterBridge bridge = new SwtFlutterBridge(widget);
            bridge.initFlutterView(dartControl.parent, dartControl);
            if (widget instanceof DartCTabFolder t) { // workaround
                t.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
                    Rectangle bounds = t.getBounds();
                    bounds.height = bounds.height + 1;
                    bridge.setBounds(t, bounds);
                }));
            }
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
        context = InitializeFlutterWindow(client.getPort(), getHandle(parent), id(control), widgetName(control));
        long view = GetView(context);
        setHandle(control, view);
    }

    protected abstract long getHandle(Control control);

    protected abstract void setHandle(DartControl control, long view);

    @Override
    public void destroy(DartWidget control) {
        if (control instanceof DartControl dartControl && control == forWidget) {
            destroyHandle(control, dartControl);
            Dispose(context);
            context = 0;
        }
    }

    protected abstract void destroyHandle(DartWidget control, DartControl dartControl);

    @Override
    public void setBounds(DartControl dartControl, Rectangle bounds) {
        if (dartControl.bridge != null && forWidget == dartControl) {
//            System.out.println("SET BOUNDS: "+bounds);
            if (dartControl instanceof DartCTabFolder) {
                SetBounds(context, bounds.x, bounds.y, bounds.width, bounds.height,
                        0, 0, bounds.width, 28);
            } else {
                SetBounds(context, bounds.x, bounds.y, bounds.width, bounds.height,
                        bounds.x, bounds.y, bounds.width, bounds.height);
            }
//            dartControl.resized();
            dartControl.sendEvent(SWT.Resize);
        }
    }

    @Override
    public abstract Object container(DartComposite parent);

    static native long InitializeFlutterWindow(int port, long parentHandle, long widgetId, String widgetName);

    static native long Dispose(long context);

    static native long SetBounds(long context, int x, int y, int w, int h, int vx, int vy, int vw, int vh);

    static native long GetView(long context);


}

