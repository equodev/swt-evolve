package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

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

    /**
     * Shows the label's image, or its text. Flutter is sent both, the one not on show as empty, so a
     * switch marks each that now reads otherwise: an empty text hidden behind an image is still empty.
     */
    static void show(DartLabel label, Runnable flip) {
        String wasText = label._displayedText();
        Image wasImage = label._displayedImage();
        flip.run();
        if (!java.util.Objects.equals(wasText, label._displayedText())) label.getValue().markDirty(VLabel.TEXT);
        if (!java.util.Objects.equals(wasImage, label._displayedImage())) label.getValue().markDirty(VLabel.IMAGE);
    }
}
