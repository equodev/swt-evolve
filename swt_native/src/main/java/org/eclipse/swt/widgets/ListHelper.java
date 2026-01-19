package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import java.util.*;

public class ListHelper {

    public static int getItemHeight(DartList list) {
        Font font = list.getFont();
        if (font == null || font.isDisposed())
            return 0;
        FontData[] fontData = font.getFontData();
        if (fontData == null || fontData.length == 0 || fontData[0] == null)
            return 0;
        return fontData[0].getHeight() + 4;
    }

    public static void sendSelection(DartList list, Event event, int selectionType) {
        list.setSelection(event.segments);
        list.sendSelectionEvent(selectionType, event, true);
    }

    public static void deselectIndex(DartList list, int index) {
        int[] selection = list._selection();
        int position = -1;
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] == index) {
                position = i;
            }
        }
        if (position != -1) {
            int[] newSelection = new int[selection.length - 1];
            System.arraycopy(selection, 0, newSelection, 0, position);
            System.arraycopy(selection, position + 1, newSelection, position, selection.length - position - 1);
            list.selection = newSelection;
            list.dirty();
        }
    }

    public static void deselectRange(DartList list, int start, int end) {
        int[] selection = list._selection();
        int count = 0;
        int[] newSelection = new int[selection.length];
        for (int idx : selection) {
            if (idx < start || idx > end) {
                newSelection[count++] = idx;
            }
        }
        if (count != selection.length) {
            list.selection = Arrays.copyOf(newSelection, count);
            list.dirty();
        }
    }

    public static void deselectIndices(DartList list, int[] indices) {
        int[] selection = list._selection();
        Set<Integer> toRemove = new HashSet<>();
        for (int idx : indices) {
            toRemove.add(idx);
        }

        int count = 0;
        int[] newSelection = new int[selection.length];
        for (int idx : selection) {
            if (!toRemove.contains(idx)) {
                newSelection[count++] = idx;
            }
        }
        if (count != selection.length) {
            list.selection = Arrays.copyOf(newSelection, count);
            list.dirty();
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
        int[] selection = list._selection();
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] == index) {
                return true;
            }
        }
        return false;
    }

    public static void selectIndex(DartList list, int index, int itemCount, int style) {
        if (0 <= index && index < itemCount) {
            if ((style & SWT.SINGLE) != 0) {
                list.selection = new int[] { index };
                list.dirty();
            } else if (!isIndexSelected(list, index)) {
                int[] selection = list._selection();
                int[] newSelection = new int[selection.length + 1];
                System.arraycopy(selection, 0, newSelection, 0, selection.length);
                newSelection[selection.length] = index;
                list.selection = newSelection;
                list.dirty();
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
            list.dirty();
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
            list.dirty();
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
                list.dirty();
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
            list.dirty();
        }
    }

    public static void selectAll(DartList list, int itemCount) {
        int[] allIndices = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            allIndices[i] = i;
        }
        list.dirty();
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
        list.dirty();
    }

    public static int setSelectionStrings(DartList list, String[] selectionItems, int itemCount, int style) {
        int length = selectionItems.length;
        boolean isSingle = (style & SWT.SINGLE) != 0;
        if (length == 0 || (isSingle && length > 1))
            return -1;

        if (isSingle) {
            int index = list.indexOf(selectionItems[0], 0);
            if (index != -1) {
                list.selection = new int[] { index };
                list.dirty();
                return index;
            }
            return -1;
        }

        java.util.List<Integer> newSelection = new java.util.ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        int firstIndex = -1;

        for (String string : selectionItems) {
            int index = 0;
            while ((index = list.indexOf(string, index)) != -1) {
                if (seen.add(index)) {
                    newSelection.add(index);
                    if (firstIndex == -1)
                        firstIndex = index;
                }
                index++;
            }
        }

        if (!newSelection.isEmpty()) {
            list.selection = newSelection.stream().mapToInt(Integer::intValue).toArray();
            list.dirty();
        }
        return firstIndex;
    }

    public static void setTopIndex(DartList list, int index, int itemCount) {
        if (index < 0 || index >= itemCount)
            return;
        if (list._topIndex() != index) {
            list.topIndex = index;
            list.dirty();
        }
    }

    public static boolean containsIndex(int[] array, int length, int value) {
        for (int i = 0; i < length; i++) {
            if (array[i] == value)
                return true;
        }
        return false;
    }
}