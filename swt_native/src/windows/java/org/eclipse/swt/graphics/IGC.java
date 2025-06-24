package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gdip.*;

public interface IGC extends IResource {

    /**
     * Copies a rectangular area of the receiver at the specified
     * position into the image, which must be of type <code>SWT.BITMAP</code>.
     *
     * @param image the image to copy into
     * @param x the x coordinate in the receiver of the area to be copied
     * @param y the y coordinate in the receiver of the area to be copied
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the image is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the image is not a bitmap or has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void copyArea(Image image, int x, int y);

    /**
     * Copies a rectangular area of the receiver at the source
     * position onto the receiver at the destination position.
     *
     * @param srcX the x coordinate in the receiver of the area to be copied
     * @param srcY the y coordinate in the receiver of the area to be copied
     * @param width the width of the area to copy
     * @param height the height of the area to copy
     * @param destX the x coordinate in the receiver of the area to copy to
     * @param destY the y coordinate in the receiver of the area to copy to
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void copyArea(int srcX, int srcY, int width, int height, int destX, int destY);

    /**
     * Copies a rectangular area of the receiver at the source
     * position onto the receiver at the destination position.
     *
     * @param srcX the x coordinate in the receiver of the area to be copied
     * @param srcY the y coordinate in the receiver of the area to be copied
     * @param width the width of the area to copy
     * @param height the height of the area to copy
     * @param destX the x coordinate in the receiver of the area to copy to
     * @param destY the y coordinate in the receiver of the area to copy to
     * @param paint if <code>true</code> paint events will be generated for old and obscured areas
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    void copyArea(int srcX, int srcY, int width, int height, int destX, int destY, boolean paint);

    /**
     * Draws the outline of a circular or elliptical arc
     * within the specified rectangular area.
     * <p>
     * The resulting arc begins at <code>startAngle</code> and extends
     * for <code>arcAngle</code> degrees, using the current color.
     * Angles are interpreted such that 0 degrees is at the 3 o'clock
     * position. A positive value indicates a counter-clockwise rotation
     * while a negative value indicates a clockwise rotation.
     * </p><p>
     * The center of the arc is the center of the rectangle whose origin
     * is (<code>x</code>, <code>y</code>) and whose size is specified by the
     * <code>width</code> and <code>height</code> arguments.
     * </p><p>
     * The resulting arc covers an area <code>width + 1</code> points wide
     * by <code>height + 1</code> points tall.
     * </p>
     *
     * @param x the x coordinate of the upper-left corner of the arc to be drawn
     * @param y the y coordinate of the upper-left corner of the arc to be drawn
     * @param width the width of the arc to be drawn
     * @param height the height of the arc to be drawn
     * @param startAngle the beginning angle
     * @param arcAngle the angular extent of the arc, relative to the start angle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle);

    /**
     * Draws a rectangle, based on the specified arguments, which has
     * the appearance of the platform's <em>focus rectangle</em> if the
     * platform supports such a notion, and otherwise draws a simple
     * rectangle in the receiver's foreground color.
     *
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #drawRectangle(int, int, int, int)
     */
    void drawFocus(int x, int y, int width, int height);

    /**
     * Draws the given image in the receiver at the specified
     * coordinates.
     *
     * @param image the image to draw
     * @param x the x coordinate of where to draw
     * @param y the y coordinate of where to draw
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the image is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the given coordinates are outside the bounds of the image</li></ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if no handles are available to perform the operation</li>
     * </ul>
     */
    void drawImage(Image image, int x, int y);

    /**
     * Copies a rectangular area from the source image into a (potentially
     * different sized) rectangular area in the receiver. If the source
     * and destination areas are of differing sizes, then the source
     * area will be stretched or shrunk to fit the destination area
     * as it is copied. The copy fails if any part of the source rectangle
     * lies outside the bounds of the source image, or if any of the width
     * or height arguments are negative.
     *
     * @param image the source image
     * @param srcX the x coordinate in the source image to copy from
     * @param srcY the y coordinate in the source image to copy from
     * @param srcWidth the width in points to copy from the source
     * @param srcHeight the height in points to copy from the source
     * @param destX the x coordinate in the destination to copy to
     * @param destY the y coordinate in the destination to copy to
     * @param destWidth the width in points of the destination rectangle
     * @param destHeight the height in points of the destination rectangle
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the image is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     *    <li>ERROR_INVALID_ARGUMENT - if any of the width or height arguments are negative.
     *    <li>ERROR_INVALID_ARGUMENT - if the source rectangle is not contained within the bounds of the source image</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if no handles are available to perform the operation</li>
     * </ul>
     */
    void drawImage(Image image, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight);

    /**
     * Draws a line, using the foreground color, between the points
     * (<code>x1</code>, <code>y1</code>) and (<code>x2</code>, <code>y2</code>).
     *
     * @param x1 the first point's x coordinate
     * @param y1 the first point's y coordinate
     * @param x2 the second point's x coordinate
     * @param y2 the second point's y coordinate
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawLine(int x1, int y1, int x2, int y2);

    /**
     * Draws the outline of an oval, using the foreground color,
     * within the specified rectangular area.
     * <p>
     * The result is a circle or ellipse that fits within the
     * rectangle specified by the <code>x</code>, <code>y</code>,
     * <code>width</code>, and <code>height</code> arguments.
     * </p><p>
     * The oval covers an area that is <code>width + 1</code>
     * points wide and <code>height + 1</code> points tall.
     * </p>
     *
     * @param x the x coordinate of the upper left corner of the oval to be drawn
     * @param y the y coordinate of the upper left corner of the oval to be drawn
     * @param width the width of the oval to be drawn
     * @param height the height of the oval to be drawn
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawOval(int x, int y, int width, int height);

    /**
     * Draws the path described by the parameter.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     *
     * @param path the path to draw
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     *
     * @see Path
     *
     * @since 3.1
     */
    void drawPath(Path path);

    /**
     * Draws an SWT logical point, using the foreground color, at the specified
     * point (<code>x</code>, <code>y</code>).
     * <p>
     * Note that the receiver's line attributes do not affect this
     * operation.
     * </p>
     *
     * @param x the point's x coordinate
     * @param y the point's y coordinate
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.0
     */
    void drawPoint(int x, int y);

    /**
     * Draws the closed polygon which is defined by the specified array
     * of integer coordinates, using the receiver's foreground color. The array
     * contains alternating x and y values which are considered to represent
     * points which are the vertices of the polygon. Lines are drawn between
     * each consecutive pair, and between the first pair and last pair in the
     * array.
     *
     * @param pointArray an array of alternating x and y values which are the vertices of the polygon
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT if pointArray is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawPolygon(int[] pointArray);

    /**
     * Draws the polyline which is defined by the specified array
     * of integer coordinates, using the receiver's foreground color. The array
     * contains alternating x and y values which are considered to represent
     * points which are the corners of the polyline. Lines are drawn between
     * each consecutive pair, but not between the first pair and last pair in
     * the array.
     *
     * @param pointArray an array of alternating x and y values which are the corners of the polyline
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the point array is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawPolyline(int[] pointArray);

    /**
     * Draws the outline of the rectangle specified by the arguments,
     * using the receiver's foreground color. The left and right edges
     * of the rectangle are at <code>x</code> and <code>x + width</code>.
     * The top and bottom edges are at <code>y</code> and <code>y + height</code>.
     *
     * @param x the x coordinate of the rectangle to be drawn
     * @param y the y coordinate of the rectangle to be drawn
     * @param width the width of the rectangle to be drawn
     * @param height the height of the rectangle to be drawn
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawRectangle(int x, int y, int width, int height);

    /**
     * Draws the outline of the specified rectangle, using the receiver's
     * foreground color. The left and right edges of the rectangle are at
     * <code>rect.x</code> and <code>rect.x + rect.width</code>. The top
     * and bottom edges are at <code>rect.y</code> and
     * <code>rect.y + rect.height</code>.
     *
     * @param rect the rectangle to draw
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the rectangle is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawRectangle(Rectangle rect);

    /**
     * Draws the outline of the round-cornered rectangle specified by
     * the arguments, using the receiver's foreground color. The left and
     * right edges of the rectangle are at <code>x</code> and <code>x + width</code>.
     * The top and bottom edges are at <code>y</code> and <code>y + height</code>.
     * The <em>roundness</em> of the corners is specified by the
     * <code>arcWidth</code> and <code>arcHeight</code> arguments, which
     * are respectively the width and height of the ellipse used to draw
     * the corners.
     *
     * @param x the x coordinate of the rectangle to be drawn
     * @param y the y coordinate of the rectangle to be drawn
     * @param width the width of the rectangle to be drawn
     * @param height the height of the rectangle to be drawn
     * @param arcWidth the width of the arc
     * @param arcHeight the height of the arc
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawRoundRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight);

    /**
     * Draws the given string, using the receiver's current font and
     * foreground color. No tab expansion or carriage return processing
     * will be performed. The background of the rectangular area where
     * the string is being drawn will be filled with the receiver's
     * background color.
     * <br><br>
     * On Windows, {@link #drawString} and {@link #drawText} are slightly
     * different, see {@link #drawString(String, int, int, boolean)} for
     * explanation.
     *
     * @param string the string to be drawn
     * @param x the x coordinate of the top left corner of the rectangular area where the string is to be drawn
     * @param y the y coordinate of the top left corner of the rectangular area where the string is to be drawn
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawString(String string, int x, int y);

    /**
     * Draws the given string, using the receiver's current font and
     * foreground color. No tab expansion or carriage return processing
     * will be performed. If <code>isTransparent</code> is <code>true</code>,
     * then the background of the rectangular area where the string is being
     * drawn will not be modified, otherwise it will be filled with the
     * receiver's background color.
     * <br><br>
     * On Windows, {@link #drawString} and {@link #drawText} are slightly
     * different:
     * <ul>
     *     <li>{@link #drawString} is faster (depends on string size)<br>~7x for 1-char strings<br>~4x for 10-char strings<br>~2x for 100-char strings</li>
     *     <li>{@link #drawString} doesn't try to find a good fallback font when character doesn't have a glyph in currently selected font</li>
     * </ul>
     *
     * @param string the string to be drawn
     * @param x the x coordinate of the top left corner of the rectangular area where the string is to be drawn
     * @param y the y coordinate of the top left corner of the rectangular area where the string is to be drawn
     * @param isTransparent if <code>true</code> the background will be transparent, otherwise it will be opaque
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawString(String string, int x, int y, boolean isTransparent);

    /**
     * Draws the given string, using the receiver's current font and
     * foreground color. Tab expansion and carriage return processing
     * are performed. The background of the rectangular area where
     * the text is being drawn will be filled with the receiver's
     * background color.
     * <br><br>
     * On Windows, {@link #drawString} and {@link #drawText} are slightly
     * different, see {@link #drawString(String, int, int, boolean)} for
     * explanation.
     *
     * @param string the string to be drawn
     * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawText(String string, int x, int y);

    /**
     * Draws the given string, using the receiver's current font and
     * foreground color. Tab expansion and carriage return processing
     * are performed. If <code>isTransparent</code> is <code>true</code>,
     * then the background of the rectangular area where the text is being
     * drawn will not be modified, otherwise it will be filled with the
     * receiver's background color.
     * <br><br>
     * On Windows, {@link #drawString} and {@link #drawText} are slightly
     * different, see {@link #drawString(String, int, int, boolean)} for
     * explanation.
     *
     * @param string the string to be drawn
     * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param isTransparent if <code>true</code> the background will be transparent, otherwise it will be opaque
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawText(String string, int x, int y, boolean isTransparent);

    /**
     * Draws the given string, using the receiver's current font and
     * foreground color. Tab expansion, line delimiter and mnemonic
     * processing are performed according to the specified flags. If
     * <code>flags</code> includes <code>DRAW_TRANSPARENT</code>,
     * then the background of the rectangular area where the text is being
     * drawn will not be modified, otherwise it will be filled with the
     * receiver's background color.
     * <br><br>
     * On Windows, {@link #drawString} and {@link #drawText} are slightly
     * different, see {@link #drawString(String, int, int, boolean)} for
     * explanation.
     *
     * <p>
     * The parameter <code>flags</code> may be a combination of:
     * </p>
     * <dl>
     * <dt><b>DRAW_DELIMITER</b></dt>
     * <dd>draw multiple lines</dd>
     * <dt><b>DRAW_TAB</b></dt>
     * <dd>expand tabs</dd>
     * <dt><b>DRAW_MNEMONIC</b></dt>
     * <dd>underline the mnemonic character</dd>
     * <dt><b>DRAW_TRANSPARENT</b></dt>
     * <dd>transparent background</dd>
     * </dl>
     *
     * @param string the string to be drawn
     * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param flags the flags specifying how to process the text
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void drawText(String string, int x, int y, int flags);

    /**
     * Compares the argument to the receiver, and returns true
     * if they represent the <em>same</em> object using a class
     * specific comparison.
     *
     * @param object the object to compare with this object
     * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
     *
     * @see #hashCode
     */
    boolean equals(Object object);

    /**
     * Fills the interior of a circular or elliptical arc within
     * the specified rectangular area, with the receiver's background
     * color.
     * <p>
     * The resulting arc begins at <code>startAngle</code> and extends
     * for <code>arcAngle</code> degrees, using the current color.
     * Angles are interpreted such that 0 degrees is at the 3 o'clock
     * position. A positive value indicates a counter-clockwise rotation
     * while a negative value indicates a clockwise rotation.
     * </p><p>
     * The center of the arc is the center of the rectangle whose origin
     * is (<code>x</code>, <code>y</code>) and whose size is specified by the
     * <code>width</code> and <code>height</code> arguments.
     * </p><p>
     * The resulting arc covers an area <code>width + 1</code> points wide
     * by <code>height + 1</code> points tall.
     * </p>
     *
     * @param x the x coordinate of the upper-left corner of the arc to be filled
     * @param y the y coordinate of the upper-left corner of the arc to be filled
     * @param width the width of the arc to be filled
     * @param height the height of the arc to be filled
     * @param startAngle the beginning angle
     * @param arcAngle the angular extent of the arc, relative to the start angle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #drawArc
     */
    void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle);

    /**
     * Fills the interior of the specified rectangle with a gradient
     * sweeping from left to right or top to bottom progressing
     * from the receiver's foreground color to its background color.
     *
     * @param x the x coordinate of the rectangle to be filled
     * @param y the y coordinate of the rectangle to be filled
     * @param width the width of the rectangle to be filled, may be negative
     *        (inverts direction of gradient if horizontal)
     * @param height the height of the rectangle to be filled, may be negative
     *        (inverts direction of gradient if vertical)
     * @param vertical if true sweeps from top to bottom, else
     *        sweeps from left to right
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #drawRectangle(int, int, int, int)
     */
    void fillGradientRectangle(int x, int y, int width, int height, boolean vertical);

    /**
     * Fills the interior of an oval, within the specified
     * rectangular area, with the receiver's background
     * color.
     *
     * @param x the x coordinate of the upper left corner of the oval to be filled
     * @param y the y coordinate of the upper left corner of the oval to be filled
     * @param width the width of the oval to be filled
     * @param height the height of the oval to be filled
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #drawOval
     */
    void fillOval(int x, int y, int width, int height);

    /**
     * Fills the path described by the parameter.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     *
     * @param path the path to fill
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     *
     * @see Path
     *
     * @since 3.1
     */
    void fillPath(Path path);

    /**
     * Fills the interior of the closed polygon which is defined by the
     * specified array of integer coordinates, using the receiver's
     * background color. The array contains alternating x and y values
     * which are considered to represent points which are the vertices of
     * the polygon. Lines are drawn between each consecutive pair, and
     * between the first pair and last pair in the array.
     *
     * @param pointArray an array of alternating x and y values which are the vertices of the polygon
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT if pointArray is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #drawPolygon
     */
    void fillPolygon(int[] pointArray);

    /**
     * Fills the interior of the rectangle specified by the arguments,
     * using the receiver's background color.
     *
     * @param x the x coordinate of the rectangle to be filled
     * @param y the y coordinate of the rectangle to be filled
     * @param width the width of the rectangle to be filled
     * @param height the height of the rectangle to be filled
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #drawRectangle(int, int, int, int)
     */
    void fillRectangle(int x, int y, int width, int height);

    /**
     * Fills the interior of the specified rectangle, using the receiver's
     * background color.
     *
     * @param rect the rectangle to be filled
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the rectangle is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #drawRectangle(int, int, int, int)
     */
    void fillRectangle(Rectangle rect);

    /**
     * Fills the interior of the round-cornered rectangle specified by
     * the arguments, using the receiver's background color.
     *
     * @param x the x coordinate of the rectangle to be filled
     * @param y the y coordinate of the rectangle to be filled
     * @param width the width of the rectangle to be filled
     * @param height the height of the rectangle to be filled
     * @param arcWidth the width of the arc
     * @param arcHeight the height of the arc
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #drawRoundRectangle
     */
    void fillRoundRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight);

    /**
     * Returns the <em>advance width</em> of the specified character in
     * the font which is currently selected into the receiver.
     * <p>
     * The advance width is defined as the horizontal distance the cursor
     * should move after printing the character in the selected font.
     * </p>
     *
     * @param ch the character to measure
     * @return the distance in the x direction to move past the character before painting the next
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    int getAdvanceWidth(char ch);

    /**
     * Returns <code>true</code> if receiver is using the operating system's
     * advanced graphics subsystem.  Otherwise, <code>false</code> is returned
     * to indicate that normal graphics are in use.
     * <p>
     * Advanced graphics may not be installed for the operating system.  In this
     * case, <code>false</code> is always returned.  Some operating system have
     * only one graphics subsystem.  If this subsystem supports advanced graphics,
     * then <code>true</code> is always returned.  If any graphics operation such
     * as alpha, antialias, patterns, interpolation, paths, clipping or transformation
     * has caused the receiver to switch from regular to advanced graphics mode,
     * <code>true</code> is returned.  If the receiver has been explicitly switched
     * to advanced mode and this mode is supported, <code>true</code> is returned.
     * </p>
     *
     * @return the advanced value
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setAdvanced
     *
     * @since 3.1
     */
    boolean getAdvanced();

    /**
     * Returns the receiver's alpha value. The alpha value
     * is between 0 (transparent) and 255 (opaque).
     *
     * @return the alpha value
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    int getAlpha();

    /**
     * Returns the receiver's anti-aliasing setting value, which will be
     * one of <code>SWT.DEFAULT</code>, <code>SWT.OFF</code> or
     * <code>SWT.ON</code>. Note that this controls anti-aliasing for all
     * <em>non-text drawing</em> operations.
     *
     * @return the anti-aliasing setting
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getTextAntialias
     *
     * @since 3.1
     */
    int getAntialias();

    /**
     * Returns the background color.
     *
     * @return the receiver's background color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Color getBackground();

    /**
     * Returns the background pattern. The default value is
     * <code>null</code>.
     *
     * @return the receiver's background pattern
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Pattern
     *
     * @since 3.1
     */
    Pattern getBackgroundPattern();

    /**
     * Returns the width of the specified character in the font
     * selected into the receiver.
     * <p>
     * The width is defined as the space taken up by the actual
     * character, not including the leading and tailing whitespace
     * or overhang.
     * </p>
     *
     * @param ch the character to measure
     * @return the width of the character
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    int getCharWidth(char ch);

    /**
     * Returns the bounding rectangle of the receiver's clipping
     * region. If no clipping region is set, the return value
     * will be a rectangle which covers the entire bounds of the
     * object the receiver is drawing on.
     *
     * @return the bounding rectangle of the clipping region
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Rectangle getClipping();

    /**
     * Sets the region managed by the argument to the current
     * clipping region of the receiver.
     *
     * @param region the region to fill with the clipping region
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the region is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the region is disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void getClipping(Region region);

    /**
     * Returns the receiver's fill rule, which will be one of
     * <code>SWT.FILL_EVEN_ODD</code> or <code>SWT.FILL_WINDING</code>.
     *
     * @return the receiver's fill rule
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    int getFillRule();

    /**
     * Returns the font currently being used by the receiver
     * to draw and measure text.
     *
     * @return the receiver's font
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Font getFont();

    /**
     * Returns a FontMetrics which contains information
     * about the font currently being used by the receiver
     * to draw and measure text.
     *
     * @return font metrics for the receiver's font
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    FontMetrics getFontMetrics();

    /**
     * Returns the receiver's foreground color.
     *
     * @return the color used for drawing foreground things
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Color getForeground();

    /**
     * Returns the foreground pattern. The default value is
     * <code>null</code>.
     *
     * @return the receiver's foreground pattern
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Pattern
     *
     * @since 3.1
     */
    Pattern getForegroundPattern();

    /**
     * Returns the GCData.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>GC</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @return the receiver's GCData
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see GCData
     *
     * @noreference This method is not intended to be referenced by clients.
     *
     * @since 3.2
     */
    GCData getGCData();

    /**
     * Returns the receiver's interpolation setting, which will be one of
     * <code>SWT.DEFAULT</code>, <code>SWT.NONE</code>,
     * <code>SWT.LOW</code> or <code>SWT.HIGH</code>.
     *
     * @return the receiver's interpolation setting
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    int getInterpolation();

    /**
     * Returns the receiver's line attributes.
     *
     * @return the line attributes used for drawing lines
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.3
     */
    LineAttributes getLineAttributes();

    /**
     * Returns the receiver's line cap style, which will be one
     * of the constants <code>SWT.CAP_FLAT</code>, <code>SWT.CAP_ROUND</code>,
     * or <code>SWT.CAP_SQUARE</code>.
     *
     * @return the cap style used for drawing lines
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    int getLineCap();

    /**
     * Returns the receiver's line dash style. The default value is
     * <code>null</code>.
     *
     * @return the line dash style used for drawing lines
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    int[] getLineDash();

    /**
     * Returns the receiver's line join style, which will be one
     * of the constants <code>SWT.JOIN_MITER</code>, <code>SWT.JOIN_ROUND</code>,
     * or <code>SWT.JOIN_BEVEL</code>.
     *
     * @return the join style used for drawing lines
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    int getLineJoin();

    /**
     * Returns the receiver's line style, which will be one
     * of the constants <code>SWT.LINE_SOLID</code>, <code>SWT.LINE_DASH</code>,
     * <code>SWT.LINE_DOT</code>, <code>SWT.LINE_DASHDOT</code> or
     * <code>SWT.LINE_DASHDOTDOT</code>.
     *
     * @return the style used for drawing lines
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    int getLineStyle();

    /**
     * Returns the width that will be used when drawing lines
     * for all of the figure drawing operations (that is,
     * <code>drawLine</code>, <code>drawRectangle</code>,
     * <code>drawPolyline</code>, and so forth.
     *
     * @return the receiver's line width
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    int getLineWidth();

    /**
     * Returns the receiver's style information.
     * <p>
     * Note that the value which is returned by this method <em>may
     * not match</em> the value which was provided to the constructor
     * when the receiver was created. This can occur when the underlying
     * operating system does not support a particular combination of
     * requested styles.
     * </p>
     *
     * @return the style bits
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 2.1.2
     */
    int getStyle();

    /**
     * Returns the receiver's text drawing anti-aliasing setting value,
     * which will be one of <code>SWT.DEFAULT</code>, <code>SWT.OFF</code> or
     * <code>SWT.ON</code>. Note that this controls anti-aliasing
     * <em>only</em> for text drawing operations.
     *
     * @return the anti-aliasing setting
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getAntialias
     *
     * @since 3.1
     */
    int getTextAntialias();

    /**
     * Sets the parameter to the transform that is currently being
     * used by the receiver.
     *
     * @param transform the destination to copy the transform into
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Transform
     *
     * @since 3.1
     */
    void getTransform(Transform transform);

    /**
     * Returns <code>true</code> if this GC is drawing in the mode
     * where the resulting color in the destination is the
     * <em>exclusive or</em> of the color values in the source
     * and the destination, and <code>false</code> if it is
     * drawing in the mode where the destination color is being
     * replaced with the source color value.
     *
     * @return <code>true</code> true if the receiver is in XOR mode, and false otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    boolean getXORMode();

    /**
     * Returns an integer hash code for the receiver. Any two
     * objects that return <code>true</code> when passed to
     * <code>equals</code> must return the same value for this
     * method.
     *
     * @return the receiver's hash
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #equals
     */
    int hashCode();

    /**
     * Returns <code>true</code> if the receiver has a clipping
     * region set into it, and <code>false</code> otherwise.
     * If this method returns false, the receiver will draw on all
     * available space in the destination. If it returns true,
     * it will draw only in the area that is covered by the region
     * that can be accessed with <code>getClipping(region)</code>.
     *
     * @return <code>true</code> if the GC has a clipping region, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    boolean isClipped();

    /**
     * Returns <code>true</code> if the GC has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the GC.
     * When a GC has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the GC.
     *
     * @return <code>true</code> when the GC is disposed and <code>false</code> otherwise
     */
    boolean isDisposed();

    /**
     * Sets the receiver to always use the operating system's advanced graphics
     * subsystem for all graphics operations if the argument is <code>true</code>.
     * If the argument is <code>false</code>, the advanced graphics subsystem is
     * no longer used, advanced graphics state is cleared and the normal graphics
     * subsystem is used from now on.
     * <p>
     * Normally, the advanced graphics subsystem is invoked automatically when
     * any one of the alpha, antialias, patterns, interpolation, paths, clipping
     * or transformation operations in the receiver is requested.  When the receiver
     * is switched into advanced mode, the advanced graphics subsystem performs both
     * advanced and normal graphics operations.  Because the two subsystems are
     * different, their output may differ.  Switching to advanced graphics before
     * any graphics operations are performed ensures that the output is consistent.
     * </p><p>
     * Advanced graphics may not be installed for the operating system.  In this
     * case, this operation does nothing.  Some operating system have only one
     * graphics subsystem, so switching from normal to advanced graphics does
     * nothing.  However, switching from advanced to normal graphics will always
     * clear the advanced graphics state, even for operating systems that have
     * only one graphics subsystem.
     * </p>
     *
     * @param advanced the new advanced graphics state
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #setAlpha
     * @see #setAntialias
     * @see #setBackgroundPattern
     * @see #setClipping(Path)
     * @see #setForegroundPattern
     * @see #setLineAttributes
     * @see #setInterpolation
     * @see #setTextAntialias
     * @see #setTransform
     * @see #getAdvanced
     *
     * @since 3.1
     */
    void setAdvanced(boolean advanced);

    /**
     * Sets the receiver's anti-aliasing value to the parameter,
     * which must be one of <code>SWT.DEFAULT</code>, <code>SWT.OFF</code>
     * or <code>SWT.ON</code>. Note that this controls anti-aliasing for all
     * <em>non-text drawing</em> operations.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     *
     * @param antialias the anti-aliasing setting
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter is not one of <code>SWT.DEFAULT</code>,
     *                                 <code>SWT.OFF</code> or <code>SWT.ON</code></li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     *
     * @see #getAdvanced
     * @see #setAdvanced
     * @see #setTextAntialias
     *
     * @since 3.1
     */
    void setAntialias(int antialias);

    /**
     * Sets the receiver's alpha value which must be
     * between 0 (transparent) and 255 (opaque).
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * @param alpha the alpha value
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     *
     * @see #getAdvanced
     * @see #setAdvanced
     *
     * @since 3.1
     */
    void setAlpha(int alpha);

    /**
     * Sets the background color. The background color is used
     * for fill operations and as the background color when text
     * is drawn.
     *
     * @param color the new background color for the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the color is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the color has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void setBackground(Color color);

    /**
     * Sets the background pattern. The default value is <code>null</code>.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     *
     * @param pattern the new background pattern
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     *
     * @see Pattern
     * @see #getAdvanced
     * @see #setAdvanced
     *
     * @since 3.1
     */
    void setBackgroundPattern(Pattern pattern);

    /**
     * Sets the area of the receiver which can be changed
     * by drawing operations to the rectangular area specified
     * by the arguments.
     *
     * @param x the x coordinate of the clipping rectangle
     * @param y the y coordinate of the clipping rectangle
     * @param width the width of the clipping rectangle
     * @param height the height of the clipping rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void setClipping(int x, int y, int width, int height);

    /**
     * Sets the area of the receiver which can be changed
     * by drawing operations to the path specified
     * by the argument.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     *
     * @param path the clipping path.
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the path has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     *
     * @see Path
     * @see #getAdvanced
     * @see #setAdvanced
     *
     * @since 3.1
     */
    void setClipping(Path path);

    /**
     * Sets the area of the receiver which can be changed
     * by drawing operations to the rectangular area specified
     * by the argument.  Specifying <code>null</code> for the
     * rectangle reverts the receiver's clipping area to its
     * original value.
     *
     * @param rect the clipping rectangle or <code>null</code>
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void setClipping(Rectangle rect);

    /**
     * Sets the area of the receiver which can be changed
     * by drawing operations to the region specified
     * by the argument.  Specifying <code>null</code> for the
     * region reverts the receiver's clipping area to its
     * original value.
     *
     * @param region the clipping region or <code>null</code>
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the region has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void setClipping(Region region);

    /**
     * Sets the receiver's fill rule to the parameter, which must be one of
     * <code>SWT.FILL_EVEN_ODD</code> or <code>SWT.FILL_WINDING</code>.
     *
     * @param rule the new fill rule
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the rule is not one of <code>SWT.FILL_EVEN_ODD</code>
     *                                 or <code>SWT.FILL_WINDING</code></li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    void setFillRule(int rule);

    /**
     * Sets the font which will be used by the receiver
     * to draw and measure text to the argument. If the
     * argument is null, then a default font appropriate
     * for the platform will be used instead.
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
    void setFont(Font font);

    /**
     * Sets the foreground color. The foreground color is used
     * for drawing operations including when text is drawn.
     *
     * @param color the new foreground color for the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the color is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the color has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void setForeground(Color color);

    /**
     * Sets the foreground pattern. The default value is <code>null</code>.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * @param pattern the new foreground pattern
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     *
     * @see Pattern
     * @see #getAdvanced
     * @see #setAdvanced
     *
     * @since 3.1
     */
    void setForegroundPattern(Pattern pattern);

    /**
     * Sets the receiver's interpolation setting to the parameter, which
     * must be one of <code>SWT.DEFAULT</code>, <code>SWT.NONE</code>,
     * <code>SWT.LOW</code> or <code>SWT.HIGH</code>.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     *
     * @param interpolation the new interpolation setting
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the rule is not one of <code>SWT.DEFAULT</code>,
     *                                 <code>SWT.NONE</code>, <code>SWT.LOW</code> or <code>SWT.HIGH</code>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     *
     * @see #getAdvanced
     * @see #setAdvanced
     *
     * @since 3.1
     */
    void setInterpolation(int interpolation);

    /**
     * Sets the receiver's line attributes.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * @param attributes the line attributes
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the attributes is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if any of the line attributes is not valid</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     *
     * @see LineAttributes
     * @see #getAdvanced
     * @see #setAdvanced
     *
     * @since 3.3
     */
    void setLineAttributes(LineAttributes attributes);

    /**
     * Sets the receiver's line cap style to the argument, which must be one
     * of the constants <code>SWT.CAP_FLAT</code>, <code>SWT.CAP_ROUND</code>,
     * or <code>SWT.CAP_SQUARE</code>.
     *
     * @param cap the cap style to be used for drawing lines
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the style is not valid</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    void setLineCap(int cap);

    /**
     * Sets the receiver's line dash style to the argument. The default
     * value is <code>null</code>. If the argument is not <code>null</code>,
     * the receiver's line style is set to <code>SWT.LINE_CUSTOM</code>, otherwise
     * it is set to <code>SWT.LINE_SOLID</code>.
     *
     * @param dashes the dash style to be used for drawing lines
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if any of the values in the array is less than or equal 0</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    void setLineDash(int[] dashes);

    /**
     * Sets the receiver's line join style to the argument, which must be one
     * of the constants <code>SWT.JOIN_MITER</code>, <code>SWT.JOIN_ROUND</code>,
     * or <code>SWT.JOIN_BEVEL</code>.
     *
     * @param join the join style to be used for drawing lines
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the style is not valid</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @since 3.1
     */
    void setLineJoin(int join);

    /**
     * Sets the receiver's line style to the argument, which must be one
     * of the constants <code>SWT.LINE_SOLID</code>, <code>SWT.LINE_DASH</code>,
     * <code>SWT.LINE_DOT</code>, <code>SWT.LINE_DASHDOT</code> or
     * <code>SWT.LINE_DASHDOTDOT</code>.
     *
     * @param lineStyle the style to be used for drawing lines
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the style is not valid</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void setLineStyle(int lineStyle);

    /**
     * Sets the width that will be used when drawing lines
     * for all of the figure drawing operations (that is,
     * <code>drawLine</code>, <code>drawRectangle</code>,
     * <code>drawPolyline</code>, and so forth.
     * <p>
     * Note that line width of zero is used as a hint to
     * indicate that the fastest possible line drawing
     * algorithms should be used. This means that the
     * output may be different from line width one and
     * specially at high DPI it's not recommended to mix
     * line width zero with other line widths.
     * </p>
     *
     * @param lineWidth the width of a line
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void setLineWidth(int lineWidth);

    /**
     * If the argument is <code>true</code>, puts the receiver
     * in a drawing mode where the resulting color in the destination
     * is the <em>exclusive or</em> of the color values in the source
     * and the destination, and if the argument is <code>false</code>,
     * puts the receiver in a drawing mode where the destination color
     * is replaced with the source color value.
     *
     * @param xor if <code>true</code>, then <em>xor</em> mode is used, otherwise <em>source copy</em> mode is used
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void setXORMode(boolean xor);

    /**
     * Sets the receiver's text anti-aliasing value to the parameter,
     * which must be one of <code>SWT.DEFAULT</code>, <code>SWT.OFF</code>
     * or <code>SWT.ON</code>. Note that this controls anti-aliasing only
     * for all <em>text drawing</em> operations.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     *
     * @param antialias the anti-aliasing setting
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter is not one of <code>SWT.DEFAULT</code>,
     *                                 <code>SWT.OFF</code> or <code>SWT.ON</code></li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     *
     * @see #getAdvanced
     * @see #setAdvanced
     * @see #setAntialias
     *
     * @since 3.1
     */
    void setTextAntialias(int antialias);

    /**
     * Sets the transform that is currently being used by the receiver. If
     * the argument is <code>null</code>, the current transform is set to
     * the identity transform.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     *
     * @param transform the transform to set
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     *
     * @see Transform
     * @see #getAdvanced
     * @see #setAdvanced
     *
     * @since 3.1
     */
    void setTransform(Transform transform);

    /**
     * Returns the extent of the given string. No tab
     * expansion or carriage return processing will be performed.
     * <p>
     * The <em>extent</em> of a string is the width and height of
     * the rectangular area it would cover if drawn in a particular
     * font (in this case, the current font in the receiver).
     * </p>
     *
     * @param string the string to measure
     * @return a point containing the extent of the string
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Point stringExtent(String string);

    /**
     * Returns the extent of the given string. Tab expansion and
     * carriage return processing are performed.
     * <p>
     * The <em>extent</em> of a string is the width and height of
     * the rectangular area it would cover if drawn in a particular
     * font (in this case, the current font in the receiver).
     * </p>
     *
     * @param string the string to measure
     * @return a point containing the extent of the string
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Point textExtent(String string);

    /**
     * Returns the extent of the given string. Tab expansion, line
     * delimiter and mnemonic processing are performed according to
     * the specified flags, which can be a combination of:
     * <dl>
     * <dt><b>DRAW_DELIMITER</b></dt>
     * <dd>draw multiple lines</dd>
     * <dt><b>DRAW_TAB</b></dt>
     * <dd>expand tabs</dd>
     * <dt><b>DRAW_MNEMONIC</b></dt>
     * <dd>underline the mnemonic character</dd>
     * <dt><b>DRAW_TRANSPARENT</b></dt>
     * <dd>transparent background</dd>
     * </dl>
     * <p>
     * The <em>extent</em> of a string is the width and height of
     * the rectangular area it would cover if drawn in a particular
     * font (in this case, the current font in the receiver).
     * </p>
     *
     * @param string the string to measure
     * @param flags the flags specifying how to process the text
     * @return a point containing the extent of the string
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Point textExtent(String string, int flags);

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    String toString();

    GC getApi();
}
