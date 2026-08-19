package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
@ExtendWith(Mocks.class)
class StyledTextVerifyKeyDispatchTest {

    private RecordingBridge bridge;
    private final Deque<Runnable> asyncQueue = new ArrayDeque<>();

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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        asyncQueue.clear();
    }

    private StyledText styledText() {
        Shell shell = Mocks.swtShell();
        Display display = shell.getDisplay();
        SwtDisplay displayImpl = (SwtDisplay) display.getImpl();
        doAnswer(inv -> {
            Event ev = inv.getArgument(1);
            if (ev != null && (ev.type == SWT.KeyDown || ev.type == SWT.KeyUp
                    || ev.type == SWT.Selection || ev.type == SWT.Verify
                    || ev.type == ST.VerifyKey)) {
                return inv.callRealMethod();
            }
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        StyledText st = new StyledText(shell, SWT.MULTI);
        st.setText("");
        st.setBounds(0, 0, 400, 300);
        pumpAsync();
        return st;
    }

    private void pumpAsync() {
        while (!asyncQueue.isEmpty()) {
            asyncQueue.poll().run();
        }
    }

    private void typeFromFlutter(StyledText st, char character) {
        int caret = st.getCaretOffset();

        Event key = new Event();
        key.keyCode = character;
        key.character = character;
        key.stateMask = 0;
        bridge.comm.fireContaining("/" + st.hashCode() + "/Key/KeyDown", key);
        pumpAsync();

        Event verify = new Event();
        verify.keyCode = character;
        verify.character = character;
        verify.stateMask = 0;
        bridge.comm.fireContaining("/" + st.hashCode() + "/VerifyKey/verifyKey", verify);
        pumpAsync();

        Event modify = new Event();
        modify.text = String.valueOf(character);
        modify.start = caret;
        modify.end = caret;
        bridge.comm.fireContaining("/" + st.hashCode() + "/Modify/Modify", modify);
        pumpAsync();
    }

    private static void installAutoCloseBrackets(StyledText st) {
        st.addVerifyKeyListener(event -> {
            if (!event.doit || event.character != '(')
                return;
            int offset = st.getCaretOffset();
            st.replaceTextRange(offset, 0, "()");
            st.setCaretOffset(offset + 1);
            event.doit = false;
        });
    }

    @Test
    @DisplayName("a printable keystroke notifies VerifyKey exactly once")
    void printableKeyNotifiesVerifyKeyOnce() {
        StyledText st = styledText();
        List<Character> verified = new ArrayList<>();
        st.addVerifyKeyListener(event -> verified.add(event.character));

        typeFromFlutter(st, '(');

        assertThat(verified)
                .as("ST.VerifyKey must fire once per keystroke; a VerifyKeyListener that edits the "
                        + "document (auto-close brackets) applies its edit once per notification")
                .containsExactly('(');
    }

    @Test
    @DisplayName("a keystroke no listener rejects still reaches the document")
    void acceptedKeyIsApplied() {
        StyledText st = styledText();
        installAutoCloseBrackets(st);

        typeFromFlutter(st, 'a');

        assertThat(st.getText())
                .as("the veto path must not swallow ordinary typing")
                .isEqualTo("a");
    }

    @Test
    @DisplayName("auto-close brackets insert a single pair for a single '('")
    void autoCloseInsertsOnePair() {
        StyledText st = styledText();
        installAutoCloseBrackets(st);

        typeFromFlutter(st, '(');

        assertThat(st.getText())
                .as("typing '(' in an editor with auto-close brackets yields '()' natively")
                .isEqualTo("()");
    }
}
