package dev.equo;

import dev.equo.swt.WindowPolicy;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Drag and drop from a modeless dialog to its parent shell, with each shell in a window of its own.
 *
 * <p>The dialog holds two drag sources: a childless Canvas that draws its own rows, as a NatTable
 * does, and a Tree. The main shell holds a Tree drop target. Drag a row from either source onto the
 * "Inventory" tree: the drop adds it there. The status line and stdout log every DnD event.
 *
 * <pre>
 * -Ddev.equo.swt.windows=all      the default here; the dialog opens in its own window
 * -Ddev.equo.swt.windows=single   the dialog is drawn inside the main window
 * </pre>
 */
public class CrossWindowDragDropSnippet {

    private static final String[] ROWS = {"Row 1", "Row 2", "Row 3"};
    private static final int ROW_HEIGHT = 22;

    private static Label status;

    public static void main(String[] args) {
        defaultProperty("dev.equo.swt.mode", "desktop");
        defaultProperty(WindowPolicy.MODE_PROPERTY, "all");

        Display display = new Display();
        Shell main = new Shell(display);
        main.setText("Main");
        main.setLayout(new GridLayout(1, false));
        status = new Label(main, SWT.NONE);
        status.setText("ready");
        status.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        Tree inventory = new Tree(main, SWT.BORDER);
        inventory.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        TreeItem root = new TreeItem(inventory, SWT.NONE);
        root.setText("Inventory");
        addDropSupport(inventory, root);
        main.setBounds(40, 40, 420, 420);
        main.open();

        Shell addData = new Shell(main, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS);
        addData.setText("Add Data");
        addData.setLayout(new FillLayout(SWT.VERTICAL));
        Canvas table = new Canvas(addData, SWT.BORDER);
        int[] selected = {0};
        table.addPaintListener(e -> {
            for (int i = 0; i < ROWS.length; i++) {
                if (i == selected[0]) {
                    e.gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
                    e.gc.fillRectangle(0, i * ROW_HEIGHT, table.getClientArea().width, ROW_HEIGHT);
                }
                e.gc.drawText(ROWS[i], 4, i * ROW_HEIGHT + 3, true);
            }
        });
        table.addListener(SWT.MouseDown, e -> {
            selected[0] = Math.max(0, Math.min(ROWS.length - 1, e.y / ROW_HEIGHT));
            table.redraw();
        });
        Tree sourceTree = new Tree(addData, SWT.BORDER);
        for (String name : new String[]{"Item A", "Item B"}) {
            new TreeItem(sourceTree, SWT.NONE).setText(name);
        }
        addData.setBounds(500, 80, 320, 300);
        addData.open();
        // After both shells are shown, as JFace's addDragSupport does on a live viewer.
        addDragSupport(table, () -> ROWS[selected[0]]);
        addDragSupport(sourceTree,
                () -> sourceTree.getSelectionCount() == 0 ? null : sourceTree.getSelection()[0].getText());

        while (!main.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void addDragSupport(org.eclipse.swt.widgets.Control control,
                                       java.util.function.Supplier<String> data) {
        DragSource source = new DragSource(control, DND.DROP_COPY | DND.DROP_MOVE);
        source.setTransfer(new Transfer[]{TextTransfer.getInstance()});
        String name = control.getClass().getSimpleName();
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = data.get() != null;
                log("dragStart " + name + " doit=" + event.doit);
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = data.get();
                log("dragSetData " + name + " " + event.data);
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                log("dragFinished " + name + " detail=" + event.detail);
            }
        });
    }

    private static void addDropSupport(Tree tree, TreeItem root) {
        DropTarget target = new DropTarget(tree, DND.DROP_COPY | DND.DROP_MOVE);
        target.setTransfer(new Transfer[]{TextTransfer.getInstance()});
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                event.detail = DND.DROP_COPY;
                log("dragEnter Inventory");
            }

            @Override
            public void dragOver(DropTargetEvent event) {
                event.detail = DND.DROP_COPY;
            }

            @Override
            public void drop(DropTargetEvent event) {
                log("drop Inventory " + event.data);
                if (event.data != null) {
                    new TreeItem(root, SWT.NONE).setText((String) event.data);
                    root.setExpanded(true);
                }
            }
        });
    }

    private static void log(String line) {
        System.out.println("[dnd] " + line);
        if (!status.isDisposed()) status.setText(line);
    }

    private static void defaultProperty(String key, String value) {
        if (System.getProperty(key) == null) System.setProperty(key, value);
    }
}
