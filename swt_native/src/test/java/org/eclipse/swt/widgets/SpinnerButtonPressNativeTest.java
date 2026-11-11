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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

/**
 * The order a press on a Spinner's up or down button reports its events in. Win32 and GTK step the value as
 * the button goes down: the text changes (Verify, Modify) before the MouseDown is delivered, and the Selection
 * follows it, with button 1 held. A held button repeats the step, each with its Selection at once.
 */
@Tag("native-unit")
class SpinnerButtonPressNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private final java.util.List<Runnable> queued = new ArrayList<>();
    private final java.util.List<String> heard = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private Spinner spinner() {
        Spinner spinner = new Spinner(DartMocks.dartShell(display), SWT.NONE);
        spinner.setValues(50, 0, 100, 0, 10, 25);
        spinner.setBounds(0, 0, 200, 60);
        spinner.addListener(SWT.Verify, e -> heard.add("Verify " + e.text));
        spinner.addListener(SWT.Modify, e -> heard.add("Modify " + spinner.getText()));
        spinner.addListener(SWT.MouseDown, e -> heard.add("MouseDown"));
        spinner.addListener(SWT.MouseUp, e -> heard.add("MouseUp"));
        spinner.addListener(SWT.Selection, e -> heard.add("Selection " + spinner.getSelection() + " stateMask=" + e.stateMask));
        return spinner;
    }

    private void fromFlutter(Widget widget, String channel, Event event) {
        bridge.comm.fireContaining(widget.getClass().getSimpleName() + "/" + widget.hashCode() + "/" + channel, event);
    }

    private void runQueued() {
        java.util.List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static Event mouse(int button) {
        Event event = new Event();
        event.button = button;
        event.x = 190;
        event.y = 15;
        return event;
    }

    /** The client's step of a button: {@code button} 1 for the one its press makes, 0 for a repeat while it is held. */
    private static Event step(int direction, int button) {
        Event event = new Event();
        event.detail = direction;
        event.button = button;
        return event;
    }

    @Test
    @DisplayName("a press on the up button changes the text before its MouseDown, and reports the Selection after it")
    void aPressStepsBeforeItsMouseDown() {
        Spinner spinner = spinner();

        fromFlutter(spinner, "Selection/Selection", step(SWT.ARROW_UP, 1));
        fromFlutter(spinner, "Mouse/MouseDown", mouse(1));
        fromFlutter(spinner, "Mouse/MouseUp", mouse(1));
        runQueued();

        assertThat(heard).containsExactly("Verify 60", "Modify 60", "MouseDown", "Selection 60 stateMask=" + SWT.BUTTON1,
                "MouseUp");
    }

    @Test
    @DisplayName("a held button's repeats report their Selection at once, with button 1 held")
    void aHeldButtonRepeats() {
        Spinner spinner = spinner();

        fromFlutter(spinner, "Selection/Selection", step(SWT.ARROW_DOWN, 1));
        fromFlutter(spinner, "Mouse/MouseDown", mouse(1));
        fromFlutter(spinner, "Selection/Selection", step(SWT.ARROW_DOWN, 0));
        fromFlutter(spinner, "Mouse/MouseUp", mouse(1));
        runQueued();

        assertThat(heard).containsExactly("Verify 40", "Modify 40", "MouseDown", "Selection 40 stateMask=" + SWT.BUTTON1,
                "Verify 30", "Modify 30", "Selection 30 stateMask=" + SWT.BUTTON1, "MouseUp");
    }

    @Test
    @DisplayName("a button step while a modal dialog blocks the spinner steps nothing and leaves nothing held")
    void aBlockedSpinnerTakesNoStep() {
        Spinner spinner = spinner();
        DartDisplay impl = (DartDisplay) display.getImpl();
        doReturn(org.mockito.Mockito.mock(Dialog.class)).when(impl).getModalDialog();
        try {
            fromFlutter(spinner, "Selection/Selection", step(SWT.ARROW_UP, 1));
            runQueued();
        } finally {
            doReturn(null).when(impl).getModalDialog();
        }

        assertThat(heard).isEmpty();
        assertThat(spinner.getSelection()).isEqualTo(50);
        assertThat(((DartSpinner) spinner.getImpl()).pressSelection).isNull();
    }

    @Test
    @DisplayName("a MouseDown listener that disposes the spinner ends the press: no Selection is told")
    void aMouseDownListenerThatDisposesEndsThePress() {
        Spinner spinner = spinner();
        spinner.addListener(SWT.MouseDown, e -> spinner.dispose());

        fromFlutter(spinner, "Selection/Selection", step(SWT.ARROW_UP, 1));
        fromFlutter(spinner, "Mouse/MouseDown", mouse(1));
        runQueued();

        assertThat(spinner.isDisposed()).isTrue();
        assertThat(heard).containsExactly("Verify 60", "Modify 60", "MouseDown");
    }

    @Test
    @DisplayName("a held Selection whose listener disposes the spinner ends the next step there")
    void aSelectionListenerThatDisposesEndsTheNextStep() {
        Spinner spinner = spinner();
        spinner.addListener(SWT.Selection, e -> spinner.dispose());

        fromFlutter(spinner, "Selection/Selection", step(SWT.ARROW_UP, 1));
        fromFlutter(spinner, "Selection/Selection", step(SWT.ARROW_UP, 1));
        runQueued();

        assertThat(spinner.isDisposed()).isTrue();
        assertThat(heard).containsExactly("Verify 60", "Modify 60", "Selection 60 stateMask=" + SWT.BUTTON1);
    }

    @Test
    @DisplayName("a press's Selection whose MouseDown never came is reported before the next step")
    void aHeldSelectionIsReportedBeforeTheNextStep() {
        Spinner spinner = spinner();

        fromFlutter(spinner, "Selection/Selection", step(SWT.ARROW_UP, 1));
        fromFlutter(spinner, "Selection/Selection", step(SWT.ARROW_UP, 1));
        runQueued();

        assertThat(heard).containsExactly("Verify 60", "Modify 60", "Selection 60 stateMask=" + SWT.BUTTON1,
                "Verify 70", "Modify 70");
    }
}
