package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

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

    /**
     * The client platform and the lone Alt live in the display's data, which the mock does not keep.
     * One map per test: the display mock is shared by the JVM, so a map left from another test would
     * hand this one its platform.
     */
    @BeforeEach
    void keepDisplayData() {
        Display display = Mocks.display();
        Map<String, Object> data = new HashMap<>();
        doAnswer(inv -> data.put(inv.getArgument(0), inv.getArgument(1))).when(display).setData(anyString(), any());
        doAnswer(inv -> data.get(inv.getArgument(0))).when(display).getData(anyString());
    }

    private Thread displayThread;

    /**
     * The shared display mock belongs to whichever thread built it, which can be a timeout thread of
     * another test. Off that thread the client's platform goes through asyncExec, which the shell drops.
     */
    @BeforeEach
    void runOnDisplayThread() {
        DartDisplay impl = (DartDisplay) Mocks.display().getImpl();
        displayThread = impl.thread;
        impl.thread = Thread.currentThread();
    }

    @AfterEach
    void tearDownBridge() {
        FlutterBridge.set(null);
        ((DartDisplay) Mocks.display().getImpl()).thread = displayThread;
    }

    /** A shell whose display really sends the events its controls fire. */
    static Shell shell() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        doAnswer(inv -> inv.callRealMethod()).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        // The display mock is shared by the JVM and other tests leave asyncExec running at once; this drops the runnables again.
        doNothing().when(display).asyncExec(any(Runnable.class));
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
