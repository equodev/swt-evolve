package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

import java.util.function.Consumer;

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

    /**
     * Tells the columns shown after {@code column} that they moved, as a resize of it does natively,
     * and has an owner-drawn tree's rows drawn again: native SWT repaints the cells a resize moves, and
     * a column that had width 0 was never drawn.
     */
    public static void columnResized(Tree tree, TreeColumn column) {
        if (column.isDisposed() || tree == null || tree.isDisposed())
            return;
        sendMovesAfter(tree.getColumns(), tree.getColumnOrder(), tree.indexOf(column));
        DartTree impl = (DartTree) tree.getImpl();
        if (impl.hooks(SWT.PaintItem)) TreeHelper.nameOwnerDrawnCells(impl);
    }

    /**
     * Tells the columns shown after {@code column} that they moved, as a resize of it does natively,
     * and has an owner-drawn table's rows drawn again: native SWT repaints the cells a resize moves,
     * and a column that had width 0 was never drawn.
     */
    public static void columnResized(Table table, TableColumn column) {
        if (column.isDisposed() || table == null || table.isDisposed())
            return;
        sendMovesAfter(table.getColumns(), table.getColumnOrder(), table.indexOf(column));
        DartTable impl = (DartTable) table.getImpl();
        if (impl.hooks(SWT.PaintItem)) TableHelper.nameOwnerDrawnCells(impl);
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

    /**
     * A column the user dragged in the header and dropped at {@code displayIndex}: the columns are reordered as a
     * native header drag does, which sends SWT.Move to the columns whose place changed. Only a moveable column
     * moves.
     */
    public static void movedByUser(TreeColumn column, int displayIndex) {
        Tree tree = column.getParent();
        movedByUser(tree.getColumnOrder(), tree.indexOf(column), displayIndex, column.getMoveable(), tree::setColumnOrder);
    }

    /** {@link #movedByUser(TreeColumn, int)} for a table's column. */
    public static void movedByUser(TableColumn column, int displayIndex) {
        Table table = column.getParent();
        movedByUser(table.getColumnOrder(), table.indexOf(column), displayIndex, column.getMoveable(), table::setColumnOrder);
    }

    private static void movedByUser(int[] order, int index, int displayIndex, boolean moveable, Consumer<int[]> setOrder) {
        int[] moved = moved(order, index, displayIndex, moveable);
        if (moved != null)
            setOrder.accept(moved);
    }

    /** {@code order} with column {@code index} shown at {@code displayIndex}, or null when nothing moves. */
    static int[] moved(int[] order, int index, int displayIndex, boolean moveable) {
        int from = displayIndex(order, index);
        int to = Math.max(0, Math.min(displayIndex, order.length - 1));
        if (!moveable || from == -1 || from == to)
            return null;
        int[] result = new int[order.length];
        for (int i = 0, j = 0; i < order.length; i++) {
            if (i == to) {
                result[i] = index;
                continue;
            }
            if (j == from)
                j++;
            result[i] = order[j++];
        }
        return result;
    }

    /**
     * The left edge of every column, by column index, with the columns shown in {@code order}; an order that does
     * not list every column is the creation order.
     */
    static int[] positions(int[] widths, int[] order) {
        if (order == null || order.length != widths.length) {
            order = new int[widths.length];
            for (int i = 0; i < order.length; i++)
                order[i] = i;
        }
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
            if (!column.isDisposed() && !shownAfterADisposedColumn(columns, order, i))
                column.getImpl().sendEvent(SWT.Move);
        }
    }

    /**
     * Whether a column shown before {@code displayIndex} was disposed by a Move listener of this pass: its
     * disposal already told every column shown after it that it moved, and a native list moves each once.
     */
    private static boolean shownAfterADisposedColumn(Item[] columns, int[] order, int displayIndex) {
        for (int i = 0; i < displayIndex; i++) {
            if (columns[order[i]].isDisposed())
                return true;
        }
        return false;
    }
}
