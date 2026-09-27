package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/** Input the Display synthesizes itself: {@link Display#post} and the control under the pointer. */
public final class DisplayInput {

    /** Where posted moves put the pointer; setCursorLocation posts a MouseMove itself, so it cannot. */
    private static final java.util.Map<Display, Point> postedPointer =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    private DisplayInput() {
    }

    /** The deepest visible control at a point in display coordinates, topmost shell first. */
    public static Control controlAt(Display display, int x, int y) {
        Shell active = display.getActiveShell();
        if (active != null && contains(display, active, x, y))
            return deepestAt(display, active, x, y);
        Shell[] shells = display.getShells();
        for (int i = shells.length - 1; i >= 0; i--) {
            if (contains(display, shells[i], x, y))
                return deepestAt(display, shells[i], x, y);
        }
        return null;
    }

    private static Control deepestAt(Display display, Control control, int x, int y) {
        if (control instanceof Composite) {
            // Children come topmost first.
            for (Control child : ((Composite) control).getChildren()) {
                if (contains(display, child, x, y))
                    return deepestAt(display, child, x, y);
            }
        }
        return control;
    }

    private static boolean contains(Display display, Control control, int x, int y) {
        if (control.isDisposed() || !control.isVisible())
            return false;
        Rectangle bounds = control instanceof Shell ? control.getBounds()
                : display.map(control.getParent(), null, control.getBounds());
        return bounds.contains(x, y);
    }

    /** Queues a synthetic event where the platform would deliver one; false for what cannot be posted. */
    public static boolean post(Display display, Event event) {
        switch (event.type) {
            case SWT.KeyDown:
            case SWT.KeyUp:
                break;
            case SWT.MouseDown:
            case SWT.MouseUp:
                if (event.button < 1 || event.button > 5)
                    return false;
                break;
            case SWT.MouseMove:
            case SWT.MouseWheel:
                break;
            default:
                return false;
        }
        Event posted = copy(event);
        display.asyncExec(() -> deliver(display, posted));
        return true;
    }

    private static void deliver(Display display, Event event) {
        if (display.isDisposed())
            return;
        if (event.type == SWT.KeyDown || event.type == SWT.KeyUp) {
            Control focus = display.getFocusControl();
            if (focus == null || focus.isDisposed() || !(focus.getImpl() instanceof DartControl))
                return;
            DartControl target = (DartControl) focus.getImpl();
            if (event.type == SWT.KeyDown)
                ControlHelper.routeKeyDown(target, event);
            else
                target.sendEvent(SWT.KeyUp, event);
            return;
        }
        if (event.type == SWT.MouseMove)
            postedPointer.put(display, new Point(event.x, event.y));
        Point cursor = postedPointer.get(display);
        if (cursor == null)
            cursor = display.getCursorLocation();
        Control control = controlAt(display, cursor.x, cursor.y);
        if (control == null || !(control.getImpl() instanceof DartControl))
            return;
        Point local = control.toControl(cursor);
        event.x = local.x;
        event.y = local.y;
        ((DartControl) control.getImpl()).sendEvent(event.type, event);
    }

    private static Event copy(Event event) {
        Event e = new Event();
        e.type = event.type;
        e.keyCode = event.keyCode;
        e.character = event.character;
        e.keyLocation = event.keyLocation;
        e.stateMask = event.stateMask;
        e.button = event.button;
        e.count = event.count;
        e.x = event.x;
        e.y = event.y;
        e.detail = event.detail;
        e.doit = true;
        return e;
    }
}
