package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;

public class SashHelper {

    private SashHelper() {
    }

    /**
     * A Selection the client reports for a press, a drag or a release. The press's comes ahead of its
     * MouseDown, and natively the press has activated the Sash before either
     * (Shell.setActiveControl on the down); the Selection carries no button, so a press is told by
     * the button it holds. A Dart Sash is always SMOOTH, and native SWT's smooth sash applies its own
     * move after the listeners unless one vetoes it with doit (SwtSash mouseDragged/mouseUp).
     * <p>
     * The client reports where the pointer takes the sash; a listener may put it elsewhere (a
     * SashForm keeps its children's minimum size). The client marks its release with button 1,
     * which no Selection carries; natively a release reports where the listeners last put the
     * sash ({@code lastX}, {@code lastY}), which a press sets to where the sash is.
     */
    public static void selected(DartSash sash, Event e) {
        boolean press = (e.stateMask & SWT.BUTTON_MASK) != 0;
        boolean release = !press && e.button != 0;
        e.button = 0;
        if (press) {
            ControlHelper.sendActivateToAncestors(sash, SWT.MouseDown);
            if (sash.isDisposed())
                return;
            Rectangle at = sash.getBounds();
            sash.lastX = at.x;
            sash.lastY = at.y;
        }
        sash.reportedX = e.x;
        sash.reportedY = e.y;
        if (release) {
            e.x = sash.lastX;
            e.y = sash.lastY;
        }
        sash.sendEvent(SWT.Selection, e);
        if (sash.isDisposed())
            return;
        // The client draws the sash where the drag left it until this answer arrives, even one that
        // moves nothing.
        if (release)
            sash.getValue().markDirty(VControl.BOUNDS);
        if (!e.doit)
            return;
        sash.lastX = e.x;
        sash.lastY = e.y;
        Rectangle bounds = sash.getBounds();
        sash.setBounds(e.x, e.y, bounds.width, bounds.height);
    }

    /**
     * The client places a drag's MouseUp on the sash where the drag last reported it; natively it
     * lies on the sash where the listeners last put it ({@code lastX}, {@code lastY}).
     */
    static void placeRelease(DartSash sash, Event e) {
        if (e.button != 1)
            return;
        e.x += sash.reportedX - sash.lastX;
        e.y += sash.reportedY - sash.lastY;
        sash.reportedX = sash.lastX;
        sash.reportedY = sash.lastY;
    }

    /**
     * An arrow across the focused sash moves it by its page step, or by one pixel with Ctrl, inside
     * its parent, as a Selection the listeners can veto; then the pointer is put on the moved sash.
     * The arrows along it, and any arrow while a button is held, do nothing.
     */
    public static void moveByKey(DartSash sash, Event key) {
        if ((key.stateMask & SWT.BUTTON_MASK) != 0)
            return;
        boolean vertical = (sash.getApi().getStyle() & SWT.VERTICAL) != 0;
        int step = DartSash.keyStep((key.stateMask & SWT.CTRL) != 0);
        switch (key.keyCode) {
            case SWT.ARROW_LEFT:
            case SWT.ARROW_RIGHT:
                if (!vertical)
                    return;
                if (key.keyCode == SWT.ARROW_LEFT)
                    step = -step;
                break;
            case SWT.ARROW_UP:
            case SWT.ARROW_DOWN:
                if (vertical)
                    return;
                if (key.keyCode == SWT.ARROW_UP)
                    step = -step;
                break;
            default:
                return;
        }
        Composite parent = sash.getApi().getParent();
        Rectangle area = parent.getClientArea();
        Rectangle bounds = sash.getBounds();
        int x = bounds.x, y = bounds.y;
        if (vertical)
            x = Math.min(Math.max(0, x + step), area.width - bounds.width);
        else
            y = Math.min(Math.max(0, y + step), area.height - bounds.height);
        if (x == bounds.x && y == bounds.y)
            return;
        Event event = new Event();
        event.setBounds(new Rectangle(x, y, bounds.width, bounds.height));
        event.stateMask = key.stateMask;
        sash.sendEvent(SWT.Selection, event);
        if (sash.isDisposed() || !event.doit)
            return;
        sash.setBounds(event.x, event.y, bounds.width, bounds.height);
        if (sash.isDisposed())
            return;
        sash.lastX = event.x;
        sash.lastY = event.y;
        int pointerX = vertical ? event.x : event.x + bounds.width / 2;
        int pointerY = vertical ? event.y + bounds.height / 2 : event.y;
        sash.getDisplay().setCursorLocation(parent.toDisplay(pointerX, pointerY));
    }
}
