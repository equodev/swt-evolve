package org.eclipse.swt.widgets;

import java.nio.charset.StandardCharsets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** What a composition the client's input method reports does to a StyledText, through its IME. */
@Tag("native-unit")
class StyledTextImeFromFlutterNativeTest extends EventsFromFlutterNativeTestBase {

    private StyledText styledText(String text, int caret) {
        StyledText st = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        st.setBounds(0, 0, 200, 100);
        st.setText(text);
        st.setCaretOffset(caret);
        return st;
    }

    /** What the client sends on ImeComposition, applied as its handler does. */
    private static void report(StyledText st, String text, int caret, boolean commit) {
        String json = "{\"text\":\"" + text + "\",\"caret\":" + caret + (commit ? ",\"commit\":true" : "") + "}";
        ImeHelper.apply((DartCanvas) st.getImpl(), json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void a_composition_is_in_the_text_while_it_is_composed() {
        StyledText st = styledText("ab", 1);

        report(st, "n", 1, false);
        report(st, "ni", 2, false);

        assertThat(st.getText()).isEqualTo("anib");
        assertThat(st.getCaretOffset()).isEqualTo(3);
    }

    @Test
    void a_commit_leaves_the_committed_text_once() {
        StyledText st = styledText("ab", 1);

        report(st, "ni", 2, false);
        report(st, "に", 1, true);

        assertThat(st.getText()).isEqualTo("aにb");
        assertThat(st.getCaretOffset()).isEqualTo(2);
    }

    @Test
    void a_read_only_text_takes_no_composition() {
        StyledText st = styledText("ab", 1);
        st.setEditable(false);

        report(st, "ni", 2, false);

        assertThat(st.getText()).isEqualTo("ab");
    }
}
