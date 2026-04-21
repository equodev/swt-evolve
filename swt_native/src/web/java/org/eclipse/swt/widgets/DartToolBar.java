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
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;
import dev.equo.swt.*;

/**
 * Instances of this class support the layout of selectable
 * tool bar items.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>ToolItem</code>.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to add <code>Control</code> children to it,
 * or set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>FLAT, WRAP, RIGHT, HORIZONTAL, VERTICAL, SHADOW_OUT</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#toolbar">ToolBar, ToolItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartToolBar extends DartComposite implements IToolBar {

    int itemCount;

    ToolItem[] items;

    ToolItem lastFocus;

    static int NEXT_ID;

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
     * @see SWT#FLAT
     * @see SWT#WRAP
     * @see SWT#RIGHT
     * @see SWT#HORIZONTAL
     * @see SWT#SHADOW_OUT
     * @see SWT#VERTICAL
     * @see Widget#checkSubclass()
     * @see Widget#getStyle()
     */
    public DartToolBar(Composite parent, int style, ToolBar api) {
        this(parent, style, false, api);
    }

    DartToolBar(Composite parent, int style, boolean internal, ToolBar api) {
        super(parent, checkStyle(parent, style, internal), api);
        /*
	* Ensure that either of HORIZONTAL or VERTICAL is set.
	* NOTE: HORIZONTAL and VERTICAL have the same values
	* as H_SCROLL and V_SCROLL so it is necessary to first
	* clear these bits to avoid scroll bars and then reset
	* the bits using the original style supplied by the
	* programmer.
	*/
        if ((style & SWT.VERTICAL) != 0) {
            this.getApi().style |= SWT.VERTICAL;
        } else {
            this.getApi().style |= SWT.HORIZONTAL;
        }
    }

    static int checkStyle(Composite parent, int style, boolean internal) {
        /*
	* Even though it is legal to create this widget
	* with scroll bars, they serve no useful purpose
	* because they do not automatically scroll the
	* widget's client area.  The fix is to clear
	* the SWT style.
	*/
        int newStyle = style & ~(SWT.H_SCROLL | SWT.V_SCROLL);
        /*
	 * Only internal clients can create an NSToolbar-based ToolBar.
	 */
        if (!internal && (newStyle & SWT.SMOOTH) != 0) {
            newStyle &= ~SWT.SMOOTH;
        }
        /*
	 * A unified toolbar can only be parented to a Shell, and
	 * there can only be one unified toolbar per shell. If neither of these
	 * conditions hold, turn off the SMOOTH flag.
	 */
        if ((style & SWT.SMOOTH) != 0) {
            if (parent instanceof Shell s) {
            } else {
                newStyle &= ~SWT.SMOOTH;
            }
        }
        /*
	 * Unified toolbar only supports a horizontal layout, and doesn't wrap.
	 */
        if ((newStyle & SWT.SMOOTH) != 0) {
            newStyle &= ~(SWT.VERTICAL | SWT.WRAP);
            newStyle |= SWT.HORIZONTAL;
        }
        return newStyle;
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return Sizes.computeSize(this, wHint, hHint, changed);
    }

    @Override
    public Rectangle computeTrim(int x, int y, int width, int height) {
        return Sizes.computeTrim(this, x, y, width, height);
    }

    @Override
    void createHandle() {
    }

    void createItem(ToolItem item, int index) {
        if (!(0 <= index && index <= itemCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (itemCount == items.length) {
            ToolItem[] newItems = new ToolItem[itemCount + 4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }
        ((DartWidget) item.getImpl()).createWidget();
        System.arraycopy(items, index, items, index + 1, itemCount++ - index);
        items[index] = item;
        relayout();
    }

    @Override
    void createWidget() {
        super.createWidget();
        items = new ToolItem[4];
        itemCount = 0;
    }

    @Override
    void deregister() {
        super.deregister();
    }

    void destroyItem(ToolItem item) {
        int index = 0;
        while (index < itemCount) {
            if (items[index] == item)
                break;
            index++;
        }
        if (index == itemCount)
            return;
        if (item == lastFocus)
            lastFocus = null;
        System.arraycopy(items, index + 1, items, index, --itemCount - index);
        items[itemCount] = null;
        relayout();
    }

    @Override
    void enableWidget(boolean enabled) {
        super.enableWidget(enabled);
        for (int i = 0; i < itemCount; i++) {
            ToolItem item = items[i];
            if (item != null) {
                ((DartToolItem) item.getImpl()).enableWidget(enabled && item.getEnabled());
            }
        }
    }

    @Override
    void setZOrder() {
        super.setZOrder();
    }

    @Override
    public Rectangle getBounds() {
        checkWidget();
        return super.getBounds();
    }

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
    public ToolItem getItem(int index) {
        checkWidget();
        if (0 <= index && index < itemCount)
            return items[index];
        error(SWT.ERROR_INVALID_RANGE);
        return null;
    }

    /**
     * Returns the item at the given point in the receiver
     * or null if no such item exists. The point is in the
     * coordinate system of the receiver.
     *
     * @param point the point used to locate the item
     * @return the item at the given point
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public ToolItem getItem(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        for (int i = 0; i < itemCount; i++) {
            Rectangle rect = items[i].getBounds();
            if (rect.contains(point))
                return items[i];
        }
        return null;
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
        return itemCount;
    }

    /**
     * Returns an array of <code>ToolItem</code>s which are the items
     * in the receiver.
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
    public ToolItem[] getItems() {
        checkWidget();
        ToolItem[] result = new ToolItem[itemCount];
        System.arraycopy(items, 0, result, 0, itemCount);
        return result;
    }

    /**
     * Returns the number of rows in the receiver. When
     * the receiver has the <code>WRAP</code> style, the
     * number of rows can be greater than one.  Otherwise,
     * the number of rows is always one.
     *
     * @return the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getRowCount() {
        checkWidget();
        Rectangle rect = getClientArea();
        return layout(rect.width, rect.height, false)[0];
    }

    @Override
    boolean hasKeyboardFocus(long inId) {
        return hasFocus();
    }

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
     *    <li>ERROR_NULL_ARGUMENT - if the tool item is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the tool item has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int indexOf(ToolItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        for (int i = 0; i < itemCount; i++) {
            if (items[i] == item)
                return i;
        }
        return -1;
    }

    int[] layoutHorizontal(int width, int height, boolean resize) {
        int xSpacing = 0, ySpacing = 2;
        int marginWidth = 0, marginHeight = 0;
        int x = marginWidth, y = marginHeight;
        int maxX = 0, rows = 1;
        boolean wrap = (getApi().style & SWT.WRAP) != 0;
        int itemHeight = 0;
        Point[] sizes = new Point[itemCount];
        for (int i = 0; i < itemCount; i++) {
            Point size = sizes[i] = ((DartToolItem) items[i].getImpl()).computeSize();
            itemHeight = Math.max(itemHeight, size.y);
        }
        for (int i = 0; i < itemCount; i++) {
            ToolItem item = items[i];
            Point size = sizes[i];
            if (wrap && i != 0 && x + size.x > width) {
                rows++;
                x = marginWidth;
                y += ySpacing + itemHeight;
            }
            if (resize) {
                ((DartToolItem) item.getImpl()).setBounds(x, y, size.x, itemHeight);
                boolean visible = x + size.x <= width && y + itemHeight <= height;
                ((DartToolItem) item.getImpl()).setVisible(visible);
                Control control = ((DartToolItem) item.getImpl()).control;
                if (control != null) {
                    control.setBounds(x, y, size.x, itemHeight);
                }
            }
            x += xSpacing + size.x;
            maxX = Math.max(maxX, x);
        }
        return new int[] { rows, maxX, y + itemHeight };
    }

    int[] layoutUnified(int width, int height, boolean resize) {
        int x = 0, y = 0;
        int itemHeight = 0, maxX = 0;
        Point[] sizes = new Point[itemCount];
        // This next line relies on the observation that all of the toolbar item views are children of the first
        for (int i = 0; i < itemCount; i++) {
            Point size = sizes[i] = ((DartToolItem) items[i].getImpl()).computeSize();
            itemHeight = Math.max(itemHeight, size.y);
        }
        for (int i = 0; i < itemCount; i++) {
            ToolItem item = items[i];
            Point size = sizes[i];
            if (resize) {
                ((DartToolItem) item.getImpl()).setBounds(0, 0, size.x, itemHeight);
                Control control = ((DartToolItem) item.getImpl()).control;
                if (control != null) {
                    control.setBounds(x, y, size.x, itemHeight);
                }
            }
            maxX = Math.max(maxX, x);
        }
        return new int[] { 1, maxX, itemHeight };
    }

    int[] layoutVertical(int width, int height, boolean resize) {
        int xSpacing = 2, ySpacing = 0;
        int marginWidth = 0, marginHeight = 0;
        int x = marginWidth, y = marginHeight;
        int maxY = 0, cols = 1;
        boolean wrap = (getApi().style & SWT.WRAP) != 0;
        int itemWidth = 0;
        Point[] sizes = new Point[itemCount];
        for (int i = 0; i < itemCount; i++) {
            Point size = sizes[i] = ((DartToolItem) items[i].getImpl()).computeSize();
            itemWidth = Math.max(itemWidth, size.x);
        }
        for (int i = 0; i < itemCount; i++) {
            ToolItem item = items[i];
            Point size = sizes[i];
            if (wrap && i != 0 && y + size.y > height) {
                cols++;
                x += xSpacing + itemWidth;
                y = marginHeight;
            }
            if (resize) {
                ((DartToolItem) item.getImpl()).setBounds(x, y, itemWidth, size.y);
                boolean visible = x + itemWidth <= width && y + size.y <= height;
                ((DartToolItem) item.getImpl()).setVisible(visible);
                Control control = ((DartToolItem) item.getImpl()).control;
                if (control != null) {
                    control.setBounds(x, y, itemWidth, size.y);
                }
            }
            y += ySpacing + size.y;
            maxY = Math.max(maxY, y);
        }
        return new int[] { cols, x + itemWidth, maxY };
    }

    int[] layout(int nWidth, int nHeight, boolean resize) {
        if ((getApi().style & SWT.VERTICAL) != 0) {
            return layoutVertical(nWidth, nHeight, resize);
        } else {
            return layoutHorizontal(nWidth, nHeight, resize);
        }
    }

    @Override
    void register() {
        super.register();
    }

    void relayout() {
        if (!getDrawing())
            return;
        Rectangle rect = getClientArea();
        layout(rect.width, rect.height, true);
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (items != null) {
            for (int i = 0; i < itemCount; i++) {
                ToolItem item = items[i];
                if (item != null && !item.isDisposed()) {
                    item.getImpl().release(false);
                }
            }
            itemCount = 0;
            items = null;
        }
        super.releaseChildren(destroy);
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
    }

    @Override
    public void removeControl(Control control) {
        super.removeControl(control);
        for (int i = 0; i < itemCount; i++) {
            ToolItem item = items[i];
            if (((DartToolItem) item.getImpl()).control == control)
                item.setControl(null);
        }
    }

    @Override
    void resized() {
        super.resized();
        relayout();
    }

    @Override
    void reskinChildren(int flags) {
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                ToolItem item = items[i];
                if (item != null)
                    item.reskin(flags);
            }
        }
        super.reskinChildren(flags);
    }

    @Override
    void setBounds(int x, int y, int width, int height, boolean move, boolean resize) {
        // In the unified toolbar case, the toolbar view size and position is completely controlled
        super.setBounds(x, y, width, height, move, resize);
    }

    @Override
    void setForeground(double[] color) {
        super.setForeground(color);
        for (int i = 0; i < itemCount; i++) {
            ((DartToolItem) items[i].getImpl()).updateStyle();
        }
    }

    @Override
    public void setRedraw(boolean redraw) {
        dirty();
        checkWidget();
        super.setRedraw(redraw);
        if (redraw && drawCount == 0)
            relayout();
    }

    @Override
    public void setVisible(boolean visible) {
        dirty();
        super.setVisible(visible);
    }

    public int _itemCount() {
        return itemCount;
    }

    public ToolItem[] _items() {
        return items;
    }

    public ToolItem _lastFocus() {
        return lastFocus;
    }

    protected void _hookEvents() {
        super._hookEvents();
    }

    public ToolBar getApi() {
        if (api == null)
            api = ToolBar.createApi(this);
        return (ToolBar) api;
    }

    public VToolBar getValue() {
        if (value == null)
            value = new VToolBar(this);
        return (VToolBar) value;
    }
}
