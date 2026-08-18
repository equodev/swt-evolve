package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A VerifyListener may <em>rewrite</em> the text of an edit, not just veto it: JFace runs every
 * {@code IAutoEditStrategy} inside {@code TextViewer.verifyText} and hands the result back through
 * {@code VerifyEvent.text} (auto-indent, bracket auto-close, tabs-to-spaces). The Flutter editor
 * applies the keystroke optimistically and forwards it as a Modify, so the rewritten text — and the
 * caret position it implies — is only known after the widget has run its Verify listeners.
 */
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
@ExtendWith(Mocks.class)
class StyledTextAutoIndentVerifyTest {

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
                    || ev.type == SWT.Modify
                    || ev.type == org.eclipse.swt.custom.ST.VerifyKey)) {
                return inv.callRealMethod();
            }
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        StyledText st = new StyledText(shell, SWT.MULTI);
        st.setText("\tint alpha = 1;");
        st.setBounds(0, 0, 400, 300);
        pumpAsync();
        return st;
    }

    private void pumpAsync() {
        while (!asyncQueue.isEmpty()) {
            asyncQueue.poll().run();
        }
    }

    /**
     * The Modify the Flutter editor sends after applying a keystroke locally: a range replacement
     * carrying only what the render side inserted.
     */
    private void fireFlutterModify(StyledText st, int start, int end, String text) {
        Event e = new Event();
        e.start = start;
        e.end = end;
        e.text = text;
        bridge.comm.fireContaining("/" + st.hashCode() + "/Modify/Modify", e);
        pumpAsync();
    }

    /** JFace's DefaultIndentLineAutoEditStrategy, reduced to what it does to the VerifyEvent. */
    private void installAutoIndent(StyledText st) {
        st.addVerifyListener(e -> {
            if (!"\n".equals(e.text)) {
                return;
            }
            String upToCaret = st.getText().substring(0, e.start);
            int lineStart = upToCaret.lastIndexOf('\n') + 1;
            StringBuilder indent = new StringBuilder();
            for (int i = lineStart; i < upToCaret.length(); i++) {
                char c = upToCaret.charAt(i);
                if (c != ' ' && c != '\t') {
                    break;
                }
                indent.append(c);
            }
            e.text = e.text + indent;
        });
    }

    @Test
    @DisplayName("a Verify listener's rewritten text is what the document keeps")
    void rewrittenTextReachesTheDocument() {
        StyledText st = styledText();
        installAutoIndent(st);

        fireFlutterModify(st, 15, 15, "\n");

        assertThat(st.getText())
                .as("the auto-indent strategy's rewrite must be the text the document keeps")
                .isEqualTo("\tint alpha = 1;\n\t");
    }

    @Test
    @DisplayName("the caret follows the rewritten text, not the keystroke the client sent")
    void caretFollowsRewrittenText() {
        StyledText st = styledText();
        installAutoIndent(st);

        fireFlutterModify(st, 15, 15, "\n");

        assertThat(st.getCaretOffset())
                .as("the new line carries one tab, so the caret belongs after it (17), not "
                        + "between the newline and the indent (16) — typing on from 16 puts the "
                        + "user's text at column 0 with the indent stranded after it")
                .isEqualTo(17);
    }

}
