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
 *      Karsten Thoms <karsten.thoms@itemis.de> - Bug 522349
 * *****************************************************************************
 */
package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cocoa.*;

/**
 * Instances of this class manage operating system resources that
 * specify the appearance of the on-screen pointer. To create a
 * cursor you specify the device and either a simple cursor style
 * describing one of the standard operating system provided cursors
 * or the image and mask data for the desired appearance.
 * <p>
 * Application code must explicitly invoke the <code>Cursor.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>
 *   CURSOR_ARROW, CURSOR_WAIT, CURSOR_CROSS, CURSOR_APPSTARTING, CURSOR_HELP,
 *   CURSOR_SIZEALL, CURSOR_SIZENESW, CURSOR_SIZENS, CURSOR_SIZENWSE, CURSOR_SIZEWE,
 *   CURSOR_SIZEN, CURSOR_SIZES, CURSOR_SIZEE, CURSOR_SIZEW, CURSOR_SIZENE, CURSOR_SIZESE,
 *   CURSOR_SIZESW, CURSOR_SIZENW, CURSOR_UPARROW, CURSOR_IBEAM, CURSOR_NO, CURSOR_HAND
 * </dd>
 * </dl>
 * <p>
 * Note: Only one of the above styles may be specified.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#cursor">Cursor snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class Cursor extends Resource {

    /**
     * the handle to the OS cursor resource
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
    public NSCursor handle;

    /**
     * Constructs a new cursor given a device and a style
     * constant describing the desired cursor appearance.
     * <p>
     * You must dispose the cursor when it is no longer required.
     * </p>
     * NOTE:
     * It is recommended to use {@link org.eclipse.swt.widgets.Display#getSystemCursor(int)}
     * instead of using this constructor. This way you can avoid the
     * overhead of disposing the Cursor resource.
     *
     * @param device the device on which to allocate the cursor
     * @param style the style of cursor to allocate
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_INVALID_ARGUMENT - when an unknown style is specified</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if a handle could not be obtained for cursor creation</li>
     * </ul>
     *
     * @see SWT#CURSOR_ARROW
     * @see SWT#CURSOR_WAIT
     * @see SWT#CURSOR_CROSS
     * @see SWT#CURSOR_APPSTARTING
     * @see SWT#CURSOR_HELP
     * @see SWT#CURSOR_SIZEALL
     * @see SWT#CURSOR_SIZENESW
     * @see SWT#CURSOR_SIZENS
     * @see SWT#CURSOR_SIZENWSE
     * @see SWT#CURSOR_SIZEWE
     * @see SWT#CURSOR_SIZEN
     * @see SWT#CURSOR_SIZES
     * @see SWT#CURSOR_SIZEE
     * @see SWT#CURSOR_SIZEW
     * @see SWT#CURSOR_SIZENE
     * @see SWT#CURSOR_SIZESE
     * @see SWT#CURSOR_SIZESW
     * @see SWT#CURSOR_SIZENW
     * @see SWT#CURSOR_UPARROW
     * @see SWT#CURSOR_IBEAM
     * @see SWT#CURSOR_NO
     * @see SWT#CURSOR_HAND
     * @see #dispose()
     */
    public Cursor(Device device, int style) {
        this(new nat.org.eclipse.swt.graphics.Cursor((nat.org.eclipse.swt.graphics.Device) device.getDelegate(), style));
    }

    /**
     * Constructs a new cursor given a device, image and mask
     * data describing the desired cursor appearance, and the x
     * and y coordinates of the <em>hotspot</em> (that is, the point
     * within the area covered by the cursor which is considered
     * to be where the on-screen pointer is "pointing").
     * <p>
     * The mask data is allowed to be null, but in this case the source
     * must be an ImageData representing an icon that specifies both
     * color data and mask data.
     * <p>
     * You must dispose the cursor when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the cursor
     * @param source the color data for the cursor
     * @param mask the mask data for the cursor (or null)
     * @param hotspotX the x coordinate of the cursor's hotspot
     * @param hotspotY the y coordinate of the cursor's hotspot
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the source is null</li>
     *    <li>ERROR_NULL_ARGUMENT - if the mask is null and the source does not have a mask</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the source and the mask are not the same
     *          size, or if the hotspot is outside the bounds of the image</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if a handle could not be obtained for cursor creation</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Cursor(Device device, ImageData source, ImageData mask, int hotspotX, int hotspotY) {
        this(new nat.org.eclipse.swt.graphics.Cursor((nat.org.eclipse.swt.graphics.Device) device.getDelegate(), source, mask, hotspotX, hotspotY));
    }

    /**
     * Constructs a new cursor given a device, image data describing
     * the desired cursor appearance, and the x and y coordinates of
     * the <em>hotspot</em> (that is, the point within the area
     * covered by the cursor which is considered to be where the
     * on-screen pointer is "pointing").
     * <p>
     * You must dispose the cursor when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the cursor
     * @param source the image data for the cursor
     * @param hotspotX the x coordinate of the cursor's hotspot
     * @param hotspotY the y coordinate of the cursor's hotspot
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the image is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the hotspot is outside the bounds of the
     * 		 image</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES - if a handle could not be obtained for cursor creation</li>
     * </ul>
     *
     * @see #dispose()
     *
     * @since 3.0
     */
    public Cursor(Device device, ImageData source, int hotspotX, int hotspotY) {
        this(new nat.org.eclipse.swt.graphics.Cursor((nat.org.eclipse.swt.graphics.Device) device.getDelegate(), source, hotspotX, hotspotY));
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
        return getDelegate().equals(object);
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
        return getDelegate().hashCode();
    }

    /**
     * Returns <code>true</code> if the cursor has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the cursor.
     * When a cursor has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the cursor.
     *
     * @return <code>true</code> when the cursor is disposed and <code>false</code> otherwise
     */
    @Override
    public boolean isDisposed() {
        return getDelegate().isDisposed();
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    @Override
    public String toString() {
        return getDelegate().toString();
    }

    protected Cursor(ICursor delegate) {
        super(delegate);
    }

    public ICursor getDelegate() {
        return (ICursor) super.getDelegate();
    }
}
