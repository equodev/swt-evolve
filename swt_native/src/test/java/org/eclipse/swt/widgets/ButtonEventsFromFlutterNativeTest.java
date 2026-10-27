package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What a Selection or DefaultSelection arriving from Flutter does to a Button. In SWT a pressed button's
 * events reach only a live button that is not blocked by a modal shell, and a RADIO inside a
 * {@code NO_RADIO_GROUP} parent is selected on its own, leaving its siblings as they were.
 */
@Tag("native-unit")
class ButtonEventsFromFlutterNativeTest extends EventsFromFlutterNativeTestBase {

    @Test
    @DisplayName("a DefaultSelection from Flutter reaches the button's DefaultSelection listeners")
    void defaultSelectionReachesListeners() {
        Button button = new Button(DartMocks.dartShell(display), SWT.PUSH);
        List<Event> events = record(button, SWT.DefaultSelection);

        fromFlutter(button, "Selection/DefaultSelection");
        runQueued();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).widget).isSameAs(button);
    }

    @Test
    @DisplayName("a DefaultSelection that arrives after the button was disposed is dropped")
    void defaultSelectionToADisposedButtonIsDropped() {
        Button button = new Button(DartMocks.dartShell(display), SWT.PUSH);
        List<Event> events = record(button, SWT.DefaultSelection);

        fromFlutter(button, "Selection/DefaultSelection");
        button.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a DefaultSelection while a modal shell blocks the button's shell is dropped")
    void defaultSelectionBehindAModalShellIsDropped() {
        Shell shell = DartMocks.dartShell(display);
        Shell modal = mock(Shell.class);
        when(((DartShell) shell.getImpl()).getModalShell()).thenReturn(modal);
        Button button = new Button(shell, SWT.PUSH);
        List<Event> events = record(button, SWT.DefaultSelection);

        fromFlutter(button, "Selection/DefaultSelection");
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a click from Flutter selects a PUSH button without giving it a selection state")
    void clickOnAPushButtonSendsSelectionOnly() {
        Button button = new Button(DartMocks.dartShell(display), SWT.PUSH);
        List<Event> events = record(button, SWT.Selection);

        fromFlutter(button, "Selection/Selection");
        runQueued();

        assertThat(events).hasSize(1);
        assertThat(button.getSelection()).as("a PUSH button has no selection to toggle").isFalse();
    }

    @Test
    @DisplayName("a click from Flutter on a checked CHECK button clears it")
    void clickOnACheckedCheckButtonClearsIt() {
        Button button = new Button(DartMocks.dartShell(display), SWT.CHECK);
        button.setSelection(true);
        List<Event> events = record(button, SWT.Selection);

        fromFlutter(button, "Selection/Selection");
        runQueued();

        assertThat(button.getSelection()).isFalse();
        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("a click that arrives after the button was disposed is dropped")
    void clickOnADisposedButtonIsDropped() {
        Button button = new Button(DartMocks.dartShell(display), SWT.CHECK);
        List<Event> events = record(button, SWT.Selection);

        fromFlutter(button, "Selection/Selection");
        button.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a click on a RADIO in a NO_RADIO_GROUP parent selects it and leaves its siblings")
    void radioInANoRadioGroupParentLeavesItsSiblings() {
        Composite group = new Composite(DartMocks.dartShell(display), SWT.NO_RADIO_GROUP);
        Button first = new Button(group, SWT.RADIO);
        Button second = new Button(group, SWT.RADIO);
        first.setSelection(true);
        List<Event> secondHears = record(second, SWT.Selection);

        fromFlutter(second, "Selection/Selection");
        runQueued();

        assertThat(second.getSelection()).isTrue();
        assertThat(first.getSelection()).as("no radio group: the sibling keeps its selection").isTrue();
        assertThat(secondHears).hasSize(1);
    }
}
