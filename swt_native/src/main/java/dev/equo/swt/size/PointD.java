package dev.equo.swt.size;

import org.eclipse.swt.graphics.Point;

public final class PointD {
    private final double x;
    private final double y;

    public PointD(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() { return x; }
    public double y() { return y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointD)) return false;
        PointD other = (PointD) o;
        return Double.compare(x, other.x) == 0
            && Double.compare(y, other.y) == 0;
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(x, y); }

    @Override
    public String toString() { return "PointD[x=" + x + ", y=" + y + "]"; }

    public static final PointD zero = new PointD(0, 0);

    public Point toPoint() {
        return new Point((int) Math.round(x), (int) Math.round(y));
    }
}
