package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** What a Dart-backed {@link Text} answers where SWT's platforms differ: one behaviour on every OS. */
@Tag("native-unit")
class TextConsistencyTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static Text text(int style) {
        return new Text(DartMocks.dartShell(), style);
    }

    @Test
    @DisplayName("a negative tab count is refused, as on Win32 and GTK")
    void negativeTabsAreRefused() {
        for (int style : new int[] {SWT.SINGLE, SWT.MULTI}) {
            Text text = text(style);
            text.setTabs(3);
            text.setTabs(-1);

            assertThat(text.getTabs()).isEqualTo(3);
            text.setTabs(0);
            assertThat(text.getTabs()).isZero();
        }
    }

    @Test
    @DisplayName("clearing the selection of a single-line text leaves the caret at its end, as on Win32 and GTK")
    void clearSelectionOfALineLeavesTheCaretAtTheEnd() {
        Text text = text(SWT.SINGLE);
        text.setText("Hello World");
        text.setSelection(2, 7);

        text.clearSelection();

        assertThat(text.getSelection()).isEqualTo(new Point(7, 7));
        assertThat(text.getSelectionText()).isEmpty();
    }

    @Test
    @DisplayName("clearing the selection of a multi-line text leaves the caret at its start, as on GTK and Cocoa")
    void clearSelectionOfAMultiLineTextLeavesTheCaretAtTheStart() {
        Text text = text(SWT.MULTI);
        text.setText("Hello World");
        text.setSelection(2, 7);

        text.clearSelection();

        assertThat(text.getSelection()).isEqualTo(new Point(2, 2));
        assertThat(text.getSelectionText()).isEmpty();
    }

    @Test
    @DisplayName("a negative text limit means no limit, as on Win32 and GTK")
    void aNegativeTextLimitMeansNoLimit() {
        for (int style : new int[] {SWT.SINGLE, SWT.MULTI}) {
            Text text = text(style);
            text.setTextLimit(7);

            text.setTextLimit(-1);

            assertThat(text.getTextLimit()).isEqualTo(Text.LIMIT);
        }
    }

    @Test
    @DisplayName("a password text answers the character it draws until another one is set, as on Win32 and GTK")
    void aPasswordTextAnswersItsEchoCharacter() {
        Text password = text(SWT.PASSWORD);
        assertThat(password.getEchoChar()).isEqualTo('•');

        password.setEchoChar('\0');
        assertThat(password.getEchoChar()).isEqualTo('\0');
        assertThat(text(SWT.SINGLE).getEchoChar()).isEqualTo('\0');
    }
}
