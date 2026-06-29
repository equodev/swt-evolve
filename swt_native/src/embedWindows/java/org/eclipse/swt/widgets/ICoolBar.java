package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;

public interface ICoolBar extends IComposite, ImplCoolBar {

    void checkSubclass();

    /**
     * Returns the item that is currently displayed at the given,
     * zero-relative index. Throws an exception if the index is
     * out of range.
     *
     * @param index the visual index of the item to return
     * @return the item at the given visual index
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    CoolItem getItem(int index);

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
     * Returns an array of zero-relative ints that map
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
     */
    int[] getItemOrder();

    /**
     * Returns an array of <code>CoolItem</code>s in the order
     * in which they are currently being displayed.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of items, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return the receiver's items in their current visual order
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    CoolItem[] getItems();

    /**
     * Returns an array of points whose x and y coordinates describe
     * the widths and heights (respectively) of the items in the receiver
     * in the order in which they are currently being displayed.
     *
     * @return the receiver's item sizes in their current visual order
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    Point[] getItemSizes();

    /**
     * Returns whether or not the receiver is 'locked'. When a coolbar
     * is locked, its items cannot be repositioned.
     *
     * @return true if the coolbar is locked, false otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.0
     */
    boolean getLocked();

    /**
     * Returns an array of ints that describe the zero-relative
     * indices of any item(s) in the receiver that will begin on
     * a new row. The 0th visible item always begins the first row,
     * therefore it does not count as a wrap index.
     *
     * @return an array containing the receiver's wrap indices, or an empty array if all items are in one row
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int[] getWrapIndices();

    /**
     * Searches the receiver's items in the order they are currently
     * being displayed, starting at the first item (index 0), until
     * an item is found that is equal to the argument, and returns
     * the index of that item. If no item is found, returns -1.
     *
     * @param item the search item
     * @return the visual order index of the search item, or -1 if the item is not found
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the item is disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int indexOf(CoolItem item);

    /**
     * Sets the receiver's item order, wrap indices, and item sizes
     * all at once. This method is typically used to restore the
     * displayed state of the receiver to a previously stored state.
     * <p>
     * The item order is the order in which the items in the receiver
     * should be displayed, given in terms of the zero-relative ordering
     * of when the items were added.
     * </p><p>
     * The wrap indices are the indices of all item(s) in the receiver
     * that will begin on a new row. The indices are given in the order
     * specified by the item order. The 0th item always begins the first
     * row, therefore it does not count as a wrap index. If wrap indices
     * is null or empty, the items will be placed on one line.
     * </p><p>
     * The sizes are specified in an array of points whose x and y
     * coordinates describe the new widths and heights (respectively)
     * of the receiver's items in the order specified by the item order.
     * </p>
     *
     * @param itemOrder an array of indices that describe the new order to display the items in
     * @param wrapIndices an array of wrap indices, or null
     * @param sizes an array containing the new sizes for each of the receiver's items in visual order
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if item order or sizes is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if item order or sizes is not the same length as the number of items</li>
     * </ul>
     */
    void setItemLayout(int[] itemOrder, int[] wrapIndices, Point[] sizes);

    /**
     * Sets whether or not the receiver is 'locked'. When a coolbar
     * is locked, its items cannot be repositioned.
     *
     * @param locked lock the coolbar if true, otherwise unlock the coolbar
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.0
     */
    void setLocked(boolean locked);

    /**
     * Sets the indices of all item(s) in the receiver that will
     * begin on a new row. The indices are given in the order in
     * which they are currently being displayed. The 0th item
     * always begins the first row, therefore it does not count
     * as a wrap index. If indices is null or empty, the items
     * will be placed on one line.
     *
     * @param indices an array of wrap indices, or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setWrapIndices(int[] indices);

    CoolBar getApi();
}
