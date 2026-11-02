package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * What a {@link DartText}'s API did to its contents, for the client's undo: the platforms' controls
 * undo setText, append, insert, paste and cut each in their own way, and the text alone does not say
 * which of them made a change.
 *
 * <p>Each edit travels as its number, kind and start in {@code VText.edits}, and, for paste and cut,
 * which the controls record, the text it replaced and inserted in {@code VText.editTexts}. Numbers
 * rise per field, so the client applies each edit once however often it is sent. The edits a send has
 * read stay until the next edit, so a second client describing the field is told them too.
 */
final class TextApiEdits {

    static final int SET_TEXT = 1;
    static final int APPEND = 2;
    static final int INSERT = 3;
    static final int PASTE = 4;
    static final int CUT = 5;

    private final List<int[]> edits = new ArrayList<>();
    private final List<String> texts = new ArrayList<>();
    private int count;
    private boolean read;

    /** Records an edit of {@code kind} about to replace the selection with {@code inserted}. */
    void record(DartText text, int kind, String inserted) {
        if (text.isDisposed())
            return;
        Point selection = text.getSelection();
        int length = text.getCharCount();
        int start = Math.min(selection.x, length), end = Math.min(selection.y, length);
        boolean recorded = kind == PASTE || kind == CUT;
        if (read) {
            edits.clear();
            texts.clear();
            read = false;
        }
        edits.add(new int[] { ++count, kind, start });
        texts.add(recorded ? text.getText().substring(start, end) : "");
        texts.add(recorded ? inserted : "");
        text.getValue().markDirty(VText.EDITS);
        text.getValue().markDirty(VText.EDIT_TEXTS);
    }

    /** Number, kind and start of each edit, in order; what is read here is dropped at the next edit. */
    int[] read() {
        read = true;
        int[] out = new int[edits.size() * 3];
        for (int i = 0; i < edits.size(); i++)
            System.arraycopy(edits.get(i), 0, out, i * 3, 3);
        return out;
    }

    /** The replaced and inserted text of each edit, in the order of {@link #read()}. */
    String[] texts() {
        return texts.toArray(new String[0]);
    }
}
