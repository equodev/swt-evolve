package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The control under the pointer as the application hears of it, kept in {@link DartDisplay#currentControl}. The
 * client reports the crossings the pointer makes. A control that Java moves or resizes off a resting pointer is
 * left here: natively its MouseExit follows the listeners of the event that moved it (a TableCursor's Selection) and
 * comes ahead of the next one (that key's KeyUp), whereas the client sees the change only on its next frame. The
 * MouseExit the client reports for it then is dropped.
 */
final class PointerCrossingHelper {

    /** The controls left here whose MouseExit the client has yet to report. */
    private static final String LEFT_BY_JAVA = "dev.equo.swt.pointer.leftByJava";

    /** Set while the client's latest pointer move had a button held: the pressed control keeps the pointer. */
    private static final String BUTTON_HELD = "dev.equo.swt.pointer.buttonHeld";

    private PointerCrossingHelper() {
    }

    static void moved(DartControl control, Event e) {
        ((DartDisplay) control.display.getImpl()).cursorLocation = control.toDisplay(e.x, e.y);
        control.display.setData(BUTTON_HELD, (e.stateMask & SWT.BUTTON_MASK) != 0 ? Boolean.TRUE : null);
    }

    static void entered(DartControl control, Event e) {
        Set<Control> left = leftByJava(control.display, false);
        if (left != null)
            left.remove(control.getApi());
        ((DartDisplay) control.display.getImpl()).currentControl = control.getApi();
        control.sendEvent(SWT.MouseEnter, e);
    }

    static void exited(DartControl control, Event e) {
        Set<Control> left = leftByJava(control.display, false);
        if (left != null && left.remove(control.getApi()))
            return;
        DartDisplay display = (DartDisplay) control.display.getImpl();
        if (display.currentControl == control.getApi())
            display.currentControl = null;
        control.sendEvent(SWT.MouseExit, e);
    }

    /** {@code control}'s bounds changed: the control under the pointer is left if it, or {@code control} with it, moved off the pointer. */
    static void boundsChanged(DartControl control) {
        Display display = control.display;
        DartDisplay impl = (DartDisplay) display.getImpl();
        Control current = impl.currentControl;
        Control moved = control.getApi();
        if (current == null || current.isDisposed() || moved instanceof Shell || display.getData(BUTTON_HELD) != null)
            return;
        if (!contains(moved, current) || !(current.getImpl() instanceof DartWidget))
            return;
        Point pointer = display.getCursorLocation();
        if (isUnder(current, pointer))
            return;
        impl.currentControl = null;
        leftByJava(display, true).add(current);
        Point local = current.toControl(pointer);
        Event exit = new Event();
        exit.x = local.x;
        exit.y = local.y;
        ((DartWidget) current.getImpl()).postEvent(SWT.MouseExit, exit);
    }

    private static boolean contains(Control ancestor, Control control) {
        for (Control c = control; c != null; c = c.getParent()) {
            if (c == ancestor)
                return true;
        }
        return false;
    }

    /** Whether {@code pointer}, in display coordinates, is within {@code control} and each of its parents below the Shell. */
    private static boolean isUnder(Control control, Point pointer) {
        for (Control c = control; c != null && !(c instanceof Shell); c = c.getParent()) {
            Rectangle bounds = c.getBounds();
            Point origin = c.getParent().toDisplay(bounds.x, bounds.y);
            if (!new Rectangle(origin.x, origin.y, bounds.width, bounds.height).contains(pointer))
                return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static Set<Control> leftByJava(Display display, boolean create) {
        Object set = display.getData(LEFT_BY_JAVA);
        if (set == null && create) {
            set = Collections.newSetFromMap(new WeakHashMap<Control, Boolean>());
            display.setData(LEFT_BY_JAVA, set);
        }
        return (Set<Control>) set;
    }
}
