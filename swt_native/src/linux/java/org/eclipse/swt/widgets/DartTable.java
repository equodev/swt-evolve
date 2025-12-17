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
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 509648
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cairo.*;
import java.util.Objects;
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

    long modelHandle, checkRenderer;

    int itemCount, columnCount, lastIndexOf, sortDirection;

    int selectionCountOnPress, selectionCountOnRelease;

    long ignoreCell;

    TableItem[] items;

    TableColumn[] columns;

    TableItem currentItem;

    TableColumn sortColumn;

    boolean firstCustomDraw;

    /**
     * True iff computeSize has never been called on this Table
     */
    boolean firstCompute = true;

    int drawState, drawFlags;

    Color headerBackground, headerForeground;

    boolean ownerDraw, ignoreSize, pixbufSizeSet, hasChildren;

    int maxWidth = 0;

    int topIndex;

    double cachedAdjustment, currentAdjustment;

    int pixbufHeight, pixbufWidth;

    int headerHeight;

    boolean boundsChangedSinceLastDraw, headerVisible, wasScrolled;

    boolean rowActivated;

    private long headerCSSProvider;

    static final int CHECKED_COLUMN = 0;

    static final int GRAYED_COLUMN = 1;

    static final int FOREGROUND_COLUMN = 2;

    static final int BACKGROUND_COLUMN = 3;

    static final int FONT_COLUMN = 4;

    static final int FIRST_COLUMN = FONT_COLUMN + 1;

    static final int CELL_PIXBUF = 0;

    static final int CELL_TEXT = 1;

    static final int CELL_FOREGROUND = 2;

    static final int CELL_BACKGROUND = 3;

    static final int CELL_FONT = 4;

    static final int CELL_SURFACE = 5;

    static final int CELL_TYPES = CELL_SURFACE + 1;

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
        if (!ownerDraw) {
            switch(eventType) {
                case SWT.MeasureItem:
                case SWT.EraseItem:
                case SWT.PaintItem:
                    ownerDraw = true;
                    recreateRenderers();
                    break;
            }
        }
    }

    TableItem _getItem(int index) {
        if ((getApi().style & SWT.VIRTUAL) == 0)
            return items[index];
        if (items[index] != null)
            return items[index];
        return items[index] = new TableItem(this.getApi(), SWT.NONE, index, false);
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
        /* GTK is always FULL_SELECTION */
        style |= SWT.FULL_SELECTION;
        return checkBits(style, SWT.SINGLE, SWT.MULTI, 0, 0, 0, 0);
    }

    @Override
    long cellDataProc(long tree_column, long cell, long tree_model, long iter, long data) {
        if (cell == ignoreCell)
            return 0;
        int[] index = new int[1];
        TableItem item = _getItem(index[0]);
        int modelIndex = -1;
        boolean customDraw = false;
        if (columnCount == 0) {
            modelIndex = DartTable.FIRST_COLUMN;
            customDraw = firstCustomDraw;
        } else {
            TableColumn column = (TableColumn) ((SwtDisplay) display.getImpl()).getWidget(tree_column);
            if (column != null) {
                modelIndex = ((DartTableColumn) column.getImpl()).modelIndex;
                customDraw = ((DartTableColumn) column.getImpl()).customDraw;
            }
        }
        if (modelIndex == -1)
            return 0;
        boolean setData = false;
        if ((getApi().style & SWT.VIRTUAL) != 0) {
            if (!((DartTableItem) item.getImpl()).cached) {
                lastIndexOf = index[0];
                setData = checkData(item);
            }
        }
        long[] ptr = new long[1];
        if (setData) {
            ptr[0] = 0;
        }
        if (customDraw) {
            if (!ownerDraw) {
                ptr[0] = 0;
                if (ptr[0] != 0) {
                }
            }
        }
        if (setData) {
            ignoreCell = cell;
            setScrollWidth(tree_column, item);
            ignoreCell = 0;
        }
        return 0;
    }

    boolean checkData(TableItem item) {
        if (((DartTableItem) item.getImpl()).cached)
            return true;
        if ((getApi().style & SWT.VIRTUAL) != 0) {
            ((DartTableItem) item.getImpl()).cached = true;
            Event event = new Event();
            event.item = item;
            event.index = indexOf(item);
            currentItem = item;
            ((DartTableItem) item.getImpl()).settingData = true;
            sendEvent(SWT.SetData, event);
            ((DartTableItem) item.getImpl()).settingData = false;
            //widget could be disposed at this point
            currentItem = null;
            if (isDisposed())
                return false;
            if (item.isDisposed())
                return false;
        }
        return true;
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

    int calculateWidth(long column, long iter) {
        /*
	* Bug in GTK.  The width calculated by gtk_tree_view_column_cell_get_size()
	* always grows in size regardless of the text or images in the table.
	* The fix is to determine the column width from the cell renderers.
	*/
        //This workaround is causing the problem Bug 459834 in GTK3. So reverting the workaround for GTK3
        int[] width = new int[1];
        int[] xpad = new int[1];
        return width[0] + xpad[0] * 2;
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
        if (!(0 <= index && index < itemCount)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        TableItem item = items[index];
        if (item != null)
            ((DartTableItem) item.getImpl()).clear();
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
        if (!(0 <= start && start <= end && end < itemCount)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        if (start == 0 && end == itemCount - 1) {
            clearAll();
        } else {
            for (int i = start; i <= end; i++) {
                TableItem item = items[i];
                if (item != null)
                    ((DartTableItem) item.getImpl()).clear();
            }
        }
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
            if (!(0 <= indices[i] && indices[i] < itemCount)) {
                error(SWT.ERROR_INVALID_RANGE);
            }
        }
        for (int i = 0; i < indices.length; i++) {
            TableItem item = items[indices[i]];
            if (item != null)
                ((DartTableItem) item.getImpl()).clear();
        }
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
        for (int i = 0; i < itemCount; i++) {
            TableItem item = items[i];
            if (item != null)
                ((DartTableItem) item.getImpl()).clear();
        }
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    void copyModel(long oldModel, int oldStart, long newModel, int newStart, int modelLength) {
        for (int i = 0; i < itemCount; i++) {
            TableItem item = items[i];
            if (item == null) {
                continue;
            }
            // Copy header fields
            for (int iColumn = 0; iColumn < FIRST_COLUMN; iColumn++) {
            }
            // Copy requested columns
            for (int iOffset = 0; iOffset < modelLength - FIRST_COLUMN; iOffset++) {
            }
        }
    }

    void createColumn(TableColumn column, int index) {
        int modelIndex = FIRST_COLUMN;
        if (columnCount != 0) {
            for (int i = 0; i < columnCount; i++) {
                for (int j = 0; j < CELL_TYPES; j++) {
                }
            }
        }
        if (index == 0 && columnCount > 0) {
            TableColumn checkColumn = columns[0];
            createRenderers(checkColumn.handle, ((DartTableColumn) checkColumn.getImpl()).modelIndex, false, checkColumn.style);
        }
        if ((getApi().style & SWT.VIRTUAL) == 0 && columnCount == 0) {
        } else {
        }
        if (column != null) {
            ((DartTableColumn) column.getImpl()).modelIndex = modelIndex;
        }
        if (!searchEnabled()) {
        } else {
        }
    }

    @Override
    void createHandle(int index) {
    }

    void createItem(TableColumn column, int index) {
        if (!(0 <= index && index <= columnCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (columnCount == 0) {
            ((DartTableColumn) column.getImpl()).modelIndex = FIRST_COLUMN;
            createRenderers(column.handle, ((DartTableColumn) column.getImpl()).modelIndex, true, column.style);
            ((DartTableColumn) column.getImpl()).customDraw = firstCustomDraw;
            firstCustomDraw = false;
        } else {
            createColumn(column, index);
        }
        if (columnCount == columns.length) {
            TableColumn[] newColumns = new TableColumn[columns.length + 4];
            System.arraycopy(columns, 0, newColumns, 0, columns.length);
            columns = newColumns;
        }
        System.arraycopy(columns, index, columns, index + 1, columnCount++ - index);
        columns[index] = column;
        if ((getApi().state & FONT) != 0) {
            long fontDesc = getFontDescription();
            ((DartTableColumn) column.getImpl()).setFontDescription(fontDesc);
        }
        if (columnCount >= 1) {
            for (int i = 0; i < itemCount; i++) {
                TableItem item = items[i];
                if (item != null) {
                    // Bug 545139: For consistency, do not wipe out content of first TableColumn created after TableItem
                    boolean doNotModify;
                    Font[] cellFont = ((DartTableItem) item.getImpl()).cellFont;
                    doNotModify = columnCount == 1 && cellFont != null && cellFont.length == columnCount;
                    if (cellFont != null && !doNotModify) {
                        Font[] temp = new Font[columnCount];
                        System.arraycopy(cellFont, 0, temp, 0, index);
                        System.arraycopy(cellFont, index, temp, index + 1, columnCount - index - 1);
                        ((DartTableItem) item.getImpl()).cellFont = temp;
                    }
                    String[] strings = ((DartTableItem) item.getImpl()).strings;
                    doNotModify = columnCount == 1 && strings != null && strings.length == columnCount;
                    if (strings != null && !doNotModify) {
                        String[] temp = new String[columnCount];
                        System.arraycopy(strings, 0, temp, 0, index);
                        System.arraycopy(strings, index, temp, index + 1, columnCount - index - 1);
                        temp[index] = "";
                        ((DartTableItem) item.getImpl()).strings = temp;
                    }
                }
            }
        }
        updateHeaderCSS();
        /*
	 * Feature in GTK. The tree view does not resize immediately if a table
	 * column is created when the table is not visible. If the width of the
	 * new column is queried, GTK returns an incorrect value. The fix is to
	 * ensure that the columns are resized before any queries.
	 */
        if (!isVisible()) {
            forceResize();
        }
    }

    void createItem(TableItem item, int index) {
        if (!(0 <= index && index <= itemCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (itemCount == items.length) {
            int length = drawCount <= 0 ? items.length + 4 : Math.max(4, items.length * 3 / 2);
            TableItem[] newItems = new TableItem[length];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }
        ;
        /*
	* Feature in GTK.  It is much faster to append to a list store
	* than to insert at the end using gtk_list_store_insert().
	*/
        if (index == itemCount) {
        } else {
        }
        System.arraycopy(items, index, items, index + 1, itemCount++ - index);
        items[index] = item;
    }

    void createRenderers(long columnHandle, int modelIndex, boolean check, int columnStyle) {
        if ((getApi().style & SWT.CHECK) != 0 && check) {
            if (ownerDraw) {
            } else {
            }
        }
        if (ownerDraw) {
        }
        /*
	* Feature in GTK.  When a tree view column contains only one activatable
	* cell renderer such as a toggle renderer, mouse clicks anywhere in a cell
	* activate that renderer. The workaround is to set a second cell renderer
	* to be activatable.
	*/
        if ((getApi().style & SWT.CHECK) != 0 && check) {
        }
        /* Set alignment */
        if ((columnStyle & SWT.RIGHT) != 0) {
        } else if ((columnStyle & SWT.CENTER) != 0) {
        } else {
        }
        /* Add attributes */
        if (!ownerDraw) {
        }
        boolean customDraw = firstCustomDraw;
        if (columnCount != 0) {
            for (int i = 0; i < columnCount; i++) {
                if (columns[i].handle == columnHandle) {
                    customDraw = ((DartTableColumn) columns[i].getImpl()).customDraw;
                    break;
                }
            }
        }
        if ((getApi().style & SWT.VIRTUAL) != 0 || customDraw || ownerDraw) {
        }
    }

    @Override
    void createWidget(int index) {
        super.createWidget(index);
        items = new TableItem[4];
        columns = new TableColumn[4];
        itemCount = columnCount = 0;
        // In GTK 3 font description is inherited from parent widget which is not how SWT has always worked,
        // reset to default font to get the usual behavior
        setFontDescription(defaultFont().handle);
    }

    @Override
    int applyThemeBackground() {
        return -1;
        /* No Change */
    }

    @Override
    void deregister() {
        super.deregister();
        if (checkRenderer != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(checkRenderer);
        ((SwtDisplay) display.getImpl()).removeWidget(modelHandle);
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
        if (index < 0 || index >= itemCount)
            return;
        boolean fixColumn = showFirstColumn();
        if (fixColumn)
            hideFirstColumn();
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
        boolean fixColumn = showFirstColumn();
        for (int index = start; index <= end; index++) {
            if (index < 0 || index >= itemCount)
                continue;
        }
        if (fixColumn)
            hideFirstColumn();
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
        boolean fixColumn = showFirstColumn();
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            if (index < 0 || index >= itemCount)
                continue;
        }
        if (fixColumn)
            hideFirstColumn();
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
        boolean fixColumn = showFirstColumn();
        if (fixColumn)
            hideFirstColumn();
        selection = new int[0];
    }

    void destroyItem(TableColumn column) {
        int index = 0;
        while (index < columnCount) {
            if (columns[index] == column)
                break;
            index++;
        }
        if (index == columnCount)
            return;
        if (columnCount == 1) {
            firstCustomDraw = ((DartTableColumn) column.getImpl()).customDraw;
        }
        System.arraycopy(columns, index + 1, columns, index, --columnCount - index);
        columns[columnCount] = null;
        if (columnCount == 0) {
            createColumn(null, 0);
        } else {
            for (int i = 0; i < itemCount; i++) {
                TableItem item = items[i];
                if (item != null) {
                    int modelIndex = ((DartTableColumn) column.getImpl()).modelIndex;
                    Font[] cellFont = ((DartTableItem) item.getImpl()).cellFont;
                    if (cellFont != null) {
                        if (columnCount == 0) {
                            ((DartTableItem) item.getImpl()).cellFont = null;
                        } else {
                            Font[] temp = new Font[columnCount];
                            System.arraycopy(cellFont, 0, temp, 0, index);
                            System.arraycopy(cellFont, index + 1, temp, index, columnCount - index);
                            ((DartTableItem) item.getImpl()).cellFont = temp;
                        }
                    }
                }
            }
            if (index == 0) {
                TableColumn checkColumn = columns[0];
                createRenderers(checkColumn.handle, ((DartTableColumn) checkColumn.getImpl()).modelIndex, true, checkColumn.style);
            }
        }
        if (!searchEnabled()) {
        } else {
        }
    }

    void destroyItem(TableItem item) {
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
        if (itemCount == 0)
            resetCustomDraw();
    }

    @Override
    boolean dragDetect(int x, int y, boolean filter, boolean dragOnTimeout, boolean[] consume) {
        return false;
    }

    @Override
    long eventWindow() {
        return paintWindow();
    }

    @Override
    long eventSurface() {
        return paintSurface();
    }

    @Override
    Rectangle getClientAreaInPixels() {
        checkWidget();
        return null;
    }

    @Override
    int getClientWidth() {
        int[] w = new int[1];
        return w[0];
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

    long[] getColumnTypes(int columnCount) {
        long[] types = new long[FIRST_COLUMN + (columnCount * CELL_TYPES)];
        // per cell data
        for (int i = FIRST_COLUMN; i < types.length; i += CELL_TYPES) {
        }
        return types;
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
        return this.columnOrder;
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

    TableItem getFocusItem() {
        long[] path = new long[1];
        if (path[0] == 0)
            return null;
        TableItem item = null;
        return item;
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
        return headerBackground != null ? headerBackground : display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
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
        return headerForeground != null ? headerForeground : display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
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
        int height = 0;
        if (columnCount > 0) {
            for (int i = 0; i < columnCount; i++) {
                long buttonHandle = ((DartTableColumn) columns[i].getImpl()).buttonHandle;
                if (buttonHandle != 0) {
                }
            }
        } else {
        }
        return height;
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
        if (!(0 <= index && index < itemCount))
            error(SWT.ERROR_INVALID_RANGE);
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
        long[] path = new long[1];
        if (path[0] == 0)
            return null;
        TableItem item = null;
        return item;
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
        int height = 0;
        if (itemCount == 0) {
            int[] h = new int[1];
            ignoreSize = true;
            height = h[0];
            height += h[0];
            ignoreSize = false;
        } else {
            int columnCount = Math.max(1, this.columnCount);
            for (int i = 0; i < columnCount; i++) {
                int[] h = new int[1];
                int[] ypad = new int[1];
                height = Math.max(height, h[0] + ypad[0]);
            }
        }
        return height;
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
        TableItem[] result = new TableItem[itemCount];
        if ((getApi().style & SWT.VIRTUAL) != 0) {
            for (int i = 0; i < itemCount; i++) {
                result[i] = _getItem(i);
            }
        } else {
            System.arraycopy(items, 0, result, 0, itemCount);
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
     */
    public boolean getLinesVisible() {
        checkWidget();
        return this.linesVisible;
    }

    long getPixbufRenderer(long column) {
        long pixbufRenderer = 0;
        return pixbufRenderer;
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
        return new TableItem[0];
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
        return -1;
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
        return new int[0];
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

    long getTextRenderer(long column) {
        long textRenderer = 0;
        return textRenderer;
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
        if (cachedAdjustment == currentAdjustment) {
            return topIndex;
        } else {
            long[] path = new long[1];
            if (path[0] == 0)
                return 0;
            int[] index = new int[1];
            return index[0];
        }
    }

    private void toggleItemAndSendEvent(TableItem item) {
        item.setChecked(!item.getChecked());
        Event event = new Event();
        event.detail = SWT.CHECK;
        event.item = item;
        sendSelectionEvent(SWT.Selection, event, false);
    }

    /**
     * Used to emulate DefaultSelection event. See Bug 312568.
     * Feature in GTK. 'row-activation' event comes before DoubleClick event.
     * This is causing the editor not to get focus after double-click.
     * The solution is to manually send the DefaultSelection event after a double-click,
     * and to emulate it for Space/Return.
     */
    void sendTreeDefaultSelection() {
        //Note, similar DefaultSelectionHandling in SWT List/Table/Tree
        TableItem tableItem = getFocusItem();
        if (tableItem == null)
            return;
        Event event = new Event();
        event.item = tableItem;
        sendSelectionEvent(SWT.DefaultSelection, event, false);
    }

    void hideFirstColumn() {
    }

    @Override
    void hookEvents() {
        super.hookEvents();
        if (checkRenderer != 0) {
        }
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
        if (1 <= lastIndexOf && lastIndexOf < itemCount - 1) {
            if (items[lastIndexOf] == item)
                return lastIndexOf;
            if (items[lastIndexOf + 1] == item)
                return ++lastIndexOf;
            if (items[lastIndexOf - 1] == item)
                return --lastIndexOf;
        }
        if (lastIndexOf < itemCount / 2) {
            for (int i = 0; i < itemCount; i++) {
                if (items[i] == item)
                    return lastIndexOf = i;
            }
        } else {
            for (int i = itemCount - 1; i >= 0; --i) {
                if (items[i] == item)
                    return lastIndexOf = i;
            }
        }
        return -1;
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
    boolean mnemonicHit(char key) {
        for (int i = 0; i < columnCount; i++) {
            long labelHandle = ((DartTableColumn) columns[i].getImpl()).labelHandle;
            if (labelHandle != 0 && mnemonicHit(labelHandle, key))
                return true;
        }
        return false;
    }

    @Override
    boolean mnemonicMatch(char key) {
        for (int i = 0; i < columnCount; i++) {
            long labelHandle = ((DartTableColumn) columns[i].getImpl()).labelHandle;
            if (labelHandle != 0 && mnemonicMatch(labelHandle, key))
                return true;
        }
        return false;
    }

    @Override
    long paintWindow() {
        return 0;
    }

    void recreateRenderers() {
        if (checkRenderer != 0) {
            ((SwtDisplay) display.getImpl()).removeWidget(checkRenderer);
            if (checkRenderer == 0)
                error(SWT.ERROR_NO_HANDLES);
            ((SwtDisplay) display.getImpl()).addWidget(checkRenderer, this.getApi());
        }
        if (columnCount == 0) {
        } else {
            for (int i = 0; i < columnCount; i++) {
                TableColumn column = columns[i];
                createRenderers(column.handle, ((DartTableColumn) column.getImpl()).modelIndex, i == 0, column.style);
            }
        }
    }

    @Override
    void redrawBackgroundImage() {
        Control control = findBackgroundControl();
        if (control != null && control.getImpl()._backgroundImage() != null) {
            redrawWidget(0, 0, 0, 0, true, false, false);
        }
    }

    @Override
    void register() {
        super.register();
        if (checkRenderer != 0)
            ((SwtDisplay) display.getImpl()).addWidget(checkRenderer, this.getApi());
        ((SwtDisplay) display.getImpl()).addWidget(modelHandle, this.getApi());
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (items != null) {
            for (int i = 0; i < itemCount; i++) {
                TableItem item = items[i];
                if (item != null && !item.isDisposed()) {
                    item.getImpl().release(false);
                }
            }
            items = null;
        }
        if (columns != null) {
            for (int i = 0; i < columnCount; i++) {
                TableColumn column = columns[i];
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
        modelHandle = 0;
        checkRenderer = 0;
        currentItem = null;
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
        if (!(0 <= index && index < itemCount))
            error(SWT.ERROR_ITEM_NOT_REMOVED);
        TableItem item = items[index];
        boolean disposed = false;
        if (item != null) {
            disposed = item.isDisposed();
            if (!disposed) {
                item.getImpl().release(false);
            }
        } else {
        }
        if (!disposed) {
            System.arraycopy(items, index + 1, items, index, --itemCount - index);
            items[itemCount] = null;
        }
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
        if (!(0 <= start && start <= end && end < itemCount)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        if (start == 0 && end == itemCount - 1) {
            removeAll();
            return;
        }
        checkSetDataInProcessBeforeRemoval(start, end + 1);
        int index = -1;
        for (index = start; index <= end; index++) {
            TableItem item = items[index];
            if (item != null && !item.isDisposed())
                item.getImpl().release(false);
        }
        index = end + 1;
        System.arraycopy(items, index, items, start, itemCount - index);
        for (int i = itemCount - (index - start); i < itemCount; i++) items[i] = null;
        itemCount = itemCount - (index - start);
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
        int start = newIndices[newIndices.length - 1], end = newIndices[0];
        if (!(0 <= start && start <= end && end < itemCount)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        int last = -1;
        for (int i = 0; i < newIndices.length; i++) {
            int index = newIndices[i];
            if (index != last) {
                TableItem item = items[index];
                boolean disposed = false;
                if (item != null) {
                    disposed = item.isDisposed();
                    if (!disposed) {
                        item.getImpl().release(false);
                    }
                } else {
                }
                if (!disposed) {
                    System.arraycopy(items, index + 1, items, index, --itemCount - index);
                    items[itemCount] = null;
                }
                last = index;
            }
        }
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
        checkSetDataInProcessBeforeRemoval(0, items.length);
        int index = itemCount - 1;
        while (index >= 0) {
            TableItem item = items[index];
            if (item != null && !item.isDisposed())
                item.getImpl().release(false);
            --index;
        }
        items = new TableItem[4];
        itemCount = 0;
        resetCustomDraw();
        if (!searchEnabled()) {
        } else {
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

    void sendMeasureEvent(long cell, long width, long height) {
    }

    @Override
    long rendererGetPreferredWidthProc(long cell, long handle, long minimun_size, long natural_size) {
        sendMeasureEvent(cell, minimun_size, 0);
        return 0;
    }

    @Override
    long rendererSnapshotProc(long cell, long snapshot, long widget, long background_area, long cell_area, long flags) {
        return 0;
    }

    @Override
    long rendererRenderProc(long cell, long cr, long widget, long background_area, long cell_area, long flags) {
        rendererRender(cell, cr, 0, widget, background_area, cell_area, 0, flags);
        return 0;
    }

    void rendererRender(long cell, long cr, long snapshot, long widget, long background_area, long cell_area, long expose_area, long flags) {
        TableItem item = null;
        int columnIndex = 0;
        if (columnCount > 0) {
            for (int i = 0; i < columnCount; i++) {
            }
        }
        {
        }
        if (item != null) {
        }
        if ((drawState & SWT.BACKGROUND) != 0 && (drawState & SWT.SELECTED) == 0) {
            GC gc = getGC(cr);
            gc.setBackground(item.getBackground(columnIndex));
            gc.dispose();
        }
        if (item != null) {
        }
    }

    private GC getGC(long cr) {
        GCData gcData = new GCData();
        gcData.cairo = cr;
        return null;
    }

    void resetCustomDraw() {
        if ((getApi().style & SWT.VIRTUAL) != 0 || ownerDraw)
            return;
        int end = Math.max(1, columnCount);
        for (int i = 0; i < end; i++) {
            boolean customDraw = columnCount != 0 ? ((DartTableColumn) columns[i].getImpl()).customDraw : firstCustomDraw;
            if (customDraw) {
                if (columnCount != 0)
                    ((DartTableColumn) columns[i].getImpl()).customDraw = false;
            }
        }
        firstCustomDraw = false;
    }

    @Override
    void reskinChildren(int flags) {
        if (items != null) {
            for (int i = 0; i < itemCount; i++) {
                TableItem item = items[i];
                if (item != null)
                    item.reskin(flags);
            }
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

    boolean searchEnabled() {
        /* Disable searching when using VIRTUAL or NO_SEARCH */
        if ((getApi().style & SWT.VIRTUAL) != 0 || (getApi().style & SWT.NO_SEARCH) != 0)
            return false;
        return true;
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
        if (!(0 <= index && index < itemCount))
            return;
        boolean fixColumn = showFirstColumn();
        this.selection = newValue;
        if (fixColumn)
            hideFirstColumn();
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
        if (itemCount == 0 || start >= itemCount)
            return;
        start = Math.max(0, start);
        end = Math.min(end, itemCount - 1);
        boolean fixColumn = showFirstColumn();
        for (int index = start; index <= end; index++) {
        }
        this.selection = newValue;
        if (fixColumn)
            hideFirstColumn();
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
        boolean fixColumn = showFirstColumn();
        for (int i = 0; i < length; i++) {
            int index = indices[i];
            if (!(0 <= index && index < itemCount))
                continue;
        }
        this.selection = newValue;
        if (fixColumn)
            hideFirstColumn();
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
        boolean fixColumn = showFirstColumn();
        if (fixColumn)
            hideFirstColumn();
        {
            selection = new int[items.length];
            for (int i = 0; i < items.length; ++i) {
                selection[i] = i;
            }
        }
    }

    void selectFocusIndex(int index) {
        /*
	* Note that this method both selects and sets the focus to the
	* specified index, so any previous selection in the list will be lost.
	* gtk does not provide a way to just set focus to a specified list item.
	*/
        if (!(0 <= index && index < itemCount))
            return;
    }

    @Override
    void setBackgroundSurface(Image image) {
        ownerDraw = true;
        recreateRenderers();
    }

    @Override
    int setBounds(int x, int y, int width, int height, boolean move, boolean resize) {
        int result = super.setBounds(x, y, width, height, move, resize);
        if (result != 0) {
            boundsChangedSinceLastDraw = true;
        }
        return result;
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
            if (order.length > 0)
                error(SWT.ERROR_INVALID_ARGUMENT);
            return;
        }
        if (order.length != columnCount)
            error(SWT.ERROR_INVALID_ARGUMENT);
        boolean[] seen = new boolean[columnCount];
        for (int i = 0; i < order.length; i++) {
            int index = order[i];
            if (index < 0 || index >= columnCount)
                error(SWT.ERROR_INVALID_RANGE);
            if (seen[index])
                error(SWT.ERROR_INVALID_ARGUMENT);
            seen[index] = true;
        }
        this.columnOrder = newValue;
        for (int i = 0; i < order.length; i++) {
        }
    }

    @Override
    void setFontDescription(long font) {
        super.setFontDescription(font);
        TableColumn[] columns = getColumns();
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] != null) {
                ((DartTableColumn) columns[i].getImpl()).setFontDescription(font);
            }
        }
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
        checkWidget();
        if (!java.util.Objects.equals(this.headerBackground, color)) {
            dirty();
        }
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (color.equals(headerBackground))
                return;
        }
        headerBackground = color;
        updateHeaderCSS();
    }

    void updateHeaderCSS() {
        StringBuilder css = new StringBuilder("button {");
        if (headerBackground != null) {
        }
        if (headerForeground != null) {
        }
        css.append("}\n");
        if (columnCount == 0) {
            if (headerCSSProvider == 0) {
            }
        } else {
            for (TableColumn column : columns) {
                if (column != null) {
                    ((DartTableColumn) column.getImpl()).setHeaderCSS(css.toString());
                }
            }
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
        checkWidget();
        if (!java.util.Objects.equals(this.headerForeground, color)) {
            dirty();
        }
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
            if (color.equals(headerForeground))
                return;
        }
        headerForeground = color;
        updateHeaderCSS();
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
        checkWidget();
        if (!java.util.Objects.equals(this.headerVisible, show)) {
            dirty();
        }
        this.headerHeight = this.getHeaderHeight();
        this.headerVisible = show;
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
        if (count == itemCount)
            return;
        boolean isVirtual = (getApi().style & SWT.VIRTUAL) != 0;
        if (!isVirtual)
            setRedraw(false);
        remove(count, itemCount - 1);
        int length = Math.max(4, (count + 3) / 4 * 4);
        TableItem[] newItems = new TableItem[length];
        System.arraycopy(items, 0, newItems, 0, itemCount);
        items = newItems;
        if (isVirtual) {
            for (int i = itemCount; i < count; i++) {
            }
            itemCount = count;
        } else {
            for (int i = itemCount; i < count; i++) {
                new TableItem(this.getApi(), SWT.NONE, i, true);
            }
        }
        if (!isVirtual)
            setRedraw(true);
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

    void setModel(long newModel) {
        ((SwtDisplay) display.getImpl()).removeWidget(modelHandle);
        modelHandle = newModel;
        ((SwtDisplay) display.getImpl()).addWidget(modelHandle, this.getApi());
    }

    @Override
    void setOrientation(boolean create) {
        super.setOrientation(create);
        for (int i = 0; i < itemCount; i++) {
            if (items[i] != null)
                ((DartWidget) items[i].getImpl()).setOrientation(create);
        }
        for (int i = 0; i < columnCount; i++) {
            if (columns[i] != null)
                ((DartTableColumn) columns[i].getImpl()).setOrientation(create);
        }
    }

    @Override
    void setParentBackground() {
        ownerDraw = true;
        recreateRenderers();
    }

    @Override
    public void setRedraw(boolean redraw) {
        dirty();
        checkWidget();
        super.setRedraw(redraw);
        if (redraw && drawCount == 0) {
            /* Resize the item array to match the item count */
            if (items.length > 4 && items.length - itemCount > 3) {
                int length = Math.max(4, (itemCount + 3) / 4 * 4);
                TableItem[] newItems = new TableItem[length];
                System.arraycopy(items, 0, newItems, 0, itemCount);
                items = newItems;
            }
        }
    }

    void setScrollWidth(long column, TableItem item) {
        if (columnCount != 0 || currentItem == item)
            return;
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
        }
        sortColumn = column;
        if (sortColumn != null && sortDirection != SWT.NONE) {
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
        if (direction != SWT.UP && direction != SWT.DOWN && direction != SWT.NONE)
            return;
        sortDirection = direction;
        if (sortColumn == null || sortColumn.isDisposed())
            return;
        if (sortDirection == SWT.NONE) {
        } else {
        }
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
        dirty();
        int[] newValue = new int[] { index };
        checkWidget();
        boolean fixColumn = showFirstColumn();
        deselectAll();
        selectFocusIndex(index);
        showSelection();
        this.selection = newValue;
        if (fixColumn)
            hideFirstColumn();
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
        if (itemCount == 0 || start >= itemCount)
            return;
        boolean fixColumn = showFirstColumn();
        start = Math.max(0, start);
        end = Math.min(end, itemCount - 1);
        selectFocusIndex(start);
        if ((getApi().style & SWT.MULTI) != 0) {
            select(start, end);
        }
        showSelection();
        if (fixColumn)
            hideFirstColumn();
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
        checkWidget();
        if (indices == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        deselectAll();
        int length = indices.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))
            return;
        boolean fixColumn = showFirstColumn();
        selectFocusIndex(indices[0]);
        if ((getApi().style & SWT.MULTI) != 0) {
            select(indices);
        }
        showSelection();
        if (fixColumn)
            hideFirstColumn();
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
        boolean fixColumn = showFirstColumn();
        deselectAll();
        int length = items.length;
        if (!(length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))) {
            boolean first = true;
            for (int i = 0; i < length; i++) {
                int index = indexOf(items[i]);
                if (index != -1) {
                    if (first) {
                        first = false;
                        selectFocusIndex(index);
                    } else {
                        select(index);
                    }
                }
            }
            showSelection();
        }
        if (fixColumn)
            hideFirstColumn();
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
        checkWidget();
        if (!java.util.Objects.equals(this.topIndex, index)) {
            dirty();
        }
        if (!(0 <= index && index < itemCount))
            return;
        topIndex = index;
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
    }

    boolean showFirstColumn() {
        /*
	* Bug in GTK.  If no columns are visible, changing the selection
	* will fail.  The fix is to temporarily make a column visible.
	*/
        int columnCount = Math.max(1, this.columnCount);
        for (int i = 0; i < columnCount; i++) {
        }
        return true;
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
        if (((DartTableItem) item.getImpl()).parent != this.getApi())
            return;
        showItem(item.handle);
    }

    void showItem(long iter) {
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
        TableItem[] selection = getSelection();
        if (selection.length == 0)
            return;
        TableItem item = selection[0];
        showItem(item.handle);
    }

    @Override
    void updateScrollBarValue(ScrollBar bar) {
        super.updateScrollBarValue(bar);
    }

    /**
     * Check the table item range [start, end) for items that are in process of
     * sending {@code SWT#SetData} event. If such items exist, throw an exception.
     *
     * Does nothing if the given range contains no indices.
     *
     * @param start index of first item to check
     * @param end index after the last item to check
     */
    void checkSetDataInProcessBeforeRemoval(int start, int end) {
        /*
	 * Bug 182598 - assertion failed in gtktreestore.c
	 *
	 * To prevent a crash in GTK, we ensure we are not setting data on the tree items we are about to remove.
	 * Removing an item while its data is being set will invalidate it, which will cause a crash.
	 *
	 * We therefore throw an exception to prevent the crash.
	 */
        for (int i = start; i < end; i++) {
            TableItem item = items[i];
            if (item != null && ((DartTableItem) item.getImpl()).settingData) {
                String message = "Cannot remove a table item while its data is being set. " + "At item " + i + " in range [" + start + ", " + end + ").";
                throw new SWTException(message);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (headerCSSProvider != 0) {
            headerCSSProvider = 0;
        }
    }

    int[] columnOrder = new int[0];

    boolean linesVisible;

    int[] selection = new int[0];

    public long _modelHandle() {
        return modelHandle;
    }

    public long _checkRenderer() {
        return checkRenderer;
    }

    public int _itemCount() {
        return itemCount;
    }

    public int _columnCount() {
        return columnCount;
    }

    public int _lastIndexOf() {
        return lastIndexOf;
    }

    public int _sortDirection() {
        return sortDirection;
    }

    public int _selectionCountOnPress() {
        return selectionCountOnPress;
    }

    public int _selectionCountOnRelease() {
        return selectionCountOnRelease;
    }

    public long _ignoreCell() {
        return ignoreCell;
    }

    public TableItem[] _items() {
        return items;
    }

    public TableColumn[] _columns() {
        return columns;
    }

    public TableItem _currentItem() {
        return currentItem;
    }

    public TableColumn _sortColumn() {
        return sortColumn;
    }

    public boolean _firstCustomDraw() {
        return firstCustomDraw;
    }

    public boolean _firstCompute() {
        return firstCompute;
    }

    public int _drawState() {
        return drawState;
    }

    public int _drawFlags() {
        return drawFlags;
    }

    public Color _headerBackground() {
        return headerBackground;
    }

    public Color _headerForeground() {
        return headerForeground;
    }

    public boolean _ownerDraw() {
        return ownerDraw;
    }

    public boolean _ignoreSize() {
        return ignoreSize;
    }

    public boolean _pixbufSizeSet() {
        return pixbufSizeSet;
    }

    public boolean _hasChildren() {
        return hasChildren;
    }

    public int _maxWidth() {
        return maxWidth;
    }

    public int _topIndex() {
        return topIndex;
    }

    public double _cachedAdjustment() {
        return cachedAdjustment;
    }

    public double _currentAdjustment() {
        return currentAdjustment;
    }

    public int _pixbufHeight() {
        return pixbufHeight;
    }

    public int _pixbufWidth() {
        return pixbufWidth;
    }

    public int _headerHeight() {
        return headerHeight;
    }

    public boolean _boundsChangedSinceLastDraw() {
        return boundsChangedSinceLastDraw;
    }

    public boolean _headerVisible() {
        return headerVisible;
    }

    public boolean _wasScrolled() {
        return wasScrolled;
    }

    public boolean _rowActivated() {
        return rowActivated;
    }

    public int[] _columnOrder() {
        return columnOrder;
    }

    public boolean _linesVisible() {
        return linesVisible;
    }

    public int[] _selection() {
        return selection;
    }

    protected void _hookEvents() {
        super._hookEvents();
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
