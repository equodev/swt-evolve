package org.eclipse.swt.widgets;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterLibraryLoader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.Platform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

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
        if (widget instanceof DartStyledText){
            StyledTextBridge.hookevents(widget);
        }
        if (widget instanceof DartControl dartControl && dartControl.parent.getImpl() instanceof SwtComposite) {
//            SwtComposite parentComposite = new SwtComposite(dartControl.parent, SWT.NONE, null);
            SwtFlutterBridge bridge = new SwtFlutterBridge(widget);
            widget.bridge = bridge;
            bridge.initFlutterView(dartControl.parent, dartControl);
            if (widget instanceof DartCTabFolder t) { // workaround
                if (!Platform.PLATFORM.equals("win32")) {
                    t.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
                        Rectangle bounds = t.getBounds();
                        bounds.height = bounds.height + 1;
                        bridge.setBounds(t, bounds);
                    }));
                }
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
            destroyHandle(dartControl);
            Dispose(context);
            context = 0;
        }
    }

    protected abstract void destroyHandle(DartControl dartControl);

    @Override
    public void setBounds(DartControl dartControl, Rectangle bounds) {
        if (dartControl.bridge != null && forWidget == dartControl) {
            System.out.println("SET BOUNDS: " + dartControl + " " + bounds);
            if (dartControl instanceof DartCTabFolder) {
                SetBounds(context, bounds.x, bounds.y, bounds.width, bounds.height,
                        0, 0, bounds.width, 28);
            } else {
                SetBounds(context, bounds.x, bounds.y, bounds.width, bounds.height,
                        bounds.x, bounds.y, bounds.width, bounds.height);
            }
//            dartControl.resized();
            dartControl.sendEvent(SWT.Resize);
            if (dartControl instanceof DartComposite c && c.layout != null) {
                c.markLayout(false, false);
                c.updateLayout(false);
            }
        }
    }

    @Override
    public abstract Object container(DartComposite parent);

    static native long InitializeFlutterWindow(int port, long parentHandle, long widgetId, String widgetName);

    static native long Dispose(long context);

    static native long SetBounds(long context, int x, int y, int w, int h, int vx, int vy, int vw, int vh);

    static native long GetView(long context);


}

