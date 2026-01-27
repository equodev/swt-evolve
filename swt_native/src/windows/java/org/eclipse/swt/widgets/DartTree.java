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
import org.eclipse.swt.internal.*;
import java.util.Objects;
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

    TreeItem[] items;

    TreeColumn[] columns;

    int columnCount;

    TreeItem currentItem;

    TreeColumn sortColumn;

    long hAnchor, hInsert, hSelect;

    int lastID;

    int sortDirection;

    boolean dragStarted, gestureCompleted, insertAfter, shrink, ignoreShrink;

    boolean ignoreSelect, ignoreExpand, ignoreDeselect, ignoreResize;

    boolean lockSelection, oldSelected, newSelected, ignoreColumnMove, ignoreColumnResize;

    boolean linesVisible, customDraw, painted, ignoreItemHeight;

    boolean ignoreCustomDraw, ignoreDrawForeground, ignoreDrawBackground, ignoreDrawFocus;

    boolean ignoreDrawSelection, ignoreDrawHot, ignoreFullSelection, explorerTheme;

    boolean createdAsRTL;

    boolean headerItemDragging;

    int scrollWidth, selectionForeground;

    long lastTimerID = -1;

    int lastTimerCount;

    int headerBackground = -1;

    int headerForeground = -1;

    // Cached variables for fast item lookup
    int[] cachedItemOrder;

    // Used to figure when other cache variables need updating
    long cachedFirstItem;

    // Item for which #cachedIndex is saved
    long cachedIndexItem;

    // cached Tree#indexOf() or TreeItem#indexOf() of #cachedIndexItem
    int cachedIndex;

    // cached Tree#getItemCount() or TreeItem#getItemCount()
    int cachedItemCount;

    static final boolean ENABLE_TVS_EX_FADEINOUTEXPANDOS = System.getProperty("org.eclipse.swt.internal.win32.enableFadeInOutExpandos") != null;

    static final int TIMER_MAX_COUNT = 8;

    static final int INSET = 3;

    static final int GRID_WIDTH = 1;

    static final int SORT_WIDTH = 10;

    static final int HEADER_MARGIN = 12;

    static final int HEADER_EXTRA = 3;

    static final int INCREMENT = 5;

    static final int EXPLORER_EXTRA = 2;

    static final int DRAG_IMAGE_SIZE = 301;

    // The default Indent at 100 dpi
    static final int DEFAULT_INDENT = 16;

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
        /*
	* Note: Windows only supports TVS_NOSCROLL and TVS_NOHSCROLL.
	*/
        if ((style & SWT.H_SCROLL) != 0 && (style & SWT.V_SCROLL) == 0) {
            style |= SWT.V_SCROLL;
        }
        return checkBits(style, SWT.SINGLE, SWT.MULTI, 0, 0, 0, 0);
    }

    @Override
    void _addListener(int eventType, Listener listener) {
        super._addListener(eventType, listener);
        switch(eventType) {
            case SWT.DragDetect:
                {
                    if ((getApi().state & DRAG_DETECT) != 0) {
                    }
                    break;
                }
            case SWT.MeasureItem:
            case SWT.EraseItem:
            case SWT.PaintItem:
                {
                    customDraw = true;
                    getApi().style |= SWT.DOUBLE_BUFFERED;
                    if (isCustomToolTip())
                        createItemToolTips();
                    if (eventType == SWT.MeasureItem) {
                    }
                    /*
			* Feature in Windows.  When the tree has the style
			* TVS_FULLROWSELECT, the background color for the
			* entire row is filled when an item is painted,
			* drawing on top of any custom drawing.  The fix
			* is to clear TVS_FULLROWSELECT.
			*/
                    if ((getApi().style & SWT.FULL_SELECTION) != 0) {
                        if (eventType != SWT.MeasureItem) {
                        }
                    }
                    break;
                }
        }
    }

    TreeItem _getItem(long hItem) {
        return null;
    }

    TreeItem _getItem(long hItem, int id) {
        if ((getApi().style & SWT.VIRTUAL) == 0)
            return items[id];
        return id != -1 ? items[id] : new TreeItem(this.getApi(), SWT.NONE, -1, -1, hItem);
    }

    @Override
    void _removeListener(int eventType, Listener listener) {
        super._removeListener(eventType, listener);
        switch(eventType) {
            case SWT.MeasureItem:
                {
                    /**
                     * If H_SCROLL is set, reverting the TVS_NOHSCROLL settings which
                     * was applied while adding SWT.MeasureItem event Listener.
                     */
                    if ((getApi().style & SWT.H_SCROLL) != 0 && (getApi().state & DISPOSE_SENT) == 0) {
                    }
                    break;
                }
        }
    }

    void _setBackgroundPixel(int newPixel) {
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

    @Override
    long borderHandle() {
        return 0;
    }

    int getFirstColumnIndex() {
        int index = getColumnIndex(0);
        return index;
    }

    /**
     * @return An index value for an item based on its order in the header control.
     * Same as getColumnOrder()[order] - but cached.
     * @see #setColumnOrder(int[])
     * @see #getColumnOrder()
     */
    private int getColumnIndex(int order) {
        if (order < 0 || order >= columnCount || columnCount == 1) {
            return 0;
        }
        /*	returns getColumnIndexFromOS(order)*/
        return getColumnOrder()[order];
    }

    /**
     * for junit only
     * @see #getColumnIndex*
     */
    @SuppressWarnings("unused")
    private int getColumnIndexFromOS(int order) {
        return 0;
    }

    @Override
    void checkBuffered() {
        super.checkBuffered();
        if ((getApi().style & SWT.VIRTUAL) != 0) {
            getApi().style |= SWT.DOUBLE_BUFFERED;
        }
    }

    boolean checkData(TreeItem item, boolean redraw) {
        if ((getApi().style & SWT.VIRTUAL) == 0)
            return true;
        if (!((DartTreeItem) item.getImpl()).cached) {
            TreeItem parentItem = item.getParentItem();
            return checkData(item, parentItem == null ? indexOf(item) : parentItem.indexOf(item), redraw);
        }
        return true;
    }

    boolean checkData(TreeItem item, int index, boolean redraw) {
        if ((getApi().style & SWT.VIRTUAL) == 0)
            return true;
        if (!((DartTreeItem) item.getImpl()).cached) {
            ((DartTreeItem) item.getImpl()).cached = true;
            Event event = new Event();
            event.item = item;
            event.index = index;
            TreeItem oldItem = currentItem;
            currentItem = item;
            sendEvent(SWT.SetData, event);
            //widget could be disposed at this point
            currentItem = oldItem;
            if (isDisposed() || item.isDisposed())
                return false;
            if (redraw)
                ((DartTreeItem) item.getImpl()).redraw();
        }
        return true;
    }

    boolean checkScroll(long hItem) {
        /*
	* Feature in Windows.  If redraw is turned off using WM_SETREDRAW
	* and a tree item that is not a child of the first root is selected or
	* scrolled using TVM_SELECTITEM or TVM_ENSUREVISIBLE, then scrolling
	* does not occur.  The fix is to detect this case, and make sure
	* that redraw is temporarily enabled.  To avoid flashing, DefWindowProc()
	* is called to disable redrawing.
	*
	* NOTE:  The code that actually works around the problem is in the
	* callers of this method.
	*/
        if (getDrawing())
            return false;
        return false;
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
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
        if (all) {
        }
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
        if (all) {
            boolean redraw = false;
            for (TreeItem item : items) {
                if (item != null && item != currentItem) {
                    ((DartTreeItem) item.getImpl()).clear();
                    redraw = true;
                }
            }
        } else {
        }
    }

    long CompareFunc(long lParam1, long lParam2, long lParamSort) {
        TreeItem item1 = items[(int) lParam1], item2 = items[(int) lParam2];
        String text1 = item1.getText((int) lParamSort), text2 = item2.getText((int) lParamSort);
        return sortDirection == SWT.UP ? text1.compareTo(text2) : text2.compareTo(text1);
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.computeSize(this, wHint, hHint, changed);
    }

    @Override
    void createHandle() {
    }

    void createHeaderToolTips() {
    }

    void createItem(TreeColumn column, int index) {
        if (!(0 <= index && index <= columnCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (columnCount == columns.length) {
            TreeColumn[] newColumns = new TreeColumn[columns.length + 4];
            System.arraycopy(columns, 0, newColumns, 0, columns.length);
            columns = newColumns;
        }
        for (TreeItem item : items) {
            if (item != null) {
                String[] strings = ((DartTreeItem) item.getImpl()).strings;
                if (strings != null) {
                    String[] temp = new String[columnCount + 1];
                    System.arraycopy(strings, 0, temp, 0, index);
                    System.arraycopy(strings, index, temp, index + 1, columnCount - index);
                    ((DartTreeItem) item.getImpl()).strings = temp;
                }
                Image[] images = ((DartTreeItem) item.getImpl()).images;
                if (images != null) {
                    Image[] temp = new Image[columnCount + 1];
                    System.arraycopy(images, 0, temp, 0, index);
                    System.arraycopy(images, index, temp, index + 1, columnCount - index);
                    ((DartTreeItem) item.getImpl()).images = temp;
                }
                if (index == 0) {
                    if (columnCount != 0) {
                        if (strings == null) {
                            ((DartTreeItem) item.getImpl()).strings = new String[columnCount + 1];
                            ((DartTreeItem) item.getImpl()).strings[1] = ((DartItem) item.getImpl()).text;
                        }
                        ((DartItem) item.getImpl()).text = "";
                        if (images == null) {
                            ((DartTreeItem) item.getImpl()).images = new Image[columnCount + 1];
                            ((DartTreeItem) item.getImpl()).images[1] = ((DartItem) item.getImpl()).image;
                        }
                        ((DartItem) item.getImpl()).image = null;
                    }
                }
                if (((DartTreeItem) item.getImpl()).cellBackground != null) {
                    int[] cellBackground = ((DartTreeItem) item.getImpl()).cellBackground;
                    int[] temp = new int[columnCount + 1];
                    System.arraycopy(cellBackground, 0, temp, 0, index);
                    System.arraycopy(cellBackground, index, temp, index + 1, columnCount - index);
                    temp[index] = -1;
                    ((DartTreeItem) item.getImpl()).cellBackground = temp;
                }
                if (((DartTreeItem) item.getImpl()).cellForeground != null) {
                    int[] cellForeground = ((DartTreeItem) item.getImpl()).cellForeground;
                    int[] temp = new int[columnCount + 1];
                    System.arraycopy(cellForeground, 0, temp, 0, index);
                    System.arraycopy(cellForeground, index, temp, index + 1, columnCount - index);
                    temp[index] = -1;
                    ((DartTreeItem) item.getImpl()).cellForeground = temp;
                }
                if (((DartTreeItem) item.getImpl()).cellFont != null) {
                    Font[] cellFont = ((DartTreeItem) item.getImpl()).cellFont;
                    Font[] temp = new Font[columnCount + 1];
                    System.arraycopy(cellFont, 0, temp, 0, index);
                    System.arraycopy(cellFont, index, temp, index + 1, columnCount - index);
                    ((DartTreeItem) item.getImpl()).cellFont = temp;
                }
            }
        }
        System.arraycopy(columns, index, columns, index + 1, columnCount++ - index);
        columns[index] = column;
        // conservative
        cachedItemOrder = null;
        /* When the first column is created, hide the horizontal scroll bar */
        if (columnCount == 1) {
            scrollWidth = 0;
            if ((getApi().style & SWT.H_SCROLL) != 0) {
            }
            createItemToolTips();
        }
        setScrollWidth();
        updateImageList();
        updateScrollBar();
    }

    /**
     * The fastest way to insert many items is documented in {@link TreeItem#TreeItem(Tree,int,int)}
     * and {@link TreeItem#setItemCount}
     */
    void createItem(TreeItem item, long hParent, long hInsertAfter, long hItem) {
        if (item == null) {
            return;
        }
        boolean wasEmpty = (itemCount == 0);
        int index = (int) hInsertAfter;
        if (index == -1 && hInsertAfter == -1) {
            index = itemCount;
        }
        int requiredSize = Math.max(index + 1, itemCount + 1);
        if (items == null || requiredSize > items.length) {
            itemsGrowArray(Math.max(4, requiredSize + 4));
        }
        if (index < itemCount) {
            System.arraycopy(items, index, items, index + 1, itemCount - index);
        }
        items[index] = item;
        lastID = Math.max(lastID, index + 1);
        itemCount++;
        if (wasEmpty) {
            Event event = new Event();
            event.detail = 0;
            sendEvent(SWT.EmptinessChanged, event);
        }
    }

    void createItemToolTips() {
    }

    /**
     * On Windows, Tree does not support columns. The workaround is to emulate it
     * by adding a Header control and custom-drawing Tree items.
     *
     * Creates Header (for columns) and wraps (Tree+Header) into an intermediate
     * parent, so that (Tree+Header) behave as one whole. The wrapper is designed
     * to mimic original Tree as much as possible. For that reason, all sorts of
     * settings are copied over.
     */
    void createParent() {
        forceResize();
        /* Columns are emulated by custom drawing items */
        customDraw = true;
        deregister();
        register();
        subclass();
    }

    @Override
    void createWidget() {
        super.createWidget();
        items = new TreeItem[4];
        columns = new TreeColumn[4];
        cachedItemCount = -1;
    }

    private boolean customHeaderDrawing() {
        return headerBackground != -1 || headerForeground != -1;
    }

    @Override
    int defaultBackground() {
        return 0;
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
            if ((getApi().style & SWT.VIRTUAL) != 0) {
            } else {
                for (TreeItem item : items) {
                    if (item != null) {
                    }
                }
            }
        }
        selection = new TreeItem[0];
    }

    void destroyItem(TreeColumn column) {
        int index = 0;
        while (index < columnCount) {
            if (columns[index] == column)
                break;
            index++;
        }
        int[] oldOrder = getColumnOrder();
        int orderIndex = 0;
        while (orderIndex < columnCount) {
            if (oldOrder[orderIndex] == index)
                break;
            orderIndex++;
        }
        System.arraycopy(columns, index + 1, columns, index, --columnCount - index);
        // conservative
        cachedItemOrder = null;
        columns[columnCount] = null;
        if (cachedItemOrder != null && cachedItemOrder.length > 0) {
            int[] newOrder = new int[columnCount];
            int newOrderIndex = 0;
            for (int i = 0; i < cachedItemOrder.length; i++) {
                int orderValue = cachedItemOrder[i];
                if (orderValue == index) {
                    continue;
                } else if (orderValue > index) {
                    newOrder[newOrderIndex++] = orderValue - 1;
                } else {
                    newOrder[newOrderIndex++] = orderValue;
                }
            }
            cachedItemOrder = newOrder;
            columnOrder = newOrder;
        }
        for (TreeItem item : items) {
            if (item != null) {
                if (columnCount == 0) {
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
                        String[] temp = new String[columnCount];
                        System.arraycopy(strings, 0, temp, 0, index);
                        System.arraycopy(strings, index + 1, temp, index, columnCount - index);
                        ((DartTreeItem) item.getImpl()).strings = temp;
                    } else {
                        if (index == 0)
                            ((DartItem) item.getImpl()).text = "";
                    }
                    if (((DartTreeItem) item.getImpl()).images != null) {
                        Image[] images = ((DartTreeItem) item.getImpl()).images;
                        if (index == 0)
                            ((DartItem) item.getImpl()).image = images[1];
                        Image[] temp = new Image[columnCount];
                        System.arraycopy(images, 0, temp, 0, index);
                        System.arraycopy(images, index + 1, temp, index, columnCount - index);
                        ((DartTreeItem) item.getImpl()).images = temp;
                    } else {
                        if (index == 0)
                            ((DartItem) item.getImpl()).image = null;
                    }
                    if (((DartTreeItem) item.getImpl()).cellBackground != null) {
                        int[] cellBackground = ((DartTreeItem) item.getImpl()).cellBackground;
                        int[] temp = new int[columnCount];
                        System.arraycopy(cellBackground, 0, temp, 0, index);
                        System.arraycopy(cellBackground, index + 1, temp, index, columnCount - index);
                        ((DartTreeItem) item.getImpl()).cellBackground = temp;
                    }
                    if (((DartTreeItem) item.getImpl()).cellForeground != null) {
                        int[] cellForeground = ((DartTreeItem) item.getImpl()).cellForeground;
                        int[] temp = new int[columnCount];
                        System.arraycopy(cellForeground, 0, temp, 0, index);
                        System.arraycopy(cellForeground, index + 1, temp, index, columnCount - index);
                        ((DartTreeItem) item.getImpl()).cellForeground = temp;
                    }
                    if (((DartTreeItem) item.getImpl()).cellFont != null) {
                        Font[] cellFont = ((DartTreeItem) item.getImpl()).cellFont;
                        Font[] temp = new Font[columnCount];
                        System.arraycopy(cellFont, 0, temp, 0, index);
                        System.arraycopy(cellFont, index + 1, temp, index, columnCount - index);
                        ((DartTreeItem) item.getImpl()).cellFont = temp;
                    }
                }
            }
        }
        /*
	* When the last column is deleted, show the horizontal
	* scroll bar.  Otherwise, left align the first column
	* and redraw the columns to the right.
	*/
        if (columnCount == 0) {
            scrollWidth = 0;
            if (!hooks(SWT.MeasureItem)) {
            }
        } else {
            if (index == 0) {
                columns[0].style &= ~(SWT.LEFT | SWT.RIGHT | SWT.CENTER);
                columns[0].style |= SWT.LEFT;
            }
        }
        setScrollWidth();
        updateImageList();
        updateScrollBar();
        if (columnCount != 0) {
            TreeColumn[] newColumns = new TreeColumn[columnCount - orderIndex];
            for (TreeColumn newColumn : newColumns) {
                if (!newColumn.isDisposed()) {
                    newColumn.getImpl().sendEvent(SWT.Move);
                }
            }
        }
    }

    void destroyItem(TreeItem item, long hItem) {
        cachedFirstItem = cachedIndexItem = 0;
        cachedItemCount = -1;
        boolean fixRedraw = false;
        if ((getApi().style & SWT.DOUBLE_BUFFERED) == 0) {
        }
        if (fixRedraw) {
        }
        ignoreDeselect = ignoreSelect = lockSelection = true;
        shrink = ignoreShrink = true;
        ignoreShrink = false;
        /*
	 * Bug 546333: When TVGN_CARET item is deleted, Windows automatically
	 * sets selection to some other item. We do not want that.
	 */
        ignoreDeselect = ignoreSelect = lockSelection = false;
        if (fixRedraw) {
        }
        /*
	 Note: Don't update scrollbars when drawing is disabled.
	 This gives significant improvement for bulk remove scenarios.
	 Later, setRedraw(true) will update scrollbars once.
	 */
        if (getDrawing())
            updateScrollBar();
        {
            boolean wasNotEmpty = (itemCount > 0);
            if (itemCount > 0) {
                itemCount--;
            }
            if (wasNotEmpty && itemCount == 0) {
                Event event = new Event();
                event.detail = 1;
                sendEvent(SWT.EmptinessChanged, event);
            }
        }
    }

    @Override
    void destroyScrollBar(int type) {
        super.destroyScrollBar(type);
        if ((getApi().style & (SWT.H_SCROLL | SWT.V_SCROLL)) == 0) {
        } else {
            if ((getApi().style & SWT.H_SCROLL) == 0) {
            }
        }
    }

    @Override
    void enableDrag(boolean enabled) {
        if (enabled && hooks(SWT.DragDetect)) {
        } else {
        }
    }

    @Override
    void enableWidget(boolean enabled) {
        super.enableWidget(enabled);
        /*
	* Bug in Windows.  On Vista only, Windows does not draw using
	* the background color when the tree is disabled.  The fix is
	* to set the default color, even when the color has not been
	* changed, causing Windows to draw correctly.
	*/
        Control control = findBackgroundControl();
        if (control == null)
            control = this.getApi();
        if (control.getImpl()._backgroundImage() == null) {
            _setBackgroundPixel(hasCustomBackground() ? control.getImpl().getBackgroundPixel() : -1);
        }
        /*
	* Feature in Windows.  When the tree has the style
	* TVS_FULLROWSELECT, the background color for the
	* entire row is filled when an item is painted,
	* drawing on top of the sort column color.  The fix
	* is to clear TVS_FULLROWSELECT when there is
	* as sort column.
	*/
        updateFullSelection();
    }

    int findIndex(long hFirstItem, long hItem) {
        if (hFirstItem == 0)
            return -1;
        if (hFirstItem == cachedFirstItem) {
            if (cachedFirstItem == hItem) {
                cachedIndexItem = cachedFirstItem;
                return cachedIndex = 0;
            }
            if (cachedIndexItem == hItem)
                return cachedIndex;
            return -1;
        }
        int index = 0;
        long hNextItem = hFirstItem;
        while (hNextItem != 0 && hNextItem != hItem) {
            index++;
        }
        if (hNextItem == hItem) {
            cachedItemCount = -1;
            cachedFirstItem = hFirstItem;
            cachedIndexItem = hNextItem;
            return cachedIndex = index;
        }
        return -1;
    }

    @Override
    Widget findItem(long hItem) {
        return _getItem(hItem);
    }

    long findItem(long hFirstItem, int index) {
        if (hFirstItem == 0)
            return 0;
        if (hFirstItem == cachedFirstItem) {
            if (index == 0) {
                cachedIndex = 0;
                return cachedIndexItem = cachedFirstItem;
            }
            if (cachedIndex == index)
                return cachedIndexItem;
            if (cachedIndex - 1 == index) {
                --cachedIndex;
            }
            if (cachedIndex + 1 == index) {
                cachedIndex++;
            }
            if (index < cachedIndex) {
                int previousIndex = cachedIndex - 1;
                if (index == previousIndex) {
                    cachedIndex = previousIndex;
                }
            } else {
                int nextIndex = cachedIndex + 1;
                if (index == nextIndex) {
                    cachedIndex = nextIndex;
                }
            }
            return 0;
        }
        int nextIndex = 0;
        long hNextItem = hFirstItem;
        while (hNextItem != 0 && nextIndex < index) {
            nextIndex++;
        }
        if (index == nextIndex) {
            cachedItemCount = -1;
            cachedIndex = nextIndex;
            cachedFirstItem = hFirstItem;
            return cachedIndexItem = hNextItem;
        }
        return 0;
    }

    TreeItem getFocusItem() {
        return null;
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
        return DPIUtil.pixelToPoint(getGridLineWidthInPixels(), getZoom());
    }

    int getGridLineWidthInPixels() {
        return GRID_WIDTH;
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
        return SwtColor.win32_new(display, getHeaderBackgroundPixel());
    }

    private int getHeaderBackgroundPixel() {
        return headerBackground != -1 ? headerBackground : defaultBackground();
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
        return SwtColor.win32_new(display, getHeaderForegroundPixel());
    }

    private int getHeaderForegroundPixel() {
        return headerForeground != -1 ? headerForeground : defaultForeground();
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
        return DPIUtil.pixelToPoint(getHeaderHeightInPixels(), getZoom());
    }

    int getHeaderHeightInPixels() {
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

    Point getImageSize() {
        return new Point(0, getItemHeightInPixels());
    }

    long getBottomItem() {
        return 0;
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
        if (cachedItemOrder != null) {
            return cachedItemOrder.clone();
        }
        return this.columnOrder;
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
        if (items == null)
            error(SWT.ERROR_INVALID_RANGE);
        int nonNullCount = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                if (nonNullCount == index) {
                    return items[i];
                }
                nonNullCount++;
            }
        }
        error(SWT.ERROR_INVALID_RANGE);
        // Never reached due to error() above
        return null;
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
        return null;
    }

    TreeItem getItemInPixels(Point point) {
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
        if (this.items == null)
            return 0;
        int count = 0;
        for (TreeItem item : this.items) {
            if (item != null) {
                count++;
            }
        }
        return count;
    }

    int getItemCount(long hItem) {
        int count = 0;
        long hFirstItem = hItem;
        if (hItem == cachedFirstItem) {
            if (cachedItemCount != -1)
                return cachedItemCount;
            hFirstItem = cachedIndexItem;
            count = cachedIndex;
        }
        while (hFirstItem != 0) {
            count++;
        }
        if (hItem == cachedFirstItem)
            cachedItemCount = count;
        return count;
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
        return DPIUtil.pixelToPoint(getItemHeightInPixels(), getZoom());
    }

    int getItemHeightInPixels() {
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
        if (this.items == null)
            return new TreeItem[0];
        int count = getItemCount();
        TreeItem[] result = new TreeItem[count];
        int resultIndex = 0;
        for (int i = 0; i < this.items.length && resultIndex < count; i++) {
            if (this.items[i] != null) {
                result[resultIndex++] = this.items[i];
            }
        }
        return result;
    }

    TreeItem[] getItems(long hTreeItem) {
        int count = 0;
        long hItem = hTreeItem;
        while (hItem != 0) {
            count++;
        }
        int index = 0;
        TreeItem[] result = new TreeItem[count];
        if (index != count) {
            TreeItem[] newResult = new TreeItem[index];
            System.arraycopy(result, 0, newResult, 0, index);
            result = newResult;
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
        return linesVisible;
    }

    long getNextSelection(long hItem) {
        while (hItem != 0) {
        }
        return 0;
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

    int getSortColumnPixel() {
        return 0;
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

    boolean hitTestSelection(long hItem, int x, int y) {
        if (hItem == 0)
            return false;
        TreeItem item = _getItem(hItem);
        if (item == null)
            return false;
        if (!hooks(SWT.MeasureItem))
            return false;
        boolean result = false;
        //BUG? - moved columns, only hittest first column
        //BUG? - check drag detect
        int[] order = new int[1], index = new int[1];
        //	if (isDisposed () || item.isDisposed ()) return false;
        return result;
    }

    int imageIndex(Image image, int index) {
        return 0;
    }

    int imageIndexHeader(Image image) {
        return 0;
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
        return TreeHelper.indexOf(this, item);
    }

    boolean isCustomToolTip() {
        return hooks(SWT.MeasureItem);
    }

    @Override
    boolean isUseWsBorder() {
        return true;
    }

    int itemsGetFreeCapacity() {
        int count = 0;
        for (TreeItem item : items) {
            if (item == null)
                count++;
        }
        return count;
    }

    void itemsGrowArray(int newCapacity) {
        TreeItem[] newItems = new TreeItem[newCapacity];
        System.arraycopy(items, 0, newItems, 0, items.length);
        items = newItems;
    }

    void redrawSelection() {
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
    }

    @Override
    void register() {
        super.register();
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (items != null) {
            for (TreeItem item : items) {
                if (item != null && !item.isDisposed()) {
                    item.getImpl().release(false);
                }
            }
            items = null;
        }
        if (columns != null) {
            for (TreeColumn column : columns) {
                if (column != null && !column.isDisposed()) {
                    column.getImpl().release(false);
                }
            }
            columns = null;
        }
        super.releaseChildren(destroy);
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        /*
	* Feature in Windows.  For some reason, when TVM_GETIMAGELIST
	* or TVM_SETIMAGELIST is sent, the tree issues NM_CUSTOMDRAW
	* messages.  This behavior is unwanted when the tree is being
	* disposed.  The fix is to ignore NM_CUSTOMDRAW messages by
	* clearing the custom draw flag.
	*
	* NOTE: This only happens on Windows XP.
	*/
        customDraw = false;
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
        cachedFirstItem = cachedIndexItem = 0;
        cachedItemCount = -1;
        for (TreeItem item : items) {
            if (item != null && !item.isDisposed()) {
                item.getImpl().release(false);
            }
        }
        ignoreDeselect = ignoreSelect = true;
        shrink = ignoreShrink = true;
        ignoreShrink = false;
        ignoreDeselect = ignoreSelect = false;
        hAnchor = hInsert = cachedFirstItem = cachedIndexItem = 0;
        cachedItemCount = -1;
        items = new TreeItem[4];
        scrollWidth = 0;
        setScrollWidth();
        lastID = 0;
        itemCount = 0;
        updateScrollBar();
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
            for (TreeItem item : items) {
                if (item != null)
                    ((DartWidget) item.getImpl()).reskinChildren(flags);
            }
        }
        if (columns != null) {
            for (TreeColumn column : columns) {
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
        long hItem = 0;
        if (item != null) {
            if (item.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            hItem = item.handle;
        }
        hInsert = hItem;
        insertAfter = !before;
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
        count = Math.max(0, count);
        TreeHelper.setItemCount(this, count);
    }

    void setItemCount(int count, long hParent) {
        long itemInsertAfter = 0;
        int indexInsertAfter = 0;
        int numInserted = 0;
        long itemDeleteFrom = 0;
        {
        }
        boolean redraw = false;
        boolean expanded = false;
        if (!redraw && (getApi().style & SWT.VIRTUAL) != 0) {
        }
        if (itemDeleteFrom != 0) {
            while (itemDeleteFrom != 0) {
            }
        } else {
            // For performance reasons, reserve the necessary space in items[]
            int freeCapacity = itemsGetFreeCapacity();
            if (numInserted > freeCapacity)
                itemsGrowArray(items.length + numInserted - freeCapacity);
            // Adjust cached variables to insertion point.
            {
                cachedFirstItem = 0;
                cachedIndexItem = 0;
                cachedIndex = 0;
                cachedItemCount = 0;
            }
            // Note: on Windows, insert complexity is O(pos), so for performance
            // reasons, all items are inserted at minimum possible position, that
            // is, all at the same position.
            if ((getApi().style & SWT.VIRTUAL) != 0) {
                for (int i = 0; i < numInserted; i++) {
                    /*
				 * Bug 206806: Windows sends 'TVN_GETDISPINFO' when item is
				 * being inserted. This causes 'SWT.SetData' to be sent to
				 * user code, but user code will likely be confused by
				 * inconsistent Tree state (because we're still inserting):
				 * - 'getItemCount()' will be wrong
				 * - 'Event.index' will be wrong
				 * The workaround is to temporarily suppress 'SWT.SetData'. Note
				 * that the boolean flag is misleadingly used for multiple
				 * purposes. What really happens is that 'TVN_GETDISPINFO' will
				 * queue a repaint for item and early return.
				 */
                    if (expanded)
                        ignoreShrink = true;
                    createItem(null, hParent, itemInsertAfter, 0);
                    if (expanded)
                        ignoreShrink = false;
                }
            } else {
                for (int i = 0; i < numInserted; i++) {
                    new TreeItem(this.getApi(), SWT.NONE, hParent, itemInsertAfter, 0);
                }
            }
        }
        if (redraw) {
        }
    }

    /**
     * Sets the height of the area which would be used to
     * display <em>one</em> of the items in the tree.
     *
     * @param itemHeight the height of one item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    /*public*/
    void setItemHeight(int itemHeight) {
        checkWidget();
        if (itemHeight < -1)
            error(SWT.ERROR_INVALID_ARGUMENT);
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
        checkWidget();
        if (!java.util.Objects.equals(this.linesVisible, show)) {
            dirty();
        }
        if (linesVisible == show)
            return;
        linesVisible = show;
    }

    @Override
    long scrolledHandle() {
        return 0;
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
        TreeItem[] newValue = new TreeItem[] { item };
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if ((getApi().style & SWT.SINGLE) != 0) {
            setSelection(item);
            return;
        }
        expandToItem(item);
        this.selection = newValue;
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
        items = TreeHelper.collectAllItems(this);
        selection = items;
    }

    Event sendMeasureItemEvent(TreeItem item, int index, long hDC, int detail) {
        GCData data = new GCData();
        data.device = display;
        data.font = item.getFont(index);
        GC gc = createNewGC(hDC, data);
        Event event = new Event();
        event.item = item;
        event.gc = gc;
        event.index = index;
        event.detail = detail;
        sendEvent(SWT.MeasureItem, event);
        event.gc = null;
        gc.dispose();
        if (isDisposed() || item.isDisposed())
            return null;
        return event;
    }

    @Override
    void setBackgroundImage(long hBitmap) {
        super.setBackgroundImage(hBitmap);
        if (hBitmap != 0) {
            _setBackgroundPixel(-1);
        } else {
            Control control = findBackgroundControl();
            if (control == null)
                control = this.getApi();
            if (control.getImpl()._backgroundImage() == null) {
                setBackgroundPixel(control.getImpl().getBackgroundPixel());
            }
        }
        /*
	* Feature in Windows.  When the tree has the style
	* TVS_FULLROWSELECT, the background color for the
	* entire row is filled when an item is painted,
	* drawing on top of the background image.  The fix
	* is to clear TVS_FULLROWSELECT when a background
	* image is set.
	*/
        updateFullSelection();
    }

    @Override
    void setBackgroundPixel(int pixel) {
        Control control = findImageControl();
        if (control != null) {
            setBackgroundImage(control.getImpl()._backgroundImage());
            return;
        }
        _setBackgroundPixel(pixel);
        /*
	* Feature in Windows.  When the tree has the style
	* TVS_FULLROWSELECT, the background color for the
	* entire row is filled when an item is painted,
	* drawing on top of the background image.  The fix
	* is to restore TVS_FULLROWSELECT when a background
	* color is set.
	*/
        updateFullSelection();
    }

    @Override
    public void setCursor() {
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
        int[] newValue = order;
        if (!java.util.Objects.equals(this.columnOrder, newValue)) {
            dirty();
        }
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
                error(SWT.ERROR_INVALID_RANGE);
            if (seen[index])
                error(SWT.ERROR_INVALID_ARGUMENT);
            seen[index] = true;
            if (index != oldOrder[i])
                reorder = true;
        }
        this.columnOrder = newValue;
        if (reorder) {
            for (int i = 0; i < columnCount; i++) {
            }
            cachedItemOrder = order.clone();
            updateImageList();
            TreeColumn[] newColumns = new TreeColumn[columnCount];
            System.arraycopy(columns, 0, newColumns, 0, columnCount);
            for (int i = 0; i < columnCount; i++) {
                TreeColumn column = newColumns[i];
                if (!column.isDisposed()) {
                }
            }
        }
    }

    void setCheckboxImageList() {
        if ((getApi().style & SWT.CHECK) == 0)
            return;
    }

    @Override
    public void setFont(Font font) {
        dirty();
        checkWidget();
        super.setFont(font);
        if ((getApi().style & SWT.CHECK) != 0)
            setCheckboxImageList();
    }

    @Override
    void setForegroundPixel(int pixel) {
        /*
	* Bug in Windows.  When the tree is using the explorer
	* theme, it does not use COLOR_WINDOW_TEXT for the
	* foreground.  When TVM_SETTEXTCOLOR is called with -1,
	* it resets the color to black, not COLOR_WINDOW_TEXT.
	* The fix is to explicitly set the color.
	*/
        if (explorerTheme) {
            if (pixel == -1)
                pixel = defaultForeground();
        }
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
        Color newValue = color;
        if (!java.util.Objects.equals(this._headerBackground, newValue)) {
            dirty();
        }
        checkWidget();
        int pixel = -1;
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            pixel = color.handle;
        }
        if (pixel == headerBackground)
            return;
        headerBackground = pixel;
        this._headerBackground = newValue;
        if (getHeaderVisible()) {
        }
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
        Color newValue = color;
        if (!java.util.Objects.equals(this._headerForeground, newValue)) {
            dirty();
        }
        checkWidget();
        int pixel = -1;
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            pixel = color.handle;
        }
        if (pixel == headerForeground)
            return;
        headerForeground = pixel;
        this._headerForeground = newValue;
        if (getHeaderVisible()) {
        }
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
        boolean newValue = show;
        if (!java.util.Objects.equals(this.headerVisible, newValue)) {
            dirty();
        }
        checkWidget();
        if (show) {
        } else {
        }
        setScrollWidth();
        updateHeaderToolTips();
        this.headerVisible = newValue;
        updateScrollBar();
    }

    @Override
    public void setRedraw(boolean redraw) {
        dirty();
        checkWidget();
        /*
	* Feature in Windows.  When WM_SETREDRAW is used to
	* turn off redraw, the scroll bars are updated when
	* items are added and removed.  The fix is to call
	* the default window proc to stop all drawing.
	*
	* Bug in Windows.  For some reason, when WM_SETREDRAW
	* is used to turn redraw on for a tree and the tree
	* contains no items, the last item in the tree does
	* not redraw properly.  If the tree has only one item,
	* that item is not drawn.  If another window is dragged
	* on top of the item, parts of the item are redrawn
	* and erased at random.  The fix is to ensure that this
	* case doesn't happen by inserting and deleting an item
	* when redraw is turned on and there are no items in
	* the tree.
	*/
        long hItem = 0;
        boolean willEnableDraw = redraw && (drawCount == 1);
        if (willEnableDraw) {
            updateScrollBar();
        }
        super.setRedraw(redraw);
        boolean haveDisabledDraw = !redraw && (drawCount == 1);
        if (haveDisabledDraw) {
        }
        if (hItem != 0) {
            ignoreShrink = true;
            ignoreShrink = false;
        }
    }

    void setScrollWidth() {
        int width = 0;
        for (int i = 0; i < columnCount; i++) {
        }
        setScrollWidth(Math.max(scrollWidth, width));
    }

    void setScrollWidth(int width) {
        //TEMPORARY CODE
        if (columnCount == 0 && width == 0) {
        } else {
            if ((getApi().style & SWT.H_SCROLL) != 0) {
            }
        }
        if (horizontalBar != null) {
            horizontalBar.setIncrement(INCREMENT);
        }
        boolean oldIgnore = ignoreResize;
        ignoreResize = true;
        ignoreResize = oldIgnore;
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
        TreeItem[] newValue = items;
        if (!java.util.Objects.equals(this.selection, newValue)) {
            dirty();
        }
        checkWidget();
        if (items == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        int length = items.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1)) {
            deselectAll();
            return;
        }
        /* Select/deselect the first item */
        TreeItem item = items[0];
        if (item != null) {
            if (item.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            long hNewItem = hAnchor = item.handle;
            /*
		* Bug in Windows.  When TVM_SELECTITEM is used to select and
		* scroll an item to be visible and the client area of the tree
		* is smaller that the size of one item, TVM_SELECTITEM makes
		* the next item in the tree visible by making it the top item
		* instead of making the desired item visible.  The fix is to
		* detect the case when the client area is too small and make
		* the desired visible item be the top item in the tree.
		*
		* Note that TVM_SELECTITEM when called with TVGN_FIRSTVISIBLE
		* also requires the work around for scrolling.
		*/
            boolean fixScroll = checkScroll(hNewItem);
            if (fixScroll) {
            }
            ignoreSelect = true;
            ignoreSelect = false;
            if (fixScroll) {
            }
        }
        {
            if ((getApi().style & SWT.SINGLE) != 0) {
                if (item != null && !item.isDisposed()) {
                    this.selection = new TreeItem[] { item };
                } else {
                    this.selection = new TreeItem[0];
                }
                return;
            }
        }
        if ((getApi().style & SWT.SINGLE) != 0)
            return;
        if ((getApi().style & SWT.VIRTUAL) != 0) {
        } else {
            for (TreeItem item2 : this.items) {
                item = item2;
                if (item != null) {
                    int index = 0;
                    while (index < length) {
                        if (items[index] == item)
                            break;
                        index++;
                    }
                }
            }
        }
        java.util.Set<TreeItem> uniqueItems = new java.util.LinkedHashSet<>();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                uniqueItems.add(items[i]);
            }
        }
        TreeItem[] validItems = uniqueItems.toArray(new TreeItem[0]);
        java.util.Arrays.sort(validItems, (a, b) -> {
            int indexA = -1;
            int indexB = -1;
            for (int i = 0; i < this.items.length; i++) {
                if (this.items[i] == a)
                    indexA = i;
                if (this.items[i] == b)
                    indexB = i;
            }
            return Integer.compare(indexA, indexB);
        });
        this.selection = validItems;
    }

    void expandToItem(TreeItem item) {
        TreeItem parentItem = item.getParentItem();
        if (parentItem != null && !parentItem.getExpanded()) {
            expandToItem(parentItem);
            parentItem.setExpanded(true);
            Event event = new Event();
            event.item = parentItem;
            sendEvent(SWT.Expand, event);
        }
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
        checkWidget();
        if (!java.util.Objects.equals(this.sortColumn, column)) {
            dirty();
        }
        if (column != null && column.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (sortColumn != null && !sortColumn.isDisposed()) {
            ((DartTreeColumn) sortColumn.getImpl()).setSortDirection(SWT.NONE);
        }
        sortColumn = column;
        if (sortColumn != null && sortDirection != SWT.NONE) {
            ((DartTreeColumn) sortColumn.getImpl()).setSortDirection(sortDirection);
        }
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
        checkWidget();
        if (!java.util.Objects.equals(this.sortDirection, direction)) {
            dirty();
        }
        if ((direction & (SWT.UP | SWT.DOWN)) == 0 && direction != SWT.NONE)
            return;
        sortDirection = direction;
        if (sortColumn != null && !sortColumn.isDisposed()) {
            ((DartTreeColumn) sortColumn.getImpl()).setSortDirection(direction);
        }
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
        TreeItem newValue = item;
        if (!java.util.Objects.equals(this.topItem, newValue)) {
            dirty();
        }
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        long hItem = item.handle;
        boolean fixScroll = checkScroll(hItem), redraw = false;
        if (fixScroll) {
        } else {
        }
        if (fixScroll) {
        } else {
            if (redraw) {
            }
        }
        this.topItem = newValue;
        updateScrollBar();
    }

    /**
     * Set indent for Tree;
     * In a Tree without imageList, the indent also controls the chevron (glyph) size.
     */
    private void calculateAndApplyIndentSize() {
    }

    void showItem(long hItem) {
        updateScrollBar();
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
        int index = indexOf(column);
        if (index == -1)
            return;
        if (0 <= index && index < columnCount) {
            forceResize();
        }
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
        showItem(item.handle);
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
        long hItem = 0;
        if ((getApi().style & SWT.SINGLE) != 0) {
            if (hItem == 0)
                return;
        } else {
            if ((getApi().style & SWT.VIRTUAL) != 0) {
            } else {
                //FIXME - this code expands first selected item it finds
                int index = 0;
                while (index < items.length) {
                    TreeItem item = items[index];
                    if (item != null) {
                    }
                    index++;
                }
            }
        }
        if (hItem != 0)
            showItem(hItem);
    }

    /*public*/
    void sort() {
        checkWidget();
        if ((getApi().style & SWT.VIRTUAL) != 0)
            return;
    }

    void sort(long hParent, boolean all) {
        cachedFirstItem = cachedIndexItem = 0;
        if (sortDirection == SWT.UP || sortDirection == SWT.NONE) {
        } else {
        }
    }

    @Override
    void subclass() {
        super.subclass();
    }

    @Override
    public long topHandle() {
        return 0;
    }

    void updateFullSelection() {
        if ((getApi().style & SWT.FULL_SELECTION) != 0) {
        }
    }

    void updateHeaderToolTips() {
        for (int i = 0; i < columnCount; i++) {
        }
    }

    void updateImageList() {
        int i = 0, index = getFirstColumnIndex();
        while (i < items.length) {
            TreeItem item = items[i];
            if (item != null) {
                Image image = null;
                if (index == 0) {
                    image = ((DartItem) item.getImpl()).image;
                } else {
                    Image[] images = ((DartTreeItem) item.getImpl()).images;
                    if (images != null)
                        image = images[index];
                }
                if (image != null)
                    break;
            }
            i++;
        }
    }

    @Override
    void updateMenuLocation(Event event) {
        Rectangle clientArea = getClientAreaInPixels();
        int x = clientArea.x, y = clientArea.y;
        TreeItem focusItem = getFocusItem();
        if (focusItem != null) {
            Rectangle bounds = ((DartTreeItem) focusItem.getImpl()).getBoundsInPixels(0);
            if (((DartItem) focusItem.getImpl()).text != null && ((DartItem) focusItem.getImpl()).text.length() != 0) {
                bounds = ((DartTreeItem) focusItem.getImpl()).getBoundsInPixels();
            }
            x = Math.max(x, bounds.x + bounds.width / 2);
            x = Math.min(x, clientArea.x + clientArea.width);
            y = Math.max(y, bounds.y + bounds.height);
            y = Math.min(y, clientArea.y + clientArea.height);
        }
        Point pt = toDisplayInPixels(x, y);
        int zoom = getZoom();
        event.setLocation(DPIUtil.pixelToPoint(pt.x, zoom), DPIUtil.pixelToPoint(pt.y, zoom));
    }

    @Override
    void updateOrientation() {
        super.updateOrientation();
        if ((getApi().style & SWT.CHECK) != 0)
            setCheckboxImageList();
    }

    /**
     * Copies Tree's scrollbar state to intermediate parent.
     *
     * If Tree has a header, then (Tree+header) get wrapped into intermediate
     * parent. This parent also has scrollbar, and it is configured to
     * obscure the Tree's scrollbar - I think this is due to aesthetic
     * reasons where the new scrollbar also extends over header. Since it
     * obscures the true scrollbar, it always needs to be in sync with the
     * true scrollbar.
     */
    void updateScrollBar() {
    }

    @Override
    void unsubclass() {
        super.unsubclass();
    }

    @Override
    int widgetStyle() {
        if ((getApi().style & (SWT.H_SCROLL | SWT.V_SCROLL)) == 0) {
        } else {
            if ((getApi().style & SWT.H_SCROLL) == 0) {
            }
        }
        return 0;
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof Tree tree)) {
            return;
        }
        // if the item height was set at least once programmatically with TVM_SETITEMHEIGHT,
        // the item height of the tree is not managed by the OS anymore e.g. when the zoom
        // on the monitor is changed, the height of the item will stay at the fixed size.
        // Resetting it will re-enable the default behavior again
        ((DartTree) tree.getImpl()).setItemHeight(-1);
        ((DartTree) tree.getImpl()).calculateAndApplyIndentSize();
        ((DartTree) tree.getImpl()).updateOrientation();
        ((DartTree) tree.getImpl()).setScrollWidth();
        // Reset of CheckBox Size required (if SWT.Check is not set, this is a no-op)
        ((DartTree) tree.getImpl()).setCheckboxImageList();
    }

    int[] columnOrder = new int[0];

    boolean editable = false;

    Color _headerBackground;

    Color _headerForeground;

    boolean headerVisible;

    TreeItem[] selection = new TreeItem[0];

    TreeItem topItem;

    public TreeItem[] _items() {
        return items;
    }

    public TreeColumn[] _columns() {
        return columns;
    }

    public int _columnCount() {
        return columnCount;
    }

    public TreeItem _currentItem() {
        return currentItem;
    }

    public TreeColumn _sortColumn() {
        return sortColumn;
    }

    public long _hAnchor() {
        return hAnchor;
    }

    public long _hInsert() {
        return hInsert;
    }

    public long _hSelect() {
        return hSelect;
    }

    public int _lastID() {
        return lastID;
    }

    public int _sortDirection() {
        return sortDirection;
    }

    public boolean _dragStarted() {
        return dragStarted;
    }

    public boolean _gestureCompleted() {
        return gestureCompleted;
    }

    public boolean _insertAfter() {
        return insertAfter;
    }

    public boolean _shrink() {
        return shrink;
    }

    public boolean _ignoreShrink() {
        return ignoreShrink;
    }

    public boolean _ignoreSelect() {
        return ignoreSelect;
    }

    public boolean _ignoreExpand() {
        return ignoreExpand;
    }

    public boolean _ignoreDeselect() {
        return ignoreDeselect;
    }

    public boolean _ignoreResize() {
        return ignoreResize;
    }

    public boolean _lockSelection() {
        return lockSelection;
    }

    public boolean _oldSelected() {
        return oldSelected;
    }

    public boolean _newSelected() {
        return newSelected;
    }

    public boolean _ignoreColumnMove() {
        return ignoreColumnMove;
    }

    public boolean _ignoreColumnResize() {
        return ignoreColumnResize;
    }

    public boolean _linesVisible() {
        return linesVisible;
    }

    public boolean _customDraw() {
        return customDraw;
    }

    public boolean _painted() {
        return painted;
    }

    public boolean _ignoreItemHeight() {
        return ignoreItemHeight;
    }

    public boolean _ignoreCustomDraw() {
        return ignoreCustomDraw;
    }

    public boolean _ignoreDrawForeground() {
        return ignoreDrawForeground;
    }

    public boolean _ignoreDrawBackground() {
        return ignoreDrawBackground;
    }

    public boolean _ignoreDrawFocus() {
        return ignoreDrawFocus;
    }

    public boolean _ignoreDrawSelection() {
        return ignoreDrawSelection;
    }

    public boolean _ignoreDrawHot() {
        return ignoreDrawHot;
    }

    public boolean _ignoreFullSelection() {
        return ignoreFullSelection;
    }

    public boolean _explorerTheme() {
        return explorerTheme;
    }

    public boolean _createdAsRTL() {
        return createdAsRTL;
    }

    public boolean _headerItemDragging() {
        return headerItemDragging;
    }

    public int _scrollWidth() {
        return scrollWidth;
    }

    public int _selectionForeground() {
        return selectionForeground;
    }

    public long _lastTimerID() {
        return lastTimerID;
    }

    public int _lastTimerCount() {
        return lastTimerCount;
    }

    public int _headerBackground() {
        return headerBackground;
    }

    public int _headerForeground() {
        return headerForeground;
    }

    public int[] _cachedItemOrder() {
        return cachedItemOrder;
    }

    public long _cachedFirstItem() {
        return cachedFirstItem;
    }

    public long _cachedIndexItem() {
        return cachedIndexItem;
    }

    public int _cachedIndex() {
        return cachedIndex;
    }

    public int _cachedItemCount() {
        return cachedItemCount;
    }

    public int[] _columnOrder() {
        return columnOrder;
    }

    public boolean _editable() {
        return editable;
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

    public TreeItem[] _selection() {
        return selection;
    }

    public TreeItem _topItem() {
        return topItem;
    }

    public void updateChildItems() {
        if (items == null)
            return;
        items = java.util.Arrays.stream(items).filter(child -> child != null && !child.isDisposed()).toArray(TreeItem[]::new);
        lastID = items.length;
    }

    int itemCount;

    public void _setEditable(boolean value) {
        if (this.editable != value) {
            this.editable = value;
            dirty();
        }
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Modify", "Modify", e -> {
            getDisplay().asyncExec(() -> {
                TreeHelper.handleModify(this, e);
            });
        });
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
