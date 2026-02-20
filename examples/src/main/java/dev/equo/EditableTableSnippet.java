package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

public class EditableTableSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SWT TableEditor Example");
        shell.setSize(500, 300);
        shell.setLayout(new FillLayout());

        Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn col1 = new TableColumn(table, SWT.NONE);
        col1.setText("Type (Combo)");
        col1.setWidth(200);

        TableColumn col2 = new TableColumn(table, SWT.NONE);
        col2.setText("Value (Text)");
        col2.setWidth(200);

        for (int i = 0; i < 3; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            TableEditor comboEditor = new TableEditor(table);
            Combo combo = new Combo(table, SWT.READ_ONLY);
            combo.setItems(new String[]{"Option 1", "Option 2", "Option 3"});
            combo.select(0);

            comboEditor.grabHorizontal = true;
            comboEditor.setEditor(combo, item, 0);

            TableEditor textEditor = new TableEditor(table);
            Text text = new Text(table, SWT.NONE);
            text.setText("Editable...");

            textEditor.grabHorizontal = true;
            textEditor.setEditor(text, item, 1);
        }

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}