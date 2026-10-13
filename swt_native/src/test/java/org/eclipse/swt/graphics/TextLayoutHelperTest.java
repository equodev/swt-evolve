package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The text-only decisions of the web TextLayout engine, the same on every OS: where lines break, what a cluster and
 * a word are, the text direction and the tab stops. Widths come from a monospace advance of 10 pixels per character,
 * a tab advancing to the next multiple of 40 from the layout's left edge.
 */
@Tag("native-unit")
class TextLayoutHelperTest {

    private static final TextLayoutHelper.Advance MONOSPACE = (lineStart, start, end, lineX) -> {
        double x = lineX, atStart = lineX;
        for (int i = Math.min(lineStart, start); i < end; i++) {
            if (i == start) atStart = x;
            x = TextLayoutHelperTest.text.charAt(i) == '\t' ? (Math.floor(x / 40) + 1) * 40 : x + 10;
        }
        return x - atStart;
    };

    private static String text;

    private static int[] breaks(String content, int wrapWidth, int indent, int wrapIndent) {
        text = content;
        return TextLayoutHelper.lineBreaks(content, wrapWidth, indent, wrapIndent, MONOSPACE, new int[0]);
    }

    private static int[] breaks(String content, int wrapWidth) {
        return breaks(content, wrapWidth, 0, 0);
    }

    @Test
    void aFinalDelimiterStartsAnEmptyLine() {
        assertThat(breaks("0123456\n890123\n", -1)).containsExactly(0, 8, 15, 15);
        assertThat(breaks("\n", -1)).containsExactly(0, 1, 1);
    }

    @Test
    void crLfCrAndLfEndAParagraph() {
        assertThat(breaks("ab\r\ncd", -1)).containsExactly(0, 4, 6);
        assertThat(breaks("ab\rcd", -1)).containsExactly(0, 3, 5);
        assertThat(breaks("a\n\nb", -1)).containsExactly(0, 2, 3, 4);
        assertThat(breaks("", -1)).containsExactly(0, 0);
        assertThat(breaks("ab\rc", -1)).containsExactly(0, 3, 4);
        assertThat(breaks("ab\r", -1)).containsExactly(0, 3, 3);
    }

    @Test
    void theSpaceThatEndsAWrappedLineHangsPastTheWidth() {
        assertThat(breaks("alpha beta gamma", 101)).containsExactly(0, 11, 16);
        assertThat(breaks("alpha beta gamma", 100)).containsExactly(0, 11, 16);
        assertThat(breaks("alpha beta gamma", 99)).containsExactly(0, 6, 11, 16);
    }

    @Test
    void aWordWiderThanTheLineBreaksInside() {
        assertThat(breaks("abcdefghij", 51)).containsExactly(0, 5, 10);
        assertThat(breaks("ab abcdefghij", 51)).containsExactly(0, 3, 8, 13);
        assertThat(breaks("abc", 5)).as("a character wider than the line still takes one").containsExactly(0, 1, 2, 3);
    }

    @Test
    void aSurrogatePairIsNotSplit() {
        assertThat(breaks("a\uD83D\uDE00b", 15)).containsExactly(0, 1, 3, 4);
        assertThat(breaks("ab\uD83D", 15)).as("an unpaired surrogate is a character").containsExactly(0, 1, 2, 3);
        assertThat(breaks("a\uD83Dx", 15)).containsExactly(0, 1, 2, 3);
    }

    @Test
    void theIndentNarrowsTheFirstLineAndTheWrapIndentTheOthers() {
        assertThat(breaks("aaaa bbbb cccc", 100, 0, 0)).containsExactly(0, 10, 14);
        assertThat(breaks("aaaa bbbb cccc", 100, 20, 0)).containsExactly(0, 5, 14);
        assertThat(breaks("aaaa bbbb cccc dddd", 100, 0, 60)).containsExactly(0, 10, 15, 19);
        assertThat(breaks("aaaa\nbbbb cccc", 100, 20, 0)).as("every paragraph's first line").containsExactly(0, 5, 10, 14);
    }

    @Test
    void aTabIsMeasuredFromTheLayoutsLeftEdge() {
        assertThat(breaks("ab\tcd", 90, 0, 0)).as("the tab reaches 40, cd ends at 60").containsExactly(0, 5);
        assertThat(breaks("ab\tcd", 90, 25, 0)).as("indented by 25, the tab reaches 80, cd ends 75 past the indent")
                .containsExactly(0, 3, 5);
    }

    @Test
    void aDelimiterGivenAsASegmentCharacterEndsAParagraph() {
        text = "AbcdefGhijKl";
        assertThat(TextLayoutHelper.lineBreaks(text, -1, 0, 0, MONOSPACE, new int[] {6, 10})).containsExactly(0, 6, 10, 12);
        assertThat(TextLayoutHelper.lineBreaks(text, 35, 0, 0, MONOSPACE, new int[] {6}))
                .as("each paragraph wraps on its own").containsExactly(0, 3, 6, 9, 12);
        assertThat(TextLayoutHelper.lineBreaks(text, -1, 0, 0, MONOSPACE, new int[] {0})).as("no empty line before the text")
                .containsExactly(0, 12);
    }

    @Test
    void withoutAWidthOnlyDelimitersBreak() {
        assertThat(breaks("alpha beta gamma delta", -1)).containsExactly(0, 22);
    }

    @Test
    void textHelpers() {
        assertThat(TextLayoutHelper.contentEnd("ab\r\n", 0, 4)).isEqualTo(2);
        assertThat(TextLayoutHelper.contentEnd("ab", 0, 2)).isEqualTo(2);
        assertThat(TextLayoutHelper.startsParagraph("ab\ncd", 3)).isTrue();
        assertThat(TextLayoutHelper.startsParagraph("ab cd", 3)).isFalse();
        assertThat(TextLayoutHelper.startsParagraph("ab", 0)).isTrue();
        assertThat(TextLayoutHelper.visibleEnd("ab  ", 0, 4)).isEqualTo(2);
    }

    @Test
    void aClusterIsACharacterWithItsMarks() {
        String combining = "e\u0301x";
        assertThat(TextLayoutHelper.nextCluster(combining, 0, true)).isEqualTo(2);
        assertThat(TextLayoutHelper.nextCluster(combining, 2, false)).isEqualTo(0);
        String pair = "a\uD83D\uDE00b";
        assertThat(TextLayoutHelper.nextCluster(pair, 1, true)).isEqualTo(3);
        assertThat(TextLayoutHelper.nextCluster(pair, 3, false)).isEqualTo(1);
        assertThat(TextLayoutHelper.nextCluster(pair, 3, true)).isEqualTo(4);
    }

    @Test
    void aWordIsLettersDigitsAndUnderscoresAndADelimiterIsAStop() {
        String text = "foo_1, bar\nbaz";
        assertThat(TextLayoutHelper.wordOffset(text, 0, SWT.MOVEMENT_WORD, true)).isEqualTo(5);
        assertThat(TextLayoutHelper.wordOffset(text, 5, SWT.MOVEMENT_WORD_END, true)).isEqualTo(10);
        assertThat(TextLayoutHelper.wordOffset(text, 13, SWT.MOVEMENT_WORD, false)).isEqualTo(11);
        assertThat(TextLayoutHelper.wordOffset(text, 11, SWT.MOVEMENT_WORD, false)).isEqualTo(10);
        assertThat(TextLayoutHelper.wordOffset(text, 3, SWT.MOVEMENT_WORD, false)).isZero();
        assertThat(TextLayoutHelper.wordOffset(text, 13, SWT.MOVEMENT_WORD_END, false)).isEqualTo(10);
        assertThat(TextLayoutHelper.wordOffset(text, 0, SWT.MOVEMENT_WORD_START, true)).isEqualTo(7);
        assertThat(TextLayoutHelper.wordOffset(text, 7, SWT.MOVEMENT_WORD_START, true)).isEqualTo(10);
        assertThat(TextLayoutHelper.wordOffset("ab  ", 1, SWT.MOVEMENT_WORD_START, true)).isEqualTo(4);
        assertThat(TextLayoutHelper.wordOffset(text, 13, SWT.MOVEMENT_WORD_START, false)).isEqualTo(11);
        assertThat(TextLayoutHelper.wordOffset("ab  ", 2, SWT.MOVEMENT_WORD_END, true)).isEqualTo(4);
        assertThat(TextLayoutHelper.wordOffset("ab", 2, SWT.MOVEMENT_WORD_END, false)).isZero();
        assertThat(TextLayoutHelper.wordOffset("a bc", 3, SWT.MOVEMENT_WORD_START, false)).isEqualTo(2);
        assertThat(TextLayoutHelper.wordOffset("ab\ncd", 3, SWT.MOVEMENT_WORD_START, false)).isEqualTo(2);
        assertThat(TextLayoutHelper.wordOffset(text, 7, SWT.MOVEMENT_WORD_START, false)).isZero();
        assertThat(TextLayoutHelper.wordOffset(text, 2, SWT.MOVEMENT_CLUSTER, true)).isEqualTo(2);
        assertThat(TextLayoutHelper.wordOffset("don't foo.bar", 0, SWT.MOVEMENT_WORD_END, true)).as("an apostrophe ends a word").isEqualTo(3);
        assertThat(TextLayoutHelper.wordOffset("foo, bar_baz qux", 3, SWT.MOVEMENT_WORD_END, true)).isEqualTo(12);
    }

    @Test
    void theTextDirectionIsItsOwnSetting() {
        assertThat(TextLayoutHelper.textDirection(SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT)).isEqualTo(SWT.RIGHT_TO_LEFT);
        assertThat(TextLayoutHelper.textDirection(SWT.RIGHT_TO_LEFT, SWT.NONE)).isEqualTo(SWT.RIGHT_TO_LEFT);
        assertThat(TextLayoutHelper.textDirection(SWT.RIGHT_TO_LEFT, SWT.LEFT_TO_RIGHT | SWT.BOLD)).isEqualTo(SWT.LEFT_TO_RIGHT);
        assertThat(TextLayoutHelper.textDirection(SWT.LEFT_TO_RIGHT, SWT.AUTO_TEXT_DIRECTION)).isEqualTo(SWT.AUTO_TEXT_DIRECTION);
        assertThat(TextLayoutHelper.resolvedTextDirection(SWT.RIGHT_TO_LEFT, "abc")).isEqualTo(SWT.RIGHT_TO_LEFT);
        assertThat(TextLayoutHelper.resolvedTextDirection(SWT.AUTO_TEXT_DIRECTION, "\u05e9\u05e0 abc")).isEqualTo(SWT.RIGHT_TO_LEFT);
        assertThat(TextLayoutHelper.resolvedTextDirection(SWT.AUTO_TEXT_DIRECTION, "abc")).isEqualTo(SWT.LEFT_TO_RIGHT);
    }

    @Test
    void tabStops() {
        int[] single = {40}, pair = {30, 50};
        assertThat(TextLayoutHelper.nextTabStop(10, single, 0, () -> 5)).isEqualTo(40);
        assertThat(TextLayoutHelper.nextTabStop(40, single, 0, () -> 5)).isEqualTo(80);
        assertThat(TextLayoutHelper.nextTabStop(39.995, single, 0, () -> 5)).isEqualTo(80);
        assertThat(TextLayoutHelper.nextTabStop(35, pair, 0, () -> 5)).isEqualTo(50);
        assertThat(TextLayoutHelper.nextTabStop(75, pair, 0, () -> 5)).isEqualTo(90);
        assertThat(TextLayoutHelper.nextTabStop(10, null, 0, () -> 5)).as("with no stops set, one every 8 spaces").isEqualTo(40);
        assertThat(TextLayoutHelper.nextTabStop(10, new int[0], 25, () -> 5)).isEqualTo(25);
        assertThat(TextLayoutHelper.nextTabStop(10, null, 0, () -> 0)).isEqualTo(10);
    }
}
