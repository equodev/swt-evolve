package org.eclipse.swt.custom;

import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.GenFontMetrics;
import dev.equo.swt.Metrics;
import dev.equo.swt.Serializer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.DartFont;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class for DartStyledText to handle Flutter state updates.
 */
public class StyledTextHelper {

    private static final Serializer serializer = new Serializer();
    private static final int FONT_METRICS_BASE = 10;
    private static final int GLYPH_START = 32;

    /**
     * Registers the StateUpdate handler for receiving unified state updates from Flutter.
     */
    public static void registerStateUpdateHandler(DartStyledText styledText) {
        FlutterBridge.onPayload(styledText, "StateUpdate", payload -> {
            styledText.getDisplay().asyncExec(() -> {
                if (payload instanceof String) {
                    processStateUpdate(styledText, (String) payload);
                }
            });
        });
    }

    private static void processStateUpdate(DartStyledText styledText, String payload) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
            @SuppressWarnings("unchecked")
            Map<String, Object> stateUpdate = serializer.from(Map.class, in);

            if (stateUpdate == null) return;

            if (stateUpdate.containsKey("text")) {
                String newText = (String) stateUpdate.get("text");
                if (newText != null) {
                    styledText.setText(newText);
                }
            }

            if (stateUpdate.containsKey("caretOffset")) {
                int caretOffset = ((Number) stateUpdate.get("caretOffset")).intValue();
                styledText.setCaretOffset(caretOffset);
            }

            if (stateUpdate.containsKey("renderer") && styledText.renderer != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rendererData = (Map<String, Object>) stateUpdate.get("renderer");
                if (rendererData != null && rendererData.containsKey("styles")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> styles =
                        (List<Map<String, Object>>) rendererData.get("styles");
                    if (styles != null && !styles.isEmpty()) {
                        DartStyledTextRenderer rendererImpl =
                            (DartStyledTextRenderer) styledText.renderer.getImpl();
                        applyStyleRanges(styledText.getDisplay(), rendererImpl, styles);
                    }
                }
            }
        } catch (IOException ex) {
            // Ignore deserialization errors
        }
    }

    /**
     * Synchronizes the renderer's lineCount with the current content.
     * This ensures the lines array has enough capacity without clearing style ranges.
     */
    public static void syncRendererLineCount(DartStyledTextRenderer renderer) {
        if (renderer == null || renderer.content == null) return;

        int contentLineCount = renderer.content.getLineCount();
        if (contentLineCount > renderer.lineCount) {
            // Resize lineSizes array if needed
            if (renderer.lineSizes == null || renderer.lineSizes.length < contentLineCount) {
                DartStyledTextRenderer.LineSizeInfo[] newLineSizes =
                    new DartStyledTextRenderer.LineSizeInfo[contentLineCount];
                if (renderer.lineSizes != null) {
                    System.arraycopy(renderer.lineSizes, 0, newLineSizes, 0, renderer.lineCount);
                }
                renderer.lineSizes = newLineSizes;
            }
            // Resize lines array if it exists and is too small
            if (renderer.lines != null && renderer.lines.length < contentLineCount) {
                DartStyledTextRenderer.LineInfo[] newLines =
                    new DartStyledTextRenderer.LineInfo[contentLineCount];
                System.arraycopy(renderer.lines, 0, newLines, 0, renderer.lines.length);
                renderer.lines = newLines;
            }
            renderer.lineCount = contentLineCount;
        }
    }

    /**
     * Applies style ranges from Flutter to the renderer.
     */
    public static void applyStyleRanges(Display display, DartStyledTextRenderer renderer,
                                        List<Map<String, Object>> styleRanges) {
        List<Integer> rangePositions = new ArrayList<>();
        List<StyleRange> stylesList = new ArrayList<>();

        for (Map<String, Object> range : styleRanges) {
            try {
                int start = ((Number) range.get("start")).intValue();
                int length = ((Number) range.get("length")).intValue();

                if (start < 0 || length <= 0) continue;

                StyleRange style = new StyleRange();
                style.start = start;
                style.length = length;

                if (range.containsKey("foreground")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> fg = (Map<String, Object>) range.get("foreground");
                    style.foreground = new Color(
                        ((Number) fg.get("red")).intValue(),
                        ((Number) fg.get("green")).intValue(),
                        ((Number) fg.get("blue")).intValue()
                    );
                }

                if (range.containsKey("background")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bg = (Map<String, Object>) range.get("background");
                    style.background = new Color(
                        ((Number) bg.get("red")).intValue(),
                        ((Number) bg.get("green")).intValue(),
                        ((Number) bg.get("blue")).intValue()
                    );
                }

                if (range.containsKey("fontStyle")) {
                    style.fontStyle = ((Number) range.get("fontStyle")).intValue();
                }

                if (range.containsKey("fontSize") || range.containsKey("fontName")) {
                    int fontSize = range.containsKey("fontSize") ?
                        ((Number) range.get("fontSize")).intValue() : 14;
                    String fontName = range.containsKey("fontName") ?
                        (String) range.get("fontName") : "Segoe UI";
                    // Create DartFont directly to avoid SwtFont NPE on logFont.lfHeight
                    DartFont dartFont = new DartFont(display, fontName, fontSize, style.fontStyle, null);
                    style.font = dartFont.getApi();
                }

                if (range.containsKey("underline")) {
                    style.underline = (Boolean) range.get("underline");
                    if (range.containsKey("underlineStyle")) {
                        style.underlineStyle = ((Number) range.get("underlineStyle")).intValue();
                    }
                }

                if (range.containsKey("strikeout")) {
                    style.strikeout = (Boolean) range.get("strikeout");
                }

                rangePositions.add(start);
                rangePositions.add(length);
                stylesList.add(style);

            } catch (Exception e) {
            }
        }

        int[] ranges = rangePositions.stream().mapToInt(i -> i).toArray();
        StyleRange[] styles = stylesList.toArray(new StyleRange[0]);

        // Reset existing styles before applying new ones
        renderer.setStyleRanges(null, null);
        renderer.setStyleRanges(ranges, styles);
    }

    /**
     * Creates a DartFont from FontData array.
     */
    private static Font createDartFont(DartStyledTextRenderer renderer, FontData[] fontDatas) {
        if (fontDatas == null || fontDatas.length == 0) {
            return null;
        }
        DartFont dartFont = new DartFont(renderer.device, fontDatas[0], null);
        return dartFont.getApi();
    }

    /**
     * Gets or creates a font for the specified style.
     */
    public static Font getFont(DartStyledTextRenderer renderer, int style) {
        switch (style) {
            case SWT.BOLD:
                if (renderer.boldFont != null)
                    return renderer.boldFont;
                return renderer.boldFont = createDartFont(renderer, renderer.getFontData(style));
            case SWT.ITALIC:
                if (renderer.italicFont != null)
                    return renderer.italicFont;
                return renderer.italicFont = createDartFont(renderer, renderer.getFontData(style));
            case SWT.BOLD | SWT.ITALIC:
                if (renderer.boldItalicFont != null)
                    return renderer.boldItalicFont;
                return renderer.boldItalicFont = createDartFont(renderer, renderer.getFontData(style));
            default:
                return renderer.regularFont;
        }
    }

    /**
     * Container for font metrics used by the StyledText renderer.
     */
    public static class RendererFontMetrics {
        public final int ascent;
        public final int descent;
        public final int averageCharWidth;
        public final int tabWidth;

        public RendererFontMetrics(int ascent, int descent, int averageCharWidth, int tabWidth) {
            this.ascent = ascent;
            this.descent = descent;
            this.averageCharWidth = averageCharWidth;
            this.tabWidth = tabWidth;
        }

        /**
         * Returns the line height (ascent + descent).
         */
        public int getLineHeight() {
            return ascent + descent;
        }
    }

    /**
     * Calculates font metrics for the given font and tab length.
     * Uses GenFontMetrics data to compute accurate values.
     *
     * @param font the font to calculate metrics for
     * @param tabLength the number of spaces per tab
     * @return RendererFontMetrics with calculated values
     */
    public static RendererFontMetrics calculateFontMetrics(Font font, int tabLength) {
        if (font == null) {
            return new RendererFontMetrics(11, 4, 8, 8 * tabLength);
        }

        FontData[] fontDatas = font.getFontData();
        if (fontDatas == null || fontDatas.length == 0) {
            return new RendererFontMetrics(11, 4, 8, 8 * tabLength);
        }

        FontData fd = fontDatas[0];
        String fontId = FontMetricsUtil.getId(fd);
        int fontSizePoints = fd.getHeight();
        double fontSizePixels = fontSizePoints * (96.0 / 72.0);

        Metrics metrics = GenFontMetrics.DATA.get(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = GenFontMetrics.DATA.get(baseFontId);
        }

        if (metrics == null) {
            int defaultAscent = (int) Math.round(fontSizePixels * 1.1);
            int defaultDescent = (int) Math.round(fontSizePixels * 0.25);
            int defaultAvgCharWidth = (int) Math.round(fontSizePixels * 0.5);
            return new RendererFontMetrics(defaultAscent, defaultDescent, defaultAvgCharWidth,
                    defaultAvgCharWidth * tabLength);
        }

        double scale = fontSizePixels / FONT_METRICS_BASE;

        double exactAscent = metrics.ascent() * fontSizePixels;
        double exactDescent = metrics.descent() * fontSizePixels;
        int lineHeight = (int) Math.round(exactAscent + exactDescent);
        int ascent = (int) Math.round(exactAscent);
        int descent = lineHeight - ascent;
        int averageCharWidth = (int) Math.round(metrics.avgCharWidth() * scale);
        int tabWidth = averageCharWidth * tabLength;

        ascent = Math.max(1, ascent);
        descent = Math.max(1, descent);
        averageCharWidth = Math.max(1, averageCharWidth);
        tabWidth = Math.max(1, tabWidth);

        return new RendererFontMetrics(ascent, descent, averageCharWidth, tabWidth);
    }

    /**
     * Updates the renderer's font metrics based on the given font.
     * This method should be called whenever the font changes.
     *
     * @param renderer the renderer to update
     * @param font the new font
     * @param tabLength the number of spaces per tab
     */
    public static void updateRendererFontMetrics(DartStyledTextRenderer renderer, Font font, int tabLength) {
        RendererFontMetrics metrics = calculateFontMetrics(font, tabLength);
        renderer.ascent = metrics.ascent;
        renderer.descent = metrics.descent;
        renderer.averageCharWidth = metrics.averageCharWidth;
        renderer.tabWidth = metrics.tabWidth;
    }

    /**
     * Computes the width of the given text using the font metrics.
     *
     * @param text the text to measure
     * @param font the font to use for measurement
     * @return the width of the text in pixels
     */
    public static int computeTextWidth(String text, Font font) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        if (font == null) {
            return text.length() * 8;
        }

        FontData[] fontDatas = font.getFontData();
        if (fontDatas == null || fontDatas.length == 0) {
            return text.length() * 8;
        }

        FontData fd = fontDatas[0];
        String fontId = FontMetricsUtil.getId(fd);
        int fontSizePoints = fd.getHeight();
        double fontSizePixels = fontSizePoints * (96.0 / 72.0);

        Metrics metrics = GenFontMetrics.DATA.get(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = GenFontMetrics.DATA.get(baseFontId);
        }

        if (metrics == null) {
            return text.length() * (int) Math.round(fontSizePixels * 0.5);
        }

        double scale = fontSizePixels / FONT_METRICS_BASE;
        double width = 0;

        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);

            double glyphWidth;
            if (metrics.glyphWidths() != null) {
                int index = cp - GLYPH_START;
                if (index >= 0 && index < metrics.glyphWidths().length) {
                    glyphWidth = metrics.glyphWidths()[index];
                } else {
                    glyphWidth = metrics.avgCharWidth();
                }
            } else {
                glyphWidth = metrics.avgCharWidth();
            }
            width += glyphWidth * scale;
        }

        return (int) Math.round(width);
    }

    /**
     * Computes the height of a single line of text using the font metrics.
     *
     * @param font the font to use for measurement
     * @return the line height in pixels
     */
    public static int computeLineHeight(Font font) {
        if (font == null) {
            return 15;
        }

        FontData[] fontDatas = font.getFontData();
        if (fontDatas == null || fontDatas.length == 0) {
            return 15;
        }

        FontData fd = fontDatas[0];
        String fontId = FontMetricsUtil.getId(fd);
        int fontSizePoints = fd.getHeight();
        double fontSizePixels = fontSizePoints * (96.0 / 72.0);

        Metrics metrics = GenFontMetrics.DATA.get(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = GenFontMetrics.DATA.get(baseFontId);
        }

        if (metrics == null) {
            return (int) Math.round(fontSizePixels * 1.35);
        }

        double height = (metrics.ascent() + metrics.descent()) * fontSizePixels;

        return Math.max(1, (int) Math.round(height));
    }

    /**
     * Gets the ascent for the given font.
     *
     * @param font the font
     * @return the ascent in pixels
     */
    public static int getAscent(Font font) {
        if (font == null) {
            return 11;
        }

        FontData[] fontDatas = font.getFontData();
        if (fontDatas == null || fontDatas.length == 0) {
            return 11;
        }

        FontData fd = fontDatas[0];
        String fontId = FontMetricsUtil.getId(fd);
        int fontSizePoints = fd.getHeight();
        double fontSizePixels = fontSizePoints * (96.0 / 72.0);

        Metrics metrics = GenFontMetrics.DATA.get(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = GenFontMetrics.DATA.get(baseFontId);
        }

        if (metrics == null) {
            return (int) Math.round(fontSizePixels * 1.1);
        }

        return Math.max(1, (int) Math.round(metrics.ascent() * fontSizePixels));
    }

    /**
     * Gets the descent for the given font.
     *
     * @param font the font
     * @return the descent in pixels
     */
    public static int getDescent(Font font) {
        if (font == null) {
            return 4;
        }

        FontData[] fontDatas = font.getFontData();
        if (fontDatas == null || fontDatas.length == 0) {
            return 4;
        }

        FontData fd = fontDatas[0];
        String fontId = FontMetricsUtil.getId(fd);
        int fontSizePoints = fd.getHeight();
        double fontSizePixels = fontSizePoints * (96.0 / 72.0);

        Metrics metrics = GenFontMetrics.DATA.get(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = GenFontMetrics.DATA.get(baseFontId);
        }

        if (metrics == null) {
            return (int) Math.round(fontSizePixels * 0.25);
        }

        return Math.max(1, (int) Math.round(metrics.descent() * fontSizePixels));
    }

    /**
     * Gets the average character width for the given font.
     *
     * @param font the font
     * @return the average character width in pixels
     */
    public static int getAverageCharWidth(Font font) {
        if (font == null) {
            return 8;
        }

        FontData[] fontDatas = font.getFontData();
        if (fontDatas == null || fontDatas.length == 0) {
            return 8;
        }

        FontData fd = fontDatas[0];
        String fontId = FontMetricsUtil.getId(fd);
        int fontSizePoints = fd.getHeight();
        double fontSizePixels = fontSizePoints * (96.0 / 72.0);

        Metrics metrics = GenFontMetrics.DATA.get(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = GenFontMetrics.DATA.get(baseFontId);
        }

        if (metrics == null) {
            return (int) Math.round(fontSizePixels * 0.5);
        }

        double scale = fontSizePixels / FONT_METRICS_BASE;
        return Math.max(1, (int) Math.round(metrics.avgCharWidth() * scale));
    }

    /**
     * Calculates the line height at a specific offset, considering styles.
     * This method uses GenFontMetrics instead of native TextLayout metrics.
     *
     * @param renderer the renderer
     * @param content the text content
     * @param offset the character offset
     * @return the line height in pixels
     */
    public static int getLineHeightAtOffset(DartStyledTextRenderer renderer,
                                            StyledTextContent content,
                                            int offset) {
        int lineIndex = content.getLineAtOffset(offset);
        int lineOffset = content.getOffsetAtLine(lineIndex);
        String lineText = content.getLine(lineIndex);
        int lineLength = lineText.length();

        int baseHeight = renderer.ascent + renderer.descent;

        StyleRange[] styles = renderer.getStyleRanges(lineOffset, lineLength, true);
        if (styles == null || styles.length == 0) {
            return baseHeight;
        }

        int maxHeight = baseHeight;
        for (StyleRange style : styles) {
            int styleHeight = baseHeight;

            if (style.font != null) {
                styleHeight = computeLineHeight(style.font);
            }

            if (style.metrics != null) {
                int metricsHeight = style.metrics.ascent + style.metrics.descent;
                styleHeight = Math.max(styleHeight, metricsHeight);
            }

            if (style.rise != 0) {
                styleHeight += Math.abs(style.rise);
            }

            maxHeight = Math.max(maxHeight, styleHeight);
        }

        return maxHeight;
    }

    /**
     * Calculates the line height for a specific line index, considering styles and word wrap.
     *
     * @param renderer the renderer
     * @param content the text content
     * @param lineIndex the line index
     * @param wrapWidth the wrap width (0 if no wrapping)
     * @return the total line height in pixels (including all visual lines if wrapped)
     */
    public static int getLineHeightForLine(DartStyledTextRenderer renderer,
                                           StyledTextContent content,
                                           int lineIndex,
                                           int wrapWidth) {
        int lineOffset = content.getOffsetAtLine(lineIndex);
        String lineText = content.getLine(lineIndex);
        int lineLength = lineText.length();

        int singleLineHeight = getLineHeightAtOffset(renderer, content, lineOffset);

        if (wrapWidth <= 0 || lineLength == 0) {
            return singleLineHeight;
        }

        int textWidth = computeTextWidth(lineText, renderer.regularFont);
        int visualLines = Math.max(1, (textWidth + wrapWidth - 1) / wrapWidth);

        return singleLineHeight * visualLines;
    }

    /**
     * Calculates the X position of a character within a line.
     * Uses font metrics to compute accurate character positions.
     *
     * @param text the line text
     * @param offsetInLine the character offset within the line
     * @param font the font used for rendering
     * @param tabWidth the width of a tab in pixels
     * @return the X position in pixels
     */
    public static int getXAtOffset(String text, int offsetInLine, Font font, int tabWidth) {
        if (text == null || offsetInLine <= 0) {
            return 0;
        }

        offsetInLine = Math.min(offsetInLine, text.length());
        String substring = text.substring(0, offsetInLine);

        int width = 0;
        int tabCount = 0;
        StringBuilder currentSegment = new StringBuilder();

        for (int i = 0; i < substring.length(); i++) {
            char c = substring.charAt(i);
            if (c == '\t') {
                if (currentSegment.length() > 0) {
                    width += computeTextWidth(currentSegment.toString(), font);
                    currentSegment.setLength(0);
                }
                int tabStop = ((width / tabWidth) + 1) * tabWidth;
                width = tabStop;
            } else {
                currentSegment.append(c);
            }
        }

        if (currentSegment.length() > 0) {
            width += computeTextWidth(currentSegment.toString(), font);
        }

        return width;
    }

    /**
     * Calculates the character offset at a given X position within a line.
     *
     * @param text the line text
     * @param x the X position in pixels
     * @param font the font used for rendering
     * @param tabWidth the width of a tab in pixels
     * @return the character offset, and trailing info in trailing[0] if provided
     */
    public static int getOffsetAtX(String text, int x, Font font, int tabWidth, int[] trailing) {
        if (text == null || text.isEmpty() || x <= 0) {
            if (trailing != null && trailing.length > 0) trailing[0] = 0;
            return 0;
        }

        int currentX = 0;
        int lastX = 0;

        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            int charCount = Character.charCount(cp);
            char c = text.charAt(i);

            int charWidth;
            if (c == '\t') {
                int tabStop = ((currentX / tabWidth) + 1) * tabWidth;
                charWidth = tabStop - currentX;
            } else {
                charWidth = computeTextWidth(String.valueOf(Character.toChars(cp)), font);
            }

            if (currentX + charWidth > x) {
                if (trailing != null && trailing.length > 0) {
                    trailing[0] = (x - currentX > charWidth / 2) ? charCount : 0;
                }
                return i;
            }

            lastX = currentX;
            currentX += charWidth;
            i += charCount;
        }

        if (trailing != null && trailing.length > 0) trailing[0] = 0;
        return text.length();
    }

    /**
     * Gets the pixel position (x, y) of a character offset in the StyledText.
     *
     * @param renderer the renderer
     * @param content the text content
     * @param offset the character offset
     * @param leftMargin the left margin
     * @param horizontalScrollOffset the horizontal scroll offset
     * @param linePixelProvider a function to get the Y pixel position of a line
     * @return the Point (x, y) position
     */
    public static org.eclipse.swt.graphics.Point getPointAtOffset(
            DartStyledTextRenderer renderer,
            StyledTextContent content,
            int offset,
            int leftMargin,
            int horizontalScrollOffset,
            java.util.function.IntUnaryOperator linePixelProvider) {

        int contentLength = content.getCharCount();
        offset = Math.max(0, Math.min(offset, contentLength));

        int lineIndex = content.getLineAtOffset(offset);
        int lineOffset = content.getOffsetAtLine(lineIndex);
        int offsetInLine = offset - lineOffset;

        org.eclipse.swt.graphics.TextLayout layout = renderer.getTextLayout(lineIndex);
        org.eclipse.swt.graphics.Point point = layout.getLocation(offsetInLine, false);
        renderer.disposeTextLayout(layout);

        point.x += leftMargin - horizontalScrollOffset;
        point.y += linePixelProvider.applyAsInt(lineIndex);

        return point;
    }

    /**
     * Simplified version of getPointAtOffset that uses the renderer's font metrics
     * when TextLayout is not available.
     *
     * @param content the text content
     * @param offset the character offset
     * @param font the font
     * @param tabWidth the tab width
     * @param leftMargin the left margin
     * @param horizontalScrollOffset the horizontal scroll offset
     * @param topMargin the top margin
     * @param lineHeight the line height
     * @param verticalScrollOffset the vertical scroll offset
     * @return the Point (x, y) position
     */
    public static org.eclipse.swt.graphics.Point getPointAtOffsetSimple(
            StyledTextContent content,
            int offset,
            Font font,
            int tabWidth,
            int leftMargin,
            int horizontalScrollOffset,
            int topMargin,
            int lineHeight,
            int verticalScrollOffset) {

        int contentLength = content.getCharCount();
        offset = Math.max(0, Math.min(offset, contentLength));

        int lineIndex = content.getLineAtOffset(offset);
        int lineOffset = content.getOffsetAtLine(lineIndex);
        int offsetInLine = offset - lineOffset;

        String lineText = content.getLine(lineIndex);
        int x = getXAtOffset(lineText, offsetInLine, font, tabWidth);

        x += leftMargin - horizontalScrollOffset;
        int y = topMargin + (lineIndex * lineHeight) - verticalScrollOffset;

        return new org.eclipse.swt.graphics.Point(x, y);
    }
}
