package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Table's rows are laid out and scrolled on the Dart side, and Flutter reports how far with a
 * Scroll event. Java's coordinate lookups -- {@code getItem(Point)} and an item's bounds -- have to
 * take that distance off: a drag source resolves its item from the press coordinates, so without it
 * a drag started on the row under the pointer picked the row that sat there before the scroll.
 */
@Tag("flutter-it")
class TableGetItemFollowsScrollTest {

    private static final int ROWS = 40;
    private static final int SCROLLED_ROWS = 7;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    private RecordingBridge bridge;
    // A real Display: a mocked one drops the asyncExec the Scroll hook queues.
    private Display display;
    private Table table;
    private int rowHeight;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        table = new Table(new Shell(display), SWT.NONE);
        table.setBounds(0, 0, 200, 100);
        for (int i = 0; i < ROWS; i++) {
            new TableItem(table, SWT.NONE).setText("row " + i);
        }
        rowHeight = table.getItemHeight();
        assertThat(rowHeight).isPositive();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    /** What Flutter sends once the user has scrolled the table by {@code pixels}. */
    private void scrolledTo(int pixels) {
        Event scroll = new Event();
        scroll.y = pixels;
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) table.getImpl(), "Scroll", "Scroll"), scroll);
        while (display.readAndDispatch()) {
            // drain the asyncExec the hook queues
        }
    }

    private Point firstRowMiddle() {
        return new Point(10, table.getHeaderHeight() + rowHeight / 2);
    }

    @Test
    void getItem_resolves_the_row_under_the_pointer_after_a_scroll() {
        assertThat(table.getItem(firstRowMiddle())).as("before any scroll").isSameAs(table.getItem(0));

        scrolledTo(SCROLLED_ROWS * rowHeight);

        assertThat(table.getItem(firstRowMiddle())).as("after the scroll").isSameAs(table.getItem(SCROLLED_ROWS));
    }

    @Test
    void an_items_bounds_move_up_by_the_scrolled_distance() {
        scrolledTo(SCROLLED_ROWS * rowHeight);

        int header = table.getHeaderHeight();
        assertThat(table.getItem(SCROLLED_ROWS).getBounds().y).as("the first visible row").isEqualTo(header);
        assertThat(table.getItem(SCROLLED_ROWS).getBounds(0).y).as("its first cell").isEqualTo(header);
        assertThat(table.getItem(0).getBounds().y).as("a row scrolled out above").isEqualTo(header - SCROLLED_ROWS * rowHeight);
    }

    @Test
    void scrolling_back_to_the_top_restores_the_lookup() {
        scrolledTo(SCROLLED_ROWS * rowHeight);
        scrolledTo(0);

        assertThat(table.getItem(firstRowMiddle())).isSameAs(table.getItem(0));
        assertThat(table.getItem(0).getBounds().y).isEqualTo(table.getHeaderHeight());
    }
}
