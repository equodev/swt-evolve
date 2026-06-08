package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.widgets.DartTable;

public class TableSizes {

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
}