package org.eclipse.swt.custom;

import dev.equo.swt.harness.UserInput;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The focus leaves an editable CCombo for the control beside it, by Tab or by a click, and the CCombo
 * says so with its FocusOut, as SWT's does: its arrow is no Tab stop of its own. SWT fires that FocusOut
 * from a Display filter on the next control's FocusIn, so before that FocusIn's listeners.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CComboFocusFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;
    private CCombo combo;
    private Spinner spinner;
    private final List<String> focus = new ArrayList<>();
    private final List<String> activation = new ArrayList<>();

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 400, 200);
        shell.open();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @AfterEach
    void disposeControls() {
        for (Control child : shell.getChildren())
            child.dispose();
        flutter.settle();
    }

    private void focusTheCombo() {
        combo = new CCombo(shell, SWT.BORDER);
        combo.setItems(new String[] {"alpha", "bravo", "charlie"});
        combo.setBounds(20, 20, 200, 30);
        spinner = new Spinner(shell, SWT.BORDER);
        spinner.setBounds(250, 20, 100, 30);
        shell.forceFocus();
        flutter.settle();
        focus.clear();
        activation.clear();
        combo.addListener(SWT.FocusIn, e -> focus.add("CCombo.FocusIn"));
        combo.addListener(SWT.FocusOut, e -> focus.add("CCombo.FocusOut"));
        spinner.addListener(SWT.FocusIn, e -> focus.add("Spinner.FocusIn"));
        spinner.addListener(SWT.Activate, e -> activation.add("Spinner.Activate"));
        double[] rect = flutter.renderedRect(combo);
        flutter.input().click(rect[0] + rect[2] / 2, rect[1] + rect[3] / 2);
        flutter.settle();
        assertThat(combo.isFocusControl()).as("the click focused the CCombo").isTrue();
    }

    @Test
    void tab_moves_the_focus_from_the_field_to_the_next_control() {
        focusTheCombo();

        flutter.input().press(UserInput.Key.TAB);
        flutter.settle();

        assertThat(focus).containsExactly("CCombo.FocusIn", "CCombo.FocusOut", "Spinner.FocusIn");
        assertThat(activation).containsExactly("Spinner.Activate");
        assertThat(display.getFocusControl()).isSameAs(spinner);
    }

    @Test
    void tab_with_the_list_open_closes_it_and_moves_the_focus_on() {
        focusTheCombo();
        combo.setListVisible(true);
        flutter.settle();
        assertThat(combo.getListVisible()).as("the list opened").isTrue();
        List<Integer> traverseKeys = new ArrayList<>();
        combo.addListener(SWT.Traverse, e -> traverseKeys.add(e.keyCode));

        flutter.input().press(UserInput.Key.TAB);
        flutter.settle();

        assertThat(traverseKeys).as("the field's traverse(detail) carries no key").containsExactly(0);
        assertThat(combo.getListVisible()).isFalse();
        assertThat(focus).containsExactly("CCombo.FocusIn", "CCombo.FocusOut", "Spinner.FocusIn");
        assertThat(display.getFocusControl()).isSameAs(spinner);
    }

    @Test
    void a_click_on_the_next_control_is_the_combo_focus_out() {
        focusTheCombo();

        double[] rect = flutter.renderedRect(spinner);
        flutter.input().click(rect[0] + rect[2] / 3, rect[1] + rect[3] / 2);
        flutter.settle();

        assertThat(focus).containsExactly("CCombo.FocusIn", "CCombo.FocusOut", "Spinner.FocusIn");
        assertThat(activation).containsExactly("Spinner.Activate");
        assertThat(combo.isFocusControl()).isFalse();
    }
}
