package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class DisplayHelper {

    /** The OS each display's client runs on, as Flutter's {@code TargetPlatform} name, from its ClientReady. */
    private static final Map<Display, String> clientPlatforms = Collections.synchronizedMap(new WeakHashMap<>());

    public static void clientReady(Display display, String targetPlatform) {
        if (targetPlatform != null)
            clientPlatforms.put(display, targetPlatform);
    }

    /**
     * The platform whose keyboard conventions the display's client follows, named as {@link SWT#getPlatform()}
     * names them: {@code "win32"}, {@code "gtk"} or {@code "cocoa"}. It is the OS the client runs on, which for a
     * web client is the browser's, not the server's; before the client reports it, the one this jar is built for.
     * Any other OS follows Windows.
     */
    static String keyboardPlatform(Display display) {
        String platform = clientPlatforms.get(display);
        switch (platform != null ? platform : SWT.getPlatform()) {
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

}
