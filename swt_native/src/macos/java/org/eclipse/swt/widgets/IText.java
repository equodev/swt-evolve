package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display.*;

public interface IText extends IScrollable {

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
    void addSegmentListener(SegmentListener listener);

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
     */
    void addVerifyListener(VerifyListener listener);

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
    void append(String string);

    /**
     * Clears the selection.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void clearSelection();

    Point computeSize(int wHint, int hHint, boolean changed);

    Rectangle computeTrim(int x, int y, int width, int height);

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
     */
    void cut();

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
    int getCaretLineNumber();

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
    Point getCaretLocation();

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
    int getCaretPosition();

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
    int getCharCount();

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
    boolean getDoubleClickEnabled();

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
    char getEchoChar();

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
    boolean getEditable();

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
    int getLineCount();

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
    String getLineDelimiter();

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
    int getLineHeight();

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
    int getOrientation();

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
    String getMessage();

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
    Point getSelection();

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
    int getSelectionCount();

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
    String getSelectionText();

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
    int getTabs();

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
    String getText();

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
    String getText(int start, int end);

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
    char[] getTextChars();

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
    int getTextLimit();

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
    int getTopIndex();

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
    int getTopPixel();

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
    void insert(String string);

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
    void paste();

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
    void removeSegmentListener(SegmentListener listener);

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
     */
    void removeVerifyListener(VerifyListener listener);

    /**
     * Selects all the text in the receiver.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    void selectAll();

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
    void setDoubleClickEnabled(boolean doubleClick);

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
    void setEchoChar(char echo);

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
    void setEditable(boolean editable);

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
    void setOrientation(int orientation);

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
    void setMessage(String message);

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
    void setSelection(int start);

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
    void setSelection(int start, int end);

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
    void setSelection(Point selection);

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
    void setTabs(int tabs);

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
    void setText(String string);

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
    void setTextChars(char[] text);

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
    void setTextLimit(int limit);

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
    void setTopIndex(int index);

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
    void showSelection();

    Text getApi();

    void setApi(Text api);
}
