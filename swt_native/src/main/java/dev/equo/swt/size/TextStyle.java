package dev.equo.swt.size;

import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public record TextStyle(String name, int size, boolean italic, boolean bold) {

    public static TextStyle from(Font font) {
        FontData fd = font.getFontData()[0];
        return new TextStyle(fd.getName(), fd.getHeight(), FontMetricsUtil.isItalic(fd), FontMetricsUtil.isBold(fd));
    }

    public TextStyle withStyleFrom(Font font) {
        if (font == null || font.getFontData() == null || font.getFontData().length == 0) {
            return this;
        }
        FontData fd = font.getFontData()[0];
        return new TextStyle(name, size, FontMetricsUtil.isItalic(fd), FontMetricsUtil.isBold(fd));
    }

    public static TextStyle def() {
        return new TextStyle("System", 10, false, false);
    }

}
