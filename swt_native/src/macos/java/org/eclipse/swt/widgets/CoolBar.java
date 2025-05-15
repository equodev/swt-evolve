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
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import dev.equo.swt.Convert;

/**
 * Instances of this class provide an area for dynamically
 * positioning the items they contain.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>CoolItem</code>.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to add <code>Control</code> children to it,
 * or set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>FLAT, HORIZONTAL, VERTICAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#coolbar">CoolBar snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CoolBar extends Composite {

    /**
     * Constructs a new instance of this class given its parent
     * and a style value describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in
     * class <code>SWT</code> which is applicable to instances of this
     * class, or must be built by <em>bitwise OR</em>'ing together
     * (that is, using the <code>int</code> "|" operator) two or more
     * of those <code>SWT</code> style constants. The class description
     * lists the style constants that are applicable to the class.
     * Style bits are also inherited from superclasses.
     * </p>
     *
     * @param parent a composite control which will be the parent of the new instance (cannot be null)
     * @param style the style of control to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT
     * @see SWT#FLAT
     * @see SWT#HORIZONTAL
     * @see SWT#VERTICAL
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public CoolBar(Composite parent, int style) {
        this(new nat.org.eclipse.swt.widgets.CoolBar((nat.org.eclipse.swt.widgets.Composite) (parent != null ? parent.getDelegate() : null), style));
    }

    protected void checkSubclass() {
        getDelegate().checkSubclass();
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        IPoint ret = getDelegate().computeSize(wHint, hHint, changed);
        return ret != null ? ret.getApi() : null;
    }

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
    public CoolItem getItem(int index) {
        ICoolItem ret = getDelegate().getItem(index);
        return ret != null ? ret.getApi() : null;
    }

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
    public int getItemCount() {
        return getDelegate().getItemCount();
    }

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
    public CoolItem[] getItems() {
        return Convert.array(getDelegate().getItems(), ICoolItem::getApi, CoolItem[]::new);
    }

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
    public int indexOf(CoolItem item) {
        return getDelegate().indexOf((item != null ? item.getDelegate() : null));
    }

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
    public int[] getItemOrder() {
        return getDelegate().getItemOrder();
    }

    public void setBackground(Color color) {
        getDelegate().setBackground((color != null ? color.getDelegate() : null));
    }

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
    public Point[] getItemSizes() {
        return Convert.array(getDelegate().getItemSizes(), IPoint::getApi, Point[]::new);
    }

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
    public boolean getLocked() {
        return getDelegate().getLocked();
    }

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
    public int[] getWrapIndices() {
        return getDelegate().getWrapIndices();
    }

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
    public void setLocked(boolean locked) {
        getDelegate().setLocked(locked);
    }

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
    public void setWrapIndices(int[] indices) {
        getDelegate().setWrapIndices(indices);
    }

    public void setCursor(Cursor cursor) {
        getDelegate().setCursor((cursor != null ? cursor.getDelegate() : null));
    }

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
    public void setItemLayout(int[] itemOrder, int[] wrapIndices, Point[] sizes) {
        getDelegate().setItemLayout(itemOrder, wrapIndices, Convert.array(sizes, Point::getDelegate, IPoint[]::new));
    }

    public void setOrientation(int orientation) {
        getDelegate().setOrientation(orientation);
    }

    protected CoolBar(ICoolBar delegate) {
        super(delegate);
    }

    public static CoolBar createApi(ICoolBar delegate) {
        return new CoolBar(delegate);
    }

    public ICoolBar getDelegate() {
        return (ICoolBar) super.getDelegate();
    }
}
