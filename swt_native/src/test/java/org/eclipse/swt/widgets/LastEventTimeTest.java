package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The last event time is the time of the last event the client sent, as a native toolkit answers it
 * with the time of the last OS event. Eclipse takes a command triggered at the same time as the one
 * before for a repeat and drops it, so two events never share a time, however close they arrive.
 * Each display keeps its own: one JVM serves several sessions.
 */
@ExtendWith(Mocks.class)
class LastEventTimeTest {

    private RecordingBridge bridge;

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

    @Test
    void eachEventFromTheClientMovesTheTimeOnEvenWithinOneMillisecond() {
        Button button = new Button(Mocks.shell(), SWT.PUSH);
        String channel = "Button/" + button.hashCode() + "/Selection/Selection";

        bridge.comm.fireContaining(channel, new Event());
        int first = DisplayBridge.lastEventTime(impl(button));
        bridge.comm.fireContaining(channel, new Event());
        int second = DisplayBridge.lastEventTime(impl(button));
        bridge.comm.fireContaining(channel, new Event());
        int third = DisplayBridge.lastEventTime(impl(button));

        assertThat(second).isGreaterThan(first);
        assertThat(third).isGreaterThan(second);
    }

    @Test
    void anEventStampsOnlyTheDisplayOfItsWidget() {
        Button button = new Button(Mocks.shell(), SWT.PUSH);
        DartDisplay other = org.mockito.Mockito.mock(DartDisplay.class);

        bridge.comm.fireContaining("Button/" + button.hashCode() + "/Selection/Selection", new Event());

        assertThat(impl(button).lastEventTime).isNotZero();
        assertThat(other.lastEventTime).isZero();
    }

    @Test
    void withoutANewEventTheTimeStaysThatOfTheLastOne() {
        Button button = new Button(Mocks.shell(), SWT.PUSH);
        bridge.comm.fireContaining("Button/" + button.hashCode() + "/Selection/Selection", new Event());
        DartDisplay display = impl(button);
        int stamped = DisplayBridge.lastEventTime(display);

        button.dispose();

        assertThat(DisplayBridge.lastEventTime(display)).isEqualTo(stamped);
    }

    private static DartDisplay impl(Button button) {
        return (DartDisplay) button.getDisplay().getImpl();
    }
}
