package org.eclipse.swt.widgets;

import java.util.ArrayList;
import java.util.List;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A key/value {@link TableViewer} whose toolbar "Remove" deletes the rows the user picked: the
 * item's enablement comes from an {@link org.eclipse.jface.viewers.ISelectionChangedListener} and
 * its handler reads {@link Table#getSelectionIndices()}, so a row click has to land on the SWT
 * Table itself, not only on the Flutter side.
 */
@Tag("flutter-it")
class TableViewerRemoveSelectedRowFlutterTest {

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
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    private static final class Row {
        String name;
        String value;

        Row(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private static final class RowEditingSupport extends EditingSupport {
        private final TextCellEditor editor;
        private final boolean nameColumn;

        RowEditingSupport(TableViewer viewer, boolean nameColumn) {
            super(viewer);
            this.editor = new TextCellEditor(viewer.getTable());
            this.nameColumn = nameColumn;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        Control editorControl() {
            return editor.getControl();
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            Row row = (Row) element;
            return nameColumn ? row.name : row.value;
        }

        @Override
        protected void setValue(Object element, Object value) {
            Row row = (Row) element;
            if (nameColumn) {
                row.name = String.valueOf(value);
            } else {
                row.value = String.valueOf(value);
            }
            getViewer().update(element, null);
        }
    }

    private final List<Row> data = new ArrayList<>();

    private RowEditingSupport nameEditing;

    private TableViewer keyValueViewer() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());

        data.add(new Row("Cookie", "multipart/form-data"));
        data.add(new Row("User-Agent", "application/javascript"));

        TableViewer viewer = new TableViewer(shell,
                SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // The viewer creates a TableEditor up front and only attaches it to a cell while editing.
        new TableEditor(table);

        nameEditing = addColumn(viewer, "Name", 400, true);
        addColumn(viewer, "Value", 500, false);

        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setInput(data);
        return viewer;
    }

    private static RowEditingSupport addColumn(TableViewer viewer, String title, int width, boolean nameColumn) {
        TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
        column.getColumn().setText(title);
        column.getColumn().setWidth(width);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Row row = (Row) element;
                return nameColumn ? row.name : row.value;
            }
        });
        RowEditingSupport editing = new RowEditingSupport(viewer, nameColumn);
        column.setEditingSupport(editing);
        return editing;
    }

    /** What Flutter sends when the user clicks a row. */
    private static void clickRow(Table table, int row) {
        Event e = new Event();
        e.type = SWT.MouseDown;
        e.button = 1;
        e.segments = new int[] { row };
        e.widget = table;
        table.notifyListeners(SWT.MouseDown, e);
    }

    /** What the toolbar's Remove handler does. */
    private void removeSelectedRows(TableViewer viewer) {
        List<Row> doomed = new ArrayList<>();
        for (int index : viewer.getTable().getSelectionIndices()) {
            doomed.add(data.get(index));
        }
        if (doomed.isEmpty()) {
            return;
        }
        data.removeAll(doomed);
        viewer.refresh();
    }

    @Test
    @DisplayName("clicking a row arms the Remove item and Remove deletes that row")
    void clickingARowThenRemovingDeletesIt() {
        TableViewer viewer = keyValueViewer();
        Table table = viewer.getTable();

        boolean[] removeEnabled = { false };
        viewer.addSelectionChangedListener(e -> removeEnabled[0] = table.getSelectionCount() > 0);

        clickRow(table, 0);

        assertThat(table.getSelectionIndices())
                .as("the clicked row must be the Table's selection, not only Flutter's")
                .containsExactly(0);
        assertThat(removeEnabled[0])
                .as("the viewer's selection listener must fire so the Remove item enables")
                .isTrue();

        removeSelectedRows(viewer);

        assertThat(table.getItemCount())
                .as("Remove must delete the selected row")
                .isEqualTo(1);
        assertThat(table.getItem(0).getText(0))
                .as("the surviving row is the one that was not selected")
                .isEqualTo("User-Agent");
    }

    @Test
    @DisplayName("committing an open cell editor by focus loss leaves the viewer usable")
    void committingAnEditorByFocusLossLeavesTheViewerUsable() {
        TableViewer viewer = keyValueViewer();
        Table table = viewer.getTable();

        clickRow(table, 1);
        viewer.editElement(data.get(1), 0);
        Control cellEditorControl = nameEditing.editorControl();

        // Flutter reports the editor losing focus when the user clicks elsewhere; JFace commits the
        // value and then removes its own listeners from this control.
        cellEditorControl.notifyListeners(SWT.FocusOut, new Event());

        clickRow(table, 0);
        removeSelectedRows(viewer);

        assertThat(table.getItemCount())
                .as("Remove must still work after an editor was committed by focus loss")
                .isEqualTo(1);
        assertThat(table.getItem(0).getText(0)).isEqualTo("User-Agent");
    }

    @Test
    @DisplayName("a row clicked while another row is being edited becomes the selection")
    void clickingAwayFromAnEditedRowMovesTheSelection() {
        TableViewer viewer = keyValueViewer();
        Table table = viewer.getTable();

        clickRow(table, 1);
        viewer.editElement(data.get(1), 0);

        clickRow(table, 0);

        assertThat(table.getSelectionIndices())
                .as("clicking another row while a cell editor is open must move the selection")
                .containsExactly(0);

        removeSelectedRows(viewer);

        assertThat(table.getItemCount()).isEqualTo(1);
        assertThat(table.getItem(0).getText(0)).isEqualTo("User-Agent");
    }
}
