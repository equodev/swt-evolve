package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Port of upstream Eclipse SWT Snippet91 (drag leaf items in a tree), validating: the
 * leaf-only dragStart veto (evaluated locally in Dart from the already-mirrored child
 * count — see TreeItemImpl#_wrapItemForDrag — with no round trip or flicker, since the
 * condition is known client-side), and item resolution at any nesting depth via
 * Event#itemId (see DartDropTarget#resolveItem, swt-evolve's DND generalization —
 * Control#getItem(int)/Event#index alone only ever resolves top-level items).
 *
 * TreeItem#getBounds() and the drop position sent from Dart are both tree-relative (see
 * Sizes.getBounds(DartTreeItem) and TreeImpl#wrapTreeForDrop's resolvePosition), matching
 * what upstream's dragOver/drop before/after/into split (pt.y vs bounds.y+height/3)
 * expects — this is what makes reordering onto an item above the dragged one behave the
 * same as reordering onto one below it.
 */
public class Snippet91DragDropSnippet {

    public static void main(String[] args) {
        Config.useEquo(Tree.class);

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("Snippet 91");
        shell.setLayout(new FillLayout());
        final Tree tree = new Tree(shell, SWT.BORDER);
        for (int i = 0; i < 3; i++) {
            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText("item " + i);
            for (int j = 0; j < 3; j++) {
                TreeItem subItem = new TreeItem(item, SWT.NONE);
                subItem.setText("item " + i + " " + j);
                for (int k = 0; k < 3; k++) {
                    TreeItem subsubItem = new TreeItem(subItem, SWT.NONE);
                    subsubItem.setText("item " + i + " " + j + " " + k);
                }
            }
        }

        Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

        final DragSource source = new DragSource(tree, operations);
        source.setTransfer(types);
        final TreeItem[] dragSourceItem = new TreeItem[1];
        source.addDragListener(new DragSourceListener() {
            @Override
            public void dragStart(DragSourceEvent event) {
                TreeItem[] selection = tree.getSelection();
                if (selection.length > 0 && selection[0].getItemCount() == 0) {
                    event.doit = true;
                    dragSourceItem[0] = selection[0];
                } else {
                    event.doit = false;
                }
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = dragSourceItem[0].getText();
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                if (event.detail == DND.DROP_MOVE)
                    dragSourceItem[0].dispose();
                dragSourceItem[0] = null;
            }
        });

        DropTarget target = new DropTarget(tree, operations);
        target.setTransfer(types);
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetEvent event) {
                event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
                if (event.item != null) {
                    TreeItem item = (TreeItem) event.item;
                    Point pt = display.map(null, tree, event.x, event.y);
                    Rectangle bounds = item.getBounds();
                    if (pt.y < bounds.y + bounds.height / 3) {
                        event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
                    } else if (pt.y > bounds.y + 2 * bounds.height / 3) {
                        event.feedback |= DND.FEEDBACK_INSERT_AFTER;
                    } else {
                        event.feedback |= DND.FEEDBACK_SELECT;
                    }
                }
            }

            @Override
            public void drop(DropTargetEvent event) {
                if (event.data == null) {
                    event.detail = DND.DROP_NONE;
                    return;
                }
                String text = (String) event.data;
                if (event.item == null) {
                    TreeItem item = new TreeItem(tree, SWT.NONE);
                    item.setText(text);
                } else {
                    TreeItem item = (TreeItem) event.item;
                    Point pt = display.map(null, tree, event.x, event.y);
                    Rectangle bounds = item.getBounds();
                    TreeItem parent = item.getParentItem();
                    if (parent != null) {
                        TreeItem[] items = parent.getItems();
                        int index = 0;
                        for (int i = 0; i < items.length; i++) {
                            if (items[i] == item) {
                                index = i;
                                break;
                            }
                        }
                        if (pt.y < bounds.y + bounds.height / 3) {
                            TreeItem newItem = new TreeItem(parent, SWT.NONE, index);
                            newItem.setText(text);
                        } else if (pt.y > bounds.y + 2 * bounds.height / 3) {
                            TreeItem newItem = new TreeItem(parent, SWT.NONE, index + 1);
                            newItem.setText(text);
                        } else {
                            TreeItem newItem = new TreeItem(item, SWT.NONE);
                            newItem.setText(text);
                        }
                    } else {
                        TreeItem[] items = tree.getItems();
                        int index = 0;
                        for (int i = 0; i < items.length; i++) {
                            if (items[i] == item) {
                                index = i;
                                break;
                            }
                        }
                        if (pt.y < bounds.y + bounds.height / 3) {
                            TreeItem newItem = new TreeItem(tree, SWT.NONE, index);
                            newItem.setText(text);
                        } else if (pt.y > bounds.y + 2 * bounds.height / 3) {
                            TreeItem newItem = new TreeItem(tree, SWT.NONE, index + 1);
                            newItem.setText(text);
                        } else {
                            TreeItem newItem = new TreeItem(item, SWT.NONE);
                            newItem.setText(text);
                        }
                    }
                }
            }
        });

        shell.setSize(400, 400);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}
