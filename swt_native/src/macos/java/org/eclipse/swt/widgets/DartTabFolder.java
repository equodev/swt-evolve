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
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import java.util.Objects;
import dev.equo.swt.*;

/**
 * Instances of this class implement the notebook user interface
 * metaphor.  It allows the user to select a notebook page from
 * set of pages.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>TabItem</code>.
 * <code>Control</code> children are created and then set into a
 * tab item using <code>TabItem#setControl</code>.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>TOP, BOTTOM</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles TOP and BOTTOM may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#tabfolder">TabFolder, TabItem snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartTabFolder extends DartComposite implements ITabFolder {

    TabItem[] items;

    int itemCount;

    boolean ignoreSelect;

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
     * @see SWT#TOP
     * @see SWT#BOTTOM
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTabFolder(Composite parent, int style, TabFolder api) {
        super(parent, checkStyle(style), api);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the user changes the receiver's selection, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * When <code>widgetSelected</code> is called, the item field of the event object is valid.
     * <code>widgetDefaultSelected</code> is not called.
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
    public void addSelectionListener(SelectionListener listener) {
        addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
    }

    static int checkStyle(int style) {
        style = checkBits(style, SWT.TOP, SWT.BOTTOM, 0, 0, 0, 0);
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

    void createItem(TabItem item, int index) {
        int count = itemCount;
        if (!(0 <= index && index <= count))
            error(SWT.ERROR_INVALID_RANGE);
        if (count == items.length) {
            TabItem[] newItems = new TabItem[items.length + 4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }
        System.arraycopy(items, index, items, index + 1, count - index);
        if (index >= items.length) {
            TabItem[] newItems = new TabItem[items.length * 2];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }
        items[index] = item;
        itemCount++;
        ((DartWidget) item.getImpl()).createJNIRef();
        ((DartTabItem) item.getImpl()).register();
    }

    @Override
    void createWidget() {
        super.createWidget();
        items = new TabItem[4];
    }

    void destroyItem(TabItem item) {
        int count = itemCount;
        int index = 0;
        while (index < count) {
            if (items[index] == item)
                break;
            index++;
        }
        if (index == count)
            return;
        --count;
        System.arraycopy(items, index + 1, items, index, count - index);
        items[count] = null;
        if (count == 0) {
            items = new TabItem[4];
        }
        itemCount = count;
    }

    @Override
    public Rectangle getClientArea() {
        return Sizes.getClientArea(this);
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
    public TabItem getItem(int index) {
        checkWidget();
        int count = itemCount;
        if (!(0 <= index && index < count))
            error(SWT.ERROR_INVALID_RANGE);
        return items[index];
    }

    /**
     * Returns the tab item at the given point in the receiver
     * or null if no such item exists. The point is in the
     * coordinate system of the receiver.
     *
     * @param point the point used to locate the item
     * @return the tab item at the given point, or null if the point is not in a tab item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    public TabItem getItem(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        for (int i = 0; i < itemCount; i++) {
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
        int count = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null)
                count++;
        }
        return count;
    }

    /**
     * Returns an array of <code>TabItem</code>s which are the items
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
    public TabItem[] getItems() {
        checkWidget();
        int count = getItemCount();
        TabItem[] result = new TabItem[count];
        int index = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                result[index++] = items[i];
            }
        }
        return result;
    }

    /**
     * Returns an array of <code>TabItem</code>s that are currently
     * selected in the receiver. An empty array indicates that no
     * items are selected.
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
    public TabItem[] getSelection() {
        checkWidget();
        int index = getSelectionIndex();
        if (index == -1)
            return new TabItem[0];
        return new TabItem[] { items[index] };
    }

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
    public int getSelectionIndex() {
        checkWidget();
        if (this.selection != null && this.selection.length > 0) {
            return indexOf(this.selection[0]);
        }
        return -1;
    }

    @Override
    public float getThemeAlpha() {
        return (background != null ? 1 : 0.25f) * parent.getImpl().getThemeAlpha();
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
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int indexOf(TabItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        int count = itemCount;
        for (int i = 0; i < count; i++) {
            if (items[i] == item)
                return i;
        }
        for (int i = 0; i < items.length; i++) {
            if (items[i] == item)
                return i;
        }
        return -1;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    Point minimumSize(int wHint, int hHint, boolean flushCache) {
        return Sizes.minimumSize(this, wHint, hHint, flushCache);
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                TabItem item = items[i];
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
        int count = itemCount;
        for (int i = 0; i < count; i++) {
            TabItem item = items[i];
            if (((DartTabItem) item.getImpl()).control == control)
                item.setControl(null);
        }
    }

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
     * @see #addSelectionListener
     */
    public void removeSelectionListener(SelectionListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Selection, listener);
        eventTable.unhook(SWT.DefaultSelection, listener);
    }

    @Override
    void reskinChildren(int flags) {
        if (items != null) {
            for (int i = 0; i < itemCount; i++) {
                TabItem item = items[i];
                if (item != null)
                    item.reskin(flags);
            }
        }
        super.reskinChildren(flags);
    }

    @Override
    void setForeground(double[] color) {
        super.setForeground(color);
        int index = getSelectionIndex();
        for (int i = 0; i < itemCount; i++) {
            ((DartTabItem) items[i].getImpl()).updateText(i == index);
        }
    }

    /**
     * Sets the receiver's selection to the given item.
     * The current selected is first cleared, then the new item is
     * selected.
     *
     * @param item the item to select
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the item is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public void setSelection(TabItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setSelection(new TabItem[] { item });
    }

    /**
     * Sets the receiver's selection to be the given array of items.
     * The current selected is first cleared, then the new items are
     * selected.
     *
     * @param items the array of items
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the items array is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(TabItem[] items) {
        TabItem[] newValue = items;
        if (!java.util.Objects.equals(this.selection, newValue)) {
            dirty();
        }
        checkWidget();
        if (items == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        this.selection = newValue;
        if (items.length == 0) {
            setSelection(-1, false, false);
        } else {
            for (int i = items.length - 1; i >= 0; --i) {
                int index = indexOf(items[i]);
                if (index != -1)
                    setSelection(index, false, false);
            }
        }
    }

    /**
     * Selects the item at the given zero-relative index in the receiver.
     * If the item at the index was already selected, it remains selected.
     * The current selection is first cleared, then the new items are
     * selected. Indices that are out of range are ignored.
     *
     * @param index the index of the item to select
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(int index) {
        checkWidget();
        int count = itemCount;
        if (!(0 <= index && index < count))
            return;
        setSelection(index, false, false);
    }

    void setSelection(int index, boolean notify, boolean force) {
        dirty();
        if (!(0 <= index && index < itemCount))
            return;
        int currentIndex = getSelectionIndex();
        if (!force && currentIndex == index)
            return;
        if (currentIndex != -1) {
            TabItem item = items[currentIndex];
            if (item != null) {
                Control control = ((DartTabItem) item.getImpl()).control;
                if (control != null && !control.isDisposed()) {
                    control.setVisible(false);
                }
            }
        }
        // Update the selection field
        if (index < 0 || index >= items.length || items[index] == null) {
            this.selection = new TabItem[0];
        } else {
            this.selection = new TabItem[] { items[index] };
        }
        //ignoreSelect = true;
        ;
        ignoreSelect = false;
        //index = getSelectionIndex();
        ;
        if (index != -1) {
            TabItem item = items[index];
            if (item != null) {
                Control control = ((DartTabItem) item.getImpl()).control;
                if (control != null && !control.isDisposed()) {
                    control.setVisible(true);
                }
                if (notify) {
                    Event event = new Event();
                    event.item = item;
                    sendSelectionEvent(SWT.Selection, event, true);
                }
            }
        }
    }

    @Override
    void setSmallSize() {
    }

    @Override
    boolean traversePage(boolean next) {
        int count = getItemCount();
        if (count == 0)
            return false;
        int index = getSelectionIndex();
        if (index == -1) {
            index = 0;
        } else {
            int offset = (next) ? 1 : -1;
            index = (index + offset + count) % count;
        }
        setSelection(index, true, false);
        return index == getSelectionIndex();
    }

    TabItem[] selection = new TabItem[0];

    public TabItem[] _items() {
        return items;
    }

    public int _itemCount() {
        return itemCount;
    }

    public boolean _ignoreSelect() {
        return ignoreSelect;
    }

    public TabItem[] _selection() {
        return selection;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.DefaultSelection, e);
            });
        });
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                setSelection(e.index);
            });
        });
    }

    public TabFolder getApi() {
        if (api == null)
            api = TabFolder.createApi(this);
        return (TabFolder) api;
    }

    public VTabFolder getValue() {
        if (value == null)
            value = new VTabFolder(this);
        return (VTabFolder) value;
    }
}
