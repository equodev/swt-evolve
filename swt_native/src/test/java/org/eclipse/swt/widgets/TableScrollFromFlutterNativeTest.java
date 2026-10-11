package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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
 * What a scroll of the rows arriving from Flutter does to a Table, and how API scrolling tells Flutter where
 * to scroll. Every event from Flutter hops through {@code asyncExec}, so by the time it runs the table may be
 * gone.
 */
@Tag("native-unit")
class TableScrollFromFlutterNativeTest {

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
        return table(rows, SWT.SINGLE);
    }

    private Table table(int rows, int style) {
        Table table = new Table(DartMocks.dartShell(display), style);
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

    private static Event scrolledTo(int y) {
        Event event = new Event();
        event.y = y;
        return event;
    }

    @Test
    @DisplayName("a scroll the client reports moves the top index, the rows' bounds and the row a point finds")
    void aScrollMovesTheTopIndexAndTheRows() {
        Table table = table(40);
        int itemHeight = table.getItemHeight();
        Rectangle before = table.getItem(12).getBounds();

        fromFlutter(table, "Scroll/Scroll", scrolledTo(7 * itemHeight));
        runQueued();

        assertThat(table.getTopIndex()).isEqualTo(7);
        assertThat(table.getItem(12).getBounds().y).isEqualTo(before.y - 7 * itemHeight);
        assertThat(table.getItem(12).getBounds(0).y).isEqualTo(before.y - 7 * itemHeight);
        assertThat(table.getItem(new Point(5, table.getHeaderHeight() + 1))).isSameAs(table.getItem(7));
    }

    @Test
    @DisplayName("the top index the client reports is the one the next description names, without asking it to scroll")
    void aClientScrollIsNotSentBack() {
        Table table = table(40);
        value(table).clearDirty();

        fromFlutter(table, "Scroll/Scroll", scrolledTo(5 * table.getItemHeight()));
        runQueued();

        assertThat(value(table).getTopIndex()).isEqualTo(5);
        assertThat(value(table).changedKeys()).doesNotContain(VTable.TOP_INDEX);
    }

    @Test
    @DisplayName("a scroll past the last row is read as the end of the rows")
    void aScrollPastTheEndStopsAtTheLastRows() {
        Table table = table(10);

        fromFlutter(table, "Scroll/Scroll", scrolledTo(100 * table.getItemHeight()));
        runQueued();

        int top = table.getTopIndex();
        assertThat(top).isGreaterThan(0).isLessThan(10);
        assertThat(table.getItem(9).getBounds().y).isLessThan(100);
    }

    @Test
    @DisplayName("setTopIndex and showItem ask the client to scroll to the row they bring into view")
    void apiScrollingTellsTheClient() {
        Table table = table(60);
        value(table).clearDirty();

        table.setTopIndex(20);
        assertThat(value(table).changedKeys()).contains(VTable.TOP_INDEX);
        assertThat(value(table).getTopIndex()).isEqualTo(20);

        value(table).clearDirty();
        table.showItem(table.getItem(21));
        assertThat(value(table).changedKeys()).doesNotContain(VTable.TOP_INDEX);

        table.showItem(table.getItem(50));
        assertThat(value(table).changedKeys()).contains(VTable.TOP_INDEX);
        assertThat(table.getTopIndex()).isLessThanOrEqualTo(50).isGreaterThan(20);
    }

    @Test
    @DisplayName("a table not laid out yet shows every row, so showing one does not scroll")
    void aTableWithoutRoomDoesNotScrollToShowARow() {
        Table table = table(40);
        table.setBounds(0, 0, 0, 0);
        value(table).clearDirty();

        table.showItem(table.getItem(30));

        assertThat(table.getTopIndex()).isZero();
        assertThat(value(table).changedKeys()).doesNotContain(VTable.TOP_INDEX);
    }

    @Test
    @DisplayName("an empty table is at its top and has nowhere to scroll")
    void anEmptyTableDoesNotScroll() {
        Table table = table(0);
        value(table).clearDirty();

        table.setTopIndex(3);

        assertThat(table.getTopIndex()).isZero();
        assertThat(value(table).changedKeys()).doesNotContain(VTable.TOP_INDEX);
    }

    @Test
    @DisplayName("a top index the rows cannot scroll to is the one asked for on GTK, and the top on Cocoa and Win32")
    void aTopIndexWithNowhereToScrollFollowsThePlatform() {
        Table table = table(2);

        table.setTopIndex(1);

        boolean keepsTheRequest = "gtk".equals(SWT.getPlatform());
        assertThat(table.getTopIndex()).isEqualTo(keepsTheRequest ? 1 : 0);
        assertThat(table.getItem(1).getBounds().y).isEqualTo(table.getItem(0).getBounds().y + table.getItemHeight());
    }

    @Test
    @DisplayName("an index past the last row scrolls to the top on Cocoa, stops at the last page on Win32 and is kept on GTK")
    void aTopIndexPastTheRowsFollowsThePlatform() {
        Table table = table(60);

        table.setTopIndex(20);
        table.setTopIndex(1000);
        int pastTheRows = table.getTopIndex();
        table.setTopIndex(20);
        table.setTopIndex(60);
        int firstWithoutARow = table.getTopIndex();
        table.setTopIndex(20);
        table.setTopIndex(59);
        int lastRow = table.getTopIndex();

        if ("cocoa".equals(SWT.getPlatform())) {
            assertThat(pastTheRows).isZero();
            assertThat(firstWithoutARow).isZero();
            assertThat(lastRow).isGreaterThan(0).isLessThan(59);
        } else if ("gtk".equals(SWT.getPlatform())) {
            assertThat(pastTheRows).isEqualTo(59);
            assertThat(firstWithoutARow).isEqualTo(59);
            assertThat(lastRow).isEqualTo(59);
        } else {
            assertThat(pastTheRows).isGreaterThan(0).isLessThan(59);
            assertThat(firstWithoutARow).isEqualTo(pastTheRows);
            assertThat(lastRow).isEqualTo(pastTheRows);
        }
    }

    @Test
    @DisplayName("an index below the first row is the first row on every platform")
    void aTopIndexBeforeTheRowsIsTheTop() {
        Table table = table(60);
        table.setTopIndex(20);

        table.setTopIndex(-5);

        assertThat(table.getTopIndex()).isZero();
    }

    @Test
    @DisplayName("setSelection(TableItem[]) shows the last item it was given on Cocoa, the first row of the selection elsewhere")
    void aSelectionOfItemsShowsTheRowThePlatformShows() {
        Table table = table(100, SWT.MULTI);
        TableItem[] rows = table.getItems();
        int visible = Math.max(1, (table.getClientArea().height - table.getHeaderHeight()) / table.getItemHeight());
        boolean cocoa = "cocoa".equals(SWT.getPlatform());

        table.setSelection(new TableItem[] { rows[80], rows[90] });
        int top = table.getTopIndex();
        int shown = cocoa ? 90 : 80;
        assertThat(top).isLessThanOrEqualTo(shown);
        assertThat(top + visible).isGreaterThan(shown);

        // The last item given, not the highest row: here Cocoa shows the same row as the others.
        table.setTopIndex(0);
        table.setSelection(new TableItem[] { rows[90], rows[80] });
        top = table.getTopIndex();
        assertThat(top).isLessThanOrEqualTo(80);
        assertThat(top + visible).isGreaterThan(80);
    }

    @Test
    @DisplayName("a top index the rows can scroll to is the same on every platform")
    void aTopIndexWithRoomToScrollIsTheOneAskedFor() {
        Table table = table(60);

        table.setTopIndex(20);

        assertThat(table.getTopIndex()).isEqualTo(20);
    }

    @Test
    @DisplayName("a scroll the client reports replaces the top index asked for")
    void aClientScrollReplacesTheTopIndexAskedFor() {
        Table table = table(2);
        table.setTopIndex(1);

        fromFlutter(table, "Scroll/Scroll", scrolledTo(0));
        runQueued();

        assertThat(table.getTopIndex()).isZero();
    }

    @Test
    @DisplayName("a top index asked for does not outlive the rows it named")
    void aTopIndexAskedForStopsAtTheRowsThatRemain() {
        Table table = table(3);
        table.setTopIndex(2);

        table.remove(1, 2);

        assertThat(table.getTopIndex()).isLessThan(table.getItemCount());
    }

    @Test
    @DisplayName("removing the rows brings the scroll back to the top")
    void removingTheRowsGoesBackToTheTop() {
        Table table = table(60);
        table.setTopIndex(30);

        table.removeAll();
        for (int i = 0; i < 60; i++)
            new TableItem(table, SWT.NONE);

        assertThat(table.getTopIndex()).isZero();
        assertThat(table.getItem(0).getBounds().y).isEqualTo(table.getHeaderHeight());
    }

    @Test
    @DisplayName("a scroll that arrives after the table was disposed is dropped")
    void aScrollForADisposedTableIsDropped() {
        Table table = table(5);
        fromFlutter(table, "Scroll/Scroll", scrolledTo(10));
        table.dispose();

        assertThatCode(this::runQueued).doesNotThrowAnyException();
    }
}
