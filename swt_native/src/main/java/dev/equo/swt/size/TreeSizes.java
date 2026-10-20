package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartTree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.DartTreeItem;

public class TreeSizes {

    private static final double ROW_HEIGHT = 34.0;
    private static final double ROW_HEIGHT_WITH_COLS = 40.0;

    private static final double HEADER_HEIGHT = 32.0;
    private static final double HEADER_HEIGHT_WITH_COLS = 40.0;

    private static final double ROW_PADDING_VERTICAL = 2.0;
    private static final int WIDTH_NO_COLUMNS = 200;
    private static final int NATIVE_SCROLLER_AND_BEZEL_TRIM = 15 + 2;

    private static final double EDGE_GAP_FRACTION = 0.0275;
    private static final double EDGE_GAP_FRACTION_WITH_COLS = 0.005;
    private static final double ITEM_PADDING_LEFT = 8.0;
    private static final double ITEM_PADDING_LEFT_WITH_COLS = 12.0;
    private static final double ITEM_INDENT = 16.0;
    private static final double ITEM_INDENT_WITH_COLS = 16.0;
    private static final double EXPAND_ICON_SIZE = 12.0;
    private static final double EXPAND_ICON_SPACING = 8.0;
    private static final double CHECKBOX_SPACE = 18.0 + 4.0;
    private static final int ICON_SIZE = 16;
    private static final int ICON_SPACE = ICON_SIZE + 8;
    private static final int TEXT_PADDING = 8;
    private static final int CELL_PADDING = 20;
    private static final int HEADER_PADDING = 20;
    private static final int CELL_INSET = 4;
    private static final int ALIGNMENT_INSET = 4;

    public static Point computeSize(DartTree tree, int wHint, int hHint, boolean changed) {
        int columnCount = tree.getColumnCount();
        int style = tree.getStyle();
        int width;
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        } else {
            width = columnCount > 0 ? getColumnsWidth(tree) : WIDTH_NO_COLUMNS;
            if ((style & SWT.V_SCROLL) != 0) {
                ScrollBar vBar = tree.getVerticalBar();
                width += vBar != null ? vBar.getSize().x : NATIVE_SCROLLER_AND_BEZEL_TRIM;
            }
        }
        int height;
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        } else {
            height = getPreferredHeight(tree);
            if ((style & SWT.H_SCROLL) != 0) height += NATIVE_SCROLLER_AND_BEZEL_TRIM;
        }
        return new Point(width, height);
    }

    private static int getColumnsWidth(DartTree tree) {
        int width = 0;
        for (TreeColumn column : tree.getColumns()) {
            width += column.getWidth();
        }
        return width;
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
        if (!Config.getConfigFlags().use_swt_fonts || tree.getExplicitFont() == null) {
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

    /** Where a row's box starts: after the edge gap. An owner-drawn row's overlay starts here. */
    public static double getRowLeft(DartTree tree) {
        double gapFraction = tree.getColumnCount() > 1 ? EDGE_GAP_FRACTION_WITH_COLS : EDGE_GAP_FRACTION;
        Rectangle bounds = tree.getBounds();
        return (bounds != null ? bounds.width : 0) * gapFraction;
    }

    public static double getExpanderLeft(DartTree tree, int level) {
        boolean withCols = tree.getColumnCount() > 1;
        double paddingLeft = withCols ? ITEM_PADDING_LEFT_WITH_COLS : ITEM_PADDING_LEFT;
        double indent = withCols ? ITEM_INDENT_WITH_COLS : ITEM_INDENT;
        return getRowLeft(tree) + paddingLeft + indent * level;
    }

    /** Where the first column starts in a row: the expander's left edge at level 0, after the edge gap and the row's padding. */
    public static int getCellsLeft(DartTree tree) {
        return (int) Math.round(getExpanderLeft(tree, 0));
    }

    /** Where an item's first cell content starts: after its indent, the expander and any checkbox. */
    public static int getContentLeft(DartTree tree, TreeItem item) {
        double left = getExpanderLeft(tree, levelOf(item)) + EXPAND_ICON_SIZE + EXPAND_ICON_SPACING;
        if ((tree.getStyle() & SWT.CHECK) != 0) left += CHECKBOX_SPACE;
        return (int) Math.ceil(left);
    }

    /** The size an item's image is drawn at. */
    public static int getIconSize() {
        return ICON_SIZE;
    }

    /** The width an image takes before the text, its spacing included. */
    public static int getIconSpace() {
        return ICON_SPACE;
    }

    /** Where a multi-column cell's content starts, from its column's left edge. */
    public static int getCellInset(int alignment) {
        return (alignment & (SWT.CENTER | SWT.RIGHT)) == 0 ? CELL_INSET + ALIGNMENT_INSET : CELL_INSET;
    }

    /** The width a cell's text is shown in: the text and its padding. */
    public static int getTextWidth(TreeItem item, int index) {
        String text = item.getText(index);
        TextStyle ts = TreeItemTheme.get().textStyle().withStyleFrom(item.getFont(index));
        int width = text == null || text.isEmpty() ? 0 : (int) Math.ceil(FontMetricsUtil.getFontSize(text, ts).x());
        return width + TEXT_PADDING;
    }

    /** The width a multi-column cell needs to show its text whole. */
    public static int getPackedCellWidth(TreeItem item, int index) {
        return measure(item.getText(index), item.getFont(index)) + CELL_PADDING;
    }

    /** The width a column's header needs to show its title whole: a multi-column header shows it in capitals. */
    public static int getPackedHeaderWidth(TreeColumn column) {
        String text = column.getText();
        String shown = text == null ? null : text.toUpperCase(java.util.Locale.ROOT);
        return measure(shown, column.getParent().getFont()) + HEADER_PADDING;
    }

    private static int measure(String text, org.eclipse.swt.graphics.Font font) {
        if (text == null || text.isEmpty()) return 0;
        TextStyle ts = TreeItemTheme.get().textStyle().withStyleFrom(font);
        return (int) Math.ceil(FontMetricsUtil.getFontSize(text, ts).x());
    }

    static int levelOf(TreeItem item) {
        int level = 0;
        for (TreeItem parent = item.getParentItem(); parent != null; parent = parent.getParentItem()) {
            level++;
        }
        return level;
    }

    public static double getExpanderWidth() {
        return EXPAND_ICON_SIZE;
    }

    public static boolean isOverExpander(DartTree tree, TreeItem item, int x) {
        if (item == null || item.getItemCount() == 0) return false;
        double left = getExpanderLeft(tree, levelOf(item));
        return x >= left && x < left + EXPAND_ICON_SIZE;
    }
}
