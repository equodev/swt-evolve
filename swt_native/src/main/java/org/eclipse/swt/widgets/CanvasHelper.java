package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Rectangle;

/** What a Canvas does that its generated implementation cannot say by itself. */
public final class CanvasHelper {

    private CanvasHelper() {
    }

    /**
     * Whether Canvas.scroll goes on past its argument checks on the given platform's SWT: Cocoa needs some of
     * the canvas on screen (its view's visible rectangle), GTK needs the canvas visible, Win32 always goes on.
     */
    public static boolean scrolls(Control canvas, String platform) {
        if ("cocoa".equals(platform))
            return hasVisibleArea(canvas);
        if ("gtk".equals(platform))
            return canvas.isVisible();
        return true;
    }

    /**
     * Whether a Cocoa view would have a visible rectangle: the control is visible in a shell on screen, and some
     * of it is left once every ancestor up to the shell has clipped it to its client area.
     */
    static boolean hasVisibleArea(Control control) {
        if (!control.isVisible())
            return false;
        Rectangle visible = control.getBounds();
        for (Control c = control; !visible.isEmpty() && !(c instanceof Shell); c = c.getParent()) {
            Composite parent = c.getParent();
            Rectangle client = parent.getClientArea();
            visible = visible.intersection(new Rectangle(0, 0, client.width, client.height));
            Rectangle at = parent.getBounds();
            visible.x += at.x;
            visible.y += at.y;
        }
        return !visible.isEmpty();
    }
}
