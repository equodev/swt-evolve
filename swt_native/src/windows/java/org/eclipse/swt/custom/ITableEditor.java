package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ITableEditor extends IControlEditor {

    /**
     * Removes all associations between the TableEditor and the cell in the table.  The
     * Table and the editor Control are <b>not</b> disposed.
     */
    void dispose();

    /**
     * Returns the zero based index of the column of the cell being tracked by this editor.
     *
     * @return the zero based index of the column of the cell being tracked by this editor
     */
    int getColumn();

    /**
     * Returns the TableItem for the row of the cell being tracked by this editor.
     *
     * @return the TableItem for the row of the cell being tracked by this editor
     */
    TableItem getItem();

    /**
     * Sets the zero based index of the column of the cell being tracked by this editor.
     *
     * @param column the zero based index of the column of the cell being tracked by this editor
     */
    void setColumn(int column);

    /**
     * Specifies the <code>TableItem</code> that is to be edited.
     *
     * @param item the item to be edited
     */
    void setItem(TableItem item);

    void setEditor(Control editor);

    /**
     * Specify the Control that is to be displayed and the cell in the table that it is to be positioned above.
     *
     * <p>Note: The Control provided as the editor <b>must</b> be created with its parent being the Table control
     * specified in the TableEditor constructor.
     *
     * @param editor the Control that is displayed above the cell being edited
     * @param item the TableItem for the row of the cell being tracked by this editor
     * @param column the zero based index of the column of the cell being tracked by this editor
     */
    void setEditor(Control editor, TableItem item, int column);

    void layout();

    TableEditor getApi();
}
