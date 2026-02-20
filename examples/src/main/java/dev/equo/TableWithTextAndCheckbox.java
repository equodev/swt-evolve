package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.TableEditor;

public class TableWithTextAndCheckbox {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Table with Text and Checkbox");
        shell.setSize(400, 300);
        shell.setLayout(new FillLayout());

        Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn colText = new TableColumn(table, SWT.NONE);
        colText.setText("Text");

        TableColumn colCheck = new TableColumn(table, SWT.NONE);
        colCheck.setText("Active");

        for (int i = 0; i < 5; i++) {
            TableItem item = new TableItem(table, SWT.NONE);

            TableEditor textEditor = new TableEditor(table);
            Text text = new Text(table, SWT.NONE);
            text.setText("Row " + (i + 1));
            textEditor.grabHorizontal = true;
            textEditor.setEditor(text, item, 0);

            TableEditor checkEditor = new TableEditor(table);
            Button checkbox = new Button(table, SWT.CHECK);
            checkbox.setSelection(false);
            checkEditor.horizontalAlignment = SWT.CENTER;
            checkEditor.setEditor(checkbox, item, 1);
        }

        colText.pack();
        colCheck.pack();

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}