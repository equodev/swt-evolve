package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
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
 * <p>A row's ops are sent only once Flutter has said its overlay is listening. Ops that reach a
 * channel before its listener exists are held per channel and replayed out of order across the
 * state and op channels, which is not a display list. Until then the row shows the text and image
 * the capture derived.
 */
public final class OwnerDrawOverlay {

    private OwnerDrawOverlay() {
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
                forgetWhatIsShown(item.getApi());
                TableHelper.nameOwnerDrawnCells(item);
            } else if (found instanceof DartTreeItem) {
                DartTreeItem item = (DartTreeItem) found;
                if (item.parent.getImpl() != parent) continue;
                item.ownerDrawOverlay = true;
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
     * translated so the row's top edge is its origin, which is where the row's overlay starts.
     */
    static void paint(Control parent, Item row, int columnCount) {
        DartWidget owner = (DartWidget) parent.getImpl();
        Rectangle first = bounds(row, 0);
        GC gc = new GC(parent);
        ((DartGC) gc.getImpl()).paintOwnerDrawnItem(row);
        Transform toRow = new Transform(parent.getDisplay());
        try {
            toRow.translate(0, -first.y);
            gc.setTransform(toRow);
            for (int i = 0; i < columnCount; i++) {
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

    private static Font font(Item row, int index) {
        if (row instanceof TableItem) return ((TableItem) row).getFont(index);
        return ((TreeItem) row).getFont(index);
    }
}
