package org.eclipse.swt.widgets;

import dev.equo.swt.size.ListSizes;
import org.eclipse.swt.SWT;

import java.util.*;

public class ListHelper {

    /**
     * The index of the item with the focus: the selected one, or the one that was selected when the selection
     * was cleared, as on Win32 and GTK. Cocoa has it only while something is selected.
     */
    public static int getFocusIndex(DartList list) {
        int selected = getMinSelectionIndex(list);
        if (selected != -1)
            return selected;
        int kept = list.keptFocusIndex;
        return kept < list.getItemCount() ? kept : -1;
    }

    /** The selection is about to be cleared. */
    public static void rememberFocus(DartList list) {
        int selected = getMinSelectionIndex(list);
        if (selected != -1)
            list.keptFocusIndex = selected;
    }

    private static int viewportHeight(DartList list) {
        return Math.max(0, list.getClientArea().height);
    }

    private static int maxScrollOffset(DartList list, int pitch) {
        return Math.max(0, list.getItemCount() * pitch - viewportHeight(list));
    }

    private static int scrollOffset(DartList list, int pitch) {
        return Math.min(list.scrollOffset, maxScrollOffset(list, pitch));
    }

    private static int pitch(DartList list) {
        return Math.max(1, ListSizes.getItemHeight(list));
    }

    /** The index of the item at the top; 0 for an empty list. */
    public static int getTopIndex(DartList list) {
        int count = list.getItemCount();
        if (count == 0)
            return 0;
        int pitch = pitch(list);
        return Math.min(scrollOffset(list, pitch) / pitch, count - 1);
    }

    /**
     * Scrolls so that item {@code index} is at the top, or as near as the items below it allow. An index
     * before the first item is the first one and an index past the last item is the last one, as on Win32.
     * The platforms differ there: Cocoa scrolls to the top for an index past the last item, and GTK ignores
     * an index outside the items.
     */
    public static void setTopIndex(DartList list, int index, int itemCount) {
        if (itemCount == 0)
            return;
        scrollToRow(list, Math.max(0, Math.min(index, itemCount - 1)));
    }

    /** Scrolls the least that shows all of item {@code index}; an item already shown does not scroll. */
    public static void showIndex(DartList list, int index) {
        if (!(0 <= index && index < list.getItemCount()))
            return;
        int viewport = viewportHeight(list);
        if (viewport <= 0)
            return;
        int pitch = pitch(list);
        int offset = scrollOffset(list, pitch);
        if (index * pitch < offset)
            scrollTo(list, index * pitch);
        else if ((index + 1) * pitch > offset + viewport)
            scrollTo(list, Math.min((index + 1) * pitch - viewport, maxScrollOffset(list, pitch)));
    }

    private static void scrollToRow(DartList list, int row) {
        int pitch = pitch(list);
        scrollTo(list, Math.min(row * pitch, maxScrollOffset(list, pitch)));
    }

    /**
     * Java records the offset the items will end at, so what the API reads back right away is where the list
     * will be; Flutter is sent the item to bring to the top.
     */
    private static void scrollTo(DartList list, int offset) {
        list.scrollOffset = offset;
        list.getValue().markDirty(VList.TOP_INDEX);
    }

    /**
     * Flutter scrolled the items to {@code event.y} pixels, in items {@code event.height} pixels high. The
     * offset is kept in this list's item pitch, so the top index read back is the item Flutter shows at the
     * top. Flutter is not sent the top index back.
     */
    public static void recordScrollOffset(DartList list, Event event) {
        int y = Math.max(0, event.y);
        int extent = event.height;
        list.scrollOffset = extent > 0 ? (int) ((long) y * pitch(list) / extent) : y;
    }

    /**
     * Up, Down, Page Up, Page Down, Home and End move the selection to the row they reach, as Win32's and
     * GTK's list do for a key no listener vetoed, and report it as a Selection; the last four also bring
     * that row into view. A page is the rows in view less one, and at least one. A MULTI list moves from
     * its first selected row and ends with that one row selected; Shift and Ctrl are not handled.
     */
    public static void moveSelectionByKey(DartList list, Event key) {
        if ((key.stateMask & SWT.MODIFIER_MASK) != 0)
            return;
        int count = list.getItemCount();
        if (count == 0)
            return;
        int current = list.getSelectionIndex();
        int next;
        switch (key.keyCode) {
            case SWT.ARROW_UP:
                next = current < 0 ? 0 : current - 1;
                break;
            case SWT.ARROW_DOWN:
                next = current < 0 ? 0 : current + 1;
                break;
            case SWT.HOME:
                next = 0;
                break;
            case SWT.END:
                next = count - 1;
                break;
            case SWT.PAGE_DOWN: {
                int bottom = list.getTopIndex() + visibleRows(list) - 1;
                next = current < bottom ? bottom : current + page(list);
                break;
            }
            case SWT.PAGE_UP: {
                int top = list.getTopIndex();
                next = current > top ? top : current - page(list);
                break;
            }
            default:
                return;
        }
        next = Math.max(0, Math.min(count - 1, next));
        if (next == current)
            return;
        Event selection = new Event();
        selection.segments = new int[] { next };
        list.setSelection(selection.segments);
        if (key.keyCode != SWT.ARROW_UP && key.keyCode != SWT.ARROW_DOWN)
            showIndex(list, next);
        list.sendSelectionEvent(SWT.Selection, selection, true);
    }

    private static int visibleRows(DartList list) {
        return Math.max(1, viewportHeight(list) / pitch(list));
    }

    private static int page(DartList list) {
        return Math.max(1, visibleRows(list) - 1);
    }

    /**
     * A pick from Flutter. The items are selected at once, so a MouseUp listener reads the new selection;
     * a Selection made while button 1 is down is told after the MouseUp, as Win32 and Cocoa do (GTK tells
     * it at the press).
     */
    public static void sendSelection(DartList list, Event event, int selectionType) {
        list.setSelection(event.segments);
        sendHeldSelection(list);
        if (list.isDisposed())
            return;
        if (selectionType == SWT.Selection && list.pressInFlight)
            list.heldSelection = event;
        else
            list.sendSelectionEvent(selectionType, event, true);
    }

    /** A press starts; a Selection still held from a press whose release never came is told first. */
    public static void pressed(DartList list, Event event) {
        sendHeldSelection(list);
        list.pressInFlight = event.button == 1;
    }

    /** The MouseUp listeners have run: the click's Selection is told. */
    public static void released(DartList list) {
        list.pressInFlight = false;
        sendHeldSelection(list);
    }

    private static void sendHeldSelection(DartList list) {
        Event held = list.heldSelection;
        list.heldSelection = null;
        if (held != null)
            list.sendSelectionEvent(SWT.Selection, held, true);
    }

    public static void deselectIndex(DartList list, int index) {
        deselectRange(list, index, index);
    }

    public static void deselectRange(DartList list, int start, int end) {
        int[] selection = list._selection();
        int[] newSelection = ListItemsHelper.deselect(selection, start, end);
        if (newSelection.length != selection.length) {
            list.selection = newSelection;
            list.getValue().markDirty(VList.SELECTION);
        }
    }

    /** The items from {@code start} to {@code end} are about to go: their selection goes and the later ones move up. */
    public static void itemsRemoved(DartList list, int start, int end) {
        int[] selection = list._selection();
        int[] newSelection = ListItemsHelper.indicesAfterRemove(selection, start, end);
        if (!Arrays.equals(newSelection, selection)) {
            list.selection = newSelection;
            list.getValue().markDirty(VList.SELECTION);
        }
    }

    /** An item went in at {@code index} ({@code add}) or the one there went: the selection follows its items. */
    public static void fixSelection(DartList list, int index, boolean add) {
        int[] selection = list.getSelectionIndices();
        int[] fixed = add ? ListItemsHelper.indicesAfterInsert(selection, index)
                : ListItemsHelper.indicesAfterRemove(selection, index, index);
        if (Arrays.equals(fixed, selection))
            return;
        list.selection = fixed;
        list.getValue().markDirty(VList.SELECTION);
    }

    public static void deselectIndices(DartList list, int[] indices) {
        int[] selection = list._selection();
        int[] newSelection = ListItemsHelper.deselect(selection, indices);
        if (newSelection.length != selection.length) {
            list.selection = newSelection;
            list.getValue().markDirty(VList.SELECTION);
        }
    }

    public static int[] getSelectionIndicesSorted(DartList list) {
        int[] selection = list._selection();
        if (selection == null || selection.length == 0) {
            return new int[0];
        }
        int[] result = new int[selection.length];
        System.arraycopy(selection, 0, result, 0, selection.length);
        Arrays.sort(result);
        return result;
    }

    public static int getMinSelectionIndex(DartList list) {
        int[] selection = list._selection();
        if (selection == null || selection.length == 0) {
            return -1;
        }
        int min = selection[0];
        for (int i = 1; i < selection.length; i++) {
            if (selection[i] < min) {
                min = selection[i];
            }
        }
        return min;
    }

    public static boolean isIndexSelected(DartList list, int index) {
        return ListItemsHelper.contains(list._selection(), index);
    }

    public static void selectIndex(DartList list, int index, int itemCount, int style) {
        if (0 <= index && index < itemCount) {
            if ((style & SWT.SINGLE) != 0) {
                list.selection = new int[] { index };
                list.getValue().markDirty(VList.SELECTION);
            } else if (!isIndexSelected(list, index)) {
                int[] selection = list._selection();
                int[] newSelection = new int[selection.length + 1];
                System.arraycopy(selection, 0, newSelection, 0, selection.length);
                newSelection[selection.length] = index;
                list.selection = newSelection;
                list.getValue().markDirty(VList.SELECTION);
            }
        }
    }

    public static void selectRange(DartList list, int start, int end, int itemCount, int style) {
        boolean isSingle = (style & SWT.SINGLE) != 0;
        if (end < 0 || start > end || (isSingle && start != end))
            return;
        if (itemCount == 0 || start >= itemCount)
            return;
        start = Math.max(0, start);
        end = Math.min(end, itemCount - 1);

        if (isSingle) {
            list.selection = new int[] { start };
            list.getValue().markDirty(VList.SELECTION);
            return;
        }

        int[] selection = list._selection();
        Set<Integer> currentSelection = new HashSet<>();
        for (int idx : selection) {
            currentSelection.add(idx);
        }
        boolean changed = false;
        for (int i = start; i <= end; i++) {
            if (currentSelection.add(i)) {
                changed = true;
            }
        }
        if (changed) {
            list.selection = currentSelection.stream().mapToInt(Integer::intValue).toArray();
            list.getValue().markDirty(VList.SELECTION);
        }
    }

    public static void selectIndices(DartList list, int[] indices, int itemCount, int style) {
        int length = indices.length;
        boolean isSingle = (style & SWT.SINGLE) != 0;
        if (length == 0 || (isSingle && length > 1))
            return;

        if (isSingle) {
            int index = indices[0];
            if (index >= 0 && index < itemCount) {
                list.selection = new int[] { index };
                list.getValue().markDirty(VList.SELECTION);
            }
            return;
        }

        int[] selection = list._selection();
        Set<Integer> currentSelection = new HashSet<>();
        for (int idx : selection) {
            currentSelection.add(idx);
        }
        boolean changed = false;
        for (int index : indices) {
            if (index >= 0 && index < itemCount && currentSelection.add(index)) {
                changed = true;
            }
        }
        if (changed) {
            list.selection = currentSelection.stream().mapToInt(Integer::intValue).toArray();
            list.getValue().markDirty(VList.SELECTION);
        }
    }

    public static void selectAll(DartList list, int itemCount) {
        int[] allIndices = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            allIndices[i] = i;
        }
        list.getValue().markDirty(VList.SELECTION);
        list.selection = allIndices;
    }

    public static void setSelectionRange(DartList list, int start, int end, int itemCount, int style) {
        if (end < 0 || start > end || ((style & SWT.SINGLE) != 0 && start != end))
            return;
        if (itemCount == 0 || start >= itemCount)
            return;
        start = Math.max(0, start);
        end = Math.min(end, itemCount - 1);
        int[] newSelection = new int[end - start + 1];
        for (int i = start; i <= end; i++) {
            newSelection[i - start] = i;
        }
        list.selection = newSelection;
        list.getValue().markDirty(VList.SELECTION);
    }

    public static int setSelectionStrings(DartList list, String[] selectionItems, int itemCount, int style) {
        int length = selectionItems.length;
        boolean isSingle = (style & SWT.SINGLE) != 0;
        if (length == 0 || (isSingle && length > 1))
            return -1;

        if (isSingle) {
            int index = selectionItems[0] == null ? -1 : list.indexOf(selectionItems[0], 0);
            if (index != -1) {
                list.selection = new int[] { index };
                list.getValue().markDirty(VList.SELECTION);
                return index;
            }
            return -1;
        }

        java.util.List<Integer> newSelection = new java.util.ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        int firstIndex = -1;

        // Backwards, so firstIndex ends on the earliest string that matched: the item GTK and Win32 show.
        // A null among the strings is skipped, as GTK and Win32 skip it; only Cocoa rejects it.
        for (int i = length - 1; i >= 0; i--) {
            String string = selectionItems[i];
            if (string == null)
                continue;
            int index = 0;
            int firstMatch = -1;
            while ((index = list.indexOf(string, index)) != -1) {
                if (seen.add(index))
                    newSelection.add(index);
                if (firstMatch == -1)
                    firstMatch = index;
                index++;
            }
            if (firstMatch != -1)
                firstIndex = firstMatch;
        }

        if (!newSelection.isEmpty()) {
            list.selection = newSelection.stream().mapToInt(Integer::intValue).toArray();
            list.getValue().markDirty(VList.SELECTION);
        }
        return firstIndex;
    }

    public static boolean containsIndex(int[] array, int length, int value) {
        for (int i = 0; i < length; i++) {
            if (array[i] == value)
                return true;
        }
        return false;
    }
}