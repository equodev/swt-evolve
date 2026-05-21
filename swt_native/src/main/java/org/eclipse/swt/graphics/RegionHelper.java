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
}