package dev.equo.swt;// FontMetricsUtil.java
import dev.equo.swt.size.PointD;
import dev.equo.swt.size.TextStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public final class FontMetricsUtil {

    private FontMetricsUtil() {}

    /**
     * Computes scaled font metric values for the given font.
     * Returns int[] {ascent, descent, height, avgCharWidth}, or null if the
     * font has no entry in {@link GenFontMetrics#DATA}.
     */
    public static int[] computeFontMetrics(Font font) {
        if (font == null) return null;
        FontData fd = font.getFontData()[0];
        Metrics m = GenFontMetrics.DATA.get(getId(fd));
        if (m == null) return null;
        int h = fd.getHeight();
        double scale = (double) h / GenFontMetrics.BASE;
        Display display = Display.getCurrent();
        double dpiScale = display != null ? display.getDPI().x / 72.0 : 1.0;
        return new int[]{
            (int) Math.round(m.ascent() * h * dpiScale),
            (int) Math.round(m.descent() * h * dpiScale),
            (int) Math.round(m.height() * h * dpiScale),
            (int) Math.round(m.avgCharWidth() * scale * dpiScale)
        };
    }

    public static String getId(String name, boolean italic, boolean bold) {
        if (name == null || name.isBlank())
            name = "System";
        return name + "-" + (italic ? 1 : 0 ) + "-" + (bold ? 6 : 3);
    }

    public static String getId(FontData fd) {
        return getId(fd.getName(), isItalic(fd), isBold(fd));
    }

    public static boolean isBold(FontData f) {
        return switch (f.getStyle()) {
            case SWT.BOLD, SWT.BOLD | SWT.ITALIC -> true;
            default -> false;
        };
    }

    public static boolean isItalic(FontData f) {
        return switch (f.getStyle()) {
            case SWT.ITALIC, SWT.BOLD | SWT.ITALIC -> true;
            default -> false;
        };
    }

    public static PointD getFontSize(String text, Font font) {
        FontData fontDatum = font.getFontData()[0];
        return getFontSize(text, FontMetricsUtil.getId(fontDatum), fontDatum.getHeight());
    }

    public static PointD getFontSize(String text, TextStyle textStyle) {
        return getFontSize(text, getId(textStyle.name(), textStyle.italic(), textStyle.bold()), textStyle.size());
    }

    /**
     * Returns width/height for the given text and fontId at the requested size (height).
     * Uses per-glyph widths if available; falls back to avgCharWidth when glyph missing.
     *
     * @param text font text
     * @param fontId font family name (must exist in GeneratedFontMetrics.DATA)
     * @param height requested size (same units as those stored by generator; e.g. size)
     * @return Point2D.Double(width, height)
     */
    public static PointD getFontSize(String text, String fontId, int height) {
//        GeneratedFontMetrics.Metrics metrics = getMetrics(fontId, height);
        Metrics metrics = GenFontMetrics.DATA.get(fontId);
        if (metrics == null)
            return new PointD(0, 0);

        double scale = (double) height / GenFontMetrics.BASE;

        Display display = Display.getCurrent();
        double dpiScale = display != null ? display.getDPI().x / 72.0 : 1.0;

        double w = 0.0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);

            double glyphWidth;
            if (metrics.glyphWidths() != null) {
                // Array is indexed from 0, representing codepoints starting from GLYPH_START (32)
                int index = cp - GenFontMetrics.GLYPH_START;
                if (index >= 0 && index < metrics.glyphWidths().length) {
                    glyphWidth = metrics.glyphWidths()[index];
                } else {
                    glyphWidth = metrics.avgCharWidth(); // fallback for out of range
                }
            } else {
                glyphWidth = metrics.avgCharWidth(); // fallback for monospace fonts
            }
            w += glyphWidth * scale;
        }

        double h = (metrics.height() * height) + ((metrics.ascent() + metrics.descent()) * scale);
        return new PointD(w * dpiScale, h * dpiScale);
    }

    /**
     * Returns interpolated or extrapolated MetricsPerSize for requested font and size.
     * Uses linear interpolation between nearest stored sizes. If outside stored range,
     * uses simple proportional scaling relative to the nearest stored size.
     *
     * @param fontName font family name
     * @param size requested size
     * @return MetricsPerSize-like object with fields ascent, descent, height, avgCharWidth, glyphWidths
     */
    private static Metrics getMetrics(String fontName, int size) {
        Metrics m = GenFontMetrics.DATA.get(fontName);
        if (m == null) return null;

        // exact hit
        if (size == GenFontMetrics.BASE) return m;

        double scale = (double) size / GenFontMetrics.BASE;
        return scaledFrom(m, scale);
    }

//    // linear interpolation between two stored sizes
//    private static GeneratedFontMetrics.MetricsPerSize lerpMetrics(
//            GeneratedFontMetrics.MetricsPerSize a,
//            GeneratedFontMetrics.MetricsPerSize b,
//            double t,
//            int resultSize
//    ) {
//        double ascent = lerp(a.ascent, b.ascent, t);
//        double descent = lerp(a.descent, b.descent, t);
//        double height = lerp(a.height, b.height, t);
//        double avg = lerp(a.avgCharWidth, b.avgCharWidth, t);
//
//        // merge glyph sets: iterate union of keys, interpolating widths when both exist,
//        // otherwise use avg as fallback.
//        Set<Integer> keys = new HashSet<>();
//        keys.addAll(a.glyphWidths.keySet());
//        keys.addAll(b.glyphWidths.keySet());
//        Map<Integer, Double> glyphs = new HashMap<>();
//        for (Integer cp : keys) {
//            Double wa = a.glyphWidths.get(cp);
//            Double wb = b.glyphWidths.get(cp);
//            double w;
//            if (wa != null && wb != null) {
//                w = lerp(wa, wb, t);
//            } else if (wa != null) {
//                w = lerp(wa, avg, t); // gradually approach avg
//            } else if (wb != null) {
//                w = lerp(avg, wb, t);
//            } else {
//                w = avg;
//            }
//            glyphs.put(cp, w);
//        }
//
//        return new GeneratedFontMetrics.MetricsPerSize(resultSize, ascent, descent, height, avg, glyphs);
//    }

    // proportional scaling (extrapolation) from a base size
    private static Metrics scaledFrom(Metrics base, double scale) {
        double ascent = base.ascent() * scale;
        double descent = base.descent() * scale;
        double height = base.height() * scale;
        double avg = base.avgCharWidth() * scale;

        double[] glyphs = null;
        if (base.glyphWidths() != null) {
            glyphs = new double[base.glyphWidths().length];
            for (int i = 0; i < base.glyphWidths().length; i++) {
                glyphs[i] = base.glyphWidths()[i] * scale;
            }
        }
        return new Metrics(ascent, descent, height, avg, glyphs);
    }

}
