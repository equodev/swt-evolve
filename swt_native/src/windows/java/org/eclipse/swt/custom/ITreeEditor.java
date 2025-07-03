package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ITreeEditor extends IControlEditor {

    /**
     * Removes all associations between the TreeEditor and the row in the tree.  The
     * tree and the editor Control are <b>not</b> disposed.
     */
    void dispose();

    /**
     * Returns the zero based index of the column of the cell being tracked by this editor.
     *
     * @return the zero based index of the column of the cell being tracked by this editor
     *
     * @since 3.1
     */
    int getColumn();

    /**
     * Returns the TreeItem for the row of the cell being tracked by this editor.
     *
     * @return the TreeItem for the row of the cell being tracked by this editor
     */
    TreeItem getItem();

    /**
     * Sets the zero based index of the column of the cell being tracked by this editor.
     *
     * @param column the zero based index of the column of the cell being tracked by this editor
     *
     * @since 3.1
     */
    void setColumn(int column);

    /**
     * Specifies the <code>TreeItem</code> that is to be edited.
     *
     * @param item the item to be edited
     */
    void setItem(TreeItem item);

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
    void setEditor(Control editor, TreeItem item, int column);

    void setEditor(Control editor);

    /**
     * Specify the Control that is to be displayed and the cell in the tree that it is to be positioned above.
     *
     * <p>Note: The Control provided as the editor <b>must</b> be created with its parent being the Tree control
     * specified in the TreeEditor constructor.
     *
     * @param editor the Control that is displayed above the cell being edited
     * @param item the TreeItem for the row of the cell being tracked by this editor
     */
    void setEditor(Control editor, TreeItem item);

    void layout();

    TreeEditor getApi();
}
