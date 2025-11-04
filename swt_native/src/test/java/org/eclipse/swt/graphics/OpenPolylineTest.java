package org.eclipse.swt.graphics;

import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.Graphics;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OpenPolylineTest {

    @Test
    void should_not_draw_when_points_are_null() {
        Graphics g = Mockito.mock(Graphics.class);
        OpenPolyline polyline = new OpenPolyline();

        polyline.outlineShape(g);

        Mockito.verify(g, Mockito.never()).drawLine(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void should_not_draw_when_less_than_two_points() {
        Graphics g = Mockito.mock(Graphics.class);
        OpenPolyline polyline = new OpenPolyline();

        PointList points = new PointList();
        points.addPoint(new Point(10, 10));
        polyline.setPoints(points);

        polyline.outlineShape(g);

        Mockito.verify(g, Mockito.never()).drawLine(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void should_draw_lines_between_consecutive_points() {
        Graphics g = Mockito.mock(Graphics.class);
        OpenPolyline polyline = new OpenPolyline();

        PointList points = new PointList();
        points.addPoint(new Point(0, 0));
        points.addPoint(new Point(10, 10));
        points.addPoint(new Point(20, 5));
        polyline.setPoints(points);

        polyline.outlineShape(g);

        Mockito.verify(g).drawLine(0, 0, 10, 10);
        Mockito.verify(g).drawLine(10, 10, 20, 5);

        Mockito.verifyNoMoreInteractions(g);
    }
}
