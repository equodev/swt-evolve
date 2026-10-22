package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.List;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** A selection set by the application, or forced by a new range, lets Verify see and refuse the new text. */
@Tag("native-unit")
class SpinnerProgrammaticVerifyTest {

    private Spinner spinner;
    private DartDisplay display;
    private final List<String> heard = new ArrayList<>();

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        Display api = DartMocks.dartDisplay();
        display = (DartDisplay) api.getImpl();
        doCallRealMethod().when(display).sendEvent(any(EventTable.class), any(Event.class));
        spinner = new Spinner(DartMocks.dartShell(api), SWT.NONE);
        spinner.setSelection(20);
        spinner.addListener(SWT.Verify, e -> heard.add("Verify " + e.text + " " + e.start + "-" + e.end));
        spinner.addListener(SWT.Modify, e -> heard.add("Modify " + spinner.getText()));
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    @Test
    void setSelectionSendsVerifyWithTheNewTextAndThenModify() {
        spinner.setSelection(45);

        assertThat(heard).containsExactly("Verify 45 0-2", "Modify 45");
    }

    @Test
    void raisingTheMinimumPastTheSelectionSendsVerifyAndModify() {
        spinner.setMinimum(30);

        assertThat(heard).containsExactly("Verify 30 0-2", "Modify 30");
    }

    @Test
    void aRefusedTextSendsNoModify() {
        spinner.addListener(SWT.Verify, e -> e.doit = false);

        spinner.setSelection(7);

        assertThat(heard).containsExactly("Verify 7 0-2");
        assertThat(spinner.getSelection()).as("the position moved, as the native stepper's does").isEqualTo(7);
        assertThat(spinner.getText()).as("the refused text was never written").isEqualTo("20");
    }

    @Test
    void aRefusedTextIsSentToTheClientUntilATextIsAccepted() {
        boolean[] refuse = {true};
        spinner.addListener(SWT.Verify, e -> e.doit = !refuse[0]);
        VSpinner value = ((DartSpinner) spinner.getImpl()).getValue();

        spinner.setSelection(7);
        assertThat(value.changedKeys()).contains(VSpinner.KEPT_TEXT);
        assertThat(value.getKeptText()).isEqualTo("20");
        heard.clear();
        spinner.setSelection(8);
        assertThat(heard).as("a second refusal is asked over the text still shown").containsExactly("Verify 8 0-2");

        refuse[0] = false;
        value.clearDirty();
        spinner.setSelection(9);
        assertThat(value.changedKeys()).contains(VSpinner.KEPT_TEXT);
        assertThat(value.getKeptText()).isNull();
        assertThat(spinner.getText()).isEqualTo("9");
    }

    @Test
    void theVerifiedTextCarriesTheDigits() {
        spinner.setMaximum(10000);
        spinner.setDigits(2);
        heard.clear();

        spinner.setSelection(1234);

        assertThat(heard.get(0)).isEqualTo("Verify 12" + SpinnerHelper.decimalSeparator((DartSpinner) spinner.getImpl()) + "34 0-4");
    }

    @Test
    void aSpinnerDisposedByItsVerifyListenerStopsThere() {
        spinner.addListener(SWT.Verify, e -> spinner.dispose());

        spinner.setSelection(7);

        assertThat(heard).containsExactly("Verify 7 0-2");
        assertThat(spinner.isDisposed()).isTrue();
    }
}
