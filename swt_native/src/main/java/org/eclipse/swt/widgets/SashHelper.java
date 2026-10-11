package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

/**
 * Preferred size of a Dart-backed Sash, as the SWT of each platform computes it: the default length along
 * the bar and a fixed thickness across it. Cocoa is 5 thick and takes a hint as it comes; GTK and Windows
 * are 3 thick and take a negative hint as 0. Neither adds a border width, as a Sash has none.
 */
public class SashHelper {

    private static final int COCOA_THICKNESS = 5;

    private static final int THICKNESS = 3;

    public static Point computeSize(DartSash sash, int wHint, int hHint) {
        return computeSize(SWT.getPlatform(), sash.getApi().style, wHint, hHint);
    }

    public static Point computeSize(String platform, int style, int wHint, int hHint) {
        boolean cocoa = "cocoa".equals(platform);
        int thickness = cocoa ? COCOA_THICKNESS : THICKNESS;
        if (!cocoa) {
            if (wHint != SWT.DEFAULT && wHint < 0) {
                wHint = 0;
            }
            if (hHint != SWT.DEFAULT && hHint < 0) {
                hHint = 0;
            }
        }
        boolean horizontal = (style & SWT.HORIZONTAL) != 0;
        int width = horizontal ? DartWidget.DEFAULT_WIDTH : thickness;
        int height = horizontal ? thickness : DartWidget.DEFAULT_HEIGHT;
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }
        return new Point(width, height);
    }
}
