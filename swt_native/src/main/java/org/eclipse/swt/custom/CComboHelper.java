package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Event;

/**
 * Glue for {@code DartCCombo}, whose drop-down list is drawn by Flutter: Java learns that it is open
 * only from Flutter, and its keyboard focus stays on the field while it is. The CCombo holds the state
 * a native one keeps in its List.
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
     * Gives the field's Escape the meaning it has on the open list, where a native CCombo's focus is:
     * the Traverse stops at the CCombo with doit=false, and the key closes the list, in Flutter too.
     */
    public static boolean routeToOpenList(DartCCombo combo, Event event) {
        if (!combo.isDropped())
            return false;
        if (event.type == SWT.Traverse && event.detail == SWT.TRAVERSE_ESCAPE) {
            Event e = new Event();
            e.time = event.time;
            e.detail = event.detail;
            e.doit = false;
            e.character = event.character;
            e.keyCode = event.keyCode;
            e.keyLocation = event.keyLocation;
            combo.notifyListeners(SWT.Traverse, e);
            event.doit = e.doit;
            event.detail = e.detail;
            return true;
        }
        if (event.type == SWT.KeyDown && event.character == SWT.ESC) {
            combo.dropDown(false);
            if (combo.isDisposed())
                return true;
            Event e = new Event();
            e.time = event.time;
            e.character = event.character;
            e.keyCode = event.keyCode;
            e.keyLocation = event.keyLocation;
            e.stateMask = event.stateMask;
            combo.notifyListeners(SWT.KeyDown, e);
            event.doit = e.doit;
            return true;
        }
        return false;
    }

    /** The height of a row of the list Flutter draws: the font's height plus the row's margin, as a List's. */
    static int itemHeight(DartCCombo combo) {
        Font font = combo.getFont();
        if (font == null || font.isDisposed())
            return 0;
        FontData[] fontData = font.getFontData();
        if (fontData == null || fontData.length == 0 || fontData[0] == null)
            return 0;
        return fontData[0].getHeight() + 4;
    }
}
