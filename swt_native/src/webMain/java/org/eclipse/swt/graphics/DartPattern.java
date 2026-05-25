/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
import dev.equo.swt.*;

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
public class DartPattern extends DartResource implements IPattern {

    Image image;

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
    public DartPattern(Device device, Image image, Pattern api) {
        super(device, api);
        if (image == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (image.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        try {
            this.image = image;
            init();
        } finally {
        }
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
    public DartPattern(Device device, float x1, float y1, float x2, float y2, Color color1, Color color2, Pattern api) {
        this(device, x1, y1, x2, y2, color1, 0xFF, color2, 0xFF, api);
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
    public DartPattern(Device device, float x1, float y1, float x2, float y2, Color color1, int alpha1, Color color2, int alpha2, Pattern api) {
        super(device, api);
        if (color1.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (color2.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        this.color1 = new Color(new RGB(color1.getRed(), color1.getGreen(), color1.getBlue()), alpha1);
        this.color2 = new Color(new RGB(color2.getRed(), color2.getGreen(), color2.getBlue()), alpha2);
        this.startX = x1;
        this.startY = y1;
        this.endX = x2;
        this.endY = y2;
        init();
    }

    @Override
    void destroy() {
        image = null;
        color1 = null;
        color2 = null;
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
    @Override
    public boolean isDisposed() {
        return device == null;
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
            return "Pattern {*DISPOSED*}";
        return null;
    }

    float endX;

    float endY;

    float startX;

    float startY;

    public Image _image() {
        return image;
    }

    public float _endX() {
        return endX;
    }

    public float _endY() {
        return endY;
    }

    public float _startX() {
        return startX;
    }

    public float _startY() {
        return startY;
    }

    Color color1;

    Color color2;

    public Pattern getApi() {
        if (api == null)
            api = Pattern.createApi(this);
        return (Pattern) api;
    }

    public VPattern getValue() {
        if (value == null)
            value = new VPattern(this);
        return (VPattern) value;
    }
}
