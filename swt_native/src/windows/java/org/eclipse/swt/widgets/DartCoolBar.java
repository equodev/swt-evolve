/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

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
public class DartCoolBar extends DartComposite implements ICoolBar {

    CoolItem[] items;

    CoolItem[] originalItems;

    boolean locked;

    boolean ignoreResize;

    static final int SEPARATOR_WIDTH = 2;

    static final int MAX_WIDTH = 0x7FFF;

    static final int DEFAULT_COOLBAR_WIDTH = 0;

    static final int DEFAULT_COOLBAR_HEIGHT = 0;

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
    public DartCoolBar(Composite parent, int style, CoolBar api) {
        super(parent, checkStyle(style), api);
        /*
	* Ensure that either of HORIZONTAL or VERTICAL is set.
	* NOTE: HORIZONTAL and VERTICAL have the same values
	* as H_SCROLL and V_SCROLL so it is necessary to first
	* clear these bits to avoid scroll bars and then reset
	* the bits using the original style supplied by the
	* programmer.
	*
	* NOTE: The CCS_VERT style cannot be applied when the
	* widget is created because of this conflict.
	*/
        if ((style & SWT.VERTICAL) != 0) {
            this.getApi().style |= SWT.VERTICAL;
        } else {
            this.getApi().style |= SWT.HORIZONTAL;
        }
    }

    static int checkStyle(int style) {
        style |= SWT.NO_FOCUS;
        /*
	* Even though it is legal to create this widget
	* with scroll bars, they serve no useful purpose
	* because they do not automatically scroll the
	* widget's client area.  The fix is to clear
	* the SWT style.
	*/
        return style & ~(SWT.H_SCROLL | SWT.V_SCROLL);
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    @Override
    void createHandle() {
    }

    void createItem(CoolItem item, int index) {
        int id = 0;
        while (id < items.length && items[id] != null) id++;
        if (id == items.length) {
            CoolItem[] newItems = new CoolItem[items.length + 4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }
        if ((item.style & SWT.DROP_DOWN) != 0) {
        }
        /*
	* Feature in Windows.  When inserting an item at end of a row,
	* sometimes, Windows will begin to place the item on the right
	* side of the cool bar.  The fix is to resize the new items to
	* the maximum size and then resize the next to last item to the
	* ideal size.
	*/
        int lastIndex = getLastIndexOfRow(index - 1);
        boolean fixLast = index == lastIndex + 1;
        if (fixLast) {
        }
        /* Resize the next to last item to the ideal size */
        if (fixLast) {
            resizeToPreferredWidth(lastIndex);
        }
        items[((DartCoolItem) item.getImpl()).id = id] = item;
        int length = originalItems.length;
        int insertIndex = Math.min(index, length);
        CoolItem[] newOriginals = new CoolItem[length + 1];
        System.arraycopy(originalItems, 0, newOriginals, 0, insertIndex);
        System.arraycopy(originalItems, insertIndex, newOriginals, insertIndex + 1, length - insertIndex);
        newOriginals[insertIndex] = item;
        originalItems = newOriginals;
    }

    @Override
    void createWidget() {
        super.createWidget();
        items = new CoolItem[4];
        originalItems = new CoolItem[0];
    }

    void destroyItem(CoolItem item) {
        /*
	* Feature in Windows.  When Windows removed a rebar
	* band, it makes the band child invisible.  The fix
	* is to show the child.
	*/
        Control control = ((DartCoolItem) item.getImpl()).control;
        boolean wasVisible = control != null && !control.isDisposed() && control.getVisible();
        /*
	* When a wrapped item is being deleted, make the next
	* item in the row wrapped in order to preserve the row.
	* In order to avoid an unnecessary layout, temporarily
	* ignore WM_SIZE.  If the next item is wrapped then a
	* row will be deleted and the WM_SIZE is necessary.
	*/
        CoolItem nextItem = null;
        if (((DartCoolItem) item.getImpl()).getWrap()) {
        }
        items[((DartCoolItem) item.getImpl()).id] = null;
        ((DartCoolItem) item.getImpl()).id = -1;
        if (ignoreResize) {
            ((DartCoolItem) nextItem.getImpl()).setWrap(true);
            ignoreResize = false;
        }
        /* Restore the visible state of the control */
        if (wasVisible)
            control.setVisible(true);
        int length = originalItems.length - 1;
        CoolItem[] newOriginals = new CoolItem[length];
        originalItems = newOriginals;
    }

    @Override
    public Control findThemeControl() {
        if ((getApi().style & SWT.FLAT) != 0)
            return this.getApi();
        return background == -1 && backgroundImage == null ? this.getApi() : super.findThemeControl();
    }

    int getMargin(int index) {
        int margin = 0;
        if ((getApi().style & SWT.FLAT) != 0) {
            /*
		* Bug in Windows.  When the style bit  RBS_BANDBORDERS is not set
		* the rectangle returned by RBS_BANDBORDERS is four pixels too small.
		* The fix is to add four pixels to the result.
		*/
            if ((getApi().style & SWT.VERTICAL) != 0) {
            } else {
            }
        } else {
            if ((getApi().style & SWT.VERTICAL) != 0) {
            } else {
            }
        }
        if ((getApi().style & SWT.FLAT) == 0) {
            if (!isLastItemOfRow(index)) {
                margin += DPIUtil.scaleUp(SEPARATOR_WIDTH, getZoom());
            }
        }
        return margin;
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
        checkWidget();
        return this.items[index];
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
        checkWidget();
        return this.items != null ? this.items.length : 0;
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
        checkWidget();
        return this.itemOrder;
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
        checkWidget();
        return this.originalItems;
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
        checkWidget();
        Point[] sizes = getItemSizesInPixels();
        if (sizes != null) {
            for (int i = 0; i < sizes.length; i++) {
                sizes[i] = DPIUtil.scaleDown(sizes[i], getZoom());
            }
        }
        return sizes;
    }

    Point[] getItemSizesInPixels() {
        return null;
    }

    int getLastIndexOfRow(int index) {
        return 0;
    }

    boolean isLastItemOfRow(int index) {
        return false;
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
        checkWidget();
        return locked;
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
        checkWidget();
        CoolItem[] items = getItems();
        int[] indices = new int[items.length];
        int count = 0;
        for (int i = 0; i < items.length; i++) {
            if (((DartCoolItem) items[i].getImpl()).getWrap())
                indices[count++] = i;
        }
        int[] result = new int[count];
        System.arraycopy(indices, 0, result, 0, count);
        return result;
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
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        return 0;
    }

    void resizeToPreferredWidth(int index) {
    }

    void resizeToMaximumWidth(int index) {
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (items != null) {
            for (CoolItem item : items) {
                if (item != null && !item.isDisposed()) {
                    item.getImpl().release(false);
                }
            }
            items = null;
        }
        super.releaseChildren(destroy);
    }

    @Override
    public void removeControl(Control control) {
        super.removeControl(control);
        for (CoolItem item : items) {
            if (item != null && ((DartCoolItem) item.getImpl()).control == control) {
                item.setControl(null);
            }
        }
    }

    @Override
    void reskinChildren(int flags) {
        if (items != null) {
            for (CoolItem item : items) {
                if (item != null)
                    item.reskin(flags);
            }
        }
        super.reskinChildren(flags);
    }

    @Override
    void setBackgroundPixel(int pixel) {
        if (pixel == -1)
            pixel = defaultBackground();
    }

    @Override
    void setForegroundPixel(int pixel) {
        if (pixel == -1)
            pixel = defaultForeground();
    }

    void setItemColors(int foreColor, int backColor) {
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
        checkWidget();
        if (sizes == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        Point[] sizesInPoints = new Point[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            sizesInPoints[i] = DPIUtil.scaleUp(sizes[i], getZoom());
        }
        setItemLayoutInPixels(itemOrder, wrapIndices, sizesInPoints);
    }

    void setItemLayoutInPixels(int[] itemOrder, int[] wrapIndices, Point[] sizes) {
        setRedraw(false);
        setItemOrder(itemOrder);
        setWrapIndices(wrapIndices);
        setItemSizes(sizes);
        setRedraw(true);
    }

    /*
 * Sets the order that the items in the receiver should
 * be displayed in to the given argument which is described
 * in terms of the zero-relative ordering of when the items
 * were added.
 *
 * @param itemOrder the new order to display the items in
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the item order is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the item order is not the same length as the number of items</li>
 * </ul>
 */
    void setItemOrder(int[] itemOrder) {
        dirty();
        if (itemOrder == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        for (int i = 0; i < itemOrder.length; i++) {
        }
        this.itemOrder = itemOrder;
    }

    /*
 * Sets the width and height of the receiver's items to the ones
 * specified by the argument, which is an array of points whose x
 * and y coordinates describe the widths and heights (respectively)
 * in the order in which the items are currently being displayed.
 *
 * @param sizes an array containing the new sizes for each of the receiver's items in visual order
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the array of sizes is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the array of sizes is not the same length as the number of items</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
    void setItemSizes(Point[] sizes) {
        dirty();
        if (sizes == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        this.itemSizes = sizes;
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
        dirty();
        checkWidget();
        this.locked = locked;
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
        dirty();
        checkWidget();
        if (indices == null)
            indices = new int[0];
        int count = getItemCount();
        for (int index : indices) {
            if (index < 0 || index >= count) {
                error(SWT.ERROR_INVALID_RANGE);
            }
        }
        setRedraw(false);
        CoolItem[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            CoolItem item = items[i];
            if (((DartCoolItem) item.getImpl()).getWrap()) {
                resizeToPreferredWidth(i - 1);
                ((DartCoolItem) item.getImpl()).setWrap(false);
            }
        }
        resizeToMaximumWidth(count - 1);
        for (int index : indices) {
            if (0 <= index && index < items.length) {
                CoolItem item = items[index];
                ((DartCoolItem) item.getImpl()).setWrap(true);
                resizeToMaximumWidth(index - 1);
            }
        }
        setRedraw(true);
        this.wrapIndices = indices;
    }

    @Override
    int widgetStyle() {
        return 0;
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof CoolBar coolBar)) {
            return;
        }
        Point[] sizes = ((DartCoolBar) coolBar.getImpl()).getItemSizesInPixels();
        Point[] scaledSizes = new Point[sizes.length];
        Point[] prefSizes = new Point[sizes.length];
        Point[] minSizes = new Point[sizes.length];
        int[] indices = coolBar.getWrapIndices();
        int[] itemOrder = coolBar.getItemOrder();
        CoolItem[] items = coolBar.getItems();
        for (int index = 0; index < sizes.length; index++) {
            minSizes[index] = ((DartCoolItem) items[index].getImpl()).getMinimumSizeInPixels();
            prefSizes[index] = ((DartCoolItem) items[index].getImpl()).getPreferredSizeInPixels();
        }
        for (int index = 0; index < sizes.length; index++) {
            CoolItem item = items[index];
            Control control = ((DartCoolItem) item.getImpl()).control;
            if (control != null) {
                item.setControl(control);
            }
            Point preferredControlSize = ((DartControl) item.getControl().getImpl()).computeSizeInPixels(SWT.DEFAULT, SWT.DEFAULT, true);
            int controlWidth = preferredControlSize.x;
            int controlHeight = preferredControlSize.y;
            if (((coolBar.style & SWT.VERTICAL) != 0)) {
                scaledSizes[index] = new Point(Math.round((sizes[index].x) * scalingFactor), Math.max(Math.round((sizes[index].y) * scalingFactor), 0));
                ((DartCoolItem) item.getImpl()).setMinimumSizeInPixels(Math.round(minSizes[index].x * scalingFactor), Math.max(Math.round((minSizes[index].y) * scalingFactor), controlWidth));
                ((DartCoolItem) item.getImpl()).setPreferredSizeInPixels(Math.round(prefSizes[index].x * scalingFactor), Math.max(Math.round((prefSizes[index].y) * scalingFactor), controlWidth));
            } else {
                scaledSizes[index] = new Point(Math.round((sizes[index].x) * scalingFactor), Math.max(Math.round((sizes[index].y) * scalingFactor), 0));
                ((DartCoolItem) item.getImpl()).setMinimumSizeInPixels(Math.round(minSizes[index].x * scalingFactor), controlHeight);
                ((DartCoolItem) item.getImpl()).setPreferredSizeInPixels(Math.round(prefSizes[index].x * scalingFactor), controlHeight);
            }
        }
        ((DartCoolBar) coolBar.getImpl()).setItemLayoutInPixels(itemOrder, indices, scaledSizes);
        coolBar.getImpl().updateLayout(true);
    }

    int[] itemOrder = new int[0];

    Point[] itemSizes = new Point[0];

    int[] wrapIndices = new int[0];

    public CoolItem[] _items() {
        return items;
    }

    public CoolItem[] _originalItems() {
        return originalItems;
    }

    public boolean _locked() {
        return locked;
    }

    public boolean _ignoreResize() {
        return ignoreResize;
    }

    public int[] _itemOrder() {
        return itemOrder;
    }

    public Point[] _itemSizes() {
        return itemSizes;
    }

    public int[] _wrapIndices() {
        return wrapIndices;
    }

    Point getItemPosition(int index) {
        if (index < 0 || index >= items.length || items[index] == null) {
            return null;
        }
        int x = 0, y = 0;
        boolean isVertical = (getApi().style & SWT.VERTICAL) != 0;
        for (int i = 0; i < index; i++) {
            if (items[i] == null)
                continue;
            Point itemSize = ((DartCoolItem) items[i].getImpl()).getSizeInPixels();
            if (itemSize == null) {
                itemSize = new Point(0, 0);
            }
            boolean wraps = ((DartCoolItem) items[i].getImpl()).getWrap();
            if (isVertical) {
                if (wraps && i > 0) {
                    x += itemSize.x;
                    y = 0;
                } else {
                    y += itemSize.y;
                }
            } else {
                if (wraps && i > 0) {
                    y += itemSize.y;
                    x = 0;
                } else {
                    x += itemSize.x;
                }
            }
        }
        return new Point(x, y);
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    public CoolBar getApi() {
        if (api == null)
            api = CoolBar.createApi(this);
        return (CoolBar) api;
    }

    public VCoolBar getValue() {
        if (value == null)
            value = new VCoolBar(this);
        return (VCoolBar) value;
    }
}
