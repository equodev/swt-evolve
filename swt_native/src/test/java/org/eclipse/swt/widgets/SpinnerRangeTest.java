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

/**
 * Editing a spinner's range moves a selection that falls outside the new range to its nearest end, and its text
 * follows.
 */
@Tag("native-unit")
class SpinnerRangeTest {

    private Spinner spinner;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        spinner = new Spinner(DartMocks.dartShell(), SWT.NONE);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private int[] range() {
        return new int[] {spinner.getMinimum(), spinner.getMaximum(), spinner.getSelection()};
    }

    @Test
    @DisplayName("raising the minimum past the selection moves the selection and its text up")
    void raisingTheMinimumMovesTheSelection() {
        spinner.setSelection(20);

        spinner.setMinimum(30);

        assertThat(range()).containsExactly(30, 100, 30);
        assertThat(spinner.getText()).isEqualTo("30");
    }

    @Test
    @DisplayName("lowering the maximum below the selection moves the selection and its text down")
    void loweringTheMaximumMovesTheSelection() {
        spinner.setSelection(80);

        spinner.setMaximum(60);

        assertThat(range()).containsExactly(0, 60, 60);
        assertThat(spinner.getText()).isEqualTo("60");
    }

    @Test
    @DisplayName("a bound that keeps the selection inside the range leaves it where it is")
    void aBoundThatKeepsTheSelectionInsideLeavesIt() {
        spinner.setSelection(50);

        spinner.setMinimum(30);
        spinner.setMaximum(70);

        assertThat(range()).containsExactly(30, 70, 50);
        assertThat(spinner.getText()).isEqualTo("50");
    }

    @Test
    @DisplayName("a bound the range cannot take changes neither the range nor the selection")
    void aRejectedBoundChangesNothing() {
        spinner.setSelection(50);

        spinner.setMinimum(500);
        spinner.setMaximum(-5);

        assertThat(range()).containsExactly(0, 100, 50);
    }

    @Test
    @DisplayName("a maximum equal to the minimum is accepted and pins the selection to it")
    void maximumEqualToTheMinimumIsAccepted() {
        spinner.setMinimum(10);

        spinner.setMaximum(10);

        assertThat(range()).containsExactly(10, 10, 10);
    }
}
