package org.eclipse.swt.custom;

import dev.equo.swt.harness.UserInput.Key;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Client-driven StyledText edits the keystroke oracle cannot express: vetoing listeners, focus,
 * line delimiters, surrogate pairs, input methods and the system clipboard.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextEditingFlutterTest {

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

    private void fresh(String content) {
        fresh(SWT.MULTI | SWT.V_SCROLL, content);
    }

    private void fresh(int style, String content) {
        stage.fresh(style, content);
        text = stage.subject;
    }

    private void assertShowsDocument() {
        List<String> expected = new ArrayList<>();
        for (int i = 0; i < text.getLineCount(); i++) expected.add(text.getLine(i));
        assertThat(stage.renderedLines(text)).as("shown document").containsExactlyElementsOf(expected);
    }

    private void assertShowsCaretAt(int offset) {
        assertThat(text.getCaretOffset()).as("caret").isEqualTo(offset);
        assertThat(stage.renderedCarets(text)).as("shown caret").containsExactly(offset);
    }

    // ---------------- typing ----------------

    @Test
    @DisplayName("typed characters reach the document once, and the caret follows them")
    void typedTextReachesTheDocumentOnce() {
        fresh("");
        stage.focusAt(0);
        stage.type("hello world");

        assertThat(text.getText()).isEqualTo("hello world");
        assertShowsCaretAt(11);
        assertShowsDocument();
        assertThat(stage.subjectLog.count(SWT.Modify)).as("one Modify per character").isEqualTo(11);
    }

    @Test
    @DisplayName("a Modify listener already reads the caret after the change")
    void modifyListenerReadsTheUpdatedCaret() {
        fresh("");
        List<Integer> seen = new ArrayList<>();
        text.addModifyListener(e -> seen.add(text.getCaretOffset()));
        stage.focusAt(0);
        stage.type("ab");

        assertThat(seen).containsExactly(1, 2);
    }

    @Test
    @DisplayName("typing fires no Selection event")
    void typingFiresNoSelection() {
        fresh("");
        stage.focusAt(0);
        stage.subjectLog.clear();
        stage.type("abc");

        assertThat(stage.subjectLog.count(SWT.Selection)).isZero();
    }

    @Test
    @DisplayName("a character a VerifyKey listener rejects is neither inserted nor shown")
    void vetoedCharacterIsNotShown() {
        fresh("");
        text.addVerifyKeyListener(e -> e.doit = e.character != 'x');
        stage.focusAt(0);
        stage.type("axb");

        assertThat(text.getText()).isEqualTo("ab");
        assertShowsDocument();
        assertShowsCaretAt(2);
    }

    @Test
    @DisplayName("an edit a Verify listener rejects leaves document, caret and screen alone")
    void verifyVetoLeavesEverythingAlone() {
        fresh("keep");
        text.addVerifyListener(e -> e.doit = false);
        stage.focusAt(4);
        stage.type("xy");
        stage.press(Key.BACKSPACE);

        assertThat(text.getText()).isEqualTo("keep");
        assertShowsDocument();
        assertShowsCaretAt(4);
    }

    @Test
    @DisplayName("text a Verify listener rewrites is what lands, and later keys land after it")
    void verifyRewriteKeepsLaterKeysInPlace() {
        fresh("");
        text.addVerifyListener(e -> {
            if ("\n".equals(e.text) || text.getLineDelimiter().equals(e.text)) e.text = e.text + "    ";
        });
        stage.focusAt(0);
        // One burst, as a fast typist produces it: nothing settles between the keys.
        stage.input().type("a\nbc");
        stage.settle();

        String delimiter = text.getLineDelimiter();
        assertThat(text.getText()).isEqualTo("a" + delimiter + "    bc");
        assertShowsCaretAt(text.getCharCount());
        assertShowsDocument();
    }

    @Test
    @DisplayName("an auto-closed bracket leaves the caret between the brackets, where the app put it")
    void autoCloseLeavesCaretInside() {
        fresh("");
        // What a JFace auto-edit strategy does: veto the key's edit, apply its own, place the caret.
        text.addVerifyListener(e -> {
            if ("(".equals(e.text)) {
                e.doit = false;
                text.replaceTextRange(e.start, e.end - e.start, "()");
                text.setSelection(e.start + 1);
            }
        });
        stage.focusAt(0);
        stage.type("(");
        stage.type("x");

        assertThat(text.getText()).isEqualTo("(x)");
        assertShowsCaretAt(2);
    }

    @Test
    @DisplayName("typing while the application restyles the document keeps every character in order")
    void typingWhileRestyledKeepsText() {
        fresh("");
        Color red = text.getDisplay().getSystemColor(SWT.COLOR_RED);
        text.addModifyListener(e -> text.getDisplay().asyncExec(() -> {
            if (text.isDisposed() || text.getCharCount() == 0) return;
            text.setStyleRange(new StyleRange(0, text.getCharCount(), red, null));
        }));
        stage.focusAt(0);
        stage.input().type("the quick brown fox");
        stage.settle();

        assertThat(text.getText()).isEqualTo("the quick brown fox");
        assertShowsCaretAt(19);
        assertShowsDocument();
    }

    @Test
    @DisplayName("a caret the application moves during an edit is where the next key lands")
    void programmaticCaretIsWhereTypingLands() {
        fresh("world");
        stage.focusAt(5);
        text.setCaretOffset(0);
        stage.pushState();
        assertShowsCaretAt(0);

        stage.type("x");

        assertThat(text.getText()).isEqualTo("xworld");
        assertShowsCaretAt(1);
    }

    @Test
    @DisplayName("a selection the application clears does not come back")
    void clearedSelectionStaysCleared() {
        fresh("hello world");
        stage.focusAt(0);
        text.setSelection(0, 5);
        stage.pushState();
        stage.press(Key.RIGHT);
        text.setStyleRange(new StyleRange(6, 5, text.getDisplay().getSystemColor(SWT.COLOR_BLUE), null));
        stage.pushState();

        assertThat(text.getSelectionCount()).isZero();
        assertThat(stage.renderedSelections(text)).as("shown selection").isEmpty();

        stage.type("x");
        assertThat(text.getText()).isEqualTo("hellox world");
    }

    @Test
    @DisplayName("the application's setText during an edit replaces the shown document and caret")
    void setTextDuringEditIsShown() {
        fresh("before");
        stage.focusAt(6);
        text.setText("after it");
        stage.pushState();

        assertShowsDocument();
        assertShowsCaretAt(0);
        stage.type("z");
        assertThat(text.getText()).isEqualTo("zafter it");
    }

    @Test
    @DisplayName("typing reaches a StyledText the application focused, without a click")
    void typingReachesAProgrammaticallyFocusedEditor() {
        fresh("");
        stage.clickOutside();
        text.setFocus();
        stage.pushState();
        stage.type("hi");

        assertThat(text.getText()).isEqualTo("hi");
        assertShowsCaretAt(2);
    }

    @Test
    @DisplayName("a text limit stops typed characters")
    void textLimitStopsTyping() {
        fresh("");
        text.setTextLimit(3);
        stage.focusAt(0);
        stage.type("abcd");

        assertThat(text.getText()).isEqualTo("abc");
        assertShowsDocument();
    }

    @Test
    @DisplayName("a read-only editor ignores typing")
    void readOnlyIgnoresTyping() {
        fresh(SWT.MULTI | SWT.READ_ONLY, "fixed");
        stage.focusAt(5);
        stage.type("x");
        stage.press(Key.BACKSPACE);

        assertThat(text.getText()).isEqualTo("fixed");
        assertShowsDocument();
    }

    @Test
    @DisplayName("Enter in a single-line editor inserts nothing")
    void enterInSingleLineInsertsNothing() {
        fresh(SWT.SINGLE, "one");
        stage.focusAt(3);
        stage.press(Key.ENTER);

        assertThat(text.getText()).isEqualTo("one");
    }

    // ---------------- focus ----------------

    @Test
    @DisplayName("leaving the editor changes neither document, styles, caret nor scroll")
    void leavingTheEditorChangesNothing() {
        fresh(lines(60));
        Font bold = new Font(text.getDisplay(), "Arial", 14, SWT.BOLD);
        try {
            StyleRange squiggle = new StyleRange();
            squiggle.start = 0;
            squiggle.length = 4;
            squiggle.underline = true;
            squiggle.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
            squiggle.underlineColor = text.getDisplay().getSystemColor(SWT.COLOR_RED);
            StyleRange font = new StyleRange();
            font.start = 5;
            font.length = 3;
            font.font = bold;
            text.setStyleRanges(new StyleRange[] { squiggle, font });
            stage.focusAt(text.getOffsetAtLine(1) + 2);
            stage.type("x");
            text.setTopIndex(10);
            stage.pushState();
            String document = text.getText();
            StyleRange[] styles = text.getStyleRanges();
            int caret = text.getCaretOffset();
            int top = text.getTopPixel();
            stage.subjectLog.clear();

            stage.clickOutside();

            assertThat(stage.subjectLog.count(SWT.Modify)).as("Modify events").isZero();
            assertThat(stage.subjectLog.count(SWT.Verify)).as("Verify events").isZero();
            assertThat(text.getText()).as("document").isEqualTo(document);
            assertThat(Arrays.asList(text.getStyleRanges())).as("styles").containsExactlyElementsOf(Arrays.asList(styles));
            assertThat(text.getCaretOffset()).as("caret").isEqualTo(caret);
            assertThat(text.getTopPixel()).as("scroll").isEqualTo(top);
            assertThat(stage.renderedScroll(text)[1]).as("shown scroll").isEqualTo(top);
        } finally {
            bold.dispose();
        }
    }

    @Test
    @DisplayName("leaving the editor fires FocusOut, and hides the caret")
    void leavingTheEditorFiresFocusOut() {
        fresh("abc");
        stage.focusAt(1);
        stage.subjectLog.clear();
        stage.clickOutside();

        assertThat(stage.subjectLog.count(SWT.FocusOut)).isEqualTo(1);
        assertThat(stage.renderedCarets(text)).isEmpty();
    }

    // ---------------- line delimiters ----------------

    @Test
    @DisplayName("in a CRLF document, a typed character lands at the clicked offset")
    void crlfTypingLandsAtTheCaret() {
        fresh("one\r\ntwo\r\nthree");
        stage.focusAt(text.getCharCount());
        assertThat(text.getCaretOffset()).as("precondition: caret at the end").isEqualTo(text.getCharCount());
        stage.type("X");

        assertThat(text.getText()).isEqualTo("one\r\ntwo\r\nthreeX");
        assertShowsCaretAt(text.getCharCount());
    }

    @Test
    @DisplayName("in a CRLF document, Enter inserts the widget's line delimiter")
    void crlfEnterInsertsTheWidgetDelimiter() {
        fresh("one\r\ntwo");
        stage.focusAt(3);
        stage.press(Key.ENTER);

        // StyledText.doContent inserts getLineDelimiter(), which DefaultContent answers with the
        // platform's line separator whatever the document holds.
        String delimiter = text.getLineDelimiter();
        assertThat(text.getText()).isEqualTo("one" + delimiter + "\r\ntwo");
        assertShowsCaretAt(3 + delimiter.length());
        assertShowsDocument();
    }

    @Test
    @DisplayName("in a CRLF document, a backspace at a line start joins the lines")
    void crlfBackspaceJoinsLines() {
        fresh("one\r\ntwo");
        stage.focusAt(5);
        stage.press(Key.BACKSPACE);

        assertThat(text.getText()).isEqualTo("onetwo");
        assertShowsCaretAt(3);
    }

    @Test
    @DisplayName("in a CRLF document, the shown selection covers the selected characters")
    void crlfSelectionIsShownOnTheRightCharacters() {
        fresh("one\r\ntwo\r\nthree");
        stage.focusAt(0);
        text.setSelection(10, 15);
        stage.pushState();

        assertThat(stage.renderedSelections(text)).containsExactly(new Point(10, 15));
    }

    @Test
    @DisplayName("in a CRLF document, leaving the editor keeps the delimiters")
    void crlfLeavingKeepsDelimiters() {
        fresh("one\r\ntwo");
        stage.focusAt(8);
        stage.type("s");
        stage.clickOutside();

        assertThat(text.getText()).isEqualTo("one\r\ntwos");
    }

    // ---------------- surrogate pairs ----------------

    @Test
    @DisplayName("Backspace after an emoji deletes the whole character")
    void backspaceDeletesAWholeSurrogatePair() {
        fresh("a😀b");
        stage.focusAt(4);
        stage.press(Key.LEFT);
        assertThat(text.getCaretOffset()).as("precondition: caret after the emoji").isEqualTo(3);
        stage.press(Key.BACKSPACE);

        assertThat(text.getText()).isEqualTo("ab");
        assertShowsCaretAt(1);
    }

    @Test
    @DisplayName("an arrow key steps over an emoji as one character")
    void arrowStepsOverASurrogatePair() {
        fresh("a😀b");
        stage.focusAt(0);
        stage.press(Key.RIGHT);
        stage.press(Key.RIGHT);

        assertShowsCaretAt(3);
    }

    // ---------------- input methods ----------------

    @Test
    @DisplayName("text composed with an input method is shown while composing and inserted once on commit")
    void imeCompositionIsShownAndCommittedOnce() {
        fresh("ab");
        stage.focusAt(1);
        stage.input().compose("n");
        stage.settle();
        stage.input().compose("ni");
        stage.settle();

        assertThat(stage.renderedLines(text)).as("shown while composing").containsExactly("anib");

        stage.input().commit("你");
        stage.settle();

        assertThat(text.getText()).isEqualTo("a你b");
        assertShowsDocument();
        assertShowsCaretAt(2);
    }

    @Test
    @DisplayName("an input method's composition reaches the widget's ImeComposition listeners")
    void imeCompositionReachesListeners() {
        fresh("");
        stage.focusAt(0);
        stage.subjectLog.clear();
        stage.input().compose("ka");
        stage.settle();
        stage.input().commit("か");
        stage.settle();

        assertThat(stage.subjectLog.ofType(SWT.ImeComposition))
                .extracting(e -> e.detail)
                .contains(SWT.COMPOSITION_CHANGED);
        assertThat(text.getText()).isEqualTo("か");
    }

    @Test
    @DisplayName("committed input-method text replaces the selection")
    void imeCommitReplacesTheSelection() {
        fresh("hello world");
        stage.focusAt(0);
        text.setSelection(6, 11);
        stage.pushState();
        stage.input().compose("sekai");
        stage.settle();
        stage.input().commit("世界");
        stage.settle();

        assertThat(text.getText()).isEqualTo("hello 世界");
        assertShowsCaretAt(8);
    }

    @Test
    @DisplayName("a read-only editor accepts no input-method text")
    void imeInReadOnlyIsRejected() {
        fresh(SWT.MULTI | SWT.READ_ONLY, "fixed");
        stage.focusAt(5);
        stage.input().compose("x");
        stage.input().commit("x");
        stage.settle();

        assertThat(text.getText()).isEqualTo("fixed");
        assertShowsDocument();
    }

    // ---------------- system clipboard ----------------

    @Test
    @DisplayName("text copied in another application is pasted once")
    void externalTextIsPastedOnce() {
        fresh("");
        stage.focusAt(0);
        stage.input().writeSystemClipboard("from outside");
        stage.press(Key.of('v'), SWT.MOD1);

        assertThat(text.getText()).isEqualTo("from outside");
        assertShowsCaretAt(12);
    }

    @Test
    @DisplayName("StyledText.paste() inserts text copied in another application")
    void pasteApiReadsTheSystemClipboard() {
        fresh("");
        stage.focusAt(0);
        stage.input().writeSystemClipboard("outside");
        text.paste();
        stage.pushState();

        assertThat(text.getText()).isEqualTo("outside");
        assertShowsDocument();
    }

    @Test
    @DisplayName("copying puts the selection on the system clipboard")
    void copyReachesTheSystemClipboard() {
        fresh("hello world");
        stage.focusAt(0);
        text.setSelection(0, 5);
        stage.pushState();
        stage.press(Key.of('c'), SWT.MOD1);

        assertThat(stage.input().readSystemClipboard()).isEqualTo("hello");
        assertThat(text.getText()).isEqualTo("hello world");
    }

    @Test
    @DisplayName("StyledText.copy() puts the selection on the system clipboard")
    void copyApiReachesTheSystemClipboard() {
        fresh("hello world");
        text.setSelection(6, 11);
        text.copy();
        stage.pushState();

        assertThat(stage.input().readSystemClipboard()).isEqualTo("world");
    }

    private static String lines(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append("line ").append(i).append(" text\n");
        return sb.toString();
    }
}
