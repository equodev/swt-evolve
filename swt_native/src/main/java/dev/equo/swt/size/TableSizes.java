package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartTable;

public class TableSizes {

    private static final double ROW_HEIGHT = 24.0;
    private static final double ROW_HEIGHT_WITH_COLS = 24.0;

    private static final double HEADER_HEIGHT = 28.0;
    private static final double HEADER_HEIGHT_WITH_COLS = 28.0;

    private static final double ROW_PADDING_VERTICAL = 8.0;
    private static final double HEADER_PADDING_VERTICAL = 8.0;
    private static final double BORDER_WIDTH = 2.0;
    private static final int WIDTH_PER_COLUMN = 70;
    private static final int WIDTH_NO_COLUMNS = 70;

    public static Point computeSize(DartTable table, int wHint, int hHint, boolean changed) {
        int columnCount = table.getColumnCount();
        int width = wHint != SWT.DEFAULT ? wHint
            : (columnCount > 0 ? columnCount * WIDTH_PER_COLUMN : WIDTH_NO_COLUMNS);
        int height = hHint != SWT.DEFAULT ? hHint : getPreferredHeight(table);
        return new Point(width, height);
    }

    public static int getPreferredHeight(DartTable table) {
        return getHeaderHeight(table) + table.getItemCount() * getItemHeight(table) + 2 * getBorderWidth();
    }

    public static int getBorderWidth() {
        return (int) BORDER_WIDTH;
    }

    public static int getItemHeight(DartTable table) {
        double minHeight = table.getColumnCount() > 1 ? ROW_HEIGHT_WITH_COLS : ROW_HEIGHT;
        TextStyle ts;
        if (!Config.getConfigFlags().use_swt_fonts) {
            ts = TableItemTheme.get().textStyle().withStyleFrom(table.getFont());
        } else {
            ts = TextStyle.from(table.getFont());
        }
        double textHeight = FontMetricsUtil.getFontSize("Ag", ts).y() + ROW_PADDING_VERTICAL;
        return (int) Math.ceil(Math.max(textHeight, minHeight));
    }

    public static int getHeaderHeight(DartTable table) {
        if (!table.getHeaderVisible()) return 0;
        double minHeight = table.getColumnCount() > 1 ? HEADER_HEIGHT_WITH_COLS : HEADER_HEIGHT;
        TextStyle ts;
        if (!Config.getConfigFlags().use_swt_fonts) {
            ts = TableHeaderTheme.get().textStyle().withStyleFrom(table.getFont());
        } else {
            ts = TextStyle.from(table.getFont());
        }
        double textHeight = FontMetricsUtil.getFontSize("Ag", ts).y() + HEADER_PADDING_VERTICAL;
        return (int) Math.ceil(Math.max(textHeight, minHeight));
    }
}
