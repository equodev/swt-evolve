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
 * *****************************************************************************
 */
package org.eclipse.swt.graphics;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gdip.*;
import org.eclipse.swt.internal.win32.*;

/**
 * Instances of this class represent paths through the two-dimensional
 * coordinate system. Paths do not have to be continuous, and can be
 * described using lines, rectangles, arcs, cubic or quadratic bezier curves,
 * glyphs, or other paths.
 * <p>
 * Application code must explicitly invoke the <code>Path.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 * <p>
 * This class requires the operating system's advanced graphics subsystem
 * which may not be available on some platforms.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#path">Path, Pattern snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: GraphicsExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.1
 */
public class SwtPath extends SwtResource implements IPath {

    private int initialZoom;

    private HashMap<Integer, Long> zoomLevelToHandle = new HashMap<>();

    PointF currentPoint = new PointF(), startPoint = new PointF();

    /**
     * Constructs a new empty Path.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * <p>
     * You must dispose the path when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the path
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the device is null and there is no current device</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle for the path could not be obtained</li>
     * </ul>
     *
     * @see #dispose()
     */
    public SwtPath(Device device, Path api) {
        this(device, DPIUtil.getDeviceZoom(), api);
    }

    SwtPath(Device device, int zoom, Path api) {
        super(device, api);
        ((SwtDevice) this.device.getImpl()).checkGDIP();
        initialZoom = zoom;
        long handle = Gdip.GraphicsPath_new(Gdip.FillModeAlternate);
        if (handle == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
        zoomLevelToHandle.put(initialZoom, handle);
        init();
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
    }

    /**
     * Constructs a new Path that is a copy of <code>path</code>. If
     * <code>flatness</code> is less than or equal to zero, an unflatten
     * copy of the path is created. Otherwise, it specifies the maximum
     * error between the path and its flatten copy. Smaller numbers give
     * better approximation.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * <p>
     * You must dispose the path when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the path
     * @param path the path to make a copy
     * @param flatness the flatness value
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the path is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the path has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle for the path could not be obtained</li>
     * </ul>
     *
     * @see #dispose()
     * @since 3.4
     */
    public SwtPath(Device device, Path path, float flatness, Path api) {
        super(device, api);
        if (path == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (path.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        flatness = Math.max(0, flatness);
        long handle = Gdip.GraphicsPath_Clone(((SwtPath) path.getImpl()).getHandle(((SwtPath) path.getImpl()).initialZoom));
        if (flatness != 0)
            Gdip.GraphicsPath_Flatten(handle, 0, flatness);
        if (handle == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
        initialZoom = ((SwtPath) path.getImpl()).initialZoom;
        zoomLevelToHandle.put(initialZoom, handle);
        init();
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
    }

    /**
     * Constructs a new Path with the specified PathData.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * <p>
     * You must dispose the path when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the path
     * @param data the data for the path
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the data is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle for the path could not be obtained</li>
     * </ul>
     *
     * @see #dispose()
     * @since 3.4
     */
    public SwtPath(Device device, PathData data, Path api) {
        this(device, data, DPIUtil.getDeviceZoom(), api);
    }

    SwtPath(Device device, PathData data, int zoom, Path api) {
        this(device, zoom, api);
        if (data == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        init(data);
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
    }

    /**
     * Adds to the receiver a circular or elliptical arc that lies within
     * the specified rectangular area.
     * <p>
     * The resulting arc begins at <code>startAngle</code> and extends
     * for <code>arcAngle</code> degrees.
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
     * @param x the x coordinate of the upper-left corner of the arc
     * @param y the y coordinate of the upper-left corner of the arc
     * @param width the width of the arc
     * @param height the height of the arc
     * @param startAngle the beginning angle
     * @param arcAngle the angular extent of the arc, relative to the start angle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void addArc(float x, float y, float width, float height, float startAngle, float arcAngle) {
        if (width == 0 || height == 0 || arcAngle == 0)
            return;
        Drawable drawable = getDevice();
        x = DPIUtil.scaleUp(drawable, x, initialZoom);
        y = DPIUtil.scaleUp(drawable, y, initialZoom);
        width = DPIUtil.scaleUp(drawable, width, initialZoom);
        height = DPIUtil.scaleUp(drawable, height, initialZoom);
        addArcInPixels(x, y, width, height, startAngle, arcAngle);
    }

    void addArcInPixels(float x, float y, float width, float height, float startAngle, float arcAngle) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (width < 0) {
            x = x + width;
            width = -width;
        }
        if (height < 0) {
            y = y + height;
            height = -height;
        }
        if (width == height) {
            Gdip.GraphicsPath_AddArc(getHandle(initialZoom), x, y, width, height, -startAngle, -arcAngle);
        } else {
            long path = Gdip.GraphicsPath_new(Gdip.FillModeAlternate);
            if (path == 0)
                SWT.error(SWT.ERROR_NO_HANDLES);
            long matrix = Gdip.Matrix_new(width, 0, 0, height, x, y);
            if (matrix == 0)
                SWT.error(SWT.ERROR_NO_HANDLES);
            Gdip.GraphicsPath_AddArc(path, 0, 0, 1, 1, -startAngle, -arcAngle);
            Gdip.GraphicsPath_Transform(path, matrix);
            Gdip.GraphicsPath_AddPath(getHandle(initialZoom), path, true);
            Gdip.Matrix_delete(matrix);
            Gdip.GraphicsPath_delete(path);
        }
        Gdip.GraphicsPath_GetLastPoint(getHandle(initialZoom), currentPoint);
    }

    /**
     * Adds to the receiver the path described by the parameter.
     *
     * @param path the path to add to the receiver
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void addPath(Path path) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (path == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (path.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        //TODO - expose connect?
        Gdip.GraphicsPath_AddPath(getHandle(initialZoom), ((SwtPath) path.getImpl()).getHandle(initialZoom), false);
        currentPoint.X = ((SwtPath) path.getImpl()).currentPoint.X;
        currentPoint.Y = ((SwtPath) path.getImpl()).currentPoint.Y;
    }

    /**
     * Adds to the receiver the rectangle specified by x, y, width and height.
     *
     * @param x the x coordinate of the rectangle to add
     * @param y the y coordinate of the rectangle to add
     * @param width the width of the rectangle to add
     * @param height the height of the rectangle to add
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void addRectangle(float x, float y, float width, float height) {
        addRectangleInPixels(x, y, width, height);
    }

    void addRectangleInPixels(float x, float y, float width, float height) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        RectF rect = new RectF();
        rect.X = x;
        rect.Y = y;
        rect.Width = width;
        rect.Height = height;
        Gdip.GraphicsPath_AddRectangle(getHandle(initialZoom), rect);
        currentPoint.X = x;
        currentPoint.Y = y;
    }

    /**
     * Adds to the receiver the pattern of glyphs generated by drawing
     * the given string using the given font starting at the point (x, y).
     *
     * @param string the text to use
     * @param x the x coordinate of the starting point
     * @param y the y coordinate of the starting point
     * @param font the font to use
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the font is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the font has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void addString(String string, float x, float y, Font font) {
        Drawable drawable = getDevice();
        x = DPIUtil.scaleUp(drawable, x, initialZoom);
        y = DPIUtil.scaleUp(drawable, y, initialZoom);
        addStringInPixels(string, x, y, font);
    }

    void addStringInPixels(String string, float x, float y, Font font) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (font == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (font.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        char[] buffer = string.toCharArray();
        long hDC = device.internal_new_GC(null);
        long[] family = new long[1];
        long gdipFont = SwtGC.createGdipFont(hDC, SWTFontProvider.getFont(device, font.getFontData()[0], initialZoom).handle, 0, ((SwtDevice) device.getImpl()).fontCollection, family, null);
        PointF point = new PointF();
        point.X = x - (Gdip.Font_GetSize(gdipFont) / 6);
        point.Y = y;
        int style = Gdip.Font_GetStyle(gdipFont);
        float size = Gdip.Font_GetSize(gdipFont);
        Gdip.GraphicsPath_AddString(getHandle(initialZoom), buffer, buffer.length, family[0], style, size, point, 0);
        Gdip.GraphicsPath_GetLastPoint(getHandle(initialZoom), currentPoint);
        Gdip.FontFamily_delete(family[0]);
        Gdip.Font_delete(gdipFont);
        device.internal_dispose_GC(hDC, null);
    }

    /**
     * Closes the current sub path by adding to the receiver a line
     * from the current point of the path back to the starting point
     * of the sub path.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void close() {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        Gdip.GraphicsPath_CloseFigure(getHandle(initialZoom));
        /*
	* Feature in GDI+. CloseFigure() does affect the last
	* point, so GetLastPoint() does not return the starting
	* point of the subpath after calling CloseFigure().  The
	* fix is to remember the subpath starting point and use
	* it instead.
	*/
        currentPoint.X = startPoint.X;
        currentPoint.Y = startPoint.Y;
    }

    /**
     * Returns <code>true</code> if the specified point is contained by
     * the receiver and false otherwise.
     * <p>
     * If outline is <code>true</code>, the point (x, y) checked for containment in
     * the receiver's outline. If outline is <code>false</code>, the point is
     * checked to see if it is contained within the bounds of the (closed) area
     * covered by the receiver.
     *
     * @param x the x coordinate of the point to test for containment
     * @param y the y coordinate of the point to test for containment
     * @param gc the GC to use when testing for containment
     * @param outline controls whether to check the outline or contained area of the path
     * @return <code>true</code> if the path contains the point and <code>false</code> otherwise
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the gc is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the gc has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public boolean contains(float x, float y, GC gc, boolean outline) {
        Drawable drawable = getDevice();
        x = DPIUtil.scaleUp(drawable, x, initialZoom);
        y = DPIUtil.scaleUp(drawable, y, initialZoom);
        return containsInPixels(x, y, gc, outline);
    }

    boolean containsInPixels(float x, float y, GC gc, boolean outline) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (gc == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (gc.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        //TODO - should use GC transformation
        ((SwtGC) gc.getImpl()).initGdip();
        ((SwtGC) gc.getImpl()).checkGC(SwtGC.LINE_CAP | SwtGC.LINE_JOIN | SwtGC.LINE_STYLE | SwtGC.LINE_WIDTH);
        int mode = OS.GetPolyFillMode(gc.handle) == OS.WINDING ? Gdip.FillModeWinding : Gdip.FillModeAlternate;
        Gdip.GraphicsPath_SetFillMode(getHandle(initialZoom), mode);
        if (outline) {
            return Gdip.GraphicsPath_IsOutlineVisible(getHandle(initialZoom), x, y, ((SwtGC) gc.getImpl()).data.gdipPen, ((SwtGC) gc.getImpl()).data.gdipGraphics);
        } else {
            return Gdip.GraphicsPath_IsVisible(getHandle(initialZoom), x, y, ((SwtGC) gc.getImpl()).data.gdipGraphics);
        }
    }

    /**
     * Adds to the receiver a cubic bezier curve based on the parameters.
     *
     * @param cx1 the x coordinate of the first control point of the spline
     * @param cy1 the y coordinate of the first control of the spline
     * @param cx2 the x coordinate of the second control of the spline
     * @param cy2 the y coordinate of the second control of the spline
     * @param x the x coordinate of the end point of the spline
     * @param y the y coordinate of the end point of the spline
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void cubicTo(float cx1, float cy1, float cx2, float cy2, float x, float y) {
        Drawable drawable = getDevice();
        cx1 = DPIUtil.scaleUp(drawable, cx1, initialZoom);
        cy1 = DPIUtil.scaleUp(drawable, cy1, initialZoom);
        cx2 = DPIUtil.scaleUp(drawable, cx2, initialZoom);
        cy2 = DPIUtil.scaleUp(drawable, cy2, initialZoom);
        x = DPIUtil.scaleUp(drawable, x, initialZoom);
        y = DPIUtil.scaleUp(drawable, y, initialZoom);
        cubicToInPixels(cx1, cy1, cx2, cy2, x, y);
    }

    void cubicToInPixels(float cx1, float cy1, float cx2, float cy2, float x, float y) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        Gdip.GraphicsPath_AddBezier(getHandle(initialZoom), currentPoint.X, currentPoint.Y, cx1, cy1, cx2, cy2, x, y);
        Gdip.GraphicsPath_GetLastPoint(getHandle(initialZoom), currentPoint);
    }

    @Override
    void destroy() {
        ((SwtDevice) device.getImpl()).deregisterResourceWithZoomSupport(this.getApi());
        zoomLevelToHandle.values().forEach(Gdip::GraphicsPath_delete);
        zoomLevelToHandle.clear();
    }

    @Override
    void destroyHandlesExcept(Set<Integer> zoomLevels) {
        zoomLevelToHandle.entrySet().removeIf(entry -> {
            final Integer zoom = entry.getKey();
            if (!zoomLevels.contains(zoom) && zoom != initialZoom) {
                Gdip.GraphicsPath_delete(entry.getValue());
                return true;
            }
            return false;
        });
    }

    /**
     * Replaces the first four elements in the parameter with values that
     * describe the smallest rectangle that will completely contain the
     * receiver (i.e. the bounding box).
     *
     * @param bounds the array to hold the result
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter is too small to hold the bounding box</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void getBounds(float[] bounds) {
        if (bounds == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        getBoundsInPixels(bounds);
        float[] scaledbounds = DPIUtil.scaleDown(getDevice(), bounds, initialZoom);
        System.arraycopy(scaledbounds, 0, bounds, 0, 4);
    }

    void getBoundsInPixels(float[] bounds) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (bounds.length < 4)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        RectF rect = new RectF();
        Gdip.GraphicsPath_GetBounds(getHandle(initialZoom), rect, 0, 0);
        bounds[0] = rect.X;
        bounds[1] = rect.Y;
        bounds[2] = rect.Width;
        bounds[3] = rect.Height;
    }

    /**
     * Replaces the first two elements in the parameter with values that
     * describe the current point of the path.
     *
     * @param point the array to hold the result
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parameter is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parameter is too small to hold the end point</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void getCurrentPoint(float[] point) {
        if (point == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        getCurrentPointInPixels(point);
        float[] scaledpoint = DPIUtil.scaleDown(getDevice(), point, initialZoom);
        System.arraycopy(scaledpoint, 0, point, 0, 2);
    }

    void getCurrentPointInPixels(float[] point) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (point.length < 2)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        point[0] = currentPoint.X;
        point[1] = currentPoint.Y;
    }

    /**
     * Returns a device independent representation of the receiver.
     *
     * @return the PathData for the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see PathData
     */
    public PathData getPathData() {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        PathData result = getPathDataInPixels();
        result.points = DPIUtil.scaleDown(getDevice(), result.points, initialZoom);
        return result;
    }

    PathData getPathDataInPixels() {
        int count = Gdip.GraphicsPath_GetPointCount(getHandle(initialZoom));
        byte[] gdipTypes = new byte[count];
        float[] points = new float[count * 2];
        Gdip.GraphicsPath_GetPathTypes(getHandle(initialZoom), gdipTypes, count);
        Gdip.GraphicsPath_GetPathPoints(getHandle(initialZoom), points, count);
        byte[] types = new byte[count * 2];
        int index = 0, typesIndex = 0;
        while (index < count) {
            byte type = gdipTypes[index];
            boolean close = false;
            switch(type & Gdip.PathPointTypePathTypeMask) {
                case Gdip.PathPointTypeStart:
                    types[typesIndex++] = SWT.PATH_MOVE_TO;
                    close = (type & Gdip.PathPointTypeCloseSubpath) != 0;
                    index += 1;
                    break;
                case Gdip.PathPointTypeLine:
                    types[typesIndex++] = SWT.PATH_LINE_TO;
                    close = (type & Gdip.PathPointTypeCloseSubpath) != 0;
                    index += 1;
                    break;
                case Gdip.PathPointTypeBezier:
                    types[typesIndex++] = SWT.PATH_CUBIC_TO;
                    close = (gdipTypes[index + 2] & Gdip.PathPointTypeCloseSubpath) != 0;
                    index += 3;
                    break;
                default:
                    index++;
            }
            if (close) {
                types[typesIndex++] = SWT.PATH_CLOSE;
            }
        }
        if (typesIndex != types.length) {
            byte[] newTypes = new byte[typesIndex];
            System.arraycopy(types, 0, newTypes, 0, typesIndex);
            types = newTypes;
        }
        PathData result = new PathData();
        result.types = types;
        result.points = points;
        return result;
    }

    /**
     * Adds to the receiver a line from the current point to
     * the point specified by (x, y).
     *
     * @param x the x coordinate of the end of the line to add
     * @param y the y coordinate of the end of the line to add
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void lineTo(float x, float y) {
        Drawable drawable = getDevice();
        lineToInPixels(DPIUtil.scaleUp(drawable, x, initialZoom), DPIUtil.scaleUp(drawable, y, initialZoom));
    }

    void lineToInPixels(float x, float y) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        Gdip.GraphicsPath_AddLine(getHandle(initialZoom), currentPoint.X, currentPoint.Y, x, y);
        Gdip.GraphicsPath_GetLastPoint(getHandle(initialZoom), currentPoint);
    }

    void init(PathData data) {
        byte[] types = data.types;
        float[] points = data.points;
        for (int i = 0, j = 0; i < types.length; i++) {
            switch(types[i]) {
                case SWT.PATH_MOVE_TO:
                    moveTo(points[j++], points[j++]);
                    break;
                case SWT.PATH_LINE_TO:
                    lineTo(points[j++], points[j++]);
                    break;
                case SWT.PATH_CUBIC_TO:
                    cubicTo(points[j++], points[j++], points[j++], points[j++], points[j++], points[j++]);
                    break;
                case SWT.PATH_QUAD_TO:
                    quadTo(points[j++], points[j++], points[j++], points[j++]);
                    break;
                case SWT.PATH_CLOSE:
                    close();
                    break;
                default:
                    dispose();
                    SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
        }
    }

    /**
     * Returns <code>true</code> if the Path has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the Path.
     * When a Path has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the Path.
     *
     * @return <code>true</code> when the Path is disposed, and <code>false</code> otherwise
     */
    @Override
    public boolean isDisposed() {
        return zoomLevelToHandle.isEmpty();
    }

    /**
     * Sets the current point of the receiver to the point
     * specified by (x, y). Note that this starts a new
     * sub path.
     *
     * @param x the x coordinate of the new end point
     * @param y the y coordinate of the new end point
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void moveTo(float x, float y) {
        Drawable drawable = getDevice();
        moveToInPixels(DPIUtil.scaleUp(drawable, x, initialZoom), DPIUtil.scaleUp(drawable, y, initialZoom));
    }

    void moveToInPixels(float x, float y) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        Gdip.GraphicsPath_StartFigure(getHandle(initialZoom));
        currentPoint.X = startPoint.X = x;
        currentPoint.Y = startPoint.Y = y;
    }

    /**
     * Adds to the receiver a quadratic curve based on the parameters.
     *
     * @param cx the x coordinate of the control point of the spline
     * @param cy the y coordinate of the control point of the spline
     * @param x the x coordinate of the end point of the spline
     * @param y the y coordinate of the end point of the spline
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public void quadTo(float cx, float cy, float x, float y) {
        Drawable drawable = getDevice();
        cx = DPIUtil.scaleUp(drawable, cx, initialZoom);
        cy = DPIUtil.scaleUp(drawable, cy, initialZoom);
        x = DPIUtil.scaleUp(drawable, x, initialZoom);
        y = DPIUtil.scaleUp(drawable, y, initialZoom);
        quadToInPixels(cx, cy, x, y);
    }

    void quadToInPixels(float cx, float cy, float x, float y) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        float cx1 = currentPoint.X + 2 * (cx - currentPoint.X) / 3;
        float cy1 = currentPoint.Y + 2 * (cy - currentPoint.Y) / 3;
        float cx2 = cx1 + (x - currentPoint.X) / 3;
        float cy2 = cy1 + (y - currentPoint.Y) / 3;
        Gdip.GraphicsPath_AddBezier(getHandle(initialZoom), currentPoint.X, currentPoint.Y, cx1, cy1, cx2, cy2, x, y);
        Gdip.GraphicsPath_GetLastPoint(getHandle(initialZoom), currentPoint);
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
            return "Path {*DISPOSED*}";
        return "Path " + zoomLevelToHandle;
    }

    long getHandle(int zoom) {
        if (!zoomLevelToHandle.containsKey(zoom)) {
            PathData pathData = getPathData();
            Path scaledPath = new Path(getDevice(), pathData, zoom);
            zoomLevelToHandle.put(zoom, ((SwtPath) scaledPath.getImpl()).getHandle(((SwtPath) scaledPath.getImpl()).initialZoom));
        }
        return zoomLevelToHandle.get(zoom);
    }

    public Path getApi() {
        if (api == null)
            api = Path.createApi(this);
        return (Path) api;
    }
}
