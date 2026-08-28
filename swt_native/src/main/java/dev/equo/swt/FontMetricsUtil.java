package dev.equo.swt;// FontMetricsUtil.java
import dev.equo.swt.size.PointD;
import dev.equo.swt.size.TextStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public final class FontMetricsUtil {

    private FontMetricsUtil() {}

    private static final java.util.Map<String, String> fontNameSubstitutions = new java.util.HashMap<>();

    /** Register a font-name substitution for metrics lookup (called by webMain during static init). */
    public static void registerFontSubstitution(String from, String to) {
        fontNameSubstitutions.put(from, to);
    }

    /** Return the substituted font name, or the original if no substitution is registered. */
    public static String substituteFontName(String name) {
        return fontNameSubstitutions.getOrDefault(name, name);
    }

    /** Returns the set of font names that have substitutions registered (e.g. for web testing). */
    public static java.util.Set<String> fontSubstitutionKeys() {
        return java.util.Collections.unmodifiableSet(fontNameSubstitutions.keySet());
    }

    /**
     * The font families a Dart-backed {@code Device} can actually honour: every name we know how
     * to map to a real typeface, plus the system font.
     *
     * <p>Native SWT enumerates the OS font list; there is no OS to ask here, so without this the
     * impl reports only the system font (named literally "system" on web). Callers that intersect
     * a CSS {@code font-family} list against this set — Eclipse theming does, via
     * {@code Display.getFontList(null, true)} — then match nothing and fall back to null, which
     * they happily pass to {@code FontData.setName}.</p>
     *
     * <p>Reads the substitution table, never populates it: registering is the web runtime's call
     * (see {@code WebFlutterServer}), and forcing it here would make a desktop build substitute
     * names it can render natively.</p>
     */
    public static FontData[] getFontList(String faceName, boolean scalable, FontData systemFontData) {
        if (!scalable) return new FontData[0];
        java.util.LinkedHashSet<String> names = new java.util.LinkedHashSet<>();
        if (systemFontData != null && systemFontData.getName() != null) {
            names.add(systemFontData.getName());
        }
        names.addAll(fontSubstitutionKeys());
        int height = Math.max(1, systemFontData != null ? systemFontData.getHeight() : 11);
        java.util.List<FontData> result = new java.util.ArrayList<>();
        for (String name : names) {
            if (faceName == null || faceName.equalsIgnoreCase(name)) {
                result.add(new FontData(name, height, SWT.NORMAL));
            }
        }
        return result.toArray(new FontData[0]);
    }

    /**
     * Device pixels per 72-dpi point. {@link GenFontMetrics} stores point advances while text is
     * painted at the display DPI, so widths derived from the table must be multiplied by this to
     * land on painted glyphs.
     */
    public static double dpiScale() {
        Display display = Display.getCurrent();
        return (display != null && display.getDPI().x > 0) ? display.getDPI().x / 72.0 : 96.0 / 72.0;
    }

    /**
     * Computes scaled font metric values for the given font.
     * Returns int[] {ascent, descent, height, avgCharWidth}, or null if the
     * font has no entry in {@link GenFontMetrics#DATA}.
     */
    public static int[] computeFontMetrics(Font font) {
        if (font == null) return null;
        FontData fd = font.getFontData()[0];
        Metrics m = GenFontMetrics.DATA.get(getId(fd));
        if (m == null) m = GenFontMetrics.DATA.get("Verdana-0-3");
        if (m == null) return null;
        Display display = Display.getCurrent();
        int h = effectiveHeight(fd, display);
        double scale = (double) h / GenFontMetrics.BASE;
        double dpiScale = display != null ? display.getDPI().x / 72.0 : 1.0;
        return new int[]{
            (int) Math.round(m.ascent() * h * dpiScale),
            (int) Math.round(m.descent() * h * dpiScale),
            (int) Math.round(m.height() * h * dpiScale),
            (int) Math.round(m.avgCharWidth() * scale * dpiScale)
        };
    }

    /**
     * Height 0 is a legal FontData value (native SWT allows it too): native SWT resolves it to a
     * real handle and every later query — {@code getFontData()} included — reads the OS-assigned
     * size back off that handle, so nothing downstream ever observes 0. We have no OS to do that
     * substitution for us, so {@link org.eclipse.swt.graphics.DartFont}'s constructors substitute
     * a real height at construction time (see the generator's {@code init(String, float, int, ...)}
     * transform) — this mirrors that same substitution for callers still holding a raw
     * {@link FontData} (e.g. a {@code FontData} built directly, never passed through a Font).
     */
    public static int substituteHeight(float height, Display display) {
        if (height > 0) return (int) height;
        // display.getSystemFont() is itself null before Device.systemFont has been assigned (a Font
        // built with height 0 that early would otherwise NPE here instead of falling through to 11).
        Font sysFont = display != null ? display.getSystemFont() : null;
        FontData sysFd = sysFont != null ? sysFont.getFontData()[0] : null;
        return (sysFd != null && sysFd.getHeight() > 0) ? sysFd.getHeight() : 11;
    }

    private static int effectiveHeight(FontData fd, Display display) {
        return substituteHeight(fd.getHeight(), display);
    }

    /** Weight for normal (400) and bold (700); matches gen_fonts.dart weightKey and supported 300,400,500,600,700. */
    public static int getWeightFromBold(boolean bold) {
        return bold ? 700 : 400;
    }

    public static String getId(String name, boolean italic, boolean bold) {
        return getId(name, italic, getWeightFromBold(bold));
    }

    /** Key 0-8 to match gen_fonts.dart weightKey (FontWeight.index). 400->3, 700->6. */
    public static String getId(String name, boolean italic, int weight) {
        if (name == null || name.isBlank())
            name = "System";
        name = substituteFontName(name);
        int key = (weight >= 100 && weight <= 900) ? ((weight - 100) / 100) : (weight >= 600 ? 6 : 3);
        return name + "-" + (italic ? 1 : 0) + "-" + key;
    }

    public static String getId(FontData fd) {
        return getId(fd.getName(), isItalic(fd), getWeightFromBold(isBold(fd)));
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
        int height = effectiveHeight(fontDatum, Display.getCurrent());
        return getFontSize(text, FontMetricsUtil.getId(fontDatum), height, 0);
    }

    public static PointD getFontSize(String text, TextStyle textStyle) {
        return getFontSize(text, getId(textStyle.name(), textStyle.italic(), textStyle.weight()), textStyle.size(), textStyle.height());
    }

    public static PointD getFontSize(String text, String fontId, int fontSize){
        return getFontSize(text, fontId, fontSize, 0);
    }

    /**
     * Returns width/height for the given text at the requested size, with wrapping support.
     * If wrapWidth > 0 and text would exceed that width, calculates height for wrapped text.
     *
     * @param text text to measure
     * @param fontId font identifier
     * @param fontSize font size
     * @param wrapWidth maximum width before wrapping (0 = no wrapping)
     * @return PointD(width, height) where height accounts for wrapping if applicable
     */
    public static PointD getFontSize(String text, String fontId, int fontSize, int wrapWidth) {
        return getFontSize(text, fontId, fontSize, 0, wrapWidth);
    }

    /**
     * Returns width/height for the given text and fontId at the requested size (height).
     * Uses per-glyph widths if available; falls back to avgCharWidth when glyph missing.
     * Multi-line text (containing \n) returns the widest line width and total height,
     * matching SWT's GC.textExtent behaviour with DRAW_DELIMITER.
     *
     * @param text font text
     * @param fontId font family name (must exist in GeneratedFontMetrics.DATA)
     * @param fontSize requested size (same units as those stored by generator; e.g. size)
     * @return Point2D.Double(width, height)
     */
    public static PointD getFontSize(String text, String fontId, int fontSize, double fontHeight) {
        return getFontSize(text, fontId, fontSize, fontHeight, 0);
    }

    /**
     * Returns width/height for the given text and fontId at the requested size (height).
     * Uses per-glyph widths if available; falls back to avgCharWidth when glyph missing.
     * Multi-line text (containing \n) returns the widest line width and total height.
     * If wrapWidth > 0 and text exceeds that width, simulates wrapping and returns wrapped height.
     *
     * @param text font text
     * @param fontId font family name (must exist in GeneratedFontMetrics.DATA)
     * @param fontSize requested size
     * @param fontHeight line-height multiplier (0 = use metrics.height())
     * @param wrapWidth max width before wrapping (0 = no wrapping)
     * @return PointD(width, height)
     */
    public static PointD getFontSize(String text, String fontId, int fontSize, double fontHeight, double wrapWidth) {
        if (text.contains("\n") || text.contains("\r")) {
            String[] lines = text.split("\r\n|\r|\n", -1);
            double maxW = 0;
            double totalH = 0;
            double lineH = lineHeight(fontId, fontSize, fontHeight);
            for (String line : lines) {
                double lineWidth = measureLine(line, fontId, fontSize);
                if (wrapWidth > 0 && lineWidth > wrapWidth) {
                    // Text wraps - estimate number of lines needed
                    double avgCharWidth = measureLine("M", fontId, fontSize);
                    if (avgCharWidth > 0) {
                        int estimatedLines = (int) Math.ceil(lineWidth / wrapWidth);
                        totalH += lineH * estimatedLines;
                    } else {
                        totalH += lineH;
                    }
                } else {
                    maxW = Math.max(maxW, lineWidth);
                    totalH += lineH;
                }
            }
            return new PointD(maxW > 0 ? maxW : wrapWidth, totalH);
        }
        double w = measureLine(text, fontId, fontSize);
        double h = lineHeight(fontId, fontSize, fontHeight);
        if (wrapWidth > 0 && w > wrapWidth) {
            // Single line text wraps
            double avgCharWidth = measureLine("M", fontId, fontSize);
            if (avgCharWidth > 0) {
                int estimatedLines = (int) Math.ceil(w / wrapWidth);
                h *= estimatedLines;
            }
            w = wrapWidth;
        }
        return new PointD(w, h);
    }

    private static double lineHeight(String fontId, int fontSize, double fontHeight) {
        Metrics metrics = GenFontMetrics.DATA.get(fontId);
        if (metrics == null) metrics = GenFontMetrics.DATA.get("System-0-3");
        if (metrics == null) return fontSize;
        return fontHeight != 0 ? fontHeight * fontSize : metrics.height() * fontSize;
    }

    private static double measureLine(String text, String fontId, int fontSize) {
        Metrics metrics = GenFontMetrics.DATA.get(fontId);
        if (metrics == null)
            metrics = GenFontMetrics.DATA.get("System-0-3");
        if (metrics == null)
            return 0;

        double scale = (double) fontSize / GenFontMetrics.BASE;

        double w = 0.0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);

            double glyphWidth;
            if (metrics.glyphWidths() != null) {
                int index = cp - GenFontMetrics.GLYPH_START;
                glyphWidth = (index >= 0 && index < metrics.glyphWidths().length)
                        ? metrics.glyphWidths()[index]
                        : metrics.avgCharWidth();
            } else {
                glyphWidth = metrics.avgCharWidth();
            }
            w += glyphWidth * scale;
        }
        return w;
    }

    public static PointD getFontSizeWrapped(String text, TextStyle textStyle, double maxWidth) {
        if (text == null || text.isEmpty()) return PointD.zero;
        if (maxWidth <= 0) return getFontSize(text, textStyle);
        double lineHeight = getFontSize("Ag", textStyle).y();
        if (lineHeight <= 0) return getFontSize(text, textStyle);
        double spaceWidth = getFontSize(" ", textStyle).x();
        double wrapWidth = Math.max(1.0, maxWidth - 1.0);
        String[] paragraphs = text.split("\n", -1);
        int totalLines = 0;
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) { totalLines++; continue; }
            String[] words = paragraph.split(" ", -1);
            double lineWidth = 0.0;
            boolean lineStart = true;
            int linesInParagraph = 1;
            for (String word : words) {
                if (word.isEmpty()) { if (!lineStart) lineWidth += spaceWidth; continue; }
                double wordWidth = getFontSize(word, textStyle).x();
                if (lineStart) { lineWidth = wordWidth; lineStart = false; }
                else {
                    double extended = lineWidth + spaceWidth + wordWidth;
                    if (extended > wrapWidth && wordWidth <= wrapWidth) { linesInParagraph++; lineWidth = wordWidth; }
                    else lineWidth = extended;
                }
            }
            totalLines += linesInParagraph;
        }
        if (totalLines == 0) totalLines = 1;
        return new PointD(maxWidth, lineHeight * totalLines + 2.0);
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
