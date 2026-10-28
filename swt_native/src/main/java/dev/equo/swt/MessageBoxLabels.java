package dev.equo.swt;

import java.util.Map;

import org.eclipse.swt.SWT;

/**
 * A MessageBox's button labels. The button order below is the order they travel to Flutter in, and the only
 * place that order is written down on this side.
 */
public final class MessageBoxLabels {

    /** The buttons a label is carried for, in the order the labels travel. */
    private static final int[] BUTTONS = {SWT.OK, SWT.CANCEL, SWT.YES, SWT.NO, SWT.ABORT, SWT.RETRY, SWT.IGNORE};

    private static final String[] KEYS = {"SWT_OK", "SWT_Cancel", "SWT_Yes", "SWT_No", "SWT_Abort", "SWT_Retry",
            "SWT_Ignore"};

    private MessageBoxLabels() {
    }

    /** Every button's label: the one {@code custom} sets for it, else SWT's own. */
    public static String[] wire(Map<Integer, String> custom) {
        String[] result = new String[BUTTONS.length];
        for (int i = 0; i < BUTTONS.length; i++) {
            String label = custom != null ? custom.get(BUTTONS[i]) : null;
            result[i] = label != null ? label : SWT.getMessage(KEYS[i]);
        }
        return result;
    }
}
