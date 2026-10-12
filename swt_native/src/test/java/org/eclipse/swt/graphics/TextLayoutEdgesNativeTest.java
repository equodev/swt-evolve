package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.text.Bidi;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The web TextLayout engine at its edges: right-to-left text and orientation, surrogate pairs at the end of a line,
 * segments, a measurement the render side handed over, resources disposed under it, and the selection arguments
 * a draw ignores. Every platform's SWT agrees on what is asserted here.
 */
@Tag("native-unit")
class TextLayoutEdgesNativeTest {

    private final Display display = DartMocks.dartDisplay();
    private final List<TextLayout> layouts = new ArrayList<>();
    private final List<String> drawn = new ArrayList<>();
    private final List<int[]> fills = new ArrayList<>();

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

    private GC gc() {
        GC gc = mock(GC.class);
        Color black = new Color(display, 0, 0, 0);
        Font font = font(12, SWT.NORMAL);
        when(gc.getFont()).thenReturn(font);
        when(gc.getForeground()).thenReturn(black);
        when(gc.getBackground()).thenReturn(black);
        when(gc.getLineStyle()).thenReturn(SWT.LINE_SOLID);
        doAnswer(i -> drawn.add(i.getArgument(0))).when(gc).drawString(anyString(), anyInt(), anyInt(), anyBoolean());
        doAnswer(i -> fills.add(new int[] {i.getArgument(0), i.getArgument(1), i.getArgument(2), i.getArgument(3)}))
                .when(gc).fillRectangle(anyInt(), anyInt(), anyInt(), anyInt());
        return gc;
    }

    private static TextLayoutMeasurement oneRow(int length, double charWidth, double height) {
        double[] xs = new double[length + 1];
        for (int i = 0; i <= length; i++) xs[i] = i * charWidth;
        return new TextLayoutMeasurement(length, new int[] {0}, new double[] {0}, new double[] {0},
                new double[] {length * charWidth}, new double[] {height}, new double[][] {xs}, 0);
    }

    @Test
    void aMeasurementFromTheRenderSideAnswersWhileItFitsTheText() {
        TextLayout layout = layout("abcd");
        DartTextLayout impl = (DartTextLayout) layout.getImpl();
        impl.setMeasurement(oneRow(4, 10, 15));
        assertThat(layout.getBounds()).isEqualTo(new Rectangle(0, 0, 40, 15));
        assertThat(layout.getLineBounds(0)).isEqualTo(new Rectangle(0, 0, 40, 15));
        assertThat(layout.getLocation(2, false).x).isEqualTo(20);
        assertThat(layout.getOffset(25, 5, null)).isEqualTo(2);
        assertThat(layout.getLineOffsets()).containsExactly(0, 4);
        assertThat(layout.getLineMetrics(0).getHeight()).isPositive();

        TextLayout shorter = layout("abc");
        ((DartTextLayout) shorter.getImpl()).setMeasurement(oneRow(4, 10, 15));
        assertThat(shorter.getLocation(2, false).x).as("a measurement of another text is not used").isNotEqualTo(20);

        TextLayout segmented = layout("abcd");
        segmented.setSegments(new int[] {0, 2});
        ((DartTextLayout) segmented.getImpl()).setMeasurement(oneRow(4, 10, 15));
        assertThat(segmented.getLocation(2, false).x).as("a measurement of the text without its segments is not used").isNotEqualTo(20);
    }

    @Test
    void rightToLeftOrientationHitsAndEnds() {
        TextLayout layout = layout("\u05e9\u05e0\u05e2");
        layout.setOrientation(SWT.RIGHT_TO_LEFT);
        int width = layout.getBounds().width;
        int[] trailing = new int[1];
        assertThat(layout.getOffset(width + 20, 1, trailing)).as("past the right edge").isZero();
        assertThat(layout.getOffset(-20, 1, trailing)).as("left of the text").isEqualTo(2);
        assertThat(trailing[0]).isEqualTo(1);
        TextLayout twoLines = layout("\u05e9\u05e0\n\u05e2");
        twoLines.setOrientation(SWT.RIGHT_TO_LEFT);
        assertThat(twoLines.getLocation(2, false).x).as("the end of a right-to-left line is its left edge").isZero();
        int firstRight = layout.getLocation(0, false).x;
        assertThat(layout.getOffset(firstRight - 1, 1, trailing)).isZero();
        assertThat(trailing[0]).as("the right half of a right-to-left character leads").isZero();
        assertThat(layout.getOffset(layout.getLocation(0, true).x + 1, 1, trailing)).isZero();
        assertThat(trailing[0]).isEqualTo(1);
        assertThat(layout.getLevel(3)).isEqualTo(1);
        assertThat(layout.getOffset(-20, 1, null)).isEqualTo(2);
    }

    @Test
    void aMixedLineIsMeasuredAndDrawnRunByRun() {
        TextLayout layout = layout("ab \u05e9\u05e0 cd");
        Rectangle hebrew = layout.getBounds(3, 4);
        assertThat(hebrew.width).isPositive();
        assertThat(hebrew.x).isCloseTo(layout.getLocation(4, true).x, within(1));
        layout.draw(gc(), 0, 0);
        assertThat(String.join("", drawn)).isEqualTo("ab \u05e9\u05e0 cd");
        TextLayout pair = layout("\u05e9\uD83D\uDE00\u05e0");
        assertThat(pair.getLocation(1, true).x).isNotEqualTo(pair.getLocation(1, false).x);
        assertThat(pair.getBounds(1, 2).width).isEqualTo(pair.getBounds(1, 1).width);
    }

    @Test
    void anUnpairedSurrogateIsACharacterOfItsOwn() {
        TextLayout end = layout("ab\uD83D");
        int[] trailing = new int[1];
        assertThat(end.getOffset(end.getBounds().width + 20, 1, trailing)).isEqualTo(2);
        assertThat(trailing[0]).isEqualTo(1);
        assertThat(end.getLocation(2, true).x).isEqualTo(end.getLocation(3, false).x);
        assertThat(layout("a").getOffset(30, 1, trailing)).isZero();
        assertThat(trailing[0]).isEqualTo(1);
        TextLayout lone = layout("a\uDE00");
        assertThat(lone.getOffset(lone.getBounds().width + 20, 1, trailing)).isEqualTo(1);
        assertThat(trailing[0]).isEqualTo(1);
        TextLayout unpaired = layout("a\uD83Dx");
        assertThat(unpaired.getLocation(1, true).x).isEqualTo(unpaired.getLocation(2, false).x);
        TextLayout mixed = layout("\u05e9\uD83Dx");
        assertThat(mixed.getOffset(mixed.getLocation(2, false).x + 1, 1, null)).isEqualTo(2);
        assertThat(layout("\n").getOffset(1, 100, null)).as("below a text that is one delimiter").isBetween(0, 1);
        TextLayout low = layout("\uDE00b");
        assertThat(low.getOffset(low.getBounds().width + 20, 1, trailing)).isEqualTo(1);
        assertThat(trailing[0]).isEqualTo(1);
        assertThat(low.getNextOffset(0, SWT.MOVEMENT_CLUSTER)).isEqualTo(1);
    }

    @Test
    void justifyLeavesALineWithoutSpacesOrRightToLeftTextAsItIs() {
        TextLayout word = layout("abcdefghijklmnop");
        word.setWidth(word.getBounds(0, 7).width + 1);
        word.setJustify(true);
        assertThat(word.getLineCount()).isGreaterThan(1);
        assertThat(word.getLineBounds(0).width).isLessThan(word.getWidth());
        TextLayout rtl = layout("\u05e9\u05e0 \u05e9\u05e0 \u05e9\u05e0 \u05e9\u05e0");
        rtl.setWidth(rtl.getBounds(0, 4).width + 4);
        rtl.setJustify(true);
        assertThat(rtl.getLineCount()).isGreaterThan(1);
        assertThat(rtl.getLineBounds(0).width).isLessThan(rtl.getWidth());
    }

    @Test
    void onlyADelimiterSegmentInsideTheTextStartsALine() {
        TextLayout layout = layout("abcdef");
        layout.setSegments(new int[] {0, 3, 6});
        layout.setSegmentsChars(new char[] {'\n', '\r', '\n'});
        assertThat(layout.getLineIndex(3)).isEqualTo(layout.getLineIndex(2) + 1);
        assertThat(layout.getLineIndex(1)).isEqualTo(layout.getLineIndex(0));
        assertThat(layout.getLineIndex(5)).isEqualTo(layout.getLineIndex(3));
        layout.setSegmentsChars(new char[] {'a', 'b', 'c'});
        assertThat(layout.getLineCount()).isEqualTo(1);
    }

    @Test
    void aLineThatFillsTheWidthExactlyIsNotStretched() {
        TextLayout layout = layout("ab cd ef");
        TextStyle fixed = new TextStyle();
        fixed.metrics = new GlyphMetrics(10, 3, 10);
        layout.setStyle(fixed, 0, 7);
        layout.setWidth(50);
        layout.setJustify(true);
        assertThat(layout.getLineOffsets()[1]).isEqualTo(6);
        assertThat(layout.getLocation(3, false).x).isEqualTo(30);
        assertThat(layout.getLineBounds(0).width).isEqualTo(50);
    }

    @Test
    void segmentsTranslateOnlyWhenTheyAddCharacters() {
        TextLayout layout = layout("abcd");
        int plain = layout.getLocation(2, false).x;
        layout.setSegments(new int[0]);
        assertThat(layout.getLocation(2, false).x).isEqualTo(plain);
        assertThat(layout.getLineOffsets()).containsExactly(0, 4);
        assertThat(layout.getLevel(2)).isZero();
        layout.setSegments(new int[] {0, 4});
        assertThat(layout.getLineOffsets()).containsExactly(0, 4);
        assertThat(layout.getLevel(2)).isZero();
        layout.setSegmentsChars(new char[] {'\u200f'});
        assertThat(layout.getLineOffsets()).as("chars for some segments only").containsExactly(0, 4);
        assertThat(layout.getLevel(3)).isZero();
        layout.setSegments(new int[] {0, 2});
        assertThat(layout.getLevel(3)).as("the default mark goes before the segment without a char").isZero();
        layout.setSegmentsChars(null);
        layout.setSegments(new int[] {0, 2});
        assertThat(layout.getLineOffsets()).containsExactly(0, 4);
        layout.setSegments(new int[] {0, 6});
        layout.setSegmentsChars(new char[] {'\u200e'});
        assertThat(layout.getLineOffsets()).as("a segment past the text, with no char of its own").containsExactly(0, 4);
        layout.setSegmentsChars(null);
        layout.setSegments(new int[] {1, 3});
        layout.setOrientation(SWT.RIGHT_TO_LEFT);
        assertThat(layout.getLineOffsets()).containsExactly(0, 4);
        assertThat(layout.getLevel(0)).isEqualTo(2);
        assertThat(layout.getNextOffset(1, SWT.MOVEMENT_CLUSTER)).isEqualTo(2);
    }

    @Test
    void aLevelIsTheOneTheUnicodeBidiAlgorithmGives() {
        String[] texts = {
                "abc", "\u05e9\u05e0\u05e2", "abc \u05e9\u05e0 def", "\u0627\u0644\u0639 123", "\u0627\u0644 1,2 \u0639",
                "1+2 \u05e9", "\u05e9 $12 \u05e0", "\u05e9 12% \u05e0", "\u05e9\u0301x", 
                "a\u200bb \u05e9",
                "\u05e9 (abc) \u05e0", "\u0661\u0662 \u05e9", "\u0627 1.5 \u0628", "a 1/2 \u05e9", "\u05e9 a-1 \u05e0",
                "\u0301a \u05e9", "\u0301\u05e9 a", "1+a \u05e9", "a+1 \u05e9", "1,a \u05e9", "\u0661,\u0662 \u05e9", "\u0661,a \u05e9",
                "\u05e9 %% a", "\u05e9 1%% b", "\u05e9 $1", "a+b \u05e9", "\u05e9\t\u05e0 a", "\u05e9 \u05e0 ", "a \u05e9\t", "\u05e9\u202c \t\u05e0", "!a \u05e9", "\u05e9 ! \u05e0", "a ! \u05e9",
                "\u0627 12 \u0628", "\u0627 1,2", "12 \u0661", "\u05e9 12.5% \u05e0", "x \u05e9 1 \u05e0 y",
                "\u05e9 $", "$1 \u05e9", "\u05e9 $ \u05e0", "a\n\u05e9 b", "\u05e9\n\u05e0", "\u05e9\u2029a",
        };
        for (int orientation : new int[] {SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT}) {
            for (String text : texts) {
                TextLayout layout = layout(text);
                layout.setOrientation(orientation);
                Bidi bidi = new Bidi(text, orientation == SWT.RIGHT_TO_LEFT ? Bidi.DIRECTION_RIGHT_TO_LEFT : Bidi.DIRECTION_LEFT_TO_RIGHT);
                for (int i = 0; i < text.length(); i++) {
                    if (Character.getType(text.charAt(i)) == Character.FORMAT) continue;
                    assertThat(layout.getLevel(i)).as("level of %d in \"%s\" (%s)", i, text, orientation == SWT.RIGHT_TO_LEFT ? "RTL" : "LTR")
                            .isEqualTo(bidi.getLevelAt(i));
                }
            }
        }
    }

    @Test
    void explicitEmbeddingsAndOverridesRaiseTheLevel() {
        TextLayout rle = layout("\u202babc\u202c def");
        assertThat(rle.getLevel(1)).as("left to right text in a right-to-left embedding").isEqualTo(2);
        assertThat(rle.getLevel(6)).isZero();
        TextLayout lre = layout("\u202aabc\u202c \u05e9");
        assertThat(lre.getLevel(1)).isEqualTo(2);
        assertThat(lre.getLevel(6)).isEqualTo(1);
        TextLayout rlo = layout("\u202e\u05e9ab\u202c");
        assertThat(rlo.getLevel(2)).as("overridden to right to left").isEqualTo(1);
        rlo.setOrientation(SWT.RIGHT_TO_LEFT);
        assertThat(rlo.getLevel(2)).as("the next odd level above the paragraph's").isEqualTo(3);
    }

    @Test
    void aLeftToRightOverrideRaisesItsTextToAnEvenLevel() {
        TextLayout layout = layout("\u202dab\u05e9\u202c");
        assertThat(layout.getLevel(1)).isEqualTo(2);
        assertThat(layout.getLevel(3)).as("the Hebrew letter is overridden to left to right").isEqualTo(2);
    }

    @Test
    void embeddingsDeeperThanTheLimitStayAtIt() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 70; i++) text.append('\u202a').append('\u202b');
        text.append('x');
        TextLayout layout = layout(text.toString());
        assertThat(layout.getLevel(text.length() - 1)).as("125 explicit levels at most, one more implicit").isEqualTo(126);
        assertThat(layout("").getLevel(0)).isZero();
        TextLayout unbalanced = layout("a\u202cb \u05e9");
        assertThat(unbalanced.getLevel(2)).as("a pop with nothing pushed").isZero();
    }

    @Test
    void disposedFontsFallBack() {
        TextLayout layout = layout("abc def");
        Font styleFont = font(30, SWT.NORMAL);
        layout.setStyle(new TextStyle(styleFont, null, null), 4, 6);
        int tall = layout.getLineBounds(0).height;
        styleFont.dispose();
        layout.setWidth(500);
        assertThat(layout.getLineBounds(0).height).isLessThan(tall);
        layout.draw(gc(), 0, 0);
        assertThat(drawn).containsExactly("abc ", "def");
    }

    @Test
    void selectionArgumentsThatSelectNothing() {
        TextLayout layout = layout("abc");
        layout.draw(gc(), 0, 0, 2, 1, null, null);
        layout.draw(gc(), 0, 0, -1, 1, null, null);
        layout.draw(gc(), 0, 0, 1, -1, null, null);
        layout.draw(gc(), 0, 0, -1, -1, null, null, SWT.LAST_LINE_SELECTION);
        layout.draw(gc(), 0, 0, -1, -1, null, null, SWT.FULL_SELECTION);
        layout.draw(gc(), 0, 0, -5, -1, null, null);
        assertThat(fills).isEmpty();
        assertThat(drawn).hasSize(6);
    }

    @Test
    void aBorderWithoutAColourTakesTheRunsForeground() {
        TextLayout layout = layout("abc");
        TextStyle bordered = new TextStyle();
        bordered.borderStyle = SWT.BORDER_DOT;
        layout.setStyle(bordered, 0, 2);
        GC gc = gc();
        List<int[]> rectangles = new ArrayList<>();
        doAnswer(i -> rectangles.add(new int[] {i.getArgument(0), i.getArgument(1), i.getArgument(2), i.getArgument(3)}))
                .when(gc).drawRectangle(anyInt(), anyInt(), anyInt(), anyInt());
        layout.draw(gc, 0, 0);
        assertThat(rectangles).hasSize(1);
        assertThat(rectangles.get(0)[3]).isEqualTo(layout.getLineBounds(0).height - 1);
    }

    @Test
    void settersGivenAnEqualValueKeepTheLayout() {
        TextLayout layout = layout("a\tb");
        layout.setTabs(new int[] {40});
        int x = layout.getLocation(2, false).x;
        layout.setTabs(new int[] {40});
        layout.setTabs(new int[] {40, 80});
        layout.setTabs(new int[] {40, 80});
        layout.setTabs(new int[] {40, 90});
        assertThat(layout.getLocation(2, false).x).isEqualTo(x);
        layout.setTabs(new int[0]);
        assertThat(layout.getLocation(2, false).x).isGreaterThanOrEqualTo(layout.getLocation(1, false).x);
        Font a = font(12, SWT.NORMAL), b = font(20, SWT.NORMAL);
        layout.setFont(a);
        layout.setFont(b);
        assertThat(layout.getFont()).isSameAs(b);
    }

    @Test
    void withNoFontAndNoDisplayOnTheThreadNothingIsMeasured() throws Exception {
        TextLayout layout = new TextLayout(display);
        layouts.add(layout);
        layout.setText("abc");
        int[] width = new int[1];
        GC gc = gc();
        Thread worker = new Thread(() -> {
            width[0] = layout.getBounds().width;
            layout.draw(gc, 0, 0);
        });
        worker.start();
        worker.join();
        assertThat(width[0]).isZero();
        assertThat(drawn).containsExactly("abc");
    }

    @Test
    void theStateTheGeneratedAccessorsRead() {
        TextLayout layout = layout("abc\ndef");
        Font font = layout.getFont();
        layout.setStyle(new TextStyle(), 0, 1);
        layout.setSpacing(2);
        layout.setAscent(9);
        layout.setDescent(3);
        layout.setIndent(4);
        layout.setWrapIndent(5);
        layout.setJustify(true);
        layout.setAlignment(SWT.CENTER);
        layout.setTabs(new int[] {30});
        layout.setSegments(new int[] {1});
        layout.setSegmentsChars(new char[] {'\u200e'});
        layout.setWidth(200);
        layout.setVerticalIndent(6);
        layout.setOrientation(SWT.RIGHT_TO_LEFT);
        layout.getLineCount();
        DartTextLayout impl = (DartTextLayout) layout.getImpl();
        assertThat(impl._font()).isSameAs(font);
        assertThat(impl._text()).isEqualTo("abc\ndef");
        assertThat(impl._stylesCount()).isEqualTo(3);
        assertThat(impl._spacing()).isEqualTo(2);
        assertThat(impl._ascent()).isEqualTo(9);
        assertThat(impl._descent()).isEqualTo(3);
        assertThat(impl._indent()).isEqualTo(4);
        assertThat(impl._wrapIndent()).isEqualTo(5);
        assertThat(impl._justify()).isTrue();
        assertThat(impl._alignment()).isEqualTo(SWT.CENTER);
        assertThat(impl._tabs()).containsExactly(30);
        assertThat(impl._segments()).containsExactly(1);
        assertThat(impl._segmentsChars()).containsExactly('\u200e');
        assertThat(impl._wrapWidth()).isEqualTo(200);
        assertThat(impl._verticalIndentInPoints()).isEqualTo(6);
        assertThat(impl._orientation()).isEqualTo(SWT.RIGHT_TO_LEFT);
        assertThat(impl._lineOffsets()).hasSize(3);
        assertThat(impl._invalidOffsets()).isNull();
        assertThat(impl.__styles()).isNotNull();
        assertThat(impl.getApi()).isSameAs(layout);
    }

    @Test
    void aStyleItemNamesItsRun() {
        DartTextLayout.StyleItem item = new DartTextLayout.StyleItem();
        item.start = 3;
        assertThat(item.toString()).isEqualTo("StyleItem {3, null}");
    }

    @Test
    void aTabAfterTheIndentIsMeasuredFromTheLeftEdge() {
        TextLayout layout = layout("a\tb");
        layout.setTabs(new int[] {40});
        layout.setIndent(20);
        assertThat((double) layout.getLocation(2, false).x).isCloseTo(40, within(0.5));
    }
}
