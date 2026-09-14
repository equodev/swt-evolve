package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Drags a CTabItem from one CTabFolder onto another.
 *
 * <p>CTabFolder has no built-in cross-folder move: an application builds one on top of
 * DragSource/DropTarget, the way the Eclipse workbench moves a view between stacks — reparent
 * the tab's content Control and create a CTabItem in the target folder. A folder that has a
 * DropTarget owns every drop on its tab row, including a move within itself, so this snippet
 * handles both cases through the same {@link #moveTab} call.
 */
public class CTabFolderDragDropSnippet {

    /**
     * The tab in flight. TextTransfer only has to carry something for the type negotiation; the
     * item itself never leaves the process, the same split JFace's LocalSelectionTransfer makes.
     */
    private static CTabItem dragged;

    public static void main(String[] args) {
        Config.useEquo(CTabFolder.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CTabFolderDragDropSnippet");
        shell.setLayout(new FillLayout(SWT.HORIZONTAL));
        shell.setSize(700, 300);

        addDragAndDrop(createFolder(shell, "Left", 3));
        addDragAndDrop(createFolder(shell, "Right", 2));

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static CTabFolder createFolder(Shell shell, String prefix, int count) {
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        for (int i = 0; i < count; i++) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText(prefix + " " + (i + 1));
            Label content = new Label(folder, SWT.CENTER);
            content.setText("Content of " + item.getText());
            item.setControl(content);
        }
        folder.setSelection(0);
        return folder;
    }

    private static void addDragAndDrop(CTabFolder folder) {
        DragSource source = new DragSource(folder, DND.DROP_MOVE);
        source.setTransfer(TextTransfer.getInstance());
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragStart(DragSourceEvent event) {
                dragged = folder.getSelection();
                event.doit = dragged != null;
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = dragged != null ? dragged.getText() : "";
            }
        });

        DropTarget target = new DropTarget(folder, DND.DROP_MOVE);
        target.setTransfer(TextTransfer.getInstance());
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                if (dragged == null || dragged.isDisposed()) return;
                // event.item is the tab under the cursor; no tab there means the drop landed
                // past the last one, so the moved tab goes to the end.
                int index = event.item instanceof CTabItem hovered
                        ? folder.indexOf(hovered)
                        : folder.getItemCount();
                moveTab(dragged, folder, index);
                dragged = null;
            }
        });
    }

    private static void moveTab(CTabItem item, CTabFolder target, int index) {
        CTabFolder source = item.getParent();
        String text = item.getText();
        Control content = item.getControl();
        int removed = source.indexOf(item);

        // Detach the content before disposing the item so the old folder stops laying it out
        // (destroying the selected item moves the selection, which hides its control).
        item.setControl(null);
        item.dispose();

        if (source == target && removed < index) index--;
        if (content != null && !content.isDisposed()) content.setParent(target);

        CTabItem moved = new CTabItem(target, SWT.NONE, Math.max(0, Math.min(index, target.getItemCount())));
        moved.setText(text);
        moved.setControl(content);
        target.setSelection(moved);
        target.layout(true, true);
    }
}
