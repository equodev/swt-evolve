package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

public class SpinnerHelper {

    public static String getText(DartSpinner c) {
        c.checkWidget();
        return format(c.selection, c.digits);
    }

    /** Renders a spinner value the way its text field shows it: {@code digits} decimal places. */
    static String format(int value, int digits) {
        if (digits <= 0) {
            return String.valueOf(value);
        }
        String string = String.valueOf(value);
        int index = string.length() - digits;
        StringBuilder buffer = new StringBuilder();
        if (index > 0) {
            buffer.append(string, 0, index);
            buffer.append(".");
            buffer.append(string.substring(index));
        } else {
            buffer.append("0.");
            while (index++ < 0) buffer.append("0");
            buffer.append(string);
        }
        return buffer.toString();
    }

    /**
     * Up and Down step the value by the increment, Page Up and Page Down by the page increment, as
     * the platform's spinner does for a key no listener vetoed: the new text is offered as a Verify
     * over the old, then Modify and Selection. A WRAP spinner wraps past either end to the other.
     */
    public static void stepByKey(DartSpinner c, Event key) {
        if ((key.stateMask & SWT.MODIFIER_MASK) != 0)
            return;
        int delta;
        switch (key.keyCode) {
            case SWT.ARROW_UP:
                delta = c.increment;
                break;
            case SWT.ARROW_DOWN:
                delta = -c.increment;
                break;
            case SWT.PAGE_UP:
                delta = c.pageIncrement;
                break;
            case SWT.PAGE_DOWN:
                delta = -c.pageIncrement;
                break;
            default:
                return;
        }
        int value = c.selection + delta;
        if ((c.getApi().getStyle() & SWT.WRAP) != 0) {
            if (value > c.maximum)
                value = c.minimum;
            else if (value < c.minimum)
                value = c.maximum;
        }
        value = Math.max(c.minimum, Math.min(c.maximum, value));
        if (value == c.selection)
            return;
        Event verify = new Event();
        verify.text = format(value, c.digits);
        verify.start = 0;
        verify.end = getText(c).length();
        c.sendEvent(SWT.Verify, verify);
        if (!verify.doit || c.isDisposed())
            return;
        setSelection(c, value, true, true, true);
    }

    static void setSelection(DartSpinner c, int value, boolean setPos, boolean setText, boolean notify) {
        int newValue = value;
        if (!java.util.Objects.equals(c.selection, newValue)) {
            c.getValue().markDirty(VSpinner.SELECTION);
        }
        if (setPos) {
        }
        if (setText) {
            String string = String.valueOf(value);
            if (c.digits > 0) {
                int index = string.length() - c.digits;
                StringBuilder buffer = new StringBuilder();
                if (index > 0) {
                    buffer.append(string.substring(0, index));
                    buffer.append(string.substring(index));
                } else {
                    buffer.append("0");
                    while (index++ < 0) buffer.append("0");
                    buffer.append(string);
                }
                string = buffer.toString();
            }
            if (c.hooks(SWT.Verify) || c.filters(SWT.Verify)) {
                if (string == null)
                    return;
            }
            c.selection = newValue;
            c.sendEvent(SWT.Modify);
        } else {
            c.selection = newValue;
        }
        if (notify)
            c.sendSelectionEvent(SWT.Selection);
    }

}