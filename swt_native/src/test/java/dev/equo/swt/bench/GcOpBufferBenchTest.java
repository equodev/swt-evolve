package dev.equo.swt.bench;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * What buffering a GC's draw ops costs per op.
 *
 * <p>Every draw call of every paint looks its GC's batch up, so the lookup is paid per op rather than
 * per frame. Two measurements: a whole paint through the real path, and the lookup alone, since the
 * former is dominated by serializing each op.
 *
 * <p>Not an assertion of a number - machines differ. It prints, so a change can be measured before
 * and after on the same machine.
 */
@Tag("bench")
@ExtendWith(Mocks.class)
class GcOpBufferBenchTest {

    private static final int OPS_PER_PAINT = 1000;
    private static final int WARMUP_PAINTS = 300;
    private static final int PAINTS = 2000;

    private RecordingBridge bridge;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void paintCostPerOp() {
        Shell shell = Mocks.shell();
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 400, 300);
        for (int i = 0; i < WARMUP_PAINTS; i++) paint(canvas);
        long best = Long.MAX_VALUE;
        long total = 0;
        for (int i = 0; i < PAINTS; i++) {
            long t = paint(canvas);
            total += t;
            if (t < best) best = t;
        }
        System.out.printf("[gc-ops] paint of %d ops: best=%6.1fns/op mean=%6.1fns/op%n", OPS_PER_PAINT,
                best / (double) OPS_PER_PAINT, total / (double) PAINTS / OPS_PER_PAINT);
    }

    private long paint(Canvas canvas) {
        bridge.comm.sent.clear();
        long start = System.nanoTime();
        GC gc = new GC(canvas);
        for (int i = 0; i < OPS_PER_PAINT; i++) gc.drawLine(i, 0, i, 10);
        gc.dispose();
        return System.nanoTime() - start;
    }

    /** The per-op lookup of each way to hold the batches, apart from everything else a draw does. */
    @Test
    void lookupCostPerOp() {
        Object gc = new Object();

        Map<Object, Object> shared = Collections.synchronizedMap(new WeakHashMap<>());
        report("shared map", () -> shared.computeIfAbsent(gc, g -> new Object()) != null);

        ThreadLocal<Map<Object, Object>> perThread = ThreadLocal.withInitial(WeakHashMap::new);
        report("per-thread map", () -> perThread.get().computeIfAbsent(gc, g -> new Object()) != null);

        Map<Object, Owned> owned = Collections.synchronizedMap(new WeakHashMap<>());
        report("owner-tagged map", () -> owned.computeIfAbsent(gc, g -> new Owned()).owner == Thread.currentThread());
    }

    private static final class Owned {
        final Thread owner = Thread.currentThread();
    }

    private static void report(String label, java.util.function.BooleanSupplier lookup) {
        int ops = 10_000_000;
        long best = Long.MAX_VALUE;
        int hits = 0;
        for (int run = 0; run < 15; run++) {
            long start = System.nanoTime();
            for (int i = 0; i < ops; i++) if (lookup.getAsBoolean()) hits++;
            long elapsed = System.nanoTime() - start;
            if (run >= 5 && elapsed < best) best = elapsed;
        }
        System.out.printf("[gc-ops] lookup %-16s best=%5.2fns/op (%d)%n", label, best / (double) ops, hits);
    }
}
