package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * A native control raises DragDetect only for a DragDetect listener of its own and while drag
 * detection is on; a Display filter alone never sees one.
 */
@ExtendWith(Mocks.class)
class DragDetectGateTest {

    private RecordingBridge bridge;
    private DartDisplay displayImpl;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    /** A Canvas whose display has a DragDetect filter registered, as an application's would. */
    private Canvas canvas() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        displayImpl = (DartDisplay) display.getImpl();
        doAnswer(inv -> inv.callRealMethod()).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doReturn(true).when(displayImpl).filters(SWT.DragDetect);
        // The client's events are delivered through asyncExec; run them at once.
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        return new Canvas(shell, SWT.NONE);
    }

    private void fireDragDetect(Control control) {
        bridge.comm.fireContaining("/" + control.hashCode() + "/DragDetect/DragDetect", new Event());
    }

    private void verifyNoDragDetectSent() {
        verify(displayImpl, never()).sendEvent(any(), argThat((Event e) -> e != null && e.type == SWT.DragDetect));
    }

    @Test
    @DisplayName("with only a Display filter, no DragDetect is sent")
    void filterAloneSeesNone() {
        Canvas canvas = canvas();

        fireDragDetect(canvas);

        verifyNoDragDetectSent();
    }

    @Test
    @DisplayName("a DragDetect listener gets one")
    void listenerGetsIt() {
        java.util.List<String> seen = new ArrayList<>();
        Canvas canvas = canvas();
        canvas.addListener(SWT.DragDetect, e -> seen.add("listener"));

        fireDragDetect(canvas);

        assertThat(seen).containsExactly("listener");
    }

    @Test
    @DisplayName("with drag detection off, even a listener gets none")
    void dragDetectOffSeesNone() {
        java.util.List<String> seen = new ArrayList<>();
        Canvas canvas = canvas();
        canvas.addListener(SWT.DragDetect, e -> seen.add("listener"));
        canvas.setDragDetect(false);

        fireDragDetect(canvas);

        assertThat(seen).isEmpty();
        verifyNoDragDetectSent();
    }
}
