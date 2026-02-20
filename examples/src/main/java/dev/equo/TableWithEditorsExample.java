package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

public class TableWithEditorsExample {

    public static void main(String[] args) {

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SWT Table with Editors");
        shell.setSize(700, 400);
        shell.setLayout(new FillLayout());

        Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        String[] titles = {"Name", "Type", "Value", "Enabled"};
        for (String title : titles) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(title);
            column.setWidth(170);
        }

        createItem(table, "Item 1", "Category", "N/A", true);
        createItem(table, "Item 2", "Text", "Editable", false);
        createItem(table, "Item 3", "Combo", "Option 1", true);

        TableEditor editor = new TableEditor(table);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;

//        table.addListener(SWT.MouseDoubleClick, event -> {
            table.addListener(SWT.MouseDown, event -> {

            int row = table.getSelectionIndex();
            if (row == -1) return;

            TableItem item = table.getItem(row);

            // Detectar columna clickeada
            int column = -1;
            for (int i = 0; i < table.getColumnCount(); i++) {
                Rectangle rect = item.getBounds(i);
                if (rect.contains(event.x, event.y)) {
                    column = i;
                    break;
                }
            }

            if (column == -1) return;

            Control oldEditor = editor.getEditor();
            if (oldEditor != null) oldEditor.dispose();

            // Name y Value → Text
            if (column == 0 || column == 2) {

                Text text = new Text(table, SWT.NONE);
                text.setText(item.getText(column));
                text.selectAll();
                text.setFocus();

                int col = column;

                text.addListener(SWT.FocusOut, e -> {
                    item.setText(col, text.getText());
                    text.dispose();
                });

                text.addListener(SWT.Traverse, e -> {
                    if (e.detail == SWT.TRAVERSE_RETURN) {
                        item.setText(col, text.getText());
                        text.dispose();
                        e.doit = false;
                    }
                    if (e.detail == SWT.TRAVERSE_ESCAPE) {
                        text.dispose();
                        e.doit = false;
                    }
                });

                editor.setEditor(text, item, column);
            }

            // Type → Combo
            else if (column == 1) {

                Combo combo = new Combo(table, SWT.READ_ONLY);
                combo.setItems("Text", "Combo", "Category");
                combo.setText(item.getText(column));
                combo.setFocus();

                int col = column;

                combo.addListener(SWT.Selection, e -> {
                    item.setText(col, combo.getText());
                    combo.dispose();
                });

                combo.addListener(SWT.FocusOut, e -> combo.dispose());

                editor.setEditor(combo, item, column);
            }

            // Enabled → Checkbox
            else if (column == 3) {

                Button check = new Button(table, SWT.CHECK);

                Boolean enabled = (Boolean) item.getData("enabled");
                check.setSelection(enabled != null && enabled);
                check.setFocus();

                int col = column;

                check.addListener(SWT.Selection, e -> {
                    boolean value = check.getSelection();
                    item.setData("enabled", value);
                    item.setText(col, value ? "Yes" : "No");
                });

                check.addListener(SWT.FocusOut, e -> check.dispose());

                editor.grabHorizontal = false;
                editor.minimumWidth = 50;
                editor.setEditor(check, item, column);
            }
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private static void createItem(Table table, String name, String type, String value, boolean enabled) {

        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[]{
                name,
                type,
                value,
                enabled ? "Yes" : "No"
        });

        item.setData("enabled", enabled);
    }
}