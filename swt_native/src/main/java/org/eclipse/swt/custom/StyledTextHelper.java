package org.eclipse.swt.custom;

import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Metrics;
import dev.equo.swt.Serializer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.DartFont;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
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
 * Helper class for DartStyledText: the render side's scroll reports and text geometry, and font metrics.
 */
public class StyledTextHelper {

    private static final Serializer serializer = new Serializer();
    private static final int FONT_METRICS_BASE = 10;
    private static final int GLYPH_START = 32;

    private static double dpiScale() {
        return FontMetricsUtil.dpiScale();
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
        /** The client's name for this table; a change names the one it was computed from. */
        int version;
        double contentWidth, contentHeight;
        VisualLine[] lines;      // in document order, y ascending
        private int[] firstRows; // per logical line, the index of its first visual line

        /** Index into {@link #lines} of the first visual line of {@code lineIndex}, or -1. */
        int firstRow(int lineIndex) {
            if (firstRows == null) {
                int count = lines.length == 0 ? 0 : lines[lines.length - 1].logicalLine + 1;
                int[] first = new int[count];
                java.util.Arrays.fill(first, -1);
                for (int i = lines.length - 1; i >= 0; i--) first[lines[i].logicalLine] = i;
                firstRows = first;
            }
            return lineIndex >= 0 && lineIndex < firstRows.length ? firstRows[lineIndex] : -1;
        }
    }

    /** Null when the table does not describe that line as the content now holds it. */
    static VisualLine[] rowsOfLine(DartStyledText styledText, int lineIndex) {
        TextGeometry g = freshGeometry(styledText);
        if (g == null || lineIndex < 0 || lineIndex >= styledText.content.getLineCount()) return null;
        int first = g.firstRow(lineIndex);
        if (first < 0) return null;
        int last = first;
        while (last + 1 < g.lines.length && g.lines[last + 1].logicalLine == lineIndex) last++;
        int lineStart = styledText.content.getOffsetAtLine(lineIndex);
        int lineLength = styledText.content.getLine(lineIndex).length();
        if (g.lines[first].start != lineStart || g.lines[last].end != lineStart + lineLength) return null;
        return java.util.Arrays.copyOfRange(g.lines, first, last + 1);
    }

    /** The height of the whole document as the render side laid it out, or null. */
    static Integer measuredContentHeight(DartStyledText styledText) {
        TextGeometry g = freshGeometry(styledText);
        return g == null ? null : (int) Math.round(g.contentHeight);
    }

    /** Where the render side put the top of {@code lineIndex}, in the document's own pixels, or null. */
    static Integer measuredLineTop(DartStyledText styledText, int lineIndex) {
        VisualLine[] rows = rowsOfLine(styledText, lineIndex);
        return rows == null ? null : (int) Math.round(rows[0].y);
    }

    /** The width of the widest row the render side laid out, or null. */
    static Integer measuredContentWidth(DartStyledText styledText) {
        TextGeometry g = freshGeometry(styledText);
        return g == null ? null : (int) Math.ceil(g.contentWidth);
    }

    /** The height the render side gave {@code lineIndex}, below its vertical indent, or null. */
    static Integer measuredLineHeight(DartStyledText styledText, int lineIndex) {
        VisualLine[] rows = rowsOfLine(styledText, lineIndex);
        if (rows == null) return null;
        VisualLine last = rows[rows.length - 1];
        return (int) Math.round(last.y + last.h - rows[0].y - rows[0].vi);
    }

    /** The height of the visual row {@code offsetInLine} falls on, as the render side laid it out. */
    static Integer measuredRowHeight(DartStyledText styledText, int lineIndex, int offsetInLine) {
        if (styledText == null) return null;
        VisualLine[] rows = rowsOfLine(styledText, lineIndex);
        if (rows == null) return null;
        int offset = styledText.content.getOffsetAtLine(lineIndex) + Math.max(0, offsetInLine);
        VisualLine row = rows[0];
        for (VisualLine candidate : rows) {
            if (candidate.start > offset) break;
            row = candidate;
        }
        return (int) Math.round(row.h - (row == rows[0] ? row.vi : 0));
    }

    /**
     * Sent before the edit's state frame, so its caret and selection refer to the spliced text. A
     * client whose replica does not end at {@code charCount} asks for {@code ResendText}.
     */
    public static void sendTextChange(DartStyledText styledText, TextChangingEvent event) {
        String text = event.newText == null ? "" : event.newText;
        java.util.Map<String, Object> change = new java.util.HashMap<>();
        change.put("start", event.start);
        change.put("replaced", event.replaceCharCount);
        change.put("text", text);
        change.put("charCount", styledText.content.getCharCount() - event.replaceCharCount + text.length());
        carriedAcross(styledText, event.start, event.replaceCharCount, text.length());
        FlutterBridge.sendNow(styledText, "TextChange", change);
    }

    /** Answers a client whose text replica drifted: the whole document, once. */
    public static void registerResendTextHandler(DartStyledText styledText) {
        FlutterBridge.onPayload(styledText, "ResendText", payload ->
                styledText.getDisplay().asyncExec(() -> {
                    if (!styledText.isDisposed()) styledText.getValue().markDirty(VStyledText.TEXT);
                }));
    }

    /** One row of the pushed table, or null when the client described it in a way this cannot read. */
    private static VisualLine readVisualLine(Object o) {
        if (!(o instanceof Map<?, ?>)) return null;
        Map<?, ?> lm = (Map<?, ?>) o;
        VisualLine v = new VisualLine();
        v.logicalLine = asInt(lm.get("l"), -1);
        v.start = asInt(lm.get("s"), -1);
        v.end = asInt(lm.get("e"), -1);
        v.x = asDouble(lm.get("x"), 0);
        v.y = asDouble(lm.get("y"), 0);
        v.w = asDouble(lm.get("w"), 0);
        v.h = asDouble(lm.get("h"), 0);
        v.vi = asDouble(lm.get("vi"), 0);
        if (v.logicalLine < 0 || v.start < 0 || v.end < v.start) return null;
        if (lm.get("cx") instanceof List<?>) {
            List<?> cx = (List<?>) lm.get("cx");
            if (cx.size() != v.end - v.start + 1) return null;
            v.charX = new double[cx.size()];
            for (int i = 0; i < cx.size(); i++) v.charX[i] = asDouble(cx.get(i), 0);
        } else if (lm.get("cxu") instanceof List<?>) {
            // A uniform-advance row (monospace) travels as origin and step; multiplying out keeps
            // positions exact, where summing rounded advances would accumulate error.
            List<?> cxu = (List<?>) lm.get("cxu");
            if (cxu.size() != 2) return null;
            double origin = asDouble(cxu.get(0), 0);
            double advance = asDouble(cxu.get(1), 0);
            int count = v.end - v.start + 1;
            if (count < 0) return null;
            v.charX = new double[count];
            for (int i = 0; i < count; i++) v.charX[i] = origin + i * advance;
        }
        return v;
    }

    /**
     * The held table with one run of rows replaced and the rows after it shifted. Null, after asking
     * for the whole table, when the change does not fit what is held.
     */
    private static VisualLine[] splicedLines(DartStyledText styledText, Map<String, Object> map,
                                             int[] changedLines) {
        TextGeometry held = textGeometries.get(styledText);
        Object rowsObj = map.get("rows");
        int rowStart = asInt(map.get("rowStart"), -1);
        int replaced = asInt(map.get("rowsReplaced"), -1);
        if (held == null || held.lines == null || !(rowsObj instanceof List<?>)
                || held.version != asInt(map.get("base"), -1)
                || rowStart < 0 || replaced < 0 || rowStart + replaced > held.lines.length) {
            requestWholeGeometry(styledText);
            return null;
        }
        List<?> rows = (List<?>) rowsObj;
        int lineDelta = asInt(map.get("lineDelta"), 0);
        int offsetDelta = asInt(map.get("offsetDelta"), 0);
        double yDelta = asDouble(map.get("yDelta"), 0);
        VisualLine[] merged = new VisualLine[held.lines.length - replaced + rows.size()];
        System.arraycopy(held.lines, 0, merged, 0, rowStart);
        int at = rowStart;
        for (Object o : rows) {
            VisualLine v = readVisualLine(o);
            if (v == null) {
                requestWholeGeometry(styledText);
                return null;
            }
            if (changedLines[0] < 0 || v.logicalLine < changedLines[0]) changedLines[0] = v.logicalLine;
            if (v.logicalLine > changedLines[1]) changedLines[1] = v.logicalLine;
            merged[at++] = v;
        }
        // Rows that were replaced count as changed even when nothing new took their place.
        for (int i = rowStart; i < rowStart + replaced; i++) {
            int line = held.lines[i].logicalLine;
            if (changedLines[0] < 0 || line < changedLines[0]) changedLines[0] = line;
            if (line > changedLines[1]) changedLines[1] = line;
        }
        for (int i = rowStart + replaced; i < held.lines.length; i++) {
            VisualLine old = held.lines[i];
            VisualLine v = new VisualLine();
            v.logicalLine = old.logicalLine + lineDelta;
            v.start = old.start + offsetDelta;
            v.end = old.end + offsetDelta;
            v.x = old.x;
            v.y = old.y + yDelta;
            v.w = old.w;
            v.h = old.h;
            v.vi = old.vi;
            // The x of each character boundary is where it sits on its row, which the shift
            // does not move.
            v.charX = old.charX;
            merged[at++] = v;
        }
        return merged;
    }

    /** Asks the client for the whole table: what it holds here cannot be changed into the new one. */
    private static void requestWholeGeometry(DartStyledText styledText) {
        textGeometries.remove(styledText);
        FlutterBridge.sendNow(styledText, "ResendGeometry", java.util.Collections.emptyMap());
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
        TextGeometry g = new TextGeometry();
        g.charCount = asInt(map.get("charCount"), -1);
        g.contentWidth = asDouble(map.get("contentWidth"), 0);
        g.contentHeight = asDouble(map.get("contentHeight"), 0);
        g.version = asInt(map.get("v"), 0);
        // The logical lines this push changed, or null when it describes the whole layout.
        int[] changedLines = null;
        Object linesObj = map.get("lines");
        if (linesObj instanceof List<?>) {
            List<?> lineMaps = (List<?>) linesObj;
            List<VisualLine> lines = new ArrayList<>(lineMaps.size());
            for (Object o : lineMaps) {
                VisualLine v = readVisualLine(o);
                if (v == null) return;
                lines.add(v);
            }
            g.lines = lines.toArray(new VisualLine[0]);
        } else {
            changedLines = new int[] { -1, -1 };
            VisualLine[] spliced = splicedLines(styledText, map, changedLines);
            if (spliced == null) return;
            g.lines = spliced;
        }
        TextGeometry previous = textGeometries.put(styledText, g);
        // A layout change reaches the render side first and only lands here afterwards: the
        // painters that answer from this table (JFace's ruler columns) have already drawn
        // against the previous one and nothing would ask again. A wrap toggle is the visible
        // case — the text rewraps while the gutter keeps the numbering it computed from the
        // unwrapped table.
        if (describesADifferentLayout(previous, g)) {
            // A change naming its rows invalidates only those lines; a whole table, the document.
            // Not resetCache(): it marks half the widget dirty, a round trip on every keystroke.
            if (!styledText.isDisposed() && styledText.renderer != null && styledText.content != null) {
                int lineCount = styledText.content.getLineCount();
                int from = changedLines == null || changedLines[0] < 0 ? 0 : Math.min(changedLines[0], lineCount - 1);
                int count = changedLines == null || changedLines[0] < 0
                        ? lineCount
                        : Math.min(changedLines[1] - changedLines[0] + 1, lineCount - from);
                ((DartStyledTextRenderer) styledText.renderer.getImpl()).reset(from, Math.max(1, count));
                styledText.setScrollBars(true);
            }
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
        if (control.isListening(SWT.Paint)
                && control.getImpl() instanceof org.eclipse.swt.widgets.DartControl) {
            ControlHelper.paint((org.eclipse.swt.widgets.DartControl) control.getImpl());
        }
        if (control instanceof Composite) { Composite composite = (Composite) control;
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
        return o instanceof Number ? ((Number) o).intValue() : def;
    }

    private static double asDouble(Object o, double def) {
        return o instanceof Number ? ((Number) o).doubleValue() : def;
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
        boolean lastRowOfLine = true;
        for (int i = 0; i < g.lines.length; i++) {
            if (contentY < g.lines[i].y + g.lines[i].h) {
                line = g.lines[i];
                lastRowOfLine = i + 1 >= g.lines.length || g.lines[i + 1].logicalLine != line.logicalLine;
                break;
            }
        }
        if (line == null || line.charX == null) return null;
        // Beside the text there is no offset (-1), except past a wrapped row, which continues below.
        if (contentX > line.x + line.w) return lastRowOfLine ? -1 : line.end;
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

    // ---- What the renderer will actually paint ----
    //
    // LineStyle/LineBackground listener answers are resolved once per push, since the client draws.

    // ---- Banding ----
    //
    // Only styles around the viewport travel, quantised so a scroll inside the band sends the same
    // payload. ControlHelper#markListenerProvidedStyles must re-push a banded renderer on scroll.

    /** Whether runs drawn alike share one palette entry. Off gives each run its own. */
    private static final boolean SHARE_STYLES =
            !"false".equals(System.getProperty("dev.equo.swt.styles.palette"));

    /** Lines above and below the viewport that travel with it, as multiples of a screen. */
    private static final int BAND_MARGIN_SCREENS = 2;

    /** Bands start on a multiple of this, so a scroll inside one changes nothing. */
    private static final int BAND_QUANTUM_LINES = 100;

    /** A document at or under this many lines travels whole: banding saves less than it costs. */
    private static final int BAND_MIN_LINES = 300;

    /** The viewport is counted in steps of this, so resizing inside one changes nothing. */
    private static final int BAND_VISIBLE_QUANTUM_LINES = 25;

    /** The styles that travel, with their positions - filtered together so they cannot disagree. */
    private static final class Banded {

        /** The distinct styles, each said once. */
        final StyleRange[] styles;

        /** Positions for the runs, or null when each range carries its own. */
        final int[] ranges;

        /** Which of {@link #styles} each run is drawn in. */
        final int[] index;

        Banded(StyleRange[] styles, int[] ranges, int[] index) {
            this.styles = styles;
            this.ranges = ranges;
            this.index = index;
        }

        /** A band's runs as a palette of distinct styles and a run of names into it. */
        static Banded of(List<StyleRange> runs, int[] ranges) {
            java.util.List<StyleRange> entries = new ArrayList<>();
            java.util.Map<StyleRange, Integer> palette = new java.util.HashMap<>();
            // Name, start and length per run, the palette's own name first: one array, so names and
            // positions cannot be delivered apart.
            int[] index = new int[1 + 3 * runs.size()];
            for (int i = 0; i < runs.size(); i++) {
                StyleRange run = runs.get(i);
                // -Ddev.equo.swt.styles.palette=false gives every run its own entry, to bisect sharing.
                StyleRange entry = placeless(run);
                Integer shared = SHARE_STYLES ? palette.get(entry) : null;
                if (shared == null) {
                    shared = entries.size();
                    entries.add(entry);
                    if (SHARE_STYLES) palette.put(entry, shared);
                }
                index[1 + 3 * i] = shared;
                index[2 + 3 * i] = ranges != null && (i << 1) + 1 < ranges.length
                        ? ranges[i << 1]
                        : run.start;
                index[3 + 3 * i] = ranges != null && (i << 1) + 1 < ranges.length
                        ? ranges[(i << 1) + 1]
                        : run.length;
            }
            StyleRange[] styles = entries.toArray(new StyleRange[0]);
            // Naming the palette in the index makes the index change whenever the palette does, so
            // a changed palette is never delivered without its names.
            int name = paletteName(styles);
            index[0] = name == STYLE_INDEX_EDIT ? name + 1 : name;
            return new Banded(styles, ranges, index);
        }

        /** {@code run} at no place, so that two runs drawn alike are one entry. */
        private static StyleRange placeless(StyleRange run) {
            if (run.start == 0 && run.length == 0) return run;
            StyleRange copy = (StyleRange) run.clone();
            copy.start = 0;
            copy.length = 0;
            return copy;
        }

        /** Built from the travelling fields: {@link StyleRange#hashCode()} ignores colour. */
        private static int paletteName(StyleRange[] styles) {
            int name = 1;
            for (StyleRange style : styles) {
                name = 31 * name + colourName(style.foreground);
                name = 31 * name + colourName(style.background);
                name = 31 * name + colourName(style.underlineColor);
                name = 31 * name + colourName(style.strikeoutColor);
                name = 31 * name + colourName(style.borderColor);
                name = 31 * name + style.fontStyle;
                name = 31 * name + style.underlineStyle;
                name = 31 * name + style.borderStyle;
                name = 31 * name + style.rise;
                name = 31 * name + (style.underline ? 1 : 0);
                name = 31 * name + (style.strikeout ? 1 : 0);
                name = 31 * name + fontName(style.font);
                name = 31 * name + (style.metrics == null ? 0 : style.metrics.hashCode());
            }
            return name;
        }

        private static int colourName(org.eclipse.swt.graphics.Color colour) {
            if (colour == null || colour.isDisposed()) return 0;
            return (colour.getAlpha() << 24) | (colour.getRed() << 16)
                    | (colour.getGreen() << 8) | colour.getBlue();
        }

        private static int fontName(org.eclipse.swt.graphics.Font font) {
            if (font == null || font.isDisposed()) return 0;
            org.eclipse.swt.graphics.FontData[] data = font.getFontData();
            return data == null || data.length == 0 ? 0 : data[0].toString().hashCode();
        }
    }


    /**
     * Drops a foreground equal to the widget's, which the client paints anyway. Cloned: these are
     * the application's own StyleRanges, read back through {@code getStyleRanges()}.
     */
    private static StyleRange withoutRedundantForeground(StyleRange style, Color widgetForeground) {
        if (widgetForeground == null || style == null || style.foreground == null) return style;
        if (!widgetForeground.equals(style.foreground)) return style;
        StyleRange copy = (StyleRange) style.clone();
        copy.foreground = null;
        return copy;
    }

    /** The colour a range needs no foreground to be painted in, or null when there is none. */
    private static Color widgetForeground(DartStyledTextRenderer renderer) {
        DartStyledText styledText = widgetOf(renderer);
        if (styledText == null) return null;
        try {
            return styledText.getApi().getForeground();
        } catch (RuntimeException disposedOrUnset) {
            return null;
        }
    }

    /** The line range whose styles travel, or null when the whole document does. */
    private static int[] styleBand(DartStyledTextRenderer renderer) {
        DartStyledText styledText = widgetOf(renderer);
        StyledTextContent content = renderer.content;
        if (styledText == null || content == null) return null;
        int lineCount = content.getLineCount();
        if (lineCount <= BAND_MIN_LINES) return null;
        int lineHeight = Math.max(1, styledText.getLineHeight());
        int height = styledText.getClientArea().height;
        if (height <= 0) return null;
        // Quantised, so a sash drag does not rebuild the band per pixel; rounded up to cover it.
        int exactVisible = Math.max(1, height / lineHeight);
        int visible = ((exactVisible + BAND_VISIBLE_QUANTUM_LINES - 1)
                / BAND_VISIBLE_QUANTUM_LINES) * BAND_VISIBLE_QUANTUM_LINES;
        int margin = visible * BAND_MARGIN_SCREENS;
        int quantum = Math.max(BAND_QUANTUM_LINES, visible);
        int top = Math.max(0, styledText.getTopIndex());
        // Keep the band while the viewport is at least half a margin inside it. A line-count change
        // keeps it too: only its end is clamped to the document, or follows it if it reached it.
        int[] held = heldBands.get(renderer);
        if (held != null && held[HELD_VISIBLE] == visible) {
            boolean reachedEnd = held[1] >= held[HELD_LINES] - 1;
            int heldTo = reachedEnd ? lineCount - 1 : Math.min(held[1], lineCount - 1);
            if (held[0] <= heldTo && top >= held[0] + margin / 2
                    && (top + visible <= heldTo - margin / 2 || heldTo >= lineCount - 1)) {
                if (held[0] == 0 && heldTo >= lineCount - 1) {
                    heldBands.remove(renderer);
                    return null;
                }
                if (heldTo != held[1] || held[HELD_LINES] != lineCount) {
                    heldBands.put(renderer, new int[] { held[0], heldTo, lineCount, visible });
                }
                return new int[] { held[0], heldTo };
            }
        }
        int from = Math.max(0, Math.floorDiv(top - margin, quantum) * quantum);
        int to = Math.min(lineCount - 1, from + quantum + 2 * margin);
        if (from == 0 && to >= lineCount - 1) {
            heldBands.remove(renderer);
            return null;
        }
        heldBands.put(renderer, new int[] { from, to, lineCount, visible });
        return new int[] { from, to };
    }

    /** Per renderer: the band being sent, and the line count and viewport height it was built for. */
    private static final Map<DartStyledTextRenderer, int[]> heldBands =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    private static final int HELD_LINES = 2;
    private static final int HELD_VISIBLE = 3;

    /** Whether this renderer sends part of its styles, so a scroll has to re-push them. */
    public static boolean stylesAreBanded(DartStyledTextRenderer renderer) {
        return styleBand(renderer) != null;
    }

    /** As {@link #stylesAreBanded(DartStyledTextRenderer)}, from outside this package. */
    public static boolean stylesAreBanded(StyledText text) {
        if (text == null || text.isDisposed() || !(text.getImpl() instanceof DartStyledText))
            return false;
        StyledTextRenderer renderer = ((DartStyledText) text.getImpl()).renderer;
        return renderer != null && renderer.getImpl() instanceof DartStyledTextRenderer
                && stylesAreBanded((DartStyledTextRenderer) renderer.getImpl());
    }

    /** The style ranges to paint: the renderer's own, or what a LineStyleListener answers. */
    public static StyleRange[] wireStyles(DartStyledTextRenderer renderer) {
        return banded(renderer).styles;
    }

    /** Test seam: the palette a band of {@code runs} travels as. */
    static StyleRange[] bandPaletteForTesting(List<StyleRange> runs) {
        return Banded.of(runs, null).styles;
    }

    /** Test seam: the names those runs travel as, the palette's own name first. */
    static int[] bandNamesForTesting(List<StyleRange> runs) {
        return Banded.of(runs, null).index;
    }

    /** Which distinct style each run of the band is drawn in, parallel to its positions. */
    public static int[] wireStyleIndex(DartStyledTextRenderer renderer) {
        return banded(renderer).index;
    }

    /**
     * First element of a {@link #styleIndexForWire} that is a change rather than a whole index. A
     * whole index starts with its palette's name, which {@code paletteName} never makes this.
     */
    static final int STYLE_INDEX_EDIT = Integer.MIN_VALUE;

    /** Stamps each change sent, so two alike are never taken for a repeat and suppressed. */
    private static final java.util.concurrent.atomic.AtomicInteger editSerial =
            new java.util.concurrent.atomic.AtomicInteger();

    /** Per widget and client: the index it holds and the edits it has carried it across since. */
    private static final Map<DartStyledText, Map<Integer, HeldIndex>> heldIndexes =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    private static final class HeldIndex {
        int[] index;
        final List<int[]> edits = new ArrayList<>();
    }

    /**
     * Whole, or to a client known to hold an index, the change {@code [STYLE_INDEX_EDIT, checksum,
     * serial, paletteName, from, removed, inserted...]}; the serial keeps it from being suppressed.
     */
    public static int[] styleIndexForWire(DartStyledText text) {
        int[] whole = wireStyleIndex(text);
        int connection = Serializer.targetConnection();
        if (whole == null || connection == 0) return whole;
        Serializer.whenDelivered(delivered -> held(text, delivered, whole));
        if (!Serializer.writingUpdateOf(text.getValue())) return whole;
        Map<Integer, HeldIndex> perClient = heldIndexes.get(text);
        HeldIndex held = perClient == null ? null : perClient.get(connection);
        if (held == null || held.index == null) return whole;
        int[] base = held.index;
        for (int[] edit : held.edits) base = spliceStyleIndex(base, edit[0], edit[1], edit[2]);
        return styleIndexChange(base, whole);
    }

    /** Records that {@code connection} now holds {@code index}, with nothing carried since. */
    private static void held(DartStyledText text, int connection, int[] index) {
        HeldIndex held = heldIndexes.computeIfAbsent(text,
                t -> java.util.Collections.synchronizedMap(new java.util.HashMap<>()))
                .computeIfAbsent(connection, c -> new HeldIndex());
        synchronized (held) {
            held.index = index;
            held.edits.clear();
        }
    }

    /** Notes an edit every client holding an index will carry that index across. */
    private static void carriedAcross(DartStyledText text, int start, int replaced, int inserted) {
        Map<Integer, HeldIndex> perClient = heldIndexes.get(text);
        if (perClient == null) return;
        synchronized (perClient) {
            for (HeldIndex held : perClient.values()) {
                synchronized (held) {
                    if (held.index != null) held.edits.add(new int[] { start, replaced, inserted });
                }
            }
        }
    }

    /** The change turning {@code base} into {@code target}. */
    static int[] styleIndexChange(int[] base, int[] target) {
        int prefix = 1;
        int limit = Math.min(base.length, target.length);
        while (prefix < limit && base[prefix] == target[prefix]) prefix++;
        int suffix = 0;
        while (suffix < limit - prefix
                && base[base.length - 1 - suffix] == target[target.length - 1 - suffix]) {
            suffix++;
        }
        int removed = base.length - prefix - suffix;
        int inserted = target.length - prefix - suffix;
        int[] change = new int[6 + inserted];
        change[0] = STYLE_INDEX_EDIT;
        change[1] = styleIndexChecksum(target);
        // A no-op change is always identical, so suppressing it as a repeat is safe.
        change[2] = removed == 0 && inserted == 0 ? 0 : editSerial.incrementAndGet() & 0x3FFFFFFF;
        change[3] = target[0];
        change[4] = prefix;
        change[5] = removed;
        System.arraycopy(target, prefix, change, 6, inserted);
        return change;
    }

    /**
     * A number both sides compute alike over an index: kept below 2^30 so every intermediate is an
     * exact integer in a JavaScript double as well.
     */
    static int styleIndexChecksum(int[] index) {
        long hash = index.length;
        for (int value : index) hash = (hash * 31 + value) & 0x3FFFFFFF;
        return (int) hash;
    }

    /**
     * The index after {@code replaced} characters at {@code start} became {@code inserted}, as the
     * client carries it ({@code spliceStyleIndex} there): each run's start moves as a caret would,
     * its end stays put at the edit, and a run the edit removed entirely is dropped.
     */
    static int[] spliceStyleIndex(int[] index, int start, int replaced, int inserted) {
        if (index.length < 4) return index;
        int[] out = new int[index.length];
        int n = 0;
        out[n++] = index[0];
        for (int i = 1; i + 2 < index.length; i += 3) {
            int from = spliceOffset(index[i + 1], start, replaced, inserted);
            int to = spliceEnd(index[i + 1] + index[i + 2], start, replaced, inserted);
            if (to > from) {
                out[n++] = index[i];
                out[n++] = from;
                out[n++] = to - from;
            }
        }
        return n == out.length ? out : java.util.Arrays.copyOf(out, n);
    }

    private static int spliceOffset(int offset, int start, int replaced, int inserted) {
        if (offset >= start + replaced) return offset + inserted - replaced;
        if (offset > start) return start + inserted;
        return offset;
    }

    private static int spliceEnd(int end, int start, int replaced, int inserted) {
        if (end > start + replaced) return end + inserted - replaced;
        if (end > start) return start;
        return end;
    }

    /** The same, for the widget whose value carries it. */
    public static int[] wireStyleIndex(DartStyledText text) {
        if (text == null) return null;
        StyledTextRenderer renderer = text.renderer;
        if (renderer == null || !(renderer.getImpl() instanceof DartStyledTextRenderer)) return null;
        return wireStyleIndex((DartStyledTextRenderer) renderer.getImpl());
    }

    public static StyleRange[] wireStyles(DartStyledText text) {
        if (text == null) return null;
        StyledTextRenderer renderer = text.renderer;
        if (renderer == null || !(renderer.getImpl() instanceof DartStyledTextRenderer)) return null;
        return wireStyles((DartStyledTextRenderer) renderer.getImpl());
    }

    private static Banded banded(DartStyledTextRenderer renderer) {
        int[] band = styleBand(renderer);
        List<StyleRange> listener = listenerStyles(renderer, band);
        if (listener != null) return Banded.of(listener, null);
        BandedMemo memo = bandedMemos.get(renderer);
        Color foreground = widgetForeground(renderer);
        if (memo != null && memo.describes(renderer, band, foreground)) return memo.banded;
        Banded banded = ownStylesBanded(renderer, band);
        bandedMemos.put(renderer, new BandedMemo(renderer, band, foreground, banded));
        return banded;
    }

    /**
     * The band last built for a renderer and its exact inputs, compared rather than hashed: a stale
     * band draws the wrong styles.
     */
    private static final class BandedMemo {
        final StyleRange[] styles;
        final int[] ints;
        final Color foreground;
        final Banded banded;

        BandedMemo(DartStyledTextRenderer renderer, int[] band, Color foreground, Banded banded) {
            this.styles = renderer.styles == null ? null : renderer.styles.clone();
            this.ints = inputsOf(renderer, band);
            this.foreground = foreground;
            this.banded = banded;
        }

        boolean describes(DartStyledTextRenderer renderer, int[] band, Color foreground) {
            StyleRange[] current = renderer.styles;
            if ((current == null) != (styles == null)) return false;
            if (current != null) {
                if (current.length != styles.length) return false;
                for (int i = 0; i < current.length; i++) {
                    if (current[i] != styles[i]) return false;
                }
            }
            return java.util.Objects.equals(foreground, this.foreground)
                    && java.util.Arrays.equals(inputsOf(renderer, band), ints);
        }

        /** The band, the count in use, every range and every style's own position. */
        private static int[] inputsOf(DartStyledTextRenderer renderer, int[] band) {
            StyleRange[] styles = renderer.styles;
            int[] ranges = renderer.ranges;
            int count = styles == null ? 0 : ranges != null ? renderer.styleCount : styles.length;
            int rangeInts = ranges == null ? 0 : Math.min(ranges.length, 2 * count);
            int[] out = new int[5 + rangeInts + 2 * (styles == null ? 0 : styles.length)];
            int k = 0;
            out[k++] = band == null ? -1 : band[0];
            out[k++] = band == null ? -1 : band[1];
            out[k++] = count;
            out[k++] = ranges == null ? -1 : ranges.length;
            out[k++] = rangeInts;
            for (int i = 0; i < rangeInts; i++) out[k++] = ranges[i];
            if (styles != null) {
                for (StyleRange style : styles) {
                    out[k++] = style == null ? -1 : style.start;
                    out[k++] = style == null ? -1 : style.length;
                }
            }
            return out;
        }
    }

    private static final Map<DartStyledTextRenderer, BandedMemo> bandedMemos =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    /** The renderer's own styles and positions, keeping only what the band covers. */
    private static Banded ownStylesBanded(DartStyledTextRenderer renderer, int[] band) {
        StyleRange[] styles = renderer.styles;
        if (styles == null) return new Banded(null, null, null);
        int[] ranges = renderer.ranges;
        int count = ranges != null ? renderer.styleCount : styles.length;
        int[] span = bandOffsets(renderer, band);
        Color foreground = widgetForeground(renderer);
        List<StyleRange> keptStyles = new ArrayList<>(Math.min(styles.length, count));
        List<Integer> keptRanges = ranges != null ? new ArrayList<>() : null;
        for (int i = 0; i < styles.length && i < count; i++) {
            if (styles[i] == null) continue;
            int start, length;
            if (ranges != null && (i << 1) + 1 < ranges.length) {
                start = ranges[i << 1];
                length = ranges[(i << 1) + 1];
            } else {
                start = styles[i].start;
                length = styles[i].length;
            }
            if (span != null && (start + length <= span[0] || start >= span[1])) continue;
            keptStyles.add(withoutRedundantForeground(styles[i], foreground));
            if (keptRanges != null) {
                keptRanges.add(start);
                keptRanges.add(length);
            }
        }
        int[] outRanges = null;
        if (keptRanges != null) {
            outRanges = new int[keptRanges.size()];
            for (int i = 0; i < outRanges.length; i++) outRanges[i] = keptRanges.get(i);
        }
        return Banded.of(keptStyles, outRanges);
    }

    /** The character span [start, end) the band covers, or null when it covers everything. */
    private static int[] bandOffsets(DartStyledTextRenderer renderer, int[] band) {
        if (band == null) return null;
        StyledTextContent content = renderer.content;
        if (content == null) return null;
        int lineCount = content.getLineCount();
        int last = Math.min(band[1], lineCount - 1);
        if (band[0] > last) return null;
        int start = content.getOffsetAtLine(band[0]);
        int end = content.getOffsetAtLine(last) + content.getLine(last).length();
        return new int[] { start, end };
    }

    /** Always null: positions travel in {@link #wireStyleIndex}. */
    public static int[] wireRanges(DartStyledTextRenderer renderer) {
        return null;
    }

    /** How many of {@link #wireStyles} are valid. */
    public static int wireStyleCount(DartStyledTextRenderer renderer) {
        StyleRange[] styles = wireStyles(renderer);
        return styles == null ? 0 : styles.length;
    }

    /** The renderer's own styles, trimmed to the count that is in use. */
    private static StyleRange[] ownStyles(DartStyledTextRenderer renderer) {
        StyleRange[] styles = renderer.styles;
        if (styles == null)
            return null;
        List<StyleRange> result = new ArrayList<>(styles.length);
        int count = renderer.ranges != null ? renderer.styleCount : styles.length;
        for (int i = 0; i < styles.length && i < count; i++) {
            if (styles[i] != null)
                result.add(styles[i]);
        }
        return result.toArray(new StyleRange[0]);
    }

    /** Every line's styles as a LineStyleListener answers them, or null when there is no listener. */
    private static List<StyleRange> listenerStyles(DartStyledTextRenderer renderer, int[] band) {
        DartStyledText styledText = widgetOf(renderer);
        if (styledText == null || !styledText.getApi().isListening(ST.LineGetStyle))
            return null;
        List<StyleRange> all = new ArrayList<>();
        StyledTextContent content = renderer.content;
        // Only the band: the application's listener is not asked about lines nobody can see.
        Color foreground = widgetForeground(renderer);
        int first = band == null ? 0 : Math.max(0, band[0]);
        int last = band == null ? content.getLineCount() - 1
                : Math.min(band[1], content.getLineCount() - 1);
        for (int line = first; line <= last; line++) {
            int lineOffset = content.getOffsetAtLine(line);
            StyledTextEvent event = styledText.getLineStyleData(lineOffset, content.getLine(line));
            if (event == null || event.styles == null)
                continue;
            for (int i = 0; i < event.styles.length; i++) {
                StyleRange style = event.styles[i];
                if (style == null)
                    continue;
                if (event.ranges != null && (i << 1) + 1 < event.ranges.length) {
                    style = (StyleRange) style.clone();
                    style.start = event.ranges[i << 1];
                    style.length = event.ranges[(i << 1) + 1];
                }
                all.add(withoutRedundantForeground(style, foreground));
            }
        }
        return all;
    }

    /** The line attributes to paint, with a LineBackgroundListener's answer filled in. */
    public static VStyledTextRenderer.VLineInfo[] wireLines(DartStyledTextRenderer renderer) {
        DartStyledTextRenderer.LineInfo[] lines = renderer.lines;
        DartStyledText styledText = widgetOf(renderer);
        boolean hasListener = styledText != null && styledText.getApi().isListening(ST.LineGetBackground);
        if (!hasListener && renderer.bullets == null) {
            if (lines == null)
                return null;
            VStyledTextRenderer.VLineInfo[] result = new VStyledTextRenderer.VLineInfo[lines.length];
            for (int i = 0; i < lines.length; i++) {
                if (lines[i] != null)
                    result[i] = new VStyledTextRenderer.VLineInfo(lines[i]);
            }
            return result;
        }
        int count = renderer.content.getLineCount();
        VStyledTextRenderer.VLineInfo[] result = new VStyledTextRenderer.VLineInfo[count];
        int askFrom = 0, askTo = hasListener ? count - 1 : -1;
        if (hasListener) {
            // Ask only about lines that can be on screen before the next repaint; the rest keep the
            // renderer's background.
            int visible = Math.max(1, styledText.getPartialBottomIndex() - styledText.topIndex + 1);
            askFrom = Math.max(0, styledText.topIndex - visible);
            askTo = Math.min(count - 1, styledText.getPartialBottomIndex() + visible);
        }
        for (int i = 0; i < count; i++) {
            DartStyledTextRenderer.LineInfo info = lines != null && i < lines.length && lines[i] != null
                    ? lines[i]
                    : new DartStyledTextRenderer.LineInfo();
            StyledTextEvent event = (i >= askFrom && i <= askTo) ? styledText.getLineBackgroundData(
                    renderer.content.getOffsetAtLine(i), renderer.content.getLine(i)) : null;
            Color background = event != null && event.lineBackground != null
                    ? event.lineBackground
                    : info.background;
            Object[] bullet = bulletOf(renderer, i);
            int bulletIndent = bullet == null ? 0 : bulletWidth((Bullet) bullet[0]);
            if (background != info.background || bulletIndent != 0) {
                DartStyledTextRenderer.LineInfo resolved = new DartStyledTextRenderer.LineInfo(info);
                resolved.background = background;
                // A bullet reserves its glyph width before the text, the way the renderer adds it
                // to the layout's indent when it draws the line itself.
                resolved.indent += bulletIndent;
                if (bulletIndent != 0) resolved.flags |= DartStyledTextRenderer.INDENT;
                info = resolved;
            }
            result[i] = new VStyledTextRenderer.VLineInfo(info);
        }
        return result;
    }

    /**
     * The bullet on {@code lineIndex} and the index it counts from, or null -- the same lookup the
     * renderer does when it draws one.
     */
    private static Object[] bulletOf(DartStyledTextRenderer renderer, int lineIndex) {
        if (renderer.bullets == null) return null;
        if (renderer.bulletsIndices != null) {
            int index = lineIndex - renderer.topIndex;
            if (index < 0 || index >= renderer.bulletsIndices.length || index >= renderer.bullets.length)
                return null;
            Bullet bullet = renderer.bullets[index];
            return bullet == null ? null : new Object[] { bullet, renderer.bulletsIndices[index] };
        }
        for (Bullet bullet : renderer.bullets) {
            int bulletIndex = bullet == null ? -1 : bullet.indexOf(lineIndex);
            if (bulletIndex != -1) return new Object[] { bullet, bulletIndex };
        }
        return null;
    }

    /** The width a bullet reserves before its line's text, from its style's glyph metrics. */
    private static int bulletWidth(Bullet bullet) {
        return bullet.style == null || bullet.style.metrics == null ? 0 : bullet.style.metrics.width;
    }

    /**
     * What the renderer would draw for each line's bullet, or null when no line has one. The text
     * is the client's to paint; the width it reserves is already in the line's indent.
     */
    public static String[] wireBulletTexts(DartStyledTextRenderer renderer) {
        if (renderer.bullets == null || renderer.content == null) return null;
        String[] texts = null;
        int count = renderer.content.getLineCount();
        for (int i = 0; i < count; i++) {
            Object[] found = bulletOf(renderer, i);
            if (found == null) continue;
            if (texts == null) texts = new String[count];
            texts[i] = bulletText((Bullet) found[0], (Integer) found[1]);
        }
        return texts;
    }

    /** {@code DartStyledTextRenderer#drawBullet}'s string, for the client to paint. */
    private static String bulletText(Bullet bullet, int index) {
        String string = "";
        switch (bullet.type & (ST.BULLET_DOT | ST.BULLET_NUMBER | ST.BULLET_LETTER_LOWER | ST.BULLET_LETTER_UPPER)) {
            case ST.BULLET_DOT:
                string = "\u2022";
                break;
            case ST.BULLET_NUMBER:
                string = String.valueOf(index + 1);
                break;
            case ST.BULLET_LETTER_LOWER:
                string = String.valueOf((char) (index % 26 + 97));
                break;
            case ST.BULLET_LETTER_UPPER:
                string = String.valueOf((char) (index % 26 + 65));
                break;
        }
        if ((bullet.type & ST.BULLET_TEXT) != 0 && bullet.text != null) string += bullet.text;
        return string;
    }

    /**
     * Marks the StyledText properties that carry the renderer. Call it on writes to state the wire
     * value reads, not on any renderer call: its layout cache resets on every scroll.
     */
    public static void markRendererDirty(DartStyledTextRenderer renderer) {
        if (renderer == null) return;
        DartStyledText styledText = widgetOf(renderer);
        if (styledText == null) return;
        if (dev.equo.swt.Config.isDebug()) reportMark(styledText);
        styledText.getValue().markDirty(VStyledText.RENDERER);
        styledText.getValue().markDirty(VStyledText.STYLES);
        // The names are names in that palette, so they are never left behind by it.
        styledText.getValue().markDirty(VStyledText.STYLE_INDEX);
    }

    /** Debug: names the renderer method whose write marked the value changed. */
    private static void reportMark(DartStyledText styledText) {
        StackTraceElement[] frames = new Throwable().getStackTrace();
        String caller = frames.length > 2 ? frames[2].getMethodName() : "?";
        System.out.println("mark: StyledText/" + FlutterBridge.id(styledText.getApi())
                + "/renderer: " + caller);
    }

    private static DartStyledText widgetOf(DartStyledTextRenderer renderer) {
        StyledText styledText = renderer.styledText;
        if (styledText == null || styledText.isDisposed() || !(styledText.getImpl() instanceof DartStyledText))
            return null;
        return (DartStyledText) styledText.getImpl();
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

        Metrics metrics = FontMetricsUtil.metrics(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = FontMetricsUtil.metrics(baseFontId);
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

        Metrics metrics = FontMetricsUtil.metrics(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = FontMetricsUtil.metrics(baseFontId);
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

        Metrics metrics = FontMetricsUtil.metrics(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = FontMetricsUtil.metrics(baseFontId);
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

        Metrics metrics = FontMetricsUtil.metrics(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = FontMetricsUtil.metrics(baseFontId);
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

        Metrics metrics = FontMetricsUtil.metrics(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = FontMetricsUtil.metrics(baseFontId);
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

        Metrics metrics = FontMetricsUtil.metrics(fontId);
        if (metrics == null) {
            String baseFontId = fd.getName() + "-0-3";
            metrics = FontMetricsUtil.metrics(baseFontId);
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
        // The render side laid the line out; its row is the height, not an estimate from font
        // metrics that the two sides round differently (ADR-014).
        Integer measured = measuredRowHeight(widgetOf(renderer), lineIndex,
                offset - content.getOffsetAtLine(lineIndex));
        if (measured != null) return measured;
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
        if (renderer.styledText != null && renderer.styledText.getImpl() instanceof DartStyledText) {
            Integer measured = measuredLineHeight((DartStyledText) renderer.styledText.getImpl(), lineIndex);
            if (measured != null) return measured;
        }
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
