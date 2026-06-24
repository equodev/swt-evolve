package dev.equo.jface;

import dev.equo.swt.Config;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.List;

/**
 * JFace TableViewer with per-column cell editors (Text, ComboBox, Checkbox).
 * Exercises getItem(Point) and getBounds(index) via the JFace editing framework.
 */
public class JFaceTableWithCellEditorsSnippet {

    static final String[] ROLES = {"Developer", "Designer", "Manager", "QA"};

    public static void main(String[] args) {
        Config.forceEclipse();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("JFace Table - Cell Editors");
        shell.setSize(600, 400);
        shell.setLayout(new FillLayout());

        new JFaceTableWithCellEditorsSnippet(shell);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    public static class Person {
        public String name;
        public int roleIndex;
        public boolean active;

        public Person(String name, int roleIndex, boolean active) {
            this.name = name;
            this.roleIndex = roleIndex;
            this.active = active;
        }
    }

    public JFaceTableWithCellEditorsSnippet(Shell shell) {
        final TableViewer viewer = new TableViewer(shell, SWT.BORDER | SWT.FULL_SELECTION);
        viewer.setContentProvider(ArrayContentProvider.getInstance());

        // --- Name column (TextCellEditor) ---
        TableViewerColumn nameCol = new TableViewerColumn(viewer, SWT.NONE);
        nameCol.getColumn().setText("Name");
        nameCol.getColumn().setWidth(200);
        nameCol.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Person) element).name;
            }
        });
        nameCol.setEditingSupport(new EditingSupport(viewer) {
            @Override
            protected CellEditor getCellEditor(Object element) {
                return new TextCellEditor((Composite) viewer.getControl());
            }

            @Override
            protected boolean canEdit(Object element) {
                return true;
            }

            @Override
            protected Object getValue(Object element) {
                return ((Person) element).name;
            }

            @Override
            protected void setValue(Object element, Object value) {
                ((Person) element).name = (String) value;
                viewer.update(element, null);
            }
        });

        // --- Role column (ComboBoxCellEditor) ---
        TableViewerColumn roleCol = new TableViewerColumn(viewer, SWT.NONE);
        roleCol.getColumn().setText("Role");
        roleCol.getColumn().setWidth(150);
        roleCol.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                int idx = ((Person) element).roleIndex;
                return (idx >= 0 && idx < ROLES.length) ? ROLES[idx] : "";
            }
        });
        roleCol.setEditingSupport(new EditingSupport(viewer) {
            @Override
            protected CellEditor getCellEditor(Object element) {
                return new ComboBoxCellEditor(viewer.getTable(), ROLES, SWT.READ_ONLY);
            }

            @Override
            protected boolean canEdit(Object element) {
                return true;
            }

            @Override
            protected Object getValue(Object element) {
                return ((Person) element).roleIndex;
            }

            @Override
            protected void setValue(Object element, Object value) {
                ((Person) element).roleIndex = (Integer) value;
                viewer.update(element, null);
            }
        });

        // --- Active column (CheckboxCellEditor) ---
        TableViewerColumn activeCol = new TableViewerColumn(viewer, SWT.NONE);
        activeCol.getColumn().setText("Active");
        activeCol.getColumn().setWidth(80);
        activeCol.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Person) element).active ? "Yes" : "No";
            }
        });
        activeCol.setEditingSupport(new EditingSupport(viewer) {
            @Override
            protected CellEditor getCellEditor(Object element) {
                return new CheckboxCellEditor(viewer.getTable());
            }

            @Override
            protected boolean canEdit(Object element) {
                return true;
            }

            @Override
            protected Object getValue(Object element) {
                return ((Person) element).active;
            }

            @Override
            protected void setValue(Object element, Object value) {
                ((Person) element).active = (Boolean) value;
                viewer.update(element, null);
            }
        });

        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        viewer.setInput(createModel());
    }

    private List<Person> createModel() {
        List<Person> people = new ArrayList<>();
        people.add(new Person("Alice", 0, true));
        people.add(new Person("Bob", 2, false));
        people.add(new Person("Carol", 1, true));
        people.add(new Person("Dave", 3, true));
        people.add(new Person("Eve", 0, false));
        return people;
    }
}