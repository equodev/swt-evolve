/**
 * ****************************************************************************
 *  Copyright (c) 2009, 2016 IBM Corporation and others.
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
package org.eclipse.swt.accessibility;

/**
 * This adapter class provides default implementations for the
 * methods in the <code>AccessibleTableCellListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>AccessibleTableCell</code> events can
 * extend this class and override only the methods that they are
 * interested in.
 * </p>
 *
 * @see AccessibleTableCellListener
 * @see AccessibleTableCellEvent
 *
 * @since 3.6
 */
public class AccessibleTableCellAdapter implements AccessibleTableCellListener {

    /**
     * Returns the number of columns occupied by this cell accessible.
     * <p>
     * This is 1 if the specified cell is only in one column, or
     * more than 1 if the specified cell spans multiple columns.
     * </p>
     *
     * @param e an event object containing the following fields:<ul>
     * <li>[out] count - the 1 based number of columns spanned by the specified cell
     * </ul>
     */
    @Override
    public void getColumnSpan(AccessibleTableCellEvent e) {
    }

    /**
     * Returns the column headers as an array of cell accessibles.
     * TODO: doc that this is a more efficient way to get headers of a cell than TableListener.getRow/ColHeaders
     *
     * @param e an event object containing the following fields:<ul>
     * <li>[out] accessibles - an array of cell accessibles, or null if there are no column headers
     * </ul>
     */
    @Override
    public void getColumnHeaders(AccessibleTableCellEvent e) {
    }

    /**
     * Translates this cell accessible into the corresponding column index.
     *
     * @param e an event object containing the following fields:<ul>
     * <li>[out] index - the 0 based column index of the specified cell,
     * 		or the index of the first column if the cell spans multiple columns
     * </ul>
     */
    @Override
    public void getColumnIndex(AccessibleTableCellEvent e) {
    }

    /**
     * Returns the number of rows occupied by this cell accessible.
     * <p>
     * This is 1 if the specified cell is only in one row, or
     * more than 1 if the specified cell spans multiple rows.
     * </p>
     *
     * @param e an event object containing the following fields:<ul>
     * <li>[out] count - the 1 based number of rows spanned by the specified cell
     * </ul>
     */
    @Override
    public void getRowSpan(AccessibleTableCellEvent e) {
    }

    /**
     * Returns the row headers as an array of cell accessibles.
     * TODO: doc that this is a more efficient way to get headers of a cell than TableListener.getRow/ColHeaders
     *
     * @param e an event object containing the following fields:<ul>
     * <li>[out] accessibles - an array of cell accessibles, or null if there are no row headers
     * </ul>
     */
    @Override
    public void getRowHeaders(AccessibleTableCellEvent e) {
    }

    /**
     * Translates this cell accessible into the corresponding row index.
     *
     * @param e an event object containing the following fields:<ul>
     * <li>[out] index - the 0 based row index of the specified cell,
     * 		or the index of the first row if the cell spans multiple rows
     * </ul>
     */
    @Override
    public void getRowIndex(AccessibleTableCellEvent e) {
    }

    /**
     * Returns the accessible for the table containing this cell.
     *
     * @param e an event object containing the following fields:<ul>
     * <li>[out] accessible - the accessible for the containing table
     * </ul>
     */
    @Override
    public void getTable(AccessibleTableCellEvent e) {
    }

    /**
     * Returns a boolean value indicating whether this cell is selected.
     *
     * @param e an event object containing the following fields:<ul>
     * <li>[out] isSelected - true if the specified cell is selected and false otherwise
     * </ul>
     */
    @Override
    public void isSelected(AccessibleTableCellEvent e) {
    }
}
