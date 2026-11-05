package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Enter runs the whole traversal, as on every native: it climbs from the focused control to the
 * shell, whose default button it presses (a focused push button is the default while it holds the
 * focus). Keypad Enter is an Enter too. A control that keeps Enter (a multi-line Text), a Traverse
 * listener that vetoes it, or a shell with no usable default button leaves the key to be delivered.
 */
class EnterPressesDefaultButtonTest extends KeyRoutingTestBase {

    private Shell shell;
    private DartShell shellImpl;
    private DartDisplay displayImpl;
    private Button ok;
    private final List<String> okEvents = new ArrayList<>();

    @BeforeEach
    void dialogWithDefaultButton() {
        shell = shell();
        shellImpl = (DartShell) shell.getImpl();
        // A pressed button posts its Selection; deliver it as the event loop would.
        displayImpl = (DartDisplay) shell.getDisplay().getImpl();
        doAnswer(inv -> {
            Event e = inv.getArgument(0);
            ((DartWidget) e.widget.getImpl()).sendEvent(e);
            return null;
        }).when(displayImpl).postEvent(any(Event.class));
        when(shell.isVisible()).thenReturn(true);
        when(shellImpl.traverse(any(Event.class))).thenCallRealMethod();
        doCallRealMethod().when(shellImpl).traverseReturn();
        ok = new Button(shell, SWT.PUSH);
        ok.setText("Apply and Close");
        ok.addListener(SWT.Selection, e -> okEvents.add("Selection"));
        shellImpl.defaultButton = ok;
    }

    @AfterEach
    void stopDeliveringPostedEvents() {
        doNothing().when(displayImpl).postEvent(any(Event.class));
    }

    private static List<String> keyEventsOf(Control control) {
        List<String> events = new ArrayList<>();
        control.addListener(SWT.Traverse, e -> events.add("Traverse:" + e.detail));
        control.addListener(SWT.KeyDown, e -> events.add("KeyDown"));
        return events;
    }

    private static void pressEnter(Control control) {
        pressEnter(control, SWT.CR);
    }

    private static void pressEnter(Control control, int keyCode) {
        Event key = new Event();
        key.keyCode = keyCode;
        key.character = '\r';
        ControlHelper.routeKeyDown((DartControl) control.getImpl(), key);
    }

    @Test
    @DisplayName("Enter on a focused check box presses the shell's default button and is not delivered")
    void enterOnCheckBoxPressesDefaultButton() {
        Button check = new Button(shell, SWT.CHECK);
        List<String> events = keyEventsOf(check);

        pressEnter(check);

        assertThat(okEvents).containsExactly("Selection");
        assertThat(events).containsExactly("Traverse:" + SWT.TRAVERSE_RETURN);
    }

    @Test
    @DisplayName("a Traverse listener that vetoes Enter keeps the default button quiet; the key is delivered")
    void vetoedEnterIsDelivered() {
        Button check = new Button(shell, SWT.CHECK);
        check.addListener(SWT.Traverse, e -> e.doit = false);
        List<String> events = keyEventsOf(check);

        pressEnter(check);

        assertThat(okEvents).isEmpty();
        assertThat(events).containsExactly("Traverse:" + SWT.TRAVERSE_RETURN, "KeyDown");
    }

    @Test
    @DisplayName("Enter in a multi-line Text is the Text's own: no default button, the key is delivered")
    void enterInMultiLineTextStaysInTheText() {
        Text text = new Text(shell, SWT.MULTI);
        List<String> events = keyEventsOf(text);

        pressEnter(text);

        assertThat(okEvents).isEmpty();
        assertThat(events).containsExactly("Traverse:" + SWT.TRAVERSE_RETURN, "KeyDown");
    }

    @Test
    @DisplayName("Enter with a disabled default button is delivered")
    void enterWithDisabledDefaultButtonIsDelivered() {
        ok.setEnabled(false);
        Button check = new Button(shell, SWT.CHECK);
        List<String> events = keyEventsOf(check);

        pressEnter(check);

        assertThat(okEvents).isEmpty();
        assertThat(events).containsExactly("Traverse:" + SWT.TRAVERSE_RETURN, "KeyDown");
    }

    @Test
    @DisplayName("Enter with no default button is delivered")
    void enterWithNoDefaultButtonIsDelivered() {
        shellImpl.defaultButton = null;
        Button check = new Button(shell, SWT.CHECK);
        List<String> events = keyEventsOf(check);

        pressEnter(check);

        assertThat(events).containsExactly("Traverse:" + SWT.TRAVERSE_RETURN, "KeyDown");
    }

    @Test
    @DisplayName("keypad Enter presses the shell's default button too and is not delivered")
    void keypadEnterPressesDefaultButton() {
        Button check = new Button(shell, SWT.CHECK);
        List<String> events = keyEventsOf(check);

        pressEnter(check, SWT.KEYPAD_CR);

        assertThat(okEvents).containsExactly("Selection");
        assertThat(events).containsExactly("Traverse:" + SWT.TRAVERSE_RETURN);
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.SINGLE, SWT.SINGLE | SWT.READ_ONLY})
    @DisplayName("keypad Enter in a single-line Text presses the default button, as Return does")
    void keypadEnterInSingleLineTextPressesDefaultButton(int style) {
        Text text = new Text(shell, style);
        List<String> events = keyEventsOf(text);

        pressEnter(text, SWT.KEYPAD_CR);

        assertThat(okEvents).containsExactly("Selection");
        assertThat(events).containsExactly("Traverse:" + SWT.TRAVERSE_RETURN);
    }

    @Test
    @DisplayName("Enter on a focused push button presses that button, not the default one")
    void enterPressesTheFocusedPushButton() {
        doCallRealMethod().when(shellImpl).defaultFollowsFocus(any(Button.class), anyInt());
        Button cancel = new Button(shell, SWT.PUSH);
        List<String> cancelEvents = new ArrayList<>();
        cancel.addListener(SWT.Selection, e -> cancelEvents.add("Selection"));

        ((DartButton) cancel.getImpl()).sendFocusEvent(SWT.FocusIn);
        pressEnter(cancel);

        assertThat(cancelEvents).containsExactly("Selection");
        assertThat(okEvents).isEmpty();
    }

    @Test
    @DisplayName("Enter with a hidden default button is delivered")
    void enterWithHiddenDefaultButtonIsDelivered() {
        Button hidden = mock(Button.class);
        when(hidden.isVisible()).thenReturn(false);
        shellImpl.defaultButton = hidden;
        Button check = new Button(shell, SWT.CHECK);
        List<String> events = keyEventsOf(check);

        pressEnter(check);

        assertThat(events).containsExactly("Traverse:" + SWT.TRAVERSE_RETURN, "KeyDown");
    }

    @Test
    @DisplayName("Enter with a disposed default button is delivered")
    void enterWithDisposedDefaultButtonIsDelivered() {
        Button disposed = mock(Button.class);
        when(disposed.isDisposed()).thenReturn(true);
        shellImpl.defaultButton = disposed;
        Button check = new Button(shell, SWT.CHECK);
        List<String> events = keyEventsOf(check);

        pressEnter(check);

        assertThat(events).containsExactly("Traverse:" + SWT.TRAVERSE_RETURN, "KeyDown");
    }
}
