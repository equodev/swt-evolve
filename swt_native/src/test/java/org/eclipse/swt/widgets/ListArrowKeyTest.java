package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Up, Down, Page Up, Page Down, Home and End move a List's selection, after the KeyDown and only if no
 * listener vetoed it.
 */
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
    @DisplayName("Page Down, Page Up, Home and End select the row they reach, as Win32 and GTK do")
    void pageHomeEndMoveTheSelection() {
        List list = list();
        list.setSelection(0);
        java.util.List<String> events = new ArrayList<>();
        list.addListener(SWT.Selection, e -> events.add("Selection " + list.getSelectionIndex()));

        press(list, SWT.PAGE_DOWN);
        press(list, SWT.PAGE_UP);
        press(list, SWT.HOME);
        press(list, SWT.END);

        assertThat(list.getSelectionIndex()).isEqualTo(2);
        assertThat(events).containsExactly("Selection 1", "Selection 0", "Selection 2");
    }

    @Test
    @DisplayName("Page Down first goes to the last row in view, and Page Up from the top row goes a page up")
    void pageKeysStopAtTheEdgeOfTheViewFirst() {
        List list = new List(shell(), SWT.SINGLE);
        String[] items = new String[20];
        for (int i = 0; i < items.length; i++)
            items[i] = "item " + i;
        list.setItems(items);
        list.setBounds(20, 20, 200, list.getItemHeight() * 5);
        int rows = list.getClientArea().height / list.getItemHeight();
        assertThat(rows).as("rows in view").isGreaterThan(2);

        list.setSelection(0);
        press(list, SWT.PAGE_DOWN);
        assertThat(list.getSelectionIndex()).isEqualTo(rows - 1);

        list.setSelection(10);
        list.setTopIndex(10);
        press(list, SWT.PAGE_UP);
        assertThat(list.getSelectionIndex()).isEqualTo(10 - (rows - 1));
    }

    @Test
    @DisplayName("End brings the last row into view")
    void endShowsTheLastRow() {
        List list = new List(shell(), SWT.SINGLE);
        list.setItems(new String[] {"alpha", "bravo", "charlie"});
        list.setBounds(20, 20, 200, 28);
        list.setSelection(0);

        press(list, SWT.END);

        assertThat(list.getTopIndex()).isPositive();
    }

    @Test
    @DisplayName("a key that does not move a row moves nothing")
    void anotherKeyMovesNothing() {
        List list = list();
        java.util.List<String> events = new ArrayList<>();
        list.addListener(SWT.Selection, e -> events.add("Selection"));

        press(list, SWT.ARROW_LEFT);
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
