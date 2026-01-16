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
                parent.getImpl().sendEvent(SWT.EmptinessChanged, event);
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

}
