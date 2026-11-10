package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * The result list of Eclipse's Quick Access: an owner-drawn two-column table whose rows draw their
 * category and label through a TextLayout with the colours the paint GC is handed, and a separator
 * line above each category in a grey blended from the table's own background and foreground. The
 * table is black on white, as an Eclipse 4 light CSS colours it, whatever theme the snippet runs in.
 */
public class QuickAccessThemeSnippet {

    private static final String MATCH = "Out";
    private static final String[][] ROWS = {
            {"Previous Choices", "Outline (General)"},
            {"", "Open Resource (Navigate)"},
            {"Views", "Outline (General)"},
            {"", "Console Output (General)"},
            {"", "Problems (General)"},
            {"Commands", "Format - Format Source"},
            {"", "Toggle Outline - Show the quick outline"},
            {"Help", "Search 'Out' in Help"},
    };

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Quick Access");
        shell.setLayout(new GridLayout());
        Text filter = new Text(shell, SWT.SINGLE | SWT.BORDER);
        filter.setText(MATCH);
        filter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Table table = new Table(shell, SWT.SINGLE | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        table.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        table.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        new TableColumn(table, SWT.NONE).setWidth(150);
        new TableColumn(table, SWT.NONE).setWidth(400);
        for (String[] row : ROWS) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setData(row);
        }
        Color gray = new Color(display, blend(table.getBackground().getRGB(), table.getForeground().getRGB()));
        FontData sys = display.getSystemFont().getFontData()[0];
        Font bold = new Font(display, sys.getName(), sys.getHeight(), SWT.BOLD);
        TextStyle boldStyle = new TextStyle(bold, null, null);
        TextLayout layout = new TextLayout(display);
        table.addListener(SWT.MeasureItem, e -> e.height = 22);
        table.addListener(SWT.EraseItem, e -> e.detail &= ~SWT.FOREGROUND);
        table.addListener(SWT.PaintItem, e -> paint(e, layout, boldStyle, gray));
        table.setSelection(0);

        shell.addListener(SWT.Dispose, e -> {
            layout.dispose();
            bold.dispose();
            gray.dispose();
        });
        shell.setSize(600, 280);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private static void paint(Event e, TextLayout layout, TextStyle boldStyle, Color gray) {
        TableItem item = (TableItem) e.item;
        String[] row = (String[]) item.getData();
        layout.setFont(item.getParent().getFont());
        String text = row[e.index];
        if (text.isEmpty()) return;
        layout.setText(text);
        if (e.index == 1) {
            for (int i = text.indexOf(MATCH); i >= 0; i = text.indexOf(MATCH, i + 1))
                layout.setStyle(boldStyle, i, i + MATCH.length() - 1);
        }
        Rectangle bounds = item.getTextBounds(e.index);
        Rectangle textBounds = layout.getBounds();
        layout.draw(e.gc, bounds.x, bounds.y + (bounds.height - textBounds.height) / 2);
        if (e.index == 0) {
            e.gc.setForeground(gray);
            Rectangle cell = item.getBounds(e.index);
            e.gc.drawLine(cell.x, cell.y, cell.x + 550, cell.y);
        }
    }

    private static RGB blend(RGB a, RGB b) {
        return new RGB((a.red + b.red) / 2, (a.green + b.green) / 2, (a.blue + b.blue) / 2);
    }
}
