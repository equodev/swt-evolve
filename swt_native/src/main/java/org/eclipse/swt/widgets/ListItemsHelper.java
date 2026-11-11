package org.eclipse.swt.widgets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The bookkeeping every widget with a list of items (List, Combo, CCombo, Table, Tree) shares: its items kept as an
 * array, and the indices of its selected items as items come and go. A selected index follows its item; an item that
 * goes takes its selection with it.
 */
public final class ListItemsHelper {

    private ListItemsHelper() {
    }

    /** {@code items} with {@code item} at {@code index}; the items from there on move one place down. */
    public static <T> T[] insert(T[] items, int index, T item) {
        T[] result = Arrays.copyOf(items, items.length + 1);
        result[index] = item;
        System.arraycopy(items, index, result, index + 1, items.length - index);
        return result;
    }

    /** {@code items} without the ones from {@code start} to {@code end}, both included. */
    public static <T> T[] remove(T[] items, int start, int end) {
        int count = end - start + 1;
        T[] result = Arrays.copyOf(items, items.length - count);
        System.arraycopy(items, end + 1, result, start, items.length - end - 1);
        return result;
    }

    /**
     * Removes the items at {@code ascending}, distinct indices from lowest to highest, from the first {@code count}
     * slots of {@code items} in place: the ones that stay close up in their order and the freed slots are cleared.
     * Returns the count left.
     */
    public static <T> int removeAll(T[] items, int count, int[] ascending) {
        int kept = ascending[0];
        int next = 0;
        for (int i = ascending[0]; i < count; i++) {
            if (next < ascending.length && ascending[next] == i)
                next++;
            else
                items[kept++] = items[i];
        }
        Arrays.fill(items, kept, count, null);
        return kept;
    }

    /** {@code indices} without repeats, from lowest to highest, whatever their order. */
    public static int[] distinctAscending(int[] indices) {
        int[] sorted = indices.clone();
        Arrays.sort(sorted);
        int count = 0;
        for (int i = 0; i < sorted.length; i++) {
            if (i == 0 || sorted[i] != sorted[i - 1])
                sorted[count++] = sorted[i];
        }
        return Arrays.copyOf(sorted, count);
    }

    /** How many of the last items of a list of {@code count} are in {@code ascending}, from the last one back. */
    public static int trailingRun(int[] ascending, int count) {
        int run = 0;
        while (run < ascending.length && ascending[ascending.length - 1 - run] == count - 1 - run)
            run++;
        return run;
    }

    /** Whether one of {@code indices} is between {@code start} and {@code end}, both included. */
    public static boolean anyIn(int[] indices, int start, int end) {
        for (int index : indices) {
            if (start <= index && index <= end)
                return true;
        }
        return false;
    }

    /**
     * {@code items} with {@code item} at {@code index}, where only the first {@code count} slots hold items: the
     * array grows by four slots when it is full, and is shifted in place otherwise. Returns the array that holds the
     * items, which is {@code items} itself unless it grew.
     */
    public static <T> T[] insertInCapacity(T[] items, int count, int index, T item) {
        T[] result = count >= items.length ? Arrays.copyOf(items, items.length + 4) : items;
        System.arraycopy(result, index, result, index + 1, count - index);
        result[index] = item;
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

    /** The selected indices once an item went in at {@code index}, in the order they had. */
    public static int[] indicesAfterInsert(int[] selected, int index) {
        int[] result = new int[selected.length];
        for (int i = 0; i < selected.length; i++)
            result[i] = indexAfterInsert(selected[i], index);
        return result;
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

    /**
     * The selected indices once the items at {@code ascending}, distinct indices from lowest to highest, went, in
     * the order they had.
     */
    public static int[] indicesAfterRemove(int[] selected, int[] ascending) {
        int[] result = new int[selected.length];
        int count = 0;
        for (int index : selected) {
            int at = Arrays.binarySearch(ascending, index);
            if (at < 0)
                result[count++] = index + at + 1;
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

    /** The selected indices without the ones in {@code indices}; the items stay where they are. */
    public static int[] deselect(int[] selected, int[] indices) {
        Set<Integer> gone = new HashSet<>();
        for (int index : indices)
            gone.add(index);
        int[] result = new int[selected.length];
        int count = 0;
        for (int index : selected) {
            if (!gone.contains(index))
                result[count++] = index;
        }
        return Arrays.copyOf(result, count);
    }

    /** Whether {@code index} is one of {@code indices}. */
    public static boolean contains(int[] indices, int index) {
        for (int i : indices) {
            if (i == index)
                return true;
        }
        return false;
    }
}
