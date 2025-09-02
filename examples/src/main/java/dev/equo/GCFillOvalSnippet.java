package dev.equo;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates fillOval operations using GC (Graphics Context).
 * This example shows filled ovals of different sizes, colors, aspect ratios,
 * overlapping shapes, and handling of negative dimensions.
 */

public class GCFillOvalSnippet {

    public static void main (String [] args) {
        final Display display = new Display ();
        final Shell shell = new Shell (display, SWT.SHELL_TRIM);
        shell.setText("GCFillOvalSnippet");
        shell.setSize (650, 650);

        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(50, 50, 600, 600);
        canvas.addPaintListener(e -> {
            GC gc = e.gc;
            // Demo 1: Basic filled ovals with different colors
            gc.setBackground(new Color(255, 0, 0)); // Red
            gc.fillOval(50, 50, 100, 80);

            gc.setBackground(new Color(0, 255, 0)); // Green
            gc.fillOval(200, 50, 120, 120); // Circle

            gc.setBackground(new Color(0, 0, 255)); // Blue
            gc.fillOval(350, 50, 80, 120); // Tall oval

            // Demo 2: Filled oval with outline
            gc.setBackground(new Color(255, 255, 0)); // Yellow
            gc.setForeground(new Color(0, 0, 0)); // Black
            gc.fillOval(50, 200, 150, 100);
            gc.drawOval(50, 200, 150, 100); // Add black outline

            // Demo 3: Overlapping filled ovals
            gc.setBackground(new Color(0, 255, 255)); // Cyan
            gc.fillOval(250, 200, 100, 100);

            gc.setBackground(new Color(255, 0, 255)); // Magenta
            gc.fillOval(280, 230, 100, 100);

            // Demo 4: Negative width/height handling
            gc.setBackground(new Color(0, 128, 0)); // Dark Green
            gc.fillOval(450, 250, -80, -60); // Should draw at (370, 190)

            // Add labels
            gc.setForeground(new Color(0, 0, 0)); // Black
            gc.drawText("Red Oval", 50, 140, true);
            gc.drawText("Green Circle", 200, 180, true);
            gc.drawText("Blue Tall", 350, 180, true);
            gc.drawText("Yellow + Outline", 50, 310, true);
            gc.drawText("Overlapping", 250, 310, true);
            gc.drawText("Negative coords", 370, 260, true);
        });

        shell.setSize (650, 650);
        shell.open ();

        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ())
                display.sleep ();
        }
        display.dispose ();
    }
}