package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;

/** The size rules of a {@link Shell} that are kept out of the generated implementation. */
public class ShellHelper {

    private ShellHelper() {
    }

    /** A minimum or maximum size as the caller may keep it: a copy, and {@code unset} on both axes while none was given. */
    static Point sizeLimit(Point limit, int unset) {
        return limit == null ? new Point(unset, unset) : new Point(limit.x, limit.y);
    }

    /** A shell that is smaller than its new minimum size grows to it, keeping its location. */
    static void growToMinimum(Shell shell) {
        Point size = shell.getSize();
        Point min = shell.getMinimumSize();
        if (min.x > size.x || min.y > size.y)
            shell.setSize(Math.max(min.x, size.x), Math.max(min.y, size.y));
    }

    /** A shell that is larger than its new maximum size shrinks to it, keeping its location. */
    static void shrinkToMaximum(Shell shell) {
        Point size = shell.getSize();
        Point max = shell.getMaximumSize();
        if (max.x < size.x || max.y < size.y)
            shell.setSize(Math.min(max.x, size.x), Math.min(max.y, size.y));
    }

    /** A size asked for is raised to the minimum size on each axis. */
    static Point atLeastMinimum(Shell shell, int width, int height) {
        Point min = shell.getMinimumSize();
        return new Point(Math.max(width, min.x), Math.max(height, min.y));
    }

    /** A shell shaped by a region is as big as the region reaches. */
    static void fitToRegion(Shell shell, Region region) {
        if (region == null)
            return;
        Rectangle bounds = region.getBounds();
        shell.setSize(bounds.x + bounds.width, bounds.y + bounds.height);
    }
}
