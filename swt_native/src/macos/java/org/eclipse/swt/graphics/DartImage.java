/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2025 IBM Corporation and others.
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

import static org.eclipse.swt.internal.image.ImageColorTransformer.DEFAULT_DISABLED_IMAGE_TRANSFORMER;
import java.io.*;
import java.util.*;
import java.util.function.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.graphics.*;
import org.eclipse.swt.internal.image.*;
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
     * The GC the image is currently selected in.
     */
    GC memGC;

    /**
     * The global alpha value to be used for every pixel.
     */
    /**
     * The width of the image.
     */
    int width = -1;

    /**
     * The height of the image.
     */
    int height = -1;

    /**
     * Specifies the default scanline padding.
     */
    static final int DEFAULT_SCANLINE_PAD = 4;

    /**
     * ImageFileNameProvider to provide file names at various Zoom levels
     */
    private ImageFileNameProvider imageFileNameProvider;

    /**
     * ImageDataProvider to provide ImageData at various Zoom levels
     */
    private ImageDataProvider imageDataProvider;

    /**
     * ImageGcDrawer to provide a callback to draw on a GC for various zoom levels
     */
    private ImageGcDrawer imageGcDrawer;

    /**
     * Style flag used to differentiate normal, gray-scale and disabled images based
     * on image data providers. Without this, a normal and a disabled image of the
     * same image data provider would be considered equal.
     */
    private int styleFlag = SWT.IMAGE_COPY;

    /**
     * Alpha information objects for 100%, 200%
     */
    private AlphaInfo alphaInfo_100, alphaInfo_200;

    static class AlphaInfo {

        /**
         * The alpha data of the image.
         */
        byte[] alphaData;

        /**
         * The global alpha value to be used for every pixel.
         */
        int alpha;

        /**
         * specifies the transparent pixel
         */
        int transparentPixel;

        AlphaInfo() {
            transparentPixel = -1;
            alpha = -1;
        }
    }

    DartImage(Device device, Image api) {
        super(device, api);
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
        try {
            init(width, height);
            init();
        } finally {
        }
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
        if (srcImage == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (srcImage.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        switch(flag) {
            case SWT.IMAGE_COPY:
            case SWT.IMAGE_DISABLE:
            case SWT.IMAGE_GRAY:
                break;
            default:
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.imageData = ((DartImage) srcImage.getImpl()).imageData;
        try {
            this.getApi().type = srcImage.type;
            /* Copy alpha information (transparent pixel and alpha data) for 100% & 200% image representations from source image*/
            alphaInfo_100 = new AlphaInfo();
            copyAlphaInfo(((DartImage) srcImage.getImpl()).alphaInfo_100, alphaInfo_100);
            if (((DartImage) srcImage.getImpl()).alphaInfo_200 != null) {
                alphaInfo_200 = new AlphaInfo();
                copyAlphaInfo(((DartImage) srcImage.getImpl()).alphaInfo_200, alphaInfo_200);
            }
            imageFileNameProvider = ((DartImage) srcImage.getImpl()).imageFileNameProvider;
            imageDataProvider = ((DartImage) srcImage.getImpl()).imageDataProvider;
            imageGcDrawer = ((DartImage) srcImage.getImpl()).imageGcDrawer;
            this.styleFlag = ((DartImage) srcImage.getImpl()).styleFlag | flag;
            if (imageFileNameProvider != null || imageDataProvider != null || ((DartImage) srcImage.getImpl()).imageGcDrawer != null) {
            }
            init();
        } finally {
        }
    }

    /**
     * Copies the AlphaInfo from source to destination.
     */
    private void copyAlphaInfo(AlphaInfo src_alphaInfo, AlphaInfo dest_alphaInfo) {
        dest_alphaInfo.transparentPixel = src_alphaInfo.transparentPixel;
        dest_alphaInfo.alpha = src_alphaInfo.alpha;
        if (src_alphaInfo.alphaData != null) {
            dest_alphaInfo.alphaData = new byte[src_alphaInfo.alphaData.length];
            System.arraycopy(src_alphaInfo.alphaData, 0, dest_alphaInfo.alphaData, 0, dest_alphaInfo.alphaData.length);
        }
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
     *
     * @deprecated use {@link Image#Image(Device, int, int)} instead
     */
    @Deprecated(since = "2025-06", forRemoval = true)
    public DartImage(Device device, Rectangle bounds, Image api) {
        super(device, api);
        if (bounds == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        try {
            init(bounds.width, bounds.height);
            init();
        } finally {
        }
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
        try {
            init(data, 100);
            init();
        } finally {
        }
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
        this.imageData = source;
        try {
            mask = ImageData.convertMask(mask);
            ImageData image = new ImageData(source.width, source.height, source.depth, source.palette, source.scanlinePad, source.data);
            image.maskPad = mask.scanlinePad;
            image.maskData = mask.data;
            init(image, 100);
        } finally {
        }
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
        if (stream == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        try {
            byte[] input = stream.readAllBytes();
            initWithSupplier(zoom -> ImageDataLoader.canLoadAtZoom(new ByteArrayInputStream(input), FileFormat.DEFAULT_ZOOM, zoom), zoom -> ImageDataLoader.load(new ByteArrayInputStream(input), FileFormat.DEFAULT_ZOOM, zoom).element());
            init();
        } catch (IOException e) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT, e);
        } finally {
        }
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
        this.filename = GraphicsUtils.getFilename(filename);
        try {
            if (filename == null)
                SWT.error(SWT.ERROR_NULL_ARGUMENT);
            initNative(filename);
            init();
        } finally {
        }
        ImageData data = new ImageData(filename);
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
        this.filename = GraphicsUtils.getFilename(imageFileNameProvider.getImagePath(100));
        if (imageFileNameProvider == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        this.imageFileNameProvider = imageFileNameProvider;
        String filename = imageFileNameProvider.getImagePath(100);
        if (filename == null)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        try {
            initNative(filename);
            init();
            String filename2x = imageFileNameProvider.getImagePath(200);
            if (filename2x != null) {
                alphaInfo_200 = new AlphaInfo();
            } else if (ImageDataLoader.canLoadAtZoom(filename, 100, 200)) {
                alphaInfo_200 = new AlphaInfo();
            }
        } finally {
        }
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
        if (imageDataProvider == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        this.imageDataProvider = imageDataProvider;
        ImageData data = imageDataProvider.getImageData(100);
        if (data == null)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        try {
            init(data, 100);
            init();
            ImageData data2x = imageDataProvider.getImageData(200);
            if (data2x != null) {
                alphaInfo_200 = new AlphaInfo();
            }
        } finally {
        }
    }

    /**
     * The provided ImageGcDrawer will be called on demand whenever a new variant of the
     * Image for an additional zoom is required. Depending on the OS-specific implementation
     * these calls will be done during the instantiation or later when a new variant is
     * requested.
     *
     * @param device the device on which to create the image
     * @param imageGcDrawer the ImageGcDrawer object to be called when a new image variant
     * for another zoom is required.
     * @param width the width of the new image in points
     * @param height the height of the new image in points
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
     *    <li>ERROR_NULL_ARGUMENT - if the ImageGcDrawer is null</li>
     * </ul>
     * @since 3.129
     */
    public DartImage(Device device, ImageGcDrawer imageGcDrawer, int width, int height, Image api) {
        super(device, api);
        if (imageGcDrawer == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        this.imageGcDrawer = imageGcDrawer;
        this.width = width;
        this.height = height;
        ImageData data = drawWithImageGcDrawer(imageGcDrawer, width, height, DPIUtil.getDeviceZoom());
        if (data == null)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        try {
            init(data, DPIUtil.getDeviceZoom());
            init();
        } finally {
        }
    }

    private ImageData drawWithImageGcDrawer(ImageGcDrawer imageGcDrawer, int width, int height, int zoom) {
        int gcStyle = imageGcDrawer.getGcStyle();
        Image image;
        if ((gcStyle & SWT.TRANSPARENT) != 0) {
            /* Create a 24 bit image data with alpha channel */
            final ImageData resultData = new ImageData(width, height, 24, new PaletteData(0xFF, 0xFF00, 0xFF0000));
            resultData.alphaData = new byte[width * height];
            image = new Image(device, resultData);
        } else {
            image = new Image(device, width, height);
        }
        GC gc = new GC(image, gcStyle);
        try {
            imageGcDrawer.drawOn(gc, width, height);
            ImageData imageData = image.getImageData(zoom);
            imageGcDrawer.postProcess(imageData);
            return imageData;
        } finally {
            gc.dispose();
            image.dispose();
        }
    }

    public void createAlpha() {
        AlphaInfo info = alphaInfo_100;
        if (info.transparentPixel == -1 && info.alpha == -1 && info.alphaData == null)
            return;
        try {
            if (info.transparentPixel != -1) {
                {
                }
            } else if (info.alpha != -1) {
            } else {
            }
        } finally {
        }
    }

    @Override
    void destroy() {
        if (memGC != null)
            memGC.dispose();
        memGC = null;
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
        if (object != null && ((Image) object).getImpl() instanceof SwtImage swtImage)
            return (this.getImageData().equals(swtImage.getImageData()));
        if (object == this.getApi())
            return true;
        if (!(object instanceof Image image))
            return false;
        if (device != image.getImpl()._device() || alphaInfo_100.transparentPixel != ((DartImage) image.getImpl()).alphaInfo_100.transparentPixel)
            return false;
        if (imageDataProvider != null && ((DartImage) image.getImpl()).imageDataProvider != null) {
            return styleFlag == ((DartImage) image.getImpl()).styleFlag && imageDataProvider.equals(((DartImage) image.getImpl()).imageDataProvider);
        } else if (imageFileNameProvider != null && ((DartImage) image.getImpl()).imageFileNameProvider != null) {
            return styleFlag == ((DartImage) image.getImpl()).styleFlag && imageFileNameProvider.equals(((DartImage) image.getImpl()).imageFileNameProvider);
        } else if (imageGcDrawer != null && ((DartImage) image.getImpl()).imageGcDrawer != null) {
            return styleFlag == ((DartImage) image.getImpl()).styleFlag && imageGcDrawer.equals(((DartImage) image.getImpl()).imageGcDrawer) && width == image.getImpl()._width() && height == image.getImpl()._height();
        } else {
        }
        return false;
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
        return this.background;
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
        try {
            if (width != -1 && height != -1) {
                return new Rectangle(0, 0, width, height);
            }
        } finally {
        }
        return new Rectangle(0, 0, width, height);
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
    @Deprecated(since = "2025-09", forRemoval = true)
    public Rectangle getBoundsInPixels() {
        Rectangle bounds = getBounds();
        return bounds;
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
        return getImageData(100);
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
    @Deprecated(since = "2025-09", forRemoval = true)
    public ImageData getImageDataAtCurrentZoom() {
        return getImageData(DPIUtil.getDeviceZoom());
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
            return imageFileNameProvider.hashCode();
        } else if (imageGcDrawer != null) {
            return Objects.hash(imageGcDrawer, height, width);
        } else {
        }
        return 0;
    }

    void init(int width, int height) {
        if (width <= 0 || height <= 0) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.getApi().type = SWT.BITMAP;
        this.width = width;
        this.height = height;
        if (alphaInfo_100 == null)
            alphaInfo_100 = new AlphaInfo();
    }

    void init(ImageData image, int imageZoom) {
        if (image == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        this.imageData = image;
        this.width = image.width * 100 / imageZoom;
        this.height = image.height * 100 / imageZoom;
        if (alphaInfo_100 == null)
            alphaInfo_100 = new AlphaInfo();
    }

    private void initWithSupplier(Function<Integer, Boolean> canLoadAtZoom, Function<Integer, ImageData> zoomToImageData) {
        ImageData imageData = zoomToImageData.apply(100);
        init(imageData, 100);
        if (canLoadAtZoom.apply(200)) {
            alphaInfo_200 = new AlphaInfo();
        }
    }

    void initNative(String filename) {
        try {
            // initByReferencingFile returns null if the file can't be found or is
            if (alphaInfo_100 == null)
                alphaInfo_100 = new AlphaInfo();
            // For compatibility, images created from .ico files are treated as SWT.ICON format, even though
            // they are no different than other bitmaps in Cocoa.
            if (filename.toLowerCase().endsWith(".ico")) {
                this.getApi().type = SWT.ICON;
            } else {
                this.getApi().type = SWT.BITMAP;
            }
        } finally {
        }
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
        if (getApi().type != SWT.BITMAP || memGC != null) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        try {
            if (data != null) {
                int mask = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
                if ((data.style & mask) == 0) {
                    data.style |= SWT.LEFT_TO_RIGHT;
                }
                data.device = device;
                data.font = ((SwtDevice) device.getImpl()).systemFont;
                data.image = this.getApi();
            }
        } finally {
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
        long context = hDC;
        try {
            if (context != 0) {
            }
            //		handle.setCacheMode(OS.NSImageCacheDefault);
        } finally {
        }
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
        return false;
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
        Color newValue = color;
        if (isDisposed())
            SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
        if (color == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (color.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        this.background = newValue;
        try {
            final int redOffset, greenOffset, blueOffset;
            {
                redOffset = 1;
                greenOffset = 2;
                blueOffset = 3;
            }
        } finally {
        }
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
        return null;
    }

    /**
     * <b>IMPORTANT:</b> This method is not part of the public
     * API for Image. It is marked public only so that it
     * can be shared within the packages provided by SWT.
     *
     * Draws a scaled image using the GC for a given imageData.
     *
     * @param gc the GC to draw on the resulting image
     * @param imageData the imageData which is used to draw the scaled Image
     * @param width the width of the original image
     * @param height the height of the original image
     * @param scaleFactor the factor with which the image is supposed to be scaled
     *
     * @noreference This method is not intended to be referenced by clients.
     */
    public static void drawScaled(GC gc, ImageData imageData, int width, int height, float scaleFactor) {
    }

    Color background;

    String filename;

    ImageData imageData;

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
