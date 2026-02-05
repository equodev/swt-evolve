/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *      Roland Oldenburg <r.oldenburg@hsp-software.de> - Bug 292199
 *      Conrad Groth - Bug 384906
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

//import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import java.util.Objects;
import java.util.Arrays;
import dev.equo.swt.*;

/**
 * Instances of this class implement a selectable user interface
 * object that displays a list of images and strings and issues
 * notification when selected.
 * <p>
 * The item children that may be added to instances of this class
 * must be of type <code>TableItem</code>.
 * </p><p>
 * Style <code>VIRTUAL</code> is used to create a <code>Table</code> whose
 * <code>TableItem</code>s are to be populated by the client on an on-demand basis
 * instead of up-front.  This can provide significant performance improvements for
 * tables that are very large or for which <code>TableItem</code> population is
 * expensive (for example, retrieving values from an external source).
 * </p><p>
 * Here is an example of using a <code>Table</code> with style <code>VIRTUAL</code>:</p>
 * <pre><code>
 *  final Table table = new Table (parent, SWT.VIRTUAL | SWT.BORDER);
 *  table.setItemCount (1000000);
 *  table.addListener (SWT.SetData, new Listener () {
 *      public void handleEvent (Event event) {
 *          TableItem item = (TableItem) event.item;
 *          int index = table.indexOf (item);
 *          item.setText ("Item " + index);
 *          System.out.println (item.getText ());
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
 * <dd>SINGLE, MULTI, CHECK, FULL_SELECTION, HIDE_SELECTION, VIRTUAL, NO_SCROLL, NO_SEARCH</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection, SetData, MeasureItem, EraseItem, PaintItem</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles SINGLE, and MULTI may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#table">Table, TableItem, TableColumn snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartTable extends DartComposite implements ITable {

    TableItem[] items;

    TableColumn[] columns;

    int columnCount;

    TableItem currentItem;

    TableColumn sortColumn;

    boolean[] columnVisible;

    long headerToolTipHandle, hwndHeader, itemToolTipHandle;

    boolean ignoreCustomDraw, ignoreDrawForeground, ignoreDrawBackground, ignoreDrawFocus, ignoreDrawSelection, ignoreDrawHot;

    boolean customDraw, dragStarted, explorerTheme, firstColumnImage, fixScrollWidth, tipRequested, wasSelected, wasResized, painted;

    boolean ignoreActivate, ignoreSelect, ignoreShrink, ignoreResize, ignoreColumnMove, ignoreColumnResize, fullRowSelect, settingItemHeight;

    boolean headerItemDragging;

    int itemHeight, lastIndexOf, lastWidth, sortDirection, resizeCount, selectionForeground, hotIndex;

    int headerBackground = -1;

    int headerForeground = -1;

    static long /*final*/
    HeaderProc;

    static final int INSET = 4;

    static final int GRID_WIDTH = 1;

    static final int SORT_WIDTH = 10;

    static final int HEADER_MARGIN = 12;

    static final int HEADER_EXTRA = 3;

    static final int VISTA_EXTRA = 2;

    static final int EXPLORER_EXTRA = 2;

    static final int H_SCROLL_LIMIT = 32;

    static final int V_SCROLL_LIMIT = 16;

    static final int DRAG_IMAGE_SIZE = 301;

    static boolean COMPRESS_ITEMS = true;

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
     * @see SWT#HIDE_SELECTION
     * @see SWT#VIRTUAL
     * @see SWT#NO_SCROLL
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartTable(Composite parent, int style, Table api) {
        super(parent, checkStyle(style), api);
    }

    @Override
    void _addListener(int eventType, Listener listener) {
        super._addListener(eventType, listener);
        switch(eventType) {
            case SWT.MeasureItem:
            case SWT.EraseItem:
            case SWT.PaintItem:
                setCustomDraw(true);
                setBackgroundTransparent(true);
                break;
        }
    }

    boolean _checkGrow(int count) {
        return false;
    }

    void _checkShrink() {
        //TODO - code could be shared but it would mix keyed and non-keyed logic
    }

    void _clearItems() {
        items = null;
    }

    TableItem _getItem(int index) {
        return _getItem(index, true);
    }

    //TODO - check senders who have count (watch methods that change the count)
    TableItem _getItem(int index, boolean create) {
        return _getItem(index, create, -1);
    }

    TableItem _getItem(int index, boolean create, int count) {
        if (items == null || index < 0 || index >= items.length)
            return null;
        if ((getApi().style & SWT.VIRTUAL) == 0 || !create)
            return items[index];
        if (items[index] != null)
            return items[index];
        return items[index] = new TableItem(this.getApi(), SWT.NONE, -1, false);
    }

    void _getItems(TableItem[] result, int count) {
    }

    boolean _hasItems() {
        return items != null;
    }

    void _initItems() {
        items = new TableItem[0];
    }

    /* NOTE: The array has already been grown to have space for the new item */
    void _insertItem(int index, TableItem item, int count) {
    }

    void _removeItem(int index, int count) {
    }

    /* NOTE: Removes from start to index - 1 */
    void _removeItems(int start, int index, int count) {
    }

    void _setItemCount(int count, int itemCount) {
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
        return checkBits(style, SWT.SINGLE, SWT.MULTI, 0, 0, 0, 0);
    }

    @Override
    void checkBuffered() {
        super.checkBuffered();
        getApi().style |= SWT.DOUBLE_BUFFERED;
    }

    boolean checkData(TableItem item, boolean redraw) {
        if ((getApi().style & SWT.VIRTUAL) == 0)
            return true;
        return checkData(item, indexOf(item), redraw);
    }

    boolean checkData(TableItem item, int index, boolean redraw) {
        if ((getApi().style & SWT.VIRTUAL) == 0)
            return true;
        if (!((DartTableItem) item.getImpl()).cached) {
            ((DartTableItem) item.getImpl()).cached = true;
            Event event = new Event();
            event.item = item;
            event.index = index;
            currentItem = item;
            sendEvent(SWT.SetData, event);
            //widget could be disposed at this point
            currentItem = null;
            if (isDisposed() || item.isDisposed())
                return false;
            if (redraw) {
                if (!setScrollWidth(item, false)) {
                    ((DartTableItem) item.getImpl()).redraw();
                }
            }
        }
        return true;
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

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
    public void clear(int index) {
        checkWidget();
        TableItem item = _getItem(index, false);
        if (item != null) {
            if (item != currentItem)
                ((DartTableItem) item.getImpl()).clear();
            /*
		* Bug in Windows.  Despite the fact that every item in the
		* table always has LPSTR_TEXTCALLBACK, Windows caches the
		* bounds for the selected items.  This means that
		* when you change the string to be something else, Windows
		* correctly asks you for the new string but when the item
		* is selected, the selection draws using the bounds of the
		* previous item.  The fix is to reset LPSTR_TEXTCALLBACK
		* even though it has not changed, causing Windows to flush
		* cached bounds.
		*/
            if ((getApi().style & SWT.VIRTUAL) == 0 && ((DartTableItem) item.getImpl()).cached) {
                ((DartTableItem) item.getImpl()).cached = false;
            }
            setScrollWidth(item, false);
        }
    }

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
    public void clear(int start, int end) {
        checkWidget();
        if (start > end)
            return;
    }

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
    public void clear(int[] indices) {
        checkWidget();
        if (indices == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (indices.length == 0)
            return;
        for (int i = 0; i < indices.length; i++) {
        }
        boolean cleared = false;
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            TableItem item = _getItem(index, false);
            if (item != null) {
                if (item != currentItem) {
                    cleared = true;
                    ((DartTableItem) item.getImpl()).clear();
                }
                /*
			* Bug in Windows.  Despite the fact that every item in the
			* table always has LPSTR_TEXTCALLBACK, Windows caches the
			* bounds for the selected items.  This means that
			* when you change the string to be something else, Windows
			* correctly asks you for the new string but when the item
			* is selected, the selection draws using the bounds of the
			* previous item.  The fix is to reset LPSTR_TEXTCALLBACK
			* even though it has not changed, causing Windows to flush
			* cached bounds.
			*/
                if ((getApi().style & SWT.VIRTUAL) == 0 && ((DartTableItem) item.getImpl()).cached) {
                    ((DartTableItem) item.getImpl()).cached = false;
                }
            }
        }
        if (cleared)
            setScrollWidth(null, false);
    }

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
    public void clearAll() {
        checkWidget();
        boolean cleared = false;
        if (cleared) {
            setScrollWidth(null, false);
        }
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.computeSize(this, wHint, hHint, changed);
    }

    @Override
    void createHandle() {
    }

    @Override
    int applyThemeBackground() {
        /*
	 * Just inheriting the THEME_BACKGROUND doesn't turn complete Table
	 * background transparent, TableItem background remains as-is.
	 */
        return -1;
        /* No Change */
    }

    void createHeaderToolTips() {
        if (headerToolTipHandle != 0)
            return;
        if (headerToolTipHandle == 0)
            error(SWT.ERROR_NO_HANDLES);
        maybeEnableDarkSystemTheme(headerToolTipHandle);
    }

    void createItem(TableColumn column, int index) {
        if (!(0 <= index && index <= columnCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (columnCount == columns.length) {
            TableColumn[] newColumns = new TableColumn[columns.length + 4];
            System.arraycopy(columns, 0, newColumns, 0, columns.length);
            columns = newColumns;
        }
        /*
	* Insert the column into the columns array before inserting
	* it into the widget so that the column will be present when
	* any callbacks are issued as a result of LVM_INSERTCOLUMN
	* or LVM_SETCOLUMN.
	*/
        System.arraycopy(columns, index, columns, index + 1, columnCount++ - index);
        columns[index] = column;
        /*
	* Ensure that resize listeners for the table and for columns
	* within the table are not called.  This can happen when the
	* first column is inserted into a table or when a new column
	* is inserted in the first position.
	*/
        ignoreColumnResize = true;
        if (index == 0) {
            if (columnCount > 1) {
            } else {
            }
            /*
		* Bug in Windows.  Despite the fact that every item in the
		* table always has LPSTR_TEXTCALLBACK, Windows caches the
		* bounds for the selected items.  This means that
		* when you change the string to be something else, Windows
		* correctly asks you for the new string but when the item
		* is selected, the selection draws using the bounds of the
		* previous item.  The fix is to reset LPSTR_TEXTCALLBACK
		* even though it has not changed, causing Windows to flush
		* cached bounds.
		*/
            if ((getApi().style & SWT.VIRTUAL) == 0) {
            }
        } else {
        }
        ignoreColumnResize = false;
        /* Add the tool tip item for the header */
        if (headerToolTipHandle != 0) {
        }
    }

    void createItem(TableItem item, int index) {
        if (index < 0 || index > items.length) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        if (index == items.length) {
            TableItem[] newItems = new TableItem[items.length + 1];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }
        items[index] = item;
    }

    @Override
    void createWidget() {
        super.createWidget();
        itemHeight = hotIndex = -1;
        _initItems();
        columns = new TableColumn[4];
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
        if (hwndHeader != 0)
            ((SwtDisplay) display.getImpl()).removeControl(hwndHeader);
    }

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
    public void deselect(int[] indices) {
        checkWidget();
        if (indices == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (indices.length == 0)
            return;
        for (int index : indices) {
            /*
		* An index of -1 will apply the change to all
		* items.  Ensure that indices are greater than -1.
		*/
            if (index >= 0) {
                ignoreSelect = true;
                ignoreSelect = false;
            }
        }
    }

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
    public void deselect(int index) {
        checkWidget();
        /*
	* An index of -1 will apply the change to all
	* items.  Ensure that index is greater than -1.
	*/
        if (index < 0)
            return;
        ignoreSelect = true;
        ignoreSelect = false;
    }

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
    public void deselect(int start, int end) {
        checkWidget();
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
        selection = new int[0];
    }

    void destroyItem(TableColumn column) {
        int index = 0;
        while (index < columnCount) {
            if (columns[index] == column)
                break;
            index++;
        }
        int orderIndex = 0;
        int[] oldOrder = new int[columnCount];
        while (orderIndex < columnCount) {
            if (oldOrder[orderIndex] == index)
                break;
            orderIndex++;
        }
        ignoreColumnResize = true;
        boolean first = false;
        if (index == 0) {
            first = true;
            /*
		* Changing the content of a column using LVM_SETCOLUMN causes
		* the table control to send paint events. At this point the
		* partially disposed column is still part of the table and
		* paint handler can try to access it. This can cause exceptions.
		* The fix is to turn redraw off.
		*/
            setRedraw(false);
            if (columnCount > 1) {
                index = 1;
            } else {
            }
            setRedraw(true);
            /*
		* Bug in Windows.  Despite the fact that every item in the
		* table always has LPSTR_TEXTCALLBACK, Windows caches the
		* bounds for the selected items.  This means that
		* when you change the string to be something else, Windows
		* correctly asks you for the new string but when the item
		* is selected, the selection draws using the bounds of the
		* previous item.  The fix is to reset LPSTR_TEXTCALLBACK
		* even though it has not changed, causing Windows to flush
		* cached bounds.
		*/
            if ((getApi().style & SWT.VIRTUAL) == 0) {
            }
        }
        if (columnCount > 1) {
        }
        if (first)
            index = 0;
        System.arraycopy(columns, index + 1, columns, index, --columnCount - index);
        columns[columnCount] = null;
        if (columnCount == 0)
            setScrollWidth(null, true);
        updateMoveable();
        ignoreColumnResize = false;
        if (columnCount != 0) {
            /*
		* Bug in Windows.  When LVM_DELETECOLUMN is used to delete a
		* column zero when that column is both the first column in the
		* table and the first column in the column order array, Windows
		* incorrectly computes the new column order.  For example, both
		* the orders {0, 3, 1, 2} and {0, 3, 2, 1} give a new column
		* order of {0, 2, 1}, while {0, 2, 1, 3} gives {0, 1, 2, 3}.
		* The fix is to compute the new order and compare it with the
		* order that Windows is using.  If the two differ, the new order
		* is used.
		*/
            int count = 0;
            int oldIndex = oldOrder[orderIndex];
            int[] newOrder = new int[columnCount];
            for (int element : oldOrder) {
                if (element != oldIndex) {
                    int newIndex = element <= oldIndex ? element : element - 1;
                    newOrder[count++] = newIndex;
                }
            }
            int j = 0;
            while (j < newOrder.length) {
                if (oldOrder[j] != newOrder[j])
                    break;
                j++;
            }
            if (j != newOrder.length) {
            }
            TableColumn[] newColumns = new TableColumn[columnCount - orderIndex];
            for (int i = orderIndex; i < newOrder.length; i++) {
                newColumns[i - orderIndex] = columns[newOrder[i]];
                ((DartTableColumn) newColumns[i - orderIndex].getImpl()).updateToolTip(newOrder[i]);
            }
            for (TableColumn newColumn : newColumns) {
                if (!newColumn.isDisposed()) {
                    newColumn.getImpl().sendEvent(SWT.Move);
                }
            }
        }
        /* Remove the tool tip item for the header */
        if (headerToolTipHandle != 0) {
        }
    }

    void destroyItem(TableItem item) {
        setDeferResize(true);
        ignoreSelect = ignoreShrink = true;
        ignoreSelect = ignoreShrink = false;
        setDeferResize(false);
    }

    void fixCheckboxImageList(boolean fixScroll) {
        /*
	* Bug in Windows.  When the state image list is larger than the
	* image list, Windows incorrectly positions the state images.  When
	* the table is scrolled, Windows draws garbage.  The fix is to force
	* the state image list to be the same size as the image list.
	*/
        if ((getApi().style & SWT.CHECK) == 0)
            return;
        int[] cx = new int[1], cy = new int[1];
        int[] stateCx = new int[1], stateCy = new int[1];
        if (cx[0] == stateCx[0] && cy[0] == stateCy[0])
            return;
        setCheckboxImageList(cx[0], cy[0], fixScroll);
    }

    void fixCheckboxImageListColor(boolean fixScroll) {
        if ((getApi().style & SWT.CHECK) == 0)
            return;
        int[] cx = new int[1], cy = new int[1];
        setCheckboxImageList(cx[0], cy[0], fixScroll);
    }

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
    public TableColumn getColumn(int index) {
        checkWidget();
        if (!(0 <= index && index < columnCount))
            error(SWT.ERROR_INVALID_RANGE);
        return columns[index];
    }

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
     * @see Table#setColumnOrder(int[])
     * @see TableColumn#getMoveable()
     * @see TableColumn#setMoveable(boolean)
     * @see SWT#Move
     *
     * @since 3.1
     */
    public int[] getColumnOrder() {
        checkWidget();
        if (columnCount == 0)
            return new int[0];
        int[] order = new int[columnCount];
        return order;
    }

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
    public TableColumn[] getColumns() {
        checkWidget();
        TableColumn[] result = new TableColumn[columnCount];
        System.arraycopy(columns, 0, result, 0, columnCount);
        return result;
    }

    int getFocusIndex() {
        return 0;
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
     * @since 2.0
     */
    public int getHeaderHeight() {
        checkWidget();
        return DPIUtil.pixelToPoint(getHeaderHeightInPixels(), getZoom());
    }

    int getHeaderHeightInPixels() {
        if (hwndHeader == 0)
            return 0;
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
     */
    public TableItem getItem(int index) {
        checkWidget();
        return _getItem(index);
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
    public TableItem getItem(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return null;
    }

    TableItem getItemInPixels(Point point) {
        if ((getApi().style & SWT.FULL_SELECTION) == 0) {
            if (hooks(SWT.MeasureItem)) {
                return null;
            }
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
        return this.items != null ? this.items.length : 0;
    }

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
    public int getItemHeight() {
        checkWidget();
        return DPIUtil.pixelToPoint(getItemHeightInPixels(), getZoom());
    }

    int getItemHeightInPixels() {
        if (!painted && hooks(SWT.MeasureItem))
            hitTestSelection(0, 0, 0);
        return 0;
    }

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
    public TableItem[] getItems() {
        checkWidget();
        if ((getApi().style & SWT.VIRTUAL) != 0) {
        } else {
        }
        return this.items;
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
     */
    public boolean getLinesVisible() {
        checkWidget();
        return _getLinesVisible();
    }

    private boolean _getLinesVisible() {
        return false;
    }

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
    public TableItem[] getSelection() {
        checkWidget();
        {
            TableItem[] result = new TableItem[selection.length];
            for (int i = 0; i < selection.length; ++i) {
                result[i] = _getItem(selection[i]);
            }
            return result;
        }
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
        return (this.selection != null && this.selection.length > 0) ? this.selection[0] : -1;
    }

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
    public int[] getSelectionIndices() {
        checkWidget();
        return this.selection;
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
     * @see #setSortColumn(TableColumn)
     *
     * @since 3.2
     */
    public TableColumn getSortColumn() {
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
    public int getTopIndex() {
        checkWidget();
        return this.topIndex;
    }

    boolean hasChildren() {
        return false;
    }

    boolean hitTestSelection(int index, int x, int y) {
        if (!hooks(SWT.MeasureItem))
            return false;
        boolean result = false;
        return result;
    }

    int imageIndex(Image image, int column) {
        if (column == 0) {
            firstColumnImage = true;
        } else {
            setSubImagesVisible(true);
        }
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
     */
    public int indexOf(TableColumn column) {
        checkWidget();
        if (column == null)
            error(SWT.ERROR_NULL_ARGUMENT);
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
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int indexOf(TableItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        int count = items.length;
        if (1 <= lastIndexOf && lastIndexOf < count - 1) {
            if (items[lastIndexOf] == item)
                return lastIndexOf;
            if (items[lastIndexOf + 1] == item)
                return ++lastIndexOf;
            if (items[lastIndexOf - 1] == item)
                return --lastIndexOf;
        }
        if (lastIndexOf < count / 2) {
            for (int i = 0; i < count; i++) {
                if (items[i] == item)
                    return lastIndexOf = i;
            }
        } else {
            for (int i = count - 1; i >= 0; --i) {
                if (items[i] == item)
                    return lastIndexOf = i;
            }
        }
        return -1;
    }

    boolean isCustomToolTip() {
        return hooks(SWT.MeasureItem);
    }

    boolean isOptimizedRedraw() {
        if ((getApi().style & SWT.H_SCROLL) == 0 || (getApi().style & SWT.V_SCROLL) == 0)
            return false;
        return !hasChildren() && !hooks(SWT.Paint) && !filters(SWT.Paint) && !customHeaderDrawing();
    }

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
    public boolean isSelected(int index) {
        checkWidget();
        return false;
    }

    @Override
    boolean isUseWsBorder() {
        return super.isUseWsBorder() || ((display != null) && ((SwtDisplay) display.getImpl()).useWsBorderTable);
    }

    @Override
    void register() {
        super.register();
        if (hwndHeader != 0)
            ((SwtDisplay) display.getImpl()).addControl(hwndHeader, this.getApi());
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (_hasItems()) {
            _clearItems();
        }
        if (columns != null) {
            for (int i = 0; i < columnCount; i++) {
                TableColumn column = columns[i];
                if (!column.isDisposed())
                    column.getImpl().release(false);
            }
            columns = null;
        }
        super.releaseChildren(destroy);
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        customDraw = false;
        currentItem = null;
        headerToolTipHandle = 0;
    }

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
    public void remove(int[] indices) {
        checkWidget();
        if (indices == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (indices.length == 0)
            return;
        int[] newIndices = new int[indices.length];
        System.arraycopy(indices, 0, newIndices, 0, indices.length);
        sort(newIndices);
        setDeferResize(true);
        int last = -1;
        for (int index : newIndices) {
            if (index != last) {
                TableItem item = _getItem(index, false);
                if (item != null && !item.isDisposed())
                    item.getImpl().release(false);
                ignoreSelect = ignoreShrink = true;
                ignoreSelect = ignoreShrink = false;
                last = index;
            }
        }
        setDeferResize(false);
    }

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
    public void remove(int index) {
        checkWidget();
        TableItem item = _getItem(index, false);
        if (item != null && !item.isDisposed())
            item.getImpl().release(false);
        setDeferResize(true);
        ignoreSelect = ignoreShrink = true;
        ignoreSelect = ignoreShrink = false;
        setDeferResize(false);
    }

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
    public void remove(int start, int end) {
        checkWidget();
        if (start > end)
            return;
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
        setDeferResize(true);
        ignoreSelect = ignoreShrink = true;
        ignoreSelect = ignoreShrink = false;
        setTableEmpty();
        setDeferResize(false);
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
     * @see #addSelectionListener(SelectionListener)
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
    public void select(int[] indices) {
        int[] newValue = indices;
        if (!java.util.Objects.equals(this.selection, newValue)) {
            dirty();
        }
        checkWidget();
        if (indices == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        int length = indices.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))
            return;
        this.selection = newValue;
        for (int i = length - 1; i >= 0; --i) {
            /*
		* An index of -1 will apply the change to all
		* items.  Ensure that indices are greater than -1.
		*/
            if (indices[i] >= 0) {
                ignoreSelect = true;
                ignoreSelect = false;
            }
        }
    }

    @Override
    void reskinChildren(int flags) {
        if (_hasItems()) {
        }
        if (columns != null) {
            for (int i = 0; i < columnCount; i++) {
                TableColumn column = columns[i];
                if (!column.isDisposed())
                    column.reskin(flags);
            }
        }
        super.reskinChildren(flags);
    }

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
    public void select(int index) {
        dirty();
        int[] newValue = new int[] { index };
        checkWidget();
        /*
	* An index of -1 will apply the change to all
	* items.  Ensure that index is greater than -1.
	*/
        if (index < 0)
            return;
        ignoreSelect = true;
        this.selection = newValue;
        ignoreSelect = false;
    }

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
    public void select(int start, int end) {
        dirty();
        int[] newValue = new int[] { start };
        checkWidget();
        if (end < 0 || start > end || ((getApi().style & SWT.SINGLE) != 0 && start != end))
            return;
        start = Math.max(0, start);
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
        ignoreSelect = true;
        ignoreSelect = false;
    }

    Event sendMeasureItemEvent(TableItem item, int row, int column, long hDC) {
        GCData data = new GCData();
        data.device = display;
        data.font = item.getFont(column);
        GC gc = createNewGC(hDC, data);
        Event event = new Event();
        event.item = item;
        event.gc = gc;
        event.index = column;
        boolean drawSelected = false;
        if (drawSelected)
            event.detail |= SWT.SELECTED;
        sendEvent(SWT.MeasureItem, event);
        event.gc = null;
        gc.dispose();
        if (!isDisposed() && !item.isDisposed()) {
            if (columnCount == 0) {
            }
        }
        return event;
    }

    @Override
    void setBackgroundImage(long hBitmap) {
        super.setBackgroundImage(hBitmap);
        if (hBitmap != 0) {
            setBackgroundTransparent(true);
        } else {
            if (!hooks(SWT.MeasureItem) && !hooks(SWT.EraseItem) && !hooks(SWT.PaintItem)) {
                setBackgroundTransparent(false);
            }
        }
    }

    @Override
    void setBackgroundPixel(int newPixel) {
    }

    void setBackgroundTransparent(boolean transparent) {
        if (transparent) {
        } else {
        }
    }

    @Override
    void setBoundsInPixels(int x, int y, int width, int height, int flags, boolean defer) {
        /*
	* Bug in Windows.  If the table column widths are adjusted
	* in WM_SIZE or WM_POSITIONCHANGED using LVM_SETCOLUMNWIDTH
	* blank lines may be inserted at the top of the table.  A
	* call to LVM_GETTOPINDEX will return a negative number (this
	* is an impossible result).  Once the blank lines appear,
	* there seems to be no way to get rid of them, other than
	* destroying and recreating the table.  The fix is to send
	* the resize notification after the size has been changed in
	* the operating system.
	*
	* NOTE:  This does not fix the case when the user is resizing
	* columns dynamically.  There is no fix for this case at this
	* time.
	*/
        setDeferResize(true);
        super.setBoundsInPixels(x, y, width, height, flags, false);
        setDeferResize(false);
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
     * @see Table#getColumnOrder()
     * @see TableColumn#getMoveable()
     * @see TableColumn#setMoveable(boolean)
     * @see SWT#Move
     *
     * @since 3.1
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
        int[] oldOrder = new int[columnCount];
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
            TableColumn[] newColumns = new TableColumn[columnCount];
            System.arraycopy(columns, 0, newColumns, 0, columnCount);
            for (int i = 0; i < columnCount; i++) {
                TableColumn column = newColumns[i];
                if (!column.isDisposed()) {
                }
            }
        }
    }

    void setCustomDraw(boolean customDraw) {
        if (this.customDraw == customDraw)
            return;
        if (!this.customDraw && customDraw && currentItem != null) {
        }
        this.customDraw = customDraw;
    }

    void setDeferResize(boolean defer) {
        if (defer) {
            if (resizeCount++ == 0) {
                wasResized = false;
                /*
			* Feature in Windows.  When LVM_SETBKCOLOR is used with CLR_NONE
			* to make the background of the table transparent, drawing becomes
			* slow.  The fix is to temporarily clear CLR_NONE when redraw is
			* turned off.
			*/
                if (hooks(SWT.MeasureItem) || hooks(SWT.EraseItem) || hooks(SWT.PaintItem)) {
                }
            }
        } else {
            if (--resizeCount == 0) {
                if (hooks(SWT.MeasureItem) || hooks(SWT.EraseItem) || hooks(SWT.PaintItem)) {
                    if (--drawCount == 0) /*&& OS.IsWindowVisible (handle)*/
                    {
                    }
                }
                if (wasResized) {
                    wasResized = false;
                    setResizeChildren(false);
                    sendEvent(SWT.Resize);
                    if (isDisposed())
                        return;
                    if (layout != null) {
                        markLayout(false, false);
                        updateLayout(false, false);
                    }
                    setResizeChildren(true);
                }
            }
        }
    }

    void setCheckboxImageList(int width, int height, boolean fixScroll) {
        if ((getApi().style & SWT.CHECK) == 0)
            return;
        /*
	* Bug in Windows.  Making any change to an item that
	* changes the item height of a table while the table
	* is scrolled can cause the lines to draw incorrectly.
	* This happens even when the lines are not currently
	* visible and are shown afterwards.  The fix is to
	* save the top index, scroll to the top of the table
	* and then restore the original top index.
	*/
        int topIndex = getTopIndex();
        if (fixScroll && topIndex != 0) {
            setRedraw(false);
            setTopIndex(0);
        }
        if (fixScroll && topIndex != 0) {
            setTopIndex(topIndex);
            setRedraw(true);
        }
    }

    void setFocusIndex(int index) {
        //	checkWidget ();
        /*
	* An index of -1 will apply the change to all
	* items.  Ensure that index is greater than -1.
	*/
        if (index < 0)
            return;
        ignoreSelect = true;
        ignoreSelect = false;
    }

    @Override
    public void setFont(Font font) {
        dirty();
        checkWidget();
        /*
	* Bug in Windows.  Making any change to an item that
	* changes the item height of a table while the table
	* is scrolled can cause the lines to draw incorrectly.
	* This happens even when the lines are not currently
	* visible and are shown afterwards.  The fix is to
	* save the top index, scroll to the top of the table
	* and then restore the original top index.
	*/
        int topIndex = getTopIndex();
        if (topIndex != 0) {
            setRedraw(false);
            setTopIndex(0);
        }
        if (itemHeight != -1) {
        }
        super.setFont(font);
        if (itemHeight != -1) {
        }
        setScrollWidth(null, true);
        if (topIndex != 0) {
            setTopIndex(topIndex);
            setRedraw(true);
        }
    }

    @Override
    void setForegroundPixel(int pixel) {
    }

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
     */
    public void setHeaderVisible(boolean show) {
        boolean newValue = show;
        if (!java.util.Objects.equals(this.headerVisible, newValue)) {
            dirty();
        }
        checkWidget();
        /*
	* Feature in Windows.  Setting or clearing LVS_NOCOLUMNHEADER
	* causes the table to scroll to the beginning.  The fix is to
	* save and restore the top index causing the table to scroll
	* to the new location.
	*/
        int oldIndex = getTopIndex();
        /*
	* Bug in Windows.  Making any change to an item that
	* changes the item height of a table while the table
	* is scrolled can cause the lines to draw incorrectly.
	* This happens even when the lines are not currently
	* visible and are shown afterwards.  The fix is to
	* save the top index, scroll to the top of the table
	* and then restore the original top index.
	*/
        int newIndex = getTopIndex();
        if (newIndex != 0) {
            setRedraw(false);
            setTopIndex(0);
        }
        setTopIndex(oldIndex);
        if (newIndex != 0) {
            setRedraw(true);
        }
        this.headerVisible = newValue;
        updateHeaderToolTips();
    }

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
    public void setItemCount(int count) {
        checkWidget();
        count = Math.max(0, count);
        setDeferResize(true);
        boolean isVirtual = (getApi().style & SWT.VIRTUAL) != 0;
        if (!isVirtual)
            setRedraw(false);
        if (isVirtual) {
        } else {
        }
        if (!isVirtual)
            setRedraw(true);
        setDeferResize(false);
    }

    void setItemHeight(boolean fixScroll) {
        /*
	* Bug in Windows.  Making any change to an item that
	* changes the item height of a table while the table
	* is scrolled can cause the lines to draw incorrectly.
	* This happens even when the lines are not currently
	* visible and are shown afterwards.  The fix is to
	* save the top index, scroll to the top of the table
	* and then restore the original top index.
	*/
        int topIndex = getTopIndex();
        if (fixScroll && topIndex != 0) {
            setRedraw(false);
            setTopIndex(0);
        }
        if (itemHeight == -1) {
        } else {
            /*
		* Feature in Windows.  Window has no API to set the item
		* height for a table.  The fix is to set temporarily set
		* LVS_OWNERDRAWFIXED then resize the table, causing a
		* WM_MEASUREITEM to be sent, then clear LVS_OWNERDRAWFIXED.
		*/
            forceResize();
            ignoreResize = true;
            ignoreResize = false;
        }
        if (fixScroll && topIndex != 0) {
            setTopIndex(topIndex);
            setRedraw(true);
        }
    }

    /**
     * Sets the height of the area which would be used to
     * display <em>one</em> of the items in the table.
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
        this.itemHeight = itemHeight;
        setItemHeight(true);
        setScrollWidth(null, true);
    }

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
    public void setLinesVisible(boolean show) {
        boolean newValue = show;
        if (!java.util.Objects.equals(this.linesVisible, newValue)) {
            dirty();
        }
        checkWidget();
        this.linesVisible = newValue;
    }

    @Override
    public void setRedraw(boolean redraw) {
        dirty();
        checkWidget();
        /*
	 * Feature in Windows.  When WM_SETREDRAW is used to turn
	 * off drawing in a widget, it clears the WS_VISIBLE bits
	 * and then sets them when redraw is turned back on.  This
	 * means that WM_SETREDRAW will make a widget unexpectedly
	 * visible.  The fix is to track the visibility state while
	 * drawing is turned off and restore it when drawing is turned
	 * back on.
	 */
        if (drawCount == 0) {
        }
        if (redraw) {
            if (--drawCount == 0) {
                /*
			* When many items are added to a table, it is faster to
			* temporarily unsubclass the window proc so that messages
			* are dispatched directly to the table.
			*
			* NOTE: This is optimization somewhat dangerous because any
			* operation can occur when redraw is turned off, even operations
			* where the table must be subclassed in order to have the correct
			* behavior or work around a Windows bug.
			*
			* This code is intentionally commented.
			*/
                //			subclass ();
                /* Set the width of the horizontal scroll bar */
                setScrollWidth(null, true);
                /*
			* Bug in Windows.  For some reason, when WM_SETREDRAW is used
			* to turn redraw back on this may result in a WM_SIZE.  If the
			* table column widths are adjusted in WM_SIZE, blank lines may
			* be inserted at the top of the widget.  A call to LVM_GETTOPINDEX
			* will return a negative number (this is an impossible result).
			* The fix is to send the resize notification after the size has
			* been changed in the operating system.
			*/
                setDeferResize(true);
                if ((getApi().state & HIDDEN) != 0) {
                    getApi().state &= ~HIDDEN;
                } else {
                }
                setDeferResize(false);
            }
        } else {
            if (drawCount++ == 0) {
                /*
			* When many items are added to a table, it is faster to
			* temporarily unsubclass the window proc so that messages
			* are dispatched directly to the table.
			*
			* NOTE: This is optimization somewhat dangerous because any
			* operation can occur when redraw is turned off, even operations
			* where the table must be subclassed in order to have the correct
			* behavior or work around a Windows bug.
			*
			* This code is intentionally commented.
			*/
                //			unsubclass ();
            }
        }
    }

    void setScrollWidth(int width) {
    }

    boolean setScrollWidth(TableItem item, boolean force) {
        if (currentItem != null) {
            if (currentItem != item)
                fixScrollWidth = true;
            return false;
        }
        fixScrollWidth = false;
        /*
	* NOTE: It is much faster to measure the strings and compute the
	* width of the scroll bar in non-virtual table rather than using
	* LVM_SETCOLUMNWIDTH with LVSCW_AUTOSIZE.
	*/
        if (columnCount == 0) {
            int newWidth = 0, imageIndent = 0;
            /*
		* Bug in Windows.  When the width of the first column is
		* small but not zero, Windows draws '...' outside of the
		* bounds of the text.  This is strange, but only causes
		* problems when the item is selected.  In this case, Windows
		* clears the '...' but doesn't redraw it when the item is
		* deselected, causing pixel corruption.  The fix is to ensure
		* that the column is at least wide enough to draw a single
		* space.
		*/
            if (newWidth == 0) {
            }
            {
                /*
			* Bug in Windows.  When LVM_SETIMAGELIST is used to remove the
			* image list by setting it to NULL, the item width and height
			* is not changed and space is reserved for icons despite the
			* fact that there are none.  The fix is to set the image list
			* to be very small before setting it to NULL.  This causes
			* Windows to reserve the smallest possible space when an image
			* list is removed.  In this case, the scroll width must be one
			* pixel larger.
			*/
                newWidth++;
            }
            newWidth += INSET * 2 + VISTA_EXTRA;
        }
        return false;
    }

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
    public void setSelection(int[] indices) {
        int[] newValue = indices;
        if (!java.util.Objects.equals(this.selection, newValue)) {
            dirty();
        }
        checkWidget();
        if (indices == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        deselectAll();
        int length = indices.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))
            return;
        select(indices);
        int focusIndex = indices[0];
        if (focusIndex != -1)
            setFocusIndex(focusIndex);
        this.selection = newValue;
        showSelection();
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
    public void setSelection(TableItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setSelection(new TableItem[] { item });
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
     * @see Table#deselectAll()
     * @see Table#select(int[])
     * @see Table#setSelection(int[])
     */
    public void setSelection(TableItem[] items) {
        checkWidget();
        if (items == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        deselectAll();
        int length = items.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))
            return;
        int focusIndex = -1;
        for (int i = length - 1; i >= 0; --i) {
            int index = indexOf(items[i]);
            if (index != -1) {
                select(focusIndex = index);
            }
        }
        if (focusIndex != -1)
            setFocusIndex(focusIndex);
        showSelection();
    }

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
    public void setSelection(int index) {
        checkWidget();
        deselectAll();
        select(index);
        if (index != -1)
            setFocusIndex(index);
        showSelection();
    }

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
    public void setSelection(int start, int end) {
        checkWidget();
        deselectAll();
        if (end < 0 || start > end || ((getApi().style & SWT.SINGLE) != 0 && start != end))
            return;
        start = Math.max(0, start);
        select(start, end);
        setFocusIndex(start);
        showSelection();
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
    public void setSortColumn(TableColumn column) {
        checkWidget();
        if (!java.util.Objects.equals(this.sortColumn, column)) {
            dirty();
        }
        if (column != null && column.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (sortColumn != null && !sortColumn.isDisposed()) {
            ((DartTableColumn) sortColumn.getImpl()).setSortDirection(SWT.NONE);
        }
        sortColumn = column;
        if (sortColumn != null && sortDirection != SWT.NONE) {
            ((DartTableColumn) sortColumn.getImpl()).setSortDirection(sortDirection);
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
            ((DartTableColumn) sortColumn.getImpl()).setSortDirection(direction);
        }
    }

    void setSubImagesVisible(boolean visible) {
    }

    void setTableEmpty() {
        if (!hooks(SWT.MeasureItem) && !hooks(SWT.EraseItem) && !hooks(SWT.PaintItem)) {
            Control control = findBackgroundControl();
            if (control == null)
                control = this.getApi();
            if (control.getImpl()._backgroundImage() == null) {
                setCustomDraw(false);
                setBackgroundTransparent(false);
            }
        }
        _initItems();
        if (columnCount == 0) {
            setScrollWidth(null, false);
        }
    }

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
    public void setTopIndex(int index) {
        int newValue = index;
        if (!java.util.Objects.equals(this.topIndex, newValue)) {
            dirty();
        }
        checkWidget();
        if (!painted && hooks(SWT.MeasureItem))
            hitTestSelection(index, 0, 0);
        /*
	* Bug in Windows.  For some reason, LVM_SCROLL refuses to
	* scroll a table vertically when the width and height of
	* the table is smaller than a certain size.  The values
	* that seem to cause the problem are width=68 and height=6
	* but there is no guarantee that these values cause the
	* failure on different machines or on different versions
	* of Windows.  It may depend on the font and any number
	* of other factors.  For example, setting the font to
	* anything but the default sometimes fixes the problem.
	* The fix is to use LVM_GETCOUNTPERPAGE to detect the
	* case when the number of visible items is zero and
	* use LVM_ENSUREVISIBLE to scroll the table to make the
	* index visible.
	*/
        ignoreCustomDraw = true;
        ignoreCustomDraw = false;
        this.topIndex = newValue;
    }

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
    public void showColumn(TableColumn column) {
        checkWidget();
        if (column == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (column.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (((DartTableColumn) column.getImpl()).parent != this.getApi())
            return;
        int index = indexOf(column);
        if (!(0 <= index && index < columnCount))
            return;
        if (index == 0) {
            ignoreCustomDraw = true;
            ignoreCustomDraw = false;
        } else {
            ignoreCustomDraw = true;
            ignoreCustomDraw = false;
        }
        if (_getLinesVisible()) {
        }
        /*
	* Bug in Windows.  When a table that is drawing grid lines
	* is slowly scrolled horizontally to the left, the table does
	* not redraw the newly exposed vertical grid lines.  The fix
	* is to save the old scroll position, call the window proc,
	* get the new scroll position and redraw the new area.
	*/
        if (_getLinesVisible()) {
        }
    }

    void showItem(int index) {
        if (!painted && hooks(SWT.MeasureItem))
            hitTestSelection(index, 0, 0);
    }

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
    public void showItem(TableItem item) {
        checkWidget();
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        int index = indexOf(item);
        if (index != -1)
            showItem(index);
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
     * @see Table#showItem(TableItem)
     */
    public void showSelection() {
        checkWidget();
    }

    /*public*/
    void sort() {
        checkWidget();
        //	if ((style & SWT.VIRTUAL) != 0) return;
        //	int itemCount = OS.SendMessage (handle, OS.LVM_GETITEMCOUNT, 0, 0);
        //	if (itemCount == 0 || itemCount == 1) return;
        //	Comparator comparator = new Comparator () {
        //		int index = sortColumn == null ? 0 : indexOf (sortColumn);
        //		public int compare (Object object1, Object object2) {
        //			TableItem item1 = (TableItem) object1, item2 = (TableItem) object2;
        //			if (sortDirection == SWT.UP || sortDirection == SWT.NONE) {
        //				return item1.getText (index).compareTo (item2.getText (index));
        //			} else {
        //				return item2.getText (index).compareTo (item1.getText (index));
        //			}
        //		}
        //	};
        //	Arrays.sort (items, 0, itemCount, comparator);
        //	redraw ();
    }

    @Override
    void subclass() {
        super.subclass();
        if (HeaderProc != 0) {
        }
    }

    @Override
    void unsubclass() {
        super.unsubclass();
        if (HeaderProc != 0) {
        }
    }

    @Override
    void update(boolean all) {
        //	checkWidget ();
        boolean fixSubclass = isOptimizedRedraw();
        if (fixSubclass) {
        }
        super.update(all);
        if (fixSubclass) {
        }
    }

    void updateHeaderToolTips() {
        if (headerToolTipHandle == 0)
            return;
        for (int i = 0; i < columnCount; i++) {
        }
    }

    @Override
    void updateMenuLocation(Event event) {
        Rectangle clientArea = getClientAreaInPixels();
        int x = clientArea.x, y = clientArea.y;
        int focusIndex = getFocusIndex();
        if (focusIndex != -1) {
            TableItem focusItem = getItem(focusIndex);
            Rectangle bounds = ((DartTableItem) focusItem.getImpl()).getBoundsInPixels(0);
            if (((DartItem) focusItem.getImpl()).text != null && ((DartItem) focusItem.getImpl()).text.length() != 0) {
                bounds = ((DartTableItem) focusItem.getImpl()).getBoundsInPixels();
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

    void updateMoveable() {
        int index = 0;
        while (index < columnCount) {
            if (((DartTableColumn) columns[index].getImpl()).moveable)
                break;
            index++;
        }
    }

    @Override
    void updateOrientation() {
        super.updateOrientation();
        if ((getApi().style & SWT.CHECK) != 0)
            fixCheckboxImageListColor(false);
    }

    @Override
    boolean updateTextDirection(int textDirection) {
        if (super.updateTextDirection(textDirection)) {
            if (textDirection == AUTO_TEXT_DIRECTION || (getApi().state & HAS_AUTO_DIRECTION) != 0) {
                for (TableItem item : items) {
                    if (item != null) {
                        ((DartItem) item.getImpl()).updateTextDirection(textDirection == AUTO_TEXT_DIRECTION ? AUTO_TEXT_DIRECTION : getApi().style & SWT.FLIP_TEXT_DIRECTION);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    int widgetStyle() {
        /*
	* This code is intentionally commented.  In the future,
	* the FLAT bit may be used to make the header flat and
	* unresponsive to mouse clicks.
	*/
        return 0;
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof Table table)) {
            return;
        }
        ((DartTable) table.getImpl()).settingItemHeight = true;
        var scrollWidth = 0;
        // Request ScrollWidth
        if (table.getColumns().length == 0) {
        }
        // if the item height was set at least once programmatically with CDDS_SUBITEMPREPAINT,
        // the item height of the table is not managed by the OS anymore e.g. when the zoom
        // on the monitor is changed, the height of the item will stay at the fixed size.
        // Resetting it will re-enable the default behavior again
        ((DartTable) table.getImpl()).setItemHeight(-1);
        if (table.getColumns().length == 0 && scrollWidth != 0) {
            // Update scrollbar width if no columns are available
            ((DartTable) table.getImpl()).setScrollWidth(scrollWidth);
        }
        ((DartTable) table.getImpl()).fixCheckboxImageListColor(true);
        ((DartTable) table.getImpl()).settingItemHeight = false;
    }

    int[] columnOrder = new int[0];

    boolean editable = false;

    Color _headerBackground;

    Color _headerForeground;

    boolean headerVisible;

    boolean linesVisible;

    int[] selection = new int[0];

    int topIndex;

    public TableItem[] _items() {
        return items;
    }

    public TableColumn[] _columns() {
        return columns;
    }

    public int _columnCount() {
        return columnCount;
    }

    public TableItem _currentItem() {
        return currentItem;
    }

    public TableColumn _sortColumn() {
        return sortColumn;
    }

    public boolean[] _columnVisible() {
        return columnVisible;
    }

    public long _headerToolTipHandle() {
        return headerToolTipHandle;
    }

    public long _hwndHeader() {
        return hwndHeader;
    }

    public long _itemToolTipHandle() {
        return itemToolTipHandle;
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

    public boolean _customDraw() {
        return customDraw;
    }

    public boolean _dragStarted() {
        return dragStarted;
    }

    public boolean _explorerTheme() {
        return explorerTheme;
    }

    public boolean _firstColumnImage() {
        return firstColumnImage;
    }

    public boolean _fixScrollWidth() {
        return fixScrollWidth;
    }

    public boolean _tipRequested() {
        return tipRequested;
    }

    public boolean _wasSelected() {
        return wasSelected;
    }

    public boolean _wasResized() {
        return wasResized;
    }

    public boolean _painted() {
        return painted;
    }

    public boolean _ignoreActivate() {
        return ignoreActivate;
    }

    public boolean _ignoreSelect() {
        return ignoreSelect;
    }

    public boolean _ignoreShrink() {
        return ignoreShrink;
    }

    public boolean _ignoreResize() {
        return ignoreResize;
    }

    public boolean _ignoreColumnMove() {
        return ignoreColumnMove;
    }

    public boolean _ignoreColumnResize() {
        return ignoreColumnResize;
    }

    public boolean _fullRowSelect() {
        return fullRowSelect;
    }

    public boolean _settingItemHeight() {
        return settingItemHeight;
    }

    public boolean _headerItemDragging() {
        return headerItemDragging;
    }

    public int _itemHeight() {
        return itemHeight;
    }

    public int _lastIndexOf() {
        return lastIndexOf;
    }

    public int _lastWidth() {
        return lastWidth;
    }

    public int _sortDirection() {
        return sortDirection;
    }

    public int _resizeCount() {
        return resizeCount;
    }

    public int _selectionForeground() {
        return selectionForeground;
    }

    public int _hotIndex() {
        return hotIndex;
    }

    public int _headerBackground() {
        return headerBackground;
    }

    public int _headerForeground() {
        return headerForeground;
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

    public boolean _linesVisible() {
        return linesVisible;
    }

    public int[] _selection() {
        return selection;
    }

    public int _topIndex() {
        return topIndex;
    }

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
                TableHelper.handleModify(this, e);
            });
        });
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                TableHelper.sendSelection(this, e, SWT.DefaultSelection);
            });
        });
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                TableHelper.sendSelection(this, e, SWT.Selection);
            });
        });
    }

    public Table getApi() {
        if (api == null)
            api = Table.createApi(this);
        return (Table) api;
    }

    public VTable getValue() {
        if (value == null)
            value = new VTable(this);
        return (VTable) value;
    }
}
