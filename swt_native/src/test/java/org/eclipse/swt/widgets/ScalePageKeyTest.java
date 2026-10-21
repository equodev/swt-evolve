package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Page Down and Page Up move a Scale by its page increment, Home and End to its ends, each with a Selection. */
class ScalePageKeyTest extends KeyRoutingTestBase {

    private Scale scale() {
        Shell shell = shell();
        Scale scale = new Scale(new Composite(shell, SWT.NONE), SWT.HORIZONTAL);
        scale.setMinimum(0);
        scale.setMaximum(100);
        scale.setPageIncrement(10);
        scale.setSelection(50);
        return scale;
    }

    @Test
    @DisplayName("the page keys step by the page increment, Home and End jump to the ends, each a Selection")
    void pageHomeEndMoveTheScale() {
        Scale scale = scale();
        List<Integer> selections = new ArrayList<>();
        scale.addListener(SWT.Selection, e -> selections.add(scale.getSelection()));

        press(scale, SWT.PAGE_DOWN);
        press(scale, SWT.PAGE_UP);
        press(scale, SWT.HOME);
        press(scale, SWT.END);

        assertThat(selections).containsExactly(60, 50, 0, 100);
    }

    @Test
    @DisplayName("a KeyDown listener that vetoes the key keeps the value")
    void vetoedKeyMovesNothing() {
        Scale scale = scale();
        scale.addListener(SWT.KeyDown, e -> e.doit = false);

        press(scale, SWT.END);

        assertThat(scale.getSelection()).isEqualTo(50);
    }
}
