package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The text-only decisions of the web TextLayout engine, per platform: where lines break, what a cluster and a
 * word are, and which settings a platform's SWT honours. Widths come from a monospace advance of 10 pixels per
 * character, a tab advancing to the next multiple of 40 from the layout's left edge.
 */
@Tag("native-unit")
class TextLayoutHelperTest {

    private static final String[] PLATFORMS = {"cocoa", "gtk", "win32", "web"};

    private static final TextLayoutHelper.Advance MONOSPACE = (lineStart, start, end, lineX) -> {
        double x = lineX, atStart = lineX;
        for (int i = Math.min(lineStart, start); i < end; i++) {
            if (i == start) atStart = x;
            x = TextLayoutHelperTest.text.charAt(i) == '\t' ? (Math.floor(x / 40) + 1) * 40 : x + 10;
        }
        return x - atStart;
    };

    private static String text;

    private static int[] breaks(String content, int wrapWidth, int indent, int wrapIndent, String platform) {
        text = content;
        return TextLayoutHelper.lineBreaks(content, wrapWidth, indent, wrapIndent, platform, MONOSPACE, new int[0]);
    }

    private static int[] breaks(String content, int wrapWidth) {
        return breaks(content, wrapWidth, 0, 0, "gtk");
    }

    @Test
    void aFinalDelimiterStartsAnEmptyLineExceptOnCocoa() {
        assertThat(breaks("0123456\n890123\n", -1, 0, 0, "cocoa")).containsExactly(0, 8, 15);
        assertThat(breaks("\n", -1, 0, 0, "cocoa")).containsExactly(0, 1);
        for (String platform : new String[] {"gtk", "win32", "web"}) {
            assertThat(breaks("0123456\n890123\n", -1, 0, 0, platform)).as(platform).containsExactly(0, 8, 15, 15);
            assertThat(breaks("\n", -1, 0, 0, platform)).as(platform).containsExactly(0, 1, 1);
        }
    }

    @Test
    void crLfCrAndLfEndAParagraph() {
        for (String platform : PLATFORMS) {
            assertThat(breaks("ab\r\ncd", -1, 0, 0, platform)).as(platform).containsExactly(0, 4, 6);
            assertThat(breaks("ab\rcd", -1, 0, 0, platform)).as(platform).containsExactly(0, 3, 5);
            assertThat(breaks("a\n\nb", -1, 0, 0, platform)).as(platform).containsExactly(0, 2, 3, 4);
            assertThat(breaks("", -1, 0, 0, platform)).as(platform).containsExactly(0, 0);
            assertThat(breaks("ab\rc", -1, 0, 0, platform)).as(platform).containsExactly(0, 3, 4);
            assertThat(breaks("ab\r", -1, 0, 0, platform)).as(platform).containsExactly("cocoa".equals(platform) ? new int[] {0, 3} : new int[] {0, 3, 3});
        }
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
        assertThat(breaks("aaaa bbbb cccc", 100, 0, 0, "gtk")).containsExactly(0, 10, 14);
        assertThat(breaks("aaaa bbbb cccc", 100, 20, 0, "gtk")).containsExactly(0, 5, 14);
        assertThat(breaks("aaaa bbbb cccc dddd", 100, 0, 60, "gtk")).containsExactly(0, 10, 15, 19);
        assertThat(breaks("aaaa\nbbbb cccc", 100, 20, 0, "gtk")).as("every paragraph's first line").containsExactly(0, 5, 10, 14);
    }

    @Test
    void aTabIsMeasuredFromTheLayoutsLeftEdge() {
        assertThat(breaks("ab\tcd", 90, 0, 0, "gtk")).as("the tab reaches 40, cd ends at 60").containsExactly(0, 5);
        assertThat(breaks("ab\tcd", 90, 25, 0, "gtk")).as("indented by 25, the tab reaches 80, cd ends 75 past the indent")
                .containsExactly(0, 3, 5);
    }

    @Test
    void aDelimiterGivenAsASegmentCharacterEndsAParagraph() {
        text = "AbcdefGhijKl";
        for (String platform : PLATFORMS) {
            assertThat(TextLayoutHelper.lineBreaks(text, -1, 0, 0, platform, MONOSPACE, new int[] {6, 10})).as(platform)
                    .containsExactly(0, 6, 10, 12);
        }
        assertThat(TextLayoutHelper.lineBreaks(text, 35, 0, 0, "gtk", MONOSPACE, new int[] {6}))
                .as("each paragraph wraps on its own").containsExactly(0, 3, 6, 9, 12);
        assertThat(TextLayoutHelper.lineBreaks(text, -1, 0, 0, "gtk", MONOSPACE, new int[] {0})).as("no empty line before the text")
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
    void aClusterIsACodePointOnCocoaAndACharacterWithItsMarksElsewhere() {
        String combining = "e\u0301x";
        assertThat(TextLayoutHelper.nextCluster(combining, 0, true, "cocoa")).isEqualTo(1);
        assertThat(TextLayoutHelper.nextCluster(combining, 2, false, "cocoa")).isEqualTo(1);
        for (String platform : new String[] {"gtk", "win32", "web"}) {
            assertThat(TextLayoutHelper.nextCluster(combining, 0, true, platform)).as(platform).isEqualTo(2);
            assertThat(TextLayoutHelper.nextCluster(combining, 2, false, platform)).as(platform).isEqualTo(0);
        }
        assertThat(TextLayoutHelper.nextCluster("a\uDE00b", 0, true, "cocoa")).as("a low surrogate on its own").isEqualTo(1);
        String pair = "a\uD83D\uDE00b";
        for (String platform : PLATFORMS) {
            assertThat(TextLayoutHelper.nextCluster(pair, 1, true, platform)).as(platform).isEqualTo(3);
            assertThat(TextLayoutHelper.nextCluster(pair, 3, false, platform)).as(platform).isEqualTo(1);
            assertThat(TextLayoutHelper.nextCluster(pair, 3, true, platform)).as(platform).isEqualTo(4);
        }
    }

    @Test
    void cocoaWordsEndForwardAndStartBackward() {
        String text = "word word word";
        assertThat(TextLayoutHelper.cocoaWordOffset(text, 0, SWT.MOVEMENT_WORD, true)).isEqualTo(4);
        assertThat(TextLayoutHelper.cocoaWordOffset(text, 4, SWT.MOVEMENT_WORD, true)).isEqualTo(9);
        assertThat(TextLayoutHelper.cocoaWordOffset(text, 14, SWT.MOVEMENT_WORD, false)).isEqualTo(10);
        assertThat(TextLayoutHelper.cocoaWordOffset(text, 5, SWT.MOVEMENT_WORD, false)).isEqualTo(0);
        assertThat(TextLayoutHelper.cocoaWordOffset(text, 0, SWT.MOVEMENT_WORD_START, true)).isEqualTo(5);
        assertThat(TextLayoutHelper.cocoaWordOffset(text, 10, SWT.MOVEMENT_WORD_START, true)).isEqualTo(14);
        assertThat(TextLayoutHelper.cocoaWordOffset(text, 6, SWT.MOVEMENT_WORD_START, false)).isEqualTo(5);
        assertThat(TextLayoutHelper.cocoaWordOffset(text, 2, SWT.MOVEMENT_WORD_END, true)).isEqualTo(4);
        assertThat(TextLayoutHelper.cocoaWordOffset(text, 14, SWT.MOVEMENT_WORD_END, false)).isEqualTo(9);
        assertThat(TextLayoutHelper.cocoaWordOffset(text, 4, SWT.MOVEMENT_WORD_END, false)).isEqualTo(0);
        assertThat(TextLayoutHelper.cocoaWordOffset(text, 3, SWT.MOVEMENT_CLUSTER, true)).as("not a word movement").isEqualTo(3);
    }

    @Test
    void aCocoaWordKeepsItsApostrophePeriodAndUnderscoreAndStopsAtNoDelimiter() {
        assertThat(TextLayoutHelper.cocoaWordOffset("don't foo.bar", 0, SWT.MOVEMENT_WORD_END, true)).isEqualTo(5);
        assertThat(TextLayoutHelper.cocoaWordOffset("don't foo.bar", 5, SWT.MOVEMENT_WORD_END, true)).isEqualTo(13);
        assertThat(TextLayoutHelper.cocoaWordOffset("foo, bar_baz qux", 3, SWT.MOVEMENT_WORD_END, true)).isEqualTo(12);
        assertThat(TextLayoutHelper.cocoaWordOffset("ab cd\nef gh", 3, SWT.MOVEMENT_WORD_START, true)).isEqualTo(6);
        assertThat(TextLayoutHelper.cocoaWordOffset("ab cd\nef gh", 8, SWT.MOVEMENT_WORD_START, false)).isEqualTo(6);
    }

    @Test
    void withNoWordLeftAMovementGoesToTheEnd() {
        assertThat(TextLayoutHelper.cocoaWordOffset("ab  ", 2, SWT.MOVEMENT_WORD, true)).isEqualTo(4);
        assertThat(TextLayoutHelper.cocoaWordOffset("  ab", 2, SWT.MOVEMENT_WORD, false)).isEqualTo(0);
        assertThat(TextLayoutHelper.cocoaWordOffset("  ", 1, SWT.MOVEMENT_WORD_END, false)).isEqualTo(0);
        assertThat(TextLayoutHelper.cocoaWordOffset("", 0, SWT.MOVEMENT_WORD_START, true)).isEqualTo(0);
        assertThat(TextLayoutHelper.cocoaWordOffset("", 0, SWT.MOVEMENT_WORD_END, false)).isEqualTo(0);
    }

    @Test
    void elsewhereAWordIsLettersDigitsAndUnderscoresAndADelimiterIsAStop() {
        for (String platform : new String[] {"gtk", "win32", "web"}) {
            String text = "foo_1, bar\nbaz";
            assertThat(TextLayoutHelper.wordOffset(text, 0, SWT.MOVEMENT_WORD, true, platform)).as(platform).isEqualTo(5);
            assertThat(TextLayoutHelper.wordOffset(text, 5, SWT.MOVEMENT_WORD_END, true, platform)).as(platform).isEqualTo(10);
            assertThat(TextLayoutHelper.wordOffset(text, 13, SWT.MOVEMENT_WORD, false, platform)).as(platform).isEqualTo(11);
            assertThat(TextLayoutHelper.wordOffset(text, 11, SWT.MOVEMENT_WORD, false, platform)).as(platform).isEqualTo(10);
            assertThat(TextLayoutHelper.wordOffset(text, 3, SWT.MOVEMENT_WORD, false, platform)).as(platform).isZero();
            assertThat(TextLayoutHelper.wordOffset(text, 13, SWT.MOVEMENT_WORD_END, false, platform)).as(platform).isEqualTo(10);
            assertThat(TextLayoutHelper.wordOffset(text, 0, SWT.MOVEMENT_WORD_START, true, platform)).as(platform).isEqualTo(7);
            assertThat(TextLayoutHelper.wordOffset(text, 7, SWT.MOVEMENT_WORD_START, true, platform)).as(platform).isEqualTo(10);
            assertThat(TextLayoutHelper.wordOffset("ab  ", 1, SWT.MOVEMENT_WORD_START, true, platform)).as(platform).isEqualTo(4);
            assertThat(TextLayoutHelper.wordOffset(text, 13, SWT.MOVEMENT_WORD_START, false, platform)).as(platform).isEqualTo(11);
            assertThat(TextLayoutHelper.wordOffset("ab  ", 2, SWT.MOVEMENT_WORD_END, true, platform)).as(platform).isEqualTo(4);
            assertThat(TextLayoutHelper.wordOffset("ab", 2, SWT.MOVEMENT_WORD_END, false, platform)).as(platform).isZero();
            assertThat(TextLayoutHelper.wordOffset("a bc", 3, SWT.MOVEMENT_WORD_START, false, platform)).as(platform).isEqualTo(2);
            assertThat(TextLayoutHelper.wordOffset("ab\ncd", 3, SWT.MOVEMENT_WORD_START, false, platform)).as(platform).isEqualTo(2);
            assertThat(TextLayoutHelper.wordOffset(text, 7, SWT.MOVEMENT_WORD_START, false, platform)).as(platform).isZero();
            assertThat(TextLayoutHelper.wordOffset(text, 2, SWT.MOVEMENT_CLUSTER, true, platform)).as(platform).isEqualTo(2);
        }
        assertThat(TextLayoutHelper.wordOffset("foo, bar_baz qux", 3, SWT.MOVEMENT_WORD_END, true, "cocoa")).isEqualTo(12);
    }

    @Test
    void theTextDirectionIsItsOwnSettingOnlyOnWin32AndTheWeb() {
        for (String platform : new String[] {"cocoa", "gtk"}) {
            assertThat(TextLayoutHelper.textDirection(platform, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT)).as(platform).isEqualTo(SWT.LEFT_TO_RIGHT);
            assertThat(TextLayoutHelper.resolvedTextDirection(platform, SWT.RIGHT_TO_LEFT, SWT.LEFT_TO_RIGHT, "abc")).as(platform)
                    .isEqualTo(SWT.RIGHT_TO_LEFT);
        }
        for (String platform : new String[] {"win32", "web"}) {
            assertThat(TextLayoutHelper.textDirection(platform, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT)).as(platform).isEqualTo(SWT.RIGHT_TO_LEFT);
            assertThat(TextLayoutHelper.textDirection(platform, SWT.RIGHT_TO_LEFT, SWT.NONE)).as(platform).isEqualTo(SWT.RIGHT_TO_LEFT);
            assertThat(TextLayoutHelper.textDirection(platform, SWT.RIGHT_TO_LEFT, SWT.LEFT_TO_RIGHT | SWT.BOLD)).as(platform)
                    .isEqualTo(SWT.LEFT_TO_RIGHT);
            assertThat(TextLayoutHelper.textDirection(platform, SWT.LEFT_TO_RIGHT, SWT.AUTO_TEXT_DIRECTION)).as(platform)
                    .isEqualTo(SWT.AUTO_TEXT_DIRECTION);
            assertThat(TextLayoutHelper.resolvedTextDirection(platform, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, "abc")).as(platform)
                    .isEqualTo(SWT.RIGHT_TO_LEFT);
            assertThat(TextLayoutHelper.resolvedTextDirection(platform, SWT.LEFT_TO_RIGHT, SWT.AUTO_TEXT_DIRECTION, "\u05e9\u05e0 abc"))
                    .as(platform).isEqualTo(SWT.RIGHT_TO_LEFT);
            assertThat(TextLayoutHelper.resolvedTextDirection(platform, SWT.RIGHT_TO_LEFT, SWT.AUTO_TEXT_DIRECTION, "abc"))
                    .as(platform).isEqualTo(SWT.LEFT_TO_RIGHT);
        }
    }

    @Test
    void tabStops() {
        int[] single = {40}, pair = {30, 50};
        for (String platform : PLATFORMS) {
            assertThat(TextLayoutHelper.nextTabStop(10, single, 0, platform, () -> 5)).as(platform).isEqualTo(40);
            assertThat(TextLayoutHelper.nextTabStop(40, single, 0, platform, () -> 5)).as(platform).isEqualTo(80);
            assertThat(TextLayoutHelper.nextTabStop(39.995, single, 0, platform, () -> 5)).as(platform).isEqualTo(80);
            assertThat(TextLayoutHelper.nextTabStop(35, pair, 0, platform, () -> 5)).as(platform).isEqualTo(50);
            assertThat(TextLayoutHelper.nextTabStop(75, pair, 0, platform, () -> 5)).as(platform).isEqualTo(90);
        }
        assertThat(TextLayoutHelper.nextTabStop(10, null, 0, "cocoa", () -> 5)).as("Cocoa has no stops of its own").isEqualTo(10);
        assertThat(TextLayoutHelper.nextTabStop(10, new int[0], 0, "cocoa", () -> 5)).isEqualTo(10);
        for (String platform : new String[] {"gtk", "win32", "web"}) {
            assertThat(TextLayoutHelper.nextTabStop(10, null, 0, platform, () -> 5)).as(platform).isEqualTo(40);
            assertThat(TextLayoutHelper.nextTabStop(10, new int[0], 25, platform, () -> 5)).as(platform).isEqualTo(25);
            assertThat(TextLayoutHelper.nextTabStop(10, null, 0, platform, () -> 0)).as(platform).isEqualTo(10);
        }
    }

    @Test
    void whatEachPlatformHonours() {
        assertThat(TextLayoutHelper.hasTextDirection("cocoa")).isFalse();
        assertThat(TextLayoutHelper.hasTextDirection("gtk")).isFalse();
        assertThat(TextLayoutHelper.hasTextDirection("win32")).isTrue();
        assertThat(TextLayoutHelper.hasTextDirection("web")).isTrue();
        assertThat(TextLayoutHelper.drawsSelectionForeground("cocoa")).isFalse();
        assertThat(TextLayoutHelper.drawsSelectionForeground("gtk")).isTrue();
        assertThat(TextLayoutHelper.drawsSelectionForeground("win32")).isTrue();
    }
}
