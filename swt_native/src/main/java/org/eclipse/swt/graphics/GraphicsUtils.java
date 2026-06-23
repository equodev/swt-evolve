package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for converting SWT graphics objects (Image, FontData, etc.) to Dart equivalents.
 * This class provides methods to copy and convert graphics resources from native SWT implementations
 * to Dart implementations for cross-platform compatibility.
 */
public class GraphicsUtils {

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
        } else if (image.getImpl() instanceof DartImage di) {
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

    private static java.util.Map<String, java.io.File> assetFilesCache = null;

    private static void initAssetsCache() {
        if (assetFilesCache != null) return;
        assetFilesCache = new java.util.HashMap<>();
        String assetsPath = Config.getConfigFlags().assets_path;
        if (assetsPath == null || assetsPath.isBlank()) return;
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
        String ext = n.substring(dot + 1).toLowerCase();
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
        if (path == null) return null;
        String fileName = new java.io.File(path).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
    }
}