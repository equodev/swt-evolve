package org.eclipse.swt.custom;

import dev.equo.swt.size.CLabelSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;

/**
 * Logic of {@code DartCLabel} kept out of the generated implementation: its size follows the CLabel's
 * margins, and the default text shortening, which the generated code cannot keep because it draws with a GC.
 */
public class CLabelHelper {

    private static final String ELLIPSIS = "...";

    private static final int DRAW_FLAGS = SWT.DRAW_MNEMONIC | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER;

    /** The margin on each side of the content that {@link CLabelSizes} is measured with. */
    private static final int MEASURED_MARGIN = 3;

    private CLabelHelper() {
    }

    /** The measured size, with the margins the CLabel has in place of the measured ones; a hint stands as given. */
    public static Point computeSize(DartCLabel label, int wHint, int hHint, boolean changed) {
        Point size = CLabelSizes.computeSize(label, wHint, hHint, changed);
        int width = wHint != SWT.DEFAULT ? size.x
                : Math.max(0, size.x + label.getLeftMargin() + label.getRightMargin() - 2 * MEASURED_MARGIN);
        int height = hHint != SWT.DEFAULT ? size.y
                : Math.max(0, size.y + label.getTopMargin() + label.getBottomMargin() - 2 * MEASURED_MARGIN);
        return new Point(width, height);
    }

    /** Replaces the middle of {@code text} with an ellipsis so that it fits {@code width}. */
    public static String shortenText(Display display, GC gc, String text, int width) {
        if (text == null)
            return null;
        int ellipsis = gc.textExtent(ELLIPSIS, DRAW_FLAGS).x;
        if (width <= ellipsis)
            return text;
        int length = text.length();
        int max = length / 2;
        int min = 0;
        int mid = (max + min) / 2 - 1;
        if (mid <= 0)
            return text;
        TextLayout layout = new TextLayout(display);
        try {
            layout.setText(text);
            mid = validOffset(layout, mid);
            while (min < mid && mid < max) {
                String head = text.substring(0, mid);
                String tail = text.substring(validOffset(layout, length - mid), length);
                int shortened = gc.textExtent(head, DRAW_FLAGS).x + ellipsis + gc.textExtent(tail, DRAW_FLAGS).x;
                if (shortened > width) {
                    max = mid;
                    mid = validOffset(layout, (max + min) / 2);
                } else if (shortened < width) {
                    min = mid;
                    mid = validOffset(layout, (max + min) / 2);
                } else {
                    min = max;
                }
            }
            return mid == 0 ? text : text.substring(0, mid) + ELLIPSIS + text.substring(validOffset(layout, length - mid), length);
        } finally {
            layout.dispose();
        }
    }

    private static int validOffset(TextLayout layout, int offset) {
        int next = layout.getNextOffset(offset, SWT.MOVEMENT_CLUSTER);
        return next != offset ? layout.getPreviousOffset(next, SWT.MOVEMENT_CLUSTER) : offset;
    }
}
