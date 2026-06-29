/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2022 IBM Corporation and others.
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
package org.eclipse.swt.graphics;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gdip.*;
import org.eclipse.swt.internal.ole.win32.*;
import org.eclipse.swt.internal.win32.*;

/**
 * <code>TextLayout</code> is a graphic object that represents
 * styled text.
 * <p>
 * Instances of this class provide support for drawing, cursor
 * navigation, hit testing, text wrapping, alignment, tab expansion
 * line breaking, etc.  These are aspects required for rendering internationalized text.
 * </p><p>
 * Application code must explicitly invoke the <code>TextLayout#dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#textlayout">TextLayout, TextStyle snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: CustomControlExample, StyledText tab</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.0
 */
public final class TextLayout extends Resource {

    /**
     * Constructs a new instance of this class on the given device.
     * <p>
     * You must dispose the text layout when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the text layout
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     * </ul>
     *
     * @see #dispose()
     */
    public TextLayout(Device device) {
        this((ITextLayout) null);
        setImpl(new SwtTextLayout(device, this));
    }

    /**
     * Draws the receiver's text using the specified GC at the specified
     * point.
     *
     * @param gc the GC to draw
     * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the gc is null</li>
     * </ul>
     */
    public void draw(GC gc, int x, int y) {
        getImpl().draw(gc, x, y);
    }

    /**
     * Draws the receiver's text using the specified GC at the specified
     * point.
     *
     * @param gc the GC to draw
     * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param selectionStart the offset where the selections starts, or -1 indicating no selection
     * @param selectionEnd the offset where the selections ends, or -1 indicating no selection
     * @param selectionForeground selection foreground, or NULL to use the system default color
     * @param selectionBackground selection background, or NULL to use the system default color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the gc is null</li>
     * </ul>
     */
    public void draw(GC gc, int x, int y, int selectionStart, int selectionEnd, Color selectionForeground, Color selectionBackground) {
        getImpl().draw(gc, x, y, selectionStart, selectionEnd, selectionForeground, selectionBackground);
    }

    /**
     * Draws the receiver's text using the specified GC at the specified
     * point.
     * <p>
     * The parameter <code>flags</code> can include one of <code>SWT.DELIMITER_SELECTION</code>
     * or <code>SWT.FULL_SELECTION</code> to specify the selection behavior on all lines except
     * for the last line, and can also include <code>SWT.LAST_LINE_SELECTION</code> to extend
     * the specified selection behavior to the last line.
     * </p>
     * @param gc the GC to draw
     * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param selectionStart the offset where the selections starts, or -1 indicating no selection
     * @param selectionEnd the offset where the selections ends, or -1 indicating no selection
     * @param selectionForeground selection foreground, or NULL to use the system default color
     * @param selectionBackground selection background, or NULL to use the system default color
     * @param flags drawing options
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the gc is null</li>
     * </ul>
     *
     * @since 3.3
     */
    public void draw(GC gc, int x, int y, int selectionStart, int selectionEnd, Color selectionForeground, Color selectionBackground, int flags) {
        getImpl().draw(gc, x, y, selectionStart, selectionEnd, selectionForeground, selectionBackground, flags);
    }

    /**
     * Returns the receiver's horizontal text alignment, which will be one
     * of <code>SWT.LEFT</code>, <code>SWT.CENTER</code> or
     * <code>SWT.RIGHT</code>.
     *
     * @return the alignment used to positioned text horizontally
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int getAlignment() {
        return getImpl().getAlignment();
    }

    /**
     * Returns the ascent of the receiver.
     *
     * @return the ascent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getDescent()
     * @see #setDescent(int)
     * @see #setAscent(int)
     * @see #getLineMetrics(int)
     */
    public int getAscent() {
        return getImpl().getAscent();
    }

    /**
     * Returns the bounds of the receiver. The width returned is either the
     * width of the longest line or the width set using {@link TextLayout#setWidth(int)}.
     * To obtain the text bounds of a line use {@link TextLayout#getLineBounds(int)}.
     *
     * @return the bounds of the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setWidth(int)
     * @see #getLineBounds(int)
     */
    public Rectangle getBounds() {
        return getImpl().getBounds();
    }

    /**
     * Returns the bounds for the specified range of characters. The
     * bounds is the smallest rectangle that encompasses all characters
     * in the range. The start and end offsets are inclusive and will be
     * clamped if out of range.
     *
     * @param start the start offset
     * @param end the end offset
     * @return the bounds of the character range
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public Rectangle getBounds(int start, int end) {
        return getImpl().getBounds(start, end);
    }

    /**
     * Returns the descent of the receiver.
     *
     * @return the descent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getAscent()
     * @see #setAscent(int)
     * @see #setDescent(int)
     * @see #getLineMetrics(int)
     */
    public int getDescent() {
        return getImpl().getDescent();
    }

    /**
     * Returns the default font currently being used by the receiver
     * to draw and measure text.
     *
     * @return the receiver's font
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public Font getFont() {
        return getImpl().getFont();
    }

    /**
     * Returns the receiver's indent.
     *
     * @return the receiver's indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.2
     */
    public int getIndent() {
        return getImpl().getIndent();
    }

    /**
     * Returns the receiver's justification.
     *
     * @return the receiver's justification
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.2
     */
    public boolean getJustify() {
        return getImpl().getJustify();
    }

    /**
     * Returns the embedding level for the specified character offset. The
     * embedding level is usually used to determine the directionality of a
     * character in bidirectional text.
     *
     * @param offset the character offset
     * @return the embedding level
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the character offset is out of range</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li></ul>
     */
    public int getLevel(int offset) {
        return getImpl().getLevel(offset);
    }

    /**
     * Returns the bounds of the line for the specified line index.
     *
     * @param lineIndex the line index
     * @return the line bounds
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the line index is out of range</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public Rectangle getLineBounds(int lineIndex) {
        return getImpl().getLineBounds(lineIndex);
    }

    /**
     * Returns the receiver's line count. This includes lines caused
     * by wrapping.
     *
     * @return the line count
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int getLineCount() {
        return getImpl().getLineCount();
    }

    /**
     * Returns the index of the line that contains the specified
     * character offset.
     *
     * @param offset the character offset
     * @return the line index
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the character offset is out of range</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int getLineIndex(int offset) {
        return getImpl().getLineIndex(offset);
    }

    /**
     * Returns the font metrics for the specified line index.
     *
     * @param lineIndex the line index
     * @return the font metrics
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the line index is out of range</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public FontMetrics getLineMetrics(int lineIndex) {
        return getImpl().getLineMetrics(lineIndex);
    }

    /**
     * Returns the line offsets.  Each value in the array is the
     * offset for the first character in a line except for the last
     * value, which contains the length of the text.
     *
     * @return the line offsets
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int[] getLineOffsets() {
        return getImpl().getLineOffsets();
    }

    /**
     * Returns the location for the specified character offset. The
     * <code>trailing</code> argument indicates whether the offset
     * corresponds to the leading or trailing edge of the cluster.
     *
     * @param offset the character offset
     * @param trailing the trailing flag
     * @return the location of the character offset
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getOffset(Point, int[])
     * @see #getOffset(int, int, int[])
     */
    public Point getLocation(int offset, boolean trailing) {
        return getImpl().getLocation(offset, trailing);
    }

    /**
     * Returns the next offset for the specified offset and movement
     * type.  The movement is one of <code>SWT.MOVEMENT_CHAR</code>,
     * <code>SWT.MOVEMENT_CLUSTER</code>, <code>SWT.MOVEMENT_WORD</code>,
     * <code>SWT.MOVEMENT_WORD_END</code> or <code>SWT.MOVEMENT_WORD_START</code>.
     *
     * @param offset the start offset
     * @param movement the movement type
     * @return the next offset
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the offset is out of range</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getPreviousOffset(int, int)
     */
    public int getNextOffset(int offset, int movement) {
        return getImpl().getNextOffset(offset, movement);
    }

    /**
     * Returns the character offset for the specified point.
     * For a typical character, the trailing argument will be filled in to
     * indicate whether the point is closer to the leading edge (0) or
     * the trailing edge (1).  When the point is over a cluster composed
     * of multiple characters, the trailing argument will be filled with the
     * position of the character in the cluster that is closest to
     * the point.
     *
     * @param point the point
     * @param trailing the trailing buffer
     * @return the character offset
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the trailing length is less than <code>1</code></li>
     *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getLocation(int, boolean)
     */
    public int getOffset(Point point, int[] trailing) {
        return getImpl().getOffset(point, trailing);
    }

    /**
     * Returns the character offset for the specified point.
     * For a typical character, the trailing argument will be filled in to
     * indicate whether the point is closer to the leading edge (0) or
     * the trailing edge (1).  When the point is over a cluster composed
     * of multiple characters, the trailing argument will be filled with the
     * position of the character in the cluster that is closest to
     * the point.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param trailing the trailing buffer
     * @return the character offset
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the trailing length is less than <code>1</code></li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getLocation(int, boolean)
     */
    public int getOffset(int x, int y, int[] trailing) {
        return getImpl().getOffset(x, y, trailing);
    }

    /**
     * Returns the orientation of the receiver.
     *
     * @return the orientation style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int getOrientation() {
        return getImpl().getOrientation();
    }

    /**
     * Returns the previous offset for the specified offset and movement
     * type.  The movement is one of <code>SWT.MOVEMENT_CHAR</code>,
     * <code>SWT.MOVEMENT_CLUSTER</code> or <code>SWT.MOVEMENT_WORD</code>,
     * <code>SWT.MOVEMENT_WORD_END</code> or <code>SWT.MOVEMENT_WORD_START</code>.
     *
     * @param offset the start offset
     * @param movement the movement type
     * @return the previous offset
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the offset is out of range</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getNextOffset(int, int)
     */
    public int getPreviousOffset(int offset, int movement) {
        return getImpl().getPreviousOffset(offset, movement);
    }

    /**
     * Gets the ranges of text that are associated with a <code>TextStyle</code>.
     *
     * @return the ranges, an array of offsets representing the start and end of each
     * text style.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getStyles()
     *
     * @since 3.2
     */
    public int[] getRanges() {
        return getImpl().getRanges();
    }

    /**
     * Returns the text segments offsets of the receiver.
     *
     * @return the text segments offsets
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int[] getSegments() {
        return getImpl().getSegments();
    }

    /**
     * Returns the segments characters of the receiver.
     *
     * @return the segments characters
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.6
     */
    public char[] getSegmentsChars() {
        return getImpl().getSegmentsChars();
    }

    /**
     * Returns the line spacing of the receiver.
     *
     * @return the line spacing
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int getSpacing() {
        return getImpl().getSpacing();
    }

    /**
     * Returns the vertical indent of the receiver.
     *
     * @return the vertical indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @since 3.109
     */
    public int getVerticalIndent() {
        return getImpl().getVerticalIndent();
    }

    /**
     * Gets the style of the receiver at the specified character offset.
     *
     * @param offset the text offset
     * @return the style or <code>null</code> if not set
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the character offset is out of range</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public TextStyle getStyle(int offset) {
        return getImpl().getStyle(offset);
    }

    /**
     * Gets all styles of the receiver.
     *
     * @return the styles
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getRanges()
     *
     * @since 3.2
     */
    public TextStyle[] getStyles() {
        return getImpl().getStyles();
    }

    /**
     * Returns the tab list of the receiver.
     *
     * @return the tab list
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int[] getTabs() {
        return getImpl().getTabs();
    }

    /**
     * Gets the receiver's text, which will be an empty
     * string if it has never been set.
     *
     * @return the receiver's text
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public String getText() {
        return getImpl().getText();
    }

    /**
     * Returns the text direction of the receiver.
     *
     * @return the text direction value
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @since 3.103
     */
    public int getTextDirection() {
        return getImpl().getTextDirection();
    }

    /**
     * Returns the width of the receiver.
     *
     * @return the width
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int getWidth() {
        return getImpl().getWidth();
    }

    /**
     * Returns the receiver's wrap indent.
     *
     * @return the receiver's wrap indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.6
     */
    public int getWrapIndent() {
        return getImpl().getWrapIndent();
    }

    /**
     * Returns <code>true</code> if the text layout has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the text layout.
     * When a text layout has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the text layout.
     * </p>
     *
     * @return <code>true</code> when the text layout is disposed and <code>false</code> otherwise
     */
    public boolean isDisposed() {
        return getImpl().isDisposed();
    }

    /**
     * Sets the text alignment for the receiver. The alignment controls
     * how a line of text is positioned horizontally. The argument should
     * be one of <code>SWT.LEFT</code>, <code>SWT.RIGHT</code> or <code>SWT.CENTER</code>.
     * <p>
     * The default alignment is <code>SWT.LEFT</code>.  Note that the receiver's
     * width must be set in order to use <code>SWT.RIGHT</code> or <code>SWT.CENTER</code>
     * alignment.
     * </p>
     *
     * @param alignment the new alignment
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setWidth(int)
     */
    public void setAlignment(int alignment) {
        getImpl().setAlignment(alignment);
    }

    /**
     * Sets the ascent of the receiver. The ascent is distance in points
     * from the baseline to the top of the line and it is applied to all
     * lines. The default value is <code>-1</code> which means that the
     * ascent is calculated from the line fonts.
     *
     * @param ascent the new ascent
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the ascent is less than <code>-1</code></li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setDescent(int)
     * @see #getLineMetrics(int)
     */
    public void setAscent(int ascent) {
        getImpl().setAscent(ascent);
    }

    /**
     * Sets the descent of the receiver. The descent is distance in points
     * from the baseline to the bottom of the line and it is applied to all
     * lines. The default value is <code>-1</code> which means that the
     * descent is calculated from the line fonts.
     *
     * @param descent the new descent
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the descent is less than <code>-1</code></li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setAscent(int)
     * @see #getLineMetrics(int)
     */
    public void setDescent(int descent) {
        getImpl().setDescent(descent);
    }

    /**
     * Forces line heights in receiver to obey provided value. This is
     * useful with texts that contain glyphs from different scripts,
     * such as mixing latin glyphs with hieroglyphs or emojis.
     * <p>
     * Text lines with different metrics will be forced to fit. This means
     * painting text in such a way that its baseline is where specified by
     * given 'metrics'. This can sometimes introduce small visual artifacs,
     * such as taller lines overpainting or being clipped by content above
     * and below.
     * </p>
     * The possible ways to set FontMetrics include:
     * <ul>
     * <li>Obtaining 'FontMetrics' via {@link GC#getFontMetrics}. Note that
     * this will only obtain metrics for currently selected font and will not
     * account for font fallbacks (for example, with a latin font selected,
     * painting hieroglyphs usually involves a fallback font).</li>
     * <li>Obtaining 'FontMetrics' via a temporary 'TextLayout'. This would
     * involve setting a desired text sample to 'TextLayout', then measuring
     * it with {@link TextLayout#getLineMetrics(int)}. This approach will also
     * take fallback fonts into account.</li>
     * </ul>
     *
     * NOTE: Does not currently support (as in, undefined behavior) multi-line
     * layouts, including those caused by word wrapping. StyledText uses one
     * TextLayout per line and is only affected by word wrap restriction.
     *
     * @since 3.125
     */
    public void setFixedLineMetrics(FontMetrics metrics) {
        getImpl().setFixedLineMetrics(metrics);
    }

    /**
     * Sets the default font which will be used by the receiver
     * to draw and measure text. If the
     * argument is null, then a default font appropriate
     * for the platform will be used instead. Note that a text
     * style can override the default font.
     *
     * @param font the new font for the receiver, or null to indicate a default font
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the font has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void setFont(Font font) {
        getImpl().setFont(font);
    }

    /**
     * Sets the indent of the receiver. This indent is applied to the first line of
     * each paragraph.
     *
     * @param indent new indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setWrapIndent(int)
     *
     * @since 3.2
     */
    public void setIndent(int indent) {
        getImpl().setIndent(indent);
    }

    /**
     * Sets the justification of the receiver. Note that the receiver's
     * width must be set in order to use justification.
     *
     * @param justify new justify
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.2
     */
    public void setJustify(boolean justify) {
        getImpl().setJustify(justify);
    }

    /**
     * Sets the orientation of the receiver, which must be one
     * of <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
     *
     * @param orientation new orientation style
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void setOrientation(int orientation) {
        getImpl().setOrientation(orientation);
    }

    /**
     * Sets the offsets of the receiver's text segments. Text segments are used to
     * override the default behavior of the bidirectional algorithm.
     * Bidirectional reordering can happen within a text segment but not
     * between two adjacent segments.
     * <p>
     * Each text segment is determined by two consecutive offsets in the
     * <code>segments</code> arrays. The first element of the array should
     * always be zero and the last one should always be equals to length of
     * the text.
     * </p>
     * <p>
     * When segments characters are set, the segments are the offsets where
     * the characters are inserted in the text.
     * </p>
     *
     * @param segments the text segments offset
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setSegmentsChars(char[])
     */
    public void setSegments(int[] segments) {
        getImpl().setSegments(segments);
    }

    /**
     * Sets the characters to be used in the segments boundaries. The segments
     * are set by calling <code>setSegments(int[])</code>. The application can
     * use this API to insert Unicode Control Characters in the text to control
     * the display of the text and bidi reordering. The characters are not
     * accessible by any other API in <code>TextLayout</code>.
     *
     * @param segmentsChars the segments characters
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setSegments(int[])
     *
     * @since 3.6
     */
    public void setSegmentsChars(char[] segmentsChars) {
        getImpl().setSegmentsChars(segmentsChars);
    }

    /**
     * Sets the line spacing of the receiver.  The line spacing
     * is the space left between lines.
     *
     * @param spacing the new line spacing
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the spacing is negative</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void setSpacing(int spacing) {
        getImpl().setSpacing(spacing);
    }

    /**
     * Sets the vertical indent of the receiver.  The vertical indent
     * is the space left before the first line.
     *
     * @param verticalIndent the new vertical indent
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the vertical indent is negative</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @since 3.109
     */
    public void setVerticalIndent(int verticalIndent) {
        getImpl().setVerticalIndent(verticalIndent);
    }

    /**
     * Sets the style of the receiver for the specified range.  Styles previously
     * set for that range will be overwritten.  The start and end offsets are
     * inclusive and will be clamped if out of range.
     *
     * @param style the style
     * @param start the start offset
     * @param end the end offset
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void setStyle(TextStyle style, int start, int end) {
        getImpl().setStyle(style, start, end);
    }

    /**
     * Sets the receiver's tab list. Each value in the tab list specifies
     * the space in points from the origin of the text layout to the respective
     * tab stop.  The last tab stop width is repeated continuously.
     *
     * @param tabs the new tab list
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void setTabs(int[] tabs) {
        getImpl().setTabs(tabs);
    }

    /**
     *  Sets the receiver's text.
     * <p>
     *  Note: Setting the text also clears all the styles. This method
     *  returns without doing anything if the new text is the same as
     *  the current text.
     *  </p>
     *
     *  @param text the new text
     *
     *  @exception IllegalArgumentException <ul>
     *     <li>ERROR_NULL_ARGUMENT - if the text is null</li>
     *  </ul>
     *  @exception SWTException <ul>
     *     <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *  </ul>
     */
    public void setText(String text) {
        getImpl().setText(text);
    }

    /**
     * Sets the text direction of the receiver, which must be one
     * of <code>SWT.LEFT_TO_RIGHT</code>, <code>SWT.RIGHT_TO_LEFT</code>
     * or <code>SWT.AUTO_TEXT_DIRECTION</code>.
     *
     * <p>
     * <b>Warning</b>: This API is currently only implemented on Windows.
     * It doesn't set the base text direction on GTK and Cocoa.
     * </p>
     *
     * @param textDirection the new text direction
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @since 3.103
     */
    public void setTextDirection(int textDirection) {
        getImpl().setTextDirection(textDirection);
    }

    /**
     * Sets the line width of the receiver, which determines how
     * text should be wrapped and aligned. The default value is
     * <code>-1</code> which means wrapping is disabled.
     *
     * @param width the new width
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the width is <code>0</code> or less than <code>-1</code></li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setAlignment(int)
     */
    public void setWidth(int width) {
        getImpl().setWidth(width);
    }

    /**
     * Sets the wrap indent of the receiver. This indent is applied to all lines
     * in the paragraph except the first line.
     *
     * @param wrapIndent new wrap indent
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setIndent(int)
     *
     * @since 3.6
     */
    public void setWrapIndent(int wrapIndent) {
        getImpl().setWrapIndent(wrapIndent);
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    public String toString() {
        return getImpl().toString();
    }

    /**
     * Sets Default Tab Width in terms if number of space characters.
     *
     * @param tabLength in number of characters
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the tabLength is less than <code>0</code></li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @noreference This method is not intended to be referenced by clients.
     *
     * DO NOT USE This might be removed in 4.8
     * @since 3.107
     */
    public void setDefaultTabWidth(int tabLength) {
        getImpl().setDefaultTabWidth(tabLength);
    }

    protected TextLayout(ITextLayout impl) {
        super(impl);
    }

    static TextLayout createApi(ITextLayout impl) {
        return new TextLayout(impl);
    }

    public ITextLayout getImpl() {
        return (ITextLayout) super.getImpl();
    }
}
