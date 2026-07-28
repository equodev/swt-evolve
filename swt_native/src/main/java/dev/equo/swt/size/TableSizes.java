package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.widgets.DartTable;

public class TableSizes {

    private static final double HEADER_VERTICAL_PADDING = 8.0;
    private static final double BORDER_WIDTH = 2.0;

    public static int getBorderWidth() {
        return (int) BORDER_WIDTH;
    }

    public static int getItemHeight(DartTable table) {
        TextStyle ts;
        if (!Config.getConfigFlags().use_swt_fonts) {
            ts = TableItemTheme.get().textStyle().withStyleFrom(table.getFont());
        } else {
            ts = TextStyle.from(table.getFont());
        }
        PointD text = FontMetricsUtil.getFontSize("", ts);
        return (int) Math.ceil(Math.max(text.y() + TableItemSizes.NONE.VERTICAL_PADDING, TableItemSizes.NONE.MIN_HEIGHT));
    }

    public static int getHeaderHeight(DartTable table) {
        if (!table.getHeaderVisible()) return 0;
        TextStyle ts;
        if (!Config.getConfigFlags().use_swt_fonts) {
            ts = TableHeaderTheme.get().textStyle().withStyleFrom(table.getFont());
        } else {
            ts = TextStyle.from(table.getFont());
        }
        return (int) Math.ceil(FontMetricsUtil.getFontSize("Ag", ts).y() + HEADER_VERTICAL_PADDING);
    }
}
