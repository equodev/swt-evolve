package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

/** A {@link DartText} paste from the in-memory web clipboard, the same in single-line and multi-line Text. */
final class TextPasteHelper {

    private TextPasteHelper() {
    }

    /**
     * Replaces the selection with the clipboard text once the Verify listeners have seen it, clipped to the text
     * limit. A veto leaves the text alone; the insert sends the Modify, only when the text changes.
     */
    static void paste(DartText text) {
        if ((text.getApi().style & SWT.READ_ONLY) != 0)
            return;
        String newText = text.getClipboardText();
        if (newText == null)
            return;
        if (text.hooks(SWT.Verify) || text.filters(SWT.Verify)) {
            Point selection = text.getSelection();
            newText = text.verifyText(newText, selection.x, selection.y);
            if (newText == null)
                return;
        }
        newText = TextHelper.clipToLimit(text, newText, text.getSelection());
        text.apiEdits.record(text, TextApiEdits.PASTE, newText);
        text.insertEditText(newText);
    }
}
