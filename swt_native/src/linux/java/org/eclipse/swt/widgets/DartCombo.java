/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2020 IBM Corporation and others.
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

    long buttonHandle, entryHandle, textRenderer, cellHandle, popupHandle, menuHandle, buttonBoxHandle, cellBoxHandle, arrowHandle;

    int lastEventTime, visibleCount = 10;

    long imContext;

    long gdkEventKey = 0;

    int fixStart = -1, fixEnd = -1;

    String[] items = new String[0];

    int indexSelected;

    long cssProvider;

    boolean firstDraw = true;

    boolean unselected = true, fitModelToggled = false;

    // Bug 489640. Gtk3.
    private boolean delayedEnableWrap = false;

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
        if (!(0 <= index && index <= items.length)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        String[] newItems = new String[items.length + 1];
        System.arraycopy(items, 0, newItems, 0, index);
        newItems[index] = string;
        System.arraycopy(items, index, newItems, index + 1, items.length - index);
        items = newItems;
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0 && popupHandle != 0) {
        }
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
        if (entryHandle != 0) {
        }
    }

    void clearText() {
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        } else {
        }
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    @Override
    Point computeNativeSize(long h, int wHint, int hHint, boolean changed) {
        // Set fit-model property when computing size, if it was previously disabled
        if (fitModelToggled) {
        }
        int[] xpad = new int[1];
        Point nativeSize = super.computeNativeSize(h, wHint, hHint, changed);
        nativeSize.x += xpad[0] * 2;
        // Re-set fit-model to false as it was before
        if (fitModelToggled) {
        }
        return nativeSize;
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
        if (entryHandle != 0) {
        }
    }

    @Override
    void createHandle(int index) {
        // In GTK 3 font description is inherited from parent widget which is not how SWT has always worked,
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
        if (entryHandle != 0) {
        }
    }

    @Override
    void deregister() {
        super.deregister();
        if (buttonHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(buttonHandle);
        if (entryHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(entryHandle);
        if (popupHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(popupHandle);
        if (menuHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(menuHandle);
        long imContext = imContext();
        if (imContext != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(imContext);
    }

    @Override
    boolean filterKey(long event) {
        gdkEventKey = event;
        return false;
    }

    long findPopupHandle(long oldList) {
        long result = 0;
        return result;
    }

    long findButtonHandle() {
        /*
	* Feature in GTK.  There is no API to query the button
	* handle from a combo box although it is possible to get the
	* text field.  The button handle is needed to hook events.  The
	* fix is to walk the combo tree and find the first child that is
	* an instance of button.
	*/
        long result = 0;
        return result;
    }

    long findArrowHandle() {
        long result = 0;
        if (cellBoxHandle != 0) {
        }
        return result;
    }

    long findMenuHandle() {
        /*
	* Feature in GTK.  There is no API to query the menu
	* handle from a combo box. So we walk the popupHandle to
	* find the handle for the menu.
	*/
        long result = 0;
        return result;
    }

    @Override
    void fixModal(long group, long modalGroup) {
        if (popupHandle != 0) {
            if (group != 0) {
            } else {
                if (modalGroup != 0) {
                }
            }
        }
    }

    void fixIM() {
        /*
	*  The IM filter has to be called one time for each key press event.
	*  When the IM is open the key events are duplicated. The first event
	*  is filtered by SWT and the second event is filtered by GTK.  In some
	*  cases the GTK handler does not run (the widget is destroyed, the
	*  application code consumes the event, etc), for these cases the IM
	*  filter has to be called by SWT.
	*/
        if (gdkEventKey != 0 && gdkEventKey != -1) {
            long imContext = imContext();
            if (imContext != 0) {
                gdkEventKey = -1;
                return;
            }
        }
        gdkEventKey = 0;
    }

    @Override
    long fontHandle() {
        if (entryHandle != 0)
            return entryHandle;
        return super.fontHandle();
    }

    @Override
    long focusHandle() {
        if (entryHandle != 0)
            return entryHandle;
        return super.focusHandle();
    }

    @Override
    boolean hasFocus() {
        if (super.hasFocus())
            return true;
        return false;
    }

    @Override
    void hookEvents() {
        super.hookEvents();
        if (entryHandle != 0) {
        }
        hookEvents(new long[] { buttonHandle, entryHandle, menuHandle });
        long imContext = imContext();
        if (imContext != 0) {
        }
    }

    void hookEvents(long[] handles) {
        for (int i = 0; i < handles.length; i++) {
            long eventHandle = handles[i];
            if (eventHandle != 0) {
            }
        }
    }

    long imContext() {
        if (imContext != 0)
            return imContext;
        return 0;
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
        if (index < 0 || index >= items.length)
            return;
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
        clearText();
    }

    @Override
    boolean dragDetect(int x, int y, boolean filter, boolean dragOnTimeout, boolean[] consume) {
        if (filter && entryHandle != 0) {
            return false;
        }
        return super.dragDetect(x, y, filter, dragOnTimeout, consume);
    }

    @Override
    long enterExitHandle() {
        return fixedHandle;
    }

    @Override
    long eventWindow() {
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        }
        return 0;
    }

    @Override
    long eventSurface() {
        return paintSurface();
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
        return DPIUtil.autoScaleDown(getCaretLocationInPixels());
    }

    Point getCaretLocationInPixels() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0) {
            return new Point(0, 0);
        }
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
        if ((getApi().style & SWT.READ_ONLY) != 0) {
            return 0;
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
        if (!(0 <= index && index < items.length)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
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
        return items.length;
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
        long fontDesc = getFontDescription();
        int result = fontHeight(fontDesc, getApi().handle);
        return result;
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
        String[] result = new String[items.length];
        System.arraycopy(items, 0, result, 0, items.length);
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
        return this.listVisible;
    }

    @Override
    String getNameText() {
        return getText();
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
            int length = 0;
            return new Point(0, length);
        }
        int[] start = new int[1];
        int[] end = new int[1];
        if (entryHandle != 0) {
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
        if (entryHandle != 0) {
            long str = 0;
            if (str == 0) {
                return "";
            } else {
            }
        } else {
        }
        return this.text;
    }

    String getText(int start, int stop) {
        /*
	* NOTE: The current implementation uses substring ()
	* which can reference a potentially large character
	* array.
	*/
        return getText().substring(start, stop - 1);
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
        return DPIUtil.autoScaleDown(getTextHeightInPixels());
    }

    int getTextHeightInPixels() {
        checkWidget();
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
    void adjustChildClipping(long widget) {
        /*
	 * When adjusting the GtkCellView's clip, take into account
	 * the position of the "arrow" icon. We set the clip of the
	 * GtkCellView to the icon's position, minus the icon's width.
	 *
	 * This ensures the text never draws longer than the Combo itself.
	 * See bug 539367.
	 */
        if (widget == cellHandle && (getApi().style & SWT.READ_ONLY) != 0 && !unselected) {
            /*
		 * Set "fit-model" mode for READ_ONLY Combos on GTK3.20+ to false.
		 * This means the GtkCellView rendering the text can be set to
		 * a size other than the maximum. See bug 539367.
		 */
            if (!fitModelToggled) {
                fitModelToggled = true;
            }
            return;
        } else {
            super.adjustChildClipping(widget);
        }
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
        checkWidget();
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
        if (!(0 <= start && start < items.length))
            return -1;
        for (int i = start; i < items.length; i++) {
            if (string.equals(items[i]))
                return i;
        }
        return -1;
    }

    @Override
    boolean isFocusHandle(long widget) {
        if (buttonHandle != 0 && widget == buttonHandle)
            return true;
        if (entryHandle != 0 && widget == entryHandle)
            return true;
        return super.isFocusHandle(widget);
    }

    @Override
    long paintSurface() {
        /*
	 * TODO: GTK4 no access to children of the surface
	 * for combobox may need to use gtk_combo_box_get_child ().
	 * See also Bug 570331, maybe entire function just needs to be removed
	 */
        return 0;
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
    }

    @Override
    public long parentingHandle() {
        return fixedHandle;
    }

    @Override
    void register() {
        super.register();
        if (buttonHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(buttonHandle, this.getApi());
        if (entryHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(entryHandle, this.getApi());
        if (popupHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(popupHandle, this.getApi());
        if (menuHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(menuHandle, this.getApi());
        long imContext = imContext();
        if (imContext != 0)
            ((SwtDisplay) display.getImpl()).addWidget(imContext, this.getApi());
    }

    @Override
    void releaseHandle() {
        super.releaseHandle();
        if (menuHandle != 0) {
            menuHandle = 0;
        }
        if (buttonHandle != 0) {
            buttonHandle = 0;
        }
        if (buttonBoxHandle != 0) {
            buttonBoxHandle = 0;
        }
        if (cellBoxHandle != 0) {
            cellBoxHandle = 0;
        }
        entryHandle = 0;
        if (cssProvider != 0) {
            cssProvider = 0;
        }
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        textRenderer = 0;
        fixIM();
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
        if (!(0 <= index && index < items.length)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        String[] oldItems = items;
        String[] newItems = new String[oldItems.length - 1];
        System.arraycopy(oldItems, 0, newItems, 0, index);
        System.arraycopy(oldItems, index + 1, newItems, index, oldItems.length - index - 1);
        items = newItems;
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
        if (!(0 <= start && start <= end && end < items.length)) {
            error(SWT.ERROR_INVALID_RANGE);
        }
        String[] oldItems = items;
        String[] newItems = new String[oldItems.length - (end - start + 1)];
        System.arraycopy(oldItems, 0, newItems, 0, start);
        System.arraycopy(oldItems, end + 1, newItems, start, oldItems.length - end - 1);
        items = newItems;
        for (int i = end; i >= start; i--) {
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
        items = new String[0];
        clearText();
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
        checkWidget();
        if (index < 0 || index >= items.length)
            return;
        unselected = false;
    }

    @Override
    int setBounds(int x, int y, int width, int height, boolean move, boolean resize) {
        int newHeight = height;
        if (resize)
            newHeight = Math.max(getTextHeightInPixels(), height);
        return super.setBounds(x, y, width, newHeight, move, resize);
    }

    void setButtonHandle(long widget) {
        if (buttonHandle == widget)
            return;
        if (buttonHandle != 0) {
            ((SwtDisplay) display.getImpl()).removeWidget(buttonHandle);
        }
        buttonHandle = widget;
        if (buttonHandle != 0) {
            ((SwtDisplay) display.getImpl()).addWidget(buttonHandle, this.getApi());
            hookEvents(new long[] { buttonHandle });
        }
    }

    void setMenuHandle(long widget) {
        if (menuHandle == widget)
            return;
        if (menuHandle != 0) {
            ((SwtDisplay) display.getImpl()).removeWidget(menuHandle);
        }
        menuHandle = widget;
        if (menuHandle != 0) {
            ((SwtDisplay) display.getImpl()).addWidget(menuHandle, this.getApi());
            hookEvents(new long[] { menuHandle });
        }
    }

    @Override
    void setFontDescription(long font) {
        super.setFontDescription(font);
        if (entryHandle != 0)
            setFontDescription(entryHandle, font);
        if ((getApi().style & SWT.READ_ONLY) != 0) {
        }
    }

    @Override
    void setInitialBounds() {
        if ((getApi().state & ZERO_WIDTH) != 0 && (getApi().state & ZERO_HEIGHT) != 0) {
            if ((parent.style & SWT.MIRRORED) != 0) {
            } else {
            }
        } else {
            super.setInitialBounds();
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
        if (!(0 <= index && index < items.length)) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        items[index] = string;
        if (getApi().handle != 0) {
        }
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0 && popupHandle != 0) {
        }
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
        this.items = new String[items.length];
        System.arraycopy(items, 0, this.items, 0, items.length);
        clearText();
        for (int i = 0; i < items.length; i++) {
            if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0 && popupHandle != 0) {
            }
        }
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
        this.listVisible = visible;
        if (visible) {
        } else {
        }
    }

    @Override
    void setOrientation(boolean create) {
        dirty();
        super.setOrientation(create);
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0 || !create) {
            if (!create) {
            }
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
        super.setOrientation(orientation);
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
        this.selection = selection;
        if (selection == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if ((getApi().style & SWT.READ_ONLY) != 0)
            return;
        if (entryHandle != 0) {
        }
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
        dirty();
        checkWidget();
        this.text = string;
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if ((getApi().style & SWT.READ_ONLY) != 0) {
            int index = indexOf(string);
            if (index == -1)
                return;
            select(index);
            return;
        }
        /*
	* Feature in gtk.  When text is set in gtk, separate events are fired for the deletion and
	* insertion of the text.  This is not wrong, but is inconsistent with other platforms.  The
	* fix is to block the firing of these events and fire them ourselves in a consistent manner.
	*/
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            if (string == null)
                return;
        }
        sendEvent(SWT.Modify);
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
        this.textLimit = limit;
        if (limit == 0)
            error(SWT.ERROR_CANNOT_BE_ZERO);
    }

    @Override
    void setToolTipText(Shell shell, String newString) {
        if (entryHandle != 0)
            setToolTipText(entryHandle, newString);
        if (buttonHandle != 0)
            setToolTipText(buttonHandle, newString);
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
        visibleCount = count;
    }

    @Override
    boolean checkSubwindow() {
        return false;
    }

    @Override
    boolean translateTraversal(long event) {
        return super.translateTraversal(event);
    }

    void updateCss() {
        if (cssProvider == 0) {
            if (menuHandle != 0) {
            }
            if (buttonHandle != 0) {
            }
            if (entryHandle != 0) {
            }
        }
    }

    String verifyText(String string, int start, int end) {
        if (string.length() == 0 && start == end)
            return null;
        Event event = new Event();
        event.text = string;
        event.start = start;
        event.end = end;
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

    boolean listVisible;

    Point selection;

    String text = "";

    int textLimit;

    public long _buttonHandle() {
        return buttonHandle;
    }

    public long _entryHandle() {
        return entryHandle;
    }

    public long _textRenderer() {
        return textRenderer;
    }

    public long _cellHandle() {
        return cellHandle;
    }

    public long _popupHandle() {
        return popupHandle;
    }

    public long _menuHandle() {
        return menuHandle;
    }

    public long _buttonBoxHandle() {
        return buttonBoxHandle;
    }

    public long _cellBoxHandle() {
        return cellBoxHandle;
    }

    public long _arrowHandle() {
        return arrowHandle;
    }

    public int _lastEventTime() {
        return lastEventTime;
    }

    public int _visibleCount() {
        return visibleCount;
    }

    public long _imContext() {
        return imContext;
    }

    public int _fixStart() {
        return fixStart;
    }

    public int _fixEnd() {
        return fixEnd;
    }

    public String[] _items() {
        return items;
    }

    public int _indexSelected() {
        return indexSelected;
    }

    public long _cssProvider() {
        return cssProvider;
    }

    public boolean _firstDraw() {
        return firstDraw;
    }

    public boolean _unselected() {
        return unselected;
    }

    public boolean _fitModelToggled() {
        return fitModelToggled;
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
