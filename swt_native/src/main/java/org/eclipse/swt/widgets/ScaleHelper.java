package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

public class ScaleHelper {

    /**
     * Page Down and Page Up move a Scale by its page increment, Home and End to its minimum and
     * maximum, as Win32 and GTK do for a key no listener vetoed, each with a Selection. The arrows are
     * the client's slider's own.
     */
    public static void stepByKey(DartScale c, Event key) {
        if ((key.stateMask & SWT.MODIFIER_MASK) != 0)
            return;
        int value;
        switch (key.keyCode) {
            case SWT.PAGE_DOWN:
                value = c.selection + c.pageIncrement;
                break;
            case SWT.PAGE_UP:
                value = c.selection - c.pageIncrement;
                break;
            case SWT.HOME:
                value = c.minimum;
                break;
            case SWT.END:
                value = c.maximum;
                break;
            default:
                return;
        }
        int before = c.selection;
        c.setSelection(value);
        if (c.isDisposed() || c.selection == before)
            return;
        c.sendSelectionEvent(SWT.Selection, new Event(), true);
    }
}
