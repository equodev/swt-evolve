package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A keystroke reaches Java as the field's whole new text; its Verify names only the range it
 * replaces and the text typed into it, with the key that typed it, as Win32 and GTK report it.
 */
@ExtendWith(Mocks.class)
class TextTypedVerifyTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private Text field(String content) {
        Shell shell = Mocks.shell();
        DartDisplay displayImpl = (DartDisplay) shell.getDisplay().getImpl();
        doAnswer(inv -> inv.callRealMethod()).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        Text text = new Text(new Composite(shell, SWT.NONE), SWT.SINGLE | SWT.BORDER);
        text.setText(content);
        return text;
    }

    /** A key as the client reports it: the KeyDown, then the whole proposed text and the caret after it. */
    private static void type(Text text, char c, int keyCode, String proposed, int caret) {
        DartText impl = (DartText) text.getImpl();
        Event key = new Event();
        key.character = c;
        key.keyCode = keyCode;
        ControlHelper.routeKeyDown(impl, key);
        Event modify = new Event();
        modify.text = proposed;
        modify.start = caret;
        TextHelper.handleModify(impl, modify);
    }

    @Test
    @DisplayName("a typed character's Verify names the character, its key and the caret it lands at")
    void typedCharacterVerifiesOnlyItself() {
        Text text = field("abc");
        TextHelper.clientCaretMoved((DartText) text.getImpl(), 3, 3);
        List<Event> verifies = new ArrayList<>();
        List<Integer> modifies = new ArrayList<>();
        text.addListener(SWT.Verify, e -> verifies.add(copy(e)));
        text.addListener(SWT.Modify, e -> modifies.add(text.getCharCount()));

        type(text, 'x', 'x', "abcx", 4);

        assertThat(verifies).hasSize(1);
        Event v = verifies.get(0);
        assertThat(v.text).isEqualTo("x");
        assertThat(v.start).isEqualTo(3);
        assertThat(v.end).isEqualTo(3);
        assertThat(v.character).isEqualTo('x');
        assertThat(v.keyCode).isEqualTo('x');
        assertThat(modifies).containsExactly(4);
        assertThat(text.getText()).isEqualTo("abcx");
        assertThat(text.getCaretPosition()).isEqualTo(4);
    }

    @Test
    @DisplayName("typing over a selection replaces exactly that range")
    void typingOverASelectionReplacesIt() {
        Text text = field("hello");
        TextHelper.clientCaretMoved((DartText) text.getImpl(), 4, 1);
        List<Event> verifies = new ArrayList<>();
        text.addListener(SWT.Verify, e -> verifies.add(copy(e)));

        type(text, 'a', 'a', "hao", 2);

        assertThat(verifies).hasSize(1);
        assertThat(verifies.get(0).text).isEqualTo("a");
        assertThat(verifies.get(0).start).isEqualTo(1);
        assertThat(verifies.get(0).end).isEqualTo(4);
        assertThat(text.getText()).isEqualTo("hao");
    }

    @Test
    @DisplayName("Backspace verifies the removed range with no text, and its key")
    void backspaceVerifiesTheRemovedRange() {
        Text text = field("abc");
        TextHelper.clientCaretMoved((DartText) text.getImpl(), 3, 3);
        List<Event> verifies = new ArrayList<>();
        text.addListener(SWT.Verify, e -> verifies.add(copy(e)));

        type(text, SWT.BS, SWT.BS, "ab", 2);

        assertThat(verifies).hasSize(1);
        assertThat(verifies.get(0).text).isEmpty();
        assertThat(verifies.get(0).start).isEqualTo(2);
        assertThat(verifies.get(0).end).isEqualTo(3);
        assertThat(verifies.get(0).character).isEqualTo(SWT.BS);
        assertThat(text.getText()).isEqualTo("ab");
    }

    @Test
    @DisplayName("a Verify that vetoes the keystroke leaves the text as it was")
    void vetoedKeystrokeChangesNothing() {
        Text text = field("abc");
        TextHelper.clientCaretMoved((DartText) text.getImpl(), 3, 3);
        List<Integer> modifies = new ArrayList<>();
        text.addListener(SWT.Verify, e -> e.doit = Character.isDigit(e.character));
        text.addListener(SWT.Modify, e -> modifies.add(text.getCharCount()));

        type(text, 'x', 'x', "abcx", 4);

        assertThat(text.getText()).isEqualTo("abc");
        assertThat(modifies).isEmpty();
    }

    @Test
    @DisplayName("a Verify that rewrites the typed text applies the rewrite at the same place")
    void rewrittenKeystrokeIsApplied() {
        Text text = field("ab");
        TextHelper.clientCaretMoved((DartText) text.getImpl(), 2, 2);
        text.addListener(SWT.Verify, e -> e.text = e.text.toUpperCase());

        type(text, 'c', 'c', "abc", 3);

        assertThat(text.getText()).isEqualTo("abC");
    }

    /** An edit the client reports with no KeyDown before it, as a paste does. */
    private static void paste(Text text, String proposed, int caret) {
        Event modify = new Event();
        modify.text = proposed;
        modify.start = caret;
        TextHelper.handleModify((DartText) text.getImpl(), modify);
    }

    @Test
    @DisplayName("Backspace among repeated characters removes the one before the caret")
    void backspaceOverRepeatedCharacters() {
        Text text = field("aa");
        TextHelper.clientCaretMoved((DartText) text.getImpl(), 1, 1);
        List<Event> verifies = new ArrayList<>();
        text.addListener(SWT.Verify, e -> verifies.add(copy(e)));

        type(text, SWT.BS, SWT.BS, "a", 0);

        assertThat(verifies).hasSize(1);
        assertThat(verifies.get(0).start).isEqualTo(0);
        assertThat(verifies.get(0).end).isEqualTo(1);
        assertThat(text.getText()).isEqualTo("a");
        assertThat(text.getCaretPosition()).isEqualTo(0);
    }

    @Test
    @DisplayName("a paste's Verify carries no key")
    void pasteVerifiesWithoutAKey() {
        Text text = field("ab");
        TextHelper.clientCaretMoved((DartText) text.getImpl(), 2, 2);
        List<Event> verifies = new ArrayList<>();
        text.addListener(SWT.Verify, e -> verifies.add(copy(e)));

        paste(text, "abcd", 4);

        assertThat(verifies).hasSize(1);
        assertThat(verifies.get(0).text).isEqualTo("cd");
        assertThat(verifies.get(0).start).isEqualTo(2);
        assertThat(verifies.get(0).character).isEqualTo('\0');
        assertThat(verifies.get(0).keyCode).isZero();
    }

    @Test
    @DisplayName("a key typed into another control is not attached to this field's edit")
    void keyOfAnotherControlIsNotAttached() {
        Text other = field("");
        Text text = field("a");
        TextHelper.clientCaretMoved((DartText) text.getImpl(), 1, 1);
        List<Event> verifies = new ArrayList<>();
        text.addListener(SWT.Verify, e -> verifies.add(copy(e)));
        Event key = new Event();
        key.character = 'b';
        key.keyCode = 'b';
        ControlHelper.routeKeyDown((DartText) other.getImpl(), key);

        paste(text, "ab", 2);

        assertThat(verifies).hasSize(1);
        assertThat(verifies.get(0).character).isEqualTo('\0');
    }

    @Test
    @DisplayName("a key typed while no Verify listener exists is not attached to a later edit")
    void keyIsConsumedWithoutAVerifyListener() {
        Text text = field("");
        TextHelper.clientCaretMoved((DartText) text.getImpl(), 0, 0);
        type(text, 'x', 'x', "x", 1);
        List<Event> verifies = new ArrayList<>();
        text.addListener(SWT.Verify, e -> verifies.add(copy(e)));

        paste(text, "xx", 2);

        assertThat(verifies).hasSize(1);
        assertThat(verifies.get(0).character).isEqualTo('\0');
    }

    @Test
    @DisplayName("a paste past the text limit keeps what fits")
    void pastePastTheLimitIsTruncated() {
        Text text = field("ab");
        text.setTextLimit(4);
        TextHelper.clientCaretMoved((DartText) text.getImpl(), 2, 2);

        paste(text, "abcdef", 6);

        assertThat(text.getText()).isEqualTo("abcd");
        assertThat(text.getCaretPosition()).isEqualTo(4);
    }

    private static Event copy(Event e) {
        Event c = new Event();
        c.text = e.text;
        c.start = e.start;
        c.end = e.end;
        c.character = e.character;
        c.keyCode = e.keyCode;
        return c;
    }
}
