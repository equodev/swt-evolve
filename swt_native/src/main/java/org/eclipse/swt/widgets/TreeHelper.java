package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

public class TreeHelper {

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
            dartTree.getBridge().dirty(dartTree);
            dartTree.sendEvent(expand ? SWT.Expand : SWT.Collapse, event);
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
            item.getImpl().sendEvent(SWT.EmptinessChanged, event);
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
}
