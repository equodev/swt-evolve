package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * cut() asks the Segments listeners for the segments of the text it leaves, as Win32 and GTK do,
 * whether or not anything was selected to cut.
 */
@Tag("native-unit")
class TextCutSegmentsNativeTest extends EventsFromFlutterNativeTestBase {

    private Text text(int style, String content) {
        Text text = new Text(DartMocks.dartShell(display), style);
        text.setText(content);
        return text;
    }

    private static List<String> texts(List<Event> events) {
        return events.stream().map(e -> e.text).toList();
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.SINGLE, SWT.MULTI, SWT.SINGLE | SWT.PASSWORD})
    @DisplayName("each cut() with nothing selected reports the whole text, even while disabled")
    void cutWithNothingSelectedReportsTheText(int style) {
        Text text = text(style, "abcdef");
        List<Event> events = record(text, SWT.Segments);

        text.cut();
        text.cut();
        text.setEnabled(false);
        text.cut();

        assertThat(texts(events)).containsExactly("abcdef", "abcdef", "abcdef");
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.SINGLE, SWT.MULTI})
    @DisplayName("a cut() reports the text the cut leaves")
    void cutReportsTheRemainingText(int style) {
        Text text = text(style, "abcdef");
        text.setSelection(1, 3);
        List<Event> events = record(text, SWT.Segments);

        text.cut();

        assertThat(text.getText()).isEqualTo("adef");
        assertThat(texts(events)).containsExactly("adef");
    }

    @Test
    @DisplayName("a password text cuts nothing and reports the whole text")
    void passwordCutReportsTheWholeText() {
        Text text = text(SWT.SINGLE | SWT.PASSWORD, "abcdef");
        text.setSelection(1, 3);
        List<Event> events = record(text, SWT.Segments);

        text.cut();

        assertThat(text.getText()).isEqualTo("abcdef");
        assertThat(texts(events)).containsExactly("abcdef");
    }

    @Test
    @DisplayName("a read-only text cuts nothing and reports no Segments")
    void readOnlyCutReportsNoSegments() {
        Text text = text(SWT.SINGLE | SWT.READ_ONLY, "abcdef");
        text.setSelection(1, 3);
        List<Event> events = record(text, SWT.Segments);

        text.cut();

        assertThat(text.getText()).isEqualTo("abcdef");
        assertThat(events).isEmpty();
    }
}
