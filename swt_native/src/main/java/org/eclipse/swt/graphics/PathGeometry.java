package org.eclipse.swt.graphics;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;

/**
 * Geometry of a handle-less Path, answered from its PathData: bounds, hit testing and flattening.
 * Curves are flattened into segments; a native backend asks its graphics library the same questions.
 */
public final class PathGeometry {

    /** Curves are cut into segments this close to them when a caller asks for no tolerance. */
    private static final float DEFAULT_FLATNESS = 0.25f;

    private PathGeometry() {
    }

    /** Each subpath as a polyline of x,y pairs, curves flattened to [flatness]. */
    static List<float[]> polylines(PathData data, float flatness) {
        List<float[]> result = new ArrayList<>();
        if (data == null || data.types == null) return result;
        float tolerance = flatness > 0 ? flatness : DEFAULT_FLATNESS;
        List<Float> current = new ArrayList<>();
        float x = 0, y = 0, startX = 0, startY = 0;
        float[] p = data.points;
        int j = 0;
        for (byte type : data.types) {
            switch (type) {
                case SWT.PATH_MOVE_TO:
                    flush(result, current);
                    x = startX = p[j++];
                    y = startY = p[j++];
                    add(current, x, y);
                    break;
                case SWT.PATH_LINE_TO:
                    if (current.isEmpty()) add(current, x, y);
                    x = p[j++];
                    y = p[j++];
                    add(current, x, y);
                    break;
                case SWT.PATH_QUAD_TO: {
                    if (current.isEmpty()) add(current, x, y);
                    float cx = p[j++], cy = p[j++], ex = p[j++], ey = p[j++];
                    int n = segments(x, y, cx, cy, cx, cy, ex, ey, tolerance);
                    for (int i = 1; i <= n; i++) {
                        float t = (float) i / n, u = 1 - t;
                        add(current, u * u * x + 2 * u * t * cx + t * t * ex, u * u * y + 2 * u * t * cy + t * t * ey);
                    }
                    x = ex;
                    y = ey;
                    break;
                }
                case SWT.PATH_CUBIC_TO: {
                    if (current.isEmpty()) add(current, x, y);
                    float c1x = p[j++], c1y = p[j++], c2x = p[j++], c2y = p[j++], ex = p[j++], ey = p[j++];
                    int n = segments(x, y, c1x, c1y, c2x, c2y, ex, ey, tolerance);
                    for (int i = 1; i <= n; i++) {
                        float t = (float) i / n, u = 1 - t;
                        float a = u * u * u, b = 3 * u * u * t, c = 3 * u * t * t, d = t * t * t;
                        add(current, a * x + b * c1x + c * c2x + d * ex, a * y + b * c1y + c * c2y + d * ey);
                    }
                    x = ex;
                    y = ey;
                    break;
                }
                case SWT.PATH_CLOSE:
                    if (!current.isEmpty()) add(current, startX, startY);
                    flush(result, current);
                    x = startX;
                    y = startY;
                    break;
            }
        }
        flush(result, current);
        return result;
    }

    /** Enough segments that none strays further than [tolerance] from the curve. */
    private static int segments(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3,
            float tolerance) {
        double dd = Math.max(Math.hypot(x0 - 2 * x1 + x2, y0 - 2 * y1 + y2), Math.hypot(x1 - 2 * x2 + x3, y1 - 2 * y2 + y3));
        int n = (int) Math.ceil(Math.sqrt(0.75 * dd / tolerance));
        return Math.max(1, Math.min(n, 256));
    }

    private static void add(List<Float> points, float x, float y) {
        points.add(x);
        points.add(y);
    }

    private static void flush(List<float[]> result, List<Float> current) {
        if (current.size() >= 2) {
            float[] a = new float[current.size()];
            for (int i = 0; i < a.length; i++) a[i] = current.get(i);
            result.add(a);
        }
        current.clear();
    }

    /** The tight bounds {x, y, width, height}, joined with [extra] when it is not null. */
    public static void bounds(PathData data, Rectangle extra, float[] out) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (float[] line : polylines(data, 0)) {
            for (int i = 0; i + 1 < line.length; i += 2) {
                minX = Math.min(minX, line[i]);
                maxX = Math.max(maxX, line[i]);
                minY = Math.min(minY, line[i + 1]);
                maxY = Math.max(maxY, line[i + 1]);
            }
        }
        if (extra != null) {
            minX = Math.min(minX, extra.x);
            minY = Math.min(minY, extra.y);
            maxX = Math.max(maxX, extra.x + extra.width);
            maxY = Math.max(maxY, extra.y + extra.height);
        }
        if (minX > maxX) {
            out[0] = out[1] = out[2] = out[3] = 0;
            return;
        }
        out[0] = minX;
        out[1] = minY;
        out[2] = maxX - minX;
        out[3] = maxY - minY;
    }

    /**
     * Whether (x, y) is inside the filled path under [fillRule], or, with [outline], within half the
     * line width of its stroke. [extra] are boxes that count as filled (the text a path holds).
     */
    public static boolean contains(PathData data, List<Rectangle> extra, float x, float y, int fillRule,
            boolean outline, float lineWidth) {
        List<float[]> lines = polylines(data, 0);
        if (outline) {
            double reach = Math.max(lineWidth, 1) / 2.0;
            for (float[] line : lines)
                for (int i = 0; i + 3 < line.length; i += 2)
                    if (distance(x, y, line[i], line[i + 1], line[i + 2], line[i + 3]) <= reach) return true;
            return false;
        }
        if (extra != null)
            for (Rectangle r : extra)
                if (x >= r.x && y >= r.y && x < r.x + r.width && y < r.y + r.height) return true;
        int winding = 0;
        int crossings = 0;
        for (float[] line : lines) {
            int n = line.length / 2;
            // Filling closes every subpath.
            for (int i = 0; i < n; i++) {
                float x0 = line[2 * i], y0 = line[2 * i + 1];
                float x1 = line[2 * ((i + 1) % n)], y1 = line[2 * ((i + 1) % n) + 1];
                if ((y0 <= y) != (y1 <= y)) {
                    float cross = x0 + (y - y0) * (x1 - x0) / (y1 - y0);
                    if (cross > x) {
                        crossings++;
                        winding += y1 > y0 ? 1 : -1;
                    }
                }
            }
        }
        return fillRule == SWT.FILL_WINDING ? winding != 0 : (crossings & 1) == 1;
    }

    private static double distance(float px, float py, float x0, float y0, float x1, float y1) {
        double dx = x1 - x0, dy = y1 - y0;
        double len2 = dx * dx + dy * dy;
        double t = len2 == 0 ? 0 : Math.max(0, Math.min(1, ((px - x0) * dx + (py - y0) * dy) / len2));
        return Math.hypot(px - (x0 + t * dx), py - (y0 + t * dy));
    }

    /** [data] with every curve replaced by line segments within [flatness] of it. */
    public static PathData flatten(PathData data, float flatness) {
        PathData flat = new PathData();
        List<float[]> lines = polylines(data, flatness);
        int types = 0, points = 0;
        for (float[] line : lines) {
            types += line.length / 2;
            points += line.length;
        }
        flat.types = new byte[types];
        flat.points = new float[points];
        int t = 0, p = 0;
        for (float[] line : lines) {
            for (int i = 0; i < line.length; i += 2) {
                flat.types[t++] = (byte) (i == 0 ? SWT.PATH_MOVE_TO : SWT.PATH_LINE_TO);
                flat.points[p++] = line[i];
                flat.points[p++] = line[i + 1];
            }
        }
        return flat;
    }
}
