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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/** The column places and the SWT.Move events {@link ColumnHelper} works out for a Tree and a Table. */
@Tag("native-unit")
class ColumnHelperTest {

    private Display display;
    private Shell shell;
    private final List<String> moved = new ArrayList<>();

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        // The mock display would swallow every event; its real dispatch only needs the listener table.
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        shell = DartMocks.dartShell(display);
    }

    @AfterEach
    void tearDown() {
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private <C extends Item> C watched(C column, String name) {
        column.setText(name);
        column.addListener(SWT.Move, e -> moved.add(name));
        return column;
    }

    private TreeColumn[] treeColumns(Tree tree) {
        TreeColumn[] columns = new TreeColumn[3];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = watched(new TreeColumn(tree, SWT.NONE), "c" + i);
            columns[i].setWidth(10 * (i + 1));
        }
        moved.clear();
        return columns;
    }

    private TableColumn[] tableColumns(Table table) {
        TableColumn[] columns = new TableColumn[3];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = watched(new TableColumn(table, SWT.NONE), "c" + i);
            columns[i].setWidth(10 * (i + 1));
        }
        moved.clear();
        return columns;
    }

    @Test
    @DisplayName("each column's left edge follows the order it is shown in, and an index outside the columns is skipped")
    void positions() {
        assertThat(ColumnHelper.positions(new int[] {10, 20, 30}, new int[] {2, 5, 0})).containsExactly(30, 0, 0);
        assertThat(ColumnHelper.positions(new int[] {10, 20, 30}, new int[] {1, 0})).as("an order of another length is the creation order")
                .containsExactly(0, 10, 30);
        assertThat(ColumnHelper.positions(new int[] {10, 20, 30}, null)).containsExactly(0, 10, 30);
        assertThat(ColumnHelper.positions(new int[] {10, 20, 30}, new int[] {2, -1, 0})).containsExactly(30, 0, 0);

        Tree tree = new Tree(shell, SWT.NONE);
        treeColumns(tree);
        assertThat(ColumnHelper.positions(tree, new int[] {1, 2, 0})).containsExactly(50, 0, 20);
        Table table = new Table(shell, SWT.NONE);
        tableColumns(table);
        assertThat(ColumnHelper.positions(table, new int[] {0, 1, 2})).containsExactly(0, 10, 30);
    }

    @Test
    @DisplayName("a column dragged to its own place, a column not shown and a column that does not move leave the order")
    void movedLeavesTheOrder() {
        int[] order = {0, 1, 2};
        assertThat(ColumnHelper.moved(order, 1, 1, true)).isNull();
        assertThat(ColumnHelper.moved(order, 3, 0, true)).isNull();
        assertThat(ColumnHelper.moved(order, 0, 2, false)).isNull();
        assertThat(ColumnHelper.moved(order, 0, 2, true)).containsExactly(1, 2, 0);
    }

    @Test
    @DisplayName("a column that is not shown has no place")
    void displayIndex() {
        assertThat(ColumnHelper.displayIndex(new int[] {2, 0, 1}, 0)).isEqualTo(1);
        assertThat(ColumnHelper.displayIndex(new int[] {2, 0, 1}, 3)).isEqualTo(-1);
    }

    @Test
    @DisplayName("resizing a column moves the columns shown after it, in a Tree and in a Table")
    void resizedColumn() {
        Tree tree = new Tree(shell, SWT.NONE);
        TreeColumn[] treeColumns = treeColumns(tree);
        ColumnHelper.columnResized(tree, treeColumns[0]);
        assertThat(moved).containsExactly("c1", "c2");

        moved.clear();
        Table table = new Table(shell, SWT.NONE);
        TableColumn[] tableColumns = tableColumns(table);
        ColumnHelper.columnResized(table, tableColumns[1]);
        assertThat(moved).containsExactly("c2");
    }

    @Test
    @DisplayName("a disposed column, a column of another tree and a disposed parent move nothing")
    void resizedColumnThatMovesNothing() {
        Tree tree = new Tree(shell, SWT.NONE);
        TreeColumn[] columns = treeColumns(tree);
        Tree other = new Tree(shell, SWT.NONE);
        TreeColumn foreign = new TreeColumn(other, SWT.NONE);
        ColumnHelper.columnResized(tree, foreign);
        ColumnHelper.columnResized(null, columns[0]);

        Table table = new Table(shell, SWT.NONE);
        TableColumn[] tableColumns = tableColumns(table);
        ColumnHelper.columnResized((Table) null, tableColumns[0]);
        TableColumn gone = new TableColumn(table, SWT.NONE);
        gone.dispose();
        moved.clear();
        ColumnHelper.columnResized(table, gone);
        assertThat(moved).isEmpty();

        table.dispose();
        moved.clear();
        ColumnHelper.columnResized(table, tableColumns[0]);
        assertThat(moved).isEmpty();
    }

    @Test
    @DisplayName("the columns shown from a place on are told they moved; a place before the first moves nothing")
    void shiftedColumns() {
        Tree tree = new Tree(shell, SWT.NONE);
        treeColumns(tree);
        ColumnHelper.columnsShiftedFrom(tree, 1);
        assertThat(moved).containsExactly("c1", "c2");

        moved.clear();
        Table table = new Table(shell, SWT.NONE);
        tableColumns(table);
        ColumnHelper.columnsShiftedFrom(table, 2);
        ColumnHelper.columnsShiftedFrom(table, -1);
        assertThat(moved).containsExactly("c2");
    }
}
