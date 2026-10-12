package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyledTextHelper;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * How the web TextLayout engine places its lines: each line as high as its tallest glyphs, the spacing below
 * every line, the indent before a paragraph's first line and the wrap indent before the others, the alignment
 * and the justification inside the wrap width. What every platform's SWT agrees on; the platform-specific
 * choices are in {@link TextLayoutHelperTest}.
 */
@Tag("native-unit")
class TextLayoutLinesNativeTest {

    private final Display display = DartMocks.dartDisplay();
    private final List<TextLayout> layouts = new ArrayList<>();

    @AfterEach
    void dispose() {
        layouts.forEach(TextLayout::dispose);
    }

    private Font font(int size, int style) {
        return new DartFont(display, "System", size, style, null).getApi();
    }

    private TextLayout layout(String text) {
        TextLayout layout = new TextLayout(display);
        layouts.add(layout);
        layout.setFont(font(12, SWT.NORMAL));
        layout.setText(text);
        return layout;
    }

    private int width(String text) {
        return layout(text).getBounds().width;
    }

    @Test
    void theSpacingGoesBelowEveryLine() {
        TextLayout plain = layout("abc\ndef\nghi");
        TextLayout spaced = layout("abc\ndef\nghi");
        spaced.setSpacing(10);
        assertThat(spaced.getLineBounds(0)).isEqualTo(plain.getLineBounds(0));
        assertThat(spaced.getLineBounds(1).y).isEqualTo(plain.getLineBounds(1).y + 10);
        assertThat(spaced.getLineBounds(2).y).isEqualTo(plain.getLineBounds(2).y + 20);
        assertThat(spaced.getLineBounds(2).height).isEqualTo(plain.getLineBounds(2).height);
        assertThat(spaced.getBounds().height).isEqualTo(plain.getBounds().height + 30);
        assertThat(spaced.getLocation(8, false).y).isEqualTo(plain.getLocation(8, false).y + 20);
        assertThat(spaced.getOffset(1, spaced.getLineBounds(2).y + 1, null)).isEqualTo(8);
        assertThat(spaced.getBounds(4, 9).y).isEqualTo(spaced.getLineBounds(1).y);
    }

    @Test
    void aBiggerRunRaisesItsLineAndPushesTheNextDown() {
        TextLayout plain = layout("small BIG\nnext");
        TextLayout mixed = layout("small BIG\nnext");
        Font big = font(30, SWT.NORMAL);
        mixed.setStyle(new TextStyle(big, null, null), 6, 8);
        TextLayout bigOnly = layout("BIG");
        bigOnly.setFont(big);
        assertThat(mixed.getLineBounds(0).height).isEqualTo(bigOnly.getLineBounds(0).height).isGreaterThan(plain.getLineBounds(0).height);
        assertThat(mixed.getLineMetrics(0).getAscent()).isEqualTo(bigOnly.getLineMetrics(0).getAscent());
        assertThat(mixed.getLineBounds(1).y).isEqualTo(mixed.getLineBounds(0).height);
        assertThat(mixed.getLineBounds(1).height).isEqualTo(plain.getLineBounds(1).height);
        assertThat(mixed.getLocation(10, false).y).isEqualTo(mixed.getLineBounds(1).y);
        assertThat(mixed.getBounds().height).isEqualTo(bigOnly.getBounds().height + plain.getLineBounds(1).height);
    }

    @Test
    void aLineIsAsHighAsStyledTextEstimatesItForAFamilyWithoutMetrics() {
        for (int size = 6; size <= 48; size++) {
            Font font = new DartFont(display, "NoSuchFamily", size, SWT.NORMAL, null).getApi();
            Font big = new DartFont(display, "NoSuchFamily", size * 2, SWT.NORMAL, null).getApi();
            Font little = new DartFont(display, "NoSuchFamily", Math.max(1, size / 2), SWT.NORMAL, null).getApi();
            TextLayout single = layout("a");
            single.setFont(font);
            assertThat(single.getBounds().height).as("one run at %d", size).isEqualTo(StyledTextHelper.computeLineHeight(font));
            TextLayout bigOnly = layout("a");
            bigOnly.setFont(big);
            TextLayout mixed = layout("abc");
            mixed.setFont(font);
            mixed.setStyle(new TextStyle(big, null, null), 0, 0);
            mixed.setStyle(new TextStyle(little, null, null), 1, 1);
            assertThat(mixed.getLineBounds(0).height).as("a line with a big and a little run at %d", size)
                    .isEqualTo(bigOnly.getBounds().height).isEqualTo(StyledTextHelper.computeLineHeight(big));
        }
    }

    @Test
    void aGlyphMetricsRunHoldsItsAscentAndDescent() {
        TextLayout layout = layout("abc");
        TextStyle style = new TextStyle();
        style.metrics = new GlyphMetrics(40, 15, 10);
        layout.setStyle(style, 1, 1);
        assertThat(layout.getLineBounds(0).height).isEqualTo(55);
        assertThat(layout.getLineMetrics(0).getAscent()).isEqualTo(40);
        assertThat(layout.getLineMetrics(0).getDescent()).isEqualTo(15);
        assertThat(layout.getLocation(2, false).x - layout.getLocation(1, false).x).isEqualTo(10);
    }

    @Test
    void aRaisedRunRaisesTheLineAndALoweredOneDeepensIt() {
        TextLayout plain = layout("abc");
        TextLayout raised = layout("abc");
        TextStyle up = new TextStyle();
        up.rise = 8;
        raised.setStyle(up, 1, 1);
        TextLayout lowered = layout("abc");
        TextStyle down = new TextStyle();
        down.rise = -6;
        lowered.setStyle(down, 1, 1);
        assertThat(raised.getLineBounds(0).height).isEqualTo(plain.getLineBounds(0).height + 8);
        assertThat(raised.getLineMetrics(0).getAscent()).isEqualTo(plain.getLineMetrics(0).getAscent() + 8);
        assertThat(lowered.getLineBounds(0).height).isEqualTo(plain.getLineBounds(0).height + 6);
        assertThat(lowered.getLineMetrics(0).getAscent()).isEqualTo(plain.getLineMetrics(0).getAscent());
    }

    @Test
    void anEmptyLineTakesTheGlyphsOfItsDelimiter() {
        TextLayout layout = layout("a\n\nb");
        layout.setStyle(new TextStyle(font(30, SWT.NORMAL), null, null), 2, 2);
        assertThat(layout.getLineBounds(1).height).isGreaterThan(layout.getLineBounds(0).height);
        assertThat(layout.getLineBounds(1).width).isZero();
    }

    @Test
    void theIndentShiftsTheFirstLineOfEveryParagraph() {
        TextLayout plain = layout("MOW\nWOM");
        TextLayout indented = layout("MOW\nWOM");
        indented.setIndent(20);
        assertThat(indented.getLocation(0, false).x).isEqualTo(20);
        assertThat(indented.getLocation(4, false).x).isEqualTo(20);
        assertThat(indented.getLineBounds(1).x).isEqualTo(20);
        assertThat(indented.getLineBounds(1).width).isEqualTo(plain.getLineBounds(1).width);
        assertThat(indented.getBounds()).as("the bounds leave the indent out").isEqualTo(plain.getBounds());
        assertThat(indented.getOffset(21, 1, null)).isZero();
        assertThat(indented.getBounds(0, 2).x).isEqualTo(20);
    }

    @Test
    void theWrapIndentShiftsTheWrappedLines() {
        TextLayout layout = layout("alpha beta gamma");
        layout.setWidth(width("alpha beta") + 30);
        layout.setWrapIndent(25);
        assertThat(layout.getLineOffsets()).containsExactly(0, 11, 16);
        assertThat(layout.getLocation(0, false).x).isZero();
        assertThat(layout.getLocation(11, false).x).isEqualTo(25);
        assertThat(layout.getLineBounds(1).x).isEqualTo(25);
        assertThat(layout.getOffset(26, layout.getLineBounds(1).y + 1, null)).isEqualTo(11);
    }

    @Test
    void theIndentNarrowsTheWidthAWrappedLineHas() {
        TextLayout layout = layout("alpha beta gamma");
        layout.setWidth(width("alpha beta") + 1);
        assertThat(layout.getLineOffsets()).containsExactly(0, 11, 16);
        layout.setIndent(20);
        assertThat(layout.getLineOffsets()[1]).isEqualTo(6);
    }

    @Test
    void theWhitespaceEndingAWrappedLineHangsPastTheWidth() {
        TextLayout layout = layout("alpha beta gamma");
        int wrap = width("alpha beta") + 1;
        layout.setWidth(wrap);
        assertThat(layout.getLineOffsets()).containsExactly(0, 11, 16);
        assertThat(layout.getLineBounds(0).width).isEqualTo(width("alpha beta"));
        layout.setAlignment(SWT.RIGHT);
        assertThat(layout.getLineBounds(0).x + layout.getLineBounds(0).width).isEqualTo(wrap);
    }

    @Test
    void alignmentPlacesALineInTheWrapWidth() {
        TextLayout layout = layout("MOW");
        int width = layout.getBounds().width;
        layout.setWidth(width + 100);
        layout.setIndent(20);
        layout.setAlignment(SWT.CENTER);
        assertThat(layout.getLineBounds(0).x).isEqualTo(60);
        assertThat(layout.getBounds(0, 2).x).isEqualTo(60);
        assertThat(layout.getOffset(61, 1, null)).isZero();
        layout.setAlignment(SWT.RIGHT);
        assertThat(layout.getLineBounds(0).x).isEqualTo(100);
        assertThat(layout.getLocation(3, false).x).isEqualTo(100 + width);
    }

    @Test
    void justifyStretchesEveryLineButAParagraphsLast() {
        String text = "aa bb cc dd ee ff gg hh ii";
        TextLayout plain = layout(text);
        TextLayout justified = layout(text);
        // Half-way to the next word: it stays on the second line and the first keeps room to stretch.
        int wrap = (width("aa bb cc dd") + width("aa bb cc dd ee")) / 2;
        plain.setWidth(wrap);
        justified.setWidth(wrap);
        justified.setJustify(true);
        justified.setAlignment(SWT.RIGHT);
        assertThat(justified.getLineOffsets()).containsExactly(plain.getLineOffsets());
        int lastOfFirstLine = plain.getLineOffsets()[1] - 2;
        assertThat(justified.getLocation(lastOfFirstLine, true).x).isCloseTo(wrap, within(1))
                .isGreaterThan(plain.getLocation(lastOfFirstLine, true).x);
        assertThat(justified.getLineBounds(0).width).isCloseTo(wrap, within(1));
        int last = plain.getLineCount() - 1;
        assertThat(justified.getLineBounds(last)).as("a paragraph's last line is left as it is").isEqualTo(plain.getLineBounds(last));
        int space = text.indexOf(' ');
        int hit = justified.getLocation(space + 1, false).x;
        assertThat(justified.getOffset(hit + 1, 1, null)).isEqualTo(space + 1);
    }

    @Test
    void aRangeOverTwoLinesCoversBoth() {
        TextLayout layout = layout("abc\ndefgh");
        Rectangle both = layout.getBounds(1, 6);
        Rectangle second = layout.getLineBounds(1);
        assertThat(both.y).isZero();
        assertThat(both.x).isZero();
        assertThat(both.y + both.height).isEqualTo(second.y + second.height);
        assertThat(both.width).isGreaterThanOrEqualTo(layout.getLineBounds(0).width);
        Rectangle delimiter = layout.getBounds(3, 3);
        assertThat(delimiter.x).isEqualTo(layout.getLineBounds(0).width);
        assertThat(delimiter.width).isZero();
    }

    @Test
    void ascentAndDescentRaiseALineThatIsLower() {
        TextLayout layout = layout("abc");
        int natural = layout.getLineBounds(0).height;
        layout.setAscent(40);
        layout.setDescent(20);
        assertThat(layout.getLineBounds(0).height).isEqualTo(60);
        assertThat(layout.getLineMetrics(0).getAscent()).isEqualTo(40);
        layout.setAscent(2);
        layout.setDescent(2);
        assertThat(layout.getLineBounds(0).height).isEqualTo(natural);
    }

    @Test
    void segmentsDoNotMoveTheLines() {
        TextLayout plain = layout("abcd\nefgh");
        TextLayout segmented = layout("abcd\nefgh");
        segmented.setSegments(new int[] {0, 2, 6});
        for (int offset = 0; offset <= 9; offset++) {
            assertThat(segmented.getLineIndex(offset)).as("line of %d", offset).isEqualTo(plain.getLineIndex(offset));
        }
        assertThat(segmented.getLineOffsets()).containsExactly(0, 5, 9);
    }

    @Test
    void aSurrogatePairIsHitAsOneCharacter() {
        TextLayout layout = layout("a\uD83D\uDE00b");
        int left = layout.getLocation(1, false).x, right = layout.getLocation(3, false).x;
        assertThat(layout.getLocation(1, true).x).isEqualTo(right);
        int[] trailing = new int[1];
        assertThat(layout.getOffset(right - 1, 2, trailing)).isEqualTo(1);
        assertThat(trailing[0]).isEqualTo(2);
        assertThat(layout.getOffset(left + 1, 2, trailing)).isEqualTo(1);
        assertThat(trailing[0]).isZero();
        TextLayout end = layout("ab\uD83D\uDE00");
        assertThat(end.getOffset(end.getBounds().width + 20, 2, trailing)).isEqualTo(2);
        assertThat(trailing[0]).isEqualTo(2);
    }

    @Test
    void theLineOfAnOffsetFollowsTheLineStarts() {
        TextLayout layout = layout("one two three four five six seven");
        layout.setWidth(width("one two three") + 1);
        int[] offsets = layout.getLineOffsets();
        for (int line = 0; line + 1 < offsets.length; line++) {
            for (int offset = offsets[line]; offset < offsets[line + 1]; offset++) {
                assertThat(layout.getLineIndex(offset)).as("line of %d", offset).isEqualTo(line);
            }
        }
        assertThat(layout.getLineIndex(layout.getText().length())).isEqualTo(layout.getLineCount() - 1);
    }

    @Test
    void aDisposedLayoutRefusesItsLineBounds() {
        TextLayout layout = layout("abc");
        layout.dispose();
        assertThatThrownBy(() -> layout.getLineBounds(0)).isInstanceOfSatisfying(SWTException.class,
                e -> assertThat(e.code).isEqualTo(SWT.ERROR_GRAPHIC_DISPOSED));
    }
}
