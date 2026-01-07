package dev.equo.swt.size;

import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public record FontVariation(String name, boolean italic, boolean bold) {
    public String getId() {
        return getId(name, italic, bold);
    }

    @Override
    public String toString() {
        String var = "normal";
        if (bold && italic) var = "itelic bold";
        else if (bold) var = "bold";
        else if (italic) var = "italic";
        return name + " ("+var+")";
    }

    public static FontVariation from(Font font) {
        FontData fd = font.getFontData()[0];
        return from(fd);
    }

    public static FontVariation from(FontData fd) {
        return new FontVariation(fd.getName(), FontMetricsUtil.isItalic(fd), FontMetricsUtil.isBold(fd));
    }

    public static String getId(String name, boolean italic, boolean bold) {
        return FontMetricsUtil.getId(name, italic, bold);
    }

    public static String getId(FontData fd) {
        return FontMetricsUtil.getId(fd);
    }

}
