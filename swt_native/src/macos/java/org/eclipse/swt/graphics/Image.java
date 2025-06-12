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

import java.io.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.internal.graphics.*;

/**
 * Instances of this class are graphics which have been prepared
 * for display on a specific device. That is, they are ready
 * to paint using methods such as <code>GC.drawImage()</code>
 * and display on widgets with, for example, <code>Button.setImage()</code>.
 * <p>
 * If loaded from a file format that supports it, an
 * <code>Image</code> may have transparency, meaning that certain
 * pixels are specified as being transparent when drawn. Examples
 * of file formats that support transparency are GIF and PNG.
 * </p><p>
 * There are two primary ways to use <code>Images</code>.
 * The first is to load a graphic file from disk and create an
 * <code>Image</code> from it. This is done using an <code>Image</code>
 * constructor, for example:
 * <pre>
 *    Image i = new Image(device, "C:\\graphic.bmp");
 * </pre>
 * A graphic file may contain a color table specifying which
 * colors the image was intended to possess. In the above example,
 * these colors will be mapped to the closest available color in
 * SWT. It is possible to get more control over the mapping of
 * colors as the image is being created, using code of the form:
 * <pre>
 *    ImageData data = new ImageData("C:\\graphic.bmp");
 *    RGB[] rgbs = data.getRGBs();
 *    // At this point, rgbs contains specifications of all
 *    // the colors contained within this image. You may
 *    // allocate as many of these colors as you wish by
 *    // using the Color constructor Color(RGB), then
 *    // create the image:
 *    Image i = new Image(device, data);
 * </pre>
 * <p>
 * Applications which require even greater control over the image
 * loading process should use the support provided in class
 * <code>ImageLoader</code>.
 * </p><p>
 * Application code must explicitly invoke the <code>Image.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 *
 * @see Color
 * @see ImageData
 * @see ImageLoader
 * @see <a href="http://www.eclipse.org/swt/snippets/#image">Image snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Examples: GraphicsExample, ImageAnalyzer</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class Image extends Resource implements Drawable {

    /**
     * specifies whether the receiver is a bitmap or an icon
     * (one of <code>SWT.BITMAP</code>, <code>SWT.ICON</code>)
     * <p>
     * <b>IMPORTANT:</b> This field is <em>not</em> part of the SWT
     * public API. It is marked public only so that it can be shared
     * within the packages provided by SWT. It is not available on all
     * platforms and should never be accessed from application code.
     * </p>
     *
     * @noreference This field is not intended to be referenced by clients.
     */
    public int type;

    /**
     * the handle to the OS image resource
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
    public NSImage handle;

    /**
     * The global alpha value to be used for every pixel.
     */
    Image(Device device) {
        this((IImage) null);
        setImpl(new SwtImage(device, this));
    }

    /**
     * Constructs an empty instance of this class with the
     * specified width and height. The result may be drawn upon
     * by creating a GC and using any of its drawing operations,
     * as shown in the following example:
     * <pre>
     *    Image i = new Image(device, width, height);
     *    GC gc = new GC(i);
     *    gc.drawRectangle(0, 0, 50, 50);
     *    gc.dispose();
     * </pre>
     * <p>
     * Note: Some platforms may have a limitation on the size
     * of image that can be created (size depends on width, height,
     * and depth). For example, Windows 95, 98, and ME do not allow
     * images larger than 16M.
     * </p>
     * <p>
     * You must dispose the image when it is no longer required.
     * </p>
     *
     * @param device the device on which to create the image
     * @param width the width of the new image
     * @param height the height of the new image
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_INVALID_ARGUMENT - if either the width or height is negative or zero</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Image(Device device, int width, int height) {
        this((IImage) null);
        setImpl(new SwtImage(device, width, height, this));
    }

    /**
     * Constructs a new instance of this class based on the
     * provided image, with an appearance that varies depending
     * on the value of the flag. The possible flag values are:
     * <dl>
     * <dt><b>{@link SWT#IMAGE_COPY}</b></dt>
     * <dd>the result is an identical copy of srcImage</dd>
     * <dt><b>{@link SWT#IMAGE_DISABLE}</b></dt>
     * <dd>the result is a copy of srcImage which has a <em>disabled</em> look</dd>
     * <dt><b>{@link SWT#IMAGE_GRAY}</b></dt>
     * <dd>the result is a copy of srcImage which has a <em>gray scale</em> look</dd>
     * </dl>
     * <p>
     * You must dispose the image when it is no longer required.
     * </p>
     *
     * @param device the device on which to create the image
     * @param srcImage the image to use as the source
     * @param flag the style, either <code>IMAGE_COPY</code>, <code>IMAGE_DISABLE</code> or <code>IMAGE_GRAY</code>
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if srcImage is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the flag is not one of <code>IMAGE_COPY</code>, <code>IMAGE_DISABLE</code> or <code>IMAGE_GRAY</code></li>
     *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon, or is otherwise in an invalid state</li>
     *    <li>ERROR_UNSUPPORTED_DEPTH - if the depth of the image is not supported</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Image(Device device, Image srcImage, int flag) {
        this((IImage) null);
        setImpl(new SwtImage(device, srcImage, flag, this));
    }

    /**
     * Constructs an empty instance of this class with the
     * width and height of the specified rectangle. The result
     * may be drawn upon by creating a GC and using any of its
     * drawing operations, as shown in the following example:
     * <pre>
     *    Image i = new Image(device, boundsRectangle);
     *    GC gc = new GC(i);
     *    gc.drawRectangle(0, 0, 50, 50);
     *    gc.dispose();
     * </pre>
     * <p>
     * Note: Some platforms may have a limitation on the size
     * of image that can be created (size depends on width, height,
     * and depth). For example, Windows 95, 98, and ME do not allow
     * images larger than 16M.
     * </p>
     * <p>
     * You must dispose the image when it is no longer required.
     * </p>
     *
     * @param device the device on which to create the image
     * @param bounds a rectangle specifying the image's width and height (must not be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the bounds rectangle is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if either the rectangle's width or height is negative</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Image(Device device, Rectangle bounds) {
        this((IImage) null);
        setImpl(new SwtImage(device, bounds, this));
    }

    /**
     * Constructs an instance of this class from the given
     * <code>ImageData</code>.
     * <p>
     * You must dispose the image when it is no longer required.
     * </p>
     *
     * @param device the device on which to create the image
     * @param data the image data to create the image from (must not be null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the image data is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_UNSUPPORTED_DEPTH - if the depth of the ImageData is not supported</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Image(Device device, ImageData data) {
        this((IImage) null);
        setImpl(new SwtImage(device, data, this));
    }

    /**
     * Constructs an instance of this class, whose type is
     * <code>SWT.ICON</code>, from the two given <code>ImageData</code>
     * objects. The two images must be the same size. Pixel transparency
     * in either image will be ignored.
     * <p>
     * The mask image should contain white wherever the icon is to be visible,
     * and black wherever the icon is to be transparent. In addition,
     * the source image should contain black wherever the icon is to be
     * transparent.
     * </p>
     * <p>
     * You must dispose the image when it is no longer required.
     * </p>
     *
     * @param device the device on which to create the icon
     * @param source the color data for the icon
     * @param mask the mask data for the icon
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if either the source or mask is null </li>
     *    <li>ERROR_INVALID_ARGUMENT - if source and mask are different sizes</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Image(Device device, ImageData source, ImageData mask) {
        this((IImage) null);
        setImpl(new SwtImage(device, source, mask, this));
    }

    /**
     * Constructs an instance of this class by loading its representation
     * from the specified input stream. Throws an error if an error
     * occurs while loading the image, or if the result is an image
     * of an unsupported type.  Application code is still responsible
     * for closing the input stream.
     * <p>
     * This constructor is provided for convenience when loading a single
     * image only. If the stream contains multiple images, only the first
     * one will be loaded. To load multiple images, use
     * <code>ImageLoader.load()</code>.
     * </p><p>
     * This constructor may be used to load a resource as follows:
     * </p>
     * <pre>
     *     static Image loadImage (Display display, Class clazz, String string) {
     *          InputStream stream = clazz.getResourceAsStream (string);
     *          if (stream == null) return null;
     *          Image image = null;
     *          try {
     *               image = new Image (display, stream);
     *          } catch (SWTException ex) {
     *          } finally {
     *               try {
     *                    stream.close ();
     *               } catch (IOException ex) {}
     *          }
     *          return image;
     *     }
     * </pre>
     * <p>
     * You must dispose the image when it is no longer required.
     * </p>
     *
     * @param device the device on which to create the image
     * @param stream the input stream to load the image from
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the stream is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_IO - if an IO error occurs while reading from the stream</li>
     *    <li>ERROR_INVALID_IMAGE - if the image stream contains invalid data </li>
     *    <li>ERROR_UNSUPPORTED_DEPTH - if the image stream describes an image with an unsupported depth</li>
     *    <li>ERROR_UNSUPPORTED_FORMAT - if the image stream contains an unrecognized format</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Image(Device device, InputStream stream) {
        this((IImage) null);
        setImpl(new SwtImage(device, stream, this));
    }

    /**
     * Constructs an instance of this class by loading its representation
     * from the file with the specified name. Throws an error if an error
     * occurs while loading the image, or if the result is an image
     * of an unsupported type.
     * <p>
     * This constructor is provided for convenience when loading
     * a single image only. If the specified file contains
     * multiple images, only the first one will be used.
     * <p>
     * You must dispose the image when it is no longer required.
     * </p>
     *
     * @param device the device on which to create the image
     * @param filename the name of the file to load the image from
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the file name is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_IO - if an IO error occurs while reading from the file</li>
     *    <li>ERROR_INVALID_IMAGE - if the image file contains invalid data </li>
     *    <li>ERROR_UNSUPPORTED_DEPTH - if the image file describes an image with an unsupported depth</li>
     *    <li>ERROR_UNSUPPORTED_FORMAT - if the image file contains an unrecognized format</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
     * </ul>
     *
     * @see #dispose()
     */
    public Image(Device device, String filename) {
        this((IImage) null);
        setImpl(new SwtImage(device, filename, this));
    }

    /**
     * Constructs an instance of this class by loading its representation
     * from the file retrieved from the ImageFileNameProvider. Throws an
     * error if an error occurs while loading the image, or if the result
     * is an image of an unsupported type.
     * <p>
     * This constructor is provided for convenience for loading image as
     * per DPI level.
     *
     * @param device the device on which to create the image
     * @param imageFileNameProvider the ImageFileNameProvider object that is
     * to be used to get the file name
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the ImageFileNameProvider is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the fileName provided by ImageFileNameProvider is null at 100% zoom</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_IO - if an IO error occurs while reading from the file</li>
     *    <li>ERROR_INVALID_IMAGE - if the image file contains invalid data </li>
     *    <li>ERROR_UNSUPPORTED_DEPTH - if the image file describes an image with an unsupported depth</li>
     *    <li>ERROR_UNSUPPORTED_FORMAT - if the image file contains an unrecognized format</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
     * </ul>
     * @since 3.104
     */
    public Image(Device device, ImageFileNameProvider imageFileNameProvider) {
        this((IImage) null);
        setImpl(new SwtImage(device, imageFileNameProvider, this));
    }

    /**
     * Constructs an instance of this class by loading its representation
     * from the ImageData retrieved from the ImageDataProvider. Throws an
     * error if an error occurs while loading the image, or if the result
     * is an image of an unsupported type.
     * <p>
     * This constructor is provided for convenience for loading image as
     * per DPI level.
     *
     * @param device the device on which to create the image
     * @param imageDataProvider the ImageDataProvider object that is
     * to be used to get the ImageData
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the ImageDataProvider is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the ImageData provided by ImageDataProvider is null at 100% zoom</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_IO - if an IO error occurs while reading from the file</li>
     *    <li>ERROR_INVALID_IMAGE - if the image file contains invalid data </li>
     *    <li>ERROR_UNSUPPORTED_DEPTH - if the image file describes an image with an unsupported depth</li>
     *    <li>ERROR_UNSUPPORTED_FORMAT - if the image file contains an unrecognized format</li>
     * </ul>
     * @exception SWTError <ul>
     *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
     * </ul>
     * @since 3.104
     */
    public Image(Device device, ImageDataProvider imageDataProvider) {
        this((IImage) null);
        setImpl(new SwtImage(device, imageDataProvider, this));
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
    public Color getBackground() {
        return getImpl().getBackground();
    }

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
    public Rectangle getBounds() {
        return getImpl().getBounds();
    }

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
    @Deprecated
    public Rectangle getBoundsInPixels() {
        return getImpl().getBoundsInPixels();
    }

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
    public ImageData getImageData() {
        return getImpl().getImageData();
    }

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
    @Deprecated
    public ImageData getImageDataAtCurrentZoom() {
        return getImpl().getImageDataAtCurrentZoom();
    }

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
    public ImageData getImageData(int zoom) {
        return getImpl().getImageData(zoom);
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
    public long internal_new_GC(GCData data) {
        return getImpl().internal_new_GC(data);
    }

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
    public void internal_dispose_GC(long hDC, GCData data) {
        getImpl().internal_dispose_GC(hDC, data);
    }

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
    public boolean isDisposed() {
        return getImpl().isDisposed();
    }

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
    public void setBackground(Color color) {
        getImpl().setBackground(color);
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

    protected Image(IImage impl) {
        super(impl);
    }

    static Image createApi(IImage impl) {
        return new Image(impl);
    }

    public IImage getImpl() {
        return (IImage) super.getImpl();
    }
}
