package org.eclipse.swt.widgets;

import dev.equo.swt.harness.UserInput;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tabbing into an editable Combo selects all of its text and getSelection() reports that range; typing then
 * replaces the text and leaves the caret after what was typed. Native SWT on Windows answers (0,7) with the caret
 * at 7 after a Tab into a Combo showing "Initial", and (1,1) once "x" is typed. An item picked from the list
 * replaces the text selected entirely, as Win32's combo box selects it.
 */
@Tag("flutter-it")
class ComboSelectionAfterTabFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Text start;
    private Combo combo;

    @BeforeEach
    void open() {
        Display display = flutter.boot();
        Shell shell = new Shell(display);
        shell.setLayout(new RowLayout(SWT.VERTICAL));
        start = new Text(shell, SWT.BORDER);
        start.setText("start");
        combo = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER);
        combo.setItems("Initial", "Other");
        combo.setText("Initial");
        shell.setSize(300, 200);
        shell.open();
        flutter.settle();
        double[] rect = flutter.renderedRect(start);
        flutter.input().click(rect[0] + rect[2] / 2, rect[1] + rect[3] / 2);
        flutter.settle();
        assertThat(display.getFocusControl()).isSameAs(start);
    }

    @AfterEach
    void shutdown() {
        flutter.teardown();
    }

    @Test
    void aTabIntoAnEditableComboSelectsAllOfItsText() {
        flutter.input().press(UserInput.Key.TAB);
        flutter.settle();

        assertThat(combo.getDisplay().getFocusControl()).isSameAs(combo);
        assertThat(combo.getSelection()).isEqualTo(new Point(0, 7));
        assertThat(combo.getCaretPosition()).isEqualTo(7);
    }

    @Test
    void typingAfterTheTabReplacesTheTextAndLeavesTheCaretAfterIt() {
        flutter.input().press(UserInput.Key.TAB);
        flutter.settle();
        flutter.input().type("x");
        flutter.settle();

        assertThat(combo.getText()).isEqualTo("x");
        assertThat(combo.getSelection()).isEqualTo(new Point(1, 1));
        assertThat(combo.getCaretPosition()).isEqualTo(1);
    }

    @Test
    void anItemPickedFromTheListAfterTheTabIsSelectedEntirely() {
        flutter.input().press(UserInput.Key.TAB);
        flutter.settle();
        flutter.input().press(UserInput.Key.DOWN, SWT.ALT);
        flutter.input().press(UserInput.Key.DOWN);
        flutter.input().press(UserInput.Key.DOWN);
        flutter.input().press(UserInput.Key.ENTER);
        flutter.settle();

        assertThat(combo.getText()).isEqualTo("Other");
        assertThat(combo.getSelection()).isEqualTo(new Point(0, 5));
        assertThat(combo.getCaretPosition()).isEqualTo(5);
    }
}
