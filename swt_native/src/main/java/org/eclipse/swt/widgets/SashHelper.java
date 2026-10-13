package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

/**
 * Preferred size of a Dart-backed Sash: the default length along the bar and a fixed thickness across it, the
 * one the theme draws. A negative hint other than {@link SWT#DEFAULT} counts as 0, as on GTK and Windows. No
 * border width is added, as a Sash has none.
 */
public class SashHelper {

    private static final int THICKNESS = 5;

    public static Point computeSize(DartSash sash, int wHint, int hHint) {
        return computeSize(sash.getApi().style, wHint, hHint);
    }

    static Point computeSize(int style, int wHint, int hHint) {
        if (wHint != SWT.DEFAULT && wHint < 0) {
            wHint = 0;
        }
        if (hHint != SWT.DEFAULT && hHint < 0) {
            hHint = 0;
        }
        boolean horizontal = (style & SWT.HORIZONTAL) != 0;
        int width = horizontal ? DartWidget.DEFAULT_WIDTH : THICKNESS;
        int height = horizontal ? THICKNESS : DartWidget.DEFAULT_HEIGHT;
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }
        return new Point(width, height);
    }
}
