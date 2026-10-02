package dev.equo.swt.size;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;

/**
 * How a CoolBar sizes itself: SWT's rows of items, inside the frame the render side draws around
 * them. The frame is measured, per theme, into {@link CoolBarTheme}; the row layout is SWT's own.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure_coolbar.dart
 */
public class CoolBarSizes {

    /** What a CoolItem adds to its control on the layout axis: SWT's gripper plus its margins. */
    public static final int ITEM_TRIM = 10;

    /** The gap SWT's row layout leaves between two rows of a bar that is not SWT.FLAT. */
    public static final int ROW_SPACING = 2;

    /**
     * SWT's emulated CoolBar.computeSize: rows break at the explicit wrap indices and wherever the
     * next item's minimum width would overflow the hint; a row is as long as its items' preferred
     * widths and as thick as its thickest item. The rows the bar is currently wrapped into for its
     * present size are deliberately ignored: a bar squeezed onto several rows must still report its
     * natural size, or its parent keeps it squeezed.
     */
    public static Point computeSize(CoolBar bar, int wHint, int hHint) {
        boolean vertical = (bar.getStyle() & SWT.VERTICAL) != 0;
        int rowSpacing = (bar.getStyle() & SWT.FLAT) != 0 ? 0 : ROW_SPACING;
        int maxLength = vertical ? hHint : wHint;
        int[] order = bar.getItemOrder();
        java.util.Set<Integer> wraps = new java.util.HashSet<>();
        for (int index : bar.getWrapIndices()) wraps.add(index);

        int length = 0, thickness = 0, rows = 0;
        int rowLength = 0, rowMinLength = 0, rowThickness = 0;
        for (int i = 0; i < order.length; i++) {
            CoolItem item = bar.getItem(order[i]);
            Point preferred = item.getPreferredSize();
            Point minimum = item.getMinimumSize();
            int itemLength = vertical ? preferred.y : preferred.x;
            int itemThickness = vertical ? preferred.x : preferred.y;
            int itemMinLength = (vertical ? minimum.y : minimum.x) + ITEM_TRIM;
            boolean overflows = maxLength != SWT.DEFAULT && rowMinLength + itemMinLength > maxLength;
            if (i > 0 && (wraps.contains(i) || overflows)) {
                length = Math.max(length, rowLength);
                thickness += rowThickness + (rows > 0 ? rowSpacing : 0);
                rows++;
                rowLength = rowMinLength = rowThickness = 0;
            }
            rowLength += itemLength;
            rowMinLength += itemMinLength;
            rowThickness = Math.max(rowThickness, itemThickness);
        }
        if (order.length > 0) {
            length = Math.max(length, rowLength);
            thickness += rowThickness + (rows > 0 ? rowSpacing : 0);
        }

        int width = vertical ? thickness : length;
        int height = vertical ? length : thickness;
        if (wHint != SWT.DEFAULT) width = wHint;
        if (hHint != SWT.DEFAULT) height = hHint;
        Rectangle trim = computeTrim(0, 0, width, height);
        return new Point(trim.width, trim.height);
    }

    /** The bar's bounds around a client area, with the frame the render side draws added back. */
    public static Rectangle computeTrim(int x, int y, int width, int height) {
        CoolBarTheme frame = CoolBarTheme.get();
        return new Rectangle(x - frame.frameLeft(), y - frame.frameTop(),
                width + frame.frameLeft() + frame.frameRight(),
                height + frame.frameTop() + frame.frameBottom());
    }

    /** What the frame the render side draws leaves of the bar's bounds for its items. */
    public static Rectangle getClientArea(Rectangle bounds) {
        CoolBarTheme frame = CoolBarTheme.get();
        return new Rectangle(frame.frameLeft(), frame.frameTop(),
                Math.max(0, bounds.width - frame.frameLeft() - frame.frameRight()),
                Math.max(0, bounds.height - frame.frameTop() - frame.frameBottom()));
    }
}
