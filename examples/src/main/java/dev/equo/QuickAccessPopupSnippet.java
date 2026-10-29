package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * A filter popup over the main shell, the way a JFace PopupDialog opens one: a resizable on-top tool
 * shell holding a filter field, a separator and an owner-drawn result table that draws the match in
 * a bold derived from the system font. Opening it focuses the field; typing must land there.
 * Run it under each window policy (-Ddev.equo.swt.windows=single|all) to compare. Each edit of the
 * field prints {@code filter=<text>}, so a run without a screen shows where the typing went.
 */
public class QuickAccessPopupSnippet {

    private static final String MATCH = "Interpre";
    private static final String[] ROWS = {"Interpreters (Preferences)", "Run Interpreter (Interpreters)",
            "Interpret Script - Interpret Selection"};

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Quick Access popup");
        shell.setLayout(new GridLayout());
        Button open = new Button(shell, SWT.PUSH);
        open.setText("Open popup");
        open.addListener(SWT.Selection, e -> openPopup(shell));
        shell.setSize(700, 500);
        shell.open();
        display.timerExec(3000, () -> openPopup(shell));
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private static void openPopup(Shell parent) {
        Display display = parent.getDisplay();
        Shell popup = new Shell(parent, SWT.ON_TOP | SWT.TOOL | SWT.RESIZE);
        popup.setText("Find Actions");
        popup.setLayout(new GridLayout());
        Text filter = new Text(popup, SWT.SINGLE | SWT.BORDER);
        filter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        new Label(popup, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Table table = new Table(popup, SWT.SINGLE | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Font rowFont = new Font(display, "Liberation Sans", 11, SWT.NORMAL);
        table.setFont(rowFont);
        new TableColumn(table, SWT.NONE).setWidth(460);
        for (String row : ROWS) new TableItem(table, SWT.NONE).setText(row);
        FontData sys = display.getSystemFont().getFontData()[0];
        Font bold = new Font(display, sys.getName(), sys.getHeight(), SWT.BOLD);
        TextLayout layout = new TextLayout(display);
        table.addListener(SWT.MeasureItem, e -> e.height = 22);
        table.addListener(SWT.EraseItem, e -> e.detail &= ~SWT.FOREGROUND);
        table.addListener(SWT.PaintItem, e -> paint(e, layout, rowFont, bold));

        filter.addListener(SWT.Modify, e -> System.out.println("filter=" + filter.getText()));
        popup.addListener(SWT.Dispose, e -> {
            layout.dispose();
            bold.dispose();
            rowFont.dispose();
        });
        popup.setBounds(parent.getBounds().x + 100, parent.getBounds().y + 80, 500, 260);
        popup.open();
        filter.setFocus();
    }

    private static void paint(Event e, TextLayout layout, Font regular, Font bold) {
        String text = ((TableItem) e.item).getText();
        layout.setFont(regular);
        layout.setText(text);
        layout.setStyle(null, 0, text.length() - 1);
        for (int i = text.indexOf(MATCH); i >= 0; i = text.indexOf(MATCH, i + 1))
            layout.setStyle(new TextStyle(bold, null, null), i, i + MATCH.length() - 1);
        layout.draw(e.gc, e.x + 4, e.y + 3);
    }
}
