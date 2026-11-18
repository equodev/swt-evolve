package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates a VIRTUAL table whose rows are drawn only by an {@code SWT.PaintItem} listener, at
 * the event's top edge: a 16x16 icon at {@code (e.x, e.y)} and the label 20 px to its right at
 * {@code e.y}, with no {@code SWT.MeasureItem} or {@code SWT.EraseItem}. The application sets
 * {@code org.eclipse.swt.internal.cocoa.useNativeItemHeight=false}, so a native macOS row is one
 * text line tall and the label sits level with the icon. Prints the row height and the first paint
 * event's bounds.
 */
public class OwnerDrawnRowTopSnippet {
    public static void main(String[] args) {
        System.setProperty("org.eclipse.swt.internal.cocoa.useNativeItemHeight", "false");
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("OwnerDrawnRowTopSnippet");
        shell.setLayout(new FillLayout());

        Image icon = new Image(display, 16, 16);
        GC iconGc = new GC(icon);
        iconGc.setBackground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
        iconGc.fillRectangle(0, 0, 16, 16);
        iconGc.dispose();

        String[] labels = {"Open file", "Save all", "Find in files", "Show log"};
        Table table = new Table(shell, SWT.VIRTUAL | SWT.SINGLE | SWT.FULL_SELECTION);
        table.addListener(SWT.SetData, e -> ((TableItem) e.item).setText(""));
        boolean[] printed = {false};
        table.addListener(SWT.PaintItem, e -> {
            if (!printed[0]) {
                printed[0] = true;
                System.out.println("itemHeight=" + table.getItemHeight() + " paint y=" + e.y + " height=" + e.height
                        + " fontHeight=" + e.gc.getFontMetrics().getHeight());
            }
            int row = table.indexOf((TableItem) e.item);
            Color fg = e.gc.getForeground();
            e.gc.drawImage(icon, e.x, e.y);
            e.gc.setForeground(fg);
            e.gc.drawText(labels[row % labels.length], e.x + 20, e.y, true);
        });
        table.setItemCount(labels.length);
        table.select(0);

        shell.setSize(320, 8 * table.getItemHeight());
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        icon.dispose();
        display.dispose();
    }
}
