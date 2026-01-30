/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
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
import java.util.List;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gdip.*;
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

    private final GCData originalData = new GCData();

    private final List<Operation> operations = new ArrayList<>();

    static final int FOREGROUND = 1 << 0;

    static final int BACKGROUND = 1 << 1;

    static final int FONT = 1 << 2;

    static final int LINE_STYLE = 1 << 3;

    static final int LINE_WIDTH = 1 << 4;

    static final int LINE_CAP = 1 << 5;

    static final int LINE_JOIN = 1 << 6;

    static final int LINE_MITERLIMIT = 1 << 7;

    static final int FOREGROUND_TEXT = 1 << 8;

    static final int BACKGROUND_TEXT = 1 << 9;

    static final int BRUSH = 1 << 10;

    static final int PEN = 1 << 11;

    static final int NULL_BRUSH = 1 << 12;

    static final int NULL_PEN = 1 << 13;

    static final int DRAW_OFFSET = 1 << 14;

    static final int DRAW = FOREGROUND | LINE_STYLE | LINE_WIDTH | LINE_CAP | LINE_JOIN | LINE_MITERLIMIT | PEN | NULL_BRUSH | DRAW_OFFSET;

    static final int FILL = BACKGROUND | BRUSH | NULL_PEN;

    static final float[] LINE_DOT_ZERO = new float[] { 3, 3 };

    static final float[] LINE_DASH_ZERO = new float[] { 18, 6 };

    static final float[] LINE_DASHDOT_ZERO = new float[] { 9, 6, 3, 6 };

    static final float[] LINE_DASHDOTDOT_ZERO = new float[] { 9, 3, 3, 3, 3, 3 };

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
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
        this(drawable, SWT.NONE, api);
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
        ((SwtGCData) data.getImpl()).copyTo(originalData);
        long hDC = drawable.internal_new_GC(data);
        Device device = data.device;
        if (device == null)
            device = SwtDevice.getDevice();
        if (device == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        this.device = data.device = device;
        init(drawable, data, hDC);
        init();
    }

    static int checkStyle(int style) {
        if ((style & SWT.LEFT_TO_RIGHT) != 0)
            style &= ~SWT.RIGHT_TO_LEFT;
        return style & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
    }

    private void validateGCState() {
        if (drawable == null) {
            return;
        }
        try {
            GCData newData = new GCData();
            long newHdc = drawable.internal_new_GC(newData);
            if (data.nativeZoom != newData.nativeZoom) {
                System.err.println("***WARNING: Zoom of the underlying Drawable of the GC has changed. This indicates a " + "long running GC that should be recreated.");
            }
            drawable.internal_dispose_GC(newHdc, newData);
        } catch (Exception e) {
            // ignore if recreation fails
        }
    }

    void checkGC(int mask) {
        int state = data.state;
        if ((state & mask) == mask)
            return;
        state = (state ^ mask) & mask;
        data.state |= mask;
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            long pen = data.gdipPen;
            float width = data.lineWidth;
            if ((state & FOREGROUND) != 0 || (pen == 0 && (state & (LINE_WIDTH | LINE_STYLE | LINE_MITERLIMIT | LINE_JOIN | LINE_CAP)) != 0)) {
                data.gdipFgBrush = 0;
                Pattern pattern = data.foregroundPattern;
                if (pattern != null) {
                    if (data.alpha == 0xFF) {
                    } else {
                    }
                    if ((data.style & SWT.MIRRORED) != 0) {
                    }
                } else {
                }
                if (pen != 0) {
                } else {
                }
            }
            if ((state & LINE_WIDTH) != 0) {
                switch(data.lineStyle) {
                    case SWT.LINE_CUSTOM:
                        state |= LINE_STYLE;
                }
            }
            if ((state & LINE_STYLE) != 0) {
                float[] dashes = null;
                float dashOffset = 0;
                switch(data.lineStyle) {
                    case SWT.LINE_SOLID:
                        break;
                    case SWT.LINE_DOT:
                        if (width == 0)
                            dashes = LINE_DOT_ZERO;
                        break;
                    case SWT.LINE_DASH:
                        if (width == 0)
                            dashes = LINE_DASH_ZERO;
                        break;
                    case SWT.LINE_DASHDOT:
                        if (width == 0)
                            dashes = LINE_DASHDOT_ZERO;
                        break;
                    case SWT.LINE_DASHDOTDOT:
                        if (width == 0)
                            dashes = LINE_DASHDOTDOT_ZERO;
                        break;
                    case SWT.LINE_CUSTOM:
                        {
                            if (data.lineDashes != null) {
                                dashOffset = data.lineDashesOffset / Math.max(1, width);
                                dashes = new float[data.lineDashes.length * 2];
                                for (int i = 0; i < data.lineDashes.length; i++) {
                                    float dash = data.lineDashes[i] / Math.max(1, width);
                                    dashes[i] = dash;
                                    dashes[i + data.lineDashes.length] = dash;
                                }
                            }
                        }
                }
                if (dashes != null) {
                } else {
                }
            }
            if ((state & LINE_MITERLIMIT) != 0) {
            }
            if ((state & LINE_JOIN) != 0) {
                int joinStyle = 0;
                switch(data.lineJoin) {
                    case SWT.JOIN_MITER:
                        break;
                    case SWT.JOIN_BEVEL:
                        break;
                    case SWT.JOIN_ROUND:
                        break;
                }
            }
            if ((state & LINE_CAP) != 0) {
                int capStyle = 0;
                switch(data.lineCap) {
                    case SWT.CAP_FLAT:
                        break;
                    case SWT.CAP_ROUND:
                        break;
                    case SWT.CAP_SQUARE:
                        break;
                }
            }
            if ((state & BACKGROUND) != 0) {
                data.gdipBgBrush = 0;
                Pattern pattern = data.backgroundPattern;
                if (pattern != null) {
                    if (data.alpha == 0xFF) {
                        data.gdipBrush = ((SwtPattern) pattern.getImpl()).getHandle(getZoom());
                    } else {
                    }
                    if ((data.style & SWT.MIRRORED) != 0) {
                    }
                } else {
                }
            }
            if ((state & FONT) != 0) {
                long[] hFont = new long[1];
                data.hGDIFont = hFont[0];
            }
            if ((state & DRAW_OFFSET) != 0) {
                int effectiveLineWidth = data.lineWidth < 1 ? 1 : Math.round(data.lineWidth);
                if (effectiveLineWidth % 2 == 1) {
                    // In case the effective line width is odd, shift coordinates by (0.5, 0.5).
                    // I.e., a line starting at (0,0) will effectively start in the pixel right
                    // The offset will be applied to the coordinate system of the GC; so transform
                    // it from the drawing coordinate system to the coordinate system of the GC by
                    // applying the inverse transformation as the one applied to the GC and correct
                } else {
                    data.gdipXOffset = data.gdipYOffset = 0;
                }
            }
            return;
        }
        if ((state & (FOREGROUND | LINE_CAP | LINE_JOIN | LINE_STYLE | LINE_WIDTH)) != 0) {
            int width = (int) data.lineWidth;
            int[] dashes = null;
            switch(data.lineStyle) {
                case SWT.LINE_SOLID:
                    break;
                case SWT.LINE_DASH:
                    break;
                case SWT.LINE_DOT:
                    break;
                case SWT.LINE_DASHDOT:
                    break;
                case SWT.LINE_DASHDOTDOT:
                    break;
                case SWT.LINE_CUSTOM:
                    {
                        if (data.lineDashes != null) {
                            dashes = new int[data.lineDashes.length];
                            for (int i = 0; i < dashes.length; i++) {
                                dashes[i] = (int) data.lineDashes[i];
                            }
                        }
                        break;
                    }
            }
            if ((state & LINE_STYLE) != 0) {
            }
            int joinStyle = 0;
            switch(data.lineJoin) {
                case SWT.JOIN_MITER:
                    break;
                case SWT.JOIN_ROUND:
                    break;
                case SWT.JOIN_BEVEL:
                    break;
            }
            int capStyle = 0;
            switch(data.lineCap) {
                case SWT.CAP_ROUND:
                    break;
                case SWT.CAP_FLAT:
                    break;
                case SWT.CAP_SQUARE:
                    break;
            }
            data.state |= PEN;
            data.state &= ~NULL_PEN;
        } else if ((state & PEN) != 0) {
            data.state &= ~NULL_PEN;
        } else if ((state & NULL_PEN) != 0) {
            data.state &= ~PEN;
        }
        if ((state & BACKGROUND) != 0) {
            data.state |= BRUSH;
            data.state &= ~NULL_BRUSH;
        } else if ((state & BRUSH) != 0) {
            data.state &= ~NULL_BRUSH;
        } else if ((state & NULL_BRUSH) != 0) {
            data.state &= ~BRUSH;
        }
        if ((state & BACKGROUND_TEXT) != 0) {
        }
        if ((state & FOREGROUND_TEXT) != 0) {
        }
        if ((state & FONT) != 0) {
        }
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

    private abstract class ImageOperation extends Operation {

        private Image image;

        ImageOperation(Image image) {
            setImage(image);
            ((DartImage) image.getImpl()).addOnDisposeListener(this::setCopyOfImage);
        }

        private void setImage(Image image) {
            this.image = image;
        }

        private void setCopyOfImage(Image image) {
            if (!DartGC.this.getApi().isDisposed()) {
                Image copiedImage = new Image(image.getImpl()._device(), image, SWT.IMAGE_COPY);
                setImage(copiedImage);
                registerForDisposal(copiedImage);
            }
        }

        protected Image getImage() {
            return image;
        }
    }

    private class CopyAreaToImageOperation extends ImageOperation {

        private final int x;

        private final int y;

        CopyAreaToImageOperation(Image image, int x, int y) {
            super(image);
            this.x = x;
            this.y = y;
        }

        @Override
        void apply() {
        }
    }

    private void copyAreaInPixels(Image image, int x, int y) {
        if (image.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
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

    private class CopyAreaOperation extends Operation {

        private final Rectangle source;

        private final Rectangle destination;

        private final boolean paint;

        CopyAreaOperation(Rectangle source, Rectangle destination, boolean paint) {
            this.source = source;
            this.destination = destination;
            this.paint = paint;
        }

        @Override
        void apply() {
        }
    }

    private void copyAreaInPixels(int srcX, int srcY, int width, int height, int destX, int destY, boolean paint) {
        checkNonDisposed();
        long hwnd = data.hwnd;
        if (hwnd == 0) {
        } else {
        }
    }

    /**
     * Create a new brush with transparency from the image in {@link brush}.
     *
     * The returned brush has to be disposed by the caller.
     *
     * @param brush Brush with pattern
     * @return new brush with transparency
     * @exception SWTError <ul>
     *    <li>ERROR_CANNOT_BE_ZERO - if the image in the brush is null</li>
     *    <li>ERROR_NO_HANDLES - if no handles are available to perform the operation</li>
     * </ul>
     */
    static long createAlphaTextureBrush(long brush, int alpha) {
        if (brush == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
        return 0;
    }

    /**
     * Disposes of the operating system resources associated with
     * the graphics context. Applications must dispose of all GCs
     * which they allocate.
     *
     * @exception SWTError <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS if not called from the thread that created the drawable</li>
     * </ul>
     */
    @Override
    void destroy() {
        boolean gdip = data.gdipGraphics != 0;
        if (gdip && (data.style & SWT.MIRRORED) != 0) {
        }
        /* Select stock pen and brush objects and free resources */
        if (data.hPen != 0) {
            data.hPen = 0;
        }
        if (data.hBrush != 0) {
            data.hBrush = 0;
        }
        /*
	* Put back the original bitmap into the device context.
	* This will ensure that we have not left a bitmap
	* selected in it when we delete the HDC.
	*/
        long hNullBitmap = data.hNullBitmap;
        if (hNullBitmap != 0) {
            data.hNullBitmap = 0;
        }
        Image image = data.image;
        if (image != null) {
            if (image.getImpl() instanceof DartImage) {
                ((DartImage) image.getImpl()).memGC = null;
            }
            if (image.getImpl() instanceof SwtImage) {
                ((SwtImage) image.getImpl()).memGC = null;
            }
        }
        /*
	* Dispose the HDC.
	*/
        if (drawable != null)
            drawable.internal_dispose_GC(getApi().handle, data);
        drawable = null;
        getApi().handle = 0;
        data.image = null;
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

    private class DrawArcOperation extends Operation {

        private final Rectangle rectangle;

        private final int startAngle;

        private final int arcAngle;

        DrawArcOperation(Rectangle rectangle, int startAngle, int arcAngle) {
            this.rectangle = rectangle;
            this.startAngle = startAngle;
            this.arcAngle = arcAngle;
        }

        @Override
        void apply() {
        }
    }

    private void drawArcInPixels(int x, int y, int width, int height, int startAngle, int arcAngle) {
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
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            if (width == height) {
            } else {
            }
            return;
        }
        if ((data.style & SWT.MIRRORED) != 0) {
            if (data.lineWidth != 0 && data.lineWidth % 2 == 0)
                x--;
        }
        int x1, y1, x2, y2, tmp;
        boolean isNegative;
        if (arcAngle >= 360 || arcAngle <= -360) {
            x1 = x2 = x + width;
            y1 = y2 = y + height / 2;
        } else {
            isNegative = arcAngle < 0;
            arcAngle = arcAngle + startAngle;
            if (isNegative) {
                // swap angles
                tmp = startAngle;
                startAngle = arcAngle;
                arcAngle = tmp;
            }
            x1 = cos(startAngle, width) + x + width / 2;
            y1 = -1 * sin(startAngle, height) + y + height / 2;
            x2 = cos(arcAngle, width) + x + width / 2;
            y2 = -1 * sin(arcAngle, height) + y + height / 2;
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

    private class DrawFocusOperation extends Operation {

        private final Rectangle rectangle;

        DrawFocusOperation(Rectangle rectangle) {
            this.rectangle = rectangle;
        }

        @Override
        void apply() {
        }
    }

    private void drawFocusInPixels(int x, int y, int width, int height) {
        data.focusDrawn = true;
        int state = 0;
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            long clipRgn = 0;
            float[] lpXform = null;
            if (lpXform != null) {
            }
            if (clipRgn != 0) {
            }
        }
        if (gdipGraphics != 0) {
        } else {
            data.state &= ~(BACKGROUND_TEXT | FOREGROUND_TEXT);
        }
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

    private class DrawImageOperation extends ImageOperation {

        private final Point location;

        DrawImageOperation(Image image, Point location) {
            super(image);
            this.location = location;
        }

        @Override
        void apply() {
        }

        private void drawImageInPixels(Image image, Point location) {
            if (image.isDisposed())
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            drawImage(image, 0, 0, -1, -1, location.x, location.y, -1, -1, true, getZoom());
        }
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
        storeAndApplyOperationForExistingHandle(new DrawImageToImageOperation(srcImage, new Rectangle(srcX, srcY, srcWidth, srcHeight), new Rectangle(destX, destY, destWidth, destHeight), simple));
    }

    private class DrawScalingImageToImageOperation extends ImageOperation {

        private final Rectangle source;

        private final Rectangle destination;

        DrawScalingImageToImageOperation(Image image, Rectangle source, Rectangle destination) {
            super(image);
            this.source = source;
            this.destination = destination;
        }

        @Override
        void apply() {
            int gcZoom = getZoom();
            int srcImageZoom = calculateZoomForImage(gcZoom, source.width, source.height, destination.width, destination.height);
            drawImage(getImage(), source.x, source.y, source.width, source.height, destination.x, destination.y, destination.width, destination.height, gcZoom, srcImageZoom);
        }

        private Collection<Integer> getAllCurrentMonitorZooms() {
            if (device instanceof Display display) {
                return Arrays.stream(display.getMonitors()).map(Monitor::getZoom).collect(Collectors.toSet());
            }
            return Collections.emptySet();
        }

        private int calculateZoomForImage(int gcZoom, int srcWidth, int srcHeight, int destWidth, int destHeight) {
            if (srcWidth == 1 && srcHeight == 1) {
                // One pixel images can use the GC zoom
                return gcZoom;
            }
            if (destWidth == srcWidth && destHeight == srcHeight) {
                // unscaled images can use the GC zoom
                return gcZoom;
            }
            float imageScaleFactor = 1f * destWidth / srcWidth;
            int imageZoom = Math.round(gcZoom * imageScaleFactor);
            if (getAllCurrentMonitorZooms().contains(imageZoom)) {
                return imageZoom;
            }
            if (imageZoom > 150) {
                return 200;
            }
            return 100;
        }
    }

    private void drawImage(Image image, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, int imageZoom, int scaledImageZoom) {
        if (scaledImageZoom != 100) {
        }
    }

    private class DrawImageToImageOperation extends ImageOperation {

        private final Rectangle source;

        private final Rectangle destination;

        private final boolean simple;

        DrawImageToImageOperation(Image image, Rectangle source, Rectangle destination, boolean simple) {
            super(image);
            this.source = source;
            this.destination = destination;
            this.simple = simple;
        }

        @Override
        void apply() {
            drawImage(getImage(), source.x, source.y, source.width, source.height, destination.x, destination.y, destination.width, destination.height, simple, getZoom());
        }
    }

    private void drawImage(Image srcImage, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, boolean simple, int imageZoom) {
        if (data.gdipGraphics != 0) {
            if (simple) {
            } else {
            }
            if (data.alpha != 0xFF) {
            }
            if ((data.style & SWT.MIRRORED) != 0) {
            }
            if ((data.style & SWT.MIRRORED) != 0) {
            }
            return;
        }
        long imageHandle = ((DartImage) srcImage.getImpl()).getHandle(imageZoom, data.nativeZoom);
        switch(srcImage.type) {
            case SWT.BITMAP:
                drawBitmap(srcImage, imageHandle, srcX, srcY, srcWidth, srcHeight, destX, destY, destWidth, destHeight, simple);
                break;
            case SWT.ICON:
                drawIcon(imageHandle, srcX, srcY, srcWidth, srcHeight, destX, destY, destWidth, destHeight, simple);
                break;
        }
    }

    private void drawIcon(long imageHandle, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, boolean simple) {
        if (simple) {
        }
    }

    private void drawBitmap(Image srcImage, long imageHandle, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, boolean simple) {
        if (simple) {
        } else {
        }
        boolean mustRestore = false;
        GC memGC = srcImage.getImpl()._memGC();
        if (memGC != null && !memGC.isDisposed()) {
            ((DartGC) memGC.getImpl()).flush();
            mustRestore = true;
            GCData data = memGC.getImpl()._data();
            if (data.hNullBitmap != 0) {
                data.hNullBitmap = 0;
            }
        }
        if (mustRestore) {
        }
    }

    private void drawBitmapAlpha(long imageHandle, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, boolean simple) {
        boolean alphaBlendSupport = true;
        if (alphaBlendSupport) {
            return;
        }
        /* Check clipping */
        Rectangle rect = getClippingInPixels();
        rect = rect.intersection(new Rectangle(destX, destY, destWidth, destHeight));
        if (rect.isEmpty())
            return;
        /*
	* Optimization.  Recalculate src and dest rectangles so that
	* only the clipping area is drawn.
	*/
        int sx1 = srcX + (((rect.x - destX) * srcWidth) / destWidth);
        int sx2 = srcX + ((((rect.x + rect.width) - destX) * srcWidth) / destWidth);
        int sy1 = srcY + (((rect.y - destY) * srcHeight) / destHeight);
        int sy2 = srcY + ((((rect.y + rect.height) - destY) * srcHeight) / destHeight);
        destX = rect.x;
        destY = rect.y;
        destWidth = rect.width;
        destHeight = rect.height;
        srcX = sx1;
        srcY = sy1;
        srcWidth = Math.max(1, sx2 - sx1);
        srcHeight = Math.max(1, sy2 - sy1);
        long memDib = DartImage.createDIB(Math.max(srcWidth, destWidth), Math.max(srcHeight, destHeight), 32);
        if (memDib == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
        int dp = 0;
        for (int y = 0; y < destHeight; ++y) {
            for (int x = 0; x < destWidth; ++x) {
                dp += 4;
            }
        }
    }

    private void drawBitmapMask(long srcColor, long srcMask, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, boolean simple, int imgWidth, int imgHeight, boolean offscreen) {
        int srcColorY = srcY;
        if (srcColor == 0) {
            srcColor = srcMask;
            srcColorY += imgHeight;
        }
        long destHdc = getApi().handle;
        int x = destX, y = destY;
        long tempHdc = 0;
        if (offscreen) {
            destHdc = tempHdc;
            x = y = 0;
        } else {
        }
        if (!simple && (srcWidth != destWidth || srcHeight != destHeight)) {
        } else {
        }
        if (offscreen) {
        } else {
        }
    }

    private void drawBitmapColor(long imageHandle, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, boolean simple) {
        if (!simple && (srcWidth != destWidth || srcHeight != destHeight)) {
        } else {
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

    private class DrawLineOperation extends Operation {

        private final Point start;

        private final Point end;

        DrawLineOperation(int x1, int y1, int x2, int y2) {
            this.start = new Point(x1, y1);
            this.end = new Point(x2, y2);
        }

        @Override
        void apply() {
        }
    }

    private void drawLineInPixels(int x1, int y1, int x2, int y2) {
        checkGC(DRAW);
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            return;
        }
        if ((data.style & SWT.MIRRORED) != 0) {
            if (data.lineWidth != 0 && data.lineWidth % 2 == 0) {
                x1--;
                x2--;
            }
        }
        if (data.lineWidth <= 1) {
        }
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

    private class DrawOvalOperation extends Operation {

        private final Rectangle bounds;

        DrawOvalOperation(Rectangle bounds) {
            this.bounds = bounds;
        }

        @Override
        void apply() {
        }
    }

    private void drawOvalInPixels(int x, int y, int width, int height) {
        checkGC(DRAW);
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            return;
        }
        if ((data.style & SWT.MIRRORED) != 0) {
            if (data.lineWidth != 0 && data.lineWidth % 2 == 0)
                x--;
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
        checkNonDisposed();
        if (path == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (path.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        storeAndApplyOperationForExistingHandle(new DrawPathOperation(path));
    }

    private class DrawPathOperation extends Operation {

        private final PathData pathData;

        DrawPathOperation(Path path) {
            this.pathData = path.getPathData();
        }

        @Override
        void apply() {
            Path path = new Path(device, pathData);
            try {
                long pathHandle = ((SwtPath) path.getImpl()).getHandle(getZoom());
                if (pathHandle == 0)
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
                checkGC(DRAW);
            } finally {
                path.dispose();
            }
        }
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

    private class DrawPointOperation extends Operation {

        private final Point location;

        DrawPointOperation(int x, int y) {
            this.location = new Point(x, y);
        }

        @Override
        void apply() {
        }
    }

    private void drawPointInPixels(int x, int y) {
        if (data.gdipGraphics != 0) {
            checkGC(DRAW);
            return;
        }
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

    private class DrawPolygonOperation extends Operation {

        private final int[] pointArray;

        DrawPolygonOperation(int[] pointArray) {
            this.pointArray = pointArray;
        }

        @Override
        void apply() {
        }
    }

    private void drawPolygonInPixels(int[] pointArray) {
        checkGC(DRAW);
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            return;
        }
        if ((data.style & SWT.MIRRORED) != 0) {
            if (data.lineWidth != 0 && data.lineWidth % 2 == 0) {
                for (int i = 0; i < pointArray.length; i += 2) {
                    pointArray[i]--;
                }
            }
        }
        if ((data.style & SWT.MIRRORED) != 0) {
            if (data.lineWidth != 0 && data.lineWidth % 2 == 0) {
                for (int i = 0; i < pointArray.length; i += 2) {
                    pointArray[i]++;
                }
            }
        }
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

    private class DrawPolylineOperation extends Operation {

        private final int[] pointArray;

        DrawPolylineOperation(int[] pointArray) {
            this.pointArray = pointArray;
        }

        @Override
        void apply() {
        }
    }

    private void drawPolylineInPixels(int[] pointArray) {
        checkGC(DRAW);
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            return;
        }
        if ((data.style & SWT.MIRRORED) != 0) {
            if (data.lineWidth != 0 && data.lineWidth % 2 == 0) {
                for (int i = 0; i < pointArray.length; i += 2) {
                    pointArray[i]--;
                }
            }
        }
        int length = pointArray.length;
        if (length >= 2) {
            if (data.lineWidth <= 1) {
            }
        }
        if ((data.style & SWT.MIRRORED) != 0) {
            if (data.lineWidth != 0 && data.lineWidth % 2 == 0) {
                for (int i = 0; i < pointArray.length; i += 2) {
                    pointArray[i]++;
                }
            }
        }
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

    private class DrawRectangleOperation extends Operation {

        private final Rectangle rectangle;

        DrawRectangleOperation(Rectangle rectangle) {
            this.rectangle = rectangle;
        }

        @Override
        void apply() {
        }
    }

    private void drawRectangleInPixels(int x, int y, int width, int height) {
        checkGC(DRAW);
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            if (width < 0) {
                x = x + width;
                width = -width;
            }
            if (height < 0) {
                y = y + height;
                height = -height;
            }
            return;
        }
        if ((data.style & SWT.MIRRORED) != 0) {
            /*
		* Note that Rectangle() subtracts one pixel in MIRRORED mode when
		* the pen was created with CreatePen() and its width is 0 or 1.
		*/
            if (data.lineWidth > 1) {
                if ((data.lineWidth % 2) == 1)
                    x++;
            } else {
            }
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

    private class DrawRoundRectangleOperation extends Operation {

        private final Rectangle rectangle;

        private final int arcWidth;

        private final int arcHeight;

        DrawRoundRectangleOperation(Rectangle rectangle, int arcWidth, int arcHeight) {
            this.rectangle = rectangle;
            this.arcWidth = arcWidth;
            this.arcHeight = arcHeight;
        }

        @Override
        void apply() {
        }
    }

    private void drawRoundRectangleInPixels(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        checkGC(DRAW);
        if (data.gdipGraphics != 0) {
            return;
        }
        if ((data.style & SWT.MIRRORED) != 0) {
            if (data.lineWidth != 0 && data.lineWidth % 2 == 0)
                x--;
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

    private class DrawStringOperation extends Operation {

        private final String string;

        private final Point location;

        private final boolean isTransparent;

        DrawStringOperation(String string, Point location, boolean isTransparent) {
            this.string = string;
            this.location = location;
            this.isTransparent = isTransparent;
        }

        @Override
        void apply() {
        }
    }

    private void drawStringInPixels(String string, int x, int y, boolean isTransparent) {
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            checkGC(FONT | FOREGROUND | (isTransparent ? 0 : BACKGROUND));
            return;
        }
        checkGC(FONT | FOREGROUND_TEXT | BACKGROUND_TEXT);
        if ((data.style & SWT.MIRRORED) != 0) {
            if (!isTransparent) {
            }
            x--;
        }
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

    private class DrawTextOperation extends Operation {

        private final String string;

        private final Point location;

        private final int flags;

        DrawTextOperation(String string, Point location, int flags) {
            this.string = string;
            this.location = location;
            this.flags = flags;
        }

        @Override
        void apply() {
        }
    }

    private void drawTextInPixels(String string, int x, int y, int flags) {
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            checkGC(FONT | FOREGROUND | ((flags & SWT.DRAW_TRANSPARENT) != 0 ? 0 : BACKGROUND));
            return;
        }
        checkGC(FONT | FOREGROUND_TEXT | BACKGROUND_TEXT);
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
        return (object == this.getApi()) || ((object instanceof GC) && (getApi().handle == ((GC) object).handle));
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

    private class FillArcOperation extends Operation {

        private final Rectangle bounds;

        private final int startAngle;

        private final int arcAngle;

        FillArcOperation(Rectangle bounds, int startAngle, int arcAngle) {
            this.bounds = bounds;
            this.startAngle = startAngle;
            this.arcAngle = arcAngle;
        }

        @Override
        void apply() {
        }
    }

    private void fillArcInPixels(int x, int y, int width, int height, int startAngle, int arcAngle) {
        checkNonDisposed();
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
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            if (width == height) {
            } else {
            }
            return;
        }
        if ((data.style & SWT.MIRRORED) != 0)
            x--;
        int x1, y1, x2, y2, tmp;
        boolean isNegative;
        if (arcAngle >= 360 || arcAngle <= -360) {
            x1 = x2 = x + width;
            y1 = y2 = y + height / 2;
        } else {
            isNegative = arcAngle < 0;
            arcAngle = arcAngle + startAngle;
            if (isNegative) {
                // swap angles
                tmp = startAngle;
                startAngle = arcAngle;
                arcAngle = tmp;
            }
            x1 = cos(startAngle, width) + x + width / 2;
            y1 = -1 * sin(startAngle, height) + y + height / 2;
            x2 = cos(arcAngle, width) + x + width / 2;
            y2 = -1 * sin(arcAngle, height) + y + height / 2;
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

    private class FillGradientRectangleOperation extends FillRectangleOperation {

        private final boolean vertical;

        FillGradientRectangleOperation(Rectangle rectangle, boolean vertical) {
            super(rectangle);
            this.vertical = vertical;
        }

        @Override
        void apply() {
        }
    }

    private void fillGradientRectangleInPixels(int x, int y, int width, int height, boolean vertical, int zoom) {
        if (width == 0 || height == 0)
            return;
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
        if (data.gdipGraphics != 0) {
            if (vertical) {
            } else {
            }
            return;
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

    private class FillOvalOperation extends Operation {

        private final Rectangle bounds;

        FillOvalOperation(Rectangle bounds) {
            this.bounds = bounds;
        }

        @Override
        void apply() {
        }
    }

    private void fillOvalInPixels(int x, int y, int width, int height) {
        checkNonDisposed();
        checkGC(FILL);
        if (data.gdipGraphics != 0) {
            return;
        }
        if ((data.style & SWT.MIRRORED) != 0)
            x--;
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
        checkNonDisposed();
        if (path == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (path.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        storeAndApplyOperationForExistingHandle(new FillPathOperation(path));
    }

    private class FillPathOperation extends Operation {

        private final PathData pathData;

        FillPathOperation(Path path) {
            this.pathData = path.getPathData();
        }

        @Override
        void apply() {
            Path path = new Path(device, pathData);
            try {
                long pathHandle = ((SwtPath) path.getImpl()).getHandle(getZoom());
                if (pathHandle == 0)
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
                checkGC(FILL);
            } finally {
                path.dispose();
            }
        }
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

    private class FillPolygonOperation extends Operation {

        private final int[] pointArray;

        FillPolygonOperation(int[] pointArray) {
            this.pointArray = pointArray;
        }

        @Override
        void apply() {
        }
    }

    private void fillPolygonInPixels(int[] pointArray) {
        checkNonDisposed();
        checkGC(FILL);
        if (data.gdipGraphics != 0) {
            return;
        }
        if ((data.style & SWT.MIRRORED) != 0) {
            for (int i = 0; i < pointArray.length; i += 2) {
                pointArray[i]--;
            }
        }
        if ((data.style & SWT.MIRRORED) != 0) {
            for (int i = 0; i < pointArray.length; i += 2) {
                pointArray[i]++;
            }
        }
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

    private class FillRectangleOperation extends Operation {

        protected final Rectangle rectangle;

        FillRectangleOperation(Rectangle rectangle) {
            this.rectangle = rectangle;
        }

        @Override
        void apply() {
        }
    }

    void fillRectangleInPixels(int x, int y, int width, int height) {
        checkNonDisposed();
        checkGC(FILL);
        if (data.gdipGraphics != 0) {
            if (width < 0) {
                x = x + width;
                width = -width;
            }
            if (height < 0) {
                y = y + height;
                height = -height;
            }
            return;
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

    private class FillRoundRectangleOperation extends Operation {

        private final Rectangle rectangle;

        private final int arcWidth;

        private final int arcHeight;

        FillRoundRectangleOperation(Rectangle rectangle, int arcWidth, int arcHeight) {
            this.rectangle = rectangle;
            this.arcWidth = arcWidth;
            this.arcHeight = arcHeight;
        }

        @Override
        void apply() {
        }
    }

    private void fillRoundRectangleInPixels(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        checkNonDisposed();
        checkGC(FILL);
        if (data.gdipGraphics != 0) {
            return;
        }
        if ((data.style & SWT.MIRRORED) != 0)
            x--;
    }

    private void flush() {
        if (data.gdipGraphics != 0) {
        }
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
        checkNonDisposed();
        checkGC(FONT);
        int[] width = new int[1];
        return width[0];
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
        checkNonDisposed();
        return data.gdipGraphics != 0;
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
        checkNonDisposed();
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
        checkNonDisposed();
        if (data.gdipGraphics == 0)
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
        checkNonDisposed();
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
        checkNonDisposed();
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
        checkNonDisposed();
        checkGC(FONT);
        return 0;
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
        return this.clipping;
    }

    Rectangle getClippingInPixels() {
        checkNonDisposed();
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
        }
        return this.clipping;
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
        checkNonDisposed();
        if (region == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (region.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        storeAndApplyOperationForExistingHandle(new GetClippingOperation(region));
    }

    private class GetClippingOperation extends Operation {

        private final Map<Integer, Long> zoomToRegionHandle = new HashMap<>();

        public GetClippingOperation(Region region) {
            ((SwtRegion) region.getImpl()).set(zoom -> {
                if (!zoomToRegionHandle.containsKey(zoom)) {
                    System.err.println("No clipping handle for zoom " + zoom + " has been created on this GC");
                    return zoomToRegionHandle.values().iterator().next();
                }
                return zoomToRegionHandle.get(zoom);
            }, getZoom());
        }

        // Whenever the GC handle is recalculated for a new zoom, we compute and store the clipping
        // at the times when getClipping(Region) was originally called, such that the region to which
        // that clipping is set can retrieve it from the storage when required.
        @Override
        void apply() {
            zoomToRegionHandle.computeIfAbsent(getZoom(), __ -> getClippingRegion());
        }

        @Override
        void disposeAll() {
            super.disposeAll();
        }
    }

    /**
     * @return a region handle with the current clipping region of this GC
     */
    private long getClippingRegion() {
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
        }
        return 0;
    }

    long getFgBrush() {
        return data.foregroundPattern != null ? ((SwtPattern) data.foregroundPattern.getImpl()).getHandle(getZoom()) : data.gdipFgBrush;
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
        checkNonDisposed();
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
        checkNonDisposed();
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
        checkNonDisposed();
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
        checkNonDisposed();
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
        checkNonDisposed();
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
        checkNonDisposed();
        if (data.gdipGraphics == 0)
            return SWT.DEFAULT;
        return SWT.DEFAULT;
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
        LineAttributes attributes = getLineAttributesInPixels();
        if (attributes.dash != null) {
        }
        return attributes;
    }

    LineAttributes getLineAttributesInPixels() {
        checkNonDisposed();
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
        checkNonDisposed();
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
        checkNonDisposed();
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
        checkNonDisposed();
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
        checkNonDisposed();
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
        return this.lineWidth;
    }

    int getLineWidthInPixels() {
        checkNonDisposed();
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
        checkNonDisposed();
        return data.style;
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
        checkNonDisposed();
        if (data.gdipGraphics == 0)
            return SWT.DEFAULT;
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
        checkNonDisposed();
        if (transform == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (transform.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
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
        checkNonDisposed();
        return this.XORMode;
    }

    private long identity() {
        if ((data.style & SWT.MIRRORED) != 0) {
        }
        return 0;
    }

    private void init(Drawable drawable, GCData data, long hDC) {
        if (this.background == null) {
            Color white = new Color(255, 255, 255);
            data.background = white.handle;
            this.background = white;
        }
        if (this.foreground == null) {
            Color black = new Color(0, 0, 0);
            data.foreground = black.handle;
            this.foreground = black;
        }
        if (hDC == 0)
            hDC = 1;
        int foreground = data.foreground;
        if (foreground != -1) {
            data.state &= ~(FOREGROUND | FOREGROUND_TEXT | PEN);
        } else {
        }
        int background = data.background;
        if (background != -1) {
            data.state &= ~(BACKGROUND | BACKGROUND_TEXT | BRUSH);
        } else {
        }
        data.state &= ~(NULL_BRUSH | NULL_PEN);
        if (data.nativeZoom == 0) {
            data.nativeZoom = extractZoom(hDC);
        }
        if (data.font != null) {
            data.state &= ~FONT;
        } else {
        }
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
        }
        int layout = data.layout;
        if (layout != -1) {
            if ((data.style & SWT.RIGHT_TO_LEFT) != 0)
                data.style |= SWT.MIRRORED;
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
        getApi().handle = hDC;
    }

    private static int extractZoom(long hDC) {
        return 0;
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
        checkNonDisposed();
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
        }
        return false;
    }

    private void checkNonDisposed() {
        if (isDisposed()) {
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        }
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

    private float measureSpace(long font, long format) {
        return 0;
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
        checkNonDisposed();
        this.advanced = newValue;
        storeAndApplyOperationForExistingHandle(new SetAdvancedOperation(advanced));
    }

    private class SetAdvancedOperation extends Operation {

        private final boolean advanced;

        SetAdvancedOperation(boolean advanced) {
            this.advanced = advanced;
        }

        @Override
        void apply() {
            if (advanced && data.gdipGraphics != 0)
                return;
            if (advanced) {
            } else {
                data.alpha = 0xFF;
                data.backgroundPattern = data.foregroundPattern = null;
                data.state = 0;
                setClipping(0);
                if ((data.style & SWT.MIRRORED) != 0) {
                }
            }
        }
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
        checkNonDisposed();
        this.antialias = newValue;
        storeAndApplyOperationForExistingHandle(new SetAntialiasOperation(antialias));
    }

    private class SetAntialiasOperation extends Operation {

        private final int antialias;

        SetAntialiasOperation(int antialias) {
            this.antialias = antialias;
        }

        @Override
        void apply() {
            if (data.gdipGraphics == 0 && antialias == SWT.DEFAULT)
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
        checkNonDisposed();
        this.alpha = newValue;
        storeAndApplyOperationForExistingHandle(new SetAlphaOperation(alpha));
    }

    private class SetAlphaOperation extends Operation {

        private final int alpha;

        SetAlphaOperation(int alpha) {
            this.alpha = alpha;
        }

        @Override
        void apply() {
            if (data.gdipGraphics == 0 && (alpha & 0xFF) == 0xFF)
                return;
            data.alpha = alpha & 0xFF;
            data.state &= ~(BACKGROUND | FOREGROUND);
            if (data.gdipFgPatternBrushAlpha != 0) {
                data.gdipFgPatternBrushAlpha = 0;
            }
            if (data.gdipBgPatternBrushAlpha != 0) {
                data.gdipBgPatternBrushAlpha = 0;
            }
        }
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
        checkNonDisposed();
        if (color == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (color.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        storeAndApplyOperationForExistingHandle(new SetBackgroundOperation(color));
    }

    private class SetBackgroundOperation extends Operation {

        private final Color color;

        SetBackgroundOperation(Color color) {
            RGB rgb = color.getRGB();
            this.color = new Color(color.getDevice(), rgb);
            registerForDisposal(this.color);
        }

        @Override
        void apply() {
            if (data.backgroundPattern == null && data.background == color.handle)
                return;
            data.backgroundPattern = null;
            data.background = color.handle;
            data.state &= ~(BACKGROUND | BACKGROUND_TEXT);
        }
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
        checkNonDisposed();
        if (pattern != null && pattern.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        this.backgroundPattern = newValue;
        storeAndApplyOperationForExistingHandle(new SetBackgroundPatternOperation(pattern));
    }

    private class SetBackgroundPatternOperation extends Operation {

        private final Pattern pattern;

        SetBackgroundPatternOperation(Pattern pattern) {
            this.pattern = pattern == null ? null : ((SwtPattern) pattern.getImpl()).copy();
            registerForDisposal(this.pattern);
        }

        @Override
        void apply() {
            if (data.gdipGraphics == 0 && pattern == null)
                return;
            if (data.backgroundPattern == pattern)
                return;
            data.backgroundPattern = pattern;
            data.state &= ~BACKGROUND;
            if (data.gdipBgPatternBrushAlpha != 0) {
                data.gdipBgPatternBrushAlpha = 0;
            }
        }
    }

    private void setClipping(long clipRgn) {
        checkNonDisposed();
        setClippingRegion(clipRgn);
    }

    private class SetClippingRegionOperation extends Operation {

        private final Region clipRgn;

        SetClippingRegionOperation(Region clipRgn) {
            this.clipRgn = clipRgn != null ? ((SwtRegion) clipRgn.getImpl()).copy() : null;
            registerForDisposal(this.clipRgn);
        }

        @Override
        void apply() {
        }
    }

    private void setClippingRegion(long hRgn) {
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            if (hRgn != 0) {
            } else {
            }
        } else {
            if (hRgn != 0) {
            }
            if (hRgn != 0) {
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
        dirty();
        Rectangle newValue = new Rectangle(x, y, width, height);
        checkNonDisposed();
        this.clipping = newValue;
        storeAndApplyOperationForExistingHandle(new SetClippingOperation(new Rectangle(x, y, width, height)));
    }

    private class SetClippingOperation extends Operation {

        private final Rectangle rectangle;

        SetClippingOperation(Rectangle rectangle) {
            this.rectangle = rectangle;
        }

        @Override
        void apply() {
        }
    }

    private void setClippingInPixels(int x, int y, int width, int height) {
        checkNonDisposed();
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
        Rectangle newValue = new Rectangle(clipping.x, clipping.y, clipping.width, clipping.height);
        checkNonDisposed();
        if (path != null && path.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        this.clipping = newValue;
        storeAndApplyOperationForExistingHandle(new SetClippingPathOperation(path));
    }

    private class SetClippingPathOperation extends Operation {

        private final PathData pathData;

        SetClippingPathOperation(Path path) {
            this.pathData = path == null ? null : path.getPathData();
        }

        @Override
        void apply() {
            setClipping(0);
            if (pathData != null) {
                Path path = new Path(device, pathData);
                path.dispose();
            }
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
        Rectangle newValue = rect;
        if (!java.util.Objects.equals(this.clipping, newValue)) {
            dirty();
        }
        checkNonDisposed();
        this.clipping = newValue;
        if (rect == null) {
            storeAndApplyOperationForExistingHandle(new SetClippingRegionOperation(null));
        } else {
            storeAndApplyOperationForExistingHandle(new SetClippingOperation(rect));
        }
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
        dirty();
        Rectangle newValue = new Rectangle(clipping.x, clipping.y, clipping.width, clipping.height);
        checkNonDisposed();
        if (region != null && region.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        this.clipping = newValue;
        storeAndApplyOperationForExistingHandle(new SetClippingRegionOperation(region));
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
        checkNonDisposed();
        this.fillRule = newValue;
        storeAndApplyOperationForExistingHandle(new SetFillRuleOperation(rule));
    }

    private class SetFillRuleOperation extends Operation {

        private final int rule;

        SetFillRuleOperation(int rule) {
            this.rule = rule;
        }

        @Override
        void apply() {
            switch(rule) {
                case SWT.FILL_WINDING:
                    break;
                case SWT.FILL_EVEN_ODD:
                    break;
                default:
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
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
        checkNonDisposed();
        if (font != null && font.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        this.font = newValue;
        storeAndApplyOperationForExistingHandle(new SetFontOperation(font));
    }

    private class SetFontOperation extends Operation {

        SetFontOperation(Font font) {
        }

        @Override
        void apply() {
            data.state &= ~FONT;
        }
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
        checkNonDisposed();
        if (color == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (color.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        storeAndApplyOperationForExistingHandle(new SetForegroundOperation(color));
    }

    private class SetForegroundOperation extends Operation {

        private final Color color;

        SetForegroundOperation(Color color) {
            RGB rgb = color.getRGB();
            this.color = new Color(color.getDevice(), rgb);
            registerForDisposal(this.color);
        }

        @Override
        void apply() {
            if (data.foregroundPattern == null && color.handle == data.foreground)
                return;
            data.foregroundPattern = null;
            data.foreground = color.handle;
            data.state &= ~(FOREGROUND | FOREGROUND_TEXT);
        }
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
        checkNonDisposed();
        if (pattern != null && pattern.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        this.foregroundPattern = newValue;
        storeAndApplyOperationForExistingHandle(new SetForegroundPatternOperation(pattern));
    }

    private class SetForegroundPatternOperation extends Operation {

        private final Pattern pattern;

        SetForegroundPatternOperation(Pattern pattern) {
            this.pattern = pattern == null ? null : ((SwtPattern) pattern.getImpl()).copy();
            registerForDisposal(this.pattern);
        }

        @Override
        void apply() {
            if (data.gdipGraphics == 0 && pattern == null)
                return;
            if (data.foregroundPattern == pattern)
                return;
            data.foregroundPattern = pattern;
            data.state &= ~FOREGROUND;
            if (data.gdipFgPatternBrushAlpha != 0) {
                data.gdipFgPatternBrushAlpha = 0;
            }
        }
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
        checkNonDisposed();
        this.interpolation = newValue;
        storeAndApplyOperationForExistingHandle(new SetInterpolationOperation(interpolation));
    }

    private class SetInterpolationOperation extends Operation {

        private final int interpolation;

        SetInterpolationOperation(int interpolation) {
            this.interpolation = interpolation;
        }

        @Override
        void apply() {
            if (data.gdipGraphics == 0 && interpolation == SWT.DEFAULT)
                return;
            switch(interpolation) {
                case SWT.DEFAULT:
                    break;
                case SWT.NONE:
                    break;
                case SWT.LOW:
                    break;
                case SWT.HIGH:
                    break;
                default:
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
        }
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
        LineAttributes newValue = attributes;
        if (!java.util.Objects.equals(this.lineAttributes, newValue)) {
            dirty();
        }
        if (attributes == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        checkNonDisposed();
        this.lineAttributes = newValue;
        storeAndApplyOperationForExistingHandle(new SetLineAttributesOperation(attributes));
    }

    private class SetLineAttributesOperation extends Operation {

        private final LineAttributes attributes;

        SetLineAttributesOperation(LineAttributes attributes) {
            this.attributes = new LineAttributes(attributes.width, attributes.cap, attributes.join, attributes.style, attributes.dash, attributes.dashOffset, attributes.miterLimit);
        }

        @Override
        void apply() {
            setLineAttributesInPixels(attributes);
        }
    }

    private void setLineAttributesInPixels(LineAttributes attributes) {
        checkNonDisposed();
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
                for (int i = 0; i < newDashes.length; i++) {
                }
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
        checkNonDisposed();
        this.lineCap = newValue;
        storeAndApplyOperationForExistingHandle(new SetLineCapOperation(cap));
    }

    private class SetLineCapOperation extends Operation {

        private final int cap;

        SetLineCapOperation(int cap) {
            this.cap = cap;
        }

        @Override
        void apply() {
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
            data.state &= ~LINE_CAP;
        }
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
        checkNonDisposed();
        this.lineDash = newValue;
        storeAndApplyOperationForExistingHandle(new SetLineDashOperation(dashes));
        this.lineDash = dashes;
    }

    private class SetLineDashOperation extends Operation {

        private final int[] dashes;

        SetLineDashOperation(int[] dashes) {
            this.dashes = dashes == null ? null : Arrays.copyOf(dashes, dashes.length);
        }

        @Override
        void apply() {
            float[] lineDashes = data.lineDashes;
            if (dashes != null && dashes.length > 0) {
                boolean changed = data.lineStyle != SWT.LINE_CUSTOM || lineDashes == null || lineDashes.length != dashes.length;
                float[] newDashes = new float[dashes.length];
                for (int i = 0; i < dashes.length; i++) {
                    if (dashes[i] <= 0)
                        SWT.error(SWT.ERROR_INVALID_ARGUMENT);
                    if (!changed && lineDashes[i] != newDashes[i])
                        changed = true;
                }
                if (!changed)
                    return;
                data.lineDashes = newDashes;
                data.lineStyle = SWT.LINE_CUSTOM;
            } else {
                if (data.lineStyle == SWT.LINE_SOLID && (lineDashes == null || lineDashes.length == 0))
                    return;
                data.lineDashes = null;
                data.lineStyle = SWT.LINE_SOLID;
            }
            data.state &= ~LINE_STYLE;
        }
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
        checkNonDisposed();
        this.lineJoin = newValue;
        storeAndApplyOperationForExistingHandle(new SetLineJoinOperation(join));
    }

    private class SetLineJoinOperation extends Operation {

        private final int join;

        SetLineJoinOperation(int join) {
            this.join = join;
        }

        @Override
        void apply() {
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
            data.state &= ~LINE_JOIN;
        }
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
        checkNonDisposed();
        this.lineStyle = newValue;
        storeAndApplyOperationForExistingHandle(new SetLineStyleOperation(lineStyle));
    }

    private class SetLineStyleOperation extends Operation {

        private final int lineStyle;

        SetLineStyleOperation(int lineStyle) {
            this.lineStyle = lineStyle;
        }

        @Override
        void apply() {
            if (data.lineStyle == lineStyle)
                return;
            int newLineStyle = this.lineStyle;
            switch(newLineStyle) {
                case SWT.LINE_SOLID:
                case SWT.LINE_DASH:
                case SWT.LINE_DOT:
                case SWT.LINE_DASHDOT:
                case SWT.LINE_DASHDOTDOT:
                    break;
                case SWT.LINE_CUSTOM:
                    if (data.lineDashes == null)
                        newLineStyle = SWT.LINE_SOLID;
                    break;
                default:
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
            data.lineStyle = newLineStyle;
            data.state &= ~LINE_STYLE;
        }
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
        int newValue = lineWidth;
        if (!java.util.Objects.equals(this.lineWidth, newValue)) {
            dirty();
        }
        checkNonDisposed();
        this.lineWidth = newValue;
        storeAndApplyOperationForExistingHandle(new SetLineWidthOperation(lineWidth));
    }

    private class SetLineWidthOperation extends Operation {

        private final int width;

        SetLineWidthOperation(int width) {
            this.width = width;
        }

        @Override
        void apply() {
        }
    }

    private void setLineWidthInPixels(int lineWidth) {
        if (data.lineWidth == lineWidth)
            return;
        data.lineWidth = lineWidth;
        data.state &= ~(LINE_WIDTH | DRAW_OFFSET);
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
        checkNonDisposed();
        this.XORMode = newValue;
        storeAndApplyOperationForExistingHandle(new SetXORModeOperation(xor));
    }

    private class SetXORModeOperation extends Operation {

        private final boolean xor;

        SetXORModeOperation(boolean xor) {
            this.xor = xor;
        }

        @Override
        void apply() {
        }
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
        checkNonDisposed();
        this.textAntialias = newValue;
        storeAndApplyOperationForExistingHandle(new SetTextAntialiasOperation(antialias));
    }

    private class SetTextAntialiasOperation extends Operation {

        private final int antialias;

        SetTextAntialiasOperation(int antialias) {
            this.antialias = antialias;
        }

        @Override
        void apply() {
            if (data.gdipGraphics == 0 && antialias == SWT.DEFAULT)
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
        }
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
        checkNonDisposed();
        if (transform != null && transform.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        this.transform = newValue;
        storeAndApplyOperationForExistingHandle(new SetTransformOperation(transform));
    }

    private class SetTransformOperation extends Operation {

        private final Transform transform;

        SetTransformOperation(Transform transform) {
            if (transform != null) {
                float[] elements = new float[6];
                transform.getElements(elements);
                this.transform = new Transform(device, elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
                registerForDisposal(this.transform);
            } else {
                this.transform = null;
            }
        }

        @Override
        void apply() {
            if (data.gdipGraphics == 0 && transform == null)
                return;
            if (transform != null) {
            }
            data.state &= ~DRAW_OFFSET;
        }
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
        if (string == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        return null;
    }

    Point stringExtentInPixels(String string) {
        checkNonDisposed();
        checkGC(FONT);
        int length = string.length();
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            Point size = new Point(0, 0);
            return size;
        }
        if (length == 0) {
        } else {
        }
        return null;
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
        return null;
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
        return null;
    }

    Point textExtentInPixels(String string, int flags) {
        checkNonDisposed();
        if (string == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        checkGC(FONT);
        long gdipGraphics = data.gdipGraphics;
        if (gdipGraphics != 0) {
            Point size = new Point(0, 0);
            return size;
        }
        if (string.length() == 0) {
        }
        return null;
    }

    void refreshFor(Drawable drawable) {
        if (drawable == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        destroy();
        GCData newData = new GCData();
        ((SwtGCData) originalData.getImpl()).copyTo(newData);
        createGcHandle(drawable, newData);
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

    /**
     * Answers the length of the side adjacent to the given angle
     * of a right triangle. In other words, it returns the integer
     * conversion of length * cos (angle).
     *
     * @param angle the angle in degrees
     * @param length the length of the triangle's hypotenuse
     * @return the integer conversion of length * cos (angle)
     */
    private static int cos(int angle, int length) {
        return (int) (Math.cos(angle * (Math.PI / 180)) * length);
    }

    /**
     * Answers the length of the side opposite to the given angle
     * of a right triangle. In other words, it returns the integer
     * conversion of length * sin (angle).
     *
     * @param angle the angle in degrees
     * @param length the length of the triangle's hypotenuse
     * @return the integer conversion of length * sin (angle)
     */
    private static int sin(int angle, int length) {
        return (int) (Math.sin(angle * (Math.PI / 180)) * length);
    }

    public int getZoom() {
        return DPIUtil.getZoomForAutoscaleProperty(data.nativeZoom);
    }

    private void storeAndApplyOperationForExistingHandle(Operation operation) {
        operations.add(operation);
        operation.apply();
    }

    private void createGcHandle(Drawable drawable, GCData newData) {
        long newHandle = drawable.internal_new_GC(newData);
        if (newHandle == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
        init(drawable, newData, newHandle);
        for (Operation operation : operations) {
            operation.apply();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        disposeOperations();
    }

    private void disposeOperations() {
        for (Operation op : operations) {
            op.disposeAll();
        }
        operations.clear();
    }

    private abstract class Operation {

        private final List<Resource> disposables = new ArrayList<>();

        abstract void apply();

        protected void registerForDisposal(Resource resource) {
            if (resource != null) {
                disposables.add(resource);
            }
        }

        void disposeAll() {
            for (Resource r : disposables) {
                r.dispose();
            }
            disposables.clear();
        }
    }

    boolean XORMode;

    boolean advanced;

    int alpha;

    int antialias;

    Color background;

    Pattern backgroundPattern;

    Rectangle clipping = new Rectangle(0, 0, 0, 0);

    int fillRule;

    Font font;

    Color foreground;

    Pattern foregroundPattern;

    int interpolation;

    LineAttributes lineAttributes;

    int lineCap;

    int[] lineDash;

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

    public Rectangle _clipping() {
        return clipping;
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
