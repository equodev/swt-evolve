/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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
 * Instances of this class represent a selectable user interface
 * object that displays a list of strings and issues notification
 * when a string is selected.  A list may be single or multi select.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SINGLE, MULTI, NO_SEARCH</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection</dd>
 * </dl>
 * <p>
 * Note: Only one of SINGLE and MULTI may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#list">List snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartList extends DartScrollable implements IList {

    static final int INSET = 3;

    // indicates whether Bidi UCC were added; 'state & HAS_AUTO_DIRECTION' isn't a sufficient indicator
    boolean addedUCC = false;

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
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartList(Composite parent, int style, List api) {
        super(parent, checkStyle(style), api);
    }

    /**
     * Adds the argument to the end of the receiver's list.
     * <p>
     * Note: If control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
     * @param string the new item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #add(String,int)
     */
    public void add(String string) {
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
    }

    /**
     * Adds the argument to the receiver's list at the given
     * zero-relative index.
     * <p>
     * Note: To add an item at the end of the list, use the
     * result of calling <code>getItemCount()</code> as the
     * index or use <code>add(String)</code>.
     * </p><p>
     * Also note, if control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
     *
     * @param string the new item
     * @param index the index for the item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #add(String)
     */
    public void add(String string, int index) {
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (index == -1)
            error(SWT.ERROR_INVALID_RANGE);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the user changes the receiver's selection, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is called when the selection changes.
     * <code>widgetDefaultSelected</code> is typically called when an item is double-clicked.
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
        return checkBits(style, SWT.SINGLE, SWT.MULTI, 0, 0, 0, 0);
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.computeSize(this, wHint, hHint, changed);
    }

    @Override
    int defaultBackground() {
        return 0;
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
        if ((getApi().style & SWT.SINGLE) != 0) {
            for (int index : indices) {
            }
            return;
        }
        for (int index : indices) {
            if (index != -1) {
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
        if (index == -1)
            return;
        if ((getApi().style & SWT.SINGLE) != 0) {
            return;
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
        if ((getApi().style & SWT.SINGLE) != 0) {
            return;
        }
        if (start < 0 && end < 0)
            return;
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
        }
        selection = new int[0];
    }

    /**
     * Returns the zero-relative index of the item which currently
     * has the focus in the receiver, or -1 if no item has focus.
     *
     * @return the index of the selected item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getFocusIndex() {
        checkWidget();
        return 0;
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
    public String getItem(int index) {
        checkWidget();
        if (items == null || index < 0 || index >= items.length) {
            error(SWT.ERROR_INVALID_RANGE);
        }
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
        return this.items != null ? this.items.length : 0;
    }

    /**
     * Returns the height of the area which would be used to
     * display <em>one</em> of the items in the list.
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
     * Returns a (possibly empty) array of <code>String</code>s which
     * are the items in the receiver.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of items, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return the items in the receiver's list
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String[] getItems() {
        checkWidget();
        int count = getItemCount();
        String[] result = new String[count];
        for (int i = 0; i < count; i++) result[i] = getItem(i);
        return result;
    }

    /**
     * Returns an array of <code>String</code>s that are currently
     * selected in the receiver.  The order of the items is unspecified.
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
    public String[] getSelection() {
        checkWidget();
        int[] indices = getSelectionIndices();
        String[] result = new String[indices.length];
        for (int i = 0; i < indices.length; i++) {
            result[i] = getItem(indices[i]);
        }
        return result;
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
        if ((getApi().style & SWT.SINGLE) != 0) {
            return 1;
        }
        return this.selection != null ? this.selection.length : 0;
    }

    /**
     * Returns the zero-relative index of the item which is currently
     * selected in the receiver, or -1 if no item is selected.
     *
     * @return the index of the selected item or -1
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getSelectionIndex() {
        checkWidget();
        if ((getApi().style & SWT.SINGLE) != 0) {
        }
        int[] buffer = new int[1];
        return buffer[0];
    }

    /**
     * Returns the zero-relative indices of the items which are currently
     * selected in the receiver.  The order of the indices is unspecified.
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        }
        return this.selection;
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
     * Gets the index of an item.
     * <p>
     * The list is searched starting at 0 until an
     * item is found that is equal to the search item.
     * If no item is found, -1 is returned.  Indexing
     * is zero based.
     *
     * @param string the search item
     * @return the index of the item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int indexOf(String string) {
        return indexOf(string, 0);
    }

    /**
     * Searches the receiver's list starting at the given,
     * zero-relative index until an item is found that is equal
     * to the argument, and returns the index of that item. If
     * no item is found or the starting index is out of range,
     * returns -1.
     *
     * @param string the search item
     * @param start the zero-relative index at which to start the search
     * @return the index of the item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int indexOf(String string, int start) {
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        /*
	* Bug in Windows.  For some reason, LB_FINDSTRINGEXACT
	* will not find empty strings even though it is legal
	* to insert an empty string into a list.  The fix is
	* to search the list, an item at a time.
	*/
        if (string.length() == 0) {
            int count = getItemCount();
            for (int i = start; i < count; i++) {
                if (string.equals(getItem(i)))
                    return i;
            }
            return -1;
        }
        int index = start - 1;
        do {
        } while (!string.equals(getItem(index)));
        return index;
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
        return super.isUseWsBorder() || ((display != null) && ((SwtDisplay) display.getImpl()).useWsBorderList);
    }

    /**
     * Removes the items from the receiver at the given
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
        int newWidth = 0;
        if ((getApi().style & SWT.H_SCROLL) != 0) {
        }
        int i = 0, topCount = 0, last = -1;
        while (i < newIndices.length) {
            int index = newIndices[i];
            if (index != last) {
                char[] buffer = null;
                int length = 0;
                if ((getApi().style & SWT.H_SCROLL) != 0) {
                    buffer = new char[length + 1];
                }
                if ((getApi().style & SWT.H_SCROLL) != 0) {
                }
                last = index;
            }
            i++;
        }
        if ((getApi().style & SWT.H_SCROLL) != 0) {
            setScrollWidth(newWidth, false);
        }
        if (topCount > 0) {
        }
        if (i < newIndices.length)
            error(SWT.ERROR_ITEM_NOT_REMOVED);
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
        char[] buffer = null;
        if ((getApi().style & SWT.H_SCROLL) != 0) {
        }
        if ((getApi().style & SWT.H_SCROLL) != 0)
            setScrollWidth(buffer, false);
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
        int newWidth = 0;
        if ((getApi().style & SWT.H_SCROLL) != 0) {
        }
        int index = start;
        while (index <= end) {
            char[] buffer = null;
            int length = 0;
            if ((getApi().style & SWT.H_SCROLL) != 0) {
                buffer = new char[length + 1];
            }
            if ((getApi().style & SWT.H_SCROLL) != 0) {
            }
            index++;
        }
        if ((getApi().style & SWT.H_SCROLL) != 0) {
            setScrollWidth(newWidth, false);
        }
        if (index <= end)
            error(SWT.ERROR_ITEM_NOT_REMOVED);
    }

    /**
     * Searches the receiver's list starting at the first item
     * until an item is found that is equal to the argument,
     * and removes that item from the list.
     *
     * @param string the item to remove
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the string is not found in the list</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void remove(String string) {
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        int index = indexOf(string, 0);
        if (index == -1)
            error(SWT.ERROR_INVALID_ARGUMENT);
        remove(index);
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
        if ((getApi().style & SWT.H_SCROLL) != 0) {
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

    /**
     * Selects the items at the given zero-relative indices in the receiver.
     * The current selection is not cleared before the new items are selected.
     * <p>
     * If the item at a given index is not selected, it is selected.
     * If the item at a given index was already selected, it remains selected.
     * Indices that are out of range and duplicate indices are ignored.
     * If the receiver is single-select and multiple indices are specified,
     * then all indices are ignored.
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
     * @see List#setSelection(int[])
     */
    public void select(int[] indices) {
        checkWidget();
        if (indices == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        int length = indices.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))
            return;
        select(indices, false);
    }

    void select(int[] indices, boolean scroll) {
        int[] newValue = indices;
        if (!java.util.Objects.equals(this.selection, newValue)) {
            dirty();
        }
        int i = 0;
        while (i < indices.length) {
            int index = indices[i];
            if (index != -1) {
                select(index, false);
            }
            i++;
        }
        this.selection = newValue;
        if (scroll)
            showSelection();
    }

    /**
     * Selects the item at the given zero-relative index in the receiver's
     * list.  If the item at the index was already selected, it remains
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
        checkWidget();
        select(index, false);
    }

    void select(int index, boolean scroll) {
        dirty();
        int[] newValue = new int[] { index };
        if (index < 0)
            return;
        if (scroll) {
            if ((getApi().style & SWT.SINGLE) != 0) {
            } else {
            }
            return;
        }
        int focusIndex = -1;
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        if ((getApi().style & SWT.MULTI) != 0) {
            if (focusIndex != -1) {
            }
        }
        this.selection = newValue;
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
     *
     * @param start the start of the range
     * @param end the end of the range
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see List#setSelection(int,int)
     */
    public void select(int start, int end) {
        checkWidget();
        if (end < 0 || start > end || ((getApi().style & SWT.SINGLE) != 0 && start != end))
            return;
        start = Math.max(0, start);
        if ((getApi().style & SWT.SINGLE) != 0) {
            select(start, false);
        } else {
            select(start, end, false);
        }
    }

    void select(int start, int end, boolean scroll) {
        dirty();
        int[] newValue = new int[] { start };
        /*
	* Note that when start = end, LB_SELITEMRANGEEX
	* deselects the item.
	*/
        if (start == end) {
            select(start, scroll);
            return;
        }
        this.selection = newValue;
        if (scroll)
            showSelection();
    }

    /**
     * Selects all of the items in the receiver.
     * <p>
     * If the receiver is single-select, do nothing.
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
    }

    void setFocusIndex(int index) {
    }

    @Override
    public void setFont(Font font) {
        dirty();
        checkWidget();
        super.setFont(font);
        if ((getApi().style & SWT.H_SCROLL) != 0)
            setScrollWidth();
    }

    /**
     * Sets the text of the item in the receiver's list at the given
     * zero-relative index to the string argument.
     *
     * @param index the index for the item
     * @param string the new text for the item
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setItem(int index, String string) {
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        int topIndex = getTopIndex();
        boolean isSelected = isSelected(index);
        remove(index);
        add(string, index);
        if (isSelected)
            select(index, false);
        setTopIndex(topIndex);
    }

    /**
     * Sets the receiver's items to be the given array of items.
     *
     * @param items the array of items
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the items array is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if an item in the items array is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setItems(String... items) {
        dirty();
        String[] newValue = items;
        checkWidget();
        if (items == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        for (String item : items) {
            if (item == null)
                error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if ((getApi().style & SWT.H_SCROLL) != 0) {
        }
        int length = items.length;
        int index = 0;
        while (index < length) {
            if ((getApi().style & SWT.H_SCROLL) != 0) {
            }
            index++;
        }
        if ((getApi().style & SWT.H_SCROLL) != 0) {
        }
        this.items = newValue;
        if (index < items.length)
            error(SWT.ERROR_ITEM_NOT_ADDED);
    }

    /**
     * Calculates the scroll width depending on the item with the highest width
     */
    void setScrollWidth() {
    }

    void setScrollWidth(char[] buffer, boolean grow) {
    }

    void setScrollWidth(int newWidth, boolean grow) {
        newWidth += INSET;
        if (grow) {
        } else {
            setScrollWidth();
        }
    }

    /**
     * Selects the items at the given zero-relative indices in the receiver.
     * The current selection is cleared before the new items are selected,
     * and if necessary the receiver is scrolled to make the new selection visible.
     * <p>
     * Indices that are out of range and duplicate indices are ignored.
     * If the receiver is single-select and multiple indices are specified,
     * then all indices are ignored.
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
     * @see List#deselectAll()
     * @see List#select(int[])
     */
    public void setSelection(int[] indices) {
        checkWidget();
        if (indices == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        deselectAll();
        int length = indices.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))
            return;
        select(indices, true);
        if ((getApi().style & SWT.MULTI) != 0) {
            int focusIndex = indices[0];
            if (focusIndex >= 0)
                setFocusIndex(focusIndex);
        }
    }

    /**
     * Sets the receiver's selection to be the given array of items.
     * The current selection is cleared before the new items are selected,
     * and if necessary the receiver is scrolled to make the new selection visible.
     * <p>
     * Items that are not in the receiver are ignored.
     * If the receiver is single-select and multiple items are specified,
     * then all items are ignored.
     *
     * @param items the array of items
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the array of items is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see List#deselectAll()
     * @see List#select(int[])
     * @see List#setSelection(int[])
     */
    public void setSelection(String[] items) {
        checkWidget();
        if (items == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        deselectAll();
        int length = items.length;
        if (length == 0 || ((getApi().style & SWT.SINGLE) != 0 && length > 1))
            return;
        int focusIndex = -1;
        for (int i = length - 1; i >= 0; --i) {
            String string = items[i];
            int index = 0;
            if (string != null) {
                int localFocus = -1;
                while ((index = indexOf(string, index)) != -1) {
                    if (localFocus == -1)
                        localFocus = index;
                    select(index, false);
                    if ((getApi().style & SWT.SINGLE) != 0 && isSelected(index)) {
                        showSelection();
                        return;
                    }
                    index++;
                }
                if (localFocus != -1)
                    focusIndex = localFocus;
            }
        }
        if ((getApi().style & SWT.MULTI) != 0) {
            if (focusIndex >= 0)
                setFocusIndex(focusIndex);
        }
    }

    /**
     * Selects the item at the given zero-relative index in the receiver.
     * If the item at the index was already selected, it remains selected.
     * The current selection is first cleared, then the new item is selected,
     * and if necessary the receiver is scrolled to make the new selection visible.
     * Indices that are out of range are ignored.
     *
     * @param index the index of the item to select
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @see List#deselectAll()
     * @see List#select(int)
     */
    public void setSelection(int index) {
        checkWidget();
        deselectAll();
        select(index, true);
        if ((getApi().style & SWT.MULTI) != 0) {
            if (index >= 0)
                setFocusIndex(index);
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
     *
     * @param start the start index of the items to select
     * @param end the end index of the items to select
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see List#deselectAll()
     * @see List#select(int,int)
     */
    public void setSelection(int start, int end) {
        checkWidget();
        deselectAll();
        if (end < 0 || start > end || ((getApi().style & SWT.SINGLE) != 0 && start != end))
            return;
        start = Math.max(0, start);
        if ((getApi().style & SWT.SINGLE) != 0) {
            select(start, true);
        } else {
            select(start, end, true);
            setFocusIndex(start);
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
        this.topIndex = newValue;
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
     */
    public void showSelection() {
        checkWidget();
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        forceResize();
    }

    @Override
    void updateMenuLocation(Event event) {
        Rectangle clientArea = getClientAreaInPixels();
        int x = clientArea.x, y = clientArea.y;
        int focusIndex = getFocusIndex();
        if (focusIndex != -1) {
            x = Math.min(x, clientArea.x + clientArea.width);
            y = Math.min(y, clientArea.y + clientArea.height);
        }
        Point pt = toDisplayInPixels(x, y);
        int zoom = getZoom();
        event.setLocation(DPIUtil.pixelToPoint(pt.x, zoom), DPIUtil.pixelToPoint(pt.y, zoom));
    }

    @Override
    boolean updateTextDirection(int textDirection) {
        if (textDirection == AUTO_TEXT_DIRECTION) {
            /* If auto is already in effect, there's nothing to do. */
            if ((getApi().state & HAS_AUTO_DIRECTION) != 0)
                return false;
            getApi().state |= HAS_AUTO_DIRECTION;
        } else {
            getApi().state &= ~HAS_AUTO_DIRECTION;
            if (!addedUCC) /*(state & HAS_AUTO_DIRECTION) == 0*/
            {
                return super.updateTextDirection(textDirection);
            }
        }
        addedUCC = false;
        return textDirection == AUTO_TEXT_DIRECTION || super.updateTextDirection(textDirection);
    }

    @Override
    int widgetStyle() {
        if ((getApi().style & SWT.MULTI) != 0) {
        }
        return 0;
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof List list)) {
            return;
        }
        if ((list.style & SWT.H_SCROLL) != 0) {
            // Recalculate the Scroll width, as length of items has changed
            ((DartList) list.getImpl()).setScrollWidth();
        }
    }

    String[] items = new String[0];

    int[] selection = new int[0];

    int topIndex;

    public boolean _addedUCC() {
        return addedUCC;
    }

    public String[] _items() {
        return items;
    }

    public int[] _selection() {
        return selection;
    }

    public int _topIndex() {
        return topIndex;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                ListHelper.sendSelection(this, e, SWT.DefaultSelection);
            });
        });
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                ListHelper.sendSelection(this, e, SWT.Selection);
            });
        });
    }

    public List getApi() {
        if (api == null)
            api = List.createApi(this);
        return (List) api;
    }

    public VList getValue() {
        if (value == null)
            value = new VList(this);
        return (VList) value;
    }
}
