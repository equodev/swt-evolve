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

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import dev.equo.swt.*;

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
 * @see <a href="https://eclipse.dev/eclipse/swt/snippets/#path">Path, Pattern snippets</a>
 * @see <a href="https://eclipse.dev/eclipse/swt/examples.html">SWT Example: GraphicsExample</a>
 * @see <a href="https://eclipse.dev/eclipse/swt/">Sample code and further information</a>
 *
 * @since 3.1
 */
public class DartPath extends DartResource implements IPath {

    boolean closed = true;

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
    public DartPath(Device device, Path api) {
        super(device, api);
        try {
            init();
        } finally {
        }
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
    public DartPath(Device device, Path path, float flatness, Path api) {
        super(device, api);
        if (path == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (path.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        appendPathData(((DartPath) path.getImpl()).getPathData());
        closed = ((DartPath) path.getImpl()).closed;
        init();
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
    public DartPath(Device device, PathData data, Path api) {
        this(device, api);
        try {
            if (data == null)
                SWT.error(SWT.ERROR_NULL_ARGUMENT);
            init(data);
        } finally {
        }
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (width == 0 || height == 0 || arcAngle == 0)
            return;
        appendArc(x, y, width, height, startAngle, arcAngle);
        closed = Math.abs(arcAngle) >= 360;
        if (closed)
            close();
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
        DartPath source = (DartPath) path.getImpl();
        appendPathData(source.getPathData());
        closed = source.closed;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        moveTo(x, y);
        lineTo(x + width, y);
        lineTo(x + width, y + height);
        lineTo(x, y + height);
        close();
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (font == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (font.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        try {
            closed = true;
        } finally {
        }
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
        if (hasCurrentPoint) {
            appendElement((byte) SWT.PATH_CLOSE);
            currentX = startX;
            currentY = startY;
        }
        closed = true;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (gc == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        try {
            //TODO - see windows
            if (outline) {
                int[] buffer = new int[] { 0xFFFFFFFF };
                GCData data = ((DartGC) gc.getImpl()).data;
                switch(data.lineCap) {
                    case SWT.CAP_ROUND:
                        break;
                    case SWT.CAP_FLAT:
                        break;
                    case SWT.CAP_SQUARE:
                        break;
                }
                switch(data.lineJoin) {
                    case SWT.JOIN_MITER:
                        break;
                    case SWT.JOIN_ROUND:
                        break;
                    case SWT.JOIN_BEVEL:
                        break;
                }
                return buffer[0] != 0xFFFFFFFF;
            } else {
            }
        } finally {
        }
        return false;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        ensureCurrentPoint();
        appendElement((byte) SWT.PATH_CUBIC_TO, cx1, cy1, cx2, cy2, x, y);
        currentX = x;
        currentY = y;
        closed = false;
    }

    @Override
    void destroy() {
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (bounds == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (bounds.length < 4)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        try {
        } finally {
        }
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (point == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (point.length < 2)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        point[0] = currentX;
        point[1] = currentY;
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
        PathData data = new PathData();
        data.types = pathData == null ? new byte[0] : pathData.types.clone();
        data.points = pathData == null ? new float[0] : pathData.points.clone();
        return data;
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
        return device == null;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        ensureCurrentPoint();
        appendElement((byte) SWT.PATH_LINE_TO, x, y);
        currentX = x;
        currentY = y;
        closed = false;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        appendElement((byte) SWT.PATH_MOVE_TO, x, y);
        currentX = startX = x;
        currentY = startY = y;
        hasCurrentPoint = true;
        closed = true;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        ensureCurrentPoint();
        float c1x = currentX + 2 * (cx - currentX) / 3;
        float c1y = currentY + 2 * (cy - currentY) / 3;
        float c2x = c1x + (x - currentX) / 3;
        float c2y = c1y + (y - currentY) / 3;
        appendElement((byte) SWT.PATH_CUBIC_TO, c1x, c1y, c2x, c2y, x, y);
        currentX = x;
        currentY = y;
        closed = false;
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
        return "Path {}";
    }

    PathData pathData;

    public boolean _closed() {
        return closed;
    }

    public PathData _pathData() {
        return pathData;
    }

    float currentX, currentY, startX, startY;

    boolean hasCurrentPoint;

    void appendElement(byte type, float... coords) {
        if (pathData == null) {
            pathData = new PathData();
            pathData.types = new byte[0];
            pathData.points = new float[0];
        }
        byte[] types = new byte[pathData.types.length + 1];
        System.arraycopy(pathData.types, 0, types, 0, pathData.types.length);
        types[types.length - 1] = type;
        pathData.types = types;
        if (coords.length > 0) {
            float[] points = new float[pathData.points.length + coords.length];
            System.arraycopy(pathData.points, 0, points, 0, pathData.points.length);
            System.arraycopy(coords, 0, points, pathData.points.length, coords.length);
            pathData.points = points;
        }
    }

    void ensureCurrentPoint() {
        if (!hasCurrentPoint)
            moveTo(0, 0);
    }

    void appendArc(float x, float y, float width, float height, float startAngle, float arcAngle) {
        double rx = width / 2.0, ry = height / 2.0;
        double cx = x + rx, cy = y + ry;
        double start = Math.toRadians(startAngle);
        double sweep = Math.toRadians(arcAngle);
        if (closed) {
            moveTo((float) (cx + rx * Math.cos(start)), (float) (cy - ry * Math.sin(start)));
        } else {
            lineTo((float) (cx + rx * Math.cos(start)), (float) (cy - ry * Math.sin(start)));
        }
        int segments = (int) Math.ceil(Math.abs(sweep) / (Math.PI / 2));
        double step = sweep / segments;
        double k = 4.0 / 3.0 * Math.tan(step / 4);
        double a = start;
        for (int i = 0; i < segments; i++) {
            double b = a + step;
            double cosA = Math.cos(a), sinA = Math.sin(a);
            double cosB = Math.cos(b), sinB = Math.sin(b);
            appendElement((byte) SWT.PATH_CUBIC_TO, (float) (cx + rx * (cosA - k * sinA)), (float) (cy - ry * (sinA + k * cosA)), (float) (cx + rx * (cosB + k * sinB)), (float) (cy - ry * (sinB - k * cosB)), (float) (cx + rx * cosB), (float) (cy - ry * sinB));
            a = b;
        }
        currentX = (float) (cx + rx * Math.cos(a));
        currentY = (float) (cy - ry * Math.sin(a));
        hasCurrentPoint = true;
    }

    void appendPathData(PathData data) {
        if (data == null || data.types == null)
            return;
        for (int i = 0, j = 0; i < data.types.length; i++) {
            switch(data.types[i]) {
                case SWT.PATH_MOVE_TO:
                    if (closed)
                        moveTo(data.points[j++], data.points[j++]);
                    else
                        lineTo(data.points[j++], data.points[j++]);
                    break;
                case SWT.PATH_LINE_TO:
                    lineTo(data.points[j++], data.points[j++]);
                    break;
                case SWT.PATH_CUBIC_TO:
                    cubicTo(data.points[j++], data.points[j++], data.points[j++], data.points[j++], data.points[j++], data.points[j++]);
                    break;
                case SWT.PATH_QUAD_TO:
                    quadTo(data.points[j++], data.points[j++], data.points[j++], data.points[j++]);
                    break;
                case SWT.PATH_CLOSE:
                    close();
                    break;
            }
        }
    }

    public Path getApi() {
        if (api == null)
            api = Path.createApi(this);
        return (Path) api;
    }

    public VPath getValue() {
        if (value == null)
            value = new VPath(this);
        return (VPath) value;
    }
}
