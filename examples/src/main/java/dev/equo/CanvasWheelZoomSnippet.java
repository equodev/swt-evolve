package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import java.util.Arrays;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A Canvas that zooms by a fixed step per MouseWheel event, using only the sign of the event's
 * count, as chart libraries do. The label counts the events and sums their counts,
 * so the number of wheel events a gesture produces can be compared across platforms.
 */
public class CanvasWheelZoomSnippet {

    static final double STEP = 1.1;

    static int events;
    static int sum;
    static int last;
    static double zoom = 1;

    /**
     * Pass {@code native} to run on the platform's own SWT, for comparison, and {@code chart} to zoom
     * a Composite inside a Composite, as a chart's plot area sits inside the chart.
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("native")) Config.defaultToEclipse();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CanvasWheelZoomSnippet");
        shell.setLayout(new GridLayout());

        Label status = new Label(shell, SWT.NONE);
        status.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Composite canvas;
        if (Arrays.asList(args).contains("chart")) {
            // A chart's layout: a chart Composite whose plot-area Composite paints and zooms.
            Composite chart = new Composite(shell, SWT.NONE);
            chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            chart.setLayout(new FillLayout());
            canvas = new Composite(chart, SWT.DOUBLE_BUFFERED);
        } else {
            canvas = new Canvas(shell, SWT.BORDER | SWT.DOUBLE_BUFFERED);
            canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        }
        canvas.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        canvas.addPaintListener(e -> {
            var size = canvas.getClientArea();
            int w = (int) Math.min(100_000, 80 * zoom);
            e.gc.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
            e.gc.fillRectangle((size.width - w) / 2, (size.height - w) / 2, w, w);
        });
        canvas.addListener(SWT.MouseWheel, e -> {
            events++;
            sum += e.count;
            last = e.count;
            zoom *= e.count > 0 ? STEP : 1 / STEP;
            String text = String.format("events=%d sum=%d last=%d zoom=%.3f", events, sum, last, zoom);
            System.out.println("MouseWheel " + text + " time=" + e.time);
            status.setText(text);
            canvas.redraw();
        });
        status.setText("events=0 sum=0 last=0 zoom=1.000");

        shell.setSize(500, 450);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
