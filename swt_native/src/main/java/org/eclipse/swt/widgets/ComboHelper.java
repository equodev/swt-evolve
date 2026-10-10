package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class ComboHelper {

    private ComboHelper() {
    }

    private static final int VISIBLE_COUNT = 5;
    /** The row height the native combos size their drop-down list by, in points. */
    private static final int LIST_ITEM_HEIGHT = 16;

    private static boolean cocoa() {
        return "cocoa".equals(SWT.getPlatform());
    }

    private static boolean readOnly(DartCombo combo) {
        return (combo.getApi().style & SWT.READ_ONLY) != 0;
    }

    /**
     * The number of items a new combo shows at once: GTK always ten, Win32 and Cocoa a third of the screen
     * (Win32 keeps five for a simple combo).
     */
    public static int defaultVisibleItemCount(DartCombo combo) {
        String platform = SWT.getPlatform();
        if ("gtk".equals(platform))
            return 10;
        if ("win32".equals(platform) && (combo.getApi().style & SWT.SIMPLE) != 0)
            return VISIBLE_COUNT;
        Rectangle screen = combo.getDisplay().getBounds();
        int height = screen == null ? 0 : screen.height;
        return Math.max(VISIBLE_COUNT, height / 3 / LIST_ITEM_HEIGHT);
    }

    /** The length of the text shown: of the selected item for a read-only combo. */
    public static int getCharCount(DartCombo combo) {
        return combo.text == null ? 0 : combo.text.length();
    }

    /**
     * A read-only combo shows its selected item and so selects all of it; an editable one answers a copy of
     * the selection in its text. Cocoa leaves it unset after the text changes, and then the caret is at the end.
     */
    public static Point getSelection(DartCombo combo) {
        int length = getCharCount(combo);
        if (readOnly(combo))
            return new Point(0, length);
        Point selection = combo.selection;
        return selection == null ? new Point(length, length) : new Point(selection.x, selection.y);
    }

    /** Cocoa knows the caret only once a selection was set since the text last changed. */
    public static int getCaretPosition(DartCombo combo) {
        return cocoa() && combo.selection != null ? combo.selection.x : 0;
    }

    /** Asks the Verify listeners and the display's Verify filters; null when one of them vetoed. */
    public static String verifyText(DartCombo combo, String string, int start, int end) {
        Event event = new Event();
        event.text = string;
        event.start = start;
        event.end = end;
        combo.sendEvent(SWT.Verify, event);
        if (!event.doit || combo.isDisposed())
            return null;
        return event.text;
    }

    /**
     * Sets the text of an editable combo, or selects the item a read-only combo has under that name. {@code verify}
     * is false for a text the user typed, which the Verify listeners have already ruled on.
     */
    public static void setText(DartCombo combo, String string, boolean notify, boolean verify) {
        if (verify && (combo.hooks(SWT.Verify) || combo.filters(SWT.Verify))) {
            string = verifyText(combo, string, 0, getCharCount(combo));
            if (string == null)
                return;
        }
        if (readOnly(combo)) {
            int index = combo.indexOf(string);
            if (index != -1)
                combo.select(index);
            return;
        }
        String text = string.substring(0, Math.min(string.length(), combo.textLimit));
        if (!java.util.Objects.equals(combo.text, text))
            combo.getValue().markDirty(VCombo.TEXT);
        combo.text = text;
        // The old selection belonged to the old text. Cocoa leaves it unset, its caret at the end; the other
        // natives go back to the start.
        combo.selection = cocoa() ? null : new Point(0, 0);
        if (notify)
            combo.sendEvent(SWT.Modify);
    }

    /** A text the user typed: the Verify listeners have already ruled on it. */
    public static void handleModify(DartCombo combo, Event e) {
        if (e.text == null)
            combo.error(SWT.ERROR_NULL_ARGUMENT);
        setText(combo, e.text, true, false);
        sendSegments(combo);
    }

    /**
     * The selected index. Cocoa's combo box keeps the row of its list that was last selected whatever happens to
     * the items, and answers it as long as the list is long enough: after {@code select(1)} and a {@code setItems}
     * of three items it still answers 1, and a list that is too short answers -1 until it grows again. A pop-up
     * button and the other natives answer the index of the item itself.
     */
    public static int getSelectionIndex(DartCombo combo) {
        int row = combo.selectedIndex;
        return cocoa() && !readOnly(combo) && row >= combo.items.length ? -1 : row;
    }

    /** All items are gone: only Cocoa's combo box keeps its selected row. */
    public static void listEmptied(DartCombo combo) {
        if (!cocoa() || readOnly(combo))
            combo.selectedIndex = -1;
        syncReadOnlyText(combo);
    }

    /**
     * Deselects whatever is selected. A read-only combo shows nothing then and says so, even when nothing was
     * selected; Cocoa's combo box deselects the row only while it answers it, so a row the list is too short
     * for survives.
     */
    public static void deselectAll(DartCombo combo) {
        if (readOnly(combo)) {
            combo.selectedIndex = -1;
            syncReadOnlyText(combo);
            combo.sendEvent(SWT.Modify);
        } else if (!cocoa() || combo.getSelectionIndex() != -1) {
            combo.selectedIndex = -1;
        }
    }

    /** A read-only combo shows its selected item, or nothing: its text follows whatever moved the selection. */
    public static void syncReadOnlyText(DartCombo combo) {
        if (!readOnly(combo))
            return;
        int index = combo.selectedIndex;
        String text = index >= 0 ? combo.items[index] : "";
        if (!text.equals(combo.text)) {
            combo.text = text;
            combo.getValue().markDirty(VCombo.TEXT);
        }
    }

    /**
     * Deselects the selected item. A read-only combo shows nothing then and says so; an editable one keeps its
     * text on Cocoa and empties it on the other natives.
     */
    public static void deselect(DartCombo combo) {
        combo.selectedIndex = -1;
        if (readOnly(combo)) {
            syncReadOnlyText(combo);
            combo.sendEvent(SWT.Modify);
        } else if (!cocoa()) {
            clearText(combo);
        }
    }

    private static void clearText(DartCombo combo) {
        combo.text = "";
        combo.getValue().markDirty(VCombo.TEXT);
    }

    /** An item went in at {@code index}: a read-only combo keeps its item selected, wherever it moved to. */
    public static void itemInserted(DartCombo combo, int index) {
        if (readOnly(combo) && combo.selectedIndex >= 0 && index <= combo.selectedIndex)
            combo.selectedIndex++;
    }

    /**
     * Takes the item at {@code index} out. Cocoa's pop-up button hands the selection of the removed item to
     * the first item, and its combo box keeps the row it had (see {@link #getSelectionIndex}) and the text;
     * neither tells the listeners. The other natives deselect the item, empty the text and say so.
     */
    public static void removeAt(DartCombo combo, int index) {
        combo.getValue().markDirty(VCombo.ITEMS);
        String[] items = new String[combo.items.length - 1];
        System.arraycopy(combo.items, 0, items, 0, index);
        System.arraycopy(combo.items, index + 1, items, index, combo.items.length - index - 1);
        combo.items = items;
        int selected = combo.selectedIndex;
        if (cocoa()) {
            if (!readOnly(combo))
                return;
            if (index == selected)
                combo.selectedIndex = items.length > 0 ? 0 : -1;
            else if (index < selected)
                combo.selectedIndex--;
            syncReadOnlyText(combo);
            return;
        }
        if (index == selected) {
            combo.selectedIndex = -1;
            if (combo.text != null && !combo.text.isEmpty()) {
                clearText(combo);
                combo.sendEvent(SWT.Modify);
            }
        } else if (index < selected) {
            combo.selectedIndex--;
        }
    }

    /** Asks the Segments listeners about the text the combo shows, unless a listener has disposed it by now. */
    public static void sendSegments(DartCombo combo) {
        if (!combo.isDisposed())
            sendSegments(combo, combo.getText());
    }

    /** Asks the Segments listeners for the segments of {@code text}; the separators are not drawn. */
    public static void sendSegments(DartCombo combo, String text) {
        if (combo.isDisposed() || text == null || (!combo.hooks(SWT.Segments) && !combo.filters(SWT.Segments)))
            return;
        Event event = new Event();
        event.text = text;
        combo.sendEvent(SWT.Segments, event);
        int[] segments = event.segments;
        if (segments == null)
            return;
        for (int i = 1; i < segments.length; i++) {
            if (segments[i] < segments[i - 1] || segments[i] > text.length())
                combo.error(SWT.ERROR_INVALID_ARGUMENT);
        }
    }

    public static void copy(DartCombo combo) {
        if ((combo.getApi().style & SWT.PASSWORD) != 0)
            return;

        Point selection = combo.getSelection();
        if (selection.x == selection.y)
            return;

        boolean processSegments = combo._segments() != null;
        if (processSegments) {
            combo.clearSegments(true);
        }

        copySelectionToClipboard(combo, selection);

        if (processSegments) {
            combo.applyEditSegments();
        }
    }


    public static void cut(DartCombo combo) {
        if ((combo.getApi().style & SWT.PASSWORD) != 0)
            return;
        if ((combo.getApi().style & SWT.READ_ONLY) != 0)
            return;

        Point selection = combo.getSelection();
        if (selection.x == selection.y)
            return;

        boolean processSegments = combo.hooks(SWT.Segments) || combo.filters(SWT.Segments);
        if (processSegments) {
            combo.clearSegments(true);
        }

        copySelectionToClipboard(combo, selection);

        String text = combo.getText();
        int start = selection.x;
        String newText = text.substring(0, start) + text.substring(selection.y);

        if (!processSegments) {
            combo.clearSegments(false);
        }
        combo.text = newText;
        combo.setSelection(new Point(start, start));

        if (processSegments) {
            combo.applyEditSegments();
        }

        combo.sendEvent(SWT.Modify);
    }


    public static void paste(DartCombo combo) {
        if ((combo.getApi().style & SWT.READ_ONLY) != 0)
            return;

        boolean processSegments = combo.hooks(SWT.Segments) || combo.filters(SWT.Segments);
        if (processSegments) {
            combo.clearSegments(true);
        }

        Clipboard clipboard = new Clipboard(combo.display);
        String textToPaste = (String) clipboard.getContents(TextTransfer.getInstance());
        clipboard.dispose();

        if (textToPaste != null && !textToPaste.isEmpty()) {
            Point selection = combo.getSelection();
            int start = selection.x, end = selection.y;
            String text = combo.getText();
            String leftText = text.substring(0, start);
            String rightText = text.substring(end);

            if (combo.textLimit != Combo.LIMIT) {
                int charCount = text.length();
                if (charCount - (end - start) + textToPaste.length() > combo.textLimit) {
                    textToPaste = textToPaste.substring(0, Math.max(0, combo.textLimit - charCount + (end - start)));
                }
            }

            String newText = leftText + textToPaste + rightText;

            if (!processSegments) {
                combo.clearSegments(false);
            }
            combo.text = newText;
            start += textToPaste.length();
            combo.setSelection(new Point(start, start));

            if (processSegments) {
                combo.applyEditSegments();
            }

            combo.sendEvent(SWT.Modify);
        }
    }

    public static void select(DartCombo combo, int index) {
        if (index < 0 || index >= combo.items.length)
            return;
        int previousSelection = combo.selectedIndex;
        combo.noSelection = false;
        combo.selectedIndex = index;
        combo.text = combo.items[index];
        combo.selection = new Point(0, 0);
        if (previousSelection != index) {
            combo.sendEvent(SWT.Modify);
        }
    }

    public static void remove(DartCombo combo, int index, boolean notify) {
        remove(combo, index, index, notify);
    }

    public static void remove(DartCombo combo, int start, int end) {
        remove(combo, start, end, true);
    }

    private static void remove(DartCombo combo, int start, int end, boolean notify) {
        if (start > end)
            return;
        int count = combo.items.length;
        if (!(0 <= start && end < count)) {
            combo.error(SWT.ERROR_INVALID_RANGE);
        }

        combo.getValue().markDirty(VCombo.ITEMS);

        int removeCount = end - start + 1;

        boolean wasSelected = (combo.selectedIndex >= start && combo.selectedIndex <= end);

        String[] newItems = new String[combo.items.length - removeCount];
        System.arraycopy(combo.items, 0, newItems, 0, start);
        System.arraycopy(combo.items, end + 1, newItems, start, combo.items.length - end - 1);
        combo.items = newItems;

        if (combo.selectedIndex >= start && combo.selectedIndex <= end) {
            combo.selectedIndex = -1;
            combo.text = "";
        } else if (combo.selectedIndex > end) {
            combo.selectedIndex -= removeCount;
        }

        int newWidth = 0;
        if ((combo.getApi().style & SWT.H_SCROLL) != 0) {
            combo.setScrollWidth(newWidth, false);
        }

        if (notify && wasSelected) {
            combo.sendEvent(SWT.Modify);
        }
    }

    private static void copySelectionToClipboard(DartCombo combo, Point selection) {
        String text = combo.getText();
        if (selection.x < 0 || selection.y > text.length() || selection.x == selection.y)
            return;

        String textToCopy = text.substring(selection.x, selection.y);

        Clipboard clipboard = new Clipboard(combo.display);
        try {
            clipboard.setContents(
                    new Object[] { textToCopy },
                    new Transfer[] { TextTransfer.getInstance() }
            );
        } finally {
            clipboard.dispose();
        }
    }

}
