package dev.equo.jface;

import dev.equo.swt.Config;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Edit cell values in a table
 */
public class Snippet009CellEditors {

    public static void main(String[] args) {
//        Config.forceEclipse();
        Display display = new Display ();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        new Snippet009CellEditors(shell);
        shell.open ();

        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }

        display.dispose ();
    }

    public static class MyModel {
        public int counter;

        public MyModel(int counter) {
            this.counter = counter;
        }

        @Override
        public String toString() {
            return "Item " + this.counter;
        }
    }

    public Snippet009CellEditors(Shell shell) {
        final TableViewer v = new TableViewer(shell,SWT.BORDER|SWT.FULL_SELECTION);
        v.setContentProvider(ArrayContentProvider.getInstance());

        TableViewerColumn viewerColumn = new TableViewerColumn(v, SWT.NONE);
        viewerColumn.getColumn().setText("Column1");
        viewerColumn.getColumn().setWidth(300);
        viewerColumn.setLabelProvider(new ColumnLabelProvider());
        viewerColumn.setEditingSupport(new EditingSupport(v) {

            @Override
            protected void setValue(Object element, Object value) {
                ((MyModel) element).counter = Integer.parseInt(value.toString());
                getViewer().update(element, null);
            }

            @Override
            protected Object getValue(Object element) {
                return ((MyModel) element).counter + "";
            }

            @Override
            protected CellEditor getCellEditor(Object element) {
                return new TextCellEditor((Composite) getViewer().getControl());
            }

            @Override
            protected boolean canEdit(Object element) {
                return ((MyModel) element).counter % 2 == 0;
            }
        });


        MyModel[] model = createModel();
        v.setInput(model);
        v.getTable().setLinesVisible(true);
        v.getTable().setHeaderVisible(true);
    }

    private MyModel[] createModel() {
        MyModel[] elements = new MyModel[10];

        for( int i = 0; i < 10; i++ ) {
            elements[i] = new MyModel(i);
        }

        return elements;
    }
}