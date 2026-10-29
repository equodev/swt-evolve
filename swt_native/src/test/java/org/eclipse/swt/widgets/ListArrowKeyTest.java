package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/** Up and Down move a List's selection a row, after the KeyDown and only if no listener vetoed it. */
class ListArrowKeyTest extends KeyRoutingTestBase {

    private List list() {
        List list = new List(new Composite(shell(), SWT.NONE), SWT.SINGLE);
        list.setItems(new String[] {"alpha", "bravo", "charlie"});
        list.setSelection(1);
        return list;
    }

    @Test
    @DisplayName("Down then Up move the selection a row each, as a Selection after the KeyDown")
    void arrowsMoveTheSelection() {
        List list = list();
        java.util.List<String> events = new ArrayList<>();
        list.addListener(SWT.KeyDown, e -> events.add("KeyDown"));
        list.addListener(SWT.Selection, e -> events.add("Selection " + list.getSelectionIndex()));

        press(list, SWT.ARROW_DOWN);
        assertThat(list.getSelectionIndex()).isEqualTo(2);
        press(list, SWT.ARROW_DOWN);
        assertThat(list.getSelectionIndex()).isEqualTo(2);
        press(list, SWT.ARROW_UP);
        assertThat(list.getSelectionIndex()).isEqualTo(1);

        assertThat(events).containsExactly("KeyDown", "Selection 2", "KeyDown", "KeyDown", "Selection 1");
    }

    @Test
    @DisplayName("a KeyDown listener that vetoes the key keeps the selection where it was")
    void vetoedKeyMovesNothing() {
        List list = list();
        list.addListener(SWT.KeyDown, e -> e.doit = false);

        press(list, SWT.ARROW_DOWN);

        assertThat(list.getSelectionIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("an arrow held with a modifier moves nothing: Shift and Ctrl ranges are the client's")
    void anArrowWithAModifierMovesNothing() {
        List list = list();
        java.util.List<String> events = new ArrayList<>();
        list.addListener(SWT.Selection, e -> events.add("Selection"));

        for (int modifier : new int[] {SWT.SHIFT, SWT.CTRL, SWT.ALT, SWT.COMMAND}) {
            Event key = new Event();
            key.keyCode = SWT.ARROW_DOWN;
            key.stateMask = modifier;
            ControlHelper.routeKeyDown((DartControl) list.getImpl(), key);
        }

        assertThat(list.getSelectionIndex()).isEqualTo(1);
        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a key that is not Up or Down moves nothing")
    void anotherKeyMovesNothing() {
        List list = list();
        java.util.List<String> events = new ArrayList<>();
        list.addListener(SWT.Selection, e -> events.add("Selection"));

        press(list, SWT.ARROW_LEFT);
        press(list, SWT.END);
        press(list, 'a');

        assertThat(list.getSelectionIndex()).isEqualTo(1);
        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("an arrow into a list with nothing selected takes the first row, either direction")
    void anArrowWithNoSelectionTakesTheFirstRow() {
        List down = list();
        down.deselectAll();
        press(down, SWT.ARROW_DOWN);
        assertThat(down.getSelectionIndex()).isZero();

        List up = list();
        up.deselectAll();
        press(up, SWT.ARROW_UP);
        assertThat(up.getSelectionIndex()).isZero();
    }

    @Test
    @DisplayName("an empty list has no row to move to")
    void anEmptyListMovesNothing() {
        List list = new List(new Composite(shell(), SWT.NONE), SWT.SINGLE);
        java.util.List<String> events = new ArrayList<>();
        list.addListener(SWT.Selection, e -> events.add("Selection"));

        press(list, SWT.ARROW_DOWN);
        press(list, SWT.ARROW_UP);

        assertThat(list.getSelectionIndex()).isEqualTo(-1);
        assertThat(events).isEmpty();
    }
}
