package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.DartImage;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import dev.equo.swt.Serializer;

public class TreeHelper {

    // How far Flutter has scrolled the tree, in pixels. A Tree's rows are laid out and scrolled on
    // the Dart side, so Java has no other way to know: without it every coordinate-to-row lookup
    // counts from the first item of the model and resolves the wrong row on a scrolled tree.
    // Weak keys so a disposed Tree does not hold its entry.
    private static final java.util.Map<DartTree, Integer> SCROLL_OFFSETS =
        java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    public static void recordScrollOffset(DartTree tree, Event event) {
        SCROLL_OFFSETS.put(tree, Math.max(0, event.y));
    }

    public static int scrollOffsetY(DartTree tree) {
        Integer offset = SCROLL_OFFSETS.get(tree);
        return offset == null ? 0 : offset;
    }

    /** SWT.EmptinessChanged value (56), defined here for compatibility with SWT versions before 3.118 */
    public static final int EMPTINESS_CHANGED = 56;

    public static TreeItem[] collectAllItems(DartTree dartTree) {
        java.util.List<TreeItem> result = new java.util.ArrayList<>();
        TreeItem[] rootItems = dartTree.getApi().getItems();
        for (TreeItem rootItem : rootItems) {
            if (rootItem != null) {
                collectAllItemsRecursive(rootItem, result);
            }
        }
        return result.toArray(new TreeItem[result.size()]);
    }

    /**
     * Sends a selection event for the tree item at the specified visual index.
     * The visual index corresponds to the flat, sequential position of visible items
     * in the tree (only expanded items are counted).
     *
     * @param tree the DartTree instance
     * @param event the event containing the index
     * @param selectionType the type of selection event
     */
    public static void sendSelection(DartTree tree, Event event, int selectionType) {
        TreeItem item = getVisibleItemAtIndex(tree, event.index);
        if (item == null) {
            return;
        }

        event.item = item;
        if (event.detail == SWT.CHECK) {
            item.setChecked(event.doit);
        }
        tree.setSelection(item);
        tree.sendSelectionEvent(selectionType, event, true);
    }

    public static void sendExpand(DartTree dartTree, Event event, boolean expand) {
        TreeItem item = getVisibleItemAtIndex(dartTree, event.index);
        if (item == null) {
            return;
        }

        // Check if item has children
        if (item.getItemCount() == 0) {
            return;
        }
        event.item = item;
        
        // Store the previous state for comparison
        boolean wasExpanded = item.getExpanded();

        if (wasExpanded != expand) {
            item.setExpanded(expand);
            dartTree.sendEvent(expand ? SWT.Expand : SWT.Collapse, event);
            dartTree.getBridge().dirty(dartTree);
        }
    }

    public static void createItem(TreeItem item, long hParent, long hInsertAfter, long hItem) {
        if (item == null) {
            return;
        }
        // Count current non-null items first
        int currentSize = 0;
        TreeItem[] items = item.getItems();
        for (TreeItem existingItem : items) {
            if (existingItem != null) {
                currentSize++;
            }
        }

        // Determine insertion index based on hInsertAfter
        // hInsertAfter=0 means append at end
        // For non-zero hInsertAfter, insert AFTER that position (hence +1)
        int insertIndex = (hInsertAfter == 0) ? currentSize : (int) hInsertAfter + 1;

        // Bounds checking for insertion index
        if (insertIndex < 0) {
            insertIndex = 0;
        }

        // Ensure insertIndex doesn't exceed current size
        if (insertIndex > currentSize) {
            insertIndex = currentSize;
        }

        // Ensure array has enough capacity
        if (currentSize >= items.length) {
            TreeItem[] newItems = new TreeItem[items.length + 4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }

        // Find the actual position to insert at (accounting for null gaps)
        int actualInsertPos = 0;
        int nonNullCount = 0;

        // Find the position where we should insert
        for (int i = 0; i < items.length && nonNullCount < insertIndex; i++) {
            if (items[i] != null) {
                nonNullCount++;
            }
            if (nonNullCount == insertIndex) {
                actualInsertPos = i + 1;
                break;
            }
        }

        // If inserting at the end or array is empty, find first available slot
        if (insertIndex == currentSize || currentSize == 0) {
            actualInsertPos = 0;
            while (actualInsertPos < items.length && items[actualInsertPos] != null) {
                actualInsertPos++;
            }
        }

        // Insert the item at the determined position
        items[actualInsertPos] = item;

        // Send emptiness changed event if this is the first item
        if (currentSize == 0) {
            Event event = new Event();
            event.detail = 0;
            item.getImpl().sendEvent(TreeHelper.EMPTINESS_CHANGED, event);
        }
    }

    public static void createItem(TreeItem item, TreeItem parentItem, int index, Tree parent) {
        int count;
        TreeItem[] items;
        if (parentItem != null) {
            count = ((DartTreeItem) parentItem.getImpl()).getItemCount();
            items = ((DartTreeItem) parentItem.getImpl()).items;
        } else {
            count = ((DartTree) parent.getImpl()).getItemCount();
            items = ((DartTree) parent.getImpl()).items;
        }
        if (index == -1)
            index = count;
        if (!(0 <= index && index <= count))
            SWT.error(SWT.ERROR_INVALID_RANGE);

        // Check if we need to grow the array
        if (count >= items.length) {
            // Array is full - need to grow
            TreeItem[] newItems = new TreeItem[items.length + 4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
            if (parentItem != null) {
                ((DartTreeItem) parentItem.getImpl()).items = items;
            } else {
                ((DartTree) parent.getImpl()).items = items;
            }
        }

        // Always shift and add the item
        if (index < count) {
            // Inserting within existing items - shift items
            System.arraycopy(items, index, items, index + 1, count - index);
        }
        items[index] = item;

        ((DartTreeItem) item.getImpl()).items = new TreeItem[4];

        // Only fire EmptinessChanged for root items when tree becomes non-empty
        if (parentItem == null) {
            int treeCount = parent.getImpl().getItemCount();
            if (treeCount == 1) {
                Event event = new Event();
                event.detail = 0;
                parent.getImpl().sendEvent(TreeHelper.EMPTINESS_CHANGED, event);
            }
        }
    }

    public static void removeAll(DartTreeItem treeItem) {
        /**
         * Performance optimization, switch off redraw for high amount of elements
         */
        boolean disableRedraw = treeItem.parent.getImpl().getItemCount() > 30;
        if (disableRedraw) {
            treeItem.parent.setRedraw(false);
        }
        try {
            TreeItem[] items = treeItem.items;
            if (items != null) {
                for (TreeItem item : items) {
                    if (item != null && !item.isDisposed()) {
                        item.getImpl().release(false);
                    }
                }
                treeItem.items = new TreeItem[4];
            }
        } finally {
            if (disableRedraw) {
                treeItem.parent.setRedraw(true);
            }
        }
    }

    public static int getNonNullItemCount(DartTree dartTree) {
        TreeItem[] items = dartTree.getItems();
        if (items == null) return 0;
        int count = 0;
        for (TreeItem item : items) {
            if (item != null) {
                count++;
            }
        }
        return count;
    }

    private static void collectAllItemsRecursive(TreeItem item, java.util.List<TreeItem> result) {
        if (item != null) {
            result.add(item);
            TreeItem[] childItems = item.getItems();
            for (TreeItem childItem : childItems) {
                if (childItem != null) {
                    collectAllItemsRecursive(childItem, result);
                }
            }
        }
    }

    /**
     * Gets the visible TreeItem at the specified index from the tree.
     *
     * @param tree the DartTree instance
     * @param index the visual index of the item (0-based)
     * @return the TreeItem at the specified index, or null if invalid
     */
    private static TreeItem getVisibleItemAtIndex(DartTree tree, int index) {
        if (tree == null || index < 0) {
            return null;
        }

        // Collect visible items in visual order
        java.util.List<TreeItem> visibleItems = new java.util.ArrayList<>();
        TreeItem[] rootItems = tree.getApi().getItems();
        for (TreeItem rootItem : rootItems) {
            if (rootItem != null) {
                collectVisibleItemsRecursive(rootItem, visibleItems);
            }
        }

        // Check if index is valid
        if (index >= visibleItems.size()) {
            return null;
        }

        return visibleItems.get(index);
    }

    /**
     * Recursively collects only visible (expanded) tree items in visual order.
     * This mimics the behavior of how items appear in the tree UI.
     *
     * @param item   the current tree item
     * @param result the list to collect visible items into
     */
    private static void collectVisibleItemsRecursive(TreeItem item, java.util.List<TreeItem> result) {
        if (item != null) {
            result.add(item);
            // Only add children if this item is expanded
            if (item.getExpanded()) {
                TreeItem[] childItems = item.getItems();
                for (TreeItem childItem : childItems) {
                    if (childItem != null) {
                        collectVisibleItemsRecursive(childItem, result);
                    }
                }
            }
        }
    }

    public static void setItemCount(DartTreeItem treeItem, int count) {
        count = Math.max(0, count);
        int currentCount = treeItem.getItemCount();

        if (count == currentCount) return;

        if (count > currentCount) {
            // Need to add items
            for (int i = currentCount; i < count; i++) {
                new TreeItem(treeItem.getApi(), SWT.NONE);
            }
        } else {
            // Need to remove items (count < currentCount)
            int toRemove = currentCount - count;
            int removed = 0;

            // Remove from the end
            for (int i = treeItem.items.length - 1; i >= 0 && removed < toRemove; i--) {
                if (treeItem.items[i] != null) {
                    treeItem.items[i].dispose();
                    removed++;
                }
            }
        }
    }

    public static long findPrevius(Tree parent, int index) {
        if (parent == null)
            return 0;
        if (index < 0)
            SWT.error(SWT.ERROR_INVALID_RANGE);
        // Validate that index is not greater than item count
        int count = parent.getImpl().getItemCount();
        if (index > count)
            SWT.error(SWT.ERROR_INVALID_RANGE);
        return index;
    }

    public static long findPrevius(TreeItem parentItem, int index) {
        if (parentItem == null)
            return 0;
        if (index < 0)
            SWT.error(SWT.ERROR_INVALID_RANGE);
        // Validate that index is not greater than item count
        int count = parentItem.getImpl().getItemCount();
        if (index > count)
            SWT.error(SWT.ERROR_INVALID_RANGE);
        return 0; // ????? Maybe this is not neccessary
    }

    public static int indexOf(DartTree tree, TreeItem item) {
        TreeItem[] items = tree.items;
        if (items == null)
            return -1;
        int nonNullIndex = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                if (items[i] == item) {
                    return nonNullIndex;
                }
                nonNullIndex++;
            }
        }
        return -1;
    }

    public static int indexOf(DartTreeItem tree, TreeItem item) {
        TreeItem[] items = tree.items;
        if (items == null)
            return -1;
        int nonNullIndex = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                if (items[i] == item) {
                    return nonNullIndex;
                }
                nonNullIndex++;
            }
        }
        return -1;
    }

    public static void setItemCount(DartTree tree, int count) {
        int currentCount = tree.getItemCount();

        if (count == currentCount) return;

        if (count > currentCount) {
            // Need to add items
            for (int i = currentCount; i < count; i++) {
                new TreeItem(tree.getApi(), SWT.NONE);
            }
        } else {
            // Need to remove items (count < currentCount)
            int toRemove = currentCount - count;

            // Collect items to remove before disposing them
            // This prevents issues when dispose() modifies the items array
            java.util.List<TreeItem> itemsToRemove = new java.util.ArrayList<>();
            for (int i = tree.items.length - 1; i >= 0 && itemsToRemove.size() < toRemove; i--) {
                if (tree.items[i] != null) {
                    itemsToRemove.add(tree.items[i]);
                }
            }

            // Now dispose the collected items
            for (TreeItem item : itemsToRemove) {
                item.dispose();
            }
        }
    }

    /**
     * Handles a Modify event from Flutter and updates the text of the corresponding TreeItem.
     * 
     * @param tree the DartTree instance
     * @param event the event containing the index and text
     */
    public static void handleModify(DartTree tree, Event event) {
        if (event.text != null && event.index >= 0) {
            TreeItem item = getVisibleItemAtIndex(tree, event.index);
            if (item != null) {
                item.setText(event.text);
                tree.dirty();
            }
        }
    }

    public static Image[] getImages(DartTreeItem item) {
        int count = Math.max(1, item.parent.getColumnCount());
        Image[] result = ownImages(item, count);
        OwnerDraw drawn = ownerDraw(item);
        if (drawn != null) {
            for (int i = 0; i < count; i++) {
                if (drawn.suppressed[i] || result[i] == null) {
                    result[i] = dartImageOrNull(drawn.images[i]);
                }
            }
        }
        return result;
    }

    public static String[] getTexts(DartTreeItem item) {
        String[] model = item.strings;
        OwnerDraw drawn = ownerDraw(item);
        if (drawn == null) {
            return model;
        }
        String[] result = new String[drawn.suppressed.length];
        for (int i = 0; i < result.length; i++) {
            String own = model != null && i < model.length ? model[i] : null;
            // An empty cell is the absence of a model value, not a value of "". setText(1, s) on a
            // fresh item builds the whole array and seeds strings[0] from the (empty) text, so an
            // owner-drawn column 0 always has one -- treating it as a value would let it outrank
            // what the PaintItem listener drew, which is the only content such a cell ever has.
            if (own != null && own.isEmpty()) {
                own = null;
            }
            // setText(0, s) leaves `strings` null and keeps the label in `text`, so column 0's model
            // value can live there alone.
            if (i == 0 && own == null && item.text != null && !item.text.isEmpty()) {
                own = item.text;
            }
            // Model wins only when not suppressed AND it has its own text; otherwise use what PaintItem drew.
            if (!drawn.suppressed[i] && own != null) {
                result[i] = own;
                continue;
            }
            String captured = drawn.texts[i];
            if ((captured == null || captured.isEmpty()) && drawn.textDrawn[i] && own != null && !own.isEmpty()) {
                result[i] = own;
            } else {
                result[i] = captured;
            }
        }
        return result;
    }

    public static void setImages(Image[] value, DartTreeItem item) {
        item.images = value;
        if (value != null && value.length > 0) {
            item.image = value[0];
        }
    }

    private static Image[] ownImages(DartTreeItem item, int count) {
        Image[] result = new Image[count];
        Image[] rowImages = item.images;
        if (rowImages != null) {
            for (int i = 0; i < count; i++) {
                result[i] = dartImageOrNull(i < rowImages.length ? rowImages[i] : null);
            }
        } else {
            result[0] = dartImageOrNull(item.image);
        }
        return result;
    }

    /** What an SWT.PaintItem listener actually drew into each cell of one item. */
    private static final class OwnerDraw {

        final String[] texts;

        final boolean[] textDrawn;

        final Image[] images;

        final boolean[] suppressed;

        OwnerDraw(String[] texts, boolean[] textDrawn, Image[] images, boolean[] suppressed) {
            this.texts = texts;
            this.textDrawn = textDrawn;
            this.images = images;
            this.suppressed = suppressed;
        }
    }

    private static OwnerDraw ownerDraw(DartTreeItem item) {
        if (!((DartWidget) item.parent.getImpl()).hooks(SWT.PaintItem)) {
            return null;
        }
        // Capturing runs the app's SWT.PaintItem listener once per cell. Native SWT runs it once per
        // cell per paint; both texts and images are derived from one capture, so one payload gets one.
        return Serializer.oncePerPayload(item, () -> captureOwnerDraw(item));
    }

    private static OwnerDraw captureOwnerDraw(DartTreeItem item) {
        int count = Math.max(1, item.parent.getColumnCount());
        DartWidget parent = (DartWidget) item.parent.getImpl();
        Image[] own = ownImages(item, count);
        boolean[] suppressed = suppressedForegrounds(item, parent, count);

        String[] texts = new String[count];
        boolean[] textDrawn = new boolean[count];
        Image[] images = new Image[count];
        GC gc = new GC(item.parent);
        DartGC dartGc = (DartGC) gc.getImpl();
        // This GC only ever draws through textCapture/imageCapture below -- Flutter never
        // learns it exists, so its dispose must not tell Flutter otherwise.
        dartGc.silentDispose = true;
        int itemHeight = ((DartTree) item.parent.getImpl()).getItemHeight();
        try {
            for (int i = 0; i < count; i++) {
                if (!suppressed[i] && own[i] != null) {
                    continue;
                }
                StringBuilder text = new StringBuilder();
                boolean[] drewText = new boolean[1];
                Image[] image = new Image[1];
                dartGc.textCapture = drawn -> {
                    drewText[0] = true;
                    if (drawn != null)
                        text.append(drawn);
                };
                dartGc.imageCapture = drawn -> image[0] = drawn;
                Event event = new Event();
                event.item = item.getApi();
                event.index = i;
                event.gc = gc;
                event.height = itemHeight;
                parent.sendEvent(SWT.PaintItem, event);
                texts[i] = text.toString();
                textDrawn[i] = drewText[0];
                images[i] = image[0];
            }
        } finally {
            dartGc.textCapture = null;
            dartGc.imageCapture = null;
            gc.dispose();
        }
        return new OwnerDraw(texts, textDrawn, images, suppressed);
    }

    /**
     * Asks the SWT.EraseItem listeners which cells they paint themselves. An owner-drawing app
     * clears SWT.FOREGROUND there to mean "do not paint this item's own text/image, I will paint
     * it" -- Eclipse's QuickAccessEntry.erase() does exactly {@code detail &= ~SWT.FOREGROUND}.
     */
    private static boolean[] suppressedForegrounds(DartTreeItem item, DartWidget parent, int count) {
        boolean[] suppressed = new boolean[count];
        if (!parent.hooks(SWT.EraseItem)) {
            return suppressed;
        }
        for (int i = 0; i < count; i++) {
            Event event = new Event();
            event.item = item.getApi();
            event.index = i;
            event.detail = SWT.FOREGROUND | SWT.BACKGROUND;
            parent.sendEvent(SWT.EraseItem, event);
            suppressed[i] = (event.detail & SWT.FOREGROUND) == 0;
        }
        return suppressed;
    }

    private static Image dartImageOrNull(Image img) {
        if (img == null)
            return null;
        if (img.getImpl() instanceof DartImage)
            return img;
        return img;
    }
}
