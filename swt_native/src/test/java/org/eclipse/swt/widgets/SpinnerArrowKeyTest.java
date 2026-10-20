package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Up, Down, Page Up and Page Down step a Spinner, after the KeyDown and only if no listener vetoed it: Verify, Modify, then a posted Selection. */
class SpinnerArrowKeyTest extends KeyRoutingTestBase {

    private Spinner spinner() {
        Shell shell = shell();
        Spinner spinner = new Spinner(new Composite(shell, SWT.NONE), SWT.BORDER);
        spinner.setValues(0, 0, 100, 0, 1, 10);
        return spinner;
    }

    @Test
    @DisplayName("Up then Down step the value, as Verify and Modify after the KeyDown (Selection is posted)")
    void arrowsStepTheValue() {
        Spinner spinner = spinner();
        List<String> events = new ArrayList<>();
        spinner.addListener(SWT.KeyDown, e -> events.add("KeyDown"));
        spinner.addListener(SWT.Verify, e -> events.add("Verify " + e.text + " " + e.start + ".." + e.end));
        spinner.addListener(SWT.Modify, e -> events.add("Modify"));

        press(spinner, SWT.ARROW_UP);
        assertThat(spinner.getSelection()).isEqualTo(1);
        press(spinner, SWT.ARROW_DOWN);
        assertThat(spinner.getSelection()).isEqualTo(0);

        assertThat(events).containsExactly(
                "KeyDown", "Verify 1 0..1", "Modify",
                "KeyDown", "Verify 0 0..1", "Modify");
    }

    @Test
    @DisplayName("a WRAP spinner wraps past either end to the other; a plain one stops there")
    void wrapWrapsPastTheEnds() {
        Spinner wrapping = new Spinner(new Composite(shell(), SWT.NONE), SWT.BORDER | SWT.WRAP);
        wrapping.setValues(0, 0, 100, 0, 1, 10);
        press(wrapping, SWT.ARROW_DOWN);
        assertThat(wrapping.getSelection()).isEqualTo(100);
        press(wrapping, SWT.ARROW_UP);
        assertThat(wrapping.getSelection()).isEqualTo(0);
        press(wrapping, SWT.PAGE_DOWN);
        assertThat(wrapping.getSelection()).isEqualTo(100);

        Spinner plain = spinner();
        press(plain, SWT.ARROW_DOWN);
        assertThat(plain.getSelection()).isEqualTo(0);
    }

    @Test
    @DisplayName("a Verify listener that rejects the step keeps the value")
    void rejectedVerifyKeepsTheValue() {
        Spinner spinner = spinner();
        spinner.addListener(SWT.Verify, e -> e.doit = false);

        press(spinner, SWT.ARROW_UP);

        assertThat(spinner.getSelection()).isEqualTo(0);
    }

    @Test
    @DisplayName("Page Up and Page Down step by the page increment; Home and End leave the value alone")
    void pageKeysStepByThePage() {
        Spinner spinner = spinner();

        press(spinner, SWT.PAGE_DOWN);
        assertThat(spinner.getSelection()).isEqualTo(0);
        press(spinner, SWT.PAGE_UP);
        assertThat(spinner.getSelection()).isEqualTo(10);
        press(spinner, SWT.HOME);
        press(spinner, SWT.END);
        assertThat(spinner.getSelection()).isEqualTo(10);
    }
}
