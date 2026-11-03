package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Table whose columns are wider than the table scrolls sideways on the Dart side, which reports
 * the offset as the Scroll event's x. A cell's bounds are in the table's client area, so they and
 * {@code getItem(Point)} have to take that offset off the column positions.
 */
@Tag("flutter-it")
class TableGetItemFollowsHorizontalScrollTest {

    private static final int[] WIDTHS = {195, 1479, 106};
    private static final int TABLE_WIDTH = 587;
    private static final int SCROLLED_X = 1300;

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

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        table = new Table(new Shell(display), SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        for (int w : WIDTHS) {
            new TableColumn(table, SWT.NONE).setWidth(w);
        }
        table.setBounds(0, 0, TABLE_WIDTH, 200);
        for (int i = 0; i < 3; i++) {
            new TableItem(table, SWT.NONE).setText(new String[] {"jre " + i, "/opt/jre" + i, "Standard VM"});
        }
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    /** What Flutter sends once the user has scrolled the table sideways by {@code pixels}. */
    private void scrolledAcross(int pixels) {
        Event scroll = new Event();
        scroll.x = pixels;
        scroll.y = 0;
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) table.getImpl(), "Scroll", "Scroll"), scroll);
        while (display.readAndDispatch()) {
            // drain the asyncExec the hook queues
        }
    }

    @Test
    void a_cells_bounds_move_left_by_the_scrolled_distance() {
        int typeLeft = table.getItem(0).getBounds(2).x;

        scrolledAcross(SCROLLED_X);

        assertThat(table.getItem(0).getBounds(2).x).as("the Type cell after a sideways scroll")
            .isEqualTo(typeLeft - SCROLLED_X);
    }

    @Test
    void getItem_finds_the_row_at_a_point_in_the_scrolled_in_column() {
        scrolledAcross(SCROLLED_X);

        Rectangle type = table.getItem(1).getBounds(2);
        assertThat(type.x).as("the Type cell is in view").isBetween(0, TABLE_WIDTH);
        Point inTypeCell = new Point(type.x + type.width / 2, type.y + type.height / 2);
        assertThat(table.getItem(inTypeCell)).as("the row under the point").isSameAs(table.getItem(1));
    }
}
