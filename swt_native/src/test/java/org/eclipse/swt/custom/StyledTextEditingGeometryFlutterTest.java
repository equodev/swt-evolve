package org.eclipse.swt.custom;

import dev.equo.swt.harness.UserInput.Key;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.ScrollBar;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Editing a large highlighted document keeps StyledText's position API, and its scroll ranges, the
 * ones an editor handed the same document fresh answers with. The client builds the table those
 * answers come from after each edit out of the table before it, so every way of editing is checked
 * against an editor that never saw the edits: typing, new lines, joined lines, pasting, edits far
 * from the view, a line growing past the view (the horizontal scroll bar appearing) and shrinking
 * back, restyles, the current line moving, and scrolling.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextEditingGeometryFlutterTest {

    /** Past the size below which the client describes the whole document with every character's position. */
    private static final int LINES = 1200;

    private final StyledTextFlutterStage stage = new StyledTextFlutterStage();
    private StyledText text;
    private Color[] palette;
    private Color currentLine;
    private Font bold;
    /** The line the current-line background is on. */
    private int highlighted = -1;

    @BeforeAll
    void boot() {
        stage.boot();
        palette = new Color[6];
        for (int i = 0; i < palette.length; i++) palette[i] = new Color(stage.display, 127, 30 * i, 85);
        currentLine = new Color(stage.display, 55, 55, 55);
        FontData data = stage.subject.getFont().getFontData()[0];
        bold = new Font(stage.display, data.getName(), data.getHeight(), SWT.BOLD);
    }

    @AfterAll
    void shutdown() {
        stage.shutdown();
    }

    // ---------------- documents ----------------

    /** A source file's worth of lines: indented code, blank lines, tabs and a few long ones. */
    private static String code(int lines) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            if (i > 0) out.append('\n');
            if (i % 17 == 0) continue;
            if (i % 23 == 0) out.append("\tcase ").append(i).append(":\t\treturn value").append(i).append(';');
            else if (i % 97 == 0) out.append("    // ").append("a long comment that goes well past the others ".repeat(3)).append(i);
            else out.append("        final total").append(i).append(" = prices.get(sku).times(quantity); // ").append(i);
        }
        return out.toString();
    }

    /** Every line wider than the view. */
    private static String wideLines(int lines) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            if (i > 0) out.append('\n');
            out.append("        final total").append(i).append(" = prices.get(sku).times(quantity).plus(shipping.of(order, region)).minus(discounts.applicableTo(customer, order)); // ").append(i);
        }
        return out.toString();
    }

    /** Short lines only: nothing to scroll sideways to. */
    private static String shortLines(int lines) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            if (i > 0) out.append('\n');
            out.append("item ").append(i).append(" = ok;");
        }
        return out.toString();
    }

    private void open(String document) {
        open(document, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    }

    private void open(String document, int style) {
        stage.fresh(style, document);
        text = stage.subject;
        highlight(text);
        highlighted = -1;
        moveCurrentLine(0);
        stage.settle();
    }

    /** Words coloured by where they start, as a highlighter colours a file. */
    private void highlight(StyledText widget) {
        List<StyleRange> ranges = new ArrayList<>();
        String content = widget.getText();
        int at = 0;
        while (at < content.length()) {
            while (at < content.length() && !Character.isLetterOrDigit(content.charAt(at))) at++;
            int start = at;
            while (at < content.length() && Character.isLetterOrDigit(content.charAt(at))) at++;
            if (at > start) ranges.add(new StyleRange(start, at - start, palette[(start - widget.getOffsetAtLine(widget.getLineAtOffset(start))) % palette.length], null));
        }
        widget.setStyleRanges(ranges.toArray(new StyleRange[0]));
    }

    private void moveCurrentLine(int line) {
        if (highlighted >= 0 && highlighted < text.getLineCount()) text.setLineBackground(highlighted, 1, null);
        text.setLineBackground(line, 1, currentLine);
        highlighted = line;
    }

    // ---------------- the oracle ----------------

    /**
     * Checks the subject's position API and vertical scroll range against an editor handed the
     * subject's document, styles, current line and scroll position directly. The horizontal range is
     * the widest line laid out so far, as natively, so it depends on what was shown before and is
     * checked by the scenarios about it instead.
     */
    private void answersAsAFreshEditor(String because) {
        stage.settle();
        StyledText fresh = new StyledText(stage.shell, text.getStyle() & (SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL));
        try {
            fresh.setBounds(StyledTextFlutterStage.WIDTH + 20, 0, text.getSize().x, text.getSize().y);
            fresh.setFont(text.getFont());
            fresh.setLineSpacing(text.getLineSpacing());
            fresh.setTabs(text.getTabs());
            fresh.setText(text.getText());
            fresh.setStyleRanges(text.getStyleRanges());
            if (highlighted >= 0 && highlighted < fresh.getLineCount()) fresh.setLineBackground(highlighted, 1, currentLine);
            fresh.setTopPixel(text.getTopPixel());
            fresh.setHorizontalPixel(text.getHorizontalPixel());
            stage.settle();

            for (int line : probeLines()) {
                assertThat(text.getLinePixel(line)).as("%s: the pixel of line %d", because, line)
                        .isEqualTo(fresh.getLinePixel(line));
                int start = text.getOffsetAtLine(line);
                int length = text.getLine(line).length();
                for (int offset : new int[] { start, start + length / 2, start + length }) {
                    assertThat(inDocument(text, text.getLocationAtOffset(offset))).as("%s: the location of offset %d, line %d", because, offset, line)
                            .isEqualTo(inDocument(fresh, fresh.getLocationAtOffset(offset)));
                }
            }
            for (int line = text.getTopIndex(); line < Math.min(text.getLineCount(), text.getTopIndex() + 8); line++) {
                int start = text.getOffsetAtLine(line);
                for (int offset = start; offset <= start + text.getLine(line).length(); offset += 5) {
                    Point location = text.getLocationAtOffset(offset);
                    Point inFresh = new Point(location.x + text.getHorizontalPixel() - fresh.getHorizontalPixel(), location.y);
                    assertThat(text.getOffsetAtPoint(location)).as("%s: the offset at the location of %d", because, offset)
                            .isEqualTo(fresh.getOffsetAtPoint(inFresh));
                }
            }
            // A wrapped editor's height counts lines not laid out yet at an estimate, so it too
            // depends on what was shown before.
            if ((text.getStyle() & SWT.WRAP) == 0) {
                sameRange(text.getVerticalBar(), fresh.getVerticalBar(), because + ": the vertical scroll bar");
            }
        } finally {
            fresh.dispose();
            stage.settle();
        }
    }

    /**
     * A location in the editor's client area as it lies in the document: the horizontal scroll
     * reaches only as far as the lines laid out so far, so a fresh editor may not scroll as far.
     */
    private static Point inDocument(StyledText editor, Point location) {
        return new Point(location.x + editor.getHorizontalPixel(), location.y);
    }

    /** Lines in view and around it, at the ends of the document, and spread through it. */
    private List<Integer> probeLines() {
        int count = text.getLineCount();
        List<Integer> lines = new ArrayList<>();
        int top = text.getTopIndex();
        for (int l = Math.max(0, top - 3); l < Math.min(count, top + 25); l++) lines.add(l);
        for (int l : new int[] { 0, 1, 97, count / 3, count / 2, count - 2, count - 1 }) {
            if (l >= 0 && l < count && !lines.contains(l)) lines.add(l);
        }
        return lines;
    }

    /** The range and where it is scrolled to; the thumb is the view's height, less the horizontal bar while that shows. */
    private static void sameRange(ScrollBar subject, ScrollBar fresh, String what) {
        assertThat(new int[] { subject.getMaximum(), subject.getSelection() }).as(what)
                .containsExactly(fresh.getMaximum(), fresh.getSelection());
    }

    private static boolean scrollable(ScrollBar bar) {
        return bar.getMaximum() > bar.getThumb();
    }

    // ---------------- typing ----------------

    @Test
    @DisplayName("typing and deleting characters inside the view")
    void typingInView() {
        open(code(LINES));
        stage.focusAt(text.getOffsetAtLine(3) + 10);
        stage.type("xy");
        answersAsAFreshEditor("after typing two characters");
        stage.press(Key.BACKSPACE);
        stage.press(Key.BACKSPACE);
        answersAsAFreshEditor("after deleting them");
    }

    @Test
    @DisplayName("typing into a line of tabs")
    void typingAmongTabs() {
        open(code(LINES));
        stage.focusAt(text.getOffsetAtLine(23) + 1);
        stage.press(Key.TAB);
        stage.type("ab");
        answersAsAFreshEditor("after a tab and two characters");
    }

    // ---------------- lines ----------------

    @Test
    @DisplayName("Enter in the middle of a line, then Backspace joins it again")
    void newLineAndJoin() {
        open(code(LINES));
        stage.focusAt(text.getOffsetAtLine(5) + 20);
        stage.press(Key.ENTER);
        answersAsAFreshEditor("after Enter");
        stage.press(Key.ENTER);
        answersAsAFreshEditor("after a second Enter");
        stage.press(Key.BACKSPACE);
        stage.press(Key.BACKSPACE);
        answersAsAFreshEditor("after joining the lines again");
    }

    @Test
    @DisplayName("pasting several lines")
    void pasteLines() {
        open(code(LINES));
        stage.focusAt(text.getOffsetAtLine(12) + 8);
        stage.setClipboardText("one\ntwo\n\tthree\nfour");
        stage.pressAction(ST.PASTE);
        answersAsAFreshEditor("after pasting four lines");
    }

    @Test
    @DisplayName("lines added and removed far below the view")
    void editsFarBelow() {
        open(code(LINES));
        int at = text.getOffsetAtLine(1000);
        text.replaceTextRange(at, 0, "an inserted line\n");
        answersAsAFreshEditor("after a line was inserted far below");
        text.replaceTextRange(at, "an inserted line\n".length(), "");
        answersAsAFreshEditor("after it was removed");
        int from = text.getOffsetAtLine(700);
        text.replaceTextRange(from, text.getOffsetAtLine(720) - from, "");
        answersAsAFreshEditor("after twenty lines were deleted far below");
    }

    @Test
    @DisplayName("at the very start and the very end of the document")
    void editsAtTheEnds() {
        open(code(LINES));
        text.replaceTextRange(0, 0, "first\n");
        answersAsAFreshEditor("after a line was added at the start");
        text.replaceTextRange(text.getCharCount(), 0, "\nlast");
        answersAsAFreshEditor("after a line was added at the end");
    }

    // ---------------- the horizontal scroll bar ----------------

    @Test
    @DisplayName("the horizontal scroll bar appears when a line in view outgrows the view, and goes when it is cut back")
    void horizontalBarFollowsALineInView() {
        open(shortLines(LINES * 2));
        assertThat(scrollable(text.getHorizontalBar())).as("short lines leave nothing to scroll sideways to").isFalse();
        int line = 4;
        stage.focusAt(text.getOffsetAtLine(line) + text.getLine(line).length());
        stage.type("w".repeat(90));
        assertThat(scrollable(text.getHorizontalBar())).as("a line wider than the view").isTrue();
        answersAsAFreshEditor("after the line outgrew the view");
        int end = text.getOffsetAtLine(line) + text.getLine(line).length();
        text.replaceTextRange(end - 90, 90, "");
        assertThat(scrollable(text.getHorizontalBar())).as("the line cut back").isFalse();
        answersAsAFreshEditor("after the line was cut back");
    }

    @Test
    @DisplayName("a document opened with lines wider than the view can be scrolled sideways to their ends")
    void horizontalBarAtOpen() {
        for (int lines : new int[] { 40, LINES }) {
            open(wideLines(lines));
            int widest = 0;
            for (int line = text.getTopIndex(); line <= text.getLineIndex(text.getClientArea().height - 1); line++) {
                widest = Math.max(widest, text.getLocationAtOffset(text.getOffsetAtLine(line) + text.getLine(line).length()).x
                        + text.getHorizontalPixel());
            }
            assertThat(scrollable(text.getHorizontalBar())).as("%d lines: wider than the view at open", lines).isTrue();
            assertThat(text.getHorizontalBar().getMaximum()).as("%d lines: the range reaches the widest line in view", lines)
                    .isGreaterThanOrEqualTo(widest);
            answersAsAFreshEditor(lines + " lines: at open");
        }
    }

    @Test
    @DisplayName("a line far below that outgrew the others widens the horizontal scroll range once it is shown")
    void horizontalRangeFollowsALineFarBelow() {
        open(shortLines(LINES * 2));
        int at = text.getOffsetAtLine(800) + 5;
        text.replaceTextRange(at, 0, "w".repeat(300));
        answersAsAFreshEditor("after a line far below outgrew the view");
        text.setTopIndex(795);
        stage.settle();
        assertThat(scrollable(text.getHorizontalBar())).as("the widened line in view").isTrue();
        answersAsAFreshEditor("scrolled to the widened line");
    }

    @Test
    @DisplayName("scrolled sideways to the end of a long line, typing there")
    void typingScrolledSideways() {
        open(code(LINES));
        text.setHorizontalPixel(text.getHorizontalBar().getMaximum());
        stage.settle();
        int line = text.getTopIndex() + 2;
        stage.focusAt(text.getOffsetAtLine(line) + text.getLine(line).length());
        stage.type("tail");
        answersAsAFreshEditor("after typing at the end of a line scrolled to");
    }

    // ---------------- scrolling ----------------

    @Test
    @DisplayName("scrolled to the middle and to the end, typing at each")
    void typingAfterScrolling() {
        open(code(LINES));
        text.setTopIndex(600);
        stage.settle();
        answersAsAFreshEditor("scrolled to the middle");
        stage.focusAt(text.getOffsetAtLine(603) + 9);
        stage.type("x");
        stage.press(Key.ENTER);
        answersAsAFreshEditor("after typing in the middle");
        text.setTopIndex(text.getLineCount() - 5);
        stage.settle();
        int last = text.getLineCount() - 2;
        stage.focusAt(text.getOffsetAtLine(last) + 3);
        stage.press(Key.ENTER);
        answersAsAFreshEditor("after a new line at the end");
    }

    // ---------------- styles and line properties ----------------

    @Test
    @DisplayName("a line turned bold, out of view and in view")
    void boldLines() {
        open(code(LINES));
        int far = text.getOffsetAtLine(700);
        text.setStyleRange(new StyleRange(far, text.getLine(700).length(), null, null) {{ font = bold; }});
        answersAsAFreshEditor("after a line far below turned bold");
        int near = text.getOffsetAtLine(6);
        text.setStyleRange(new StyleRange(near, text.getLine(6).length(), null, null) {{ font = bold; }});
        answersAsAFreshEditor("after a line in view turned bold");
    }

    @Test
    @DisplayName("the current line moving, in view and far below it")
    void currentLineMoves() {
        open(code(LINES));
        moveCurrentLine(12);
        answersAsAFreshEditor("current line in view");
        moveCurrentLine(1100);
        answersAsAFreshEditor("current line far below");
    }

    // ---------------- selections ----------------

    @Test
    @DisplayName("typing over a selected word")
    void typingOverASelection() {
        open(code(LINES));
        int start = text.getOffsetAtLine(4) + 14;
        stage.focusAt(start);
        text.setSelection(start, start + 5);
        stage.settle();
        stage.type("q");
        answersAsAFreshEditor("after typing over five selected characters");
    }

    @Test
    @DisplayName("deleting a selection across several lines")
    void deletingASelectionAcrossLines() {
        open(code(LINES));
        int start = text.getOffsetAtLine(3) + 10;
        stage.focusAt(start);
        text.setSelection(start, text.getOffsetAtLine(8) + 4);
        stage.settle();
        stage.press(Key.BACKSPACE);
        answersAsAFreshEditor("after deleting five lines' worth");
    }

    @Test
    @DisplayName("Delete at the end of a line joins the next one")
    void forwardDeleteJoins() {
        open(code(LINES));
        stage.focusAt(text.getOffsetAtLine(6) + text.getLine(6).length());
        stage.press(Key.DELETE);
        answersAsAFreshEditor("after Delete at a line's end");
    }

    @Test
    @DisplayName("clicking after edits puts the caret on the offset clicked")
    void clickingAfterEdits() {
        open(code(LINES));
        stage.focusAt(text.getOffsetAtLine(2) + 4);
        stage.type("abc");
        stage.press(Key.ENTER);
        text.replaceTextRange(text.getOffsetAtLine(900), 0, "far\n");
        stage.settle();
        for (int line = text.getTopIndex() + 1; line < text.getTopIndex() + 8; line++) {
            int offset = text.getOffsetAtLine(line) + Math.min(6, text.getLine(line).length());
            stage.clickAtOffset(offset);
            assertThat(text.getCaretOffset()).as("a click on offset %d of line %d", offset, line).isEqualTo(offset);
        }
    }

    // ---------------- other layouts ----------------

    @Test
    @DisplayName("a word-wrapped editor, typing and adding lines")
    void wrappedEditor() {
        open(code(LINES / 3), SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        stage.focusAt(text.getOffsetAtLine(3) + 10);
        stage.type("w".repeat(60));
        answersAsAFreshEditor("after a line wrapped further");
        stage.press(Key.ENTER);
        answersAsAFreshEditor("after Enter in a wrapped line");
        text.replaceTextRange(text.getOffsetAtLine(300), 0, "inserted\n");
        answersAsAFreshEditor("after a line far below in a wrapped editor");
    }

    @Test
    @DisplayName("the line spacing and the tab width changed after edits")
    void layoutChangesAfterEdits() {
        open(code(LINES));
        stage.focusAt(text.getOffsetAtLine(4) + 9);
        stage.type("xy");
        text.setLineSpacing(4);
        answersAsAFreshEditor("after line spacing");
        text.setTabs(8);
        answersAsAFreshEditor("after a wider tab");
        text.replaceTextRange(text.getOffsetAtLine(700), 0, "\tmore\n");
        answersAsAFreshEditor("after an edit far below with both");
    }

    @Test
    @DisplayName("the font changed after edits")
    void fontChangeAfterEdits() {
        open(code(LINES));
        stage.focusAt(text.getOffsetAtLine(4) + 9);
        stage.type("xy");
        FontData data = text.getFont().getFontData()[0];
        Font larger = new Font(stage.display, data.getName(), data.getHeight() + 3, SWT.NORMAL);
        try {
            text.setFont(larger);
            answersAsAFreshEditor("after a larger font");
            text.replaceTextRange(text.getOffsetAtLine(700), 0, "more\n");
            answersAsAFreshEditor("after an edit far below in it");
        } finally {
            text.setFont(null);
            larger.dispose();
        }
    }

    @Test
    @DisplayName("the editor resized, then edited")
    void resizedEditor() {
        open(code(LINES));
        text.setSize(StyledTextFlutterStage.WIDTH - 120, StyledTextFlutterStage.HEIGHT - 60);
        stage.settle();
        answersAsAFreshEditor("after shrinking");
        text.replaceTextRange(text.getOffsetAtLine(800), 0, "x");
        answersAsAFreshEditor("after an edit far below in the smaller editor");
        text.setSize(StyledTextFlutterStage.WIDTH, StyledTextFlutterStage.HEIGHT);
        stage.settle();
        stage.focusAt(text.getOffsetAtLine(3) + 3);
        stage.type("z");
        answersAsAFreshEditor("after growing back and typing");
    }

    // ---------------- everything at once ----------------

    @Test
    @DisplayName("a run of random edits, restyles and scrolls")
    void randomEdits() {
        // On SWT 3.120 and older, an edit in a scrolled view moves the top pixel by whole lines and
        // leaves the vertical scroll bar where it was.
        assumeTrue(SWT.getVersion() > 4952, "an edit moves the view without its scroll bar on SWT 3.120 and older");
        open(code(LINES));
        Random random = new Random(5);
        String[] pieces = { "x", "yz", "\n", "\t", "\n    ", "w".repeat(120), "" };
        for (int i = 0; i < 20; i++) {
            int kind = random.nextInt(10);
            if (kind < 6) {
                int start = random.nextInt(text.getCharCount() + 1);
                int replaced = random.nextBoolean() ? 0 : random.nextInt(Math.min(30, text.getCharCount() - start) + 1);
                text.replaceTextRange(start, replaced, pieces[random.nextInt(pieces.length)]);
            } else if (kind < 8) {
                int line = random.nextInt(text.getLineCount());
                text.setStyleRange(new StyleRange(text.getOffsetAtLine(line), text.getLine(line).length(), null, null) {{ font = bold; }});
            } else if (kind < 9) {
                moveCurrentLine(random.nextInt(text.getLineCount()));
            } else {
                text.setTopIndex(random.nextInt(text.getLineCount()));
            }
            answersAsAFreshEditor("random step " + i);
        }
    }
}
