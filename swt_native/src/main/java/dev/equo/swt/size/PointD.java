package dev.equo.swt.size;

import org.eclipse.swt.graphics.Point;

public record PointD(double x, double y) {
    public static final PointD zero = new PointD(0, 0);

    public Point toPoint() {
        return new Point((int) Math.round(x), (int) Math.round(y));
    }
}
