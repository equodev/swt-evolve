/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2022 IBM Corporation and others.
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
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.DPIUtil.*;
import org.eclipse.swt.internal.gdip.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.*;

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
public final class DartImage extends DartResource implements Drawable, IImage {

    /**
     * specifies the transparent pixel
     */
    int transparentPixel = -1, transparentColor = -1;

    /**
     * the GC which is drawing on the image
     */
    GC memGC;

    /**
     * Base image data at given zoom in % of the standard resolution. It will be used for
     * scaled variants of this image
     */
    private ElementAtZoom<ImageData> dataAtBaseZoom;

    /**
     * ImageFileNameProvider to provide file names at various Zoom levels
     */
    private ImageFileNameProvider imageFileNameProvider;

    /**
     * ImageDataProvider to provide ImageData at various Zoom levels
     */
    private ImageDataProvider imageDataProvider;

    /**
     * Style flag used to differentiate normal, gray-scale and disabled images based
     * on image data providers. Without this, a normal and a disabled image of the
     * same image data provider would be considered equal.
     */
    private int styleFlag = SWT.IMAGE_COPY;

    /**
     * Attribute to cache current native zoom level
     */
    private int initialNativeZoom = 100;

    /**
     * width of the image
     */
    int width = -1;

    /**
     * height of the image
     */
    int height = -1;

    /**
     * specifies the default scanline padding
     */
    static final int DEFAULT_SCANLINE_PAD = 4;

    private HashMap<Integer, Long> zoomLevelToHandle = new HashMap<>();

    /**
     * Prevents uninitialized instances from being created outside the package.
     */
    DartImage(Device device, Image api) {
        super(device, api);
        initialNativeZoom = DPIUtil.getNativeDeviceZoom();
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
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
    public DartImage(Device device, int width, int height, Image api) {
        super(device, api);
        initialNativeZoom = DPIUtil.getNativeDeviceZoom();
        final int zoom = getZoom();
        width = DPIUtil.scaleUp(width, zoom);
        height = DPIUtil.scaleUp(height, zoom);
        init(width, height);
        init();
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
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
    public DartImage(Device device, Image srcImage, int flag, Image api) {
        super(device, api);
        device = this.device;
        if (srcImage == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (srcImage.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        Rectangle rect = srcImage.getBoundsInPixels();
        this.getApi().type = srcImage.type;
        this.imageDataProvider = ((DartImage) srcImage.getImpl()).imageDataProvider;
        this.imageFileNameProvider = ((DartImage) srcImage.getImpl()).imageFileNameProvider;
        this.styleFlag = ((DartImage) srcImage.getImpl()).styleFlag | flag;
        initialNativeZoom = ((DartImage) srcImage.getImpl()).initialNativeZoom;
        this.dataAtBaseZoom = ((DartImage) srcImage.getImpl()).dataAtBaseZoom;
        switch(flag) {
            case SWT.IMAGE_COPY:
                {
                    switch(getApi().type) {
                        case SWT.BITMAP:
                            /* Get the HDC for the device */
                            long hDC = device.internal_new_GC(null);
                            if (getApi().handle == 0)
                                SWT.error(SWT.ERROR_NO_HANDLES);
                            /* Release the HDC for the device */
                            device.internal_dispose_GC(hDC, null);
                            transparentPixel = srcImage.getImpl()._transparentPixel();
                            break;
                        case SWT.ICON:
                            if (getApi().handle == 0)
                                SWT.error(SWT.ERROR_NO_HANDLES);
                            break;
                        default:
                            SWT.error(SWT.ERROR_INVALID_IMAGE);
                    }
                    break;
                }
            case SWT.IMAGE_DISABLE:
                {
                    ImageData data = srcImage.getImageData(((DartImage) srcImage.getImpl()).getZoom());
                    ImageData newData = applyDisableImageData(data, rect.height, rect.width);
                    init(newData, getZoom());
                    break;
                }
            case SWT.IMAGE_GRAY:
                {
                    ImageData data = srcImage.getImageData(((DartImage) srcImage.getImpl()).getZoom());
                    ImageData newData = applyGrayImageData(data, rect.height, rect.width);
                    init(newData, getZoom());
                    break;
                }
            default:
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        init();
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
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
    public DartImage(Device device, Rectangle bounds, Image api) {
        super(device, api);
        if (bounds == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        initialNativeZoom = DPIUtil.getNativeDeviceZoom();
        bounds = DPIUtil.scaleUp(bounds, getZoom());
        init(bounds.width, bounds.height);
        init();
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
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
    public DartImage(Device device, ImageData data, Image api) {
        super(device, api);
        if (data == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        initialNativeZoom = DPIUtil.getNativeDeviceZoom();
        this.dataAtBaseZoom = new ElementAtZoom<>(data, 100);
        data = DPIUtil.autoScaleUp(device, this.dataAtBaseZoom);
        init(data, getZoom());
        init();
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
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
    public DartImage(Device device, ImageData source, ImageData mask, Image api) {
        super(device, api);
        if (source == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (mask == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (source.width != mask.width || source.height != mask.height) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        initialNativeZoom = DPIUtil.getNativeDeviceZoom();
        this.dataAtBaseZoom = new ElementAtZoom<>(applyMask(source, ImageData.convertMask(mask)), 100);
        source = DPIUtil.autoScaleUp(device, source);
        mask = DPIUtil.autoScaleUp(device, mask);
        mask = ImageData.convertMask(mask);
        init(this.device, this.getApi(), source, mask, getZoom());
        init();
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
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
    public DartImage(Device device, InputStream stream, Image api) {
        super(device, api);
        initialNativeZoom = DPIUtil.getNativeDeviceZoom();
        this.dataAtBaseZoom = new ElementAtZoom<>(new ImageData(stream), 100);
        ImageData data = DPIUtil.autoScaleUp(device, this.dataAtBaseZoom);
        init(data, getZoom());
        init();
        //((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
        ;
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
    public DartImage(Device device, String filename, Image api) {
        super(device, api);
        this.filename = ImageUtils.getFilename(filename);
        if (filename == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        initialNativeZoom = DPIUtil.getNativeDeviceZoom();
        this.dataAtBaseZoom = new ElementAtZoom<>(new ImageData(filename), 100);
        ImageData data = DPIUtil.autoScaleUp(device, this.dataAtBaseZoom);
        init(data, getZoom());
        init();
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
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
    public DartImage(Device device, ImageFileNameProvider imageFileNameProvider, Image api) {
        super(device, api);
        this.filename = ImageUtils.getFilename(imageFileNameProvider.getImagePath(100));
        this.imageFileNameProvider = imageFileNameProvider;
        initialNativeZoom = DPIUtil.getNativeDeviceZoom();
        ElementAtZoom<String> fileName = DPIUtil.validateAndGetImagePathAtZoom(imageFileNameProvider, getZoom());
        if (fileName.zoom() == getZoom()) {
            long handle = initNative(fileName.element(), getZoom());
            if (handle == 0) {
                init(new ImageData(fileName.element()), getZoom());
            } else {
                setHandleForZoomLevel(handle, getZoom());
            }
        } else {
            ImageData resizedData = DPIUtil.autoScaleImageData(device, new ImageData(fileName.element()), fileName.zoom());
            init(resizedData, getZoom());
        }
        init();
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
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
    public DartImage(Device device, ImageDataProvider imageDataProvider, Image api) {
        super(device, api);
        this.imageDataProvider = imageDataProvider;
        initialNativeZoom = DPIUtil.getNativeDeviceZoom();
        ElementAtZoom<ImageData> data = DPIUtil.validateAndGetImageDataAtZoom(imageDataProvider, getZoom());
        ImageData resizedData = DPIUtil.scaleImageData(device, data.element(), getZoom(), data.zoom());
        init(resizedData, getZoom());
        init();
        ((SwtDevice) this.device.getImpl()).registerResourceWithZoomSupport(this.getApi());
    }

    private ImageData adaptImageDataIfDisabledOrGray(ImageData data) {
        ImageData returnImageData = null;
        switch(this.styleFlag) {
            case SWT.IMAGE_DISABLE:
                {
                    ImageData newData = applyDisableImageData(data, data.height, data.width);
                    returnImageData = newData;
                    break;
                }
            case SWT.IMAGE_GRAY:
                {
                    ImageData newData = applyGrayImageData(data, data.height, data.width);
                    returnImageData = newData;
                    break;
                }
            default:
                {
                    returnImageData = data;
                    break;
                }
        }
        return returnImageData;
    }

    private ImageData applyDisableImageData(ImageData data, int height, int width) {
        PaletteData palette = data.palette;
        RGB[] rgbs = new RGB[3];
        rgbs[0] = this.device.getSystemColor(SWT.COLOR_BLACK).getRGB();
        rgbs[1] = this.device.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW).getRGB();
        rgbs[2] = this.device.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB();
        ImageData newData = new ImageData(width, height, 8, new PaletteData(rgbs));
        newData.alpha = data.alpha;
        newData.alphaData = data.alphaData;
        newData.maskData = data.maskData;
        newData.maskPad = data.maskPad;
        if (data.transparentPixel != -1)
            newData.transparentPixel = 0;
        /* Convert the pixels. */
        int[] scanline = new int[width];
        int[] maskScanline = null;
        ImageData mask = null;
        if (data.maskData != null)
            mask = data.getTransparencyMask();
        if (mask != null)
            maskScanline = new int[width];
        int redMask = palette.redMask;
        int greenMask = palette.greenMask;
        int blueMask = palette.blueMask;
        int redShift = palette.redShift;
        int greenShift = palette.greenShift;
        int blueShift = palette.blueShift;
        for (int y = 0; y < height; y++) {
            int offset = y * newData.bytesPerLine;
            data.getPixels(0, y, width, scanline, 0);
            if (mask != null)
                mask.getPixels(0, y, width, maskScanline, 0);
            for (int x = 0; x < width; x++) {
                int pixel = scanline[x];
                if (!((data.transparentPixel != -1 && pixel == data.transparentPixel) || (mask != null && maskScanline[x] == 0))) {
                    int red, green, blue;
                    if (palette.isDirect) {
                        red = pixel & redMask;
                        red = (redShift < 0) ? red >>> -redShift : red << redShift;
                        green = pixel & greenMask;
                        green = (greenShift < 0) ? green >>> -greenShift : green << greenShift;
                        blue = pixel & blueMask;
                        blue = (blueShift < 0) ? blue >>> -blueShift : blue << blueShift;
                    } else {
                        red = palette.colors[pixel].red;
                        green = palette.colors[pixel].green;
                        blue = palette.colors[pixel].blue;
                    }
                    int intensity = red * red + green * green + blue * blue;
                    if (intensity < 98304) {
                        newData.data[offset] = (byte) 1;
                    } else {
                        newData.data[offset] = (byte) 2;
                    }
                }
                offset++;
            }
        }
        return newData;
    }

    private ImageData applyGrayImageData(ImageData data, int pHeight, int pWidth) {
        PaletteData palette = data.palette;
        ImageData newData = data;
        if (!palette.isDirect) {
            /* Convert the palette entries to gray. */
            RGB[] rgbs = palette.getRGBs();
            for (int i = 0; i < rgbs.length; i++) {
                if (data.transparentPixel != i) {
                    RGB color = rgbs[i];
                    int red = color.red;
                    int green = color.green;
                    int blue = color.blue;
                    int intensity = (red + red + green + green + green + green + green + blue) >> 3;
                    color.red = color.green = color.blue = intensity;
                }
            }
            newData.palette = new PaletteData(rgbs);
        } else {
            /* Create a 8 bit depth image data with a gray palette. */
            RGB[] rgbs = new RGB[256];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(i, i, i);
            }
            newData = new ImageData(pWidth, pHeight, 8, new PaletteData(rgbs));
            newData.alpha = data.alpha;
            newData.alphaData = data.alphaData;
            newData.maskData = data.maskData;
            newData.maskPad = data.maskPad;
            if (data.transparentPixel != -1)
                newData.transparentPixel = 254;
            /* Convert the pixels. */
            int[] scanline = new int[pWidth];
            int redMask = palette.redMask;
            int greenMask = palette.greenMask;
            int blueMask = palette.blueMask;
            int redShift = palette.redShift;
            int greenShift = palette.greenShift;
            int blueShift = palette.blueShift;
            for (int y = 0; y < pHeight; y++) {
                int offset = y * newData.bytesPerLine;
                data.getPixels(0, y, pWidth, scanline, 0);
                for (int x = 0; x < pWidth; x++) {
                    int pixel = scanline[x];
                    if (pixel != data.transparentPixel) {
                        int red = pixel & redMask;
                        red = (redShift < 0) ? red >>> -redShift : red << redShift;
                        int green = pixel & greenMask;
                        green = (greenShift < 0) ? green >>> -greenShift : green << greenShift;
                        int blue = pixel & blueMask;
                        blue = (blueShift < 0) ? blue >>> -blueShift : blue << blueShift;
                        int intensity = (red + red + green + green + green + green + green + blue) >> 3;
                        if (newData.transparentPixel == intensity)
                            intensity = 255;
                        newData.data[offset] = (byte) intensity;
                    } else {
                        newData.data[offset] = (byte) 254;
                    }
                    offset++;
                }
            }
        }
        return newData;
    }

    long initNative(String filename, int zoom) {
        long handle = 0;
        boolean gdip = true;
        /*
	* Bug in GDI+. Bitmap.LockBits() fails to load GIF files in
	* Windows 7 when the image has a position offset in the first frame.
	* The fix is to not use GDI+ image loading in this case.
	*/
        if (filename.toLowerCase().endsWith(".gif"))
            gdip = false;
        if (gdip) {
            int length = filename.length();
            char[] chars = new char[length + 1];
            filename.getChars(0, length, chars, 0);
        }
        return handle;
    }

    @Override
    void destroy() {
        ((SwtDevice) device.getImpl()).deregisterResourceWithZoomSupport(this.getApi());
        if (memGC != null)
            memGC.dispose();
        destroyHandle();
        memGC = null;
    }

    static int count = 0;

    private void destroyHandle() {
        for (Long handle : zoomLevelToHandle.values()) {
            destroyHandle(handle);
        }
        zoomLevelToHandle.clear();
        getApi().handle = 0;
    }

    @Override
    void destroyHandlesExcept(Set<Integer> zoomLevels) {
        zoomLevelToHandle.entrySet().removeIf(entry -> {
            final Integer zoom = entry.getKey();
            if (!zoomLevels.contains(zoom) && zoom != DPIUtil.getZoomForAutoscaleProperty(initialNativeZoom)) {
                destroyHandle(entry.getValue());
                return true;
            }
            return false;
        });
    }

    private void destroyHandle(long handle) {
        if (getApi().type == SWT.ICON) {
        } else {
        }
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
        if (!(object instanceof Image))
            return false;
        Image image = (Image) object;
        if (!(image.getImpl() instanceof DartImage))
            return false;
        if (device != image.getImpl()._device() || transparentPixel != image.getImpl()._transparentPixel() || getZoom() != ((DartImage) image.getImpl()).getZoom())
            return false;
        if (imageDataProvider != null && ((DartImage) image.getImpl()).imageDataProvider != null) {
            return (styleFlag == ((DartImage) image.getImpl()).styleFlag) && imageDataProvider.equals(((DartImage) image.getImpl()).imageDataProvider);
        } else if (imageFileNameProvider != null && ((DartImage) image.getImpl()).imageFileNameProvider != null) {
            return (styleFlag == ((DartImage) image.getImpl()).styleFlag) && imageFileNameProvider.equals(((DartImage) image.getImpl()).imageFileNameProvider);
        } else {
            return getApi().handle == image.handle;
        }
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (background != null)
            return background;
        if (transparentPixel == -1)
            return null;
        /* Get the HDC for the device */
        long hDC = device.internal_new_GC(null);
        int red = 0, green = 0, blue = 0;
        {
        }
        /* Release the HDC for the device */
        device.internal_dispose_GC(hDC, null);
        return SwtColor.win32_new(device, (blue << 16) | (green << 8) | red);
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return getBounds(100);
    }

    Rectangle getBounds(int zoom) {
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        // Read the bounds in pixels from native layer.
        Rectangle bounds = getBoundsInPixelsFromNative();
        if (bounds != null && zoom != getZoom()) {
            bounds = DPIUtil.scaleBounds(bounds, zoom, getZoom());
        }
        return bounds;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (width != -1 && height != -1) {
            return new Rectangle(0, 0, width, height);
        }
        return getBoundsInPixelsFromNative();
    }

    private Rectangle getBoundsInPixelsFromNative() {
        return new Rectangle(0, 0, 0, 0);
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        return getImageData(100);
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
        return this.imageData;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        switch(getApi().type) {
            case SWT.ICON:
                {
                    int numColors = 0;
                    /* Get the HDC for the device */
                    long hDC = device.internal_new_GC(null);
                    /* Calculate the palette */
                    PaletteData palette = null;
                    /* Release the HDC for the device */
                    device.internal_dispose_GC(hDC, null);
                }
            case SWT.BITMAP:
                {
                    /* Get the HDC for the device */
                    long hDC = device.internal_new_GC(null);
                    /* Calculate number of colors */
                    int numColors = 0;
                    /* Calculate the palette */
                    PaletteData palette = null;
                    /* Release the HDC for the device */
                    device.internal_dispose_GC(hDC, null);
                }
            default:
                SWT.error(SWT.ERROR_INVALID_IMAGE);
                return null;
        }
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
        if (imageDataProvider != null) {
            return imageDataProvider.hashCode();
        } else if (imageFileNameProvider != null) {
            return Objects.hash(imageFileNameProvider, styleFlag, transparentPixel, getZoom());
        } else {
            return (int) getApi().handle;
        }
    }

    void init(int width, int height) {
        if (width <= 0 || height <= 0) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        getApi().type = SWT.BITMAP;
        long hDC = device.internal_new_GC(null);
        /*
	* Feature in Windows.  CreateCompatibleBitmap() may fail
	* for large images.  The fix is to create a DIB section
	* in that case.
	*/
        if (getApi().handle == 0) {
        }
        if (getApi().handle != 0) {
        }
        device.internal_dispose_GC(hDC, null);
        this.getApi().handle = 1;
        if (getApi().handle == 0) {
            SWT.error(SWT.ERROR_NO_HANDLES, null, ((SwtDevice) device.getImpl()).getLastError());
        }
    }

    static long createDIB(int width, int height, int depth) {
        return 0;
    }

    static ImageData indexToIndex(ImageData src, int newDepth) {
        ImageData img = new ImageData(src.width, src.height, newDepth, src.palette);
        ImageData.blit(src.data, src.depth, src.bytesPerLine, src.getByteOrder(), src.width, src.height, img.data, img.depth, img.bytesPerLine, src.getByteOrder(), img.width, img.height, false, false);
        img.transparentPixel = src.transparentPixel;
        img.maskPad = src.maskPad;
        img.maskData = src.maskData;
        img.alpha = src.alpha;
        img.alphaData = src.alphaData;
        return img;
    }

    static ImageData indexToDirect(ImageData src, int newDepth, PaletteData newPalette, int newByteOrder) {
        ImageData img = new ImageData(src.width, src.height, newDepth, newPalette);
        RGB[] rgbs = src.palette.getRGBs();
        byte[] srcReds = new byte[rgbs.length];
        byte[] srcGreens = new byte[rgbs.length];
        byte[] srcBlues = new byte[rgbs.length];
        for (int j = 0; j < rgbs.length; j++) {
            RGB rgb = rgbs[j];
            if (rgb == null)
                continue;
            srcReds[j] = (byte) rgb.red;
            srcGreens[j] = (byte) rgb.green;
            srcBlues[j] = (byte) rgb.blue;
        }
        ImageData.blit(src.width, src.height, src.data, src.depth, src.bytesPerLine, src.getByteOrder(), srcReds, srcGreens, srcBlues, img.data, img.depth, img.bytesPerLine, newByteOrder, newPalette.redMask, newPalette.greenMask, newPalette.blueMask);
        if (src.transparentPixel != -1) {
            img.transparentPixel = newPalette.getPixel(src.palette.getRGB(src.transparentPixel));
        }
        img.maskPad = src.maskPad;
        img.maskData = src.maskData;
        img.alpha = src.alpha;
        img.alphaData = src.alphaData;
        return img;
    }

    static ImageData directToDirect(ImageData src, int newDepth, PaletteData newPalette, int newByteOrder) {
        ImageData img = new ImageData(src.width, src.height, newDepth, newPalette);
        ImageData.blit(src.data, src.depth, src.bytesPerLine, src.getByteOrder(), src.width, src.height, src.palette.redMask, src.palette.greenMask, src.palette.blueMask, img.data, img.depth, img.bytesPerLine, newByteOrder, img.width, img.height, img.palette.redMask, img.palette.greenMask, img.palette.blueMask, false, false);
        if (src.transparentPixel != -1) {
            img.transparentPixel = img.palette.getPixel(src.palette.getRGB(src.transparentPixel));
        }
        img.maskPad = src.maskPad;
        img.maskData = src.maskData;
        img.alpha = src.alpha;
        img.alphaData = src.alphaData;
        return img;
    }

    static long[] init(Device device, Image image, ImageData i, Integer zoom) {
        /* Windows does not support 2-bit images. Convert to 4-bit image. */
        if (i.depth == 2) {
            i = indexToIndex(i, 4);
        }
        /* Windows does not support 16-bit palette images. Convert to RGB. */
        if ((i.depth == 16) && !i.palette.isDirect) {
            PaletteData newPalette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
            i = indexToDirect(i, 24, newPalette, ImageData.MSB_FIRST);
        }
        boolean hasAlpha = i.alpha != -1 || i.alphaData != null;
        /*
	 * Windows supports 16-bit mask of (0x7C00, 0x3E0, 0x1F),
	 * 24-bit mask of (0xFF0000, 0xFF00, 0xFF) and 32-bit mask
	 * (0x00FF0000, 0x0000FF00, 0x000000FF) as documented in
	 * MSDN BITMAPINFOHEADER.  Make sure the image is
	 * Windows-supported.
	 */
        if (i.palette.isDirect) {
            final PaletteData palette = i.palette;
            final int redMask = palette.redMask;
            final int greenMask = palette.greenMask;
            final int blueMask = palette.blueMask;
            int newDepth = i.depth;
            int newOrder = ImageData.MSB_FIRST;
            PaletteData newPalette = null;
            if (hasAlpha) {
                newDepth = 32;
                newPalette = new PaletteData(0xFF00, 0xFF0000, 0xFF000000);
            } else {
                switch(i.depth) {
                    case 8:
                        /*
					 * Bug 566545. Usually each color mask selects a different part of the pixel
					 * value to encode the according color. In this common case it is rather trivial
					 * to convert an 8-bit direct color image to the Windows supported 16-bit image.
					 * However there is no enforcement for the color masks to be disjunct. For
					 * example an 8-bit image where all color masks select the same 8-bit of pixel
					 * value (mask = 0xFF and shift = 0 for all colors) results in a very efficient
					 * 8-bit gray-scale image without the need of defining a color table.
					 *
					 * That's why we need to calculate the actual required depth if all colors are
					 * stored non-overlapping which might require 24-bit instead of the usual
					 * expected 16-bit.
					 */
                        int minDepth = ImageData.getChannelWidth(redMask, palette.redShift) + ImageData.getChannelWidth(greenMask, palette.greenShift) + ImageData.getChannelWidth(blueMask, palette.blueShift);
                        if (minDepth <= 16) {
                            newDepth = 16;
                            newOrder = ImageData.LSB_FIRST;
                            newPalette = new PaletteData(0x7C00, 0x3E0, 0x1F);
                        } else {
                            newDepth = 24;
                            newPalette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
                        }
                        break;
                    case 16:
                        newOrder = ImageData.LSB_FIRST;
                        if (!(redMask == 0x7C00 && greenMask == 0x3E0 && blueMask == 0x1F)) {
                            newPalette = new PaletteData(0x7C00, 0x3E0, 0x1F);
                        }
                        break;
                    case 24:
                        if (!(redMask == 0xFF && greenMask == 0xFF00 && blueMask == 0xFF0000)) {
                            newPalette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
                        }
                        break;
                    case 32:
                        if (i.getTransparencyType() != SWT.TRANSPARENCY_MASK) {
                            newDepth = 24;
                            newPalette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
                        } else if (!(redMask == 0xFF00 && greenMask == 0xFF0000 && blueMask == 0xFF000000)) {
                            newPalette = new PaletteData(0xFF00, 0xFF0000, 0xFF000000);
                        }
                        break;
                    default:
                        SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
                }
            }
            if (newPalette != null) {
                i = directToDirect(i, newDepth, newPalette, newOrder);
            }
        } else if (hasAlpha) {
            PaletteData newPalette = new PaletteData(0xFF00, 0xFF0000, 0xFF000000);
            i = indexToDirect(i, 32, newPalette, ImageData.MSB_FIRST);
        }
        if (i.alpha != -1) {
            int alpha = i.alpha & 0xFF;
            byte[] data = i.data;
            for (int dp = 0; dp < i.data.length; dp += 4) {
                /* pre-multiplied alpha */
                int r = ((data[dp] & 0xFF) * alpha) + 128;
                r = (r + (r >> 8)) >> 8;
                int g = ((data[dp + 1] & 0xFF) * alpha) + 128;
                g = (g + (g >> 8)) >> 8;
                int b = ((data[dp + 2] & 0xFF) * alpha) + 128;
                b = (b + (b >> 8)) >> 8;
                data[dp] = (byte) b;
                data[dp + 1] = (byte) g;
                data[dp + 2] = (byte) r;
                data[dp + 3] = (byte) alpha;
            }
        } else if (i.alphaData != null) {
            byte[] data = i.data;
            for (int ap = 0, dp = 0; dp < i.data.length; ap++, dp += 4) {
                /* pre-multiplied alpha */
                int a = i.alphaData[ap] & 0xFF;
                int r = ((data[dp] & 0xFF) * a) + 128;
                r = (r + (r >> 8)) >> 8;
                int g = ((data[dp + 1] & 0xFF) * a) + 128;
                g = (g + (g >> 8)) >> 8;
                int b = ((data[dp + 2] & 0xFF) * a) + 128;
                b = (b + (b >> 8)) >> 8;
                data[dp] = (byte) r;
                data[dp + 1] = (byte) g;
                data[dp + 2] = (byte) b;
                data[dp + 3] = (byte) a;
            }
        }
        /* Construct bitmap info header by hand */
        RGB[] rgbs = i.palette.getRGBs();
        if (!i.palette.isDirect) {
        }
        /* In case of a scanline pad other than 4, do the work to convert it */
        byte[] data = i.data;
        if (i.scanlinePad != 4 && (i.bytesPerLine % 4 != 0)) {
            data = ImageData.convertPad(data, i.width, i.height, i.depth, i.scanlinePad, 4);
        }
        long[] result = null;
        if (i.getTransparencyType() == SWT.TRANSPARENCY_MASK) {
            /* Get the HDC for the device */
            long hDC = device.internal_new_GC(null);
            /* Release the HDC for the device */
            device.internal_dispose_GC(hDC, null);
            if (image == null) {
            } else {
                image.type = SWT.ICON;
            }
        } else {
            if (image == null) {
            } else {
                image.type = SWT.BITMAP;
                if (image.getImpl() instanceof DartImage) {
                    ((DartImage) image.getImpl()).transparentPixel = i.transparentPixel;
                }
                if (image.getImpl() instanceof SwtImage) {
                    ((SwtImage) image.getImpl()).transparentPixel = i.transparentPixel;
                }
            }
        }
        return result;
    }

    private void setHandleForZoomLevel(long handle, Integer zoom) {
        this.getApi().handle = 1;
        if (this.getApi().handle == 0) {
            // Set handle for default zoom level
            this.getApi().handle = handle;
        }
        if (zoom != null && !zoomLevelToHandle.containsKey(zoom)) {
            zoomLevelToHandle.put(zoom, handle);
        }
    }

    static long[] init(Device device, Image image, ImageData source, ImageData mask, Integer zoom) {
        ImageData imageData = applyMask(source, mask);
        return init(device, image, imageData, zoom);
    }

    private static ImageData applyMask(ImageData source, ImageData mask) {
        /* Create a temporary image and locate the black pixel */
        ImageData imageData;
        int blackIndex = 0;
        if (source.palette.isDirect) {
            imageData = new ImageData(source.width, source.height, source.depth, source.palette);
        } else {
            RGB black = new RGB(0, 0, 0);
            RGB[] rgbs = source.getRGBs();
            if (source.transparentPixel != -1) {
                /*
			 * The source had transparency, so we can use the transparent pixel
			 * for black.
			 */
                RGB[] newRGBs = new RGB[rgbs.length];
                System.arraycopy(rgbs, 0, newRGBs, 0, rgbs.length);
                if (source.transparentPixel >= newRGBs.length) {
                    /* Grow the palette with black */
                    rgbs = new RGB[source.transparentPixel + 1];
                    System.arraycopy(newRGBs, 0, rgbs, 0, newRGBs.length);
                    for (int i = newRGBs.length; i <= source.transparentPixel; i++) {
                        rgbs[i] = new RGB(0, 0, 0);
                    }
                } else {
                    newRGBs[source.transparentPixel] = black;
                    rgbs = newRGBs;
                }
                blackIndex = source.transparentPixel;
                imageData = new ImageData(source.width, source.height, source.depth, new PaletteData(rgbs));
            } else {
                while (blackIndex < rgbs.length) {
                    if (rgbs[blackIndex].equals(black))
                        break;
                    blackIndex++;
                }
                if (blackIndex == rgbs.length) {
                    /*
				 * We didn't find black in the palette, and there is no transparent
				 * pixel we can use.
				 */
                    if ((1 << source.depth) > rgbs.length) {
                        /* We can grow the palette and add black */
                        RGB[] newRGBs = new RGB[rgbs.length + 1];
                        System.arraycopy(rgbs, 0, newRGBs, 0, rgbs.length);
                        newRGBs[rgbs.length] = black;
                        rgbs = newRGBs;
                    } else {
                        /* No room to grow the palette */
                        blackIndex = -1;
                    }
                }
                imageData = new ImageData(source.width, source.height, source.depth, new PaletteData(rgbs));
            }
        }
        if (blackIndex == -1) {
            /* There was no black in the palette, so just copy the data over */
            System.arraycopy(source.data, 0, imageData.data, 0, imageData.data.length);
        } else {
            /* Modify the source image to contain black wherever the mask is 0 */
            int[] imagePixels = new int[imageData.width];
            int[] maskPixels = new int[mask.width];
            for (int y = 0; y < imageData.height; y++) {
                source.getPixels(0, y, imageData.width, imagePixels, 0);
                mask.getPixels(0, y, mask.width, maskPixels, 0);
                for (int i = 0; i < imagePixels.length; i++) {
                    if (maskPixels[i] == 0)
                        imagePixels[i] = blackIndex;
                }
                imageData.setPixels(0, y, source.width, imagePixels, 0);
            }
        }
        imageData.maskPad = mask.scanlinePad;
        imageData.maskData = mask.data;
        return imageData;
    }

    void init(ImageData i, Integer zoom) {
        this.getApi().handle = 1;
        if (i == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        this.imageData = i;
        init(device, this.getApi(), i, zoom);
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
    @Override
    public long internal_new_GC(GCData data) {
        if (getApi().handle == 0)
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        /*
	* Create a new GC that can draw into the image.
	* Only supported for bitmaps.
	*/
        if (getApi().type != SWT.BITMAP || memGC != null) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        /* Create a compatible HDC for the device */
        long hDC = device.internal_new_GC(null);
        device.internal_dispose_GC(hDC, null);
        if (data != null) {
            /* Set the GCData fields */
            int mask = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
            if ((data.style & mask) != 0) {
            } else {
                data.style |= SWT.LEFT_TO_RIGHT;
            }
            data.device = device;
            data.nativeZoom = initialNativeZoom;
            data.image = this.getApi();
        }
        return 0;
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
    @Override
    public void internal_dispose_GC(long hDC, GCData data) {
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
    @Override
    public boolean isDisposed() {
        return getApi().handle == 0;
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
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (color == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (color.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        if (transparentPixel == -1)
            return;
        transparentColor = -1;
        /* Get the HDC for the device */
        long hDC = device.internal_new_GC(null);
        /* Release the HDC for the device */
        device.internal_dispose_GC(hDC, null);
        this.background = color;
    }

    private int getZoom() {
        return DPIUtil.getZoomForAutoscaleProperty(initialNativeZoom);
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
            return "Image {*DISPOSED*}";
        return "Image {" + getApi().handle + "}";
    }

    // This class is only used for a workaround and will be removed again
    private class StaticZoomUpdater implements AutoCloseable {

        private final boolean updateStaticZoom;

        private final int currentNativeDeviceZoom;

        private StaticZoomUpdater(int targetZoom) {
            this.currentNativeDeviceZoom = DPIUtil.getNativeDeviceZoom();
            this.updateStaticZoom = this.currentNativeDeviceZoom != targetZoom && device instanceof Display display && display.isRescalingAtRuntime();
            if (updateStaticZoom) {
                DPIUtil.setDeviceZoom(targetZoom);
            }
        }

        @Override
        public void close() {
            if (updateStaticZoom) {
                DPIUtil.setDeviceZoom(currentNativeDeviceZoom);
            }
        }
    }

    Color background;

    String filename;

    ImageData imageData;

    ImageData imageDataAtCurrentZoom;

    public int _transparentPixel() {
        return transparentPixel;
    }

    public int _transparentColor() {
        return transparentColor;
    }

    public GC _memGC() {
        return memGC;
    }

    public int _width() {
        return width;
    }

    public int _height() {
        return height;
    }

    public Color _background() {
        return background;
    }

    public String _filename() {
        return filename;
    }

    public ImageData _imageData() {
        return imageData;
    }

    public ImageData _imageDataAtCurrentZoom() {
        return imageDataAtCurrentZoom;
    }

    public Image getApi() {
        if (api == null)
            api = Image.createApi(this);
        return (Image) api;
    }

    public VImage getValue() {
        if (value == null)
            value = new VImage(this);
        return (VImage) value;
    }
}
