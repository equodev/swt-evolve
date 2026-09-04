package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Draws the same panel — rectangle, oval, line, polyline, text and an image — four times:
 * once untransformed, then through a translating, a scaling and a rotating GC transform.
 * Each panel has to move as one piece; geometry and label drifting apart is the failure.
 */
public class GCTransformSnippet {

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("GCTransformSnippet");
        shell.setSize(880, 480);

        Image image = new Image(display,
                GCTransformSnippet.class.getClassLoader().getResourceAsStream("swt-evolve.png"));

        // The GC reads the matrix off its Transform after the paint returns, so these have to
        // outlive the paint listener.
        Transform translated = new Transform(display);
        translated.translate(250, 30);

        Transform scaled = new Transform(display);
        scaled.translate(20, 210);
        scaled.scale(1.35f, 1.35f);

        Transform rotated = new Transform(display);
        rotated.translate(620, 270);
        rotated.rotate(-20);

        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 880, 480);
        canvas.addPaintListener(e -> {
            GC gc = e.gc;
            Color ink = new Color(30, 30, 30);
            Color fill = new Color(214, 228, 245);

            panel(gc, ink, fill, image, 20, 30, "no transform");

            gc.setTransform(translated);
            panel(gc, ink, fill, image, 0, 0, "translate");
            gc.setTransform(null);

            gc.setTransform(scaled);
            panel(gc, ink, fill, image, 0, 0, "translate + scale 1.35x");
            gc.setTransform(null);

            gc.setTransform(rotated);
            panel(gc, ink, fill, image, 0, 0, "translate + rotate -20");
            gc.setTransform(null);
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void panel(GC gc, Color ink, Color fill, Image image, int x, int y, String title) {
        gc.setForeground(ink);
        gc.setBackground(fill);
        gc.fillRectangle(x, y, 200, 150);
        gc.drawRectangle(x, y, 200, 150);
        gc.drawText(title, x + 12, y + 8, true);
        gc.drawOval(x + 12, y + 34, 60, 34);
        gc.drawLine(x + 12, y + 78, x + 188, y + 78);
        gc.drawPolyline(new int[]{
                x + 12, y + 140, x + 60, y + 100, x + 108, y + 140, x + 156, y + 100});
        gc.drawImage(image, 0, 0, 256, 256, x + 142, y + 92, 46, 46);
    }
}
