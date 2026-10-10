package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

/**
 * The SWT.Move a native Tree or Table sends its columns when their places change: after a column is
 * resized, to the columns shown after it; after a column is disposed, to the columns that were shown after
 * it; after a reorder, to every column whose left edge moved. Natively each comes from the list view's own
 * notification or geometry, which the transform drops along with the native call it hangs off. A cell
 * editor or a cursor over a cell follows its column through these events.
 */
public final class ColumnHelper {

    private ColumnHelper() {
    }

    /** The left edge of every column, by column index, with the columns shown in {@code order}. */
    public static int[] positions(Tree tree, int[] order) {
        return positions(widths(tree.getColumns()), order);
    }

    /** The left edge of every column, by column index, with the columns shown in {@code order}. */
    public static int[] positions(Table table, int[] order) {
        return positions(widths(table.getColumns()), order);
    }

    /** Tells the columns shown after {@code column} that they moved, as a resize of it does natively. */
    public static void columnResized(Tree tree, TreeColumn column) {
        if (column.isDisposed() || tree == null || tree.isDisposed())
            return;
        sendMovesAfter(tree.getColumns(), tree.getColumnOrder(), tree.indexOf(column));
    }

    /** Tells the columns shown after {@code column} that they moved, as a resize of it does natively. */
    public static void columnResized(Table table, TableColumn column) {
        if (column.isDisposed() || table == null || table.isDisposed())
            return;
        sendMovesAfter(table.getColumns(), table.getColumnOrder(), table.indexOf(column));
    }

    /** Where column {@code index} is shown, or -1. */
    public static int displayIndex(int[] order, int index) {
        for (int i = 0; i < order.length; i++) {
            if (order[i] == index)
                return i;
        }
        return -1;
    }

    /**
     * Tells every column now shown at {@code fromDisplayIndex} or after it that it moved: what a native list
     * sends after the column that stood at that place was disposed.
     */
    public static void columnsShiftedFrom(Tree tree, int fromDisplayIndex) {
        sendMovesFrom(tree.getColumns(), tree.getColumnOrder(), fromDisplayIndex);
    }

    /**
     * Tells every column now shown at {@code fromDisplayIndex} or after it that it moved: what a native list
     * sends after the column that stood at that place was disposed.
     */
    public static void columnsShiftedFrom(Table table, int fromDisplayIndex) {
        sendMovesFrom(table.getColumns(), table.getColumnOrder(), fromDisplayIndex);
    }

    static int[] positions(int[] widths, int[] order) {
        int[] x = new int[widths.length];
        int left = 0;
        for (int index : order) {
            if (index < 0 || index >= widths.length)
                continue;
            x[index] = left;
            left += widths[index];
        }
        return x;
    }

    private static int[] widths(TreeColumn[] columns) {
        int[] widths = new int[columns.length];
        for (int i = 0; i < columns.length; i++)
            widths[i] = columns[i].getWidth();
        return widths;
    }

    private static int[] widths(TableColumn[] columns) {
        int[] widths = new int[columns.length];
        for (int i = 0; i < columns.length; i++)
            widths[i] = columns[i].getWidth();
        return widths;
    }

    private static void sendMovesAfter(Item[] columns, int[] order, int index) {
        int position = displayIndex(order, index);
        if (position != -1)
            sendMovesFrom(columns, order, position + 1);
    }

    private static void sendMovesFrom(Item[] columns, int[] order, int fromDisplayIndex) {
        if (fromDisplayIndex < 0)
            return;
        for (int i = fromDisplayIndex; i < order.length; i++) {
            Item column = columns[order[i]];
            if (!column.isDisposed())
                column.getImpl().sendEvent(SWT.Move);
        }
    }
}
