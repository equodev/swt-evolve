package org.eclipse.swt.widgets;

import dev.equo.swt.size.Mnemonics;
import org.eclipse.swt.SWT;

/**
 * Logic of {@code DartLink} kept out of the generated implementation: the text a Link shows for its markup,
 * and the anchor that holds the Link's focus.
 * <p>
 * A focused Link gives the keyboard to one of its anchors, as every native does: Enter or Space selects
 * it, and Tab or Shift+Tab moves to the next or previous one before it leaves the Link.
 */
public class LinkHelper {

    private LinkHelper() {
    }

    /**
     * The text a Link shows for {@code string}: the tags of its anchors and the '&' of its mnemonics are
     * dropped, "&&" is one '&', and anything that is not an anchor stays as written. The scan follows the
     * state machine of {@code Link.parse}, so a malformed anchor is read the way SWT reads it.
     */
    public static String displayText(String string) {
        if (string == null)
            return "";
        int length = string.length();
        StringBuilder result = new StringBuilder(length);
        int index = 0, state = 0;
        int start = 0, tagStart = 0, linkStart = 0, endtagStart = 0;
        while (index < length) {
            char c = Character.toLowerCase(string.charAt(index));
            switch (state) {
                case 0:
                    if (c == '<') {
                        tagStart = index;
                        state++;
                    }
                    break;
                case 1:
                    if (c == 'a')
                        state++;
                    break;
                case 2:
                    switch (c) {
                        case 'h':
                            state = 7;
                            break;
                        case '>':
                            linkStart = index + 1;
                            state++;
                            break;
                        default:
                            if (!Character.isWhitespace(c))
                                state = 13;
                    }
                    break;
                case 3:
                    if (c == '<') {
                        endtagStart = index;
                        state++;
                    }
                    break;
                case 4:
                    state = c == '/' ? state + 1 : 3;
                    break;
                case 5:
                    state = c == 'a' ? state + 1 : 3;
                    break;
                case 6:
                    if (c == '>') {
                        append(string, start, tagStart, result);
                        append(string, linkStart, endtagStart, result);
                        start = tagStart = linkStart = endtagStart = index + 1;
                        state = 0;
                    } else {
                        state = 3;
                    }
                    break;
                case 7:
                    state = c == 'r' ? state + 1 : 0;
                    break;
                case 8:
                    state = c == 'e' ? state + 1 : 0;
                    break;
                case 9:
                    state = c == 'f' ? state + 1 : 0;
                    break;
                case 10:
                    state = c == '=' ? state + 1 : 0;
                    break;
                case 11:
                    state = c == '"' ? state + 1 : 0;
                    break;
                case 12:
                    if (c == '"')
                        state = 2;
                    break;
                case 13:
                    if (Character.isWhitespace(c))
                        state = 0;
                    else if (c == '=')
                        state++;
                    break;
                case 14:
                    state = c == '"' ? state + 1 : 0;
                    break;
                case 15:
                    if (c == '"')
                        state = 2;
                    break;
                default:
                    state = 0;
                    break;
            }
            index++;
        }
        if (start < length) {
            append(string, start, tagStart, result);
            append(string, Math.max(tagStart, linkStart), length, result);
        }
        return result.toString();
    }

    /** Appends {@code text[start, end)} as it is drawn, with {@link Mnemonics} owning the '&' rule. */
    private static void append(String text, int start, int end, StringBuilder result) {
        if (end > start)
            result.append(Mnemonics.display(text.substring(start, end)));
    }

    /** The anchor holding the Link's focus, or -1 when it has none. */
    static int focusedAnchor(DartLink link) {
        int count = link.ids.length;
        return count == 0 ? -1 : Math.max(0, Math.min(link.focusIndex, count - 1));
    }

    /**
     * Whether a Tab traversal leaves the Link: not while an anchor is left in its direction, which the
     * Tab moves the focus to instead.
     */
    public static boolean tabLeaves(DartLink link, int detail) {
        int anchor = focusedAnchor(link);
        if (anchor < 0)
            return true;
        if (detail == SWT.TRAVERSE_TAB_NEXT)
            return anchor == link.ids.length - 1;
        if (detail == SWT.TRAVERSE_TAB_PREVIOUS)
            return anchor == 0;
        return true;
    }

    /**
     * What the Link does with a key its listeners let through: Enter or Space selects the focused
     * anchor, and a Tab that did not leave the Link moves the focus to the next or previous anchor.
     */
    public static void actOnKey(DartLink link, Event key) {
        int anchor = focusedAnchor(link);
        if (anchor < 0)
            return;
        switch (key.keyCode) {
            case SWT.CR:
            case SWT.KEYPAD_CR:
            case ' ':
                Event selection = new Event();
                selection.text = link.ids[anchor];
                link.sendEvent(SWT.Selection, selection);
                break;
            case SWT.TAB:
                int step = (key.stateMask & SWT.SHIFT) == 0 ? 1 : -1;
                link.focusIndex = Math.max(0, Math.min(anchor + step, link.ids.length - 1));
                break;
            default:
                break;
        }
    }

    /**
     * A click on the anchor the client reports by its index: that anchor takes the Link's focus, then
     * is selected. {@link #focusedAnchor} keeps an index past the anchors in range.
     */
    public static void clicked(DartLink link, Event e) {
        link.focusIndex = e.index;
        // The index only travels from the client; a native Link's Selection carries none.
        e.index = 0;
        link.sendEvent(SWT.Selection, e);
    }
}
