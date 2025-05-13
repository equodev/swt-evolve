package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public interface IDevice {

    /**
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @since 3.115
     */
    boolean isTracking();

    /**
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     * @since 3.115
     */
    void setTracking(boolean tracking);

    /**
     * Disposes of the operating system resources associated with
     * the receiver. After this method has been invoked, the receiver
     * will answer <code>true</code> when sent the message
     * <code>isDisposed()</code>.
     *
     * @see #release
     * @see #destroy
     * @see #checkDevice
     */
    void dispose();

    void dispose_Object(Object object);

    /**
     * Returns a rectangle describing the receiver's size and location.
     *
     * @return the bounding rectangle
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Rectangle getBounds();

    /**
     * Returns a <code>DeviceData</code> based on the receiver.
     * Modifications made to this <code>DeviceData</code> will not
     * affect the receiver.
     *
     * @return a <code>DeviceData</code> containing the device's data and attributes
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see DeviceData
     */
    DeviceData getDeviceData();

    /**
     * Returns a rectangle which describes the area of the
     * receiver which is capable of displaying data.
     *
     * @return the client area
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see #getBounds
     */
    Rectangle getClientArea();

    /**
     * Returns the bit depth of the screen, which is the number of
     * bits it takes to represent the number of unique colors that
     * the screen is currently capable of displaying. This number
     * will typically be one of 1, 8, 15, 16, 24 or 32.
     *
     * @return the depth of the screen
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    int getDepth();

    /**
     * Returns a point whose x coordinate is the logical horizontal
     * dots per inch of the display, and whose y coordinate
     * is the logical vertical dots per inch of the display.
     *
     * @return the horizontal and vertical DPI
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    Point getDPI();

    /**
     * Returns <code>FontData</code> objects which describe
     * the fonts that match the given arguments. If the
     * <code>faceName</code> is null, all fonts will be returned.
     *
     * @param faceName the name of the font to look for, or null
     * @param scalable if true only scalable fonts are returned, otherwise only non-scalable fonts are returned.
     * @return the matching font data
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    FontData[] getFontList(String faceName, boolean scalable);

    /**
     * Returns the matching standard color for the given
     * constant, which should be one of the color constants
     * specified in class <code>SWT</code>. Any value other
     * than one of the SWT color constants which is passed
     * in will result in the color black. This color should
     * not be freed because it was allocated by the system,
     * not the application.
     *
     * @param id the color constant
     * @return the matching color
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see SWT
     */
    IColor getSystemColor(int id);

    /**
     * Returns a reasonable font for applications to use.
     * On some platforms, this will match the "default font"
     * or "system font" if such can be found.  This font
     * should not be freed because it was allocated by the
     * system, not the application.
     * <p>
     * Typically, applications which want the default look
     * should simply not set the font on the widgets they
     * create. Widgets are always created with the correct
     * default font for the class of user-interface component
     * they represent.
     * </p>
     *
     * @return a font
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    IFont getSystemFont();

    /**
     * Returns <code>true</code> if the underlying window system prints out
     * warning messages on the console, and <code>setWarnings</code>
     * had previously been called with <code>true</code>.
     *
     * @return <code>true</code>if warnings are being handled, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    boolean getWarnings();

    /**
     * Invokes platform specific functionality to allocate a new GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Device</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param data the platform specific GC data
     * @return the platform specific GC handle
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    abstract long internal_new_GC(GCData data);

    /**
     * Invokes platform specific functionality to dispose a GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Device</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param hDC the platform specific GC handle
     * @param data the platform specific GC data
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    abstract void internal_dispose_GC(long hDC, GCData data);

    /**
     * Returns <code>true</code> if the device has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the device.
     * When a device has been disposed, it is an error to
     * invoke any other method using the device.
     *
     * @return <code>true</code> when the device is disposed and <code>false</code> otherwise
     */
    boolean isDisposed();

    /**
     * Loads the font specified by a file.  The font will be
     * present in the list of fonts available to the application.
     *
     * @param path the font file path
     * @return whether the font was successfully loaded
     *
     * @exception SWTException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if path is null</li>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     *
     * @see Font
     *
     * @since 3.3
     */
    boolean loadFont(String path);

    void new_Object(Object object);

    /**
     * If the underlying window system supports printing warning messages
     * to the console, setting warnings to <code>false</code> prevents these
     * messages from being printed. If the argument is <code>true</code> then
     * message printing is not blocked.
     *
     * @param warnings <code>true</code>if warnings should be printed, and <code>false</code> otherwise
     *
     * @exception SWTException <ul>
     *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void setWarnings(boolean warnings);

    Device getApi();

    void setApi(Device api);
}
