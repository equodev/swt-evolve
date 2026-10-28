package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/** A Dart control on a mocked shell that delivers its events, and a key pressed into it as the client would. */
@ExtendWith(Mocks.class)
abstract class KeyRoutingTestBase {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUpBridge() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDownBridge() {
        FlutterBridge.set(null);
    }

    /** A shell whose display really sends the events its controls fire. */
    static Shell shell() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        doAnswer(inv -> inv.callRealMethod()).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        // The display mock is shared by the JVM and other tests leave asyncExec running at once; this drops the runnables again.
        doNothing().when(display).asyncExec(any(Runnable.class));
        // Where a keyboard context menu (Shift+F10) opens when no item anchors it.
        when(display.getCursorLocation()).thenReturn(new Point(0, 0));
        return shell;
    }

    /** A KeyDown for {@code keyCode} routed into the control, as the client delivers one. */
    static void press(Control control, int keyCode) {
        press(control, keyCode, 0);
    }

    /** The same, with {@code stateMask} held. */
    static void press(Control control, int keyCode, int stateMask) {
        Event key = new Event();
        key.keyCode = keyCode;
        key.stateMask = stateMask;
        ControlHelper.routeKeyDown((DartControl) control.getImpl(), key);
    }
}
