package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Display logic kept out of the generated implementation: coordinate mapping and the system icons.
 */
public class DisplayHelper {

    static final int ICON_SIZE = 32;

    static final int ERROR_FILL = 0xD93025;

    static final int WARNING_FILL = 0xF9AB00;

    static final int INFORMATION_FILL = 0x1A73E8;

    static final int LIGHT_INK = 0xFFFFFF;

    static final int DARK_INK = 0x202124;

    /** Display data: the OS the display's client runs on, as Flutter's {@code TargetPlatform} name, from its ClientReady. */
    private static final String CLIENT_PLATFORM = "org.eclipse.swt.internal.clientPlatform";

    private DisplayHelper() {
    }

    /** Milliseconds until the first of {@code deadlines} (System.nanoTime) is due, 0 if one is, at most {@code cap}. */
    public static long untilNextTimer(java.util.Map<Runnable, Long> deadlines, long cap) {
        long wait = cap;
        long now = System.nanoTime();
        for (long deadline : deadlines.values()) {
            long ms = Math.max(0, (deadline - now + 999_999) / 1_000_000);
            if (ms < wait) wait = ms;
        }
        return wait;
    }

    /** Display data is written on the display's thread; a ClientReady that arrives on another one is posted to it. */
    public static void clientReady(Display display, String targetPlatform) {
        if (targetPlatform == null || display.isDisposed())
            return;
        if (display.getThread() == Thread.currentThread()) {
            display.setData(CLIENT_PLATFORM, targetPlatform);
            return;
        }
        try {
            display.asyncExec(() -> {
                if (!display.isDisposed())
                    display.setData(CLIENT_PLATFORM, targetPlatform);
            });
        } catch (SWTException disposed) {
            // The display went away between the check and asyncExec.
        }
    }

    /**
     * The platform whose keyboard conventions the display's client follows, named as {@link SWT#getPlatform()}
     * names them: {@code "win32"}, {@code "gtk"} or {@code "cocoa"}. It is the OS the client runs on, which for a
     * web client is the browser's, not the server's; before the client reports it, the one this jar is built for.
     * Any other OS follows Windows.
     */
    public static String keyboardPlatform(Display display) {
        Object platform = display.getData(CLIENT_PLATFORM);
        switch (platform instanceof String ? (String) platform : SWT.getPlatform()) {
            case "macOS":
            case "iOS":
            case "cocoa":
                return "cocoa";
            case "linux":
            case "gtk":
                return "gtk";
            default:
                return "win32";
        }
    }

    static Point mapOrigin(Control from, Control to, int x, int y) {
        int screenX, screenY;
        if (from != null) {
            Point screenPt = from.toDisplay(x, y);
            screenX = screenPt.x;
            screenY = screenPt.y;
        } else {
            screenX = x;
            screenY = y;
        }
        // The control's own inverse, not a subtraction of its origin: toDisplay may scale (a panel
        // of the main workbench area), and only toControl undoes that.
        return to != null ? to.toControl(screenX, screenY) : new Point(screenX, screenY);
    }

    /** Both corners, so the size scales with the origin wherever the mapping does. */
    static Rectangle mapRectangle(Control from, Control to, int x, int y, int width, int height) {
        Point origin = mapOrigin(from, to, x, y);
        Point far = mapOrigin(from, to, x + width, y + height);
        return new Rectangle(origin.x, origin.y, far.x - origin.x, far.y - origin.y);
    }

    /**
     * The icon that answers {@code Display.getSystemImage(id)}, or 0 for an id with none. Working
     * shares the information icon, as on every platform. The question has its own, as on Win32 and
     * GTK; Cocoa, which has no question icon in its alert set, answers the information icon.
     */
    static int systemIconOf(int id) {
        switch (id) {
            case SWT.ICON_ERROR:
            case SWT.ICON_WARNING:
            case SWT.ICON_INFORMATION:
            case SWT.ICON_QUESTION:
                return id;
            case SWT.ICON_WORKING:
                return SWT.ICON_INFORMATION;
            default:
                return 0;
        }
    }

    static Image systemIcon(Display display, int icon) {
        return new Image(display, systemIconData(icon));
    }

    /**
     * The system icons, drawn here as there is no platform icon to load: a red disc with a cross
     * (error), an amber triangle with an exclamation mark (warning) and a blue disc with an "i"
     * (information) or a question mark (question), anti-aliased over a transparent square.
     */
    static ImageData systemIconData(int icon) {
        int fill, ink;
        switch (icon) {
            case SWT.ICON_ERROR:
                fill = ERROR_FILL;
                ink = LIGHT_INK;
                break;
            case SWT.ICON_WARNING:
                fill = WARNING_FILL;
                ink = DARK_INK;
                break;
            default:
                fill = INFORMATION_FILL;
                ink = LIGHT_INK;
                break;
        }
        ImageData data = new ImageData(ICON_SIZE, ICON_SIZE, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        byte[] alpha = new byte[ICON_SIZE * ICON_SIZE];
        int samples = 4;
        for (int y = 0; y < ICON_SIZE; y++) {
            for (int x = 0; x < ICON_SIZE; x++) {
                int inShape = 0, inGlyph = 0;
                for (int sy = 0; sy < samples; sy++) {
                    for (int sx = 0; sx < samples; sx++) {
                        double px = x + (sx + 0.5) / samples, py = y + (sy + 0.5) / samples;
                        if (inShape(icon, px, py)) {
                            inShape++;
                            if (inGlyph(icon, px, py))
                                inGlyph++;
                        }
                    }
                }
                int total = samples * samples;
                alpha[y * ICON_SIZE + x] = (byte) (255 * inShape / total);
                data.setPixel(x, y, inShape == 0 ? fill : blend(fill, ink, inGlyph / (double) inShape));
            }
        }
        data.alphaData = alpha;
        return data;
    }

    private static boolean inShape(int icon, double x, double y) {
        if (icon == SWT.ICON_WARNING)
            return inTriangle(x, y, 16, 2, 30.5, 29, 1.5, 29);
        return inDisc(x, y, 16, 16, 15);
    }

    private static boolean inGlyph(int icon, double x, double y) {
        switch (icon) {
            case SWT.ICON_ERROR:
                return nearSegment(x, y, 10.5, 10.5, 21.5, 21.5, 1.75) || nearSegment(x, y, 21.5, 10.5, 10.5, 21.5, 1.75);
            case SWT.ICON_WARNING:
                return nearSegment(x, y, 16, 11, 16, 20.5, 1.6) || inDisc(x, y, 16, 24.5, 1.9);
            case SWT.ICON_QUESTION:
                return onArc(x, y, 16, 12.5, 4.75, 1.6) || nearSegment(x, y, 19.4, 15.8, 16, 18.5, 1.6)
                        || nearSegment(x, y, 16, 18.5, 16, 20.5, 1.6) || inDisc(x, y, 16, 24.5, 1.9);
            default:
                return inDisc(x, y, 16, 9.5, 2) || nearSegment(x, y, 16, 14, 16, 24, 1.6);
        }
    }

    private static boolean inDisc(double x, double y, double cx, double cy, double r) {
        return (x - cx) * (x - cx) + (y - cy) * (y - cy) <= r * r;
    }

    /** The upper ring of a question mark: the circle round (cx, cy), but not its lower-left quarter. */
    private static boolean onArc(double x, double y, double cx, double cy, double r, double half) {
        double d = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
        return Math.abs(d - r) <= half && !(x < cx && y > cy);
    }

    private static boolean nearSegment(double x, double y, double x1, double y1, double x2, double y2, double half) {
        double dx = x2 - x1, dy = y2 - y1;
        double t = ((x - x1) * dx + (y - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        double nx = x1 + t * dx - x, ny = y1 + t * dy - y;
        return nx * nx + ny * ny <= half * half;
    }

    /** Whether (x, y) is inside the triangle a, b, c, given clockwise on the screen. */
    private static boolean inTriangle(double x, double y, double ax, double ay, double bx, double by, double cx, double cy) {
        double d1 = (x - bx) * (ay - by) - (ax - bx) * (y - by);
        double d2 = (x - cx) * (by - cy) - (bx - cx) * (y - cy);
        double d3 = (x - ax) * (cy - ay) - (cx - ax) * (y - ay);
        return Math.min(d1, Math.min(d2, d3)) >= 0;
    }

    private static int blend(int from, int to, double amount) {
        int r = (int) Math.round(((from >> 16) & 0xFF) * (1 - amount) + ((to >> 16) & 0xFF) * amount);
        int g = (int) Math.round(((from >> 8) & 0xFF) * (1 - amount) + ((to >> 8) & 0xFF) * amount);
        int b = (int) Math.round((from & 0xFF) * (1 - amount) + (to & 0xFF) * amount);
        return (r << 16) | (g << 8) | b;
    }
}
