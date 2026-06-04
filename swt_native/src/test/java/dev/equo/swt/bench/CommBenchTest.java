package dev.equo.swt.bench;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.stream.Stream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Cross-language RTT bench: each (direction, shape) measures a real Java↔Dart round trip where the
 * far runtime decodes the payload with the production codec (V*.fromJson / Event deserialize /
 * base64 image) and acks back. The reported latency is the true cross-language cost.
 *
 * <p>Shapes are real V*-tree (J→D) or real VEvent (D→J) payloads, no synthetic padding. Their
 * natural serialized byte sizes are reported in the result JSON alongside the timings.
 */
@DisabledOnOs(OS.LINUX)
@Tag("bench")
public class CommBenchTest {

    private static final int WARMUP = Integer.getInteger("bench.warmup", 100);
    private static final int MEASURED = Integer.getInteger("bench.measured", 1000);
    private static final long FUTURE_TIMEOUT_MS = Long.getLong("bench.timeoutMs", 30_000);

    @RegisterExtension
    static final BenchBridge bridge = new BenchBridge();

    static final java.util.List<BenchPayloads.Shape> SHAPES =
            java.util.List.of(BenchPayloads.SMALL, BenchPayloads.MEDIUM, BenchPayloads.LARGE,
                    BenchPayloads.IMAGE_SMALL, BenchPayloads.IMAGE_LARGE, BenchPayloads.SVG,
                    BenchPayloads.WORKBENCH);

    /** Source for the J→D parameterized test — iterates the real-V* shapes. */
    static Stream<BenchPayloads.Shape> shapes() { return SHAPES.stream(); }

    private static final Map<String, Object> results = new LinkedHashMap<>();
    static {
        results.put("timestamp", Instant.now().toString());
        results.put("comm_impl", System.getProperty("bench.comm.label", System.getProperty("comm.impl", "java-websocket")));
        results.put("warmup", WARMUP);
        results.put("measured", MEASURED);
        results.put("j2d_rtt_json", new LinkedHashMap<String, Object>());
        results.put("d2j_rtt_json", new LinkedHashMap<String, Object>());
        Map<String, Object> bytes = new LinkedHashMap<>();
        for (BenchPayloads.Shape s : SHAPES) bytes.put(s.name, s.size);
        results.put("shape_bytes", bytes);
    }

    // ====== J→D RTT ======

    @ParameterizedTest(name = "J→D JSON RTT, shape={0}")
    @MethodSource("shapes")
    void j2d_rtt_json(BenchPayloads.Shape shape) {
        for (int i = 0; i < WARMUP; i++) awaitFuture(bridge.j2dJsonEcho(shape));
        long[] samples = new long[MEASURED];
        for (int i = 0; i < MEASURED; i++) {
            long t1 = System.nanoTime();
            Long t2 = awaitFuture(bridge.j2dJsonEcho(shape));
            samples[i] = t2 - t1;
        }
        record("j2d_rtt_json", shape.name, summarize(samples, shape));
    }

    // ====== D→J RTT (realistic VEvent variants only — no synthetic-size buckets) ======

    @ParameterizedTest(name = "D→J JSON RTT, shape={0}")
    @ValueSource(strings = {"MOUSE_MOVE", "KEY_DOWN", "SELECTION", "IMG_SMALL", "IMG_LARGE"})
    void d2j_rtt_json(String shape) {
        for (int i = 0; i < WARMUP; i++) awaitFuture(bridge.d2jJsonEcho(shape));
        long[] samples = new long[MEASURED];
        for (int i = 0; i < MEASURED; i++) {
            BenchBridge.EchoResult r = awaitFuture(bridge.d2jJsonEcho(shape));
            if (r == null) throw new RuntimeException("d2j json echo missing at iter " + i);
            samples[i] = r.rttMicros * 1000L;
        }
        record("d2j_rtt_json", shape, summarizeNoShape(samples));
    }

    // ====== fixtures ======

    @AfterAll
    static void writeResults() throws IOException {
        Path outDir = Paths.get(System.getProperty("user.dir"), "build", "bench-results");
        Files.createDirectories(outDir);
        String json = toJson(results);
        String stamp = Instant.now().toString().replace(':', '-');
        Path stamped = outDir.resolve("bench-" + stamp + ".json");
        Path latest = outDir.resolve("latest.json");
        Files.writeString(stamped, json, StandardCharsets.UTF_8);
        Files.writeString(latest, json, StandardCharsets.UTF_8);
        System.out.println("[bench] wrote " + latest.toAbsolutePath());
    }

    private static <R> R awaitFuture(CompletableFuture<R> f) {
        long deadline = System.currentTimeMillis() + FUTURE_TIMEOUT_MS;
        while (!f.isDone() && System.currentTimeMillis() < deadline) {
            BenchBridge.pump(1);
        }
        try {
            return f.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Future did not complete within " + FUTURE_TIMEOUT_MS + "ms", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void record(String section, String key, Map<String, Object> value) {
        Map<String, Object> bucket = (Map<String, Object>) results.get(section);
        bucket.put(key, value);
    }

    private static Map<String, Object> summarize(long[] samples, BenchPayloads.Shape shape) {
        Map<String, Object> m = summarizeNoShape(samples);
        m.put("bytes", shape.size);
        return m;
    }

    private static Map<String, Object> summarizeNoShape(long[] samples) {
        long[] sorted = samples.clone();
        Arrays.sort(sorted);
        long sum = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for (long s : sorted) {
            sum += s;
            if (s > max) max = s;
            if (s < min) min = s;
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("count", sorted.length);
        m.put("min_ns", min);
        m.put("p50_ns", percentile(sorted, 0.50));
        m.put("p95_ns", percentile(sorted, 0.95));
        m.put("p99_ns", percentile(sorted, 0.99));
        m.put("max_ns", max);
        m.put("mean_ns", sum / sorted.length);
        return m;
    }

    private static long percentile(long[] sorted, double p) {
        int idx = (int) Math.min(sorted.length - 1, Math.floor(p * sorted.length));
        return sorted[idx];
    }

    @SuppressWarnings("unchecked")
    private static String toJson(Object o) {
        StringBuilder sb = new StringBuilder();
        appendJson(sb, o, 0);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void appendJson(StringBuilder sb, Object o, int depth) {
        if (o == null) { sb.append("null"); return; }
        if (o instanceof Number || o instanceof Boolean) { sb.append(o); return; }
        if (o instanceof String) {
            sb.append('"').append(((String) o).replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            return;
        }
        if (o instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) o;
            sb.append("{\n");
            int i = 0, n = m.size();
            for (Map.Entry<String, Object> e : m.entrySet()) {
                indent(sb, depth + 1);
                sb.append('"').append(e.getKey()).append("\": ");
                appendJson(sb, e.getValue(), depth + 1);
                if (++i < n) sb.append(',');
                sb.append('\n');
            }
            indent(sb, depth);
            sb.append('}');
            return;
        }
        sb.append('"').append(o.toString()).append('"');
    }

    private static void indent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) sb.append("  ");
    }
}
