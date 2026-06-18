package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Reproduces issue #597: three RADIO buttons in the same Composite can end up
 * selected simultaneously, when a radio group must keep at most one selected.
 *
 * <p>The exclusivity logic lives in Java: when a Selection event arrives from
 * Flutter, {@code DartButton}'s handler toggles its own {@code selection} and
 * calls {@code sendSelection()}, which (for RADIO, when the parent does not
 * carry {@code SWT.NO_RADIO_GROUP}) runs {@code selectRadio()} to deselect the
 * sibling radios in the same parent. This test drives exactly that handler path
 * and asserts only one radio is selected after each "click".
 */
class ButtonRadioGroupTest extends SerializeTestBase {

    /**
     * Selecting a radio runs selectRadio() (the group logic a Selection event
     * triggers on every platform): it selects this radio and deselects the
     * siblings. Driven directly so the test is platform-agnostic.
     */
    private static void click(Button b) {
        ((DartButton) b.getImpl()).selectRadio();
    }

    private static long selectedCount(Button... buttons) {
        long n = 0;
        for (Button b : buttons) if (b.getSelection()) n++;
        return n;
    }

    @Test
    @DisplayName("only one radio in a Composite stays selected at a time")
    void radioGroupIsExclusive() {
        Shell shell = Mocks.swtShell();
        Composite group = new Composite(shell, SWT.NONE);

        Button r1 = new Button(group, SWT.RADIO);
        r1.setText("Option 1");
        Button r2 = new Button(group, SWT.RADIO);
        r2.setText("Option 2");
        Button r3 = new Button(group, SWT.RADIO);
        r3.setText("Option 3");

        // Sanity: the group really sees all three radios as siblings.
        assertThat(group.getImpl()._getChildren()).contains(r1, r2, r3);

        // Click the first radio: it becomes selected, the others stay unselected.
        click(r1);
        assertThat(r1.getSelection()).as("r1 after clicking r1").isTrue();
        assertThat(selectedCount(r1, r2, r3)).as("after clicking r1").isEqualTo(1);

        // Click the second radio: it must deselect the first.
        click(r2);
        assertThat(r2.getSelection()).as("r2 after clicking r2").isTrue();
        assertThat(r1.getSelection()).as("r1 must be deselected when r2 is chosen").isFalse();
        assertThat(selectedCount(r1, r2, r3)).as("after clicking r2").isEqualTo(1);

        // Click the third radio: it must deselect the second.
        click(r3);
        assertThat(r3.getSelection()).as("r3 after clicking r3").isTrue();
        assertThat(selectedCount(r1, r2, r3))
                .as("a radio group must never have more than one selection (issue #597)")
                .isEqualTo(1);
    }
}
