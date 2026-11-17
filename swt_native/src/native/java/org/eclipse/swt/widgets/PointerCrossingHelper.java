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
 * left here, and one it moves under the pointer inside the control there is entered: natively those crossings
 * follow the listeners of the event that moved it (a TableCursor's Selection) and come ahead of the next one (that
 * key's KeyUp), whereas the client sees the change only on its next frame. The crossings the client reports for
 * them then are dropped.
 */
final class PointerCrossingHelper {

    /** The controls left here whose MouseExit the client has yet to report. */
    private static final String LEFT_BY_JAVA = "dev.equo.swt.pointer.leftByJava";

    /** The controls entered here whose MouseEnter the client has yet to report. */
    private static final String ENTERED_BY_JAVA = "dev.equo.swt.pointer.enteredByJava";

    /** Set while a button is held, by a press or the client's latest pointer move: the pressed control keeps the pointer. */
    private static final String BUTTON_HELD = "dev.equo.swt.pointer.buttonHeld";

    private PointerCrossingHelper() {
    }

    static void moved(DartControl control, Event e) {
        ((DartDisplay) control.display.getImpl()).cursorLocation = control.toDisplay(e.x, e.y);
        control.display.setData(BUTTON_HELD, (e.stateMask & SWT.BUTTON_MASK) != 0 ? Boolean.TRUE : null);
    }

    static void pressed(Display display) {
        display.setData(BUTTON_HELD, Boolean.TRUE);
    }

    static void released(Display display) {
        display.setData(BUTTON_HELD, null);
    }

    static void entered(DartControl control, Event e) {
        Control api = control.getApi();
        DartDisplay display = (DartDisplay) control.display.getImpl();
        remove(control.display, LEFT_BY_JAVA, api);
        if (remove(control.display, ENTERED_BY_JAVA, api) && display.currentControl == api)
            return;
        display.currentControl = api;
        control.sendEvent(SWT.MouseEnter, e);
    }

    static void exited(DartControl control, Event e) {
        Control api = control.getApi();
        DartDisplay display = (DartDisplay) control.display.getImpl();
        remove(control.display, ENTERED_BY_JAVA, api);
        if (remove(control.display, LEFT_BY_JAVA, api) && display.currentControl != api)
            return;
        if (display.currentControl == api)
            display.currentControl = null;
        control.sendEvent(SWT.MouseExit, e);
    }

    /**
     * {@code control}'s bounds changed: the control under the pointer is left if it, or {@code control} with it, moved
     * off the pointer, and the parent the pointer is then on entered; {@code control} is entered if it moved under the
     * pointer inside the parent the pointer is on.
     */
    static void boundsChanged(DartControl control) {
        Display display = control.display;
        Control current = ((DartDisplay) display.getImpl()).currentControl;
        Control moved = control.getApi();
        if (current == null || current.isDisposed() || moved instanceof Shell || display.getData(BUTTON_HELD) != null)
            return;
        if (!(current.getImpl() instanceof DartWidget))
            return;
        Point pointer = display.getCursorLocation();
        Composite parent = moved.getParent();
        if (contains(moved, current)) {
            if (isUnder(current, pointer))
                return;
            cross(display, current, parent instanceof Shell || under(parent, pointer) != parent ? null : parent, pointer);
        } else if (current == parent && !(parent instanceof Shell) && moved.getVisible() && isUnder(moved, pointer)) {
            cross(display, current, under(moved, pointer), pointer);
        }
    }

    /** Posts {@code from}'s MouseExit, then {@code to}'s MouseEnter if there is one, behind the event being sent. */
    private static void cross(Display display, Control from, Control to, Point pointer) {
        if (to != null && !(to.getImpl() instanceof DartWidget))
            to = null;
        ((DartDisplay) display.getImpl()).currentControl = to;
        if (!remove(display, ENTERED_BY_JAVA, from))
            set(display, LEFT_BY_JAVA).add(from);
        post(from, SWT.MouseExit, pointer);
        if (to != null) {
            if (!remove(display, LEFT_BY_JAVA, to))
                set(display, ENTERED_BY_JAVA).add(to);
            post(to, SWT.MouseEnter, pointer);
        }
    }

    private static void post(Control control, int type, Point pointer) {
        Point local = control.toControl(pointer);
        Event event = new Event();
        event.x = local.x;
        event.y = local.y;
        ((DartWidget) control.getImpl()).postEvent(type, event);
    }

    /** The topmost visible control under {@code pointer} inside {@code control}, or {@code control} itself. */
    private static Control under(Control control, Point pointer) {
        if (control instanceof Composite) {
            for (Control child : ((Composite) control).getChildren()) {
                if (child.getVisible() && isUnder(child, pointer))
                    return under(child, pointer);
            }
        }
        return control;
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

    private static boolean remove(Display display, String key, Control control) {
        Set<Control> controls = controls(display, key);
        return controls != null && controls.remove(control);
    }

    private static Set<Control> set(Display display, String key) {
        Set<Control> controls = controls(display, key);
        if (controls == null) {
            controls = Collections.newSetFromMap(new WeakHashMap<Control, Boolean>());
            display.setData(key, controls);
        }
        return controls;
    }

    @SuppressWarnings("unchecked")
    private static Set<Control> controls(Display display, String key) {
        return (Set<Control>) display.getData(key);
    }
}
