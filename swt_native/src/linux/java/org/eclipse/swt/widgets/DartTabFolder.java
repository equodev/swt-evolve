/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2025 IBM Corporation and others.
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

    /*
	 * Implementation note (see bug 454936, bug 480794):
	 *
	 * Architecture Change on GTK3:
	 * In TabItem#setControl(Control), we reparent the child to be a child of the 'tab'
	 * rather than tabfolder's parent swtFixed container.
	 * Note, this reparenting is only on the GTK side, not on the SWT side.
	 *
	 * GTK3+:
	 * 	swtFixed
	 * 	|--	GtkNoteBook
	 * 		|-- tabLabel1
	 * 		|-- tabLabel2
	 * 		|-- pageHandle (tabItem1)
	 * 			|-- child1 //child now child of Notebook.pageHandle.
	 * 		|-- pageHandle (tabItem2)
	 * 			|-- child1
	 *
	 * This changes the hierarchy so that children are beneath gtkNotebook (as oppose to
	 * being siblings) and thus fixes DND and background color issues.
	 *
	 * Note about the reason for reparenting:
	 * Reparenting (as opposed to adding widget to a tab in the first place) is necessary
	 * because the SWT API allows situation where you create a child control before you create a TabItem.
	 */
    TabItem[] items;

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

    @Override
    long clientHandle() {
        return getApi().handle;
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    @Override
    Rectangle computeTrimInPixels(int x, int y, int width, int height) {
        checkWidget();
        forceResize();
        if ((getApi().style & SWT.BOTTOM) != 0) {
        } else {
        }
        return new Rectangle(x, y, width, height);
    }

    @Override
    Rectangle getClientAreaInPixels() {
        Rectangle clientRectangle = super.getClientAreaInPixels();
        /*
	* Bug 454936 (see also other 454936 references)
	* SWT's calls to gtk_widget_size_allocate and gtk_widget_set_allocation
	* causes GTK+ to move the clientHandle's SwtFixed down by the size of the labels.
	* These calls can come up from 'shell' and TabFolder has no control over these calls.
	*
	* This is an undesired side-effect. Client handle's x & y positions should never
	* be incremented as this is an internal sub-container.
	*
	* Note: 0 by 0 was chosen as 1 by 1 shifts controls beyond their original pos.
	* The long term fix would be to not use widget_*_allocation from higher containers,
	* but this would require removal of swtFixed.
	*
	* This is Gtk3-specific for Tabfolder as the architecture is changed in gtk3 only.
	*/
        clientRectangle.x = 0;
        clientRectangle.y = 0;
        return clientRectangle;
    }

    @Override
    void createHandle(int index) {
    }

    @Override
    void createWidget(int index) {
        super.createWidget(index);
        items = new TabItem[4];
    }

    void createItem(TabItem item, int index) {
        //int itemCount = 0;
        ;
        int itemCount = getItemCount();
        if (!(0 <= index && index <= itemCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (itemCount == items.length) {
            TabItem[] newItems = new TabItem[items.length + 4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }
        item.state |= HANDLE;
        System.arraycopy(items, index, items, index + 1, itemCount++ - index);
        items[index] = item;
        if ((getApi().state & FOREGROUND) != 0) {
        }
        if ((getApi().state & FONT) != 0) {
            long fontDesc = getFontDescription();
            ((DartTabItem) item.getImpl()).setFontDescription(fontDesc);
        }
        if (itemCount == 1) {
            Event event = new Event();
            event.item = items[0];
            sendSelectionEvent(SWT.Selection, event, false);
            // the widget could be destroyed at this point
        }
    }

    void destroyItem(TabItem item) {
        int index = 0;
        int itemCount = getItemCount();
        while (index < itemCount) {
            if (items[index] == item)
                break;
            index++;
        }
        if (index == itemCount)
            error(SWT.ERROR_ITEM_NOT_REMOVED);
        System.arraycopy(items, index + 1, items, index, --itemCount - index);
        items[itemCount] = null;
    }

    @Override
    long eventHandle() {
        return getApi().handle;
    }

    @Override
    public Control[] _getChildren() {
        Control[] directChildren = super._getChildren();
        int directCount = directChildren.length;
        int itemCount = items == null ? 0 : items.length;
        Control[] children = new Control[itemCount + directCount];
        int childrenCount = 0;
        for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
            TabItem tabItem = items[itemIndex];
            if (tabItem != null && !tabItem.isDisposed()) {
            }
        }
        if (childrenCount == itemCount + directCount) {
            return children;
        } else {
            Control[] newChildren;
            if (childrenCount == itemCount) {
                newChildren = children;
            } else {
                newChildren = new Control[childrenCount + directCount];
                System.arraycopy(children, 0, newChildren, 0, childrenCount);
            }
            System.arraycopy(directChildren, 0, newChildren, childrenCount, directCount);
            return newChildren;
        }
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
        if (!(0 <= index && index < getItemCount()))
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
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            TabItem item = items[i];
            Rectangle rect = item.getBounds();
            if (rect.contains(point))
                return item;
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
        return this.selection;
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
    void hookEvents() {
        super.hookEvents();
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
        int index = -1;
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (items[i] == item) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    Point minimumSize(int wHint, int hHint, boolean flushCache) {
        Control[] children = _getChildren();
        int width = 0, height = 0;
        for (int i = 0; i < children.length; i++) {
            Control child = children[i];
            int index = 0;
            int count = getItemCount();
            while (index < count) {
                if (((DartTabItem) items[index].getImpl()).control == child)
                    break;
                index++;
            }
            if (index == count) {
                Rectangle rect = child.getBounds();
                width = Math.max(width, rect.x + rect.width);
                height = Math.max(height, rect.y + rect.height);
            } else {
                /*
			 * Since computeSize can be overridden by subclasses, we cannot
			 * call computeSizeInPixels directly.
			 */
                Point size = child.computeSize(wHint, hHint, flushCache);
                width = Math.max(width, size.x);
                height = Math.max(height, size.y);
            }
        }
        return new Point(width, height);
    }

    @Override
    boolean mnemonicHit(char key) {
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            long labelHandle = ((DartTabItem) items[i].getImpl()).labelHandle;
            if (labelHandle != 0 && mnemonicHit(labelHandle, key))
                return true;
        }
        return false;
    }

    @Override
    boolean mnemonicMatch(char key) {
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            long labelHandle = ((DartTabItem) items[i].getImpl()).labelHandle;
            if (labelHandle != 0 && mnemonicHit(labelHandle, key))
                return true;
        }
        return false;
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
    void releaseWidget() {
        super.releaseWidget();
    }

    @Override
    public void removeControl(Control control) {
        super.removeControl(control);
        int count = getItemCount();
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
            int count = getItemCount();
            for (int i = 0; i < count; i++) {
                TabItem item = items[i];
                if (item != null)
                    item.reskin(flags);
            }
        }
        super.reskinChildren(flags);
    }

    @Override
    int setBounds(int x, int y, int width, int height, boolean move, boolean resize) {
        int result = super.setBounds(x, y, width, height, move, resize);
        if ((result & RESIZED) != 0) {
            int index = getSelectionIndex();
            if (index != -1) {
                TabItem item = items[index];
                Control control = ((DartTabItem) item.getImpl()).control;
                if (control != null && !control.isDisposed()) {
                    ((DartControl) control.getImpl()).setBoundsInPixels(getClientAreaInPixels());
                }
            }
        }
        return result;
    }

    @Override
    void setFontDescription(long font) {
        super.setFontDescription(font);
        TabItem[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                ((DartTabItem) items[i].getImpl()).setFontDescription(font);
            }
        }
    }

    @Override
    void setOrientation(boolean create) {
        super.setOrientation(create);
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null)
                    ((DartTabItem) items[i].getImpl()).setOrientation(create);
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
        dirty();
        checkWidget();
        if (!(0 <= index && index < getItemCount()))
            return;
        setSelection(index, false);
    }

    void setSelection(int index, boolean notify) {
        if (index < 0 || index >= items.length || items[index] == null) {
            this.selection = new TabItem[0];
        } else {
            this.selection = new TabItem[] { items[index] };
        }
        dirty();
        if (index < 0)
            return;
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
        checkWidget();
        if (items == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (items.length == 0) {
            setSelection(-1, false);
        } else {
            for (int i = items.length - 1; i >= 0; --i) {
                int index = indexOf(items[i]);
                if (index != -1)
                    setSelection(index, false);
            }
        }
    }

    @Override
    boolean traversePage(final boolean next) {
        if (next) {
        } else {
        }
        return true;
    }

    TabItem[] selection = new TabItem[0];

    public TabItem[] _items() {
        return items;
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
                setSelection(e.index, true);
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
