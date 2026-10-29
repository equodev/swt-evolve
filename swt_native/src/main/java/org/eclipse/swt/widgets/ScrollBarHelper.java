package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Where Flutter draws a {@link ScrollBar} of a Canvas or Composite and its thumb, in the coordinates of the
 * bar's parent: the bar runs along the far edge of the parent's client area, and the thumb follows the
 * arithmetic of {@code _CanvasScrollBarState} in {@code canvas_evolve.dart}.
 */
public final class ScrollBarHelper {

    /** The length a thumb is never drawn below, so it stays grabbable. */
    static final double MIN_THUMB = 16;

    private ScrollBarHelper() {
    }

    public static Rectangle trackBounds(DartScrollBar bar) {
        Rectangle client = bar.getParent().getClientArea();
        Point size = bar.getSize();
        if ((bar.getApi().style & SWT.HORIZONTAL) != 0)
            return new Rectangle(client.x, client.y + client.height, size.x, size.y);
        return new Rectangle(client.x + client.width, client.y, size.x, size.y);
    }

    public static Rectangle thumbBounds(DartScrollBar bar) {
        Rectangle track = trackBounds(bar);
        boolean horizontal = (bar.getApi().style & SWT.HORIZONTAL) != 0;
        int length = horizontal ? track.width : track.height;
        int minimum = bar.getMinimum(), thumb = bar.getThumb();
        int range = bar.getMaximum() - minimum;
        double size = range <= 0 ? Math.max(0, length) : confine(length * ((double) thumb / range), MIN_THUMB, length);
        int scrollRange = range - thumb;
        double offset = scrollRange <= 0 ? 0
                : confine((double) (bar.getSelection() - minimum) / scrollRange * (length - size), 0, length - size);
        int start = (int) Math.round(offset);
        int extent = (int) Math.round(size);
        if (horizontal)
            return new Rectangle(track.x + start, track.y, extent, track.height);
        return new Rectangle(track.x, track.y + start, track.width, extent);
    }

    /** {@code value} held between {@code lo} and {@code hi}; a track shorter than {@code lo} is filled. */
    static double confine(double value, double lo, double hi) {
        if (hi <= 0)
            return 0;
        if (hi <= lo)
            return hi;
        return Math.max(lo, Math.min(hi, value));
    }
}
