/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2020 IBM Corporation and others.
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
 * Instances of this store color information. To create a color you can either
 * specify the individual color components as integers in the range
 * 0 to 255 or provide an instance of an <code>RGB</code> or <code>RGBA</code>.
 * <p>
 * Colors do not need to be disposed, however to maintain compatibility
 * with older code, disposing a Color is not an error. As Colors do not require
 * disposal, the constructors which do not require a Device are recommended.
 * </p>
 *
 * @see RGB
 * @see RGBA
 * @see Device#getSystemColor
 * @see <a href="http://www.eclipse.org/swt/snippets/#color">Color and RGB snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: PaintExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class DartColor extends DartResource implements IColor {

    int alpha = 255;

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    DartColor(Color api) {
        super(api);
    }

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    DartColor(Device device, Color api) {
        super(device, api);
    }

    /**
     * Constructs a new instance of this class given a device and the
     * desired red, green and blue values expressed as ints in the range
     * 0 to 255 (where 0 is black and 255 is full brightness).
     *
     * @param device the device on which to allocate the color
     * @param red the amount of red in the color
     * @param green the amount of green in the color
     * @param blue the amount of blue in the color
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the red, green or blue argument is not between 0 and 255</li>
     * </ul>
     *
     * @see #Color(int, int, int) The equivalent constructor not requiring a Device
     */
    public DartColor(Device device, int red, int green, int blue, Color api) {
        super(device, api);
        init(red, green, blue, 255);
        init();
    }

    /**
     * Constructs a new instance of this class given the
     * desired red, green and blue values expressed as ints in the range
     * 0 to 255 (where 0 is black and 255 is full brightness).
     *
     * @param red the amount of red in the color
     * @param green the amount of green in the color
     * @param blue the amount of blue in the color
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the red, green or blue argument is not between 0 and 255</li>
     * </ul>
     * @since 3.115
     */
    public DartColor(int red, int green, int blue, Color api) {
        super(api);
        init(red, green, blue, 255);
    }

    /**
     * Constructs a new instance of this class given a device and the
     * desired red, green, blue &amp; alpha values expressed as ints in the range
     * 0 to 255 (where 0 is black and 255 is full brightness).
     *
     * @param device the device on which to allocate the color
     * @param red the amount of red in the color
     * @param green the amount of green in the color
     * @param blue the amount of blue in the color
     * @param alpha the amount of alpha in the color. Currently, SWT only honors extreme values for alpha i.e. 0 (transparent) or 255 (opaque).
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the red, green, blue or alpha argument is not between 0 and 255</li>
     * </ul>
     *
     * @see #Color(int, int, int, int) The equivalent constructor not requiring a Device
     *
     * @since 3.104
     */
    public DartColor(Device device, int red, int green, int blue, int alpha, Color api) {
        super(device, api);
        init(red, green, blue, alpha);
        init();
    }

    /**
     * Constructs a new instance of this class given the
     * desired red, green, blue &amp; alpha values expressed as ints in the range
     * 0 to 255 (where 0 is black and 255 is full brightness).
     *
     * @param red the amount of red in the color
     * @param green the amount of green in the color
     * @param blue the amount of blue in the color
     * @param alpha the amount of alpha in the color. Currently, SWT only honors extreme values for alpha i.e. 0 (transparent) or 255 (opaque).
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the red, green, blue or alpha argument is not between 0 and 255</li>
     * </ul>
     *
     * @since 3.115
     */
    public DartColor(int red, int green, int blue, int alpha, Color api) {
        super(api);
        init(red, green, blue, alpha);
    }

    /**
     * Constructs a new instance of this class given a device and an
     * <code>RGB</code> describing the desired red, green and blue values.
     *
     * @param device the device on which to allocate the color
     * @param rgb the RGB values of the desired color
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the rgb argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the red, green or blue components of the argument are not between 0 and 255</li>
     * </ul>
     *
     * @see #Color(RGB) The equivalent constructor not requiring a Device
     */
    public DartColor(Device device, RGB rgb, Color api) {
        super(device, api);
        if (rgb == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        init(rgb.red, rgb.green, rgb.blue, 255);
        init();
    }

    /**
     * Constructs a new instance of this class given an
     * <code>RGB</code> describing the desired red, green and blue values.
     *
     * @param rgb the RGB values of the desired color
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the rgb argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the red, green or blue components of the argument are not between 0 and 255</li>
     * </ul>
     * @since 3.115
     */
    public DartColor(RGB rgb, Color api) {
        super(api);
        if (rgb == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        init(rgb.red, rgb.green, rgb.blue, 255);
    }

    /**
     * Constructs a new instance of this class given a device and an
     * <code>RGBA</code> describing the desired red, green, blue &amp; alpha values.
     *
     * @param device the device on which to allocate the color
     * @param rgba the RGBA values of the desired color. Currently, SWT only honors extreme values for alpha i.e. 0 (transparent) or 255 (opaque).
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the rgba argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the red, green, blue or alpha components of the argument are not between 0 and 255</li>
     * </ul>
     *
     * @see #Color(RGBA) The equivalent constructor not requiring a Device
     *
     * @since 3.104
     */
    public DartColor(Device device, RGBA rgba, Color api) {
        super(device, api);
        if (rgba == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        init(rgba.rgb.red, rgba.rgb.green, rgba.rgb.blue, rgba.alpha);
        init();
    }

    /**
     * Constructs a new instance of this class given an
     * <code>RGBA</code> describing the desired red, green, blue &amp; alpha values.
     *
     * @param rgba the RGBA values of the desired color. Currently, SWT only honors extreme values for alpha i.e. 0 (transparent) or 255 (opaque).
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the rgba argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the red, green, blue or alpha components of the argument are not between 0 and 255</li>
     * </ul>
     *
     * @since 3.115
     */
    public DartColor(RGBA rgba, Color api) {
        super(api);
        if (rgba == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        init(rgba.rgb.red, rgba.rgb.green, rgba.rgb.blue, rgba.alpha);
    }

    /**
     * Constructs a new instance of this class given a device, an
     * <code>RGB</code> describing the desired red, green and blue values,
     * alpha specifying the level of transparency.
     *
     * @param device the device on which to allocate the color
     * @param rgb the RGB values of the desired color
     * @param alpha the alpha value of the desired color. Currently, SWT only honors extreme values for alpha i.e. 0 (transparent) or 255 (opaque).
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the rgb argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the red, green, blue or alpha components of the argument are not between 0 and 255</li>
     * </ul>
     *
     * @see #Color(RGB, int) The equivalent constructor not requiring a Device
     *
     * @since 3.104
     */
    public DartColor(Device device, RGB rgb, int alpha, Color api) {
        super(device, api);
        if (rgb == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        init(rgb.red, rgb.green, rgb.blue, alpha);
        init();
    }

    /**
     * Constructs a new instance of this class given an
     * <code>RGB</code> describing the desired red, green and blue values,
     * alpha specifying the level of transparency.
     *
     * @param rgb the RGB values of the desired color
     * @param alpha the alpha value of the desired color. Currently, SWT only honors extreme values for alpha i.e. 0 (transparent) or 255 (opaque).
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the rgb argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the red, green, blue or alpha components of the argument are not between 0 and 255</li>
     * </ul>
     *
     * @since 3.115
     */
    public DartColor(RGB rgb, int alpha, Color api) {
        super(api);
        if (rgb == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        init(rgb.red, rgb.green, rgb.blue, alpha);
    }

    @Override
    void destroy() {
        getApi().handle = -1;
    }

    /**
     * Colors do not need to be disposed, however to maintain compatibility
     * with older code, disposing a Color is not an error.
     */
    @Override
    public void dispose() {
        // Does as below to maintain API contract with Resource. Does
        // not use super.dispose() because that untracks the Color
        // from the Device tracking, however init() is overridden
        // to prevent the tracking in the first place.
        destroy();
        device = null;
    }

    /**
     * Returns the <code>Device</code> where this resource was
     * created. In cases where no <code>Device</code> was used
     * at creation, returns the current or default Device.
     *
     * <p>
     * As Color does not require a Device it is recommended to not
     * use {@link Color#getDevice()}.
     * </p>
     *
     * @return <code>Device</code> the device of the receiver
     * @since 3.2
     */
    @Override
    public Device getDevice() {
        // Fall back on Device.getDevice only if we haven't been disposed
        // already.
        if (this.device == null && this.getApi().handle != -1)
            return SwtDevice.getDevice();
        return super.getDevice();
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
        if (!(object instanceof Color))
            return false;
        Color color = (Color) object;
        if (isDisposed() || color.isDisposed()) {
            return false;
        }
        return (getApi().handle & 0xFFFFFF) == (color.handle & 0xFFFFFF) && (alpha == color.getImpl()._alpha());
    }

    /**
     * Returns the amount of alpha in the color, from 0 (transparent) to 255 (opaque).
     *
     * @return the alpha component of the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @since 3.104
     */
    public int getAlpha() {
        return this.alpha;
    }

    /**
     * Returns the amount of blue in the color, from 0 to 255.
     *
     * @return the blue component of the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int getBlue() {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return (getApi().handle & 0xFF0000) >> 16;
    }

    /**
     * Returns the amount of green in the color, from 0 to 255.
     *
     * @return the green component of the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int getGreen() {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return (getApi().handle & 0xFF00) >> 8;
    }

    /**
     * Returns the amount of red in the color, from 0 to 255.
     *
     * @return the red component of the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public int getRed() {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return getApi().handle & 0xFF;
    }

    /**
     * Returns an <code>RGB</code> representing the receiver.
     *
     * @return the RGB for the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public RGB getRGB() {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return new RGB(getApi().handle & 0xFF, (getApi().handle & 0xFF00) >> 8, (getApi().handle & 0xFF0000) >> 16);
    }

    /**
     * Returns an <code>RGBA</code> representing the receiver.
     *
     * @return the RGBA for the color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @since 3.104
     */
    public RGBA getRGBA() {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return new RGBA(getApi().handle & 0xFF, (getApi().handle & 0xFF00) >> 8, (getApi().handle & 0xFF0000) >> 16, alpha);
    }

    /**
     * Returns an integer hash code for the receiver. Any two
     * objects that return <code>true</code> when passed to
     * <code>equals</code> must return the same value for this
     * method.
     *
     * @return the receiver's hash
     *
     * @see #equals
     */
    @Override
    public int hashCode() {
        if (this.isDisposed())
            return 0;
        return getApi().handle ^ alpha;
    }

    /**
     * Allocates the operating system resources associated
     * with the receiver.
     *
     * @param device the device on which to allocate the color
     * @param red the amount of red in the color
     * @param green the amount of green in the color
     * @param blue the amount of blue in the color
     * @param alpha the amount of alpha in the color. Currently, SWT only honors extreme values for alpha i.e. 0 (transparent) or 255 (opaque).
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the red, green, blue or alpha argument is not between 0 and 255</li>
     * </ul>
     *
     * @see #dispose
     */
    void init(int red, int green, int blue, int alpha) {
        if (red > 255 || red < 0 || green > 255 || green < 0 || blue > 255 || blue < 0 || alpha > 255 || alpha < 0) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        getApi().handle = (red & 0xFF) | ((green & 0xFF) << 8) | ((blue & 0xFF) << 16);
        this.alpha = alpha;
    }

    @Override
    void init() {
        // Resource init simply tracks this resource in the Device
        // if DEBUG is on. Since Colors don't require disposal,
        // the tracking would be a memory leak and a misreport
        // on what resources are in use.
    }

    /**
     * Returns <code>true</code> if the color has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the color.
     * When a color has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the color.
     *
     * @return <code>true</code> when the color is disposed and <code>false</code> otherwise
     */
    @Override
    public boolean isDisposed() {
        return getApi().handle == -1;
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    @Override
    public String toString() {
        //$NON-NLS-1$
        if (isDisposed())
            return "Color {*DISPOSED*}";
        //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return "Color {" + getRed() + ", " + getGreen() + ", " + getBlue() + ", " + getAlpha() + "}";
    }

    public int _alpha() {
        return alpha;
    }

    public Color getApi() {
        if (api == null)
            api = Color.createApi(this);
        return (Color) api;
    }

    public VColor getValue() {
        if (value == null)
            value = new VColor(this);
        return (VColor) value;
    }
}
