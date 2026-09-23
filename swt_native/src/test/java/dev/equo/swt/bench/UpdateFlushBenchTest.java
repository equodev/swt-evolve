package dev.equo.swt.bench;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

/**
 * What one flush costs, at the three shapes a flush actually comes in.
 *
 * <p>{@link FlutterBridge#update()} runs on every turn of the event loop, so its cost is paid
 * constantly rather than once - which is the reason to measure any work added to it rather than
 * reason about it. The three shapes are the ones the payload benches already use: a whole workbench
 * tree (the worst case, every widget dirty at once), a medium composite, and a single leaf (the
 * common case, one control changing).
 *
 * <p>Not an assertion of a number - machines differ. It prints, so a change to the flush can be
 * measured before and after on the same machine.
 */
@Tag("bench")
@ExtendWith(Mocks.class)
class UpdateFlushBenchTest {

    /** Iterations scale down as the shape grows, so the whole bench stays under a minute. */
    private static int warmup(int widgets) {
        return widgets > 200 ? 5 : 200;
    }

    private static int runs(int widgets) {
        return widgets > 200 ? 30 : 2000;
    }

    private RecordingBridge bridge;
    private Shell shell;

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
        shell = Mocks.shell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void flushCostByShape() {
        report("workbench", subtreeOf(WorkbenchTree.build(shell).getApi()));
        report("medium", subtreeOf(mediumComposite()));
        report("leaf", List.of(leafButton()));
        // Bigger and deeper than the fixture, to bound the cost where a real workbench sits rather
        // than where the sample happens to.
        report("deep300", subtreeOf(deepTree(300, 10)));
    }

    /** A composite of a dozen controls - a view's worth, not a window's. */
    private Control mediumComposite() {
        Composite c = new Composite(shell, SWT.NONE);
        for (int i = 0; i < 12; i++) {
            new Button(c, SWT.PUSH).setText("b" + i);
        }
        return c;
    }

    /** {@code count} controls nested {@code depth} deep, so ordering has real work to do. */
    private Control deepTree(int count, int depth) {
        Composite root = new Composite(shell, SWT.NONE);
        Composite at = root;
        int made = 0;
        for (int level = 0; level < depth && made < count; level++) {
            Composite next = new Composite(at, SWT.NONE);
            made++;
            int perLevel = Math.max(1, (count - made) / Math.max(1, depth - level));
            for (int i = 0; i < perLevel && made < count; i++, made++) {
                new Button(next, SWT.PUSH).setText("b" + made);
            }
            at = next;
        }
        return root;
    }

    private Widget leafButton() {
        Button b = new Button(shell, SWT.PUSH);
        b.setText("OK");
        return b;
    }

    /** Every widget in the subtree, so a flush can be given the whole thing at once. */
    private List<Widget> subtreeOf(Widget root) {
        List<Widget> all = new ArrayList<>();
        collect(root, all);
        return all;
    }

    private void collect(Widget w, List<Widget> into) {
        if (w == null || w.isDisposed()) return;
        into.add(w);
        if (w instanceof Composite c) {
            for (Control child : c.getChildren()) collect(child, into);
        }
    }

    private void report(String shape, List<Widget> widgets) {
        List<DartWidget> impls = new ArrayList<>();
        for (Widget w : widgets) {
            if (w.getImpl() instanceof DartWidget d) impls.add(d);
            // Nothing is new by the time a flush is measured: a widget the client has never seen is
            // not sent on its own channel, so leaving them new would measure the skip, not the work.
            w.setData("dev.equo.swt.new", false);
        }

        int warmup = warmup(impls.size());
        int runs = runs(impls.size());
        for (int i = 0; i < warmup; i++) flushOnce(impls);
        long best = Long.MAX_VALUE;
        long total = 0;
        for (int i = 0; i < runs; i++) {
            long t = flushOnce(impls);
            total += t;
            if (t < best) best = t;
        }
        System.out.printf("[flush] %-10s widgets=%-4d best=%6.1fus mean=%6.1fus  depth-sort-if-added=%5.1fus (%.1f%% of best)%n",
                shape, impls.size(), best / 1000.0, (total / (double) runs) / 1000.0,
                depthSortCost(impls) / 1000.0, 100.0 * depthSortCost(impls) / best);
    }

    /**
     * What ordering the flush by tree depth would cost, if it were done.
     *
     * <p>It is not: {@code FlutterBridge.flushOrder} only hoists the display's frame to the front,
     * which is the one whose position changes what the far side can place. Sorting the widgets
     * among themselves by depth as well is the complete answer, and this is what that would cost -
     * a walk up the parent chain per widget, through a getter that calls {@code checkWidget()}.
     *
     * <p>Measured rather than argued, because it runs on every turn of the event loop and the
     * number decides whether the complete version is affordable. Timed apart from the flush: the
     * flush is dominated by serializing, and on a loaded machine that swamps the difference - the
     * same build measured 445us and 1138us an hour apart.
     */
    private long depthSortCost(List<DartWidget> impls) {
        long best = Long.MAX_VALUE;
        for (int run = 0; run < 200; run++) {
            long start = System.nanoTime();
            java.util.Map<Object, Integer> depth = new java.util.IdentityHashMap<>();
            for (DartWidget d : impls) {
                int n = 0;
                for (Object parent = parentOf(d); parent != null; parent = parentOf(parent)) {
                    if (++n > 64) break;
                }
                depth.put(d, n);
            }
            List<DartWidget> ordered = new ArrayList<>(impls);
            ordered.sort(java.util.Comparator.comparingInt(depth::get));
            long elapsed = System.nanoTime() - start;
            if (elapsed < best && !ordered.isEmpty()) best = elapsed;
        }
        return best;
    }

    /** The same step FlutterBridge.getParent takes: a control's parent, nothing else's. */
    private Object parentOf(Object o) {
        if (o instanceof DartControl c && !c.isDisposed()) {
            Composite parent = c.getParent();
            return parent != null && parent.getImpl() instanceof DartWidget ? parent.getImpl() : null;
        }
        return null;
    }

    /** One flush of exactly these widgets, timed. The recording comm keeps the wire out of it. */
    private long flushOnce(List<DartWidget> impls) {
        for (DartWidget d : impls) bridge.dirty(d);
        bridge.comm.sent.clear();
        long start = System.nanoTime();
        FlutterBridge.update();
        long elapsed = System.nanoTime() - start;
        bridge.comm.sent.clear();
        return elapsed;
    }
}
