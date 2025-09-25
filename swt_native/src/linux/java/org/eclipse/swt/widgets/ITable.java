package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cairo.*;

public interface ITable extends IComposite, ImplTable {

    void checkSubclass();

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the user changes the receiver's selection, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * When <code>widgetSelected</code> is called, the item field of the event object is valid.
     * If the receiver has the <code>SWT.CHECK</code> style and the check selection changes,
     * the event object detail field contains the value <code>SWT.CHECK</code>.
     * <code>widgetDefaultSelected</code> is typically called when an item is double-clicked.
     * The item field of the event object is valid for default selection, but the detail field is not used.
     * </p>
     *
     * @param listener the listener which should be notified when the user changes the receiver's selection
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SelectionListener
     * @see #removeSelectionListener
     * @see SelectionEvent
     */
    void addSelectionListener(SelectionListener listener);

    /**
     * Clears the item at the given zero-relative index in the receiver.
     * The text, icon and other attributes of the item are set to the default
     * value.  If the table was created with the <code>SWT.VIRTUAL</code> style,
     * these attributes are requested again as needed.
     *
     * @param index the index of the item to clear
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#VIRTUAL
     * @see SWT#SetData
     *
     * @since 3.0
     */
    void clear(int index);

    /**
     * Removes the items from the receiver which are between the given
     * zero-relative start and end indices (inclusive).  The text, icon
     * and other attributes of the items are set to their default values.
     * If the table was created with the <code>SWT.VIRTUAL</code> style,
     * these attributes are requested again as needed.
     *
     * @param start the start index of the item to clear
     * @param end the end index of the item to clear
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if either the start or end are not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#VIRTUAL
     * @see SWT#SetData
     *
     * @since 3.0
     */
    void clear(int start, int end);

    /**
     * Clears the items at the given zero-relative indices in the receiver.
     * The text, icon and other attributes of the items are set to their default
     * values.  If the table was created with the <code>SWT.VIRTUAL</code> style,
     * these attributes are requested again as needed.
     *
     * @param indices the array of indices of the items
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     *    <li>ERROR_NULL_ARGUMENT - if the indices array is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#VIRTUAL
     * @see SWT#SetData
     *
     * @since 3.0
     */
    void clear(int[] indices);

    /**
     * Clears all the items in the receiver. The text, icon and other
     * attributes of the items are set to their default values. If the
     * table was created with the <code>SWT.VIRTUAL</code> style, these
     * attributes are requested again as needed.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#VIRTUAL
     * @see SWT#SetData
     *
     * @since 3.0
     */
    void clearAll();

    /**
     * Deselects the item at the given zero-relative index in the receiver.
     * If the item at the index was already deselected, it remains
     * deselected. Indices that are out of range are ignored.
     *
     * @param index the index of the item to deselect
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void deselect(int index);

    /**
     * Deselects the items at the given zero-relative indices in the receiver.
     * If the item at the given zero-relative index in the receiver
     * is selected, it is deselected.  If the item at the index
     * was not selected, it remains deselected.  The range of the
     * indices is inclusive. Indices that are out of range are ignored.
     *
     * @param start the start index of the items to deselect
     * @param end the end index of the items to deselect
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void deselect(int start, int end);

    /**
     * Deselects the items at the given zero-relative indices in the receiver.
     * If the item at the given zero-relative index in the receiver
     * is selected, it is deselected.  If the item at the index
     * was not selected, it remains deselected. Indices that are out
     * of range and duplicate indices are ignored.
     *
     * @param indices the array of indices for the items to deselect
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the set of indices is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void deselect(int[] indices);

    /**
     * Deselects all selected items in the receiver.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void deselectAll();

    /**
     * Returns the column at the given, zero-relative index in the
     * receiver. Throws an exception if the index is out of range.
     * Columns are returned in the order that they were created.
     * If no <code>TableColumn</code>s were created by the programmer,
     * this method will throw <code>ERROR_INVALID_RANGE</code> despite
     * the fact that a single column of data may be visible in the table.
     * This occurs when the programmer uses the table like a list, adding
     * items but never creating a column.
     *
     * @param index the index of the column to return
     * @return the column at the given index
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Table#getColumnOrder()
     * @see Table#setColumnOrder(int[])
     * @see TableColumn#getMoveable()
     * @see TableColumn#setMoveable(boolean)
     * @see SWT#Move
     */
    TableColumn getColumn(int index);

    /**
     * Returns the number of columns contained in the receiver.
     * If no <code>TableColumn</code>s were created by the programmer,
     * this value is zero, despite the fact that visually, one column
     * of items may be visible. This occurs when the programmer uses
     * the table like a list, adding items but never creating a column.
     *
     * @return the number of columns
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getColumnCount();

    /**
     * Returns an array of zero-relative integers that map
     * the creation order of the receiver's items to the
     * order in which they are currently being displayed.
     * <p>
     * Specifically, the indices of the returned array represent
     * the current visual order of the items, and the contents
     * of the array represent the creation order of the items.
     * </p><p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of items, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return the current visual order of the receiver's items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Table#setColumnOrder(int[])
     * @see TableColumn#getMoveable()
     * @see TableColumn#setMoveable(boolean)
     * @see SWT#Move
     *
     * @since 3.1
     */
    int[] getColumnOrder();

    /**
     * Returns an array of <code>TableColumn</code>s which are the
     * columns in the receiver.  Columns are returned in the order
     * that they were created.  If no <code>TableColumn</code>s were
     * created by the programmer, the array is empty, despite the fact
     * that visually, one column of items may be visible. This occurs
     * when the programmer uses the table like a list, adding items but
     * never creating a column.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of items, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return the items in the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Table#getColumnOrder()
     * @see Table#setColumnOrder(int[])
     * @see TableColumn#getMoveable()
     * @see TableColumn#setMoveable(boolean)
     * @see SWT#Move
     */
    TableColumn[] getColumns();

    /**
     * Returns the width in points of a grid line.
     *
     * @return the width of a grid line in points
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getGridLineWidth();

    /**
     * Returns the header background color.
     *
     * @return the receiver's header background color.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.106
     */
    Color getHeaderBackground();

    /**
     * Returns the header foreground color.
     *
     * @return the receiver's header foreground color.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.106
     */
    Color getHeaderForeground();

    /**
     * Returns the height of the receiver's header
     *
     * @return the height of the header or zero if the header is not visible
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.0
     */
    int getHeaderHeight();

    /**
     * Returns <code>true</code> if the receiver's header is visible,
     * and <code>false</code> otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, this method
     * may still indicate that it is considered visible even though
     * it may not actually be showing.
     * </p>
     *
     * @return the receiver's header's visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    boolean getHeaderVisible();

    /**
     * Returns the item at the given, zero-relative index in the
     * receiver. Throws an exception if the index is out of range.
     *
     * @param index the index of the item to return
     * @return the item at the given index
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    TableItem getItem(int index);

    /**
     * Returns the item at the given point in the receiver
     * or null if no such item exists. The point is in the
     * coordinate system of the receiver.
     * <p>
     * The item that is returned represents an item that could be selected by the user.
     * For example, if selection only occurs in items in the first column, then null is
     * returned if the point is outside of the item.
     * Note that the SWT.FULL_SELECTION style hint, which specifies the selection policy,
     * determines the extent of the selection.
     * </p>
     *
     * @param point the point used to locate the item
     * @return the item at the given point, or null if the point is not in a selectable item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    TableItem getItem(Point point);

    /**
     * Returns the number of items contained in the receiver.
     *
     * @return the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getItemCount();

    /**
     * Returns the height of the area which would be used to
     * display <em>one</em> of the items in the receiver.
     *
     * @return the height of one item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getItemHeight();

    /**
     * Returns a (possibly empty) array of <code>TableItem</code>s which
     * are the items in the receiver.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of items, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return the items in the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    TableItem[] getItems();

    /**
     * Returns <code>true</code> if the receiver's lines are visible,
     * and <code>false</code> otherwise. Note that some platforms draw
     * grid lines while others may draw alternating row colors.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, this method
     * may still indicate that it is considered visible even though
     * it may not actually be showing.
     * </p>
     *
     * @return the visibility state of the lines
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    boolean getLinesVisible();

    /**
     * Returns an array of <code>TableItem</code>s that are currently
     * selected in the receiver. The order of the items is unspecified.
     * An empty array indicates that no items are selected.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its selection, so modifying the array will
     * not affect the receiver.
     * </p>
     * @return an array representing the selection
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    TableItem[] getSelection();

    /**
     * Returns the number of selected items contained in the receiver.
     *
     * @return the number of selected items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getSelectionCount();

    /**
     * Returns the zero-relative index of the item which is currently
     * selected in the receiver, or -1 if no item is selected.
     *
     * @return the index of the selected item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getSelectionIndex();

    /**
     * Returns the zero-relative indices of the items which are currently
     * selected in the receiver. The order of the indices is unspecified.
     * The array is empty if no items are selected.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its selection, so modifying the array will
     * not affect the receiver.
     * </p>
     * @return the array of indices of the selected items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int[] getSelectionIndices();

    /**
     * Returns the column which shows the sort indicator for
     * the receiver. The value may be null if no column shows
     * the sort indicator.
     *
     * @return the sort indicator
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setSortColumn(TableColumn)
     *
     * @since 3.2
     */
    TableColumn getSortColumn();

    /**
     * Returns the direction of the sort indicator for the receiver.
     * The value will be one of <code>UP</code>, <code>DOWN</code>
     * or <code>NONE</code>.
     *
     * @return the sort direction
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setSortDirection(int)
     *
     * @since 3.2
     */
    int getSortDirection();

    /**
     * Returns the zero-relative index of the item which is currently
     * at the top of the receiver. This index can change when items are
     * scrolled or new items are added or removed.
     *
     * @return the index of the top item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getTopIndex();

    /**
     * Searches the receiver's list starting at the first column
     * (index 0) until a column is found that is equal to the
     * argument, and returns the index of that column. If no column
     * is found, returns -1.
     *
     * @param column the search column
     * @return the index of the column
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the column is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int indexOf(TableColumn column);

    /**
     * Searches the receiver's list starting at the first item
     * (index 0) until an item is found that is equal to the
     * argument, and returns the index of that item. If no item
     * is found, returns -1.
     *
     * @param item the search item
     * @return the index of the item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int indexOf(TableItem item);

    /**
     * Returns <code>true</code> if the item is selected,
     * and <code>false</code> otherwise.  Indices out of
     * range are ignored.
     *
     * @param index the index of the item
     * @return the selection state of the item at the index
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    boolean isSelected(int index);

    /**
     * Removes the item from the receiver at the given
     * zero-relative index.
     *
     * @param index the index for the item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void remove(int index);

    /**
     * Removes the items from the receiver which are
     * between the given zero-relative start and end
     * indices (inclusive).
     *
     * @param start the start of the range
     * @param end the end of the range
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if either the start or end are not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void remove(int start, int end);

    /**
     * Removes the items from the receiver's list at the given
     * zero-relative indices.
     *
     * @param indices the array of indices of the items
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     *    <li>ERROR_NULL_ARGUMENT - if the indices array is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void remove(int[] indices);

    /**
     * Removes all of the items from the receiver.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void removeAll();

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the user changes the receiver's selection.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SelectionListener
     * @see #addSelectionListener(SelectionListener)
     */
    void removeSelectionListener(SelectionListener listener);

    /**
     * Selects the item at the given zero-relative index in the receiver.
     * If the item at the index was already selected, it remains
     * selected. Indices that are out of range are ignored.
     *
     * @param index the index of the item to select
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void select(int index);

    /**
     * Selects the items in the range specified by the given zero-relative
     * indices in the receiver. The range of indices is inclusive.
     * The current selection is not cleared before the new items are selected.
     * <p>
     * If an item in the given range is not selected, it is selected.
     * If an item in the given range was already selected, it remains selected.
     * Indices that are out of range are ignored and no items will be selected
     * if start is greater than end.
     * If the receiver is single-select and there is more than one item in the
     * given range, then all indices are ignored.
     * </p>
     *
     * @param start the start of the range
     * @param end the end of the range
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Table#setSelection(int,int)
     */
    void select(int start, int end);

    /**
     * Selects the items at the given zero-relative indices in the receiver.
     * The current selection is not cleared before the new items are selected.
     * <p>
     * If the item at a given index is not selected, it is selected.
     * If the item at a given index was already selected, it remains selected.
     * Indices that are out of range and duplicate indices are ignored.
     * If the receiver is single-select and multiple indices are specified,
     * then all indices are ignored.
     * </p>
     *
     * @param indices the array of indices for the items to select
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the array of indices is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Table#setSelection(int[])
     */
    void select(int[] indices);

    /**
     * Selects all of the items in the receiver.
     * <p>
     * If the receiver is single-select, do nothing.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void selectAll();

    /**
     * Sets the order that the items in the receiver should
     * be displayed in to the given argument which is described
     * in terms of the zero-relative ordering of when the items
     * were added.
     *
     * @param order the new order to display the items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the item order is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the item order is not the same length as the number of items</li>
     * </ul>
     *
     * @see Table#getColumnOrder()
     * @see TableColumn#getMoveable()
     * @see TableColumn#setMoveable(boolean)
     * @see SWT#Move
     *
     * @since 3.1
     */
    void setColumnOrder(int[] order);

    /**
     * Sets the header background color to the color specified
     * by the argument, or to the default system color if the argument is null.
     * <p>
     * Note: This operation is a <em>HINT</em> and is not supported on all platforms. If
     * the native header has a 3D look and feel (e.g. Windows 7), this method
     * will cause the header to look FLAT irrespective of the state of the table style.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.106
     */
    void setHeaderBackground(Color color);

    /**
     * Sets the header foreground color to the color specified
     * by the argument, or to the default system color if the argument is null.
     * <p>
     * Note: This operation is a <em>HINT</em> and is not supported on all platforms. If
     * the native header has a 3D look and feel (e.g. Windows 7), this method
     * will cause the header to look FLAT irrespective of the state of the table style.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.106
     */
    void setHeaderForeground(Color color);

    /**
     * Marks the receiver's header as visible if the argument is <code>true</code>,
     * and marks it invisible otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, marking
     * it visible may not actually cause it to be displayed.
     * </p>
     *
     * @param show the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setHeaderVisible(boolean show);

    /**
     * Sets the number of items contained in the receiver.
     *
     * @param count the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setItemCount(int count);

    /**
     * Marks the receiver's lines as visible if the argument is <code>true</code>,
     * and marks it invisible otherwise. Note that some platforms draw grid lines
     * while others may draw alternating row colors.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, marking
     * it visible may not actually cause it to be displayed.
     * </p>
     *
     * @param show the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setLinesVisible(boolean show);

    void setRedraw(boolean redraw);

    /**
     * Sets the column used by the sort indicator for the receiver. A null
     * value will clear the sort indicator.  The current sort column is cleared
     * before the new column is set.
     *
     * @param column the column used by the sort indicator or <code>null</code>
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the column is disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    void setSortColumn(TableColumn column);

    /**
     * Sets the direction of the sort indicator for the receiver. The value
     * can be one of <code>UP</code>, <code>DOWN</code> or <code>NONE</code>.
     *
     * @param direction the direction of the sort indicator
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    void setSortDirection(int direction);

    /**
     * Selects the item at the given zero-relative index in the receiver.
     * The current selection is first cleared, then the new item is selected,
     * and if necessary the receiver is scrolled to make the new selection visible.
     *
     * @param index the index of the item to select
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Table#deselectAll()
     * @see Table#select(int)
     */
    void setSelection(int index);

    /**
     * Selects the items in the range specified by the given zero-relative
     * indices in the receiver. The range of indices is inclusive.
     * The current selection is cleared before the new items are selected,
     * and if necessary the receiver is scrolled to make the new selection visible.
     * <p>
     * Indices that are out of range are ignored and no items will be selected
     * if start is greater than end.
     * If the receiver is single-select and there is more than one item in the
     * given range, then all indices are ignored.
     * </p>
     *
     * @param start the start index of the items to select
     * @param end the end index of the items to select
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Table#deselectAll()
     * @see Table#select(int,int)
     */
    void setSelection(int start, int end);

    /**
     * Selects the items at the given zero-relative indices in the receiver.
     * The current selection is cleared before the new items are selected,
     * and if necessary the receiver is scrolled to make the new selection visible.
     * <p>
     * Indices that are out of range and duplicate indices are ignored.
     * If the receiver is single-select and multiple indices are specified,
     * then all indices are ignored.
     * </p>
     *
     * @param indices the indices of the items to select
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the array of indices is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Table#deselectAll()
     * @see Table#select(int[])
     */
    void setSelection(int[] indices);

    /**
     * Sets the receiver's selection to the given item.
     * The current selection is cleared before the new item is selected,
     * and if necessary the receiver is scrolled to make the new selection visible.
     * <p>
     * If the item is not in the receiver, then it is ignored.
     * </p>
     *
     * @param item the item to select
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the item has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    void setSelection(TableItem item);

    /**
     * Sets the receiver's selection to be the given array of items.
     * The current selection is cleared before the new items are selected,
     * and if necessary the receiver is scrolled to make the new selection visible.
     * <p>
     * Items that are not in the receiver are ignored.
     * If the receiver is single-select and multiple items are specified,
     * then all items are ignored.
     * </p>
     *
     * @param items the array of items
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the array of items is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if one of the items has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Table#deselectAll()
     * @see Table#select(int[])
     * @see Table#setSelection(int[])
     */
    void setSelection(TableItem[] items);

    /**
     * Sets the zero-relative index of the item which is currently
     * at the top of the receiver. This index can change when items
     * are scrolled or new items are added and removed.
     *
     * @param index the index of the top item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setTopIndex(int index);

    /**
     * Shows the column.  If the column is already showing in the receiver,
     * this method simply returns.  Otherwise, the columns are scrolled until
     * the column is visible.
     *
     * @param column the column to be shown
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the column is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the column has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void showColumn(TableColumn column);

    /**
     * Shows the item.  If the item is already showing in the receiver,
     * this method simply returns.  Otherwise, the items are scrolled until
     * the item is visible.
     *
     * @param item the item to be shown
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the item has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Table#showSelection()
     */
    void showItem(TableItem item);

    /**
     * Shows the selection.  If the selection is already showing in the receiver,
     * this method simply returns.  Otherwise, the items are scrolled until
     * the selection is visible.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Table#showItem(TableItem)
     */
    void showSelection();

    void dispose();

    Table getApi();
}
