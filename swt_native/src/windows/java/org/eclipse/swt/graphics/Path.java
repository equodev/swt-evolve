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
public class Path extends Resource {

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
    public Path(Device device) {
        this((IPath) null);
        setImpl(new SwtPath(device, this));
    }

    Path(Device device, int zoom) {
        this((IPath) null);
        setImpl(new SwtPath(device, zoom, this));
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
    public Path(Device device, Path path, float flatness) {
        this((IPath) null);
        setImpl(new SwtPath(device, path, flatness, this));
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
    public Path(Device device, PathData data) {
        this((IPath) null);
        setImpl(new SwtPath(device, data, this));
    }

    Path(Device device, PathData data, int zoom) {
        this((IPath) null);
        setImpl(new SwtPath(device, data, zoom, this));
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
        getImpl().addArc(x, y, width, height, startAngle, arcAngle);
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
        getImpl().addPath(path);
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
        getImpl().addRectangle(x, y, width, height);
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
        getImpl().addString(string, x, y, font);
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
        getImpl().close();
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
        return getImpl().contains(x, y, gc, outline);
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
        getImpl().cubicTo(cx1, cy1, cx2, cy2, x, y);
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
        getImpl().getBounds(bounds);
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
        getImpl().getCurrentPoint(point);
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
        return getImpl().getPathData();
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
        getImpl().lineTo(x, y);
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
    public boolean isDisposed() {
        return getImpl().isDisposed();
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
        getImpl().moveTo(x, y);
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
        getImpl().quadTo(cx, cy, x, y);
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

    protected Path(IPath impl) {
        super(impl);
    }

    static Path createApi(IPath impl) {
        return new Path(impl);
    }

    public IPath getImpl() {
        return (IPath) super.getImpl();
    }
}
