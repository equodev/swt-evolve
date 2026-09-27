package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Guards that StyledText's position API describes the text the client shows, so painters drawing
 * at those coordinates (rulers, highlights, annotations) land on the glyphs.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextGeometryFlutterTest {

    /** Documents whose layout the estimate and the renderer have disagreed on. */
    enum Doc {
        PLAIN(SWT.MULTI, "alpha beta gamma\nsecond line of text\nthird\n"),
        TABS(SWT.MULTI, "\tone\n\t\ttwo\tthree\nx\ty\n"),
        PROPORTIONAL(SWT.MULTI, "WWWWiiiimmmm....\nllllMMMM\n"),
        WRAPPED(SWT.MULTI | SWT.WRAP, "The quick brown fox jumps over the lazy dog, then keeps running across "
                + "the whole field until the line wraps inside the editor at least twice.\nnext\n"),
        CRLF(SWT.MULTI, "first line\r\nsecond line\r\nthird line"),
        LARGE_WRAPPED(SWT.MULTI | SWT.WRAP, large());

        final int style;
        final String text;

        Doc(int style, String text) {
            this.style = style;
            this.text = text;
        }

        private static String large() {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 26_000) sb.append("lorem ipsum dolor sit amet consectetur adipiscing elit ");
            return sb.append("\nend").toString();
        }
    }

    private final StyledTextFlutterStage stage = new StyledTextFlutterStage();
    private StyledText text;

    @BeforeAll
    void boot() {
        stage.boot();
    }

    @AfterAll
    void shutdown() {
        stage.shutdown();
    }

    private void fresh(Doc doc) {
        stage.fresh(doc.style | SWT.V_SCROLL, doc.text);
        text = stage.subject;
    }

    /** Offsets spread over the document's first lines, including line ends. */
    private int[] probeOffsets() {
        int limit = Math.min(text.getCharCount(), 160);
        return new int[] { 0, 1, limit / 7, limit / 3, limit / 2, (2 * limit) / 3, limit - 1, limit };
    }

    /** An offset inside a line delimiter, which getPointAtOffset clamps to the end of the line's text. */
    private boolean insideDelimiter(int offset) {
        int line = text.getLineAtOffset(offset);
        return offset > text.getOffsetAtLine(line) + text.getLine(line).length();
    }

    @ParameterizedTest
    @EnumSource(Doc.class)
    @DisplayName("the caret is shown where getLocationAtOffset says it is")
    void caretIsShownAtTheLocationSwtReports(Doc doc) {
        fresh(doc);
        for (int probe : probeOffsets()) {
            stage.clickAtOffset(probe);
            int offset = text.getCaretOffset();
            assertThat(stage.renderedCarets(text)).as("shown caret after clicking %d", probe).containsExactly(offset);
            double[] rect = stage.renderedCaretRect(text);
            Point location = text.getLocationAtOffset(offset);
            assertThat(rect).as("shown caret rect for offset %d", offset).isNotNull();
            assertThat(rect[0]).as("x of offset %d", offset).isCloseTo(location.x, within(1.0));
            assertThat(rect[1]).as("y of offset %d", offset).isCloseTo(location.y, within(1.0));
        }
    }

    @ParameterizedTest
    @EnumSource(Doc.class)
    @DisplayName("getOffsetAtPoint maps each offset's location back to that offset")
    void offsetAtPointRoundTrips(Doc doc) {
        fresh(doc);
        stage.settle();
        for (int offset : probeOffsets()) {
            if (offset == text.getCharCount()) continue;
            int line = text.getLineAtOffset(offset);
            if (offset == text.getOffsetAtLine(line) + text.getLine(line).length()) continue;
            if (insideDelimiter(offset)) continue;
            Point location = text.getLocationAtOffset(offset);
            assertThat(text.getOffsetAtPoint(location)).as("offset at the location of %d", offset).isEqualTo(offset);
        }
    }

    @ParameterizedTest
    @EnumSource(Doc.class)
    @DisplayName("a click at getLocationAtOffset puts the caret on that offset")
    void clickAtTheReportedLocationLandsOnTheOffset(Doc doc) {
        fresh(doc);
        for (int offset : probeOffsets()) {
            if (insideDelimiter(offset)) continue;
            stage.clickAtOffset(offset);
            assertThat(text.getCaretOffset()).as("caret after clicking offset %d", offset).isEqualTo(offset);
        }
    }

    @Test
    @DisplayName("a point to the right of a short line is not over any character")
    void pointBesideAShortLineIsNotOverText() {
        fresh(Doc.PLAIN);
        stage.settle();
        int y = text.getLinePixel(2) + 1;
        assertThat(text.getOffsetAtPoint(new Point(StyledTextFlutterStage.WIDTH - 40, y))).isEqualTo(-1);
    }

    @ParameterizedTest
    @EnumSource(value = Doc.class, names = { "PLAIN", "TABS", "PROPORTIONAL", "CRLF" })
    @DisplayName("line pixels step by the line height, and the shown caret is that tall")
    void lineHeightMatchesTheShownRows(Doc doc) {
        fresh(doc);
        stage.focusAt(0);
        for (int line = 0; line + 1 < text.getLineCount(); line++) {
            int offset = text.getOffsetAtLine(line);
            assertThat(text.getLinePixel(line + 1) - text.getLinePixel(line))
                    .as("height of line %d", line).isEqualTo(text.getLineHeight(offset));
        }
        double[] caret = stage.renderedCaretRect(text);
        assertThat(caret).isNotNull();
        assertThat(caret[3]).as("shown caret height").isCloseTo(text.getLineHeight(0), within(1.0));
    }

    @Test
    @DisplayName("a taller font on part of a line makes the line, and its caret, taller")
    void aTallerRangeGrowsTheLine() {
        fresh(Doc.PLAIN);
        FontData data = text.getFont().getFontData()[0];
        Font big = new Font(text.getDisplay(), data.getName(), data.getHeight() * 2, SWT.NORMAL);
        try {
            StyleRange range = new StyleRange();
            range.start = 6;
            range.length = 4;
            range.font = big;
            text.setStyleRange(range);
            stage.focusAt(8);

            int height = text.getLinePixel(1) - text.getLinePixel(0);
            assertThat(height).as("line 0 height").isEqualTo(text.getLineHeight(8));
            assertThat(height).as("line 0 is taller than line 1").isGreaterThan(text.getLinePixel(2) - text.getLinePixel(1));
            double[] caret = stage.renderedCaretRect(text);
            assertThat(caret[3]).as("shown caret height").isCloseTo(text.getLineHeight(8), within(1.0));
        } finally {
            big.dispose();
        }
    }

    @Test
    @DisplayName("after a font change the shown caret is still where getLocationAtOffset says")
    void geometryFollowsAFontChange() {
        fresh(Doc.PLAIN);
        stage.focusAt(0);
        FontData data = text.getFont().getFontData()[0];
        Font big = new Font(text.getDisplay(), data.getName(), data.getHeight() + 6, SWT.NORMAL);
        try {
            text.setFont(big);
            stage.pushState();
            stage.clickAtOffset(12);
            double[] rect = stage.renderedCaretRect(text);
            Point location = text.getLocationAtOffset(text.getCaretOffset());
            assertThat(rect[0]).isCloseTo(location.x, within(1.0));
            assertThat(rect[1]).isCloseTo(location.y, within(1.0));
        } finally {
            text.setFont(null);
            big.dispose();
        }
    }

    @Test
    @DisplayName("after a margin change the shown caret is still where getLocationAtOffset says")
    void geometryFollowsAMarginChange() {
        fresh(Doc.PLAIN);
        stage.focusAt(0);
        text.setMargins(24, 12, 8, 4);
        stage.pushState();
        stage.clickAtOffset(20);
        double[] rect = stage.renderedCaretRect(text);
        Point location = text.getLocationAtOffset(text.getCaretOffset());
        assertThat(rect[0]).isCloseTo(location.x, within(1.0));
        assertThat(rect[1]).isCloseTo(location.y, within(1.0));
    }

    @Test
    @DisplayName("the bounds of a range ending a wrapped row start at the range")
    void textBoundsOfARowEnd() {
        fresh(Doc.WRAPPED);
        stage.settle();
        int rowY = text.getLocationAtOffset(0).y;
        int rowEnd = 0;
        while (rowEnd + 1 < text.getLine(0).length() && text.getLocationAtOffset(rowEnd + 1).y == rowY) rowEnd++;
        Rectangle bounds = text.getTextBounds(rowEnd - 3, rowEnd - 1);
        assertThat(bounds.x).isEqualTo(text.getLocationAtOffset(rowEnd - 3).x);
        assertThat(bounds.y).isEqualTo(rowY);
    }

    @Test
    @DisplayName("the top index is the first line the client shows, whatever the line heights")
    void topIndexIsTheFirstShownLine() {
        StringBuilder doc = new StringBuilder();
        for (int i = 0; i < 80; i++) doc.append("line ").append(i).append('\n');
        stage.fresh(SWT.MULTI | SWT.V_SCROLL, doc.toString());
        text = stage.subject;
        FontData data = text.getFont().getFontData()[0];
        Font big = new Font(text.getDisplay(), data.getName(), data.getHeight() * 2, SWT.NORMAL);
        try {
            for (int i = 0; i < 80; i += 3) {
                StyleRange r = new StyleRange();
                r.start = text.getOffsetAtLine(i);
                r.length = 4;
                r.font = big;
                text.setStyleRange(r);
            }
            text.setTopPixel(301);
            stage.pushState();

            assertThat(text.getTopIndex()).isEqualTo(text.getLineIndex(0));
            assertThat(stage.renderedScroll(text)[1]).isEqualTo(text.getTopPixel());
        } finally {
            big.dispose();
        }
    }

    @ParameterizedTest
    @EnumSource(value = Doc.class, names = { "PLAIN", "WRAPPED", "LARGE_WRAPPED" })
    @DisplayName("the vertical scroll range is the height of the content")
    void scrollRangeIsTheContentHeight(Doc doc) {
        fresh(doc);
        stage.settle();
        assertThat(text.getVerticalBar().getMaximum()).isCloseTo(expectedVerticalMaximum(), within(1));
    }

    /** StyledText.setScrollBar: the content height less the margins, or 1 when everything fits. */
    private int expectedVerticalMaximum() {
        int contentHeight = text.getLinePixel(text.getLineCount()) + text.getTopPixel() - text.getTopMargin();
        int margins = text.getTopMargin() + text.getBottomMargin();
        return text.getClientArea().height < contentHeight ? contentHeight - margins : 1;
    }

    @Test
    @DisplayName("turning word wrap on recomputes the scroll range")
    void wordWrapRecomputesTheScrollRange() {
        fresh(Doc.LARGE_WRAPPED);
        text.setWordWrap(false);
        stage.pushState();
        text.setWordWrap(true);
        stage.pushState();

        assertThat(text.getVerticalBar().getMaximum()).isCloseTo(expectedVerticalMaximum(), within(1));
    }
}
