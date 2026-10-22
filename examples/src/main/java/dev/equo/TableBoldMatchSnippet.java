package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * An owner-drawn Table whose rows bold the part of their text that matches a filter, the way
 * Eclipse's Quick Access lists its results: one TextLayout per row, a bold TextStyle over the match.
 */
public class TableBoldMatchSnippet {

    private static final String MATCH = "Show";
    private static final String[] FONTS = {null, "System", "Segoe UI"};

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Bold match");
        shell.setLayout(new FillLayout());

        Table table = new Table(shell, SWT.SINGLE | SWT.FULL_SELECTION);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(460);
        FontData base = table.getFont().getFontData()[0];
        Font[] regular = new Font[FONTS.length];
        Font[] bold = new Font[FONTS.length];
        for (int i = 0; i < FONTS.length; i++) {
            String name = FONTS[i];
            regular[i] = new Font(display, name == null ? base.getName() : name, base.getHeight(), SWT.NORMAL);
            bold[i] = new Font(display, name == null ? base.getName() : name, base.getHeight(), SWT.BOLD);
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(MATCH + " Active Editors  [" + (name == null ? "table font" : name) + "]");
            item.setData(i);
        }

        TextLayout layout = new TextLayout(display);
        table.addListener(SWT.MeasureItem, e -> e.height = 22);
        table.addListener(SWT.EraseItem, e -> e.detail &= ~SWT.FOREGROUND);
        table.addListener(SWT.PaintItem, e -> paint(e, layout, regular, bold));

        shell.setSize(500, 160);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        layout.dispose();
        for (int i = 0; i < FONTS.length; i++) {
            regular[i].dispose();
            bold[i].dispose();
        }
        display.dispose();
    }

    private static void paint(Event e, TextLayout layout, Font[] regular, Font[] bold) {
        TableItem item = (TableItem) e.item;
        int row = (Integer) item.getData();
        layout.setFont(regular[row]);
        layout.setText(item.getText());
        layout.setStyle(null, 0, item.getText().length() - 1);
        layout.setStyle(new TextStyle(bold[row], null, null), 0, MATCH.length() - 1);
        layout.draw(e.gc, e.x + 4, e.y + 3);
    }
}
