package org.eclipse.swt.graphics;

import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledTextHelper;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What {@code TextLayout.draw} sends to the GC an owner-drawn cell paints on (JFace's StyledCellLabelProvider):
 * every run where the run before it ends, measured with the font it is drawn in, on the line's baseline, and the
 * selection behind them. A run drawn short of where the previous one ends covers its last glyphs.
 */
@Tag("native-unit")
class TextLayoutDrawRunsNativeTest {

    /** One drawString: the text, where, and with which font and foreground. */
    private record Run(String text, int x, int y, Font font, Color foreground) {
    }

    private record Fill(int x, int y, int width, int height, Color background) {
    }

    private final Display display = DartMocks.dartDisplay();
    private final List<Run> runs = new ArrayList<>();
    private final List<Fill> fills = new ArrayList<>();
    private final List<int[]> lines = new ArrayList<>();
    private final List<TextLayout> layouts = new ArrayList<>();
    private final Color black = new Color(display, 0, 0, 0);
    private final Color white = new Color(display, 255, 255, 255);

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

    /** A GC that records what is drawn on it. */
    private GC gc() {
        GC gc = mock(GC.class);
        Font[] font = {font(12, SWT.NORMAL)};
        Color[] foreground = {black}, background = {white};
        when(gc.getFont()).thenAnswer(i -> font[0]);
        when(gc.getForeground()).thenAnswer(i -> foreground[0]);
        when(gc.getBackground()).thenAnswer(i -> background[0]);
        when(gc.getLineStyle()).thenReturn(SWT.LINE_SOLID);
        doAnswer(i -> font[0] = i.getArgument(0)).when(gc).setFont(any());
        doAnswer(i -> foreground[0] = i.getArgument(0)).when(gc).setForeground(any());
        doAnswer(i -> background[0] = i.getArgument(0)).when(gc).setBackground(any());
        doAnswer(i -> runs.add(new Run(i.getArgument(0), i.getArgument(1), i.getArgument(2), font[0], foreground[0])))
                .when(gc).drawString(anyString(), anyInt(), anyInt(), anyBoolean());
        doAnswer(i -> fills.add(new Fill(i.getArgument(0), i.getArgument(1), i.getArgument(2), i.getArgument(3), background[0])))
                .when(gc).fillRectangle(anyInt(), anyInt(), anyInt(), anyInt());
        doAnswer(i -> lines.add(new int[] {i.getArgument(0), i.getArgument(1), i.getArgument(2), i.getArgument(3)}))
                .when(gc).drawLine(anyInt(), anyInt(), anyInt(), anyInt());
        return gc;
    }

    private static double measured(String text, Font font) {
        return FontMetricsUtil.getFontSize(text, font).x() * FontMetricsUtil.dpiScale();
    }

    private Run run(String text) {
        return runs.stream().filter(r -> r.text.equals(text)).findFirst()
                .orElseThrow(() -> new AssertionError(text + " not drawn: " + texts()));
    }

    private List<String> texts() {
        return runs.stream().map(Run::text).collect(Collectors.toList());
    }

    @Test
    void eachRunStartsWhereTheRunBeforeItEndsInItsOwnFont() {
        TextLayout layout = layout("plainBOLDitalicBIGsmall");
        Font bold = font(12, SWT.BOLD), italic = font(12, SWT.ITALIC), big = font(24, SWT.NORMAL), small = font(8, SWT.NORMAL);
        layout.setStyle(new TextStyle(bold, null, null), 5, 8);
        layout.setStyle(new TextStyle(italic, null, null), 9, 14);
        layout.setStyle(new TextStyle(big, null, null), 15, 17);
        layout.setStyle(new TextStyle(small, null, null), 18, 22);
        layout.draw(gc(), 10, 5);
        assertThat(texts()).containsExactly("plain", "BOLD", "italic", "BIG", "small");
        Font[] fonts = {layout.getFont(), bold, italic, big, small};
        for (int i = 0; i < runs.size(); i++) {
            assertThat(runs.get(i).font).as("font of %s", runs.get(i).text).isSameAs(fonts[i]);
        }
        assertThat(runs.get(0).x).isEqualTo(10);
        for (int i = 1; i < runs.size(); i++) {
            Run before = runs.get(i - 1);
            assertThat((double) runs.get(i).x).as("start of %s", runs.get(i).text)
                    .isCloseTo(before.x + measured(before.text, before.font), within(1.0));
        }
    }

    @Test
    void runsOfMixedSizesShareTheLinesBaseline() {
        TextLayout layout = layout("small BIG small");
        Font big = font(30, SWT.NORMAL);
        layout.setStyle(new TextStyle(big, null, null), 6, 8);
        layout.draw(gc(), 0, 0);
        Run small = run("small "), large = run("BIG");
        int smallAscent = StyledTextHelper.calculateFontMetrics(layout.getFont(), 1).ascent;
        int bigAscent = StyledTextHelper.calculateFontMetrics(big, 1).ascent;
        assertThat(small.y).isGreaterThan(large.y);
        assertThat(small.y + smallAscent).isEqualTo(large.y + bigAscent).isEqualTo(layout.getLineMetrics(0).getAscent());
    }

    @Test
    void aRaisedRunIsDrawnHigherAndALoweredOneLower() {
        TextLayout layout = layout("aXbYc");
        TextStyle up = new TextStyle(), down = new TextStyle();
        up.rise = 6;
        down.rise = -4;
        layout.setStyle(up, 1, 1);
        layout.setStyle(down, 3, 3);
        layout.draw(gc(), 0, 0);
        assertThat(run("X").y).isEqualTo(run("a").y - 6);
        assertThat(run("Y").y).isEqualTo(run("a").y + 4);
        assertThat(run("c").y).isEqualTo(run("a").y);
    }

    @Test
    void aGlyphMetricsRunKeepsItsSpaceForTheApplication() {
        TextLayout layout = layout("aXXb");
        TextStyle placeholder = new TextStyle();
        placeholder.metrics = new GlyphMetrics(10, 3, 25);
        layout.setStyle(placeholder, 1, 2);
        layout.draw(gc(), 0, 0);
        assertThat(texts()).containsExactly("a", "b");
        assertThat((double) run("b").x).isCloseTo(measured("a", layout.getFont()) + 50, within(1.0));
    }

    @Test
    void theRunAfterATabStartsAtTheStop() {
        TextLayout layout = layout("ab\tBOLD");
        layout.setTabs(new int[] {60});
        layout.setStyle(new TextStyle(font(12, SWT.BOLD), null, null), 3, 6);
        layout.draw(gc(), 10, 0);
        assertThat(texts()).containsExactly("ab", "BOLD");
        assertThat(run("BOLD").x).isEqualTo(70);
    }

    @Test
    void theIndentAndTheVerticalIndentMoveWhatIsDrawn() {
        TextLayout layout = layout("abc\ndef");
        layout.setIndent(15);
        layout.setVerticalIndent(7);
        layout.setSpacing(3);
        layout.draw(gc(), 10, 5);
        assertThat(run("abc").x).isEqualTo(25);
        assertThat(run("abc").y).isEqualTo(12);
        assertThat(run("def").x).isEqualTo(25);
        assertThat(run("def").y).isEqualTo(12 + layout.getLineBounds(1).y);
    }

    @Test
    void aJustifiedLineDrawsItsWordsApart() {
        TextLayout layout = layout("aa bb cc ddddddddddddddddddddddddd");
        layout.setWidth(layout.getBounds(0, 7).width + 30);
        layout.setJustify(true);
        layout.draw(gc(), 0, 0);
        assertThat(texts()).startsWith("aa", "bb", "cc");
        assertThat(run("bb").x).isEqualTo(layout.getLocation(3, false).x);
        assertThat(run("cc").x).isEqualTo(layout.getLocation(6, false).x);
        assertThat((double) run("cc").x).as("two spaces sharing 30 pixels").isGreaterThan(measured("aa bb ", layout.getFont()) + 10);
    }

    @Test
    void aSelectionFillsItsRangeBehindTheText() {
        TextLayout layout = layout("Hello world");
        Color selection = new Color(display, 0, 0, 200);
        layout.draw(gc(), 10, 5, 2, 6, null, selection);
        Rectangle range = layout.getBounds(2, 6);
        assertThat(fills).containsExactly(new Fill(10 + range.x, 5 + range.y, range.width, range.height, selection));
        assertThat(texts()).isNotEmpty();
    }

    @Test
    void aSelectionOverTwoLinesFillsEachLine() {
        TextLayout layout = layout("abc\ndef");
        layout.draw(gc(), 0, 0, 1, 5, null, null);
        assertThat(fills).hasSize(2);
        assertThat(fills.get(0).y).isZero();
        assertThat(fills.get(1).y).isEqualTo(layout.getLineBounds(1).y);
        assertThat(fills.get(1).x).isZero();
        assertThat(fills.get(0).background).isSameAs(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
    }

    @Test
    void theLastLineSelectionReachesPastTheText() {
        TextLayout layout = layout("abc");
        layout.draw(gc(), 0, 0, -1, -1, null, null, SWT.FULL_SELECTION | SWT.LAST_LINE_SELECTION);
        assertThat(fills).hasSize(1);
        assertThat(fills.get(0).x).isEqualTo(layout.getLineBounds(0).width);
        assertThat(fills.get(0).width).isGreaterThan(10_000);
        fills.clear();
        layout.draw(gc(), 0, 0, -1, -1, null, null, SWT.DELIMITER_SELECTION | SWT.LAST_LINE_SELECTION);
        assertThat(fills.get(0).width).isEqualTo(layout.getLineBounds(0).height / 3);
        fills.clear();
        TextLayout empty = layout("");
        empty.draw(gc(), 0, 0, -1, -1, null, null, SWT.FULL_SELECTION | SWT.LAST_LINE_SELECTION);
        assertThat(fills).hasSize(1);
        assertThat(fills.get(0).x).isZero();
    }

    @Test
    void theSelectionForegroundIsTheSelectedTexts() {
        TextLayout layout = layout("Hello world");
        Color selected = new Color(display, 200, 0, 0);
        layout.draw(gc(), 0, 0, 2, 6, selected, null);
        assertThat(texts()).containsExactly("He", "llo w", "orld");
        assertThat(run("llo w").foreground).isSameAs(selected);
        assertThat(run("He").foreground).isSameAs(black);
        assertThat(run("orld").x).isEqualTo(layout.getLocation(7, false).x);
    }

    @Test
    void decorationsSpanTheirRun() {
        TextLayout layout = layout("plain under strike");
        TextStyle under = new TextStyle(), strike = new TextStyle();
        under.underline = true;
        strike.strikeout = true;
        layout.setStyle(under, 6, 10);
        layout.setStyle(strike, 12, 17);
        layout.draw(gc(), 0, 0);
        Run underRun = run("under");
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)[0]).isEqualTo(underRun.x);
        assertThat((double) lines.get(0)[2]).isCloseTo(underRun.x + measured("under", layout.getFont()), within(1.0));
        assertThat(lines.get(1)[0]).isEqualTo(run("strike").x);
    }

    @Test
    void aDisposedGcIsRefused() {
        TextLayout layout = layout("abc");
        GC gc = gc();
        when(gc.isDisposed()).thenReturn(true);
        assertThatThrownBy(() -> layout.draw(gc, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(runs).isEmpty();
    }
}
