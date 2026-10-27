package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a paragraph on a {@link Canvas} the way {@code org.eclipse.ui.forms}' {@code TextSegment}
 * does — measuring each candidate with {@code GC.textExtent} against the canvas's own client width —
 * and prints where the breaks land.
 *
 * <p>A {@code FormText} is a Canvas doing exactly this. Native SWT breaks such a paragraph in the
 * same place at every {@code swt.autoScale} value, because the flag scales widget coordinates and
 * images and never reaches inside a {@code paint()}. Measuring the same thing here says whether
 * Evolve does too.
 *
 * <p><b>Run it:</b>
 * <pre>
 * ./gradlew :examples:runDeskExample -PmainClass=dev.equo.WrapCanvasSnippet \
 *     -Dswt.autoScale.updateOnRuntime=false -Dswt.autoScale=200
 * </pre>
 * and compare the printed line count and break points against the same command without the flag.
 * An explicit percentage needs monitor-specific scaling off — that is what rejects a mode it cannot
 * honour.
 */
public class WrapCanvasSnippet {

    private static final String PARAGRAPH =
            "This guide provides you with a dynamic listing of workflows for the currently open "
            + "perspective. The following workflows will help you get started using the modelling "
            + "tools of this release.";

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Wrap canvas");
        shell.setLayout(new FillLayout());

        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.addPaintListener(e -> paintWrapped(e, canvas));

        shell.setSize(420, 240);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void paintWrapped(PaintEvent e, Canvas canvas) {
        Rectangle area = canvas.getClientArea();
        List<String> lines = wrap(e, area.width);

        int y = 4;
        for (String line : lines) {
            e.gc.drawText(line, 4, y, true);
            y += e.gc.textExtent(line).y;
        }

        // One report per paint, so a resize or a zoom change shows its own.
        System.out.println("[wrap] clientWidth=" + area.width
                + " lines=" + lines.size()
                + " fullExtent=" + e.gc.textExtent(PARAGRAPH).x
                + " fontHeight=" + e.gc.getFont().getFontData()[0].getHeight()
                + " deviceZoom=" + org.eclipse.swt.internal.DPIUtil.getDeviceZoom());
        for (String line : lines) {
            System.out.println("[wrap]   " + e.gc.textExtent(line).x + "px  \"" + line + "\"");
        }
    }

    /** Greedy word wrap, measuring every candidate the way a Forms text segment does. */
    private static List<String> wrap(PaintEvent e, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : PARAGRAPH.split(" ")) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            Point extent = e.gc.textExtent(candidate);
            // No margin: a margin expressed in SWT units is a different number of pixels at each
            // zoom, which would move the break on its own and mask what is being measured here.
            if (extent.x > width && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }
}
