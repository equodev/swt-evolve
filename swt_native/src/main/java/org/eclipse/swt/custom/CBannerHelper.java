package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * What {@code DartCBanner} computes for Flutter: the lines {@code CBanner.onPaint} draws, sent for
 * Flutter to draw them, and whether a hidden banner can lay its children out.
 */
public final class CBannerHelper {

    private CBannerHelper() {
    }

    /**
     * The points onPaint joins for the curve between the left and the right control, or null when
     * there is no curve to draw.
     */
    public static int[] curveLine(Control left, Control right, int curveStart, int[] curve, int curveWidth,
            Point size, int borderStripe) {
        if (left == null || right == null) {
            return null;
        }
        int[] line = new int[curve.length + 6];
        int index = 0;
        line[index++] = curveStart + 1;
        line[index++] = size.y - borderStripe;
        for (int i = 0; i < curve.length / 2; i++) {
            line[index++] = curveStart + curve[2 * i];
            line[index++] = curve[2 * i + 1];
        }
        line[index++] = curveStart + curveWidth;
        line[index++] = 0;
        line[index++] = size.x;
        line[index++] = 0;
        return line;
    }

    /**
     * Whether a hidden banner can lay its children out. Its client area is empty by contract and its
     * layout spreads the children over the whole banner, so it asks its size rather than its client
     * area; a shown one is laid out by {@code DartComposite}'s rule.
     */
    public static boolean hasRoomToLayOut(Point size) {
        return size.x > 0 || size.y > 0;
    }

    /** The row of the line onPaint draws above the bottom control, or -1 when it draws none. */
    public static int bottomLine(Control left, Control right, Control bottom, int borderStripe) {
        if (bottom == null || bottom.isDisposed() || (left == null && right == null)) {
            return -1;
        }
        return bottom.getBounds().y - borderStripe - 1;
    }
}
