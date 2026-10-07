package org.eclipse.swt.internal;

import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * The public API of the win32 {@code Win32DPIUtils}, which applications built against Windows SWT
 * call directly. Flutter renders here, so there is no process DPI awareness to change and no
 * monitor-specific scaling: what remains is the point/pixel arithmetic at the given zoom.
 */
public class Win32DPIUtils {

    public static boolean setDPIAwareness(int desiredDpiAwareness) {
        return true;
    }

    public static <T> T runWithProperDPIAwareness(Display display, Supplier<T> operation) {
        return operation.get();
    }

    public static void setMonitorSpecificScaling(boolean activate) {
    }

    public static void setAutoScaleForMonitorSpecificScaling() {
    }

    public static int getPrimaryMonitorZoomAtStartup() {
        return DPIUtil.getDeviceZoom();
    }

    public static float pointToPixel(float size, int zoom) {
        if (zoom == 100 || size == SWT.DEFAULT) return size;
        return size * zoom / 100f;
    }

    public static float pointToPixel(Drawable drawable, float size, int zoom) {
        return scalable(drawable) ? pointToPixel(size, zoom) : size;
    }

    public static int pointToPixel(Drawable drawable, int size, int zoom) {
        return scalable(drawable) ? round(pointToPixel((float) size, zoom), size) : size;
    }

    public static int[] pointToPixel(int[] pointArray, int zoom) {
        return pointToPixel(null, pointArray, zoom);
    }

    public static int[] pointToPixel(Drawable drawable, int[] pointArray, int zoom) {
        if (pointArray == null || zoom == 100 || !scalable(drawable)) return pointArray;
        int[] result = new int[pointArray.length];
        for (int i = 0; i < pointArray.length; i++) {
            result[i] = Math.round(pointArray[i] * zoom / 100f);
        }
        return result;
    }

    public static Point pointToPixelAsSize(Point point, int zoom) {
        return scale(point, zoom / 100f, false);
    }

    public static Point pointToPixelAsSize(Drawable drawable, Point point, int zoom) {
        return scalable(drawable) ? pointToPixelAsSize(point, zoom) : point;
    }

    public static Point pointToPixelAsLocation(Point point, int zoom) {
        return scale(point, zoom / 100f, false);
    }

    public static Point pointToPixelAsLocation(Drawable drawable, Point point, int zoom) {
        return scalable(drawable) ? pointToPixelAsLocation(point, zoom) : point;
    }

    public static Point pointToPixelAsSufficientlyLargeSize(Point point, int zoom) {
        return scale(point, zoom / 100f, true);
    }

    public static Rectangle pointToPixel(Rectangle rect, int zoom) {
        return scale(rect, zoom / 100f, false);
    }

    public static Rectangle pointToPixel(Drawable drawable, Rectangle rect, int zoom) {
        return scalable(drawable) ? pointToPixel(rect, zoom) : rect;
    }

    public static Rectangle pointToPixelWithSufficientlyLargeSize(Rectangle rect, int zoom) {
        return scale(rect, zoom / 100f, true);
    }

    public static float pixelToPoint(Drawable drawable, float size, int zoom) {
        if (!scalable(drawable) || zoom == 100 || size == SWT.DEFAULT) return size;
        return size * 100f / zoom;
    }

    public static int pixelToPoint(Drawable drawable, int size, int zoom) {
        return scalable(drawable) ? round(pixelToPoint(null, (float) size, zoom), size) : size;
    }

    public static float[] pixelToPoint(float[] pointArray, int zoom) {
        return pixelToPoint(null, pointArray, zoom);
    }

    public static float[] pixelToPoint(Drawable drawable, float[] pointArray, int zoom) {
        if (pointArray == null || zoom == 100 || !scalable(drawable)) return pointArray;
        float[] result = new float[pointArray.length];
        for (int i = 0; i < pointArray.length; i++) {
            result[i] = pointArray[i] * 100f / zoom;
        }
        return result;
    }

    public static Point pixelToPointAsSize(Point point, int zoom) {
        return scale(point, 100f / zoom, false);
    }

    public static Point pixelToPointAsSize(Drawable drawable, Point point, int zoom) {
        return scalable(drawable) ? pixelToPointAsSize(point, zoom) : point;
    }

    public static Point pixelToPointAsLocation(Point point, int zoom) {
        return scale(point, 100f / zoom, false);
    }

    public static Point pixelToPointAsLocation(Drawable drawable, Point point, int zoom) {
        return scalable(drawable) ? pixelToPointAsLocation(point, zoom) : point;
    }

    public static Point pixelToPointAsSufficientlyLargeSize(Point point, int zoom) {
        return scale(point, 100f / zoom, true);
    }

    public static Rectangle pixelToPoint(Rectangle rect, int zoom) {
        return scale(rect, 100f / zoom, false);
    }

    public static Rectangle pixelToPoint(Drawable drawable, Rectangle rect, int zoom) {
        return scalable(drawable) ? pixelToPoint(rect, zoom) : rect;
    }

    public static Rectangle pixelToPointWithSufficientlyLargeSize(Rectangle rect, int zoom) {
        return scale(rect, 100f / zoom, true);
    }

    public static Rectangle scaleBounds(Rectangle rect, int targetZoom, int currentZoom) {
        if (rect == null || targetZoom == currentZoom) return rect;
        return scale(rect, (float) targetZoom / currentZoom, false);
    }

    private static boolean scalable(Drawable drawable) {
        return drawable == null || drawable.isAutoScalable();
    }

    private static int round(float scaled, int original) {
        return original == SWT.DEFAULT ? original : Math.round(scaled);
    }

    private static Point scale(Point point, float factor, boolean roundUp) {
        if (point == null || factor == 1f) return point;
        return new Point(scaled(point.x, factor, roundUp), scaled(point.y, factor, roundUp));
    }

    private static Rectangle scale(Rectangle rect, float factor, boolean roundUp) {
        if (rect == null || factor == 1f) return rect;
        return new Rectangle(Math.round(rect.x * factor), Math.round(rect.y * factor),
                scaled(rect.width, factor, roundUp), scaled(rect.height, factor, roundUp));
    }

    private static int scaled(int value, float factor, boolean roundUp) {
        if (value == SWT.DEFAULT) return value;
        return roundUp ? (int) Math.ceil(value * factor) : Math.round(value * factor);
    }
}
