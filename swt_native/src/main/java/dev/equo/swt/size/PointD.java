package dev.equo.swt.size;

public record PointD(double x, double y) {
    public static final PointD zero = new PointD(0, 0);
}
