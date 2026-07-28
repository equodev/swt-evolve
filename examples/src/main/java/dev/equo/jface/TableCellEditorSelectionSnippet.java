package dev.equo.jface;

import java.util.EventObject;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TableCellEditorSelectionSnippet {

    private static final int ACTIVATION_BIT_MASK = ColumnViewerEditor.TABBING_HORIZONTAL
            | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.KEYBOARD_ACTIVATION;

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Table cell editor / selection (#841)");
        shell.setLayout(new FillLayout());
        shell.setSize(600, 500);

        final TableViewer tableViewer = new TableViewer(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);

        TableViewerColumn nameCol = new TableViewerColumn(tableViewer, SWT.NONE);
        nameCol.getColumn().setText("Name");
        nameCol.getColumn().setWidth(260);
        nameCol.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return String.valueOf(element);
            }
        });
        nameCol.setEditingSupport(new EditingSupport(tableViewer) {
            @Override
            protected CellEditor getCellEditor(Object element) {
                return new TextCellEditor(tableViewer.getTable());
            }

            @Override
            protected boolean canEdit(Object element) {
                return true;
            }

            @Override
            protected Object getValue(Object element) {
                return String.valueOf(element);
            }

            @Override
            protected void setValue(Object element, Object value) {
            }
        });

        TableViewerColumn typeCol = new TableViewerColumn(tableViewer, SWT.NONE);
        typeCol.getColumn().setText("Value Type");
        typeCol.getColumn().setWidth(160);
        typeCol.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return "String";
            }
        });

        final TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tableViewer,
                new FocusCellOwnerDrawHighlighter(tableViewer));

        ColumnViewerEditorActivationStrategy activationStrategy = new ColumnViewerEditorActivationStrategy(tableViewer) {
            @Override
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                switch (event.eventType) {
                    case ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION:
                        Object src = event.getSource();
                        return src instanceof ViewerCell && src.equals(focusCellManager.getFocusCell())
                                && isLeftClick(event.sourceEvent);
                    case ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION:
                        return isLeftClick(event.sourceEvent);
                    case ColumnViewerEditorActivationEvent.KEY_PRESSED:
                        return event.keyCode == SWT.CR;
                    case ColumnViewerEditorActivationEvent.PROGRAMMATIC:
                        return true;
                }
                return false;
            }

            private boolean isLeftClick(EventObject e) {
                return e instanceof MouseEvent && ((MouseEvent) e).button == 1;
            }
        };

        TableViewerEditor.create(tableViewer, focusCellManager, activationStrategy, ACTIVATION_BIT_MASK);

        String[] rows = new String[40];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = "variable_" + i;
        }
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        tableViewer.setInput(rows);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
