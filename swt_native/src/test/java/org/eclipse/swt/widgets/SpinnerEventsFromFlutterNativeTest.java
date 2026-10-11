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
 * What an event arriving from Flutter does to a Spinner. Every one hops through {@code asyncExec}, so by the time
 * it runs the spinner may be gone or a modal shell may have opened: Modify and Verify reach the listeners of a
 * live spinner, a Selection moves the value (clamped to the range), and a DefaultSelection moves it and reaches
 * the listeners of a spinner that nothing blocks.
 */
@Tag("native-unit")
class SpinnerEventsFromFlutterNativeTest {

    private RecordingBridge bridge;
    private Display display;
    /** The runnables a hook queued with asyncExec, run when the test says so. */
    private final List<Runnable> queued = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        // The mock display would swallow every event; its real dispatch only needs the listener table.
        DartDisplay impl = dartDisplay();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        // The display mock is shared by the whole suite: hand it back as the other tests expect it.
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = dartDisplay();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    private void fromFlutter(Spinner spinner, String channel, int index) {
        Event e = new Event();
        e.index = index;
        bridge.comm.fireContaining("Spinner/" + spinner.hashCode() + "/" + channel, e);
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static List<Event> record(Spinner spinner, int type) {
        List<Event> events = new ArrayList<>();
        spinner.addListener(type, events::add);
        return events;
    }

    private Spinner spinner() {
        return new Spinner(DartMocks.dartShell(display), SWT.NONE);
    }

    @Test
    @DisplayName("a Modify from Flutter reaches the spinner's Modify listeners")
    void modifyReachesListeners() {
        Spinner spinner = spinner();
        List<Event> events = record(spinner, SWT.Modify);

        fromFlutter(spinner, "Modify/Modify", 0);
        runQueued();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).widget).isSameAs(spinner);
    }

    @Test
    @DisplayName("a Modify that arrives after the spinner was disposed is dropped")
    void modifyToADisposedSpinnerIsDropped() {
        Spinner spinner = spinner();
        List<Event> events = record(spinner, SWT.Modify);

        fromFlutter(spinner, "Modify/Modify", 0);
        spinner.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a Verify from Flutter reaches the spinner's Verify listeners")
    void verifyReachesListeners() {
        Spinner spinner = spinner();
        List<Event> events = record(spinner, SWT.Verify);

        fromFlutter(spinner, "Verify/Verify", 0);
        runQueued();

        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("a Verify that arrives after the spinner was disposed is dropped")
    void verifyToADisposedSpinnerIsDropped() {
        Spinner spinner = spinner();
        List<Event> events = record(spinner, SWT.Verify);

        fromFlutter(spinner, "Verify/Verify", 0);
        spinner.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a Selection from Flutter moves the value, clamped to the range")
    void selectionMovesTheValue() {
        Spinner spinner = spinner();

        fromFlutter(spinner, "Selection/Selection", 60);
        runQueued();
        assertThat(spinner.getSelection()).isEqualTo(60);

        fromFlutter(spinner, "Selection/Selection", 500);
        runQueued();
        assertThat(spinner.getSelection()).isEqualTo(100);
    }

    @Test
    @DisplayName("a Selection from Flutter notifies Modify then Selection, once each, with the value already moved")
    void selectionNotifiesModifyThenSelection() {
        Spinner spinner = spinner();
        List<String> order = new ArrayList<>();
        List<Integer> valueSeenBySelection = new ArrayList<>();
        spinner.addListener(SWT.Modify, e -> order.add("Modify"));
        spinner.addListener(SWT.Selection, e -> {
            order.add("Selection");
            valueSeenBySelection.add(spinner.getSelection());
        });

        fromFlutter(spinner, "Selection/Selection", 60);
        runQueued();

        assertThat(order).containsExactly("Modify", "Selection");
        assertThat(valueSeenBySelection).containsExactly(60);
    }

    @Test
    @DisplayName("each Selection from Flutter notifies the Selection listeners")
    void everySelectionNotifies() {
        Spinner spinner = spinner();
        List<Integer> seen = new ArrayList<>();
        spinner.addListener(SWT.Selection, e -> seen.add(spinner.getSelection()));

        fromFlutter(spinner, "Selection/Selection", 1);
        fromFlutter(spinner, "Selection/Selection", 2);
        fromFlutter(spinner, "Selection/Selection", 3);
        runQueued();

        assertThat(seen).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("a Selection from Flutter does not reach the DefaultSelection listeners")
    void selectionDoesNotNotifyDefaultSelection() {
        Spinner spinner = spinner();
        List<Event> events = record(spinner, SWT.DefaultSelection);

        fromFlutter(spinner, "Selection/Selection", 60);
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a Selection that arrives after the spinner was disposed notifies nothing")
    void selectionToADisposedSpinnerNotifiesNothing() {
        Spinner spinner = spinner();
        List<Event> events = record(spinner, SWT.Selection);

        fromFlutter(spinner, "Selection/Selection", 60);
        spinner.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a Modify listener that disposes the spinner ends the notification before Selection")
    void selectionIsNotSentToASpinnerDisposedByItsModifyListener() {
        Spinner spinner = spinner();
        spinner.addListener(SWT.Modify, e -> spinner.dispose());
        List<Event> events = record(spinner, SWT.Selection);

        fromFlutter(spinner, "Selection/Selection", 60);
        runQueued();

        assertThat(spinner.isDisposed()).isTrue();
        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a Selection that arrives after the spinner was disposed is dropped")
    void selectionToADisposedSpinnerIsDropped() {
        Spinner spinner = spinner();

        fromFlutter(spinner, "Selection/Selection", 60);
        spinner.dispose();
        runQueued();

        assertThat(spinner.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("a DefaultSelection from Flutter moves the value and reaches the DefaultSelection listeners")
    void defaultSelectionMovesTheValueAndReachesListeners() {
        Spinner spinner = spinner();
        List<Event> events = record(spinner, SWT.DefaultSelection);

        fromFlutter(spinner, "Selection/DefaultSelection", 60);
        runQueued();

        assertThat(spinner.getSelection()).isEqualTo(60);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).widget).isSameAs(spinner);
    }

    @Test
    @DisplayName("a typed value committed with Enter notifies Selection once, before DefaultSelection, with the value moved")
    void typedCommitNotifiesSelectionBeforeDefaultSelection() {
        Spinner spinner = spinner();
        List<String> order = new ArrayList<>();
        List<Integer> valueSeenBySelection = new ArrayList<>();
        spinner.addListener(SWT.Selection, e -> {
            order.add("Selection");
            valueSeenBySelection.add(spinner.getSelection());
        });
        spinner.addListener(SWT.DefaultSelection, e -> order.add("DefaultSelection"));

        fromFlutter(spinner, "Selection/DefaultSelection", 60);
        runQueued();

        assertThat(order).containsExactly("Selection", "DefaultSelection");
        assertThat(valueSeenBySelection).containsExactly(60);
    }

    @Test
    @DisplayName("a Modify listener that disposes the spinner ends a typed commit before Selection and DefaultSelection")
    void typedCommitStopsWhenTheModifyListenerDisposesTheSpinner() {
        Spinner spinner = spinner();
        spinner.addListener(SWT.Modify, e -> spinner.dispose());
        List<Event> selections = record(spinner, SWT.Selection);
        List<Event> defaults = record(spinner, SWT.DefaultSelection);

        fromFlutter(spinner, "Selection/DefaultSelection", 60);
        runQueued();

        assertThat(selections).isEmpty();
        assertThat(defaults).isEmpty();
    }

    @Test
    @DisplayName("a Selection listener that disposes the spinner ends a typed commit before DefaultSelection")
    void typedCommitStopsWhenTheSelectionListenerDisposesTheSpinner() {
        Spinner spinner = spinner();
        spinner.addListener(SWT.Selection, e -> spinner.dispose());
        List<Event> defaults = record(spinner, SWT.DefaultSelection);

        fromFlutter(spinner, "Selection/DefaultSelection", 60);
        runQueued();

        assertThat(spinner.isDisposed()).isTrue();
        assertThat(defaults).isEmpty();
    }

    @Test
    @DisplayName("Enter on a value that did not change notifies DefaultSelection only")
    void enterOnAnUnchangedValueNotifiesDefaultSelectionOnly() {
        Spinner spinner = spinner();
        spinner.setSelection(60);
        List<Event> selections = record(spinner, SWT.Selection);
        List<Event> defaults = record(spinner, SWT.DefaultSelection);

        fromFlutter(spinner, "Selection/DefaultSelection", 60);
        runQueued();

        assertThat(selections).isEmpty();
        assertThat(defaults).hasSize(1);
    }

    @Test
    @DisplayName("a DefaultSelection that arrives after the spinner was disposed is dropped")
    void defaultSelectionToADisposedSpinnerIsDropped() {
        Spinner spinner = spinner();
        List<Event> events = record(spinner, SWT.DefaultSelection);

        fromFlutter(spinner, "Selection/DefaultSelection", 60);
        spinner.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a DefaultSelection while a modal shell blocks the spinner's shell is dropped")
    void defaultSelectionBehindAModalShellIsDropped() {
        Shell shell = DartMocks.dartShell(display);
        Shell modal = mock(Shell.class);
        when(((DartShell) shell.getImpl()).getModalShell()).thenReturn(modal);
        Spinner spinner = new Spinner(shell, SWT.NONE);
        List<Event> events = record(spinner, SWT.DefaultSelection);

        fromFlutter(spinner, "Selection/DefaultSelection", 60);
        runQueued();

        assertThat(events).isEmpty();
        assertThat(spinner.getSelection()).isZero();
    }
}
