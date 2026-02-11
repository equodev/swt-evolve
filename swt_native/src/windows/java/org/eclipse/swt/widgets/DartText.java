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

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

/**
 * Instances of this class are selectable user interface
 * objects that allow the user to enter and modify text.
 * Text controls can be either single or multi-line.
 * When a text control is created with a border, the
 * operating system includes a platform specific inset
 * around the contents of the control.  When created
 * without a border, an effort is made to remove the
 * inset such that the preferred size of the control
 * is the same size as the contents.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>CENTER, ICON_CANCEL, ICON_SEARCH, LEFT, MULTI, PASSWORD, SEARCH, SINGLE, RIGHT, READ_ONLY, WRAP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>DefaultSelection, Modify, Verify, OrientationChange</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles MULTI and SINGLE may be specified,
 * and only one of the styles LEFT, CENTER, and RIGHT may be specified.
 * </p>
 * <p>
 * Note: The styles ICON_CANCEL and ICON_SEARCH are hints used in combination with SEARCH.
 * When the platform supports the hint, the text control shows these icons.  When an icon
 * is selected, a default selection event is sent with the detail field set to one of
 * ICON_CANCEL or ICON_SEARCH.  Normally, application code does not need to check the
 * detail.  In the case of ICON_CANCEL, the text is cleared before the default selection
 * event is sent causing the application to search for an empty string.
 * </p>
 * <p>
 * Note: Some text actions such as Undo are not natively supported on all platforms.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#text">Text snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DartText extends DartScrollable implements IText {

    int tabs = 8, oldStart, oldEnd;

    boolean doubleClick, ignoreModify, ignoreVerify, ignoreCharacter, allowPasswordChar;

    String message;

    int[] segments;

    int clearSegmentsCount = 0;

    long hwndActiveIcon;

    static final char LTR_MARK = '\u200e';

    static final char RTL_MARK = '\u200f';

    /* Custom icons defined in swt.rc */
    static final int IDI_SEARCH = 101;

    static final int IDI_CANCEL = 102;

    static final int IDI_SEARCH_DARKTHEME = 103;

    static final int IDI_CANCEL_DARKTHEME = 104;

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
     * @see SWT#READ_ONLY
     * @see SWT#WRAP
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#CENTER
     * @see SWT#PASSWORD
     * @see SWT#SEARCH
     * @see SWT#ICON_SEARCH
     * @see SWT#ICON_CANCEL
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public DartText(Composite parent, int style, Text api) {
        super(parent, checkStyle(style), api);
    }

    @Override
    void createHandle() {
    }

    @Override
    int applyThemeBackground() {
        return (backgroundAlpha == 0 || (getApi().style & (SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL)) == 0) ? 1 : 0;
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
     * <b>Warning</b>: This API is currently only implemented on Windows and GTK.
     * <code>SegmentEvent</code>s won't be sent on Cocoa.
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
     * @since 3.8
     */
    public void addSegmentListener(SegmentListener listener) {
        addTypedListener(listener, SWT.Segments);
        clearSegments(true);
        applySegments();
    }

    /**
     * Adds the listener to the collection of listeners who will
     * be notified when the control is selected by the user, by sending
     * it one of the messages defined in the <code>SelectionListener</code>
     * interface.
     * <p>
     * <code>widgetSelected</code> is not called for texts.
     * <code>widgetDefaultSelected</code> is typically called when ENTER is pressed in a single-line text,
     * or when ENTER is pressed in a search text. If the receiver has the <code>SWT.SEARCH | SWT.ICON_CANCEL</code> style
     * and the user cancels the search, the event object detail field contains the value <code>SWT.ICON_CANCEL</code>.
     * Likewise, if the receiver has the <code>SWT.ICON_SEARCH</code> style and the icon search is selected, the
     * event object detail field contains the value <code>SWT.ICON_SEARCH</code>.
     * </p>
     *
     * @param listener the listener which should be notified when the control is selected by the user
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
     */
    public void addVerifyListener(VerifyListener listener) {
        addTypedListener(listener, SWT.Verify);
    }

    /**
     * Appends a string.
     * <p>
     * The new text is appended to the text at
     * the end of the widget.
     * </p>
     *
     * @param string the string to be appended
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void append(String string) {
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        string = SwtDisplay.withCrLf(string);
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            int charCount = getCharCount();
            string = verifyText(string, charCount, charCount);
            if (string == null)
                return;
        }
        setSelection(getCharCount());
        clearSegments(true);
        insertEditText(string);
        applySegments();
        if (string.length() != 0)
            sendEvent(SWT.Modify);
    }

    void applySegments() {
        TextHelper.applySegments(this);
    }

    static int checkStyle(int style) {
        if ((style & SWT.SINGLE) != 0 && (style & SWT.MULTI) != 0) {
            style &= ~SWT.MULTI;
        }
        style = checkBits(style, SWT.LEFT, SWT.CENTER, SWT.RIGHT, 0, 0, 0);
        /*
	 * NOTE: ICON_CANCEL and ICON_SEARCH have the same value as H_SCROLL and
	 * V_SCROLL. The meaning is determined by whether SWT.SEARCH is set.
	 */
        if ((style & SWT.SEARCH) != 0) {
            style |= SWT.SINGLE | SWT.BORDER;
            style &= ~(SWT.PASSWORD | SWT.WRAP);
        } else if ((style & SWT.SINGLE) != 0) {
            style &= ~(SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
        }
        if ((style & SWT.WRAP) != 0) {
            style |= SWT.MULTI;
            style &= ~SWT.H_SCROLL;
        }
        if ((style & SWT.MULTI) != 0)
            style &= ~SWT.PASSWORD;
        if ((style & (SWT.SINGLE | SWT.MULTI)) != 0)
            return style;
        if ((style & (SWT.H_SCROLL | SWT.V_SCROLL)) != 0)
            return style | SWT.MULTI;
        return style | SWT.SINGLE;
    }

    void clearSegments(boolean applyText) {
        TextHelper.clearSegments(this, applyText);
    }

    /**
     * Clears the selection.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void clearSelection() {
        checkWidget();
        Point sel = getSelection();
        if (sel != null && sel.x != sel.y) {
            setSelection(sel.x);
        }
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.computeSize(this, wHint, hHint, changed);
    }

    @Override
    Rectangle computeTrimInPixels(int x, int y, int width, int height) {
        return Sizes.computeTrim(this, x, y, width, height);
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
     */
    public void copy() {
        checkWidget();
        TextHelper.copy(this);
    }

    @Override
    ScrollBar createScrollBar(int type) {
        return (getApi().style & SWT.SEARCH) == 0 ? super.createScrollBar(type) : null;
    }

    @Override
    void createWidget() {
        super.createWidget();
        message = "";
        doubleClick = true;
        setTabStops(tabs = 8);
        fixAlignment();
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
     */
    public void cut() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0)
            return;
        TextHelper.cut(this);
    }

    @Override
    int defaultBackground() {
        return 0;
    }

    @Override
    void maybeEnableDarkSystemTheme() {
        /*
	 * Feature in Windows. If the control has default foreground and
	 * background, the background gets black without focus and white with
	 * focus, but the foreground color always stays black.
	 */
        if (hasCustomBackground() || hasCustomForeground()) {
            super.maybeEnableDarkSystemTheme();
        }
    }

    void fixAlignment() {
        /*
	* Feature in Windows.  When the edit control is not
	* mirrored, it uses WS_EX_RIGHT, WS_EX_RTLREADING and
	* WS_EX_LEFTSCROLLBAR to give the control a right to
	* left appearance.  This causes the control to be lead
	* aligned no matter what alignment was specified by
	* the programmer.  For example, setting ES_RIGHT and
	* WS_EX_LAYOUTRTL should cause the contents of the
	* control to be left (trail) aligned in a mirrored world.
	* When the orientation is changed by the user or
	* specified by the programmer, WS_EX_RIGHT conflicts
	* with the mirrored alignment.  The fix is to clear
	* or set WS_EX_RIGHT to achieve the correct alignment
	* according to the orientation and mirroring.
	*/
        if ((getApi().style & SWT.MIRRORED) != 0)
            return;
        if ((getApi().style & SWT.LEFT_TO_RIGHT) != 0) {
            if ((getApi().style & SWT.RIGHT) != 0) {
            }
            if ((getApi().style & SWT.LEFT) != 0) {
            }
        } else {
            if ((getApi().style & SWT.RIGHT) != 0) {
            }
            if ((getApi().style & SWT.LEFT) != 0) {
            }
        }
        if ((getApi().style & SWT.CENTER) != 0) {
        }
    }

    @Override
    int getBorderWidthInPixels() {
        checkWidget();
        return super.getBorderWidthInPixels();
    }

    /**
     * Returns the line number of the caret.
     * <p>
     * The line number of the caret is returned.
     * </p>
     *
     * @return the line number
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getCaretLineNumber() {
        checkWidget();
        return TextHelper.getCaretLineNumber(this);
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
     */
    public Point getCaretLocation() {
        checkWidget();
        return null;
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
     */
    public int getCaretPosition() {
        checkWidget();
        if (selection != null) {
            return selection.x;
        }
        return 0;
    }

    /**
     * Returns the number of characters.
     *
     * @return number of characters in the widget
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getCharCount() {
        checkWidget();
        String currentText = getText();
        return currentText != null ? currentText.length() : 0;
    }

    /**
     * Returns the double click enabled flag.
     * <p>
     * The double click flag enables or disables the
     * default action of the text widget when the user
     * double clicks.
     * </p>
     *
     * @return whether or not double click is enabled
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getDoubleClickEnabled() {
        checkWidget();
        return doubleClick;
    }

    /**
     * Returns the echo character.
     * <p>
     * The echo character is the character that is
     * displayed when the user enters text or the
     * text is changed by the programmer.
     * </p>
     *
     * @return the echo character
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setEchoChar
     */
    public char getEchoChar() {
        checkWidget();
        return this.echoCharacter;
    }

    /**
     * Returns the editable state.
     *
     * @return whether or not the receiver is editable
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getEditable() {
        checkWidget();
        return this.editable;
    }

    /**
     * Returns the number of lines.
     *
     * @return the number of lines in the widget
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getLineCount() {
        checkWidget();
        return TextHelper.getLineCount(this);
    }

    /**
     * Returns the line delimiter.
     *
     * @return a string that is the line delimiter
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #DELIMITER
     */
    public String getLineDelimiter() {
        checkWidget();
        return Text.DELIMITER;
    }

    /**
     * Returns the height of a line.
     *
     * @return the height of a row of text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getLineHeight() {
        checkWidget();
        return DPIUtil.pixelToPoint(getLineHeightInPixels(), getZoom());
    }

    int getLineHeightInPixels() {
        return 0;
    }

    /**
     * Returns the orientation of the receiver, which will be one of the
     * constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
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
     * Returns the widget message.  The message text is displayed
     * as a hint for the user, indicating the purpose of the field.
     * <p>
     * Typically this is used in conjunction with <code>SWT.SEARCH</code>.
     * </p>
     *
     * @return the widget message
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    public String getMessage() {
        checkWidget();
        return message;
    }

    /**
     * Returns the character position at the given point in the receiver
     * or -1 if no such position exists. The point is in the coordinate
     * system of the receiver.
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
     * @since 3.3
     */
    //TODO - Javadoc
    /*public*/
    int getPosition(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        return 0;
    }

    /**
     * Returns a <code>Point</code> whose x coordinate is the
     * character position representing the start of the selected
     * text, and whose y coordinate is the character position
     * representing the end of the selection. An "empty" selection
     * is indicated by the x and y coordinates having the same value.
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
        if (selection == null) {
            return new Point(caretPosition, caretPosition);
        }
        return new Point(untranslateOffset(selection.x), untranslateOffset(selection.y));
    }

    /**
     * Returns the number of selected characters.
     *
     * @return the number of selected characters.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getSelectionCount() {
        checkWidget();
        Point selection = getSelection();
        return selection.y - selection.x;
    }

    /**
     * Gets the selected text, or an empty string if there is no current selection.
     *
     * @return the selected text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getSelectionText() {
        checkWidget();
        Point selection = getSelection();
        if (selection.x == selection.y)
            return "";
        return new String(getEditText(selection.x, selection.y - 1));
    }

    /**
     * Returns the number of tabs.
     * <p>
     * Tab stop spacing is specified in terms of the
     * space (' ') character.  The width of a single
     * tab stop is the pixel width of the spaces.
     * </p>
     *
     * @return the number of tab characters
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getTabs() {
        checkWidget();
        return tabs;
    }

    int getTabWidth(int tabs) {
        return 0;
    }

    /**
     * Returns the widget text.
     * <p>
     * The text for a text widget is the characters in the widget, or
     * an empty string if this has never been set.
     * </p>
     *
     * @return the widget text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getText() {
        checkWidget();
        return this.text != null ? this.text : "";
    }

    /**
     * Returns the widget's text as a character array.
     * <p>
     * The text for a text widget is the characters in the widget, or
     * a zero-length array if this has never been set.
     * </p>
     * <p>
     * Note: Use this API to prevent the text from being written into a String
     * object whose lifecycle is outside of your control. This can help protect
     * the text, for example, when the widget is used as a password field.
     * However, the text can't be protected if an {@link SWT#Segments} or
     * {@link SWT#Verify} listener has been added to the widget.
     * </p>
     *
     * @return a character array that contains the widget's text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setTextChars(char[])
     *
     * @since 3.7
     */
    public char[] getTextChars() {
        checkWidget();
        return this.textChars;
    }

    /**
     * Returns a range of text.  Returns an empty string if the
     * start of the range is greater than the end.
     * <p>
     * Indexing is zero based.  The range of
     * a selection is from 0..N-1 where N is
     * the number of characters in the widget.
     * </p>
     *
     * @param start the start of the range
     * @param end the end of the range
     * @return the range of text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getText(int start, int end) {
        checkWidget();
        if (!(start <= end && 0 <= end))
            return "";
        if (start > end)
            return "";
        start = Math.max(0, start);
        String str = getText();
        start = Math.min(start, str.length());
        /*
	* NOTE: The current implementation uses substring ()
	* which can reference a potentially large character
	* array.
	*/
        return str.substring(start, Math.min(end + 1, str.length()));
    }

    /**
     * Returns the maximum number of characters that the receiver is capable of holding.
     * <p>
     * If this has not been changed by <code>setTextLimit()</code>,
     * it will be the constant <code>Text.LIMIT</code>.
     * </p>
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
     * Returns the zero-relative index of the line which is currently
     * at the top of the receiver.
     * <p>
     * This index can change when lines are scrolled or new lines are added or removed.
     * </p>
     *
     * @return the index of the top line
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getTopIndex() {
        checkWidget();
        if ((getApi().style & SWT.SINGLE) != 0)
            return 0;
        return this.topIndex;
    }

    /**
     * Returns the top SWT logical point.
     * <p>
     * The top point is the SWT logical point position of the line
     * that is currently at the top of the widget.  On
     * some platforms, a text widget can be scrolled by
     * points instead of lines so that a partial line
     * is displayed at the top of the widget.
     * </p><p>
     * The top SWT logical point changes when the widget is scrolled.
     * The top SWT logical point does not include the widget trimming.
     * </p>
     *
     * @return the SWT logical point position of the top line
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getTopPixel() {
        checkWidget();
        return DPIUtil.pixelToPoint(getTopPixelInPixels(), getZoom());
    }

    int getTopPixelInPixels() {
        return getTopIndex() * getLineHeightInPixels();
    }

    /**
     * Inserts a string.
     * <p>
     * The old selection is replaced with the new text.
     * </p>
     *
     * @param string the string
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is <code>null</code></li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void insert(String string) {
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        string = SwtDisplay.withCrLf(string);
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            Point selection = getSelection();
            string = verifyText(string, selection.x, selection.y);
            if (string == null)
                return;
        }
        clearSegments(true);
        ignoreCharacter = true;
        insertEditText(string);
        ignoreCharacter = false;
        if ((getApi().state & HAS_AUTO_DIRECTION) != 0) {
            super.updateTextDirection(AUTO_TEXT_DIRECTION);
        }
        applySegments();
        if (string.length() != 0)
            sendEvent(SWT.Modify);
    }

    @Override
    boolean isUseWsBorder() {
        return super.isUseWsBorder() || ((display != null) && ((SwtDisplay) display.getImpl()).useWsBorderText);
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
     */
    public void paste() {
        checkWidget();
        if ((getApi().style & SWT.READ_ONLY) != 0)
            return;
        TextHelper.paste(this);
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        message = null;
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
     * @since 3.8
     */
    public void removeSegmentListener(SegmentListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        eventTable.unhook(SWT.Segments, listener);
        clearSegments(true);
        applySegments();
    }

    /**
     * Removes the listener from the collection of listeners who will
     * be notified when the control is selected by the user.
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
     */
    public void removeVerifyListener(VerifyListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Verify, listener);
    }

    @Override
    int resolveTextDirection() {
        int textDirection = SWT.NONE;
        return textDirection;
    }

    /**
     * Selects all the text in the receiver.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void selectAll() {
        checkWidget();
        setSelection(0, getCharCount());
    }

    @Override
    void setBackgroundImage(long hBitmap) {
    }

    @Override
    void setBackgroundPixel(int pixel) {
        maybeEnableDarkSystemTheme();
    }

    @Override
    void setBoundsInPixels(int x, int y, int width, int height, int flags) {
        super.setBoundsInPixels(x, y, width, height, flags);
    }

    @Override
    void setDefaultFont() {
        super.setDefaultFont();
        setMargins();
    }

    /**
     * Sets the double click enabled flag.
     * <p>
     * The double click flag enables or disables the
     * default action of the text widget when the user
     * double clicks.
     * </p><p>
     * Note: This operation is a hint and is not supported on
     * platforms that do not have this concept.
     * </p>
     *
     * @param doubleClick the new double click flag
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setDoubleClickEnabled(boolean doubleClick) {
        checkWidget();
        if (!java.util.Objects.equals(this.doubleClick, doubleClick)) {
            dirty();
        }
        this.doubleClick = doubleClick;
    }

    /**
     * Sets the echo character.
     * <p>
     * The echo character is the character that is
     * displayed when the user enters text or the
     * text is changed by the programmer. Setting
     * the echo character to '\0' clears the echo
     * character and redraws the original text.
     * If for any reason the echo character is invalid,
     * or if the platform does not allow modification
     * of the echo character, the default echo character
     * for the platform is used.
     * </p>
     *
     * @param echo the new echo character
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setEchoChar(char echo) {
        char newValue = echo;
        if (!java.util.Objects.equals(this.echoCharacter, newValue)) {
            dirty();
        }
        checkWidget();
        if ((getApi().style & SWT.MULTI) != 0)
            return;
        allowPasswordChar = true;
        allowPasswordChar = false;
        this.echoCharacter = newValue;
    }

    /**
     * Sets the editable state.
     *
     * @param editable the new editable state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setEditable(boolean editable) {
        boolean newValue = editable;
        if (!java.util.Objects.equals(this.editable, newValue)) {
            dirty();
        }
        checkWidget();
        getApi().style &= ~SWT.READ_ONLY;
        if (!editable)
            getApi().style |= SWT.READ_ONLY;
        this.editable = newValue;
    }

    @Override
    public void setFont(Font font) {
        dirty();
        checkWidget();
        super.setFont(font);
        setTabStops(tabs);
        setMargins();
    }

    @Override
    void setForegroundPixel(int pixel) {
        maybeEnableDarkSystemTheme();
        super.setForegroundPixel(pixel);
    }

    void setMargins() {
        if ((getApi().style & SWT.SEARCH) != 0) {
            int flags = 0;
            if (flags != 0) {
            }
        }
    }

    /**
     * Sets the widget message. The message text is displayed
     * as a hint for the user, indicating the purpose of the field.
     * <p>
     * Typically this is used in conjunction with <code>SWT.SEARCH</code>.
     * </p>
     *
     * @param message the new message
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the message is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.3
     */
    public void setMessage(String message) {
        checkWidget();
        if (!java.util.Objects.equals(this.message, message)) {
            dirty();
        }
        if (message == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        this.message = message;
    }

    /**
     * Sets the orientation of the receiver, which must be one
     * of the constants <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
     * <p>
     * Note: This operation is a hint and is not supported on
     * platforms that do not have this concept.
     * </p>
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
     * Sets the selection.
     * <p>
     * Indexing is zero based.  The range of
     * a selection is from 0..N where N is
     * the number of characters in the widget.
     * </p><p>
     * Text selections are specified in terms of
     * caret positions.  In a text widget that
     * contains N characters, there are N+1 caret
     * positions, ranging from 0..N.  This differs
     * from other functions that address character
     * position such as getText () that use the
     * regular array indexing rules.
     * </p>
     *
     * @param start new caret position
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(int start) {
        checkWidget();
        setSelection(start, start);
    }

    /**
     * Sets the selection to the range specified
     * by the given start and end indices.
     * <p>
     * Indexing is zero based.  The range of
     * a selection is from 0..N where N is
     * the number of characters in the widget.
     * </p><p>
     * Text selections are specified in terms of
     * caret positions.  In a text widget that
     * contains N characters, there are N+1 caret
     * positions, ranging from 0..N.  This differs
     * from other functions that address character
     * position such as getText () that use the
     * usual array indexing rules.
     * </p>
     *
     * @param start the start of the range
     * @param end the end of the range
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(int start, int end) {
        checkWidget();
        start = translateOffset(start);
        end = translateOffset(end);
        int length = getText().length();
        if (segments != null) {
            length += segments.length;
        }
        int min = Math.min(Math.max(Math.min(start, end), 0), length);
        int max = Math.min(Math.max(Math.max(start, end), 0), length);
        Point newValue = new Point(min, max);
        if (!java.util.Objects.equals(this.selection, newValue)) {
            dirty();
        }
        this.selection = newValue;
        // the caret is in the start of the selection
        this.caretPosition = min;
    }

    @Override
    public void setRedraw(boolean redraw) {
        dirty();
        checkWidget();
        super.setRedraw(redraw);
        /*
	* Feature in Windows.  When WM_SETREDRAW is used to turn
	* redraw off, the edit control is not scrolled to show the
	* i-beam.  The fix is to detect that the i-beam has moved
	* while redraw is turned off and force it to be visible
	* when redraw is restored.
	*/
        if (!getDrawing())
            return;
        int[] start = new int[1], end = new int[1];
        if (!redraw) {
            oldStart = start[0];
            oldEnd = end[0];
        } else {
            if (oldStart == start[0] && oldEnd == end[0])
                return;
        }
    }

    /**
     * Sets the selection to the range specified
     * by the given point, where the x coordinate
     * represents the start index and the y coordinate
     * represents the end index.
     * <p>
     * Indexing is zero based.  The range of
     * a selection is from 0..N where N is
     * the number of characters in the widget.
     * </p><p>
     * Text selections are specified in terms of
     * caret positions.  In a text widget that
     * contains N characters, there are N+1 caret
     * positions, ranging from 0..N.  This differs
     * from other functions that address character
     * position such as getText () that use the
     * usual array indexing rules.
     * </p>
     *
     * @param selection the point
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
        if (selection == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        setSelection(selection.x, selection.y);
    }

    /**
     * Sets the number of tabs.
     * <p>
     * Tab stop spacing is specified in terms of the
     * space (' ') character.  The width of a single
     * tab stop is the pixel width of the spaces.
     * </p>
     *
     * @param tabs the number of tabs
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setTabs(int tabs) {
        checkWidget();
        if (!java.util.Objects.equals(this.tabs, tabs)) {
            dirty();
        }
        if (tabs < 0)
            return;
        setTabStops(this.tabs = tabs);
    }

    void setTabStops(int tabs) {
    }

    /**
     * Sets the contents of the receiver to the given string. If the receiver has style
     * SINGLE and the argument contains multiple lines of text, the result of this
     * operation is undefined and may vary from platform to platform.
     * <p>
     * Note: If control characters like '\n', '\t' etc. are used
     * in the string, then the behavior is platform dependent.
     * </p>
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
        String newValue = string;
        if (!java.util.Objects.equals(this.text, newValue)) {
            dirty();
        }
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        string = SwtDisplay.withCrLf(string);
        TextHelper.setText(this, string);
    }

    /**
     * Sets the contents of the receiver to the characters in the array. If the receiver
     * has style <code>SWT.SINGLE</code> and the argument contains multiple lines of text
     * then the result of this operation is undefined and may vary between platforms.
     * <p>
     * Note: Use this API to prevent the text from being written into a String
     * object whose lifecycle is outside of your control. This can help protect
     * the text, for example, when the widget is used as a password field.
     * However, the text can't be protected if an {@link SWT#Segments} or
     * {@link SWT#Verify} listener has been added to the widget.
     * </p>
     *
     * @param text a character array that contains the new text
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the array is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getTextChars()
     *
     * @since 3.7
     */
    public void setTextChars(char[] text) {
        char[] newValue = text;
        if (!java.util.Objects.equals(this.textChars, newValue)) {
            dirty();
        }
        checkWidget();
        if (text == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        text = SwtDisplay.withCrLf(text);
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
        }
        clearSegments(false);
        if ((getApi().state & HAS_AUTO_DIRECTION) != 0) {
            super.updateTextDirection(AUTO_TEXT_DIRECTION);
        }
        applySegments();
        this.textChars = newValue;
    }

    /**
     * Sets the maximum number of characters that the receiver
     * is capable of holding to be the argument.
     * <p>
     * Instead of trying to set the text limit to zero, consider
     * creating a read-only text widget.
     * </p><p>
     * To reset this value to the default, use <code>setTextLimit(Text.LIMIT)</code>.
     * Specifying a limit value larger than <code>Text.LIMIT</code> sets the
     * receiver's limit to <code>Text.LIMIT</code>.
     * </p>
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
     *
     * @see #LIMIT
     */
    public void setTextLimit(int limit) {
        int newValue = limit;
        if (!java.util.Objects.equals(this.textLimit, newValue)) {
            dirty();
        }
        checkWidget();
        if (limit == 0)
            error(SWT.ERROR_CANNOT_BE_ZERO);
        this.textLimit = newValue;
        if (segments != null && limit > 0) {
        } else {
        }
    }

    /**
     * Sets the zero-relative index of the line which is currently
     * at the top of the receiver. This index can change when lines
     * are scrolled or new lines are added and removed.
     *
     * @param index the index of the top item
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setTopIndex(int index) {
        //int newValue = index;
        ;
        //if (!java.util.Objects.equals(this.topIndex, newValue)) {    dirty();}
        ;
        checkWidget();
        if ((getApi().style & SWT.SINGLE) != 0)
            return;
        int count = getLineCount();
        int newValue = Math.min(Math.max(index, 0), count - 1);
        if (!java.util.Objects.equals(this.topIndex, newValue)) {
            dirty();
        }
        this.topIndex = newValue;
    }

    /**
     * Shows the selection.
     * <p>
     * If the selection is already showing
     * in the receiver, this method simply returns.  Otherwise,
     * lines are scrolled until the selection is visible.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void showSelection() {
        checkWidget();
    }

    int translateOffset(int offset) {
        if (segments == null)
            return offset;
        for (int i = 0, nSegments = segments.length; i < nSegments && offset - i >= segments[i]; i++) {
            offset++;
        }
        return offset;
    }

    int untranslateOffset(int offset) {
        if (segments == null)
            return offset;
        for (int i = 0, nSegments = segments.length; i < nSegments && offset > segments[i]; i++) {
            offset--;
        }
        return offset;
    }

    @Override
    void updateMenuLocation(Event event) {
        Point pointInPixels = ((SwtDisplay) display.getImpl()).mapInPixels(this.getApi(), null, getCaretLocationInPixels());
        int zoom = getZoom();
        event.setLocation(DPIUtil.pixelToPoint(pointInPixels.x, zoom), DPIUtil.pixelToPoint(pointInPixels.y + getLineHeightInPixels(), zoom));
    }

    @Override
    void updateOrientation() {
        if ((getApi().style & SWT.RIGHT_TO_LEFT) != 0) {
        } else {
        }
        fixAlignment();
    }

    @Override
    boolean updateTextDirection(int textDirection) {
        if (super.updateTextDirection(textDirection)) {
            clearSegments(true);
            applySegments();
            return true;
        }
        return false;
    }

    String verifyText(String string, int start, int end, Event keyEvent) {
        if (ignoreVerify)
            return string;
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
    int widgetStyle() {
        /*
	 * NOTE: ICON_CANCEL and ICON_SEARCH have the same value as H_SCROLL and
	 * V_SCROLL. The meaning is determined by whether SWT.SEARCH is set.
	 */
        if ((getApi().style & SWT.SEARCH) != 0) {
        }
        if ((getApi().style & SWT.SINGLE) != 0) {
            /*
		* Feature in Windows.  When a text control is read-only,
		* uses COLOR_3DFACE for the background .  If the text
		* controls single-line and is within a tab folder or
		* some other themed control, using WM_ERASEBKGND and
		* WM_CTRCOLOR to draw the theme background results in
		* pixel corruption.  The fix is to use an ES_MULTILINE
		* text control instead.
		* Refer Bug438901:- ES_MULTILINE doesn't apply for:
		* SWT.PASSWORD | SWT.READ_ONLY style combination.
		*/
            if ((getApi().style & SWT.READ_ONLY) != 0) {
                if ((getApi().style & (SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.PASSWORD)) == 0) {
                }
            }
        }
        return 0;
    }

    private static void handleDPIChange(Widget widget, int newZoom, float scalingFactor) {
        if (!(widget instanceof Text text)) {
            return;
        }
        ((DartText) text.getImpl()).setMargins();
    }

    int caretPosition;

    char echoCharacter;

    boolean editable = true;

    char[] hiddenText;

    Point selection;

    String text = "";

    char[] textChars = new char[0];

    int textLimit = Text.LIMIT;

    int topIndex;

    public int _tabs() {
        return tabs;
    }

    public int _oldStart() {
        return oldStart;
    }

    public int _oldEnd() {
        return oldEnd;
    }

    public boolean _doubleClick() {
        return doubleClick;
    }

    public boolean _ignoreModify() {
        return ignoreModify;
    }

    public boolean _ignoreVerify() {
        return ignoreVerify;
    }

    public boolean _ignoreCharacter() {
        return ignoreCharacter;
    }

    public boolean _allowPasswordChar() {
        return allowPasswordChar;
    }

    public String _message() {
        return message;
    }

    public int[] _segments() {
        return segments;
    }

    public int _clearSegmentsCount() {
        return clearSegmentsCount;
    }

    public long _hwndActiveIcon() {
        return hwndActiveIcon;
    }

    public int _caretPosition() {
        return caretPosition;
    }

    public char _echoCharacter() {
        return echoCharacter;
    }

    public boolean _editable() {
        return editable;
    }

    public char[] _hiddenText() {
        return hiddenText;
    }

    public Point _selection() {
        return selection;
    }

    public String _text() {
        return text;
    }

    public char[] _textChars() {
        return textChars;
    }

    public int _textLimit() {
        return textLimit;
    }

    public int _topIndex() {
        return topIndex;
    }

    void updateAutoTextDirectionIfNeeded() {
        if ((getApi().state & HAS_AUTO_DIRECTION) != 0) {
            updateTextDirection(AUTO_TEXT_DIRECTION);
        }
    }

    String verifyText(String string, int start, int end) {
        Event event = new Event();
        event.text = string;
        event.start = start;
        event.end = end;
        /*
                     * It is possible (but unlikely), that application
                     * code could have disposed the widget in the verify
                     * event. If this happens, answer null to cancel
                     * the operation.
                     */
        sendEvent(SWT.Verify, event);
        if (!event.doit || isDisposed())
            return null;
        return event.text;
    }

    void insertEditText(String string) {
        _insertEditText(string, false);
    }

    void _insertEditText(String string, boolean enableUndo) {
        TextHelper.insertEditText(this, string);
    }

    void setEditText(String string) {
        char[] text = new char[string.length()];
        string.getChars(0, text.length, text, 0);
        setEditText(text);
    }

    void setEditText(char[] text) {
        char[] buffer;
        int length = Math.min(text.length, textLimit);
        if ((getApi().style & SWT.PASSWORD) == 0 && echoCharacter != '\0') {
            hiddenText = new char[length];
            buffer = new char[length];
            for (int i = 0; i < length; i++) {
                hiddenText[i] = text[i];
                buffer[i] = echoCharacter;
            }
        } else {
            hiddenText = null;
            buffer = text;
        }
        this.text = new String(buffer, 0, Math.min(buffer.length, length));
    }

    char[] getEditText() {
        if (hiddenText != null) {
            char[] text = new char[hiddenText.length];
            System.arraycopy(hiddenText, 0, text, 0, text.length);
            return text;
        }
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        String str = this.text != null ? this.text : "";
        char[] result = new char[str.length()];
        str.getChars(0, result.length, result, 0);
        return result;
    }

    char[] getEditText(int start, int end) {
        String str = getText();
        int length = str.length();
        end = Math.min(end, length - 1);
        if (start > end)
            return new char[0];
        start = Math.max(0, start);
        int rangeLength = Math.max(0, end - start + 1);
        char[] buffer = new char[rangeLength];
        if (hiddenText != null) {
            System.arraycopy(hiddenText, start, buffer, 0, buffer.length);
        } else {
            str.getChars(start, start + rangeLength, buffer, 0);
        }
        return buffer;
    }

    protected void _hookEvents() {
        super._hookEvents();
        FlutterBridge.on(this, "Modify", "Modify", e -> {
            getDisplay().asyncExec(() -> {
                if (!isDisposed()) {
                    setText(e.text);
                    if (e.start >= 0)
                        setSelection(e.start);
                }
            });
        });
        FlutterBridge.on(this, "Segment", "Segments", e -> {
            getDisplay().asyncExec(() -> {
                if (!isDisposed()) {
                    sendEvent(SWT.Segments, e);
                }
            });
        });
        FlutterBridge.on(this, "Selection", "DefaultSelection", e -> {
            getDisplay().asyncExec(() -> {
                if (!isDisposed()) {
                    sendEvent(SWT.DefaultSelection, e);
                }
            });
        });
        FlutterBridge.on(this, "Selection", "Selection", e -> {
            getDisplay().asyncExec(() -> {
                if (!isDisposed()) {
                    setSelection(e.index);
                }
            });
        });
        FlutterBridge.on(this, "Verify", "Verify", e -> {
            getDisplay().asyncExec(() -> {
                if (!isDisposed()) {
                    sendEvent(SWT.Verify, e);
                }
            });
        });
    }

    public Text getApi() {
        if (api == null)
            api = Text.createApi(this);
        return (Text) api;
    }

    public VText getValue() {
        if (value == null)
            value = new VText(this);
        return (VText) value;
    }
}
