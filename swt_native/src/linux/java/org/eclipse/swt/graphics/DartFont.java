/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2017 IBM Corporation and others.
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
public final class DartFont extends DartResource implements IFont {

    DartFont(Device device, Font api) {
        super(device, api);
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
    public DartFont(Device device, FontData fd, Font api) {
        super(device, api);
        if (fd == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        init(fd.getName(), ((DartFontData) fd.getImpl()).getHeightF(), fd.getStyle(), fd.string);
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
    public DartFont(Device device, FontData[] fds, Font api) {
        super(device, api);
        if (fds == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (fds.length == 0)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        for (int i = 0; i < fds.length; i++) {
            if (fds[i] == null)
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        FontData fd = fds[0];
        init(fd.getName(), ((DartFontData) fd.getImpl()).getHeightF(), fd.getStyle(), fd.string);
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
    public DartFont(Device device, String name, int height, int style, Font api) {
        super(device, api);
        init(name, height, style, null);
        init();
    }

    /*public*/
    DartFont(Device device, String name, float height, int style, Font api) {
        super(device, api);
        init(name, height, style, null);
        init();
    }

    @Override
    void destroy() {
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
        return getApi().handle == ((Font) object).handle;
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
        return this.fontData;
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

    void init(String name, float height, int style, byte[] fontString) {
        if (name == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (height < 0)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        Point dpi = ((SwtDevice) device.getImpl()).dpi, screenDPI = ((SwtDevice) device.getImpl()).getScreenDPI();
        float size = height * dpi.y / screenDPI.y;
        fontData[0] = new FontData(name, height, style);
        getApi().handle = 1;
        if (fontString != null) {
            if (getApi().handle == 0)
                SWT.error(SWT.ERROR_NO_HANDLES);
        } else {
            if (getApi().handle == 0)
                SWT.error(SWT.ERROR_NO_HANDLES);
            if (size > 0) {
            }
        }
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

    FontData[] fontData = new FontData[1];

    public FontData[] _fontData() {
        return fontData;
    }

    public Font getApi() {
        if (api == null)
            api = Font.createApi(this);
        return (Font) api;
    }

    public VFont getValue() {
        if (value == null)
            value = new VFont(this);
        return (VFont) value;
    }
}
