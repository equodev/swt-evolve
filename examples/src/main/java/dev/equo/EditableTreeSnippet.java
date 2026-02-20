package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

public class EditableTreeSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Editable Tree");
        shell.setLayout(new FillLayout());

        Tree tree = new Tree(shell, SWT.BORDER | SWT.FULL_SELECTION);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TreeColumn col1 = new TreeColumn(tree, SWT.NONE);
        col1.setText("ID");
        col1.setWidth(100);

        TreeColumn col2 = new TreeColumn(tree, SWT.NONE);
        col2.setText("Name");
        col2.setWidth(200);

        for (int i = 1; i <= 3; i++) {
            TreeItem parent = new TreeItem(tree, SWT.NONE);
            parent.setText(new String[] { "P" + i, "Parent " + i });

            for (int j = 1; j <= 2; j++) {
                TreeItem child = new TreeItem(parent, SWT.NONE);
                child.setText(new String[] {
                        "P" + i + "." + j,
                        "Child " + j
                });
            }
            parent.setExpanded(true);
        }

        TreeEditor editor = new TreeEditor(tree);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Control oldEditor = editor.getEditor();
                if (oldEditor != null) oldEditor.dispose();

                TreeItem item = tree.getItem(
                        new org.eclipse.swt.graphics.Point(e.x, e.y)
                );
                if (item == null) return;

                int column = 1;

                Text text = new Text(tree, SWT.NONE);
                text.setText(item.getText(column));
                text.selectAll();
                text.setFocus();

                text.addModifyListener(event ->
                        item.setText(column, text.getText())
                );

                editor.setEditor(text, item, column);
            }
        });

        shell.setSize(400, 300);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
