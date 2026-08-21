package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.DartImage;
import org.eclipse.swt.graphics.GC;
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

    /**
     * Hooks {@link ClickSelection} the first time the Table sees a click. The generated hook runs
     * this before anything the application registered, which is exactly why it must not select
     * here: JFace's default cell-editor activation strategy is a MouseDown listener that reads the
     * table's selection to decide whether the click edits the clicked cell or only selects the row,
     * and native SWT reports MouseDown with the selection the click has not moved yet.
     */
    public static void handleMouseDownSelection(DartTable table, Event event) {
        if (table == null || table.isDisposed())
            return;
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
     * Moves the selection to the row a click landed on, behind every MouseDown listener the
     * application put on the Table -- see {@link #handleMouseDownSelection}.
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
            int[] current = table.selection;
            boolean sameSingleRow = current != null && current.length == 1 && current[0] == index;
            if (sameSingleRow)
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

    public static String[] getTexts(DartTableItem item) {
        String[] model = item.strings;
        OwnerDraw drawn = ownerDraw(item);
        if (drawn == null) {
            return model;
        }
        String[] result = new String[drawn.suppressed.length];
        for (int i = 0; i < result.length; i++) {
            String own = model != null && i < model.length ? model[i] : null;
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

    private static OwnerDraw ownerDraw(DartTableItem item) {
        if (!((DartWidget) item.parent.getImpl()).hooks(SWT.PaintItem)) {
            return null;
        }
        return captureOwnerDraw(item);
    }

    private static OwnerDraw captureOwnerDraw(DartTableItem item) {
        int count = Math.max(1, item.parent.getColumnCount());
        DartWidget parent = (DartWidget) item.parent.getImpl();
        Image[] own = ownImages(item, count);
        boolean[] suppressed = suppressedForegrounds(item, parent, count);

        String[] texts = new String[count];
        boolean[] textDrawn = new boolean[count];
        Image[] images = new Image[count];
        GC gc = new GC(item.parent);
        DartGC dartGc = (DartGC) gc.getImpl();
        // This GC only ever draws through textCapture/imageCapture below — Flutter never
        // learns it exists, so its dispose must not tell Flutter otherwise.
        dartGc.silentDispose = true;
        int itemHeight = ((DartTable) item.parent.getImpl()).getItemHeight();
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
    private static boolean[] suppressedForegrounds(DartTableItem item, DartWidget parent, int count) {
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
            if (initializeFromPrimaryImage) {
                item.images[0] = item.image;
            }
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
                TableItem item = table._getItem(i);
                if (item == null || ((DartTableItem) item.getImpl()).cached) continue;
                table.checkData(item, i);
                loadedAny = true;
            }
        } finally {
            table.loadingVirtualData = false;
        }
        table.virtualWindowEnd = end;
        if (loadedAny) table.dirty();
    }
}