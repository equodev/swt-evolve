/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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
public final class DartCursor extends DartResource implements ICursor {

    DartCursor(Device device, Cursor api) {
        super(device, api);
    }

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
    public DartCursor(Device device, int style, Cursor api) {
        super(device, api);
        if (style < 0 || style > SWT.CURSOR_HAND)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        this.cursorStyle = style;
        getApi().handle = style + 1;
        init();
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
     *
     * @deprecated Use {@link #Cursor(Device, ImageDataProvider, int, int)} instead.
     */
    @Deprecated
    public DartCursor(Device device, ImageData source, ImageData mask, int hotspotX, int hotspotY, Cursor api) {
        super(device, api);
        if (source == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (mask == null) {
            if (!(source.getTransparencyType() == SWT.TRANSPARENCY_MASK))
                SWT.error(SWT.ERROR_NULL_ARGUMENT);
            mask = source.getTransparencyMask();
        }
        /* Check the bounds. Mask must be the same size as source */
        if (mask.width != source.width || mask.height != source.height) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        /* Check the hotspots */
        if (hotspotX >= source.width || hotspotX < 0 || hotspotY >= source.height || hotspotY < 0) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        /* Convert depth to 1 */
        source = ImageData.convertMask(source);
        mask = ImageData.convertMask(mask);
        /* Swap the bits in each byte and convert to appropriate scanline pad */
        byte[] sourceData = new byte[source.data.length];
        byte[] maskData = new byte[mask.data.length];
        byte[] data = source.data;
        for (int i = 0; i < data.length; i++) {
            byte s = data[i];
            sourceData[i] = (byte) (((s & 0x80) >> 7) | ((s & 0x40) >> 5) | ((s & 0x20) >> 3) | ((s & 0x10) >> 1) | ((s & 0x08) << 1) | ((s & 0x04) << 3) | ((s & 0x02) << 5) | ((s & 0x01) << 7));
            sourceData[i] = (byte) ~sourceData[i];
        }
        sourceData = ImageData.convertPad(sourceData, source.width, source.height, source.depth, source.scanlinePad, 1);
        data = mask.data;
        for (int i = 0; i < data.length; i++) {
            byte s = data[i];
            maskData[i] = (byte) (((s & 0x80) >> 7) | ((s & 0x40) >> 5) | ((s & 0x20) >> 3) | ((s & 0x10) >> 1) | ((s & 0x08) << 1) | ((s & 0x04) << 3) | ((s & 0x02) << 5) | ((s & 0x01) << 7));
            maskData[i] = (byte) ~maskData[i];
        }
        maskData = ImageData.convertPad(maskData, mask.width, mask.height, mask.depth, mask.scanlinePad, 1);
        getApi().handle = createCursor(sourceData, maskData, source.width, source.height, hotspotX, hotspotY, true);
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
        init();
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
    public DartCursor(Device device, ImageData source, int hotspotX, int hotspotY, Cursor api) {
        super(device, api);
        setupCursorFromImageData(source, hotspotX, hotspotY);
    }

    private void setupCursorFromImageData(ImageData source, int hotspotX, int hotspotY) {
        if (source == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (hotspotX >= source.width || hotspotX < 0 || hotspotY >= source.height || hotspotY < 0) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        int width = source.width;
        int height = source.height;
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_NO_HANDLES);
        init();
    }

    /**
     * Constructs a new cursor given a device, image describing
     * the desired cursor appearance, and the x and y coordinates of
     * the <em>hotspot</em> (that is, the point within the area
     * covered by the cursor which is considered to be where the
     * on-screen pointer is "pointing").
     * <p>
     * You must dispose the cursor when it is no longer required.
     * </p>
     *
     * @param device the device on which to allocate the cursor
     * @param imageDataProvider the ImageDataProvider for the cursor
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
     * @since 3.131
     */
    public DartCursor(Device device, ImageDataProvider imageDataProvider, int hotspotX, int hotspotY, Cursor api) {
        super(device, api);
        if (imageDataProvider == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        setupCursorFromImageData(imageDataProvider.getImageData(100), hotspotX, hotspotY);
    }

    long createCursor(byte[] sourceData, byte[] maskData, int width, int height, int hotspotX, int hotspotY, boolean reverse) {
        for (int i = 0; i < sourceData.length; i++) {
            byte s = sourceData[i];
            sourceData[i] = (byte) (((s & 0x80) >> 7) | ((s & 0x40) >> 5) | ((s & 0x20) >> 3) | ((s & 0x10) >> 1) | ((s & 0x08) << 1) | ((s & 0x04) << 3) | ((s & 0x02) << 5) | ((s & 0x01) << 7));
            sourceData[i] = (byte) ~sourceData[i];
        }
        for (int i = 0; i < maskData.length; i++) {
            byte s = maskData[i];
            maskData[i] = (byte) (((s & 0x80) >> 7) | ((s & 0x40) >> 5) | ((s & 0x20) >> 3) | ((s & 0x10) >> 1) | ((s & 0x08) << 1) | ((s & 0x04) << 3) | ((s & 0x02) << 5) | ((s & 0x01) << 7));
            maskData[i] = (byte) ~maskData[i];
        }
        PaletteData palette = new PaletteData(new RGB(0, 0, 0), new RGB(255, 255, 255));
        ImageData source = new ImageData(width, height, 1, palette, 1, sourceData);
        ImageData mask = new ImageData(width, height, 1, palette, 1, maskData);
        byte[] data = new byte[source.width * source.height * 4];
        for (int y = 0; y < source.height; y++) {
            int offset = y * source.width * 4;
            for (int x = 0; x < source.width; x++) {
                int pixel = source.getPixel(x, y);
                int maskPixel = mask.getPixel(x, y);
                if (pixel == 0 && maskPixel == 0) {
                    // BLACK
                    data[offset + 3] = (byte) 0xFF;
                } else if (pixel == 0 && maskPixel == 1) {
                    // WHITE - cursor color
                    data[offset] = data[offset + 1] = data[offset + 2] = data[offset + 3] = (byte) 0xFF;
                } else if (pixel == 1 && maskPixel == 0) {
                    // SCREEN
                } else {
                    /* no support */
                    // REVERSE SCREEN -> SCREEN
                }
                offset += 4;
            }
        }
        return 0;
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
        if (!(object instanceof Cursor))
            return false;
        Cursor cursor = (Cursor) object;
        return device == cursor.getImpl()._device() && getApi().handle == cursor.handle;
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
            return "Cursor {*DISPOSED*}";
        return "Cursor {}";
    }

    int cursorStyle = -1;

    Image image;

    public int _cursorStyle() {
        return cursorStyle;
    }

    public Image _image() {
        return image;
    }

    public Cursor getApi() {
        if (api == null)
            api = Cursor.createApi(this);
        return (Cursor) api;
    }

    public VCursor getValue() {
        if (value == null)
            value = new VCursor(this);
        return (VCursor) value;
    }
}
