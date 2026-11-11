package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GCHelper;
import org.eclipse.swt.graphics.Rectangle;

/**
 * The tooltip a CLabel shows. Upstream decides it while painting: a text too wide for the label is
 * drawn shortened and shown in full as the tooltip, unless the application set one of its own. Flutter
 * draws the label, so the decision is taken whenever its inputs change: the label's size, the
 * application's tooltip, and anything that redraws it (text, image, font, margins).
 */
public final class CLabelHelper {

    private CLabelHelper() {
    }

    /** Re-decides the tooltip on every resize. */
    public static void install(DartCLabel label) {
        label.addListener(SWT.Resize, e -> updateShownToolTip(label));
    }

    /** CLabel.onPaint's tooltip decision: the full text when it is shortened, else the application's. */
    public static void updateShownToolTip(DartCLabel label) {
        Rectangle rect = label.getClientArea();
        if (rect.width <= 0 || rect.height <= 0) return;
        int availableWidth = Math.max(0, rect.width - (label.leftMargin + label.rightMargin));
        boolean shortened = textWidth(label) > availableWidth;
        label._setShownToolTip(shortened && label.appToolTipText == null ? label.text : label.appToolTipText);
    }

    /** The width of the text alone: upstream drops the image before it shortens the text. */
    private static int textWidth(DartCLabel label) {
        if (label.text == null || label.text.isEmpty()) return 0;
        return GCHelper.textExtent(label.text, DartCLabel._drawFlags(), label.getFont()).x;
    }
}
