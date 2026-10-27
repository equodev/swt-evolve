package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/**
 * What a click, a check, an edit, a mouse press and a request for rows arriving from Flutter does to a
 * Table. Every one hops through {@code asyncExec}, so by the time it runs the table may be gone.
 */
@Tag("native-unit")
class TableEventsFromFlutterNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private final List<Runnable> queued = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private Table table(int style, int rows) {
        Table table = new Table(DartMocks.dartShell(display), style);
        for (int i = 0; i < rows; i++)
            new TableItem(table, SWT.NONE).setText("row " + i);
        return table;
    }

    private void fromFlutter(Table table, String channel, Event event) {
        bridge.comm.fireContaining("Table/" + table.hashCode() + "/" + channel, event);
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static List<Event> record(Table table, int type) {
        List<Event> events = new ArrayList<>();
        table.addListener(type, events::add);
        return events;
    }

    private static Event rows(int... rows) {
        Event event = new Event();
        event.segments = rows;
        return event;
    }

    @Test
    @DisplayName("a click selects the row it names and tells the Selection listeners which item, as native SWT does")
    void aClickSelectsTheRow() {
        Table table = table(SWT.MULTI, 3);
        List<Event> events = record(table, SWT.Selection);

        fromFlutter(table, "Selection/Selection", rows(2));
        runQueued();

        assertThat(table.getSelectionIndices()).containsExactly(2);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).item).isSameAs(table.getItem(2));
        assertThat(events.get(0).index).as("a native Selection names the row by its item alone").isZero();
    }

    @Test
    @DisplayName("a click on the check box of a row toggles the row and says so in the event")
    void aCheckClickTogglesTheRow() {
        Table table = table(SWT.CHECK, 2);
        List<Event> events = record(table, SWT.Selection);
        Event click = rows(1);
        click.detail = SWT.CHECK;

        fromFlutter(table, "Selection/Selection", click);
        runQueued();

        assertThat(table.getItem(1).getChecked()).isTrue();
        assertThat(table.getItem(0).getChecked()).isFalse();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).detail).isEqualTo(SWT.CHECK);
        assertThat(events.get(0).item).isSameAs(table.getItem(1));
    }

    @Test
    @DisplayName("a click on no row, or on a row past the last one, selects nothing and names no item")
    void aClickOnNoRow() {
        Table table = table(SWT.MULTI, 2);
        List<Event> events = record(table, SWT.Selection);

        fromFlutter(table, "Selection/Selection", rows());
        fromFlutter(table, "Selection/Selection", rows(9));
        fromFlutter(table, "Selection/Selection", new Event());
        runQueued();

        assertThat(table.getSelectionCount()).isZero();
        assertThat(events).hasSize(3);
        assertThat(events).allSatisfy(e -> assertThat(e.item).isNull());
    }

    @Test
    @DisplayName("a double click reaches the DefaultSelection listeners once the table is active")
    void aDoubleClickIsADefaultSelection() {
        Table table = table(SWT.SINGLE, 2);
        List<Event> events = record(table, SWT.DefaultSelection);

        fromFlutter(table, "Selection/DefaultSelection", rows(1));
        runQueued();

        assertThat(table.getSelectionIndices()).containsExactly(1);
        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("an edit puts the text in the cell it names")
    void anEditSetsTheCellText() {
        Table table = table(SWT.NONE, 2);
        new TableColumn(table, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        Event edit = new Event();
        edit.index = 1;
        edit.start = 1;
        edit.text = "edited";

        fromFlutter(table, "Modify/Modify", edit);
        runQueued();

        assertThat(table.getItem(1).getText(1)).isEqualTo("edited");
        assertThat(table.getItem(0).getText(1)).isEmpty();
    }

    @Test
    @DisplayName("an edit that names no text, no row or a row that is gone changes nothing")
    void anEditThatNamesNothing() {
        Table table = table(SWT.NONE, 1);
        Event noText = new Event();
        noText.index = 0;
        noText.start = 0;
        Event noRow = new Event();
        noRow.index = 5;
        noRow.start = 0;
        noRow.text = "x";
        Event noColumn = new Event();
        noColumn.index = 0;
        noColumn.start = -1;
        noColumn.text = "x";

        fromFlutter(table, "Modify/Modify", noText);
        fromFlutter(table, "Modify/Modify", noRow);
        fromFlutter(table, "Modify/Modify", noColumn);
        runQueued();

        assertThat(table.getItem(0).getText()).isEqualTo("row 0");
    }

    @Test
    @DisplayName("a press on a row other than the selected one moves the selection there at once; the first press waits for its click")
    void aPressMovesTheSelection() {
        Table table = table(SWT.SINGLE, 3);
        table.setSelection(0);
        Event press = rows(2);
        press.button = 1;

        table.notifyListeners(SWT.MouseDown, press);
        assertThat(table.getSelectionIndices()).containsExactly(2);

        Table cold = table(SWT.SINGLE, 3);
        List<Event> events = record(cold, SWT.Selection);
        Event coldPress = rows(1);
        coldPress.button = 1;
        cold.notifyListeners(SWT.MouseDown, coldPress);

        assertThat(cold.getSelectionIndices()).containsExactly(1);
        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("a press of another button, without a row, or past the rows, does nothing")
    void aPressThatNamesNoRow() {
        Table table = table(SWT.SINGLE, 2);
        List<Event> events = record(table, SWT.Selection);
        Event middle = rows(1);
        middle.button = 2;
        Event noRow = new Event();
        noRow.button = 1;
        Event past = rows(7);
        past.button = 1;

        table.notifyListeners(SWT.MouseDown, middle);
        table.notifyListeners(SWT.MouseDown, noRow);
        table.notifyListeners(SWT.MouseDown, past);

        assertThat(table.getSelectionCount()).isZero();
        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("the rows the client shows are asked for their data")
    void theClientAsksForRows() {
        Table table = table(SWT.VIRTUAL, 0);
        List<Integer> asked = new ArrayList<>();
        table.addListener(SWT.SetData, e -> asked.add(e.index));
        table.setItemCount(100);
        asked.clear();
        Event window = new Event();
        window.end = 60;

        fromFlutter(table, "SetData/SetData", window);
        runQueued();

        assertThat(asked).contains(59).doesNotContain(61);
    }

    @Test
    @DisplayName("an event that arrives after the table was disposed is dropped")
    void eventsForADisposedTableAreDropped() {
        Table table = table(SWT.MULTI, 2);
        fromFlutter(table, "Selection/Selection", rows(1));
        fromFlutter(table, "Selection/DefaultSelection", rows(1));
        fromFlutter(table, "Modify/Modify", new Event());
        fromFlutter(table, "Scroll/Scroll", new Event());
        fromFlutter(table, "PaintItem/PaintItem", rows(0));
        fromFlutter(table, "SetData/SetData", new Event());
        table.dispose();

        org.assertj.core.api.Assertions.assertThatCode(this::runQueued).doesNotThrowAnyException();
    }

    private static Event check(int... rows) {
        Event click = rows(rows);
        click.detail = SWT.CHECK;
        return click;
    }

    @Test
    @DisplayName("a click on a check box past the last row, before the first or on no row toggles nothing")
    void aCheckClickOnNoRowTogglesNothing() {
        Table table = table(SWT.CHECK, 2);
        List<Event> events = record(table, SWT.Selection);
        Event noRow = new Event();
        noRow.detail = SWT.CHECK;

        fromFlutter(table, "Selection/Selection", check(2));
        fromFlutter(table, "Selection/Selection", check(-1));
        fromFlutter(table, "Selection/Selection", check());
        fromFlutter(table, "Selection/Selection", noRow);
        runQueued();

        assertThat(table.getItem(0).getChecked()).isFalse();
        assertThat(table.getItem(1).getChecked()).isFalse();
        assertThat(events).hasSize(4).allSatisfy(e -> assertThat(e.item).isNull());
    }

    @Test
    @DisplayName("a click on the check box of a virtual row not asked for yet makes the row and checks it; a second click unchecks it")
    void aCheckClickOnARowNotMadeYet() {
        Table table = table(SWT.CHECK | SWT.VIRTUAL, 0);
        table.setItemCount(TableHelper.VIRTUAL_INITIAL_ROWS + 4);
        int row = TableHelper.VIRTUAL_INITIAL_ROWS + 2;
        assertThat(impl(table).items[row]).isNull();
        List<Event> events = record(table, SWT.Selection);

        fromFlutter(table, "Selection/Selection", check(row));
        runQueued();
        assertThat(table.getItem(row).getChecked()).isTrue();
        assertThat(events.get(0).item).isSameAs(table.getItem(row));

        fromFlutter(table, "Selection/Selection", check(row));
        runQueued();
        assertThat(table.getItem(row).getChecked()).isFalse();
    }

    @Test
    @DisplayName("a Selection that carries the text of a cell edit names the row by its index as well")
    void aSelectionWithTextNamesTheIndex() {
        Table table = table(SWT.SINGLE, 3);
        List<Event> events = record(table, SWT.Selection);
        Event edit = rows(2);
        edit.text = "x";

        fromFlutter(table, "Selection/Selection", edit);
        fromFlutter(table, "Selection/Selection", rows(-1));
        runQueued();

        assertThat(events.get(0).item).isSameAs(table.getItem(2));
        assertThat(events.get(0).index).isEqualTo(2);
        assertThat(events.get(1).item).isNull();
    }

    @Test
    @DisplayName("an edit of a row before the first, of no cell, or of a virtual row not made yet changes nothing")
    void anEditOfARowNotThere() {
        Table table = table(SWT.VIRTUAL, 0);
        table.setItemCount(TableHelper.VIRTUAL_INITIAL_ROWS + 4);
        int notMade = TableHelper.VIRTUAL_INITIAL_ROWS + 2;
        Event before = new Event();
        before.index = -1;
        before.start = 0;
        before.text = "x";
        Event noCell = new Event();
        noCell.index = 0;
        noCell.start = -1;
        noCell.text = "x";
        Event virtual = new Event();
        virtual.index = notMade;
        virtual.start = 0;
        virtual.text = "x";

        fromFlutter(table, "Modify/Modify", before);
        fromFlutter(table, "Modify/Modify", noCell);
        fromFlutter(table, "Modify/Modify", virtual);
        runQueued();

        assertThat(impl(table).items[notMade]).isNull();
        assertThat(table.getItem(0).getText()).isEmpty();
    }

    @Test
    @DisplayName("once a click selected a row, a press of another button, on no row, before the first or past the last fires no Selection")
    void pressesAfterTheFirstClickThatNameNoRow() {
        Table table = table(SWT.SINGLE, 2);
        List<Event> events = record(table, SWT.Selection);
        Event first = rows(0);
        first.button = 1;
        table.notifyListeners(SWT.MouseDown, first);
        assertThat(events).hasSize(1);
        Event middle = rows(1);
        middle.button = 2;
        Event empty = rows();
        empty.button = 3;
        Event noRow = new Event();
        noRow.button = 1;
        Event before = rows(-1);
        before.button = 1;
        Event past = rows(2);
        past.button = 1;

        for (Event press : List.of(middle, empty, noRow, before, past))
            table.notifyListeners(SWT.MouseDown, press);

        assertThat(events).hasSize(1);
        assertThat(table.getSelectionIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("a right press selects the row as a left press does, and a press on the row already selected waits for nothing")
    void aRightPressAndARepeatedPress() {
        Table table = table(SWT.SINGLE, 2);
        List<Event> events = record(table, SWT.Selection);
        Event right = rows(1);
        right.button = 3;
        table.notifyListeners(SWT.MouseDown, right);
        Event again = rows(1);
        again.button = 1;
        table.notifyListeners(SWT.MouseDown, again);

        assertThat(table.getSelectionIndices()).containsExactly(1);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).stateMask & SWT.BUTTON3).isNotZero();
    }

    @Test
    @DisplayName("a MouseDown listener that disposes the table leaves the click no Selection to fire")
    void aPressThatDisposesTheTable() {
        Table table = table(SWT.SINGLE, 2);
        List<Event> events = record(table, SWT.Selection);
        boolean[] dispose = {false};
        table.addListener(SWT.MouseDown, e -> {
            if (dispose[0])
                table.dispose();
        });
        Event first = rows(0);
        first.button = 1;
        table.notifyListeners(SWT.MouseDown, first);
        assertThat(events).hasSize(1);
        dispose[0] = true;
        Event press = rows(1);
        press.button = 1;

        org.assertj.core.api.Assertions.assertThatCode(() -> table.notifyListeners(SWT.MouseDown, press))
            .doesNotThrowAnyException();
        assertThat(table.isDisposed()).isTrue();
        assertThat(events).hasSize(1);
    }

    private static Event key(int keyCode, int stateMask) {
        Event key = new Event();
        key.keyCode = keyCode;
        key.stateMask = stateMask;
        return key;
    }

    @Test
    @DisplayName("a key with a modifier, a key on an empty table and a key that does not move fire no Selection")
    void keysThatMoveNothing() {
        Table empty = table(SWT.SINGLE, 0);
        List<Event> none = record(empty, SWT.Selection);
        TableHelper.moveSelectionByKey(impl(empty), key(SWT.ARROW_DOWN, 0));
        assertThat(none).isEmpty();

        Table table = table(SWT.SINGLE, 3);
        table.setSelection(0);
        List<Event> events = record(table, SWT.Selection);
        TableHelper.moveSelectionByKey(impl(table), key(SWT.ARROW_DOWN, SWT.SHIFT));
        TableHelper.moveSelectionByKey(impl(table), key(SWT.HOME, 0));
        TableHelper.moveSelectionByKey(impl(table), key(SWT.ARROW_UP, 0));
        TableHelper.moveSelectionByKey(impl(table), key(SWT.PAGE_UP, 0));
        TableHelper.moveSelectionByKey(impl(table), key('a', 0));

        assertThat(events).isEmpty();
        assertThat(table.getSelectionIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Page Down in a table with its header shown counts only the rows below the header")
    void pageDownBelowAHeader() {
        Table table = table(SWT.SINGLE, 40);
        table.setHeaderVisible(true);
        table.setSize(200, table.getHeaderHeight() + 5 * table.getItemHeight());
        table.setSelection(0);

        TableHelper.moveSelectionByKey(impl(table), key(SWT.PAGE_DOWN, 0));

        assertThat(table.getSelectionIndices()).containsExactly(TableHelper.visibleRows(impl(table)) - 1);
        assertThat(TableHelper.visibleRows(impl(table))).isEqualTo(5);
    }

    @Test
    @DisplayName("Up and Down with no row selected go to the first row")
    void arrowsWithNoSelectionGoToTheFirstRow() {
        Table table = table(SWT.SINGLE, 3);
        TableHelper.moveSelectionByKey(impl(table), key(SWT.ARROW_UP, 0));
        assertThat(table.getSelectionIndices()).containsExactly(0);

        table.deselectAll();
        TableHelper.moveSelectionByKey(impl(table), key(SWT.ARROW_DOWN, 0));
        assertThat(table.getSelectionIndices()).containsExactly(0);
    }

    private static DartTable impl(Table table) {
        return (DartTable) table.getImpl();
    }
}
