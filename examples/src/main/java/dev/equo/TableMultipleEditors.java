package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

public class TableMultipleEditors {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Table with multiple editors");
        shell.setLayout(new FillLayout());

        Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        String[] titles = { "ID", "Name", "Type", "Active" };
        int[] widths = { 80, 150, 120, 80 };

        for (int i = 0; i < titles.length; i++) {
            TableColumn col = new TableColumn(table, SWT.NONE);
            col.setText(titles[i]);
            col.setWidth(widths[i]);
        }

        for (int i = 1; i <= 5; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] {
                    String.valueOf(i),
                    "Item " + i,
                    "A",
                    i % 2 == 0 ? "Yes" : "No" // TODO
            });
        }

        for (TableItem item : table.getItems()) {

            TableEditor textEditor = new TableEditor(table);
            Text text = new Text(table, SWT.NONE);
            text.setText(item.getText(1));
            text.addModifyListener(e ->
                    item.setText(1, text.getText())
            );
            textEditor.grabHorizontal = true;
            textEditor.setEditor(text, item, 1);

            TableEditor comboEditor = new TableEditor(table);
            Combo combo = new Combo(table, SWT.READ_ONLY);
            combo.setItems(new String[] { "A", "B", "C" });
            combo.setText(item.getText(2));
            combo.addListener(SWT.Selection, e ->
                    item.setText(2, combo.getText())
            );
            comboEditor.grabHorizontal = true;
            comboEditor.setEditor(combo, item, 2);

            TableEditor checkEditor = new TableEditor(table);
            Button check = new Button(table, SWT.CHECK);
            check.setSelection("Yes".equals(item.getText(3)));
            check.addListener(SWT.Selection, e ->
                    item.setText(3, check.getSelection() ? "Yes" : "No")
            );
            checkEditor.horizontalAlignment = SWT.CENTER;
            checkEditor.setEditor(check, item, 3);
        }

        shell.setSize(500, 300);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
