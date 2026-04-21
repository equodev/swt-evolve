/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

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
public final class DartTextLayout extends DartResource implements ITextLayout {

    Font font;

    String text;

    StyleItem[] styles;

    int stylesCount;

    int spacing, ascent = 11, descent = 4, indent, wrapIndent, verticalIndentInPoints;

    boolean justify;

    int alignment;

    int[] tabs;

    int[] segments;

    char[] segmentsChars;

    int wrapWidth;

    int orientation;

    private double defaultTabWidth;

    private FontMetrics fixedLineMetrics;

    private double fixedLineMetricsDy;

    int[] lineOffsets;

    static final byte[] SWT_OBJECT = { 'S', 'W', 'T', '_', 'O', 'B', 'J', 'E', 'C', 'T', '\0' };

    static final int TAB_COUNT = 32;

    static final int UNDERLINE_THICK = 1 << 16;

    int[] invalidOffsets;

    private boolean ignoreSegments;

    static final char LTR_MARK = '\u200E', RTL_MARK = '\u200F';

    static class StyleItem {

        TextStyle style;

        int start;

        long jniRef;

        @Override
        public String toString() {
            return "StyleItem {" + start + ", " + style + "}";
        }
    }

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
    public DartTextLayout(Device device, TextLayout api) {
        super(device, api);
        wrapWidth = ascent = descent = -1;
        alignment = SWT.LEFT;
        orientation = SWT.LEFT_TO_RIGHT;
        text = "";
        styles = new StyleItem[2];
        styles[0] = new StyleItem();
        styles[1] = new StyleItem();
        stylesCount = 2;
        init();
    }

    void checkLayout() {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
    }

    float[] computePolyline(int left, int top, int right, int bottom) {
        return null;
    }

    @Override
    void destroy() {
        freeRuns();
        font = null;
        text = null;
        styles = null;
        segments = null;
        segmentsChars = null;
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
        draw(gc, x, y, -1, -1, null, null);
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
        draw(gc, x, y, selectionStart, selectionEnd, selectionForeground, selectionBackground, 0);
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
        checkLayout();
        if (gc == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (gc.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (selectionForeground != null && selectionForeground.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (selectionBackground != null && selectionBackground.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        try {
            int length = translateOffset(text.length());
            if (length == 0 && flags == 0)
                return;
            y += getVerticalIndent();
            boolean hasSelection = selectionStart <= selectionEnd && selectionStart != -1 && selectionEnd != -1;
            if (hasSelection || ((flags & SWT.LAST_LINE_SELECTION) != 0 && (flags & (SWT.FULL_SELECTION | SWT.DELIMITER_SELECTION)) != 0)) {
                if (selectionBackground == null)
                    selectionBackground = device.getSystemColor(SWT.COLOR_LIST_SELECTION);
                if (hasSelection) {
                }
                //TODO draw full selection for wrapped text and delimiter selection for hard breaks
                if ((flags & (SWT.FULL_SELECTION | SWT.DELIMITER_SELECTION)) != 0 && (/*hasSelection ||*/
                (flags & SWT.LAST_LINE_SELECTION) != 0)) {
                }
            }
        } finally {
        }
    }

    void freeRuns() {
        lineOffsets = null;
        for (int i = 0; i < stylesCount - 1; i++) {
        }
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
        checkLayout();
        return alignment;
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
        checkLayout();
        return ascent;
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
        return getBounds(0, text.length());
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
        checkLayout();
        try {
            int length = text.length();
            if (length == 0)
                return new Rectangle(0, 0, 0, 0);
            if (start > end)
                return new Rectangle(0, 0, 0, 0);
            start = Math.min(Math.max(0, start), length - 1);
            end = Math.min(Math.max(0, end), length - 1);
            start = translateOffset(start);
            end = translateOffset(end);
            int left = 0x7FFFFFFF, right = 0;
            int top = 0x7FFFFFFF, bottom = 0;
            if (fixedLineMetrics != null)
                bottom = top + ((DartFontMetrics) fixedLineMetrics.getImpl()).height;
            return new Rectangle(left, top, right - left, bottom - top + getVerticalIndent());
        } finally {
        }
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
        checkLayout();
        return descent;
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
        checkLayout();
        return font;
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
        checkLayout();
        return indent;
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
        checkLayout();
        return justify;
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
        checkLayout();
        try {
            int length = text.length();
            if (!(0 <= offset && offset <= length))
                SWT.error(SWT.ERROR_INVALID_RANGE);
            offset = translateOffset(offset);
            byte[] bidiLevels = new byte[1];
            return bidiLevels[0];
        } finally {
        }
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
        checkLayout();
        try {
            int[] offsets = new int[lineOffsets.length];
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] = untranslateOffset(lineOffsets[i]);
            }
            return offsets;
        } finally {
        }
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
        checkLayout();
        try {
            int length = text.length();
            if (!(0 <= offset && offset <= length))
                SWT.error(SWT.ERROR_INVALID_RANGE);
            offset = translateOffset(offset);
            for (int line = 0; line < lineOffsets.length - 1; line++) {
                if (lineOffsets[line + 1] > offset) {
                    return line;
                }
            }
        } finally {
        }
        return 0;
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
        checkLayout();
        try {
        } finally {
        }
        return null;
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
        checkLayout();
        try {
            return lineOffsets.length - 1;
        } finally {
        }
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
        checkLayout();
        try {
            int lineCount = getLineCount();
            if (!(0 <= lineIndex && lineIndex < lineCount))
                SWT.error(SWT.ERROR_INVALID_RANGE);
            if (fixedLineMetrics != null)
                return ((DartFontMetrics) fixedLineMetrics.getImpl()).makeCopy();
            int length = text.length();
            if (length == 0) {
            }
        } finally {
        }
        return null;
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
        checkLayout();
        try {
            int length = text.length();
            if (!(0 <= offset && offset <= length))
                SWT.error(SWT.ERROR_INVALID_RANGE);
            if (length == 0)
                return new Point(0, 0);
            if (offset == length) {
            } else {
                offset = translateOffset(offset);
                boolean rtl = false;
                if (trailing != rtl) {
                    long[] rectCount = new long[1];
                    if (rectCount[0] > 0) {
                    }
                }
            }
        } finally {
        }
        return new Point(0, 0);
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
        try {
            return _getOffset(offset, movement, true);
        } finally {
        }
    }

    int _getOffset(int offset, int movement, boolean forward) {
        checkLayout();
        int length = text.length();
        if (!(0 <= offset && offset <= length))
            SWT.error(SWT.ERROR_INVALID_RANGE);
        if (forward && offset == length)
            return length;
        if (!forward && offset == 0)
            return 0;
        int step = forward ? 1 : -1;
        if ((movement & SWT.MOVEMENT_CHAR) != 0)
            return offset + step;
        switch(movement) {
            case SWT.MOVEMENT_CLUSTER:
                //TODO cluster
                offset += step;
                if (0 <= offset && offset < length) {
                    char ch = text.charAt(offset);
                    if (0xDC00 <= ch && ch <= 0xDFFF) {
                        if (offset > 0) {
                            ch = text.charAt(offset - 1);
                            if (0xD800 <= ch && ch <= 0xDBFF) {
                                offset += step;
                            }
                        }
                    }
                }
                break;
            case SWT.MOVEMENT_WORD:
                {
                    offset = translateOffset(offset);
                    return untranslateOffset(offset);
                }
            case SWT.MOVEMENT_WORD_END:
                {
                    offset = translateOffset(offset);
                    if (forward) {
                    } else {
                        length = translateOffset(length);
                        int result = 0;
                        while (result < length) {
                        }
                    }
                    return untranslateOffset(offset);
                }
            case SWT.MOVEMENT_WORD_START:
                {
                    offset = translateOffset(offset);
                    if (forward) {
                        int result = translateOffset(length);
                        while (result > 0) {
                        }
                    } else {
                    }
                    return untranslateOffset(offset);
                }
        }
        return offset;
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
        checkLayout();
        if (point == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return getOffset(point.x, point.y, trailing);
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
        checkLayout();
        try {
            if (trailing != null && trailing.length < 1)
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            int length = text.length();
            if (length == 0)
                return 0;
            double[] partialFraction = new double[1];
            if (trailing != null) {
                trailing[0] = Math.round((float) partialFraction[0]);
                if (partialFraction[0] >= 0.5) {
                }
            }
        } finally {
        }
        return 0;
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
        checkLayout();
        return orientation;
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
        try {
            return _getOffset(offset, movement, false);
        } finally {
        }
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
        checkLayout();
        int[] result = new int[stylesCount * 2];
        int count = 0;
        for (int i = 0; i < stylesCount - 1; i++) {
            if (styles[i].style != null) {
                result[count++] = styles[i].start;
                result[count++] = styles[i + 1].start - 1;
            }
        }
        if (count != result.length) {
            int[] newResult = new int[count];
            System.arraycopy(result, 0, newResult, 0, count);
            result = newResult;
        }
        return result;
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
        checkLayout();
        return segments;
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
        checkLayout();
        return segmentsChars;
    }

    String getSegmentsText() {
        int length = text.length();
        if (length == 0)
            return text;
        if (segments == null)
            return text;
        int nSegments = segments.length;
        if (nSegments == 0)
            return text;
        if (segmentsChars == null) {
            if (nSegments == 1)
                return text;
            if (nSegments == 2) {
                if (segments[0] == 0 && segments[1] == length)
                    return text;
            }
        }
        char[] oldChars = new char[length];
        text.getChars(0, length, oldChars, 0);
        char[] newChars = new char[length + nSegments];
        int charCount = 0, segmentCount = 0;
        char defaultSeparator = orientation == SWT.RIGHT_TO_LEFT ? RTL_MARK : LTR_MARK;
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
     * Returns the line spacing of the receiver.
     *
     * @return the line spacing
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int getSpacing() {
        checkLayout();
        return spacing;
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
        checkLayout();
        return verticalIndentInPoints;
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
        checkLayout();
        int length = text.length();
        if (!(0 <= offset && offset < length))
            SWT.error(SWT.ERROR_INVALID_RANGE);
        for (int i = 1; i < stylesCount; i++) {
            StyleItem item = styles[i];
            if (item.start > offset) {
                return styles[i - 1].style;
            }
        }
        return null;
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
        checkLayout();
        TextStyle[] result = new TextStyle[stylesCount];
        int count = 0;
        for (int i = 0; i < stylesCount; i++) {
            if (styles[i].style != null) {
                result[count++] = styles[i].style;
            }
        }
        if (count != result.length) {
            TextStyle[] newResult = new TextStyle[count];
            System.arraycopy(result, 0, newResult, 0, count);
            result = newResult;
        }
        return result;
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
        checkLayout();
        return tabs;
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
        checkLayout();
        return text;
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
        checkLayout();
        return orientation;
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
        checkLayout();
        return wrapWidth;
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
        checkLayout();
        return wrapIndent;
    }

    void initClasses() {
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
    @Override
    public boolean isDisposed() {
        return device == null;
    }

    /*
 * Returns true if the underline style is supported natively
 */
    boolean isUnderlineSupported(TextStyle style) {
        if (style != null && style.underline) {
            int uStyle = style.underlineStyle;
            return uStyle == SWT.UNDERLINE_SINGLE || uStyle == SWT.UNDERLINE_DOUBLE || uStyle == SWT.UNDERLINE_LINK || uStyle == UNDERLINE_THICK;
        }
        return false;
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
        checkLayout();
        int mask = SWT.LEFT | SWT.CENTER | SWT.RIGHT;
        alignment &= mask;
        if (alignment == 0)
            return;
        if ((alignment & SWT.LEFT) != 0)
            alignment = SWT.LEFT;
        if ((alignment & SWT.RIGHT) != 0)
            alignment = SWT.RIGHT;
        if (this.alignment == alignment)
            return;
        try {
            freeRuns();
            this.alignment = alignment;
        } finally {
        }
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
        checkLayout();
        if (ascent < -1)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (this.ascent == ascent)
            return;
        try {
            freeRuns();
            this.ascent = ascent;
        } finally {
        }
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
        checkLayout();
        if (descent < -1)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (this.descent == descent)
            return;
        try {
            freeRuns();
            this.descent = descent;
        } finally {
        }
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
        if (metrics == null) {
            fixedLineMetrics = null;
            return;
        }
        fixedLineMetrics = ((DartFontMetrics) metrics.getImpl()).makeCopy();
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
        checkLayout();
        if (font != null && font.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        Font oldFont = this.font;
        if (oldFont == font)
            return;
        this.font = font;
        if (oldFont != null && oldFont.equals(font))
            return;
        try {
            freeRuns();
        } finally {
        }
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
        checkLayout();
        if (indent < 0)
            return;
        if (this.indent == indent)
            return;
        try {
            freeRuns();
            this.indent = indent;
        } finally {
        }
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
        checkLayout();
        if (wrapIndent < 0)
            return;
        if (this.wrapIndent == wrapIndent)
            return;
        try {
            freeRuns();
            this.wrapIndent = wrapIndent;
        } finally {
        }
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
        checkLayout();
        if (justify == this.justify)
            return;
        try {
            freeRuns();
            this.justify = justify;
        } finally {
        }
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
        checkLayout();
        int mask = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
        orientation &= mask;
        if (orientation == 0)
            return;
        if ((orientation & SWT.LEFT_TO_RIGHT) != 0)
            orientation = SWT.LEFT_TO_RIGHT;
        if (this.orientation == orientation)
            return;
        this.orientation = orientation;
        try {
            freeRuns();
        } finally {
        }
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
        checkLayout();
        if (this.segments == null && segments == null)
            return;
        if (this.segments != null && segments != null) {
            if (this.segments.length == segments.length) {
                int i;
                for (i = 0; i < segments.length; i++) {
                    if (this.segments[i] != segments[i])
                        break;
                }
                if (i == segments.length)
                    return;
            }
        }
        try {
            freeRuns();
            this.segments = segments;
        } finally {
        }
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
        checkLayout();
        if (this.segmentsChars == null && segmentsChars == null)
            return;
        if (this.segmentsChars != null && segmentsChars != null) {
            if (this.segmentsChars.length == segmentsChars.length) {
                int i;
                for (i = 0; i < segmentsChars.length; i++) {
                    if (this.segmentsChars[i] != segmentsChars[i])
                        break;
                }
                if (i == segmentsChars.length)
                    return;
            }
        }
        try {
            freeRuns();
            this.segmentsChars = segmentsChars;
        } finally {
        }
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
        checkLayout();
        if (spacing < 0)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (this.spacing == spacing)
            return;
        try {
            freeRuns();
            this.spacing = spacing;
        } finally {
        }
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
        checkLayout();
        if (verticalIndent < 0)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (this.verticalIndentInPoints == verticalIndent)
            return;
        try {
            freeRuns();
            this.verticalIndentInPoints = verticalIndent;
        } finally {
        }
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
        checkLayout();
        try {
            int length = text.length();
            if (length == 0)
                return;
            if (start > end)
                return;
            start = Math.min(Math.max(0, start), length - 1);
            end = Math.min(Math.max(0, end), length - 1);
            int low = -1;
            int high = stylesCount;
            while (high - low > 1) {
                int index = (high + low) / 2;
                if (styles[index + 1].start > start) {
                    high = index;
                } else {
                    low = index;
                }
            }
            if (0 <= high && high < stylesCount) {
                StyleItem item = styles[high];
                if (item.start == start && styles[high + 1].start - 1 == end) {
                    if (style == null) {
                        if (item.style == null)
                            return;
                    } else {
                        if (style.equals(item.style))
                            return;
                    }
                }
            }
            freeRuns();
            int modifyStart = high;
            int modifyEnd = modifyStart;
            while (modifyEnd < stylesCount) {
                if (styles[modifyEnd + 1].start > end)
                    break;
                modifyEnd++;
            }
            if (modifyStart == modifyEnd) {
                int styleStart = styles[modifyStart].start;
                int styleEnd = styles[modifyEnd + 1].start - 1;
                if (styleStart == start && styleEnd == end) {
                    styles[modifyStart].style = style;
                    return;
                }
                if (styleStart != start && styleEnd != end) {
                    int newLength = stylesCount + 2;
                    if (newLength > styles.length) {
                        int newSize = Math.min(newLength + 1024, Math.max(64, newLength * 2));
                        StyleItem[] newStyles = new StyleItem[newSize];
                        System.arraycopy(styles, 0, newStyles, 0, stylesCount);
                        styles = newStyles;
                    }
                    System.arraycopy(styles, modifyEnd + 1, styles, modifyEnd + 3, stylesCount - modifyEnd - 1);
                    StyleItem item = new StyleItem();
                    item.start = start;
                    item.style = style;
                    styles[modifyStart + 1] = item;
                    item = new StyleItem();
                    item.start = end + 1;
                    item.style = styles[modifyStart].style;
                    styles[modifyStart + 2] = item;
                    stylesCount = newLength;
                    return;
                }
            }
            if (start == styles[modifyStart].start)
                modifyStart--;
            if (end == styles[modifyEnd + 1].start - 1)
                modifyEnd++;
            int newLength = stylesCount + 1 - (modifyEnd - modifyStart - 1);
            if (newLength > styles.length) {
                int newSize = Math.min(newLength + 1024, Math.max(64, newLength * 2));
                StyleItem[] newStyles = new StyleItem[newSize];
                System.arraycopy(styles, 0, newStyles, 0, stylesCount);
                styles = newStyles;
            }
            System.arraycopy(styles, modifyEnd, styles, modifyStart + 2, stylesCount - modifyEnd);
            StyleItem item = new StyleItem();
            item.start = start;
            item.style = style;
            styles[modifyStart + 1] = item;
            styles[modifyStart + 2].start = end + 1;
            stylesCount = newLength;
        } finally {
        }
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
        checkLayout();
        if (this.tabs == null && tabs == null)
            return;
        if (this.tabs != null && tabs != null) {
            if (this.tabs.length == tabs.length) {
                int i;
                for (i = 0; i < tabs.length; i++) {
                    if (this.tabs[i] != tabs[i])
                        break;
                }
                if (i == tabs.length)
                    return;
            }
        }
        try {
            freeRuns();
            this.tabs = tabs;
        } finally {
        }
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
        checkLayout();
        if (text == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (text.equals(this.text))
            return;
        try {
            freeRuns();
            this.text = text;
            styles = new StyleItem[2];
            styles[0] = new StyleItem();
            styles[1] = new StyleItem();
            styles[1].start = text.length();
            stylesCount = 2;
        } finally {
        }
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
        checkLayout();
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
        checkLayout();
        if (width < -1 || width == 0)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (this.wrapWidth == width)
            return;
        try {
            freeRuns();
            this.wrapWidth = width;
        } finally {
        }
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    @Override
    public String toString() {
        if (isDisposed())
            return "TextLayout {*DISPOSED*}";
        return "TextLayout {" + text + "}";
    }

    /*
 *  Translate a client offset to an internal offset
 */
    int translateOffset(int offset) {
        int length = text.length();
        if (length == 0)
            return offset;
        if (segments == null)
            return offset;
        int nSegments = segments.length;
        if (nSegments == 0)
            return offset;
        if (segmentsChars == null) {
            if (nSegments == 1)
                return offset;
            if (nSegments == 2) {
                if (segments[0] == 0 && segments[1] == length)
                    return offset;
            }
        }
        for (int i = 0; i < nSegments && offset - i >= segments[i]; i++) {
            offset++;
        }
        return offset;
    }

    /*
 *  Translate an internal offset to a client offset
 */
    int untranslateOffset(int offset) {
        int length = text.length();
        if (length == 0)
            return offset;
        if (segments == null)
            return offset;
        int nSegments = segments.length;
        if (nSegments == 0)
            return offset;
        if (segmentsChars == null) {
            if (nSegments == 1)
                return offset;
            if (nSegments == 2) {
                if (segments[0] == 0 && segments[1] == length)
                    return offset;
            }
        }
        for (int i = 0; i < nSegments && offset > segments[i]; i++) {
            offset--;
        }
        return offset;
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
        if (tabLength < 0)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        checkLayout();
        String oldString = getText();
        StringBuilder tabBuffer = new StringBuilder(tabLength);
        for (int i = 0; i < tabLength; i++) {
            tabBuffer.append(' ');
        }
        setText(tabBuffer.toString());
        ignoreSegments = true;
        this.defaultTabWidth = this.getTabWidth();
        ignoreSegments = false;
        setText(oldString);
    }

    double getTabWidth() {
        try {
        } finally {
        }
        return 0;
    }

    TextStyle[] _styles = new TextStyle[0];

    public Font _font() {
        return font;
    }

    public String _text() {
        return text;
    }

    public int _stylesCount() {
        return stylesCount;
    }

    public int _spacing() {
        return spacing;
    }

    public int _ascent() {
        return ascent;
    }

    public int _descent() {
        return descent;
    }

    public int _indent() {
        return indent;
    }

    public int _wrapIndent() {
        return wrapIndent;
    }

    public int _verticalIndentInPoints() {
        return verticalIndentInPoints;
    }

    public boolean _justify() {
        return justify;
    }

    public int _alignment() {
        return alignment;
    }

    public int[] _tabs() {
        return tabs;
    }

    public int[] _segments() {
        return segments;
    }

    public char[] _segmentsChars() {
        return segmentsChars;
    }

    public int _wrapWidth() {
        return wrapWidth;
    }

    public int _orientation() {
        return orientation;
    }

    public int[] _lineOffsets() {
        return lineOffsets;
    }

    public int[] _invalidOffsets() {
        return invalidOffsets;
    }

    public TextStyle[] __styles() {
        return _styles;
    }

    public TextLayout getApi() {
        if (api == null)
            api = TextLayout.createApi(this);
        return (TextLayout) api;
    }
}
