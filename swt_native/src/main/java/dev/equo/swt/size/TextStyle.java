package dev.equo.swt.size;

import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/** Font descriptor with numeric weight (100-900); id format matches gen_fonts.dart / FontMetricsUtil.getId. */
public record TextStyle(String name, int size, boolean italic, int weight, double height) {

    public TextStyle(String name, int size, boolean italic, int weight){
        this(name, size, italic, weight, 0);
    }

    public static TextStyle from(Font font) {
        FontData fd = font.getFontData()[0];
        return new TextStyle(
            fd.getName(),
            fd.getHeight(),
            FontMetricsUtil.isItalic(fd),
            FontMetricsUtil.getWeightFromBold(FontMetricsUtil.isBold(fd)));
    }

    public TextStyle withStyleFrom(Font font) {
        if (font == null || font.getFontData() == null || font.getFontData().length == 0) {
            return this;
        }
        FontData fd = font.getFontData()[0];
        boolean italic = FontMetricsUtil.isItalic(fd);
        int w = FontMetricsUtil.isBold(fd) ? 700 : this.weight;
        return new TextStyle(name, size, italic, w, this.height);
    }

    public static TextStyle def() {
        return new TextStyle("System", 10, false, 400);
    }
}
