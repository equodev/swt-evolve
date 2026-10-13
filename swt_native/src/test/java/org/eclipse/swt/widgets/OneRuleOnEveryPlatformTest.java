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

/**
 * Values SWT's platforms read or refuse in different ways. Evolve behaves the same on every OS: as the
 * javadoc says where it says anything, otherwise as two of Win32, GTK and Cocoa agree.
 */
@Tag("native-unit")
class OneRuleOnEveryPlatformTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a label given several alignments is read LEFT first, then CENTER, as on Win32 and GTK")
    void severalAlignmentsAreReadLeftFirst() {
        Label label = new Label(shell, SWT.NONE);

        label.setAlignment(SWT.LEFT | SWT.CENTER | SWT.RIGHT);
        assertThat(label.getAlignment()).isEqualTo(SWT.LEFT);
        label.setAlignment(SWT.CENTER | SWT.RIGHT);
        assertThat(label.getAlignment()).isEqualTo(SWT.CENTER);
        label.setAlignment(-1);
        assertThat(label.getAlignment()).isEqualTo(SWT.LEFT);
    }

    @Test
    @DisplayName("a label given one alignment answers that one")
    void oneAlignmentIsReadAsGiven() {
        Label label = new Label(shell, SWT.NONE);

        assertThat(label.getAlignment()).isEqualTo(SWT.LEFT);
        label.setAlignment(SWT.RIGHT);
        assertThat(label.getAlignment()).isEqualTo(SWT.RIGHT);
        label.setAlignment(SWT.CENTER);
        assertThat(label.getAlignment()).isEqualTo(SWT.CENTER);
    }

    @Test
    @DisplayName("style bits with no alignment read LEFT")
    void noAlignmentBitReadsLeft() {
        assertThat(LabelHelper.alignmentOf(SWT.NONE)).isEqualTo(SWT.LEFT);
        assertThat(LabelHelper.alignmentOf(SWT.WRAP | SWT.BORDER)).isEqualTo(SWT.LEFT);
    }

    @Test
    @DisplayName("a separator has no alignment, as the javadoc says")
    void aSeparatorHasNoAlignment() {
        for (int style : new int[] {SWT.SEPARATOR | SWT.HORIZONTAL, SWT.SEPARATOR | SWT.VERTICAL | SWT.CENTER}) {
            Label separator = new Label(shell, style);
            separator.setAlignment(SWT.RIGHT);

            assertThat(separator.getAlignment()).isEqualTo(SWT.NONE);
        }
    }

    @Test
    @DisplayName("a negative tab count is refused, as on Win32 and GTK")
    void negativeTabsAreRefused() {
        for (int style : new int[] {SWT.SINGLE, SWT.MULTI}) {
            Text text = new Text(shell, style);
            text.setTabs(3);
            text.setTabs(-1);

            assertThat(text.getTabs()).isEqualTo(3);
            text.setTabs(0);
            assertThat(text.getTabs()).isZero();
        }
    }

    @Test
    @DisplayName("replacing the text puts the caret at its start, single-line or not, as on Win32 and GTK")
    void setTextPutsTheCaretAtTheStart() {
        for (int style : new int[] {SWT.SINGLE, SWT.MULTI}) {
            Text text = new Text(shell, style);
            text.setText("Hello World");
            text.setSelection(3, 6);

            text.setText("Hi");

            assertThat(text.getSelection()).isEqualTo(new Point(0, 0));
            assertThat(text.getCaretPosition()).isZero();
        }
    }

    @Test
    @DisplayName("clearing the selection of a single-line text leaves the caret at its end, as on Win32 and GTK")
    void clearSelectionOfALineLeavesTheCaretAtTheEnd() {
        Text text = new Text(shell, SWT.SINGLE);
        text.setText("Hello World");
        text.setSelection(2, 7);

        text.clearSelection();

        assertThat(text.getSelection()).isEqualTo(new Point(7, 7));
        assertThat(text.getSelectionText()).isEmpty();
    }

    @Test
    @DisplayName("clearing the selection of a multi-line text leaves the caret at its start, as on GTK and Cocoa")
    void clearSelectionOfAMultiLineTextLeavesTheCaretAtTheStart() {
        Text text = new Text(shell, SWT.MULTI);
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
            Text text = new Text(shell, style);
            text.setTextLimit(7);

            text.setTextLimit(-1);

            assertThat(text.getTextLimit()).isEqualTo(Text.LIMIT);
        }
    }

    @Test
    @DisplayName("a password text answers the character it draws until another one is set, as on Win32 and GTK")
    void aPasswordTextAnswersItsEchoCharacter() {
        Text password = new Text(shell, SWT.PASSWORD);
        assertThat(password.getEchoChar()).isEqualTo('•');

        password.setEchoChar('\0');
        assertThat(password.getEchoChar()).isEqualTo('\0');
        assertThat(new Text(shell, SWT.SINGLE).getEchoChar()).isEqualTo('\0');
    }

    @Test
    @DisplayName("a primary click's drag detection does not fail, as on Win32 and GTK")
    void dragDetectOnAPrimaryClickDoesNotFail() {
        Event click = new Event();
        click.button = 1;
        click.count = 1;

        assertThat(new Button(shell, SWT.PUSH).dragDetect(click)).isFalse();
        assertThat(new List(shell, SWT.SINGLE).dragDetect(click)).isFalse();
    }
}
