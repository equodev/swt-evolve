package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;

public class TextHelper {

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

    public static void setText(DartText text, String string) {
        if (text.hooks(SWT.Verify) || text.filters(SWT.Verify)) {
            int length = text.getCharCount();
            string = text.verifyText(string, 0, length);
            if (string == null)
                return;
        }

        text.clearSegments(false);

        if (text.textLimit != Text.LIMIT && string.length() > text.textLimit) {
            string = string.substring(0, text.textLimit);
        }

        text.setEditText(string);
        text.text = string;
        text.selection = null;

        text.updateAutoTextDirectionIfNeeded();
        text.applySegments();
        text.sendEvent(SWT.Modify);
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
