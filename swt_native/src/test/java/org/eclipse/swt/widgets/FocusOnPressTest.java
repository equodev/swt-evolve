package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
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
        assertThat(focuses(new org.eclipse.swt.custom.CCombo(shell, SWT.NONE), 1)).isTrue();
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
        assertThat(focuses(new org.eclipse.swt.custom.CCombo(shell, SWT.NONE), 3)).isTrue();
        assertThat(focuses(new Tree(shell, SWT.NONE), 3)).isTrue();
        assertThat(focuses(new Button(shell, SWT.PUSH), 3)).isFalse();
        assertThat(focuses(new DateTime(shell, SWT.DATE), 3)).isFalse();
    }

    @Test
    @DisplayName("button 1 focuses a StyledText; button 3 leaves it to the StyledText's own press handling")
    void styledTextPress() {
        Shell shell = shell();
        assertThat(focuses(new StyledText(shell, SWT.NONE), 1)).isTrue();
        assertThat(focuses(new StyledText(shell, SWT.NONE), 3)).isFalse();
    }

    @Test
    @DisplayName("every other control takes the focus from the same buttons as before StyledText joined")
    void otherControlsUnchanged() {
        Shell shell = shell();
        Control[] focusedByButton1And3 = {new Text(shell, SWT.BORDER), new Combo(shell, SWT.DROP_DOWN),
                new Spinner(shell, SWT.NONE), new Table(shell, SWT.NONE), new Tree(shell, SWT.NONE),
                new org.eclipse.swt.custom.CCombo(shell, SWT.NONE)};
        Control[] focusedByButton1 = {new Button(shell, SWT.PUSH), new Button(shell, SWT.CHECK),
                new DateTime(shell, SWT.DATE), new org.eclipse.swt.widgets.List(shell, SWT.NONE),
                new Scale(shell, SWT.NONE)};
        Control[] focusedByNone = {new Label(shell, SWT.NONE), new Canvas(shell, SWT.NONE),
                new Composite(shell, SWT.NONE), new Group(shell, SWT.NONE), new Link(shell, SWT.NONE),
                new ProgressBar(shell, SWT.NONE), new Slider(shell, SWT.NONE), new TabFolder(shell, SWT.NONE),
                new org.eclipse.swt.custom.CLabel(shell, SWT.NONE), new org.eclipse.swt.custom.CTabFolder(shell, SWT.NONE)};
        for (Control c : focusedByButton1And3)
            assertThat(new boolean[] {focuses(c, 1), focuses(c, 2), focuses(c, 3)}).as(c.toString())
                    .containsExactly(true, false, true);
        for (Control c : focusedByButton1)
            assertThat(new boolean[] {focuses(c, 1), focuses(c, 2), focuses(c, 3)}).as(c.toString())
                    .containsExactly(true, false, false);
        for (Control c : focusedByNone)
            assertThat(new boolean[] {focuses(c, 1), focuses(c, 2), focuses(c, 3)}).as(c.toString())
                    .containsExactly(false, false, false);
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
