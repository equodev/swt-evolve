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
import dev.equo.swt.Config;

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
public final class Color extends Resource {

    /**
     * the handle to the OS color resource
     * (Warning: This field is platform dependent)
     * <p>
     * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
     * public API. It is marked public only so that it can be shared
     * within the packages provided by SWT. It is not available on all
     * platforms and should never be accessed from application code.
     * </p>
     *
     * @noreference This field is not intended to be referenced by clients.
     */
    public int handle;

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    Color() {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(this) : new SwtColor(this));
    }

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    Color(Device device) {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(device, this) : new SwtColor(device, this));
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
    public Color(Device device, int red, int green, int blue) {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(device, red, green, blue, this) : new SwtColor(device, red, green, blue, this));
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
    public Color(int red, int green, int blue) {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(red, green, blue, this) : new SwtColor(red, green, blue, this));
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
    public Color(Device device, int red, int green, int blue, int alpha) {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(device, red, green, blue, alpha, this) : new SwtColor(device, red, green, blue, alpha, this));
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
    public Color(int red, int green, int blue, int alpha) {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(red, green, blue, alpha, this) : new SwtColor(red, green, blue, alpha, this));
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
    public Color(Device device, RGB rgb) {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(device, rgb, this) : new SwtColor(device, rgb, this));
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
    public Color(RGB rgb) {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(rgb, this) : new SwtColor(rgb, this));
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
    public Color(Device device, RGBA rgba) {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(device, rgba, this) : new SwtColor(device, rgba, this));
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
    public Color(RGBA rgba) {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(rgba, this) : new SwtColor(rgba, this));
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
    public Color(Device device, RGB rgb, int alpha) {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(device, rgb, alpha, this) : new SwtColor(device, rgb, alpha, this));
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
    public Color(RGB rgb, int alpha) {
        this((IColor) null);
        setImpl(Config.isEquo(Color.class) ? new DartColor(rgb, alpha, this) : new SwtColor(rgb, alpha, this));
    }

    /**
     * Colors do not need to be disposed, however to maintain compatibility
     * with older code, disposing a Color is not an error.
     */
    public void dispose() {
        getImpl().dispose();
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
    public Device getDevice() {
        return getImpl().getDevice();
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
    public boolean equals(Object object) {
        return getImpl().equals(object);
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
        return getImpl().getAlpha();
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
        return getImpl().getBlue();
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
        return getImpl().getGreen();
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
        return getImpl().getRed();
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
        return getImpl().getRGB();
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
        return getImpl().getRGBA();
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
    public int hashCode() {
        return getImpl().hashCode();
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

    protected Color(IColor impl) {
        super(impl);
    }

    static Color createApi(IColor impl) {
        return new Color(impl);
    }

    public IColor getImpl() {
        return (IColor) super.getImpl();
    }
}
