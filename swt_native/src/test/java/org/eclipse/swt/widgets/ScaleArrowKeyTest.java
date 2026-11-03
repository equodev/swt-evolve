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
 * The arrow keys step a Scale by its increment, each with a Selection, as Win32 and GTK do for a
 * key no listener vetoed. Right and Up raise a Scale, Left and Down lower it, as on Cocoa, whose
 * vertical Scale has its minimum at the bottom. With {@code scale_vertical_minimum_at_top} a
 * vertical Scale has its minimum at the top, as on GTK and Win32, and Up lowers it there.
 */
class ScaleArrowKeyTest extends KeyRoutingTestBase {

    private final List<Integer> selections = new ArrayList<>();

    private Scale scale(int style) {
        Scale scale = new Scale(new Composite(shell(), SWT.NONE), style);
        scale.setMinimum(0);
        scale.setMaximum(100);
        scale.setIncrement(3);
        scale.setSelection(50);
        scale.addListener(SWT.Selection, e -> selections.add(scale.getSelection()));
        return scale;
    }

    @AfterEach
    void minimumAtTheBottomAgain() {
        Config.getConfigFlags().scale_vertical_minimum_at_top = false;
    }

    @Test
    @DisplayName("Right and Left step a horizontal Scale by its increment, each a Selection")
    void horizontalArrowsStepByTheIncrement() {
        Scale scale = scale(SWT.HORIZONTAL);

        press(scale, SWT.ARROW_RIGHT);
        press(scale, SWT.ARROW_LEFT);
        press(scale, SWT.ARROW_LEFT);

        assertThat(selections).containsExactly(53, 50, 47);
    }

    @Test
    @DisplayName("Up raises a vertical Scale by its increment and Down lowers it")
    void verticalArrowsStepByTheIncrement() {
        Scale scale = scale(SWT.VERTICAL);

        press(scale, SWT.ARROW_UP);
        press(scale, SWT.ARROW_DOWN);
        press(scale, SWT.ARROW_DOWN);

        assertThat(selections).containsExactly(53, 50, 47);
    }

    @Test
    @DisplayName("with its minimum at the top, Down raises a vertical Scale and Up lowers it")
    void verticalArrowsWithTheMinimumAtTheTop() {
        Config.getConfigFlags().scale_vertical_minimum_at_top = true;
        Scale scale = scale(SWT.VERTICAL);

        press(scale, SWT.ARROW_DOWN);
        press(scale, SWT.ARROW_UP);
        press(scale, SWT.ARROW_UP);
        press(scale, SWT.ARROW_RIGHT);

        assertThat(selections).containsExactly(53, 50, 47, 50);
    }

    @Test
    @DisplayName("Up raises a horizontal Scale by its increment and Down lowers it, whatever the flag")
    void horizontalUpAndDownStepByTheIncrement() {
        Config.getConfigFlags().scale_vertical_minimum_at_top = true;
        Scale scale = scale(SWT.HORIZONTAL);

        press(scale, SWT.ARROW_UP);
        press(scale, SWT.ARROW_DOWN);
        press(scale, SWT.ARROW_DOWN);

        assertThat(selections).containsExactly(53, 50, 47);
    }

    @Test
    @DisplayName("an arrow at the end of the range, or with a modifier, moves nothing and sends no Selection")
    void anArrowThatMovesNothingSendsNoSelection() {
        Scale scale = scale(SWT.HORIZONTAL);
        scale.setSelection(100);

        press(scale, SWT.ARROW_RIGHT);
        press(scale, SWT.ARROW_LEFT, SWT.MOD1);

        assertThat(selections).isEmpty();
        assertThat(scale.getSelection()).isEqualTo(100);
    }

    @Test
    @DisplayName("a KeyDown listener that vetoes the arrow keeps the value")
    void vetoedArrowMovesNothing() {
        Scale scale = scale(SWT.HORIZONTAL);
        scale.addListener(SWT.KeyDown, e -> e.doit = false);

        press(scale, SWT.ARROW_RIGHT);

        assertThat(scale.getSelection()).isEqualTo(50);
    }
}
