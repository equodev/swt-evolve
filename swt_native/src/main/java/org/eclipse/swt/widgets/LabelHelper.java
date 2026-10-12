package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

public final class LabelHelper {

    private LabelHelper() {
    }

    /**
     * What {@link Label#getAlignment()} answers for these style bits on the given platform's SWT. A
     * label given several alignment bits keeps them all; Win32 and GTK read LEFT first, Cocoa CENTER.
     * A separator has no alignment: 0 on Win32 and GTK, LEFT on Cocoa.
     */
    public static int alignmentOf(int style, String platform) {
        boolean cocoa = "cocoa".equals(platform);
        if ((style & SWT.SEPARATOR) != 0) return cocoa ? SWT.LEFT : 0;
        if (!cocoa && (style & SWT.LEFT) != 0) return SWT.LEFT;
        if ((style & SWT.CENTER) != 0) return SWT.CENTER;
        if ((style & SWT.RIGHT) != 0) return SWT.RIGHT;
        return SWT.LEFT;
    }
}
