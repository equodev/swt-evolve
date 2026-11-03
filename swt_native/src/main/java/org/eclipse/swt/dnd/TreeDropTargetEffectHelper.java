package org.eclipse.swt.dnd;

import java.util.function.LongSupplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * The scrolling and expanding a TreeDropTargetEffect does under a drag, as SWT does on Windows. A
 * drag held over the first or the last row shown for {@link #SCROLL_HYSTERESIS} ms scrolls the tree
 * one row, and one held over a collapsed item with children for {@link #EXPAND_HYSTERESIS} ms
 * expands it and sends SWT.Expand. Flutter draws the selection and insertion feedback.
 *
 * <p>One per TreeDropTargetEffect; what it holds lasts one drag.
 */
final class TreeDropTargetEffectHelper {

    static final int SCROLL_HYSTERESIS = 200;
    static final int EXPAND_HYSTERESIS = 1000;

    private final Tree tree;
    private final LongSupplier clock;

    private TreeItem scrollItem;
    private long scrollBeginTime;
    private TreeItem expandItem;
    private long expandBeginTime;

    TreeDropTargetEffectHelper(Tree tree) {
        this(tree, System::currentTimeMillis);
    }

    TreeDropTargetEffectHelper(Tree tree, LongSupplier clock) {
        this.tree = tree;
        this.clock = clock;
    }

    void dragEnter() {
        reset();
    }

    void dragLeave() {
        reset();
    }

    void dragOver(DropTargetEvent event) {
        TreeItem item = event.item instanceof TreeItem && !event.item.isDisposed() ? (TreeItem) event.item : null;
        long now = clock.getAsLong();
        if ((event.feedback & DND.FEEDBACK_SCROLL) == 0) {
            scrollBeginTime = 0;
            scrollItem = null;
        } else if (item != null && item == scrollItem && scrollBeginTime != 0) {
            if (now >= scrollBeginTime) {
                scroll(item);
                scrollBeginTime = 0;
                scrollItem = null;
            }
        } else {
            scrollBeginTime = now + SCROLL_HYSTERESIS;
            scrollItem = item;
        }
        if ((event.feedback & DND.FEEDBACK_EXPAND) == 0) {
            expandBeginTime = 0;
            expandItem = null;
        } else if (item != null && item == expandItem && expandBeginTime != 0) {
            if (now >= expandBeginTime) {
                expand(item);
                expandBeginTime = 0;
                expandItem = null;
            }
        } else {
            expandBeginTime = now + EXPAND_HYSTERESIS;
            expandItem = item;
        }
    }

    private void reset() {
        scrollItem = null;
        scrollBeginTime = 0;
        expandItem = null;
        expandBeginTime = 0;
    }

    /** Over the top row it scrolls up; over a row whose next one is not wholly shown, down. */
    private void scroll(TreeItem item) {
        TreeItem top = tree.getTopItem();
        if (item == top) {
            TreeItem previous = previousShown(item);
            if (previous != null)
                tree.setTopItem(previous);
            return;
        }
        TreeItem next = nextShown(item);
        if (next == null || top == null || shownWhole(next))
            return;
        TreeItem below = nextShown(top);
        if (below != null)
            tree.setTopItem(below);
    }

    private boolean shownWhole(TreeItem item) {
        Rectangle row = item.getBounds();
        Rectangle client = tree.getClientArea();
        return client.contains(row.x, row.y) && client.contains(row.x, row.y + row.height);
    }

    private void expand(TreeItem item) {
        if (item.getItemCount() == 0 || item.getExpanded())
            return;
        item.setExpanded(true);
        tree.redraw();
        Event expandEvent = new Event();
        expandEvent.item = item;
        tree.notifyListeners(SWT.Expand, expandEvent);
    }

    private TreeItem nextShown(TreeItem item) {
        if (item.getExpanded() && item.getItemCount() > 0)
            return item.getItem(0);
        for (TreeItem at = item; at != null; at = at.getParentItem()) {
            TreeItem[] siblings = siblings(at);
            int index = indexOf(at);
            if (index >= 0 && index + 1 < siblings.length)
                return siblings[index + 1];
        }
        return null;
    }

    private TreeItem previousShown(TreeItem item) {
        int index = indexOf(item);
        if (index <= 0)
            return item.getParentItem();
        TreeItem previous = siblings(item)[index - 1];
        while (previous.getExpanded() && previous.getItemCount() > 0)
            previous = previous.getItem(previous.getItemCount() - 1);
        return previous;
    }

    private TreeItem[] siblings(TreeItem item) {
        TreeItem parent = item.getParentItem();
        return parent != null ? parent.getItems() : tree.getItems();
    }

    private int indexOf(TreeItem item) {
        TreeItem parent = item.getParentItem();
        return parent != null ? parent.indexOf(item) : tree.indexOf(item);
    }
}
