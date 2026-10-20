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
        list.setItems("alpha", "bravo", "charlie");
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
}
