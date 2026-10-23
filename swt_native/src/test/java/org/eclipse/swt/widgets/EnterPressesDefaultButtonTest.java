package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Enter runs the whole traversal, as on every native: it climbs from the focused control to the
 * shell, whose default button it presses. A control that keeps Enter (a multi-line Text), a Traverse
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
        Event key = new Event();
        key.keyCode = SWT.CR;
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
}
