package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

/**
 * What a {@link DartTracker}'s loop does with what the client reports: the pointer moves or resizes the
 * rectangles, a key steps, ends or cancels the gesture, and the Shell the band is drawn on is found.
 */
final class TrackerHelper {

    private TrackerHelper() {
    }

    /**
     * The Shell the band is announced on: the parent's Shell, which subscribes to the Tracker channel and converts
     * a pointer position into display coordinates. A Tracker opened on a plain Composite would otherwise announce
     * itself on a channel nothing listens to, and the loop would never see the pointer.
     */
    static DartWidget host(Control parent, Display display) {
        if (parent == null)
            return displayHost(display);
        if (parent.isDisposed())
            return null;
        Shell shell = parent.getShell();
        if (shell != null && !shell.isDisposed() && shell.getImpl() instanceof DartWidget)
            return (DartWidget) shell.getImpl();
        return parent.getImpl() instanceof DartWidget ? (DartWidget) parent.getImpl() : null;
    }

    /**
     * A Tracker on the display follows the pointer over the whole screen upstream; here the pointer is only seen
     * over a Shell, so it follows it over the one the user works in.
     */
    private static DartWidget displayHost(Display display) {
        Shell active = display.getActiveShell();
        if (active != null && active.isVisible() && active.getImpl() instanceof DartWidget)
            return (DartWidget) active.getImpl();
        for (Shell shell : display.getShells()) {
            if (shell.isVisible() && shell.getImpl() instanceof DartWidget)
                return (DartWidget) shell.getImpl();
        }
        return null;
    }

    /**
     * Upstream's mouse() without the drawing: the rectangles follow the pointer, moved or resized as the style
     * says, before the listeners hear Move or Resize. The client cannot put the pointer on the rectangles as
     * upstream does when open() starts without a button down, so they follow it from the first position it
     * reports. It samples the pointer and repeats a position for a drop target to take it, so a position that did
     * not change is still announced, without touching the rectangles.
     */
    static void mouse(DartTracker tracker, Event e) {
        int newX = e.x, newY = e.y;
        if (arrivedAtWarp(tracker, newX, newY))
            return;
        if (!tracker.pointerSeen) {
            tracker.pointerSeen = true;
            tracker.oldX = newX;
            tracker.oldY = newY;
        }
        boolean moved = newX != tracker.oldX || newY != tracker.oldY;
        Event event = new Event();
        event.x = newX;
        event.y = newY;
        if (resizes(tracker)) {
            boolean orientationInit = moved && tracker.resizeRectangles(newX - tracker.oldX, newY - tracker.oldY);
            if (!notifyBand(tracker, SWT.Resize, event))
                return;
            Point cursorPos = moved ? tracker.adjustResizeCursor(orientationInit) : null;
            if (cursorPos != null && orientationInit)
                tracker.warps.add(cursorPos);
            if (cursorPos != null) {
                newX = cursorPos.x;
                newY = cursorPos.y;
            }
        } else {
            tracker.moveRectangles(newX - tracker.oldX, newY - tracker.oldY);
            if (!notifyBand(tracker, SWT.Move, event))
                return;
        }
        tracker.oldX = newX;
        tracker.oldY = newY;
    }

    /** What says, while the loop runs, that the pointer's release ends it rather than reaching a control. */
    static java.util.function.BooleanSupplier ownsRelease(DartTracker tracker) {
        return () -> tracker.tracking;
    }

    /**
     * Upstream's key(): the Tracker's listeners hear the key, and unless one of them cleared doit, its press
     * steps, ends or cancels the gesture, as on Win32 and GTK (Cocoa acts on its release as well). Answers whether
     * the Tracker took the key.
     */
    static boolean key(DartTracker tracker, int type, Event event) {
        if (!tracker.tracking || tracker.isDisposed())
            return false;
        event.doit = true;
        if (tracker.sendKeyEvent(type, event) && type != SWT.KeyUp)
            step(tracker, event);
        return true;
    }

    /** Esc cancels, Return ends, and an arrow moves or resizes the rectangles by a step. */
    private static void step(DartTracker tracker, Event event) {
        int stepSize = (event.stateMask & SWT.CTRL) != 0 ? DartTracker.STEPSIZE_SMALL : DartTracker.STEPSIZE_LARGE;
        int xChange = 0, yChange = 0;
        switch (event.keyCode) {
            case SWT.ESC:
                tracker.cancelled = true;
                tracker.tracking = false;
                break;
            case SWT.KEYPAD_CR:
            case SWT.CR:
                tracker.tracking = false;
                break;
            case SWT.ARROW_LEFT:
                xChange = -stepSize;
                break;
            case SWT.ARROW_RIGHT:
                xChange = stepSize;
                break;
            case SWT.ARROW_UP:
                yChange = -stepSize;
                break;
            case SWT.ARROW_DOWN:
                yChange = stepSize;
                break;
        }
        if (xChange == 0 && yChange == 0)
            return;
        Event moved = new Event();
        moved.x = tracker.oldX + xChange;
        moved.y = tracker.oldY + yChange;
        Point cursorPos;
        if (resizes(tracker)) {
            tracker.resizeRectangles(xChange, yChange);
            if (!notifyBand(tracker, SWT.Resize, moved))
                return;
            cursorPos = tracker.adjustResizeCursor(true);
        } else {
            tracker.moveRectangles(xChange, yChange);
            if (!notifyBand(tracker, SWT.Move, moved))
                return;
            cursorPos = tracker.adjustMoveCursor();
        }
        if (cursorPos != null) {
            tracker.warps.add(cursorPos);
            tracker.oldX = cursorPos.x;
            tracker.oldY = cursorPos.y;
        }
    }

    /**
     * Whether the client reports the pointer where the loop itself put it. The client reports the warp some time
     * after it, possibly after a later key step warped it again, so it is the warp arriving, not the user moving
     * the pointer: upstream sees no change of position there. The warps before it have arrived too; any other
     * position is the user's and forgets them all. The loop has put the pointer at oldX and oldY, so the pointer
     * counts as seen from there.
     */
    private static boolean arrivedAtWarp(DartTracker tracker, int x, int y) {
        for (int i = tracker.warps.size() - 1; i >= 0; i--) {
            Point warp = tracker.warps.get(i);
            if (warp.x == x && warp.y == y) {
                tracker.warps.subList(0, i).clear();
                tracker.pointerSeen = true;
                return true;
            }
        }
        tracker.warps.clear();
        return false;
    }

    private static boolean resizes(DartTracker tracker) {
        return (tracker.getApi().style & SWT.RESIZE) != 0;
    }

    /**
     * Tells the listeners the rectangles moved or were resized. Answers false once one of them disposed the
     * Tracker, which then counts as cancelled.
     */
    private static boolean notifyBand(DartTracker tracker, int type, Event event) {
        tracker.inEvent = true;
        tracker.sendEvent(type, event);
        tracker.inEvent = false;
        if (!tracker.isDisposed())
            return true;
        tracker.cancelled = true;
        return false;
    }
}
