package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates basic image rendering operations using GC (Graphics Context).
 * This example shows loading an image from resources and drawing it at
 * original size and scaled down
 */

public class GCImageSnippet {
    public static void main (String [] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("GCImageSnippet");
        shell.setSize(900, 720);

        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(50, 50, 800, 600);
        canvas.addPaintListener(e -> {
            GC gc = e.gc;

            Image image = new Image(display, GCImageSnippet.class.getClassLoader().getResourceAsStream("swt-evolve.png"));

            // 1. Original size (256x256)
            gc.drawImage(image, 20, 20);
            gc.drawText("Original (256x265)", 20, 5, true);

            // 2. Scaled down (128x128)
            gc.drawImage(image, 0, 0, 256, 256,
                       20, 300, 128, 128);
            gc.drawText("Scaled 0.5x (128x128)", 20, 285, true);

            image.dispose();
        });

        shell.open();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}