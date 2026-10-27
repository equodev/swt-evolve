package org.eclipse.swt.custom;

import com.google.gson.JsonObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Measures what a vertical resize costs an editor against how many ruler columns it moves. Asserts
 * nothing: results go to {@code build/editor-resize-cost.json} for before/after comparison.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EditorResizeCostFlutterTest {

    /** Height changes per variant, after a warm-up that is not measured. */
    private static final int STEPS = 20;
    private static final int LINE_HEIGHT = 18;
    private static final int TEXT_LINES = 2000;

    private final StyledTextFlutterStage stage = new StyledTextFlutterStage();
    private final List<Integer> sentBytes = new ArrayList<>();
    /** Bytes per channel, so a step's cost can be read as what it was spent on. */
    private final Map<String, int[]> byChannel = new LinkedHashMap<>();
    private final Map<String, Object> results = new LinkedHashMap<>();

    @BeforeAll
    void boot() {
        // Set before the page is served: WebFlutterServer substitutes it into index.html.
        System.setProperty("dev.equo.swt.web.perf", "true");
        stage.boot();
        // Received, from the browser's side: what Java sent. "Sent" would be the client's own acks.
        stage.flutter.devTools().onEvent("Network.webSocketFrameReceived", this::recordSent);
        stage.flutter.devTools().send("Network.enable", null);
    }

    @AfterAll
    void shutdown() throws IOException {
        stage.shutdown();
        Path out = Path.of("build", "editor-resize-cost.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(results));
        System.out.println("editor resize cost written to " + out.toAbsolutePath());
    }

    private void recordSent(JsonObject params) {
        JsonObject response = params.getAsJsonObject("response");
        if (response == null) return;
        String payload = response.has("payloadData") ? response.get("payloadData").getAsString() : null;
        if (payload == null) return;
        byte[] frame;
        try {
            frame = Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException notBase64) {
            frame = payload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        synchronized (sentBytes) {
            sentBytes.add(frame.length);
            for (String channel : channelsIn(frame)) {
                int[] tally = byChannel.computeIfAbsent(channel, c -> new int[2]);
                tally[0]++;
                tally[1] += frame.length / Math.max(1, channelsIn(frame).size());
            }
        }
    }

    /**
     * The channels a frame carries: its own, or every entry's when it is a batch. Names are
     * stripped of ids so a run of widgets reads as one line.
     */
    private static List<String> channelsIn(byte[] frame) {
        List<String> names = new ArrayList<>();
        if (frame.length < 2) return names;
        int nameLen = ((frame[0] & 0xFF) << 8) | (frame[1] & 0xFF);
        if (frame.length < 2 + nameLen) return names;
        String actionId = new String(frame, 2, nameLen, java.nio.charset.StandardCharsets.UTF_8);
        String body = new String(frame, 2 + nameLen, frame.length - 2 - nameLen,
                java.nio.charset.StandardCharsets.UTF_8);
        if (!"swt.evolve.batch".equals(actionId)) {
            names.add(actionId.replaceAll("/-?\\d+", "/#"));
            return names;
        }
        // A batch spells a channel out once and refers to repeats by entry index (see MessageBatch).
        java.util.List<String> asSent = new ArrayList<>();
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\[\\s*(?:\"([^\"]+)\"|(\\d+))\\s*,").matcher(body);
        while (m.find()) {
            String spelled = m.group(1);
            if (spelled != null) {
                asSent.add(spelled);
            } else {
                int at = Integer.parseInt(m.group(2));
                asSent.add(at >= 0 && at < asSent.size() ? asSent.get(at) : "?");
            }
            names.add(asSent.get(asSent.size() - 1).replaceAll("/-?\\d+", "/#"));
        }
        return names;
    }

    @Test
    void whatAVerticalResizeCostsPerControlItMoves() {
        for (int rulers : new int[] { 0, 3, 16 }) {
            measure(rulers, true);
            measure(rulers, false);
        }
        System.out.println("== vertical resize, by the controls the layout moves\n"
                + new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(results));
    }

    /** An editor: a container holding {@code rulers} gutter columns and the text beside them. */
    private void measure(int rulers, boolean growing) {
        Composite editor = new Composite(stage.shell, SWT.NONE);
        int gutter = 29;
        List<Canvas> columns = new ArrayList<>();
        for (int i = 0; i < rulers; i++) {
            Canvas column = new Canvas(editor, SWT.NO_BACKGROUND);
            // What a LineNumberRulerColumn does: draw the number of every line in view.
            column.addPaintListener(numbersPainter());
            columns.add(column);
        }
        StyledText text = new StyledText(editor, SWT.MULTI | SWT.V_SCROLL);
        text.setText(lines());

        int width = StyledTextFlutterStage.WIDTH;
        int height = StyledTextFlutterStage.HEIGHT;
        editor.setBounds(0, 0, width, height);
        layout(editor, columns, text, gutter, width, height);
        stage.settle();

        // Growing exposes never-painted area; shrinking exposes none.
        int delta = growing ? 3 : -3;
        // Room to move in the direction under test.
        if (growing) height -= 3 * (STEPS + 4);
        // Warm-up: the first steps pay for what the client has not built yet.
        for (int i = 0; i < 3; i++) {
            height += delta;
            editor.setBounds(0, 0, width, height);
            layout(editor, columns, text, gutter, width, height);
            stage.flutter.flush();
        }
        stage.settle();
        stage.flutter.frameCost();
        synchronized (sentBytes) {
            sentBytes.clear();
            byChannel.clear();
        }

        long[] perStep = new long[STEPS];
        for (int i = 0; i < STEPS; i++) {
            height += delta;
            long start = System.nanoTime();
            editor.setBounds(0, 0, width, height);
            layout(editor, columns, text, gutter, width, height);
            stage.flutter.flush();
            perStep[i] = System.nanoTime() - start;
        }

        int bytes, messages;
        synchronized (sentBytes) {
            messages = sentBytes.size();
            bytes = sentBytes.stream().mapToInt(Integer::intValue).sum();
            sentBytes.clear();
        }
        Map<String, String> marks = takeMarks();
        Map<String, String> rebuilds = takeRebuildCounts();
        List<Map<String, Object>> frames = stage.flutter.frameCost();
        double build = 0, raster = 0, worst = 0;
        for (Map<String, Object> f : frames) {
            double b = num(f.get("build")), r = num(f.get("raster"));
            build += b;
            raster += r;
            worst = Math.max(worst, b + r);
        }
        java.util.Arrays.sort(perStep);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("controls the layout moves", 2 + rulers);
        result.put("median ms per step", perStep[perStep.length / 2] / 1_000_000.0);
        result.put("p90 ms per step", perStep[(int) (perStep.length * 0.9)] / 1_000_000.0);
        result.put("client frames", frames.size());
        result.put("client build total ms", build);
        result.put("client build per step ms", build / STEPS);
        result.put("client raster per step ms", raster / STEPS);
        result.put("worst client frame ms", worst);
        result.put("messages to client per step", messages / (double) STEPS);
        result.put("bytes to client per step", bytes / STEPS);
        // One gcDispose is one off-screen render cycle.
        int cycles = 0, cycleOps = 0, cycleBytes = 0;
        synchronized (sentBytes) {
            for (Map.Entry<String, int[]> e : byChannel.entrySet()) {
                if (e.getKey().endsWith("/gcDispose")) cycles += e.getValue()[0];
                else if (e.getKey().startsWith("GC/#/")) {
                    cycleOps += e.getValue()[0];
                    cycleBytes += e.getValue()[1];
                }
            }
        }
        result.put("render cycles per step", cycles / (double) STEPS);
        result.put("draw ops per step", cycleOps / (double) STEPS);
        result.put("bytes of draw ops per step", cycleBytes / STEPS);
        result.put("bytes per draw op", cycleOps == 0 ? 0 : cycleBytes / cycleOps);
        Map<String, String> spent = new LinkedHashMap<>();
        synchronized (sentBytes) {
            byChannel.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue()[1], a.getValue()[1]))
                    .limit(8)
                    .forEach(e -> spent.put(e.getKey(),
                            String.format("%.1f msgs, %d B per step",
                                    e.getValue()[0] / (double) STEPS, e.getValue()[1] / STEPS)));
            byChannel.clear();
        }
        result.put("what a step is spent on", spent);
        result.put("where the client build went", marks);
        result.put("widgets Flutter rebuilt per step", rebuilds);
        results.put(rulers + " ruler columns, " + (growing ? "growing" : "shrinking"), result);

        text.dispose();
        editor.dispose();
        stage.settle();
    }

    /** The render side's own timings for the steps just measured, by pass, then cleared. */
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
                            Integer.parseInt(parts[1]) / (double) STEPS,
                            Double.parseDouble(parts[2]) / STEPS));
            }
        } catch (RuntimeException noMarks) {
            // A page without the flag reports nothing; the totals above still stand.
        }
        return marks;
    }

    /** How many widgets Flutter rebuilt, by kind, for the steps just measured, then cleared. */
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
                    counts.put(parts[0], String.format("%.1f per step", Integer.parseInt(parts[1]) / (double) STEPS));
            }
        } catch (RuntimeException noMarks) {
            // A page without the flag reports nothing.
        }
        return counts;
    }

    /** Lays the gutter columns out to the left of the text, the way a CompositeRuler does. */
    private static void layout(Composite editor, List<Canvas> columns, StyledText text,
            int gutter, int width, int height) {
        int x = 0;
        for (Canvas column : columns) {
            column.setBounds(x, 0, gutter, height);
            x += gutter;
        }
        text.setBounds(x, 0, Math.max(1, width - x), height);
    }

    private static PaintListener numbersPainter() {
        return new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                Control control = (Control) e.widget;
                Rectangle area = control.getBounds();
                for (int y = 0, line = 1; y < area.height; y += LINE_HEIGHT, line++) {
                    e.gc.drawString(Integer.toString(line), 4, y, true);
                }
            }
        };
    }

    private static String lines() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TEXT_LINES; i++) {
            sb.append("line ").append(i).append(" of the document\n");
        }
        return sb.toString();
    }

    private static double num(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0;
    }
}
