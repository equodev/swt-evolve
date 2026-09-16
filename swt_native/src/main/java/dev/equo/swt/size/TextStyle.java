package dev.equo.swt.size;

import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/** Font descriptor with numeric weight (100-900); id format matches gen_fonts.dart / FontMetricsUtil.getId. */
public final class TextStyle {
    private final String name;
    private final int size;
    private final boolean italic;
    private final int weight;
    private final double height;

    public TextStyle(String name, int size, boolean italic, int weight, double height) {
        this.name = name;
        this.size = size;
        this.italic = italic;
        this.weight = weight;
        this.height = height;
    }

    public String name() { return name; }
    public int size() { return size; }
    public boolean italic() { return italic; }
    public int weight() { return weight; }
    public double height() { return height; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextStyle)) return false;
        TextStyle other = (TextStyle) o;
        return java.util.Objects.equals(name, other.name)
            && size == other.size
            && italic == other.italic
            && weight == other.weight
            && Double.compare(height, other.height) == 0;
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(name, size, italic, weight, height); }

    @Override
    public String toString() { return "TextStyle[name=" + name + ", size=" + size + ", italic=" + italic + ", weight=" + weight + ", height=" + height + "]"; }


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
