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

/** Removing rows drops the removed ones from a Table's selection and shifts the rest, as a native Table does. */
@ExtendWith(Mocks.class)
class TableSelectionAfterRemoveTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUpBridge() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDownBridge() {
        FlutterBridge.set(null);
    }

    private static Table table(int style, int rows) {
        Table table = new Table(new Composite(Mocks.shell(), SWT.NONE), style);
        for (int i = 0; i < rows; i++)
            new TableItem(table, SWT.NONE).setText("row " + i);
        return table;
    }

    private static VTable value(Table table) {
        return ((DartTable) table.getImpl()).getValue();
    }

    private static void assertNothingSelected(Table table) {
        assertThat(table.getSelectionIndex()).isEqualTo(-1);
        assertThat(table.getSelectionIndices()).isEmpty();
        assertThat(table.getSelectionCount()).isZero();
        assertThat(table.getSelection()).isEmpty();
    }

    @Test
    @DisplayName("removeAll() leaves nothing selected and tells the client the selection changed")
    void removeAllClearsTheSelection() {
        Table table = table(SWT.SINGLE, 3);
        table.setSelection(0);
        value(table).clearDirty();

        table.removeAll();

        assertNothingSelected(table);
        assertThat(value(table).changedKeys()).contains(VTable.SELECTION);
    }

    @Test
    @DisplayName("removing the selected first row leaves nothing selected")
    void removeSelectedFirstRow() {
        Table table = table(SWT.SINGLE, 3);
        table.setSelection(0);

        table.remove(0);

        assertNothingSelected(table);
    }

    @Test
    @DisplayName("removing the selected last row leaves nothing selected and tells the client")
    void removeSelectedLastRow() {
        Table table = table(SWT.SINGLE, 3);
        table.setSelection(2);
        value(table).clearDirty();

        table.remove(2);

        assertNothingSelected(table);
        assertThat(value(table).changedKeys()).contains(VTable.SELECTION);
    }

    @Test
    @DisplayName("disposing the selected last item leaves nothing selected and tells the client")
    void disposeSelectedLastItem() {
        Table table = table(SWT.SINGLE, 3);
        table.setSelection(2);
        value(table).clearDirty();

        table.getItem(2).dispose();

        assertNothingSelected(table);
        assertThat(value(table).changedKeys()).contains(VTable.SELECTION);
    }

    @Test
    @DisplayName("removing a row above the selection shifts it up and tells the client")
    void removeRowAboveTheSelection() {
        Table table = table(SWT.SINGLE, 3);
        table.setSelection(2);
        value(table).clearDirty();

        table.remove(0);

        assertThat(table.getSelectionIndices()).containsExactly(1);
        assertThat(table.getSelection()).extracting(TableItem::getText).containsExactly("row 2");
        assertThat(value(table).changedKeys()).contains(VTable.SELECTION);
    }

    @Test
    @DisplayName("remove(start, end) drops the selected rows in the range and shifts the ones past it")
    void removeRange() {
        Table table = table(SWT.MULTI, 6);
        table.setSelection(new int[] { 1, 2, 5 });
        value(table).clearDirty();

        table.remove(1, 3);

        assertThat(table.getSelectionIndices()).containsExactly(2);
        assertThat(table.getSelection()).extracting(TableItem::getText).containsExactly("row 5");
        assertThat(value(table).changedKeys()).contains(VTable.SELECTION);
    }

    @Test
    @DisplayName("remove(int[]) drops the selected rows it names, the last one included")
    void removeIndices() {
        Table table = table(SWT.MULTI, 5);
        table.setSelection(new int[] { 1, 3, 4 });

        table.remove(new int[] { 1, 4 });

        assertThat(table.getSelectionIndices()).containsExactly(2);
        assertThat(table.getSelection()).extracting(TableItem::getText).containsExactly("row 3");
    }

    @Test
    @DisplayName("shrinking the item count past the selected row leaves nothing selected and tells the client")
    void setItemCountBelowTheSelection() {
        Table table = table(SWT.SINGLE, 4);
        table.setSelection(3);
        value(table).clearDirty();

        table.setItemCount(2);

        assertNothingSelected(table);
        assertThat(value(table).changedKeys()).contains(VTable.SELECTION);
    }

    @Test
    @DisplayName("inserting a row above the selection shifts it down and tells the client")
    void insertRowAboveTheSelection() {
        Table table = table(SWT.SINGLE, 3);
        table.setSelection(1);
        value(table).clearDirty();

        new TableItem(table, SWT.NONE, 0).setText("new");

        assertThat(table.getSelectionIndices()).containsExactly(2);
        assertThat(table.getSelection()).extracting(TableItem::getText).containsExactly("row 1");
        assertThat(value(table).changedKeys()).contains(VTable.SELECTION);
    }
}
