package org.eclipse.swt.widgets;

import dev.equo.swt.harness.UserInput;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An arrow key moves a focused Scale by its increment, once, with one Selection, as Win32's
 * trackbar (its line size) and GTK's range (its step increment) do, however the Scale got the
 * focus. A vertical Scale has its minimum at the bottom, so Up raises it. A KeyDown listener that
 * vetoes the arrow keeps the value.
 */
@Tag("flutter-it")
class ScaleArrowKeyFlutterTest {

    private static final int INCREMENT = 3;

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private final List<Integer> selections = new ArrayList<>();

    /**
     * A Scale at 50 of 0..100, focused by a click at {@code along} of its track (0.5 is the knob),
     * then set back to 50 by the application.
     */
    private Scale focusedScale(int style, double along) {
        Display display = flutter.boot();
        Shell shell = new Shell(display);
        shell.setLayout(new RowLayout());
        Scale scale = new Scale(shell, style);
        scale.setMinimum(0);
        scale.setMaximum(100);
        scale.setIncrement(INCREMENT);
        scale.setSelection(50);
        scale.addListener(SWT.Selection, e -> selections.add(scale.getSelection()));
        shell.setSize(400, 400);
        shell.open();
        flutter.settle();
        double[] rect = flutter.renderedRect(scale);
        boolean vertical = (style & SWT.VERTICAL) != 0;
        flutter.input().click(
                rect[0] + rect[2] * (vertical ? 0.5 : along),
                rect[1] + rect[3] * (vertical ? along : 0.5));
        flutter.settle();
        assertThat(display.getFocusControl()).isSameAs(scale);
        scale.setSelection(50);
        flutter.settle();
        selections.clear();
        return scale;
    }

    @AfterEach
    void shutdown() {
        flutter.teardown();
    }

    private void press(UserInput.Key key) {
        flutter.input().press(key);
        flutter.settle();
    }

    @Test
    void rightAndLeftStepAHorizontalScaleByItsIncrement() {
        Scale scale = focusedScale(SWT.HORIZONTAL, 0.25);

        press(UserInput.Key.RIGHT);
        assertThat(scale.getSelection()).isEqualTo(53);
        press(UserInput.Key.LEFT);

        assertThat(scale.getSelection()).isEqualTo(50);
        assertThat(selections).containsExactly(53, 50);
    }

    @Test
    void upAndDownStepAVerticalScaleByItsIncrement() {
        Scale scale = focusedScale(SWT.VERTICAL, 0.25);

        press(UserInput.Key.UP);
        assertThat(scale.getSelection()).isEqualTo(53);
        press(UserInput.Key.DOWN);

        assertThat(scale.getSelection()).isEqualTo(50);
        assertThat(selections).containsExactly(53, 50);
    }

    @Test
    void anArrowStepsAScaleFocusedByAClickOnItsKnob() {
        Scale scale = focusedScale(SWT.VERTICAL, 0.5);

        press(UserInput.Key.UP);

        assertThat(scale.getSelection()).isEqualTo(53);
        assertThat(selections).containsExactly(53);
    }

    @Test
    void aVetoedArrowKeepsTheValue() {
        Scale scale = focusedScale(SWT.HORIZONTAL, 0.25);
        scale.addListener(SWT.KeyDown, e -> e.doit = false);

        press(UserInput.Key.RIGHT);

        assertThat(scale.getSelection()).isEqualTo(50);
        assertThat(selections).isEmpty();
    }
}
