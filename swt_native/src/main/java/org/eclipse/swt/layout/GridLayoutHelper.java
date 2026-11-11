package org.eclipse.swt.layout;

import org.eclipse.swt.widgets.Control;

public final class GridLayoutHelper {

    private GridLayoutHelper() {
    }

    /**
     * The empty grid that replaces {@code grid} once {@code lastRow} no longer fits: it doubles, so
     * laying out n children grows it O(log n) times, and its rows stay null until a child lands in
     * one (eclipse-platform/eclipse.platform.swt#3642). The caller copies the old rows in.
     */
    public static Control[][] grownGrid(Control[][] grid, int lastRow) {
        return new Control[Math.max(lastRow + 4, grid.length * 2)][];
    }
}
