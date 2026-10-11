package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.size.TableItemTheme;
import dev.equo.swt.size.TextStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

/** The width a TableColumn packs to. */
public final class TableColumnHelper {

    /** Horizontal room a header or a cell takes around its content. */
    private static final int CELL_PADDING = 16;
    private static final int CELL_MARGIN = 2;

    private TableColumnHelper() {
    }

    /**
     * The width that shows the column's header and its cell in every row that holds its data: a virtual
     * table is not asked for the data of rows it has not filled, and an SWT.MeasureItem listener has the
     * last word on the width of every cell it is asked about, as on the native table.
     */
    public static int packWidth(DartTableColumn column) {
        Table table = column.parent;
        DartTable dartTable = (DartTable) table.getImpl();
        int index = table.indexOf(column.getApi());
        if (index < 0)
            return 0;
        int width = withPadding(headerWidth(column, dartTable));
        GC gc = null;
        if (dartTable.hooks(SWT.MeasureItem)) {
            gc = new GC(table);
            // Only ever measured with: Flutter never hears of this GC, so its dispose must not tell it.
            ((DartGC) gc.getImpl()).silentDispose = true;
        }
        try {
            boolean virtual = (table.getStyle() & SWT.VIRTUAL) != 0;
            for (int i = 0, rows = dartTable.getItemCount(); i < rows; i++) {
                TableItem item = dartTable.items[i];
                if (item == null)
                    continue;
                DartTableItem cell = (DartTableItem) item.getImpl();
                if (virtual && !cell.cached)
                    continue;
                width = Math.max(width, cellWidth(dartTable, cell, index, gc));
            }
        } finally {
            if (gc != null)
                gc.dispose();
        }
        return width;
    }

    private static int headerWidth(DartTableColumn column, DartTable table) {
        int width = 0;
        String text = TreeColumnHelper.stripMnemonic(column.getText());
        if (!text.isEmpty())
            width = measure(text, table.getApi().getFont());
        Image image = column.getImage();
        if (image != null)
            width += image.getBounds().width;
        if (table.sortColumn == column.getApi() && table.sortDirection != SWT.NONE)
            width += TableItemTheme.get().textStyle().size() + CELL_MARGIN;
        return width;
    }

    private static int cellWidth(DartTable table, DartTableItem cell, int index, GC gc) {
        String[] strings = cell._strings();
        Image[] images = cell._images();
        Font[] cellFonts = cell._cellFont();
        String text = index == 0 ? cell.text : (strings != null && index < strings.length ? strings[index] : null);
        Image image = index == 0 ? cell.image : (images != null && index < images.length ? images[index] : null);
        Font font = cellFonts != null && index < cellFonts.length ? cellFonts[index] : null;
        if (font == null)
            font = cell.font;
        if (font == null)
            font = table.getApi().getFont();
        int content = text == null || text.isEmpty() ? 0 : measure(text, font);
        if (image != null)
            content += image.getBounds().width;
        int width = withPadding(content);
        if (gc != null) {
            gc.setFont(font);
            Event event = new Event();
            event.item = cell.getApi();
            event.index = index;
            event.gc = gc;
            event.width = width;
            event.height = table.getItemHeight();
            table.sendEvent(SWT.MeasureItem, event);
            width = event.width;
        }
        return width;
    }

    private static int withPadding(int content) {
        return content > 0 ? content + CELL_PADDING + CELL_MARGIN : 0;
    }

    private static int measure(String text, Font font) {
        TextStyle style = Config.getConfigFlags().use_swt_fonts
            ? TextStyle.from(font)
            : TableItemTheme.get().textStyle().withStyleFrom(font);
        return (int) Math.ceil(FontMetricsUtil.getFontSize(text, style).x());
    }
}
