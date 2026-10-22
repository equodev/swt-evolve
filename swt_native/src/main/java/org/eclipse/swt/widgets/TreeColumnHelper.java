package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.size.Mnemonics;
import dev.equo.swt.size.TextStyle;
import dev.equo.swt.size.TreeItemTheme;
import dev.equo.swt.size.TreeSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

/** The width a TreeColumn packs to, which its generated implementation cannot work out. */
public final class TreeColumnHelper {

    /** Horizontal room a header or a cell takes around its text: both paddings and the alignment's. */
    static final int CELL_PADDING = 12;
    /** The gap before the divider that every column but the last draws at its right. */
    static final int DIVIDER_GAP = 8;
    /** The room a header image takes before the text. */
    static final int HEADER_IMAGE_GAP = 4;
    /** The text padding a cell's text is measured with. */
    static final int TEXT_PADDING = 8;
    /** The space between the expander and the text, which the tree column's header leaves too. */
    static final int EXPANDER_SPACING = 8;

    private TreeColumnHelper() {
    }

    /**
     * The width that shows the column's header and the cells of every row on screen: rows under a
     * collapsed item do not count. Cells are read as they are held, so a virtual tree is not asked
     * for the data of rows it has not filled; an SWT.MeasureItem listener has the last word on the
     * width of every cell it is asked about, as on the native tree.
     */
    public static int packWidth(DartTreeColumn column) {
        Tree tree = column.parent;
        DartTree dartTree = (DartTree) tree.getImpl();
        int index = tree.indexOf(column.getApi());
        int width = headerWidth(column, dartTree, index);
        GC gc = null;
        if (dartTree.hooks(SWT.MeasureItem)) {
            gc = new GC(tree);
            // Only ever measured with: Flutter never hears of this GC, so its dispose must not tell it.
            ((DartGC) gc.getImpl()).silentDispose = true;
        }
        try {
            width = Math.max(width, cellsWidth(dartTree, dartTree.items, index, gc));
        } finally {
            if (gc != null) gc.dispose();
        }
        boolean last = index == dartTree.columnCount - 1;
        return width + CELL_PADDING + (last ? 0 : DIVIDER_GAP);
    }

    static int headerWidth(DartTreeColumn column, DartTree tree, int index) {
        int width = 0;
        String text = Mnemonics.display(column.getText());
        if (!text.isEmpty()) {
            // A tree with several columns draws its headers in capitals.
            if (tree.columnCount > 1) text = text.toUpperCase();
            width += measure(text, tree.getApi().getFont());
        }
        Image image = column.getImage();
        if (image != null) width += image.getBounds().width + HEADER_IMAGE_GAP;
        // The tree column's header starts where the rows' text does, after the expander.
        if (index == 0 && width > 0) width += (int) Math.ceil(TreeSizes.getExpanderWidth()) + EXPANDER_SPACING;
        return width;
    }

    static int cellsWidth(DartTree tree, TreeItem[] items, int index, GC gc) {
        int width = 0;
        for (TreeItem item : items) {
            if (item == null) continue;
            DartTreeItem cell = (DartTreeItem) item.getImpl();
            width = Math.max(width, cellWidth(tree, cell, index, gc));
            if (cell.getExpanded()) width = Math.max(width, cellsWidth(tree, cell.items, index, gc));
        }
        return width;
    }

    static int cellWidth(DartTree tree, DartTreeItem cell, int index, GC gc) {
        String[] strings = cell._strings();
        Image[] images = cell._images();
        Font[] cellFonts = cell._cellFont();
        // A row's cell arrays, once made, are as long as the tree has columns.
        String text = index == 0 ? cell.text : (strings != null ? strings[index] : null);
        Image image = index == 0 ? cell.image : (images != null ? images[index] : null);
        Font font = cellFonts != null ? cellFonts[index] : null;
        if (font == null) font = cell.font;
        if (font == null) font = tree.getApi().getFont();
        int width = text == null || text.isEmpty() ? 0 : measure(text, font) + TEXT_PADDING;
        if (image != null) width += TreeSizes.getIconSpace();
        // A virtual row is measured by its listener only once it holds its data.
        boolean measured = (tree.getApi().getStyle() & SWT.VIRTUAL) == 0 || cell.cached;
        if (gc != null && measured) {
            gc.setFont(font);
            Event event = new Event();
            event.item = cell.getApi();
            event.index = index;
            event.gc = gc;
            event.width = width;
            event.height = tree.getItemHeight();
            tree.sendEvent(SWT.MeasureItem, event);
            width = event.width;
        }
        if (index == 0) {
            // Column 0 starts after the edge gap and the row padding, and holds the indent and the expander.
            width += TreeSizes.getContentLeft(tree, cell.getApi()) - TreeSizes.getCellsLeft(tree);
        }
        return width;
    }

    static int measure(String text, Font font) {
        TextStyle style = Config.getConfigFlags().use_swt_fonts
            ? TextStyle.from(font)
            : TreeItemTheme.get().textStyle().withStyleFrom(font);
        return (int) Math.ceil(FontMetricsUtil.getFontSize(text, style).x());
    }
}
