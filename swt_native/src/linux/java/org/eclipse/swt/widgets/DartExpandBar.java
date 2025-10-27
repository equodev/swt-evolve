/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class support the layout of selectable
 * expand bar items.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>ExpandItem</code>.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>V_SCROLL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Expand, Collapse</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see ExpandItem
 * @see ExpandEvent
 * @see ExpandListener
 * @see ExpandAdapter
 * @see <a href="http://www.eclipse.org/swt/snippets/#expandbar">ExpandBar snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartExpandBar extends DartComposite implements IExpandBar {

    ExpandItem[] items;

    ExpandItem lastFocus;

    int itemCount;

    int spacing;

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
     * @see SWT#V_SCROLL
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartExpandBar(Composite parent, int style, ExpandBar api) {
        super(parent, style, api);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when an item in the receiver is expanded or collapsed
     * by sending it one of the messages defined in the <code>ExpandListener</code>
     * interface.
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see ExpandListener
     * @see #removeExpandListener
     */
    public void addExpandListener(ExpandListener listener) {
        addTypedListener(listener, SWT.Expand, SWT.Collapse);
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
    void createHandle(int index) {
        // In GTK 3 font description is inherited from parent widget which is not how SWT has always worked,
    }

    void createItem(ExpandItem item, int style, int index) {
        if (!(0 <= index && index <= itemCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (itemCount == items.length) {
            ExpandItem[] newItems = new ExpandItem[itemCount + 4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }
        System.arraycopy(items, index, items, index + 1, itemCount - index);
        items[index] = item;
        itemCount++;
        ((DartExpandItem) item.getImpl()).width = Math.max(0, getClientAreaInPixels().width - spacing * 2);
        layoutItems();
    }

    @Override
    void createWidget(int index) {
        super.createWidget(index);
        items = new ExpandItem[4];
    }

    void destroyItem(ExpandItem item) {
        int index = 0;
        while (index < itemCount) {
            if (items[index] == item)
                break;
            index++;
        }
        if (index == itemCount)
            return;
        System.arraycopy(items, index + 1, items, index, --itemCount - index);
        items[itemCount] = null;
        layoutItems();
    }

    @Override
    long eventHandle() {
        return fixedHandle;
    }

    @Override
    boolean hasFocus() {
        for (int i = 0; i < itemCount; i++) {
            ExpandItem item = items[i];
            if (((DartExpandItem) item.getImpl()).hasFocus())
                return true;
        }
        return super.hasFocus();
    }

    @Override
    void hookEvents() {
        super.hookEvents();
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
    public ExpandItem getItem(int index) {
        checkWidget();
        if (!(0 <= index && index < itemCount))
            error(SWT.ERROR_INVALID_RANGE);
        return items[index];
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
     * Returns an array of <code>ExpandItem</code>s which are the items
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
    public ExpandItem[] getItems() {
        checkWidget();
        ExpandItem[] result = new ExpandItem[itemCount];
        System.arraycopy(items, 0, result, 0, itemCount);
        return result;
    }

    /**
     * Returns the receiver's spacing.
     *
     * @return the spacing
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getSpacing() {
        checkWidget();
        return DPIUtil.autoScaleDown(spacing);
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
     *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the item has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int indexOf(ExpandItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        for (int i = 0; i < itemCount; i++) {
            if (items[i] == item)
                return i;
        }
        return -1;
    }

    void layoutItems() {
        for (int i = 0; i < itemCount; i++) {
            ExpandItem item = items[i];
            if (item != null)
                ((DartExpandItem) item.getImpl()).resizeControl();
        }
    }

    @Override
    public long parentingHandle() {
        return fixedHandle;
    }

    @Override
    void releaseChildren(boolean destroy) {
        for (int i = 0; i < itemCount; i++) {
            ExpandItem item = items[i];
            if (item != null && !item.isDisposed()) {
                item.getImpl().release(false);
            }
        }
        super.releaseChildren(destroy);
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when items in the receiver are expanded or collapsed.
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
     * @see ExpandListener
     * @see #addExpandListener
     */
    public void removeExpandListener(ExpandListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Expand, listener);
        eventTable.unhook(SWT.Collapse, listener);
    }

    @Override
    void reskinChildren(int flags) {
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                ExpandItem item = items[i];
                if (item != null)
                    item.reskin(flags);
            }
        }
        super.reskinChildren(flags);
    }

    @Override
    void setWidgetBackground() {
    }

    @Override
    void setFontDescription(long font) {
        super.setFontDescription(font);
        for (int i = 0; i < itemCount; i++) {
            ((DartExpandItem) items[i].getImpl()).setFontDescription(font);
        }
        layoutItems();
    }

    @Override
    void setOrientation(boolean create) {
        super.setOrientation(create);
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null)
                    ((DartExpandItem) items[i].getImpl()).setOrientation(create);
            }
        }
    }

    /**
     * Sets the receiver's spacing. Spacing specifies the number of points allocated around
     * each item.
     *
     * @param spacing the spacing around each item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSpacing(int spacing) {
        checkWidget();
        setSpacingInPixels(DPIUtil.autoScaleUp(spacing));
    }

    void setSpacingInPixels(int spacing) {
        dirty();
        checkWidget();
        if (spacing < 0)
            return;
        if (spacing == this.spacing)
            return;
        this.spacing = spacing;
    }

    @Override
    void updateScrollBarValue(ScrollBar bar) {
        layoutItems();
    }

    public ExpandItem[] _items() {
        return items;
    }

    public ExpandItem _lastFocus() {
        return lastFocus;
    }

    public int _itemCount() {
        return itemCount;
    }

    public int _spacing() {
        return spacing;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Expand", "Collapse", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Collapse, e);
            });
        });
        FlutterBridge.on(this, "Expand", "Expand", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Expand, e);
            });
        });
    }

    public ExpandBar getApi() {
        if (api == null)
            api = ExpandBar.createApi(this);
        return (ExpandBar) api;
    }

    public VExpandBar getValue() {
        if (value == null)
            value = new VExpandBar(this);
        return (VExpandBar) value;
    }
}
