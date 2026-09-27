package org.eclipse.swt.custom;

import dev.equo.swt.harness.UserInput.Key;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Scrolling a StyledText: what the application asks for is what the client shows, what the user
 * does is what the widget reports, and the caret or selection an action targets ends up visible.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextScrollFlutterTest {

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

    private static String lines(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append("line ").append(i).append(" of the document\n");
        return sb.toString();
    }

    private static String longWrappedLines(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("paragraph ").append(i)
                    .append(" is long enough to wrap onto several rows inside a narrow editor, so its height is more than one line\n");
        }
        return sb.append("last").toString();
    }

    private void fresh(int style, String content) {
        stage.fresh(style, content);
        text = stage.subject;
    }

    @Test
    @DisplayName("the lines at the scrolled-to position are the ones painted")
    void scrolledLinesArePainted() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(400));
        for (int topIndex : new int[] { 0, 39, 120, 399 }) {
            text.setTopIndex(topIndex);
            stage.pushState();

            int[] painted = stage.paintedLines(text);
            assertThat(painted).as("painted lines with top index %d", topIndex).hasSize(2);
            int lastVisible = Math.min(text.getLineCount() - 1, text.getLineIndex(text.getClientArea().height - 1));
            assertThat(painted[0]).as("first painted line with top index %d", topIndex).isLessThanOrEqualTo(topIndex);
            assertThat(painted[1]).as("last painted line with top index %d", topIndex)
                    .isGreaterThanOrEqualTo(lastVisible);
        }
    }

    @Test
    @DisplayName("every shown line is where getLinePixel says, with lines that wrap")
    void shownWrappedLinesAreWhereTheWidgetSaysTheyAre() {
        fresh(SWT.MULTI | SWT.V_SCROLL | SWT.WRAP, longWrappedLines(120));
        scrollAndCheckAlignment();
    }

    @Test
    @DisplayName("every shown line is where getLinePixel says, with lines of different heights")
    void shownUnevenLinesAreWhereTheWidgetSaysTheyAre() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(400));
        tallerEveryFifthLine();
        stage.pushState();
        scrollAndCheckAlignment();
    }

    @Test
    @DisplayName("what a scroll redraws is sent with the scroll, not after it")
    void aRulerKeepsUpWithTheText() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(400));
        Canvas ruler = new Canvas(stage.shell, SWT.NONE);
        ruler.setBounds(StyledTextFlutterStage.WIDTH + 10, 0, 30, StyledTextFlutterStage.HEIGHT);
        ruler.addListener(SWT.Paint, e -> {
            int top = text.getTopIndex();
            e.gc.drawText(String.valueOf(top + 1), 2, text.getLinePixel(top), true);
        });
        text.addListener(SWT.Paint, e -> ruler.redraw());
        stage.settle();

        List<String> frames = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        stage.flutter.devTools().onEvent("Network.webSocketFrameReceived", params -> {
            com.google.gson.JsonObject response = params.getAsJsonObject("response");
            if (response == null || response.get("opcode").getAsInt() != 2) return;
            frames.add(new String(java.util.Base64.getDecoder().decode(response.get("payloadData").getAsString()),
                    java.nio.charset.StandardCharsets.UTF_8));
        });
        stage.flutter.devTools().send("Network.enable", null);

        try {
            for (int tick = 0; tick < 5; tick++) {
                frames.clear();
                text.setTopPixel(text.getTopPixel() + 97);
                stage.flutter.flush();

                List<String> sent = new java.util.ArrayList<>(frames);
                assertThat(sent).as("frames for tick %d", tick)
                        .anyMatch(f -> f.contains("\"topPixel\"") && f.contains(String.valueOf(text.hashCode())));
                assertThat(sent).as("the ruler's drawing goes out with the scroll, tick %d", tick)
                        .anyMatch(f -> f.contains("GC/" + ruler.hashCode()));
            }
        } finally {
            ruler.dispose();
            stage.settle();
        }
    }

    @Test
    @DisplayName("a scroll that crosses a band boundary does not re-band on every step across it")
    void theBandDoesNotFlipAtItsBoundary() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(2000));
        for (int line = 0; line < 2000; line += 2) {
            int start = text.getOffsetAtLine(line);
            text.setStyleRange(new StyleRange(start, 4, text.getDisplay().getSystemColor(SWT.COLOR_RED), null));
        }
        text.setTopIndex(900);
        stage.settle();

        List<String> frames = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        stage.flutter.devTools().onEvent("Network.webSocketFrameReceived", params -> {
            com.google.gson.JsonObject response = params.getAsJsonObject("response");
            if (response == null || response.get("opcode").getAsInt() != 2) return;
            frames.add(new String(java.util.Base64.getDecoder().decode(response.get("payloadData").getAsString()),
                    java.nio.charset.StandardCharsets.UTF_8));
        });
        stage.flutter.devTools().send("Network.enable", null);

        // Styles travel again where the band is rebuilt: its quantised edge.
        int boundary = -1;
        for (int top = 901; top < 1100 && boundary < 0; top++) {
            frames.clear();
            text.setTopIndex(top);
            stage.flutter.flush();
            if (new java.util.ArrayList<>(frames).stream().anyMatch(f -> f.contains("\"styleIndex\""))) {
                boundary = top;
            }
        }
        assertThat(boundary).as("a 200-line scroll has to rebuild the band at some point")
                .isGreaterThan(0);

        // Crossing the edge back and forth must not rebuild the band each time.
        for (int tick = 0; tick < 4; tick++) {
            frames.clear();
            text.setTopIndex(tick % 2 == 0 ? boundary - 2 : boundary);
            stage.flutter.flush();

            assertThat(new java.util.ArrayList<>(frames))
                    .as("tick %d: two lines either side of the edge is the same band", tick)
                    .noneMatch(f -> f.contains("\"styleIndex\""));
        }
    }

    @Test
    @DisplayName("a line added inside the band keeps the band, so its styles travel as a small change")
    void aNewLineKeepsTheBand() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(2000));
        for (int line = 0; line < 2000; line += 2) {
            int start = text.getOffsetAtLine(line);
            text.setStyleRange(new StyleRange(start, 4, text.getDisplay().getSystemColor(SWT.COLOR_RED), null));
        }
        text.setTopIndex(900);
        stage.settle();
        // Scrolled on as far as the band is kept for: built here, it would start a quantum later.
        int visible = Math.max(1, text.getClientArea().height / text.getLineHeight());
        int quantisedVisible = (visible + 24) / 25 * 25;
        int top = 900 + 2 * quantisedVisible;
        text.setTopIndex(top);
        stage.settle();

        List<String> frames = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        stage.flutter.devTools().onEvent("Network.webSocketFrameReceived", params -> {
            com.google.gson.JsonObject response = params.getAsJsonObject("response");
            if (response == null || response.get("opcode").getAsInt() != 2) return;
            frames.add(new String(java.util.Base64.getDecoder().decode(response.get("payloadData").getAsString()),
                    java.nio.charset.StandardCharsets.UTF_8));
        });
        stage.flutter.devTools().send("Network.enable", null);

        // What Enter does: one more line, the band's lines unchanged.
        text.replaceTextRange(text.getOffsetAtLine(top + 5), 0, "\n");
        stage.flutter.flush();

        java.util.regex.Matcher index = java.util.regex.Pattern.compile("\"styleIndex\":\\[([^\\]]*)\\]")
                .matcher(String.join("", new java.util.ArrayList<>(frames)));
        assertThat(index.find()).as("the edit moved the runs after it, so the index travels").isTrue();
        String[] ints = index.group(1).split(",");
        assertThat(Integer.parseInt(ints[0].trim()))
                .as("as a change from what the client holds")
                .isEqualTo(StyledTextHelper.STYLE_INDEX_EDIT);
        assertThat(ints.length)
                .as("a handful of ints, not the band rebuilt at another quantum")
                .isLessThan(100);
    }

    @Test
    @DisplayName("the top line a ruler is told about is the line at the top of the text")
    void theTopLineIsTheLineAtTheTop() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(400));
        tallerEveryFifthLine();
        stage.pushState();

        text.setTopIndex(120);
        stage.settle();
        assertTopLineIsConsistent("after setTopIndex(120)");

        // An edit above the top line is what makes the widget recompute where it is scrolled to.
        text.replaceTextRange(text.getOffsetAtLine(2), 0, "x");
        stage.settle();
        assertTopLineIsConsistent("after an edit above the top line");

        text.setTopPixel(text.getTopPixel() + 211);
        stage.settle();
        assertTopLineIsConsistent("after scrolling on from there");
    }

    @Test
    @DisplayName("the top line is right in a document styled by a listener, as an editor styles one")
    void theTopLineIsRightWithAStyleListener() {
        // Styled by a listener, with a squiggle under every line, as an editor's annotations are.
        StringBuilder doc = new StringBuilder();
        for (int i = 1; i <= 99; i++) doc.append(i).append('\n');
        fresh(SWT.MULTI | SWT.V_SCROLL, doc.toString());
        Color red = stage.display.getSystemColor(SWT.COLOR_RED);
        text.addLineStyleListener(event -> {
            StyleRange squiggle = new StyleRange(event.lineOffset, event.lineText.length(), red, null);
            squiggle.underline = true;
            squiggle.underlineStyle = SWT.UNDERLINE_ERROR;
            event.styles = new StyleRange[] { squiggle };
        });
        text.redraw();
        stage.settle();

        for (int top : new int[] { 0, 1, 7, 40, 60, 98 }) {
            text.setTopIndex(top);
            stage.settle();
            assertTopLineIsConsistent("with a style listener, top index " + top);
            assertThat(stage.renderedLines(text)).as("the text shown at top index %d", top)
                    .isNotEmpty();
            assertThat(text.getLine(text.getTopIndex()))
                    .as("the line the widget calls the top one, top index %d", top)
                    .isEqualTo(String.valueOf(text.getTopIndex() + 1));
        }
    }

    @Test
    @DisplayName("scrolling repaints the text, so what is drawn over it follows the viewport")
    void scrollingRepaintsWhatIsDrawnOverTheText() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(400));
        // Native SWT repaints the strip a scroll exposes; without that Paint a ruler keeps stale numbers.
        Canvas ruler = new Canvas(stage.shell, SWT.NONE);
        ruler.setBounds(StyledTextFlutterStage.WIDTH + 10, 0, 30, StyledTextFlutterStage.HEIGHT);
        List<Integer> drawnTopLine = new java.util.ArrayList<>();
        List<Integer> textPaints = new java.util.ArrayList<>();
        ruler.addListener(SWT.Paint, e -> drawnTopLine.add(text.getTopIndex()));
        text.addListener(SWT.Paint, e -> {
            textPaints.add(text.getTopIndex());
            ruler.redraw();
        });
        stage.settle();

        try {
            // A plain redraw must reach the listeners first, or the scroll case proves nothing.
            textPaints.clear();
            text.redraw();
            stage.settle();
            assertThat(textPaints).as("a plain redraw reaches the text's Paint listeners").isNotEmpty();

            for (int tick = 0; tick < 5; tick++) {
                drawnTopLine.clear();
                textPaints.clear();
                text.setTopPixel(text.getTopPixel() + 97);
                stage.settle();
                assertThat(textPaints).as("the text repainted for scroll %d", tick).isNotEmpty();
                assertThat(drawnTopLine).as("the ruler repainted for scroll %d", tick).isNotEmpty();
                assertThat(drawnTopLine.get(drawnTopLine.size() - 1))
                        .as("the top line the ruler drew for scroll %d", tick)
                        .isEqualTo(text.getTopIndex());
            }
        } finally {
            ruler.dispose();
            stage.settle();
        }
    }

    /** What a line-number ruler asks: which line is at the top, and where does each line sit. */
    private void assertTopLineIsConsistent(String when) {
        int top = text.getTopIndex();
        assertThat(text.getLineIndex(0)).as("line at the top of the client area, %s", when).isEqualTo(top);
        assertThat(text.getLinePixel(top)).as("where the top line sits, %s", when)
                .isBetween(-text.getLineHeight(text.getOffsetAtLine(top)), 0);
        Map<Integer, Double> painted = stage.paintedRowY(text);
        assertThat(painted).as("painted lines %s", when).containsKey(top);
        assertThat(painted.get(top)).as("the client painted the top line where the widget says, %s", when)
                .isCloseTo(text.getLinePixel(top), within(1.0));
    }

    private void tallerEveryFifthLine() {
        FontData data = text.getFont().getFontData()[0];
        Font big = new Font(text.getDisplay(), data.getName(), data.getHeight() * 2, SWT.NORMAL);
        for (int line = 0; line + 1 < text.getLineCount(); line += 5) {
            if (text.getLine(line).length() < 4) continue;
            StyleRange range = new StyleRange();
            range.start = text.getOffsetAtLine(line);
            range.length = 4;
            range.font = big;
            text.setStyleRange(range);
        }
    }

    private void scrollAndCheckAlignment() {
        double[] at = stage.page(text, 100, 100);
        for (int burst = 0; burst < 6; burst++) {
            for (int tick = 0; tick < 5; tick++) stage.input().wheel(at[0], at[1], 0, -120, 0);
            stage.settle();
            assertLinesAreWhereTheyAreSaidToBe("after " + (burst + 1) * 5 + " wheel ticks");
        }
    }

    @Test
    @DisplayName("every shown line is where getLinePixel says, however fast the scrolling")
    void shownLinesAreWhereTheWidgetSaysTheyAre() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(400));
        double[] at = stage.page(text, 100, 100);
        // Wheel ticks faster than frames: client and widget must never disagree on the offset.
        for (int burst = 0; burst < 6; burst++) {
            for (int tick = 0; tick < 5; tick++) stage.input().wheel(at[0], at[1], 0, -120, 0);
            stage.settle();
            assertLinesAreWhereTheyAreSaidToBe("after " + (burst + 1) * 5 + " wheel ticks");
        }
        for (int step = 0; step < 4; step++) {
            text.setTopPixel(text.getTopPixel() + 137);
            stage.pushState();
            assertLinesAreWhereTheyAreSaidToBe("after setTopPixel " + text.getTopPixel());
        }
    }

    private void assertLinesAreWhereTheyAreSaidToBe(String when) {
        Map<Integer, Double> painted = stage.paintedRowY(text);
        assertThat(painted).as("painted lines %s", when).isNotEmpty();
        int height = text.getClientArea().height;
        painted.forEach((line, y) -> {
            if (y < -text.getLineHeight() || y > height) return; // outside the client area
            assertThat(y).as("line %d painted at y, %s", line, when)
                    .isCloseTo(text.getLinePixel(line), within(1.0));
        });
    }

    private void assertShownScrollMatches() {
        double[] scroll = stage.renderedScroll(text);
        assertThat(scroll[1]).as("shown vertical scroll").isEqualTo(text.getTopPixel());
        assertThat(scroll[0]).as("shown horizontal scroll").isEqualTo(text.getHorizontalPixel());
        assertThat(text.getVerticalBar().getSelection()).as("vertical scroll bar").isEqualTo(text.getTopPixel());
    }

    /** Whether the whole line holding {@code offset} lies inside the client area. */
    private boolean lineVisible(int offset) {
        int y = text.getLocationAtOffset(offset).y;
        return y >= 0 && y + text.getLineHeight(offset) <= text.getClientArea().height;
    }

    @Test
    @DisplayName("setTopIndex is shown")
    void setTopIndexIsShown() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(100));
        text.setTopIndex(30);
        stage.pushState();

        assertThat(text.getTopIndex()).isEqualTo(30);
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("setTopPixel is shown, clamped the way SWT clamps it")
    void setTopPixelIsShown() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(100));
        text.setTopPixel(1_000_000);
        stage.pushState();

        int max = text.getVerticalBar().getMaximum() - text.getVerticalBar().getThumb();
        assertThat(text.getTopPixel()).isEqualTo(max);
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("setSelection scrolls the selection into view")
    void setSelectionRevealsTheSelection() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(100));
        int start = text.getOffsetAtLine(70);
        text.setSelection(start, start + 4);
        stage.pushState();

        assertThat(lineVisible(start)).as("line 70 visible").isTrue();
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("setCaretOffset followed by showSelection brings the caret into view")
    void showSelectionRevealsTheCaret() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(100));
        int offset = text.getOffsetAtLine(85);
        text.setCaretOffset(offset);
        text.showSelection();
        stage.pushState();

        assertThat(lineVisible(offset)).isTrue();
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("the key bound to 'text end' scrolls the end into view")
    void textEndKeyRevealsTheEnd() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(100));
        stage.focusAt(0);
        stage.pressAction(ST.TEXT_END);

        assertThat(text.getCaretOffset()).isEqualTo(text.getCharCount());
        assertThat(lineVisible(text.getCharCount())).isTrue();
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("in a wrapped document the key bound to 'text end' reaches the last row")
    void wrappedDocumentScrollsToTheEnd() {
        fresh(SWT.MULTI | SWT.V_SCROLL | SWT.WRAP, longWrappedLines(40));
        stage.focusAt(0);
        stage.pressAction(ST.TEXT_END);

        assertThat(text.getCaretOffset()).isEqualTo(text.getCharCount());
        assertThat(lineVisible(text.getCharCount())).as("last line visible").isTrue();
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("in a wrapped document, the wheel scrolls all the way to the last row")
    void wrappedDocumentWheelReachesTheEnd() {
        fresh(SWT.MULTI | SWT.V_SCROLL | SWT.WRAP, longWrappedLines(40));
        double[] at = stage.page(text, 100, 100);
        for (int i = 0; i < 400; i++) stage.input().wheel(at[0], at[1], 0, 400, 0);
        stage.settle();

        assertThat(lineVisible(text.getCharCount())).as("last line visible").isTrue();
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("End on a long unwrapped line scrolls the caret into view horizontally")
    void endRevealsTheCaretHorizontally() {
        StringBuilder wide = new StringBuilder();
        for (int i = 0; i < 60; i++) wide.append("wide").append(i).append(' ');
        fresh(SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, wide + "\nshort");
        stage.focusAt(0);
        stage.pressAction(ST.LINE_END);

        int end = text.getLine(0).length();
        assertThat(text.getCaretOffset()).isEqualTo(end);
        Point location = text.getLocationAtOffset(end);
        assertThat(location.x).as("caret x inside the client area")
                .isBetween(0, text.getClientArea().width);
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("the horizontal range covers the longest line")
    void horizontalRangeCoversTheLongestLine() {
        StringBuilder wide = new StringBuilder();
        for (int i = 0; i < 60; i++) wide.append("W\tide").append(i).append(' ');
        fresh(SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, wide + "\nshort");
        stage.settle();

        int lineEnd = text.getLine(0).length();
        int contentRight = text.getLocationAtOffset(lineEnd).x + text.getHorizontalPixel();
        assertThat(text.getHorizontalBar().getMaximum()).isGreaterThanOrEqualTo(contentRight);
    }

    @Test
    @DisplayName("PageDown moves the caret a page and keeps it in view")
    void pageDownMovesByAPage() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(100));
        stage.focusAt(0);
        stage.press(Key.PAGE_DOWN);

        assertThat(text.getLineAtOffset(text.getCaretOffset())).isGreaterThan(0);
        assertThat(lineVisible(text.getCaretOffset())).isTrue();
        assertThat(stage.renderedCarets(text)).containsExactly(text.getCaretOffset());
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("typing at the bottom edge scrolls to keep the caret visible")
    void typingAtTheBottomKeepsTheCaretVisible() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(12));
        stage.focusAt(text.getCharCount());
        stage.type("\n\n\n\n\n\n\n\n\n\nmore");

        assertThat(lineVisible(text.getCaretOffset())).isTrue();
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("a trackpad's small wheel deltas scroll by the pixel, not by the line")
    void smallWheelDeltasScrollByThePixel() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(100));
        double[] at = stage.page(text, 100, 100);
        for (int i = 0; i < 3; i++) stage.input().wheel(at[0], at[1], 0, 7, 0);
        stage.settle();

        assertThat(text.getTopPixel()).isEqualTo(21);
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("a wheel tick a listener vetoes leaves the view where the widget is")
    void vetoedWheelDoesNotScroll() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(100));
        text.addListener(SWT.MouseWheel, e -> e.doit = false);
        double[] at = stage.page(text, 100, 100);
        stage.input().wheel(at[0], at[1], 0, 120, 0);
        stage.settle();

        assertThat(text.getTopPixel()).isZero();
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("a wheel scroll keeps the application's scroll bar in step")
    void wheelKeepsTheScrollBarInStep() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(100));
        double[] at = stage.page(text, 100, 100);
        stage.input().wheel(at[0], at[1], 0, 240, 0);
        stage.settle();

        assertThat(text.getTopPixel()).isPositive();
        assertShownScrollMatches();
    }

    @Test
    @DisplayName("the application scrolling right after a user scroll is shown")
    void applicationScrollAfterUserScrollIsShown() {
        fresh(SWT.MULTI | SWT.V_SCROLL, lines(100));
        double[] at = stage.page(text, 100, 100);
        stage.input().wheel(at[0], at[1], 0, 240, 0);
        stage.settle();
        text.setTopIndex(5);
        stage.pushState();

        assertThat(text.getTopIndex()).isEqualTo(5);
        assertShownScrollMatches();
    }
}
