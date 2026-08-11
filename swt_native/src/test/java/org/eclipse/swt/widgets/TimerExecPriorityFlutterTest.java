package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Regression for a deferred-layout flash on tab/part open: a layout scheduled via
 * {@link Display#timerExec} (e.g. an expandable section that collapses itself a few ms after being
 * built) ran hundreds of ms late because {@code runTimers()} was a dead stub and elapsed timers were
 * routed through the low-priority, one-per-cycle async-message queue.
 *
 * <p>The fix restores native timer priority: {@code readAndDispatch()} drains every due timer (via
 * {@code runTimers}) ahead of async messages, and drains them all in one pass instead of one per
 * cycle. These tests drive only the public SWT event loop and are RED before the fix (the timer sits
 * behind the async backlog / only one timer runs per dispatch) and GREEN after.
 */
@Tag("flutter-it")
class TimerExecPriorityFlutterTest {

    private Display display;

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
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("an elapsed timerExec runs with priority over a queued async backlog, in a single readAndDispatch")
    void timerRunsAheadOfAsyncBacklog() throws Exception {
        List<String> order = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < 20; i++) {
            final int n = i;
            display.asyncExec(() -> order.add("async" + n));
        }
        display.timerExec(10, () -> order.add("TIMER"));

        // Let the 10ms timer elapse; it is enqueued from a java.util.Timer thread.
        Thread.sleep(120);

        display.readAndDispatch(); // a single dispatch

        assertThat(order)
                .as("the elapsed timer must run via runTimers (priority) in the first dispatch, not "
                        + "sit behind the 20 queued async runnables")
                .contains("TIMER");
    }

    @Test
    @DisplayName("all timers due this pass run in a single readAndDispatch, not one per cycle")
    void allDueTimersDrainInOneDispatch() throws Exception {
        List<String> ran = Collections.synchronizedList(new ArrayList<>());
        display.timerExec(10, () -> ran.add("t0"));
        display.timerExec(10, () -> ran.add("t1"));
        display.timerExec(10, () -> ran.add("t2"));

        Thread.sleep(120);

        display.readAndDispatch(); // a single dispatch

        assertThat(ran)
                .as("runTimers must drain every due timer in one pass (native priority), not one per dispatch")
                .contains("t0", "t1", "t2");
    }
}
