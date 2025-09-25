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
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cairo.*;
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
 * <dd>SINGLE, MULTI, CHECK, FULL_SELECTION, VIRTUAL, NO_SCROLL</dd>
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

    long modelHandle, checkRenderer;

    int columnCount, sortDirection;

    int selectionCountOnPress, selectionCountOnRelease;

    long ignoreCell;

    TreeItem[] items;

    int nextId;

    TreeColumn[] columns;

    TreeColumn sortColumn;

    TreeItem currentItem;

    boolean firstCustomDraw;

    /**
     * True iff computeSize has never been called on this Tree
     */
    boolean firstCompute = true;

    boolean modelChanged;

    boolean expandAll;

    int drawState, drawFlags;

    /**
     * The owner of the widget is responsible for drawing
     */
    boolean isOwnerDrawn;

    boolean ignoreSize, pixbufSizeSet, hasChildren;

    int pixbufHeight, pixbufWidth, headerHeight;

    boolean headerVisible;

    TreeItem topItem;

    double cachedAdjustment, currentAdjustment;

    Color headerBackground, headerForeground;

    boolean boundsChangedSinceLastDraw, wasScrolled;

    boolean rowActivated;

    private long headerCSSProvider;

    static final int ID_COLUMN = 0;

    static final int CHECKED_COLUMN = 1;

    static final int GRAYED_COLUMN = 2;

    static final int FOREGROUND_COLUMN = 3;

    static final int BACKGROUND_COLUMN = 4;

    static final int FONT_COLUMN = 5;

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
        if (!isOwnerDrawn) {
            switch(eventType) {
                case SWT.MeasureItem:
                case SWT.EraseItem:
                case SWT.PaintItem:
                    isOwnerDrawn = true;
                    recreateRenderers();
                    break;
            }
        }
    }

    TreeItem _getItem(long iter) {
        int id = getId(iter, true);
        if (items[id] != null)
            return items[id];
        return items[id];
    }

    TreeItem _getItem(long parentIter, long iter, int index) {
        int id = getId(iter, true);
        if (items[id] != null)
            return items[id];
        return items[id] = new TreeItem(this.getApi(), parentIter, SWT.NONE, index, iter);
    }

    void reallocateIds(int newSize) {
        TreeItem[] newItems = new TreeItem[newSize];
        System.arraycopy(items, 0, newItems, 0, items.length);
        items = newItems;
    }

    int findAvailableId() {
        // Adapt to cases where items[] array was resized since last search
        // This also fixes cases where +1 below went too far
        if (nextId >= items.length)
            nextId = 0;
        // Search from 'nextId' to end
        for (int id = nextId; id < items.length; id++) {
            if (items[id] == null)
                return id;
        }
        // Search from begin to nextId
        for (int id = 0; id < nextId; id++) {
            if (items[id] == null)
                return id;
        }
        // Still not found; no empty spots remaining
        int newId = items.length;
        if (drawCount <= 0) {
            reallocateIds(items.length + 4);
        } else {
            // '.setRedraw(false)' is typically used during bulk operations.
            // Reallocate to 1.5x the old size to avoid frequent reallocations.
            reallocateIds((items.length + 1) * 3 / 2);
        }
        return newId;
    }

    int getId(long iter, boolean queryModel) {
        if (queryModel) {
            int[] value = new int[1];
            if (value[0] != -1)
                return value[0];
        }
        int id = findAvailableId();
        nextId = id + 1;
        return id;
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
        TreeItem item = _getItem(iter);
        int modelIndex = -1;
        boolean customDraw = false;
        if (columnCount == 0) {
            modelIndex = DartTree.FIRST_COLUMN;
            customDraw = firstCustomDraw;
        } else {
            TreeColumn column = (TreeColumn) ((SwtDisplay) display.getImpl()).getWidget(tree_column);
            if (column != null) {
                modelIndex = ((DartTreeColumn) column.getImpl()).modelIndex;
                customDraw = ((DartTreeColumn) column.getImpl()).customDraw;
            }
        }
        if (modelIndex == -1)
            return 0;
        boolean setData = false;
        boolean updated = false;
        if ((getApi().style & SWT.VIRTUAL) != 0) {
            if (!((DartTreeItem) item.getImpl()).cached) {
                //lastIndexOf = index [0];
                setData = checkData(item);
            }
            if (((DartTreeItem) item.getImpl()).updated) {
                updated = true;
                ((DartTreeItem) item.getImpl()).updated = false;
            }
        }
        long[] ptr = new long[1];
        if (setData) {
        }
        if (customDraw) {
            if (!isOwnerDrawn) {
                ptr[0] = 0;
                if (ptr[0] != 0) {
                }
            }
        }
        if (setData || updated) {
            ignoreCell = cell;
            setScrollWidth(tree_column, item);
            ignoreCell = 0;
        }
        return 0;
    }

    boolean checkData(TreeItem item) {
        if (((DartTreeItem) item.getImpl()).cached)
            return true;
        if ((getApi().style & SWT.VIRTUAL) != 0) {
            ((DartTreeItem) item.getImpl()).cached = true;
            TreeItem parentItem = item.getParentItem();
            Event event = new Event();
            event.item = item;
            event.index = parentItem == null ? indexOf(item) : parentItem.indexOf(item);
            currentItem = item;
            ((DartTreeItem) item.getImpl()).settingData = true;
            sendEvent(SWT.SetData, event);
            ((DartTreeItem) item.getImpl()).settingData = false;
            currentItem = null;
            //widget could be disposed at this point
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

    int calculateWidth(long column, long iter, boolean recurse) {
        /*
	* Bug in GTK.  The width calculated by gtk_tree_view_column_cell_get_size()
	* always grows in size regardless of the text or images in the table.
	* The fix is to determine the column width from the cell renderers.
	*/
        // Code intentionally commented
        //int [] width = new int [1];
        //OS.gtk_tree_view_column_cell_get_size (column, null, null, null, width, null);
        //return width [0];
        int width = 0;
        if (recurse) {
        }
        return width;
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
        clear(0, index, all);
    }

    void clear(long parentIter, int index, boolean all) {
        int[] value = new int[1];
        if (value[0] != -1) {
            TreeItem item = items[value[0]];
            ((DartTreeItem) item.getImpl()).clear();
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
        clearAll(all, 0);
    }

    void clearAll(boolean all, long parentIter) {
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    void copyModel(long oldModel, int oldStart, long newModel, int newStart, long oldParent, long newParent, int modelLength) {
    }

    void createColumn(TreeColumn column, int index) {
        /*
* Bug in ATK. For some reason, ATK segments fault if
* the GtkTreeView has a column and does not have items.
* The fix is to insert the column only when an item is
* created.
*/
        int modelIndex = FIRST_COLUMN;
        if (columnCount != 0) {
            for (int i = 0; i < columnCount; i++) {
                for (int j = 0; j < CELL_TYPES; j++) {
                }
            }
        }
        if (index == 0 && columnCount > 0) {
            TreeColumn checkColumn = columns[0];
            createRenderers(checkColumn.handle, ((DartTreeColumn) checkColumn.getImpl()).modelIndex, false, checkColumn.style);
        }
        if ((getApi().style & SWT.VIRTUAL) == 0 && columnCount == 0) {
        } else {
        }
        if (column != null) {
            ((DartTreeColumn) column.getImpl()).modelIndex = modelIndex;
        }
        if (!searchEnabled()) {
        } else {
        }
    }

    @Override
    void createHandle(int index) {
    }

    /**
     * Binds the left and right arrow keys to
     * allow for expanding and collapsing of the
     * tree nodes.
     *
     * Note: This function is to only be called in GTK4.
     * Binding of the arrow keys are also done in GTK3,
     * however it is done through GtkBindingSets in CSS.
     * See Device.init() for more information, specifically,
     * swt_functional_gtk_3_20.css
     */
    void bindArrowKeyBindings() {
    }

    @Override
    int applyThemeBackground() {
        return -1;
        /* No Change */
    }

    void createItem(TreeColumn column, int index) {
        if (!(0 <= index && index <= columnCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (index == 0) {
            // first column must be left aligned
            column.style &= ~(SWT.LEFT | SWT.RIGHT | SWT.CENTER);
            column.style |= SWT.LEFT;
        }
        if (columnCount == 0) {
            ((DartTreeColumn) column.getImpl()).modelIndex = FIRST_COLUMN;
            createRenderers(column.handle, ((DartTreeColumn) column.getImpl()).modelIndex, true, column.style);
            ((DartTreeColumn) column.getImpl()).customDraw = firstCustomDraw;
            firstCustomDraw = false;
        } else {
            createColumn(column, index);
        }
        if (columnCount == columns.length) {
            TreeColumn[] newColumns = new TreeColumn[columns.length + 4];
            System.arraycopy(columns, 0, newColumns, 0, columns.length);
            columns = newColumns;
        }
        System.arraycopy(columns, index, columns, index + 1, columnCount++ - index);
        columns[index] = column;
        if ((getApi().state & FONT) != 0) {
            long fontDesc = getFontDescription();
            ((DartTreeColumn) column.getImpl()).setFontDescription(fontDesc);
        }
        if (columnCount >= 1) {
            for (int i = 0; i < items.length; i++) {
                TreeItem item = items[i];
                if (item != null) {
                    Font[] cellFont = ((DartTreeItem) item.getImpl()).cellFont;
                    if (cellFont != null) {
                        Font[] temp = new Font[columnCount];
                        System.arraycopy(cellFont, 0, temp, 0, index);
                        System.arraycopy(cellFont, index, temp, index + 1, columnCount - index - 1);
                        ((DartTreeItem) item.getImpl()).cellFont = temp;
                    }
                    String[] strings = ((DartTreeItem) item.getImpl()).strings;
                    if (strings != null) {
                        String[] temp = new String[columnCount];
                        System.arraycopy(strings, 0, temp, 0, index);
                        System.arraycopy(strings, index, temp, index + 1, columnCount - index - 1);
                        temp[index] = "";
                        ((DartTreeItem) item.getImpl()).strings = temp;
                    }
                }
            }
        }
        updateHeaderCSS();
    }

    /**
     * The fastest way to insert many items is documented in {@link TreeItem#TreeItem(org.eclipse.swt.widgets.Tree,int,int)}
     * and {@link TreeItem#setItemCount}
     */
    void createItem(TreeItem item, long parentIter, int index) {
        int id = getId(item.handle, false);
        items[id] = item;
    }

    void createRenderers(long columnHandle, int modelIndex, boolean check, int columnStyle) {
        if ((getApi().style & SWT.CHECK) != 0 && check) {
            if (isOwnerDrawn) {
            }
        }
        if (isOwnerDrawn) {
        }
        /*
	* Feature in GTK.  When a tree view column contains only one activatable
	* cell renderer such as a toggle renderer, mouse clicks anywhere in a cell
	* activate that renderer. The workaround is to set a second  cell renderer
	* to be activatable.
	*/
        if ((getApi().style & SWT.CHECK) != 0 && check) {
        }
        /* Set alignment */
        if ((columnStyle & SWT.RIGHT) != 0) {
        } else if ((columnStyle & SWT.CENTER) != 0) {
        } else {
        }
        if (!isOwnerDrawn) {
        }
        boolean customDraw = firstCustomDraw;
        if (columnCount != 0) {
            for (int i = 0; i < columnCount; i++) {
                if (columns[i].handle == columnHandle) {
                    customDraw = ((DartTreeColumn) columns[i].getImpl()).customDraw;
                    break;
                }
            }
        }
        if ((getApi().style & SWT.VIRTUAL) != 0 || customDraw || isOwnerDrawn) {
        }
    }

    @Override
    void createWidget(int index) {
        super.createWidget(index);
        items = new TreeItem[4];
        columns = new TreeColumn[4];
        columnCount = 0;
        // In GTK 3 font description is inherited from parent widget which is not how SWT has always worked,
        // reset to default font to get the usual behavior
        setFontDescription(defaultFont().handle);
    }

    @Override
    void deregister() {
        super.deregister();
        if (checkRenderer != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(checkRenderer);
        ((SwtDisplay) display.getImpl()).removeWidget(modelHandle);
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
        boolean fixColumn = showFirstColumn();
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
        selection = new TreeItem[0];
    }

    void destroyItem(TreeColumn column) {
        int index = 0;
        while (index < columnCount) {
            if (columns[index] == column)
                break;
            index++;
        }
        if (index == columnCount)
            return;
        if (columnCount == 1) {
            firstCustomDraw = ((DartTreeColumn) column.getImpl()).customDraw;
        }
        System.arraycopy(columns, index + 1, columns, index, --columnCount - index);
        columns[columnCount] = null;
        if (columnCount == 0) {
            createColumn(null, 0);
        } else {
            for (int i = 0; i < items.length; i++) {
                TreeItem item = items[i];
                if (item != null) {
                    int modelIndex = ((DartTreeColumn) column.getImpl()).modelIndex;
                    Font[] cellFont = ((DartTreeItem) item.getImpl()).cellFont;
                    if (cellFont != null) {
                        if (columnCount == 0) {
                            ((DartTreeItem) item.getImpl()).cellFont = null;
                        } else {
                            Font[] temp = new Font[columnCount];
                            System.arraycopy(cellFont, 0, temp, 0, index);
                            System.arraycopy(cellFont, index + 1, temp, index, columnCount - index);
                            ((DartTreeItem) item.getImpl()).cellFont = temp;
                        }
                    }
                }
            }
            if (index == 0) {
                // first column must be left aligned and must show check box
                TreeColumn firstColumn = columns[0];
                firstColumn.style &= ~(SWT.LEFT | SWT.RIGHT | SWT.CENTER);
                firstColumn.style |= SWT.LEFT;
                createRenderers(firstColumn.handle, ((DartTreeColumn) firstColumn.getImpl()).modelIndex, true, firstColumn.style);
            }
        }
        if (!searchEnabled()) {
        } else {
        }
    }

    void destroyItem(TreeItem item) {
        modelChanged = true;
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
        if (columnCount == 0)
            return new int[0];
        return this.columnOrder;
    }

    long[] getColumnTypes(int columnCount) {
        long[] types = new long[FIRST_COLUMN + (columnCount * CELL_TYPES)];
        // per cell data
        for (int i = FIRST_COLUMN; i < types.length; i += CELL_TYPES) {
        }
        return types;
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

    TreeItem getFocusItem() {
        long[] path = new long[1];
        if (path[0] == 0)
            return null;
        TreeItem item = null;
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
     *
     * @since 3.1
     */
    public int getGridLineWidth() {
        checkWidget();
        return DPIUtil.autoScaleDown(getGridLineWidthInPixels());
    }

    int getGridLineWidthInPixels() {
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
     * @since 3.1
     */
    public int getHeaderHeight() {
        checkWidget();
        return DPIUtil.autoScaleDown(getHeaderHeightInPixels());
    }

    int getHeaderHeightInPixels() {
        checkWidget();
        int height = 0;
        if (columnCount > 0) {
            for (int i = 0; i < columnCount; i++) {
                long buttonHandle = ((DartTreeColumn) columns[i].getImpl()).buttonHandle;
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
        return getItemInPixels(DPIUtil.autoScaleUp(point));
    }

    TreeItem getItemInPixels(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        long[] path = new long[1];
        int x = point.x;
        if ((getApi().style & SWT.MIRRORED) != 0)
            x = getClientWidth() - x;
        if (path[0] == 0)
            return null;
        TreeItem item = null;
        return item;
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
        return DPIUtil.autoScaleDown(getItemHeightInPixels());
    }

    int getItemHeightInPixels() {
        checkWidget();
        int height = 0;
        return height;
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

    TreeItem[] getItems(long parent) {
        ArrayList<TreeItem> result = new ArrayList<>();
        try {
        } finally {
        }
        return result.toArray(new TreeItem[result.size()]);
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

    long getPixbufRenderer(long column) {
        long pixbufRenderer = 0;
        return pixbufRenderer;
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

    long getTextRenderer(long column) {
        long textRenderer = 0;
        return textRenderer;
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
        TreeItem item = null;
        if (cachedAdjustment == currentAdjustment) {
            item = _getCachedTopItem();
        }
        /*
	 * Bug 501420: check to make sure the item is not disposed before returning
	 * it. If it is, find the topItem using GtkTreeView API.
	 */
        if (item != null && !item.isDisposed()) {
            return item;
        }
        // Use GTK method to get topItem if there has been changes to the vAdjustment
        long[] path = new long[1];
        if (path[0] == 0)
            return null;
        item = null;
        topItem = item;
        return item;
    }

    TreeItem _getCachedTopItem() {
        return null;
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
        TreeItem treeItem = getFocusItem();
        if (treeItem == null)
            return;
        Event event = new Event();
        event.item = treeItem;
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
     *
     * @since 3.1
     */
    public int indexOf(TreeColumn column) {
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
        int index = -1;
        return index;
    }

    @Override
    boolean mnemonicHit(char key) {
        for (int i = 0; i < columnCount; i++) {
            long labelHandle = ((DartTreeColumn) columns[i].getImpl()).labelHandle;
            if (labelHandle != 0 && mnemonicHit(labelHandle, key))
                return true;
        }
        return false;
    }

    @Override
    boolean mnemonicMatch(char key) {
        for (int i = 0; i < columnCount; i++) {
            long labelHandle = ((DartTreeColumn) columns[i].getImpl()).labelHandle;
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
                TreeColumn column = columns[i];
                createRenderers(column.handle, ((DartTreeColumn) column.getImpl()).modelIndex, i == 0, column.style);
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

    void releaseItem(TreeItem item, boolean release) {
    }

    void releaseItems(long parentIter) {
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                TreeItem item = items[i];
                if (item != null && !item.isDisposed()) {
                    item.getImpl().release(false);
                }
            }
            items = null;
        }
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
    void releaseWidget() {
        super.releaseWidget();
        modelHandle = 0;
        checkRenderer = 0;
        currentItem = null;
    }

    void remove(long parentIter, int start, int end) {
        if (start > end)
            return;
        try {
            for (int i = start; i <= end; i++) {
                int[] value = new int[1];
                TreeItem item = value[0] != -1 ? items[value[0]] : null;
                if (item != null && !item.isDisposed()) {
                    /*
				 * Bug 182598 - assertion failed in gtktreestore.c
				 * Removing an item while its data is being set will invalidate
				 * it, which will cause a crash in GTK.
				 */
                    if (((DartTreeItem) item.getImpl()).settingData) {
                        throwCannotRemoveItem(i);
                    }
                    item.dispose();
                } else {
                }
            }
        } finally {
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
        checkSetDataInProcessBeforeRemoval();
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            if (item != null && !item.isDisposed())
                item.getImpl().release(false);
        }
        items = new TreeItem[4];
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
        TreeItem item = null;
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
        if ((getApi().style & SWT.VIRTUAL) != 0 || isOwnerDrawn)
            return;
        int end = Math.max(1, columnCount);
        for (int i = 0; i < end; i++) {
            boolean customDraw = columnCount != 0 ? ((DartTreeColumn) columns[i].getImpl()).customDraw : firstCustomDraw;
            if (customDraw) {
                if (columnCount != 0)
                    ((DartTreeColumn) columns[i].getImpl()).customDraw = false;
            }
        }
        firstCustomDraw = false;
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

    boolean searchEnabled() {
        /* Disable searching when using VIRTUAL */
        if ((getApi().style & SWT.VIRTUAL) != 0)
            return false;
        return true;
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
        if (item == null) {
            return;
        }
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (((DartTreeItem) item.getImpl()).parent != this.getApi())
            return;
        long[] path = new long[1];
        if (path[0] == 0)
            return;
    }

    void setItemCount(long parentIter, int count) {
        boolean isVirtual = (getApi().style & SWT.VIRTUAL) != 0;
        if (!isVirtual)
            setRedraw(false);
        if (parentIter == 0 && count == 0) {
            removeAll();
        } else {
        }
        if (isVirtual) {
            {
            }
        } else {
        }
        if (!isVirtual)
            setRedraw(true);
        modelChanged = true;
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
        setItemCount(0, count);
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
        boolean fixColumn = showFirstColumn();
        if (fixColumn)
            hideFirstColumn();
        this.selection = new TreeItem[] { item };
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
        items = TreeHelper.collectAllItems(this);
        selection = items;
    }

    @Override
    void setBackgroundSurface(Image image) {
        isOwnerDrawn = true;
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
        long baseColumn = 0;
        for (int i = 0; i < order.length; i++) {
            long column = columns[order[i]].handle;
            baseColumn = column;
        }
        this.columnOrder = order;
    }

    @Override
    void setFontDescription(long font) {
        super.setFontDescription(font);
        TreeColumn[] columns = getColumns();
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] != null) {
                ((DartTreeColumn) columns[i].getImpl()).setFontDescription(font);
            }
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
        dirty();
        checkWidget();
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
            for (TreeColumn column : columns) {
                if (column != null) {
                    ((DartTreeColumn) column.getImpl()).setHeaderCSS(css.toString());
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
     *
     * @since 3.1
     */
    public void setHeaderVisible(boolean show) {
        dirty();
        checkWidget();
        this.headerHeight = this.getHeaderHeight();
        this.headerVisible = show;
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

    void setModel(long newModel) {
        ((SwtDisplay) display.getImpl()).removeWidget(modelHandle);
        modelHandle = newModel;
        ((SwtDisplay) display.getImpl()).addWidget(modelHandle, this.getApi());
    }

    @Override
    void setOrientation(boolean create) {
        super.setOrientation(create);
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null)
                    ((DartWidget) items[i].getImpl()).setOrientation(create);
            }
        }
        if (columns != null) {
            for (int i = 0; i < columns.length; i++) {
                if (columns[i] != null)
                    ((DartTreeColumn) columns[i].getImpl()).setOrientation(create);
            }
        }
    }

    @Override
    void setParentBackground() {
        isOwnerDrawn = true;
        recreateRenderers();
    }

    void setScrollWidth(long column, TreeItem item) {
        if (columnCount != 0 || currentItem == item)
            return;
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
        deselectAll();
        int length = items.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))
            return;
        boolean fixColumn = showFirstColumn();
        boolean first = true;
        for (int i = 0; i < length; i++) {
            TreeItem item = items[i];
            if (item == null)
                continue;
            if (item.isDisposed())
                break;
            if (((DartTreeItem) item.getImpl()).parent != this.getApi())
                continue;
            if (first) {
            }
            first = false;
        }
        if (fixColumn)
            hideFirstColumn();
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
        dirty();
        checkWidget();
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
        topItem = item;
        if (item == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (item.isDisposed())
            error(SWT.ERROR_INVALID_ARGUMENT);
        if (((DartTreeItem) item.getImpl()).parent != this.getApi())
            return;
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
        TreeItem[] items = getSelection();
        if (items.length != 0 && items[0] != null)
            showItem(items[0]);
    }

    void showItem(long path, boolean scroll) {
        if (scroll) {
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
        if (((DartTreeItem) item.getImpl()).parent != this.getApi())
            return;
    }

    @Override
    void updateScrollBarValue(ScrollBar bar) {
        super.updateScrollBarValue(bar);
    }

    /**
     * Check the tree for items that are in process of
     * sending {@code SWT#SetData} event. If such items exist, throw an exception.
     *
     * Does nothing if the given range contains no indices,
     * or if we are below GTK 3.22.0 or are using GTK 4.
     */
    void checkSetDataInProcessBeforeRemoval() {
        /*
	 * Bug 182598 - assertion failed in gtktreestore.c
	 *
	 * To prevent a crash in GTK, we ensure we are not setting data on the tree items we are about to remove.
	 * Removing an item while its data is being set will invalidate it, which will cause a crash.
	 *
	 * We therefore throw an exception to prevent the crash.
	 */
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            if (item != null && ((DartTreeItem) item.getImpl()).settingData) {
                throwCannotRemoveItem(i);
            }
        }
    }

    private void throwCannotRemoveItem(int i) {
        String message = "Cannot remove item with index " + i + ".";
        throw new SWTException(message);
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

    TreeItem[] selection = new TreeItem[0];

    public long _modelHandle() {
        return modelHandle;
    }

    public long _checkRenderer() {
        return checkRenderer;
    }

    public int _columnCount() {
        return columnCount;
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

    public TreeItem[] _items() {
        return items;
    }

    public int _nextId() {
        return nextId;
    }

    public TreeColumn[] _columns() {
        return columns;
    }

    public TreeColumn _sortColumn() {
        return sortColumn;
    }

    public TreeItem _currentItem() {
        return currentItem;
    }

    public boolean _firstCustomDraw() {
        return firstCustomDraw;
    }

    public boolean _firstCompute() {
        return firstCompute;
    }

    public boolean _modelChanged() {
        return modelChanged;
    }

    public boolean _expandAll() {
        return expandAll;
    }

    public int _drawState() {
        return drawState;
    }

    public int _drawFlags() {
        return drawFlags;
    }

    public boolean _isOwnerDrawn() {
        return isOwnerDrawn;
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

    public int _pixbufHeight() {
        return pixbufHeight;
    }

    public int _pixbufWidth() {
        return pixbufWidth;
    }

    public int _headerHeight() {
        return headerHeight;
    }

    public boolean _headerVisible() {
        return headerVisible;
    }

    public TreeItem _topItem() {
        return topItem;
    }

    public double _cachedAdjustment() {
        return cachedAdjustment;
    }

    public double _currentAdjustment() {
        return currentAdjustment;
    }

    public Color _headerBackground() {
        return headerBackground;
    }

    public Color _headerForeground() {
        return headerForeground;
    }

    public boolean _boundsChangedSinceLastDraw() {
        return boundsChangedSinceLastDraw;
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

    public TreeItem[] _selection() {
        return selection;
    }

    public void updateChildItems() {
        if (items == null)
            return;
        items = java.util.Arrays.stream(items).filter(child -> child != null && !child.isDisposed()).toArray(TreeItem[]::new);
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
