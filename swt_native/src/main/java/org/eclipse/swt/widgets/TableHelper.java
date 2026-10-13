package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.Serializer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.DartImage;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GraphicsUtils;
import org.eclipse.swt.graphics.Image;

import java.util.Arrays;
import java.lang.reflect.Array;

public class TableHelper {

    // How far Flutter has scrolled the rows, in pixels. The rows are laid out and scrolled on the Dart side,
    // so this is the only thing the row at the top, a row's bounds and API scrolling can be computed from.
    // Weak keys so a disposed Table does not hold its entry.
    private static final java.util.Map<DartTable, Integer> SCROLL_OFFSETS =
        java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    /**
     * The width of the one cell of a table without columns, which holds {@code content} and has {@code room}
     * left in the table: stretched across the table, as on Win32 and GTK. Cocoa makes it as wide as what it
     * holds.
     */
    static int columnlessCellWidth(int content, int room) {
        return Math.max(content, room);
    }

    /**
     * Flutter scrolled the table. The top index Flutter is sent is read from here, so describing the table
     * again does not move it back to where the API last put it.
     */
    public static void recordScrollOffset(DartTable table, Event event) {
        SCROLL_OFFSETS.put(table, Math.max(0, event.y));
    }

    /**
     * Whether a row is measured in the font the application gave it or its table, and not the theme's.
     * Read by the emitted {@code TableSizes} and {@code TableItemSizes}.
     */
    public static boolean usesApplicationFont(DartTableItem item) {
        return Config.getConfigFlags().use_swt_fonts
            && (item.getExplicitFont() != null || ((DartTable) item.getParent().getImpl()).getExplicitFont() != null);
    }

    /** How far the rows are scrolled, never past the point where the last row is shown. */
    public static int scrollOffsetY(DartTable table) {
        Integer offset = SCROLL_OFFSETS.get(table);
        return offset == null ? 0 : Math.min(offset, maxScrollOffset(table, Math.max(1, table.getItemHeight())));
    }

    /** The rows changed: a scroll past where they now end stops at their end. */
    public static void keepScrollInRows(DartTable table) {
        Integer offset = SCROLL_OFFSETS.get(table);
        if (offset != null && offset > scrollOffsetY(table))
            SCROLL_OFFSETS.put(table, scrollOffsetY(table));
    }

    /** The index of the row at the top; 0 for an empty table. */
    public static int getTopIndex(DartTable table) {
        if (table.getItemCount() == 0) return 0;
        return Math.min(scrollOffsetY(table) / Math.max(1, table.getItemHeight()), table.getItemCount() - 1);
    }

    /**
     * Scrolls so that row {@code index} is at the top, or as near as the rows below it allow. An index before
     * the first row is the first one and an index past the last row is the last one, as on Win32. The
     * platforms differ there: Cocoa goes to the top for an index past the last row, and GTK reports the index
     * it was given whether or not the rows can scroll that far.
     */
    public static void setTopIndex(DartTable table, int index) {
        if (table.getItemCount() == 0) return;
        scrollToRow(table, Math.max(0, Math.min(index, table.getItemCount() - 1)));
    }

    /**
     * The row {@code setSelection(TableItem[])} scrolls to, out of {@code count} selected {@code indices}
     * in the order their items were given: the first row of the selection, as on Win32 and GTK. Cocoa shows
     * the last item given.
     */
    public static int rowToShowForSelection(int[] indices, int count) {
        return Arrays.stream(indices, 0, count).min().getAsInt();
    }

    /**
     * Scrolls the least that shows all of row {@code index}; a row already shown does not scroll. A table
     * not laid out yet shows every row, as Cocoa makes it when its view has no size, so it does not scroll.
     */
    public static void showIndex(DartTable table, int index) {
        if (!(0 <= index && index < table.getItemCount())) return;
        int viewport = viewportHeight(table);
        if (viewport <= 0) return;
        int itemHeight = Math.max(1, table.getItemHeight());
        int offset = scrollOffsetY(table);
        if (index * itemHeight < offset) {
            scrollToRow(table, index);
        } else if ((index + 1) * itemHeight > offset + viewport) {
            scrollToRow(table, Math.max(0, index + 1 - Math.max(1, viewport / itemHeight)));
        }
    }

    /**
     * Flutter is told which row to bring to the top and clamps the distance itself; Java records the distance
     * it will end at, so what the API reads back right away is where the table will be.
     */
    private static void scrollToRow(DartTable table, int row) {
        int itemHeight = Math.max(1, table.getItemHeight());
        SCROLL_OFFSETS.put(table, Math.min(row * itemHeight, maxScrollOffset(table, itemHeight)));
        table.getValue().markDirty(VTable.TOP_INDEX);
    }

    /** The height the rows scroll in: Flutter's scroll bars float over the rows and take none of it. */
    private static int viewportHeight(DartTable table) {
        return Math.max(0, table.getBounds().height - table.getHeaderHeight());
    }

    private static int maxScrollOffset(DartTable table, int itemHeight) {
        return Math.max(0, table.getItemCount() * itemHeight - viewportHeight(table));
    }

    /** A column is about to be inserted: the image of the first cell moves into the images of the cells. */
    public static void keepFirstImage(TableItem item, int columnsBefore) {
        DartTableItem impl = (DartTableItem) item.getImpl();
        if (impl.images == null && impl.image != null) {
            impl.images = new Image[columnsBefore];
            impl.images[0] = impl.image;
        }
    }

    /** The selected rows, in row order; the caller may keep or change the array. */
    public static int[] selectionIndices(DartTable table) {
        return table.selection == null ? new int[0] : table.selection.clone();
    }

    /** The first {@code count} of {@code indices} become the selection. */
    public static void replaceSelection(DartTable table, int[] indices, int count) {
        int[] next = Arrays.copyOf(indices, count);
        if (!Arrays.equals(table.selection, next)) {
            table.getValue().markDirty(VTable.SELECTION);
            table.selection = next;
        }
    }

    /** The rows changed: a selected row that is no longer one is dropped from the selection. */
    public static void keepSelectionInRows(DartTable table) {
        int[] selection = table.selection;
        if (selection == null) return;
        int rows = table.getItemCount();
        int kept = 0;
        for (int index : selection)
            if (index < rows) kept++;
        if (kept < selection.length) {
            int[] next = new int[kept];
            for (int i = 0, j = 0; i < selection.length; i++)
                if (selection[i] < rows) next[j++] = selection[i];
            table.selection = next;
            table.getValue().markDirty(VTable.SELECTION);
        }
    }

    public static void selectAll(DartTable table, int itemCount) {
        table.selection = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            table.selection[i] = i;
        }
        table.getValue().markDirty(VTable.SELECTION);
    }

    /**
     * Hooks {@link ClickSelection} the first time the Table sees a click, and -- when replacing an
     * *existing* single-row selection with a different row -- moves the raw selection array to the
     * clicked row right away, matching native SWT. A hand-rolled {@code MouseDown} listener that
     * resolves "which row" via {@code getSelection()} instead of hit-testing its own coordinates
     * depends on this.
     * <p>
     * A cold start (no selection yet) must stay deferred instead: JFace's default cell-editor
     * activation strategy is an equally early {@code MouseDown} listener gated on
     * {@code getStructuredSelection().size() == 1}, and moving the array eagerly here would flip
     * that count from 0 to 1 before JFace's listener sees it -- see {@link ClickSelection}.
     */
    public static void handleMouseDownSelection(DartTable table, Event event) {
        if (table == null || table.isDisposed())
            return;
        if (event == null || (event.button != 1 && event.button != 3))
            return;
        if (event.segments == null || event.segments.length == 0)
            return;
        int index = event.segments[0];
        if (table.items == null || index < 0 || index >= table.items.length)
            return;
        int[] current = table.selection;
        boolean sameSingleRow = current != null && current.length == 1 && current[0] == index;
        boolean hadExistingSelection = current != null && current.length > 0;
        event.data = sameSingleRow;
        if (!sameSingleRow && hadExistingSelection) {
            table.setSelection(new int[] { index });
        }
        Table api = table.getApi();
        for (Listener listener : api.getListeners(SWT.MouseDown)) {
            if (listener instanceof ClickSelection)
                return;
        }
        // Hooked during this very dispatch, so it also runs for the click that installed it:
        // EventTable appends, and its send loop re-reads the listener array on every step.
        api.addListener(SWT.MouseDown, new ClickSelection(table));
    }

    /**
     * Fires the Selection event for the row a click landed on, behind every MouseDown listener the
     * application put on the Table -- see {@link #handleMouseDownSelection}. Covers the cold-start
     * case that method deliberately leaves alone: table.selection may still be empty here, so
     * sendSelection() below both applies it and fires the notification, same as before this class
     * existed at all.
     */
    private static final class ClickSelection implements Listener {

        private final DartTable table;

        ClickSelection(DartTable table) {
            this.table = table;
        }

        @Override
        public void handleEvent(Event event) {
            if (table.isDisposed())
                return;
            if (event == null || (event.button != 1 && event.button != 3))
                return;
            if (event.segments == null || event.segments.length == 0)
                return;
            int index = event.segments[0];
            if (table.items == null || index < 0 || index >= table.items.length)
                return;
            if (Boolean.TRUE.equals(event.data))
                return;
            // The Selection rides on its own Event: sendSelectionEvent() rewrites the type of the
            // Event it is handed and EventTable's send loop re-reads that type on every step, so
            // passing the in-flight MouseDown event would leave it typed SWT.Selection and drop
            // every MouseDown listener still behind this one.
            Event selection = new Event();
            selection.segments = event.segments;
            selection.detail = event.detail;
            selection.stateMask = event.stateMask;
            selection.button = event.button;
            selection.count = event.count;
            selection.x = event.x;
            selection.y = event.y;
            selection.time = event.time;
            sendSelection(table, selection, SWT.Selection);
        }
    }

    public static void sendSelection(DartTable table, Event event, int selectionType) {
        if (event.detail == SWT.CHECK) {
            if (event.segments != null && event.segments.length > 0) {
                int index = event.segments[0];
                if (table.items != null && index >= 0 && index < table.items.length) {
                    TableItem item = table.items[index];
                    item.setChecked(!item.getChecked());
                    event.item = item;
                    event.index = index;
                }
            }
        } else {
            if (event.segments != null) {
                table.setSelection(event.segments);
            }
            if (event.item == null && event.segments != null && event.segments.length > 0) {
                int index = event.segments[0];
                if (table.items != null && index >= 0 && index < table.items.length) {
                    event.item = table.items[index];
                    event.index = index;
                }
            }
        }
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
            table.getValue().markDirty(VTable.SELECTION);
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
            table.getValue().markDirty(VTable.SELECTION);
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
            table.getValue().markDirty(VTable.SELECTION);
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
            table.getValue().markDirty(VTable.SELECTION);
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

            table.getValue().markDirty(VTable.SELECTION);
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
            table.getValue().markDirty(VTable.SELECTION);
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

            table.getValue().markDirty(VTable.SELECTION);
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
                table.getValue().markDirty(VTable.SELECTION);
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
        return table.columnOrder.clone();
    }

    /**
     * Inserts the column created at {@code index} into a column order the application set: Cocoa shows a
     * new column at the position of its index, and shifts the indexes after it.
     */
    public static void insertColumnOrder(DartTable table, int index) {
        int[] order = table.columnOrder;
        int count = table.columnCount;
        if (order.length != count - 1 || count < 2) return;
        int[] next = new int[count];
        for (int i = 0, j = 0; i < count; i++) {
            next[i] = i == index ? index : (order[j] >= index ? order[j++] + 1 : order[j++]);
        }
        table.columnOrder = next;
        table.getValue().markDirty(VTable.COLUMN_ORDER);
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
            table.getValue().markDirty(VTable.SELECTION);
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
            table.getValue().markDirty(VTable.SELECTION);
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
            table.getValue().markDirty(VTable.COLUMN_ORDER);
        }
    }

    public static Image[] getImages(DartTableItem item) {
        int count = Math.max(1, item.parent.getColumnCount());
        Image[] result = ownImages(item, count);
        OwnerDraw drawn = ownerDraw(item);
        if (drawn != null) {
            // The row's overlay already shows what the listener drew, images included.
            boolean overlay = OwnerDrawOverlay.isListening(item.getApi());
            for (int i = 0; i < count; i++) {
                if (overlay) {
                    if (drawn.suppressed[i]) result[i] = null;
                } else if (drawn.suppressed[i] || result[i] == null) {
                    result[i] = dartImageOrNull(drawn.images[i]);
                }
            }
        }
        return result;
    }

    /**
     * The columns whose text the row's overlay paints: null for a row that is not owner-drawn, and
     * empty for one whose overlay Flutter has not reported listening yet, which is what asks for it.
     */
    public static int[] getPaintedTexts(DartTableItem item) {
        OwnerDraw drawn = ownerDraw(item);
        if (drawn == null) {
            return null;
        }
        if (!OwnerDrawOverlay.isListening(item.getApi())) {
            return new int[0];
        }
        int[] painted = new int[drawn.suppressed.length];
        int count = 0;
        for (int i = 0; i < drawn.suppressed.length; i++) {
            if (overlayOwnsText(item, drawn, i)) {
                painted[count++] = i;
            }
        }
        return Arrays.copyOf(painted, count);
    }

    public static String[] getTexts(DartTableItem item) {
        OwnerDraw drawn = ownerDraw(item);
        if (drawn == null) {
            return item.strings;
        }
        String[] result = new String[drawn.suppressed.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = overlayOwnsText(item, drawn, i) ? drawn.texts[i] : ownText(item, i);
        }
        return result;
    }

    /**
     * Whether cell {@code index}'s text belongs to the row's overlay rather than to the model. The
     * render side reads this to decide which cells not to paint, so it has to agree with what
     * {@link #getTexts(DartTableItem)} hands it: the model wins when the app did not suppress the
     * foreground and has its own text, and also when the listener asked to draw text but produced
     * none — that leaves the overlay with no text op, so a cell marked as the overlay's would be
     * suppressed on both sides and show nothing.
     */
    private static boolean overlayOwnsText(DartTableItem item, OwnerDraw drawn, int index) {
        String own = ownText(item, index);
        if (!drawn.suppressed[index] && own != null) {
            return false;
        }
        String captured = drawn.texts[index];
        boolean drewNoText = captured == null || captured.isEmpty();
        return !(drewNoText && drawn.textDrawn[index] && own != null && !own.isEmpty());
    }

    /** Column {@code index}'s model text, or null when the model has none for it. */
    private static String ownText(DartTableItem item, int index) {
        String[] model = item.strings;
        return model != null && index < model.length ? model[index] : null;
    }

    private static Image[] ownImages(DartTableItem item, int count) {
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

    /**
     * Names the cell content an owner-drawing table derives from its own paint listener, for every
     * row that exists.
     *
     * <p>That content is not stored anywhere: it is produced by running the application's
     * {@code SWT.PaintItem} listener at serialization time, so a repaint is the only signal that it
     * changed, and an update carries only the properties something named. Without this a cell keeps
     * whatever was captured the first time — the row still renders, and its drawn content never
     * changes again.
     *
     * <p>Only rows already materialised: reading {@code getItems()} on a virtual table would create
     * every one of them, which is the cost that style exists to avoid.
     */
    static void nameOwnerDrawnCells(DartTable table) {
        TableItem[] rows = table.items;
        if (rows == null) {
            return;
        }
        for (TableItem row : rows) {
            if (row == null || row.isDisposed() || !(row.getImpl() instanceof DartTableItem)) {
                continue;
            }
            nameOwnerDrawnCells((DartTableItem) row.getImpl());
        }
    }

    static void nameOwnerDrawnCells(DartTableItem item) {
        item.getValue().markDirty(VTableItem.IMAGES);
        item.getValue().markDirty(VTableItem.TEXTS);
        item.getValue().markDirty(VTableItem.PAINTED_TEXTS);
    }

    private static OwnerDraw ownerDraw(DartTableItem item) {
        if (!((DartWidget) item.parent.getImpl()).hooks(SWT.PaintItem)) {
            return null;
        }
        // Capturing runs the app's SWT.PaintItem listener once per cell. Native SWT runs it once per
        // cell per paint; both texts and images are derived from one capture, so one payload gets one.
        return Serializer.oncePerPayload(item, () -> captureOwnerDraw(item));
    }

    private static OwnerDraw captureOwnerDraw(DartTableItem item) {
        int count = Math.max(1, item.parent.getColumnCount());
        DartWidget parent = (DartWidget) item.parent.getImpl();
        Image[] own = ownImages(item, count);

        String[] texts = new String[count];
        boolean[] textDrawn = new boolean[count];
        Image[] images = new Image[count];
        boolean[] suppressed;
        GC gc = new GC(item.parent);
        DartGC dartGc = (DartGC) gc.getImpl();
        // This GC only ever draws through textCapture/imageCapture below — Flutter never
        // learns it exists, so its dispose must not tell Flutter otherwise.
        dartGc.silentDispose = true;
        int itemHeight = ((DartTable) item.parent.getImpl()).getItemHeight();
        try {
            measureCells(item, parent, count, gc, itemHeight);
            suppressed = suppressedForegrounds(item, parent, count, gc);
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
        if (OwnerDrawOverlay.isListening(item.getApi())) {
            OwnerDrawOverlay.paint(item.parent, item.getApi(), count);
        }
        return new OwnerDraw(texts, textDrawn, images, suppressed);
    }

    /**
     * Native SWT measures a cell before it erases or paints it, so an owner-drawing app is free to
     * compute its cell layout in {@code measure()} and read it back in {@code paint()}. A capture
     * that only paints leaves that layout uninitialised and the app's own paint code fails on it.
     *
     * <p>What the listener writes back is discarded: a Dart widget's size comes from Sizes, not
     * from the row metrics native SWT would derive here.
     */
    private static void measureCells(DartTableItem item, DartWidget parent, int count, GC gc, int itemHeight) {
        if (!parent.hooks(SWT.MeasureItem)) {
            return;
        }
        for (int i = 0; i < count; i++) {
            Event event = new Event();
            event.item = item.getApi();
            event.index = i;
            event.gc = gc;
            event.height = itemHeight;
            parent.sendEvent(SWT.MeasureItem, event);
        }
    }

    /**
     * Asks the SWT.EraseItem listeners which cells they paint themselves. An owner-drawing app
     * clears SWT.FOREGROUND there to mean "do not paint this item's own text/image, I will paint
     * it" -- Eclipse's QuickAccessEntry.erase() does exactly {@code detail &= ~SWT.FOREGROUND}.
     */
    private static boolean[] suppressedForegrounds(DartTableItem item, DartWidget parent, int count, GC gc) {
        boolean[] suppressed = new boolean[count];
        if (!parent.hooks(SWT.EraseItem)) {
            return suppressed;
        }
        for (int i = 0; i < count; i++) {
            Event event = new Event();
            event.item = item.getApi();
            event.index = i;
            // Native SWT always hands an EraseItem listener a GC, and an owner-drawing app is
            // free to read it (gc.isClipped(), gc.getClipping()) before answering.
            event.gc = gc;
            event.detail = SWT.FOREGROUND | SWT.BACKGROUND;
            parent.sendEvent(SWT.EraseItem, event);
            suppressed[i] = (event.detail & SWT.FOREGROUND) == 0;
        }
        return suppressed;
    }

    public static void setImages(Image[] value, DartTableItem item) {
        item.images = value;
        item.getValue().markDirty(VTableItem.IMAGES);
        if (value != null && value.length > 0) {
            item.image = value[0];
            item.getValue().markDirty(VTableItem.IMAGE);
        }
    }

    public static boolean setImage(DartTableItem item, int index, Image image, boolean initializeFromPrimaryImage, boolean[] drawTextOut) {
        item.getValue().markDirty(VTableItem.IMAGES);
        item.getValue().markDirty(VTableItem.IMAGE);
        item.checkWidget();
        if (image != null && image.isDisposed()) {
            item.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        Image dartImage = GraphicsUtils.copyImage(item.getDisplay(), image);
        Image oldImage = null;
        if (index == 0) {
            if (image != null && image.type == SWT.ICON && java.util.Objects.equals(image, item.image))
                return false;
            oldImage = item.image;
            item.image = dartImage;
        }
        int count = Math.max(1, item.parent.getColumnCount());
        if (index < 0 || index > count - 1)
            return false;
        if (item.images == null && (index != 0 || initializeFromPrimaryImage)) {
            item.images = new Image[count];
            item.images[0] = item.image;
        }
        if (item.images != null) {
            Image toStore = index == 0 ? item.image : dartImage;
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
        if (img.getImpl() instanceof DartImage)
            return img;
        return img;
    }

    /**
     * Rows populated before Flutter reports what is on screen; it only has to cover the first paint.
     * Must stay small: SWT caps a virtual table's SetData at three times its visible rows.
     */
    static final int VIRTUAL_INITIAL_ROWS = 16;

    /** Seeds the first page, keeping any larger window Flutter already asked for. */
    public static void loadVirtualItems(DartTable table) {
        loadVirtualWindow(table, VIRTUAL_INITIAL_ROWS);
    }

    /**
     * Fires {@code SWT.SetData} for the rows in {@code [0, endExclusive)} that have no data yet.
     *
     * <p>There is no native paint loop here to pull virtual rows in, so Flutter reports how far it
     * renders and this grows the window to match. Rows that already carry data are skipped, so a
     * repeat call is cheap and a row the application cleared still re-fires.
     */
    public static void loadVirtualWindow(DartTable table, int endExclusive) {
        if ((table.getApi().style & SWT.VIRTUAL) == 0) return;
        int end = Math.min(Math.max(endExclusive, table.virtualWindowEnd), table.getItemCount());
        if (end <= 0) return;
        boolean loadedAny = false;
        table.loadingVirtualData = true;
        try {
            for (int i = 0; i < end; i++) {
                if (table.isDisposed()) return;
                // A SetData listener may dispose rows, which leaves fewer than there were.
                if (i >= table.getItemCount()) break;
                TableItem item = table._getItem(i);
                if (item == null || ((DartTableItem) item.getImpl()).cached) continue;
                table.checkData(item, i);
                loadedAny = true;
            }
        } finally {
            table.loadingVirtualData = false;
        }
        table.virtualWindowEnd = end;
        if (loadedAny) table.getValue().markDirty(VTable.ITEMS);
    }
}