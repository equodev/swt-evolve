package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates a multi-column Tree and Table whose cells are drawn by an {@code SWT.PaintItem}
 * listener the way JFace's {@code StyledCellLabelProvider} does: {@code SWT.EraseItem} clears
 * {@code SWT.FOREGROUND} and {@code SWT.PaintItem} draws the cell's text with a {@link TextLayout}
 * at the cell's left edge, with no width set. The first column's texts are wider than the column;
 * native SWT clips each cell's drawing at its column edge, so the next columns stay readable.
 */
public class OwnerDrawnCellClipSnippet {

    private static final String[][] ROWS = {
            {"Unused local variable in a method with a long name", "SAMPLE_CLASS_01", "CLAS", "12"},
            {"Missing null check (static analysis)", "SAMPLE_CLASS_01", "CLAS", "34"},
            {"Short", "SAMPLE_INTF_02", "INTF", "5"},
    };
    private static final String[] HEADERS = {"Check", "Object Name", "Type", "Line"};
    private static final int[] WIDTHS = {120, 110, 50, 40};

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("OwnerDrawnCellClipSnippet");
        shell.setLayout(new FillLayout(SWT.VERTICAL));

        Tree tree = new Tree(shell, SWT.BORDER | SWT.FULL_SELECTION);
        tree.setHeaderVisible(true);
        for (int i = 0; i < HEADERS.length; i++) {
            TreeColumn column = new TreeColumn(tree, SWT.NONE);
            column.setText(HEADERS[i]);
            column.setWidth(WIDTHS[i]);
        }
        for (String[] row : ROWS) new TreeItem(tree, SWT.NONE).setText(row);
        drawCellsLikeStyledCellLabelProvider(tree);

        Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        for (int i = 0; i < HEADERS.length; i++) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(HEADERS[i]);
            column.setWidth(WIDTHS[i]);
        }
        for (String[] row : ROWS) new TableItem(table, SWT.NONE).setText(row);
        drawCellsLikeStyledCellLabelProvider(table);

        shell.setSize(360, 260);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void drawCellsLikeStyledCellLabelProvider(Control control) {
        TextLayout layout = new TextLayout(control.getDisplay());
        control.addListener(SWT.EraseItem, e -> e.detail &= ~SWT.FOREGROUND);
        control.addListener(SWT.PaintItem, e -> {
            String text = e.item instanceof TreeItem
                    ? ((TreeItem) e.item).getText(e.index)
                    : ((TableItem) e.item).getText(e.index);
            layout.setFont(e.gc.getFont());
            layout.setText(text);
            int y = e.y + Math.max(0, (e.height - layout.getBounds().height) / 2);
            layout.draw(e.gc, e.x + 2, y);
        });
        control.addListener(SWT.Dispose, e -> layout.dispose());
    }
}
