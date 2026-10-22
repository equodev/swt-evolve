package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;

public class ComboHelper {

    private ComboHelper() {
    }

    private static boolean readOnly(DartCombo combo) {
        return (combo.getApi().style & SWT.READ_ONLY) != 0;
    }

    /** The length of the text shown: of the selected item for a read-only combo. */
    public static int getCharCount(DartCombo combo) {
        return combo.text == null ? 0 : combo.text.length();
    }

    /**
     * A read-only combo shows its selected item and so selects all of it; an editable one answers a copy of the
     * selection in its text.
     */
    public static Point getSelection(DartCombo combo) {
        if (readOnly(combo))
            return new Point(0, getCharCount(combo));
        return new Point(combo.selection.x, combo.selection.y);
    }

    /** The caret is at the start of the selection, as Win32 and Cocoa answer when there is one. */
    public static int getCaretPosition(DartCombo combo) {
        return combo.selection.x;
    }

    /** Collapses the selection in the text onto its end, as Win32 and GTK do; a read-only combo has none. */
    public static void clearSelection(DartCombo combo) {
        if (!readOnly(combo))
            combo.selection = new Point(combo.selection.y, combo.selection.y);
    }

    /**
     * Sets the text of an editable combo, or selects the item a read-only combo has under that name. The Verify
     * listeners have already ruled on an editable combo's text: {@code DartCombo.setText(String, boolean)} asks
     * them, and a text the user typed was asked about by the client. A read-only combo asks none, as on Win32 and
     * GTK. A new text deselects the item, as on Win32 and GTK, unless it is the text of the item being selected.
     */
    public static void setText(DartCombo combo, String string, boolean notify) {
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
        // The old selection belonged to the old text.
        combo.selection = new Point(0, 0);
        if (!combo.ignoreSelection)
            combo.selectedIndex = -1;
        if (notify)
            combo.sendEvent(SWT.Modify);
    }

    /** A text the user typed: the Verify listeners have already ruled on it. */
    public static void handleModify(DartCombo combo, Event e) {
        if (e.text == null)
            combo.error(SWT.ERROR_NULL_ARGUMENT);
        setText(combo, e.text, true);
        sendSegments(combo);
    }

    /** All items are gone, and the selection with them. */
    public static void listEmptied(DartCombo combo) {
        combo.selectedIndex = -1;
        syncReadOnlyText(combo);
    }

    /**
     * Deselects whatever is selected and empties the text that showed it, as GTK and Win32 do. A read-only combo
     * says so, even when nothing was selected; an editable one does not.
     */
    public static void deselectAll(DartCombo combo) {
        combo.selectedIndex = -1;
        if (readOnly(combo)) {
            syncReadOnlyText(combo);
            combo.sendEvent(SWT.Modify);
        } else {
            clearText(combo);
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
     * Deselects the selected item and empties the text that showed it. A read-only combo says so; an editable one
     * does not.
     */
    public static void deselect(DartCombo combo) {
        combo.selectedIndex = -1;
        if (readOnly(combo)) {
            syncReadOnlyText(combo);
            combo.sendEvent(SWT.Modify);
        } else {
            clearText(combo);
        }
    }

    /** Empties the text of an editable combo, and so the selection in it. */
    private static void clearText(DartCombo combo) {
        combo.text = "";
        combo.selection = new Point(0, 0);
        combo.getValue().markDirty(VCombo.TEXT);
    }

    /** An item went in at {@code index}: the selected item stays selected, wherever it moved to. */
    public static void itemInserted(DartCombo combo, int index) {
        combo.selectedIndex = ListItemsHelper.indexAfterInsert(combo.selectedIndex, index);
    }

    /**
     * The item at {@code index} was replaced. When it is the selected one it stays selected and the combo shows its
     * new text, as {@code select} would show it: Win32's behaviour, where the three platforms differ.
     */
    public static void itemReplaced(DartCombo combo, int index) {
        if (readOnly(combo)) {
            syncReadOnlyText(combo);
            return;
        }
        if (index != combo.selectedIndex)
            return;
        boolean ignoring = combo.ignoreSelection;
        combo.ignoreSelection = true;
        combo.setText(combo.items[index], true);
        combo.ignoreSelection = ignoring;
    }

    /**
     * Takes the item at {@code index} out. Removing the selected item deselects it, empties the text and says so;
     * the selection follows an item before which one was removed.
     */
    public static void removeAt(DartCombo combo, int index) {
        combo.getValue().markDirty(VCombo.ITEMS);
        combo.items = ListItemsHelper.remove(combo.items, index, index);
        int selected = combo.selectedIndex;
        combo.selectedIndex = ListItemsHelper.indexAfterRemove(selected, index, index);
        if (index != selected)
            return;
        String shown = combo.text;
        if (readOnly(combo))
            syncReadOnlyText(combo);
        else
            clearText(combo);
        if (!shown.isEmpty())
            combo.sendEvent(SWT.Modify);
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

        boolean wasSelected = (combo.selectedIndex >= start && combo.selectedIndex <= end);

        combo.items = ListItemsHelper.remove(combo.items, start, end);
        combo.selectedIndex = ListItemsHelper.indexAfterRemove(combo.selectedIndex, start, end);
        if (wasSelected)
            combo.text = "";

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
