package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

public final class LabelHelper {

    private LabelHelper() {
    }

    /**
     * What {@link Label#getAlignment()} answers for these style bits. A separator has no alignment, so
     * NONE, as the javadoc says. A label given several alignment bits keeps them all and is read LEFT
     * first, then CENTER, then RIGHT, as Win32 and GTK do (Cocoa reads CENTER first).
     */
    public static int alignmentOf(int style) {
        if ((style & SWT.SEPARATOR) != 0) return SWT.NONE;
        if ((style & SWT.LEFT) != 0) return SWT.LEFT;
        if ((style & SWT.CENTER) != 0) return SWT.CENTER;
        if ((style & SWT.RIGHT) != 0) return SWT.RIGHT;
        return SWT.LEFT;
    }
}
