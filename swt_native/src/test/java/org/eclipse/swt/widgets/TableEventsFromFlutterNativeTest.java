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
}
