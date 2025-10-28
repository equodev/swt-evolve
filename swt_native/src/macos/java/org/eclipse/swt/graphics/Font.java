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
import org.eclipse.swt.internal.cocoa.*;
import dev.equo.swt.Config;

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
public final class Font extends Resource {

    /**
     * the handle to the OS font resource
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
    public NSFont handle;

    /**
     * the traits not supported to the OS font resource
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
    public int extraTraits;

    Font(Device device) {
        this((IFont) null);
        setImpl(Config.isEquo(Font.class) ? new DartFont(device, this) : new SwtFont(device, this));
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
    public Font(Device device, FontData fd) {
        this((IFont) null);
        setImpl(Config.isEquo(Font.class) ? new DartFont(device, fd, this) : new SwtFont(device, fd, this));
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
    public Font(Device device, FontData[] fds) {
        this((IFont) null);
        setImpl(Config.isEquo(Font.class) ? new DartFont(device, fds, this) : new SwtFont(device, fds, this));
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
    public Font(Device device, String name, int height, int style) {
        this((IFont) null);
        setImpl(Config.isEquo(Font.class) ? new DartFont(device, name, height, style, this) : new SwtFont(device, name, height, style, this));
    }

    /*public*/
    Font(Device device, String name, float height, int style) {
        this((IFont) null);
        setImpl(Config.isEquo(Font.class) ? new DartFont(device, name, height, style, this) : new SwtFont(device, name, height, style, this));
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
        return getImpl().getFontData();
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
     * Returns <code>true</code> if the font has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the font.
     * When a font has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the font.
     *
     * @return <code>true</code> when the font is disposed and <code>false</code> otherwise
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

    protected Font(IFont impl) {
        super(impl);
    }

    static Font createApi(IFont impl) {
        return new Font(impl);
    }

    public IFont getImpl() {
        return (IFont) super.getImpl();
    }
}
