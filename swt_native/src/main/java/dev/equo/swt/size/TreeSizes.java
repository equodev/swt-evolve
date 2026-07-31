package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartTree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeSizes {

    private static final double ROW_HEIGHT = 34.0;
    private static final double ROW_HEIGHT_WITH_COLS = 40.0;

    private static final double HEADER_HEIGHT = 32.0;
    private static final double HEADER_HEIGHT_WITH_COLS = 40.0;

    private static final double ROW_PADDING_VERTICAL = 2.0;
    private static final int WIDTH_PER_COLUMN = 30;
    private static final int WIDTH_NO_COLUMNS = 200;

    private static final double EDGE_GAP_FRACTION = 0.0275;
    private static final double EDGE_GAP_FRACTION_WITH_COLS = 0.005;
    private static final double ITEM_PADDING_LEFT = 8.0;
    private static final double ITEM_PADDING_LEFT_WITH_COLS = 12.0;
    private static final double ITEM_INDENT = 16.0;
    private static final double ITEM_INDENT_WITH_COLS = 16.0;
    private static final double EXPAND_ICON_SIZE = 12.0;

    public static Point computeSize(DartTree tree, int wHint, int hHint, boolean changed) {
        int columnCount = tree.getColumnCount();
        int width = wHint != SWT.DEFAULT ? wHint
            : (columnCount > 0 ? columnCount * WIDTH_PER_COLUMN : WIDTH_NO_COLUMNS);
        int height = hHint != SWT.DEFAULT ? hHint : getPreferredHeight(tree);
        return new Point(width, height);
    }

    public static int getPreferredHeight(DartTree tree) {
        return getHeaderHeight(tree) + countVisibleRows(tree.getApi().getItems()) * getItemHeight(tree);
    }

    private static int countVisibleRows(TreeItem[] items) {
        int count = 0;
        for (TreeItem item : items) {
            count++;
            if (item.getExpanded()) count += countVisibleRows(item.getItems());
        }
        return count;
    }

    public static int getItemHeight(DartTree tree) {
        double minHeight = tree.getColumnCount() > 1 ? ROW_HEIGHT_WITH_COLS : ROW_HEIGHT;
        TextStyle ts;
        if (!Config.getConfigFlags().use_swt_fonts) {
            ts = TreeItemTheme.get().textStyle().withStyleFrom(tree.getFont());
        } else {
            ts = TextStyle.from(tree.getFont());
        }
        double textHeight = FontMetricsUtil.getFontSize("Ag", ts).y() + ROW_PADDING_VERTICAL;
        return (int) Math.ceil(Math.max(textHeight, minHeight));
    }

    public static int getHeaderHeight(DartTree tree) {
        if (!tree.getHeaderVisible()) return 0;
        double minHeight = tree.getColumnCount() > 1 ? HEADER_HEIGHT_WITH_COLS : HEADER_HEIGHT;
        return (int) Math.ceil(minHeight);
    }

    public static double getExpanderLeft(DartTree tree, int level) {
        boolean withCols = tree.getColumnCount() > 1;
        double gapFraction = withCols ? EDGE_GAP_FRACTION_WITH_COLS : EDGE_GAP_FRACTION;
        double paddingLeft = withCols ? ITEM_PADDING_LEFT_WITH_COLS : ITEM_PADDING_LEFT;
        double indent = withCols ? ITEM_INDENT_WITH_COLS : ITEM_INDENT;
        Rectangle bounds = tree.getBounds();
        double widgetWidth = bounds != null ? bounds.width : 0;
        return widgetWidth * gapFraction + paddingLeft + indent * level;
    }

    public static double getExpanderWidth() {
        return EXPAND_ICON_SIZE;
    }

    public static boolean isOverExpander(DartTree tree, TreeItem item, int x) {
        if (item == null || item.getItemCount() == 0) return false;
        int level = 0;
        for (TreeItem parent = item.getParentItem(); parent != null; parent = parent.getParentItem()) {
            level++;
        }
        double left = getExpanderLeft(tree, level);
        return x >= left && x < left + EXPAND_ICON_SIZE;
    }
}
