package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;

public class ComboHelper {

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

        combo.dirty();

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
