package org.eclipse.swt.widgets;

import java.util.ArrayList;
import java.util.List;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
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
 * A {@link TableViewer} whose first column toggles a boolean through a {@link CheckboxCellEditor}
 * -- the shape JFace applications use for a "check" column. The editor has no control of its own:
 * activating it flips the value, so the toggle depends entirely on JFace resolving the clicked
 * cell from the mouse coordinates the Table reports.
 */
@Tag("flutter-it")
class TableViewerCheckboxCellEditorFlutterTest {

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

    private static final class Property {
        final String name;
        boolean selected;

        Property(String name, boolean selected) {
            this.name = name;
            this.selected = selected;
        }
    }

    private static final class SelectedEditingSupport extends EditingSupport {
        private final CheckboxCellEditor editor;

        SelectedEditingSupport(TableViewer viewer) {
            super(viewer);
            this.editor = new CheckboxCellEditor(viewer.getTable());
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            return ((Property) element).selected;
        }

        @Override
        protected void setValue(Object element, Object value) {
            ((Property) element).selected = (Boolean) value;
            getViewer().update(element, null);
        }
    }

    private static final int CHECK_COLUMN_WIDTH = 60;
    private static final int NAME_COLUMN_WIDTH = 200;

    private final List<Property> data = new ArrayList<>();

    private TableViewer propertiesViewer() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());

        data.add(new Property("name", false));
        data.add(new Property("id", false));
        data.add(new Property("class", false));

        TableViewer viewer = new TableViewer(shell, SWT.FULL_SELECTION | SWT.BORDER);
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableViewerColumn check = new TableViewerColumn(viewer, SWT.NONE);
        check.getColumn().setText("");
        check.getColumn().setWidth(CHECK_COLUMN_WIDTH);
        check.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return "";
            }
        });
        check.setEditingSupport(new SelectedEditingSupport(viewer));

        TableViewerColumn name = new TableViewerColumn(viewer, SWT.NONE);
        name.getColumn().setText("Name");
        name.getColumn().setWidth(NAME_COLUMN_WIDTH);
        name.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Property) element).name;
            }
        });

        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setInput(data);
        return viewer;
    }

    /** What Flutter sends when the user clicks the check cell of a row: cell centre + row index. */
    private static void clickCheckCell(Table table, int row) {
        Event e = new Event();
        e.type = SWT.MouseDown;
        e.button = 1;
        e.count = 1;
        e.x = CHECK_COLUMN_WIDTH / 2;
        e.y = table.getHeaderHeight() + row * table.getItemHeight() + table.getItemHeight() / 2;
        e.segments = new int[] { row };
        e.widget = table;
        table.notifyListeners(SWT.MouseDown, e);
    }

    @Test
    @DisplayName("a single click on an unselected row's check cell toggles it once the table has a selection")
    void singleClickTogglesAnUnselectedRow() {
        TableViewer viewer = propertiesViewer();
        Table table = viewer.getTable();

        // The very first click has nothing selected yet: JFace's default activation strategy needs a
        // single-element selection, so this click only selects -- native behaves the same way.
        clickCheckCell(table, 0);
        assertThat(table.getSelectionIndices()).containsExactly(0);

        clickCheckCell(table, 1);

        assertThat(table.getSelectionIndices())
                .as("the clicked row becomes the selection")
                .containsExactly(1);
        assertThat(data.get(1).selected)
                .as("one click on row 1's check cell must toggle it, not just select the row")
                .isTrue();
    }

    @Test
    @DisplayName("a click that changes the selection still reaches the MouseDown listeners behind it")
    void mouseDownDispatchSurvivesTheSelectionEvent() {
        TableViewer viewer = propertiesViewer();
        Table table = viewer.getTable();

        int[] seen = { 0 };
        table.addListener(SWT.MouseDown, e -> seen[0]++);

        clickCheckCell(table, 0);
        clickCheckCell(table, 1);

        assertThat(seen[0])
                .as("a listener behind the one that turns the click into a selection must still run")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("a second click on the same check cell toggles it back")
    void clickingTheSameRowTwiceTogglesTwice() {
        TableViewer viewer = propertiesViewer();
        Table table = viewer.getTable();

        clickCheckCell(table, 0);
        clickCheckCell(table, 0);
        assertThat(data.get(0).selected).isTrue();

        clickCheckCell(table, 0);
        assertThat(data.get(0).selected).isFalse();
    }
}
