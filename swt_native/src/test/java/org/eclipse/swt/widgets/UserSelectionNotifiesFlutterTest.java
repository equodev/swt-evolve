package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * A Spinner's arrow click and a Scale's drag reach Java as a Selection event from the client, which
 * carries the arrow or the dragged value. Natively that change notifies the application's Selection listeners; applying
 * the value without notifying leaves a listener that repaints from it never called.
 */
@Tag("flutter-it")
class UserSelectionNotifiesFlutterTest {

    private RecordingBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private Shell shell() {
        Shell shell = DartMocks.dartShell();
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
        return shell;
    }

    /** The client's Selection: a Spinner sends the arrow clicked in {@code detail}, a Scale its new value in {@code index}. */
    private void fireUserSelection(int detail, int index) {
        Event e = new Event();
        e.detail = detail;
        e.index = index;
        bridge.comm.fireContaining("/Selection/Selection", e);
    }

    @Test
    @DisplayName("a Spinner's arrow notifies Selection listeners, which see the new value")
    void spinnerArrowNotifies() {
        Spinner spinner = new DartSpinner(shell(), SWT.BORDER, null).getApi();
        spinner.setMaximum(100);
        spinner.setSelection(25);
        List<Integer> seen = new ArrayList<>();
        spinner.addListener(SWT.Selection, e -> seen.add(spinner.getSelection()));

        fireUserSelection(SWT.ARROW_UP, 0);

        assertThat(seen).containsExactly(26);
    }

    @Test
    @DisplayName("a Scale's drag notifies Selection listeners, which see the new value")
    void scaleDragNotifies() {
        Scale scale = new DartScale(shell(), SWT.HORIZONTAL, null).getApi();
        scale.setMaximum(100);
        scale.setSelection(10);
        List<Integer> seen = new ArrayList<>();
        scale.addListener(SWT.Selection, e -> seen.add(scale.getSelection()));

        fireUserSelection(SWT.NONE, 40);

        assertThat(seen).containsExactly(40);
    }

    @Test
    @DisplayName("setSelection from the application does not notify")
    void programmaticSetSelectionDoesNotNotify() {
        Spinner spinner = new DartSpinner(shell(), SWT.BORDER, null).getApi();
        spinner.setMaximum(100);
        List<Integer> seen = new ArrayList<>();
        spinner.addListener(SWT.Selection, e -> seen.add(spinner.getSelection()));

        spinner.setSelection(50);

        assertThat(seen).isEmpty();
    }
}
