package org.eclipse.swt.custom;

import dev.equo.swt.harness.UserInput.Key;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The StyledText listener contract under user input: which events fire, in which order, with which fields.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextEventsFlutterTest {

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

    @Test
    @DisplayName("releasing a key delivers KeyUp")
    void keyUpIsDelivered() {
        fresh(SWT.MULTI, "abc");
        stage.focusAt(0);
        stage.subjectLog.clear();
        stage.press(Key.RIGHT);

        assertThat(stage.subjectLog.ofType(SWT.KeyUp)).singleElement()
                .satisfies(e -> assertThat(e.keyCode).isEqualTo(SWT.ARROW_RIGHT));
    }

    @Test
    @DisplayName("a typed character's KeyDown carries SWT's keyCode and character")
    void keyDownFieldsAreSwts() {
        fresh(SWT.MULTI, "");
        stage.focusAt(0);
        stage.subjectLog.clear();
        stage.press(Key.of('Q'), SWT.SHIFT);

        assertThat(stage.subjectLog.ofType(SWT.KeyDown))
                .filteredOn(e -> e.character != 0)
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.keyCode).isEqualTo('q');
                    assertThat(e.character).isEqualTo('Q');
                    assertThat(e.stateMask & SWT.SHIFT).isEqualTo(SWT.SHIFT);
                });
    }

    @Test
    @DisplayName("Tab in an editable multi-line editor is offered as a traversal, refused, and typed")
    void tabInAMultiLineEditorIsTyped() {
        fresh(SWT.MULTI, "");
        stage.focusAt(0);
        stage.subjectLog.clear();
        stage.press(Key.TAB);

        assertThat(stage.subjectLog.ofType(SWT.Traverse)).singleElement().satisfies(e -> {
            assertThat(e.detail).isEqualTo(SWT.TRAVERSE_TAB_NEXT);
            assertThat(e.doit).isFalse();
        });
        assertThat(text.getText()).isEqualTo("\t");
        assertThat(text.isFocusControl()).isTrue();
    }

    @Test
    @DisplayName("Tab in a single-line editor moves focus away and types nothing")
    void tabInASingleLineEditorTraverses() {
        fresh(SWT.SINGLE, "one");
        stage.focusAt(3);
        stage.subjectLog.clear();
        stage.press(Key.TAB);

        assertThat(stage.subjectLog.ofType(SWT.Traverse)).singleElement().satisfies(e -> {
            assertThat(e.detail).isEqualTo(SWT.TRAVERSE_TAB_NEXT);
            assertThat(e.doit).isTrue();
        });
        assertThat(text.getText()).isEqualTo("one");
        assertThat(text.isFocusControl()).isFalse();
        assertThat(stage.renderedCarets(text)).as("no caret once focus left").isEmpty();
    }

    @Test
    @DisplayName("Tab in a read-only editor moves focus away")
    void tabInAReadOnlyEditorTraverses() {
        fresh(SWT.MULTI | SWT.READ_ONLY, "fixed");
        stage.focusAt(0);
        stage.press(Key.TAB);

        assertThat(text.getText()).isEqualTo("fixed");
        assertThat(text.isFocusControl()).isFalse();
    }

    @Test
    @DisplayName("Ctrl+Tab leaves a multi-line editor")
    void ctrlTabLeavesAMultiLineEditor() {
        fresh(SWT.MULTI, "");
        stage.focusAt(0);
        stage.press(Key.TAB, SWT.CTRL);

        assertThat(text.getText()).isEmpty();
        assertThat(text.isFocusControl()).isFalse();
    }

    @Test
    @DisplayName("a Traverse listener that allows Tab moves focus out of a multi-line editor")
    void traverseListenerCanAllowTab() {
        fresh(SWT.MULTI, "");
        text.addTraverseListener(e -> {
            if (e.detail == SWT.TRAVERSE_TAB_NEXT) e.doit = true;
        });
        stage.focusAt(0);
        stage.press(Key.TAB);

        assertThat(text.getText()).isEmpty();
        assertThat(text.isFocusControl()).isFalse();
    }

    @Test
    @DisplayName("extending the selection from the keyboard fires Selection with the new range")
    void keyboardSelectionFiresSelection() {
        fresh(SWT.MULTI, "hello world");
        stage.focusAt(0);
        stage.subjectLog.clear();
        stage.press(Key.RIGHT, SWT.SHIFT);
        stage.press(Key.RIGHT, SWT.SHIFT);

        assertThat(stage.subjectLog.ofType(SWT.Selection))
                .extracting(e -> new Point(e.x, e.y))
                .containsExactly(new Point(0, 1), new Point(0, 2));
    }

    @Test
    @DisplayName("each caret move fires CaretMoved with the new offset")
    void caretMovesFireCaretMoved() {
        fresh(SWT.MULTI, "hello");
        stage.focusAt(0);
        stage.subjectLog.clear();
        stage.press(Key.RIGHT);
        stage.press(Key.RIGHT);

        assertThat(stage.subjectLog.ofType(ST.CaretMoved)).extracting(e -> e.end).containsExactly(1, 2);
    }

    @Test
    @DisplayName("typing over a selection reports the replaced text in ExtendedModify")
    void extendedModifyReportsTheReplacedText() {
        fresh(SWT.MULTI, "alpha beta gamma");
        ExtendedModifyEvent[] seen = new ExtendedModifyEvent[1];
        text.addExtendedModifyListener(e -> seen[0] = e);
        stage.focusAt(0);
        text.setSelection(6, 10);
        stage.pushState();
        stage.type("x");

        assertThat(seen[0]).isNotNull();
        assertThat(seen[0].start).isEqualTo(6);
        assertThat(seen[0].length).isEqualTo(1);
        assertThat(seen[0].replacedText).isEqualTo("beta");
    }

    @Test
    @DisplayName("Backspace reports the removed range in Verify")
    void verifyReportsABackspace() {
        fresh(SWT.MULTI, "abc");
        VerifyEvent[] seen = new VerifyEvent[1];
        text.addVerifyListener(e -> seen[0] = e);
        stage.focusAt(3);
        stage.press(Key.BACKSPACE);

        assertThat(seen[0]).isNotNull();
        assertThat(seen[0].start).isEqualTo(2);
        assertThat(seen[0].end).isEqualTo(3);
        assertThat(seen[0].text).isEmpty();
    }

    @Test
    @DisplayName("a WordMovement listener decides where word navigation goes")
    void wordMovementListenerDecidesWordNavigation() {
        fresh(SWT.MULTI, "abcdefghij klm");
        text.addWordMovementListener(new MovementListener() {
            @Override
            public void getNextOffset(MovementEvent e) {
                e.newOffset = Math.min(e.offset + 2, e.lineText.length() + e.lineOffset);
            }

            @Override
            public void getPreviousOffset(MovementEvent e) {
                e.newOffset = Math.max(e.offset - 2, e.lineOffset);
            }
        });
        stage.focusAt(0);
        stage.pressAction(ST.WORD_NEXT);

        assertThat(text.getCaretOffset()).isEqualTo(2);
        assertThat(stage.renderedCarets(text)).containsExactly(2);
    }

    @Test
    @DisplayName("a word double-click follows the WordMovement listener")
    void doubleClickFollowsWordMovement() {
        fresh(SWT.MULTI, "abcdefghij klm");
        boolean[] asked = { false };
        MovementListener words = new MovementListener() {
            @Override
            public void getNextOffset(MovementEvent e) {
                asked[0] = true;
                if (e.movement == SWT.MOVEMENT_WORD_END) e.newOffset = Math.min(e.offset + 3, 10);
            }

            @Override
            public void getPreviousOffset(MovementEvent e) {
                asked[0] = true;
                if (e.movement == SWT.MOVEMENT_WORD_START) e.newOffset = Math.max(e.offset - 1, 0);
            }
        };
        text.addWordMovementListener(words);
        Point point = stage.pointAt(text, 4);

        // Where the word falls is StyledText.handleMouseDown's call, which differs between releases,
        // so the twin running the same release given the same double-click is the oracle.
        stage.mirrorSubjectIntoTwin();
        stage.twin.addWordMovementListener(words);
        for (int count = 1; count <= 2; count++) {
            Event e = new Event();
            e.button = 1;
            e.count = count;
            e.x = point.x;
            e.y = point.y;
            stage.twin.notifyListeners(SWT.MouseDown, e);
            stage.twin.notifyListeners(SWT.MouseUp, e);
        }
        stage.settle();
        asked[0] = false;

        stage.clickAt(point, 2, 0);

        assertThat(asked[0]).as("the listener was asked where the word is").isTrue();
        assertThat(text.getSelection()).isEqualTo(stage.twin.getSelection());
        assertThat(stage.renderedSelections(text)).containsExactly(text.getSelection());
    }

    @Test
    @DisplayName("a caret key a VerifyKey listener rejects leaves the caret alone")
    void vetoedArrowKeepsTheCaret() {
        fresh(SWT.MULTI, "hello");
        text.addVerifyKeyListener(e -> e.doit = e.keyCode != SWT.ARROW_RIGHT);
        stage.focusAt(2);
        stage.press(Key.RIGHT);

        assertThat(text.getCaretOffset()).isEqualTo(2);
        assertThat(stage.renderedCarets(text)).containsExactly(2);
    }

    @Test
    @DisplayName("a KeyDown listener sees the caret the key already moved")
    void keyDownListenerSeesTheMovedCaret() {
        fresh(SWT.MULTI, "hello");
        int[] seen = { -1 };
        text.addListener(SWT.KeyDown, e -> seen[0] = text.getCaretOffset());
        stage.focusAt(0);
        stage.press(Key.RIGHT);

        assertThat(seen[0]).isEqualTo(1);
    }

    @Test
    @DisplayName("the application's own event posted to the widget edits it, as on any platform")
    void postedKeyEditsTheWidget() {
        fresh(SWT.MULTI, "");
        stage.focusAt(0);
        Event e = StyledTextFlutterStage.keyEvent(Key.of('z'), 0);
        text.notifyListeners(SWT.KeyDown, e);
        stage.pushState();

        assertThat(text.getText()).isEqualTo("z");
        assertThat(stage.renderedLines(text)).containsExactly("z");
        assertThat(stage.renderedCarets(text)).containsExactly(1);
    }
}
