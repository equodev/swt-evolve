/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2021 IBM Corporation and others.
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
import dev.equo.swt.*;

/**
 * Instances of this class provide a selectable user interface object
 * that displays a hierarchy of items and issues notification when an
 * item in the hierarchy is selected.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>TreeItem</code>.
 * </p><p>
 * Style <code>VIRTUAL</code> is used to create a <code>Tree</code> whose
 * <code>TreeItem</code>s are to be populated by the client on an on-demand basis
 * instead of up-front.  This can provide significant performance improvements for
 * trees that are very large or for which <code>TreeItem</code> population is
 * expensive (for example, retrieving values from an external source).
 * </p><p>
 * Here is an example of using a <code>Tree</code> with style <code>VIRTUAL</code>:</p>
 * <pre><code>
 *  final Tree tree = new Tree(parent, SWT.VIRTUAL | SWT.BORDER);
 *  tree.setItemCount(20);
 *  tree.addListener(SWT.SetData, new Listener() {
 *      public void handleEvent(Event event) {
 *          TreeItem item = (TreeItem)event.item;
 *          TreeItem parentItem = item.getParentItem();
 *          String text = null;
 *          if (parentItem == null) {
 *              text = "node " + tree.indexOf(item);
 *          } else {
 *              text = parentItem.getText() + " - " + parentItem.indexOf(item);
 *          }
 *          item.setText(text);
 *          System.out.println(text);
 *          item.setItemCount(10);
 *      }
 *  });
 * </code></pre>
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not normally make sense to add <code>Control</code> children to
 * it, or set a layout on it, unless implementing something like a cell
 * editor.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SINGLE, MULTI, CHECK, FULL_SELECTION, VIRTUAL, NO_SCROLL, NO_SEARCH</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection, Collapse, Expand, SetData, MeasureItem, EraseItem, PaintItem, EmptinessChanged</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles SINGLE and MULTI may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#tree">Tree, TreeItem, TreeColumn snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartTree extends DartComposite implements ITree {

    private static final TreeItem[] NO_ITEM = new TreeItem[0];

    TreeItem[] items;

    int itemCount;

    TreeColumn[] columns;

    TreeColumn sortColumn;

    int columnCount;

    int sortDirection;

    int selectedRowIndex = -1;

    boolean ignoreExpand, ignoreSelect, ignoreRedraw, reloadPending, drawExpansion, didSelect, preventSelect, dragDetected;

    Rectangle imageBounds;

    TreeItem insertItem;

    boolean insertBefore;

    double[] headerBackground, headerForeground;

    /* Used to control drop feedback when DND.FEEDBACK_EXPAND and DND.FEEDBACK_SCROLL is set/not set */
    boolean shouldExpand = true, shouldScroll = true;

    boolean keyDown;

    static int NEXT_ID;

    /*
	 * Value has been determined experimentally, see bug 516472.
	 * On macOS 10.12, right end of expando triangle is at x=16.
	 * On macOS 11, value less than 19 doesn't work.
	 */
    static final int FIRST_COLUMN_MINIMUM_WIDTH = 19;

    static final int IMAGE_GAP = 3;

    static final int TEXT_GAP = 2;

    static final int CELL_GAP = 1;

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
     * @see SWT#SINGLE
     * @see SWT#MULTI
     * @see SWT#CHECK
     * @see SWT#FULL_SELECTION
     * @see SWT#VIRTUAL
     * @see SWT#NO_SCROLL
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTree(Composite parent, int style, Tree api) {
        super(parent, checkStyle(style), api);
    }

    @Override
    void _addListener(int eventType, Listener listener) {
        super._addListener(eventType, listener);
        clearCachedWidth(items);
    }

    TreeItem _getItem(TreeItem parentItem, int index, boolean create) {
        int count;
        TreeItem[] items;
        if (parentItem != null) {
            count = ((DartTreeItem) parentItem.getImpl()).itemCount;
            items = ((DartTreeItem) parentItem.getImpl()).items;
        } else {
            count = this.itemCount;
            items = this.items;
        }
        if (index < 0 || index >= count)
            return null;
        TreeItem item = items[index];
        if (item != null || (getApi().style & SWT.VIRTUAL) == 0 || !create)
            return item;
        item = new TreeItem(this.getApi(), parentItem, SWT.NONE, index, false);
        items[index] = item;
        return item;
    }

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
    public void addSelectionListener(SelectionListener listener) {
        addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when an item in the receiver is expanded or collapsed
     * by sending it one of the messages defined in the <code>TreeListener</code>
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
     * @see TreeListener
     * @see #removeTreeListener
     */
    public void addTreeListener(TreeListener listener) {
        addTypedListener(listener, SWT.Expand, SWT.Collapse);
    }

    int calculateWidth(TreeItem[] items, int index, GC gc, boolean recurse) {
        if (items == null)
            return 0;
        int width = 0;
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            if (item != null) {
                int itemWidth = ((DartTreeItem) item.getImpl()).calculateWidth(index, gc);
                width = Math.max(width, itemWidth);
                if (recurse && item.getExpanded()) {
                    width = Math.max(width, calculateWidth(((DartTreeItem) item.getImpl()).items, index, gc, recurse));
                }
            }
        }
        return width;
    }

    boolean checkData(TreeItem item) {
        if (((DartTreeItem) item.getImpl()).cached)
            return true;
        if ((getApi().style & SWT.VIRTUAL) != 0) {
            ((DartTreeItem) item.getImpl()).cached = true;
            Event event = new Event();
            event.item = item;
            ignoreRedraw = true;
            sendEvent(SWT.SetData, event);
            //widget could be disposed at this point
            ignoreRedraw = false;
            if (isDisposed() || item.isDisposed())
                return false;
            if (!setScrollWidth(item))
                ((DartTreeItem) item.getImpl()).redraw(-1);
        }
        return true;
    }

    static int checkStyle(int style) {
        /*
	* Feature in Windows.  Even when WS_HSCROLL or
	* WS_VSCROLL is not specified, Windows creates
	* trees and tables with scroll bars.  The fix
	* is to set H_SCROLL and V_SCROLL.
	*
	* NOTE: This code appears on all platforms so that
	* applications have consistent scroll bar behavior.
	*/
        if ((style & SWT.NO_SCROLL) == 0) {
            style |= SWT.H_SCROLL | SWT.V_SCROLL;
        }
        /* This platform is always FULL_SELECTION */
        style |= SWT.FULL_SELECTION;
        return checkBits(style, SWT.SINGLE, SWT.MULTI, 0, 0, 0, 0);
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

    void checkItems() {
        if (!reloadPending)
            return;
        reloadPending = false;
        TreeItem[] selectedItems = getSelection();
        selectItems(selectedItems, true);
        ignoreExpand = true;
        for (int i = 0; i < itemCount; i++) {
            if (items[i] != null)
                ((DartTreeItem) items[i].getImpl()).updateExpanded();
        }
        ignoreExpand = false;
    }

    void clear(TreeItem parentItem, int index, boolean all) {
        TreeItem item = _getItem(parentItem, index, false);
        if (item != null) {
            ((DartTreeItem) item.getImpl()).clear();
            ((DartTreeItem) item.getImpl()).redraw(-1);
            if (all) {
                clearAll(item, true);
            }
        }
    }

    void clearAll(TreeItem parentItem, boolean all) {
        int count = getItemCount(parentItem);
        if (count == 0)
            return;
        TreeItem[] children = parentItem == null ? items : ((DartTreeItem) parentItem.getImpl()).items;
        for (int i = 0; i < count; i++) {
            TreeItem item = children[i];
            if (item != null) {
                ((DartTreeItem) item.getImpl()).clear();
                ((DartTreeItem) item.getImpl()).redraw(-1);
                if (all)
                    clearAll(item, true);
            }
        }
    }

    /**
     * Clears the item at the given zero-relative index in the receiver.
     * The text, icon and other attributes of the item are set to the default
     * value.  If the tree was created with the <code>SWT.VIRTUAL</code> style,
     * these attributes are requested again as needed.
     *
     * @param index the index of the item to clear
     * @param all <code>true</code> if all child items of the indexed item should be
     * cleared recursively, and <code>false</code> otherwise
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
     * @since 3.2
     */
    public void clear(int index, boolean all) {
        checkWidget();
        int count = getItemCount();
        if (index < 0 || index >= count)
            error(SWT.ERROR_INVALID_RANGE);
        clear(null, index, all);
    }

    /**
     * Clears all the items in the receiver. The text, icon and other
     * attributes of the items are set to their default values. If the
     * tree was created with the <code>SWT.VIRTUAL</code> style, these
     * attributes are requested again as needed.
     *
     * @param all <code>true</code> if all child items should be cleared
     * recursively, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#VIRTUAL
     * @see SWT#SetData
     *
     * @since 3.2
     */
    public void clearAll(boolean all) {
        checkWidget();
        clearAll(null, all);
    }

    void clearCachedWidth(TreeItem[] items) {
        if (items == null)
            return;
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            if (item == null)
                break;
            ((DartTreeItem) item.getImpl()).width = -1;
            clearCachedWidth(((DartTreeItem) item.getImpl()).items);
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        checkWidget();
        int width = 0, height = 0;
        if (wHint == SWT.DEFAULT) {
            if (columnCount != 0) {
                for (int i = 0; i < columnCount; i++) {
                    width += columns[i].getWidth();
                }
            } else {
                GC gc = new GC(this.getApi());
                width = calculateWidth(items, 0, gc, true) + CELL_GAP;
                gc.dispose();
            }
            if ((getApi().style & SWT.CHECK) != 0)
                width += getCheckColumnWidth();
        } else {
            width = wHint;
        }
        if (hHint == SWT.DEFAULT) {
        } else {
            height = hHint;
        }
        if (width <= 0)
            width = DEFAULT_WIDTH;
        if (height <= 0)
            height = DEFAULT_HEIGHT;
        Rectangle rect = computeTrim(0, 0, width, height);
        return new Point(rect.width, rect.height);
    }

    void createColumn(TreeItem item, int index) {
        if (((DartTreeItem) item.getImpl()).items != null) {
            for (int i = 0; i < ((DartTreeItem) item.getImpl()).items.length; i++) {
                if (((DartTreeItem) item.getImpl()).items[i] != null)
                    createColumn(((DartTreeItem) item.getImpl()).items[i], index);
            }
        }
        String[] strings = ((DartTreeItem) item.getImpl()).strings;
        if (strings != null) {
            String[] temp = new String[columnCount];
            System.arraycopy(strings, 0, temp, 0, index);
            System.arraycopy(strings, index, temp, index + 1, columnCount - index - 1);
            temp[index] = "";
            ((DartTreeItem) item.getImpl()).strings = temp;
        }
        if (index == 0)
            ((DartItem) item.getImpl()).text = "";
        Image[] images = ((DartTreeItem) item.getImpl()).images;
        if (images != null) {
            Image[] temp = new Image[columnCount];
            System.arraycopy(images, 0, temp, 0, index);
            System.arraycopy(images, index, temp, index + 1, columnCount - index - 1);
            ((DartTreeItem) item.getImpl()).images = temp;
        }
        if (index == 0)
            ((DartItem) item.getImpl()).image = null;
        Color[] cellBackground = ((DartTreeItem) item.getImpl()).cellBackground;
        if (cellBackground != null) {
            Color[] temp = new Color[columnCount];
            System.arraycopy(cellBackground, 0, temp, 0, index);
            System.arraycopy(cellBackground, index, temp, index + 1, columnCount - index - 1);
            ((DartTreeItem) item.getImpl()).cellBackground = temp;
        }
        Color[] cellForeground = ((DartTreeItem) item.getImpl()).cellForeground;
        if (cellForeground != null) {
            Color[] temp = new Color[columnCount];
            System.arraycopy(cellForeground, 0, temp, 0, index);
            System.arraycopy(cellForeground, index, temp, index + 1, columnCount - index - 1);
            ((DartTreeItem) item.getImpl()).cellForeground = temp;
        }
        Font[] cellFont = ((DartTreeItem) item.getImpl()).cellFont;
        if (cellFont != null) {
            Font[] temp = new Font[columnCount];
            System.arraycopy(cellFont, 0, temp, 0, index);
            System.arraycopy(cellFont, index, temp, index + 1, columnCount - index - 1);
            ((DartTreeItem) item.getImpl()).cellFont = temp;
        }
    }

    @Override
    void createHandle() {
    }

    void createItem(TreeColumn column, int index) {
        if (!(0 <= index && index <= columnCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (index == 0) {
            // first column must be left aligned
            column.style &= ~(SWT.LEFT | SWT.RIGHT | SWT.CENTER);
            column.style |= SWT.LEFT;
        }
        if (columnCount == columns.length) {
            TreeColumn[] newColumns = new TreeColumn[columnCount + 4];
            System.arraycopy(columns, 0, newColumns, 0, columns.length);
            columns = newColumns;
        }
        if (columnCount == 0) {
        } else {
            if (index == 0) {
            }
        }
        ((DartWidget) column.getImpl()).createJNIRef();
        System.arraycopy(columns, index, columns, index + 1, columnCount++ - index);
        columns[index] = column;
        for (int i = 0; i < itemCount; i++) {
            TreeItem item = items[i];
            if (item != null) {
                if (columnCount > 1) {
                    createColumn(item, index);
                }
            }
        }
    }

    /**
     * The fastest way to insert many items is documented in {@link TreeItem#TreeItem(org.eclipse.swt.widgets.Tree,int,int)}
     * and {@link TreeItem#setItemCount}
     */
    void createItem(TreeItem item, TreeItem parentItem, int index) {
        int count;
        TreeItem[] items;
        if (parentItem != null) {
            count = ((DartTreeItem) parentItem.getImpl()).itemCount;
            items = ((DartTreeItem) parentItem.getImpl()).items;
        } else {
            count = this.itemCount;
            items = this.items;
        }
        if (index == -1)
            index = count;
        if (!(0 <= index && index <= count))
            error(SWT.ERROR_INVALID_RANGE);
        if (count == items.length) {
            TreeItem[] newItems = new TreeItem[items.length + 4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
            if (parentItem != null) {
                ((DartTreeItem) parentItem.getImpl()).items = items;
            } else {
                this.items = items;
            }
        }
        System.arraycopy(items, index, items, index + 1, count++ - index);
        items[index] = item;
        ((DartTreeItem) item.getImpl()).items = new TreeItem[4];
        ((DartWidget) item.getImpl()).createJNIRef();
        ((DartTreeItem) item.getImpl()).register();
        if (parentItem != null) {
            ((DartTreeItem) parentItem.getImpl()).itemCount = count;
        } else {
            this.itemCount = count;
        }
        ignoreExpand = true;
        if (getDrawing()) {
            TreeItem[] selectedItems = getSelection();
            if (parentItem != null) {
            } else {
            }
            selectItems(selectedItems, true);
        } else {
            reloadPending = true;
        }
        if (parentItem != null && ((DartTreeItem) parentItem.getImpl()).itemCount == 1 && ((DartTreeItem) parentItem.getImpl()).expanded) {
        }
        ignoreExpand = false;
        if (parentItem == null && this.itemCount == 1) {
            Event event = new Event();
            event.detail = 0;
            sendEvent(SWT.EmptinessChanged, event);
        }
    }

    @Override
    void createWidget() {
        super.createWidget();
        items = new TreeItem[4];
        columns = new TreeColumn[4];
    }

    @Override
    Color defaultBackground() {
        return ((SwtDisplay) display.getImpl()).getWidgetColor(SWT.COLOR_LIST_BACKGROUND);
    }

    @Override
    Color defaultForeground() {
        return ((SwtDisplay) display.getImpl()).getWidgetColor(SWT.COLOR_LIST_FOREGROUND);
    }

    /**
     * Deselects all selected items in the receiver.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void deselectAll() {
        checkWidget();
        ignoreSelect = true;
        ignoreSelect = false;
        selection = new TreeItem[0];
    }

    @Override
    void deregister() {
        super.deregister();
    }

    /**
     * Deselects an item in the receiver.  If the item was already
     * deselected, it remains deselected.
     *
     * @param item the item to be deselected
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
     * @since 3.4
     */
    public void deselect(TreeItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        ignoreSelect = true;
        ignoreSelect = false;
    }

    void destroyItem(TreeColumn column) {
        int index = 0;
        while (index < columnCount) {
            if (columns[index] == column)
                break;
            index++;
        }
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            if (item != null) {
                if (columnCount <= 1) {
                    ((DartTreeItem) item.getImpl()).strings = null;
                    ((DartTreeItem) item.getImpl()).images = null;
                    ((DartTreeItem) item.getImpl()).cellBackground = null;
                    ((DartTreeItem) item.getImpl()).cellForeground = null;
                    ((DartTreeItem) item.getImpl()).cellFont = null;
                } else {
                    if (((DartTreeItem) item.getImpl()).strings != null) {
                        String[] strings = ((DartTreeItem) item.getImpl()).strings;
                        if (index == 0) {
                            ((DartItem) item.getImpl()).text = strings[1] != null ? strings[1] : "";
                        }
                        String[] temp = new String[columnCount - 1];
                        System.arraycopy(strings, 0, temp, 0, index);
                        System.arraycopy(strings, index + 1, temp, index, columnCount - 1 - index);
                        ((DartTreeItem) item.getImpl()).strings = temp;
                    } else {
                        if (index == 0)
                            ((DartItem) item.getImpl()).text = "";
                    }
                    if (((DartTreeItem) item.getImpl()).images != null) {
                        Image[] images = ((DartTreeItem) item.getImpl()).images;
                        if (index == 0)
                            ((DartItem) item.getImpl()).image = images[1];
                        Image[] temp = new Image[columnCount - 1];
                        System.arraycopy(images, 0, temp, 0, index);
                        System.arraycopy(images, index + 1, temp, index, columnCount - 1 - index);
                        ((DartTreeItem) item.getImpl()).images = temp;
                    } else {
                        if (index == 0)
                            ((DartItem) item.getImpl()).image = null;
                    }
                    if (((DartTreeItem) item.getImpl()).cellBackground != null) {
                        Color[] cellBackground = ((DartTreeItem) item.getImpl()).cellBackground;
                        Color[] temp = new Color[columnCount - 1];
                        System.arraycopy(cellBackground, 0, temp, 0, index);
                        System.arraycopy(cellBackground, index + 1, temp, index, columnCount - 1 - index);
                        ((DartTreeItem) item.getImpl()).cellBackground = temp;
                    }
                    if (((DartTreeItem) item.getImpl()).cellForeground != null) {
                        Color[] cellForeground = ((DartTreeItem) item.getImpl()).cellForeground;
                        Color[] temp = new Color[columnCount - 1];
                        System.arraycopy(cellForeground, 0, temp, 0, index);
                        System.arraycopy(cellForeground, index + 1, temp, index, columnCount - 1 - index);
                        ((DartTreeItem) item.getImpl()).cellForeground = temp;
                    }
                    if (((DartTreeItem) item.getImpl()).cellFont != null) {
                        Font[] cellFont = ((DartTreeItem) item.getImpl()).cellFont;
                        Font[] temp = new Font[columnCount - 1];
                        System.arraycopy(cellFont, 0, temp, 0, index);
                        System.arraycopy(cellFont, index + 1, temp, index, columnCount - 1 - index);
                        ((DartTreeItem) item.getImpl()).cellFont = temp;
                    }
                }
            }
        }
        System.arraycopy(columns, index + 1, columns, index, --columnCount - index);
        columns[columnCount] = null;
        if (columnCount == 0) {
            setScrollWidth();
        } else {
            if (index == 0) {
            }
        }
    }

    void destroyItem(TreeItem item) {
        int count;
        TreeItem[] items;
        TreeItem parentItem = ((DartTreeItem) item.getImpl()).parentItem;
        if (parentItem != null) {
            count = ((DartTreeItem) parentItem.getImpl()).itemCount;
            items = ((DartTreeItem) parentItem.getImpl()).items;
        } else {
            count = this.itemCount;
            items = this.items;
        }
        int index = 0;
        while (index < count) {
            if (items[index] == item)
                break;
            index++;
        }
        System.arraycopy(items, index + 1, items, index, --count - index);
        items[count] = null;
        if (parentItem != null) {
            ((DartTreeItem) parentItem.getImpl()).itemCount = count;
        } else {
            this.itemCount = count;
        }
        if (getDrawing()) {
            if (parentItem != null) {
            } else {
            }
        } else {
            reloadPending = true;
        }
        setScrollWidth();
        if (this.itemCount == 0)
            imageBounds = null;
        if (insertItem == item)
            insertItem = null;
        if (parentItem == null && this.itemCount == 0) {
            Event event = new Event();
            event.detail = 1;
            sendEvent(SWT.EmptinessChanged, event);
        }
    }

    @Override
    boolean dragDetect(int x, int y, boolean filter, boolean[] consume) {
        // Let Cocoa determine if a drag is starting and fire the notification when we get the callback.
        return false;
    }

    int getCheckColumnWidth() {
        return 0;
    }

    @Override
    public Rectangle getClientArea() {
        checkWidget();
        Rectangle rect = super.getClientArea();
        return rect;
    }

    /**
     * Returns the column at the given, zero-relative index in the
     * receiver. Throws an exception if the index is out of range.
     * Columns are returned in the order that they were created.
     * If no <code>TreeColumn</code>s were created by the programmer,
     * this method will throw <code>ERROR_INVALID_RANGE</code> despite
     * the fact that a single column of data may be visible in the tree.
     * This occurs when the programmer uses the tree like a list, adding
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
     * @see Tree#getColumnOrder()
     * @see Tree#setColumnOrder(int[])
     * @see TreeColumn#getMoveable()
     * @see TreeColumn#setMoveable(boolean)
     * @see SWT#Move
     *
     * @since 3.1
     */
    public TreeColumn getColumn(int index) {
        checkWidget();
        if (!(0 <= index && index < columnCount))
            error(SWT.ERROR_INVALID_RANGE);
        return columns[index];
    }

    /**
     * Returns the number of columns contained in the receiver.
     * If no <code>TreeColumn</code>s were created by the programmer,
     * this value is zero, despite the fact that visually, one column
     * of items may be visible. This occurs when the programmer uses
     * the tree like a list, adding items but never creating a column.
     *
     * @return the number of columns
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public int getColumnCount() {
        checkWidget();
        return columnCount;
    }

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
     * @see Tree#setColumnOrder(int[])
     * @see TreeColumn#getMoveable()
     * @see TreeColumn#setMoveable(boolean)
     * @see SWT#Move
     *
     * @since 3.2
     */
    public int[] getColumnOrder() {
        checkWidget();
        int[] order = new int[columnCount];
        for (int i = 0; i < columnCount; i++) {
        }
        return order;
    }

    /**
     * Returns an array of <code>TreeColumn</code>s which are the
     * columns in the receiver. Columns are returned in the order
     * that they were created.  If no <code>TreeColumn</code>s were
     * created by the programmer, the array is empty, despite the fact
     * that visually, one column of items may be visible. This occurs
     * when the programmer uses the tree like a list, adding items but
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
     * @see Tree#getColumnOrder()
     * @see Tree#setColumnOrder(int[])
     * @see TreeColumn#getMoveable()
     * @see TreeColumn#setMoveable(boolean)
     * @see SWT#Move
     *
     * @since 3.1
     */
    public TreeColumn[] getColumns() {
        checkWidget();
        TreeColumn[] result = new TreeColumn[columnCount];
        System.arraycopy(columns, 0, result, 0, columnCount);
        return result;
    }

    /**
     * Returns the width in points of a grid line.
     *
     * @return the width of a grid line in points
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public int getGridLineWidth() {
        checkWidget();
        return 0;
    }

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
    public Color getHeaderBackground() {
        checkWidget();
        return getHeaderBackgroundColor();
    }

    private Color getHeaderBackgroundColor() {
        return headerBackground != null ? SwtColor.cocoa_new(display, headerBackground) : defaultBackground();
    }

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
    public Color getHeaderForeground() {
        checkWidget();
        return getHeaderForegroundColor();
    }

    Color getHeaderForegroundColor() {
        return headerForeground != null ? SwtColor.cocoa_new(display, headerForeground) : defaultForeground();
    }

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
     * @since 3.1
     */
    public int getHeaderHeight() {
        checkWidget();
        return 0;
    }

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
     *
     * @since 3.1
     */
    public boolean getHeaderVisible() {
        checkWidget();
        return this.headerVisible;
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
     *
     * @since 3.1
     */
    public TreeItem getItem(int index) {
        checkWidget();
        int count = getItemCount();
        if (index < 0 || index >= count)
            error(SWT.ERROR_INVALID_RANGE);
        return _getItem(null, index, true);
    }

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
    public TreeItem getItem(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        checkItems();
        return null;
    }

    /**
     * Returns the number of items contained in the receiver
     * that are direct item children of the receiver.  The
     * number that is returned is the number of roots in the
     * tree.
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

    int getItemCount(TreeItem item) {
        return item == null ? itemCount : ((DartTreeItem) item.getImpl()).itemCount;
    }

    /**
     * Returns the height of the area which would be used to
     * display <em>one</em> of the items in the tree.
     *
     * @return the height of one item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getItemHeight() {
        checkWidget();
        return 0;
    }

    /**
     * Returns a (possibly empty) array of items contained in the
     * receiver that are direct item children of the receiver.  These
     * are the roots of the tree.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of items, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return the items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public TreeItem[] getItems() {
        checkWidget();
        TreeItem[] result = new TreeItem[itemCount];
        for (int i = 0; i < itemCount; i++) {
            result[i] = _getItem(null, i, true);
        }
        return result;
    }

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
     *
     * @since 3.1
     */
    public boolean getLinesVisible() {
        checkWidget();
        return this.linesVisible;
    }

    /**
     * Returns the receiver's parent item, which must be a
     * <code>TreeItem</code> or null when the receiver is a
     * root.
     *
     * @return the receiver's parent item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public TreeItem getParentItem() {
        checkWidget();
        return null;
    }

    /**
     * Returns an array of <code>TreeItem</code>s that are currently
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
    public TreeItem[] getSelection() {
        checkWidget();
        return this.selection;
    }

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
    public int getSelectionCount() {
        checkWidget();
        return this.selection != null ? this.selection.length : 0;
    }

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
     * @see #setSortColumn(TreeColumn)
     *
     * @since 3.2
     */
    public TreeColumn getSortColumn() {
        checkWidget();
        return sortColumn;
    }

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
    public int getSortDirection() {
        checkWidget();
        return sortDirection;
    }

    /**
     * Returns the item which is currently at the top of the receiver.
     * This item can change when items are expanded, collapsed, scrolled
     * or new items are added or removed.
     *
     * @return the item at the top of the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.1
     */
    public TreeItem getTopItem() {
        checkWidget();
        return this.topItem;
    }

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
     *
     * @since 3.1
     */
    public int indexOf(TreeColumn column) {
        checkWidget();
        if (column == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (column.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        for (int i = 0; i < columnCount; i++) {
            if (columns[i] == column)
                return i;
        }
        return -1;
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
     *
     * @since 3.1
     */
    public int indexOf(TreeItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (((DartTreeItem) item.getImpl()).parentItem != null)
            return -1;
        for (int i = 0; i < itemCount; i++) {
            if (item == items[i])
                return i;
        }
        return -1;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    void sendSelection() {
        if (ignoreSelect)
            return;
    }

    @Override
    void register() {
        super.register();
    }

    @Override
    void releaseChildren(boolean destroy) {
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            if (item != null && !item.isDisposed()) {
                item.getImpl().release(false);
            }
        }
        items = null;
        if (columns != null) {
            for (int i = 0; i < columnCount; i++) {
                TreeColumn column = columns[i];
                if (column != null && !column.isDisposed()) {
                    column.getImpl().release(false);
                }
            }
            columns = null;
        }
        super.releaseChildren(destroy);
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        sortColumn = null;
    }

    /**
     * Removes all of the items from the receiver.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void removeAll() {
        checkWidget();
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            if (item != null && !item.isDisposed())
                item.getImpl().release(false);
        }
        items = new TreeItem[4];
        itemCount = 0;
        imageBounds = null;
        insertItem = null;
        ignoreSelect = true;
        ignoreSelect = false;
        setScrollWidth();
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
        eventTable.unhook(SWT.Selection, listener);
        eventTable.unhook(SWT.DefaultSelection, listener);
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
     * @see TreeListener
     * @see #addTreeListener
     */
    public void removeTreeListener(TreeListener listener) {
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
                TreeItem item = items[i];
                if (item != null)
                    ((DartWidget) item.getImpl()).reskinChildren(flags);
            }
        }
        if (columns != null) {
            for (int i = 0; i < columns.length; i++) {
                TreeColumn column = columns[i];
                if (column != null)
                    ((DartWidget) column.getImpl()).reskinChildren(flags);
            }
        }
        super.reskinChildren(flags);
    }

    /**
     *  Display a mark indicating the point at which an item will be inserted.
     *  The drop insert item has a visual hint to show where a dragged item
     *  will be inserted when dropped on the tree.
     *
     *  @param item the insert item.  Null will clear the insertion mark.
     *  @param before true places the insert mark above 'item'. false places
     * 	the insert mark below 'item'.
     *
     *  @exception IllegalArgumentException <ul>
     *     <li>ERROR_INVALID_ARGUMENT - if the item has been disposed</li>
     *  </ul>
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *  </ul>
     */
    public void setInsertMark(TreeItem item, boolean before) {
        checkWidget();
        if (item != null && item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        TreeItem oldMark = insertItem;
        insertItem = item;
        insertBefore = before;
        if (oldMark != null && !oldMark.isDisposed())
            ((DartTreeItem) oldMark.getImpl()).redraw(-1);
        if (item != null)
            ((DartTreeItem) item.getImpl()).redraw(-1);
    }

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
    public void selectAll() {
        checkWidget();
        if ((getApi().style & SWT.SINGLE) != 0)
            return;
        checkItems();
        ignoreSelect = true;
        ignoreSelect = false;
        items = TreeHelper.collectAllItems(this);
        selection = items;
    }

    /**
     * Selects an item in the receiver.  If the item was already
     * selected, it remains selected.
     *
     * @param item the item to be selected
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
     * @since 3.4
     */
    public void select(TreeItem item) {
        dirty();
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        checkItems();
        showItem(item);
        ignoreSelect = true;
        ignoreSelect = false;
        this.selection = new TreeItem[] { item };
    }

    @Override
    void sendDoubleSelection() {
    }

    void selectItems(TreeItem[] items, boolean ignoreDisposed) {
        int length = items.length;
        for (int i = 0; i < length; i++) {
            if (items[i] != null) {
                if (items[i].isDisposed()) {
                    if (ignoreDisposed)
                        continue;
                    error(SWT.ERROR_INVALID_ARGUMENT);
                }
                if (!ignoreDisposed)
                    showItem(items[i], false);
            }
        }
        ignoreSelect = true;
        ignoreSelect = false;
    }

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
     * @see Tree#getColumnOrder()
     * @see TreeColumn#getMoveable()
     * @see TreeColumn#setMoveable(boolean)
     * @see SWT#Move
     *
     * @since 3.2
     */
    public void setColumnOrder(int[] order) {
        dirty();
        checkWidget();
        if (order == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (columnCount == 0) {
            if (order.length != 0)
                error(SWT.ERROR_INVALID_ARGUMENT);
            return;
        }
        if (order.length != columnCount)
            error(SWT.ERROR_INVALID_ARGUMENT);
        int[] oldOrder = getColumnOrder();
        boolean reorder = false;
        boolean[] seen = new boolean[columnCount];
        for (int i = 0; i < order.length; i++) {
            int index = order[i];
            if (index < 0 || index >= columnCount)
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (seen[index])
                error(SWT.ERROR_INVALID_ARGUMENT);
            seen[index] = true;
            if (order[i] != oldOrder[i])
                reorder = true;
        }
        if (reorder) {
            int[] oldX = new int[oldOrder.length];
            for (int i = 0; i < oldOrder.length; i++) {
            }
            int[] newX = new int[order.length];
            for (int i = 0; i < order.length; i++) {
            }
            TreeColumn[] newColumns = new TreeColumn[columnCount];
            System.arraycopy(columns, 0, newColumns, 0, columnCount);
            for (int i = 0; i < columnCount; i++) {
                TreeColumn column = newColumns[i];
                if (!column.isDisposed()) {
                    if (newX[i] != oldX[i]) {
                        column.getImpl().sendEvent(SWT.Move);
                    }
                }
            }
        }
        this.columnOrder = order;
    }

    /**
     * Sets the header background color to the color specified
     * by the argument, or to the default system color if the argument is null.
     * <p>
     * Note: This operation is a <em>HINT</em> and is not supported on all platforms. If
     * the native header has a 3D look and feel (e.g. Windows 7), this method
     * will cause the header to look FLAT irrespective of the state of the tree style.
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
    public void setHeaderBackground(Color color) {
        dirty();
        checkWidget();
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
        }
        double[] headerBackground = color != null ? color.handle : null;
        if (equals(headerBackground, this.headerBackground))
            return;
        this.headerBackground = headerBackground;
        if (getHeaderVisible()) {
        }
        this._headerBackground = color;
    }

    /**
     * Sets the header foreground color to the color specified
     * by the argument, or to the default system color if the argument is null.
     * <p>
     * Note: This operation is a <em>HINT</em> and is not supported on all platforms. If
     * the native header has a 3D look and feel (e.g. Windows 7), this method
     * will cause the header to look FLAT irrespective of the state of the tree style.
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
    public void setHeaderForeground(Color color) {
        dirty();
        checkWidget();
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
        }
        double[] headerForeground = color != null ? color.handle : null;
        if (equals(headerForeground, this.headerForeground))
            return;
        this.headerForeground = headerForeground;
        if (getHeaderVisible()) {
        }
        this._headerForeground = color;
    }

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
     *
     * @since 3.1
     */
    public void setHeaderVisible(boolean show) {
        dirty();
        checkWidget();
        this.headerVisible = show;
    }

    /**
     * Sets the number of root-level items contained in the receiver.
     * <p>
     * The fastest way to insert many items is documented in {@link TreeItem#TreeItem(Tree,int,int)}
     * and {@link TreeItem#setItemCount}
     *
     * @param count the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public void setItemCount(int count) {
        checkWidget();
        checkItems();
        count = Math.max(0, count);
        setItemCount(null, count);
    }

    void setItemCount(TreeItem parentItem, int count) {
        int itemCount = getItemCount(parentItem);
        if (count == itemCount)
            return;
        int length = Math.max(4, (count + 3) / 4 * 4);
        TreeItem[] children = parentItem == null ? items : ((DartTreeItem) parentItem.getImpl()).items;
        boolean expanded = parentItem == null || parentItem.getExpanded();
        if (count < itemCount) {
            /*
		* Note that the item count has to be updated before the call to reloadItem(), but
		* the items have to be released after.
		*/
            if (parentItem == null) {
                this.itemCount = count;
            } else {
                ((DartTreeItem) parentItem.getImpl()).itemCount = count;
            }
            TreeItem[] selectedItems = getSelection();
            for (int index = count; index < itemCount; index++) {
                TreeItem item = children[index];
                if (item != null && !item.isDisposed())
                    item.getImpl().release(false);
            }
            selectItems(selectedItems, true);
            TreeItem[] newItems = new TreeItem[length];
            if (children != null) {
                System.arraycopy(children, 0, newItems, 0, count);
            }
            children = newItems;
            if (parentItem == null) {
                this.items = newItems;
            } else {
                ((DartTreeItem) parentItem.getImpl()).items = newItems;
            }
        } else {
            if ((getApi().style & SWT.VIRTUAL) == 0) {
                for (int i = itemCount; i < count; i++) {
                    new TreeItem(this.getApi(), parentItem, SWT.NONE, i, true);
                }
            } else {
                TreeItem[] newItems = new TreeItem[length];
                if (children != null) {
                    System.arraycopy(children, 0, newItems, 0, itemCount);
                }
                children = newItems;
                if (parentItem == null) {
                    this.items = newItems;
                    this.itemCount = count;
                } else {
                    ((DartTreeItem) parentItem.getImpl()).items = newItems;
                    ((DartTreeItem) parentItem.getImpl()).itemCount = count;
                }
                TreeItem[] selectedItems = getSelection();
                selectItems(selectedItems, true);
                if (parentItem != null && itemCount == 0 && ((DartTreeItem) parentItem.getImpl()).expanded) {
                    ignoreExpand = true;
                    ignoreExpand = false;
                }
            }
        }
    }

    /*public*/
    void setItemHeight(int itemHeight) {
        checkWidget();
        if (itemHeight < -1)
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (itemHeight == -1) {
        } else {
        }
    }

    /**
     * Marks the receiver's lines as visible if the argument is <code>true</code>,
     * and marks it invisible otherwise. Note that some platforms draw
     * grid lines while others may draw alternating row colors.
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
     *
     * @since 3.1
     */
    public void setLinesVisible(boolean show) {
        dirty();
        checkWidget();
        this.linesVisible = show;
    }

    @Override
    public void setRedraw(boolean redraw) {
        dirty();
        checkWidget();
        super.setRedraw(redraw);
        if (redraw && drawCount == 0) {
            checkItems();
            setScrollWidth();
        }
    }

    boolean setScrollWidth() {
        return setScrollWidth(true, items, true);
    }

    boolean setScrollWidth(boolean set, TreeItem[] items, boolean recurse) {
        if (items == null)
            return false;
        if (ignoreRedraw || !getDrawing())
            return false;
        if (columnCount != 0)
            return false;
        GC gc = new GC(this.getApi());
        gc.dispose();
        if (!set) {
        }
        return true;
    }

    boolean setScrollWidth(TreeItem item) {
        if (ignoreRedraw || !getDrawing())
            return false;
        if (columnCount != 0)
            return false;
        TreeItem parentItem = ((DartTreeItem) item.getImpl()).parentItem;
        if (parentItem != null && !parentItem.getExpanded())
            return false;
        GC gc = new GC(this.getApi());
        gc.dispose();
        return false;
    }

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
    public void setSelection(TreeItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setSelection(new TreeItem[] { item });
    }

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
     * @see Tree#deselectAll()
     */
    public void setSelection(TreeItem[] items) {
        dirty();
        checkWidget();
        if (items == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        checkItems();
        deselectAll();
        int length = items.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))
            return;
        selectItems(items, false);
        if (items.length > 0) {
            for (int i = 0; i < items.length; i++) {
                TreeItem item = items[i];
                if (item != null) {
                    showItem(item, true);
                    break;
                }
            }
        }
        this.selection = items;
    }

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
    public void setSortColumn(TreeColumn column) {
        dirty();
        checkWidget();
        if (column != null && column.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (column == sortColumn)
            return;
        setSort(column, sortDirection);
    }

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
    public void setSortDirection(int direction) {
        dirty();
        checkWidget();
        if (direction != SWT.UP && direction != SWT.DOWN && direction != SWT.NONE)
            return;
        if (direction == sortDirection)
            return;
        setSort(sortColumn, direction);
    }

    void setSort(TreeColumn column, int direction) {
        if (column != null) {
        }
        if (sortColumn != null && sortColumn != column) {
        }
        sortDirection = direction;
        sortColumn = column;
    }

    /**
     * Sets the item which is currently at the top of the receiver.
     * This item can change when items are expanded, collapsed, scrolled
     * or new items are added or removed.
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
     * @see Tree#getTopItem()
     *
     * @since 2.1
     */
    public void setTopItem(TreeItem item) {
        dirty();
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        checkItems();
        showItem(item, false);
        this.topItem = item;
    }

    /**
     * Shows the column.  If the column is already showing in the receiver,
     * this method simply returns.  Otherwise, the columns are scrolled until
     * the column is visible.
     *
     * @param column the column to be shown
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
     * @since 3.1
     */
    public void showColumn(TreeColumn column) {
        checkWidget();
        if (column == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (column.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (((DartTreeColumn) column.getImpl()).parent != this.getApi())
            return;
        if (columnCount <= 1)
            return;
    }

    /**
     * Shows the item.  If the item is already showing in the receiver,
     * this method simply returns.  Otherwise, the items are scrolled
     * and expanded until the item is visible.
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
     * @see Tree#showSelection()
     */
    public void showItem(TreeItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        checkItems();
        showItem(item, true);
    }

    void showItem(TreeItem item, boolean scroll) {
        TreeItem parentItem = ((DartTreeItem) item.getImpl()).parentItem;
        if (parentItem != null) {
            showItem(parentItem, false);
            if (!parentItem.getExpanded()) {
                parentItem.setExpanded(true);
                Event event = new Event();
                event.item = parentItem;
                sendEvent(SWT.Expand, event);
            }
        }
        if (scroll) {
        }
    }

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
     * @see Tree#showItem(TreeItem)
     */
    public void showSelection() {
        checkWidget();
        checkItems();
        //TODO - optimize
        TreeItem[] selection = getSelection();
        if (selection.length > 0) {
            checkData(selection[0]);
            showItem(selection[0], true);
        }
    }

    @Override
    public void updateCursorRects(boolean enabled) {
        super.updateCursorRects(enabled);
    }

    int[] columnOrder = new int[0];

    Color _headerBackground;

    Color _headerForeground;

    boolean headerVisible;

    boolean linesVisible;

    TreeItem[] selection = new TreeItem[0];

    TreeItem topItem;

    public TreeItem[] _items() {
        return items;
    }

    public int _itemCount() {
        return itemCount;
    }

    public TreeColumn[] _columns() {
        return columns;
    }

    public TreeColumn _sortColumn() {
        return sortColumn;
    }

    public int _columnCount() {
        return columnCount;
    }

    public int _sortDirection() {
        return sortDirection;
    }

    public int _selectedRowIndex() {
        return selectedRowIndex;
    }

    public boolean _ignoreExpand() {
        return ignoreExpand;
    }

    public boolean _ignoreSelect() {
        return ignoreSelect;
    }

    public boolean _ignoreRedraw() {
        return ignoreRedraw;
    }

    public boolean _reloadPending() {
        return reloadPending;
    }

    public boolean _drawExpansion() {
        return drawExpansion;
    }

    public boolean _didSelect() {
        return didSelect;
    }

    public boolean _preventSelect() {
        return preventSelect;
    }

    public boolean _dragDetected() {
        return dragDetected;
    }

    public Rectangle _imageBounds() {
        return imageBounds;
    }

    public TreeItem _insertItem() {
        return insertItem;
    }

    public boolean _insertBefore() {
        return insertBefore;
    }

    public double[] _headerBackground() {
        return headerBackground;
    }

    public double[] _headerForeground() {
        return headerForeground;
    }

    public boolean _shouldExpand() {
        return shouldExpand;
    }

    public boolean _shouldScroll() {
        return shouldScroll;
    }

    public boolean _keyDown() {
        return keyDown;
    }

    public int[] _columnOrder() {
        return columnOrder;
    }

    public Color __headerBackground() {
        return _headerBackground;
    }

    public Color __headerForeground() {
        return _headerForeground;
    }

    public boolean _headerVisible() {
        return headerVisible;
    }

    public boolean _linesVisible() {
        return linesVisible;
    }

    public TreeItem[] _selection() {
        return selection;
    }

    public TreeItem _topItem() {
        return topItem;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                TreeHelper.sendSelection(this, e, SWT.DefaultSelection);
            });
        });
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                TreeHelper.sendSelection(this, e, SWT.Selection);
            });
        });
        FlutterBridge.on(this, "Tree", "Collapse", e -> {
            getDisplay().asyncExec(() -> {
                TreeHelper.sendExpand(this, e, false);
            });
        });
        FlutterBridge.on(this, "Tree", "Expand", e -> {
            getDisplay().asyncExec(() -> {
                TreeHelper.sendExpand(this, e, true);
            });
        });
    }

    public Tree getApi() {
        if (api == null)
            api = Tree.createApi(this);
        return (Tree) api;
    }

    public VTree getValue() {
        if (value == null)
            value = new VTree(this);
        return (VTree) value;
    }
}
