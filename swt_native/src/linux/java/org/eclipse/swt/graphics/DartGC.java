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
import org.eclipse.swt.internal.cairo.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.VGC.*;
import dev.equo.swt.*;

/**
 * Class <code>GC</code> is where all of the drawing capabilities that are
 * supported by SWT are located. Instances are used to draw on either an
 * <code>Image</code>, a <code>Control</code>, or directly on a <code>Display</code>.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>LEFT_TO_RIGHT, RIGHT_TO_LEFT</dd>
 * </dl>
 *
 * <p>
 * The SWT drawing coordinate system is the two-dimensional space with the origin
 * (0,0) at the top left corner of the drawing area and with (x,y) values increasing
 * to the right and downward respectively.
 * </p>
 *
 * <p>
 * The result of drawing on an image that was created with an indexed
 * palette using a color that is not in the palette is platform specific.
 * Some platforms will match to the nearest color while other will draw
 * the color itself. This happens because the allocated image might use
 * a direct palette on platforms that do not support indexed palette.
 * </p>
 *
 * <p>
 * Application code must explicitly invoke the <code>GC.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required. This is <em>particularly</em>
 * important on Windows95 and Windows98 where the operating system has a limited
 * number of device contexts available.
 * </p>
 *
 * <p>
 * Note: Only one of LEFT_TO_RIGHT and RIGHT_TO_LEFT may be specified.
 * </p>
 *
 * @see org.eclipse.swt.events.PaintEvent
 * @see <a href="http://www.eclipse.org/swt/snippets/#gc">GC snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Examples: GraphicsExample, PaintExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class DartGC extends DartResource implements IGC {

    Drawable drawable;

    GCData data;

    /**
     * The current Cairo matrix, which positions widgets in the shell.
     * Client transformations come on top of this matrix.
     */
    private double[] cairoTransformationMatrix;

    /**
     * Tracks the last transformation with which {@link #setTransform(Transform)} was called,
     * so that we can answer clients of {@link #getTransform(Transform)}.
     */
    private double[] currentTransform;

    /**
     * Original clipping set on this GC
     */
    Rectangle clipping = new Rectangle(0, 0, 0, 0);

    final static int FOREGROUND = 1 << 0;

    final static int BACKGROUND = 1 << 1;

    final static int FONT = 1 << 2;

    final static int LINE_STYLE = 1 << 3;

    final static int LINE_CAP = 1 << 4;

    final static int LINE_JOIN = 1 << 5;

    final static int LINE_WIDTH = 1 << 6;

    final static int LINE_MITERLIMIT = 1 << 7;

    final static int BACKGROUND_BG = 1 << 8;

    final static int DRAW_OFFSET = 1 << 9;

    final static int DRAW = FOREGROUND | LINE_WIDTH | LINE_STYLE | LINE_CAP | LINE_JOIN | LINE_MITERLIMIT | DRAW_OFFSET;

    final static int FILL = BACKGROUND;

    static final float[] LINE_DOT = new float[] { 1, 1 };

    static final float[] LINE_DASH = new float[] { 3, 1 };

    static final float[] LINE_DASHDOT = new float[] { 3, 1, 1, 1 };

    static final float[] LINE_DASHDOTDOT = new float[] { 3, 1, 1, 1, 1, 1 };

    static final float[] LINE_DOT_ZERO = new float[] { 3, 3 };

    static final float[] LINE_DASH_ZERO = new float[] { 18, 6 };

    static final float[] LINE_DASHDOT_ZERO = new float[] { 9, 6, 3, 6 };

    static final float[] LINE_DASHDOTDOT_ZERO = new float[] { 9, 3, 3, 3, 3, 3 };

    DartGC(GC api) {
        super(api);
    }

    /**
     * Constructs a new instance of this class which has been
     * configured to draw on the specified drawable. Sets the
     * foreground color, background color and font in the GC
     * to match those in the drawable.
     * <p>
     * You must dispose the graphics context when it is no longer required.
     * </p>
     * @param drawable the drawable to draw on
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the drawable is null</li>
     *    <li>ERROR_NULL_ARGUMENT - if there is no current device</li>
     *    <li>ERROR_INVALID_ARGUMENT
     *          - if the drawable is an image that is not a bitmap or an icon
     *          - if the drawable is an image or printer that is already selected
     *            into another graphics context</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for GC creation</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS if not called from the thread that created the drawable</li>
     * </ul>
     * @see #dispose()
     */
    public DartGC(Drawable drawable, GC api) {
        this(drawable, 0, api);
    }

    /**
     * Constructs a new instance of this class which has been
     * configured to draw on the specified drawable. Sets the
     * foreground color, background color and font in the GC
     * to match those in the drawable.
     * <p>
     * You must dispose the graphics context when it is no longer required.
     * </p>
     *
     * @param drawable the drawable to draw on
     * @param style the style of GC to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the drawable is null</li>
     *    <li>ERROR_NULL_ARGUMENT - if there is no current device</li>
     *    <li>ERROR_INVALID_ARGUMENT
     *          - if the drawable is an image that is not a bitmap or an icon
     *          - if the drawable is an image or printer that is already selected
     *            into another graphics context</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for GC creation</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS if not called from the thread that created the drawable</li>
     * </ul>
     *
     * @see #dispose()
     *
     * @since 2.1.2
     */
    public DartGC(Drawable drawable, int style, GC api) {
        super(api);
        if (drawable == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        GCData data = new GCData();
        data.style = checkStyle(style);
        Device device = data.device;
        if (device == null)
            device = SwtDevice.getDevice();
        if (device == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        this.device = data.device = device;
        long gdkGC = drawable.internal_new_GC(data);
        init(drawable, data, gdkGC);
        init();
    }

    /**
     * Ensure that the style specified is either LEFT_TO_RIGHT <b>or</b> RIGHT_TO_LEFT.
     *
     * @param style the SWT style bit string
     * @return If only one style is specified, it is return unmodified. If both styles are specified, returns LEFT_TO_RIGHT
     */
    int checkStyle(int style) {
        if ((style & SWT.LEFT_TO_RIGHT) != 0)
            style &= ~SWT.RIGHT_TO_LEFT;
        return style & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
    }

    void checkGC(int mask) {
        int state = data.state;
        if ((state & mask) == mask)
            return;
        state = (state ^ mask) & mask;
        data.state |= mask;
        if ((state & (BACKGROUND | FOREGROUND)) != 0) {
            Pattern pattern;
            if ((state & FOREGROUND) != 0) {
                pattern = data.foregroundPattern;
                data.state &= ~BACKGROUND;
            } else {
                pattern = data.backgroundPattern;
                data.state &= ~FOREGROUND;
            }
            if (pattern != null) {
                if ((data.style & SWT.MIRRORED) != 0 && ((SwtPattern) pattern.getImpl()).surface != 0) {
                } else {
                }
            } else {
            }
        }
        if ((state & FONT) != 0) {
            if (data.layout != 0) {
            }
        }
        if ((state & LINE_CAP) != 0) {
            switch(data.lineCap) {
                case SWT.CAP_ROUND:
                    break;
                case SWT.CAP_FLAT:
                    break;
                case SWT.CAP_SQUARE:
                    break;
            }
        }
        if ((state & LINE_JOIN) != 0) {
            switch(data.lineJoin) {
                case SWT.JOIN_MITER:
                    break;
                case SWT.JOIN_ROUND:
                    break;
                case SWT.JOIN_BEVEL:
                    break;
            }
        }
        if ((state & LINE_WIDTH) != 0) {
            switch(data.lineStyle) {
                case SWT.LINE_DOT:
                case SWT.LINE_DASH:
                case SWT.LINE_DASHDOT:
                case SWT.LINE_DASHDOTDOT:
                    state |= LINE_STYLE;
            }
        }
        if ((state & LINE_STYLE) != 0) {
            float dashesOffset = 0;
            float[] dashes = null;
            float width = data.lineWidth;
            switch(data.lineStyle) {
                case SWT.LINE_SOLID:
                    break;
                case SWT.LINE_DASH:
                    dashes = width != 0 ? LINE_DASH : LINE_DASH_ZERO;
                    break;
                case SWT.LINE_DOT:
                    dashes = width != 0 ? LINE_DOT : LINE_DOT_ZERO;
                    break;
                case SWT.LINE_DASHDOT:
                    dashes = width != 0 ? LINE_DASHDOT : LINE_DASHDOT_ZERO;
                    break;
                case SWT.LINE_DASHDOTDOT:
                    dashes = width != 0 ? LINE_DASHDOTDOT : LINE_DASHDOTDOT_ZERO;
                    break;
                case SWT.LINE_CUSTOM:
                    dashes = data.lineDashes;
                    break;
            }
            if (dashes != null) {
                dashesOffset = data.lineDashesOffset;
                double[] cairoDashes = new double[dashes.length];
                for (int i = 0; i < cairoDashes.length; i++) {
                    cairoDashes[i] = width == 0 || data.lineStyle == SWT.LINE_CUSTOM ? dashes[i] : dashes[i] * width;
                }
            } else {
            }
        }
        if ((state & LINE_MITERLIMIT) != 0) {
        }
        if ((state & DRAW_OFFSET) != 0) {
            int effectiveLineWidth = data.lineWidth < 1 ? 1 : Math.round(data.lineWidth);
            if (effectiveLineWidth % 2 == 1) {
                // In case the effective line width is odd, shift coordinates by (0.5, 0.5).
                // I.e., a line starting at (0,0) will effectively start in the pixel right
                // below that coordinate with its center at (0.5, 0.5).
                double[] offsetX = new double[] { 0.5 };
                double[] offsetY = new double[] { 0.5 };
                // The offset will be applied to the coordinate system of the GC; so transform
                // it from the drawing coordinate system to the coordinate system of the GC by
                // applying the inverse transformation as the one applied to the GC and correct
                // it by the line width.
                double[] matrix = new double[6];
                data.cairoXoffset = Math.abs(offsetX[0]);
                data.cairoYoffset = Math.abs(offsetY[0]);
            } else {
                data.cairoXoffset = data.cairoYoffset = 0;
            }
        }
    }

    long convertRgn(long rgn, double[] matrix) {
        if (isIdentity(matrix)) {
        }
        int[] nRects = new int[1];
        int[] pointArray = new int[8];
        double[] x = new double[1], y = new double[1];
        for (int i = 0; i < nRects[0]; i++) {
            pointArray[0] = (int) x[0];
            pointArray[1] = (int) y[0];
            pointArray[2] = (int) Math.round(x[0]);
            pointArray[3] = (int) y[0];
            pointArray[4] = (int) Math.round(x[0]);
            pointArray[5] = (int) Math.round(y[0]);
            pointArray[6] = (int) x[0];
            pointArray[7] = (int) Math.round(y[0]);
        }
        return 0;
    }

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
    public void copyArea(Image image, int x, int y) {
        VGCCopyAreaImageintint drawOp = new VGCCopyAreaImageintint();
        drawOp.image = GraphicsUtils.copyImage(display, image);
        drawOp.x = x;
        drawOp.y = y;
        FlutterBridge.send(this, "copyAreaImageintint", drawOp);
    }

    void copyAreaInPixels(Image image, int x, int y) {
        if (data.image != null) {
        } else if (data.drawable != 0) {
        } else {
            return;
        }
    }

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
    public void copyArea(int srcX, int srcY, int width, int height, int destX, int destY) {
        VGCCopyAreaintintintintintint drawOp = new VGCCopyAreaintintintintintint();
        drawOp.srcX = srcX;
        drawOp.srcY = srcY;
        drawOp.width = width;
        drawOp.height = height;
        drawOp.destX = destX;
        drawOp.destY = destY;
        FlutterBridge.send(this, "copyAreaintintintintintint", drawOp);
    }

    void copyAreaInPixels(int srcX, int srcY, int width, int height, int destX, int destY) {
        copyAreaInPixels(srcX, srcY, width, height, destX, destY, true);
    }

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
    public void copyArea(int srcX, int srcY, int width, int height, int destX, int destY, boolean paint) {
        VGCCopyAreaintintintintintintboolean drawOp = new VGCCopyAreaintintintintintintboolean();
        drawOp.srcX = srcX;
        drawOp.srcY = srcY;
        drawOp.width = width;
        drawOp.height = height;
        drawOp.destX = destX;
        drawOp.destY = destY;
        drawOp.paint = paint;
        FlutterBridge.send(this, "copyAreaintintintintintintboolean", drawOp);
    }

    void copyAreaInPixels(int srcX, int srcY, int width, int height, int destX, int destY, boolean paint) {
        if (width <= 0 || height <= 0)
            return;
        int deltaX = destX - srcX, deltaY = destY - srcY;
        if (deltaX == 0 && deltaY == 0)
            return;
        long drawable = data.drawable;
        if (data.image != null) {
            // As source and target area may be overlapping, we need to draw on
            // an intermediate surface to avoid that parts of the source area are
        } else if (drawable != 0) {
            if (paint) {
            }
        }
        if (data.image == null && paint) {
            boolean disjoint = (destX + width < srcX) || (srcX + width < destX) || (destY + height < srcY) || (srcY + height < destY);
            if (disjoint) {
            } else {
                if (deltaX != 0) {
                    int newX = destX - deltaX;
                    if (deltaX < 0)
                        newX = destX + width;
                }
                if (deltaY != 0) {
                    int newY = destY - deltaY;
                    if (deltaY < 0)
                        newY = destY + height;
                }
            }
        }
    }

    void createLayout() {
    }

    void disposeLayout() {
        data.string = null;
        data.layout = data.context = 0;
    }

    @Override
    void destroy() {
        if (data.disposeCairo) {
        }
        data.cairo = 0;
        /* Free resources */
        long clipRgn = data.clipRgn;
        Image image = data.image;
        if (image != null) {
            if (image.getImpl() instanceof DartImage) {
                ((DartImage) image.getImpl()).memGC = null;
            }
            if (image.getImpl() instanceof SwtImage) {
                ((SwtImage) image.getImpl()).memGC = null;
            }
            if (image.getImpl()._transparentPixel() != -1)
                ((DartImage) image.getImpl()).createMask();
        }
        disposeLayout();
        /* Dispose the GC */
        if (drawable != null) {
            drawable.internal_dispose_GC(getApi().handle, data);
        }
        data.drawable = data.clipRgn = 0;
        drawable = null;
        getApi().handle = 0;
        data.image = null;
        data.string = null;
        data = null;
    }

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
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        VGCDrawArcintintintintintint drawOp = new VGCDrawArcintintintintintint();
        drawOp.x = x;
        drawOp.y = y;
        drawOp.width = width;
        drawOp.height = height;
        drawOp.startAngle = startAngle;
        drawOp.arcAngle = arcAngle;
        FlutterBridge.send(this, "drawArcintintintintintint", drawOp);
    }

    void drawArcInPixels(int x, int y, int width, int height, int startAngle, int arcAngle) {
        checkGC(DRAW);
        if (width < 0) {
            x = x + width;
            width = -width;
        }
        if (height < 0) {
            y = y + height;
            height = -height;
        }
        if (width == 0 || height == 0 || arcAngle == 0)
            return;
        if (width == height) {
            if (arcAngle >= 0) {
            } else {
            }
        } else {
            if (arcAngle >= 0) {
            } else {
            }
        }
    }

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
    public void drawFocus(int x, int y, int width, int height) {
        VGCDrawFocusintintintint drawOp = new VGCDrawFocusintintintint();
        drawOp.x = x;
        drawOp.y = y;
        drawOp.width = width;
        drawOp.height = height;
        FlutterBridge.send(this, "drawFocusintintintint", drawOp);
    }

    void drawFocusInPixels(int x, int y, int width, int height) {
        checkGC(FOREGROUND);
    }

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
    public void drawImage(Image image, int x, int y) {
        VGCDrawImageImageintint drawOp = new VGCDrawImageImageintint();
        drawOp.image = GraphicsUtils.copyImage(display, image);
        drawOp.x = x;
        drawOp.y = y;
        FlutterBridge.send(this, "drawImageImageintint", drawOp);
    }

    void drawImageInPixels(Image image, int x, int y) {
        drawImage(image, 0, 0, -1, -1, x, y, -1, -1, true);
    }

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
    public void drawImage(Image image, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight) {
        VGCDrawImageImageintintintintintintintint drawOp = new VGCDrawImageImageintintintintintintintint();
        drawOp.image = GraphicsUtils.copyImage(display, image);
        drawOp.srcX = srcX;
        drawOp.srcY = srcY;
        drawOp.srcWidth = srcWidth;
        drawOp.srcHeight = srcHeight;
        drawOp.destX = destX;
        drawOp.destY = destY;
        drawOp.destWidth = destWidth;
        drawOp.destHeight = destHeight;
        FlutterBridge.send(this, "drawImageImageintintintintintintintint", drawOp);
    }

    void drawImage(Image srcImage, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, boolean simple) {
        /* Refresh Image as per zoom level, if required. */
        ((DartImage) srcImage.getImpl()).refreshImageForZoom();
        ImageData srcImageData = srcImage.getImageData();
        int imgWidth = srcImageData.width;
        int imgHeight = srcImageData.height;
        if (simple) {
            srcWidth = destWidth = imgWidth;
            srcHeight = destHeight = imgHeight;
        } else {
            simple = srcX == 0 && srcY == 0 && srcWidth == destWidth && destWidth == imgWidth && srcHeight == destHeight && destHeight == imgHeight;
            if (srcX + srcWidth > imgWidth + 1 || srcY + srcHeight > imgHeight + 1) {
                //rounding error correction for hidpi
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
        }
        if (data.alpha != 0) {
            ((DartImage) srcImage.getImpl()).createSurface();
            if ((data.style & SWT.MIRRORED) != 0) {
            }
            if (srcWidth != destWidth || srcHeight != destHeight) {
            } else {
            }
            switch(data.interpolation) {
                case SWT.DEFAULT:
                    break;
                case SWT.NONE:
                    break;
                case SWT.LOW:
                    break;
                case SWT.HIGH:
                    break;
            }
            if (srcWidth != destWidth || srcHeight != destHeight) {
            }
            if (data.alpha != 0xFF) {
            } else {
            }
        }
    }

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
    public void drawLine(int x1, int y1, int x2, int y2) {
        VGCDrawLineintintintint drawOp = new VGCDrawLineintintintint();
        drawOp.x1 = x1;
        drawOp.y1 = y1;
        drawOp.x2 = x2;
        drawOp.y2 = y2;
        FlutterBridge.send(this, "drawLineintintintint", drawOp);
    }

    void drawLineInPixels(int x1, int y1, int x2, int y2) {
        checkGC(DRAW);
    }

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
    public void drawOval(int x, int y, int width, int height) {
        VGCDrawOvalintintintint drawOp = new VGCDrawOvalintintintint();
        drawOp.x = x;
        drawOp.y = y;
        drawOp.width = width;
        drawOp.height = height;
        FlutterBridge.send(this, "drawOvalintintintint", drawOp);
    }

    void drawOvalInPixels(int x, int y, int width, int height) {
        checkGC(DRAW);
        if (width < 0) {
            x = x + width;
            width = -width;
        }
        if (height < 0) {
            y = y + height;
            height = -height;
        }
        if (width == height) {
        } else {
        }
    }

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
    public void drawPath(Path path) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (path == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (path.handle == 0)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        checkGC(DRAW);
    }

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
    public void drawPoint(int x, int y) {
        VGCDrawPointintint drawOp = new VGCDrawPointintint();
        drawOp.x = x;
        drawOp.y = y;
        FlutterBridge.send(this, "drawPointintint", drawOp);
    }

    void drawPointInPixels(int x, int y) {
        checkGC(DRAW);
    }

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
    public void drawPolygon(int[] pointArray) {
        VGCDrawPolygonint drawOp = new VGCDrawPolygonint();
        drawOp.pointArray = pointArray;
        FlutterBridge.send(this, "drawPolygonint", drawOp);
    }

    void drawPolygonInPixels(int[] pointArray) {
        checkGC(DRAW);
    }

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
    public void drawPolyline(int[] pointArray) {
        VGCDrawPolylineint drawOp = new VGCDrawPolylineint();
        drawOp.pointArray = pointArray;
        FlutterBridge.send(this, "drawPolylineint", drawOp);
    }

    void drawPolylineInPixels(int[] pointArray) {
        checkGC(DRAW);
    }

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
    public void drawRectangle(int x, int y, int width, int height) {
        VGCDrawRectangleintintintint drawOp = new VGCDrawRectangleintintintint();
        drawOp.x = x;
        drawOp.y = y;
        drawOp.width = width;
        drawOp.height = height;
        FlutterBridge.send(this, "drawRectangleintintintint", drawOp);
    }

    void drawRectangleInPixels(int x, int y, int width, int height) {
        checkGC(DRAW);
        if (width < 0) {
            x = x + width;
            width = -width;
        }
        if (height < 0) {
            y = y + height;
            height = -height;
        }
    }

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
    public void drawRectangle(Rectangle rect) {
        VGCDrawRectangleRectangle drawOp = new VGCDrawRectangleRectangle();
        drawOp.rect = rect;
        FlutterBridge.send(this, "drawRectangleRectangle", drawOp);
    }

    void drawRectangleInPixels(Rectangle rect) {
        drawRectangleInPixels(rect.x, rect.y, rect.width, rect.height);
    }

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
    public void drawRoundRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        VGCDrawRoundRectangleintintintintintint drawOp = new VGCDrawRoundRectangleintintintintintint();
        drawOp.x = x;
        drawOp.y = y;
        drawOp.width = width;
        drawOp.height = height;
        drawOp.arcWidth = arcWidth;
        drawOp.arcHeight = arcHeight;
        FlutterBridge.send(this, "drawRoundRectangleintintintintintint", drawOp);
    }

    void drawRoundRectangleInPixels(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        checkGC(DRAW);
        int nx = x;
        int ny = y;
        int nw = width;
        int nh = height;
        int naw = arcWidth;
        int nah = arcHeight;
        if (nw < 0) {
            nw = 0 - nw;
            nx = nx - nw;
        }
        if (nh < 0) {
            nh = 0 - nh;
            ny = ny - nh;
        }
        if (naw < 0)
            naw = 0 - naw;
        if (nah < 0)
            nah = 0 - nah;
        if (naw == 0 || nah == 0) {
        } else {
        }
    }

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
    public void drawString(String string, int x, int y) {
        VGCDrawStringStringintint drawOp = new VGCDrawStringStringintint();
        drawOp.string = string;
        drawOp.x = x;
        drawOp.y = y;
        FlutterBridge.send(this, "drawStringStringintint", drawOp);
    }

    void drawStringInPixels(String string, int x, int y) {
        drawStringInPixels(string, x, y, false);
    }

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
    public void drawString(String string, int x, int y, boolean isTransparent) {
        VGCDrawStringStringintintboolean drawOp = new VGCDrawStringStringintintboolean();
        drawOp.string = string;
        drawOp.x = x;
        drawOp.y = y;
        drawOp.isTransparent = isTransparent;
        FlutterBridge.send(this, "drawStringStringintintboolean", drawOp);
    }

    void drawStringInPixels(String string, int x, int y, boolean isTransparent) {
        drawTextInPixels(string, x, y, isTransparent ? SWT.DRAW_TRANSPARENT : 0);
    }

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
    public void drawText(String string, int x, int y) {
        VGCDrawTextStringintint drawOp = new VGCDrawTextStringintint();
        drawOp.string = string;
        drawOp.x = x;
        drawOp.y = y;
        FlutterBridge.send(this, "drawTextStringintint", drawOp);
    }

    void drawTextInPixels(String string, int x, int y) {
        drawTextInPixels(string, x, y, SWT.DRAW_DELIMITER | SWT.DRAW_TAB);
    }

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
    public void drawText(String string, int x, int y, boolean isTransparent) {
        VGCDrawTextStringintintboolean drawOp = new VGCDrawTextStringintintboolean();
        drawOp.string = string;
        drawOp.x = x;
        drawOp.y = y;
        drawOp.isTransparent = isTransparent;
        FlutterBridge.send(this, "drawTextStringintintboolean", drawOp);
    }

    void drawTextInPixels(String string, int x, int y, boolean isTransparent) {
        int flags = SWT.DRAW_DELIMITER | SWT.DRAW_TAB;
        if (isTransparent)
            flags |= SWT.DRAW_TRANSPARENT;
        drawTextInPixels(string, x, y, flags);
    }

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
    public void drawText(String string, int x, int y, int flags) {
        VGCDrawTextStringintintint drawOp = new VGCDrawTextStringintintint();
        drawOp.string = string;
        drawOp.x = x;
        drawOp.y = y;
        drawOp.flags = flags;
        FlutterBridge.send(this, "drawTextStringintintint", drawOp);
    }

    void drawTextInPixels(String string, int x, int y, int flags) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (string == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (string.length() == 0)
            return;
        setString(string, flags);
        checkGC(FONT);
        if ((flags & SWT.DRAW_TRANSPARENT) == 0) {
            checkGC(BACKGROUND);
            if (data.stringWidth == -1) {
                computeStringSize();
            }
        }
        checkGC(FOREGROUND);
        if ((data.style & SWT.MIRRORED) != 0) {
            if (data.stringWidth == -1) {
                computeStringSize();
            }
        }
        if ((data.style & SWT.MIRRORED) != 0) {
        }
    }

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
    @Override
    public boolean equals(Object object) {
        if (object == this.getApi())
            return true;
        if (!(object instanceof GC))
            return false;
        return getApi().handle == ((GC) object).handle;
    }

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
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        VGCFillArcintintintintintint drawOp = new VGCFillArcintintintintintint();
        drawOp.x = x;
        drawOp.y = y;
        drawOp.width = width;
        drawOp.height = height;
        drawOp.startAngle = startAngle;
        drawOp.arcAngle = arcAngle;
        FlutterBridge.send(this, "fillArcintintintintintint", drawOp);
    }

    void fillArcInPixels(int x, int y, int width, int height, int startAngle, int arcAngle) {
        checkGC(FILL);
        if (width < 0) {
            x = x + width;
            width = -width;
        }
        if (height < 0) {
            y = y + height;
            height = -height;
        }
        if (width == 0 || height == 0 || arcAngle == 0)
            return;
        if (width == height) {
            if (arcAngle >= 0) {
            } else {
            }
        } else {
            if (arcAngle >= 0) {
            } else {
            }
        }
    }

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
    public void fillGradientRectangle(int x, int y, int width, int height, boolean vertical) {
        VGCFillGradientRectangleintintintintboolean drawOp = new VGCFillGradientRectangleintintintintboolean();
        drawOp.x = x;
        drawOp.y = y;
        drawOp.width = width;
        drawOp.height = height;
        drawOp.vertical = vertical;
        FlutterBridge.send(this, "fillGradientRectangleintintintintboolean", drawOp);
    }

    void fillGradientRectangleInPixels(int x, int y, int width, int height, boolean vertical) {
        if ((width == 0) || (height == 0))
            return;
        /* Rewrite this to use GdkPixbuf */
        RGB backgroundRGB, foregroundRGB;
        backgroundRGB = getBackground().getRGB();
        foregroundRGB = getForeground().getRGB();
        RGB fromRGB, toRGB;
        fromRGB = foregroundRGB;
        toRGB = backgroundRGB;
        boolean swapColors = false;
        if (width < 0) {
            x += width;
            width = -width;
            if (!vertical)
                swapColors = true;
        }
        if (height < 0) {
            y += height;
            height = -height;
            if (vertical)
                swapColors = true;
        }
        if (swapColors) {
            fromRGB = backgroundRGB;
            toRGB = foregroundRGB;
        }
        if (fromRGB.equals(toRGB)) {
            fillRectangleInPixels(x, y, width, height);
            return;
        }
        if (vertical) {
        } else {
        }
    }

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
    public void fillOval(int x, int y, int width, int height) {
        VGCFillOvalintintintint drawOp = new VGCFillOvalintintintint();
        drawOp.x = x;
        drawOp.y = y;
        drawOp.width = width;
        drawOp.height = height;
        FlutterBridge.send(this, "fillOvalintintintint", drawOp);
    }

    void fillOvalInPixels(int x, int y, int width, int height) {
        checkGC(FILL);
        if (width < 0) {
            x = x + width;
            width = -width;
        }
        if (height < 0) {
            y = y + height;
            height = -height;
        }
        if (width == height) {
        } else {
        }
    }

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
    public void fillPath(Path path) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (path == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (path.handle == 0)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        checkGC(FILL);
    }

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
    public void fillPolygon(int[] pointArray) {
        VGCFillPolygonint drawOp = new VGCFillPolygonint();
        drawOp.pointArray = pointArray;
        FlutterBridge.send(this, "fillPolygonint", drawOp);
    }

    void fillPolygonInPixels(int[] pointArray) {
        checkGC(FILL);
    }

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
    public void fillRectangle(int x, int y, int width, int height) {
        VGCFillRectangleintintintint drawOp = new VGCFillRectangleintintintint();
        drawOp.x = x;
        drawOp.y = y;
        drawOp.width = width;
        drawOp.height = height;
        FlutterBridge.send(this, "fillRectangleintintintint", drawOp);
    }

    void fillRectangleInPixels(int x, int y, int width, int height) {
        checkGC(FILL);
        if (width < 0) {
            x = x + width;
            width = -width;
        }
        if (height < 0) {
            y = y + height;
            height = -height;
        }
        if (data.regionSet != 0) {
        } else {
        }
    }

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
    public void fillRectangle(Rectangle rect) {
        VGCFillRectangleRectangle drawOp = new VGCFillRectangleRectangle();
        drawOp.rect = rect;
        FlutterBridge.send(this, "fillRectangleRectangle", drawOp);
    }

    void fillRectangleInPixels(Rectangle rect) {
        fillRectangleInPixels(rect.x, rect.y, rect.width, rect.height);
    }

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
    public void fillRoundRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        VGCFillRoundRectangleintintintintintint drawOp = new VGCFillRoundRectangleintintintintintint();
        drawOp.x = x;
        drawOp.y = y;
        drawOp.width = width;
        drawOp.height = height;
        drawOp.arcWidth = arcWidth;
        drawOp.arcHeight = arcHeight;
        FlutterBridge.send(this, "fillRoundRectangleintintintintintint", drawOp);
    }

    void fillRoundRectangleInPixels(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        checkGC(FILL);
        int nx = x;
        int ny = y;
        int nw = width;
        int nh = height;
        int naw = arcWidth;
        int nah = arcHeight;
        if (nw < 0) {
            nw = 0 - nw;
            nx = nx - nw;
        }
        if (nh < 0) {
            nh = 0 - nh;
            ny = ny - nh;
        }
        if (naw < 0)
            naw = 0 - naw;
        if (nah < 0)
            nah = 0 - nah;
        if (naw == 0 || nah == 0) {
        } else {
        }
    }

    int fixMnemonic(char[] buffer) {
        int i = 0, j = 0;
        int mnemonic = -1;
        while (i < buffer.length) {
            if ((buffer[j++] = buffer[i++]) == '&') {
                if (i == buffer.length) {
                    continue;
                }
                if (buffer[i] == '&') {
                    i++;
                    continue;
                }
                if (mnemonic == -1)
                    mnemonic = j;
                j--;
            }
        }
        while (j < buffer.length) buffer[j++] = 0;
        return mnemonic;
    }

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
    public int getAdvanceWidth(char ch) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        //BOGUS
        return stringExtentInPixels(new String(new char[] { ch })).x;
    }

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
    public boolean getAdvanced() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.cairo != 0;
    }

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
    public int getAlpha() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.alpha;
    }

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
    public int getAntialias() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (data.cairo == 0)
            return SWT.DEFAULT;
        return SWT.DEFAULT;
    }

    /**
     * Returns the background color.
     *
     * @return the receiver's background color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public Color getBackground() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return this.background;
    }

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
    public Pattern getBackgroundPattern() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.backgroundPattern;
    }

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
    public int getCharWidth(char ch) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        //BOGUS
        return stringExtentInPixels(new String(new char[] { ch })).x;
    }

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
    public Rectangle getClipping() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return getClippingInPixels();
    }

    Rectangle getClippingInPixels() {
        /* Calculate visible bounds in device space */
        int x = 0, y = 0, width = 0, height = 0;
        int[] w = new int[1], h = new int[1];
        getSize(w, h);
        width = w[0];
        height = h[0];
        /* Intersect visible bounds with clipping in device space and then convert then to user space */
        long cairo = data.cairo;
        long clipRgn = data.clipRgn;
        long damageRgn = data.damageRgn;
        if (clipRgn != 0 || damageRgn != 0 || cairo != 0) {
            if (damageRgn != 0) {
            }
            /* Intersect visible bounds with clipping */
            if (clipRgn != 0) {
                /* Convert clipping to device space if needed */
                if (!Arrays.equals(data.clippingTransform, currentTransform)) {
                    double[] clippingTransform;
                    if (currentTransform != null && data.clippingTransform == null) {
                        /*
					 * User actions in this case are:
					 * 1. Set clipping.
					 * 2. Set a transformation B.
		             *
					 * The clipping was specified before transformation B was set.
					 * So to convert it to the new space, we just invert the transformation B.
					 */
                        clippingTransform = currentTransform.clone();
                    } else if (currentTransform != null && data.clippingTransform != null) {
                        /*
					 * User actions in this case are:
					 * 1. Set a transformation A.
					 * 2. Set clipping.
					 * 3. Set a different transformation B. This is global and wipes out transformation A.
					 *
					 * Since step 3. wipes out transformation A, we must apply A on the clipping rectangle to have
					 * the correct clipping rectangle after transformation A is wiped.
					 * Then, we apply the inverted transformation B on the resulting clipping,
					 * to convert it to the new space (which results after applying B).
					 */
                        clippingTransform = new double[6];
                    } else {
                        /*
					 * User actions in this case are:
					 * 1. Set a transformation A.
					 * 2. Set clipping.
					 * 3. Wipe the transformation A (i.e. call GC.setTransformation(A)).
					 *
					 * We must apply transformation A on the clipping, to convert it to the new space.
					 */
                        clippingTransform = data.clippingTransform.clone();
                    }
                    clipRgn = convertRgn(clipRgn, clippingTransform);
                } else {
                }
            }
        }
        return new Rectangle(x, y, width, height);
    }

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
    public void getClipping(Region region) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (region == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (region.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        long clipRgn = data.clipRgn;
        if (clipRgn == 0) {
            int[] width = new int[1], height = new int[1];
            getSize(width, height);
        } else {
        }
        if (data.damageRgn != 0) {
        }
    }

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
    public int getFillRule() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        long cairo = data.cairo;
        if (cairo == 0)
            return SWT.FILL_EVEN_ODD;
        return this.fillRule;
    }

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
    public Font getFont() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.font;
    }

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
    public FontMetrics getFontMetrics() {
        return new FontMetrics();
    }

    /**
     * Returns the receiver's foreground color.
     *
     * @return the color used for drawing foreground things
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public Color getForeground() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_WIDGET_DISPOSED);
        return this.foreground;
    }

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
    public Pattern getForegroundPattern() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.foregroundPattern;
    }

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
    public GCData getGCData() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data;
    }

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
    public int getInterpolation() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.interpolation;
    }

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
    public LineAttributes getLineAttributes() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        LineAttributes attributes = getLineAttributesInPixels();
        attributes.width = attributes.width;
        return attributes;
    }

    LineAttributes getLineAttributesInPixels() {
        float[] dashes = null;
        if (data.lineDashes != null) {
            dashes = new float[data.lineDashes.length];
            System.arraycopy(data.lineDashes, 0, dashes, 0, dashes.length);
        }
        return new LineAttributes(data.lineWidth, data.lineCap, data.lineJoin, data.lineStyle, dashes, data.lineDashesOffset, data.lineMiterLimit);
    }

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
    public int getLineCap() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.lineCap;
    }

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
    public int[] getLineDash() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (data.lineDashes == null)
            return null;
        int[] lineDashes = new int[data.lineDashes.length];
        for (int i = 0; i < lineDashes.length; i++) {
            lineDashes[i] = (int) data.lineDashes[i];
        }
        return lineDashes;
    }

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
    public int getLineJoin() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.lineJoin;
    }

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
    public int getLineStyle() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.lineStyle;
    }

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
    public int getLineWidth() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return getLineWidthInPixels();
    }

    int getLineWidthInPixels() {
        return (int) data.lineWidth;
    }

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
    public int getStyle() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.style;
    }

    void getSize(int[] width, int[] height) {
        if (data.width != -1 && data.height != -1) {
            width[0] = data.width;
            height[0] = data.height;
            return;
        }
        if (data.drawable != 0) {
            return;
        }
    }

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
    public int getTextAntialias() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (data.cairo == 0)
            return SWT.DEFAULT;
        if (data.context != 0) {
        }
        return SWT.DEFAULT;
    }

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
    public void getTransform(Transform transform) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (transform == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (transform.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        long cairo = data.cairo;
        if (cairo != 0) {
            /*
		 * The client wants to know the relative transformation they set for their widgets.
		 * They do not want to know about the global coordinates of their widget, which is contained in Cairo.cairo_get_matrix().
		 * So we return whatever the client specified with setTransform.
		 */
            if (currentTransform != null) {
                transform.handle = currentTransform.clone();
            } else {
                transform.handle = new double[] { 1.0, 0.0, 0.0, 1.0, 0.0, 0.0 };
            }
        } else {
            transform.setElements(1, 0, 0, 1, 0, 0);
        }
    }

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
    public boolean getXORMode() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.xorMode;
    }

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
    @Override
    public int hashCode() {
        if (drawable instanceof Canvas) {
            return drawable.hashCode();
        }
        return 0;
    }

    double[] identity() {
        double[] identity = new double[6];
        if ((data.style & SWT.MIRRORED) != 0) {
            int[] w = new int[1], h = new int[1];
            getSize(w, h);
        } else {
        }
        if (data.identity != null) {
        }
        return identity;
    }

    void init(Drawable drawable, GCData data, long gdkGC) {
        if (this.background == null)
            this.background = new Color(255, 255, 255);
        if (this.foreground == null)
            this.foreground = new Color(0, 0, 0);
        if (data.font != null)
            data.state &= ~FONT;
        if (data.font == null)
            this.font = data.font = Display.getCurrent().getSystemFont();
        Image image = data.image;
        if (image != null) {
            if (image.getImpl() instanceof DartImage) {
                ((DartImage) image.getImpl()).memGC = this.getApi();
            }
            if (image.getImpl() instanceof SwtImage) {
                ((SwtImage) image.getImpl()).memGC = this.getApi();
            }
            /*
		 * The transparent pixel mask might change when drawing on
		 * the image.  Destroy it so that it is regenerated when
		 * necessary.
		 */
            if (image.getImpl()._transparentPixel() != -1)
                ((DartImage) image.getImpl()).destroyMask();
        }
        this.drawable = drawable;
        this.data = data;
        if (drawable instanceof Canvas) {
            DartWidget widget = (DartWidget) ((Canvas) drawable).getImpl();
            if (widget != null) {
                this.bridge = widget.getBridge();
                this.display = widget.getDisplay();
                getApi().handle = 1;
            }
        }
        getApi().handle = 1;
        data.state &= ~(BACKGROUND | FOREGROUND | FONT | LINE_WIDTH | LINE_CAP | LINE_JOIN | LINE_STYLE | DRAW_OFFSET);
        setClipping(data.clipRgn);
        if ((data.style & SWT.MIRRORED) != 0) {
            // Don't overwrite the Cairo transformation matrix in GTK 3.14 and above; it contains a translation relative to the parent widget.
            int[] w = new int[1], h = new int[1];
            getSize(w, h);
        }
        if (cairoTransformationMatrix == null)
            cairoTransformationMatrix = new double[6];
        clipping = getClipping();
    }

    void computeStringSize() {
        int[] width = new int[1], height = new int[1];
        data.stringHeight = height[0];
        data.stringWidth = width[0];
    }

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
    public boolean isClipped() {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return data.clipRgn != 0;
    }

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
    @Override
    public boolean isDisposed() {
        return getApi().handle == 0;
    }

    boolean isIdentity(double[] matrix) {
        if (matrix == null)
            return true;
        return matrix[0] == 1 && matrix[1] == 0 && matrix[2] == 0 && matrix[3] == 1 && matrix[4] == 0 && matrix[5] == 0;
    }

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
    public void setAdvanced(boolean advanced) {
        boolean newValue = advanced;
        if (!java.util.Objects.equals(this.advanced, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        this.advanced = newValue;
        if (!advanced) {
            setAlpha(0xFF);
            setAntialias(SWT.DEFAULT);
            setBackgroundPattern(null);
            resetClipping();
            setForegroundPattern(null);
            setInterpolation(SWT.DEFAULT);
            setTextAntialias(SWT.DEFAULT);
            setTransform(null);
        }
    }

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
    public void setAlpha(int alpha) {
        int newValue = alpha;
        if (!java.util.Objects.equals(this.alpha, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (data.cairo == 0 && (alpha & 0xff) == 0xff)
            return;
        data.alpha = alpha & 0xff;
        this.alpha = newValue;
        data.state &= ~(BACKGROUND | FOREGROUND | BACKGROUND_BG);
    }

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
    public void setAntialias(int antialias) {
        int newValue = antialias;
        if (!java.util.Objects.equals(this.antialias, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (data.cairo == 0 && antialias == SWT.DEFAULT)
            return;
        switch(antialias) {
            case SWT.DEFAULT:
                break;
            case SWT.OFF:
                break;
            case SWT.ON:
                break;
            default:
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.antialias = newValue;
    }

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
    public void setBackground(Color color) {
        Color newValue = color;
        if (!java.util.Objects.equals(this.background, newValue)) {
            dirty();
        }
        this.background = newValue;
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (color == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (color.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        data.backgroundPattern = null;
        data.state &= ~(BACKGROUND | BACKGROUND_BG);
    }

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
    public void setBackgroundPattern(Pattern pattern) {
        Pattern newValue = pattern;
        if (!java.util.Objects.equals(this.backgroundPattern, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (pattern != null && pattern.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (data.cairo == 0 && pattern == null)
            return;
        if (data.backgroundPattern == pattern)
            return;
        data.backgroundPattern = pattern;
        this.backgroundPattern = newValue;
        data.state &= ~BACKGROUND;
    }

    /**
     * Intersects given clipping with original clipping of this context, so
     * that resulting clip does not allow to paint outside of the GC bounds.
     */
    private void limitClipping(long gcClipping) {
        Region clippingRegion = new Region();
        if (currentTransform != null) {
            // we want to apply GC clipping stored in init() as is, so we invert user transformations that may distort it
            double[] invertedCurrentTransform = currentTransform.clone();
            int[] clippingWithoutUserTransform = transformRectangle(invertedCurrentTransform, clipping);
            /* Bug 540908: limiting clipping is very slow if client uses a transformation
		 * Check if client transformation has no rotation, then use Region.add(Rectangle) as its much faster than Region.add(int[])
		 */
            if (hasNoRotation(invertedCurrentTransform)) {
                Rectangle rectangle = getTransformedClippingRectangle(clippingWithoutUserTransform);
                clippingRegion.add(rectangle);
            } else {
                clippingRegion.add(clippingWithoutUserTransform);
            }
        } else {
            clippingRegion.add(clipping);
        }
        clippingRegion.dispose();
    }

    /**
     * Transforms rectangle with given matrix
     *
     * @return transformed rectangle corner coordinates, with x,y order of points.
     */
    private static int[] transformRectangle(double[] affineTransformation, Rectangle rectangle) {
        Point[] endPoints = { new Point(rectangle.x, rectangle.y), new Point(rectangle.x + rectangle.width, rectangle.y), new Point(rectangle.x + rectangle.width, rectangle.y + rectangle.height), new Point(rectangle.x, rectangle.y + rectangle.height) };
        return transformPoints(affineTransformation, endPoints);
    }

    /**
     * Transforms x,y coordinate pairs with given matrix
     *
     * @return transformed x,y coordinates.
     */
    private static int[] transformPoints(double[] transformation, Point[] points) {
        int[] transformedPoints = new int[points.length * 2];
        double[] px = new double[1], py = new double[1];
        for (int i = 0; i < points.length; ++i) {
            px[0] = points[i].x;
            py[0] = points[i].y;
            transformedPoints[(i * 2) + 0] = (int) Math.round(px[0]);
            transformedPoints[(i * 2) + 1] = (int) Math.round(py[0]);
        }
        return transformedPoints;
    }

    private static boolean hasNoRotation(double[] matrix) {
        /* Indices in the matrix are:                     (m11 m12 d1)
	 * 0: m11, 1: m12, 2: m21, 3: m22, 4: d1, 5: d2   (m21 m22 d2)
	 */
        double m12 = matrix[1];
        double m21 = matrix[2];
        return m12 == 0.0 && m21 == 0.0;
    }

    /* input must be ordered the ordered points of a rectangle:
 * pointsArray = {x1, y1, x2, y2, x3, y3, x4, y4}
 * with lines (x1,y1) to (x2,y2), (x2,y2) to (x3,y3), (x3,y3) to (x4,y4), (x4,y4) to (x1,y1)
 * the lines must be parallel or orthogonal to the x resp. y axis
 */
    private static Rectangle getTransformedClippingRectangle(int[] pointsArray) {
        int x1 = pointsArray[0];
        int y1 = pointsArray[1];
        int x2 = pointsArray[2];
        int y2 = pointsArray[3];
        int x3 = pointsArray[4];
        int y3 = pointsArray[5];
        int x4 = pointsArray[6];
        int y4 = pointsArray[7];
        // (x,y) is bottom left corner, so we need minimum x and y coordinates
        int x = Math.min(Math.min(x1, x2), Math.min(x3, x4));
        int y = Math.min(Math.min(y1, y2), Math.min(y3, y4));
        int width = Math.abs(x1 - x2);
        int height = Math.abs(y1 - y4);
        Rectangle r = new Rectangle(x, y, width, height);
        return r;
    }

    void setClipping(long clipRgn) {
        dirty();
        if (clipRgn == 0) {
            if (data.clipRgn != 0) {
                data.clipRgn = 0;
            }
            data.clippingTransform = null;
        } else {
            if (currentTransform != null) {
                // store the current transformation, to use it when the user requests clipping bounds
                data.clippingTransform = currentTransform.clone();
            } else {
                data.clippingTransform = null;
            }
        }
    }

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
    public void setClipping(int x, int y, int width, int height) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        setClippingInPixels(x, y, width, height);
    }

    void setClippingInPixels(int x, int y, int width, int height) {
        dirty();
        if (width < 0) {
            x = x + width;
            width = -width;
        }
        if (height < 0) {
            y = y + height;
            height = -height;
        }
    }

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
    public void setClipping(Path path) {
        dirty();
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (path != null && path.isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        resetClipping();
        if (path != null) {
        }
    }

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
    public void setClipping(Rectangle rect) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        setClippingInPixels(rect);
    }

    void setClippingInPixels(Rectangle rect) {
        if (rect == null) {
            resetClipping();
        } else {
            setClippingInPixels(rect.x, rect.y, rect.width, rect.height);
        }
        if (!java.util.Objects.equals(this.clipping, rect)) {
            dirty();
        }
    }

    private void resetClipping() {
        /*
	 * Bug 531667: widgets paint over other widgets
	 *
	 * The Cairo handle is shared by all widgets, and GC.setClipping(0) allows painting outside the current GC area.
	 * So if we reset any custom clipping we still want to restrict GC operations with the initial GC clipping.
	 */
        setClipping(clipping);
    }

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
    public void setClipping(Region region) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (region != null && region.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (region != null) {
            setClipping(region.handle);
        } else {
            resetClipping();
        }
    }

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
    public void setFont(Font font) {
        font = GraphicsUtils.copyFont(font);
        Font newValue = font;
        if (!java.util.Objects.equals(this.font, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (font != null && font.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        data.font = font != null ? font : ((SwtDevice) data.device.getImpl()).systemFont;
        data.state &= ~FONT;
        this.font = newValue;
        data.stringWidth = data.stringHeight = -1;
    }

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
    public void setFillRule(int rule) {
        int newValue = rule;
        if (!java.util.Objects.equals(this.fillRule, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        switch(rule) {
            case SWT.FILL_WINDING:
                break;
            case SWT.FILL_EVEN_ODD:
                break;
            default:
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.fillRule = newValue;
    }

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
    public void setForeground(Color color) {
        Color newValue = color;
        if (!java.util.Objects.equals(this.foreground, newValue)) {
            dirty();
        }
        this.foreground = newValue;
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (color == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (color.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        data.foregroundPattern = null;
        data.state &= ~FOREGROUND;
    }

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
    public void setForegroundPattern(Pattern pattern) {
        Pattern newValue = pattern;
        if (!java.util.Objects.equals(this.foregroundPattern, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (pattern != null && pattern.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (data.cairo == 0 && pattern == null)
            return;
        if (data.foregroundPattern == pattern)
            return;
        data.foregroundPattern = pattern;
        this.foregroundPattern = newValue;
        data.state &= ~FOREGROUND;
    }

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
    public void setInterpolation(int interpolation) {
        int newValue = interpolation;
        if (!java.util.Objects.equals(this.interpolation, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (data.cairo == 0 && interpolation == SWT.DEFAULT)
            return;
        switch(interpolation) {
            case SWT.DEFAULT:
            case SWT.NONE:
            case SWT.LOW:
            case SWT.HIGH:
                break;
            default:
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.interpolation = newValue;
        data.interpolation = interpolation;
    }

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
    public void setLineAttributes(LineAttributes attributes) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (attributes == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        attributes.width = attributes.width;
        setLineAttributesInPixels(attributes);
    }

    void setLineAttributesInPixels(LineAttributes attributes) {
        LineAttributes newValue = attributes;
        if (!java.util.Objects.equals(this.lineAttributes, newValue)) {
            dirty();
        }
        int mask = 0;
        float lineWidth = attributes.width;
        if (lineWidth != data.lineWidth) {
            mask |= LINE_WIDTH | DRAW_OFFSET;
        }
        int lineStyle = attributes.style;
        if (lineStyle != data.lineStyle) {
            mask |= LINE_STYLE;
            switch(lineStyle) {
                case SWT.LINE_SOLID:
                case SWT.LINE_DASH:
                case SWT.LINE_DOT:
                case SWT.LINE_DASHDOT:
                case SWT.LINE_DASHDOTDOT:
                    break;
                case SWT.LINE_CUSTOM:
                    if (attributes.dash == null)
                        lineStyle = SWT.LINE_SOLID;
                    break;
                default:
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
        }
        int join = attributes.join;
        if (join != data.lineJoin) {
            mask |= LINE_JOIN;
            switch(join) {
                case SWT.JOIN_MITER:
                case SWT.JOIN_ROUND:
                case SWT.JOIN_BEVEL:
                    break;
                default:
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
        }
        int cap = attributes.cap;
        if (cap != data.lineCap) {
            mask |= LINE_CAP;
            switch(cap) {
                case SWT.CAP_FLAT:
                case SWT.CAP_ROUND:
                case SWT.CAP_SQUARE:
                    break;
                default:
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
        }
        float[] dashes = attributes.dash;
        float[] lineDashes = data.lineDashes;
        if (dashes != null && dashes.length > 0) {
            boolean changed = lineDashes == null || lineDashes.length != dashes.length;
            for (int i = 0; i < dashes.length; i++) {
                float dash = dashes[i];
                if (dash <= 0)
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
                if (!changed && lineDashes[i] != dash)
                    changed = true;
            }
            if (changed) {
                float[] newDashes = new float[dashes.length];
                System.arraycopy(dashes, 0, newDashes, 0, dashes.length);
                dashes = newDashes;
                mask |= LINE_STYLE;
            } else {
                dashes = lineDashes;
            }
        } else {
            if (lineDashes != null && lineDashes.length > 0) {
                mask |= LINE_STYLE;
            } else {
                dashes = lineDashes;
            }
        }
        float dashOffset = attributes.dashOffset;
        if (dashOffset != data.lineDashesOffset) {
            mask |= LINE_STYLE;
        }
        float miterLimit = attributes.miterLimit;
        if (miterLimit != data.lineMiterLimit) {
            mask |= LINE_MITERLIMIT;
        }
        if (mask == 0)
            return;
        data.lineWidth = lineWidth;
        data.lineStyle = lineStyle;
        data.lineCap = cap;
        data.lineJoin = join;
        data.lineDashes = dashes;
        data.lineDashesOffset = dashOffset;
        data.lineMiterLimit = miterLimit;
        this.lineAttributes = newValue;
        data.state &= ~mask;
    }

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
    public void setLineCap(int cap) {
        int newValue = cap;
        if (!java.util.Objects.equals(this.lineCap, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (data.lineCap == cap)
            return;
        switch(cap) {
            case SWT.CAP_ROUND:
            case SWT.CAP_FLAT:
            case SWT.CAP_SQUARE:
                break;
            default:
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        data.lineCap = cap;
        this.lineCap = newValue;
        data.state &= ~LINE_CAP;
    }

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
    public void setLineDash(int[] dashes) {
        int[] newValue = dashes;
        if (!java.util.Objects.equals(this.lineDash, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        float[] lineDashes = data.lineDashes;
        if (dashes != null && dashes.length > 0) {
            boolean changed = data.lineStyle != SWT.LINE_CUSTOM || lineDashes == null || lineDashes.length != dashes.length;
            for (int i = 0; i < dashes.length; i++) {
                int dash = dashes[i];
                if (dash <= 0)
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
                if (!changed && lineDashes[i] != dash)
                    changed = true;
            }
            if (!changed)
                return;
            data.lineDashes = new float[dashes.length];
            for (int i = 0; i < dashes.length; i++) {
                data.lineDashes[i] = dashes[i];
            }
            data.lineStyle = SWT.LINE_CUSTOM;
        } else {
            if (data.lineStyle == SWT.LINE_SOLID && (lineDashes == null || lineDashes.length == 0))
                return;
            data.lineDashes = null;
            data.lineStyle = SWT.LINE_SOLID;
        }
        this.lineDash = newValue;
        data.state &= ~LINE_STYLE;
        this.lineDash = dashes;
    }

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
    public void setLineJoin(int join) {
        int newValue = join;
        if (!java.util.Objects.equals(this.lineJoin, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (data.lineJoin == join)
            return;
        switch(join) {
            case SWT.JOIN_MITER:
            case SWT.JOIN_ROUND:
            case SWT.JOIN_BEVEL:
                break;
            default:
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        data.lineJoin = join;
        this.lineJoin = newValue;
        data.state &= ~LINE_JOIN;
    }

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
    public void setLineStyle(int lineStyle) {
        int newValue = lineStyle;
        if (!java.util.Objects.equals(this.lineStyle, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (data.lineStyle == lineStyle)
            return;
        switch(lineStyle) {
            case SWT.LINE_SOLID:
            case SWT.LINE_DASH:
            case SWT.LINE_DOT:
            case SWT.LINE_DASHDOT:
            case SWT.LINE_DASHDOTDOT:
                break;
            case SWT.LINE_CUSTOM:
                if (data.lineDashes == null)
                    lineStyle = SWT.LINE_SOLID;
                break;
            default:
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        data.lineStyle = lineStyle;
        this.lineStyle = newValue;
        data.state &= ~LINE_STYLE;
    }

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
    public void setLineWidth(int lineWidth) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        setLineWidthInPixels(lineWidth);
    }

    void setLineWidthInPixels(int lineWidth) {
        int newValue = lineWidth;
        if (!java.util.Objects.equals(this.lineWidth, newValue)) {
            dirty();
        }
        if (data.lineWidth == lineWidth)
            return;
        data.lineWidth = lineWidth;
        this.lineWidth = newValue;
        data.state &= ~(LINE_WIDTH | DRAW_OFFSET);
    }

    void setString(String string, int flags) {
        if (data.layout == 0)
            createLayout();
        if (string == data.string && (flags & ~SWT.DRAW_TRANSPARENT) == (data.drawFlags & ~SWT.DRAW_TRANSPARENT)) {
            return;
        }
        int mnemonic, length = string.length();
        long layout = data.layout;
        char[] text = new char[length];
        string.getChars(0, length, text, 0);
        if ((flags & SWT.DRAW_MNEMONIC) != 0 && (mnemonic = fixMnemonic(text)) != -1) {
            char[] text1 = new char[mnemonic - 1];
            System.arraycopy(text, 0, text1, 0, text1.length);
            char[] text2 = new char[text.length - mnemonic];
            System.arraycopy(text, mnemonic - 1, text2, 0, text2.length);
        } else {
        }
        data.string = string;
        data.stringWidth = data.stringHeight = -1;
        data.drawFlags = flags;
    }

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
    public void setTextAntialias(int antialias) {
        int newValue = antialias;
        if (!java.util.Objects.equals(this.textAntialias, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (data.cairo == 0 && antialias == SWT.DEFAULT)
            return;
        switch(antialias) {
            case SWT.DEFAULT:
                break;
            case SWT.OFF:
                break;
            case SWT.ON:
                break;
            default:
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (data.context == 0)
            createLayout();
        this.textAntialias = newValue;
    }

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
    public void setTransform(Transform transform) {
        Transform newValue = transform;
        if (!java.util.Objects.equals(this.transform, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (transform != null && transform.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (data.cairo == 0 && transform == null)
            return;
        long cairo = data.cairo;
        // Re-set the original Cairo transformation matrix: it contains a translation relative to the parent widget.
        if (currentTransform != null) {
            currentTransform = null;
        }
        // Apply user transform on top of the current transformation matrix (and remember it)
        if (transform != null) {
            currentTransform = transform.handle.clone();
        }
        this.transform = newValue;
        data.state &= ~DRAW_OFFSET;
    }

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
    public void setXORMode(boolean xor) {
        boolean newValue = xor;
        if (!java.util.Objects.equals(this.XORMode, newValue)) {
            dirty();
        }
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        this.XORMode = newValue;
        data.xorMode = xor;
    }

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
    public Point stringExtent(String string) {
        return stringExtentInPixels(string);
    }

    Point stringExtentInPixels(String string) {
        return textExtentInPixels(string, 0);
    }

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
    public Point textExtent(String string) {
        return textExtentInPixels(string);
    }

    Point textExtentInPixels(String string) {
        return textExtentInPixels(string, SWT.DRAW_DELIMITER | SWT.DRAW_TAB);
    }

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
    public Point textExtent(String string, int flags) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (string == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return textExtentInPixels(string, flags);
    }

    Point textExtentInPixels(String string, int flags) {
        setString(string, flags);
        checkGC(FONT);
        if (data.stringWidth == -1) {
            computeStringSize();
        }
        return new Point(data.stringWidth, data.stringHeight);
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
            return "GC {*DISPOSED*}";
        return "GC {" + getApi().handle + "}";
    }

    boolean XORMode;

    boolean advanced;

    int alpha;

    int antialias;

    Color background;

    Pattern backgroundPattern;

    int fillRule;

    Font font;

    Color foreground;

    Pattern foregroundPattern;

    int interpolation;

    LineAttributes lineAttributes;

    int lineCap;

    int[] lineDash = new int[0];

    int lineJoin;

    int lineStyle;

    int lineWidth;

    int style;

    int textAntialias;

    Transform transform;

    public Drawable _drawable() {
        return drawable;
    }

    public GCData _data() {
        return data;
    }

    public boolean _XORMode() {
        return XORMode;
    }

    public boolean _advanced() {
        return advanced;
    }

    public int _alpha() {
        return alpha;
    }

    public int _antialias() {
        return antialias;
    }

    public Color _background() {
        return background;
    }

    public Pattern _backgroundPattern() {
        return backgroundPattern;
    }

    public int _fillRule() {
        return fillRule;
    }

    public Font _font() {
        return font;
    }

    public Color _foreground() {
        return foreground;
    }

    public Pattern _foregroundPattern() {
        return foregroundPattern;
    }

    public int _interpolation() {
        return interpolation;
    }

    public LineAttributes _lineAttributes() {
        return lineAttributes;
    }

    public int _lineCap() {
        return lineCap;
    }

    public int[] _lineDash() {
        return lineDash;
    }

    public int _lineJoin() {
        return lineJoin;
    }

    public int _lineStyle() {
        return lineStyle;
    }

    public int _lineWidth() {
        return lineWidth;
    }

    public int _style() {
        return style;
    }

    public int _textAntialias() {
        return textAntialias;
    }

    public Transform _transform() {
        return transform;
    }

    private Display display;

    public Display getDisplay() {
        return display;
    }

    public GC getApi() {
        if (api == null)
            api = GC.createApi(this);
        return (GC) api;
    }

    public VGC getValue() {
        if (value == null)
            value = new VGC(this);
        return (VGC) value;
    }
}
