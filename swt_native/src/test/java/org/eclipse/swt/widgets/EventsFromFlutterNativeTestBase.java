package org.eclipse.swt.widgets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * A test of what an event arriving from Flutter does to a widget. Every client event hops through
 * {@code asyncExec}, so the display queues it until the test calls {@link #runQueued()}, and by then the
 * widget may be gone.
 */
abstract class EventsFromFlutterNativeTestBase {

    RecordingBridge bridge;
    Display display;
    private final List<Runnable> queued = new ArrayList<>();

    @BeforeEach
    void wireTheBridgeAndTheDisplay() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        // The mock display would swallow every event; its real dispatch only needs the listener table.
        DartDisplay impl = dartDisplay();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void handBackTheDisplay() {
        // The display mock is shared by the whole suite: hand it back as the other tests expect it.
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = dartDisplay();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    /** Delivers what the client sends on {@code channel} of {@code widget}, such as {@code "Selection/Selection"}. */
    void fromFlutter(Widget widget, String channel, Event event) {
        bridge.comm.fireContaining(FlutterBridge.event(widget.getImpl(), channel), event);
    }

    void fromFlutter(Widget widget, String channel) {
        fromFlutter(widget, channel, new Event());
    }

    void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    /** Also what makes the widget send the type at all: without a listener it does not. */
    static List<Event> record(Widget widget, int type) {
        List<Event> events = new ArrayList<>();
        widget.addListener(type, events::add);
        return events;
    }
}
