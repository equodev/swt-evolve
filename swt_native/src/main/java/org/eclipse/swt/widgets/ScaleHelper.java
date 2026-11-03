package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;

public class ScaleHelper {

    private ScaleHelper() {
    }

    /**
     * A user change to a Scale, which notifies its listeners; setSelection(int) alone is the
     * programmatic setter, which does not. The event carries the button still held.
     */
    public static void selected(DartScale c, Event e) {
        c.setSelection(e.index);
        Event event = new Event();
        event.stateMask = e.stateMask;
        c.sendSelectionEvent(SWT.Selection, event, true);
    }

    /**
     * The arrows move a Scale by its increment, Page Up and Page Down by its page increment, Home and
     * End to its minimum and maximum, as Win32 and GTK do for a key no listener vetoed, each with a
     * Selection. Right, Up and Page Up raise a Scale, as on Cocoa, whose vertical Scale has its
     * maximum at the top; on a vertical Scale with its minimum at the top
     * ({@code scale_vertical_minimum_at_top}, as on GTK and Win32) Up and Page Up lower it.
     */
    public static void stepByKey(DartScale c, Event key) {
        if ((key.stateMask & SWT.MODIFIER_MASK) != 0)
            return;
        boolean minimumAtTop = (c.getApi().getStyle() & SWT.VERTICAL) != 0
                && Config.getConfigFlags().scale_vertical_minimum_at_top;
        int up = minimumAtTop ? -1 : 1;
        int value;
        switch (key.keyCode) {
            case SWT.ARROW_RIGHT:
                value = c.selection + c.increment;
                break;
            case SWT.ARROW_LEFT:
                value = c.selection - c.increment;
                break;
            case SWT.ARROW_UP:
                value = c.selection + up * c.increment;
                break;
            case SWT.ARROW_DOWN:
                value = c.selection - up * c.increment;
                break;
            case SWT.PAGE_UP:
                value = c.selection + up * c.pageIncrement;
                break;
            case SWT.PAGE_DOWN:
                value = c.selection - up * c.pageIncrement;
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
        if (c.selection == before)
            return;
        c.sendSelectionEvent(SWT.Selection, new Event(), true);
    }
}
