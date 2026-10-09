package org.eclipse.swt.widgets;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.eclipse.swt.SWT;

public class SpinnerHelper {

    private SpinnerHelper() {
    }

    public static String getText(DartSpinner c) {
        c.checkWidget();
        return format(c.selection, c.digits);
    }

    /**
     * The decimal separator of the host's locale, which is what the text of a native spinner shows (the number
     * formatter's on Cocoa, the user locale's on Win32, the C library's on GTK).
     */
    public static String decimalSeparator() {
        return String.valueOf(DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)).getDecimalSeparator());
    }

    /** Renders a spinner value the way its text field shows it: {@code digits} decimal places. */
    static String format(int value, int digits) {
        if (digits <= 0) {
            return String.valueOf(value);
        }
        String string = String.valueOf(value);
        int index = string.length() - digits;
        StringBuilder buffer = new StringBuilder();
        String separator = decimalSeparator();
        if (index > 0) {
            buffer.append(string, 0, index);
            buffer.append(separator);
            buffer.append(string.substring(index));
        } else {
            buffer.append("0").append(separator);
            while (index++ < 0) buffer.append("0");
            buffer.append(string);
        }
        return buffer.toString();
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