package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * A value typed into a Spinner and committed with Enter has to move the widget's selection, not
 * only notify listeners. The client commits the text on its own side and this DefaultSelection is
 * the only channel carrying the committed value up, so dropping it leaves {@code getSelection()}
 * answering the pre-edit value for the rest of the session — the arrows stay right, typing does not.
 */
@Tag("flutter-it")
class SpinnerTypedCommitFlutterTest {

    private RecordingBridge bridge;
    private Shell shell;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private Spinner spinner() {
        shell = DartMocks.dartShell();
        when(shell.getBounds()).thenReturn(new Rectangle(0, 0, 800, 600));
        Display display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        doAnswer(inv -> {
            EventTable table = inv.getArgument(0);
            Event event = inv.getArgument(1);
            if (table != null)
                table.sendEvent(event);
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));

        Spinner api = new DartSpinner(shell, SWT.BORDER, null).getApi();
        api.setMinimum(0);
        api.setMaximum(100);
        api.setSelection(25);
        return api;
    }

    private void fireTypedCommitFromFlutter(int value) {
        Event e = new Event();
        e.index = value;
        bridge.comm.fireContaining("/Selection/DefaultSelection", e);
    }

    @Test
    @DisplayName("a typed value committed with Enter moves getSelection()")
    void typedCommitMovesSelection() {
        Spinner spinner = spinner();

        fireTypedCommitFromFlutter(60);

        assertThat(spinner.getSelection()).isEqualTo(60);
    }

    @Test
    @DisplayName("the DefaultSelection listener already sees the committed value")
    void listenerSeesTheCommittedValue() {
        Spinner spinner = spinner();
        AtomicInteger seenByListener = new AtomicInteger(-1);
        spinner.addListener(SWT.DefaultSelection, e -> seenByListener.set(spinner.getSelection()));

        fireTypedCommitFromFlutter(60);

        assertThat(seenByListener.get())
                .as("the value is applied before the listener runs, as it is natively")
                .isEqualTo(60);
    }
}
