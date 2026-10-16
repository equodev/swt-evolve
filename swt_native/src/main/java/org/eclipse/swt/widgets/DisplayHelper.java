package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class DisplayHelper {

    static Point mapOrigin(Control from, Control to, int x, int y) {
        int screenX, screenY;
        if (from != null) {
            Point screenPt = from.toDisplay(x, y);
            screenX = screenPt.x;
            screenY = screenPt.y;
        } else {
            screenX = x;
            screenY = y;
        }
        // The control's own inverse, not a subtraction of its origin: toDisplay may scale (a panel
        // of the main workbench area), and only toControl undoes that.
        return to != null ? to.toControl(screenX, screenY) : new Point(screenX, screenY);
    }

    /** Both corners, so the size scales with the origin wherever the mapping does. */
    static Rectangle mapRectangle(Control from, Control to, int x, int y, int width, int height) {
        Point origin = mapOrigin(from, to, x, y);
        Point far = mapOrigin(from, to, x + width, y + height);
        return new Rectangle(origin.x, origin.y, far.x - origin.x, far.y - origin.y);
    }

}
