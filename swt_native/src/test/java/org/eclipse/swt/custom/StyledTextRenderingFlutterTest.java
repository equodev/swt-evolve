package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Caret;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Guards that what StyledText's appearance API asks for (style ranges, line attributes, listener
 * styles, selection and caret) is what the client paints.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextRenderingFlutterTest {

    static final String LONG_LINE = "The quick brown fox jumps over the lazy dog, then keeps running across "
            + "the whole field until the line wraps inside the editor at least twice more.";

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

    private void fresh(int style, String content) {
        stage.fresh(style, content);
        text = stage.subject;
    }

    private Color color(int r, int g, int b) {
        return new Color(text.getDisplay(), r, g, b);
    }

    private static String hex(RGB rgb) {
        return String.format("#ff%02x%02x%02x", rgb.red, rgb.green, rgb.blue);
    }

    /** The run painted at {@code offset}, once the client has the change. */
    private Map<String, Object> runAt(int offset) {
        show();
        return stage.renderedRunAt(text, offset);
    }

    /** Waits for the client to show what the test just changed. */
    private void show() {
        stage.pushState();
    }

    /** The first offset on the second visual row of logical line 0. */
    private int secondRowStart() {
        int firstY = text.getLocationAtOffset(0).y;
        int offset = 1;
        while (offset < text.getLine(0).length() && text.getLocationAtOffset(offset).y == firstY) offset++;
        return offset;
    }

    @Test
    @DisplayName("a StyledText shows its text and styles as soon as it is created")
    void textIsShownOnCreation() {
        StyledText created = new StyledText(stage.shell, SWT.MULTI);
        try {
            created.setBounds(0, StyledTextFlutterStage.HEIGHT + 50, 200, 20);
            created.setText("first sight");
            created.setStyleRange(new StyleRange(0, 5, created.getDisplay().getSystemColor(SWT.COLOR_RED), null));
            stage.settle();

            assertThat(stage.renderedLines(created)).containsExactly("first sight");
            assertThat(stage.renderedRunAt(created, 1).get("foreground"))
                    .isEqualTo(hex(created.getDisplay().getSystemColor(SWT.COLOR_RED).getRGB()));
        } finally {
            created.dispose();
        }
    }

    // ---------------- style ranges ----------------

    @Test
    @DisplayName("a range's foreground and background are painted")
    void rangeColorsArePainted() {
        fresh(SWT.MULTI, "alpha beta gamma");
        text.setStyleRange(new StyleRange(6, 4, color(200, 0, 0), color(0, 0, 200)));

        Map<String, Object> run = runAt(7);
        assertThat(run.get("foreground")).isEqualTo(hex(new RGB(200, 0, 0)));
        assertThat(run.get("background")).isEqualTo(hex(new RGB(0, 0, 200)));
        assertThat(stage.renderedRunAt(text, 2).get("foreground")).isNotEqualTo(hex(new RGB(200, 0, 0)));
    }

    @Test
    @DisplayName("bold and italic ranges are painted bold and italic")
    void fontStylesArePainted() {
        fresh(SWT.MULTI, "alpha beta gamma");
        text.setStyleRange(new StyleRange(0, 5, null, null, SWT.BOLD));
        text.setStyleRange(new StyleRange(6, 4, null, null, SWT.ITALIC));

        assertThat(runAt(1).get("bold")).isEqualTo(true);
        assertThat(stage.renderedRunAt(text, 7).get("italic")).isEqualTo(true);
    }

    @ParameterizedTest(name = "underline style {0}")
    @ValueSource(ints = { SWT.UNDERLINE_SINGLE, SWT.UNDERLINE_DOUBLE, SWT.UNDERLINE_ERROR, SWT.UNDERLINE_SQUIGGLE, SWT.UNDERLINE_LINK })
    @DisplayName("each underline style is painted as that style")
    void underlineStyleIsPainted(int underlineStyle) {
        fresh(SWT.MULTI, "alpha beta gamma");
        StyleRange range = new StyleRange(6, 4, null, null);
        range.underline = true;
        range.underlineStyle = underlineStyle;
        text.setStyleRange(range);

        Map<String, Object> run = runAt(7);
        assertThat(run.get("underline")).isEqualTo(true);
        assertThat(StyledTextFlutterStage.num(run.get("underlineStyle"))).isEqualTo(underlineStyle);
    }

    @Test
    @DisplayName("a link is painted in the link colour when the range sets none")
    void linkUsesTheLinkColour() {
        fresh(SWT.MULTI, "alpha beta gamma");
        StyleRange range = new StyleRange(6, 4, null, null);
        range.underline = true;
        range.underlineStyle = SWT.UNDERLINE_LINK;
        text.setStyleRange(range);

        RGB link = text.getDisplay().getSystemColor(SWT.COLOR_LINK_FOREGROUND).getRGB();
        assertThat(runAt(7).get("foreground")).isEqualTo(hex(link));
    }

    @Test
    @DisplayName("underline and strikeout keep their own colours")
    void underlineAndStrikeoutColours() {
        fresh(SWT.MULTI, "alpha beta gamma");
        StyleRange range = new StyleRange(6, 4, null, null);
        range.underline = true;
        range.underlineColor = color(255, 0, 0);
        range.strikeout = true;
        range.strikeoutColor = color(0, 0, 255);
        text.setStyleRange(range);

        Map<String, Object> run = runAt(7);
        assertThat(run.get("underlineColor")).isEqualTo(hex(new RGB(255, 0, 0)));
        assertThat(run.get("strikeoutColor")).isEqualTo(hex(new RGB(0, 0, 255)));
    }

    @Test
    @DisplayName("a range font is painted at its size relative to the widget font")
    void rangeFontSize() {
        fresh(SWT.MULTI, "alpha beta gamma");
        FontData data = text.getFont().getFontData()[0];
        Font big = new Font(text.getDisplay(), data.getName(), data.getHeight() * 2, SWT.NORMAL);
        try {
            StyleRange range = new StyleRange();
            range.start = 6;
            range.length = 4;
            range.font = big;
            text.setStyleRange(range);

            double ranged = StyledTextFlutterStage.num(runAt(7).get("fontSize"));
            double plain = StyledTextFlutterStage.num(stage.renderedRunAt(text, 1).get("fontSize"));
            assertThat(ranged / plain).isCloseTo(2.0, within(0.05));
        } finally {
            big.dispose();
        }
    }

    @Test
    @DisplayName("a range font's own style is painted")
    void rangeFontStyleComesFromTheFont() {
        fresh(SWT.MULTI, "alpha beta gamma");
        FontData data = text.getFont().getFontData()[0];
        Font bold = new Font(text.getDisplay(), data.getName(), data.getHeight(), SWT.BOLD);
        try {
            StyleRange range = new StyleRange();
            range.start = 6;
            range.length = 4;
            range.font = bold;
            text.setStyleRange(range);

            assertThat(runAt(7).get("bold")).isEqualTo(true);
        } finally {
            bold.dispose();
        }
    }

    @Test
    @DisplayName("rise is painted")
    void riseIsPainted() {
        fresh(SWT.MULTI, "E=mc2");
        StyleRange range = new StyleRange(4, 1, null, null);
        range.rise = 5;
        text.setStyleRange(range);

        assertThat(StyledTextFlutterStage.num(runAt(4).get("rise"))).isEqualTo(5);
    }

    @Test
    @DisplayName("a border is painted")
    void borderIsPainted() {
        fresh(SWT.MULTI, "alpha beta gamma");
        StyleRange range = new StyleRange(6, 4, null, null);
        range.borderStyle = SWT.BORDER_SOLID;
        range.borderColor = color(0, 128, 0);
        text.setStyleRange(range);

        Map<String, Object> run = runAt(7);
        assertThat(StyledTextFlutterStage.num(run.get("borderStyle"))).isEqualTo(SWT.BORDER_SOLID);
        assertThat(run.get("borderColor")).isEqualTo(hex(new RGB(0, 128, 0)));
    }

    @Test
    @DisplayName("GlyphMetrics reserve their width where the text is laid out")
    void glyphMetricsReserveSpace() {
        fresh(SWT.MULTI, "ab￼cd");
        StyleRange range = new StyleRange(2, 1, null, null);
        range.metrics = new GlyphMetrics(10, 2, 40);
        text.setStyleRange(range);
        stage.focusAt(3);

        assertThat(text.getLocationAtOffset(3).x - text.getLocationAtOffset(2).x).isEqualTo(40);
        double[] caret = stage.renderedCaretRect(text);
        assertThat(caret[0]).as("shown caret after the reserved space")
                .isCloseTo(text.getLocationAtOffset(text.getCaretOffset()).x, within(1.0));
    }

    @Test
    @DisplayName("a style range changed without a reset reaches the screen on its own")
    void styleChangeWithoutResetIsPainted() {
        fresh(SWT.MULTI, "alpha beta gamma");
        stage.settle();
        text.replaceStyleRanges(6, 4, new StyleRange[] { new StyleRange(6, 4, color(0, 150, 0), null) });
        stage.flutter.flush();
        stage.settle();

        assertThat(stage.renderedRunAt(text, 7).get("foreground")).isEqualTo(hex(new RGB(0, 150, 0)));
    }

    // ---------------- listeners ----------------

    @Test
    @DisplayName("styles from a LineStyleListener are painted")
    void lineStyleListenerStylesArePainted() {
        fresh(SWT.MULTI, "alpha beta\ngamma delta");
        Color red = color(220, 0, 0);
        text.addLineStyleListener(e -> e.styles = new StyleRange[] { new StyleRange(e.lineOffset, 3, red, null) });
        text.redraw();

        assertThat(runAt(1).get("foreground")).isEqualTo(hex(red.getRGB()));
        assertThat(stage.renderedRunAt(text, text.getOffsetAtLine(1) + 1).get("foreground")).isEqualTo(hex(red.getRGB()));
    }

    @Test
    @DisplayName("a line background is painted")
    void lineBackgroundIsPainted() {
        fresh(SWT.MULTI, "one\ntwo\nthree");
        Color green = color(0, 200, 0);
        text.setLineBackground(1, 1, green);
        show();

        assertThat(lineBackgrounds()).containsExactly(Map.of("line", 1.0, "color", hex(green.getRGB())));
    }

    @Test
    @DisplayName("a background from a LineBackgroundListener is painted")
    void lineBackgroundListenerIsPainted() {
        fresh(SWT.MULTI, "one\ntwo\nthree");
        Color yellow = color(250, 250, 0);
        text.addLineBackgroundListener(e -> {
            if (e.lineOffset == 0) e.lineBackground = yellow;
        });
        text.redraw();
        show();

        assertThat(lineBackgrounds()).containsExactly(Map.of("line", 0.0, "color", hex(yellow.getRGB())));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> lineBackgrounds() {
        return (List<Map<String, Object>>) stage.facts(text).get("lineBackgrounds");
    }

    // ---------------- line attributes ----------------

    @Test
    @DisplayName("a wrap indent starts the continuation rows")
    void wrapIndentStartsContinuationRows() {
        fresh(SWT.MULTI | SWT.WRAP, LONG_LINE);
        text.setLineWrapIndent(0, 1, 40);
        show();

        int row = secondRowStart();
        assertThat(text.getLocationAtOffset(row).x).isEqualTo(40 + text.getLeftMargin());
        stage.clickAtOffset(row);
        assertThat(stage.renderedCaretRect(text)[0]).isCloseTo(text.getLocationAtOffset(text.getCaretOffset()).x, within(1.0));
    }

    @Test
    @DisplayName("a line indent moves only the first row")
    void indentMovesOnlyTheFirstRow() {
        fresh(SWT.MULTI | SWT.WRAP, LONG_LINE);
        text.setLineIndent(0, 1, 30);
        show();

        assertThat(text.getLocationAtOffset(0).x).isEqualTo(30 + text.getLeftMargin());
        assertThat(text.getLocationAtOffset(secondRowStart()).x).isEqualTo(text.getLeftMargin());
    }

    @Test
    @DisplayName("the widget's indent, alignment and justify are painted")
    void widgetLevelLineAttributes() {
        fresh(SWT.MULTI | SWT.WRAP, "short\n" + LONG_LINE);
        text.setIndent(25);
        show();
        assertThat(text.getLocationAtOffset(0).x).isEqualTo(25 + text.getLeftMargin());

        text.setIndent(0);
        text.setAlignment(SWT.RIGHT);
        stage.focusAt(5);
        double[] caret = stage.renderedCaretRect(text);
        Point end = text.getLocationAtOffset(5);
        assertThat(end.x).as("a right-aligned short line ends near the right edge")
                .isGreaterThan(text.getClientArea().width / 2);
        assertThat(caret[0]).isCloseTo(end.x, within(1.0));
    }

    @Test
    @DisplayName("a bullet shifts its line and is painted")
    void bulletIsPainted() {
        fresh(SWT.MULTI, "one\ntwo");
        StyleRange bulletStyle = new StyleRange();
        bulletStyle.metrics = new GlyphMetrics(0, 0, 20);
        text.setLineBullet(0, 1, new Bullet(ST.BULLET_DOT, bulletStyle));
        show();

        assertThat(text.getLocationAtOffset(0).x).isEqualTo(20 + text.getLeftMargin());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> bullets = (List<Map<String, Object>>) stage.facts(text).get("bullets");
        assertThat(bullets).extracting(b -> StyledTextFlutterStage.num(b.get("line"))).containsExactly(0.0);
    }

    @Test
    @DisplayName("per-line tab stops are used")
    void lineTabStops() {
        fresh(SWT.MULTI, "\tX\n\tY");
        text.setLineTabStops(0, 1, new int[] { 100 });
        show();

        assertThat(text.getLocationAtOffset(1).x).isEqualTo(100 + text.getLeftMargin());
        assertThat(text.getLocationAtOffset(text.getOffsetAtLine(1) + 1).x).isNotEqualTo(100 + text.getLeftMargin());
    }

    @Test
    @DisplayName("the widget's tab stops are used")
    void widgetTabStops() {
        fresh(SWT.MULTI, "\ta\tb");
        text.setTabStops(new int[] { 60, 130 });
        show();

        assertThat(text.getLocationAtOffset(1).x).isEqualTo(60 + text.getLeftMargin());
        assertThat(text.getLocationAtOffset(3).x).isEqualTo(130 + text.getLeftMargin());
    }

    @Test
    @DisplayName("line spacing separates the lines, and the caret on the next line is shown there")
    void lineSpacing() {
        fresh(SWT.MULTI, "one\ntwo");
        text.setLineSpacing(10);
        stage.focusAt(text.getOffsetAtLine(1) + 1);

        assertThat(text.getLinePixel(1) - text.getLinePixel(0)).isEqualTo(text.getLineHeight() + 10);
        assertThat(stage.renderedCaretRect(text)[1]).isCloseTo(text.getLocationAtOffset(text.getCaretOffset()).y, within(1.0));
    }

    @Test
    @DisplayName("a line spacing provider's spacing is used")
    void lineSpacingProvider() {
        fresh(SWT.MULTI, "one\ntwo\nthree");
        text.setLineSpacingProvider(line -> line == 0 ? 15 : 0);
        show();

        assertThat(text.getLinePixel(1) - text.getLinePixel(0)).isEqualTo(text.getLineHeight() + 15);
        assertThat(text.getLinePixel(2) - text.getLinePixel(1)).isEqualTo(text.getLineHeight());
    }

    @Test
    @DisplayName("a vertical indent pushes its line down")
    void verticalIndent() {
        fresh(SWT.MULTI, "one\ntwo");
        text.setLineVerticalIndent(1, 12);
        stage.focusAt(text.getOffsetAtLine(1) + 1);

        assertThat(text.getLocationAtOffset(text.getOffsetAtLine(1)).y)
                .isEqualTo(text.getLinePixel(1) + 12);
        assertThat(stage.renderedCaretRect(text)[1]).isCloseTo(text.getLocationAtOffset(text.getCaretOffset()).y, within(1.0));
    }

    // ---------------- selection and caret appearance ----------------

    @Test
    @DisplayName("the selection colours are painted as set")
    void selectionColours() {
        fresh(SWT.MULTI, "alpha beta gamma");
        Color background = color(200, 30, 30);
        Color foreground = color(255, 255, 255);
        text.setSelectionBackground(background);
        text.setSelectionForeground(foreground);
        stage.focusAt(0);
        text.setSelection(6, 10);
        show();

        Map<String, Object> facts = stage.facts(text);
        assertThat(facts.get("selectionBackground")).isEqualTo(hex(background.getRGB()));
        assertThat(facts.get("selectionForeground")).isEqualTo(hex(foreground.getRGB()));
    }

    @Test
    @DisplayName("an empty line inside a selection is highlighted")
    void emptyLineInsideASelectionIsHighlighted() {
        fresh(SWT.MULTI, "one\n\nthree");
        stage.focusAt(0);
        text.setSelection(0, text.getCharCount());
        show();

        int y = text.getLinePixel(1);
        assertThat(stage.renderedSelectionRects(text))
                .as("a highlight on the empty line")
                .anySatisfy(r -> {
                    assertThat(r[1]).isCloseTo(y, within(1.0));
                    assertThat(r[2]).isPositive();
                });
    }

    @Test
    @DisplayName("FULL_SELECTION highlights selected lines to the right edge")
    void fullSelectionReachesTheEdge() {
        fresh(SWT.MULTI | SWT.FULL_SELECTION, "one\ntwo\nthree");
        stage.focusAt(0);
        text.setSelection(0, 6);
        show();

        int width = text.getClientArea().width;
        int y = text.getLinePixel(0);
        assertThat(stage.renderedSelectionRects(text))
                .anySatisfy(r -> {
                    assertThat(r[1]).isCloseTo(y, within(1.0));
                    assertThat(r[0] + r[2]).isGreaterThanOrEqualTo(width - 1);
                });
    }

    @Test
    @DisplayName("a block selection is painted as a rectangle")
    void blockSelectionIsPainted() {
        fresh(SWT.MULTI, "alpha beta\ngamma delta\nepsilon zeta");
        stage.focusAt(0);
        text.setBlockSelection(true);
        int lineHeight = text.getLineHeight();
        text.setBlockSelectionBounds(10, 1, 40, 2 * lineHeight);
        show();

        Rectangle bounds = text.getBlockSelectionBounds();
        @SuppressWarnings("unchecked")
        Map<String, Object> block = (Map<String, Object>) stage.facts(text).get("blockSelection");
        assertThat(block).isNotNull();
        assertThat(StyledTextFlutterStage.num(block.get("x"))).isCloseTo(bounds.x, within(1.0));
        assertThat(StyledTextFlutterStage.num(block.get("width"))).isCloseTo(bounds.width, within(1.0));
    }

    @Test
    @DisplayName("the caret is as tall as its line")
    void caretIsAsTallAsItsLine() {
        fresh(SWT.MULTI, "alpha beta");
        stage.focusAt(3);

        assertThat(stage.renderedCaretRect(text)[3]).isCloseTo(text.getLineHeight(3), within(1.0));
    }

    @Test
    @DisplayName("the caret's size is the widget Caret's size")
    void customCaretSizeIsPainted() {
        fresh(SWT.MULTI, "alpha beta");
        // StyledText moves an application caret but keeps its size, unlike its default caret.
        Caret caret = new Caret(text, SWT.NONE);
        caret.setSize(4, text.getLineHeight());
        text.setCaret(caret);
        stage.focusAt(3);

        assertThat(stage.renderedCaretRect(text)[2]).isCloseTo(4, within(0.5));
    }

    @Test
    @DisplayName("a background image is painted")
    void backgroundImageIsPainted() {
        fresh(SWT.MULTI, "alpha");
        Image image = new Image(text.getDisplay(), 8, 8);
        try {
            text.setBackgroundImage(image);
            show();

            assertThat(stage.facts(text).get("backgroundImage")).isEqualTo(true);
        } finally {
            text.setBackgroundImage(null);
            image.dispose();
        }
    }

    @Test
    @DisplayName("a right-to-left editor is painted right to left")
    void rightToLeftIsPainted() {
        fresh(SWT.MULTI | SWT.RIGHT_TO_LEFT, "alpha");
        stage.settle();

        assertThat(stage.facts(text).get("direction")).isEqualTo("rtl");
    }
}
