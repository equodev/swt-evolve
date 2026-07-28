package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Tag("flutter-it")
class TableCellEditorSelectionSyncFlutterTest {

    private Display display;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private Table editableTable(int rows) {
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.FULL_SELECTION);
        for (int i = 0; i < rows; i++) {
            new TableItem(table, SWT.NONE).setText("row " + i);
        }
        return table;
    }

    private static void mouseDownOnRow(Table table, int row) {
        Event e = new Event();
        e.type = SWT.MouseDown;
        e.button = 1;
        e.segments = new int[] { row };
        e.widget = table;
        table.notifyListeners(SWT.MouseDown, e);
    }

    @Test
    @DisplayName("rapid clicks keep the selection on the row that was clicked (issue #841)")
    void rapidClicksKeepSelectionOnClickedRow() {
        Table table = editableTable(3);

        List<Integer> selectionEvents = new ArrayList<>();
        table.addListener(SWT.Selection, e -> selectionEvents.add(table.getSelectionIndex()));

        mouseDownOnRow(table, 0);
        mouseDownOnRow(table, 1);

        assertThat(table.getSelectionIndices())
                .as("selection must follow the last clicked row, not stay on the first")
                .containsExactly(1);
        assertThat(selectionEvents)
                .as("SWT.Selection must fire in the mouse-down dispatch, passing through the clicked rows")
                .contains(0);
        assertThat(selectionEvents.get(selectionEvents.size() - 1))
                .as("the last Selection fired is for the row actually clicked last")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("re-clicking the already-selected row fires no redundant Selection (issue #841)")
    void reclickingSameRowIsANoOp() {
        Table table = editableTable(3);

        List<Integer> selectionEvents = new ArrayList<>();
        table.addListener(SWT.Selection, e -> selectionEvents.add(table.getSelectionIndex()));

        mouseDownOnRow(table, 2);
        int afterFirstClick = selectionEvents.size();
        mouseDownOnRow(table, 2);
        int afterSecondClick = selectionEvents.size();

        assertThat(table.getSelectionIndices()).containsExactly(2);
        assertThat(afterFirstClick)
                .as("the first click selects the row and fires Selection")
                .isGreaterThan(0);
        assertThat(afterSecondClick - afterFirstClick)
                .as("the second click on the already-selected row must not re-fire Selection")
                .isZero();
    }

    @Test
    @DisplayName("installing a new cell editor disposes the previous one (issue #841)")
    void replacingTheCellEditorDisposesThePrevious() {
        Table table = editableTable(2);
        TableItem row0 = table.getItem(0);
        TableItem row1 = table.getItem(1);

        TableEditor editor = new TableEditor(table);
        Text first = new Text(table, SWT.NONE);
        editor.setEditor(first, row0, 0);
        assertThat(first.isDisposed()).as("the freshly installed editor is alive").isFalse();

        Text second = new Text(table, SWT.NONE);
        editor.setEditor(second, row1, 0);

        assertThat(first.isDisposed())
                .as("the previous cell editor control must be disposed so it can't linger over the table")
                .isTrue();
        assertThat(second.isDisposed()).as("the new editor stays alive").isFalse();
    }
}
