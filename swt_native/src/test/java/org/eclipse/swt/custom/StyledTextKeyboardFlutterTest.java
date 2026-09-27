package org.eclipse.swt.custom;

import dev.equo.swt.harness.UserInput.Key;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every keystroke from the client must match the same key posted to a Java-driven twin running
 * upstream key handling: document, caret, selection, events, and what the screen shows.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextKeyboardFlutterTest {

    static final String DOC = "alpha beta gamma delta\n"
            + "\tindented (words) here\n"
            + "short\n"
            + "\n"
            + lines(40);

    static final String WRAPPED_DOC = "The quick brown fox jumps over the lazy dog, then keeps running across the "
            + "whole field until the line is long enough to wrap three or four times inside the editor "
            + "and some more words to be sure.\n"
            + "second logical line\n";

    private final StyledTextFlutterStage stage = new StyledTextFlutterStage();

    @BeforeAll
    void boot() {
        stage.boot();
    }

    @AfterAll
    void shutdown() {
        stage.shutdown();
    }

    static String lines(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append("line ").append(i).append(" lorem ipsum dolor sit\n");
        return sb.toString();
    }

    record Stroke(String name, Key key, int modifiers, boolean withSelection, boolean wrapped, Key then) {
        @Override
        public String toString() {
            return name;
        }
    }

    static List<Stroke> strokes() {
        int mod1 = SWT.MOD1, mod2 = SWT.MOD2, mod3 = SWT.MOD3;
        List<Stroke> list = new ArrayList<>();
        Key[] navigation = { Key.LEFT, Key.RIGHT, Key.UP, Key.DOWN, Key.HOME, Key.END, Key.PAGE_UP, Key.PAGE_DOWN };
        int[] navigationModifiers = { 0, mod2, mod1, mod1 | mod2, mod3, mod3 | mod2 };
        for (Key key : navigation) {
            for (int modifiers : navigationModifiers) {
                list.add(new Stroke(label(key, modifiers), key, modifiers, false, false, null));
            }
            list.add(new Stroke(label(key, 0) + " from a selection", key, 0, true, false, null));
            list.add(new Stroke(label(key, 0) + " on wrapped text", key, 0, false, true, null));
            list.add(new Stroke(label(key, mod2) + " on wrapped text", key, mod2, false, true, null));
        }
        for (Key key : new Key[] { Key.BACKSPACE, Key.DELETE }) {
            for (int modifiers : new int[] { 0, mod2, mod1, mod3 }) {
                list.add(new Stroke(label(key, modifiers), key, modifiers, false, false, null));
                list.add(new Stroke(label(key, modifiers) + " with a selection", key, modifiers, true, false, null));
            }
        }
        for (char c : new char[] { 'x', 'c', 'v', 'a' }) {
            list.add(new Stroke(label(Key.of(c), mod1), Key.of(c), mod1, false, false, null));
            list.add(new Stroke(label(Key.of(c), mod1) + " with a selection", Key.of(c), mod1, true, false, null));
        }
        list.add(new Stroke(label(Key.INSERT, mod2), Key.INSERT, mod2, false, false, null));
        list.add(new Stroke(label(Key.INSERT, mod1) + " with a selection", Key.INSERT, mod1, true, false, null));
        list.add(new Stroke("Insert, then a character", Key.INSERT, 0, false, false, Key.of('q')));
        for (char c : new char[] { 'x', 'Z', ' ', '(' }) {
            Key key = Key.of(c);
            int modifiers = key.toString().equals("Z") || c == '(' ? mod2 : 0;
            list.add(new Stroke("'" + c + "'", key, modifiers, false, false, null));
            list.add(new Stroke("'" + c + "' over a selection", key, modifiers, true, false, null));
        }
        list.add(new Stroke("Enter", Key.ENTER, 0, false, false, null));
        list.add(new Stroke("Enter over a selection", Key.ENTER, 0, true, false, null));
        list.add(new Stroke("Tab", Key.TAB, 0, false, false, null));
        list.add(new Stroke("Tab over a selection", Key.TAB, 0, true, false, null));
        list.add(new Stroke(label(Key.TAB, mod2), Key.TAB, mod2, false, false, null));
        list.add(new Stroke("Escape with a selection", Key.ESCAPE, 0, true, false, null));
        return list;
    }

    private static String label(Key key, int modifiers) {
        StringBuilder sb = new StringBuilder();
        if ((modifiers & SWT.CTRL) != 0) sb.append("Ctrl+");
        if ((modifiers & SWT.ALT) != 0) sb.append("Alt+");
        if ((modifiers & SWT.COMMAND) != 0) sb.append("Cmd+");
        if ((modifiers & SWT.SHIFT) != 0) sb.append("Shift+");
        return sb.append(key).toString();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("strokes")
    @DisplayName("a keystroke does what SWT's key handling does, and the screen shows it")
    void keystrokeMatchesSwt(Stroke stroke) {
        String text = stroke.wrapped() ? WRAPPED_DOC : DOC;
        stage.fresh(SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | (stroke.wrapped() ? SWT.WRAP : 0), text);
        StyledText subject = stage.subject;
        stage.setClipboardText("clip");

        // Start from a caret a user put there: a click on the second line, past the tab.
        int start = stroke.wrapped() ? 60 : subject.getOffsetAtLine(1) + 6;
        stage.focusAt(start);
        if (stroke.withSelection()) {
            subject.setSelection(start, start + 5);
            stage.pushState();
            assertThat(stage.renderedSelections(subject))
                    .as("precondition: the screen shows the selection the test set")
                    .containsExactly(new Point(start, start + 5));
        }

        stage.mirrorSubjectIntoTwin();
        stage.pressOnTwin(stroke.key(), stroke.modifiers());
        if (stroke.then() != null) stage.pressOnTwin(stroke.then(), 0);
        String clipboardAfterTwin = stage.clipboardText();

        stage.setClipboardText("clip");
        stage.subjectLog.clear();
        stage.press(stroke.key(), stroke.modifiers());
        if (stroke.then() != null) stage.press(stroke.then(), 0);

        // Every aspect is reported, so one defect shared by all keys does not hide the others.
        StyledText twin = stage.twin;
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(subject.getText()).as("document").isEqualTo(twin.getText());
        softly.assertThat(subject.getSelection()).as("selection").isEqualTo(twin.getSelection());
        softly.assertThat(subject.getCaretOffset()).as("caret").isEqualTo(twin.getCaretOffset());
        softly.assertThat(stage.clipboardText()).as("clipboard").isEqualTo(clipboardAfterTwin);
        softly.assertThat(stage.subjectLog.editingSequence())
                .as("listener events, in order")
                .containsExactlyElementsOf(stage.twinLog.editingSequence());
        softly.assertThat(subject.isFocusControl()).as("focus kept").isEqualTo(!stage.twinTraversed);
        assertShows(softly, subject);
        softly.assertAll();
    }

    @Test
    @DisplayName("the clipboard holds what SWT's cut leaves there")
    void cutLeavesTheSelectionOnTheClipboard() {
        stage.fresh(SWT.MULTI, DOC);
        StyledText subject = stage.subject;
        stage.focusAt(0);
        subject.setSelection(6, 10);
        stage.pushState();
        stage.setClipboardText("clip");

        stage.press(Key.of('x'), SWT.MOD1);

        assertThat(stage.clipboardText()).isEqualTo("beta");
        assertThat(subject.getText()).startsWith("alpha  gamma");
    }

    private void assertShows(SoftAssertions softly, StyledText subject) {
        List<String> expectedLines = new ArrayList<>();
        for (int i = 0; i < subject.getLineCount(); i++) expectedLines.add(subject.getLine(i));
        softly.assertThat(stage.renderedLines(subject)).as("shown document").containsExactlyElementsOf(expectedLines);
        // A traversal takes the focus, and the caret with it.
        if (subject.isFocusControl()) {
            softly.assertThat(stage.renderedCarets(subject)).as("shown caret").containsExactly(subject.getCaretOffset());
        } else {
            softly.assertThat(stage.renderedCarets(subject)).as("shown caret").isEmpty();
        }
        Point selection = subject.getSelection();
        if (selection.x == selection.y) {
            softly.assertThat(stage.renderedSelections(subject)).as("shown selection").isEmpty();
        } else {
            softly.assertThat(stage.renderedSelections(subject)).as("shown selection").containsExactly(selection);
        }
        double[] scroll = stage.renderedScroll(subject);
        softly.assertThat(scroll[1]).as("shown vertical scroll").isEqualTo((double) subject.getTopPixel());
        softly.assertThat(scroll[0]).as("shown horizontal scroll").isEqualTo((double) subject.getHorizontalPixel());
    }
}
