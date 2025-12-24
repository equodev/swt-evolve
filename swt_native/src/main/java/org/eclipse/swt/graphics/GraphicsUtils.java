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
     * Creates a copy of an Image, converting from SWT to Dart implementation if needed.
     * If the image is already a DartImage, it is returned as-is.
     * If the image is a SwtImage, a new DartImage is created with the same image data.
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
            DartImage dartImage = new DartImage(display, image.getImageData(), image);
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
            // Create a new FontData using the copy constructor
            return new FontData(fontData);
        } else {
            // Already DartFontData, return as-is
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
            FontData[] fontDataArray = font.getFontData();
            if (fontDataArray != null && fontDataArray.length > 0) {
                FontData copiedFontData = copyFontData(fontDataArray[0]);
                return new Font(font.getDevice(), copiedFontData);
            }
            return font;
        } else {
            return font;
        }
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