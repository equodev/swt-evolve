package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Full-stack reproduction of a radio-group bug: a radio group where selecting a radio must
 * enable only its <em>own</em> associated sibling control and disable the others'. The reported
 * bug was that the enabled sibling control lagged one click behind the radio actually selected,
 * because {@code DartButton.selectRadio()} (web) dirtied the parent {@code Composite}
 * synchronously, racing the dirty mark that the radio's own Selection listener produces when it
 * flips a sibling control's enabled state in the same tick. The fix defers that parent dirty
 * mark via {@code Display.asyncExec(...)}.
 *
 * <p>Run via the {@code nativeTest} task, which compiles this against the whole-tree-Flutter Java
 * backend so the real {@code webMain} {@code DartButton.selectRadio()} runs.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest</pre>
 */
@Tag("flutter-it")
class RadioGroupListenerRaceFlutterTest {

    private WidgetFlutterHarness flutter;
    private Display display;
    private Shell shell;

    private Button radio1, radio2, radio3;
    private Button control2, control3;

    @BeforeEach
    void setUp() {
        flutter = new WidgetFlutterHarness();
        flutter.init();
        display = new Display();
        shell = new Shell(display);
        shell.setLayout(new GridLayout(1, false));

        Group group = new Group(shell, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        radio1 = new Button(group, SWT.RADIO);
        radio1.setText("Option 1");
        radio1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        radio2 = new Button(group, SWT.RADIO);
        radio2.setText("Option 2");
        control2 = new Button(group, SWT.FLAT);
        control2.setText("Action");
        control2.setEnabled(false);

        radio3 = new Button(group, SWT.RADIO);
        radio3.setText("Option 3");
        control3 = new Button(group, SWT.FLAT);
        control3.setText("Action");
        control3.setEnabled(false);

        radio1.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                control2.setEnabled(false);
                control3.setEnabled(false);
            }
        });
        radio2.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                control2.setEnabled(true);
                control3.setEnabled(false);
            }
        });
        radio3.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                control2.setEnabled(false);
                control3.setEnabled(true);
            }
        });

        shell.setSize(420, 220);
        flutter.show(shell);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    /** Mirrors the real click path: select the radio, then fire its Selection event
     *  (the same two steps a Flutter-originated click triggers on DartButton).
     *  {@code sendSelectionEvent} posts rather than dispatches (send=false), and the
     *  fix under test defers the parent's dirty mark via {@code asyncExec} too — both
     *  only run once the event loop is drained, exactly what the listener and the fix
     *  race against in the original bug. */
    private void select(Button radio) {
        DartButton impl = (DartButton) radio.getImpl();
        impl.selectRadio();
        impl.sendSelectionEvent(SWT.Selection);
        while (display.readAndDispatch()) {}
        flutter.flush();
    }

    @Test
    @DisplayName("only the just-selected radio's sibling control is rendered enabled (real Flutter)")
    void siblingControlEnablementNeverLagsBehind() {
        select(radio2);
        assertEnabled(control2, true, "control2 right after selecting radio2");
        assertEnabled(control3, false, "control3 right after selecting radio2");

        select(radio3);
        assertEnabled(control3, true, "control3 right after selecting radio3");
        assertEnabled(control2, false, "control2 right after selecting radio3 (must not still lag as enabled)");

        select(radio1);
        assertEnabled(control2, false, "control2 right after selecting radio1");
        assertEnabled(control3, false, "control3 right after selecting radio1");
    }

    private void assertEnabled(Button control, boolean expected, String message) {
        assertThat(flutter.renderedEnabled(control)).as(message).isEqualTo(expected);
    }
}