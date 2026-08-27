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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ControlHelper;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

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

    private static double dpiScale() {
        return FontMetricsUtil.dpiScale();
    }

    private static final Map<DartStyledText, Boolean> vetoedFlutterKeys =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    public static void recordVerifyKeyVerdict(DartStyledText styledText, boolean doit) {
        if (!ControlHelper.isFlutterOriginatedKey())
            return;
        if (doit) {
            vetoedFlutterKeys.remove(styledText);
        } else {
            vetoedFlutterKeys.put(styledText, Boolean.TRUE);
        }
    }

    public static boolean consumeVerifyKeyVeto(DartStyledText styledText) {
        return vetoedFlutterKeys.remove(styledText) != null;
    }

    /**
     * Applies an edit the render side has already displayed, and puts the caret where the
     * document ended up.
     *
     * <p>A {@code VerifyListener} may rewrite an edit rather than merely veto it: JFace runs every
     * {@code IAutoEditStrategy} — auto-indent, bracket auto-close, tabs-to-spaces — inside
     * {@code TextViewer.verifyText} and hands the result back through the event. {@code
     * replaceTextRange} raises that Verify on its own event, so the applied text is not the text
     * passed in and its length cannot be used to advance the caret. Measuring the document instead
     * covers every outcome, including a veto (nothing applied, so the caret does not move) and a
     * listener that rewrites through the Document rather than the event.</p>
     */
    public static void handleModify(DartStyledText styledText, Event e) {
        if (e.text == null || e.start < 0 || e.end < e.start) {
            return;
        }
        int replaced = e.end - e.start;
        int before = styledText.getCharCount();
        styledText.replaceTextRange(e.start, replaced, e.text);
        int applied = styledText.getCharCount() - before + replaced;
        styledText.setCaretOffset(e.start + applied);
    }

    /**
     * Registers the StateUpdate handler for receiving unified state updates from Flutter.
     */
    public static void registerStateUpdateHandler(DartStyledText styledText) {
        FlutterBridge.onPayload(styledText, "StateUpdate", payload -> {
            if (payload == null) return;
            styledText.getDisplay().asyncExec(() ->
                    processStateUpdate(styledText, new String(payload, StandardCharsets.UTF_8)));
        });
    }

    // ---- Text geometry pushed by the render side ----
    //
    // The render side lays the document out with real shaping and pushes the resulting
    // per-visual-line geometry in front of every Paint request, on the same ordered
    // channel — so by the time the SWT.Paint listeners run, the geometry they align
    // against is at least as fresh as the frame they draw over. The position API
    // answers from this table when it is fresh (same character count as the current
    // content) and falls back to the glyph-table estimate otherwise, e.g. before the
    // first frame.

    /** One visual line, after wrapping. Coordinates are relative to the text origin. */
    static final class VisualLine {
        int logicalLine;
        int start, end;          // document offsets, [start, end]
        double x, y, w, h;       // the line's box; h includes vi
        double vi;               // vertical indent within the box: glyphs sit at y + vi
        double[] charX;          // x boundary per character, length end - start + 1, or null
    }

    static final class TextGeometry {
        int charCount;
        double contentWidth, contentHeight;
        VisualLine[] lines;      // in document order, y ascending
    }

    private static final Map<DartStyledText, TextGeometry> textGeometries =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    /** Registers the TextGeometry handler; mirrors {@link #registerStateUpdateHandler}. */
    public static void registerTextGeometryHandler(DartStyledText styledText) {
        FlutterBridge.onPayload(styledText, "TextGeometry", payload -> {
            if (payload == null) return;
            styledText.getDisplay().asyncExec(() ->
                    processTextGeometry(styledText, new String(payload, StandardCharsets.UTF_8)));
        });
    }

    static void processTextGeometry(DartStyledText styledText, String payload) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = serializer.from(Map.class, payload.getBytes(StandardCharsets.UTF_8));
            if (map == null) return;
            applyTextGeometry(styledText, map);
        } catch (IOException ex) {
            // Ignore deserialization errors
        }
    }

    static void applyTextGeometry(DartStyledText styledText, Map<String, Object> map) {
        Object linesObj = map.get("lines");
        if (!(linesObj instanceof List<?> lineMaps)) return;
        TextGeometry g = new TextGeometry();
        g.charCount = asInt(map.get("charCount"), -1);
        g.contentWidth = asDouble(map.get("contentWidth"), 0);
        g.contentHeight = asDouble(map.get("contentHeight"), 0);
        List<VisualLine> lines = new ArrayList<>(lineMaps.size());
        for (Object o : lineMaps) {
            if (!(o instanceof Map<?, ?> lm)) return;
            VisualLine v = new VisualLine();
            v.logicalLine = asInt(lm.get("l"), -1);
            v.start = asInt(lm.get("s"), -1);
            v.end = asInt(lm.get("e"), -1);
            v.x = asDouble(lm.get("x"), 0);
            v.y = asDouble(lm.get("y"), 0);
            v.w = asDouble(lm.get("w"), 0);
            v.h = asDouble(lm.get("h"), 0);
            v.vi = asDouble(lm.get("vi"), 0);
            if (v.logicalLine < 0 || v.start < 0 || v.end < v.start) return;
            if (lm.get("cx") instanceof List<?> cx) {
                if (cx.size() != v.end - v.start + 1) return;
                v.charX = new double[cx.size()];
                for (int i = 0; i < cx.size(); i++) v.charX[i] = asDouble(cx.get(i), 0);
            }
            lines.add(v);
        }
        g.lines = lines.toArray(new VisualLine[0]);
        TextGeometry previous = textGeometries.put(styledText, g);
        // A layout change reaches the render side first and only lands here afterwards: the
        // painters that answer from this table (JFace's ruler columns) have already drawn
        // against the previous one and nothing would ask again. A wrap toggle is the visible
        // case — the text rewraps while the gutter keeps the numbering it computed from the
        // unwrapped table.
        if (describesADifferentLayout(previous, g)) {
            repaintRulerSiblings(styledText);
        }
    }

    /** Whether the newly applied table lays the document out differently from the previous one. */
    static boolean describesADifferentLayout(TextGeometry previous, TextGeometry next) {
        return previous == null
                || previous.lines.length != next.lines.length
                || previous.contentHeight != next.contentHeight
                || previous.contentWidth != next.contentWidth;
    }

    /**
     * Repaints everything beside the StyledText — the ruler columns, which draw through a GC at
     * coordinates this table answers for. The traversal has to go down, not just across: a column
     * that paints line numbers is a child of the CompositeRuler standing beside the StyledText,
     * so painting only the direct siblings leaves the gutter drawn against the previous table.
     */
    private static void repaintRulerSiblings(DartStyledText styledText) {
        // The table is applied through asyncExec, so the editor can already be gone by the time
        // this runs — on a disposed widget every accessor below throws out of the event loop and
        // takes the Display's own teardown with it.
        if (styledText.isDisposed()) return;
        Composite parent = styledText.getParent();
        if (parent == null || parent.isDisposed()) return;
        for (Control child : parent.getChildren()) {
            repaintTree(child);
        }
    }

    private static void repaintTree(Control control) {
        if (control.isDisposed()) return;
        if (control.getImpl() instanceof org.eclipse.swt.widgets.DartControl dc) {
            ControlHelper.paint(dc);
        }
        if (control instanceof Composite composite) {
            for (Control child : composite.getChildren()) {
                repaintTree(child);
            }
        }
    }

    /**
     * Applies a vertical-indent change to the stored geometry in place — the same delta the
     * render side will carry on its next push: the line's first visual row grows by
     * {@code delta} (indent space above its glyphs), and every row below shifts by it.
     * The table's charCount staleness check cannot see this mutation, and callers read
     * positions synchronously right after it, before any new push can arrive.
     */
    public static void applyLineVerticalIndentDelta(DartStyledText styledText, int lineIndex, int delta) {
        if (delta == 0) return;
        TextGeometry g = textGeometries.get(styledText);
        if (g == null) return;
        boolean seen = false;
        for (VisualLine v : g.lines) {
            if (!seen && v.logicalLine == lineIndex) {
                v.vi += delta;
                v.h += delta;
                seen = true;     // only the first visual row of the line carries the indent
            } else if (seen) {
                v.y += delta;    // later rows of the same line, and every line below
            }
        }
        if (seen) g.contentHeight += delta;
    }

    private static int asInt(Object o, int def) {
        return o instanceof Number n ? n.intValue() : def;
    }

    private static double asDouble(Object o, double def) {
        return o instanceof Number n ? n.doubleValue() : def;
    }

    /** The pushed geometry, or null when absent or stale relative to the current content. */
    static TextGeometry freshGeometry(DartStyledText styledText) {
        TextGeometry g = textGeometries.get(styledText);
        if (g == null || g.lines.length == 0) return null;
        if (styledText.content == null || g.charCount != styledText.getCharCount()) return null;
        return g;
    }

    /** Index into g.lines of the visual line holding the offset, preferring the line it starts. */
    private static int visualLineOf(TextGeometry g, int offset) {
        int found = -1;
        for (int i = 0; i < g.lines.length; i++) {
            VisualLine v = g.lines[i];
            if (offset < v.start) break;
            if (offset <= v.end) {
                found = i;
                if (offset < v.end) break;
                // offset == v.end: prefer the next visual line if the offset starts it.
            }
        }
        return found;
    }

    /** Widget-space location of the offset, or null when the table can't answer exactly. */
    public static org.eclipse.swt.graphics.Point geometryPointAtOffset(DartStyledText styledText, int offset) {
        TextGeometry g = freshGeometry(styledText);
        if (g == null) return null;
        int vi = visualLineOf(g, offset);
        if (vi < 0) return null;
        VisualLine v = g.lines[vi];
        if (v.charX == null) return null;
        double x = v.charX[offset - v.start];
        return new org.eclipse.swt.graphics.Point(
                (int) Math.round(x) + styledText.leftMargin - styledText.horizontalScrollOffset,
                (int) Math.round(v.y + v.vi) - styledText.getVerticalScrollOffset() + styledText.topMargin);
    }

    /** Widget-space top pixel of a logical line; line == lineCount answers the content bottom. */
    public static Integer geometryLinePixel(DartStyledText styledText, int lineIndex) {
        TextGeometry g = freshGeometry(styledText);
        if (g == null) return null;
        double y = -1;
        if (lineIndex > g.lines[g.lines.length - 1].logicalLine) {
            y = g.contentHeight;
        } else {
            for (VisualLine v : g.lines) {
                if (v.logicalLine == lineIndex) { y = v.y; break; }
                if (v.logicalLine > lineIndex) break;
            }
        }
        if (y < 0) return null;
        return (int) Math.round(y) - styledText.getVerticalScrollOffset() + styledText.topMargin;
    }

    /** Logical line at a widget-space y, clamped to the content like the estimate path. */
    public static Integer geometryLineIndex(DartStyledText styledText, int y) {
        TextGeometry g = freshGeometry(styledText);
        if (g == null) return null;
        double contentY = y - styledText.topMargin + styledText.getVerticalScrollOffset();
        if (contentY < 0) return 0;
        for (VisualLine v : g.lines) {
            if (contentY < v.y + v.h) return v.logicalLine;
        }
        return g.lines[g.lines.length - 1].logicalLine;
    }

    /**
     * Document offset nearest to a widget-space point (trailing already applied, matching
     * the public getOffsetAtPoint), or null when outside the content or not answerable.
     */
    public static Integer geometryOffsetAtPoint(DartStyledText styledText, int x, int y) {
        TextGeometry g = freshGeometry(styledText);
        if (g == null) return null;
        double contentX = x - styledText.leftMargin + styledText.horizontalScrollOffset;
        double contentY = y - styledText.topMargin + styledText.getVerticalScrollOffset();
        if (contentY < 0 || contentY >= g.contentHeight) return null;
        VisualLine line = null;
        for (VisualLine v : g.lines) {
            if (contentY < v.y + v.h) { line = v; break; }
        }
        if (line == null || line.charX == null) return null;
        int best = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < line.charX.length; i++) {
            double d = Math.abs(line.charX[i] - contentX);
            if (d < bestDist) { bestDist = d; best = i; }
        }
        return line.start + best;
    }

    /** Widget-space bounds of [start, end], or null when the table can't answer exactly. */
    public static org.eclipse.swt.graphics.Rectangle geometryTextBounds(DartStyledText styledText, int start, int end) {
        TextGeometry g = freshGeometry(styledText);
        if (g == null) return null;
        int first = visualLineOf(g, start);
        int last = visualLineOf(g, end);
        if (first < 0 || last < 0) return null;
        double left = Double.MAX_VALUE, right = -Double.MAX_VALUE;
        for (int i = first; i <= last; i++) {
            VisualLine v = g.lines[i];
            if (v.charX == null) return null;
            int from = Math.max(start, v.start) - v.start;
            int to = Math.min(end, v.end) - v.start;
            left = Math.min(left, Math.min(v.charX[from], v.charX[to]));
            right = Math.max(right, Math.max(v.charX[from], v.charX[to]));
        }
        double top = g.lines[first].y + g.lines[first].vi;
        double bottom = g.lines[last].y + g.lines[last].h;
        return new org.eclipse.swt.graphics.Rectangle(
                (int) Math.round(left) + styledText.leftMargin - styledText.horizontalScrollOffset,
                (int) Math.round(top) - styledText.getVerticalScrollOffset() + styledText.topMargin,
                (int) Math.round(right - left),
                (int) Math.round(bottom - top));
    }

    private static void processStateUpdate(DartStyledText styledText, String payload) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> stateUpdate = serializer.from(Map.class, payload.getBytes(StandardCharsets.UTF_8));

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

            if (stateUpdate.containsKey("topPixel")) {
                int topPixel = ((Number) stateUpdate.get("topPixel")).intValue();
                styledText.topPixel = topPixel;
                styledText.verticalScrollOffset = topPixel;
                // Update topIndex so ruler reads correct line number.
                int lineHeight = styledText.getVerticalIncrement();
                if (lineHeight > 0) {
                    styledText.topIndex = topPixel / lineHeight;
                }
                // A render-side scroll must replay the signal a native scroll emits: the vertical
                // scrollbar's Selection event is what JFace's viewport listeners (TextViewer,
                // LineNumberRulerColumn) key their gutter redraw on. Sync the bar first so the
                // widget's own handleVerticalScroll sees a zero delta and stays a no-op; skip
                // entirely when the bar already has this value to avoid re-serializing it.
                org.eclipse.swt.widgets.ScrollBar verticalBar = styledText.getVerticalBar();
                if (verticalBar != null && verticalBar.getSelection() != topPixel) {
                    verticalBar.setSelection(topPixel);
                    verticalBar.notifyListeners(SWT.Selection, new Event());
                }
                repaintRulerSiblings(styledText);
            }

            if (stateUpdate.containsKey("horizontalPixel")) {
                styledText.horizontalScrollOffset = ((Number) stateUpdate.get("horizontalPixel")).intValue();
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
        double fontSizePixels = fontSizePoints * dpiScale();

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
        double fontSizePixels = fontSizePoints * dpiScale();

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
        double fontSizePixels = fontSizePoints * dpiScale();

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
        double fontSizePixels = fontSizePoints * dpiScale();

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
        double fontSizePixels = fontSizePoints * dpiScale();

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
        double fontSizePixels = fontSizePoints * dpiScale();

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

        // Baseline height of the widget's own font. Use computeLineHeight (the same function used for
        // styled fonts below and by TextLayout.getBounds) rather than the cached renderer.ascent/descent,
        // which can be stale relative to the current font — otherwise a style whose font is no larger than
        // the widget font still inflates the line height. Falls back to ascent+descent when no font is set.
        int baseHeight = renderer.regularFont != null
                ? computeLineHeight(renderer.regularFont)
                : renderer.ascent + renderer.descent;

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
