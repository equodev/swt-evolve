/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2025 IBM Corporation and others.
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

    long bufferHandle;

    long imContext;

    int tabs = 8, lastEventTime = 0;

    long gdkEventKey = 0;

    int fixStart = -1, fixEnd = -1;

    boolean doubleClick;

    String message = "";

    /**
     * GTK4 only field, holds the address to the underlying GtkText widget.
     */
    long textHandle;

    static final char LTR_MARK = '\u200e';

    static final char RTL_MARK = '\u200f';

    int[] segments;

    static final int SPACE_FOR_CURSOR = 1;

    long indexMark = 0;

    double cachedAdjustment, currentAdjustment;

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
        if ((style & SWT.SEARCH) != 0) {
            /*
		 * Ensure that SWT.ICON_CANCEL and ICON_SEARCH are set.
		 * NOTE: ICON_CANCEL has the same value as H_SCROLL and CON_SEARCH has the same value as V_SCROLL
		 * so it is necessary to first clear these bits to avoid a scroll bar and then reset the
		 * bit using the original style supplied by the programmer.
		 *
		 * NOTE2: Default GtkSearchEntry shows both "find" icon and "clear" icon.
		 * "find" icon can be manually removed here while "clear" icon must be removed depending on text.
		 * See gtk_changed.
		 */
            this.getApi().style |= SWT.ICON_SEARCH | SWT.ICON_CANCEL;
        }
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

    @Override
    void createHandle(int index) {
        // In GTK 3 font description is inherited from parent widget which is not how SWT has always worked,
    }

    @Override
    int applyThemeBackground() {
        return (backgroundAlpha == 0 || (getApi().style & (SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL)) == 0) ? 1 : 0;
    }

    @Override
    void createWidget(int index) {
        super.createWidget(index);
        doubleClick = true;
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
        clearSegments(true);
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        applySegments();
    }

    void applySegments() {
        /*
	 * It is possible (but unlikely), that application code could have
	 * disposed the widget in the modify event. If this happens, return to
	 * cancel the operation.
	 */
        if (isDisposed() || (!hooks(SWT.Segments) && !filters(SWT.Segments)))
            return;
        Event event = new Event();
        String string = getText();
        event.text = string;
        event.segments = segments;
        sendEvent(SWT.Segments, event);
        segments = event.segments;
        if (segments == null)
            return;
        int nSegments = segments.length;
        if (nSegments == 0)
            return;
        for (int i = 1, length = string == null ? 0 : string.length(); i < nSegments; i++) {
            if (event.segments[i] < event.segments[i - 1] || event.segments[i] > length) {
                error(SWT.ERROR_INVALID_ARGUMENT);
            }
        }
        char[] segmentsChars = event.segmentsChars;
        char[] separator = { getOrientation() == SWT.RIGHT_TO_LEFT ? RTL_MARK : LTR_MARK };
        if ((getApi().style & SWT.SINGLE) != 0) {
            int[] pos = new int[1];
            for (int i = 0; i < nSegments; i++) {
                pos[0] = segments[i] + i;
                if (segmentsChars != null && segmentsChars.length > i) {
                    separator[0] = segmentsChars[i];
                }
            }
        } else {
            for (int i = 0; i < nSegments; i++) {
                if (segmentsChars != null && segmentsChars.length > i) {
                    separator[0] = segmentsChars[i];
                }
            }
        }
    }

    void clearSegments(boolean applyText) {
        if (segments == null)
            return;
        int nSegments = segments.length;
        if (nSegments == 0)
            return;
        if ((getApi().style & SWT.SINGLE) != 0) {
            if (applyText) {
                for (int i = 0; i < nSegments; i++) {
                }
            }
        } else if (applyText) {
            for (int i = 0; i < nSegments; i++) {
            }
        }
        segments = null;
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
    }

    @Override
    Point computeSizeInPixels(int wHint, int hHint, boolean changed) {
        return Sizes.compute(this);
    }

    @Override
    Rectangle computeTrimInPixels(int x, int y, int width, int height) {
        checkWidget();
        Rectangle trim = super.computeTrimInPixels(x, y, width, height);
        int xborder = 0, yborder = 0;
        if ((getApi().style & SWT.SINGLE) != 0) {
            {
            }
            if ((getApi().style & SWT.BORDER) != 0) {
            }
        } else {
        }
        trim.x -= xborder;
        trim.y -= yborder;
        trim.width += 2 * xborder;
        trim.height += 2 * yborder;
        trim.width += SPACE_FOR_CURSOR;
        return new Rectangle(trim.x, trim.y, trim.width, trim.height);
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
            clearSegments(true);
            applySegments();
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
     */
    public void cut() {
        checkWidget();
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
            clearSegments(true);
            applySegments();
        }
    }

    char[] deprocessText(char[] text, int start, int end) {
        if (text == null)
            return new char[0];
        if (start < 0)
            start = 0;
        int length = text.length;
        if (end == -1)
            end = start + length;
        if (segments != null && end > segments[0]) {
            int nSegments = segments.length;
            if (nSegments > 0 && start <= segments[nSegments - 1]) {
                int nLeadSegments = 0;
                while (start - nLeadSegments > segments[nLeadSegments]) nLeadSegments++;
                int segmentCount = nLeadSegments;
                for (int i = start; i < end; i++) {
                    if (segmentCount < nSegments && i - segmentCount == segments[segmentCount]) {
                        ++segmentCount;
                    } else {
                        text[i - segmentCount + nLeadSegments - start] = text[i - start];
                    }
                }
                length = end - start - segmentCount + nLeadSegments;
            }
        }
        if (start != 0 || end != start + length) {
            char[] newText = new char[length];
            System.arraycopy(text, 0, newText, 0, length);
            return newText;
        }
        return text;
    }

    @Override
    void deregister() {
        super.deregister();
        if (bufferHandle != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(bufferHandle);
        if (imContext != 0)
            ((SwtDisplay) display.getImpl()).removeWidget(imContext);
    }

    @Override
    boolean dragDetect(int x, int y, boolean filter, boolean dragOnTimeout, boolean[] consume) {
        return false;
    }

    @Override
    long eventWindow() {
        if ((getApi().style & SWT.SINGLE) != 0) {
            /*
		 * Single-line Text (GtkEntry in GTK) uses a GDK_INPUT_ONLY
		 * internal window. This window can't be used for any kind
		 * of painting, but this is the window to which functions
		 * like Control.setCursor() should apply.
		 */
            long window = super.paintWindow();
            return window;
        } else {
            return paintWindow();
        }
    }

    @Override
    boolean filterKey(long event) {
        gdkEventKey = event;
        return false;
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
            if (imContext != 0) {
                gdkEventKey = -1;
                return;
            }
        }
        gdkEventKey = 0;
    }

    @Override
    int getBorderWidthInPixels() {
        checkWidget();
        if ((getApi().style & SWT.MULTI) != 0)
            return super.getBorderWidthInPixels();
        if ((this.getApi().style & SWT.BORDER) != 0) {
            return getThickness(getApi().handle).x;
        }
        return 0;
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
     */
    public Point getCaretLocation() {
        checkWidget();
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
            int[] x = new int[1];
            int[] y = new int[1];
            return new Point(x[0], y[0]);
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        return 0;
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        }
        return echoChar;
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        }
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
        if ((getApi().style & SWT.SINGLE) != 0)
            return 1;
        return 0;
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
        return "\n";
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
        long fontDesc = getFontDescription();
        int result = fontHeight(fontDesc, getApi().handle);
        return result;
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

    /*public*/
    int getPosition(Point point) {
        checkWidget();
        if (point == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        int position = -1;
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        return untranslateOffset(position);
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
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
        return Math.abs(selection.y - selection.x);
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
        return getText().substring(selection.x, selection.y);
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
        int[] width = new int[1];
        return width[0] * tabs;
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
        if (!(start <= end && 0 <= end))
            return "";
        String str = getText();
        int length = str.length();
        end = Math.min(end, length - 1);
        if (start > end)
            return "";
        start = Math.max(0, start);
        /*
	* NOTE: The current implementation uses substring ()
	* which can reference a potentially large character
	* array.
	*/
        return str.substring(start, end + 1);
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
        } else {
        }
        if (segments != null) {
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
        if ((getApi().style & SWT.MULTI) != 0)
            return Text.LIMIT;
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
        if (cachedAdjustment == currentAdjustment) {
            // If indexMark is 0, fetch topIndex using the old method
            if (indexMark != 0) {
            }
        }
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
        int[] lineTop = new int[1];
        return lineTop[0];
    }

    @Override
    boolean mustBeVisibleOnInitBounds() {
        // Bug 542940: Workaround to avoid NPE, make Text visible on initialization
        return true;
    }

    @Override
    void hookEvents() {
        super.hookEvents();
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        if (imContext != 0) {
        }
        // In GTK4, these event signals belong to GtkText which is the only child of GtkEntry
        long eventHandle = 0;
        {
            eventHandle = getApi().handle;
        }
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
        clearSegments(true);
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        applySegments();
    }

    @Override
    long paintWindow() {
        if ((getApi().style & SWT.SINGLE) != 0) {
            return super.paintWindow();
        } else {
        }
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
     */
    public void paste() {
        checkWidget();
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
            clearSegments(true);
            applySegments();
        }
    }

    @Override
    void register() {
        super.register();
        if (bufferHandle != 0)
            ((SwtDisplay) display.getImpl()).addWidget(bufferHandle, this.getApi());
        if (imContext != 0)
            ((SwtDisplay) display.getImpl()).addWidget(imContext, this.getApi());
    }

    @Override
    void releaseWidget() {
        super.releaseWidget();
        fixIM();
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
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
    }

    @Override
    public void setCursor(long cursor) {
        long defaultCursor = 0;
        if (cursor == 0) {
        }
        super.setCursor(cursor != 0 ? cursor : defaultCursor);
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
        dirty();
        checkWidget();
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
        dirty();
        checkWidget();
        if ((getApi().style & SWT.SINGLE) != 0) {
        }
        this.echoChar = echo;
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
        dirty();
        checkWidget();
        getApi().style &= ~SWT.READ_ONLY;
        if (!editable)
            getApi().style |= SWT.READ_ONLY;
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        this.editable = editable;
    }

    @Override
    void setFontDescription(long font) {
        super.setFontDescription(font);
        setTabStops(tabs);
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
        dirty();
        checkWidget();
        if (message == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        this.message = message;
        if ((getApi().style & SWT.SINGLE) != 0) {
            return;
        }
        redraw(false);
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
        dirty();
        checkWidget();
        start = translateOffset(start);
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        this.selection = new Point(start, start);
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
        start = translateOffset(start);
        end = translateOffset(end);
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        this.selection = new Point(start, end);
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
        dirty();
        checkWidget();
        if (tabs < 0)
            return;
        setTabStops(this.tabs = tabs);
    }

    void setTabStops(int tabs) {
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
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
        checkWidget();
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        /*
	* Feature in gtk.  When text is set in gtk, separate events are fired for the deletion and
	* insertion of the text.  This is not wrong, but is inconsistent with other platforms.  The
	* fix is to block the firing of these events and fire them ourselves in a consistent manner.
	*/
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            string = verifyText(string, 0, getCharCount());
            if (string == null)
                return;
        }
        char[] text = new char[string.length()];
        string.getChars(0, text.length, text, 0);
        setText(text);
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
        dirty();
        checkWidget();
        if (text == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        /*
	* Feature in gtk.  When text is set in gtk, separate events are fired for the deletion and
	* insertion of the text.  This is not wrong, but is inconsistent with other platforms.  The
	* fix is to block the firing of these events and fire them ourselves in a consistent manner.
	*/
        if (hooks(SWT.Verify) || filters(SWT.Verify)) {
            String string = verifyText(new String(text), 0, getCharCount());
            if (string == null)
                return;
            text = new char[string.length()];
            string.getChars(0, text.length, text, 0);
        }
        setText(text);
        this.textChars = text;
    }

    void setText(char[] text) {
        dirty();
        clearSegments(false);
        if ((getApi().style & SWT.SINGLE) != 0) {
        } else {
        }
        sendEvent(SWT.Modify);
        applySegments();
        this.text = new String(text);
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
        dirty();
        checkWidget();
        if (limit == 0)
            error(SWT.ERROR_CANNOT_BE_ZERO);
        if ((getApi().style & SWT.SINGLE) != 0) {
        }
        this.textLimit = limit;
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
        dirty();
        checkWidget();
        if ((getApi().style & SWT.SINGLE) != 0)
            return;
        this.topIndex = index;
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
        if ((getApi().style & SWT.SINGLE) != 0)
            return;
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
    boolean translateTraversal(long event) {
        return super.translateTraversal(event);
    }

    @Override
    int traversalCode(int key, Object event) {
        int bits = super.traversalCode(key, event);
        if ((getApi().style & SWT.READ_ONLY) != 0)
            return bits;
        if ((getApi().style & SWT.MULTI) != 0) {
            bits &= ~SWT.TRAVERSE_RETURN;
        }
        return bits;
    }

    int untranslateOffset(int offset) {
        if (segments == null)
            return offset;
        for (int i = 0, nSegments = segments.length; i < nSegments && offset > segments[i]; i++) {
            offset--;
        }
        return offset;
    }

    String verifyText(String string, int start, int end) {
        if (string != null && string.length() == 0 && start == end)
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

    char echoChar;

    boolean editable;

    Point selection;

    String text = "";

    char[] textChars = new char[0];

    int textLimit;

    int topIndex;

    public long _bufferHandle() {
        return bufferHandle;
    }

    public long _imContext() {
        return imContext;
    }

    public int _tabs() {
        return tabs;
    }

    public int _lastEventTime() {
        return lastEventTime;
    }

    public int _fixStart() {
        return fixStart;
    }

    public int _fixEnd() {
        return fixEnd;
    }

    public boolean _doubleClick() {
        return doubleClick;
    }

    public String _message() {
        return message;
    }

    public long _textHandle() {
        return textHandle;
    }

    public int[] _segments() {
        return segments;
    }

    public long _indexMark() {
        return indexMark;
    }

    public double _cachedAdjustment() {
        return cachedAdjustment;
    }

    public double _currentAdjustment() {
        return currentAdjustment;
    }

    public char _echoChar() {
        return echoChar;
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

    public int _textLimit() {
        return textLimit;
    }

    public int _topIndex() {
        return topIndex;
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
