package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

import java.util.Locale;
import java.util.function.IntFunction;

/**
 * Selecting an item by typing the start of its text, as Win32's list box and list view do. A first
 * letter searches from the item after the current one, so typing it again steps through the items it
 * starts; more letters typed without a pause extend the text, searched from the current item on. The
 * search wraps past the last item.
 */
final class TypeAhead {

    /** The key is not a letter or a digit: it is not typing. */
    static final int NOT_TYPED = -2;

    /** The key was typed but no item starts with the text. */
    static final int NO_MATCH = -1;

    /** How long a pause ends the text typed so far, so the next key starts a search of its own. */
    static final int TIMEOUT_MS = 1000;

    private String typed = "";
    private long typedAt;

    /**
     * The item {@code key} names, given the {@code current} one and the text of each of the
     * {@code count} items: its index, {@link #NO_MATCH}, or {@link #NOT_TYPED}.
     */
    int find(Event key, int current, int count, IntFunction<String> textAt) {
        char c = key.character != 0 ? key.character : (key.keyCode & SWT.KEYCODE_BIT) == 0 ? (char) key.keyCode : 0;
        if (!Character.isLetterOrDigit(c))
            return NOT_TYPED;
        long now = key.time & 0xFFFFFFFFL;
        // A key's time is a wrapping 32-bit count: a pause either way of the last key ends the text.
        if (Math.abs(now - typedAt) > TIMEOUT_MS)
            typed = "";
        typedAt = now;
        typed += Character.toLowerCase(c);
        String text = typed;
        boolean repeated = text.chars().allMatch(ch -> ch == text.charAt(0));
        String prefix = repeated ? text.substring(0, 1) : text;
        int start = repeated ? current + 1 : Math.max(0, current);
        for (int n = 0; n < count; n++) {
            int index = Math.floorMod(start + n, count);
            if (textAt.apply(index).toLowerCase(Locale.ROOT).startsWith(prefix))
                return index;
        }
        return NO_MATCH;
    }
}
