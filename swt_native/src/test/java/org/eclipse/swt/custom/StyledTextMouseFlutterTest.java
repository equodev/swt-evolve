package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pointer input on a StyledText: caret placement, multi-click and drag selection, wheel, mouse
 * events. Where SWT's own handling defines the result, the twin fed the same events is the oracle.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextMouseFlutterTest {

    static final String DOC = "alpha beta gamma delta\n"
            + "\tindented (words) here\n"
            + "short\n"
            + "The quick brown fox jumps over the lazy dog, then keeps running across the whole field "
            + "until the line wraps inside the editor at least once or twice more.\n"
            + StyledTextKeyboardFlutterTest.lines(40);

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

    /** Line starts and ends, after a tab, mid-word, and on wrapped rows. */
    @ParameterizedTest(name = "offset {0}")
    @ValueSource(ints = { 0, 3, 22, 24, 30, 47, 52, 110, 150 })
    @DisplayName("a click puts the caret where SWT locates the clicked point, and the screen shows it there")
    void clickPutsTheCaretAtTheClickedOffset(int offset) {
        fresh(SWT.MULTI | SWT.WRAP | SWT.V_SCROLL, DOC);
        Point point = stage.pointAt(text, offset);
        stage.clickAt(point, 1, 0);

        assertThat(text.getCaretOffset()).as("caret").isEqualTo(offset);
        assertThat(stage.renderedCarets(text)).as("shown caret").containsExactly(offset);
        assertThat(stage.subjectLog.count(ST.CaretMoved)).as("CaretMoved events").isLessThanOrEqualTo(1);
    }

    @Test
    @DisplayName("a click delivers MouseDown and MouseUp once, with the click's position and count")
    void clickDeliversMouseEvents() {
        fresh(SWT.MULTI, DOC);
        Point point = stage.pointAt(text, 8);
        stage.subjectLog.clear();
        stage.clickAt(point, 1, 0);

        assertThat(stage.subjectLog.ofType(SWT.MouseDown)).singleElement().satisfies(e -> {
            assertThat(e.button).isEqualTo(1);
            assertThat(e.count).isEqualTo(1);
            assertThat(e.x).isCloseTo(point.x, org.assertj.core.data.Offset.offset(1));
            assertThat(e.y).isCloseTo(point.y, org.assertj.core.data.Offset.offset(1));
        });
        assertThat(stage.subjectLog.ofType(SWT.MouseUp)).hasSize(1);
        assertThat(stage.subjectLog.count(SWT.FocusIn)).as("FocusIn").isEqualTo(1);
    }

    @Test
    @DisplayName("a Shift+click extends the selection from the caret to the clicked offset")
    void shiftClickExtendsTheSelection() {
        fresh(SWT.MULTI, DOC);
        stage.focusAt(6);
        stage.subjectLog.clear();
        stage.clickAt(stage.pointAt(text, 16), 1, SWT.SHIFT);

        assertThat(text.getSelection()).isEqualTo(new Point(6, 16));
        assertThat(stage.renderedSelections(text)).containsExactly(new Point(6, 16));
        assertThat(stage.subjectLog.ofType(SWT.Selection))
                .as("Selection events report the new range")
                .last().satisfies(e -> assertThat(new Point(e.x, e.y)).isEqualTo(new Point(6, 16)));
    }

    @ParameterizedTest(name = "{0} clicks at offset {1}")
    @org.junit.jupiter.params.provider.CsvSource({ "2, 8", "2, 26", "2, 60", "3, 8", "3, 60" })
    @DisplayName("a multi-click selects what SWT's mouse handling selects")
    void multiClickSelectsLikeSwt(int count, int offset) {
        fresh(SWT.MULTI | SWT.WRAP, DOC);
        Point point = stage.pointAt(text, offset);

        stage.mirrorSubjectIntoTwin();
        for (int i = 1; i <= count; i++) {
            stage.twin.notifyListeners(SWT.MouseDown, mouse(point, i));
            stage.twin.notifyListeners(SWT.MouseUp, mouse(point, i));
        }
        stage.settle();

        stage.subjectLog.clear();
        stage.clickAt(point, count, 0);

        assertThat(text.getSelection()).as("selection").isEqualTo(stage.twin.getSelection());
        assertThat(text.getCaretOffset()).as("caret").isEqualTo(stage.twin.getCaretOffset());
        Point selection = text.getSelection();
        assertThat(stage.renderedSelections(text)).as("shown selection").containsExactly(selection);
    }

    @Test
    @DisplayName("a double click delivers MouseDoubleClick at the clicked position, after the second MouseDown")
    void doubleClickEventHasPosition() {
        fresh(SWT.MULTI, DOC);
        Point point = stage.pointAt(text, 8);
        stage.subjectLog.clear();
        stage.clickAt(point, 2, 0);

        assertThat(stage.subjectLog.ofType(SWT.MouseDoubleClick)).singleElement().satisfies(e -> {
            assertThat(e.x).isCloseTo(point.x, org.assertj.core.data.Offset.offset(1));
            assertThat(e.y).isCloseTo(point.y, org.assertj.core.data.Offset.offset(1));
        });
        int secondDown = stage.subjectLog.lines.indexOf("MouseDown(button=1, count=2)");
        int doubleClick = stage.subjectLog.events.indexOf(stage.subjectLog.ofType(SWT.MouseDoubleClick).get(0));
        assertThat(secondDown).as("second MouseDown").isNotNegative();
        assertThat(doubleClick).as("MouseDoubleClick comes after the second MouseDown").isGreaterThan(secondDown);
    }

    @Test
    @DisplayName("an Alt/Option click adds a caret, as SWT's multi-selection does")
    void modifierClickAddsASelection() {
        fresh(SWT.MULTI, DOC);
        stage.focusAt(2);
        Point point = stage.pointAt(text, 30);

        stage.mirrorSubjectIntoTwin();
        stage.twin.notifyListeners(SWT.MouseDown, mouse(point, 1, SWT.MOD3));
        stage.twin.notifyListeners(SWT.MouseUp, mouse(point, 1, SWT.MOD3));
        stage.settle();

        stage.clickAt(point, 1, SWT.MOD3);

        assertThat(text.getSelectionRanges()).isEqualTo(stage.twin.getSelectionRanges());
        assertThat(stage.renderedCarets(text)).as("shown carets").hasSize(text.getSelectionRanges().length / 2);
    }

    @Test
    @DisplayName("a drag selects from the press to the release, and the last Selection event says so")
    void dragSelects() {
        fresh(SWT.MULTI, DOC);
        stage.focusAt(0);
        Point from = stage.pointAt(text, 6);
        Point to = stage.pointAt(text, 30);
        double[] a = stage.page(text, from.x, from.y);
        double[] b = stage.page(text, to.x, to.y);
        stage.subjectLog.clear();
        stage.separateFromLastClick();
        stage.input().drag(a[0], a[1], b[0], b[1], 8);
        stage.settle();

        assertThat(text.getSelection()).isEqualTo(new Point(6, 30));
        assertThat(stage.renderedSelections(text)).containsExactly(new Point(6, 30));
        assertThat(stage.subjectLog.ofType(SWT.Selection)).last()
                .satisfies(e -> assertThat(new Point(e.x, e.y)).isEqualTo(new Point(6, 30)));
    }

    @Test
    @DisplayName("a secondary click keeps the selection")
    void secondaryClickKeepsTheSelection() {
        fresh(SWT.MULTI, DOC);
        stage.focusAt(0);
        text.setSelection(6, 10);
        stage.pushState();
        Point inside = stage.pointAt(text, 8);
        double[] at = stage.page(text, inside.x, inside.y);
        stage.input().secondaryClick(at[0], at[1]);
        stage.settle();

        assertThat(text.getSelection()).isEqualTo(new Point(6, 10));
        assertThat(stage.renderedSelections(text)).containsExactly(new Point(6, 10));
    }

    @Test
    @DisplayName("in a CRLF document, a click lands on the clicked character")
    void crlfClickLandsOnTheCharacter() {
        fresh(SWT.MULTI, "one\r\ntwo\r\nthree");
        int offset = text.getOffsetAtLine(2) + 2;
        stage.clickAt(stage.pointAt(text, offset), 1, 0);

        assertThat(text.getCaretOffset()).isEqualTo(offset);
        assertThat(stage.renderedCarets(text)).containsExactly(offset);
    }

    @Test
    @DisplayName("a wheel tick delivers MouseWheel and scrolls both the widget and the screen")
    void wheelScrolls() {
        fresh(SWT.MULTI | SWT.V_SCROLL, DOC);
        Point middle = new Point(StyledTextFlutterStage.WIDTH / 2, StyledTextFlutterStage.HEIGHT / 2);
        double[] at = stage.page(text, middle.x, middle.y);
        stage.subjectLog.clear();
        stage.input().wheel(at[0], at[1], 0, 120, 0);
        stage.settle();

        assertThat(stage.subjectLog.count(SWT.MouseWheel)).as("MouseWheel events").isEqualTo(1);
        assertThat(text.getTopPixel()).as("scrolled").isPositive();
        assertThat(stage.renderedScroll(text)[1]).as("shown scroll").isEqualTo(text.getTopPixel());
        assertThat(text.getTopIndex()).as("top index").isEqualTo(text.getLineIndex(0));
    }

    @Test
    @DisplayName("a horizontal wheel tick scrolls a long unwrapped line")
    void horizontalWheelScrolls() {
        StringBuilder wide = new StringBuilder();
        for (int i = 0; i < 40; i++) wide.append("wide").append(i).append(' ');
        fresh(SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, wide + "\nshort");
        double[] at = stage.page(text, 50, 10);
        stage.input().wheel(at[0], at[1], 120, 0, 0);
        stage.settle();

        assertThat(text.getHorizontalPixel()).as("scrolled").isPositive();
        assertThat(stage.renderedScroll(text)[0]).as("shown scroll").isEqualTo(text.getHorizontalPixel());
    }

    @Test
    @DisplayName("a click in the empty area below the text puts the caret on the last line")
    void clickBelowTheTextGoesToTheLastLine() {
        fresh(SWT.MULTI, "one\ntwo");
        stage.clickAt(new Point(200, StyledTextFlutterStage.HEIGHT - 10), 1, 0);

        assertThat(text.getCaretOffset()).isEqualTo(text.getCharCount());
        assertThat(stage.renderedCarets(text)).containsExactly(text.getCharCount());
    }

    private static Event mouse(Point p, int count) {
        return mouse(p, count, 0);
    }

    private static Event mouse(Point p, int count, int stateMask) {
        Event e = new Event();
        e.button = 1;
        e.count = count;
        e.x = p.x;
        e.y = p.y;
        e.stateMask = stateMask;
        return e;
    }
}
