package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

public class SpinnerHelper {

    public static String getText(DartSpinner c) {
        c.checkWidget();
        if (c.digits > 0) {
            String string = String.valueOf(c.selection);
            int index = string.length() - c.digits;
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
        return String.valueOf(c.selection);
    }

    static void setSelection(DartSpinner c, int value, boolean setPos, boolean setText, boolean notify) {
        int newValue = value;
        if (!java.util.Objects.equals(c.selection, newValue)) {
            c.dirty();
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