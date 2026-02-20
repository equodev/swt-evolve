package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

public class TreeWithEditorsExample {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SWT Tree with Editors");
        shell.setSize(700, 400);
        shell.setLayout(new FillLayout());

        Tree tree = new Tree(shell, SWT.BORDER | SWT.FULL_SELECTION);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        String[] titles = {"Name", "Type", "Value", "Enabled"};
        for (String title : titles) {
            TreeColumn column = new TreeColumn(tree, SWT.NONE);
            column.setText(title);
            column.setWidth(170);
        }

        TreeItem parent1 = new TreeItem(tree, SWT.NONE);
        createItem(parent1, "P 1", "Category", "N/A", true);

        TreeItem parent2 = new TreeItem(tree, SWT.NONE);
        createItem(parent2, "P 2", "Category", "N/A", false);

        TreeItem child1 = new TreeItem(parent1, SWT.NONE);
        createItem(child1, "C 1.1", "Text", "Editable", true);

        TreeItem child2 = new TreeItem(parent1, SWT.NONE);
        createItem(child2, "C 1.2", "Combo", "Option 1", false);

        TreeItem child3 = new TreeItem(parent2, SWT.NONE);
        createItem(child3, "C 2.1", "Text", "Editable", true);

        parent1.setExpanded(true);
        parent2.setExpanded(true);

        TreeEditor editor = new TreeEditor(tree);
        editor.horizontalAlignment = SWT.CENTER;
        editor.grabHorizontal = false;

        tree.addListener(SWT.MouseDoubleClick, event -> {
            Point point = new Point(event.x, event.y);
            TreeItem item = tree.getItem(point);
            if (item == null) return;

            for (int i = 0; i < tree.getColumnCount(); i++) {
                if (item.getBounds(i).contains(point)) {

                    Control oldEditor = editor.getEditor();
                    if (oldEditor != null) oldEditor.dispose();

                    // Name y Value → Text
                    if (i == 0 || i == 2) {
                        Text text = new Text(tree, SWT.NONE);
                        text.setText(item.getText(i));
                        text.selectAll();
                        text.setFocus();

                        int columnIndex = i;

                        text.addListener(SWT.FocusOut, e -> {
                            item.setText(columnIndex, text.getText());
                            text.dispose();
                        });

                        text.addListener(SWT.Traverse, e -> {
                            if (e.detail == SWT.TRAVERSE_RETURN) {
                                item.setText(columnIndex, text.getText());
                                text.dispose();
                                e.doit = false;
                            }
                            if (e.detail == SWT.TRAVERSE_ESCAPE) {
                                text.dispose();
                                e.doit = false;
                            }
                        });

                        editor.setEditor(text, item, i);
                    }

                    // Type → Combo
                    else if (i == 1) {
                        Combo combo = new Combo(tree, SWT.READ_ONLY);
                        combo.setItems("Text", "Combo", "Category");
                        combo.setText(item.getText(i));
                        combo.setFocus();

                        int columnIndex = i;

                        combo.addListener(SWT.Selection, e -> {
                            item.setText(columnIndex, combo.getText());
                            combo.dispose();
                        });

                        combo.addListener(SWT.FocusOut, e -> combo.dispose());

                        editor.setEditor(combo, item, i);
                    }

                    // Enabled → Checkbox
                    else if (i == 3) {

                        Button check = new Button(tree, SWT.CHECK);

                        Boolean enabled = (Boolean) item.getData("enabled");
                        check.setSelection(enabled != null && enabled);

                        check.setFocus();

                        check.addListener(SWT.Selection, e -> {
                            boolean value = check.getSelection();
                            item.setData("enabled", value);
                            item.setText(3, value ? "Yes" : "No");
                        });

                        check.addListener(SWT.FocusOut, e -> check.dispose());

                        editor.setEditor(check, item, i);
                    }

                    break;
                }
            }
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private static void createItem(TreeItem item, String name, String type, String value, boolean enabled) {
        item.setText(new String[]{
                name,
                type,
                value,
                enabled ? "Yes" : "No"
        });
        item.setData("enabled", enabled);
    }
}