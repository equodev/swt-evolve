package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates basic drawing operations using GC (Graphics Context).
 * This example shows drawing lines, ovals, rectangles, and copying canvas areas
 * using a paint listener on a canvas widget.
 */

public class GCBasicSnippet {
    public static void main (String [] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("GCBasicSnippet");
        shell.setSize(650, 650);

        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(50, 50, 600, 600);
        canvas.addPaintListener(e -> {
            GC gc = e.gc;
            gc.drawLine(10, 10, 110, 90);
            gc.drawOval(20, 20, 80, 60);
            gc.drawRectangle(30, 30, 60, 40);
            gc.copyArea(10, 10, 100, 80, 200, 10);

            gc.copyArea(10, 10, 100, 80, 50, 150);

        });

        shell.open();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}
