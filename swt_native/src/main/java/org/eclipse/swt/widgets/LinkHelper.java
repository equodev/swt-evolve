package org.eclipse.swt.widgets;

import dev.equo.swt.size.Mnemonics;

/**
 * Logic of {@code DartLink} kept out of the generated implementation: the text a Link shows for its markup,
 * and the relayout that follows a change of it.
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

    /**
     * Lays the Link out again once the current event is over: its size follows the text it shows, which
     * {@link #displayText} only answers after the new markup is stored.
     */
    public static void relayout(DartLink link) {
        link.getDisplay().asyncExec(() -> {
            Control control = link.getApi();
            if (!control.isDisposed())
                control.getShell().layout(new Control[] { control });
        });
    }
}
