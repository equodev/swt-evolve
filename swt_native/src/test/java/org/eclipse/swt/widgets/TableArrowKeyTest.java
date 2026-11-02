package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
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
    @DisplayName("Page Up after two Page Downs goes to the first row of the page the view scrolled to")
    void pageUpReadsTheScrolledTopIndex() {
        Table table = longTable();

        press(table, SWT.PAGE_DOWN);
        press(table, SWT.PAGE_DOWN);
        press(table, SWT.PAGE_UP);

        assertThat(table.getSelectionIndex()).isEqualTo(4);
    }

    @Test
    @DisplayName("Enter is the selected row's DefaultSelection, and moves nothing")
    void enterIsTheDefaultSelection() {
        Table table = table();
        List<String> events = new ArrayList<>();
        table.addListener(SWT.Selection, e -> events.add("Selection " + ((TableItem) e.item).getText()));
        table.addListener(SWT.DefaultSelection, e -> events.add("DefaultSelection " + ((TableItem) e.item).getText()));

        press(table, SWT.CR);

        assertThat(events).containsExactly("DefaultSelection bravo");
        assertThat(table.getSelectionIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("Space toggles the box of every selected row of a CHECK table, each with a CHECK Selection")
    void spaceTogglesTheSelectedChecks() {
        Table table = new Table(new Composite(shell(), SWT.NONE), SWT.MULTI | SWT.CHECK);
        for (String text : new String[] {"alpha", "bravo", "charlie", "delta"})
            new TableItem(table, SWT.NONE).setText(text);
        table.setSelection(new int[] {0, 2});
        List<String> events = new ArrayList<>();
        table.addListener(SWT.Selection, e -> events.add((e.detail == SWT.CHECK ? "check " : "select ") + ((TableItem) e.item).getText()));

        press(table, ' ');

        assertThat(events).containsExactly("check alpha", "check charlie");
        assertThat(table.getItem(0).getChecked()).isTrue();
        assertThat(table.getItem(1).getChecked()).isFalse();
        assertThat(table.getItem(2).getChecked()).isTrue();
        assertThat(table.getSelectionIndices()).containsExactly(0, 2);
    }

    @Test
    @DisplayName("a typed letter selects the next row it starts, again the one after, and more letters narrow it")
    void typingSelectsTheRowItStarts() {
        Table table = table();
        for (String text : new String[] {"bob", "dora"})
            new TableItem(table, SWT.NONE).setText(text);
        List<String> events = new ArrayList<>();
        table.addListener(SWT.Selection, e -> events.add(((TableItem) e.item).getText()));

        press(table, 'd');
        press(table, 'd');
        press(table, 'd');
        assertThat(events).containsExactly("delta", "dora", "delta");

        Table other = table();
        new TableItem(other, SWT.NONE).setText("bob");
        other.setSelection(0);
        events.clear();
        other.addListener(SWT.Selection, e -> events.add(((TableItem) e.item).getText()));
        press(other, 'b');
        press(other, 'o');
        assertThat(events).containsExactly("bravo", "bob");
    }

    @Test
    @DisplayName("Enter names the row the last selection by input landed on, not the first selected one")
    void enterNamesTheFocusRow() {
        Table table = new Table(new Composite(shell(), SWT.NONE), SWT.MULTI);
        for (String text : new String[] {"alpha", "bravo", "charlie", "delta"})
            new TableItem(table, SWT.NONE).setText(text);
        Event click = new Event();
        click.segments = new int[] {2};
        TableHelper.sendSelection((DartTable) table.getImpl(), click, SWT.Selection);
        table.select(0);
        List<String> events = new ArrayList<>();
        table.addListener(SWT.DefaultSelection, e -> events.add(((TableItem) e.item).getText()));

        press(table, SWT.CR);

        assertThat(table.getSelectionIndices()).containsExactly(0, 2);
        assertThat(events).containsExactly("charlie");
    }

    @Test
    @DisplayName("a NO_SEARCH or VIRTUAL table selects no row by a typed letter")
    void noTypingSearchWhereTheStyleTurnsItOff() {
        for (int style : new int[] {TableHelper.NO_SEARCH, SWT.VIRTUAL}) {
            Table table = new Table(new Composite(shell(), SWT.NONE), SWT.SINGLE | style);
            table.addListener(SWT.SetData, e -> ((TableItem) e.item).setText("delta"));
            table.setItemCount(4);
            for (int i = 0; i < 4; i++) table.getItem(i).setText(new String[] {"alpha", "bravo", "charlie", "delta"}[i]);
            table.setSelection(1);

            press(table, 'd');

            assertThat(table.getSelectionIndex()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("keypad Enter is the DefaultSelection too; with a modifier, and with nothing selected, Enter is not")
    void keypadEnterAndTheEntersThatAreNot() {
        Table table = table();
        List<String> events = new ArrayList<>();
        table.addListener(SWT.DefaultSelection, e -> events.add(((TableItem) e.item).getText()));

        press(table, SWT.KEYPAD_CR);
        Event shiftEnter = new Event();
        shiftEnter.keyCode = SWT.CR;
        shiftEnter.stateMask = SWT.SHIFT;
        ControlHelper.routeKeyDown((DartControl) table.getImpl(), shiftEnter);
        table.deselectAll();
        press(table, SWT.CR);

        assertThat(events).containsExactly("bravo");
    }

    @Test
    @DisplayName("Enter falls back to the first selected row once the focus row is gone")
    void enterFallsBackWhenTheFocusRowIsRemoved() {
        Table table = table();
        Event click = new Event();
        click.segments = new int[] {3};
        TableHelper.sendSelection((DartTable) table.getImpl(), click, SWT.Selection);
        table.remove(3);
        table.setSelection(0);
        List<String> events = new ArrayList<>();
        table.addListener(SWT.DefaultSelection, e -> events.add(((TableItem) e.item).getText()));

        press(table, SWT.CR);

        assertThat(events).containsExactly("alpha");
    }

    @Test
    @DisplayName("Space toggles a checked box off again; it toggles nothing in a table without check boxes")
    void spaceTogglesBackAndOnlyInACheckTable() {
        Table check = new Table(new Composite(shell(), SWT.NONE), SWT.CHECK);
        new TableItem(check, SWT.NONE).setText("alpha");
        check.setSelection(0);
        press(check, ' ');
        press(check, ' ');
        assertThat(check.getItem(0).getChecked()).isFalse();

        Table plain = table();
        List<String> events = new ArrayList<>();
        plain.addListener(SWT.Selection, e -> events.add(e.detail == SWT.CHECK ? "check" : "select"));
        press(plain, ' ');
        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a Selection listener that disposes the table ends the toggling of the selected boxes")
    void disposingTheTableEndsTheToggle() {
        Table table = new Table(new Composite(shell(), SWT.NONE), SWT.MULTI | SWT.CHECK);
        for (String text : new String[] {"alpha", "bravo"})
            new TableItem(table, SWT.NONE).setText(text);
        table.setSelection(new int[] {0, 1});
        List<String> events = new ArrayList<>();
        table.addListener(SWT.Selection, e -> {
            events.add(((TableItem) e.item).getText());
            table.dispose();
        });

        press(table, ' ');

        assertThat(events).containsExactly("alpha");
    }

    @Test
    @DisplayName("typing the letter of the selected row, its only match, selects nothing new")
    void typingTheSelectedRowsOnlyLetterSelectsNothing() {
        Table table = table();
        List<String> events = new ArrayList<>();
        table.addListener(SWT.Selection, e -> events.add(((TableItem) e.item).getText()));

        press(table, 'b');

        assertThat(events).isEmpty();
        assertThat(table.getSelectionIndex()).isEqualTo(1);
    }
}

