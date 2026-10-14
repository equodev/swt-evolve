package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

public final class LabelHelper {

    private LabelHelper() {
    }

    /**
     * What {@link Label#getAlignment()} answers for a label's style bits, which always carry at least one
     * of LEFT, CENTER and RIGHT. A separator has no alignment, so NONE, as the javadoc says. A label given
     * several alignment bits keeps them all and is read LEFT first, then CENTER, then RIGHT, as Win32 and
     * GTK do (Cocoa reads CENTER first).
     */
    public static int alignmentOf(int style) {
        if ((style & SWT.SEPARATOR) != 0) return SWT.NONE;
        if ((style & SWT.LEFT) != 0) return SWT.LEFT;
        return (style & SWT.CENTER) != 0 ? SWT.CENTER : SWT.RIGHT;
    }
}
