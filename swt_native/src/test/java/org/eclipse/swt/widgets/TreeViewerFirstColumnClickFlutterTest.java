package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A click on the right part of a tree's first cell, as the client draws it, lands in that column for
 * JFace too. The Expressions view opens its inline editor on a click in the name column of the
 * "Add new expression" row; {@code ColumnViewer} picks the column with {@code TreeItem.getBounds(int)},
 * so a first column drawn wider than Java lays it out sends that click to the value column, which
 * has no editor.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TreeViewerFirstColumnClickFlutterTest {

    private static final String ADD_NEW = "Add new expression";

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 400, 300);
        shell.open();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @AfterEach
    void disposeTrees() {
        for (Control child : shell.getChildren())
            child.dispose();
        settle();
    }

    private void settle() {
        int quiet = 0;
        for (int round = 0; round < 40 && quiet < 3; round++) {
            long before = flutter.dispatched();
            flutter.drain();
            flutter.flush();
            flutter.drain();
            quiet = flutter.dispatched() == before ? quiet + 1 : 0;
        }
    }

    @Test
    void a_click_on_the_right_part_of_the_first_cell_activates_its_editor() {
        TreeViewer viewer = new TreeViewer(shell, SWT.SINGLE | SWT.FULL_SELECTION);
        Tree tree = viewer.getTree();
        tree.setBounds(0, 0, 380, 200);
        tree.setHeaderVisible(true);
        TreeViewerColumn name = column(viewer, "Name", 150);
        NameEditing editing = new NameEditing(viewer);
        name.setEditingSupport(editing);
        column(viewer, "Value", 120);
        // The Expressions view's activation: a click on the cell, no double-click or slow second click.
        TreeViewerEditor.create(viewer, new ColumnViewerEditorActivationStrategy(viewer) {
            @Override
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION;
            }
        }, ColumnViewerEditor.DEFAULT);
        viewer.setContentProvider(new ITreeContentProvider() {
            public Object[] getElements(Object input) {
                return ((List<?>) input).toArray();
            }

            public Object[] getChildren(Object parent) {
                return new Object[0];
            }

            public Object getParent(Object element) {
                return null;
            }

            public boolean hasChildren(Object element) {
                return false;
            }
        });
        viewer.setInput(List.of(ADD_NEW));
        settle();

        List<Integer> activatedColumns = new ArrayList<>();
        viewer.getColumnViewerEditor().addEditorActivationListener(new ColumnViewerEditorActivationListener() {
            @Override
            public void beforeEditorActivated(ColumnViewerEditorActivationEvent event) {
            }

            @Override
            public void afterEditorActivated(ColumnViewerEditorActivationEvent event) {
                activatedColumns.add(((ViewerCell) event.getSource()).getColumnIndex());
            }

            @Override
            public void beforeEditorDeactivated(ColumnViewerEditorDeactivationEvent event) {
            }

            @Override
            public void afterEditorDeactivated(ColumnViewerEditorDeactivationEvent event) {
            }
        });
        List<Point> presses = new ArrayList<>();
        tree.addListener(SWT.MouseDown, e -> presses.add(new Point(e.x, e.y)));
        TreeItem row = tree.getItem(0);
        // The right part of the first column as the client draws it: the end of its header label.
        double[] header = flutter.renderedRect(name.getColumn());
        double[] rowRect = flutter.renderedRect(row);
        flutter.input().click(header[0] + header[2] - 4, rowRect[1] + rowRect[3] / 2);
        settle();

        assertThat(presses).as("the click reached the tree").hasSize(1);
        Point press = presses.get(0);
        assertThat(tree.getItem(press)).as("the row under the click").isSameAs(row);
        assertThat(row.getBounds(0).contains(press))
                .as("getBounds(0) %s holds the press %s", row.getBounds(0), press)
                .isTrue();
        assertThat(row.getBounds(1).contains(press)).as("getBounds(1) does not").isFalse();
        assertThat(activatedColumns).as("the first column's editor was activated").containsExactly(0);
        assertThat(viewer.isCellEditorActive()).as("the editor is still open once the click settles").isTrue();
        Control editorText = editing.editor.getControl();
        assertThat(display.getFocusControl()).as("the focus control").isSameAs(editorText);
        assertThat(flutter.devTools().evaluate("window.evolveTest.queryPrimaryFocus()").getAsString())
                .as("the client's keyboard focus").isEqualTo("Text/" + editorText.hashCode());
        viewer.cancelEditing();
    }

    @Test
    void after_scrolling_sideways_a_click_lands_in_the_column_drawn_under_it() {
        Tree tree = new Tree(shell, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        tree.setBounds(0, 0, 220, 160);
        tree.setHeaderVisible(true);
        TreeColumn[] columns = new TreeColumn[3];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = new TreeColumn(tree, SWT.NONE);
            columns[i].setText("Column " + i);
            columns[i].setWidth(150);
        }
        TreeItem row = new TreeItem(tree, SWT.NONE);
        row.setText(new String[] {"a", "b", "c"});
        settle();
        double[] rowRect = flutter.renderedRect(row);
        double[] treeRect = flutter.renderedRect(tree);
        flutter.input().wheel(treeRect[0] + 100, rowRect[1] + rowRect[3] / 2, 120, 0, SWT.NONE);
        settle();

        List<Point> presses = new ArrayList<>();
        tree.addListener(SWT.MouseDown, e -> presses.add(new Point(e.x, e.y)));
        // The middle of the second column where the client now draws it, its header having
        // scrolled along with the rows.
        double[] header = flutter.renderedRect(columns[1]);
        assertThat(header[0]).as("the second column scrolled left").isLessThan(treeRect[0] + 150);
        flutter.input().click(header[0] + header[2] / 2, rowRect[1] + rowRect[3] / 2);
        settle();

        assertThat(presses).as("the click reached the tree").hasSize(1);
        Point press = presses.get(0);
        assertThat(tree.getItem(press)).as("the row under the click").isSameAs(row);
        assertThat(row.getBounds(1).contains(press))
                .as("getBounds(1) %s holds the press %s", row.getBounds(1), press)
                .isTrue();
        assertThat(row.getBounds(0).contains(press))
                .as("getBounds(0) %s does not hold the press %s", row.getBounds(0), press)
                .isFalse();
    }

    private static TreeViewerColumn column(TreeViewer viewer, String title, int width) {
        TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
        column.getColumn().setText(title);
        column.getColumn().setWidth(width);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return "Name".equals(title) ? String.valueOf(element) : "";
            }
        });
        return column;
    }

    private static final class NameEditing extends EditingSupport {
        private final TextCellEditor editor;

        NameEditing(TreeViewer viewer) {
            super(viewer);
            editor = new TextCellEditor(viewer.getTree());
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return ADD_NEW.equals(element);
        }

        @Override
        protected Object getValue(Object element) {
            return "";
        }

        @Override
        protected void setValue(Object element, Object value) {
        }
    }
}
