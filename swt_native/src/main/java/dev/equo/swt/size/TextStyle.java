package dev.equo.swt.size;

import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public record TextStyle(String name, int size, boolean italic, boolean bold) {

    public static TextStyle from(Font font) {
        FontData fd = font.getFontData()[0];
        return new TextStyle(fd.getName(), fd.getHeight(), FontMetricsUtil.isItalic(fd), FontMetricsUtil.isBold(fd));
    }

}
