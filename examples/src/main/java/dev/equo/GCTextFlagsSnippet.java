package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates text drawing flags in GC (Graphics Context).
 * This example shows the effect of DRAW_TRANSPARENT, DRAW_DELIMITER, and DRAW_TAB flags
 * when rendering text, with side-by-side comparisons to illustrate the differences.
 */

public class GCTextFlagsSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("GCTextFlagsSnippet");
        shell.setSize(800, 800);

        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(50, 50, 750, 750);
        canvas.addPaintListener(e -> {
            GC gc = e.gc;
            gc.setForeground(new Color(0, 0, 0));
            gc.setBackground(new Color(255, 255, 255));

            // Demo 1: DRAW_TRANSPARENT flag
            gc.drawText("1. SWT.DRAW_TRANSPARENT Flag Test:", 20, 50, SWT.DRAW_TRANSPARENT);
            gc.setBackground(new Color(255, 200, 200));
            gc.fillRectangle(20, 80, 400, 80);
            
            // Transparent text (default behavior)
            gc.setForeground(new Color(0, 0, 0));
            gc.drawText("This text is TRANSPARENT (background shows through)", 30, 90, SWT.DRAW_TRANSPARENT);
            
            // Non-transparent text 
            gc.setBackground(new Color(255, 255, 255)); // White background
            gc.drawText("This text is NON-TRANSPARENT (white background)", 30, 110, 0); // No flags = opaque
            
            // Compare side by side
            gc.setBackground(new Color(200, 255, 200)); // Light green
            gc.fillRectangle(30, 130, 200, 25);
            gc.drawText("Transparent", 35, 135, SWT.DRAW_TRANSPARENT);
            
            gc.setBackground(new Color(255, 255, 255));
            gc.drawText("vs Non-transparent", 150, 135, 0);
            
            // Demo 2: DRAW_DELIMITER flag
            gc.setForeground(new Color(0, 0, 0));
            gc.drawText("2. SWT.DRAW_DELIMITER Flag Test:", 20, 180, SWT.DRAW_TRANSPARENT);
            
            // Text with various newline characters - WITH DRAW_DELIMITER
            String textWithNewlines = "Line 1\nLine 2\r\nLine 3\rLine 4";
            gc.setForeground(new Color(0, 100, 0));
            gc.drawText("WITH DRAW_DELIMITER (preserves newlines):", 30, 210, SWT.DRAW_TRANSPARENT);
            gc.drawText(textWithNewlines, 30, 230, SWT.DRAW_DELIMITER | SWT.DRAW_TRANSPARENT);
            
            // Text with various newline characters - WITHOUT DRAW_DELIMITER  
            gc.setForeground(new Color(100, 0, 0));
            gc.drawText("WITHOUT DRAW_DELIMITER (converts to spaces):", 30, 310, SWT.DRAW_TRANSPARENT);
            gc.drawText(textWithNewlines, 30, 330, SWT.DRAW_TRANSPARENT);
            
            // Demo 3: DRAW_TAB flag
            gc.setForeground(new Color(0, 0, 0)); 
            gc.drawText("3. SWT.DRAW_TAB Flag Test:", 20, 370, SWT.DRAW_TRANSPARENT);
            String textWithTabs = "Column1\tColumn2\tColumn3\tColumn4";
            
            // WITH DRAW_TAB flag
            gc.setForeground(new Color(0, 0, 100));
            gc.drawText("WITH DRAW_TAB (preserves tab spacing):", 30, 400, SWT.DRAW_TRANSPARENT);
            gc.drawText(textWithTabs, 30, 420, SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT);
            
            // WITHOUT DRAW_TAB flag
            gc.setForeground(new Color(100, 0, 0));
            gc.drawText("WITHOUT DRAW_TAB (converts to spaces):", 30, 450, SWT.DRAW_TRANSPARENT);
            gc.drawText(textWithTabs, 30, 470, SWT.DRAW_TRANSPARENT);

        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}