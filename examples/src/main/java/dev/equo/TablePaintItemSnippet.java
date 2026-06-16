package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Replicates two real-world SWT.PaintItem patterns on the same Table, both driven by
 * TableHelper.firePaintItemForEmptyCells (the cells have no item.image of their own):
 * <ul>
 *   <li>"Value" column: like Katalon's Profile editor (MyLabelProvider) - drawText +
 *   textExtent + drawImage.</li>
 *   <li>"Selected" column: like Katalon's "Append Tags" dialog - only drawImage,
 *   toggled on click.</li>
 * </ul>
 */
public class TablePaintItemSnippet {

    public static void main(String[] args) {
        Config.useEquo(Table.class);
        Config.useEquo(TableItem.class);
        Config.useEquo(TableColumn.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("Table PaintItem - Profile + Checkbox");
        shell.setSize(450, 300);

        Image valueIcon = solidImage(display, 12, 12, new RGB(0, 100, 200));
        Image checkedImage = solidImage(display, 16, 16, new RGB(0, 150, 0));
        Image uncheckedImage = solidImage(display, 16, 16, new RGB(200, 200, 200));

        Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn valueColumn = new TableColumn(table, SWT.NONE);
        valueColumn.setText("Value");
        valueColumn.setWidth(280);

        TableColumn selectedColumn = new TableColumn(table, SWT.NONE);
        selectedColumn.setText("Selected");
        selectedColumn.setWidth(100);

        int rowCount = 6;
        boolean[] selected = new boolean[rowCount];
        for (int i = 0; i < rowCount; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, "Profile value " + i);
            // Column 1 left empty on purpose: getImages() returns null for it,
            // so firePaintItemForEmptyCells fires SWT.PaintItem for that cell.
        }

        table.addListener(SWT.PaintItem, event -> {
            TableItem item = (TableItem) event.item;
            int rowIndex = table.indexOf(item);
            if (event.index == 0) {
                String text = item.getText(0);
                int textX = event.x + 2;
                event.gc.drawText(text, textX, event.y + 2, true);
                int textWidth = event.gc.textExtent(text).x;
                int imageX = textX + textWidth + 5;
                int imageY = event.y + (event.height - valueIcon.getBounds().height) / 2;
                event.gc.drawImage(valueIcon, imageX, imageY);
            } else if (event.index == 1) {
                Image icon = selected[rowIndex] ? checkedImage : uncheckedImage;
                int imageY = event.y + (event.height - icon.getBounds().height) / 2;
                event.gc.drawImage(icon, event.x + 5, imageY);
            }
        });

        table.addListener(SWT.MouseDown, event -> {
            TableItem item = table.getItem(new Point(event.x, event.y));
            if (item == null) return;
            Rectangle cellBounds = item.getBounds(1);
            if (cellBounds.contains(event.x, event.y)) {
                int rowIndex = table.indexOf(item);
                selected[rowIndex] = !selected[rowIndex];
                // Re-sets the same text to mark the item dirty, forcing a re-render
                // so getImages() recomputes the "Selected" cell with the new state.
                item.setText(0, item.getText(0));
            }
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        valueIcon.dispose();
        checkedImage.dispose();
        uncheckedImage.dispose();
        display.dispose();
    }

    private static Image solidImage(Display display, int width, int height, RGB rgb) {
        Image image = new Image(display, width, height);
        GC gc = new GC(image);
        Color color = new Color(display, rgb);
        gc.setBackground(color);
        gc.fillRectangle(0, 0, width, height);
        gc.dispose();
        color.dispose();
        return image;
    }
}