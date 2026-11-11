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

/**
 * The text alone does not say which API call made a change, and the platforms' controls undo each
 * differently: the client is told, per call, its kind, and for paste and cut, which the controls
 * record, what they replaced and inserted.
 */
@ExtendWith(Mocks.class)
class TextApiEditsTest {

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

    /** The edits the client would read now, as {kind, start, replaced, inserted}, without their numbers. */
    private static List<List<String>> read(Text text) {
        VText value = (VText) ((DartText) text.getImpl()).getValue();
        int[] edits = value.getEdits();
        String[] texts = value.getEditTexts();
        List<List<String>> out = new ArrayList<>();
        for (int i = 0, t = 0; i + 2 < edits.length; i += 3, t += 2) {
            out.add(List.of(Integer.toString(edits[i + 1]), Integer.toString(edits[i + 2]), texts[t], texts[t + 1]));
        }
        return out;
    }

    @Test
    @DisplayName("each call is logged, with its range and texts for paste and cut")
    void eachCallIsLogged() {
        Shell shell = Mocks.shell();
        Text text = new Text(shell, SWT.MULTI);
        read(text);
        text.setText("hello");
        text.append(" world");
        text.setSelection(0, 5);
        text.insert("HI");
        text.setSelection(0, 2);
        text.cut();
        text.setSelection(1, 3);
        text.paste();

        assertThat(read(text)).containsExactly(
                List.of("1", "0", "", ""),
                List.of("2", "5", "", ""),
                List.of("3", "0", "", ""),
                List.of("5", "0", "HI", ""),
                List.of("4", "1", "wo", "HI"));
    }

    @Test
    @DisplayName("setTextChars is logged as a set text")
    void setTextCharsIsLogged() {
        Shell shell = Mocks.shell();
        Text text = new Text(shell, SWT.SINGLE);
        read(text);
        text.setTextChars("abc".toCharArray());
        assertThat(read(text)).containsExactly(List.of("1", "0", "", ""));
    }

    @Test
    @DisplayName("what the client has read is dropped at the next edit, and only then")
    void readEditsAreDroppedAtTheNext() {
        Shell shell = Mocks.shell();
        Text text = new Text(shell, SWT.SINGLE);
        read(text);
        text.setText("a");
        assertThat(read(text)).hasSize(1);
        assertThat(read(text)).as("read again before anything new").hasSize(1);
        text.append("b");
        assertThat(read(text)).containsExactly(List.of("2", "1", "", ""));
    }

    @Test
    @DisplayName("a Text disposed by a Modify listener during setText logs no edit")
    void disposedDuringSetTextLogsNothing() {
        Shell shell = Mocks.shell();
        Text text = new Text(shell, SWT.SINGLE);
        DartText impl = (DartText) text.getImpl();
        impl.apiEdits.read();
        text.addListener(SWT.Modify, e -> text.dispose());

        try (Mocks.Delivery delivery = Mocks.deliverEvents(Mocks.display())) {
            text.setText("gone");
        }

        assertThat(text.isDisposed()).isTrue();
        assertThat(impl.apiEdits.read()).isEmpty();
    }
}
