package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A partial update carries only the properties it names, so the client keeps whatever it last heard
 * about the rest. A field that opens with a select-all - every JFace input dialog does, and the
 * refactoring Rename page is the reported one - therefore holds that range until an update names
 * {@code selection} again. The client re-applies the range it holds, so a keystroke that collapses
 * the selection on this side without saying so re-asserts the select-all over the caret.
 */
@ExtendWith(Mocks.class)
class TextSelectionUpdateTest {

    /** Delivery is recorded against a client, so a test that sends has to name one. */
    private static final int CLIENT = 1;

    private final Serializer serializer = new Serializer();

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
        Serializer.enterConnection(CLIENT);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a keystroke's update tells the client the select-all collapsed")
    void keystrokeNamesTheCollapsedSelection() {
        Text field = openedDialogField();
        DartText impl = (DartText) field.getImpl();

        typeFromClient(impl, "fwneono");

        String update = new String(serializer.toDiff(impl), StandardCharsets.UTF_8);
        assertThat(update)
                .as("the client holds the select-all and re-applies it; an update that does not "
                        + "name the selection leaves the whole field highlighted while typing")
                .contains("\"selection\"");
        assertThat(field.getSelection())
                .as("one keystroke past a select-all leaves a caret, not a selection")
                .isEqualTo(new org.eclipse.swt.graphics.Point(7, 7));
    }

    @Test
    @DisplayName("selecting a range names the selection too")
    void programmaticSelectionIsNamed() {
        Text field = openedDialogField();
        DartText impl = (DartText) field.getImpl();

        field.setSelection(2, 5);

        assertThat(new String(serializer.toDiff(impl), StandardCharsets.UTF_8))
                .contains("\"selection\"");
    }

    @Test
    @DisplayName("re-selecting the range the client holds is not an update")
    void reselectingTheSameRangeSaysNothing() {
        Text field = openedDialogField();
        DartText impl = (DartText) field.getImpl();

        field.selectAll();

        assertThat(impl.getValue().anyDirty())
                .as("nothing moved, so there is nothing to tell the client")
                .isFalse();
    }

    @Test
    @DisplayName("a word the user selects reaches Java, with the caret at its end")
    void clientSelectionIsRecordedWithItsCaret() {
        Text field = openedDialogField();
        DartText impl = (DartText) field.getImpl();

        TextHelper.clientCaretMoved(impl, 3, 0);

        assertThat(field.getSelection()).isEqualTo(new org.eclipse.swt.graphics.Point(0, 3));
        assertThat(field.getCaretPosition())
                .as("Win32 and GTK put the caret where the selection ends, after a double-click")
                .isEqualTo(3);
    }

    @Test
    @DisplayName("a range the application selects keeps the caret at its start")
    void programmaticSelectionKeepsTheCaretAtItsStart() {
        Text field = openedDialogField();

        field.setSelection(2, 5);

        assertThat(field.getCaretPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("selecting again the range the client last heard, after the user moved off it, is sent")
    void reselectingAfterTheUserMovedIsNamed() {
        Text field = openedDialogField();
        DartText impl = (DartText) field.getImpl();
        field.setSelection(2, 5);
        serializer.toDiff(impl);
        Serializer.markDelivered();

        TextHelper.clientCaretMoved(impl, 0, 0);
        field.setSelection(2, 5);

        assertThat(new String(serializer.toDiff(impl), StandardCharsets.UTF_8))
                .as("the client's caret moved on from the range it was last sent; naming that range "
                        + "again is a change for it, not a repeat")
                .contains("\"selection\"");
    }

    @Test
    @DisplayName("replacing the text names the selection it leaves behind")
    void setTextNamesTheSelectionItLeaves() {
        Text field = openedDialogField();
        DartText impl = (DartText) field.getImpl();

        field.setText("Hi");

        assertThat(new String(serializer.toDiff(impl), StandardCharsets.UTF_8))
                .as("the client holds the select-all of the old text and would apply it to the new one")
                .contains("\"selection\"");
    }

    @Test
    @DisplayName("a single-line text goes back to the start of the text that replaces it")
    void setTextGoesBackToTheStart() {
        Text field = fieldWithACaretPastTheNewText();

        field.setText("Hi");

        assertThat(field.getSelection()).isEqualTo(new org.eclipse.swt.graphics.Point(0, 0));
        assertThat(field.getCaretPosition()).isZero();
    }

    @Test
    @DisplayName("replacing the text with the selection it already has is not an update of the selection")
    void setTextKeepingTheSelectionDoesNotNameIt() {
        Text field = new Text(Mocks.shell(), SWT.MULTI);
        sendWhole(field);
        DartText impl = (DartText) field.getImpl();

        field.setText("Hi");

        assertThat(new String(serializer.toDiff(impl), StandardCharsets.UTF_8)).doesNotContain("\"selection\"");
    }

    @Test
    @DisplayName("a multi-line text goes back to the start when its text is replaced")
    void setTextOnAMultiLineTextNamesTheStart() {
        Shell shell = Mocks.shell();
        Text field = new Text(shell, SWT.MULTI);
        field.setText("Hello World");
        field.setSelection(3, 8);
        sendWhole(field);
        DartText impl = (DartText) field.getImpl();

        field.setText("Hi");

        assertThat(new String(serializer.toDiff(impl), StandardCharsets.UTF_8)).contains("\"selection\"");
        assertThat(field.getSelection()).isEqualTo(new org.eclipse.swt.graphics.Point(0, 0));
    }

    /** A field as a JFace input dialog leaves it: a name in it, all of it selected, client told. */
    private Text openedDialogField() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Text field = new Text(parent, SWT.SINGLE | SWT.BORDER);
        field.setText("Chicken");
        field.selectAll();
        sendWhole(field);
        return field;
    }

    /** A single-line field whose caret sits past the end of the two-character text that will replace it. */
    private Text fieldWithACaretPastTheNewText() {
        Text field = new Text(Mocks.shell(), SWT.SINGLE);
        field.setText("Hello World");
        field.setSelection(9);
        sendWhole(field);
        return field;
    }

    /** One keystroke as the client reports it: the whole proposed text, and where the caret ended. */
    private static void typeFromClient(DartText impl, String typed) {
        Event event = new Event();
        event.text = typed;
        event.start = typed.length();
        TextHelper.handleModify(impl, event);
    }

    private void sendWhole(Text field) {
        try {
            serializer.to(field);
            Serializer.markDelivered();
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }
}
