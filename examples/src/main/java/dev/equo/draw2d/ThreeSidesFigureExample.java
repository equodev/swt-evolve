package dev.equo.draw2d;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.OpenPolyline;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ThreeSidesFigureExample {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setSize(400, 300);
        shell.setText("Draw2D Three Sides Square Example");

        FigureCanvas canvas = new FigureCanvas(shell);
        canvas.setBounds(0, 0, 400, 300);

        Figure root = new Figure();

        // Figura de solo 3 lados
        Polyline triangleSquare = new OpenPolyline();
        triangleSquare.addPoint(new Point(50, 50));
        triangleSquare.addPoint(new Point(150, 50));
        triangleSquare.addPoint(new Point(150, 150));
        triangleSquare.addPoint(new Point(100, 200));
        // NOTA: no cierra la figura (no vuelve al punto inicial)

        triangleSquare.setLineWidth(2);
        triangleSquare.setForegroundColor(ColorConstants.blue);

        root.add(triangleSquare);

        canvas.setContents(root);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}