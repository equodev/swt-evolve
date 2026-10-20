package org.eclipse.swt.widgets;

import dev.equo.swt.Serializer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;

public class TextHelper {

    private TextHelper() {
    }

    public static void insertEditText(DartText text, String string) {
        int length = string.length();
        Point selection = text.getSelection();
        String oldText = text.getText();

        if (text.hasFocus() && text.hiddenText == null) {
            if (text.textLimit != Text.LIMIT) {
                int charCount = text.getCharCount();
                int selectionLength = selection.y - selection.x;
                if (charCount - selectionLength + length > text.textLimit) {
                    length = text.textLimit - charCount + selectionLength;
                    length = Math.max(0, length);
                }
            }

            char[] buffer = new char[length];
            string.getChars(0, buffer.length, buffer, 0);

            String newText =
                    oldText.substring(0, selection.x)
                            + string.substring(0, length)
                            + oldText.substring(selection.y);

            text.setEditText(newText);
            text.setSelection(selection.x + length);
        } else {
            if (text.textLimit != Text.LIMIT) {
                int charCount = oldText.length();
                if (charCount - (selection.y - selection.x) + length > text.textLimit) {
                    int newLength = Math.max(
                            0,
                            text.textLimit - charCount + (selection.y - selection.x)
                    );
                    string = string.substring(0, newLength);
                }
            }

            String newText =
                    oldText.substring(0, selection.x)
                            + string
                            + oldText.substring(selection.y);

            text.setEditText(newText);
            text.setSelection(selection.x + string.length());
        }
    }

    /**
     * Asks the Segments listeners for the segments of the current text, as GTK does after every edit.
     * The separators are not drawn: the rendered text is the text without them.
     */
    public static void sendSegments(DartText text) {
        if (text.isDisposed() || (!text.hooks(SWT.Segments) && !text.filters(SWT.Segments)))
            return;
        String string = text.getText();
        Event event = new Event();
        event.text = string;
        event.segments = text.segments;
        text.sendEvent(SWT.Segments, event);
        text.segments = event.segments;
        if (text.segments == null)
            return;
        for (int i = 1; i < text.segments.length; i++) {
            if (text.segments[i] < text.segments[i - 1] || text.segments[i] > string.length())
                text.error(SWT.ERROR_INVALID_ARGUMENT);
        }
    }

    public static void applySegments(DartText text) {
        /*
         * It is possible (but unlikely), that application code could have
         * disposed the widget in the modify event. If this happens, return to
         * cancel the operation.
         */
        if (text.isDisposed() || --text.clearSegmentsCount != 0)
            return;
        if (!text.hooks(SWT.Segments) && !text.filters(SWT.Segments))
            return;
        /* Get segments text */
        String string = text.getText();
        Event event = new Event();
        event.text = string;
        event.segments = text.segments;
        text.sendEvent(SWT.Segments, event);
        text.segments = event.segments;
        if (text.segments == null)
            return;
        int nSegments = text.segments.length;
        if (nSegments == 0)
            return;
        for (int i = 1; i < nSegments; i++) {
        }
        char[] segmentsChars = event.segmentsChars;
        char[] segmentsCharsCrLf = segmentsChars == null ? null : withCrLf(segmentsChars);
        if (segmentsChars != segmentsCharsCrLf) {
            int[] segmentsCrLf = new int[nSegments + Math.min(nSegments, segmentsCharsCrLf.length - segmentsChars.length)];
            for (int i = 0, c = 0; i < segmentsChars.length && i < nSegments; i++) {
                if (segmentsChars[i] == '\n' && segmentsCharsCrLf[i + c] == '\r') {
                    segmentsCrLf[i + c++] = text.segments[i];
                }
                segmentsCrLf[i + c] = text.segments[i];
            }
            text.segments = segmentsCrLf;
            nSegments = text.segments.length;
            segmentsChars = segmentsCharsCrLf;
        }
        int charCount = 0, segmentCount = 0;
        char defaultSeparator = text.getOrientation() == SWT.RIGHT_TO_LEFT ? '\u200f' : '\u200e';
        while (segmentCount < nSegments) {
            text.segments[segmentCount] = charCount - segmentCount;
            segmentCount++;
        }
        /* Get the current selection */
        int[] start = new int[1], end = new int[1];
        if (text.selection != null) {
            start[0] = text.selection.x;
            end[0] = text.selection.y;
        } else {
            start[0] = 0;
            end[0] = 0;
        }
        boolean oldIgnoreCharacter = text.ignoreCharacter, oldIgnoreModify = text.ignoreModify, oldIgnoreVerify = text.ignoreVerify;
        text.ignoreCharacter = text.ignoreModify = text.ignoreVerify = true;
        /* Restore selection */
        start[0] = text.translateOffset(start[0]);
        end[0] = text.translateOffset(end[0]);
        text.selection = new Point(start[0], end[0]);
        text.ignoreCharacter = oldIgnoreCharacter;
        text.ignoreModify = oldIgnoreModify;
        text.ignoreVerify = oldIgnoreVerify;
    }


    public static void clearSegments(DartText text, boolean applyText) {
        if (text.clearSegmentsCount++ != 0)
            return;
        if (text.segments == null)
            return;
        int nSegments = text.segments.length;
        if (nSegments == 0)
            return;
        if (!applyText) {
            text.segments = null;
            return;
        }
        boolean oldIgnoreCharacter = text.ignoreCharacter, oldIgnoreModify = text.ignoreModify, oldIgnoreVerify = text.ignoreVerify;
        text.ignoreCharacter = text.ignoreModify = text.ignoreVerify = true;
        /* Get the current selection */
        int[] start = new int[1], end = new int[1];
        if (text.selection != null) {
            start[0] = text.selection.x;
            end[0] = text.selection.y;
        } else {
            start[0] = 0;
            end[0] = 0;
        }
        start[0] = text.untranslateOffset(start[0]);
        end[0] = text.untranslateOffset(end[0]);
        text.segments = null;
        text.setSelection(start[0], end[0]);
        text.ignoreCharacter = oldIgnoreCharacter;
        text.ignoreModify = oldIgnoreModify;
        text.ignoreVerify = oldIgnoreVerify;
    }

    public static void copy(DartText text) {
        if ((text.getApi().style & SWT.PASSWORD) != 0 || text.echoCharacter != '\0')
            return;
        Point selection = text.getSelection();
        if (selection.x == selection.y)
            return;

        boolean processSegments = text.segments != null;
        if (processSegments) {
            clearSegments(text, true);
        }

        char[] textToCopy = text.getEditText(selection.x, selection.y - 1);
        if (textToCopy.length > 0) {
            Clipboard clipboard = new Clipboard(text.display);
            clipboard.setContents(new Object[] { new String(textToCopy) },
                    new Transfer[] { TextTransfer.getInstance() });
            clipboard.dispose();
        }

        if (processSegments) {
            applySegments(text);
        }
    }

    public static void cut(DartText text) {
        if ((text.getApi().style & SWT.PASSWORD) != 0 || text.echoCharacter != '\0')
            return;
        Point oldSelection = text.getSelection();
        if (oldSelection.x == oldSelection.y)
            return;

        boolean processSegments = text.hooks(SWT.Segments) || text.filters(SWT.Segments);
        if (processSegments) {
            clearSegments(text, true);
        }

        char[] textToCopy = text.getEditText(oldSelection.x, oldSelection.y - 1);
        if (textToCopy.length > 0) {
            Clipboard clipboard = new Clipboard(text.display);
            clipboard.setContents(new Object[] { new String(textToCopy) },
                    new Transfer[] { TextTransfer.getInstance() });
            clipboard.dispose();
        }
        text.insertEditText("");
        text.sendEvent(SWT.Modify);

        if (processSegments) {
            applySegments(text);
        }
    }

    public static void paste(DartText text) {
        boolean processSegments = text.hooks(SWT.Segments) || text.filters(SWT.Segments);
        if (processSegments) {
            clearSegments(text, true);
        }

        Clipboard clipboard = new Clipboard(text.display);
        String textToPaste = (String) clipboard.getContents(TextTransfer.getInstance());
        clipboard.dispose();
        if (textToPaste != null && textToPaste.length() > 0) {
            Point selection = text.getSelection();
            text.insertEditText(textToPaste);
            text.sendEvent(SWT.Modify);
        }

        if (processSegments) {
            applySegments(text);
        }
    }

    public static int getCaretLineNumber(DartText text) {
        if ((text.getApi().style & SWT.SINGLE) != 0)
            return 0;

        int caretPos = text.getCaretPosition();
        String textStr = text.getText();

        int lineNumber = 0;
        for (int i = 0; i < caretPos && i < textStr.length(); i++) {
            char c = textStr.charAt(i);
            if (c == '\n') {
                lineNumber++;
            } else if (c == '\r') {
                lineNumber++;
                if (i + 1 < textStr.length() && textStr.charAt(i + 1) == '\n') {
                    i++;
                    if (i >= caretPos) {
                        break;
                    }
                }
            }
        }

        return lineNumber;
    }

    public static int getLineCount(DartText text) {
        if ((text.getApi().style & SWT.SINGLE) != 0)
            return 1;
        String string = text.getText();
        int length = string.length();
        // Empty string = 1 empty line
        if (length == 0)
            return 1;
        // Start with 1 (first line)
        int count = 1;
        for (int i = 0; i < length; i++) {
            char c = string.charAt(i);
            if (c == '\n') {
                count++;
            } else if (c == '\r') {
                count++;
                // If it's \r\n, skip the \n to avoid counting twice
                if (i + 1 < length && string.charAt(i + 1) == '\n') {
                    i++;
                }
            }
        }
        return count;
    }

    /**
     * Applies a Modify proposal coming from the Flutter client. SWT's Verify contract is that a
     * vetoed keystroke is never displayed, so a Verify-hooked field holds each edit client-side
     * until this answers with a modify/verdict: {@code doit} is whether the proposal survived the
     * Verify/textLimit chain unchanged. (FlutterBridge.send flushes pending state pushes first,
     * so a rejection's corrective push never arrives after its verdict.)
     */
    public static void handleModify(DartText text, Event e) {
        if (e.text == null)
            return;
        String proposed = e.text;
        applyClientEdit(text, proposed, e.start);
        boolean applied = proposed.equals(text.getText());
        if (text.hooks(SWT.Verify) || text.filters(SWT.Verify)) {
            dev.equo.swt.FlutterBridge.send(text, "modify/verdict",
                    dev.equo.swt.Java8.map("doit", applied));
        }
    }

    /**
     * Applies the field's whole new text as the edit the user made: the Verify names only the
     * replaced range and the text typed into it, with the KeyDown that typed it, as every native
     * reports a keystroke, rather than replacing the whole text from 0 the way setText does.
     * {@code caret} is where the client put the caret after the edit, or negative when unknown.
     */
    static void applyClientEdit(DartText text, String proposed, int caret) {
        String old = text.getText();
        Point range = editedRange(old, proposed, text.getSelection(), caret);
        String inserted = proposed.substring(range.x, proposed.length() - (old.length() - range.y));
        Event key = ControlHelper.takeTypingKey(text, inserted);
        if (text.hooks(SWT.Verify) || text.filters(SWT.Verify)) {
            Event verify = new Event();
            verify.text = inserted;
            verify.start = range.x;
            verify.end = range.y;
            if (key != null) {
                verify.character = key.character;
                verify.keyCode = key.keyCode;
                verify.stateMask = key.stateMask;
            }
            text.sendEvent(SWT.Verify, verify);
            if (!verify.doit || text.isDisposed() || verify.text == null)
                return;
            inserted = verify.text;
        }
        // Like a native field at its limit, keep what fits of the inserted text.
        if (text.textLimit != Text.LIMIT) {
            int room = text.textLimit - (old.length() - (range.y - range.x));
            inserted = inserted.substring(0, Math.max(0, Math.min(inserted.length(), room)));
            if (inserted.isEmpty() && range.x == range.y)
                return;
        }
        int at = range.x + inserted.length();
        commit(text, old.substring(0, range.x) + inserted + old.substring(range.y), at);
        if (!text.isDisposed())
            text.setSelection(at);
    }

    /**
     * The range of {@code old} that {@code proposed} replaces. The selection Java last heard from
     * the client is where typing lands, so it wins when the new text agrees with it. Otherwise the
     * text after the client's caret is what the edit left alone, which places a removal among
     * repeated characters where it happened; without a caret, the range is what lies between the
     * common prefix and the common suffix.
     */
    static Point editedRange(String old, String proposed, Point selection, int caret) {
        if (selection != null && selection.x >= 0 && selection.y <= old.length() && selection.x <= selection.y
                && proposed.length() >= selection.x + (old.length() - selection.y)
                && proposed.startsWith(old.substring(0, selection.x))
                && proposed.endsWith(old.substring(selection.y))) {
            return new Point(selection.x, selection.y);
        }
        int max = Math.min(old.length(), proposed.length());
        if (caret >= 0 && caret <= proposed.length() && proposed.length() - caret <= old.length()
                && old.endsWith(proposed.substring(caret))) {
            int suffix = proposed.length() - caret;
            int prefix = 0;
            while (prefix < max - suffix && old.charAt(prefix) == proposed.charAt(prefix))
                prefix++;
            return new Point(prefix, old.length() - suffix);
        }
        int prefix = 0;
        while (prefix < max && old.charAt(prefix) == proposed.charAt(prefix))
            prefix++;
        int suffix = 0;
        while (suffix < max - prefix
                && old.charAt(old.length() - 1 - suffix) == proposed.charAt(proposed.length() - 1 - suffix))
            suffix++;
        return new Point(prefix, old.length() - suffix);
    }

    /**
     * The caret Java recorded, within the text: where the user left it ({@link #clientCaretMoved}), or
     * the start of a selection the application set, as Win32 and Cocoa report it (GTK puts it at
     * the end). No selection yet means no caret has been placed.
     */
    public static int getCaretPosition(DartText text) {
        if (text.selection == null)
            return 0;
        return Math.max(0, Math.min(text.caretPosition, text.getCharCount()));
    }

    /**
     * Whether a Verify coming from the render side proposes no edit at all: nothing to replace, and
     * nothing to replace it with. The client sends one when the user moves the caret -- a click, an
     * arrow key -- and no native raises a Verify for that, because nothing is being edited and a
     * listener has nothing to veto. A real keystroke carries text; a real deletion carries a range.
     */
    public static boolean proposesNoChange(Event e) {
        return e.text != null && e.text.isEmpty() && e.start == e.end;
    }

    /**
     * The user moved the caret or the selection in the client's field without editing: the caret
     * at {@code offset}, the selection running from {@code anchor} to it. getCaretPosition() and
     * getSelection() answer from Java's copy, so it follows; nothing is pushed back, and what the
     * client was last sent of either stops counting as what it holds.
     */
    public static void clientCaretMoved(DartText text, int offset, int anchor) {
        int count = text.getCharCount();
        int caret = Math.max(0, Math.min(offset, count));
        int from = Math.max(0, Math.min(anchor, count));
        text.caretPosition = caret;
        text.selection = new Point(Math.min(from, caret), Math.max(from, caret));
        Serializer.clientChanged(text.getValue(), VText.CARET_POSITION);
        Serializer.clientChanged(text.getValue(), VText.SELECTION);
    }

    public static void setText(DartText text, String string) {
        if (text.hooks(SWT.Verify) || text.filters(SWT.Verify)) {
            int length = text.getCharCount();
            string = text.verifyText(string, 0, length);
            if (string == null)
                return;
        }

        if (text.textLimit != Text.LIMIT && string.length() > text.textLimit) {
            string = string.substring(0, text.textLimit);
        }
        commit(text, string, -1);
    }

    /**
     * Writes the field's new text and reports it, for setText and for an edit typed in the client
     * alike. {@code caret} is where the caret goes, or negative to leave it.
     */
    private static void commit(DartText text, String string, int caret) {
        text.clearSegments(false);
        text.setEditText(string);
        if (caret >= 0) {
            text.selection = null;
            text.caretPosition = caret;
        } else {
            // The old selection belonged to the old text: the caret goes back to the start, as on Win32
            // and GTK (Cocoa leaves a single-line text's caret at the end).
            text.setSelection(0, 0);
        }
        text.updateAutoTextDirectionIfNeeded();
        // Modify before Segments. Upstream calls applySegments() first and only then sends Modify,
        // but for a single-line Text the Modify it reports is not that one: writing the text is what
        // makes the OS raise its own change notification (EN_CHANGE on Win32), synchronously, before
        // applySegments is reached. setEditText above is where that write happens here, so this is
        // where the notification belongs -- Win32 and GTK both record Verify, Modify, Segments.
        text.sendEvent(SWT.Modify);
        text.applySegments();
    }

    public static void setEditText(DartText text, char[] chars) {
        int length = Math.min(chars.length, text.textLimit);
        char[] hidden = null;
        if ((text.getApi().style & SWT.PASSWORD) == 0 && text.echoCharacter != '\0') {
            hidden = new char[length];
            System.arraycopy(chars, 0, hidden, 0, length);
        }
        String updated = new String(chars, 0, length);
        // Recorded, because this is written straight to the field rather than through the setter
        // that would do it. An update names what changed, so a change nothing records is a change
        // the client is never told about: the content it shows stays whatever it last heard, which
        // here is the text from before this edit.
        //
        // hiddenText is kept for the Java side to read but is not recorded: it does not travel, the
        // far side masking the real text with the echo character it already has.
        if (!java.util.Objects.equals(text.text, updated))
            text.getValue().markDirty(VText.TEXT);
        text.hiddenText = hidden;
        text.text = updated;
    }

    private static char[] withCrLf(char[] string) {
        int length = string.length;
        if (length == 0)
            return string;
        int count = 0;
        for (int i = 0; i < string.length; i++) {
            if (string[i] == '\n') {
                count++;
                if (count == 1 && i > 0 && string[i - 1] == '\r')
                    return string;
            }
        }
        if (count == 0)
            return string;
        count += length;
        char[] result = new char[count];
        for (int i = 0, j = 0; i < length && j < count; i++) {
            if (string[i] == '\n') {
                result[j++] = '\r';
            }
            result[j++] = string[i];
        }
        return result;
    }
}
