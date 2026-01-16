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

    TableItem[] items;

    TableColumn[] columns;

    TableColumn sortColumn;

    TableItem currentItem;

    int columnCount, itemCount, lastIndexOf, sortDirection, selectedRowIndex = -1;

    boolean ignoreSelect, fixScrollWidth, drawExpansion, didSelect, preventSelect, dragDetected;

    Rectangle imageBounds;

    double[] headerBackground, headerForeground;

    /* Used to control drop feedback when FEEDBACK_SCROLL is set/not set */
    boolean shouldScroll = true;

    boolean keyDown;

    static int NEXT_ID;

    static final int FIRST_COLUMN_MINIMUM_WIDTH = 5;

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
        clearCachedWidth(items);
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

    TableItem _getItem(int index) {
        if ((getApi().style & SWT.VIRTUAL) == 0)
            return items[index];
        if (items[index] != null)
            return items[index];
        return items[index] = new TableItem(this.getApi(), SWT.NULL, -1, false);
    }

    int calculateWidth(TableItem[] items, int index, GC gc) {
        int width = 0;
        for (int i = 0; i < itemCount; i++) {
            TableItem item = items[i];
            if (item != null && ((DartTableItem) item.getImpl()).cached) {
                width = Math.max(width, ((DartTableItem) item.getImpl()).calculateWidth(index, gc, isSelected(index)));
            }
        }
        return width;
    }

    boolean checkData(TableItem item) {
        return true;
    }

    boolean checkData(TableItem item, int index) {
        if (((DartTableItem) item.getImpl()).cached)
            return true;
        if ((getApi().style & SWT.VIRTUAL) != 0) {
            ((DartTableItem) item.getImpl()).cached = true;
            Event event = new Event();
            event.item = item;
            currentItem = item;
            sendEvent(SWT.SetData, event);
            //widget could be disposed at this point
            currentItem = null;
            if (isDisposed() || item.isDisposed())
                return false;
            if (!setScrollWidth(item))
                ((DartTableItem) item.getImpl()).redraw(-1);
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
        if (!(0 <= index && index < itemCount))
            error(SWT.ERROR_INVALID_RANGE);
        TableItem item = items[index];
        if (item != null) {
            if (currentItem != item)
                ((DartTableItem) item.getImpl()).clear();
            if (currentItem == null)
                ((DartTableItem) item.getImpl()).redraw(-1);
            setScrollWidth(item);
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
        if (!(0 <= start && start <= end && end < itemCount)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        if (start == 0 && end == itemCount - 1) {
            clearAll();
        } else {
            for (int i = start; i <= end; i++) {
                clear(i);
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
            clear(indices[i]);
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
            if (item != null) {
                ((DartTableItem) item.getImpl()).clear();
            }
        }
        setScrollWidth(items, true);
    }

    void clearCachedWidth(TableItem[] items) {
        if (items == null)
            return;
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null)
                ((DartTableItem) items[i].getImpl()).width = -1;
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return Sizes.computeSize(this, wHint, hHint, changed);
    }

    void createColumn(TableItem item, int index) {
        String[] strings = ((DartTableItem) item.getImpl()).strings;
        if (strings != null) {
            String[] temp = new String[columnCount];
            System.arraycopy(strings, 0, temp, 0, index);
            System.arraycopy(strings, index, temp, index + 1, columnCount - index - 1);
            temp[index] = "";
            ((DartTableItem) item.getImpl()).strings = temp;
        }
        if (index == 0)
            ((DartItem) item.getImpl()).text = "";
        Image[] images = ((DartTableItem) item.getImpl()).images;
        if (images != null) {
            Image[] temp = new Image[columnCount];
            System.arraycopy(images, 0, temp, 0, index);
            System.arraycopy(images, index, temp, index + 1, columnCount - index - 1);
            ((DartTableItem) item.getImpl()).images = temp;
        }
        if (index == 0)
            ((DartItem) item.getImpl()).image = null;
        Color[] cellBackground = ((DartTableItem) item.getImpl()).cellBackground;
        if (cellBackground != null) {
            Color[] temp = new Color[columnCount];
            System.arraycopy(cellBackground, 0, temp, 0, index);
            System.arraycopy(cellBackground, index, temp, index + 1, columnCount - index - 1);
            ((DartTableItem) item.getImpl()).cellBackground = temp;
        }
        Color[] cellForeground = ((DartTableItem) item.getImpl()).cellForeground;
        if (cellForeground != null) {
            Color[] temp = new Color[columnCount];
            System.arraycopy(cellForeground, 0, temp, 0, index);
            System.arraycopy(cellForeground, index, temp, index + 1, columnCount - index - 1);
            ((DartTableItem) item.getImpl()).cellForeground = temp;
        }
        Font[] cellFont = ((DartTableItem) item.getImpl()).cellFont;
        if (cellFont != null) {
            Font[] temp = new Font[columnCount];
            System.arraycopy(cellFont, 0, temp, 0, index);
            System.arraycopy(cellFont, index, temp, index + 1, columnCount - index - 1);
            ((DartTableItem) item.getImpl()).cellFont = temp;
        }
    }

    @Override
    void createHandle() {
    }

    void createItem(TableColumn column, int index) {
        if (!(0 <= index && index <= columnCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (columnCount == columns.length) {
            TableColumn[] newColumns = new TableColumn[columnCount + 4];
            System.arraycopy(columns, 0, newColumns, 0, columns.length);
            columns = newColumns;
        }
        if (columnCount == 0) {
        } else {
        }
        ((DartWidget) column.getImpl()).createJNIRef();
        System.arraycopy(columns, index, columns, index + 1, columnCount++ - index);
        columns[index] = column;
        for (int i = 0; i < itemCount; i++) {
            TableItem item = items[i];
            if (item != null) {
                if (columnCount > 1) {
                    createColumn(item, index);
                }
            }
        }
    }

    void createItem(TableItem item, int index) {
        if (!(0 <= index && index <= itemCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (itemCount == items.length) {
            /* Grow the array faster when redraw is off */
            int length = getDrawing() ? items.length + 4 : Math.max(4, items.length * 3 / 2);
            TableItem[] newItems = new TableItem[length];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }
        System.arraycopy(items, index, items, index + 1, itemCount++ - index);
        items[index] = item;
        updateRowCount();
        if (index != itemCount)
            fixSelection(index, true);
    }

    @Override
    void createWidget() {
        super.createWidget();
        items = new TableItem[4];
        columns = new TableColumn[4];
    }

    @Override
    Color defaultBackground() {
        return ((SwtDisplay) display.getImpl()).getWidgetColor(SWT.COLOR_LIST_BACKGROUND);
    }

    @Override
    Color defaultForeground() {
        return ((SwtDisplay) display.getImpl()).getWidgetColor(SWT.COLOR_LIST_FOREGROUND);
    }

    @Override
    void deregister() {
        super.deregister();
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
        if (0 <= index && index < itemCount) {
            ignoreSelect = true;
            ignoreSelect = false;
        }
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
        if (start > end)
            return;
        if (end < 0 || start >= itemCount)
            return;
        start = Math.max(0, start);
        end = Math.min(itemCount - 1, end);
        if (start == 0 && end == itemCount - 1) {
            deselectAll();
        } else {
            ignoreSelect = true;
            for (int i = start; i <= end; i++) {
            }
            ignoreSelect = false;
        }
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
        ignoreSelect = true;
        for (int i = 0; i < indices.length; i++) {
        }
        ignoreSelect = false;
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
        for (int i = 0; i < itemCount; i++) {
            TableItem item = items[i];
            if (item != null) {
                if (columnCount <= 1) {
                    ((DartTableItem) item.getImpl()).strings = null;
                    ((DartTableItem) item.getImpl()).images = null;
                    ((DartTableItem) item.getImpl()).cellBackground = null;
                    ((DartTableItem) item.getImpl()).cellForeground = null;
                    ((DartTableItem) item.getImpl()).cellFont = null;
                } else {
                    if (((DartTableItem) item.getImpl()).strings != null) {
                        String[] strings = ((DartTableItem) item.getImpl()).strings;
                        if (index == 0) {
                            ((DartItem) item.getImpl()).text = strings[1] != null ? strings[1] : "";
                        }
                        String[] temp = new String[columnCount - 1];
                        System.arraycopy(strings, 0, temp, 0, index);
                        System.arraycopy(strings, index + 1, temp, index, columnCount - 1 - index);
                        ((DartTableItem) item.getImpl()).strings = temp;
                    } else {
                        if (index == 0)
                            ((DartItem) item.getImpl()).text = "";
                    }
                    if (((DartTableItem) item.getImpl()).images != null) {
                        Image[] images = ((DartTableItem) item.getImpl()).images;
                        if (index == 0)
                            ((DartItem) item.getImpl()).image = images[1];
                        Image[] temp = new Image[columnCount - 1];
                        System.arraycopy(images, 0, temp, 0, index);
                        System.arraycopy(images, index + 1, temp, index, columnCount - 1 - index);
                        ((DartTableItem) item.getImpl()).images = temp;
                    } else {
                        if (index == 0)
                            ((DartItem) item.getImpl()).image = null;
                    }
                    if (((DartTableItem) item.getImpl()).cellBackground != null) {
                        Color[] cellBackground = ((DartTableItem) item.getImpl()).cellBackground;
                        Color[] temp = new Color[columnCount - 1];
                        System.arraycopy(cellBackground, 0, temp, 0, index);
                        System.arraycopy(cellBackground, index + 1, temp, index, columnCount - 1 - index);
                        ((DartTableItem) item.getImpl()).cellBackground = temp;
                    }
                    if (((DartTableItem) item.getImpl()).cellForeground != null) {
                        Color[] cellForeground = ((DartTableItem) item.getImpl()).cellForeground;
                        Color[] temp = new Color[columnCount - 1];
                        System.arraycopy(cellForeground, 0, temp, 0, index);
                        System.arraycopy(cellForeground, index + 1, temp, index, columnCount - 1 - index);
                        ((DartTableItem) item.getImpl()).cellForeground = temp;
                    }
                    if (((DartTableItem) item.getImpl()).cellFont != null) {
                        Font[] cellFont = ((DartTableItem) item.getImpl()).cellFont;
                        Font[] temp = new Font[columnCount - 1];
                        System.arraycopy(cellFont, 0, temp, 0, index);
                        System.arraycopy(cellFont, index + 1, temp, index, columnCount - 1 - index);
                        ((DartTableItem) item.getImpl()).cellFont = temp;
                    }
                }
            }
        }
        System.arraycopy(columns, index + 1, columns, index, --columnCount - index);
        columns[columnCount] = null;
        if (columnCount == 0) {
            setScrollWidth();
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
        if (index != itemCount - 1)
            fixSelection(index, false);
        System.arraycopy(items, index + 1, items, index, --itemCount - index);
        items[itemCount] = null;
        updateRowCount();
        if (itemCount == 0)
            setTableEmpty();
    }

    @Override
    boolean dragDetect(int x, int y, boolean filter, boolean[] consume) {
        // Let Cocoa determine if a drag is starting and fire the notification when we get the callback.
        return false;
    }

    void fixSelection(int index, boolean add) {
        int[] selection = getSelectionIndices();
        if (selection.length == 0)
            return;
        int newCount = 0;
        boolean fix = false;
        for (int i = 0; i < selection.length; i++) {
            if (!add && selection[i] == index) {
                fix = true;
            } else {
                int newIndex = newCount++;
                selection[newIndex] = selection[i];
                if (selection[newIndex] >= index) {
                    selection[newIndex] += add ? 1 : -1;
                    fix = true;
                }
            }
        }
        if (fix)
            select(selection, newCount, true);
    }

    int getCheckColumnWidth() {
        return 0;
    }

    @Override
    public Rectangle getClientArea() {
        return Sizes.getClientArea(this);
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
        int[] order = new int[columnCount];
        for (int i = 0; i < columnCount; i++) {
        }
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
     * @since 2.0
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
                result[i] = items[selection[i]];
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
        if (!(0 <= index && index < itemCount))
            return false;
        return false;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    void register() {
        super.register();
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
    void releaseHandle() {
        super.releaseHandle();
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        currentItem = null;
        sortColumn = null;
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
            error(SWT.ERROR_INVALID_RANGE);
        TableItem item = items[index];
        if (item != null)
            item.getImpl().release(false);
        if (index != itemCount - 1)
            fixSelection(index, false);
        System.arraycopy(items, index + 1, items, index, --itemCount - index);
        items[itemCount] = null;
        updateRowCount();
        if (itemCount == 0) {
            setTableEmpty();
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
        } else {
            int numOfItemsRemoved = end - start + 1;
            for (int i = start; i <= end; i++) {
                TableItem item = items[i];
                if (item != null)
                    item.getImpl().release(false);
            }
            //fix selection
            int[] selection = getSelectionIndices();
            if (selection.length != 0) {
                int newCount = 0;
                boolean fix = false;
                for (int i = 0; i < selection.length; i++) {
                    if (selection[i] >= start && selection[i] <= end) {
                        fix = true;
                    } else {
                        int newIndex = newCount++;
                        selection[newIndex] = selection[i];
                        if (selection[newIndex] > end) {
                            selection[newIndex] -= numOfItemsRemoved;
                            fix = true;
                        }
                    }
                }
                if (fix)
                    select(selection, newCount, true);
            }
            //fix items array
            System.arraycopy(items, start + numOfItemsRemoved, items, start, itemCount - (start + numOfItemsRemoved));
            for (int i = itemCount - numOfItemsRemoved; i < itemCount; i++) {
                items[i] = null;
            }
            itemCount -= numOfItemsRemoved;
            updateRowCount();
        }
        if (itemCount == 0) {
            setTableEmpty();
        }
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
                if (item != null)
                    item.getImpl().release(false);
                if (index != itemCount - 1)
                    fixSelection(index, false);
                System.arraycopy(items, index + 1, items, index, --itemCount - index);
                items[itemCount] = null;
                last = index;
            }
        }
        updateRowCount();
        if (itemCount == 0) {
            setTableEmpty();
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
        for (int i = 0; i < itemCount; i++) {
            TableItem item = items[i];
            if (item != null && !item.isDisposed())
                item.getImpl().release(false);
        }
        setTableEmpty();
        updateRowCount();
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
        this.selection = newValue;
        if (0 <= index && index < itemCount) {
            ignoreSelect = true;
            ignoreSelect = false;
        }
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
        this.selection = newValue;
        if (start == 0 && end == itemCount - 1) {
            selectAll();
        } else {
            start = Math.max(0, start);
            end = Math.min(end, itemCount - 1);
            ignoreSelect = true;
            ignoreSelect = false;
        }
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
        int count = 0;
        for (int i = 0; i < length; i++) {
            int index = indices[i];
            if (index >= 0 && index < itemCount) {
                count++;
            }
        }
        if (count > 0) {
            ignoreSelect = true;
            ignoreSelect = false;
        }
        this.selection = newValue;
    }

    void select(int[] indices, int count, boolean clear) {
        int[] newValue = indices;
        if (!java.util.Objects.equals(this.selection, newValue)) {
            dirty();
        }
        ignoreSelect = true;
        ignoreSelect = false;
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
        {
            selection = new int[items.length];
            for (int i = 0; i < items.length; ++i) {
                selection[i] = i;
            }
        }
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
        this.columnOrder = newValue;
        if (reorder) {
            int[] oldX = new int[oldOrder.length];
            for (int i = 0; i < oldOrder.length; i++) {
            }
            int[] newX = new int[order.length];
            for (int i = 0; i < order.length; i++) {
            }
            TableColumn[] newColumns = new TableColumn[columnCount];
            System.arraycopy(columns, 0, newColumns, 0, columnCount);
            for (int i = 0; i < columnCount; i++) {
                TableColumn column = newColumns[i];
                if (!column.isDisposed()) {
                    if (newX[i] != oldX[i]) {
                        column.getImpl().sendEvent(SWT.Move);
                    }
                }
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
        Color newValue = color;
        if (!java.util.Objects.equals(this._headerBackground, newValue)) {
            dirty();
        }
        checkWidget();
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
        }
        double[] headerBackground = color != null ? color.handle : null;
        if (equals(headerBackground, this.headerBackground))
            return;
        this.headerBackground = headerBackground;
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
        if (color != null) {
            if (color.isDisposed())
                error(SWT.ERROR_INVALID_ARGUMENT);
        }
        double[] headerForeground = color != null ? color.handle : null;
        if (equals(headerForeground, this.headerForeground))
            return;
        this.headerForeground = headerForeground;
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
        this.headerVisible = newValue;
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
        TableItem[] children = items;
        if (count < itemCount) {
            for (int index = count; index < itemCount; index++) {
                TableItem item = children[index];
                if (item != null && !item.isDisposed())
                    item.getImpl().release(false);
            }
        }
        if (count > itemCount) {
            if ((getStyle() & SWT.VIRTUAL) == 0) {
                for (int i = itemCount; i < count; i++) {
                    new TableItem(this.getApi(), SWT.NONE, i, true);
                }
                return;
            }
        }
        int length = Math.max(4, (count + 3) / 4 * 4);
        TableItem[] newItems = new TableItem[length];
        if (children != null) {
            System.arraycopy(items, 0, newItems, 0, Math.min(count, itemCount));
        }
        children = newItems;
        this.items = newItems;
        this.itemCount = count;
        updateRowCount();
    }

    /*public*/
    void setItemHeight(int itemHeight) {
        checkWidget();
        if (itemHeight < -1)
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (itemHeight == -1) {
            //TODO - reset item height, ensure other API's such as setFont don't do this
        } else {
        }
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
            setScrollWidth();
        }
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

    boolean setScrollWidth() {
        return setScrollWidth(items, true);
    }

    boolean setScrollWidth(TableItem item) {
        if (columnCount != 0)
            return false;
        if (!getDrawing())
            return false;
        if (currentItem != null) {
            if (currentItem != item)
                fixScrollWidth = true;
            return false;
        }
        GC gc = new GC(this.getApi());
        gc.dispose();
        return false;
    }

    boolean setScrollWidth(TableItem[] items, boolean set) {
        if (items == null)
            return false;
        if (columnCount != 0)
            return false;
        if (!getDrawing())
            return false;
        if (currentItem != null) {
            fixScrollWidth = true;
            return false;
        }
        GC gc = new GC(this.getApi());
        for (int i = 0; i < items.length; i++) {
            TableItem item = items[i];
            if (item != null) {
            }
        }
        gc.dispose();
        if (!set) {
        }
        return true;
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
        //TODO - optimize to use expand flag
        deselectAll();
        this.selection = newValue;
        if (0 <= index && index < itemCount) {
            select(index);
            showIndex(index);
        }
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
        //TODO - optimize to use expand flag
        deselectAll();
        if (end < 0 || start > end || ((getApi().style & SWT.SINGLE) != 0 && start != end))
            return;
        if (itemCount == 0 || start >= itemCount)
            return;
        start = Math.max(0, start);
        end = Math.min(end, itemCount - 1);
        select(start, end);
        showIndex(start);
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
        //TODO - optimize to use expand flag
        deselectAll();
        int length = indices.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))
            return;
        select(indices);
        showIndex(indices[0]);
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
        //TODO - optimize to use expand flag
        deselectAll();
        int length = items.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))
            return;
        int[] indices = new int[length];
        int count = 0;
        for (int i = 0; i < length; i++) {
        }
        if (count > 0) {
            select(indices);
            showIndex(indices[0]);
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
    public void setSortColumn(TableColumn column) {
        checkWidget();
        if (!java.util.Objects.equals(this.sortColumn, column)) {
            dirty();
        }
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
        checkWidget();
        if (!java.util.Objects.equals(this.sortDirection, direction)) {
            dirty();
        }
        if (direction != SWT.UP && direction != SWT.DOWN && direction != SWT.NONE)
            return;
        if (direction == sortDirection)
            return;
        setSort(sortColumn, direction);
    }

    void setSort(TableColumn column, int direction) {
        if (column != null) {
        }
        if (sortColumn != null && sortColumn != column) {
        }
        sortDirection = direction;
        sortColumn = column;
    }

    void setTableEmpty() {
        itemCount = 0;
        items = new TableItem[4];
        imageBounds = null;
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
        if (columnCount <= 1)
            return;
    }

    void showIndex(int index) {
        if (0 <= index && index < itemCount) {
        }
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
        int index = getSelectionIndex();
        if (index >= 0) {
            checkData(_getItem(index));
            showIndex(index);
        }
    }

    @Override
    void sendDoubleSelection() {
    }

    @Override
    void sendSelection() {
        if (ignoreSelect)
            return;
        {
            Event event = new Event();
            sendSelectionEvent(SWT.Selection, event, false);
        }
    }

    private void toggleCheckedItem(TableItem item, long rowIndex) {
        ((DartTableItem) item.getImpl()).checked = !((DartTableItem) item.getImpl()).checked;
        Event event = new Event();
        event.detail = SWT.CHECK;
        event.item = item;
        event.index = (int) rowIndex;
        sendSelectionEvent(SWT.Selection, event, false);
        ((DartTableItem) item.getImpl()).redraw(-1);
    }

    void handleClickSelected() {
        /*
	 * When there are multiple selected items and one of them is clicked
	 * without modifiers, macOS supports two cases:
	 * 1) For single click, all other items are deselected
	 * 2) For double-click, selection stays as is, allowing to create
	 *    double-click event with multiple items.
	 * In order to distinguish between the two, macOS delays (1) by
	 * [NSEvent doubleClickInterval] in order to see if it's case (2).
	 * This causes SWT.Selection to occur after SWT.MouseUp.
	 *
	 * Bug 289483: For consistent cross-platform behavior, we want
	 * SWT.Selection to occur before SWT.MouseUp. The workaround is to
	 * implement (1) in SWT code and ignore the delayed selection event.
	 */
        int clickedRow = selectedRowIndex;
        selectedRowIndex = -1;
        if (clickedRow == -1)
            return;
        if (dragDetected)
            return;
        // Bug 456602: It's possible that item is removed between mouse
        // down (where 'selectedRowIndex' was cached) and mouse up (current
        // code). In such case, all other items are still deselected, because
        // 1) without workaround, selection should have happened in mouse down,
        //    where item still existed
        // 2) clicking empty space deselects all items on macOS
        // If item is deleted, then pending selection is canceled by macOS, so
        // there's no need to ignore the next selection event.
        if (clickedRow >= itemCount)
            return;
        // Emulate SWT.Selection
        Event event = new Event();
        event.item = _getItem(clickedRow);
        sendSelectionEvent(SWT.Selection, event, false);
        // Ignore real SWT.Selection that will arrive later
        ignoreSelect = true;
    }

    @Override
    public void updateCursorRects(boolean enabled) {
        super.updateCursorRects(enabled);
    }

    void updateRowCount() {
        setRedraw(false);
        ignoreSelect = true;
        ignoreSelect = false;
        setRedraw(true);
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

    public TableColumn _sortColumn() {
        return sortColumn;
    }

    public TableItem _currentItem() {
        return currentItem;
    }

    public int _columnCount() {
        return columnCount;
    }

    public int _itemCount() {
        return itemCount;
    }

    public int _lastIndexOf() {
        return lastIndexOf;
    }

    public int _sortDirection() {
        return sortDirection;
    }

    public int _selectedRowIndex() {
        return selectedRowIndex;
    }

    public boolean _ignoreSelect() {
        return ignoreSelect;
    }

    public boolean _fixScrollWidth() {
        return fixScrollWidth;
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

    public double[] _headerBackground() {
        return headerBackground;
    }

    public double[] _headerForeground() {
        return headerForeground;
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
