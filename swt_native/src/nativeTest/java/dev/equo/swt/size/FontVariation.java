package dev.equo.swt.size;

import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.graphics.FontData;

public record FontVariation(String name, boolean italic, int weight) {
    public String getId() {
        return FontMetricsUtil.getId(name, italic, weight);
    }

    @Override
    public String toString() {
        String var = weight >= 700 ? "bold" : weight <= 300 ? "light" : "normal";
        if (italic) var += " italic";
        return name + " (" + var + " " + weight + ")";
    }

    public static FontVariation from(FontData fd) {
        return new FontVariation(fd.getName(), FontMetricsUtil.isItalic(fd),
                FontMetricsUtil.getWeightFromBold(FontMetricsUtil.isBold(fd)));
    }

}
