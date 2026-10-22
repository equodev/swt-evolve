package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Which controls a press of each button focuses natively, before its MouseDown reaches listeners. */
class FocusOnPressTest extends KeyRoutingTestBase {

    private static boolean focuses(Control control, int button) {
        return ControlHelper.takesFocusOnPress((DartControl) control.getImpl(), button);
    }

    @Test
    @DisplayName("button 1 focuses the buttons, fields, lists and item views")
    void primaryPress() {
        Shell shell = shell();
        assertThat(focuses(new Button(shell, SWT.PUSH), 1)).isTrue();
        assertThat(focuses(new Text(shell, SWT.BORDER), 1)).isTrue();
        assertThat(focuses(new DateTime(shell, SWT.DATE), 1)).isTrue();
        assertThat(focuses(new Table(shell, SWT.NONE), 1)).isTrue();
        assertThat(focuses(new Label(shell, SWT.NONE), 1)).isFalse();
    }

    @Test
    @DisplayName("button 3 focuses the fields and the item views, not a button or a DateTime")
    void secondaryPress() {
        Shell shell = shell();
        assertThat(focuses(new Text(shell, SWT.BORDER), 3)).isTrue();
        assertThat(focuses(new Combo(shell, SWT.DROP_DOWN), 3)).isTrue();
        assertThat(focuses(new Tree(shell, SWT.NONE), 3)).isTrue();
        assertThat(focuses(new Button(shell, SWT.PUSH), 3)).isFalse();
        assertThat(focuses(new DateTime(shell, SWT.DATE), 3)).isFalse();
    }

    @Test
    @DisplayName("the middle button focuses nothing, and NO_FOCUS refuses every press")
    void middleAndNoFocus() {
        Shell shell = shell();
        assertThat(focuses(new Text(shell, SWT.BORDER), 2)).isFalse();
        assertThat(focuses(new Text(shell, SWT.BORDER | SWT.NO_FOCUS), 1)).isFalse();
        assertThat(focuses(new Table(shell, SWT.NO_FOCUS), 3)).isFalse();
    }
}
