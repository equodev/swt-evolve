package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Times the GC calls of one editor-shaped paint, so the cost of an op can be compared between the
 * native backend and this one. The op mix mirrors what a text editor issues per repaint: a
 * background fill, then a foreground change and a string per visible line, plus a rule and a
 * marker box every few lines.
 *
 * <p>Only the paint listener's own body is timed — the work a native GC does with a direct call
 * into the platform, and this backend does by serializing.
 *
 * <p>Native baseline: {@code GC_BENCH_NATIVE=true ./gradlew :examples:runExample
 * -PmainClass=dev.equo.GCOpCostSnippet}. This backend: the same class on {@code runWebExample}.
 */
public class GCOpCostSnippet {

    private static final int VISIBLE_LINES = 60;
    private static final int PAINTS = 120;

    private static final List<Long> paintMicros = new ArrayList<>();
    private static int opsPerPaint;

    public static void main(String[] args) {
        // An env var, not a system property: the example tasks fork a JVM and do not forward -D.
        boolean useNative = "true".equals(System.getenv("GC_BENCH_NATIVE"));
        if (useNative) {
            Config.useEclipse(Canvas.class);
            Config.useEclipse(Shell.class);
        }

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("GC op cost");
        shell.setLayout(new FillLayout());
        Canvas canvas = new Canvas(shell, SWT.NONE);

        canvas.addPaintListener(event -> {
            GC gc = event.gc;
            int ops = 0;
            long startedAt = System.nanoTime();

            gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
            gc.fillRectangle(0, 0, 900, 700);
            ops += 2;

            for (int line = 0; line < VISIBLE_LINES; line++) {
                int y = line * 16;
                gc.setForeground(display.getSystemColor(
                        line % 4 == 0 ? SWT.COLOR_DARK_BLUE : SWT.COLOR_LIST_FOREGROUND));
                gc.drawString("public int method" + line + "(int seed) { return seed; }", 40, y, true);
                ops += 2;
                if (line % 5 == 0) {
                    gc.drawLine(0, y, 900, y);
                    ops++;
                }
                if (line % 9 == 0) {
                    gc.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
                    gc.fillRectangle(4, y + 2, 10, 10);
                    gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                    ops += 3;
                }
            }

            paintMicros.add((System.nanoTime() - startedAt) / 1000);
            opsPerPaint = ops;
        });

        shell.setSize(900, 700);
        shell.open();

        long settleUntil = System.currentTimeMillis() + 6000;
        while (System.currentTimeMillis() < settleUntil) {
            if (!display.readAndDispatch()) display.sleep();
        }

        paintMicros.clear();
        long deadline = System.currentTimeMillis() + 60000;
        while (paintMicros.size() < PAINTS && System.currentTimeMillis() < deadline
                && !shell.isDisposed()) {
            canvas.redraw();
            long pumpUntil = System.currentTimeMillis() + 20;
            while (System.currentTimeMillis() < pumpUntil) {
                if (!display.readAndDispatch()) break;
            }
        }

        int count = paintMicros.size();
        if (count == 0) {
            System.out.println("[gc-op-cost] NO PAINTS OBSERVED — measurement invalid");
        } else {
            long total = 0;
            long[] sorted = new long[count];
            for (int i = 0; i < count; i++) {
                sorted[i] = paintMicros.get(i);
                total += sorted[i];
            }
            Arrays.sort(sorted);
            System.out.println("[gc-op-cost] backend=" + (useNative ? "native-swt" : "evolve")
                    + " paints=" + count
                    + " opsPerPaint=" + opsPerPaint
                    + " medianPaint=" + sorted[count / 2] + "us"
                    + " p95Paint=" + sorted[(int) (count * 0.95)] + "us"
                    + " meanPaint=" + (total / count) + "us"
                    + " perOp=" + (opsPerPaint > 0 ? (total / count) / opsPerPaint : 0) + "us");
        }
        System.out.println("[gc-op-cost] done");

        display.dispose();
        System.exit(0);
    }
}
