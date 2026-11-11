package org.eclipse.swt.widgets;

public class CanvasHelper {

    private CanvasHelper() {
    }

    /**
     * Canvas.scroll's repaint. The client cannot copy pixels it already shows, so every scroll takes
     * the path native SWT takes when it cannot copy either: it damages the source area and the
     * destination area, and the Paint draws both at the new origin.
     */
    public static void scrolled(DartCanvas canvas, int destX, int destY, int x, int y, int width, int height) {
        ControlHelper.markDamaged(canvas, x, y, width, height);
        ControlHelper.markDamaged(canvas, destX, destY, width, height);
    }
}
