package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for converting SWT graphics objects (Image, FontData, etc.) to Dart equivalents.
 * This class provides methods to copy and convert graphics resources from native SWT implementations
 * to Dart implementations for cross-platform compatibility.
 */
public class GraphicsUtils {

    /** The pixels of a new {@code Image(device, width, height)}: SWT specifies it starts white. */
    public static ImageData blankImageData(int width, int height) {
        ImageData data = new ImageData(width, height, 32, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        java.util.Arrays.fill(data.data, (byte) 0xFF);
        return data;
    }

    /**
     * Scales a buffer held at the raster scale to {@code zoom}, as DPIUtil's smooth scaling does: drawn
     * with antialiasing into another Image. That Image is built from ImageData, so it is stored at 100 %
     * and reading it back needs no further scaling. Its pixels are not sent with the draw: the render
     * side draws onto an empty, transparent canvas.
     */
    public static ImageData scaleRasterBuffer(Device device, ImageData buffer, int logicalWidth, int logicalHeight, int zoom) {
        int width = Math.max(1, Math.round(logicalWidth * zoom / 100f));
        int height = Math.max(1, Math.round(logicalHeight * zoom / 100f));
        Image source = new Image(device, buffer);
        Image target = new Image(device, new ImageData(width, height, 32, new PaletteData(0xFF0000, 0xFF00, 0xFF)));
        try {
            GC gc = new GC(target);
            try {
                gc.setAntialias(SWT.ON);
                gc.drawImage(source, 0, 0, buffer.width, buffer.height, 0, 0, width, height);
            } finally {
                gc.dispose();
            }
            return target.getImageData();
        } finally {
            source.dispose();
            target.dispose();
        }
    }

    /**
     * Creates a copy of an ImageData.
     * This is used internally by DartImage constructors to make defensive copies
     * of ImageData passed from external sources.
     *
     * @param imageData the image data to copy (can be null)
     * @return a copy of the image data, or null if input is null
     */
    public static ImageData copyImageData(ImageData imageData) {
        if (imageData == null) {
            return null;
        }
        return (ImageData) imageData.clone();
    }

    /**
     * Applies an {@code SWT.IMAGE_DISABLE} / {@code SWT.IMAGE_GRAY} style flag to a Dart-backed
     * image, in place.
     * <p>
     * Native SWT transforms the pixels of the platform image handle. There is no handle here, so
     * the transform has to land on the {@link ImageData} that {@code VImage} puts on the wire —
     * that data is the only description of the image Flutter can paint.
     *
     * @param image the image to transform (can be null)
     * @param styleFlag the merged style flag, as {@code source.styleFlag | flag}
     */
    public static void applyImageStyleFlag(DartImage image, int styleFlag) {
        if (image == null || image.imageData == null) {
            return;
        }
        ImageData transformed;
        switch (styleFlag) {
            case SWT.IMAGE_DISABLE:
                transformed = applyDisableImageData(image.imageData);
                break;
            case SWT.IMAGE_GRAY:
                transformed = applyGrayImageData(image.imageData);
                break;
            default:
                return;
        }
        image.imageData = transformed;
        // image_utils.dart resolves an image by svgContent, then by filename (asset replacement and
        // icon map), and only then by the pixel bytes. A transformed image that keeps either source
        // would be painted from it — i.e. as the untransformed original.
        image.filename = null;
        image.svgContent = null;
    }

    private static ImageData applyDisableImageData(ImageData data) {
        int width = data.width;
        int height = data.height;
        PaletteData palette = data.palette;
        ImageData newData = new ImageData(width, height, 32, new PaletteData(0xFF, 0xFF00, 0xFF0000));
        newData.alpha = data.alpha;
        newData.alphaData = copyOf(data.alphaData);
        newData.maskData = copyOf(data.maskData);
        newData.maskPad = data.maskPad;
        if (data.transparentPixel != -1)
            newData.transparentPixel = 0;
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
                    int value = disabledPixelValue(red, green, blue);
                    newData.setAlpha(x, y, data.getAlpha(x, y));
                    newData.setPixel(x, y, newData.palette.getPixel(new RGB(value, value, value)));
                }
            }
        }
        return newData;
    }

    private static ImageData applyGrayImageData(ImageData data) {
        int pWidth = data.width;
        int pHeight = data.height;
        PaletteData palette = data.palette;
        if (!palette.isDirect) {
            /* Convert the palette entries to gray. The palette is shared with the source image
             * (ImageData.clone() copies the pixels, not the palette), so build a fresh one. */
            RGB[] source = palette.getRGBs();
            RGB[] rgbs = new RGB[source.length];
            for (int i = 0; i < source.length; i++) {
                RGB color = source[i];
                if (data.transparentPixel == i) {
                    rgbs[i] = new RGB(color.red, color.green, color.blue);
                    continue;
                }
                int red = color.red;
                int green = color.green;
                int blue = color.blue;
                int intensity = (red + red + green + green + green + green + green + blue) >> 3;
                rgbs[i] = new RGB(intensity, intensity, intensity);
            }
            data.palette = new PaletteData(rgbs);
            return data;
        }
        /* Create an 8 bit depth image data with a gray palette. */
        RGB[] rgbs = new RGB[256];
        for (int i = 0; i < rgbs.length; i++) {
            rgbs[i] = new RGB(i, i, i);
        }
        ImageData newData = new ImageData(pWidth, pHeight, 8, new PaletteData(rgbs));
        newData.alpha = data.alpha;
        newData.alphaData = copyOf(data.alphaData);
        newData.maskData = copyOf(data.maskData);
        newData.maskPad = data.maskPad;
        if (data.transparentPixel != -1)
            newData.transparentPixel = 254;
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
        return newData;
    }

    /**
     * The grayscaled contrast/brightness curve SWT's own disablement transformer defaults to
     * (contrast 0.2, brightness 2.9).
     * <p>
     * Inlined rather than delegated: {@code ImageColorTransformer} only exists in recent SWT, and
     * this class is compiled against every supported version. The consequence is that the
     * {@code org.eclipse.swt.image.disablement} override is not honoured here — only the default
     * algorithm, which is what an application gets unless it sets that property.
     */
    private static int disabledPixelValue(int red, int green, int blue) {
        int gray = Math.min((77 * red + 151 * green + 28 * blue) / 255, 255);
        return (int) Math.min(Math.max(0.2f * (gray * 2.9f - 128) + 128, 0), 255);
    }

    private static byte[] copyOf(byte[] bytes) {
        return bytes == null ? null : bytes.clone();
    }

    /**
     * Creates a copy of an Image, converting from SWT to Dart implementation if needed.
     * If the image is already a DartImage, it is returned as-is.
     * If the image is a SwtImage, a new DartImage is created with the same image data.
     * Note: getImageData() already returns a copy, so no additional cloning is needed here.
     *
     * @param display the display on which to create the new image
     * @param image the image to copy (can be null)
     * @return a copy of the image as a DartImage, or null if input is null
     */
    public static Image copyImage(Display display, Image image) {
        if (image == null) {
            return null;
        }

        if (!(image.getImpl() instanceof DartImage)) {
            ImageData imageData = image.getImageData();
            DartImage dartImage = new DartImage(display, imageData, null);
            if (!Config.getConfigFlags().image_disable_icons_replacement && image.getImpl()._filename() != null) {
                if (!tryApplyReplacement(image.getImpl()._filename(), dartImage)) {
                    dartImage.filename = image.getImpl()._filename();
                }
            }
            return dartImage.getApi();
        } else if (image.getImpl() instanceof DartImage) { DartImage di = (DartImage) image.getImpl();
            if (!Config.getConfigFlags().image_disable_icons_replacement && di.filename != null) {
                if (tryApplyReplacement(di.filename, di)) {
                    di.filename = null;
                }
            }
            return image;
        } else {
            return image;
        }
    }

    /**
     * As {@link #copyImage}, for an image a GC is about to draw.
     * <p>
     * A GC opened on an Image renders only when told to — on dispose, or on an explicit snapshot —
     * unlike native SWT, where every draw is immediately visible in the image. So an application
     * that draws an image and blits it while that GC is still open would put the image's untouched
     * backing buffer on the wire. draw2d's {@code BufferedGraphicsSource} does exactly that: it
     * paints the figure tree into an off-screen Image and blits it, and never disposes the image's
     * GC ({@code SWTGraphics#dispose()} leaves it open), so nothing else ever triggers the render.
     * <p>
     * This has to run at the draw call, not at the wire read in {@code DartImage#_imageDataForWire()}:
     * that runs inside serialization, and waiting for the snapshot pumps the event loop, which
     * would re-enter it.
     *
     * @param display the display on which to create the new image
     * @param image the image about to be drawn (can be null)
     * @return the image to put on the wire, as {@link #copyImage}
     */
    public static Image copyImageForDraw(Display display, Image image) {
        if (image != null && !image.isDisposed() && image.getImpl() instanceof DartImage) { DartImage di = (DartImage) image.getImpl();
            GC openGc = di._memGC();
            if (openGc != null && !openGc.isDisposed() && openGc.getImpl() instanceof DartGC) { DartGC dgc = (DartGC) openGc.getImpl();
                dgc.requestRenderSnapshotAndWait();
            }
        }
        return copyImage(display, image);
    }

    /** The colour of the image data's transparent pixel, or null when it has none. */
    public static Color transparentColor(Device device, ImageData data) {
        if (data == null || data.transparentPixel == -1) return null;
        return new Color(device, data.palette.getRGB(data.transparentPixel));
    }

    /**
     * Creates an independent, widget-owned copy of a Color with the same RGBA channels.
     * <p>
     * Colors passed to a widget setter (background, foreground, …) may be disposed by the caller
     * afterwards — legal in SWT, since native captures the value at set-time. In the Dart backend the
     * widget instead holds the Java {@code Color} reference and reads its channels later, at async
     * serialization; a disposed color throws ERROR_GRAPHIC_DISPOSED there and aborts the whole widget
     * payload. Storing this independent copy decouples the widget from the caller's disposal.
     * Colors carry no native handle, so this is a cheap value copy.
     *
     * @param color the color to copy (can be null)
     * @return an independent DartColor copy, or null if input is null
     */
    public static Color copyColor(Color color) {
        if (color == null) {
            return null;
        }
        if (color.isDisposed()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        return ((DartColor) color.getImpl())._copy();
    }

    /** Marks {@code color} as the system colour {@code id}; its {@link #copyColor copies} keep the mark. */
    public static Color tagSystemColor(Color color, int id) {
        ((DartColor) color.getImpl()).systemColorId = id;
        return color;
    }

    /** The system colour id {@code color} was {@link #tagSystemColor tagged} with, 0 for none. */
    public static int systemColorId(Color color) {
        return color == null ? 0 : ((DartColor) color.getImpl()).systemColorId;
    }

    /**
     * Marks {@code shown} as shown in place of the application colour {@code own}; its
     * {@link #copyColor copies} keep the mark.
     */
    public static Color standIn(Color shown, Color own) {
        ((DartColor) shown.getImpl()).standsInFor = own;
        return shown;
    }

    /** The application colour {@code color} is {@link #standIn shown in place of}, null for none. */
    public static Color standsInFor(Color color) {
        return color == null ? null : ((DartColor) color.getImpl()).standsInFor;
    }

    /** Changes a system colour's channels in place, alpha kept: it follows the theme's variant. */
    public static void setRGB(Color color, RGB rgb) {
        DartColor c = (DartColor) color.getImpl();
        c.red = rgb.red;
        c.green = rgb.green;
        c.blue = rgb.blue;
    }

    /** The color's alpha, 255 on a release whose Color has no alpha. */
    public static int getAlpha(Color color) {
        return color.getImpl().getAlpha();
    }

    private static java.util.Map<String, java.io.File> assetFilesCache = null;

    private static void initAssetsCache() {
        if (assetFilesCache != null) return;
        assetFilesCache = new java.util.HashMap<>();
        String assetsPath = Config.getConfigFlags().assets_path;
        if (assetsPath == null || assetsPath.trim().isEmpty()) return;
        assetsPath = assetsPath.replace("\"", "");
        java.io.File dir = new java.io.File(assetsPath);
        if (!dir.isAbsolute()) {
            dir = new java.io.File(System.getProperty("user.dir"), assetsPath);
        }
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            for (java.io.File f : files) {
                String n = f.getName();
                int dot = n.lastIndexOf('.');
                if (dot > 0) {
                    assetFilesCache.put(n.substring(0, dot), f);
                }
            }
        }
    }

    // Returns true if a replacement was found and applied to the target DartImage.
    private static boolean tryApplyReplacement(String filename, DartImage target) {
        initAssetsCache();
        java.io.File file = assetFilesCache.get(filename);
        if (file == null) return false;
        String n = file.getName();
        int dot = n.lastIndexOf('.');
        String ext = n.substring(dot + 1).toLowerCase(java.util.Locale.ROOT);
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            if (ext.equals("svg")) {
                target.svgContent = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            } else {
                // Use ImageData as a byte transport: Flutter decodes the raw encoded bytes
                // via Image.memory(), so width/height/depth are dummy values.
                ImageData imgData = new ImageData(1, 1, 32, new PaletteData(0xFF0000, 0xFF00, 0xFF));
                imgData.data = bytes;
                target.imageData = imgData;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates a copy of a FontData, converting from SWT to Dart implementation if needed.
     * If the fontData is already a DartFontData, it is returned as-is.
     * If the fontData is a SwtFontData, a new DartFontData is created with the same font properties.
     *
     * @param fontData the font data to copy (can be null)
     * @return a copy of the font data as a DartFontData, or null if input is null
     */
    public static FontData copyFontData(FontData fontData) {
        if (fontData == null) {
            return null;
        }

        if (!(fontData.getImpl() instanceof DartFontData)) {
            FontData copy = new FontData((IFontData) null);
            copy.setImpl(new DartFontData(fontData.getName(), fontData.getHeight(), fontData.getStyle(), copy));
            return copy;
        } else {
            return fontData;
        }
    }

    /**
     * Creates a copy of a Font, converting from SWT to Dart implementation if needed.
     * If the font is already a DartFont, it is returned as-is.
     * If the font is a SwtFont, a new DartFont is created with the same font properties.
     * The device is obtained from the original font using font.getDevice().
     *
     * @param font the font to copy (can be null)
     * @return a copy of the font as a DartFont, or null if input is null
     */
    public static Font copyFont(Font font) {
        if (font == null) {
            return null;
        }
        if (!(font.getImpl() instanceof DartFont)) {
            FontData copiedFontData = copyFontData(font.getFontData()[0]);
            DartFont dartFont = new DartFont(font.getDevice(), copiedFontData, null);
            return dartFont.getApi();
        } else {
            return font;
        }
    }

    /**
     * Returns a default background color for Dart controls.
     * This is used when a DartControl has no explicitly set background color.
     *
     * @param display the display to create the color on
     * @return a default background color (white with full opacity)
     */
    public static Color getDefaultBackground(Display display) {
        return new Color(display, 255, 255, 255);
    }

    /**
     * Extracts the filename (without extension) from a given file path.
     *
     * @param path the full file path (can be null)
     * @return the filename without extension, the original filename if no extension is found,
     *     or null if path is null
     */
    static String getFilename(String path) {
        if (path == null) {
            return null;
        }
        String fileName = new java.io.File(path).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
    }

    private static final String IMAGE_DESCRIPTOR = "org.eclipse.jface.resource.ImageDescriptor";
    private static final String DEVICE_RESOURCE_DESCRIPTOR = "org.eclipse.jface.resource.DeviceResourceDescriptor";

    /**
     * The name, without extension, of the resource a provider reads its pixels from, or null when it
     * does not say. A provider built by a JFace {@code ImageDescriptor} holds that descriptor, which
     * adapts to the resource's URL; this is how an icon packed inside a jar is named. Older JFace
     * builds a provider nested in the descriptor that holds the URL itself, as a URL or a String.
     */
    static String getFilename(ImageDataProvider provider) {
        if (provider == null) {
            return null;
        }
        Class<?> owner = provider.getClass().getEnclosingClass();
        boolean nestedInDescriptor = owner != null && isImageDescriptor(owner);
        for (Class<?> c = provider.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                Class<?> type = f.getType();
                boolean holdsUrl = nestedInDescriptor && (type == java.net.URL.class || type == String.class);
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) || !(holdsUrl || canHoldImageDescriptor(type))) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object value = f.get(provider);
                    java.net.URL url = holdsUrl ? asUrl(value) : descriptorUrl(value);
                    if (url != null) {
                        return urlName(url);
                    }
                    // A composite/decoration descriptor (e.g. a navigator folder JFace draws as base
                    // icon + overlays via DecorationOverlayIcon) does not adapt to a URL; recover the
                    // base resource's name so it still matches the icon pack. Gated behind
                    // gc_icons_replacement: naming a decorated image lets the pack replace it and drop
                    // the overlay (error/VCS badges), so it stays off unless the product opts into the
                    // same aggressive icon replacement the owner-draw path already needs.
                    if (!holdsUrl && value != null && isImageDescriptor(value.getClass())
                            && Config.getConfigFlags().gc_icons_replacement) {
                        String base = compositeBaseName(value);
                        if (base != null) {
                            return base;
                        }
                    }
                } catch (RuntimeException | IllegalAccessException e) {
                    // A field we may not read names nothing.
                }
            }
        }
        return null;
    }

    /**
     * The base resource name of a composite image descriptor (e.g. {@code DecorationOverlayIcon}),
     * recovered from the base it holds — an {@code Image} that already knows its own name, or a nested
     * {@code ImageDescriptor} that adapts to a URL. Decoration can nest (a navigator item is decorated
     * more than once), so the base chain is walked recursively; bounded and cycle-guarded, since these
     * graphs hold back-references. The overlay is dropped, which is the intended look here.
     * <p>
     * It commits to the FIRST field that holds an {@code Image} or {@code ImageDescriptor} — the base —
     * and returns whatever that yields, even nothing, rather than falling through to a sibling field.
     * A composite keeps its base there ({@code DecorationOverlayIcon.referenceImageOrDescriptor}) and
     * its overlays elsewhere (an array, skipped), so this never hands back an overlay's name when the
     * base has none of its own.
     */
    private static String compositeBaseName(Object descriptor) {
        return compositeBaseName(descriptor, 0,
                java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()));
    }

    private static String compositeBaseName(Object descriptor, int depth, java.util.Set<Object> seen) {
        if (descriptor == null || depth > 8 || !seen.add(descriptor)) {
            return null;
        }
        for (Class<?> c = descriptor.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                Object value;
                try {
                    f.setAccessible(true);
                    value = f.get(descriptor);
                } catch (RuntimeException | IllegalAccessException e) {
                    continue;
                }
                if (value instanceof Image) {
                    return ((Image) value).getImpl()._filename();
                }
                if (value != null && isImageDescriptor(value.getClass())) {
                    java.net.URL url = descriptorUrl(value);
                    return url != null ? getFilename(url.getPath()) : compositeBaseName(value, depth + 1, seen);
                }
            }
        }
        return null;
    }

    /**
     * The name of the icon a URL points to: its file name without extension, or, for a URL that keeps
     * the icon's key in the host and leaves the path empty ({@code icon://IMG_EDIT}), that key without
     * a leading {@code IMG_} and lower-cased ({@code edit}), the way icon pack entries are named.
     */
    static String urlName(java.net.URL url) {
        String name = getFilename(url.getPath());
        String host = url.getHost();
        if ((name == null || name.isEmpty()) && host != null && !host.isEmpty()) {
            String key = host.regionMatches(true, 0, "IMG_", 0, 4) ? host.substring(4) : host;
            return key.toLowerCase(java.util.Locale.ROOT);
        }
        return name;
    }

    private static java.net.URL asUrl(Object value) {
        if (value instanceof java.net.URL) {
            return (java.net.URL) value;
        }
        try {
            return value instanceof String ? new java.net.URL((String) value) : null;
        } catch (java.net.MalformedURLException e) {
            return null;
        }
    }

    private static java.net.URL descriptorUrl(Object holder) {
        if (holder == null || !isImageDescriptor(holder.getClass())) {
            return null;
        }
        try {
            java.lang.reflect.Method getAdapter = holder.getClass().getMethod("getAdapter", Class.class);
            getAdapter.setAccessible(true);
            Object url = getAdapter.invoke(holder, java.net.URL.class);
            return url instanceof java.net.URL ? (java.net.URL) url : null;
        } catch (RuntimeException | ReflectiveOperationException e) {
            return null;
        }
    }

    /** Whether a field of this declared type can hold an {@code ImageDescriptor}. */
    private static boolean canHoldImageDescriptor(Class<?> type) {
        return type == Object.class || DEVICE_RESOURCE_DESCRIPTOR.equals(type.getName()) || isImageDescriptor(type);
    }

    private static boolean isImageDescriptor(Class<?> type) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            if (IMAGE_DESCRIPTOR.equals(c.getName())) {
                return true;
            }
        }
        return false;
    }
}