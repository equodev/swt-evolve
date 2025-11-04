package org.eclipse.swt.graphics;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;

public class OpenPolyline extends Polyline {

    @Override
    protected void outlineShape(Graphics g) {
        PointList pts = getPoints();

        if (pts == null || pts.size() < 2) {
            return;
        }

        for (int i = 0; i < pts.size() - 1; i++) {
            Point p1 = pts.getPoint(i);
            Point p2 = pts.getPoint(i + 1);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }
}
