package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/** A paste sends Verify before it changes the text, a veto leaves the text alone, and a change sends one Modify. */
@ExtendWith(Mocks.class)
class TextPasteVerifyTest {

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

    /** "original" with "or" on the clipboard and "ig" selected. */
    private Text field(int style) {
        Shell shell = Mocks.shell();
        DartDisplay displayImpl = (DartDisplay) shell.getDisplay().getImpl();
        doAnswer(inv -> inv.callRealMethod()).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        Text text = new Text(new Composite(shell, SWT.NONE), style | SWT.BORDER);
        text.setText("original");
        text.setSelection(0, 2);
        text.copy();
        text.setSelection(2, 4);
        return text;
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.SINGLE, SWT.MULTI})
    void aVetoedPasteLeavesTheTextAlone(int style) {
        Text text = field(style);
        text.addListener(SWT.Verify, e -> e.doit = false);
        text.paste();
        assertThat(text.getText()).isEqualTo("original");
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.SINGLE, SWT.MULTI})
    void aPasteInsertsTheTextTheListenerLeaves(int style) {
        Text text = field(style);
        List<String> verified = new ArrayList<>();
        text.addListener(SWT.Verify, e -> {
            verified.add(e.text + " " + e.start + "-" + e.end);
            e.text = "X";
        });
        text.paste();
        assertThat(verified).containsExactly("or 2-4");
        assertThat(text.getText()).isEqualTo("orXinal");
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.SINGLE, SWT.MULTI})
    void aPasteThatChangesTheTextSendsOneModify(int style) {
        Text text = field(style);
        List<String> modified = new ArrayList<>();
        text.addListener(SWT.Modify, e -> modified.add(text.getText()));
        text.paste();
        assertThat(modified).containsExactly("ororinal");
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.SINGLE, SWT.MULTI})
    void aPasteThatChangesNothingSendsNoModify(int style) {
        Text text = field(style);
        text.setSelection(0, 2);
        List<String> modified = new ArrayList<>();
        text.addListener(SWT.Modify, e -> modified.add(text.getText()));
        text.paste();
        assertThat(text.getText()).isEqualTo("original");
        assertThat(modified).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.SINGLE, SWT.MULTI})
    void aVetoedPasteSendsNoModify(int style) {
        Text text = field(style);
        text.addListener(SWT.Verify, e -> e.doit = false);
        List<String> modified = new ArrayList<>();
        text.addListener(SWT.Modify, e -> modified.add(text.getText()));
        text.paste();
        assertThat(modified).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.SINGLE, SWT.MULTI})
    void aPasteKeepsWhatFitsTheTextLimit(int style) {
        Text text = field(style);
        text.setTextLimit(7);
        text.paste();
        assertThat(text.getText()).isEqualTo("oroinal");
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.SINGLE, SWT.MULTI})
    void anEditPastALimitTheTextAlreadyExceedsInsertsNothing(int style) {
        Text text = field(style);
        text.setTextLimit(4);
        assertThatCode(() -> text.append("xy")).doesNotThrowAnyException();
        assertThat(text.getText()).doesNotContain("xy");
    }
}
