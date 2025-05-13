package org.eclipse.swt.graphics;

import java.io.*;
import org.eclipse.swt.*;

public interface IImage extends IResource {

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
    boolean equals(Object object);

    /**
     * Returns the color to which to map the transparent pixel, or null if
     * the receiver has no transparent pixel.
     * <p>
     * There are certain uses of Images that do not support transparency
     * (for example, setting an image into a button or label). In these cases,
     * it may be desired to simulate transparency by using the background
     * color of the widget to paint the transparent pixels of the image.
     * Use this method to check which color will be used in these cases
     * in place of transparency. This value may be set with setBackground().
     * </p>
     *
     * @return the background color of the image, or null if there is no transparency in the image
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    IColor getBackground();

    /**
     * Returns the bounds of the receiver. The rectangle will always
     * have x and y values of 0, and the width and height of the
     * image.
     *
     * @return a rectangle specifying the image's bounds in points.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
     * </ul>
     */
    IRectangle getBounds();

    /**
     * Returns the bounds of the receiver. The rectangle will always
     * have x and y values of 0, and the width and height of the
     * image in pixels.
     *
     * @return a rectangle specifying the image's bounds in pixels.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
     * </ul>
     * @since 3.105
     * @deprecated This API doesn't serve the purpose in an environment having
     *             multiple monitors with different DPIs, hence deprecated.
     */
    IRectangle getBoundsInPixels();

    /**
     * Returns an <code>ImageData</code> based on the receiver.
     * Modifications made to this <code>ImageData</code> will not
     * affect the Image.
     *
     * @return an <code>ImageData</code> containing the image's data and
     *         attributes at 100% zoom level.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
     * </ul>
     *
     * @see ImageData
     */
    ImageData getImageData();

    /**
     * Returns an <code>ImageData</code> based on the receiver.
     * Modifications made to this <code>ImageData</code> will not
     * affect the Image.
     *
     * @return an <code>ImageData</code> containing the image's data
     * and attributes at the current zoom level.
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
     * </ul>
     *
     * @see ImageData
     * @since 3.105
     * @deprecated This API doesn't serve the purpose in an environment having
     *             multiple monitors with different DPIs, hence deprecated. Use
     *             {@link #getImageData(int)} instead.
     */
    ImageData getImageDataAtCurrentZoom();

    /**
     * Returns an {@link ImageData} for the given zoom level based on the
     * receiver.
     * <p>
     * Note that this method is mainly intended to be used by custom
     * implementations of {@link ImageDataProvider} that draw a composite image
     * at the requested zoom level based on other images. For custom zoom
     * levels, the image data may be an auto-scaled version of the native image
     * and may look more blurred or mangled than expected.
     * </p>
     * <p>
     * Modifications made to the returned {@code ImageData} will not affect this
     * {@code Image}.
     * </p>
     *
     * @param zoom
     *            The zoom level in % of the standard resolution (which is 1
     *            physical monitor pixel == 1 SWT logical point). Typically 100,
     *            150, or 200.
     * @return an <code>ImageData</code> containing the image's data and
     *         attributes at the given zoom level
     *
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
     * </ul>
     *
     * @since 3.106
     */
    ImageData getImageData(int zoom);

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
    int hashCode();

    /**
     * Invokes platform specific functionality to allocate a new GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Image</code>. It is marked public only so that it
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
    long internal_new_GC(org.eclipse.swt.graphics.GCData data);

    /**
     * Invokes platform specific functionality to dispose a GC handle.
     * <p>
     * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
     * API for <code>Image</code>. It is marked public only so that it
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
    void internal_dispose_GC(long hDC, org.eclipse.swt.graphics.GCData data);

    /**
     * Returns <code>true</code> if the image has been disposed,
     * and <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the image.
     * When an image has been disposed, it is an error to
     * invoke any other method (except {@link #dispose()}) using the image.
     *
     * @return <code>true</code> when the image is disposed and <code>false</code> otherwise
     */
    boolean isDisposed();

    /**
     * Sets the color to which to map the transparent pixel.
     * <p>
     * There are certain uses of <code>Images</code> that do not support
     * transparency (for example, setting an image into a button or label).
     * In these cases, it may be desired to simulate transparency by using
     * the background color of the widget to paint the transparent pixels
     * of the image. This method specifies the color that will be used in
     * these cases. For example:</p>
     * <pre>
     *    Button b = new Button();
     *    image.setBackground(b.getBackground());
     *    b.setImage(image);
     * </pre>
     * <p>
     * The image may be modified by this operation (in effect, the
     * transparent regions may be filled with the supplied color).  Hence
     * this operation is not reversible and it is not legal to call
     * this function twice or with a null argument.
     * </p><p>
     * This method has no effect if the receiver does not have a transparent
     * pixel value.
     * </p>
     *
     * @param color the color to use when a transparent pixel is specified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the color is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the color has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
     * </ul>
     */
    void setBackground(IColor color);

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the receiver
     */
    String toString();

    Image getApi();
}
