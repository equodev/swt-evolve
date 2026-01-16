/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 *  A TreeEditor is a manager for a Control that appears above a cell in a Tree and tracks with the
 *  moving and resizing of that cell.  It can be used to display a text widget above a cell
 *  in a Tree so that the user can edit the contents of that cell.  It can also be used to display
 *  a button that can launch a dialog for modifying the contents of the associated cell.
 *
 *  <p> Here is an example of using a TreeEditor:</p>
 *  <pre><code>
 * 	final Tree tree = new Tree(shell, SWT.BORDER);
 * 	for (int i = 0; i &lt; 3; i++) {
 * 		TreeItem item = new TreeItem(tree, SWT.NONE);
 * 		item.setText("item " + i);
 * 		for (int j = 0; j &lt; 3; j++) {
 * 			TreeItem subItem = new TreeItem(item, SWT.NONE);
 * 			subItem.setText("item " + i + " " + j);
 * 		}
 * 	}
 *
 * 	final TreeEditor editor = new TreeEditor(tree);
 * 	//The editor must have the same size as the cell and must
 * 	//not be any smaller than 50 pixels.
 * 	editor.horizontalAlignment = SWT.LEFT;
 * 	editor.grabHorizontal = true;
 * 	editor.minimumWidth = 50;
 *
 * 	tree.addSelectionListener(new SelectionAdapter() {
 * 		public void widgetSelected(SelectionEvent e) {
 * 			// Clean up any previous editor control
 * 			Control oldEditor = editor.getEditor();
 * 			if (oldEditor != null) oldEditor.dispose();
 *
 * 			// Identify the selected row
 * 			TreeItem item = (TreeItem)e.item;
 * 			if (item == null) return;
 *
 * 			// The control that will be the editor must be a child of the Tree
 * 			Text newEditor = new Text(tree, SWT.NONE);
 * 			newEditor.setText(item.getText());
 * 			newEditor.addModifyListener(new ModifyListener() {
 * 				public void modifyText(ModifyEvent e) {
 * 					Text text = (Text)editor.getEditor();
 * 					editor.getItem().setText(text.getText());
 * 				}
 * 			});
 * 			newEditor.selectAll();
 * 			newEditor.setFocus();
 * 			editor.setEditor(newEditor, item);
 * 		}
 * 	});
 *  </code></pre>
 *
 *  @see <a href="http://www.eclipse.org/swt/snippets/#treeeditor">TreeEditor snippets</a>
 *  @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class TreeEditor extends ControlEditor {

    /**
     * Creates a TreeEditor for the specified Tree.
     *
     * @param tree the Tree Control above which this editor will be displayed
     */
    public TreeEditor(Tree tree) {
        this((ITreeEditor) null);
        setImpl(new SwtTreeEditor(tree, this));
        if (tree.getImpl() instanceof DartTree) {
            ((DartTree) tree.getImpl())._setEditable(true);
        }
    }

    /**
     * Removes all associations between the TreeEditor and the row in the tree.  The
     * tree and the editor Control are <b>not</b> disposed.
     */
    public void dispose() {
        getImpl().dispose();
    }

    /**
     * Returns the zero based index of the column of the cell being tracked by this editor.
     *
     * @return the zero based index of the column of the cell being tracked by this editor
     *
     * @since 3.1
     */
    public int getColumn() {
        return getImpl().getColumn();
    }

    /**
     * Returns the TreeItem for the row of the cell being tracked by this editor.
     *
     * @return the TreeItem for the row of the cell being tracked by this editor
     */
    public TreeItem getItem() {
        return getImpl().getItem();
    }

    /**
     * Sets the zero based index of the column of the cell being tracked by this editor.
     *
     * @param column the zero based index of the column of the cell being tracked by this editor
     *
     * @since 3.1
     */
    public void setColumn(int column) {
        getImpl().setColumn(column);
    }

    /**
     * Specifies the <code>TreeItem</code> that is to be edited.
     *
     * @param item the item to be edited
     */
    public void setItem(TreeItem item) {
        getImpl().setItem(item);
    }

    /**
     * Specify the Control that is to be displayed and the cell in the tree that it is to be positioned above.
     *
     * <p>Note: The Control provided as the editor <b>must</b> be created with its parent being the Tree control
     * specified in the TreeEditor constructor.
     *
     * @param editor the Control that is displayed above the cell being edited
     * @param item the TreeItem for the row of the cell being tracked by this editor
     * @param column the zero based index of the column of the cell being tracked by this editor
     *
     * @since 3.1
     */
    public void setEditor(Control editor, TreeItem item, int column) {
        getImpl().setEditor(editor, item, column);
    }

    public void setEditor(Control editor) {
        getImpl().setEditor(editor);
    }

    /**
     * Specify the Control that is to be displayed and the cell in the tree that it is to be positioned above.
     *
     * <p>Note: The Control provided as the editor <b>must</b> be created with its parent being the Tree control
     * specified in the TreeEditor constructor.
     *
     * @param editor the Control that is displayed above the cell being edited
     * @param item the TreeItem for the row of the cell being tracked by this editor
     */
    public void setEditor(Control editor, TreeItem item) {
        getImpl().setEditor(editor, item);
    }

    public void layout() {
        getImpl().layout();
    }

    protected TreeEditor(ITreeEditor impl) {
        super(impl);
    }

    static TreeEditor createApi(ITreeEditor impl) {
        return new TreeEditor(impl);
    }

    public ITreeEditor getImpl() {
        return (ITreeEditor) super.getImpl();
    }
}
