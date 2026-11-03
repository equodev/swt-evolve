package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Page Up and Page Down move a Scale by its page increment, Home and End to its ends, each with a
 * Selection. Page Up raises it, as Up does, except on a vertical Scale with its minimum at the top.
 */
class ScalePageKeyTest extends KeyRoutingTestBase {

    @AfterEach
    void minimumAtTheBottomAgain() {
        Config.getConfigFlags().scale_vertical_minimum_at_top = false;
    }

    private Scale scale() {
        return scale(SWT.HORIZONTAL);
    }

    private Scale scale(int style) {
        Shell shell = shell();
        Scale scale = new Scale(new Composite(shell, SWT.NONE), style);
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

        press(scale, SWT.PAGE_UP);
        press(scale, SWT.PAGE_DOWN);
        press(scale, SWT.HOME);
        press(scale, SWT.END);

        assertThat(selections).containsExactly(60, 50, 0, 100);
    }

    @Test
    @DisplayName("with its minimum at the top, Page Down raises a vertical Scale and Page Up lowers it")
    void pageKeysWithTheMinimumAtTheTop() {
        Config.getConfigFlags().scale_vertical_minimum_at_top = true;
        Scale scale = scale(SWT.VERTICAL);
        List<Integer> selections = new ArrayList<>();
        scale.addListener(SWT.Selection, e -> selections.add(scale.getSelection()));

        press(scale, SWT.PAGE_DOWN);
        press(scale, SWT.PAGE_UP);
        press(scale, SWT.PAGE_UP);

        assertThat(selections).containsExactly(60, 50, 40);
    }

    @Test
    @DisplayName("a page key pressed with a modifier, or Home at the minimum, moves nothing and sends no Selection")
    void aKeyThatMovesNothingSendsNoSelection() {
        Scale scale = scale();
        List<Integer> selections = new ArrayList<>();
        scale.addListener(SWT.Selection, e -> selections.add(scale.getSelection()));

        press(scale, SWT.PAGE_DOWN, SWT.MOD1);
        scale.setSelection(0);
        press(scale, SWT.HOME);

        assertThat(selections).isEmpty();
        assertThat(scale.getSelection()).isEqualTo(0);
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
