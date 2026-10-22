package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;

public class SashHelper {

    private SashHelper() {
    }

    /**
     * A Selection the client reports for a press, a drag or a release. The press's comes ahead of its
     * MouseDown, and natively the press has activated the Sash before either
     * (Shell.setActiveControl on the down); the Selection carries no button, so a press is told by
     * the button it holds. A Dart Sash is always SMOOTH, and native SWT's smooth sash applies its own
     * move after the listeners unless one vetoes it with doit (SwtSash mouseDragged/mouseUp).
     */
    public static void selected(DartSash sash, Event e) {
        if ((e.stateMask & SWT.BUTTON_MASK) != 0) {
            ControlHelper.sendActivateToAncestors(sash, SWT.MouseDown);
            if (sash.isDisposed())
                return;
        }
        sash.sendEvent(SWT.Selection, e);
        if (sash.isDisposed() || !e.doit)
            return;
        Rectangle bounds = sash.getBounds();
        sash.setBounds(e.x, e.y, bounds.width, bounds.height);
    }
}
