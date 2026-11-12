package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.size.TableItemTheme;
import dev.equo.swt.size.TextStyle;
import dev.equo.swt.size.TreeItemTheme;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;

/**
 * Paints the owner-drawn cells of a Table or Tree row into that row's own GC overlay, so what the
 * application's {@code SWT.EraseItem}/{@code SWT.PaintItem} listeners draw is shown as drawn: icons,
 * fonts, colors, shapes and positions, not only the text a capture can name.
 *
 * <p>A row on screen is painted as soon as it is captured: its overlay mounts in the frame the row
 * arrives in, and what reaches the overlay's channels before then is held and applied in the order
 * it arrived. Any other row is painted once Flutter reports its overlay listening. The row shows the
 * text and images the capture derived until its overlay shows; {@code paintedTexts} and
 * {@code paintedImages} name what it then stops drawing.
 */
public final class OwnerDrawOverlay {

    private OwnerDrawOverlay() {
    }

    /** Display data: the {@link State} of the display's owner-drawn rows. */
    private static final String STATE = "org.eclipse.swt.internal.ownerDrawOverlay";

    private static final class State {
        /** Rows painted before their overlay reported listening; see {@link OwnerDrawOverlay#paintAhead}. */
        final java.util.Set<Item> paintedAhead = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
        /** Per Table or Tree, the key its rows on screen are cached under for one flush. */
        final java.util.Map<Control, Object> shownKeys = new java.util.WeakHashMap<>();
        /** The fonts the cells draw in under the theme, given to the listeners' GC; see {@link OwnerDrawOverlay#font}. */
        final java.util.Map<TextStyle, Font> themeFonts = new java.util.HashMap<>();
    }

    private static State state(Widget widget) {
        Display display = widget.getDisplay();
        Object state = display.getData(STATE);
        if (!(state instanceof State)) {
            State created = new State();
            display.setData(STATE, created);
            display.disposeExec(() -> created.themeFonts.values().forEach(Font::dispose));
            state = created;
        }
        return (State) state;
    }

    /**
     * Whether to paint {@code row} now, before its overlay reports listening: it is among
     * {@code shownRows}, the rows on screen, worked out once per flush. Each row once until it reports.
     */
    static boolean paintAhead(Control parent, Item row, java.util.function.Supplier<java.util.Set<Item>> shownRows) {
        State state = state(row);
        if (state.paintedAhead.contains(row)) return false;
        Object key = state.shownKeys.computeIfAbsent(parent, p -> new Object());
        java.util.Set<Item> shown = dev.equo.swt.Serializer.oncePerTape(key, shownRows);
        if (shown == null || !shown.contains(row)) return false;
        state.paintedAhead.add(row);
        return true;
    }

    /** Whether the row's overlay draws its painted cells: it listens, or was painted ahead of listening. */
    static boolean owns(Item row) {
        return isListening(row) || state(row).paintedAhead.contains(row);
    }

    /** Flutter mounted an overlay for each of these rows: paint them into it. */
    public static void rowsListening(DartWidget parent, int[] ids) {
        if (ids == null || !parent.hooks(SWT.PaintItem)) {
            return;
        }
        for (int id : ids) {
            Object found = FlutterBridge.findById(id);
            if (found instanceof DartTableItem) {
                DartTableItem item = (DartTableItem) found;
                if (item.parent.getImpl() != parent) continue;
                item.ownerDrawOverlay = true;
                // Painted again even when painted ahead: the overlay that reports may not be the one
                // the earlier paint was held for.
                state(item.getApi()).paintedAhead.remove(item.getApi());
                forgetWhatIsShown(item.getApi());
                TableHelper.nameOwnerDrawnCells(item);
            } else if (found instanceof DartTreeItem) {
                DartTreeItem item = (DartTreeItem) found;
                if (item.parent.getImpl() != parent) continue;
                item.ownerDrawOverlay = true;
                state(item.getApi()).paintedAhead.remove(item.getApi());
                forgetWhatIsShown(item.getApi());
                TreeHelper.nameOwnerDrawnCells(item);
            }
        }
    }

    private static void forgetWhatIsShown(Item row) {
        // A newly mounted overlay is empty, whatever this row last sent to the one before it.
        FlutterBridge.forgetWhatIsShown(row);
    }

    static boolean isListening(Item row) {
        Object impl = row.getImpl();
        if (impl instanceof DartTableItem) return ((DartTableItem) impl).ownerDrawOverlay;
        return impl instanceof DartTreeItem && ((DartTreeItem) impl).ownerDrawOverlay;
    }

    /**
     * Runs the erase and paint listeners of every cell of {@code row} on a GC addressed to the row,
     * with the events native SWT sends: each cell's bounds, in the parent's coordinates. The GC is
     * translated so the row's top-left corner is its origin, which is where the row's overlay starts.
     */
    static void paint(Control parent, Item row, int columnCount) {
        DartWidget owner = (DartWidget) parent.getImpl();
        Rectangle first = bounds(row, 0);
        GC gc = new GC(parent);
        ((DartGC) gc.getImpl()).paintOwnerDrawnItem(row);
        Transform toRow = new Transform(parent.getDisplay());
        try {
            toRow.translate(-rowLeft(parent, row), -first.y);
            gc.setTransform(toRow);
            for (int i = 0; i < columnCount; i++) {
                if (isHiddenColumn(parent, i)) continue;
                Rectangle cell = bounds(row, i);
                // Native SWT hands the listeners a GC already set to the cell's colors and font,
                // and an owner-drawing app draws with them rather than setting its own.
                gc.setForeground(foreground(row, i));
                gc.setBackground(background(row, i));
                gc.setFont(font(row, i));
                if (owner.hooks(SWT.EraseItem)) {
                    Event erase = cellEvent(row, i, gc, cell);
                    erase.detail = SWT.FOREGROUND | SWT.BACKGROUND;
                    owner.sendEvent(SWT.EraseItem, erase);
                }
                Event paint = cellEvent(row, i, gc, cell);
                paint.detail = SWT.FOREGROUND;
                owner.sendEvent(SWT.PaintItem, paint);
            }
        } finally {
            toRow.dispose();
            gc.dispose();
        }
    }

    private static Event cellEvent(Item row, int index, GC gc, Rectangle cell) {
        Event event = new Event();
        event.item = row;
        event.index = index;
        event.gc = gc;
        event.x = cell.x;
        event.y = cell.y;
        event.width = cell.width;
        event.height = cell.height;
        return event;
    }

    /** Where the row's overlay starts: a Tree row after the tree's edge gap, a Table row at the table's edge. */
    private static float rowLeft(Control parent, Item row) {
        // A table's overlay scrolls sideways with its row, while the cell bounds are in the client area.
        if (row instanceof TableItem) return -TableHelper.scrollOffsetX((DartTable) parent.getImpl());
        return (float) dev.equo.swt.size.TreeSizes.getRowLeft((DartTree) parent.getImpl());
    }

    /**
     * Whether column {@code index} of {@code parent} is a column of width 0. Native SWT runs no
     * owner-draw listener for such a cell on Windows (custom draw skips an empty sub-item before
     * MeasureItem, EraseItem and PaintItem) and clips it to nothing on GTK and Cocoa.
     */
    static boolean isHiddenColumn(Control parent, int index) {
        Item column = null;
        if (parent instanceof Table && index < ((Table) parent).getColumnCount()) {
            column = ((Table) parent).getColumn(index);
        } else if (parent instanceof Tree && index < ((Tree) parent).getColumnCount()) {
            column = ((Tree) parent).getColumn(index);
        }
        if (column instanceof TableColumn) return ((TableColumn) column).getWidth() == 0;
        return column instanceof TreeColumn && ((TreeColumn) column).getWidth() == 0;
    }

    private static Rectangle bounds(Item row, int index) {
        if (row instanceof TableItem) return ((TableItem) row).getBounds(index);
        return ((TreeItem) row).getBounds(index);
    }

    private static Color foreground(Item row, int index) {
        if (row instanceof TableItem) return ((TableItem) row).getForeground(index);
        return ((TreeItem) row).getForeground(index);
    }

    private static Color background(Item row, int index) {
        if (row instanceof TableItem) return ((TableItem) row).getBackground(index);
        return ((TreeItem) row).getBackground(index);
    }

    /**
     * The font the cell's own text is drawn in, which native SWT sets the listeners' GC to: the
     * item's font under {@code use_swt_fonts}, otherwise the theme's row style with the item font's
     * weight and slant. Text a listener draws then lands on the cell's text and measures as it does.
     */
    private static Font font(Item row, int index) {
        Font font = row instanceof TableItem ? ((TableItem) row).getFont(index) : ((TreeItem) row).getFont(index);
        if (Config.getConfigFlags().use_swt_fonts) return font;
        TextStyle style = (row instanceof TableItem
                ? TableItemTheme.get().textStyle()
                : TreeItemTheme.get().textStyle()).withStyleFrom(font);
        return state(row).themeFonts.computeIfAbsent(style, s -> themeFont(row.getDisplay(), s));
    }

    /** {@code style}, whose size is in logical pixels, as a font in points; SWT has no weight between normal and bold. */
    private static Font themeFont(Display display, TextStyle style) {
        int points = (int) Math.round(style.size() / FontMetricsUtil.rawDpiScale());
        int swtStyle = (style.weight() >= 700 ? SWT.BOLD : SWT.NORMAL) | (style.italic() ? SWT.ITALIC : SWT.NORMAL);
        return new Font(display, style.name(), points, swtStyle);
    }
}
