package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.Arrays;

/**
 * Times the repaint of a double-buffered ruler next to a scrolling StyledText — the shape a text
 * editor's line-number, annotation and overview rulers all use: draw into an off-screen Image
 * through a GC, dispose the GC, then blit the Image into the paint GC.
 *
 * <p>Each iteration is one full synchronous repaint on the UI thread, so the printed per-repaint
 * milliseconds are exactly what a scroll frame pays. Run it as:
 * {@code ./gradlew :examples:runWebExample -PmainClass=dev.equo.RulerRepaintSnippet}
 */
public class RulerRepaintSnippet {

    private static final int LINES = 600;
    private static final int REPAINTS = 200;

    private static Image buffer;
    // Written only on the UI thread, from inside the paint listener.
    private static final java.util.List<Long> disposeMicros = new java.util.ArrayList<>();

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Ruler repaint timing");
        shell.setLayout(new FillLayout());

        Composite root = new Composite(shell, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 0;
        root.setLayout(layout);

        Canvas ruler = new Canvas(root, SWT.NO_BACKGROUND);
        GridData rulerData = new GridData(SWT.FILL, SWT.FILL, false, true);
        rulerData.widthHint = 48;
        ruler.setLayoutData(rulerData);

        StyledText text = new StyledText(root, SWT.V_SCROLL | SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= LINES; i++) {
            content.append("line ").append(i).append(" of the scroll target\n");
        }
        text.setText(content.toString());

        // The double-buffer the rulers use: draw into an Image through a GC, dispose the GC, blit.
        ruler.addPaintListener(event -> {
            Rectangle size = ruler.getClientArea();
            if (size.width <= 0 || size.height <= 0) {
                return;
            }
            if (buffer == null || buffer.getBounds().width != size.width
                    || buffer.getBounds().height != size.height) {
                if (buffer != null) {
                    buffer.dispose();
                }
                buffer = new Image(ruler.getDisplay(), size.width, size.height);
            }
            GC bufferGC = new GC(buffer);
            try {
                bufferGC.setBackground(ruler.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
                bufferGC.fillRectangle(0, 0, size.width, size.height);
                int top = text.getTopIndex();
                for (int row = 0; row < size.height / 16 + 1; row++) {
                    bufferGC.drawString(Integer.toString(top + row + 1), 4, row * 16, true);
                }
            } finally {
                // Disposing a GC opened on an Image is what triggers the off-screen render, and
                // where a barrier would park this thread.
                long startedAt = System.nanoTime();
                bufferGC.dispose();
                disposeMicros.add((System.nanoTime() - startedAt) / 1000);
            }
            event.gc.drawImage(buffer, 0, 0);
        });

        shell.setSize(new Point(900, 600));
        shell.open();

        // Let the Flutter side come up and paint once before timing anything.
        long settleUntil = System.currentTimeMillis() + 6000;
        while (System.currentTimeMillis() < settleUntil) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        // Paints are requested by the Flutter side, so pump until enough have run rather than
        // assuming redraw() paints synchronously.
        disposeMicros.clear();
        long scrollDeadline = System.currentTimeMillis() + 60000;
        int step = 0;
        while (disposeMicros.size() < REPAINTS && System.currentTimeMillis() < scrollDeadline) {
            text.setTopIndex(step++ % (LINES - 20));
            ruler.redraw();
            long pumpUntil = System.currentTimeMillis() + 20;
            while (System.currentTimeMillis() < pumpUntil) {
                if (!display.readAndDispatch()) {
                    break;
                }
            }
        }

        int count = disposeMicros.size();
        if (count == 0) {
            System.out.println("[ruler-repaint] NO PAINTS OBSERVED — measurement invalid");
        } else {
            long total = 0;
            for (long sample : disposeMicros) {
                total += sample;
            }
            long[] sorted = new long[count];
            for (int i = 0; i < count; i++) {
                sorted[i] = disposeMicros.get(i);
            }
            Arrays.sort(sorted);
            System.out.println("[ruler-repaint] gcDisposes=" + count
                    + " total=" + (total / 1000) + "ms"
                    + " mean=" + (total / count) + "us"
                    + " median=" + sorted[count / 2] + "us"
                    + " p95=" + sorted[(int) (count * 0.95)] + "us"
                    + " max=" + sorted[count - 1] + "us");
        }
        System.out.println("[ruler-repaint] done");

        if (buffer != null) {
            buffer.dispose();
        }
        display.dispose();
        System.exit(0);
    }
}
