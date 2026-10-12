package org.eclipse.swt.graphics;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;

/**
 * Hand-written support for {@code DartTextLayout}: what its layout engine decides from the text alone, and where
 * each platform's SWT lays text out differently. Every method that differs by platform takes the platform's name,
 * as {@code SWT.getPlatform()} gives it.
 */
public final class TextLayoutHelper {

    private TextLayoutHelper() {
    }

    /**
     * The width of {@code text[start, end)} on a line that starts at {@code lineStart} and is placed
     * {@code lineX} pixels right of the layout's left edge (a tab advances to a stop counted from that edge).
     */
    public interface Advance {
        double width(int lineStart, int start, int end, double lineX);
    }

    /**
     * Metrics of one laid-out line, {@code ascent} above its baseline and {@code descent} below. The line box comes
     * from the same glyph table the renderer sizes text with, so a caller aligning to
     * {@code getLineMetrics(...).getAscent()} lands on the baseline {@code StyledTextRenderer.getBaseline()} gives.
     */
    public static FontMetrics lineMetrics(int ascent, int descent, int averageCharWidth) {
        DartFontMetrics metrics = new DartFontMetrics(null);
        metrics.setMetrics(ascent, descent, ascent + descent, averageCharWidth);
        return metrics.getApi();
    }

    static boolean isDelimiter(char c) {
        return c == '\n' || c == '\r';
    }

    /** Where the text of the line {@code [start, end)} stops: its line delimiter left out. */
    public static int contentEnd(String text, int start, int end) {
        while (end > start && isDelimiter(text.charAt(end - 1))) end--;
        return end;
    }

    /** Whether a line starting at {@code offset} is the first line of its paragraph. */
    public static boolean startsParagraph(String text, int offset) {
        return offset == 0 || isDelimiter(text.charAt(offset - 1));
    }

    /** Where the visible text of {@code [start, end)} stops: the whitespace a wrapped line ends with hangs past it. */
    public static int visibleEnd(String text, int start, int end) {
        while (end > start && Character.isWhitespace(text.charAt(end - 1))) end--;
        return end;
    }

    /**
     * The offset each line starts at, then the text's length. A line delimiter ({@code \n}, {@code \r} or
     * {@code \r\n}) ends a paragraph; with a wrap width, a paragraph breaks at the last break opportunity whose text
     * fits, its first line {@code indent} pixels in and the others {@code wrapIndent}, and a word wider than the line
     * breaks inside. The whitespace before a break hangs past the width. GTK and Win32 lay an empty line after a
     * final delimiter; Cocoa does not. A paragraph also ends before each offset of {@code breaks}, where a line
     * delimiter is given as a segment character.
     */
    public static int[] lineBreaks(String text, int wrapWidth, int indent, int wrapIndent, String platform, Advance advance,
            int[] breaks) {
        int length = text.length();
        List<Integer> starts = new ArrayList<>();
        starts.add(0);
        int paragraph = 0;
        while (true) {
            int end = paragraph;
            while (end < length && !isDelimiter(text.charAt(end)) && !(end > paragraph && contains(breaks, end))) end++;
            if (wrapWidth != -1 && end > paragraph) wrap(text, paragraph, end, wrapWidth, indent, wrapIndent, advance, starts);
            if (end == length) break;
            int next = !isDelimiter(text.charAt(end)) ? end
                    : end + (text.charAt(end) == '\r' && end + 1 < length && text.charAt(end + 1) == '\n' ? 2 : 1);
            if (next == length && "cocoa".equals(platform)) break;
            starts.add(next);
            paragraph = next;
        }
        starts.add(length);
        int[] result = new int[starts.size()];
        for (int i = 0; i < result.length; i++) result[i] = starts.get(i);
        return result;
    }

    private static void wrap(String text, int start, int end, int wrapWidth, int indent, int wrapIndent, Advance advance,
            List<Integer> starts) {
        BreakIterator breaks = BreakIterator.getLineInstance();
        breaks.setText(text.substring(start, end));
        int lineStart = start;
        int lineIndent = indent;
        while (true) {
            double available = wrapWidth - lineIndent;
            int fit = -1;
            for (int b = breaks.following(lineStart - start); b != BreakIterator.DONE; b = breaks.next()) {
                int brk = start + b;
                if (advance.width(lineStart, lineStart, visibleEnd(text, lineStart, brk), lineIndent) > available) break;
                fit = brk;
            }
            if (fit == -1) {
                fit = next(text, lineStart, end);
                while (fit < end) {
                    int after = next(text, fit, end);
                    if (advance.width(lineStart, lineStart, after, lineIndent) > available) break;
                    fit = after;
                }
            }
            if (fit >= end) return;
            starts.add(fit);
            lineStart = fit;
            lineIndent = wrapIndent;
        }
    }

    static boolean contains(int[] offsets, int offset) {
        for (int o : offsets) {
            if (o == offset) return true;
        }
        return false;
    }

    /** The offset after the character at {@code offset}, a surrogate pair kept whole. */
    private static int next(String text, int offset, int end) {
        return offset + (Character.isHighSurrogate(text.charAt(offset)) && offset + 1 < end
                && Character.isLowSurrogate(text.charAt(offset + 1)) ? 2 : 1);
    }

    /**
     * The next cluster boundary from {@code offset}, which is neither the end when moving forward nor 0 when moving
     * backward. Cocoa steps one code point, keeping only a surrogate pair together; GTK and Win32 keep a character
     * with its combining marks.
     */
    public static int nextCluster(String text, int offset, boolean forward, String platform) {
        int length = text.length();
        if ("cocoa".equals(platform)) {
            int step = forward ? 1 : -1;
            offset += step;
            if (0 < offset && offset < length && Character.isLowSurrogate(text.charAt(offset))
                    && Character.isHighSurrogate(text.charAt(offset - 1))) {
                offset += step;
            }
            return offset;
        }
        BreakIterator clusters = BreakIterator.getCharacterInstance();
        clusters.setText(text);
        return forward ? clusters.following(offset) : clusters.preceding(offset);
    }

    /**
     * The next word boundary from {@code offset} for {@code SWT.MOVEMENT_WORD}, {@code MOVEMENT_WORD_START} or
     * {@code MOVEMENT_WORD_END} ({@code offset} is neither the end when moving forward nor 0 when moving backward).
     * Cocoa builds on {@code NSAttributedString.nextWordFromIndex}: a word is a run of letters and digits as the
     * Unicode word boundaries delimit it, so an apostrophe, a period or an underscore inside one does not end it,
     * and a line delimiter is no stop of its own. Elsewhere a word is a run of letters, digits and underscores, and
     * a line delimiter is a stop.
     */
    public static int wordOffset(String text, int offset, int movement, boolean forward, String platform) {
        if ("cocoa".equals(platform)) return cocoaWordOffset(text, offset, movement, forward);
        switch (movement) {
            case SWT.MOVEMENT_WORD:
                return forward ? wordEnd(text, offset) : previousWordStart(text, offset);
            case SWT.MOVEMENT_WORD_END:
                return forward ? wordEnd(text, offset) : previousWordEnd(text, offset);
            case SWT.MOVEMENT_WORD_START:
                return forward ? nextWordStart(text, offset) : previousWordStart(text, offset);
            default:
                return offset;
        }
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    /** The end of the word at or after {@code offset}. */
    private static int wordEnd(String text, int offset) {
        int length = text.length();
        while (offset < length && !isWordChar(text.charAt(offset))) offset++;
        while (offset < length && isWordChar(text.charAt(offset))) offset++;
        return offset;
    }

    /** The first word start or line delimiter after {@code offset}, else the end. */
    private static int nextWordStart(String text, int offset) {
        int length = text.length();
        for (int p = offset + 1; p < length; p++) {
            char c = text.charAt(p);
            if (c == '\n' || (isWordChar(c) && !isWordChar(text.charAt(p - 1)))) return p;
        }
        return length;
    }

    /** The last word start or line delimiter before {@code offset}, else 0. */
    private static int previousWordStart(String text, int offset) {
        for (int p = offset - 1; p >= 1; p--) {
            char c = text.charAt(p);
            if (c == '\n' || (isWordChar(c) && !isWordChar(text.charAt(p - 1)))) return p;
        }
        return 0;
    }

    /** The end of the word before {@code offset}. */
    private static int previousWordEnd(String text, int offset) {
        while (offset > 0 && isWordChar(text.charAt(offset - 1))) offset--;
        while (offset > 0 && !isWordChar(text.charAt(offset - 1))) offset--;
        return offset;
    }

    static int cocoaWordOffset(String text, int offset, int movement, boolean forward) {
        int length = text.length();
        switch (movement) {
            case SWT.MOVEMENT_WORD:
                return nextWord(text, offset, forward);
            case SWT.MOVEMENT_WORD_END:
                if (forward) return nextWord(text, offset, true);
                for (int result = 0; result < length; ) {
                    int wordEnd = nextWord(text, result, true);
                    if (wordEnd >= offset) return result;
                    result = wordEnd;
                }
                return offset;
            case SWT.MOVEMENT_WORD_START:
                if (!forward) return nextWord(text, offset, false);
                for (int result = length; result > 0; ) {
                    int wordStart = nextWord(text, result, false);
                    if (wordStart <= offset) return result;
                    result = wordStart;
                }
                return offset;
            default:
                return offset;
        }
    }

    /** Forward, the end of the first word that ends after {@code offset}; backward, the start of the last word that starts before it. */
    static int nextWord(String text, int offset, boolean forward) {
        BreakIterator words = BreakIterator.getWordInstance();
        words.setText(text);
        int found = forward ? text.length() : 0;
        for (int start = words.first(), end = words.next(); end != BreakIterator.DONE; start = end, end = words.next()) {
            if (!isWord(text, start, end)) continue;
            if (forward && end > offset) return end;
            if (!forward && start < offset) found = start;
        }
        return found;
    }

    private static boolean isWord(String text, int start, int end) {
        for (int i = start; i < end; i++) {
            if (Character.isLetterOrDigit(text.charAt(i))) return true;
        }
        return false;
    }

    /** Whether a selection drawn by {@code TextLayout.draw} takes the selection foreground: Cocoa only fills its background. */
    public static boolean drawsSelectionForeground(String platform) {
        return !"cocoa".equals(platform);
    }

    /** Whether {@code setTextDirection} is the platform's own setting: on GTK and Cocoa it does nothing and the text direction is the orientation. */
    public static boolean hasTextDirection(String platform) {
        return !"cocoa".equals(platform) && !"gtk".equals(platform);
    }

    /**
     * What {@code setTextDirection(requested)} leaves the text direction at, given the {@code current} one: only
     * the direction bits count, nothing changes for none, and left to right wins over right to left.
     */
    public static int textDirection(String platform, int current, int requested) {
        if (!hasTextDirection(platform)) return current;
        requested &= SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
        if (requested == 0) return current;
        if (requested != SWT.AUTO_TEXT_DIRECTION && (requested & SWT.LEFT_TO_RIGHT) != 0) return SWT.LEFT_TO_RIGHT;
        return requested;
    }

    /** What {@code getTextDirection()} answers: the orientation, or the direction set, the automatic one resolved from the text. */
    public static int resolvedTextDirection(String platform, int orientation, int textDirection, String text) {
        if (!hasTextDirection(platform)) return orientation;
        if (textDirection != SWT.AUTO_TEXT_DIRECTION) return textDirection;
        return new java.text.Bidi(text, java.text.Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).baseIsLeftToRight()
                ? SWT.LEFT_TO_RIGHT : SWT.RIGHT_TO_LEFT;
    }

    /**
     * The next tab stop strictly after {@code x}, in pixels from the layout's left edge. The stops set are honoured in
     * order, and past them the last interval repeats (a single stop repeats as a fixed interval). With none set, Cocoa
     * has no stops (a tab takes no space) and the others put one every {@code defaultTabWidth}, or every 8 spaces.
     * The epsilon keeps a run measured a hair under an exact stop from advancing to that same stop.
     */
    public static double nextTabStop(double x, int[] tabs, double defaultTabWidth, String platform, java.util.function.DoubleSupplier spaceWidth) {
        if (tabs != null && tabs.length > 0) {
            for (int tab : tabs) {
                if (tab > x + 0.01) return tab;
            }
            int last = tabs[tabs.length - 1];
            int previous = tabs.length > 1 ? tabs[tabs.length - 2] : 0;
            double interval = Math.max(1, last - previous);
            double stop = last;
            while (stop <= x + 0.01) stop += interval;
            return stop;
        }
        if ("cocoa".equals(platform)) return x;
        double width = defaultTabWidth > 0 ? defaultTabWidth : spaceWidth.getAsDouble() * 8;
        if (width <= 0) return x;
        return (Math.floor((x + 0.01) / width) + 1) * width;
    }
}
