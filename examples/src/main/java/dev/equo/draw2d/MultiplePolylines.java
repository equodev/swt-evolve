package dev.equo.draw2d;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.OpenPolyline;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MultiplePolylines {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setSize(600, 400);
        shell.setText("Draw2D Complex Polyline Example");

        FigureCanvas canvas = new FigureCanvas(shell);
        canvas.setBounds(0, 0, 600, 400);

        Figure root = new Figure();

        // ---------- Figura 1: Triángulo abierto ----------
        Polyline triangle = new OpenPolyline();
        triangle.addPoint(new Point(50, 50));
        triangle.addPoint(new Point(150, 50));
        triangle.addPoint(new Point(100, 150));
        triangle.setLineWidth(2);
        triangle.setForegroundColor(ColorConstants.blue);
        root.add(triangle);

        // ---------- Figura 2: Zigzag ----------
        Polyline zigzag = new OpenPolyline();
        zigzag.addPoint(new Point(200, 50));
        zigzag.addPoint(new Point(220, 80));
        zigzag.addPoint(new Point(240, 50));
        zigzag.addPoint(new Point(260, 80));
        zigzag.addPoint(new Point(280, 50));
        zigzag.setLineWidth(3);
        zigzag.setForegroundColor(ColorConstants.red);
        root.add(zigzag);

        // ---------- Figura 3: Polilínea más compleja ----------
        Polyline complex = new OpenPolyline();
        complex.addPoint(new Point(50, 200));
        complex.addPoint(new Point(100, 220));
        complex.addPoint(new Point(150, 210));
        complex.addPoint(new Point(180, 250));
        complex.addPoint(new Point(120, 280));
        complex.addPoint(new Point(60, 260));
        complex.setLineWidth(2);
        complex.setForegroundColor(ColorConstants.green);
        root.add(complex);

        // ---------- Figura 4: Línea abierta simple ----------
        Polyline simpleLine = new OpenPolyline();
        simpleLine.addPoint(new Point(300, 200));
        simpleLine.addPoint(new Point(400, 300));
        simpleLine.setLineWidth(4);
        simpleLine.setForegroundColor(ColorConstants.darkGray);
        root.add(simpleLine);

        canvas.setContents(root);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}

