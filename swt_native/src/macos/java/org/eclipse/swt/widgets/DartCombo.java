/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 483540
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import dev.equo.swt.*;

/**
 * Instances of this class are controls that allow the user
 * to choose an item from a list of items, or optionally
 * enter a new value by typing it into an editable text
 * field. Often, <code>Combo</code>s are used in the same place
 * where a single selection <code>List</code> widget could
 * be used but space is limited. A <code>Combo</code> takes
 * less space than a <code>List</code> widget and shows
 * similar information.
 * <p>
 * Note: Since <code>Combo</code>s can contain both a list
 * and an editable text field, it is possible to confuse methods
 * which access one versus the other (compare for example,
 * <code>clearSelection()</code> and <code>deselectAll()</code>).
 * The API documentation is careful to indicate either "the
 * receiver's list" or the "the receiver's text field" to
 * distinguish between the two cases.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to add children to it, or set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>DROP_DOWN, READ_ONLY, SIMPLE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>DefaultSelection, Modify, Selection, Verify, OrientationChange</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles DROP_DOWN and SIMPLE may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see List
 * @see <a href="http://www.eclipse.org/swt/snippets/#combo">Combo snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartCombo extends DartComposite implements ICombo {

    String text;

    int textLimit = Combo.LIMIT;

    boolean receivingFocus;

    boolean ignoreSetObject, ignoreSelection;

    boolean listVisible;

    static final int VISIBLE_COUNT = 5;

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
     * @see SWT#DROP_DOWN
     * @see SWT#READ_ONLY
     * @see SWT#SIMPLE
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartCombo(Composite parent, int style, Combo api) {
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
        add(string, items.length);
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
        if (index < 0 || index > items.length)
            error(SWT.ERROR_INVALID_RANGE);
        dirty();
        String[] newItems = new String[items.length + 1];
        System.arraycopy(items, 0, newItems, 0, index);
        newItems[index] = string;
        System.arraycopy(items, index, newItems, index + 1, items.length - index);
        items = newItems;
    }

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
    public void addModifyListener(ModifyListener listener) {
        addTypedListener(listener, SWT.Modify);
    }

    /**
     * Adds a segment listener.
     * <p>
     * A <code>SegmentEvent</code> is sent whenever text content is being modified or
     * a segment listener is added or removed. You can
     * customize the appearance of text by indicating certain characters to be inserted
     * at certain text offsets. This may be used for bidi purposes, e.g. when
     * adjacent segments of right-to-left text should not be reordered relative to
     * each other.
     * E.g., multiple Java string literals in a right-to-left language
     * should generally remain in logical order to each other, that is, the
     * way they are stored.
     * </p>
     * <p>
     * <b>Warning</b>: This API is currently only implemented on Windows.
     * <code>SegmentEvent</code>s won't be sent on GTK and Cocoa.
     * </p>
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
     * @see SegmentEvent
     * @see SegmentListener
     * @see #removeSegmentListener
     *
     * @since 3.103
     */
    public void addSegmentListener(SegmentListener listener) {
        addTypedListener(listener, SWT.Segments);
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the user changes the receiver's selection, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is called when the user changes the combo's list selection.
     * <code>widgetDefaultSelected</code> is typically called when ENTER is pressed the combo's text area.
     * </p>
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
     * @see SelectionListener
     * @see #removeSelectionListener
     * @see SelectionEvent
     */
    public void addSelectionListener(SelectionListener listener) {
        addTypedListener(listener, SWT.Selection, SWT.DefaultSelection);
    }

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
     * @since 3.1
     */
    public void addVerifyListener(VerifyListener listener) {
        addTypedListener(listener, SWT.Verify);
    }

    static int checkStyle(int style) {
        /*
	* Feature in Windows.  It is not possible to create
	* a combo box that has a border using Windows style
	* bits.  All combo boxes draw their own border and
	* do not use the standard Windows border styles.
	* Therefore, no matter what style bits are specified,
	* clear the BORDER bits so that the SWT style will
	* match the Windows widget.
	*
	* The Windows behavior is currently implemented on
	* all platforms.
	*/
        style &= ~SWT.BORDER;
        /*
	* Even though it is legal to create this widget
	* with scroll bars, they serve no useful purpose
	* because they do not automatically scroll the
	* widget's client area.  The fix is to clear
	* the SWT style.
	*/
        style &= ~(SWT.H_SCROLL | SWT.V_SCROLL);
        style = checkBits(style, SWT.DROP_DOWN, SWT.SIMPLE, 0, 0, 0, 0);
        if ((style & SWT.SIMPLE) != 0)
            return style & ~SWT.READ_ONLY;
        return style;
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
    }

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
    public void clearSelection() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) == 0) {
            Point selection = getSelection();
            selection.y = selection.x;
            setSelection(selection);
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

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
     * @since 2.1
     */
    public void copy() {
        checkWidget();
        Point selection = getSelection();
        if (selection.x == selection.y)
            return;
        copyToClipboard(getText(selection.x, selection.y));
    }

    @Override
    void createHandle() {
    }

    @Override
    void createWidget() {
        text = "";
        selection = new Point(0, 0);
        super.createWidget();
        if ((getApi().style & SWT.READ_ONLY) == 0) {
        }
    }

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
     * @since 2.1
     */
    public void cut() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0)
            return;
        Point selection = getSelection();
        if (selection.x == selection.y)
            return;
        int start = selection.x, end = selection.y;
        String text = getText();
        String leftText = text.substring(0, start);
        String rightText = text.substring(end, text.length());
        String oldText = text.substring(start, end);
        String newText = "";
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            if (newText == null)
                return;
        }
        char[] buffer = new char[oldText.length()];
        oldText.getChars(0, buffer.length, buffer, 0);
        copyToClipboard(buffer);
        setText(leftText + newText + rightText, false);
        start += newText.length();
        setSelection(new Point(start, start));
        sendEvent(SWT.Modify);
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
    public void deselect(int index) {
        checkWidget();
        if (index == -1)
            return;
        if (index == getSelectionIndex()) {
            if ((getApi().style & SWT.READ_ONLY) != 0) {
                sendEvent(SWT.Modify);
            } else {
            }
        }
    }

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
    public void deselectAll() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0) {
            sendEvent(SWT.Modify);
        } else {
        }
    }

    @Override
    boolean dragDetect(int x, int y, boolean filter, boolean[] consume) {
        if ((getApi().style & SWT.READ_ONLY) == 0) {
            return false;
        }
        return super.dragDetect(x, y, filter, consume);
    }

    @Override
    public Cursor findCursor() {
        Cursor cursor = super.findCursor();
        return cursor;
    }

    /**
     * Returns the character position of the caret.
     * <p>
     * Indexing is zero based.
     * </p>
     *
     * @return the position of the caret
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.8
     */
    public int getCaretPosition() {
        checkWidget();
        return 0;
    }

    /**
     * Returns a point describing the location of the caret relative
     * to the receiver.
     *
     * @return a point, the location of the caret
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.8
     */
    public Point getCaretLocation() {
        checkWidget();
        if (this.hasFocus()) {
        }
        return null;
    }

    int getCharCount() {
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        } else {
        }
        return 0;
    }

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
    public String getItem(int index) {
        checkWidget();
        if (index < 0 || index >= items.length)
            error(SWT.ERROR_INVALID_RANGE);
        return items[index];
    }

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
    public int getItemCount() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        } else {
        }
        return this.items != null ? this.items.length : 0;
    }

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
    public int getItemHeight() {
        checkWidget();
        //TODO - not supported by the OS
        return 26;
    }

    /**
     * Returns a (possibly empty) array of <code>String</code>s which are
     * the items in the receiver's list.
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
    public boolean getListVisible() {
        checkWidget();
        return listVisible;
    }

    @Override
    String getNameText() {
        return getText();
    }

    @Override
    int getMininumHeight() {
        return getTextHeight();
    }

    /**
     * Returns the orientation of the receiver.
     *
     * @return the orientation style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.1.2
     */
    @Override
    public int getOrientation() {
        checkWidget();
        return getApi().style & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
    }

    /**
     * Returns a <code>Point</code> whose x coordinate is the
     * character position representing the start of the selection
     * in the receiver's text field, and whose y coordinate is the
     * character position representing the end of the selection.
     * An "empty" selection is indicated by the x and y coordinates
     * having the same value.
     * <p>
     * Indexing is zero based.  The range of a selection is from
     * 0..N where N is the number of characters in the widget.
     * </p>
     *
     * @return a point representing the selection start and end
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Point getSelection() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0) {
            return new Point(0, getCharCount());
        } else {
        }
        return this.selection;
    }

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
    public int getSelectionIndex() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        } else {
        }
        return 0;
    }

    /**
     * Returns a string containing a copy of the contents of the
     * receiver's text field, or an empty string if there are no
     * contents.
     *
     * @return the receiver's text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getText() {
        checkWidget();
        return new String(getText(0, -1));
    }

    char[] getText(int start, int end) {
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        } else {
        }
        if (end == -1) {
        } else {
        }
        return text.substring(start, end == -1 ? text.length() : end).toCharArray();
    }

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
    public int getTextHeight() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        } else {
        }
        return 0;
    }

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
     *
     * @see #LIMIT
     */
    public int getTextLimit() {
        checkWidget();
        return textLimit;
    }

    /**
     * Gets the number of items that are visible in the drop
     * down portion of the receiver's list.
     * <p>
     * Note: This operation is a hint and is not supported on
     * platforms that do not have this concept.
     * </p>
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
    public int getVisibleItemCount() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0) {
            return getItemCount();
        } else {
        }
        return this.visibleItemCount;
    }

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
    public int indexOf(String string, int start) {
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        int count = getItemCount();
        if (!(0 <= start && start < count))
            return -1;
        for (int i = start; i < count; i++) {
            if (string.equals(getItem(i)))
                return i;
        }
        return -1;
    }

    @Override
    boolean isEventView(long id) {
        return true;
    }

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
     * @since 2.1
     */
    public void paste() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0)
            return;
        Point selection = getSelection();
        int start = selection.x, end = selection.y;
        String text = getText();
        String leftText = text.substring(0, start);
        String rightText = text.substring(end, text.length());
        String newText = getClipboardText();
        if (newText == null)
            return;
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            if (newText == null)
                return;
        }
        if (textLimit != Combo.LIMIT) {
            int charCount = text.length();
            if (charCount - (end - start) + newText.length() > textLimit) {
                newText = newText.substring(0, textLimit - charCount + (end - start));
            }
        }
        setText(leftText + newText + rightText, false);
        start += newText.length();
        setSelection(new Point(start, start));
        sendEvent(SWT.Modify);
    }

    @Override
    void register() {
        super.register();
    }

    @Override
    void releaseWidget() {
        if (((SwtDisplay) display.getImpl()).currentCombo == this.getApi()) {
            ((SwtDisplay) display.getImpl()).currentCombo = null;
        }
        super.releaseWidget();
        if ((getApi().style & SWT.READ_ONLY) == 0) {
        }
        text = null;
    }

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
    public void remove(int index) {
        checkWidget();
        if (index == -1)
            error(SWT.ERROR_INVALID_RANGE);
        int count = getItemCount();
        if (0 > index || index >= count)
            error(SWT.ERROR_INVALID_RANGE);
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        } else {
        }
    }

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
    public void remove(int start, int end) {
        checkWidget();
        if (start > end)
            return;
        int count = getItemCount();
        if (!(0 <= start && start <= end && end < count)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        int newEnd = Math.min(end, count - 1);
        for (int i = newEnd; i >= start; i--) {
            remove(i);
        }
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
     * Removes all of the items from the receiver's list and clear the
     * contents of receiver's text field.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void removeAll() {
        checkWidget();
        ignoreSelection = true;
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        } else {
            setText("", true);
        }
        ignoreSelection = false;
    }

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
    public void removeModifyListener(ModifyListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Modify, listener);
    }

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
     * @see SegmentEvent
     * @see SegmentListener
     * @see #addSegmentListener
     *
     * @since 3.103
     */
    public void removeSegmentListener(SegmentListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        eventTable.unhook(SWT.Segments, listener);
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
     * @since 3.1
     */
    public void removeVerifyListener(VerifyListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Verify, listener);
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
        dirty();
        checkWidget();
        int count = getItemCount();
        if (0 <= index && index < count) {
            if (index == getSelectionIndex())
                return;
            ignoreSelection = true;
            if ((getApi().style & SWT.READ_ONLY) != 0) {
                sendEvent(SWT.Modify);
            } else {
            }
            ignoreSelection = false;
        }
        this.selection = new Point(selection.x, selection.y);
    }

    @Override
    void sendSelection() {
        sendEvent(SWT.Modify);
        if (!ignoreSelection)
            sendSelectionEvent(SWT.Selection);
    }

    @Override
    void setBounds(int x, int y, int width, int height, boolean move, boolean resize) {
        /*
	 * Feature in Cocoa.  Attempting to create an NSComboBox with a
	 * height > 27 spews a very long warning message to stdout and
	 * often draws the combo incorrectly.
	 * The workaround is to limit the height of editable Combos to the
	 * height that is required to display their text. For multiline text,
	 * limit the height to frame height.
	 */
        if ((getApi().style & SWT.READ_ONLY) == 0) {
            int hLimit = 0;
            if (hLimit == 0) {
            }
            height = Math.min(height, hLimit);
        }
        super.setBounds(x, y, width, height, move, resize);
    }

    @Override
    void setForeground(double[] color) {
        super.setForeground(color);
        updateItems();
        if ((getApi().style & SWT.READ_ONLY) == 0) {
            if (color == null) {
            } else {
            }
        }
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
        int count = getItemCount();
        if (0 > index || index >= count)
            error(SWT.ERROR_INVALID_RANGE);
        int selection = getSelectionIndex();
        ignoreSelection = true;
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        } else {
        }
        if (selection != -1)
            select(selection);
        ignoreSelection = false;
    }

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
    public void setItems(String... items) {
        dirty();
        checkWidget();
        if (items == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null)
                error(SWT.ERROR_INVALID_ARGUMENT);
        }
        removeAll();
        if (items.length == 0)
            return;
        ignoreSelection = true;
        for (int i = 0; i < items.length; i++) {
            if ((getApi().style & SWT.READ_ONLY) != 0) {
            } else {
            }
        }
        ignoreSelection = false;
        this.items = items;
    }

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
    public void setListVisible(boolean visible) {
        dirty();
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        } else {
        }
    }

    /**
     * Sets the orientation of the receiver, which must be one
     * of the constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
     *
     * @param orientation new orientation style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 2.1.2
     */
    @Override
    public void setOrientation(int orientation) {
        dirty();
        checkWidget();
    }

    @Override
    void setOrientation() {
    }

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
    public void setSelection(Point selection) {
        dirty();
        checkWidget();
        if (selection == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if ((getApi().style & SWT.READ_ONLY) == 0) {
        }
        this.selection = selection;
    }

    /**
     * Sets the contents of the receiver's text field to the
     * given string.
     * <p>
     * This call is ignored when the receiver is read only and
     * the given string is not in the receiver's list.
     * </p>
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
    public void setText(String string) {
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setText(string, true);
    }

    void setText(String string, boolean notify) {
        dirty();
        if (notify) {
            if (hooks(SWT.Verify) || filters(SWT.Verify)) {
                if (string == null)
                    return;
            }
        }
        if ((getApi().style & SWT.READ_ONLY) != 0) {
            int index = indexOf(string);
            if (index != -1) {
                select(index);
            }
        } else {
            char[] buffer = new char[Math.min(string.length(), textLimit)];
            string.getChars(0, buffer.length, buffer, 0);
            text = new String(buffer, 0, buffer.length);
            if (notify)
                sendEvent(SWT.Modify);
        }
    }

    /**
     * Sets the maximum number of characters that the receiver's
     * text field is capable of holding to be the argument.
     * <p>
     * To reset this value to the default, use <code>setTextLimit(Combo.LIMIT)</code>.
     * Specifying a limit value larger than <code>Combo.LIMIT</code> sets the
     * receiver's limit to <code>Combo.LIMIT</code>.
     * </p>
     * @param limit new text limit
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_CANNOT_BE_ZERO - if the limit is zero</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #LIMIT
     */
    public void setTextLimit(int limit) {
        dirty();
        checkWidget();
        if (limit == 0)
            error(SWT.ERROR_CANNOT_BE_ZERO);
        textLimit = limit;
    }

    /**
     * Sets the number of items that are visible in the drop
     * down portion of the receiver's list.
     * <p>
     * Note: This operation is a hint and is not supported on
     * platforms that do not have this concept.
     * </p>
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
    public void setVisibleItemCount(int count) {
        dirty();
        checkWidget();
        if (count < 0)
            return;
        if ((getApi().style & SWT.READ_ONLY) != 0) {
            //TODO
        } else {
        }
        this.visibleItemCount = count;
    }

    void updateItems() {
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        } else {
        }
    }

    String[] items = new String[0];

    Point selection;

    int visibleItemCount;

    public String _text() {
        return text;
    }

    public int _textLimit() {
        return textLimit;
    }

    public boolean _receivingFocus() {
        return receivingFocus;
    }

    public boolean _ignoreSetObject() {
        return ignoreSetObject;
    }

    public boolean _ignoreSelection() {
        return ignoreSelection;
    }

    public boolean _listVisible() {
        return listVisible;
    }

    public String[] _items() {
        return items;
    }

    public Point _selection() {
        return selection;
    }

    public int _visibleItemCount() {
        return visibleItemCount;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Modify", "Modify", e -> {
            getDisplay().asyncExec(() -> {
                setText(e.text);
            });
        });
        FlutterBridge.on(this, "Segment", "Segments", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Segments, e);
            });
        });
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.DefaultSelection, e);
            });
        });
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Selection, e);
            });
        });
        FlutterBridge.on(this, "Verify", "Verify", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Verify, e);
            });
        });
    }

    public Combo getApi() {
        if (api == null)
            api = Combo.createApi(this);
        return (Combo) api;
    }

    public VCombo getValue() {
        if (value == null)
            value = new VCombo(this);
        return (VCombo) value;
    }
}
