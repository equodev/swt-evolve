/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
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
import org.eclipse.swt.internal.win32.*;

/**
 * Instances of this class manage operating system resources that
 * define how text looks when it is displayed. Fonts may be constructed
 * by providing a device and either name, size and style information
 * or a <code>FontData</code> object which encapsulates this data.
 * <p>
 * Application code must explicitly invoke the <code>Font.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 *
 * @see FontData
 * @see <a href="http://www.eclipse.org/swt/snippets/#font">Font snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Examples: GraphicsExample, PaintExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class SwtFont extends SwtResource implements IFont {

    /**
     * The zoom in % of the standard resolution used for conversion of point height to pixel height
     * (Warning: This field is platform dependent)
     */
    int zoom;

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    SwtFont(Device device, Font api) {
        super(device, api);
        this.zoom = extractZoom(this.device);
    }

    /**
     * Constructs a new font given a device and font data
     * which describes the desired font's appearance.
     * <p>
     * You must dispose the font when it is no longer required.
     * </p>
     *
     * @param device the device to create the font on
     * @param fd the FontData that describes the desired font (must not be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the fd argument is null</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if a font could not be created from the given font data</li>
     * </ul>
     *
     * @see #dispose()
     */
    public SwtFont(Device device, FontData fd, Font api) {
        super(device, api);
        this.zoom = extractZoom(this.device);
        init(fd);
        init();
    }

    SwtFont(Device device, FontData fd, int zoom, Font api) {
        super(device, api);
        this.zoom = zoom;
        init(fd);
        init();
    }

    /**
     * Constructs a new font given a device and an array
     * of font data which describes the desired font's
     * appearance.
     * <p>
     * You must dispose the font when it is no longer required.
     * </p>
     *
     * @param device the device to create the font on
     * @param fds the array of FontData that describes the desired font (must not be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the fds argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the length of fds is zero</li>
     *    <li>ERROR_NULL_ARGUMENT - if any fd in the array is null</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if a font could not be created from the given font data</li>
     * </ul>
     *
     * @see #dispose()
     *
     * @since 2.1
     */
    public SwtFont(Device device, FontData[] fds, Font api) {
        super(device, api);
        if (fds == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (fds.length == 0)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        for (FontData fd : fds) {
            if (fd == null)
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.zoom = extractZoom(this.device);
        init(fds[0]);
        init();
    }

    /**
     * Constructs a new font given a device, a font name,
     * the height of the desired font in points, and a font
     * style.
     * <p>
     * You must dispose the font when it is no longer required.
     * </p>
     *
     * @param device the device to create the font on
     * @param name the name of the font (must not be null)
     * @param height the font height in points
     * @param style a bit or combination of NORMAL, BOLD, ITALIC
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the name argument is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the height is negative</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if a font could not be created from the given arguments</li>
     * </ul>
     *
     * @see #dispose()
     */
    public SwtFont(Device device, String name, int height, int style, Font api) {
        super(device, api);
        if (name == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        this.zoom = extractZoom(this.device);
        init(new FontData(name, height, style));
        init();
    }

    /*public*/
    SwtFont(Device device, String name, float height, int style, Font api) {
        super(device, api);
        if (name == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        this.zoom = extractZoom(this.device);
        init(new FontData(name, height, style));
        init();
    }

    @Override
    void destroy() {
        OS.DeleteObject(getApi().handle);
        getApi().handle = 0;
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
        if (!(object instanceof Font))
            return false;
        Font font = (Font) object;
        return device == font.getImpl()._device() && getApi().handle == font.handle;
    }

    /**
     * Returns an array of <code>FontData</code>s representing the receiver.
     * On Windows, only one FontData will be returned per font. On X however,
     * a <code>Font</code> object <em>may</em> be composed of multiple X
     * fonts. To support this case, we return an array of font data objects.
     *
     * @return an array of font data objects describing the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    public FontData[] getFontData() {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        LOGFONT logFont = new LOGFONT();
        OS.GetObject(getApi().handle, LOGFONT.sizeof, logFont);
        float heightInPoints = ((SwtDevice) device.getImpl()).computePoints(logFont, getApi().handle, DPIUtil.mapZoomToDPI(zoom));
        return new FontData[] { SwtFontData.win32_new(logFont, heightInPoints) };
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
        return (int) getApi().handle;
    }

    void init(FontData fd) {
        if (fd == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        LOGFONT logFont = fd.data;
        int lfHeight = logFont.lfHeight;
        logFont.lfHeight = ((SwtDevice) device.getImpl()).computePixels(fd.height);
        int primaryZoom = extractZoom(device);
        if (zoom != primaryZoom) {
            float scaleFactor = 1f * zoom / primaryZoom;
            logFont.lfHeight *= scaleFactor;
        }
        getApi().handle = OS.CreateFontIndirect(logFont);
        logFont.lfHeight = lfHeight;
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
    }

    /**
     * Returns <code>true</code> if the font has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the font.
     * When a font has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the font.
     *
     * @return <code>true</code> when the font is disposed and <code>false</code> otherwise
     */
    @Override
    public boolean isDisposed() {
        return getApi().handle == 0;
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
            return "Font {*DISPOSED*}";
        return "Font {" + getApi().handle + "}";
    }

    private static int extractZoom(Device device) {
        if (device == null) {
            return DPIUtil.getNativeDeviceZoom();
        }
        return DPIUtil.mapDPIToZoom(((SwtDevice) device.getImpl())._getDPIx());
    }

    /**
     * Invokes platform specific functionality to allocate a new font.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Font</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param device the device on which to allocate the color
     * @param handle the handle for the font
     * @return a new font object containing the specified device and handle
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    public static Font win32_new(Device device, long handle) {
        Font font = new Font(device);
        if (font.getImpl() instanceof DartFont) {
            ((DartFont) font.getImpl()).zoom = extractZoom(font.getImpl()._device());
        }
        if (font.getImpl() instanceof SwtFont) {
            ((SwtFont) font.getImpl()).zoom = extractZoom(font.getImpl()._device());
        }
        font.handle = handle;
        ///*	 * When created this way, Font doesn't own its .handle, and	 * for this reason it can't be disposed. Tell leak detector	 * to just ignore it.	 */font.ignoreNonDisposed();
        ;
        return font;
    }

    /**
     * Invokes platform specific functionality to allocate a new font.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Font</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param device the device on which to allocate the font
     * @param handle the handle for the font
     * @param zoom zoom in % of the standard resolution
     * @return a new font object containing the specified device and handle
     *
     * @noreference This method is not intended to be referenced by clients.
     * @since 3.126
     */
    public static Font win32_new(Device device, long handle, int zoom) {
        Font font = win32_new(device, handle);
        if (font.getImpl() instanceof DartFont) {
            ((DartFont) font.getImpl()).zoom = zoom;
        }
        if (font.getImpl() instanceof SwtFont) {
            ((SwtFont) font.getImpl()).zoom = zoom;
        }
        return font;
    }

    /**
     * Invokes platform specific private constructor to allocate a new font.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Font</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param device the device on which to allocate the font
     * @param fontData font data to create the font for
     * @param zoom zoom in % of the standard resolution
     * @return a new font object using the specified font data with the
     * specified zoom as factor for the font data
     *
     * @noreference This method is not intended to be referenced by clients.
     * @since 3.126
     */
    public static Font win32_new(Device device, FontData fontData, int zoom) {
        return new Font(device, fontData, zoom);
    }

    /**
     * Used to receive a font for the given zoom in the context
     * of the current configuration of SWT at runtime.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Font</code>. It is marked public only so that it
     * can be shared within the packages provided by SWT. It is not
     * available on all platforms, and should never be called from
     * application code.
     * </p>
     *
     * @param font font to create a font for the given target zoom
     * @param targetZoom zoom in % of the standard resolution
     * @return a font matching the specified font and zoom in %
     *
     * @noreference This method is not intended to be referenced by clients.
     * @since 3.126
     */
    public static Font win32_new(Font font, int targetZoom) {
        if (targetZoom == font.getImpl()._zoom()) {
            return font;
        }
        return SWTFontProvider.getFont(font.getDevice(), font.getFontData()[0], targetZoom);
    }

    public int _zoom() {
        return zoom;
    }

    public Font getApi() {
        if (api == null)
            api = Font.createApi(this);
        return (Font) api;
    }
}
