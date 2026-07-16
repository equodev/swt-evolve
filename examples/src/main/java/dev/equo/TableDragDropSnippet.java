package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Reproduces issue #755: dragging a row within a Table to reorder it, using the same
 * TableViewer#addDragSupport / #addDropSupport idiom as Katalon's
 * PrioritizeSelectionMethodsComposite (DND.DROP_MOVE + TextTransfer, index-as-string payload).
 */
public class TableDragDropSnippet {

    public static void main(String[] args) {
        Config.useEquo(Table.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        shell.setText("TableDragDropSnippet");
        shell.setSize(300, 300);

        Label hint = new Label(shell, SWT.NONE);
        hint.setText("Drag a row and drop it on another row to reorder.");

        Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);

        List<String> order = new ArrayList<>(List.of(
                "id", "name", "css selector", "xpath", "link text"));

        for (String s : order) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(s);
        }

        DragSource dragSource = new DragSource(table, DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[]{TextTransfer.getInstance()});
        dragSource.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                int index = table.getSelectionIndex();
                event.data = String.valueOf(index);
            }
        });

        DropTarget dropTarget = new DropTarget(table, DND.DROP_MOVE);
        dropTarget.setTransfer(new Transfer[]{TextTransfer.getInstance()});
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                if (event.data == null) return;
                // event.item == null means the drop landed past the last row (there is no
                // TableItem there to resolve) rather than an invalid drop — treat it as
                // "append at the end", the same convention Snippet91 uses for Tree.
                TableItem targetItem = (TableItem) event.item;
                int newIndex = targetItem != null ? table.indexOf(targetItem) : order.size();
                int oldIndex = Integer.parseInt((String) event.data);
                if (oldIndex == newIndex) return;

                String moved = order.remove(oldIndex);
                // Removing the source before inserting shifts every later index down by one,
                // so a downward drag (oldIndex < newIndex) must target newIndex - 1 to land the
                // moved row on the drop target's own position rather than one row past it.
                if (oldIndex < newIndex) {
                    newIndex--;
                }
                order.add(newIndex, moved);

                table.removeAll();
                for (String s : order) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(s);
                }
                table.setSelection(newIndex);
            }
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
