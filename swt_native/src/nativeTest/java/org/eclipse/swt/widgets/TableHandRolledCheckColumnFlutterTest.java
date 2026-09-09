package org.eclipse.swt.widgets;

import java.util.ArrayList;
import java.util.List;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
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
 * A check column rendered as a plain icon (not a widget), toggled by
 * a hand-rolled {@code Table.addMouseListener} that resolves the clicked row from the viewer's
 * *current selection* rather than from the mouse coordinates -- exactly how
 * {@code org.eclipse.wst.validation.ui.internal.preferences.ValidationPreferencePage$ValidatorListPage}
 * implements its per-row check icons. This only works if the table's selection has already moved
 * to the clicked row by the time this listener runs.
 */
@Tag("flutter-it")
class TableHandRolledCheckColumnFlutterTest {

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

    private static final class Validator {
        final String name;
        boolean manual;

        Validator(String name) {
            this.name = name;
        }
    }

    private final List<Validator> data = new ArrayList<>();

    private TableViewer validatorViewer() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());

        data.add(new Validator("Row 0"));
        data.add(new Validator("Row 1"));

        TableViewer viewer = new TableViewer(shell, SWT.FULL_SELECTION | SWT.BORDER);
        Table table = viewer.getTable();

        TableViewerColumn name = new TableViewerColumn(viewer, SWT.NONE);
        name.getColumn().setWidth(200);
        name.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Validator) element).name;
            }
        });

        TableViewerColumn manual = new TableViewerColumn(viewer, SWT.NONE);
        manual.getColumn().setWidth(60);
        manual.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Validator) element).manual ? "on" : "off";
            }
        });

        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setInput(data);

        // Mirrors ValidatorListPage$1: hit-test which COLUMN was clicked from (x, y), then resolve
        // the ROW from the viewer's current selection, not from the hit-tested item.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (e.button != 1)
                    return;
                TableItem item = table.getItem(new Point(e.x, e.y));
                if (item == null || item.isDisposed())
                    return;
                int column = -1;
                for (int i = 0; i < table.getColumnCount(); i++) {
                    if (item.getBounds(i).contains(e.x, e.y)) {
                        column = i;
                        break;
                    }
                }
                if (column != 1)
                    return;
                IStructuredSelection sel = viewer.getStructuredSelection();
                Validator v = (Validator) sel.getFirstElement();
                if (v == null)
                    return;
                v.manual = !v.manual;
            }
        });

        return viewer;
    }

    /** What Flutter sends when the user clicks the "Manual" column of a row: cell centre + row index. */
    private static void clickManualColumn(Table table, int row) {
        Event e = new Event();
        e.type = SWT.MouseDown;
        e.button = 1;
        e.count = 1;
        e.x = 200 + 30; // inside the second column (name column is 200 wide)
        e.y = table.getHeaderHeight() + row * table.getItemHeight() + table.getItemHeight() / 2;
        e.segments = new int[] { row };
        e.widget = table;
        table.notifyListeners(SWT.MouseDown, e);
    }

    @Test
    @DisplayName("clicking a different row's check icon toggles that row in one click, without disturbing the row that was already checked")
    void clickOnDifferentRowTogglesThatRowWithoutDisturbingTheOther() {
        TableViewer viewer = validatorViewer();
        Table table = viewer.getTable();

        // Cold start: nothing selected yet -- native SWT (and JFace's own activation gate, which
        // this same Table also has to serve) only selects on this very first click.
        clickManualColumn(table, 0);
        assertThat(table.getSelectionIndices()).containsExactly(0);
        assertThat(data.get(0).manual)
                .as("the very first click on the table, with nothing selected before it, only selects")
                .isFalse();

        // Second click on the same, now-selected row: toggles.
        clickManualColumn(table, 0);
        assertThat(data.get(0).manual).isTrue();

        // Now click row 1's check icon: a *different*, never-before-selected row, while row 0 is
        // still selected. It must toggle row 1 in this same click.
        clickManualColumn(table, 1);

        assertThat(table.getSelectionIndices())
                .as("the clicked row becomes the selection")
                .containsExactly(1);
        assertThat(data.get(1).manual)
                .as("clicking row 1's check icon must toggle row 1 in this same click")
                .isTrue();
        assertThat(data.get(0).manual)
                .as("row 0's already-confirmed checked state must survive a click on a different row")
                .isTrue();
    }
}
