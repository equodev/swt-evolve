package dev.equo.swt;

public final class Metrics {
    /** The range of characters a glyph width or a kerning pair is stored for: printable ASCII. */
    private static final int FIRST_CHAR = 32, LAST_CHAR = 126, CHARS = LAST_CHAR - FIRST_CHAR + 1;
    /** A kerning table stores each adjustment in ten-thousandths of a pixel. */
    private static final double KERNING_UNIT = 1e-4;

    private final double ascent;
    private final double descent;
    private final double height;
    private final double avgCharWidth;
    private final double[] glyphWidths;
    private final String kerning;
    private volatile int[] kerningPairs;

    public Metrics(double ascent, double descent, double height, double avgCharWidth, double[] glyphWidths) {
        this(ascent, descent, height, avgCharWidth, glyphWidths, null);
    }

    /**
     * @param kerning each pair's adjustment in ten-thousandths of a pixel at the stored size
     *        ({@code "Te-1953,To-1709,"}); null for a face without kerning.
     */
    public Metrics(double ascent, double descent, double height, double avgCharWidth, double[] glyphWidths, String kerning) {
        this.ascent = ascent;
        this.descent = descent;
        this.height = height;
        this.avgCharWidth = avgCharWidth;
        this.glyphWidths = glyphWidths;
        this.kerning = kerning;
    }

    public double ascent() { return ascent; }
    public double descent() { return descent; }
    public double height() { return height; }
    public double avgCharWidth() { return avgCharWidth; }
    public double[] glyphWidths() { return glyphWidths; }

    /** The kerning table as the constructor took it, so faces that share one can share the text. */
    public String kerning() { return kerning; }

    /** The kerning of {@code first} followed by {@code second} at the stored size; 0 if none. */
    public double kerning(int first, int second) {
        if (kerning == null) return 0;
        int a = first - FIRST_CHAR, b = second - FIRST_CHAR;
        if (a < 0 || a >= CHARS || b < 0 || b >= CHARS) return 0;
        return kerningPairs()[a * CHARS + b] * KERNING_UNIT;
    }

    private int[] kerningPairs() {
        int[] pairs = kerningPairs;
        if (pairs == null) {
            pairs = new int[CHARS * CHARS];
            int at = 0, length = kerning.length();
            while (at + 2 < length) {
                int a = kerning.charAt(at) - FIRST_CHAR, b = kerning.charAt(at + 1) - FIRST_CHAR;
                at += 2;
                boolean negative = kerning.charAt(at) == '-';
                if (negative) at++;
                int units = 0;
                while (at < length && kerning.charAt(at) != ',') {
                    units = units * 10 + (kerning.charAt(at++) - '0');
                }
                at++;
                if (a >= 0 && a < CHARS && b >= 0 && b < CHARS) {
                    pairs[a * CHARS + b] = negative ? -units : units;
                }
            }
            kerningPairs = pairs;
        }
        return pairs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metrics)) return false;
        Metrics other = (Metrics) o;
        return Double.compare(ascent, other.ascent) == 0
            && Double.compare(descent, other.descent) == 0
            && Double.compare(height, other.height) == 0
            && Double.compare(avgCharWidth, other.avgCharWidth) == 0
            && java.util.Arrays.equals(glyphWidths, other.glyphWidths)
            && java.util.Objects.equals(kerning, other.kerning);
    }

    @Override
    public int hashCode() { return 31 * java.util.Objects.hash(ascent, descent, height, avgCharWidth, kerning) + java.util.Arrays.hashCode(glyphWidths); }

    @Override
    public String toString() { return "Metrics[ascent=" + ascent + ", descent=" + descent + ", height=" + height + ", avgCharWidth=" + avgCharWidth + ", glyphWidths=" + java.util.Arrays.toString(glyphWidths) + "]"; }

}
