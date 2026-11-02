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
 * live spinner, a Selection (an arrow click) steps the value as the arrow keys do, and a DefaultSelection moves it and reaches
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

    /** A click on the client's up ({@link SWT#ARROW_UP}) or down ({@link SWT#ARROW_DOWN}) button. */
    private void arrow(Spinner spinner, int direction) {
        Event e = new Event();
        e.detail = direction;
        bridge.comm.fireContaining("Spinner/" + spinner.hashCode() + "/Selection/Selection", e);
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

    /** The names of the Verify, Modify and Selection events the spinner notifies, in order. */
    private static List<String> recordStep(Spinner spinner) {
        List<String> order = new ArrayList<>();
        spinner.addListener(SWT.Verify, e -> order.add("Verify"));
        spinner.addListener(SWT.Modify, e -> order.add("Modify"));
        spinner.addListener(SWT.Selection, e -> order.add("Selection"));
        return order;
    }

    private Spinner spinner() {
        return spinner(SWT.NONE);
    }

    private Spinner spinner(int style) {
        return new Spinner(DartMocks.dartShell(display), style);
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
    @DisplayName("an arrow from Flutter steps the value by the increment, clamped to the range")
    void anArrowStepsTheValue() {
        Spinner spinner = spinner();
        spinner.setIncrement(5);
        spinner.setSelection(50);

        arrow(spinner, SWT.ARROW_UP);
        runQueued();
        assertThat(spinner.getSelection()).isEqualTo(55);

        arrow(spinner, SWT.ARROW_DOWN);
        arrow(spinner, SWT.ARROW_DOWN);
        runQueued();
        assertThat(spinner.getSelection()).isEqualTo(45);

        spinner.setSelection(98);
        arrow(spinner, SWT.ARROW_UP);
        runQueued();
        assertThat(spinner.getSelection()).isEqualTo(100);
    }

    @Test
    @DisplayName("an arrow from Flutter notifies Modify then Selection, once each, with the value already moved")
    void anArrowNotifiesModifyThenSelection() {
        Spinner spinner = spinner();
        spinner.setSelection(59);
        List<String> order = new ArrayList<>();
        List<Integer> valueSeenBySelection = new ArrayList<>();
        spinner.addListener(SWT.Modify, e -> order.add("Modify"));
        spinner.addListener(SWT.Selection, e -> {
            order.add("Selection");
            valueSeenBySelection.add(spinner.getSelection());
        });

        arrow(spinner, SWT.ARROW_UP);
        runQueued();

        assertThat(order).containsExactly("Modify", "Selection");
        assertThat(valueSeenBySelection).containsExactly(60);
    }

    @Test
    @DisplayName("a Selection from Flutter with no arrow in its detail neither moves the value nor notifies")
    void aSelectionWithNoArrowIsNotAStep() {
        Spinner spinner = spinner();
        spinner.setSelection(50);
        List<Event> events = record(spinner, SWT.Selection);

        fromFlutter(spinner, "Selection/Selection", 51);
        runQueued();

        assertThat(events).isEmpty();
        assertThat(spinner.getSelection()).isEqualTo(50);
    }

    @Test
    @DisplayName("an arrow from Flutter whose text Verify refuses moves the value and notifies neither Modify nor Selection")
    void aRefusedArrowFromFlutterNotifiesNothing() {
        Spinner spinner = spinner();
        spinner.setSelection(20);
        List<String> order = new ArrayList<>();
        spinner.addListener(SWT.Verify, e -> e.doit = false);
        spinner.addListener(SWT.Modify, e -> order.add("Modify"));
        spinner.addListener(SWT.Selection, e -> order.add("Selection"));

        arrow(spinner, SWT.ARROW_UP);
        runQueued();

        assertThat(order).isEmpty();
        assertThat(spinner.getSelection()).isEqualTo(21);
        assertThat(spinner.getText()).isEqualTo("20");
    }

    @Test
    @DisplayName("an arrow at either end of the range leaves the value and notifies nothing, as Win32 and GTK")
    void anArrowAtTheEndNotifiesNothing() {
        Spinner spinner = spinner();
        spinner.setSelection(100);
        List<String> order = recordStep(spinner);

        arrow(spinner, SWT.ARROW_UP);
        runQueued();
        assertThat(order).isEmpty();
        assertThat(spinner.getSelection()).isEqualTo(100);

        spinner.setSelection(0);
        order.clear();
        arrow(spinner, SWT.ARROW_DOWN);
        runQueued();
        assertThat(order).isEmpty();
        assertThat(spinner.getSelection()).isEqualTo(0);
    }

    @Test
    @DisplayName("the up arrow of a WRAP spinner at its maximum goes to the minimum and notifies the step")
    void theUpArrowOfAWrapSpinnerWraps() {
        Spinner spinner = spinner(SWT.WRAP);
        spinner.setSelection(100);
        List<String> order = recordStep(spinner);

        arrow(spinner, SWT.ARROW_UP);
        runQueued();

        assertThat(spinner.getSelection()).isEqualTo(0);
        assertThat(order).containsExactly("Verify", "Modify", "Selection");
    }

    @Test
    @DisplayName("the down arrow of a WRAP spinner at its minimum goes to the maximum")
    void theDownArrowOfAWrapSpinnerWraps() {
        Spinner spinner = spinner(SWT.WRAP);

        arrow(spinner, SWT.ARROW_DOWN);
        runQueued();

        assertThat(spinner.getSelection()).isEqualTo(100);
    }

    @Test
    @DisplayName("each arrow from Flutter steps from the value Java holds and notifies the Selection listeners")
    void everyArrowNotifies() {
        Spinner spinner = spinner();
        List<Integer> seen = new ArrayList<>();
        spinner.addListener(SWT.Selection, e -> seen.add(spinner.getSelection()));

        arrow(spinner, SWT.ARROW_UP);
        arrow(spinner, SWT.ARROW_UP);
        arrow(spinner, SWT.ARROW_UP);
        runQueued();

        assertThat(seen).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("an arrow from Flutter does not reach the DefaultSelection listeners")
    void anArrowDoesNotNotifyDefaultSelection() {
        Spinner spinner = spinner();
        List<Event> events = record(spinner, SWT.DefaultSelection);

        arrow(spinner, SWT.ARROW_UP);
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("an arrow that arrives after the spinner was disposed notifies nothing")
    void anArrowToADisposedSpinnerNotifiesNothing() {
        Spinner spinner = spinner();
        List<Event> events = record(spinner, SWT.Selection);

        arrow(spinner, SWT.ARROW_UP);
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

        arrow(spinner, SWT.ARROW_UP);
        runQueued();

        assertThat(spinner.isDisposed()).isTrue();
        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("an arrow that arrives after the spinner was disposed is dropped")
    void anArrowToADisposedSpinnerIsDropped() {
        Spinner spinner = spinner();

        arrow(spinner, SWT.ARROW_UP);
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
