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
 * The "Add" toolbar item of a key/value {@link TableViewer} appends an empty row and immediately
 * puts that row's first cell into edit mode -- {@code data.add(row); refresh(); editElement(row, 0)}.
 * Every later Add, row click or Remove therefore runs against a table that already has a cell editor
 * open, and JFace tears that editor down through
 * {@link org.eclipse.jface.viewers.ColumnViewerEditor#cancelEditing()}, which keeps using the cell
 * editor's control after clearing it from the {@link org.eclipse.swt.custom.ControlEditor}.
 */
@Tag("flutter-it")
class TableViewerAddRowFlutterTest {

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

    /** What the toolbar's "Add" handler does: append an empty row, refresh, edit its first cell. */
    private Row addEmptyRow(TableViewer viewer) {
        Row row = new Row("", "");
        data.add(row);
        viewer.refresh();
        viewer.editElement(row, 0);
        return row;
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

    /** What the toolbar's "Remove" handler does. */
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
    @DisplayName("a second Add still appends its row after the first Add opened a cell editor")
    void addingASecondEmptyRowStillWorks() {
        TableViewer viewer = keyValueViewer();
        Table table = viewer.getTable();

        addEmptyRow(viewer);
        assertThat(table.getItemCount()).as("the first Add must append its row").isEqualTo(3);

        addEmptyRow(viewer);

        assertThat(table.getItemCount())
                .as("a second Add must append its row too, not be swallowed by the open cell editor")
                .isEqualTo(4);
    }

    @Test
    @DisplayName("the row a previous Add opened for editing can still be edited afterwards")
    void theAddedEmptyRowStaysEditable() {
        TableViewer viewer = keyValueViewer();

        Row added = addEmptyRow(viewer);
        Control editorControl = nameEditing.editorControl();
        assertThat(editorControl.isDisposed())
                .as("the cell editor's control is owned by the EditingSupport and outlives one edit")
                .isFalse();

        // The user clicks away, then comes back to the empty row to type in it.
        clickRow(viewer.getTable(), 0);
        viewer.editElement(added, 0);

        assertThat(nameEditing.editorControl().isDisposed())
                .as("re-editing the added row must not run against a disposed editor control")
                .isFalse();
    }

    @Test
    @DisplayName("Remove still deletes a row after an Add opened a cell editor")
    void removeWorksAfterAnAdd() {
        TableViewer viewer = keyValueViewer();
        Table table = viewer.getTable();

        addEmptyRow(viewer);

        clickRow(table, 0);
        removeSelectedRows(viewer);

        assertThat(table.getItemCount())
                .as("Remove must still delete a row once an Add left a cell editor open")
                .isEqualTo(2);
        assertThat(table.getItem(0).getText(0)).isEqualTo("User-Agent");
    }
}
