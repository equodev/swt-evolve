package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An owner-drawn table's rows are drawn again where its columns now stand once their order changes,
 * as native SWT repaints the cells a reorder moves.
 */
@Tag("flutter-it")
class TableColumnOrderOwnerDrawTest {

    private RecordingBridge bridge;
    private Display display;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a reorder of an owner-drawn table's columns draws each cell again at its column's new place")
    void aReorderRepaintsTheCellsAtTheirNewPlace() {
        Table table = new Table(new Shell(display), SWT.FULL_SELECTION);
        table.setBounds(0, 0, 400, 200);
        table.setHeaderVisible(true);
        for (String name : new String[] {"A", "B", "C"}) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(name);
            column.setWidth(100);
            column.setMoveable(true);
        }
        List<String> painted = new ArrayList<>();
        table.addListener(SWT.EraseItem, e -> e.detail &= ~SWT.FOREGROUND);
        table.addListener(SWT.PaintItem, e -> {
            TableItem item = (TableItem) e.item;
            Rectangle text = item.getTextBounds(e.index);
            e.gc.drawText(item.getText(e.index), text.x, text.y, true);
            if (e.x == item.getBounds(e.index).x) painted.add(item.getText(e.index) + "@" + e.x);
        });
        TableItem row = new TableItem(table, SWT.NONE);
        row.setText(new String[] {"a", "b", "c"});
        flush();
        mounted(table, row);
        flush();
        int b = row.getBounds(1).x;
        painted.clear();

        table.setColumnOrder(new int[] {1, 0, 2});
        flush();

        assertThat(row.getBounds(1).x).as("the moved column now starts the row").isLessThan(b);
        assertThat(painted).as("the cells drawn after the reorder")
                .contains("b@" + row.getBounds(1).x, "a@" + row.getBounds(0).x);
    }

    @Test
    @DisplayName("a resize of an owner-drawn table's column draws the cells it moves again")
    void aResizeRepaintsTheCellsItMoves() {
        Table table = new Table(new Shell(display), SWT.FULL_SELECTION);
        table.setBounds(0, 0, 400, 200);
        for (int i = 0; i < 2; i++) new TableColumn(table, SWT.NONE).setWidth(100);
        List<String> painted = new ArrayList<>();
        table.addListener(SWT.EraseItem, e -> e.detail &= ~SWT.FOREGROUND);
        table.addListener(SWT.PaintItem, e -> painted.add(((TableItem) e.item).getText(e.index) + "@" + e.x));
        TableItem row = new TableItem(table, SWT.NONE);
        row.setText(new String[] {"a", "b"});
        flush();
        mounted(table, row);
        flush();
        painted.clear();

        table.getColumn(0).setWidth(150);
        flush();

        assertThat(painted).contains("b@" + row.getBounds(1).x);
    }

    /** The client mounted {@code row}'s overlay. */
    private void mounted(Table table, TableItem row) {
        Event e = new Event();
        e.segments = new int[] {row.hashCode(), 0xFFFFFF};
        bridge.comm.fireContaining("Table/" + table.hashCode() + "/PaintItem/PaintItem", e);
    }

    private void flush() {
        while (display.readAndDispatch()) {
            // run the handlers' asyncExec and flush
        }
    }
}
