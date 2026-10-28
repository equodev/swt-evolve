package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** A column that changes width reports its Resize, then a Move for each column shown after it. */
class TableColumnResizeMovesTest extends KeyRoutingTestBase {

    @Test
    @DisplayName("widening the first of three columns resizes it and moves the two after it, in the order shown")
    void resizeMovesTheColumnsAfterIt() {
        Table table = new Table(new Composite(shell(), SWT.NONE), SWT.NONE);
        List<String> events = new ArrayList<>();
        for (String text : new String[] {"Name", "Value", "Note"}) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(text);
            column.setWidth(60);
            column.addListener(SWT.Resize, e -> events.add("Resize " + text));
            column.addListener(SWT.Move, e -> events.add("Move " + text));
        }
        table.setColumnOrder(new int[] {0, 2, 1});
        events.clear();

        table.getColumn(0).setWidth(90);
        assertThat(events).containsExactly("Resize Name", "Move Note", "Move Value");

        events.clear();
        table.getColumn(1).setWidth(90);
        assertThat(events).containsExactly("Resize Value");
    }
}
