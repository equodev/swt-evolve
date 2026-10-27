package org.eclipse.swt.custom;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.equo.swt.harness.UserInput.Key;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * StyledText wire cost per user action, both directions; asserts nothing, writes
 * {@code build/styledtext-wire-census.json}.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextWireCensusFlutterTest {

    private static final int REPEAT = 10;

    private final StyledTextFlutterStage stage = new StyledTextFlutterStage();
    private final List<Frame> frames = new ArrayList<>();
    private final Map<String, Object> census = new LinkedHashMap<>();

    private record Frame(boolean fromJava, byte[] bytes) {
    }

    @BeforeAll
    void boot() {
        // Before boot: the server substitutes these flags into the page it serves.
        System.setProperty("dev.equo.swt.web.perf", "true");
        System.setProperty("dev.equo.swt.web.perfDetail", "true");
        stage.boot();
        stage.flutter.devTools().onEvent("Network.webSocketFrameSent", p -> record(false, p));
        stage.flutter.devTools().onEvent("Network.webSocketFrameReceived", p -> record(true, p));
        stage.flutter.devTools().send("Network.enable", null);
    }

    @AfterAll
    void stopAskingForMarks() {
        // JVM-wide: would otherwise leak into later test classes.
        System.clearProperty("dev.equo.swt.web.perf");
        System.clearProperty("dev.equo.swt.web.perfDetail");
    }

    @AfterAll
    void shutdown() throws IOException {
        stage.shutdown();
        Path out = Path.of("build", "styledtext-wire-census.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(census));
        System.out.println("wire census written to " + out.toAbsolutePath());
    }

    private void record(boolean fromJava, JsonObject params) {
        JsonObject response = params.getAsJsonObject("response");
        if (response == null || response.get("opcode").getAsInt() != 2) return;
        byte[] bytes = Base64.getDecoder().decode(response.get("payloadData").getAsString());
        synchronized (frames) {
            frames.add(new Frame(fromJava, bytes));
        }
    }

    /** Varied line shapes: identical rows would make every visible row's geometry cost the same. */
    private static String javaSource(int lines) {
        String[] shapes = {
            "package dev.equo.swt.custom;",
            "",
            "import org.eclipse.swt.widgets.Composite;",
            "",
            "/** One line of documentation about what this class is for. */",
            "public final class Sample%d implements Runnable, AutoCloseable {",
            "",
            "    private static final int SOME_CONSTANT_%d = %d;",
            "    private final Composite parent;",
            "",
            "    Sample%d(Composite parent, int style, String label, boolean enabled) {",
            "        this.parent = parent;",
            "        if (label == null || label.isEmpty()) {",
            "            throw new IllegalArgumentException(\"a label is required\");",
            "        }",
            "    }",
            "",
            "    @Override",
            "    public void run() {",
            "        for (int i = 0; i < SOME_CONSTANT_%d; i++) {",
            "            parent.getDisplay().asyncExec(() -> handle(i, \"value\" + i));",
            "        }",
            "    }",
            "",
            "    private void handle(int index, String value) {",
            "        // A comment that runs on a little, the way a real explanation does.",
            "        System.out.println(index + \": \" + value);",
            "    }",
            "",
            "    @Override",
            "    public void close() {",
            "    }",
            "}",
            "",
        };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            String shape = shapes[i % shapes.length];
            sb.append(shape.contains("%d") ? String.format(shape, i, i, i * 7, i, i) : shape).append('\n');
        }
        return sb.toString();
    }

    private void useMonospace() {
        org.eclipse.swt.graphics.Font mono =
                new org.eclipse.swt.graphics.Font(stage.display, "Liberation Mono", 12, SWT.NORMAL);
        stage.subject.setFont(mono);
        stage.twin.setFont(mono);
        stage.settle();
    }

    private void highlightDensely() {
        StyledText text = stage.subject;
        int chars = text.getCharCount();
        // The widget's own colour is in the palette: a real highlighter leaves most text in it.
        Color[] palette = {
            text.getForeground(),
            stage.display.getSystemColor(SWT.COLOR_DARK_BLUE),
            text.getForeground(),
            stage.display.getSystemColor(SWT.COLOR_DARK_GREEN),
            text.getForeground(),
            stage.display.getSystemColor(SWT.COLOR_DARK_RED),
            stage.display.getSystemColor(SWT.COLOR_DARK_MAGENTA),
        };
        List<Integer> ranges = new ArrayList<>();
        List<StyleRange> styles = new ArrayList<>();
        int i = 0;
        for (int at = 0; at + 6 < chars; at += 14, i++) {
            ranges.add(at);
            ranges.add(6);
            StyleRange style = new StyleRange();
            style.foreground = palette[i % palette.length];
            if (i % 5 == 0) style.fontStyle = SWT.BOLD;
            styles.add(style);
        }
        int[] flat = new int[ranges.size()];
        for (int k = 0; k < flat.length; k++) flat[k] = ranges.get(k);
        text.setStyleRanges(flat, styles.toArray(new StyleRange[0]));
        stage.settle();
    }

    @Test
    void wheelTickInHighlightedJavaSource() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, javaSource(2000));
        useMonospace();
        highlightDensely();
        double[] at = stage.page(stage.subject, 100, 100);
        measure("wheel tick, highlighted Java source", () -> {
            stage.input().wheel(at[0], at[1], 0, 120, 0);
            stage.settle();
        });
    }

    @Test
    void keystrokeInHighlightedJavaSource() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, javaSource(2000));
        useMonospace();
        highlightDensely();
        stage.focusAt(stage.subject.getOffsetAtLine(3));
        measure("keystroke, highlighted Java source", () -> stage.type("x"));
    }

    @Test
    void keystrokeInJavaSource() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, javaSource(2000));
        useMonospace();
        // The caret must be on screen: typing off-screen pushes no geometry.
        stage.focusAt(stage.subject.getOffsetAtLine(3));
        measure("keystroke, Java source (monospace)", () -> stage.type("x"));
    }

    @Test
    void wheelTickInJavaSource() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, javaSource(2000));
        useMonospace();
        double[] at = stage.page(stage.subject, 100, 100);
        measure("wheel tick, Java source (monospace)", () -> {
            stage.input().wheel(at[0], at[1], 0, 120, 0);
            stage.settle();
        });
    }

    private static String lines(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append("line ").append(i).append(" of a document that is being edited\n");
        return sb.toString();
    }

    @Test
    void keystrokeInASmallDocument() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(20));
        stage.focusAt(stage.subject.getOffsetAtLine(3));
        measure("keystroke, 20-line document", () -> stage.type("x"));
    }

    @Test
    void keystrokeInALargeDocument() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(2000));
        stage.focusAt(stage.subject.getOffsetAtLine(3));
        measure("keystroke, 2000-line document", () -> stage.type("x"));
    }

    /** The styling band follows the viewport, so a height-changing drag can re-send it every step. */
    @Test
    void resizeOfHighlightedJavaSource() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, javaSource(2000));
        useMonospace();
        highlightDensely();
        int[] height = { stage.subject.getSize().y };
        measure("resize, highlighted Java source", () -> {
            height[0] -= 7;
            stage.subject.setSize(stage.subject.getSize().x, height[0]);
            stage.settle();
        });
    }

    @Test
    void keystrokeDeepInHighlightedJavaSource() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, javaSource(2000));
        useMonospace();
        highlightDensely();
        // focusAt clicks on screen, so the caret moves deep only after focus is taken.
        stage.focusAt(stage.subject.getOffsetAtLine(3));
        stage.subject.setTopIndex(1880);
        stage.subject.setCaretOffset(stage.subject.getOffsetAtLine(1900));
        stage.settle();
        measure("keystroke at line 1900, highlighted Java source", () -> stage.type("x"));
    }

    @Test
    void arrowKeyInALargeDocument() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(2000));
        stage.focusAt(stage.subject.getOffsetAtLine(3));
        measure("arrow key, 2000-line document", () -> stage.press(Key.RIGHT));
    }

    @Test
    void wheelTickInALargeDocument() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(2000));
        double[] at = stage.page(stage.subject, 100, 100);
        measure("wheel tick, 2000-line document", () -> {
            stage.input().wheel(at[0], at[1], 0, 120, 0);
            stage.settle();
        });
    }

    @Test
    void resizeOfALargeDocument() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(2000));
        int[] width = { StyledTextFlutterStage.WIDTH };
        measure("resize, 2000-line document", () -> {
            width[0] += 3;
            stage.subject.setSize(width[0], StyledTextFlutterStage.HEIGHT);
            stage.settle();
        });
    }

    @Test
    void styleChangeInALargeDocument() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(2000));
        Color red = stage.display.getSystemColor(SWT.COLOR_RED);
        int[] start = { 0 };
        measure("style change, 2000-line document", () -> {
            stage.subject.setStyleRange(new StyleRange(start[0], 4, red, null));
            start[0] += 40;
            stage.settle();
        });
    }

    @Test
    void selectionChangeInALargeDocument() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(2000));
        stage.focusAt(0);
        measure("shift+arrow selection, 2000-line document", () -> stage.press(Key.RIGHT, SWT.SHIFT));
    }

    /** Engine frame times and Java's edit time, excluding the harness round trip. */
    @Test
    void keystrokeCost() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int lineCount : new int[] { 20, 2000 }) {
            stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(lineCount));
            stage.focusAt(stage.subject.getOffsetAtLine(3));
            for (int warmup = 0; warmup < 5; warmup++) stage.type("w");
            stage.settle();

            long[] editNanos = new long[20];
            long[] flushNanos = new long[20];
            for (int i = 0; i < editNanos.length; i++) {
                int at = stage.subject.getCaretOffset();
                long start = System.nanoTime();
                stage.subject.replaceTextRange(at, 0, "j");
                long edited = System.nanoTime();
                stage.flutter.flush();
                long flushed = System.nanoTime();
                editNanos[i] = edited - start;
                flushNanos[i] = flushed - edited;
            }
            java.util.Arrays.sort(editNanos);
            java.util.Arrays.sort(flushNanos);
            result.put(lineCount + " lines: Java replaceTextRange, median ms", editNanos[editNanos.length / 2] / 1_000_000.0);
            result.put(lineCount + " lines: Java flush, median ms", flushNanos[flushNanos.length / 2] / 1_000_000.0);

            stage.settle();
            stage.flutter.frameCost();
            @SuppressWarnings("unchecked")
            Map<String, Object> beforeTyping = (Map<String, Object>) stage.facts(stage.subject).get("layout");
            for (int i = 0; i < 20; i++) stage.type("x");
            stage.settle();
            @SuppressWarnings("unchecked")
            Map<String, Object> afterTyping = (Map<String, Object>) stage.facts(stage.subject).get("layout");
            List<Map<String, Object>> frames = stage.flutter.frameCost();
            double build = 0, raster = 0, worst = 0;
            for (Map<String, Object> f : frames) {
                double b = num(f.get("build")), r = num(f.get("raster"));
                build += b;
                raster += r;
                worst = Math.max(worst, b + r);
            }
            int count = Math.max(1, frames.size());
            result.put(lineCount + " lines: client frames per 20 keystrokes", frames.size());
            result.put(lineCount + " lines: client build, mean ms", build / count);
            result.put(lineCount + " lines: client raster, mean ms", raster / count);
            result.put(lineCount + " lines: client worst frame, ms", worst);
            result.put(lineCount + " lines: geometry table per keystroke, ms",
                    (num(afterTyping.get("geometryMs")) - num(beforeTyping.get("geometryMs"))) / 20);
            result.put(lineCount + " lines: shape rebuild per keystroke, ms",
                    (num(afterTyping.get("rebuildMs")) - num(beforeTyping.get("rebuildMs"))) / 20);
        }
        // Both axes: only a width change re-wraps, but an editor's sash drag changes the height.
        for (int style : new int[] { SWT.MULTI | SWT.V_SCROLL, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP })
        for (boolean horizontal : new boolean[] { true, false })
        for (int lineCount : new int[] { 2000 }) {
            String label = lineCount + ((style & SWT.WRAP) != 0 ? " wrapped lines" : " lines")
                    + (horizontal ? ", width" : ", height");
            stage.fresh(style, lines(lineCount));
            stage.settle();
            stage.flutter.frameCost();
            synchronized (frames) {
                frames.clear();
            }
            int[] size = { horizontal ? StyledTextFlutterStage.WIDTH : StyledTextFlutterStage.HEIGHT };
            long[] resizeNanos = new long[10];
            for (int i = 0; i < resizeNanos.length; i++) {
                size[0] += 7;
                long start = System.nanoTime();
                stage.subject.setSize(horizontal ? size[0] : StyledTextFlutterStage.WIDTH,
                        horizontal ? StyledTextFlutterStage.HEIGHT : size[0]);
                stage.flutter.flush();
                resizeNanos[i] = System.nanoTime() - start;
            }
            java.util.Arrays.sort(resizeNanos);
            long resizeBytesToJava = 0, resizeBytesToClient = 0;
            synchronized (frames) {
                for (Frame f : frames) {
                    if (f.fromJava()) resizeBytesToClient += f.bytes().length;
                    else resizeBytesToJava += f.bytes().length;
                }
                frames.clear();
            }
            result.put(label + ": resize, bytes to Java per step", resizeBytesToJava / resizeNanos.length);
            result.put(label + ": resize, bytes to client per step", resizeBytesToClient / resizeNanos.length);
            @SuppressWarnings("unchecked")
            Map<String, Object> resizeLayout = (Map<String, Object>) stage.facts(stage.subject).get("layout");
            @SuppressWarnings("unchecked")
            Map<String, Object> beforeStep = (Map<String, Object>) stage.facts(stage.subject).get("layout");
            size[0] += 7;
            stage.subject.setSize(horizontal ? size[0] : StyledTextFlutterStage.WIDTH,
                    horizontal ? StyledTextFlutterStage.HEIGHT : size[0]);
            stage.flutter.flush();
            @SuppressWarnings("unchecked")
            Map<String, Object> afterStep = (Map<String, Object>) stage.facts(stage.subject).get("layout");
            result.put(label + ": one resize step, lines laid out",
                    num(afterStep.get("laidOut")) - num(beforeStep.get("laidOut")));
            result.put(label + ": one resize step, lines from cache",
                    num(afterStep.get("cached")) - num(beforeStep.get("cached")));
            result.put(label + ": one resize step, painters built",
                    num(afterStep.get("painters")) - num(beforeStep.get("painters")));
            result.put(label + ": one resize step, line tops ms",
                    num(afterStep.get("lineTopsMs")) - num(beforeStep.get("lineTopsMs")));
            result.put(label + ": one resize step, paint ms",
                    num(afterStep.get("drawMs")) - num(beforeStep.get("drawMs")));
            result.put(label + ": one resize step, paints",
                    num(afterStep.get("drawCalls")) - num(beforeStep.get("drawCalls")));
            result.put(label + ": one resize step, geometry ms",
                    num(afterStep.get("geometryMs")) - num(beforeStep.get("geometryMs")));
            result.put(label + ": one resize step, geometry builds",
                    num(afterStep.get("geometryCalls")) - num(beforeStep.get("geometryCalls")));
            result.put(label + ": one resize step, shape rebuilds",
                    num(afterStep.get("rebuildCalls")) - num(beforeStep.get("rebuildCalls")));
            List<Map<String, Object>> resizeFrames = stage.flutter.frameCost();
            double worstResize = 0, resizeBuild = 0, resizeRaster = 0;
            for (Map<String, Object> f : resizeFrames) {
                worstResize = Math.max(worstResize, num(f.get("build")) + num(f.get("raster")));
                resizeBuild += num(f.get("build"));
                resizeRaster += num(f.get("raster"));
            }
            int resizeFrameCount = Math.max(1, resizeFrames.size());
            result.put(label + ": resize, frames", resizeFrames.size());
            result.put(label + ": resize, build mean ms", resizeBuild / resizeFrameCount);
            result.put(label + ": resize, raster mean ms", resizeRaster / resizeFrameCount);
            result.put(label + ": resize, median ms", resizeNanos[resizeNanos.length / 2] / 1_000_000.0);
            result.put(label + ": resize, worst client frame ms", worstResize);
        }
        stage.flutter.styledTextPerf();
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(2000));
        stage.focusAt(stage.subject.getOffsetAtLine(3));
        stage.settle();
        stage.flutter.styledTextPerf();
        for (int i = 0; i < 10; i++) stage.type("p");
        stage.settle();
        Map<String, Object> perf = stage.flutter.styledTextPerf();
        result.put("2000 lines: counters, paints per keystroke", num(perf.get("paints")) / 10);
        result.put("2000 lines: counters, paint ms per keystroke", num(perf.get("paintMs")) / 10);
        result.put("2000 lines: counters, lines laid out per keystroke", num(perf.get("linesLaidOut")) / 10);
        result.put("2000 lines: counters, lines painted in the last paint", num(perf.get("linesInLastPaint")));
        census.put("keystroke, what the two sides spend", result);
        System.out.println("== keystroke, what the two sides spend\n"
                + new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));
    }

    /** Two document sizes: their difference is the per-keystroke work proportional to the document. */
    @Test
    void typingLatency() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int lineCount : new int[] { 20, 2000 }) {
            stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(lineCount));
            stage.focusAt(stage.subject.getOffsetAtLine(3));
            stage.settle();
            for (int warmup = 0; warmup < 5; warmup++) stage.type("w");
            stage.settle();
            long[] samples = new long[20];
            for (int i = 0; i < samples.length; i++) {
                long start = System.nanoTime();
                stage.type("x");
                samples[i] = System.nanoTime() - start;
            }
            java.util.Arrays.sort(samples);
            result.put(lineCount + " lines: median ms", samples[samples.length / 2] / 1_000_000.0);
            result.put(lineCount + " lines: p90 ms", samples[(int) (samples.length * 0.9)] / 1_000_000.0);
        }
        // Baseline: an action that changes no layout.
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(2000));
        stage.focusAt(stage.subject.getOffsetAtLine(3));
        stage.settle();
        long[] arrows = new long[20];
        for (int i = 0; i < arrows.length; i++) {
            long start = System.nanoTime();
            stage.press(Key.RIGHT);
            arrows[i] = System.nanoTime() - start;
        }
        java.util.Arrays.sort(arrows);
        @SuppressWarnings("unchecked")
        Map<String, Object> layoutBefore = (Map<String, Object>) stage.facts(stage.subject).get("layout");
        stage.type("z");
        @SuppressWarnings("unchecked")
        Map<String, Object> layoutAfter = (Map<String, Object>) stage.facts(stage.subject).get("layout");
        result.put("2000 lines, one keystroke: lines laid out",
                num(layoutAfter.get("laidOut")) - num(layoutBefore.get("laidOut")));
        result.put("2000 lines, one keystroke: lines served from cache",
                num(layoutAfter.get("cached")) - num(layoutBefore.get("cached")));
        result.put("2000 lines, one keystroke: live painters built",
                num(layoutAfter.get("painters")) - num(layoutBefore.get("painters")));
        @SuppressWarnings("unchecked")
        Map<String, Object> idle = (Map<String, Object>) stage.facts(stage.subject).get("layout");
        result.put("2000 lines, nothing happening: live painters built",
                num(idle.get("painters")) - num(layoutAfter.get("painters")));
        stage.press(Key.RIGHT);
        @SuppressWarnings("unchecked")
        Map<String, Object> afterArrow = (Map<String, Object>) stage.facts(stage.subject).get("layout");
        result.put("2000 lines, one arrow key: live painters built",
                num(afterArrow.get("painters")) - num(idle.get("painters")));
        result.put("2000 lines, one arrow key: paints", num(afterArrow.get("draws")) - num(idle.get("draws")));
        result.put("2000 lines: painters in the last paint", num(afterArrow.get("paintersLastDraw")));
        result.put("2000 lines, arrow key (no relayout): median ms", arrows[arrows.length / 2] / 1_000_000.0);
        census.put("keystroke, round trip to a painted frame", result);
        System.out.println("== keystroke, round trip to a painted frame\n"
                + new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));
    }

    /** A keystroke must push a splice of the geometry table Java holds, not the whole table. */
    @Test
    void geometryTablePerKeystroke() {
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, lines(2000));
        stage.focusAt(stage.subject.getOffsetAtLine(3));
        stage.settle();
        // The first keystroke settles what Java holds; the second is the one measured against it.
        String whole = lastGeometry(() -> stage.type("x"));
        String push = lastGeometry(() -> stage.type("y"));
        Assertions.assertNotNull(push, "a keystroke pushed no geometry at all");

        JsonObject payload = JsonParser.parseString(push).getAsJsonObject();
        Assertions.assertNull(payload.get("lines"),
                "a keystroke described the whole table instead of the rows it changed");
        JsonArray rows = payload.getAsJsonArray("rows");
        Assertions.assertNotNull(rows, "the push named no rows");
        Assertions.assertTrue(rows.size() <= 4,
                "a keystroke rewrote " + rows.size() + " rows, which is not a splice of one line");

        int rowBytes = 0;
        for (JsonElement row : rows) {
            rowBytes += row.toString().length();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows pushed", rows.size());
        result.put("push bytes", push.length());
        result.put("bytes of the pushed rows", rowBytes);
        result.put("first row rewritten", num(payload.get("rowStart") == null ? 0 : payload.get("rowStart").getAsInt()));
        result.put("rows replaced", num(payload.get("rowsReplaced") == null ? 0 : payload.get("rowsReplaced").getAsInt()));
        if (whole != null) {
            result.put("previous push bytes", whole.length());
        }
        census.put("geometry table, keystroke in a 2000-line document", result);
        System.out.println("== geometry table, keystroke in a 2000-line document\n"
                + new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));
    }

    private static double num(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : 0;
    }

    private static boolean sameField(JsonObject a, JsonObject b, String key) {
        return String.valueOf(a.get(key)).equals(String.valueOf(b.get(key)));
    }

    private String lastGeometry(Runnable action) {
        synchronized (frames) {
            frames.clear();
        }
        action.run();
        stage.settle();
        List<Frame> taken;
        synchronized (frames) {
            taken = new ArrayList<>(frames);
        }
        String last = null;
        for (Frame f : taken) {
            if (f.fromJava()) continue;
            byte[] frame = f.bytes();
            if (frame.length < 2) continue;
            int nameLength = ((frame[0] & 0xff) << 8) | (frame[1] & 0xff);
            if (2 + nameLength > frame.length) continue;
            String name = new String(frame, 2, nameLength, StandardCharsets.UTF_8);
            if (!name.endsWith("/TextGeometry")) continue;
            last = new String(frame, 2 + nameLength, frame.length - 2 - nameLength, StandardCharsets.UTF_8);
        }
        return last;
    }

    private void measure(String scenario, Runnable action) {
        stage.settle();
        synchronized (frames) {
            frames.clear();
        }
        stage.flutter.frameCost();
        stage.flutter.styledTextPerf();
        takeMarks();
        takeRebuildCounts();
        for (int i = 0; i < REPEAT; i++) action.run();
        stage.settle();
        List<Frame> taken;
        synchronized (frames) {
            taken = new ArrayList<>(frames);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, long[]> toClient = new TreeMap<>(), toJava = new TreeMap<>();
        long bytesToClient = 0, bytesToJava = 0;
        int framesToClient = 0, framesToJava = 0;
        for (Frame f : taken) {
            if (f.fromJava()) {
                framesToClient++;
                bytesToClient += f.bytes().length;
                account(f.bytes(), toClient);
            } else {
                framesToJava++;
                bytesToJava += f.bytes().length;
                account(f.bytes(), toJava);
            }
        }
        result.put("per action: frames to client", framesToClient / (double) REPEAT);
        result.put("per action: bytes to client", bytesToClient / REPEAT);
        result.put("per action: frames to Java", framesToJava / (double) REPEAT);
        result.put("per action: bytes to Java", bytesToJava / REPEAT);
        result.put("to client, by channel (messages, bytes per action)", perAction(toClient));
        result.put("to Java, by channel (messages, bytes per action)", perAction(toJava));
        // Bytes are half the cost: a payload is paid again in client build and raster.
        List<Map<String, Object>> rendered = stage.flutter.frameCost();
        double build = 0, raster = 0, worst = 0;
        for (Map<String, Object> f : rendered) {
            double b = num(f.get("build")), r = num(f.get("raster"));
            build += b;
            raster += r;
            worst = Math.max(worst, b + r);
        }
        result.put("per action: client frames", rendered.size() / (double) REPEAT);
        result.put("per action: client build ms", build / REPEAT);
        result.put("per action: client raster ms", raster / REPEAT);
        result.put("worst client frame ms", worst);
        Map<String, Object> shape = stage.flutter.styledTextPerf();
        for (String k : new String[] { "linesLaidOut", "linesFromCache", "lineKeyMisses", "lineCaretMisses", "textPaintersBuilt",
                "paints", "paintMs", "lineTopsMs" }) {
            Object v = shape.get(k);
            if (v instanceof Number) {
                double d = ((Number) v).doubleValue() / REPEAT;
                result.put("per action: " + k, Math.round(d * 100) / 100.0);
            }
        }
        result.put("where the client build went", takeMarks());
        result.put("widgets Flutter rebuilt per action", takeRebuildCounts());
        census.put(scenario, result);
        System.out.println("== " + scenario + "\n" + new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));
    }

    private static Map<String, String> perAction(Map<String, long[]> byChannel) {
        Map<String, String> out = new LinkedHashMap<>();
        byChannel.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[1], a.getValue()[1]))
                .forEach(e -> out.put(e.getKey(),
                        String.format("%.1f msgs, %d B", e.getValue()[0] / (double) REPEAT, e.getValue()[1] / REPEAT)));
        return out;
    }

    /** Splits a frame into its channels: a name-prefixed payload, or a batch of them. */
    private static void account(byte[] frame, Map<String, long[]> byChannel) {
        if (frame.length < 2) return;
        int nameLength = ((frame[0] & 0xff) << 8) | (frame[1] & 0xff);
        if (2 + nameLength > frame.length) return;
        String name = new String(frame, 2, nameLength, StandardCharsets.UTF_8);
        int payloadStart = 2 + nameLength;
        if (name.equals("swt.evolve.batch")) {
            try {
                JsonArray entries = JsonParser.parseString(
                        new String(frame, payloadStart, frame.length - payloadStart, StandardCharsets.UTF_8)).getAsJsonArray();
                for (JsonElement entry : entries) {
                    JsonArray pair = entry.getAsJsonArray();
                    add(byChannel, pair.get(0).getAsString(), pair.get(1).toString().length(), pair.get(1));
                }
                return;
            } catch (RuntimeException notJson) {
                // counted whole below
            }
        }
        add(byChannel, name, frame.length - payloadStart, null);
    }

    private static void add(Map<String, long[]> byChannel, String channel, long bytes, JsonElement payload) {
        String kind = channel.replaceAll("/-?\\d+", "/#");
        if (payload != null && payload.isJsonObject() && kind.startsWith("StyledText/")) {
            JsonObject object = payload.getAsJsonObject();
            // Which properties an update names, or that it is the whole widget.
            kind += object.has("_d") ? " " + object.get("_d") : " (whole)";
        }
        long[] tally = byChannel.computeIfAbsent(kind, k -> new long[2]);
        tally[0]++;
        tally[1] += bytes;
    }
    private Map<String, String> takeMarks() {
        com.google.gson.JsonObject params = new com.google.gson.JsonObject();
        params.addProperty("returnByValue", true);
        params.addProperty("expression",
                "(() => {const t={};for (const m of performance.getEntriesByType('measure'))"
                + "{const e=t[m.name]||(t[m.name]=[0,0]);e[0]++;e[1]+=m.duration;}"
                + "performance.clearMeasures();return Object.entries(t)"
                + ".sort((a,b)=>b[1][1]-a[1][1]).map(([n,[c,d]])=>n+'|'+c+'|'+d.toFixed(1)).join(';');})()");
        Map<String, String> marks = new LinkedHashMap<>();
        try {
            com.google.gson.JsonObject res = stage.flutter.devTools().send("Runtime.evaluate", params);
            com.google.gson.JsonObject value = res == null ? null : res.getAsJsonObject("result");
            String flat = value == null || !value.has("value") ? "" : value.get("value").getAsString();
            for (String entry : flat.split(";")) {
                String[] parts = entry.split("\\|");
                if (parts.length == 3)
                    marks.put(parts[0], String.format("%.2f calls, %.2f ms per step",
                            Integer.parseInt(parts[1]) / (double) REPEAT,
                            Double.parseDouble(parts[2]) / REPEAT));
            }
        } catch (RuntimeException noMarks) {
            // A page without the flag reports nothing.
        }
        return marks;
    }

    private Map<String, String> takeRebuildCounts() {
        com.google.gson.JsonObject params = new com.google.gson.JsonObject();
        params.addProperty("returnByValue", true);
        params.addProperty("expression",
                "(() => {const t={};for (const m of performance.getEntriesByType('mark'))"
                + "{if (!m.name.startsWith('count:')) continue;const n=m.name.slice(6);t[n]=(t[n]||0)+1;}"
                + "performance.clearMarks();return Object.entries(t)"
                + ".sort((a,b)=>b[1]-a[1]).map(([n,c])=>n+'|'+c).join(';');})()");
        Map<String, String> counts = new LinkedHashMap<>();
        try {
            com.google.gson.JsonObject res = stage.flutter.devTools().send("Runtime.evaluate", params);
            com.google.gson.JsonObject value = res == null ? null : res.getAsJsonObject("result");
            String flat = value == null || !value.has("value") ? "" : value.get("value").getAsString();
            for (String entry : flat.split(";")) {
                String[] parts = entry.split("\\|");
                if (parts.length == 2)
                    counts.put(parts[0], String.format("%.1f per step", Integer.parseInt(parts[1]) / (double) REPEAT));
            }
        } catch (RuntimeException noMarks) {
            // A page without the flag reports nothing.
        }
        return counts;
    }

}
