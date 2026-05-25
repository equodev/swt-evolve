package org.eclipse.swt.graphics;

/**
 * Utility class for converting SWT graphics objects (Image, FontData, etc.) to Dart equivalents.
 * This class provides methods to copy and convert graphics resources from native SWT implementations
 * to Dart implementations for cross-platform compatibility.
 */
public class GraphicsUtilsSwt {

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
}