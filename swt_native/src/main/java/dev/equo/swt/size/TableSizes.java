package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartTable;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.TableItem;

public class TableSizes {

    private static final double ROW_HEIGHT = 24.0;
    private static final double ROW_HEIGHT_WITH_COLS = 24.0;

    private static final double HEADER_HEIGHT = 28.0;
    private static final double HEADER_HEIGHT_WITH_COLS = 28.0;

    private static final double ROW_PADDING_VERTICAL = 8.0;
    private static final double HEADER_PADDING_VERTICAL = 8.0;
    private static final double BORDER_WIDTH = 2.0;
    private static final int WIDTH_NO_COLUMNS = 70;
    private static final int NATIVE_SCROLLER_AND_BEZEL_TRIM = 15 + 2;
    private static final int CELL_PADDING_LEFT = 8;
    private static final int CELL_PADDING_HORIZONTAL = 16;
    private static final int CELL_MARGIN = 2;
    private static final int CHECKBOX_WIDTH = 20;

    public static Point computeSize(DartTable table, int wHint, int hHint, boolean changed) {
        int columnCount = table.getColumnCount();
        int style = table.getStyle();
        int width;
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        } else {
            width = columnCount > 0 ? getColumnsWidth(table)
                    : Math.max(WIDTH_NO_COLUMNS, getContentWidth(table));
            if ((style & SWT.V_SCROLL) != 0) {
                ScrollBar vBar = table.getVerticalBar();
                width += vBar != null ? vBar.getSize().x : NATIVE_SCROLLER_AND_BEZEL_TRIM;
            }
        }
        int height;
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        } else {
            height = getPreferredHeight(table);
            if ((style & SWT.H_SCROLL) != 0) height += NATIVE_SCROLLER_AND_BEZEL_TRIM - 2 * getBorderWidth();
        }
        return new Point(width, height);
    }

    private static int getColumnsWidth(DartTable table) {
        int width = 0;
        for (TableColumn column : table.getColumns()) {
            width += column.getWidth();
        }
        if ((table.getStyle() & SWT.CHECK) != 0) width += CHECKBOX_WIDTH + CELL_PADDING_LEFT;
        return width;
    }

    private static int getContentWidth(DartTable table) {
        if ((table.getStyle() & SWT.VIRTUAL) != 0) return 0;
        int widest = 0;
        for (TableItem item : table.getItems()) {
            if (item == null) continue;
            TextStyle ts;
            if (!Config.getConfigFlags().use_swt_fonts) {
                ts = TableItemTheme.get().textStyle().withStyleFrom(item.getFont());
            } else {
                ts = TextStyle.from(item.getFont());
            }
            int cellWidth = 0;
            String text = item.getText();
            if (text != null && !text.isEmpty()) {
                cellWidth += (int) Math.ceil(FontMetricsUtil.getFontSize(text, ts).x());
            }
            if (item.getImage() != null) {
                cellWidth += ts.size() + CELL_PADDING_LEFT;
            }
            widest = Math.max(widest, cellWidth);
        }
        if (widest == 0) return 0;
        if ((table.getStyle() & SWT.CHECK) != 0) widest += CHECKBOX_WIDTH + CELL_PADDING_LEFT;
        return widest + CELL_PADDING_HORIZONTAL + CELL_MARGIN;
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
