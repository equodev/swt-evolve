/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
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
import org.eclipse.swt.widgets.Display.*;
import java.util.Objects;
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

    int textLimit = Text.LIMIT, tabs = 8;

    char echoCharacter;

    boolean doubleClick, receivingFocus;

    char[] hiddenText;

    String message;

    long actionSearch, actionCancel;

    APPEARANCE lastAppAppearance;

    static final char PASSWORD = '\u2022';

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
        selection = new Point(0, 0);
        if ((style & SWT.SEARCH) != 0) {
            if ((style & SWT.ICON_CANCEL) != 0) {
                this.getApi().style |= SWT.ICON_CANCEL;
            } else {
            }
            if ((style & SWT.ICON_SEARCH) != 0) {
                this.getApi().style |= SWT.ICON_SEARCH;
            } else {
            }
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
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            int charCount = getCharCount();
            string = verifyText(string, charCount, charCount);
            if (string == null)
                return;
        }
        setSelection(getCharCount());
        insertEditText(string);
        if (string.length() != 0)
            sendEvent(SWT.Modify);
    }

    static int checkStyle(int style) {
        if ((style & SWT.SEARCH) != 0) {
            style |= SWT.SINGLE | SWT.BORDER;
            style &= ~SWT.PASSWORD;
            /*
		* NOTE: ICON_CANCEL has the same value as H_SCROLL and
		* ICON_SEARCH has the same value as V_SCROLL so they are
		* cleared because SWT.SINGLE is set.
		*/
        }
        if ((style & SWT.SINGLE) != 0 && (style & SWT.MULTI) != 0) {
            style &= ~SWT.MULTI;
        }
        style = checkBits(style, SWT.LEFT, SWT.CENTER, SWT.RIGHT, 0, 0, 0);
        if ((style & SWT.SINGLE) != 0)
            style &= ~(SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
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
        Point selection = getSelection();
        if (selection != null) {
            setSelection(selection.x);
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    @Override
    public Rectangle computeTrim(int x, int y, int width, int height) {
        Rectangle result = super.computeTrim(x, y, width, height);
        if ((getApi().style & SWT.SINGLE) != 0) {
            if ((getApi().style & SWT.SEARCH) != 0) {
            }
        }
        return result;
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
        if ((getApi().style & SWT.PASSWORD) != 0 || echoCharacter != '\0')
            return;
        if ((getApi().style & SWT.SINGLE) != 0) {
            Point selection = getSelection();
            if (selection.x == selection.y)
                return;
            copyToClipboard(getEditText(selection.x, selection.y - 1));
        } else {
        }
    }

    @Override
    void createHandle() {
    }

    @Override
    void createWidget() {
        super.createWidget();
        if ((getApi().style & SWT.PASSWORD) != 0) {
        }
        doubleClick = true;
        message = "";
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
        if ((getApi().style & SWT.PASSWORD) != 0 || echoCharacter != '\0')
            return;
        boolean cut = true;
        char[] oldText = null;
        Point oldSelection = getSelection();
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            if (oldSelection.x != oldSelection.y) {
                oldText = getEditText(oldSelection.x, oldSelection.y - 1);
            }
        }
        if (cut) {
            if ((getApi().style & SWT.SINGLE) != 0) {
                if (oldText == null)
                    oldText = getEditText(oldSelection.x, oldSelection.y - 1);
                copyToClipboard(oldText);
                insertEditText("");
            } else {
            }
        }
        Point newSelection = getSelection();
        if (!cut || !oldSelection.equals(newSelection))
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        }
    }

    @Override
    boolean dragDetect(int x, int y, boolean filter, boolean[] consume) {
        Point selection = getSelection();
        if (selection.x != selection.y) {
            long position = getPosition(x, y);
            if (selection.x <= position && position < selection.y) {
                if (super.dragDetect(x, y, filter, consume)) {
                    if (consume != null)
                        consume[0] = true;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    void enableWidget(boolean enabled) {
        super.enableWidget(enabled);
        if ((getApi().style & SWT.MULTI) != 0) {
            setForeground(this.foreground);
        }
    }

    @Override
    public Cursor findCursor() {
        Cursor cursor = super.findCursor();
        return cursor;
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
        if ((getApi().style & SWT.SINGLE) != 0)
            return 0;
        return (getTopPixel() + getCaretLocation().y) / getLineHeight();
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
        if ((getApi().style & SWT.SINGLE) != 0) {
            if (this.hasFocus()) {
            }
        } else {
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
        return echoCharacter;
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
        return (getApi().style & SWT.READ_ONLY) == 0;
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
        if ((getApi().style & SWT.SINGLE) != 0)
            return 1;
        String string = getText();
        int length = string.length();
        // Empty string = 1 empty line
        if (length == 0)
            return 1;
        // Start with 1 (first line)
        int count = 1;
        for (int i = 0; i < length; i++) {
            char c = string.charAt(i);
            if (c == '\n') {
                count++;
            } else if (c == '\r') {
                count++;
                // If it's \r\n, skip the \n to avoid counting twice
                if (i + 1 < length && string.charAt(i + 1) == '\n') {
                    i++;
                }
            }
        }
        return count;
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
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
        checkWidget();
        return getApi().style & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
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

    long getPosition(long x, long y) {
        //	checkWidget ();
        if ((getApi().style & SWT.MULTI) != 0) {
        } else {
            //TODO
            return 0;
        }
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
            return new Point(0, 0);
        }
        return this.selection;
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
        //$NON-NLS-1$
        if (!(start <= end && 0 <= end))
            return "";
        ;
        //$NON-NLS-1$
        if (start > end)
            return "";
        start = Math.max(0, start);
        start = Math.min(start, this.text.length());
        return this.text.substring(start, Math.min(end + 1, this.text.length()));
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
        if ((getApi().style & SWT.SINGLE) != 0) {
            return getEditText();
        } else {
        }
        return this.textChars;
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
        return textLimit;
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
        if ((getApi().style & SWT.SINGLE) != 0)
            return 0;
        return 0;
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
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            Point selection = getSelection();
            string = verifyText(string, selection.x, selection.x);
            if (string == null)
                return;
        }
        insertEditText(string);
        if (string.length() != 0)
            sendEvent(SWT.Modify);
    }

    void insertEditText(String string) {
        _insertEditText(string, false);
    }

    void _insertEditText(String string, boolean enableUndo) {
        int length = string.length();
        Point selection = getSelection();
        if (hasFocus() && hiddenText == null) {
            if (textLimit != Text.LIMIT) {
                int charCount = getCharCount();
                int selectionLength = selection.y - selection.x;
                if (charCount - selectionLength + length > textLimit) {
                    length = textLimit - charCount + selectionLength;
                    length = Math.max(0, length);
                }
            }
            char[] buffer = new char[length];
            string.getChars(0, buffer.length, buffer, 0);
        } else {
            String oldText = getText();
            if (textLimit != Text.LIMIT) {
                int charCount = oldText.length();
                if (charCount - (selection.y - selection.x) + length > textLimit) {
                    string = string.substring(0, textLimit - charCount + (selection.y - selection.x));
                }
            }
            String newText = oldText.substring(0, selection.x) + string + oldText.substring(selection.y);
            setEditText(newText);
            setSelection(selection.x + string.length());
        }
    }

    @Override
    boolean isEventView(long id) {
        return true;
    }

    @Override
    boolean isNeeded(ScrollBar scrollbar) {
        boolean result = false;
        if ((getApi().style & SWT.MULTI) != 0) {
            if ((scrollbar.style & SWT.VERTICAL) != 0) {
            } else {
            }
        }
        return result;
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
        _paste(false);
    }

    void _paste(boolean enableUndo) {
        if ((getApi().style & SWT.READ_ONLY) != 0)
            return;
        boolean paste = true;
        String oldText = null;
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            oldText = getClipboardText();
            if (oldText != null) {
            }
        }
        if (paste) {
            if ((getApi().style & SWT.SINGLE) != 0) {
                if (oldText == null)
                    oldText = getClipboardText();
                if (oldText == null)
                    return;
                _insertEditText(oldText, enableUndo);
            } else {
                if (textLimit != Text.LIMIT) {
                    if (oldText == null)
                        oldText = getClipboardText();
                    if (oldText == null)
                        return;
                } else {
                }
            }
        }
        sendEvent(SWT.Modify);
    }

    @Override
    void register() {
        super.register();
        if ((getApi().style & SWT.SINGLE) != 0) {
        }
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        hiddenText = null;
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
    boolean sendKeyEvent(int type, Event event) {
        if ((event.stateMask & SWT.COMMAND) != 0) {
            switch(event.keyCode) {
                case 'z':
                    return false;
            }
        }
        if (isDisposed())
            return false;
        return false;
    }

    @Override
    void sendSearchSelection() {
        Event event = new Event();
        event.detail = SWT.ICON_SEARCH;
        sendSelectionEvent(SWT.DefaultSelection, event, false);
    }

    @Override
    void sendCancelSelection() {
        Event event = new Event();
        event.detail = SWT.ICON_CANCEL;
        sendSelectionEvent(SWT.DefaultSelection, event, false);
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
        checkWidget();
        if (!java.util.Objects.equals(this.echoCharacter, echo)) {
            dirty();
        }
        if ((getApi().style & SWT.MULTI) != 0)
            return;
        if ((getApi().style & SWT.PASSWORD) == 0) {
            Point selection = getSelection();
            char[] text = getTextChars();
            echoCharacter = echo;
            setEditText(text);
            setSelection(selection);
        }
        echoCharacter = echo;
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
        if (editable) {
            getApi().style &= ~SWT.READ_ONLY;
        } else {
            getApi().style |= SWT.READ_ONLY;
        }
        this.editable = newValue;
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
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

    @Override
    void setForeground(double[] color) {
        if (color == null) {
        } else {
            double alpha = 1;
            if ((getApi().style & SWT.MULTI) != 0 && !isEnabled())
                alpha = 0.5f;
        }
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
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
        checkWidget();
    }

    @Override
    void setOrientation() {
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        }
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
        dirty();
        checkWidget();
        int length = getCharCount();
        int min = Math.min(Math.max(Math.min(start, end), 0), length);
        int max = Math.min(Math.max(Math.max(start, end), 0), length);
        Point newValue = new Point(min, max);
        this.selection = newValue;
        // the caret is in the start of the selection
        this.caretPosition = min;
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
        if (this.tabs == tabs)
            return;
        this.tabs = tabs;
        if ((getApi().style & SWT.SINGLE) != 0)
            return;
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
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            if (string == null)
                return;
        }
        int length = getCharCount();
        string = verifyText(string, 0, length);
        if ((getApi().style & SWT.SINGLE) != 0) {
            setEditText(string);
        } else {
            char[] buffer = new char[Math.min(string.length(), textLimit)];
            string.getChars(0, buffer.length, buffer, 0);
        }
        this.text = string;
        selection = null;
        sendEvent(SWT.Modify);
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
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
        }
        if ((getApi().style & SWT.SINGLE) != 0) {
            setEditText(text);
        } else {
        }
        this.textChars = newValue;
        sendEvent(SWT.Modify);
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
        checkWidget();
        if (!java.util.Objects.equals(this.textLimit, limit)) {
            dirty();
        }
        if (limit == 0)
            error(SWT.ERROR_CANNOT_BE_ZERO);
        if (limit < 0)
            return;
        textLimit = limit;
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
        int newValue = index;
        if (!java.util.Objects.equals(this.topIndex, newValue)) {
            dirty();
        }
        checkWidget();
        if ((getApi().style & SWT.SINGLE) != 0)
            return;
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
        if ((getApi().style & SWT.SINGLE) != 0) {
            setSelection(getSelection());
        } else {
        }
    }

    @Override
    int traversalCode(int key, Object theEvent) {
        int bits = super.traversalCode(key, theEvent);
        if ((getApi().style & SWT.READ_ONLY) != 0)
            return bits;
        if ((getApi().style & SWT.MULTI) != 0) {
            bits &= ~SWT.TRAVERSE_RETURN;
            if (key == 48 && /* Tab */
            theEvent != null) {
            }
        }
        return bits;
    }

    @Override
    public void updateCursorRects(boolean enabled) {
        super.updateCursorRects(enabled);
    }

    void updateThemeColors() {
        // See code comment in Link.updateThemeColors() for explanation
        // Avoid infinite loop of redraws
        if (lastAppAppearance == ((SwtDisplay) display.getImpl()).appAppearance)
            return;
        lastAppAppearance = ((SwtDisplay) display.getImpl()).appAppearance;
        // Only multi-line controls are affected
        if ((getApi().style & SWT.MULTI) == 0)
            return;
        if (foreground == null) {
            if (getEnabled()) {
            } else {
            }
        }
        if ((backgroundImage == null) && (background == null)) {
        }
    }

    int caretPosition;

    boolean editable;

    Point selection;

    String text = "";

    char[] textChars = new char[0];

    int topIndex;

    public int _textLimit() {
        return textLimit;
    }

    public int _tabs() {
        return tabs;
    }

    public char _echoCharacter() {
        return echoCharacter;
    }

    public boolean _doubleClick() {
        return doubleClick;
    }

    public boolean _receivingFocus() {
        return receivingFocus;
    }

    public char[] _hiddenText() {
        return hiddenText;
    }

    public String _message() {
        return message;
    }

    public long _actionSearch() {
        return actionSearch;
    }

    public long _actionCancel() {
        return actionCancel;
    }

    public APPEARANCE _lastAppAppearance() {
        return lastAppAppearance;
    }

    public int _caretPosition() {
        return caretPosition;
    }

    public boolean _editable() {
        return editable;
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

    public int _topIndex() {
        return topIndex;
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
                setSelection(e.index);
            });
        });
        FlutterBridge.on(this, "Verify", "Verify", e -> {
            getDisplay().asyncExec(() -> {
                sendEvent(SWT.Verify, e);
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
