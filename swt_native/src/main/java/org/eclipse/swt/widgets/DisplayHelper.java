package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Point;

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
        if (to != null) {
            Point toOrigin = to.toDisplay(0, 0);
            return new Point(screenX - toOrigin.x, screenY - toOrigin.y);
        }
        return new Point(screenX, screenY);
    }

}