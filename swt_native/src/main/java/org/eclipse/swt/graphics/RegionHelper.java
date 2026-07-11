package org.eclipse.swt.graphics;

import java.util.ArrayList;
import java.util.List;

public class RegionHelper {

    public static void add(List<int[]> rects, int x, int y, int width, int height) {
        if (width == 0 || height == 0) return;
        rects.add(new int[]{ x, y, width, height });
    }

    public static void subtract(List<int[]> rects, int x, int y, int width, int height) {
        if (width == 0 || height == 0 || rects.isEmpty()) return;
        List<int[]> result = new ArrayList<>();
        for (int[] r : rects) {
            int rx = r[0], ry = r[1], rw = r[2], rh = r[3];
            int ix = Math.max(rx, x), iy = Math.max(ry, y);
            int iw = Math.min(rx + rw, x + width) - ix;
            int ih = Math.min(ry + rh, y + height) - iy;
            if (iw <= 0 || ih <= 0) { result.add(r); continue; }
            if (iy > ry) result.add(new int[]{ rx, ry, rw, iy - ry });
            if (iy + ih < ry + rh) result.add(new int[]{ rx, iy + ih, rw, (ry + rh) - (iy + ih) });
            if (ix > rx) result.add(new int[]{ rx, iy, ix - rx, ih });
            if (ix + iw < rx + rw) result.add(new int[]{ ix + iw, iy, (rx + rw) - (ix + iw), ih });
        }
        rects.clear();
        rects.addAll(result);
    }

    public static Rectangle getBounds(List<int[]> rects) {
        if (rects.isEmpty()) return new Rectangle(0, 0, 0, 0);
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (int[] r : rects) {
            minX = Math.min(minX, r[0]);
            minY = Math.min(minY, r[1]);
            maxX = Math.max(maxX, r[0] + r[2]);
            maxY = Math.max(maxY, r[1] + r[3]);
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public static boolean contains(List<int[]> rects, int x, int y) {
        for (int[] r : rects) {
            if (x >= r[0] && x < r[0] + r[2] && y >= r[1] && y < r[1] + r[3]) return true;
        }
        return false;
    }

    public static boolean intersects(List<int[]> rects, int x, int y, int width, int height) {
        for (int[] r : rects) {
            int ix = Math.max(r[0], x), iy = Math.max(r[1], y);
            int iw = Math.min(r[0] + r[2], x + width) - ix;
            int ih = Math.min(r[1] + r[3], y + height) - iy;
            if (iw > 0 && ih > 0) return true;
        }
        return false;
    }

    public static boolean isEmpty(List<int[]> rects) {
        for (int[] r : rects) if (r[2] > 0 && r[3] > 0) return false;
        return true;
    }

    public static void intersect(List<int[]> rects, int x, int y, int width, int height) {
        List<int[]> result = new ArrayList<>();
        for (int[] r : rects) {
            int ix = Math.max(r[0], x), iy = Math.max(r[1], y);
            int iw = Math.min(r[0] + r[2], x + width) - ix;
            int ih = Math.min(r[1] + r[3], y + height) - iy;
            if (iw > 0 && ih > 0) result.add(new int[]{ ix, iy, iw, ih });
        }
        rects.clear();
        rects.addAll(result);
    }

    public static void intersectRegion(List<int[]> rects, List<int[]> other) {
        List<int[]> result = new ArrayList<>();
        for (int[] r : rects) {
            for (int[] o : other) {
                int ix = Math.max(r[0], o[0]), iy = Math.max(r[1], o[1]);
                int iw = Math.min(r[0] + r[2], o[0] + o[2]) - ix;
                int ih = Math.min(r[1] + r[3], o[1] + o[3]) - iy;
                if (iw > 0 && ih > 0) result.add(new int[]{ ix, iy, iw, ih });
            }
        }
        rects.clear();
        rects.addAll(result);
    }

    public static void addRegion(List<int[]> rects, List<int[]> other) {
        for (int[] o : other) rects.add(new int[]{ o[0], o[1], o[2], o[3] });
    }

    public static void subtractRegion(List<int[]> rects, List<int[]> other) {
        for (int[] o : other) subtract(rects, o[0], o[1], o[2], o[3]);
    }

    public static void translate(List<int[]> rects, int dx, int dy) {
        for (int[] r : rects) { r[0] += dx; r[1] += dy; }
    }

    public static void addPolygon(List<int[]> rects, int[] poly, int count) {
        int[] b = polyBounds(poly, count);
        if (b != null) add(rects, b[0], b[1], b[2], b[3]);
    }

    public static void subtractPolygon(List<int[]> rects, int[] poly, int count) {
        int[] b = polyBounds(poly, count);
        if (b != null) subtract(rects, b[0], b[1], b[2], b[3]);
    }

    private static int[] polyBounds(int[] poly, int count) {
        count = count / 2 * 2;
        if (count <= 2) return null;
        int minX = poly[0], maxX = poly[0], minY = poly[1], maxY = poly[1];
        for (int i = 2; i < count; i += 2) {
            minX = Math.min(minX, poly[i]);
            maxX = Math.max(maxX, poly[i]);
            minY = Math.min(minY, poly[i + 1]);
            maxY = Math.max(maxY, poly[i + 1]);
        }
        return new int[]{ minX, minY, maxX - minX, maxY - minY };
    }
}