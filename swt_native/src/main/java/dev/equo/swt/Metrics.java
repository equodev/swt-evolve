package dev.equo.swt;

public final class Metrics {
    private final double ascent;
    private final double descent;
    private final double height;
    private final double avgCharWidth;
    private final double[] glyphWidths;

    public Metrics(double ascent, double descent, double height, double avgCharWidth, double[] glyphWidths) {
        this.ascent = ascent;
        this.descent = descent;
        this.height = height;
        this.avgCharWidth = avgCharWidth;
        this.glyphWidths = glyphWidths;
    }

    public double ascent() { return ascent; }
    public double descent() { return descent; }
    public double height() { return height; }
    public double avgCharWidth() { return avgCharWidth; }
    public double[] glyphWidths() { return glyphWidths; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metrics)) return false;
        Metrics other = (Metrics) o;
        return Double.compare(ascent, other.ascent) == 0
            && Double.compare(descent, other.descent) == 0
            && Double.compare(height, other.height) == 0
            && Double.compare(avgCharWidth, other.avgCharWidth) == 0
            && java.util.Objects.equals(glyphWidths, other.glyphWidths);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(ascent, descent, height, avgCharWidth, glyphWidths); }

    @Override
    public String toString() { return "Metrics[ascent=" + ascent + ", descent=" + descent + ", height=" + height + ", avgCharWidth=" + avgCharWidth + ", glyphWidths=" + glyphWidths + "]"; }

}
