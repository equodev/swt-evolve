package org.eclipse.swt.widgets;

import java.util.Arrays;

/**
 * The bookkeeping every list-based widget (List, Combo, CCombo) shares: its items kept as an array, and the indices
 * of its selected items as items come and go. A selected index follows its item; an item that goes takes its
 * selection with it.
 */
public final class ListItemsHelper {

    private ListItemsHelper() {
    }

    /** {@code items} with {@code item} at {@code index}; the items from there on move one place down. */
    public static String[] insert(String[] items, int index, String item) {
        String[] result = new String[items.length + 1];
        System.arraycopy(items, 0, result, 0, index);
        result[index] = item;
        System.arraycopy(items, index, result, index + 1, items.length - index);
        return result;
    }

    /** {@code items} without the ones from {@code start} to {@code end}, both included. */
    public static String[] remove(String[] items, int start, int end) {
        int count = end - start + 1;
        String[] result = new String[items.length - count];
        System.arraycopy(items, 0, result, 0, start);
        System.arraycopy(items, end + 1, result, start, items.length - end - 1);
        return result;
    }

    /** Where the item at {@code selected} is once an item went in at {@code index}; -1 stays -1. */
    public static int indexAfterInsert(int selected, int index) {
        return selected >= 0 && index <= selected ? selected + 1 : selected;
    }

    /**
     * Where the item at {@code selected} is once the items from {@code start} to {@code end} went: -1 when it was
     * one of them.
     */
    public static int indexAfterRemove(int selected, int start, int end) {
        if (selected < start)
            return selected;
        return selected <= end ? -1 : selected - (end - start + 1);
    }

    /** The selected indices once the items from {@code start} to {@code end} went, in the order they had. */
    public static int[] indicesAfterRemove(int[] selected, int start, int end) {
        int[] result = new int[selected.length];
        int count = 0;
        for (int index : selected) {
            int moved = indexAfterRemove(index, start, end);
            if (moved != -1)
                result[count++] = moved;
        }
        return Arrays.copyOf(result, count);
    }

    /** The selected indices without the ones from {@code start} to {@code end}; the items stay where they are. */
    public static int[] deselect(int[] selected, int start, int end) {
        int[] result = new int[selected.length];
        int count = 0;
        for (int index : selected) {
            if (index < start || index > end)
                result[count++] = index;
        }
        return Arrays.copyOf(result, count);
    }
}
