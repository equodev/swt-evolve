/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
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
 * Instances of this class represent patterns to use while drawing. Patterns
 * can be specified either as bitmaps or gradients.
 * <p>
 * Application code must explicitly invoke the <code>Pattern.dispose()</code>
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
public class Pattern extends Resource {

    /**
     * Constructs a new Pattern given an image. Drawing with the resulting
     * pattern will cause the image to be tiled over the resulting area.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * <p>
     * You must dispose the pattern when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the pattern
     * @param image the image that the pattern will draw
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the device is null and there is no current device, or the image is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle for the pattern could not be obtained</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Pattern(Device device, Image image) {
        this((IPattern) null);
        setImpl(new SwtPattern(device, image, this));
    }

    /**
     * Constructs a new Pattern that represents a linear, two color
     * gradient. Drawing with the pattern will cause the resulting area to be
     * tiled with the gradient specified by the arguments.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * <p>
     * You must dispose the pattern when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the pattern
     * @param x1 the x coordinate of the starting corner of the gradient
     * @param y1 the y coordinate of the starting corner of the gradient
     * @param x2 the x coordinate of the ending corner of the gradient
     * @param y2 the y coordinate of the ending corner of the gradient
     * @param color1 the starting color of the gradient
     * @param color2 the ending color of the gradient
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the device is null and there is no current device,
     *                              or if either color1 or color2 is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if either color1 or color2 has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle for the pattern could not be obtained</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Pattern(Device device, float x1, float y1, float x2, float y2, Color color1, Color color2) {
        this((IPattern) null);
        setImpl(new SwtPattern(device, x1, y1, x2, y2, color1, color2, this));
    }

    /**
     * Constructs a new Pattern that represents a linear, two color
     * gradient. Drawing with the pattern will cause the resulting area to be
     * tiled with the gradient specified by the arguments.
     * <p>
     * This operation requires the operating system's advanced
     * graphics subsystem which may not be available on some
     * platforms.
     * </p>
     * <p>
     * You must dispose the pattern when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the pattern
     * @param x1 the x coordinate of the starting corner of the gradient
     * @param y1 the y coordinate of the starting corner of the gradient
     * @param x2 the x coordinate of the ending corner of the gradient
     * @param y2 the y coordinate of the ending corner of the gradient
     * @param color1 the starting color of the gradient
     * @param alpha1 the starting alpha value of the gradient
     * @param color2 the ending color of the gradient
     * @param alpha2 the ending alpha value of the gradient
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the device is null and there is no current device,
     *                              or if either color1 or color2 is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if either color1 or color2 has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_NO_GRAPHICS_LIBRARY - if advanced graphics are not available</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle for the pattern could not be obtained</li>
     * </ul>
     *
     * @see #dispose()
     *
     * @since 3.2
     */
    public Pattern(Device device, float x1, float y1, float x2, float y2, Color color1, int alpha1, Color color2, int alpha2) {
        this((IPattern) null);
        setImpl(new SwtPattern(device, x1, y1, x2, y2, color1, alpha1, color2, alpha2, this));
    }

    /**
     * Returns <code>true</code> if the Pattern has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the Pattern.
     * When a Pattern has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the Pattern.
     *
     * @return <code>true</code> when the Pattern is disposed, and <code>false</code> otherwise
     */
    public boolean isDisposed() {
        return getImpl().isDisposed();
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

    protected Pattern(IPattern impl) {
        super(impl);
    }

    static Pattern createApi(IPattern impl) {
        return new Pattern(impl);
    }

    public IPattern getImpl() {
        return (IPattern) super.getImpl();
    }
}
