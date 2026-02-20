package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Snippet with two Trees:
 * 1. Tree WITH editing support via TreeEditor - listener + Text replacing the cell.
 * 2. Tree WITHOUT editing - read-only.
 */
public class TreeEditableSnippet {

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TreeEditableSnippet - TreeEditor vs Read-only");
        shell.setLayout(new GridLayout(1, false));

        // ----- Tree 1: WITH TreeEditor (listener + Text) -----
        Label lblEditable = new Label(shell, SWT.NONE);
        lblEditable.setText("Tree with TreeEditor - click on item to edit:");
        Tree treeEditable = new Tree(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        treeEditable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        populateTree(treeEditable);

        final TreeEditor editor = new TreeEditor(treeEditable);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50;

        treeEditable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Control oldEditor = editor.getEditor();
                if (oldEditor != null)
                    oldEditor.dispose();

                TreeItem item = (TreeItem) e.item;
                if (item == null)
                    return;

                Text newEditor = new Text(treeEditable, SWT.BORDER);
                newEditor.setText(item.getText());
                newEditor.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent ev) {
                        Text text = (Text) editor.getEditor();
                        if (text != null && !text.isDisposed()) {
                            editor.getItem().setText(text.getText());
                        }
                    }
                });
                newEditor.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent ev) {
                        Text text = (Text) editor.getEditor();
                        if (text != null && !text.isDisposed()) {
                            editor.getItem().setText(text.getText());
                            text.dispose();
                        }
                    }
                });
                newEditor.selectAll();
                newEditor.setFocus();
                editor.setEditor(newEditor, item);
            }
        });

        // ----- Tree 2: WITHOUT editing (read-only) -----
        Label lblReadOnly = new Label(shell, SWT.NONE);
        lblReadOnly.setText("Tree without editing (read-only):");
        Tree treeReadOnly = new Tree(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        treeReadOnly.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        populateTree(treeReadOnly);

        shell.pack();
        shell.setSize(300, 450);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private static void populateTree(Tree tree) {
        for (int i = 0; i < 3; i++) {
            TreeItem item0 = new TreeItem(tree, 0);
            item0.setText("Item " + i);
            for (int j = 0; j < 2; j++) {
                TreeItem item1 = new TreeItem(item0, 0);
                item1.setText("SubItem " + i + "." + j);
            }
        }
    }
}