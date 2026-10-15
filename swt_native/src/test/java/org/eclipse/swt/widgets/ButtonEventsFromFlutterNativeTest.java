package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What a Selection or DefaultSelection arriving from Flutter does to a Button. Both hop through
 * {@code asyncExec}, so by the time they run the button may be gone or a modal shell may have opened;
 * in SWT a pressed button's events reach only a live button that is not blocked by a modal shell, and
 * a RADIO inside a {@code NO_RADIO_GROUP} parent is selected on its own, leaving its siblings as they were.
 */
@Tag("native-unit")
class ButtonEventsFromFlutterNativeTest {

    private RecordingBridge bridge;
    private Display display;
    /** The runnables a hook queued with asyncExec, run when the test says so. */
    private final List<Runnable> queued = new ArrayList<>();
    /** Events posted to the display queue; a Selection is posted, as in SWT, not sent. */
    private final List<Event> posted = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        // The mock display would swallow every event; its real dispatch only needs the listener table.
        DartDisplay impl = dartDisplay();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(invocation -> posted.add(invocation.getArgument(0))).when(impl).postEvent(any(Event.class));
    }

    @AfterEach
    void tearDown() {
        // The display mock is shared by the whole suite: hand it back as the other tests expect it.
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = dartDisplay();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doNothing().when(impl).postEvent(any(Event.class));
        FlutterBridge.set(null);
    }

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    private void fromFlutter(Button button, String event) {
        bridge.comm.fireContaining("Button/" + button.hashCode() + "/Selection/" + event, new Event());
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private List<Event> postedFor(Button button, int type) {
        return posted.stream().filter(e -> e.widget == button && e.type == type).toList();
    }

    /** Also what makes the button send or post the type at all: without a listener it does neither. */
    private static List<Event> record(Button button, int type) {
        List<Event> events = new ArrayList<>();
        button.addListener(type, events::add);
        return events;
    }

    @Test
    @DisplayName("a DefaultSelection from Flutter reaches the button's DefaultSelection listeners")
    void defaultSelectionReachesListeners() {
        Button button = new Button(DartMocks.dartShell(display), SWT.PUSH);
        List<Event> events = record(button, SWT.DefaultSelection);

        fromFlutter(button, "DefaultSelection");
        runQueued();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).widget).isSameAs(button);
    }

    @Test
    @DisplayName("a DefaultSelection that arrives after the button was disposed is dropped")
    void defaultSelectionToADisposedButtonIsDropped() {
        Button button = new Button(DartMocks.dartShell(display), SWT.PUSH);
        List<Event> events = record(button, SWT.DefaultSelection);

        fromFlutter(button, "DefaultSelection");
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

        fromFlutter(button, "DefaultSelection");
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a click from Flutter selects a PUSH button without giving it a selection state")
    void clickOnAPushButtonSendsSelectionOnly() {
        Button button = new Button(DartMocks.dartShell(display), SWT.PUSH);
        record(button, SWT.Selection);

        fromFlutter(button, "Selection");
        runQueued();

        assertThat(postedFor(button, SWT.Selection)).hasSize(1);
        assertThat(button.getSelection()).as("a PUSH button has no selection to toggle").isFalse();
    }

    @Test
    @DisplayName("a click from Flutter on a checked CHECK button clears it")
    void clickOnACheckedCheckButtonClearsIt() {
        Button button = new Button(DartMocks.dartShell(display), SWT.CHECK);
        button.setSelection(true);
        record(button, SWT.Selection);

        fromFlutter(button, "Selection");
        runQueued();

        assertThat(button.getSelection()).isFalse();
        assertThat(postedFor(button, SWT.Selection)).hasSize(1);
    }

    @Test
    @DisplayName("a click that arrives after the button was disposed is dropped")
    void clickOnADisposedButtonIsDropped() {
        Button button = new Button(DartMocks.dartShell(display), SWT.CHECK);
        record(button, SWT.Selection);

        fromFlutter(button, "Selection");
        button.dispose();
        runQueued();

        assertThat(postedFor(button, SWT.Selection)).isEmpty();
    }

    @Test
    @DisplayName("a click on a RADIO in a NO_RADIO_GROUP parent selects it and leaves its siblings")
    void radioInANoRadioGroupParentLeavesItsSiblings() {
        Composite group = new Composite(DartMocks.dartShell(display), SWT.NO_RADIO_GROUP);
        Button first = new Button(group, SWT.RADIO);
        Button second = new Button(group, SWT.RADIO);
        first.setSelection(true);
        record(second, SWT.Selection);

        fromFlutter(second, "Selection");
        runQueued();

        assertThat(second.getSelection()).isTrue();
        assertThat(first.getSelection()).as("no radio group: the sibling keeps its selection").isTrue();
        assertThat(postedFor(second, SWT.Selection)).hasSize(1);
    }
}
