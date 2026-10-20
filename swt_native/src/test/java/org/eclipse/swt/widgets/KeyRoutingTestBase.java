package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

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
        DartDisplay displayImpl = (DartDisplay) shell.getDisplay().getImpl();
        doAnswer(inv -> inv.callRealMethod()).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        return shell;
    }

    /** A KeyDown for {@code keyCode} routed into the control, as the client delivers one. */
    static void press(Control control, int keyCode) {
        Event key = new Event();
        key.keyCode = keyCode;
        ControlHelper.routeKeyDown((DartControl) control.getImpl(), key);
    }
}
