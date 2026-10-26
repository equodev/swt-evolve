package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/** A paste sends Verify before it changes the text, and a veto leaves the text alone. */
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
    private Text field() {
        Shell shell = Mocks.shell();
        DartDisplay displayImpl = (DartDisplay) shell.getDisplay().getImpl();
        doAnswer(inv -> inv.callRealMethod()).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        Text text = new Text(new Composite(shell, SWT.NONE), SWT.SINGLE | SWT.BORDER);
        text.setText("original");
        text.setSelection(0, 2);
        text.copy();
        text.setSelection(2, 4);
        return text;
    }

    @Test
    void aVetoedPasteLeavesTheTextAlone() {
        Text text = field();
        text.addListener(SWT.Verify, e -> e.doit = false);
        text.paste();
        assertThat(text.getText()).isEqualTo("original");
    }

    @Test
    void aPasteInsertsTheTextTheListenerLeaves() {
        Text text = field();
        List<String> verified = new ArrayList<>();
        text.addListener(SWT.Verify, e -> {
            verified.add(e.text + " " + e.start + "-" + e.end);
            e.text = "X";
        });
        text.paste();
        assertThat(verified).containsExactly("or 2-4");
        assertThat(text.getText()).isEqualTo("orXinal");
    }
}
