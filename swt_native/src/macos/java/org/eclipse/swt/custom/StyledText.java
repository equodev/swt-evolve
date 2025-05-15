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
 *      Andrey Loskutov <loskutov@gmx.de> - bug 488172
 *      Stefan Xenos (Google) - bug 487254 - StyledText.getTopIndex() can return negative values
 *      Angelo Zerr <angelo.zerr@gmail.com> - Customize different line spacing of StyledText - Bug 522020
 *      Karsten Thoms <thoms@itemis.de> - bug 528746 add getOffsetAtPoint(Point)
 * *****************************************************************************
 */
package org.eclipse.swt.custom;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.printing.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.Convert;

/**
 * A StyledText is an editable user interface object that displays lines
 * of text.  The following style attributes can be defined for the text:
 * <ul>
 * <li>foreground color
 * <li>background color
 * <li>font style (bold, italic, bold-italic, regular)
 * <li>underline
 * <li>strikeout
 * </ul>
 * <p>
 * In addition to text style attributes, the background color of a line may
 * be specified.
 * </p><p>
 * There are two ways to use this widget when specifying text style information.
 * You may use the API that is defined for StyledText or you may define your own
 * LineStyleListener.  If you define your own listener, you will be responsible
 * for maintaining the text style information for the widget.  IMPORTANT: You may
 * not define your own listener and use the StyledText API.  The following
 * StyledText API is not supported if you have defined a LineStyleListener:</p>
 * <ul>
 * <li>getStyleRangeAtOffset(int)
 * <li>getStyleRanges()
 * <li>replaceStyleRanges(int,int,StyleRange[])
 * <li>setStyleRange(StyleRange)
 * <li>setStyleRanges(StyleRange[])
 * </ul>
 * <p>
 * There are two ways to use this widget when specifying line background colors.
 * You may use the API that is defined for StyledText or you may define your own
 * LineBackgroundListener.  If you define your own listener, you will be responsible
 * for maintaining the line background color information for the widget.
 * IMPORTANT: You may not define your own listener and use the StyledText API.
 * The following StyledText API is not supported if you have defined a
 * LineBackgroundListener:</p>
 * <ul>
 * <li>getLineBackground(int)
 * <li>setLineBackground(int,int,Color)
 * </ul>
 * <p>
 * The content implementation for this widget may also be user-defined.  To do so,
 * you must implement the StyledTextContent interface and use the StyledText API
 * setContent(StyledTextContent) to initialize the widget.
 * </p>
 * <dl>
 * <dt><b>Styles:</b><dd>FULL_SELECTION, MULTI, READ_ONLY, SINGLE, WRAP
 * <dt><b>Events:</b><dd>ExtendedModify, LineGetBackground, LineGetSegments, LineGetStyle, Modify, Selection, Verify, VerifyKey, OrientationChange
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#styledtext">StyledText snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Examples: CustomControlExample, TextEditor</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class StyledText extends Canvas {

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
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     * </ul>
     *
     * @see SWT#FULL_SELECTION
     * @see SWT#MULTI
     * @see SWT#READ_ONLY
     * @see SWT#SINGLE
     * @see SWT#WRAP
     * @see #getStyle
     */
    public StyledText(Composite parent, int style) {
        this(new nat.org.eclipse.swt.custom.StyledText((nat.org.eclipse.swt.widgets.Composite) parent.getDelegate(), style));
    }

    /**
     * Adds an extended modify listener. An ExtendedModify event is sent by the
     * widget when the widget text has changed.
     *
     * @param extendedModifyListener the listener
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void addExtendedModifyListener(ExtendedModifyListener extendedModifyListener) {
        getDelegate().addExtendedModifyListener(extendedModifyListener);
    }

    /**
     * Adds a bidirectional segment listener.
     * <p>
     * A BidiSegmentEvent is sent
     * whenever a line of text is measured or rendered. You can
     * specify text ranges in the line that should be treated as if they
     * had a different direction than the surrounding text.
     * This may be used when adjacent segments of right-to-left text should
     * not be reordered relative to each other.
     * E.g., multiple Java string literals in a right-to-left language
     * should generally remain in logical order to each other, that is, the
     * way they are stored.
     * </p>
     *
     * @param listener the listener
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     * @see BidiSegmentEvent
     * @since 2.0
     */
    public void addBidiSegmentListener(BidiSegmentListener listener) {
        getDelegate().addBidiSegmentListener(listener);
    }

    /**
     * Adds a caret listener. CaretEvent is sent when the caret offset changes.
     *
     * @param listener the listener
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     *
     * @since 3.5
     */
    public void addCaretListener(CaretListener listener) {
        getDelegate().addCaretListener(listener);
    }

    /**
     * Adds a line background listener. A LineGetBackground event is sent by the
     * widget to determine the background color for a line.
     *
     * @param listener the listener
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void addLineBackgroundListener(LineBackgroundListener listener) {
        getDelegate().addLineBackgroundListener(listener);
    }

    /**
     * Adds a line style listener. A LineGetStyle event is sent by the widget to
     * determine the styles for a line.
     *
     * @param listener the listener
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void addLineStyleListener(LineStyleListener listener) {
        getDelegate().addLineStyleListener(listener);
    }

    /**
     * Adds a modify listener. A Modify event is sent by the widget when the widget text
     * has changed.
     *
     * @param modifyListener the listener
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void addModifyListener(ModifyListener modifyListener) {
        getDelegate().addModifyListener(modifyListener);
    }

    /**
     * Adds a paint object listener. A paint object event is sent by the widget when an object
     * needs to be drawn.
     *
     * @param listener the listener
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     *
     * @since 3.2
     *
     * @see PaintObjectListener
     * @see PaintObjectEvent
     */
    public void addPaintObjectListener(PaintObjectListener listener) {
        getDelegate().addPaintObjectListener(listener);
    }

    /**
     * Adds a selection listener. A Selection event is sent by the widget when the
     * user changes the selection.
     * <p>
     * When <code>widgetSelected</code> is called, the event x and y fields contain
     * the start and end caret indices of the selection[0]. The selection values returned are visual
     * (i.e., x will always always be &lt;= y).
     * No event is sent when the caret is moved while the selection length is 0.
     * </p><p>
     * <code>widgetDefaultSelected</code> is not called for StyledTexts.
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
        getDelegate().addSelectionListener(listener);
    }

    /**
     * Adds a verify key listener. A VerifyKey event is sent by the widget when a key
     * is pressed. The widget ignores the key press if the listener sets the doit field
     * of the event to false.
     *
     * @param listener the listener
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void addVerifyKeyListener(VerifyKeyListener listener) {
        getDelegate().addVerifyKeyListener(listener);
    }

    /**
     * Adds a verify listener. A Verify event is sent by the widget when the widget text
     * is about to change. The listener can set the event text and the doit field to
     * change the text that is set in the widget or to force the widget to ignore the
     * text change.
     *
     * @param verifyListener the listener
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void addVerifyListener(VerifyListener verifyListener) {
        getDelegate().addVerifyListener(verifyListener);
    }

    /**
     * Adds a word movement listener. A movement event is sent when the boundary
     * of a word is needed. For example, this occurs during word next and word
     * previous actions.
     *
     * @param movementListener the listener
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     *
     * @see MovementEvent
     * @see MovementListener
     * @see #removeWordMovementListener
     *
     * @since 3.3
     */
    public void addWordMovementListener(MovementListener movementListener) {
        getDelegate().addWordMovementListener(movementListener);
    }

    /**
     * Appends a string to the text at the end of the widget.
     *
     * @param string the string to be appended
     * @see #replaceTextRange(int,int,String)
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void append(String string) {
        getDelegate().append(string);
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        return getDelegate().computeSize(wHint, hHint, changed).getApi();
    }

    /**
     * Copies the selected text to the <code>DND.CLIPBOARD</code> clipboard.
     * <p>
     * The text will be put on the clipboard in plain text, HTML, and RTF formats.
     * The <code>DND.CLIPBOARD</code> clipboard is used for data that is
     * transferred by keyboard accelerator (such as Ctrl+C/Ctrl+V) or
     * by menu action.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void copy() {
        getDelegate().copy();
    }

    /**
     * Copies the selected text to the specified clipboard.  The text will be put in the
     * clipboard in plain text, HTML, and RTF formats.
     * <p>
     * The clipboardType is  one of the clipboard constants defined in class
     * <code>DND</code>.  The <code>DND.CLIPBOARD</code>  clipboard is
     * used for data that is transferred by keyboard accelerator (such as Ctrl+C/Ctrl+V)
     * or by menu action.  The <code>DND.SELECTION_CLIPBOARD</code>
     * clipboard is used for data that is transferred by selecting text and pasting
     * with the middle mouse button.
     * </p>
     *
     * @param clipboardType indicates the type of clipboard
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.1
     */
    public void copy(int clipboardType) {
        getDelegate().copy(clipboardType);
    }

    /**
     *  Returns the alignment of the widget.
     *
     *  @return the alignment
     *
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *  </ul>
     * R
     *  @see #getLineAlignment(int)
     *
     *  @since 3.2
     */
    public int getAlignment() {
        return getDelegate().getAlignment();
    }

    /**
     * Returns the Always Show Scrollbars flag.  True if the scrollbars are
     * always shown even if they are not required.  False if the scrollbars are only
     * visible when some part of the content needs to be scrolled to be seen.
     * The H_SCROLL and V_SCROLL style bits are also required to enable scrollbars in the
     * horizontal and vertical directions.
     *
     * @return the Always Show Scrollbars flag value
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.8
     */
    public boolean getAlwaysShowScrollBars() {
        return getDelegate().getAlwaysShowScrollBars();
    }

    /**
     * Returns the color of the margins.
     *
     * @return the color of the margins.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public Color getMarginColor() {
        return getDelegate().getMarginColor().getApi();
    }

    /**
     * Moves the selected text to the clipboard.  The text will be put in the
     * clipboard in plain text, HTML, and RTF formats.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void cut() {
        getDelegate().cut();
        // Abort cut operation if copy to clipboard fails.
    }

    public Color getBackground() {
        return getDelegate().getBackground().getApi();
    }

    /**
     * Returns the baseline, in points.
     *
     * Note: this API should not be used if a StyleRange attribute causes lines to
     * have different heights (i.e. different fonts, rise, etc).
     *
     * @return baseline the baseline
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.0
     *
     * @see #getBaseline(int)
     */
    public int getBaseline() {
        return getDelegate().getBaseline();
    }

    /**
     * Returns the baseline at the given offset, in points.
     *
     * @param offset the offset
     *
     * @return baseline the baseline
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when the offset is outside the valid range (&lt; 0 or &gt; getCharCount())</li>
     * </ul>
     *
     * @since 3.2
     */
    public int getBaseline(int offset) {
        return getDelegate().getBaseline(offset);
    }

    /**
     * Gets the BIDI coloring mode.  When true the BIDI text display
     * algorithm is applied to segments of text that are the same
     * color.
     *
     * @return the current coloring mode
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @deprecated use BidiSegmentListener instead.
     */
    @Deprecated
    public boolean getBidiColoring() {
        return getDelegate().getBidiColoring();
    }

    /**
     * Returns whether the widget is in block selection mode.
     *
     * @return true if widget is in block selection mode, false otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public boolean getBlockSelection() {
        return getDelegate().getBlockSelection();
    }

    /**
     * Returns the block selection bounds. The bounds is
     * relative to the upper left corner of the document.
     *
     * @return the block selection bounds
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public Rectangle getBlockSelectionBounds() {
        return getDelegate().getBlockSelectionBounds().getApi();
    }

    /**
     * Returns the bottom margin.
     *
     * @return the bottom margin.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public int getBottomMargin() {
        return getDelegate().getBottomMargin();
    }

    /**
     * Returns the caret position relative to the start of the text.
     *
     * @return the caret position relative to the start of the text.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getCaretOffset() {
        return getDelegate().getCaretOffset();
    }

    /**
     * Returns the content implementation that is used for text storage.
     *
     * @return content the user defined content implementation that is used for
     * text storage or the default content implementation if no user defined
     * content implementation has been set.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public StyledTextContent getContent() {
        return getDelegate().getContent();
    }

    public boolean getDragDetect() {
        return getDelegate().getDragDetect();
    }

    /**
     * Returns whether the widget implements double click mouse behavior.
     *
     * @return true if double clicking a word selects the word, false if double clicks
     * have the same effect as regular mouse clicks
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getDoubleClickEnabled() {
        return getDelegate().getDoubleClickEnabled();
    }

    /**
     * Returns whether the widget content can be edited.
     *
     * @return true if content can be edited, false otherwise
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public boolean getEditable() {
        return getDelegate().getEditable();
    }

    public Color getForeground() {
        return getDelegate().getForeground().getApi();
    }

    /**
     * Returns the horizontal scroll offset relative to the start of the line.
     *
     * @return horizontal scroll offset relative to the start of the line,
     * measured in character increments starting at 0, if &gt; 0 the content is scrolled
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getHorizontalIndex() {
        return getDelegate().getHorizontalIndex();
    }

    /**
     * Returns the horizontal scroll offset relative to the start of the line.
     *
     * @return the horizontal scroll offset relative to the start of the line,
     * measured in SWT logical point starting at 0, if &gt; 0 the content is scrolled.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getHorizontalPixel() {
        return getDelegate().getHorizontalPixel();
    }

    /**
     * Returns the line indentation of the widget.
     *
     * @return the line indentation
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getLineIndent(int)
     *
     * @since 3.2
     */
    public int getIndent() {
        return getDelegate().getIndent();
    }

    /**
     * Returns whether the widget justifies lines.
     *
     * @return whether lines are justified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getLineJustify(int)
     *
     * @since 3.2
     */
    public boolean getJustify() {
        return getDelegate().getJustify();
    }

    /**
     * Returns the action assigned to the key.
     * Returns SWT.NULL if there is no action associated with the key.
     *
     * @param key a key code defined in SWT.java or a character.
     * 	Optionally ORd with a state mask.  Preferred state masks are one or more of
     *  SWT.MOD1, SWT.MOD2, SWT.MOD3, since these masks account for modifier platform
     *  differences.  However, there may be cases where using the specific state masks
     *  (i.e., SWT.CTRL, SWT.SHIFT, SWT.ALT, SWT.COMMAND) makes sense.
     * @return one of the predefined actions defined in ST.java or SWT.NULL
     * 	if there is no action associated with the key.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getKeyBinding(int key) {
        return getDelegate().getKeyBinding(key);
    }

    /**
     * Gets the number of characters.
     *
     * @return number of characters in the widget
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getCharCount() {
        return getDelegate().getCharCount();
    }

    /**
     * Returns the line at the given line index without delimiters.
     * Index 0 is the first line of the content. When there are not
     * any lines, getLine(0) is a valid call that answers an empty string.
     *
     * @param lineIndex index of the line to return.
     * @return the line text without delimiters
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when the line index is outside the valid range (&lt; 0 or &gt;= getLineCount())</li>
     * </ul>
     * @since 3.4
     */
    public String getLine(int lineIndex) {
        return getDelegate().getLine(lineIndex);
    }

    /**
     * Returns the alignment of the line at the given index.
     *
     * @param index the index of the line
     *
     * @return the line alignment
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT when the index is invalid</li>
     * </ul>
     *
     * @see #getAlignment()
     *
     * @since 3.2
     */
    public int getLineAlignment(int index) {
        return getDelegate().getLineAlignment(index);
    }

    /**
     * Returns the line at the specified offset in the text
     * where 0 &lt; offset &lt; getCharCount() so that getLineAtOffset(getCharCount())
     * returns the line of the insert location.
     *
     * @param offset offset relative to the start of the content.
     * 	0 &lt;= offset &lt;= getCharCount()
     * @return line at the specified offset in the text
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when the offset is outside the valid range (&lt; 0 or &gt; getCharCount())</li>
     * </ul>
     */
    public int getLineAtOffset(int offset) {
        return getDelegate().getLineAtOffset(offset);
    }

    /**
     * Returns the background color of the line at the given index.
     * Returns null if a LineBackgroundListener has been set or if no background
     * color has been specified for the line. Should not be called if a
     * LineBackgroundListener has been set since the listener maintains the
     * line background colors.
     *
     * @param index the index of the line
     * @return the background color of the line at the given index.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT when the index is invalid</li>
     * </ul>
     */
    public Color getLineBackground(int index) {
        return getDelegate().getLineBackground(index).getApi();
    }

    /**
     * Returns the bullet of the line at the given index.
     *
     * @param index the index of the line
     *
     * @return the line bullet
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT when the index is invalid</li>
     * </ul>
     *
     * @since 3.2
     */
    public Bullet getLineBullet(int index) {
        return getDelegate().getLineBullet(index).getApi();
    }

    /**
     * Gets the number of text lines.
     *
     * @return the number of lines in the widget
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getLineCount() {
        return getDelegate().getLineCount();
    }

    /**
     * Returns the line delimiter used for entering new lines by key down
     * or paste operation.
     *
     * @return line delimiter used for entering new lines by key down
     * or paste operation.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getLineDelimiter() {
        return getDelegate().getLineDelimiter();
    }

    /**
     * Returns the line height.
     * <p>
     * Note: this API should not be used if a StyleRange attribute causes lines to
     * have different heights (i.e. different fonts, rise, etc).
     * </p>
     *
     * @return line height in points.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @see #getLineHeight(int)
     */
    public int getLineHeight() {
        return getDelegate().getLineHeight();
    }

    /**
     * Returns the line height at the given offset.
     *
     * @param offset the offset
     *
     * @return line height in points
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when the offset is outside the valid range (&lt; 0 or &gt; getCharCount())</li>
     * </ul>
     *
     * @since 3.2
     */
    public int getLineHeight(int offset) {
        return getDelegate().getLineHeight(offset);
    }

    /**
     * Returns the indentation of the line at the given index.
     *
     * @param index the index of the line
     *
     * @return the line indentation
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT when the index is invalid</li>
     * </ul>
     *
     * @see #getIndent()
     *
     * @since 3.2
     */
    public int getLineIndent(int index) {
        return getDelegate().getLineIndent(index);
    }

    /**
     * Returns the vertical indentation of the line at the given index.
     *
     * @param index the index of the line
     *
     * @return the line vertical indentation
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT when the index is invalid</li>
     * </ul>
     *
     * @since 3.109
     */
    public int getLineVerticalIndent(int index) {
        return getDelegate().getLineVerticalIndent(index);
    }

    /**
     * Returns whether the line at the given index is justified.
     *
     * @param index the index of the line
     *
     * @return whether the line is justified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT when the index is invalid</li>
     * </ul>
     *
     * @see #getJustify()
     *
     * @since 3.2
     */
    public boolean getLineJustify(int index) {
        return getDelegate().getLineJustify(index);
    }

    /**
     * Returns the line spacing of the widget.
     *
     * @return the line spacing
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     */
    public int getLineSpacing() {
        return getDelegate().getLineSpacing();
    }

    /**
     * Returns the top SWT logical point, relative to the client area, of a given line.
     * Clamps out of ranges index.
     *
     * @param lineIndex the line index, the max value is lineCount. If
     * lineIndex == lineCount it returns the bottom SWT logical point of the last line.
     * It means this function can be used to retrieve the bottom SWT logical point of any line.
     *
     * @return the top SWT logical point of a given line index
     *
     * @since 3.2
     */
    public int getLinePixel(int lineIndex) {
        return getDelegate().getLinePixel(lineIndex);
    }

    /**
     * Returns the line index for a y, relative to the client area.
     * The line index returned is always in the range 0..lineCount - 1.
     *
     * @param y the y-coordinate point
     *
     * @return the line index for a given y-coordinate point
     *
     * @since 3.2
     */
    public int getLineIndex(int y) {
        return getDelegate().getLineIndex(y);
    }

    /**
     * Returns the tab stops of the line at the given <code>index</code>.
     *
     * @param index the index of the line
     *
     * @return the tab stops for the line
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT when the index is invalid</li>
     * </ul>
     *
     * @see #getTabStops()
     *
     * @since 3.6
     */
    public int[] getLineTabStops(int index) {
        return getDelegate().getLineTabStops(index);
    }

    /**
     * Returns the wrap indentation of the line at the given <code>index</code>.
     *
     * @param index the index of the line
     *
     * @return the wrap indentation
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT when the index is invalid</li>
     * </ul>
     *
     * @see #getWrapIndent()
     *
     * @since 3.6
     */
    public int getLineWrapIndent(int index) {
        return getDelegate().getLineWrapIndent(index);
    }

    /**
     * Returns the left margin.
     *
     * @return the left margin.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public int getLeftMargin() {
        return getDelegate().getLeftMargin();
    }

    /**
     * Returns the x, y location of the upper left corner of the character
     * bounding box at the specified offset in the text. The point is
     * relative to the upper left corner of the widget client area.
     *
     * @param offset offset relative to the start of the content.
     * 	0 &lt;= offset &lt;= getCharCount()
     * @return x, y location of the upper left corner of the character
     * 	bounding box at the specified offset in the text.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when the offset is outside the valid range (&lt; 0 or &gt; getCharCount())</li>
     * </ul>
     */
    public Point getLocationAtOffset(int offset) {
        return getDelegate().getLocationAtOffset(offset).getApi();
    }

    /**
     * Returns <code>true</code> if the mouse navigator is enabled.
     * When mouse navigator is enabled, the user can navigate through the widget by pressing the middle button and moving the cursor
     *
     * @return the mouse navigator's enabled state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getEnabled
     * @since 3.110
     */
    public boolean getMouseNavigatorEnabled() {
        return getDelegate().getMouseNavigatorEnabled();
    }

    /**
     *  Returns the character offset of the first character of the given line.
     *
     *  @param lineIndex index of the line, 0 based relative to the first
     *  	line in the content. 0 &lt;= lineIndex &lt; getLineCount(), except
     *  	lineIndex may always be 0
     *  @return offset offset of the first character of the line, relative to
     *  	the beginning of the document. The first character of the document is
     * 	at offset 0.
     *   When there are not any lines, getOffsetAtLine(0) is a valid call that
     *  	answers 0.
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *  </ul>
     *  @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE when the line index is outside the valid range (&lt; 0 or &gt;= getLineCount())</li>
     *  </ul>
     *  @since 2.0
     */
    public int getOffsetAtLine(int lineIndex) {
        return getDelegate().getOffsetAtLine(lineIndex);
    }

    /**
     * Returns the offset of the character at the given location relative
     * to the first character in the document.
     * <p>
     * The return value reflects the character offset that the caret will
     * be placed at if a mouse click occurred at the specified location.
     * If the x coordinate of the location is beyond the center of a character
     * the returned offset will be behind the character.
     * </p>
     *
     * @param point the origin of character bounding box relative to
     *  the origin of the widget client area.
     * @return offset of the character at the given location relative
     *  to the first character in the document.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT when point is null</li>
     *   <li>ERROR_INVALID_ARGUMENT when there is no character at the specified location</li>
     * </ul>
     *
     * @deprecated Use {@link #getOffsetAtPoint(Point)} instead for better performance
     */
    @Deprecated
    public int getOffsetAtLocation(Point point) {
        return getDelegate().getOffsetAtLocation(point.getDelegate());
    }

    /**
     * Returns the offset of the character at the given point relative
     * to the first character in the document.
     * <p>
     * The return value reflects the character offset that the caret will
     * be placed at if a mouse click occurred at the specified point.
     * If the x coordinate of the point is beyond the center of a character
     * the returned offset will be behind the character.
     * </p>
     * Note: This method is functionally similar to {@link #getOffsetAtLocation(Point)} except that
     * it does not throw an exception when no character is found and thus performs faster.
     *
     * @param point the origin of character bounding box relative to
     *  the origin of the widget client area.
     * @return offset of the character at the given point relative
     *  to the first character in the document.
     * -1 when there is no character at the specified location.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT when point is <code>null</code></li>
     * </ul>
     *
     * @since 3.107
     */
    public int getOffsetAtPoint(Point point) {
        return getDelegate().getOffsetAtPoint(point.getDelegate());
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
    public int getOrientation() {
        return getDelegate().getOrientation();
    }

    /**
     * Returns all the ranges of text that have an associated StyleRange.
     * Returns an empty array if a LineStyleListener has been set.
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * <p>
     * The ranges array contains start and length pairs.  Each pair refers to
     * the corresponding style in the styles array.  For example, the pair
     * that starts at ranges[n] with length ranges[n+1] uses the style
     * at styles[n/2] returned by <code>getStyleRanges(int, int, boolean)</code>.
     * </p>
     *
     * @return the ranges or an empty array if a LineStyleListener has been set.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     *
     * @see #getStyleRanges(boolean)
     */
    public int[] getRanges() {
        return getDelegate().getRanges();
    }

    /**
     * Returns the ranges of text that have an associated StyleRange.
     * Returns an empty array if a LineStyleListener has been set.
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * <p>
     * The ranges array contains start and length pairs.  Each pair refers to
     * the corresponding style in the styles array.  For example, the pair
     * that starts at ranges[n] with length ranges[n+1] uses the style
     * at styles[n/2] returned by <code>getStyleRanges(int, int, boolean)</code>.
     * </p>
     *
     * @param start the start offset of the style ranges to return
     * @param length the number of style ranges to return
     *
     * @return the ranges or an empty array if a LineStyleListener has been set.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE if start or length are outside the widget content</li>
     * </ul>
     *
     * @since 3.2
     *
     * @see #getStyleRanges(int, int, boolean)
     */
    public int[] getRanges(int start, int length) {
        return getDelegate().getRanges(start, length);
    }

    /**
     * Returns the right margin.
     *
     * @return the right margin.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public int getRightMargin() {
        return getDelegate().getRightMargin();
    }

    /**
     * Returns the selection.
     * <p>
     * Text selections are specified in terms of caret positions.  In a text
     * widget that contains N characters, there are N+1 caret positions,
     * ranging from 0..N
     * </p>
     * <p>
     * It is usually better to use {@link #getSelectionRanges()} which better
     * support multiple selection and carets and block selection.
     * </p>
     *
     * @return start and end of the selection, x is the offset of the first
     * 	selected character, y is the offset after the last selected character.
     *  The selection values returned are visual (i.e., x will always always be
     *  &lt;= y).  To determine if a selection is right-to-left (RtoL) vs. left-to-right
     *  (LtoR), compare the caretOffset to the start and end of the selection
     *  (e.g., caretOffset == start of selection implies that the selection is RtoL).
     * @see #getSelectionRanges
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public Point getSelection() {
        return getDelegate().getSelection().getApi();
    }

    /**
     * Returns the selection.
     * <p>
     * It is usually better to use {@link #getSelectionRanges()} which better
     * support multiple selection and carets and block selection.
     * </p>
     *
     * @return start and length of the selection, x is the offset of the
     * 	first selected character, relative to the first character of the
     * 	widget content. y is the length of the selection.
     *  The selection values returned are visual (i.e., length will always always be
     *  positive).  To determine if a selection is right-to-left (RtoL) vs. left-to-right
     *  (LtoR), compare the caretOffset to the start and end of the selection
     *  (e.g., caretOffset == start of selection implies that the selection is RtoL).
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @see #getSelectionRanges
     */
    public Point getSelectionRange() {
        return getDelegate().getSelectionRange().getApi();
    }

    /**
     * Returns the selected ranges of text.
     * If block is enabled, return the ranges that are inside the block selection rectangle.
     * <p>
     * The ranges array contains start and length pairs.
     * <p>
     * When the receiver is not
     * in block selection mode the return arrays contains the start and length of
     * the regular selections.
     *
     * @return the ranges array
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     * @see #setSelectionRanges(int[])
     */
    public int[] getSelectionRanges() {
        return getDelegate().getSelectionRanges();
    }

    /**
     * Returns the receiver's selection background color.
     *
     * @return the selection background color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 2.1
     */
    public Color getSelectionBackground() {
        return getDelegate().getSelectionBackground().getApi();
    }

    /**
     * Gets the number of selected characters.
     *
     * @return the number of selected characters.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getSelectionCount() {
        return getDelegate().getSelectionCount();
    }

    /**
     * Returns the receiver's selection foreground color.
     *
     * @return the selection foreground color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 2.1
     */
    public Color getSelectionForeground() {
        return getDelegate().getSelectionForeground().getApi();
    }

    /**
     * Returns the selected text.
     *
     * @return selected text, or an empty String if there is no selection.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getSelectionText() {
        return getDelegate().getSelectionText();
    }

    /**
     * Returns the style range at the given offset.
     * <p>
     * Returns null if a LineStyleListener has been set or if a style is not set
     * for the offset.
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * </p>
     *
     * @param offset the offset to return the style for.
     * 	0 &lt;= offset &lt; getCharCount() must be true.
     * @return a StyleRange with start == offset and length == 1, indicating
     * 	the style at the given offset. null if a LineStyleListener has been set
     * 	or if a style is not set for the given offset.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when the offset is invalid</li>
     * </ul>
     */
    public StyleRange getStyleRangeAtOffset(int offset) {
        return getDelegate().getStyleRangeAtOffset(offset).getApi();
    }

    /**
     * Returns the styles.
     * <p>
     * Returns an empty array if a LineStyleListener has been set.
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * </p><p>
     * Note: Because a StyleRange includes the start and length, the
     * same instance cannot occur multiple times in the array of styles.
     * If the same style attributes, such as font and color, occur in
     * multiple StyleRanges, <code>getStyleRanges(boolean)</code>
     * can be used to get the styles without the ranges.
     * </p>
     *
     * @return the styles or an empty array if a LineStyleListener has been set.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getStyleRanges(boolean)
     */
    public StyleRange[] getStyleRanges() {
        return Convert.array(getDelegate().getStyleRanges(), IStyleRange::getApi, StyleRange[]::new);
    }

    /**
     * Returns the styles.
     * <p>
     * Returns an empty array if a LineStyleListener has been set.
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * </p><p>
     * Note: When <code>includeRanges</code> is true, the start and length
     * fields of each StyleRange will be valid, however the StyleRange
     * objects may need to be cloned. When <code>includeRanges</code> is
     * false, <code>getRanges(int, int)</code> can be used to get the
     * associated ranges.
     * </p>
     *
     * @param includeRanges whether the start and length field of the StyleRanges should be set.
     *
     * @return the styles or an empty array if a LineStyleListener has been set.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.2
     *
     * @see #getRanges(int, int)
     * @see #setStyleRanges(int[], StyleRange[])
     */
    public StyleRange[] getStyleRanges(boolean includeRanges) {
        return Convert.array(getDelegate().getStyleRanges(includeRanges), IStyleRange::getApi, StyleRange[]::new);
    }

    /**
     * Returns the styles for the given text range.
     * <p>
     * Returns an empty array if a LineStyleListener has been set.
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * </p><p>
     * Note: Because the StyleRange includes the start and length, the
     * same instance cannot occur multiple times in the array of styles.
     * If the same style attributes, such as font and color, occur in
     * multiple StyleRanges, <code>getStyleRanges(int, int, boolean)</code>
     * can be used to get the styles without the ranges.
     * </p>
     * @param start the start offset of the style ranges to return
     * @param length the number of style ranges to return
     *
     * @return the styles or an empty array if a LineStyleListener has
     *  been set.  The returned styles will reflect the given range.  The first
     *  returned <code>StyleRange</code> will have a starting offset &gt;= start
     *  and the last returned <code>StyleRange</code> will have an ending
     *  offset &lt;= start + length - 1
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when start and/or end are outside the widget content</li>
     * </ul>
     *
     * @see #getStyleRanges(int, int, boolean)
     *
     * @since 3.0
     */
    public StyleRange[] getStyleRanges(int start, int length) {
        return Convert.array(getDelegate().getStyleRanges(start, length), IStyleRange::getApi, StyleRange[]::new);
    }

    /**
     * Returns the styles for the given text range.
     * <p>
     * Returns an empty array if a LineStyleListener has been set.
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * </p><p>
     * Note: When <code>includeRanges</code> is true, the start and length
     * fields of each StyleRange will be valid, however the StyleRange
     * objects may need to be cloned. When <code>includeRanges</code> is
     * false, <code>getRanges(int, int)</code> can be used to get the
     * associated ranges.
     * </p>
     *
     * @param start the start offset of the style ranges to return
     * @param length the number of style ranges to return
     * @param includeRanges whether the start and length field of the StyleRanges should be set.
     *
     * @return the styles or an empty array if a LineStyleListener has
     *  been set.  The returned styles will reflect the given range.  The first
     *  returned <code>StyleRange</code> will have a starting offset &gt;= start
     *  and the last returned <code>StyleRange</code> will have an ending
     *  offset &gt;= start + length - 1
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when start and/or end are outside the widget content</li>
     * </ul>
     *
     * @since 3.2
     *
     * @see #getRanges(int, int)
     * @see #setStyleRanges(int[], StyleRange[])
     */
    public StyleRange[] getStyleRanges(int start, int length, boolean includeRanges) {
        return Convert.array(getDelegate().getStyleRanges(start, length, includeRanges), IStyleRange::getApi, StyleRange[]::new);
    }

    /**
     * Returns the tab width measured in characters.
     *
     * @return tab width measured in characters
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getTabStops()
     */
    public int getTabs() {
        return getDelegate().getTabs();
    }

    /**
     * Returns the tab list of the receiver.
     *
     * @return the tab list
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.6
     */
    public int[] getTabStops() {
        return getDelegate().getTabStops();
    }

    /**
     * Returns a copy of the widget content.
     *
     * @return copy of the widget content
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public String getText() {
        return getDelegate().getText();
    }

    /**
     * Returns the widget content between the two offsets.
     *
     * @param start offset of the first character in the returned String
     * @param end offset of the last character in the returned String
     * @return widget content starting at start and ending at end
     * @see #getTextRange(int,int)
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when start and/or end are outside the widget content</li>
     * </ul>
     */
    public String getText(int start, int end) {
        return getDelegate().getText(start, end);
    }

    /**
     * Returns the smallest bounding rectangle that includes the characters between two offsets.
     *
     * @param start offset of the first character included in the bounding box
     * @param end offset of the last character included in the bounding box
     * @return bounding box of the text between start and end
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when start and/or end are outside the widget content</li>
     * </ul>
     * @since 3.1
     */
    public Rectangle getTextBounds(int start, int end) {
        return getDelegate().getTextBounds(start, end).getApi();
    }

    /**
     * Returns the widget content starting at start for length characters.
     *
     * @param start offset of the first character in the returned String
     * @param length number of characters to return
     * @return widget content starting at start and extending length characters.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when start and/or length are outside the widget content</li>
     * </ul>
     */
    public String getTextRange(int start, int length) {
        return getDelegate().getTextRange(start, length);
    }

    /**
     * Returns the maximum number of characters that the receiver is capable of holding.
     *
     * @return the text limit
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getTextLimit() {
        return getDelegate().getTextLimit();
    }

    /**
     * Gets the top index.
     * <p>
     * The top index is the index of the fully visible line that is currently
     * at the top of the widget or the topmost partially visible line if no line is fully visible.
     * The top index changes when the widget is scrolled. Indexing is zero based.
     * </p>
     *
     * @return the index of the top line
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getTopIndex() {
        return getDelegate().getTopIndex();
    }

    /**
     * Returns the top margin.
     *
     * @return the top margin.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public int getTopMargin() {
        return getDelegate().getTopMargin();
    }

    /**
     * Gets the top SWT logical point.
     * <p>
     * The top point is the SWT logical point position of the line that is
     * currently at the top of the widget. The text widget can be scrolled by points
     * by dragging the scroll thumb so that a partial line may be displayed at the top
     * the widget.  The top point changes when the widget is scrolled.  The top point
     * does not include the widget trimming.
     * </p>
     *
     * @return SWT logical point position of the top line
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getTopPixel() {
        return getDelegate().getTopPixel();
    }

    /**
     * Returns whether the widget wraps lines.
     *
     * @return true if widget wraps lines, false otherwise
     * @since 2.0
     */
    public boolean getWordWrap() {
        return getDelegate().getWordWrap();
    }

    /**
     * Returns the wrap indentation of the widget.
     *
     * @return the wrap indentation
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #getLineWrapIndent(int)
     *
     * @since 3.6
     */
    public int getWrapIndent() {
        return getDelegate().getWrapIndent();
    }

    /**
     * Inserts a string.  The old selection is replaced with the new text.
     *
     * @param string the string
     * @see #replaceTextRange(int,int,String)
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when string is null</li>
     * </ul>
     */
    public void insert(String string) {
        getDelegate().insert(string);
    }

    /**
     * Executes the action.
     *
     * @param action one of the actions defined in ST.java
     */
    public void invokeAction(int action) {
        getDelegate().invokeAction(action);
    }

    /**
     * Returns <code>true</code> if any text in the widget is selected,
     * and <code>false</code> otherwise.
     *
     * @return the text selection state
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.103
     */
    public boolean isTextSelected() {
        return getDelegate().isTextSelected();
    }

    /**
     * Replaces the selection with the text on the <code>DND.CLIPBOARD</code>
     * clipboard  or, if there is no selection,  inserts the text at the current
     * caret offset.   If the widget has the SWT.SINGLE style and the
     * clipboard text contains more than one line, only the first line without
     * line delimiters is  inserted in the widget.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void paste() {
        getDelegate().paste();
    }

    /**
     * Prints the widget's text to the default printer.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void print() {
        getDelegate().print();
    }

    /**
     * Returns a runnable that will print the widget's text
     * to the specified printer.
     * <p>
     * The runnable may be run in a non-UI thread.
     * </p>
     *
     * @param printer the printer to print to
     *
     * @return a <code>Runnable</code> for printing the receiver's text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when printer is null</li>
     * </ul>
     */
    public Runnable print(Printer printer) {
        return getDelegate().print(printer.getDelegate());
    }

    /**
     * Returns a runnable that will print the widget's text
     * to the specified printer.
     * <p>
     * The runnable may be run in a non-UI thread.
     * </p>
     *
     * @param printer the printer to print to
     * @param options print options to use during printing
     *
     * @return a <code>Runnable</code> for printing the receiver's text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when printer or options is null</li>
     * </ul>
     * @since 2.1
     */
    public Runnable print(Printer printer, StyledTextPrintOptions options) {
        return getDelegate().print(printer.getDelegate(), options.getDelegate());
    }

    /**
     * Causes the entire bounds of the receiver to be marked
     * as needing to be redrawn. The next time a paint request
     * is processed, the control will be completely painted.
     * <p>
     * Recalculates the content width for all lines in the bounds.
     * When a <code>LineStyleListener</code> is used a redraw call
     * is the only notification to the widget that styles have changed
     * and that the content width may have changed.
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Control#update()
     */
    public void redraw() {
        getDelegate().redraw();
    }

    /**
     * Causes the rectangular area of the receiver specified by
     * the arguments to be marked as needing to be redrawn.
     * The next time a paint request is processed, that area of
     * the receiver will be painted. If the <code>all</code> flag
     * is <code>true</code>, any children of the receiver which
     * intersect with the specified area will also paint their
     * intersecting areas. If the <code>all</code> flag is
     * <code>false</code>, the children will not be painted.
     * <p>
     * Marks the content width of all lines in the specified rectangle
     * as unknown. Recalculates the content width of all visible lines.
     * When a <code>LineStyleListener</code> is used a redraw call
     * is the only notification to the widget that styles have changed
     * and that the content width may have changed.
     * </p>
     *
     * @param x the x coordinate of the area to draw
     * @param y the y coordinate of the area to draw
     * @param width the width of the area to draw
     * @param height the height of the area to draw
     * @param all <code>true</code> if children should redraw, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see Control#update()
     */
    public void redraw(int x, int y, int width, int height, boolean all) {
        getDelegate().redraw(x, y, width, height, all);
    }

    /**
     *  Redraws the specified text range.
     *
     *  @param start offset of the first character to redraw
     *  @param length number of characters to redraw
     *  @param clearBackground true if the background should be cleared as
     *   part of the redraw operation.  If true, the entire redraw range will
     *   be cleared before anything is redrawn.  If the redraw range includes
     * 	the last character of a line (i.e., the entire line is redrawn) the
     *  	line is cleared all the way to the right border of the widget.
     *  	The redraw operation will be faster and smoother if clearBackground
     *  	is set to false.  Whether or not the flag can be set to false depends
     *  	on the type of change that has taken place.  If font styles or
     *  	background colors for the redraw range have changed, clearBackground
     *  	should be set to true.  If only foreground colors have changed for
     *  	the redraw range, clearBackground can be set to false.
     *  @exception SWTException <ul>
     *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *  </ul>
     *  @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE when start and/or end are outside the widget content</li>
     *  </ul>
     */
    public void redrawRange(int start, int length, boolean clearBackground) {
        getDelegate().redrawRange(start, length, clearBackground);
    }

    /**
     * Removes the specified bidirectional segment listener.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     *
     * @since 2.0
     */
    public void removeBidiSegmentListener(BidiSegmentListener listener) {
        getDelegate().removeBidiSegmentListener(listener);
    }

    /**
     * Removes the specified caret listener.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     *
     * @since 3.5
     */
    public void removeCaretListener(CaretListener listener) {
        getDelegate().removeCaretListener(listener);
    }

    /**
     * Removes the specified extended modify listener.
     *
     * @param extendedModifyListener the listener which should no longer be notified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void removeExtendedModifyListener(ExtendedModifyListener extendedModifyListener) {
        getDelegate().removeExtendedModifyListener(extendedModifyListener);
    }

    /**
     * Removes the specified line background listener.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void removeLineBackgroundListener(LineBackgroundListener listener) {
        getDelegate().removeLineBackgroundListener(listener);
    }

    /**
     * Removes the specified line style listener.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void removeLineStyleListener(LineStyleListener listener) {
        getDelegate().removeLineStyleListener(listener);
    }

    /**
     * Removes the specified modify listener.
     *
     * @param modifyListener the listener which should no longer be notified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void removeModifyListener(ModifyListener modifyListener) {
        getDelegate().removeModifyListener(modifyListener);
    }

    /**
     * Removes the specified listener.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     * @since 3.2
     */
    public void removePaintObjectListener(PaintObjectListener listener) {
        getDelegate().removePaintObjectListener(listener);
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
        getDelegate().removeSelectionListener(listener);
    }

    /**
     * Removes the specified verify listener.
     *
     * @param verifyListener the listener which should no longer be notified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void removeVerifyListener(VerifyListener verifyListener) {
        getDelegate().removeVerifyListener(verifyListener);
    }

    /**
     * Removes the specified key verify listener.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void removeVerifyKeyListener(VerifyKeyListener listener) {
        getDelegate().removeVerifyKeyListener(listener);
    }

    /**
     * Removes the specified word movement listener.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     *
     * @see MovementEvent
     * @see MovementListener
     * @see #addWordMovementListener
     *
     * @since 3.3
     */
    public void removeWordMovementListener(MovementListener listener) {
        getDelegate().removeWordMovementListener(listener);
    }

    /**
     * Replaces the styles in the given range with new styles.  This method
     * effectively deletes the styles in the given range and then adds the
     * the new styles.
     * <p>
     * Note: Because a StyleRange includes the start and length, the
     * same instance cannot occur multiple times in the array of styles.
     * If the same style attributes, such as font and color, occur in
     * multiple StyleRanges, <code>setStyleRanges(int, int, int[], StyleRange[])</code>
     * can be used to share styles and reduce memory usage.
     * </p><p>
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * </p>
     *
     * @param start offset of first character where styles will be deleted
     * @param length length of the range to delete styles in
     * @param ranges StyleRange objects containing the new style information.
     * The ranges should not overlap and should be within the specified start
     * and length. The style rendering is undefined if the ranges do overlap
     * or are ill-defined. Must not be null.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when either start or end is outside the valid range (0 &lt;= offset &lt;= getCharCount())</li>
     *   <li>ERROR_NULL_ARGUMENT when ranges is null</li>
     * </ul>
     *
     * @since 2.0
     *
     * @see #setStyleRanges(int, int, int[], StyleRange[])
     */
    public void replaceStyleRanges(int start, int length, StyleRange[] ranges) {
        getDelegate().replaceStyleRanges(start, length, Convert.array(ranges, StyleRange::getDelegate, IStyleRange[]::new));
    }

    /**
     * Replaces the given text range with new text.
     * If the widget has the SWT.SINGLE style and "text" contains more than
     * one line, only the first line is rendered but the text is stored
     * unchanged. A subsequent call to getText will return the same text
     * that was set. Note that only a single line of text should be set when
     * the SWT.SINGLE style is used.
     * <p>
     * <b>NOTE:</b> During the replace operation the current selection is
     * changed as follows:
     * </p>
     * <ul>
     * <li>selection before replaced text: selection unchanged
     * <li>selection after replaced text: adjust the selection so that same text
     * remains selected
     * <li>selection intersects replaced text: selection is cleared and caret
     * is placed after inserted text
     * </ul>
     *
     * @param start offset of first character to replace
     * @param length number of characters to replace. Use 0 to insert text
     * @param text new text. May be empty to delete text.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when either start or end is outside the valid range (0 &lt;= offset &lt;= getCharCount())</li>
     *   <li>ERROR_INVALID_ARGUMENT when either start or end is inside a multi byte line delimiter.
     * 		Splitting a line delimiter for example by inserting text in between the CR and LF and deleting part of a line delimiter is not supported</li>
     *   <li>ERROR_NULL_ARGUMENT when string is null</li>
     * </ul>
     */
    public void replaceTextRange(int start, int length, String text) {
        getDelegate().replaceTextRange(start, length, text);
    }

    public void scroll(int destX, int destY, int x, int y, int width, int height, boolean all) {
        getDelegate().scroll(destX, destY, x, y, width, height, all);
    }

    /**
     * Selects all the text.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void selectAll() {
        getDelegate().selectAll();
    }

    /**
     * Sets the alignment of the widget. The argument should be one of <code>SWT.LEFT</code>,
     * <code>SWT.CENTER</code> or <code>SWT.RIGHT</code>. The alignment applies for all lines.
     * <p>
     * Note that if <code>SWT.MULTI</code> is set, then <code>SWT.WRAP</code> must also be set
     * in order to stabilize the right edge before setting alignment.
     * </p>
     *
     * @param alignment the new alignment
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setLineAlignment(int, int, int)
     *
     * @since 3.2
     */
    public void setAlignment(int alignment) {
        getDelegate().setAlignment(alignment);
    }

    /**
     * Set the Always Show Scrollbars flag.  True if the scrollbars are
     * always shown even if they are not required (default value).  False if the scrollbars are only
     * visible when some part of the content needs to be scrolled to be seen.
     * The H_SCROLL and V_SCROLL style bits are also required to enable scrollbars in the
     * horizontal and vertical directions.
     *
     * @param show true to show the scrollbars even when not required, false to show scrollbars only when required
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.8
     */
    public void setAlwaysShowScrollBars(boolean show) {
        getDelegate().setAlwaysShowScrollBars(show);
    }

    /**
     * @see Control#setBackground(Color)
     */
    public void setBackground(Color color) {
        getDelegate().setBackground(color.getDelegate());
    }

    /**
     * Sets the block selection mode.
     *
     * @param blockSelection true=enable block selection, false=disable block selection
     *
     * @since 3.5
     */
    public void setBlockSelection(boolean blockSelection) {
        getDelegate().setBlockSelection(blockSelection);
    }

    /**
     * Sets the block selection bounds. The bounds is
     * relative to the upper left corner of the document.
     *
     * @param rect the new bounds for the block selection
     *
     * @see #setBlockSelectionBounds(int, int, int, int)
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT when point is null</li>
     * </ul>
     *
     * @since 3.5
     */
    public void setBlockSelectionBounds(Rectangle rect) {
        getDelegate().setBlockSelectionBounds(rect.getDelegate());
    }

    /**
     * Sets the block selection bounds. The bounds is
     * relative to the upper left corner of the document.
     *
     * @param x the new x coordinate for the block selection
     * @param y the new y coordinate for the block selection
     * @param width the new width for the block selection
     * @param height the new height for the block selection
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public void setBlockSelectionBounds(int x, int y, int width, int height) {
        getDelegate().setBlockSelectionBounds(x, y, width, height);
    }

    /**
     * Sets the receiver's caret.  Set the caret's height and location.
     *
     * @param caret the new caret for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setCaret(Caret caret) {
        getDelegate().setCaret(caret.getDelegate());
    }

    /**
     * Sets the BIDI coloring mode.  When true the BIDI text display
     * algorithm is applied to segments of text that are the same
     * color.
     *
     * @param mode the new coloring mode
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @deprecated use BidiSegmentListener instead.
     */
    @Deprecated
    public void setBidiColoring(boolean mode) {
        getDelegate().setBidiColoring(mode);
    }

    /**
     * Sets the bottom margin.
     *
     * @param bottomMargin the bottom margin.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public void setBottomMargin(int bottomMargin) {
        getDelegate().setBottomMargin(bottomMargin);
    }

    /**
     * Sets the caret offset.
     *
     * @param offset caret offset, relative to the first character in the text.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when the offset is inside a multi byte line
     *   delimiter (and thus neither clearly in front of or after the line delimiter)
     * </ul>
     */
    public void setCaretOffset(int offset) {
        getDelegate().setCaretOffset(offset);
    }

    /**
     * Sets the content implementation to use for text storage.
     *
     * @param newContent StyledTextContent implementation to use for text storage.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when listener is null</li>
     * </ul>
     */
    public void setContent(StyledTextContent newContent) {
        getDelegate().setContent(newContent);
    }

    /**
     * Sets the receiver's cursor to the cursor specified by the
     * argument.  Overridden to handle the null case since the
     * StyledText widget uses an ibeam as its default cursor.
     *
     * @see Control#setCursor(Cursor)
     */
    public void setCursor(Cursor cursor) {
        getDelegate().setCursor(cursor.getDelegate());
    }

    /**
     * Sets whether the widget implements double click mouse behavior.
     *
     * @param enable if true double clicking a word selects the word, if false
     * 	double clicks have the same effect as regular mouse clicks.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setDoubleClickEnabled(boolean enable) {
        getDelegate().setDoubleClickEnabled(enable);
    }

    public void setDragDetect(boolean dragDetect) {
        getDelegate().setDragDetect(dragDetect);
    }

    /**
     * Sets whether the widget content can be edited.
     *
     * @param editable if true content can be edited, if false content can not be
     * 	edited
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setEditable(boolean editable) {
        getDelegate().setEditable(editable);
    }

    public void setEnabled(boolean enabled) {
        getDelegate().setEnabled(enabled);
    }

    public boolean setFocus() {
        return getDelegate().setFocus();
    }

    /**
     * See {@link TextLayout#setFixedLineMetrics}
     *
     * @since 3.125
     */
    public void setFixedLineMetrics(FontMetrics metrics) {
        getDelegate().setFixedLineMetrics(metrics.getDelegate());
    }

    /**
     * Sets a new font to render text with.
     * <p>
     * <b>NOTE:</b> Italic fonts are not supported unless they have no overhang
     * and the same baseline as regular fonts.
     * </p>
     *
     * @param font new font
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setFont(Font font) {
        getDelegate().setFont(font.getDelegate());
    }

    public void setForeground(Color color) {
        getDelegate().setForeground(color.getDelegate());
    }

    /**
     * Sets the horizontal scroll offset relative to the start of the line.
     * Do nothing if there is no text set.
     * <p>
     * <b>NOTE:</b> The horizontal index is reset to 0 when new text is set in the
     * widget.
     * </p>
     *
     * @param offset horizontal scroll offset relative to the start
     * 	of the line, measured in character increments starting at 0, if
     * 	equal to 0 the content is not scrolled, if &gt; 0 = the content is scrolled.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setHorizontalIndex(int offset) {
        getDelegate().setHorizontalIndex(offset);
        // allow any value if client area width is unknown or 0.
        // offset will be checked in resize handler.
        // don't use isVisible since width is known even if widget
    }

    /**
     * Sets the horizontal SWT logical point offset relative to the start of the line.
     * Do nothing if there is no text set.
     * <p>
     * <b>NOTE:</b> The horizontal SWT logical point offset is reset to 0 when new text
     * is set in the widget.
     * </p>
     *
     * @param pixel horizontal SWT logical point offset relative to the start
     * 	of the line.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 2.0
     */
    public void setHorizontalPixel(int pixel) {
        getDelegate().setHorizontalPixel(pixel);
        // allow any value if client area width is unknown or 0.
        // offset will be checked in resize handler.
        // don't use isVisible since width is known even if widget
    }

    /**
     * Sets the line indentation of the widget.
     * <p>
     * It is the amount of blank space, in points, at the beginning of each line.
     * When a line wraps in several lines only the first one is indented.
     * </p>
     *
     * @param indent the new indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setLineIndent(int, int, int)
     *
     * @since 3.2
     */
    public void setIndent(int indent) {
        getDelegate().setIndent(indent);
    }

    /**
     * Sets whether the widget should justify lines.
     *
     * @param justify whether lines should be justified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setLineJustify(int, int, boolean)
     *
     * @since 3.2
     */
    public void setJustify(boolean justify) {
        getDelegate().setJustify(justify);
    }

    /**
     * Maps a key to an action.
     * <p>
     * One action can be associated with N keys. However, each key can only
     * have one action (key:action is N:1 relation).
     * </p>
     *
     * @param key a key code defined in SWT.java or a character.
     * 	Optionally ORd with a state mask.  Preferred state masks are one or more of
     *  SWT.MOD1, SWT.MOD2, SWT.MOD3, since these masks account for modifier platform
     *  differences.  However, there may be cases where using the specific state masks
     *  (i.e., SWT.CTRL, SWT.SHIFT, SWT.ALT, SWT.COMMAND) makes sense.
     * @param action one of the predefined actions defined in ST.java.
     * 	Use SWT.NULL to remove a key binding.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setKeyBinding(int key, int action) {
        getDelegate().setKeyBinding(key, action);
    }

    /**
     * Sets the left margin.
     *
     * @param leftMargin the left margin.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public void setLeftMargin(int leftMargin) {
        getDelegate().setLeftMargin(leftMargin);
    }

    /**
     * Sets the alignment of the specified lines. The argument should be one of <code>SWT.LEFT</code>,
     * <code>SWT.CENTER</code> or <code>SWT.RIGHT</code>.
     * <p>
     * Note that if <code>SWT.MULTI</code> is set, then <code>SWT.WRAP</code> must also be set
     * in order to stabilize the right edge before setting alignment.
     * </p><p>
     * Should not be called if a LineStyleListener has been set since the listener
     * maintains the line attributes.
     * </p><p>
     * All line attributes are maintained relative to the line text, not the
     * line index that is specified in this method call.
     * During text changes, when entire lines are inserted or removed, the line
     * attributes that are associated with the lines after the change
     * will "move" with their respective text. An entire line is defined as
     * extending from the first character on a line to the last and including the
     * line delimiter.
     * </p>
     * When two lines are joined by deleting a line delimiter, the top line
     * attributes take precedence and the attributes of the bottom line are deleted.
     * For all other text changes line attributes will remain unchanged.
     *
     * @param startLine first line the alignment is applied to, 0 based
     * @param lineCount number of lines the alignment applies to.
     * @param alignment line alignment
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when the specified line range is invalid</li>
     * </ul>
     * @see #setAlignment(int)
     * @since 3.2
     */
    public void setLineAlignment(int startLine, int lineCount, int alignment) {
        getDelegate().setLineAlignment(startLine, lineCount, alignment);
    }

    /**
     * Sets the background color of the specified lines.
     * <p>
     * The background color is drawn for the width of the widget. All
     * line background colors are discarded when setText is called.
     * The text background color if defined in a StyleRange overlays the
     * line background color.
     * </p><p>
     * Should not be called if a LineBackgroundListener has been set since the
     * listener maintains the line backgrounds.
     * </p><p>
     * All line attributes are maintained relative to the line text, not the
     * line index that is specified in this method call.
     * During text changes, when entire lines are inserted or removed, the line
     * attributes that are associated with the lines after the change
     * will "move" with their respective text. An entire line is defined as
     * extending from the first character on a line to the last and including the
     * line delimiter.
     * </p><p>
     * When two lines are joined by deleting a line delimiter, the top line
     * attributes take precedence and the attributes of the bottom line are deleted.
     * For all other text changes line attributes will remain unchanged.
     * </p>
     *
     * @param startLine first line the color is applied to, 0 based
     * @param lineCount number of lines the color applies to.
     * @param background line background color
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when the specified line range is invalid</li>
     * </ul>
     */
    public void setLineBackground(int startLine, int lineCount, Color background) {
        getDelegate().setLineBackground(startLine, lineCount, background.getDelegate());
    }

    /**
     * Sets the bullet of the specified lines.
     * <p>
     * Should not be called if a LineStyleListener has been set since the listener
     * maintains the line attributes.
     * </p><p>
     * All line attributes are maintained relative to the line text, not the
     * line index that is specified in this method call.
     * During text changes, when entire lines are inserted or removed, the line
     * attributes that are associated with the lines after the change
     * will "move" with their respective text. An entire line is defined as
     * extending from the first character on a line to the last and including the
     * line delimiter.
     * </p><p>
     * When two lines are joined by deleting a line delimiter, the top line
     * attributes take precedence and the attributes of the bottom line are deleted.
     * For all other text changes line attributes will remain unchanged.
     * </p>
     *
     * @param startLine first line the bullet is applied to, 0 based
     * @param lineCount number of lines the bullet applies to.
     * @param bullet line bullet
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when the specified line range is invalid</li>
     * </ul>
     * @since 3.2
     */
    public void setLineBullet(int startLine, int lineCount, Bullet bullet) {
        getDelegate().setLineBullet(startLine, lineCount, bullet.getDelegate());
    }

    /**
     * Sets the indent of the specified lines.
     * <p>
     * Should not be called if a LineStyleListener has been set since the listener
     * maintains the line attributes.
     * </p><p>
     * All line attributes are maintained relative to the line text, not the
     * line index that is specified in this method call.
     * During text changes, when entire lines are inserted or removed, the line
     * attributes that are associated with the lines after the change
     * will "move" with their respective text. An entire line is defined as
     * extending from the first character on a line to the last and including the
     * line delimiter.
     * </p><p>
     * When two lines are joined by deleting a line delimiter, the top line
     * attributes take precedence and the attributes of the bottom line are deleted.
     * For all other text changes line attributes will remain unchanged.
     * </p>
     *
     * @param startLine first line the indent is applied to, 0 based
     * @param lineCount number of lines the indent applies to.
     * @param indent line indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when the specified line range is invalid</li>
     * </ul>
     * @see #setIndent(int)
     * @since 3.2
     */
    public void setLineIndent(int startLine, int lineCount, int indent) {
        getDelegate().setLineIndent(startLine, lineCount, indent);
    }

    /**
     * Sets the vertical indent of the specified lines.
     * <p>
     * Should not be called if a LineStyleListener has been set since the listener
     * maintains the line attributes.
     * </p><p>
     * All line attributes are maintained relative to the line text, not the
     * line index that is specified in this method call.
     * During text changes, when entire lines are inserted or removed, the line
     * attributes that are associated with the lines after the change
     * will "move" with their respective text. An entire line is defined as
     * extending from the first character on a line to the last and including the
     * line delimiter.
     * </p><p>
     * When two lines are joined by deleting a line delimiter, the top line
     * attributes take precedence and the attributes of the bottom line are deleted.
     * For all other text changes line attributes will remain unchanged.
     * </p><p>
     * Setting both line spacing and vertical indent on a line would result in the
     * spacing and indent add up for the line.
     * </p>
     *
     * @param lineIndex line index the vertical indent is applied to, 0 based
     * @param verticalLineIndent vertical line indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when the specified line index is invalid</li>
     * </ul>
     * @since 3.109
     */
    public void setLineVerticalIndent(int lineIndex, int verticalLineIndent) {
        getDelegate().setLineVerticalIndent(lineIndex, verticalLineIndent);
    }

    /**
     * Sets the justify of the specified lines.
     * <p>
     * Should not be called if a LineStyleListener has been set since the listener
     * maintains the line attributes.
     * </p><p>
     * All line attributes are maintained relative to the line text, not the
     * line index that is specified in this method call.
     * During text changes, when entire lines are inserted or removed, the line
     * attributes that are associated with the lines after the change
     * will "move" with their respective text. An entire line is defined as
     * extending from the first character on a line to the last and including the
     * line delimiter.
     * </p><p>
     * When two lines are joined by deleting a line delimiter, the top line
     * attributes take precedence and the attributes of the bottom line are deleted.
     * For all other text changes line attributes will remain unchanged.
     * </p>
     *
     * @param startLine first line the justify is applied to, 0 based
     * @param lineCount number of lines the justify applies to.
     * @param justify true if lines should be justified
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when the specified line range is invalid</li>
     * </ul>
     * @see #setJustify(boolean)
     * @since 3.2
     */
    public void setLineJustify(int startLine, int lineCount, boolean justify) {
        getDelegate().setLineJustify(startLine, lineCount, justify);
    }

    /**
     * Sets the line spacing of the widget. The line spacing applies for all lines.
     * In the case of #setLineSpacingProvider(StyledTextLineSpacingProvider) is customized,
     * the line spacing are applied only for the lines which are not managed by {@link StyledTextLineSpacingProvider}.
     *
     * @param lineSpacing the line spacing
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @see #setLineSpacingProvider(StyledTextLineSpacingProvider)
     * @since 3.2
     */
    public void setLineSpacing(int lineSpacing) {
        getDelegate().setLineSpacing(lineSpacing);
    }

    /**
     * Sets the line spacing provider of the widget. The line spacing applies for some lines with customized spacing
     * or reset the customized spacing if the argument is null.
     *
     * @param lineSpacingProvider the line spacing provider (or null)
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @see #setLineSpacingProvider(StyledTextLineSpacingProvider)
     * @since 3.107
     */
    public void setLineSpacingProvider(StyledTextLineSpacingProvider lineSpacingProvider) {
        getDelegate().setLineSpacingProvider(lineSpacingProvider);
    }

    /**
     * Sets the tab stops of the specified lines.
     * <p>
     * Should not be called if a <code>LineStyleListener</code> has been set since the listener
     * maintains the line attributes.
     * </p><p>
     * All line attributes are maintained relative to the line text, not the
     * line index that is specified in this method call.
     * During text changes, when entire lines are inserted or removed, the line
     * attributes that are associated with the lines after the change
     * will "move" with their respective text. An entire line is defined as
     * extending from the first character on a line to the last and including the
     * line delimiter.
     * </p><p>
     * When two lines are joined by deleting a line delimiter, the top line
     * attributes take precedence and the attributes of the bottom line are deleted.
     * For all other text changes line attributes will remain unchanged.
     * </p>
     *
     * @param startLine first line the justify is applied to, 0 based
     * @param lineCount number of lines the justify applies to.
     * @param tabStops tab stops
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when the specified line range is invalid</li>
     * </ul>
     * @see #setTabStops(int[])
     * @since 3.6
     */
    public void setLineTabStops(int startLine, int lineCount, int[] tabStops) {
        getDelegate().setLineTabStops(startLine, lineCount, tabStops);
    }

    /**
     * Sets the wrap indent of the specified lines.
     * <p>
     * Should not be called if a <code>LineStyleListener</code> has been set since the listener
     * maintains the line attributes.
     * </p><p>
     * All line attributes are maintained relative to the line text, not the
     * line index that is specified in this method call.
     * During text changes, when entire lines are inserted or removed, the line
     * attributes that are associated with the lines after the change
     * will "move" with their respective text. An entire line is defined as
     * extending from the first character on a line to the last and including the
     * line delimiter.
     * </p><p>
     * When two lines are joined by deleting a line delimiter, the top line
     * attributes take precedence and the attributes of the bottom line are deleted.
     * For all other text changes line attributes will remain unchanged.
     * </p>
     *
     * @param startLine first line the wrap indent is applied to, 0 based
     * @param lineCount number of lines the wrap indent applies to.
     * @param wrapIndent line wrap indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when the specified line range is invalid</li>
     * </ul>
     * @see #setWrapIndent(int)
     * @since 3.6
     */
    public void setLineWrapIndent(int startLine, int lineCount, int wrapIndent) {
        getDelegate().setLineWrapIndent(startLine, lineCount, wrapIndent);
    }

    /**
     * Sets the color of the margins.
     *
     * @param color the new color (or null)
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public void setMarginColor(Color color) {
        getDelegate().setMarginColor(color.getDelegate());
    }

    /**
     * Sets the margins.
     *
     * @param leftMargin the left margin.
     * @param topMargin the top margin.
     * @param rightMargin the right margin.
     * @param bottomMargin the bottom margin.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public void setMargins(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        getDelegate().setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
    }

    /**
     * Sets the enabled state of the mouse navigator. When the mouse navigator is enabled, the user can navigate through the widget
     * by pressing the middle button and moving the cursor.
     *
     * @param enabled if true, the mouse navigator is enabled, if false the mouse navigator is deactivated
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 3.110
     */
    public void setMouseNavigatorEnabled(boolean enabled) {
        getDelegate().setMouseNavigatorEnabled(enabled);
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
    public void setOrientation(int orientation) {
        getDelegate().setOrientation(orientation);
    }

    /**
     * Sets the right margin.
     *
     * @param rightMargin the right margin.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public void setRightMargin(int rightMargin) {
        getDelegate().setRightMargin(rightMargin);
    }

    /**
     * Sets the selection to the given position and scrolls it into view.  Equivalent to setSelection(start,start).
     *
     * @param start new caret position
     * @see #setSelection(int,int)
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when either the start or the end of the selection range is inside a
     * multi byte line delimiter (and thus neither clearly in front of or after the line delimiter)
     * </ul>
     */
    public void setSelection(int start) {
        getDelegate().setSelection(start);
    }

    /**
     * Sets the selection and scrolls it into view.
     * <p>
     * Indexing is zero based.  Text selections are specified in terms of
     * caret positions.  In a text widget that contains N characters, there are
     * N+1 caret positions, ranging from 0..N
     * </p>
     *
     * @param point x=selection start offset, y=selection end offset
     * 	The caret will be placed at the selection start when x &gt; y.
     * @see #setSelection(int,int)
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_NULL_ARGUMENT when point is null</li>
     *   <li>ERROR_INVALID_ARGUMENT when either the start or the end of the selection range is inside a
     * multi byte line delimiter (and thus neither clearly in front of or after the line delimiter)
     * </ul>
     */
    public void setSelection(Point point) {
        getDelegate().setSelection(point.getDelegate());
    }

    /**
     * Sets the receiver's selection background color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     *
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 2.1
     */
    public void setSelectionBackground(Color color) {
        getDelegate().setSelectionBackground(color.getDelegate());
    }

    /**
     * Sets the receiver's selection foreground color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note that this is a <em>HINT</em>. Some platforms do not allow the application
     * to change the selection foreground color.
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
     * @since 2.1
     */
    public void setSelectionForeground(Color color) {
        getDelegate().setSelectionForeground(color.getDelegate());
    }

    /**
     * Sets the selection and scrolls it into view.
     * <p>
     * Indexing is zero based.  Text selections are specified in terms of
     * caret positions.  In a text widget that contains N characters, there are
     * N+1 caret positions, ranging from 0..N
     * </p>
     *
     * @param start selection start offset. The caret will be placed at the
     * 	selection start when start &gt; end.
     * @param end selection end offset
     * @see #setSelectionRange(int,int)
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when either the start or the end of the selection range is inside a
     * multi byte line delimiter (and thus neither clearly in front of or after the line delimiter)
     * </ul>
     */
    public void setSelection(int start, int end) {
        getDelegate().setSelection(start, end);
    }

    /**
     * Sets the selection.
     * <p>
     * The new selection may not be visible. Call showSelection to scroll the selection
     * into view. A negative length places the caret at the visual start of the selection.
     * </p>
     *
     * @param start offset of the first selected character
     * @param length number of characters to select
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when either the start or the end of the selection range is inside a
     * multi byte line delimiter (and thus neither clearly in front of or after the line delimiter)
     * </ul>
     */
    public void setSelectionRange(int start, int length) {
        getDelegate().setSelectionRange(start, length);
    }

    /**
     * Sets the selected locations/ranges.
     * <p>
     * The new selection may not be visible. Call showSelection to scroll the selection
     * into view. A negative length places the caret at the visual start of the selection.
     * </p>
     *
     * @param ranges an array of offset/length pairs.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_ARGUMENT when either the start or the end of one selection range is inside a
     * multi byte line delimiter (and thus neither clearly in front of or after the line delimiter)</li>
     *   <li>ERROR_INVALID_ARGUMENT when ranges are null or size isn't valid (not pair)</li>
     * </ul>
     * @see #getSelectionRanges()
     * @since 3.117
     */
    public void setSelectionRanges(int[] ranges) {
        getDelegate().setSelectionRanges(ranges);
    }

    /**
     * Adds the specified style.
     * <p>
     * The new style overwrites existing styles for the specified range.
     * Existing style ranges are adjusted if they partially overlap with
     * the new style. To clear an individual style, call setStyleRange
     * with a StyleRange that has null attributes.
     * </p><p>
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * </p>
     *
     * @param range StyleRange object containing the style information.
     * Overwrites the old style in the given range. May be null to delete
     * all styles.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_INVALID_RANGE when the style range is outside the valid range (&gt; getCharCount())</li>
     * </ul>
     */
    public void setStyleRange(StyleRange range) {
        getDelegate().setStyleRange(range.getDelegate());
    }

    /**
     * Clears the styles in the range specified by <code>start</code> and
     * <code>length</code> and adds the new styles.
     * <p>
     * The ranges array contains start and length pairs.  Each pair refers to
     * the corresponding style in the styles array.  For example, the pair
     * that starts at ranges[n] with length ranges[n+1] uses the style
     * at styles[n/2].  The range fields within each StyleRange are ignored.
     * If ranges or styles is null, the specified range is cleared.
     * </p><p>
     * Note: It is expected that the same instance of a StyleRange will occur
     * multiple times within the styles array, reducing memory usage.
     * </p><p>
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * </p>
     *
     * @param start offset of first character where styles will be deleted
     * @param length length of the range to delete styles in
     * @param ranges the array of ranges.  The ranges must not overlap and must be in order.
     * @param styles the array of StyleRanges.  The range fields within the StyleRange are unused.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when an element in the styles array is null</li>
     *    <li>ERROR_INVALID_RANGE when the number of ranges and style do not match (ranges.length * 2 == styles.length)</li>
     *    <li>ERROR_INVALID_RANGE when a range is outside the valid range (&gt; getCharCount() or less than zero)</li>
     *    <li>ERROR_INVALID_RANGE when a range overlaps</li>
     * </ul>
     *
     * @since 3.2
     */
    public void setStyleRanges(int start, int length, int[] ranges, StyleRange[] styles) {
        getDelegate().setStyleRanges(start, length, ranges, Convert.array(styles, StyleRange::getDelegate, IStyleRange[]::new));
    }

    /**
     * Sets styles to be used for rendering the widget content.
     * <p>
     * All styles in the widget will be replaced with the given set of ranges and styles.
     * The ranges array contains start and length pairs.  Each pair refers to
     * the corresponding style in the styles array.  For example, the pair
     * that starts at ranges[n] with length ranges[n+1] uses the style
     * at styles[n/2].  The range fields within each StyleRange are ignored.
     * If either argument is null, the styles are cleared.
     * </p><p>
     * Note: It is expected that the same instance of a StyleRange will occur
     * multiple times within the styles array, reducing memory usage.
     * </p><p>
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * </p>
     *
     * @param ranges the array of ranges.  The ranges must not overlap and must be in order.
     * @param styles the array of StyleRanges.  The range fields within the StyleRange are unused.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when an element in the styles array is null</li>
     *    <li>ERROR_INVALID_RANGE when the number of ranges and style do not match (ranges.length * 2 == styles.length)</li>
     *    <li>ERROR_INVALID_RANGE when a range is outside the valid range (&gt; getCharCount() or less than zero)</li>
     *    <li>ERROR_INVALID_RANGE when a range overlaps</li>
     * </ul>
     *
     * @since 3.2
     */
    public void setStyleRanges(int[] ranges, StyleRange[] styles) {
        getDelegate().setStyleRanges(ranges, Convert.array(styles, StyleRange::getDelegate, IStyleRange[]::new));
    }

    /**
     * Sets styles to be used for rendering the widget content. All styles
     * in the widget will be replaced with the given set of styles.
     * <p>
     * Note: Because a StyleRange includes the start and length, the
     * same instance cannot occur multiple times in the array of styles.
     * If the same style attributes, such as font and color, occur in
     * multiple StyleRanges, <code>setStyleRanges(int[], StyleRange[])</code>
     * can be used to share styles and reduce memory usage.
     * </p><p>
     * Should not be called if a LineStyleListener has been set since the
     * listener maintains the styles.
     * </p>
     *
     * @param ranges StyleRange objects containing the style information.
     * The ranges should not overlap. The style rendering is undefined if
     * the ranges do overlap. Must not be null. The styles need to be in order.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when the list of ranges is null</li>
     *    <li>ERROR_INVALID_RANGE when the last of the style ranges is outside the valid range (&gt; getCharCount())</li>
     * </ul>
     *
     * @see #setStyleRanges(int[], StyleRange[])
     */
    public void setStyleRanges(StyleRange[] ranges) {
        getDelegate().setStyleRanges(Convert.array(ranges, StyleRange::getDelegate, IStyleRange[]::new));
    }

    /**
     * Sets the tab width.
     *
     * @param tabs tab width measured in characters.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setTabStops(int[])
     */
    public void setTabs(int tabs) {
        getDelegate().setTabs(tabs);
    }

    /**
     * Sets the receiver's tab list. Each value in the tab list specifies
     * the space in points from the origin of the document to the respective
     * tab stop.  The last tab stop width is repeated continuously.
     *
     * @param tabs the new tab list (or null)
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if a tab stop is negative or less than the previous stop in the list</li>
     * </ul>
     *
     * @see StyledText#getTabStops()
     *
     * @since 3.6
     */
    public void setTabStops(int[] tabs) {
        getDelegate().setTabStops(tabs);
    }

    /**
     * Sets the widget content.
     * If the widget has the SWT.SINGLE style and "text" contains more than
     * one line, only the first line is rendered but the text is stored
     * unchanged. A subsequent call to getText will return the same text
     * that was set.
     * <p>
     * <b>Note:</b> Only a single line of text should be set when the SWT.SINGLE
     * style is used.
     * </p>
     *
     * @param text new widget content. Replaces existing content. Line styles
     * 	that were set using StyledText API are discarded.  The
     * 	current selection is also discarded.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT when string is null</li>
     * </ul>
     */
    public void setText(String text) {
        getDelegate().setText(text);
    }

    /**
     * Sets the base text direction (a.k.a. "paragraph direction") of the receiver,
     * which must be one of the constants <code>SWT.LEFT_TO_RIGHT</code> or
     * <code>SWT.RIGHT_TO_LEFT</code>.
     * <p>
     * <code>setOrientation</code> would override this value with the text direction
     * that is consistent with the new orientation.
     * </p>
     * <p>
     * <b>Warning</b>: This API is currently only implemented on Windows.
     * It doesn't set the base text direction on GTK and Cocoa.
     * </p>
     *
     * @param textDirection the base text direction style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SWT#FLIP_TEXT_DIRECTION
     */
    public void setTextDirection(int textDirection) {
        getDelegate().setTextDirection(textDirection);
    }

    /**
     * Sets the text limit to the specified number of characters.
     * <p>
     * The text limit specifies the amount of text that
     * the user can type into the widget.
     * </p>
     *
     * @param limit the new text limit.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *   <li>ERROR_CANNOT_BE_ZERO when limit is 0</li>
     * </ul>
     */
    public void setTextLimit(int limit) {
        getDelegate().setTextLimit(limit);
    }

    /**
     * Sets the top index. Do nothing if there is no text set.
     * <p>
     * The top index is the index of the line that is currently at the top
     * of the widget. The top index changes when the widget is scrolled.
     * Indexing starts from zero.
     * Note: The top index is reset to 0 when new text is set in the widget.
     * </p>
     *
     * @param topIndex new top index. Must be between 0 and
     * 	getLineCount() - fully visible lines per page. If no lines are fully
     * 	visible the maximum value is getLineCount() - 1. An out of range
     * 	index will be adjusted accordingly.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setTopIndex(int topIndex) {
        getDelegate().setTopIndex(topIndex);
    }

    /**
     * Sets the top margin.
     *
     * @param topMargin the top margin.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @since 3.5
     */
    public void setTopMargin(int topMargin) {
        getDelegate().setTopMargin(topMargin);
    }

    /**
     * Sets the top SWT logical point offset. Do nothing if there is no text set.
     * <p>
     * The top point offset is the vertical SWT logical point offset of the widget. The
     * widget is scrolled so that the given SWT logical point position is at the top.
     * The top index is adjusted to the corresponding top line.
     * Note: The top point is reset to 0 when new text is set in the widget.
     * </p>
     *
     * @param pixel new top point offset. Must be between 0 and
     * 	(getLineCount() - visible lines per page) / getLineHeight()). An out
     * 	of range offset will be adjusted accordingly.
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     * @since 2.0
     */
    public void setTopPixel(int pixel) {
        getDelegate().setTopPixel(pixel);
    }

    /**
     * Sets whether the widget wraps lines.
     * <p>
     * This overrides the creation style bit SWT.WRAP.
     * </p>
     *
     * @param wrap true=widget wraps lines, false=widget does not wrap lines
     * @since 2.0
     */
    public void setWordWrap(boolean wrap) {
        getDelegate().setWordWrap(wrap);
    }

    /**
     * Sets the wrap line indentation of the widget.
     * <p>
     * It is the amount of blank space, in points, at the beginning of each wrapped line.
     * When a line wraps in several lines all the lines but the first one is indented
     * by this amount.
     * </p>
     *
     * @param wrapIndent the new wrap indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see #setLineWrapIndent(int, int, int)
     *
     * @since 3.6
     */
    public void setWrapIndent(int wrapIndent) {
        getDelegate().setWrapIndent(wrapIndent);
    }

    /**
     * Scrolls the selection into view.
     * <p>
     * The end of the selection will be scrolled into view.
     * Note that if a right-to-left selection exists, the end of the selection is
     * the visual beginning of the selection (i.e., where the caret is located).
     * </p>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void showSelection() {
        getDelegate().showSelection();
    }

    /**
     * The method accepts a StyledText and a callback which takes
     * all the carets of the StyledText as the argument and executes it.
     * The caret is refreshed after the execution of the callback.
     *
     * @param styledText the StyledText to get the carets from
     * @param caretUpdater the callback which works with the carets
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    public static void updateAndRefreshCarets(StyledText styledText, Consumer<Caret> caretUpdater) {
        nat.org.eclipse.swt.custom.StyledText.updateAndRefreshCarets((nat.org.eclipse.swt.custom.StyledText) styledText.getDelegate(), arg -> caretUpdater.accept(arg.getApi()));
    }

    protected StyledText(IStyledText delegate) {
        super(delegate);
    }

    public static StyledText createApi(IStyledText delegate) {
        return new StyledText(delegate);
    }

    public IStyledText getDelegate() {
        return (IStyledText) super.getDelegate();
    }
}
