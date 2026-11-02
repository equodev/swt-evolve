package org.eclipse.swt.widgets;

import dev.equo.swt.Serializer;
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

    /**
     * The caret is the end of the selection the user left it at ({@link #clientCaretMoved}); a selection set from
     * code has it at its start, as Win32 and Cocoa answer when there is one.
     */
    public static int getCaretPosition(DartCombo combo) {
        Point selection = combo.selection;
        return combo.caretPosition == selection.y ? selection.y : selection.x;
    }

    /**
     * The user moved the caret or the selection in the client's field without editing: the caret at {@code offset},
     * the selection running from {@code anchor} to it. getSelection() and getCaretPosition() answer from Java's
     * copy, so it follows.
     */
    public static void clientCaretMoved(DartCombo combo, int offset, int anchor) {
        if (readOnly(combo))
            return;
        TextHelper.ClientCaret moved = new TextHelper.ClientCaret(offset, anchor, getCharCount(combo));
        combo.caretPosition = moved.caret;
        combo.selection = moved.selection;
        Serializer.clientChanged(combo.getValue(), VCombo.CARET_POSITION);
    }

    /**
     * Whether a Verify from the client only reports a caret move ({@link TextHelper#proposesNoChange}). Named here so
     * DartCombo does not name TextHelper, which the widget parity gate then counts as Text's alone.
     */
    public static boolean proposesNoChange(Event e) {
        return TextHelper.proposesNoChange(e);
    }

    /** Collapses the selection in the text onto its end, as Win32 and GTK do; a read-only combo has none. */
    public static void clearSelection(DartCombo combo) {
        if (!readOnly(combo))
            combo.selection = new Point(combo.selection.y, combo.selection.y);
    }

    /**
     * Sets the text of an editable combo, or selects the item a read-only combo has under that name. The Verify
     * listeners have already ruled on an editable combo's text: {@code DartCombo.setText(String, boolean)} asks
     * them, and a text the user typed was asked about by the client. A read-only combo asks them too, as Win32 and
     * Cocoa do, in Win32's order: once it has found the item, and only when that item is not the one shown. A veto
     * keeps the item shown, and a replacement that names another item selects that one. A text set from code
     * leaves the selected item selected, as on Win32 and Cocoa; an {@code edited} one, typed, cut or pasted,
     * deselects it.
     */
    public static void setText(DartCombo combo, String string, boolean notify, boolean edited) {
        if (readOnly(combo)) {
            int index = combo.indexOf(string);
            if (index == -1)
                return;
            if (!string.equals(combo.text) && (combo.hooks(SWT.Verify) || combo.filters(SWT.Verify))) {
                String verified = verify(combo, string, 0, getCharCount(combo));
                if (verified == null)
                    return;
                int named = combo.indexOf(verified);
                if (named != -1)
                    index = named;
            }
            combo.select(index);
            return;
        }
        String text = string.substring(0, Math.min(string.length(), combo.textLimit));
        if (!java.util.Objects.equals(combo.text, text))
            combo.getValue().markDirty(VCombo.TEXT);
        combo.text = text;
        // The old selection belonged to the old text.
        combo.selection = new Point(0, 0);
        if (edited)
            combo.selectedIndex = -1;
        if (notify)
            combo.sendEvent(SWT.Modify);
    }

    /**
     * Asks the Verify listeners about {@code text} replacing the range {@code start..end}; answers what they let
     * in, or null when they refuse it or dispose the combo.
     */
    private static String verify(DartCombo combo, String text, int start, int end) {
        Event event = new Event();
        event.text = text;
        event.start = start;
        event.end = end;
        combo.sendEvent(SWT.Verify, event);
        if (!event.doit || combo.isDisposed())
            return null;
        return event.text;
    }

    /**
     * A text the user typed: the Verify listeners have already ruled on it. It deselects the item, as on Win32 and
     * GTK.
     */
    public static void handleModify(DartCombo combo, Event e) {
        if (e.text == null)
            combo.error(SWT.ERROR_NULL_ARGUMENT);
        setText(combo, e.text, true, true);
        sendSegments(combo);
    }

    /** Empties the list; the caller has already asked the Verify listeners about an editable combo's text. */
    public static void removeAll(DartCombo combo) {
        combo.getValue().markDirty(VCombo.ITEMS);
        combo.items = new String[0];
        listEmptied(combo);
    }

    /** Replaces the items, selecting none of them. */
    public static void setItems(DartCombo combo, String[] items) {
        if (items == null)
            combo.error(SWT.ERROR_NULL_ARGUMENT);
        for (String item : items) {
            if (item == null)
                combo.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        combo.removeAll();
        if (items.length == 0)
            return;
        combo.getValue().markDirty(VCombo.ITEMS);
        combo.items = items.clone();
        for (String item : combo.items)
            sendSegments(combo, item);
    }

    /** Selects a range of an editable combo's text, clamped to it; a read-only combo shows its whole item selected. */
    public static void setSelection(DartCombo combo, Point selection) {
        if (selection == null)
            combo.error(SWT.ERROR_NULL_ARGUMENT);
        if (readOnly(combo))
            return;
        int length = combo.getText().length();
        int start = Math.min(Math.max(Math.min(selection.x, selection.y), 0), length);
        int end = Math.min(Math.max(Math.max(selection.x, selection.y), 0), length);
        combo.selection = new Point(start, end);
        combo.caretPosition = start;
    }

    /** All items are gone, and the selection with them. */
    public static void listEmptied(DartCombo combo) {
        combo.selectedIndex = -1;
        syncReadOnlyText(combo);
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
     * Deselects the selected item and empties the text that showed it, as GTK and Win32 do. A read-only combo says
     * so, even when nothing was selected. An editable one with a text asks the Verify listeners about emptying it,
     * and tells the Modify listeners once they allow it.
     */
    public static void deselect(DartCombo combo) {
        combo.selectedIndex = -1;
        if (readOnly(combo)) {
            syncReadOnlyText(combo);
            combo.sendEvent(SWT.Modify);
        } else {
            emptyText(combo);
        }
    }

    /**
     * Empties an editable combo's text, as GTK does: the Verify listeners may keep or rewrite it, and the Modify
     * listeners hear of the text it ends up with.
     */
    private static void emptyText(DartCombo combo) {
        if (getCharCount(combo) == 0)
            return;
        String text = verify(combo, "", 0, getCharCount(combo));
        // A listener that emptied the text again has already said so.
        if (text != null && !text.equals(combo.text))
            setText(combo, text, true, false);
    }

    /** An item went in at {@code index}: the selected item stays selected, wherever it moved to. */
    public static void itemInserted(DartCombo combo, int index) {
        combo.selectedIndex = ListItemsHelper.indexAfterInsert(combo.selectedIndex, index);
    }

    /**
     * The item at {@code index} was replaced. When it is the selected one it is selected again, so the combo shows its
     * new text: Win32's behaviour, where the three platforms differ.
     */
    public static void itemReplaced(DartCombo combo, int index) {
        if (readOnly(combo)) {
            syncReadOnlyText(combo);
            return;
        }
        if (index != combo.selectedIndex)
            return;
        combo.selectedIndex = -1;
        combo.select(index);
    }

    /**
     * Takes the item at {@code index} out. Removing the selected item deselects it and empties the text, which an
     * editable combo asks the Verify listeners about first; the selection follows an item before which one was
     * removed.
     */
    public static void removeAt(DartCombo combo, int index) {
        combo.getValue().markDirty(VCombo.ITEMS);
        combo.items = ListItemsHelper.remove(combo.items, index, index);
        int selected = combo.selectedIndex;
        combo.selectedIndex = ListItemsHelper.indexAfterRemove(selected, index, index);
        if (index != selected)
            return;
        if (!readOnly(combo)) {
            emptyText(combo);
            return;
        }
        String shown = combo.text;
        syncReadOnlyText(combo);
        if (!shown.isEmpty())
            combo.sendEvent(SWT.Modify);
    }

    /** Asks the Segments listeners about the text the combo shows, unless a listener has disposed it by now. */
    public static void sendSegments(DartCombo combo) {
        if (!combo.isDisposed())
            sendSegments(combo, combo.getText());
    }

    /** Flutter draws the drop-down list, so only Flutter can say that it opened or closed. */
    public static void listShownByFlutter(DartCombo combo, boolean shown) {
        combo.listVisible = shown;
        // Only a change from what Java last sent reaches Flutter, so Java's own close must follow this.
        combo.getValue().markDirty(VCombo.LIST_VISIBLE);
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
