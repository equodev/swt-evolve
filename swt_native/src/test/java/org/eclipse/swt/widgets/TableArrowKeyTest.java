package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Up and Down move a Table's selection a row, the page keys to the rows in view, after the KeyDown and only if no listener vetoed it. */
class TableArrowKeyTest extends KeyRoutingTestBase {

    private Table table() {
        Shell shell = shell();
        Table table = new Table(new Composite(shell, SWT.NONE), SWT.SINGLE);
        for (String text : new String[] {"alpha", "bravo", "charlie", "delta"})
            new TableItem(table, SWT.NONE).setText(text);
        table.setSelection(1);
        return table;
    }

    @Test
    @DisplayName("Down then Up move the selection a row each, reporting the item after the KeyDown")
    void arrowsMoveTheSelection() {
        Table table = table();
        List<String> events = new ArrayList<>();
        table.addListener(SWT.KeyDown, e -> events.add("KeyDown"));
        table.addListener(SWT.Selection, e -> events.add("Selection " + ((TableItem) e.item).getText()));

        press(table, SWT.ARROW_DOWN);
        assertThat(table.getSelectionIndex()).isEqualTo(2);
        press(table, SWT.ARROW_UP);
        assertThat(table.getSelectionIndex()).isEqualTo(1);

        assertThat(events).containsExactly("KeyDown", "Selection charlie", "KeyDown", "Selection bravo");
    }

    @Test
    @DisplayName("a KeyDown listener that vetoes the key keeps the selection where it was")
    void vetoedKeyMovesNothing() {
        Table table = table();
        table.addListener(SWT.KeyDown, e -> e.doit = false);

        press(table, SWT.ARROW_DOWN);

        assertThat(table.getSelectionIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("End and Page Down go to the last row in view, Home and Page Up to the first")
    void pageKeysMoveToTheEnds() {
        Table table = table();
        table.setBounds(0, 0, 200, 300);
        List<String> events = new ArrayList<>();
        table.addListener(SWT.Selection, e -> events.add(((TableItem) e.item).getText()));

        press(table, SWT.PAGE_DOWN);
        assertThat(table.getSelectionIndex()).isEqualTo(3);
        press(table, SWT.PAGE_UP);
        assertThat(table.getSelectionIndex()).isEqualTo(0);
        press(table, SWT.HOME);
        press(table, SWT.END);
        assertThat(table.getSelectionIndex()).isEqualTo(3);

        assertThat(events).containsExactly("delta", "alpha", "delta");
    }

    /** Twenty rows, five in view, the selection on the first. */
    private Table longTable() {
        Table table = table();
        for (int i = 4; i < 20; i++)
            new TableItem(table, SWT.NONE).setText("row " + i);
        int itemHeight = table.getItemHeight();
        table.setBounds(0, 0, 200, 5 * itemHeight + itemHeight / 2);
        table.setSelection(0);
        return table;
    }

    @Test
    @DisplayName("Page Down goes to the last row in view, then a page on from there")
    void pageDownPagesThroughALongTable() {
        Table table = longTable();
        assertThat(TableHelper.visibleRows((DartTable) table.getImpl())).isEqualTo(5);

        press(table, SWT.PAGE_DOWN);
        assertThat(table.getSelectionIndex()).isEqualTo(4);
        press(table, SWT.PAGE_DOWN);
        assertThat(table.getSelectionIndex()).isEqualTo(8);
    }

    @Test
    @Disabled("the top index never moves: the client neither scrolls to it nor reports its own scroll")
    @DisplayName("Page Up after two Page Downs goes to the first row of the page the view scrolled to")
    void pageUpReadsTheScrolledTopIndex() {
        Table table = longTable();

        press(table, SWT.PAGE_DOWN);
        press(table, SWT.PAGE_DOWN);
        press(table, SWT.PAGE_UP);

        assertThat(table.getSelectionIndex()).isEqualTo(4);
    }
}
