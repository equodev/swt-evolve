package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates filled shapes and gradients using GC (Graphics Context).
 * This example renders rectangles, ovals, polygons, and gradient effects,
 * including a composite landscape scene combining multiple drawing operations.
 */

public class GCShapesGradientSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("GCShapesGradientSnippet");
        shell.setSize(850, 850);

        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(50, 50, 800, 800);
        canvas.addPaintListener(e -> {
            GC gc = e.gc;
            gc.setForeground(new Color(0, 0, 0));
            
            // Demo 1: Basic filled shapes
            gc.drawText("Basic Filled Shapes:", 50, 30, true);
            
            // Filled rectangle
            gc.setBackground(new Color(255, 100, 100));
            gc.fillRectangle(50, 60, 80, 60);
            gc.drawText("Rectangle", 50, 130, true);
            
            // Filled oval
            gc.setBackground(new Color(100, 255, 100));
            gc.fillOval(150, 60, 80, 60);
            gc.drawText("Oval", 150, 130, true);
            
            // Filled round rectangle
            gc.setBackground(new Color(100, 100, 255));
            gc.fillRoundRectangle(250, 60, 80, 60, 20, 20);
            gc.drawText("Round Rect", 250, 130, true);
            
            // Filled arc
            gc.setBackground(new Color(255, 255, 100));
            gc.fillArc(350, 60, 80, 60, 45, 180);
            gc.drawText("Arc (45°-225°)", 350, 130, true);
            
            // Demo 2: Filled polygons
            gc.drawText("Filled Polygons:", 50, 170, true);
            
            // Triangle
            gc.setBackground(new Color(255, 0, 255));
            int[] triangle = {50, 250, 100, 200, 150, 250};
            gc.fillPolygon(triangle);
            gc.drawText("Triangle", 75, 260, true);
            
            // Diamond
            gc.setBackground(new Color(0, 255, 255));
            int[] diamond = {225, 200, 250, 225, 225, 250, 200, 225};
            gc.fillPolygon(diamond);
            gc.drawText("Diamond", 200, 260, true);
            
            // Star (approximation)
            gc.setBackground(new Color(255, 165, 0));
            int[] star = {
                300, 200,   // top
                310, 220,   // right inner
                330, 220,   // right outer
                315, 235,   // right bottom inner
                325, 255,   // bottom right
                300, 245,   // bottom inner
                275, 255,   // bottom left
                285, 235,   // left bottom inner
                270, 220,   // left outer
                290, 220    // left inner
            };
            gc.fillPolygon(star);
            gc.drawText("Star", 290, 260, true);
            
            // Demo 3: Horizontal gradients
            gc.drawText("Horizontal Gradients:", 50, 300, true);
            
            gc.setForeground(new Color(255, 0, 0));  // Start color (red)
            gc.setBackground(new Color(255, 255, 0)); // End color (yellow)
            gc.fillGradientRectangle(50, 330, 150, 40, false); // Horizontal
            gc.setForeground(new Color(0, 0, 0));
            gc.drawText("Red to Yellow", 50, 380, true);
            
            gc.setForeground(new Color(0, 0, 255));    // Start color (blue)
            gc.setBackground(new Color(255, 255, 255)); // End color (white)
            gc.fillGradientRectangle(220, 330, 150, 40, false); // Horizontal
            gc.drawText("Blue to White", 220, 380, true);
            
            gc.setForeground(new Color(0, 255, 0));   // Start color (green)
            gc.setBackground(new Color(0, 0, 0));     // End color (black)
            gc.fillGradientRectangle(390, 330, 150, 40, false); // Horizontal
            gc.drawText("Green to Black", 390, 380, true);
            
            // Demo 4: Vertical gradients
            gc.drawText("Vertical Gradients:", 50, 410, true);
            
            gc.setForeground(new Color(255, 0, 255));  // Magenta
            gc.setBackground(new Color(0, 255, 255));  // Cyan
            gc.fillGradientRectangle(50, 440, 60, 80, true); // Vertical
            gc.drawText("Magenta", 50, 525, true);
            gc.drawText("to Cyan", 50, 540, true);
            
            gc.setForeground(new Color(255, 165, 0));  // Orange
            gc.setBackground(new Color(128, 0, 128));  // Purple
            gc.fillGradientRectangle(130, 440, 60, 80, true); // Vertical
            gc.drawText("Orange", 130, 525, true);
            gc.drawText("to Purple", 130, 540, true);
            
            gc.setForeground(new Color(255, 255, 255)); // White
            gc.setBackground(new Color(100, 100, 100)); // Gray
            gc.fillGradientRectangle(210, 440, 60, 80, true); // Vertical
            gc.drawText("White", 210, 525, true);
            gc.drawText("to Gray", 210, 540, true);
            
            // Demo 5: Mixed shapes with gradients and fills
            gc.drawText("Complex Scene:", 350, 410, true);
            
            // Gradient background
            gc.setForeground(new Color(135, 206, 235)); // Sky blue
            gc.setBackground(new Color(255, 255, 255)); // White
            gc.fillGradientRectangle(350, 440, 200, 100, true);
            
            // Sun (filled circle)
            gc.setBackground(new Color(255, 255, 0));
            gc.fillOval(450, 450, 30, 30);
            
            // Mountains (filled triangles)
            gc.setBackground(new Color(139, 69, 19)); // Brown
            int[] mountain1 = {350, 540, 380, 480, 410, 540};
            gc.fillPolygon(mountain1);
            
            int[] mountain2 = {390, 540, 430, 470, 470, 540};
            gc.fillPolygon(mountain2);
            
            // Trees (filled shapes)
            gc.setBackground(new Color(34, 139, 34)); // Forest green
            gc.fillOval(500, 510, 20, 20); // Tree crown
            gc.setBackground(new Color(139, 69, 19)); // Brown
            gc.fillRectangle(507, 525, 6, 15); // Tree trunk
            
            gc.setBackground(new Color(34, 139, 34));
            gc.fillOval(525, 505, 25, 25); // Larger tree crown
            gc.setBackground(new Color(139, 69, 19));
            gc.fillRectangle(535, 525, 6, 15); // Tree trunk
            
            gc.drawText("Landscape scene", 350, 550, true);
            gc.drawText("with gradients", 350, 565, true);
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}