package org.eclipse.swt.custom;

import dev.equo.swt.size.CComboListSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TextHelper;

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
     * Gives the field's Escape, Enter and Tab the meaning they have on the open list, where a native
     * CCombo's focus is. The Traverse of Escape and Enter stops at the CCombo with doit=false, and the
     * key closes the list, in Flutter too; Enter also fires DefaultSelection. Tab is the field's
     * traverse(detail), which carries no key, and the list closes once it goes through.
     */
    public static boolean routeToOpenList(DartCCombo combo, Event event) {
        if (!combo.isDropped())
            return false;
        if (event.type == SWT.Traverse && (event.detail == SWT.TRAVERSE_ESCAPE || event.detail == SWT.TRAVERSE_RETURN)) {
            forwardTraverse(combo, event, false, true);
            return true;
        }
        if (event.type == SWT.Traverse && (event.detail == SWT.TRAVERSE_TAB_NEXT || event.detail == SWT.TRAVERSE_TAB_PREVIOUS)) {
            forwardTraverse(combo, event, event.doit, false);
            if (event.doit && !combo.isDisposed())
                combo.dropDown(false);
            return true;
        }
        if (event.type == SWT.KeyDown && (event.character == SWT.ESC || event.character == SWT.CR)) {
            combo.dropDown(false);
            if (event.character == SWT.CR) {
                Event e = new Event();
                e.time = event.time;
                e.stateMask = event.stateMask;
                combo.notifyListeners(SWT.DefaultSelection, e);
            }
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

    /** Fires the field's Traverse as the CCombo's own, and takes back its verdict. */
    private static void forwardTraverse(DartCCombo combo, Event event, boolean doit, boolean withKey) {
        Event e = new Event();
        e.time = event.time;
        e.detail = event.detail;
        e.doit = doit;
        if (withKey) {
            e.character = event.character;
            e.keyCode = event.keyCode;
            e.keyLocation = event.keyLocation;
        }
        combo.notifyListeners(SWT.Traverse, e);
        event.doit = e.doit;
        event.detail = e.detail;
    }

    /**
     * Whether {@code control} hands its focus to a field of its own: a CCombo, whose field is a
     * Java-only Text that never reaches Flutter, kept because SWT's own tests observe it as the
     * focus control. The CCombo is NO_FOCUS itself, so the shared press, Tab and focus code asks
     * here rather than reading its style, and routes the focus to the CCombo with setFocus.
     */
    public static boolean ownsFieldFocus(Control control) {
        return control instanceof CCombo;
    }

    /**
     * The control that takes a focus the client gave {@code control}: a CCombo's field, which SWT's
     * CCombo focuses without ever holding the focus itself; any other control is its own.
     */
    public static DartControl focusTarget(DartControl control) {
        return control instanceof DartCCombo ? (DartControl) ((DartCCombo) control)._text().getImpl() : control;
    }

    /** Whether {@code focused} is the field of {@code control}, which then holds the focus through it. */
    public static boolean focusedThroughField(Control focused, Control control) {
        return ownsFieldFocus(control) && focused != null && !focused.isDisposed()
                && focused.getParent() == control;
    }

    /**
     * Delivers a vertical wheel over a CCombo as its field does natively, through the CCombo's own
     * handler for its field's events: its listeners first, then a step of the selection. Flutter
     * reports the wheel on the CCombo, so no field event carries it there.
     */
    public static boolean deliverWheel(Control control, Event event) {
        if (!(control instanceof CCombo) || !(control.getImpl() instanceof DartCCombo))
            return false;
        event.type = SWT.MouseWheel;
        ((DartCCombo) control.getImpl()).textEvent(event);
        return true;
    }

    /**
     * Enter in the field fires DefaultSelection with the list closed, as a native Text does after its
     * KeyDown listeners: the field is not drawn here, so no platform widget does it. Runs after
     * {@link #routeToOpenList}, which has handled an open list's Enter. Returns whether the event was
     * handled, KeyDown included.
     */
    public static boolean keyDefaultAction(DartCCombo combo, Event event) {
        if (event.type != SWT.KeyDown || event.character != SWT.CR)
            return false;
        Event keyEvent = new Event();
        keyEvent.time = event.time;
        keyEvent.character = event.character;
        keyEvent.keyCode = event.keyCode;
        keyEvent.keyLocation = event.keyLocation;
        keyEvent.stateMask = event.stateMask;
        combo.notifyListeners(SWT.KeyDown, keyEvent);
        event.doit = keyEvent.doit;
        if (combo.isDisposed() || !event.doit)
            return true;
        Event action = new Event();
        action.type = SWT.DefaultSelection;
        action.time = event.time;
        action.stateMask = event.stateMask;
        combo.textEvent(action);
        return true;
    }

    /**
     * The field's whole new text after the user edited it in Flutter, applied to the CCombo's Text as
     * the keystroke that made it: its Verify and Modify, which the CCombo forwards as its own, name
     * the edit as a native CCombo's Text does. {@code e.start} is the client's caret.
     */
    public static void fieldEdited(DartCCombo combo, Event e) {
        if (e.text == null)
            return;
        TextHelper.applyClientEdit((DartText) combo._text().getImpl(), e.text, e.start);
    }

    /** The height of a row of the list Flutter draws. */
    static int itemHeight(DartCCombo combo) {
        return CComboListSizes.getItemHeight(combo);
    }
}
