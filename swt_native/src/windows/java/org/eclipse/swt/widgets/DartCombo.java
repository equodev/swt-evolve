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
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 483540
 * *****************************************************************************
 */
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
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

    boolean noSelection, ignoreDefaultSelection, ignoreCharacter, ignoreModify, ignoreResize, lockText;

    int scrollWidth, visibleCount;

    long cbtHook;

    String[] items = new String[0];

    int[] segments;

    int clearSegmentsCount = 0;

    boolean stateFlagsUsable;

    static final char LTR_MARK = '\u200e';

    static final char RTL_MARK = '\u200f';

    static final int VISIBLE_COUNT = 5;

    /*
	 * These are the undocumented control id's for the children of
	 * a combo box.  Since there are no constants for these values,
	 * they may change with different versions of Windows (but have
	 * been the same since Windows 3.0).
	 */
    static final int CBID_LIST = 1000;

    static final int CBID_EDIT = 1001;

    static long /*final*/
    EditProc, ListProc;

    /* Undocumented values. Remained the same at least between Win7 and Win10 */
    static final int stateFlagsOffset = (C.PTR_SIZEOF == 8) ? 0x68 : 0x54;

    static final int stateFlagsFirstPaint = 0x02000000;

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
        this.getApi().style |= SWT.H_SCROLL;
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
        if (!noSelection) {
        }
        clearSegments(true);
        applyEditSegments();
        applyListSegments();
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

    void applyEditSegments() {
        if (--clearSegmentsCount != 0)
            return;
        if (!hooks(SWT.Segments) && !filters(SWT.Segments) && (getApi().state & HAS_AUTO_DIRECTION) == 0)
            return;
        /* Get segments */
        segments = null;
        int nSegments = segments.length;
        if (nSegments == 0)
            return;
        int charCount = 0, segmentCount = 0;
        char defaultSeparator = getOrientation() == SWT.RIGHT_TO_LEFT ? RTL_MARK : LTR_MARK;
        while (segmentCount < nSegments) {
            segments[segmentCount] = charCount - segmentCount;
            segmentCount++;
        }
        /* Get the current selection */
        int[] start = new int[1], end = new int[1];
        boolean oldIgnoreCharacter = ignoreCharacter, oldIgnoreModify = ignoreModify;
        ignoreCharacter = ignoreModify = true;
        /* Restore selection */
        start[0] = translateOffset(start[0]);
        end[0] = translateOffset(end[0]);
        ignoreCharacter = oldIgnoreCharacter;
        ignoreModify = oldIgnoreModify;
    }

    void applyListSegments() {
        int index = items.length;
        int cp = getCodePage();
        String string;
        if (!noSelection) {
        }
        while (index-- > 0) {
        }
    }

    @Override
    public void checkSubclass() {
        if (!isValidSubclass())
            error(SWT.ERROR_INVALID_SUBCLASS);
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

    void clearSegments(boolean applyText) {
        if (clearSegmentsCount++ != 0)
            return;
        if (segments == null)
            return;
        int nSegments = segments.length;
        if (nSegments == 0)
            return;
        if (!applyText) {
            segments = null;
            return;
        }
        boolean oldIgnoreCharacter = ignoreCharacter, oldIgnoreModify = ignoreModify;
        ignoreCharacter = ignoreModify = true;
        int cp = getCodePage();
        /* Get the current selection */
        int[] start = new int[1], end = new int[1];
        start[0] = untranslateOffset(start[0]);
        end[0] = untranslateOffset(end[0]);
        segments = null;
        ignoreCharacter = oldIgnoreCharacter;
        ignoreModify = oldIgnoreModify;
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
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
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
    }

    @Override
    void createHandle() {
        /*
	* Feature in Windows.  When the selection changes in a combo box,
	* Windows draws the selection, even when the combo box does not
	* have focus.  Strictly speaking, this is the correct Windows
	* behavior because the combo box sets ES_NOHIDESEL on the text
	* control that it creates.  Despite this, it looks strange because
	* Windows also clears the selection and selects all the text when
	* the combo box gets focus.  The fix is use the CBT hook to clear
	* the ES_NOHIDESEL style bit when the text control is created.
	*/
        if ((getApi().style & (SWT.READ_ONLY | SWT.SIMPLE)) != 0) {
            super.createHandle();
        } else {
            super.createHandle();
            cbtHook = 0;
        }
        getApi().state &= ~(CANVAS | THEME_BACKGROUND);
        if (((SwtDisplay) display.getImpl()).comboUseDarkTheme) {
        }
        stateFlagsUsable = stateFlagsTest();
        /*
	* Bug in Windows.  If the combo box has the CBS_SIMPLE style,
	* the list portion of the combo box is not drawn correctly the
	* first time, causing pixel corruption.  The fix is to ensure
	* that the combo box has been resized more than once.
	*/
        if ((getApi().style & SWT.SIMPLE) != 0) {
        }
    }

    @Override
    void createWidget() {
        super.createWidget();
        visibleCount = VISIBLE_COUNT;
        if ((getApi().style & SWT.SIMPLE) == 0) {
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
        sendEvent(SWT.Modify);
        // widget could be disposed at this point
        clearSegments(false);
        clearSegmentsCount--;
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
        sendEvent(SWT.Modify);
        // widget could be disposed at this point
        clearSegments(false);
        clearSegmentsCount--;
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
        return DPIUtil.scaleDown(getCaretLocationInPixels(), getZoom());
    }

    Point getCaretLocationInPixels() {
        return null;
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
        int[] start = new int[1], end = new int[1];
        /*
	* In Windows, there is no API to get the position of the caret
	* when the selection is not an i-beam.  The best that can be done
	* is to query the pixel position of the current caret and compare
	* it to the pixel position of the start and end of the selection.
	*
	* NOTE:  This does not work when the i-beam belongs to another
	* control.  In this case, guess that the i-beam is at the start
	* of the selection.
	*/
        int caret = start[0];
        if (start[0] != end[0]) {
        }
        return untranslateOffset(caret);
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
        error(SWT.ERROR_INVALID_RANGE);
        return "";
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
        return 0;
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
        return DPIUtil.scaleDown(getItemHeightInPixels(), getZoom());
    }

    int getItemHeightInPixels() {
        return 0;
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
        String[] result;
        int count = getItemCount();
        result = new String[count];
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
        if ((getApi().style & SWT.DROP_DOWN) != 0) {
        }
        return true;
    }

    @Override
    String getNameText() {
        return getText();
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
        checkWidget();
        this.listVisible = visible;
        getBridge().dirty(this);
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
        return super.getOrientation();
    }

    Event getSegments(String string) {
        Event event = null;
        if (hooks(SWT.Segments) || filters(SWT.Segments)) {
            event = new Event();
            event.text = string;
            sendEvent(SWT.Segments, event);
            if (event != null && event.segments != null) {
                for (int i = 1, segmentCount = event.segments.length, lineLength = string == null ? 0 : string.length(); i < segmentCount; i++) {
                    if (event.segments[i] < event.segments[i - 1] || event.segments[i] > lineLength) {
                        SWT.error(SWT.ERROR_INVALID_ARGUMENT);
                    }
                }
            }
        }
        if ((getApi().state & HAS_AUTO_DIRECTION) != 0) {
            int[] oldSegments = null;
            char[] oldSegmentsChars = null;
            if (event == null) {
                event = new Event();
            } else {
                oldSegments = event.segments;
                oldSegmentsChars = event.segmentsChars;
            }
            int nSegments = oldSegments == null ? 0 : oldSegments.length;
            event.segments = new int[nSegments + 1];
            event.segmentsChars = new char[nSegments + 1];
            if (oldSegments != null) {
                System.arraycopy(oldSegments, 0, event.segments, 1, nSegments);
            }
            if (oldSegmentsChars != null) {
                System.arraycopy(oldSegmentsChars, 0, event.segmentsChars, 1, nSegments);
            }
            event.segments[0] = 0;
        }
        return event;
    }

    String getSegmentsText(String text, Event event) {
        if (text == null || event == null)
            return text;
        int[] segments = event.segments;
        if (segments == null)
            return text;
        int nSegments = segments.length;
        if (nSegments == 0)
            return text;
        char[] segmentsChars = /*event == null ? this.segmentsChars : */
        event.segmentsChars;
        int length = text.length();
        char[] oldChars = new char[length];
        text.getChars(0, length, oldChars, 0);
        char[] newChars = new char[length + nSegments];
        int charCount = 0, segmentCount = 0;
        char defaultSeparator = getOrientation() == SWT.RIGHT_TO_LEFT ? RTL_MARK : LTR_MARK;
        while (charCount < length) {
            if (segmentCount < nSegments && charCount == segments[segmentCount]) {
                char separator = segmentsChars != null && segmentsChars.length > segmentCount ? segmentsChars[segmentCount] : defaultSeparator;
                newChars[charCount + segmentCount++] = separator;
            } else {
                newChars[charCount + segmentCount] = oldChars[charCount++];
            }
        }
        while (segmentCount < nSegments) {
            segments[segmentCount] = charCount;
            char separator = segmentsChars != null && segmentsChars.length > segmentCount ? segmentsChars[segmentCount] : defaultSeparator;
            newChars[charCount + segmentCount++] = separator;
        }
        return new String(newChars, 0, newChars.length);
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
        if ((getApi().style & SWT.DROP_DOWN) != 0 && (getApi().style & SWT.READ_ONLY) != 0) {
        }
        int[] start = new int[1], end = new int[1];
        return new Point(untranslateOffset(start[0]), untranslateOffset(end[0]));
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
        if (noSelection)
            return -1;
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
        if (segments != null) {
        }
        return this.text;
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
        return DPIUtil.scaleDown(getTextHeightInPixels(), getZoom());
    }

    int getTextHeightInPixels() {
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
        return this.textLimit;
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
        return visibleCount;
    }

    @Override
    boolean hasFocus() {
        return false;
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
        /*
	* Bug in Windows.  For some reason, CB_FINDSTRINGEXACT
	* will not find empty strings even though it is legal
	* to insert an empty string into a combo.  The fix is
	* to search the combo, an item at a time.
	*/
        if (string.length() == 0) {
            int count = getItemCount();
            for (int i = start; i < count; i++) {
                if (string.equals(getItem(i)))
                    return i;
            }
            return -1;
        }
        int index = start - 1, last = 0;
        do {
        } while (!string.equals(getItem(index)));
        return index;
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
    }

    void stateFlagsAdd(int flags) {
        int[] stateFlags = new int[1];
        stateFlags[0] |= flags;
    }

    /*
 * Verify that undocumented internal data is in expected location.
 * The test is performed at creation time, when the value of state flags is predictable.
 * For simplicity, only SWT.READ_ONLY combos are handled.
 */
    boolean stateFlagsTest() {
        int[] stateFlags = new int[1];
        /*
	 * 0x00000002 is unknown
	 * 0x00002000 is set in WM_NCCREATE
	 * 0x00004000 means CBS_DROPDOWNLIST (SWT.READ_ONLY)
	 * 0x02000000 is set in WM_NCCREATE and reset after first WM_PAINT
	 */
        return (stateFlags[0] == 0x02006002);
    }

    @Override
    void register() {
        super.register();
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
        remove(index, true);
    }

    void remove(int index, boolean notify) {
        char[] buffer = null;
        if ((getApi().style & SWT.H_SCROLL) != 0) {
        }
        if ((getApi().style & SWT.H_SCROLL) != 0)
            setScrollWidth(buffer, true);
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
        long hDC = 0, oldFont = 0, newFont = 0;
        int newWidth = 0;
        if ((getApi().style & SWT.H_SCROLL) != 0) {
        }
        for (int i = start; i <= end; i++) {
            char[] buffer = null;
            if ((getApi().style & SWT.H_SCROLL) != 0) {
            }
            if ((getApi().style & SWT.H_SCROLL) != 0) {
            }
        }
        if ((getApi().style & SWT.H_SCROLL) != 0) {
            setScrollWidth(newWidth, false);
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
        sendEvent(SWT.Modify);
        if (isDisposed())
            return;
        if ((getApi().style & SWT.H_SCROLL) != 0)
            setScrollWidth(0);
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
        if (!noSelection) {
        }
        clearSegments(true);
        applyEditSegments();
        applyListSegments();
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
        checkWidget();
    }

    @Override
    void setBackgroundImage(long hBitmap) {
        super.setBackgroundImage(hBitmap);
    }

    @Override
    void setBackgroundPixel(int pixel) {
        super.setBackgroundPixel(pixel);
    }

    @Override
    void setBoundsInPixels(int x, int y, int width, int height, int flags) {
        /*
	* Feature in Windows.  If the combo box has the CBS_DROPDOWN
	* or CBS_DROPDOWNLIST style, Windows uses the height that the
	* programmer sets in SetWindowPos () to control height of the
	* drop down list.  When the width is non-zero, Windows remembers
	* this value and sets the height to be the height of the text
	* field part of the combo box.  If the width is zero, Windows
	* allows the height to have any value.  Therefore, when the
	* programmer sets and then queries the height, the values can
	* be different depending on the width.  The problem occurs when
	* the programmer uses computeSize () to determine the preferred
	* height (always the height of the text field) and then uses
	* this value to set the height of the combo box.  The result
	* is a combo box with a zero size drop down list.  The fix, is
	* to always set the height to show a fixed number of combo box
	* items and ignore the height value that the programmer supplies.
	*/
        if ((getApi().style & SWT.DROP_DOWN) != 0) {
            int visibleCount = getItemCount() == 0 ? VISIBLE_COUNT : this.visibleCount;
            height = getTextHeightInPixels() + (getItemHeightInPixels() * visibleCount) + 2;
        } else {
            super.setBoundsInPixels(x, y, width, height, flags);
        }
    }

    @Override
    public void setFont(Font font) {
        checkWidget();
        /*
	* Feature in Windows.  For some reason, in a editable combo box,
	* when WM_SETFONT is used to set the font of the control
	* and the current text does not match an item in the
	* list, Windows selects the item that most closely matches the
	* contents of the combo.  The fix is to lock the current text
	* by ignoring all WM_SETTEXT messages during processing of
	* WM_SETFONT.
	*/
        boolean oldLockText = lockText;
        if ((getApi().style & SWT.READ_ONLY) == 0)
            lockText = true;
        super.setFont(font);
        if ((getApi().style & SWT.READ_ONLY) == 0)
            lockText = oldLockText;
        if ((getApi().style & SWT.H_SCROLL) != 0)
            setScrollWidth();
        getBridge().dirty(this);
    }

    @Override
    void setForegroundPixel(int pixel) {
        super.setForegroundPixel(pixel);
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
        int selection = getSelectionIndex();
        remove(index, false);
        if (isDisposed())
            return;
        add(string, index);
        if (selection != -1)
            select(selection);
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
        checkWidget();
        if (items == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        for (String item : items) {
            if (item == null)
                error(SWT.ERROR_INVALID_ARGUMENT);
        }
        long hDC = 0, oldFont = 0, newFont = 0;
        int newWidth = 0;
        if ((getApi().style & SWT.H_SCROLL) != 0) {
            setScrollWidth(0);
        }
        int codePage = getCodePage();
        for (String item : items) {
            if ((getApi().style & SWT.H_SCROLL) != 0) {
            }
        }
        if ((getApi().style & SWT.H_SCROLL) != 0) {
            setScrollWidth(newWidth + 3);
        }
        sendEvent(SWT.Modify);
        getBridge().dirty(this);
        // widget could be disposed at this point
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
        super.setOrientation(orientation);
        getBridge().dirty(this);
    }

    void setScrollWidth() {
        int newWidth = 0;
        long newFont, oldFont = 0;
        setScrollWidth(newWidth + 3);
    }

    void setScrollWidth(int scrollWidth) {
        this.scrollWidth = scrollWidth;
        if ((getApi().style & SWT.SIMPLE) != 0) {
            return;
        }
        boolean scroll = false;
        /*
	* Feature in Windows.  For some reason, in a editable combo box,
	* when CB_SETDROPPEDWIDTH is used to set the width of the drop
	* down list and the current text does not match an item in the
	* list, Windows selects the item that most closely matches the
	* contents of the combo.  The fix is to lock the current text
	* by ignoring all WM_SETTEXT messages during processing of
	* CB_SETDROPPEDWIDTH.
	*/
        boolean oldLockText = lockText;
        if ((getApi().style & SWT.READ_ONLY) == 0)
            lockText = true;
        if (scroll) {
        } else {
        }
        if ((getApi().style & SWT.READ_ONLY) == 0)
            lockText = oldLockText;
    }

    void setScrollWidth(char[] buffer, boolean grow) {
    }

    void setScrollWidth(int newWidth, boolean grow) {
        if (grow) {
            if (newWidth <= scrollWidth)
                return;
            setScrollWidth(newWidth + 3);
        } else {
            if (newWidth < scrollWidth)
                return;
            setScrollWidth();
        }
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
        checkWidget();
        this.selection = selection;
        if (selection == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        getBridge().dirty(this);
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
        this.text = string;
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if ((getApi().style & SWT.READ_ONLY) != 0) {
            int index = indexOf(string);
            if (index != -1)
                select(index);
            return;
        }
        clearSegments(false);
        int limit = Combo.LIMIT;
        if (string.length() > limit)
            string = string.substring(0, limit);
        getBridge().dirty(this);
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
        checkWidget();
        this.textLimit = limit;
        if (limit == 0)
            error(SWT.ERROR_CANNOT_BE_ZERO);
        if (segments != null && limit > 0) {
        } else {
        }
        getBridge().dirty(this);
    }

    @Override
    void setToolTipText(Shell shell, String string) {
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
        checkWidget();
        if (count < 0)
            return;
        visibleCount = count;
        updateDropDownHeight();
        getBridge().dirty(this);
    }

    @Override
    void subclass() {
        super.subclass();
    }

    int translateOffset(int offset) {
        if (segments == null)
            return offset;
        for (int i = 0, nSegments = segments.length; i < nSegments && offset - i >= segments[i]; i++) {
            offset++;
        }
        return offset;
    }

    @Override
    boolean traverseEscape() {
        if ((getApi().style & SWT.DROP_DOWN) != 0) {
        }
        return super.traverseEscape();
    }

    @Override
    boolean traverseReturn() {
        if ((getApi().style & SWT.DROP_DOWN) != 0) {
        }
        return super.traverseReturn();
    }

    @Override
    void unsubclass() {
        super.unsubclass();
    }

    int untranslateOffset(int offset) {
        if (segments == null)
            return offset;
        for (int i = 0, nSegments = segments.length; i < nSegments && offset > segments[i]; i++) {
            offset--;
        }
        return offset;
    }

    void updateDropDownHeight() {
        /*
	* Feature in Windows.  If the combo box has the CBS_DROPDOWN
	* or CBS_DROPDOWNLIST style, Windows uses the height that the
	* programmer sets in SetWindowPos () to control height of the
	* drop down list.  See #setBounds() for more details.
	*/
        if ((getApi().style & SWT.DROP_DOWN) != 0) {
        }
    }

    void updateDropDownTheme() {
    }

    @Override
    boolean updateTextDirection(int textDirection) {
        if (super.updateTextDirection(textDirection)) {
            clearSegments(true);
            applyEditSegments();
            applyListSegments();
            return true;
        }
        return false;
    }

    @Override
    void updateOrientation() {
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0) {
        } else {
        }
        long hwndText = 0, hwndList = 0;
        if (hwndText != 0) {
            if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0) {
            } else {
            }
        }
        if (hwndList != 0) {
            if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0) {
            } else {
            }
        }
    }

    String verifyText(String string, int start, int end, Event keyEvent) {
        Event event = new Event();
        event.text = string;
        event.start = start;
        event.end = end;
        if (keyEvent != null) {
            event.character = keyEvent.character;
            event.keyCode = keyEvent.keyCode;
            event.stateMask = keyEvent.stateMask;
        }
        event.start = untranslateOffset(event.start);
        event.end = untranslateOffset(event.end);
        /*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in the verify
	* event.  If this happens, answer null to cancel
	* the operation.
	*/
        sendEvent(SWT.Verify, event);
        if (!event.doit || isDisposed())
            return null;
        return event.text;
    }

    @Override
    int widgetExtStyle() {
        return 0;
    }

    @Override
    int widgetStyle() {
        return 0;
    }

    void forceScrollingToCaret() {
        if ((getApi().style & SWT.READ_ONLY) == 0) {
            Point oldSelection = this.getSelection();
            Point tmpSelection = new Point(0, 0);
            if (!oldSelection.equals(tmpSelection)) {
                this.setSelection(tmpSelection);
                this.setSelection(oldSelection);
            }
        }
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof Combo combo)) {
            return;
        }
        if ((combo.style & SWT.H_SCROLL) != 0) {
            ((DartCombo) combo.getImpl()).scrollWidth = 0;
            ((DartCombo) combo.getImpl()).setScrollWidth();
        }
    }

    boolean listVisible;

    Point selection;

    String text;

    int textLimit;

    public boolean _noSelection() {
        return noSelection;
    }

    public boolean _ignoreDefaultSelection() {
        return ignoreDefaultSelection;
    }

    public boolean _ignoreCharacter() {
        return ignoreCharacter;
    }

    public boolean _ignoreModify() {
        return ignoreModify;
    }

    public boolean _ignoreResize() {
        return ignoreResize;
    }

    public boolean _lockText() {
        return lockText;
    }

    public int _scrollWidth() {
        return scrollWidth;
    }

    public int _visibleCount() {
        return visibleCount;
    }

    public long _cbtHook() {
        return cbtHook;
    }

    public String[] _items() {
        return items;
    }

    public int[] _segments() {
        return segments;
    }

    public int _clearSegmentsCount() {
        return clearSegmentsCount;
    }

    public boolean _stateFlagsUsable() {
        return stateFlagsUsable;
    }

    public boolean _listVisible() {
        return listVisible;
    }

    public Point _selection() {
        return selection;
    }

    public String _text() {
        return text;
    }

    public int _textLimit() {
        return textLimit;
    }

    protected void hookEvents() {
        super.hookEvents();
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
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Selection, e);
            });
        });
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.DefaultSelection, e);
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
