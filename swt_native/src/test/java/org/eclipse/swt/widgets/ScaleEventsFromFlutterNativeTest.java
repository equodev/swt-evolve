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
 * What a Selection or DefaultSelection arriving from Flutter does to a Scale. Both hop through {@code asyncExec},
 * so by the time they run the scale may be gone or a modal shell may have opened: a Selection moves the knob
 * (clamped to the range) and a DefaultSelection reaches the listeners of a live scale that nothing blocks.
 */
@Tag("native-unit")
class ScaleEventsFromFlutterNativeTest {

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

    private void fromFlutter(Scale scale, String event, int index) {
        Event e = new Event();
        e.index = index;
        bridge.comm.fireContaining("Scale/" + scale.hashCode() + "/Selection/" + event, e);
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static List<Event> record(Scale scale, int type) {
        List<Event> events = new ArrayList<>();
        scale.addListener(type, events::add);
        return events;
    }

    @Test
    @DisplayName("a Selection from Flutter moves the knob, clamped to the range")
    void selectionMovesTheKnob() {
        Scale scale = new Scale(DartMocks.dartShell(display), SWT.NONE);

        fromFlutter(scale, "Selection", 60);
        runQueued();
        assertThat(scale.getSelection()).isEqualTo(60);

        fromFlutter(scale, "Selection", 500);
        runQueued();
        assertThat(scale.getSelection()).isEqualTo(100);
    }

    @Test
    @DisplayName("a Selection that arrives after the scale was disposed is dropped")
    void selectionToADisposedScaleIsDropped() {
        Scale scale = new Scale(DartMocks.dartShell(display), SWT.NONE);

        fromFlutter(scale, "Selection", 60);
        scale.dispose();
        runQueued();

        assertThat(scale.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("a DefaultSelection from Flutter reaches the scale's DefaultSelection listeners")
    void defaultSelectionReachesListeners() {
        Scale scale = new Scale(DartMocks.dartShell(display), SWT.NONE);
        List<Event> events = record(scale, SWT.DefaultSelection);

        fromFlutter(scale, "DefaultSelection", 0);
        runQueued();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).widget).isSameAs(scale);
    }

    @Test
    @DisplayName("a DefaultSelection that arrives after the scale was disposed is dropped")
    void defaultSelectionToADisposedScaleIsDropped() {
        Scale scale = new Scale(DartMocks.dartShell(display), SWT.NONE);
        List<Event> events = record(scale, SWT.DefaultSelection);

        fromFlutter(scale, "DefaultSelection", 0);
        scale.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a DefaultSelection while a modal shell blocks the scale's shell is dropped")
    void defaultSelectionBehindAModalShellIsDropped() {
        Shell shell = DartMocks.dartShell(display);
        Shell modal = mock(Shell.class);
        when(((DartShell) shell.getImpl()).getModalShell()).thenReturn(modal);
        Scale scale = new Scale(shell, SWT.NONE);
        List<Event> events = record(scale, SWT.DefaultSelection);

        fromFlutter(scale, "DefaultSelection", 0);
        runQueued();

        assertThat(events).isEmpty();
    }
}
