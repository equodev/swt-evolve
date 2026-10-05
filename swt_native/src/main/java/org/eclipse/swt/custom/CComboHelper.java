package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

/**
 * Glue for {@code DartCCombo}, whose drop-down list is drawn by Flutter: Java learns that it is open
 * only from Flutter, and its keyboard focus stays on the field while it is.
 */
public class CComboHelper {

    public static void listShownByFlutter(DartCCombo combo, boolean shown) {
        if (combo.isDisposed())
            return;
        combo.listVisible = shown;
        // Only a change from what Java last sent reaches Flutter, so Java's own close must follow this.
        combo.getValue().markDirty(VCCombo.LIST_VISIBLE);
        // Opening the list focuses the CCombo, as a click on a native one does. Flutter's focus may
        // never have left it while Java moved its own elsewhere, so no FocusIn would come to say so.
        if (shown && !combo.isFocusControl())
            combo.setFocus();
    }

    /** Natively the list closes once the focus leaves the CCombo; Flutter draws it, so Java says so. */
    public static void focusLeft(DartCCombo combo) {
        if (combo.isDisposed() || !combo.listVisible)
            return;
        combo.listVisible = false;
        combo.getValue().markDirty(VCCombo.LIST_VISIBLE);
    }

    /**
     * Hands the field's Escape to the open list, which is where a native CCombo's focus is: the list
     * closes, in Flutter too, and the Traverse stops at the CCombo with doit=false.
     */
    public static boolean routeToOpenList(DartCCombo combo, Event event) {
        if (!combo.isDropped())
            return false;
        boolean escape = event.type == SWT.Traverse
                ? event.detail == SWT.TRAVERSE_ESCAPE
                : event.type == SWT.KeyDown && event.character == SWT.ESC;
        if (!escape)
            return false;
        combo.listEvent(event);
        if (!combo.isDisposed() && !combo.isDropped())
            combo.getValue().markDirty(VCCombo.LIST_VISIBLE);
        return true;
    }
}
