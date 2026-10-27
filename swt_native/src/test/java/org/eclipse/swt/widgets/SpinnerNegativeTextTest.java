package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** A negative value with digits is written with its sign in front of the whole part, as Win32 and GTK write it. */
@Tag("native-unit")
class SpinnerNegativeTextTest {

    private Spinner spinner;
    private String separator;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        spinner = new Spinner(DartMocks.dartShell(), SWT.NONE);
        spinner.setValues(105, Integer.MIN_VALUE, Integer.MAX_VALUE, 2, 1, 10);
        separator = spinner.getText().substring(1, 2);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private String text(int selection) {
        spinner.setSelection(selection);
        return spinner.getText();
    }

    @Test
    @DisplayName("a negative value smaller than one whole unit keeps a zero before the separator and the sign before it")
    void aFractionKeepsItsSignInFront() {
        assertThat(text(-5)).isEqualTo("-0" + separator + "05");
        assertThat(text(-50)).isEqualTo("-0" + separator + "50");
    }

    @Test
    @DisplayName("a negative value of whole units and more is written as its positive with a minus")
    void aLargerValueIsItsPositiveWithAMinus() {
        assertThat(text(-123)).isEqualTo("-1" + separator + "23");
        assertThat(text(Integer.MIN_VALUE)).isEqualTo("-21474836" + separator + "48");
    }
}
