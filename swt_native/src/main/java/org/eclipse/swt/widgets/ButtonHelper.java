package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

/** The behaviour of a {@link Button} that is kept out of the generated implementation. */
public final class ButtonHelper {

    private ButtonHelper() {
    }

    /**
     * The shell's default button after {@code button} gets {@code type}, {@link SWT#FocusIn} or
     * {@link SWT#FocusOut}. A push button holding the focus is the default, as on Win32 and GTK; when the
     * focus leaves it, {@code setDefault}, the default set with {@link Decorations#setDefaultButton(Button)},
     * comes back.
     */
    public static Button defaultAfterFocus(Button button, int type, Button current, Button setDefault) {
        if ((button.style & SWT.PUSH) == 0)
            return current;
        if (type == SWT.FocusIn)
            return button;
        return current == button ? setDefault : current;
    }
}
