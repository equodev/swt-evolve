package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DartImage;
import org.eclipse.swt.graphics.GraphicsUtils;
import org.eclipse.swt.graphics.Image;

import java.util.Arrays;
import java.lang.reflect.Array;

public class TableHelper {

    public static void selectAll(DartTable table, int itemCount) {
        table.selection = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            table.selection[i] = i;
        }
        table.dirty();
    }

    public static void sendSelection(DartTable table, Event event, int selectionType) {
        table.setSelection(event.segments);
        table.sendSelectionEvent(selectionType, event, true);
    }

    public static void handleModify(DartTable table, Event event) {
        if (event.text != null && event.index >= 0 && event.start >= 0) {
            int rowIndex = event.index;
            int columnIndex = event.start;
            TableItem[] items = table.items;
            if (items != null && rowIndex >= 0 && rowIndex < items.length) {
                TableItem item = items[rowIndex];
                if (item != null) {
                    item.setText(columnIndex, event.text);
                    table.dirty();
                }
            }
        }
    }

    public static void deselectIndex(DartTable table, int index) {
        int[] currentSelection = table.selection != null ? table.selection : new int[0];
        if (currentSelection.length == 0)
            return;

        int[] newSelection = new int[currentSelection.length];
        int count = 0;
        boolean found = false;
        for (int i = 0; i < currentSelection.length; i++) {
            if (currentSelection[i] == index) {
                found = true;
            } else {
                newSelection[count++] = currentSelection[i];
            }
        }

        if (found && count < currentSelection.length) {
            int[] finalSelection = new int[count];
            System.arraycopy(newSelection, 0, finalSelection, 0, count);
            table.dirty();
            table.selection = finalSelection;
        }
    }

    public static void deselectRange(DartTable table, int start, int end) {
        int[] currentSelection = table.selection != null ? table.selection : new int[0];
        if (currentSelection.length == 0)
            return;

        int[] newSelection = new int[currentSelection.length];
        int count = 0;
        for (int i = 0; i < currentSelection.length; i++) {
            if (currentSelection[i] < start || currentSelection[i] > end) {
                newSelection[count++] = currentSelection[i];
            }
        }

        if (count < currentSelection.length) {
            int[] finalSelection = new int[count];
            System.arraycopy(newSelection, 0, finalSelection, 0, count);
            table.dirty();
            table.selection = finalSelection;
        }
    }

    public static void deselectIndices(DartTable table, int[] indices, int itemCount) {
        if (indices.length == 0)
            return;

        int[] currentSelection = table.selection != null ? table.selection : new int[0];
        if (currentSelection.length == 0)
            return;

        boolean[] toDeselect = new boolean[itemCount];
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] >= 0 && indices[i] < itemCount) {
                toDeselect[indices[i]] = true;
            }
        }

        int[] newSelection = new int[currentSelection.length];
        int count = 0;
        for (int i = 0; i < currentSelection.length; i++) {
            if (!toDeselect[currentSelection[i]]) {
                newSelection[count++] = currentSelection[i];
            }
        }

        if (count < currentSelection.length) {
            int[] finalSelection = new int[count];
            System.arraycopy(newSelection, 0, finalSelection, 0, count);
            table.dirty();
            table.selection = finalSelection;
        }
    }

    public static boolean isIndexSelected(DartTable table, int index) {
        int[] selection = table.selection != null ? table.selection : new int[0];
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] == index)
                return true;
        }
        return false;
    }

    public static void selectIndex(DartTable table, int index, int itemCount, int style) {
        if (!(0 <= index && index < itemCount)) {
            return;
        }

        if ((style & SWT.SINGLE) != 0) {
            table.dirty();
            table.selection = new int[] { index };
        } else {
            int[] currentSelection = table.selection != null ? table.selection : new int[0];

            for (int i = 0; i < currentSelection.length; i++) {
                if (currentSelection[i] == index) {
                    return;
                }
            }

            int[] newSelection = new int[currentSelection.length + 1];
            System.arraycopy(currentSelection, 0, newSelection, 0, currentSelection.length);
            newSelection[currentSelection.length] = index;
            Arrays.sort(newSelection);

            table.dirty();
            table.selection = newSelection;
        }
    }

    public static void selectRange(DartTable table, int start, int end, int itemCount, int style) {
        if (end < 0 || start > end || ((style & SWT.SINGLE) != 0 && start != end))
            return;
        if (itemCount == 0 || start >= itemCount)
            return;

        start = Math.max(0, start);
        end = Math.min(end, itemCount - 1);

        if ((style & SWT.SINGLE) != 0) {
            table.dirty();
            table.selection = new int[] { start };
        } else {
            int[] currentSelection = table.selection != null ? table.selection : new int[0];
            int rangeSize = end - start + 1;
            int[] combined = new int[currentSelection.length + rangeSize];
            System.arraycopy(currentSelection, 0, combined, 0, currentSelection.length);
            for (int i = 0; i < rangeSize; i++) {
                combined[currentSelection.length + i] = start + i;
            }

            Arrays.sort(combined);
            int[] newSelection = new int[combined.length];
            int newCount = 0;
            int lastValue = -1;
            for (int i = 0; i < combined.length; i++) {
                if (combined[i] != lastValue) {
                    newSelection[newCount++] = combined[i];
                    lastValue = combined[i];
                }
            }

            int[] finalSelection = new int[newCount];
            System.arraycopy(newSelection, 0, finalSelection, 0, newCount);

            table.dirty();
            table.selection = finalSelection;
        }
    }

    public static void selectIndices(DartTable table, int[] indices, int itemCount, int style) {
        int length = indices.length;
        if (length == 0 || ((style & SWT.SINGLE) != 0 && length > 1))
            return;

        int[] temp = new int[length];
        int count = 0;
        for (int i = 0; i < length; i++) {
            int index = indices[i];
            if (index >= 0 && index < itemCount) {
                temp[count++] = index;
            }
        }

        if (count > 0) {
            int[] validIndices = new int[count];
            System.arraycopy(temp, 0, validIndices, 0, count);
            Arrays.sort(validIndices);

            int[] finalSelection;
            if ((style & SWT.SINGLE) != 0) {
                finalSelection = new int[] { validIndices[0] };
            } else {
                int[] currentSelection = table.selection != null ? table.selection : new int[0];
                int[] combined = new int[currentSelection.length + validIndices.length];
                System.arraycopy(currentSelection, 0, combined, 0, currentSelection.length);
                System.arraycopy(validIndices, 0, combined, currentSelection.length, validIndices.length);

                Arrays.sort(combined);
                int[] newSelection = new int[combined.length];
                int newCount = 0;
                int lastValue = -1;
                for (int i = 0; i < combined.length; i++) {
                    if (combined[i] != lastValue) {
                        newSelection[newCount++] = combined[i];
                        lastValue = combined[i];
                    }
                }

                finalSelection = new int[newCount];
                System.arraycopy(newSelection, 0, finalSelection, 0, newCount);
            }

            if (!java.util.Objects.equals(table.selection, finalSelection)) {
                table.dirty();
            }
            table.selection = finalSelection;
        }
    }

    public static int[] getColumnOrder(DartTable table) {
        if (table.columnCount == 0) {
            return new int[0];
        }
        if (table.columnOrder.length != table.columnCount) {
            int[] order = new int[table.columnCount];
            for (int i = 0; i < table.columnCount; i++) {
                order[i] = i;
            }
            return order;
        }
        return table.columnOrder;
    }

    public static TableItem[] getSelection(DartTable table) {
        int[] selection = table.selection != null ? table.selection : new int[0];
        int itemCount = table.items != null ? table.items.length : 0;
        int count = 0;
        for (int i = 0; i < selection.length; ++i) {
            if (selection[i] >= 0 && selection[i] < itemCount) count++;
        }
        TableItem[] result = new TableItem[count];
        int j = 0;
        for (int i = 0; i < selection.length; ++i) {
            if (selection[i] >= 0 && selection[i] < itemCount) {
                result[j++] = table.items[selection[i]];
            }
        }
        return result;
    }

    public static void fixSelection(DartTable table, int index, boolean add) {
        int[] selection = table.selection != null ? table.selection : new int[0];
        if (selection.length == 0) return;
        int newCount = 0;
        boolean fix = false;
        for (int i = 0; i < selection.length; i++) {
            if (!add && selection[i] == index) {
                fix = true;
            } else {
                selection[newCount] = selection[i];
                if (selection[newCount] >= index) {
                    selection[newCount] += add ? 1 : -1;
                    fix = true;
                }
                newCount++;
            }
        }
        if (fix) {
            int[] newSelection = new int[newCount];
            System.arraycopy(selection, 0, newSelection, 0, newCount);
            table.dirty();
            table.selection = newSelection;
        }
    }

    public static void fixSelectionRange(DartTable table, int start, int end) {
        int[] selection = table.selection != null ? table.selection : new int[0];
        if (selection.length == 0) return;
        int numRemoved = end - start + 1;
        int newCount = 0;
        boolean fix = false;
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] >= start && selection[i] <= end) {
                fix = true;
            } else {
                selection[newCount] = selection[i];
                if (selection[newCount] > end) {
                    selection[newCount] -= numRemoved;
                    fix = true;
                }
                newCount++;
            }
        }
        if (fix) {
            int[] newSelection = new int[newCount];
            System.arraycopy(selection, 0, newSelection, 0, newCount);
            table.dirty();
            table.selection = newSelection;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] insertAt(T[] arr, int index, int newSize) {
        T[] temp = (T[]) Array.newInstance(arr.getClass().getComponentType(), newSize);
        System.arraycopy(arr, 0, temp, 0, index);
        System.arraycopy(arr, index, temp, index + 1, newSize - index - 1);
        return temp;
    }

    public static int[] insertAt(int[] arr, int index, int newSize) {
        int[] temp = new int[newSize];
        System.arraycopy(arr, 0, temp, 0, index);
        System.arraycopy(arr, index, temp, index + 1, newSize - index - 1);
        return temp;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] removeAt(T[] arr, int index, int newSize) {
        T[] temp = (T[]) Array.newInstance(arr.getClass().getComponentType(), newSize);
        System.arraycopy(arr, 0, temp, 0, index);
        System.arraycopy(arr, index + 1, temp, index, newSize - index);
        return temp;
    }

    public static int[] removeAt(int[] arr, int index, int newSize) {
        int[] temp = new int[newSize];
        System.arraycopy(arr, 0, temp, 0, index);
        System.arraycopy(arr, index + 1, temp, index, newSize - index);
        return temp;
    }

    public static void updateColumnOrderOnDestroy(DartTable table, int removedIndex) {
        if (table.columnOrder.length > 0) {
            int[] oldOrder = table.columnOrder;
            int[] newOrder = new int[table.columnCount];
            int count = 0;
            for (int element : oldOrder) {
                if (element != removedIndex) {
                    int newIndex = element < removedIndex ? element : element - 1;
                    newOrder[count++] = newIndex;
                }
            }
            table.columnOrder = newOrder;
        }
    }

    public static Image[] getImages(DartTableItem item) {
        int count = Math.max(1, item.parent.getColumnCount());
        Image[] result = new Image[count];
        Image[] rowImages = item.images;
        if (rowImages != null) {
            for (int i = 0; i < count; i++) {
                Image img = i < rowImages.length ? rowImages[i] : null;
                result[i] = dartImageOrNull(img);
            }
        } else {
            result[0] = dartImageOrNull(item.image);
        }
        return result;
    }

    public static void setImages(Image[] value, DartTableItem item) {
        item.images = value;
        if (value != null && value.length > 0) {
            item.image = value[0];
        }
    }

    public static boolean setImage(DartTableItem item, int index, Image image, boolean initializeFromPrimaryImage, boolean[] drawTextOut) {
        item.dirty();
        item.checkWidget();
        if (image != null && image.isDisposed()) {
            item.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        Image oldImage = null;
        if (index == 0) {
            if (image != null && image.type == SWT.ICON && java.util.Objects.equals(image, item.image))
                return false;
            oldImage = item.image;
            item.image = image;
        }
        int count = Math.max(1, item.parent.getColumnCount());
        if (index < 0 || index > count - 1)
            return false;
        if (item.images == null && (index != 0 || initializeFromPrimaryImage)) {
            item.images = new Image[count];
            if (initializeFromPrimaryImage) {
                item.images[0] = item.image;
            }
        }
        if (item.images != null) {
            Image toStore = index == 0 ? item.image : GraphicsUtils.copyImage(item.getDisplay(), image);
            oldImage = item.images[index];
            item.images[index] = toStore;
        }
        if ((item.parent.style & SWT.VIRTUAL) != 0)
            item.cached = true;
        if (drawTextOut != null && drawTextOut.length > 0) {
            drawTextOut[0] = (image == null && oldImage != null) || (image != null && oldImage == null);
        }
        return true;
    }

    private static Image dartImageOrNull(Image img) {
        if (img == null)
            return null;
        if (!(img.getImpl() instanceof DartImage))
            return null;
        return img;
    }

}
