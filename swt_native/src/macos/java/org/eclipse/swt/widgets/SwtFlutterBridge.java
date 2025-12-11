package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.cocoa.NSView;
import org.eclipse.swt.internal.cocoa.NSWindow;
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
    public void setFocus(DartControl widget) {
        NSView focusView = widget.getApi().view;
//        if (!focusView.canBecomeKeyView())
//            return;
        boolean rest = forceFocus(widget, focusView);
    }

    @Override
    public boolean hasFocus(DartControl widget) {
        return widget.display.getFocusControl() == widget.getApi();
    }

    boolean forceFocus(DartControl widget, NSView focusView) {
        if (focusView == null) {
            return false;
        }
        NSWindow window = focusView.window();
        if (window == null) {
            return false;
        }
        return window.makeFirstResponder(focusView);
    }

    @Override
    protected void destroyHandle(DartControl control) {
        ((SwtDisplay) control.display.getImpl()).removeWidget(control.getApi().view);
        if (control.jniRef != 0)
            OS.DeleteGlobalRef(control.jniRef);
        control.jniRef = 0;
        control.getApi().view = null;
    }

    @Override
    public void reparent(DartControl dartControl, Composite parent) {
        NSView topView = dartControl.getApi().view;
        if (topView != null) {
            topView.retain();
            topView.removeFromSuperview();
            if (!(parent.getImpl() instanceof DartComposite) && parent.getImpl() instanceof SwtControl) {
                ((SwtControl) parent.getImpl()).contentView().addSubview(topView, OS.NSWindowBelow, null);
            }
            topView.release();
        }
    }
}
