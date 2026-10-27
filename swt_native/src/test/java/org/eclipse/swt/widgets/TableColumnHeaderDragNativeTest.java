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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/**
 * What a header drag arriving from Flutter does to a Table's columns. Every event from Flutter hops through
 * {@code asyncExec}, so by the time it runs the column may be gone.
 */
@Tag("native-unit")
class TableColumnHeaderDragNativeTest {

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

    private Table table(int rows) {
        Table table = new Table(DartMocks.dartShell(display), SWT.SINGLE);
        table.setBounds(0, 0, 200, 100);
        for (int i = 0; i < rows; i++)
            new TableItem(table, SWT.NONE).setText("row " + i);
        return table;
    }

    private void fromFlutter(Widget widget, String channel, Event event) {
        bridge.comm.fireContaining(widget.getClass().getSimpleName() + "/" + widget.hashCode() + "/" + channel, event);
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static VTable value(Table table) {
        return ((DartTable) table.getImpl()).getValue();
    }

    @Test
    @DisplayName("a moveable column dragged in the header is shown where it was dropped, and the columns that moved hear it")
    void headerDragReordersTheColumns() {
        Table table = table(2);
        TableColumn[] columns = new TableColumn[3];
        List<TableColumn> moved = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            TableColumn column = columns[i] = new TableColumn(table, SWT.NONE);
            column.setWidth(40 + 10 * i);
            column.addListener(SWT.Move, e -> moved.add(column));
        }
        columns[2].setMoveable(true);
        Event dropped = new Event();
        dropped.index = 0;

        fromFlutter(columns[1], "Control/Move", dropped);
        runQueued();
        assertThat(table.getColumnOrder()).containsExactly(0, 1, 2);

        fromFlutter(columns[2], "Control/Move", dropped);
        runQueued();
        assertThat(table.getColumnOrder()).containsExactly(2, 0, 1);
        assertThat(moved).containsExactlyInAnyOrder(columns);
        assertThat(value(table).changedKeys()).contains(VTable.COLUMN_ORDER);
    }

    @Test
    @DisplayName("a header drag that arrives after the table was disposed is dropped")
    void aDragForADisposedTableIsDropped() {
        Table table = table(5);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setMoveable(true);
        fromFlutter(column, "Control/Move", new Event());
        table.dispose();

        assertThatCode(this::runQueued).doesNotThrowAnyException();
    }
}
