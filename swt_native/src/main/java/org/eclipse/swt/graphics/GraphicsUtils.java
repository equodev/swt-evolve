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

        if (image.getImpl() instanceof SwtImage si) {
            ImageData imageData = image.getImageData();
            DartImage dartImage = new DartImage(display, imageData, null);
            if (!Config.getConfigFlags().image_disable_icons_replacement && si.filename != null) {
                dartImage.filename = si.filename;
            }
            return dartImage.getApi();
        } else {
            return image;
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

        if (fontData.getImpl() instanceof SwtFontData) {
            FontData copy = new FontData((IFontData) null);
            copy.setImpl(new DartFontData(fontData, copy));
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
        if (font.getImpl() instanceof SwtFont) {
            FontData copiedFontData = copyFontData(font.getFontData()[0]);
            DartFont dartFont = new DartFont(font.getDevice(), copiedFontData, null);
            return dartFont.getApi();
        } else {
            return font;
        }
    }

    /**
     * Creates a SwtFont from any Font using only the basic font properties (name, height, style).
     * If the font is already a SwtFont, it is returned as-is.
     * If the font is a DartFont, a new SwtFont is created.
     *
     * @param font the font to convert (can be null)
     * @return a SwtFont with the same name, height and style, or null if input is null
     */
    public static Font copyFontToSwt(Font font) {
        if (font == null) {
            return null;
        }
        if (font.getImpl() instanceof DartFont) {
            FontData fd = font.getFontData()[0];
            SwtFont swtFont = new SwtFont(font.getDevice(), fd.getName(), fd.getHeight(), fd.getStyle(), null);
            return swtFont.getApi();
        }
        return font;
    }

    /**
     * Creates a SwtFontData from any FontData using the basic font properties (name, height, style).
     * If the fontData is already a SwtFontData, it is returned as-is.
     * If the fontData is a DartFontData, a new SwtFontData is created.
     *
     * @param fontData the fontData to convert (can be null)
     * @return a FontData with SwtFontData, or null if input is null
     */
    public static FontData copyFontDataToSwt(FontData fontData) {
        if (fontData == null) {
            return null;
        }
        if (fontData.getImpl() instanceof DartFontData) {
            SwtFontData swtFontData = new SwtFontData(fontData.getName(), fontData.getHeight(), fontData.getStyle(), null);
            return swtFontData.getApi();
        }
        return fontData;
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
     * @param path the full file path
     * @return the filename without extension, or the original filename if no extension is found
     */
    static String getFilename(String path) {
        String fileName = new java.io.File(path).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
    }
}