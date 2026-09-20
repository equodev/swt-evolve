package dev.equo.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Measures how fast the SWT event loop reacts to work posted from another thread — the path every
 * Flutter event takes, since the comm thread hands events to the UI thread with {@code asyncExec}.
 *
 * <p>Two measurements, both driven from a non-UI thread while the loop is idle:
 * <ul>
 *   <li><b>wake latency</b> — {@code asyncExec} to the runnable starting, sampled at randomized
 *       intervals so samples cannot align with a fixed idle-wait period;</li>
 *   <li><b>burst drain</b> — the time for a batch of runnables posted at once to all run, which
 *       measures loop turn cost rather than wake-up.</li>
 * </ul>
 *
 * <p>Run it in a render mode rather than on its own: {@code runDeskExample} for the native window,
 * {@code runWebExample} for the browser surface, and the {@code :cross} wrappers for the Linux and
 * Windows guests. Results go to stdout and to {@code build/bench-results/}.
 *
 * <p>Tuning: {@code -Dbench.samples} (default 200), {@code -Dbench.burst} (200),
 * {@code -Dbench.warmupMs} (4000), {@code -Dbench.minGapMs} (25), {@code -Dbench.maxGapMs} (60).
 *
 * <p>Keep the pointer off the window while it runs: on a native surface a stray OS event wakes the
 * idle wait early and flatters the measurement.
 */
public class EventLoopWakeBench {

    private static final int SAMPLES = Integer.getInteger("bench.samples", 200);
    private static final int BURST = Integer.getInteger("bench.burst", 200);
    private static final int WARMUP_MS = Integer.getInteger("bench.warmupMs", 4000);
    private static final int MIN_GAP_MS = Integer.getInteger("bench.minGapMs", 25);
    private static final int MAX_GAP_MS = Integer.getInteger("bench.maxGapMs", 60);

    /** Counts idle parks, so the report can say whether the loop was actually asleep when sampled. */
    private static final AtomicInteger parks = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        Display display = new Display();
        display.addListener(SWT.PreExternalEventDispatch, e -> parks.incrementAndGet());

        Shell shell = new Shell(display);
        shell.setText("EventLoopWakeBench");
        shell.setLayout(new RowLayout(SWT.VERTICAL));
        Label status = new Label(shell, SWT.NONE);
        status.setText("Measuring event-loop wake latency...");
        shell.setSize(420, 140);
        shell.open();

        Results results = new Results(mode(), os());
        Thread driver = new Thread(() -> drive(display, results), "wake-bench-driver");
        driver.setDaemon(true);
        driver.start();

        while (!shell.isDisposed() && !results.done) {
            if (!display.readAndDispatch()) display.sleep();
        }
        results.parks = parks.get();
        report(results);
        display.dispose();
    }

    /** Runs both measurements from outside the UI thread, then ends the loop. */
    private static void drive(Display display, Results results) {
        try {
            Thread.sleep(WARMUP_MS);
            int warmupParks = parks.get();
            if (warmupParks == 0) {
                results.warning = "the loop never parked during warmup; latencies below are not idle-wake times";
            }
            results.wakeNanos = measureWakeLatency(display);
            results.burstNanos = measureBurstDrain(display);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            results.done = true;
            if (!display.isDisposed()) display.wake();
        }
    }

    /**
     * One {@code asyncExec} at a time, each after a randomized idle gap: the runnable can only start
     * once the loop leaves its idle wait, so the sample is the wake-up itself.
     */
    private static long[] measureWakeLatency(Display display) throws InterruptedException {
        Random random = new Random(20250919L);
        long[] samples = new long[SAMPLES];
        int taken = 0;
        for (int i = 0; i < SAMPLES; i++) {
            Thread.sleep(MIN_GAP_MS + random.nextInt(Math.max(1, MAX_GAP_MS - MIN_GAP_MS)));
            if (display.isDisposed()) break;
            CountDownLatch ran = new CountDownLatch(1);
            AtomicLong startedAt = new AtomicLong();
            long postedAt = System.nanoTime();
            display.asyncExec(() -> {
                startedAt.set(System.nanoTime());
                ran.countDown();
            });
            if (!ran.await(5, TimeUnit.SECONDS)) break;
            samples[taken++] = startedAt.get() - postedAt;
        }
        return Arrays.copyOf(samples, taken);
    }

    /** All runnables posted before the loop can drain any of them: measures per-turn cost. */
    private static long measureBurstDrain(Display display) throws InterruptedException {
        if (display.isDisposed()) return -1;
        CountDownLatch all = new CountDownLatch(BURST);
        long postedAt = System.nanoTime();
        for (int i = 0; i < BURST; i++) display.asyncExec(all::countDown);
        if (!all.await(60, TimeUnit.SECONDS)) return -1;
        return System.nanoTime() - postedAt;
    }

    private static void report(Results results) {
        long[] sorted = results.wakeNanos.clone();
        Arrays.sort(sorted);
        List<String> lines = new ArrayList<>();
        lines.add("mode=" + results.mode + " os=" + results.os + " samples=" + sorted.length + " parks=" + results.parks);
        if (results.warning != null) lines.add("WARNING: " + results.warning);
        lines.add(String.format(Locale.ROOT, "wake latency ms: min=%.3f p50=%.3f p95=%.3f p99=%.3f max=%.3f mean=%.3f",
                ms(min(sorted)), ms(percentile(sorted, 50)), ms(percentile(sorted, 95)),
                ms(percentile(sorted, 99)), ms(max(sorted)), ms(mean(sorted))));
        lines.add(String.format(Locale.ROOT, "burst of %d: total=%.1f ms, per runnable=%.3f ms",
                BURST, ms(results.burstNanos), ms(results.burstNanos / Math.max(1, BURST))));
        lines.forEach(System.out::println);
        writeJson(results, sorted);
    }

    private static void writeJson(Results results, long[] sorted) {
        Path dir = Path.of("build", "bench-results");
        Path file = dir.resolve("wake-" + results.mode + "-" + results.os + ".json");
        try {
            Files.createDirectories(dir);
            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(file))) {
                out.println("{");
                out.println("  \"mode\": \"" + results.mode + "\",");
                out.println("  \"os\": \"" + results.os + "\",");
                out.println("  \"samples\": " + sorted.length + ",");
                out.println("  \"parks\": " + results.parks + ",");
                out.println("  \"wake_ms\": {");
                out.println("    \"min\": " + fmt(min(sorted)) + ", \"p50\": " + fmt(percentile(sorted, 50))
                        + ", \"p95\": " + fmt(percentile(sorted, 95)) + ", \"p99\": " + fmt(percentile(sorted, 99))
                        + ", \"max\": " + fmt(max(sorted)) + ", \"mean\": " + fmt(mean(sorted)));
                out.println("  },");
                out.println("  \"burst\": { \"count\": " + BURST + ", \"total_ms\": " + fmt(results.burstNanos)
                        + ", \"per_runnable_ms\": " + fmt(results.burstNanos / Math.max(1, BURST)) + " }");
                out.println("}");
            }
            System.out.println("results: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("could not write results: " + e);
        }
    }

    private static String fmt(double nanos) {
        return String.format(Locale.ROOT, "%.3f", ms(nanos));
    }

    private static double ms(double nanos) {
        return nanos / 1_000_000.0;
    }

    private static double percentile(long[] sorted, int p) {
        if (sorted.length == 0) return 0;
        int index = Math.min(sorted.length - 1, (int) Math.ceil(p / 100.0 * sorted.length) - 1);
        return sorted[Math.max(0, index)];
    }

    private static double min(long[] sorted) {
        return sorted.length == 0 ? 0 : sorted[0];
    }

    private static double max(long[] sorted) {
        return sorted.length == 0 ? 0 : sorted[sorted.length - 1];
    }

    private static double mean(long[] sorted) {
        if (sorted.length == 0) return 0;
        long total = 0;
        for (long sample : sorted) total += sample;
        return (double) total / sorted.length;
    }

    private static String mode() {
        String mode = System.getProperty("dev.equo.swt.mode");
        return mode == null || mode.isBlank() ? "web" : mode;
    }

    private static String os() {
        String name = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
        if (name.contains("mac")) return "macos";
        if (name.contains("win")) return "windows";
        if (name.contains("linux")) return "linux";
        return name.replaceAll("\\s+", "-");
    }

    private static final class Results {
        final String mode;
        final String os;
        long[] wakeNanos = new long[0];
        long burstNanos = -1;
        int parks;
        String warning;
        volatile boolean done;

        Results(String mode, String os) {
            this.mode = mode;
            this.os = os;
        }
    }
}
