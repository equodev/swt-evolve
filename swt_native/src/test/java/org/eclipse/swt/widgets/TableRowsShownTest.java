package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The top row the client reports is the top index the table records for it, while the table still has that
 * row. It is where the user scrolled the client to, so it is kept without being sent back.
 */
@ExtendWith(Mocks.class)
class TableRowsShownTest {

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
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static Table table(int rows) {
        Table table = new Table(Mocks.shell(), SWT.NONE);
        for (int i = 0; i < rows; i++) {
            new TableItem(table, SWT.NONE).setText("row " + i);
        }
        return table;
    }

    private static void report(Table table, int top, int end) {
        Event e = new Event();
        e.index = top;
        e.end = end;
        TableHelper.rowsShown((DartTable) table.getImpl(), e);
    }

    private static int recordedTop(Table table) {
        return ((DartTable) table.getImpl())._topIndex();
    }

    @Test
    @DisplayName("the row the client shows at the top is recorded as the top index")
    void reportedTopBecomesTheTopIndex() {
        Table table = table(10);
        report(table, 4, 10);
        assertThat(recordedTop(table)).isEqualTo(4);
    }

    @Test
    @DisplayName("a report naming a row removed before it arrived leaves the top index")
    void rowRemovedSinceIsIgnored() {
        Table table = table(10);
        report(table, 2, 10);
        table.remove(5, 9);
        report(table, 7, 10);
        assertThat(recordedTop(table)).isEqualTo(2);
    }

    @Test
    @DisplayName("a report with no row leaves the top index")
    void noRowIsIgnored() {
        Table table = table(10);
        report(table, 3, 10);
        report(table, -1, 0);
        assertThat(recordedTop(table)).isEqualTo(3);
    }
}
