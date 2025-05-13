package org.eclipse.swt.custom;

import java.util.function.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ICCombo extends IComposite {

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
    void add(String string);

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
    void add(String string, int index);

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the receiver's text is modified, by sending
     * it one of the messages defined in the <code>ModifyListener</code>
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
     * @see ModifyListener
     * @see #removeModifyListener
     */
    void addModifyListener(ModifyListener listener);

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the user changes the receiver's selection, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is called when the combo's list selection changes.
     * <code>widgetDefaultSelected</code> is typically called when ENTER is pressed the combo's text area.
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
    void addSelectionListener(SelectionListener listener);

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the receiver's text is verified, by sending
     * it one of the messages defined in the <code>VerifyListener</code>
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
     * @see VerifyListener
     * @see #removeVerifyListener
     *
     * @since 3.3
     */
    void addVerifyListener(VerifyListener listener);

    /**
     * Sets the selection in the receiver's text field to an empty
     * selection starting just before the first character. If the
     * text field is editable, this has the effect of placing the
     * i-beam at the start of the text.
     * <p>
     * Note: To clear the selected items in the receiver's list,
     * use <code>deselectAll()</code>.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #deselectAll
     */
    void clearSelection();

    IPoint computeSize(int wHint, int hHint, boolean changed);

    /**
     * Copies the selected text.
     * <p>
     * The current selection is copied to the clipboard.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    void copy();

    /**
     * Cuts the selected text.
     * <p>
     * The current selection is first copied to the
     * clipboard and then deleted from the widget.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    void cut();

    /**
     * Deselects the item at the given zero-relative index in the receiver's
     * list.  If the item at the index was already deselected, it remains
     * deselected. Indices that are out of range are ignored.
     *
     * @param index the index of the item to deselect
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void deselect(int index);

    /**
     * Deselects all selected items in the receiver's list.
     * <p>
     * Note: To clear the selection in the receiver's text field,
     * use <code>clearSelection()</code>.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #clearSelection
     */
    void deselectAll();

    /**
     * Returns the horizontal alignment.
     * The alignment style (LEFT, CENTER or RIGHT) is returned.
     *
     * @return SWT.LEFT, SWT.RIGHT or SWT.CENTER
     * @since 3.113
     */
    int getAlignment();

    IControl[] getChildren();

    /**
     * Gets the editable state.
     *
     * @return whether or not the receiver is editable
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    boolean getEditable();

    /**
     * Returns the item at the given, zero-relative index in the
     * receiver's list. Throws an exception if the index is out
     * of range.
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
    String getItem(int index);

    /**
     * Returns the number of items contained in the receiver's list.
     *
     * @return the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getItemCount();

    /**
     * Returns the height of the area which would be used to
     * display <em>one</em> of the items in the receiver's list.
     *
     * @return the height of one item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getItemHeight();

    /**
     * Returns an array of <code>String</code>s which are the items
     * in the receiver's list.
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
    String[] getItems();

    /**
     * Returns <code>true</code> if the receiver's list is visible,
     * and <code>false</code> otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, this method
     * may still indicate that it is considered visible even though
     * it may not actually be showing.
     * </p>
     *
     * @return the receiver's list's visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    boolean getListVisible();

    IMenu getMenu();

    /**
     * Returns a <code>Point</code> whose x coordinate is the start
     * of the selection in the receiver's text field, and whose y
     * coordinate is the end of the selection. The returned values
     * are zero-relative. An "empty" selection as indicated by
     * the the x and y coordinates having the same value.
     *
     * @return a point representing the selection start and end
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    IPoint getSelection();

    /**
     * Returns the zero-relative index of the item which is currently
     * selected in the receiver's list, or -1 if no item is selected.
     *
     * @return the index of the selected item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getSelectionIndex();

    IShell getShell();

    int getStyle();

    /**
     * Returns a string containing a copy of the contents of the
     * receiver's text field.
     *
     * @return the receiver's text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    String getText();

    /**
     * Returns the height of the receivers's text field.
     *
     * @return the text height
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getTextHeight();

    /**
     * Returns the maximum number of characters that the receiver's
     * text field is capable of holding. If this has not been changed
     * by <code>setTextLimit()</code>, it will be the constant
     * <code>Combo.LIMIT</code>.
     *
     * @return the text limit
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    int getTextLimit();

    /**
     * Gets the number of items that are visible in the drop
     * down portion of the receiver's list.
     *
     * @return the number of items that are visible
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    int getVisibleItemCount();

    /**
     * Searches the receiver's list starting at the first item
     * (index 0) until an item is found that is equal to the
     * argument, and returns the index of that item. If no item
     * is found, returns -1.
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
    int indexOf(String string);

    /**
     * Searches the receiver's list starting at the given,
     * zero-relative index until an item is found that is equal
     * to the argument, and returns the index of that item. If
     * no item is found or the starting index is out of range,
     * returns -1.
     *
     * @param string the search item
     * @param start the zero-relative index at which to begin the search
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
    int indexOf(String string, int start);

    boolean isFocusControl();

    /**
     * Pastes text from clipboard.
     * <p>
     * The selected text is deleted from the widget
     * and new text inserted from the clipboard.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    void paste();

    void redraw();

    void redraw(int x, int y, int width, int height, boolean all);

    /**
     * Removes the item from the receiver's list at the given
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
    void remove(int index);

    /**
     * Removes the items from the receiver's list which are
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
    void remove(int start, int end);

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
    void remove(String string);

    /**
     * Removes all of the items from the receiver's list and clear the
     * contents of receiver's text field.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void removeAll();

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the receiver's text is modified.
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
     * @see ModifyListener
     * @see #addModifyListener
     */
    void removeModifyListener(ModifyListener listener);

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
    void removeSelectionListener(SelectionListener listener);

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the control is verified.
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
     * @see VerifyListener
     * @see #addVerifyListener
     *
     * @since 3.3
     */
    void removeVerifyListener(VerifyListener listener);

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
    void select(int index);

    /**
     * Set the horizontal alignment of the CCombo.
     * Use the values LEFT, CENTER and RIGHT to align image and text within the available space.
     *
     * @param align the alignment style of LEFT, RIGHT or CENTER
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the value of align is not one of SWT.LEFT, SWT.RIGHT or SWT.CENTER</li>
     * </ul>
     * @since 3.113
     */
    void setAlignment(int align);

    void setBackground(IColor color);

    /**
     * Sets the editable state.
     *
     * @param editable the new editable state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setEditable(boolean editable);

    void setEnabled(boolean enabled);

    boolean setFocus();

    void setFont(IFont font);

    void setForeground(IColor color);

    /**
     * Sets the text of the item in the receiver's list at the given
     * zero-relative index to the string argument. This is equivalent
     * to <code>remove</code>'ing the old item at the index, and then
     * <code>add</code>'ing the new item at that index.
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
    void setItem(int index, String string);

    /**
     * Sets the receiver's list to be the given array of items.
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
    void setItems(String[] items);

    /**
     * Sets the layout which is associated with the receiver to be
     * the argument which may be null.
     * <p>
     * Note: No Layout can be set on this Control because it already
     * manages the size and position of its children.
     * </p>
     *
     * @param layout the receiver's new layout or null
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setLayout(ILayout layout);

    /**
     * Marks the receiver's list as visible if the argument is <code>true</code>,
     * and marks it invisible otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, marking
     * it visible may not actually cause it to be displayed.
     * </p>
     *
     * @param visible the new visibility state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.4
     */
    void setListVisible(boolean visible);

    void setMenu(IMenu menu);

    /**
     * Sets the selection in the receiver's text field to the
     * range specified by the argument whose x coordinate is the
     * start of the selection and whose y coordinate is the end
     * of the selection.
     *
     * @param selection a point representing the new selection start and end
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setSelection(IPoint selection);

    /**
     * Sets the contents of the receiver's text field to the
     * given string.
     * <p>
     * Note: The text field in a <code>Combo</code> is typically
     * only capable of displaying a single line of text. Thus,
     * setting the text to a string containing line breaks or
     * other special characters will probably cause it to
     * display incorrectly.
     * </p><p>
     * Also note, if control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
     *
     * @param string the new text
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setText(String string);

    /**
     * Sets the maximum number of characters that the receiver's
     * text field is capable of holding to be the argument.
     *
     * @param limit new text limit
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_CANNOT_BE_ZERO - if the limit is zero</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void setTextLimit(int limit);

    void setToolTipText(String string);

    void setVisible(boolean visible);

    /**
     * Sets the number of items that are visible in the drop
     * down portion of the receiver's list.
     *
     * @param count the new number of items to be visible
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.0
     */
    void setVisibleItemCount(int count);

    boolean traverse(int event);

    CCombo getApi();
}
