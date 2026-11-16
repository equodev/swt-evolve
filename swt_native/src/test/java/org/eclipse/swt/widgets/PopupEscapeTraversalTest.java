package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Escape that closes a drop-down's open popup arrives from the client with doit=false. As on
 * Win32, it is offered to the control and its parents as a Traverse with doit=false, then as a KeyDown.
 */
class PopupEscapeTraversalTest extends KeyRoutingTestBase {

    private final java.util.List<String> events = new ArrayList<>();

    private DateTime dropDownIn(Composite parent) {
        parent.addListener(SWT.Traverse, e -> events.add("parent Traverse " + e.detail + " doit=" + e.doit));
        DateTime date = new DateTime(parent, SWT.DATE | SWT.DROP_DOWN);
        date.addListener(SWT.Traverse, e -> events.add("date Traverse " + e.detail + " doit=" + e.doit));
        date.addListener(SWT.KeyDown, e -> events.add("date KeyDown"));
        return date;
    }

    private static void pressClosingPopup(Control control) {
        Event key = new Event();
        key.keyCode = SWT.ESC;
        key.character = SWT.ESC;
        key.doit = false;
        ControlHelper.routeKeyDown((DartControl) control.getImpl(), key);
    }

    @Test
    @DisplayName("the Escape that closes the calendar is a Traverse with doit=false up the parents, then a KeyDown")
    void closingEscapeTraversesWithDoitFalse() {
        DateTime date = dropDownIn(new Composite(shell(), SWT.NONE));

        pressClosingPopup(date);

        assertThat(events).containsExactly(
                "date Traverse " + SWT.TRAVERSE_ESCAPE + " doit=false",
                "parent Traverse " + SWT.TRAVERSE_ESCAPE + " doit=false",
                "date KeyDown");
    }

    @Test
    @DisplayName("a listener that sets doit=true still traverses, and the KeyDown is then not delivered")
    void aListenerCanStillTakeTheTraversal() {
        DateTime date = dropDownIn(new Composite(shell(), SWT.NONE));
        date.addListener(SWT.Traverse, e -> {
            e.doit = true;
            e.detail = SWT.TRAVERSE_NONE;
        });

        pressClosingPopup(date);

        assertThat(events).containsExactly("date Traverse " + SWT.TRAVERSE_ESCAPE + " doit=false");
    }

    @Test
    @DisplayName("an Escape with no popup to close keeps its doit=true traversal")
    void plainEscapeKeepsItsTraversal() {
        DateTime date = dropDownIn(new Composite(shell(), SWT.NONE));

        press(date, SWT.ESC);

        assertThat(events).first().isEqualTo("date Traverse " + SWT.TRAVERSE_ESCAPE + " doit=true");
    }
}
