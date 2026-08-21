package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A push carries the whole widget, so a Table changed once per event-loop turn would ship the whole
 * table per change. The event loop's flush coalesces those to one push per frame; an explicit
 * {@code update()} and every widget other than Table are never held back.
 */
@ExtendWith(Mocks.class)
class FrameCoalescingTest {

    /** Comfortably past FlutterBridge's 16ms push interval. */
    private static final long PAST_THE_FRAME_MS = 40;

    private static final int BURST = 200;

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
    @DisplayName("a burst of changes to a Table costs a push per frame, not a push per change")
    void framePushesAreCoalesced() throws InterruptedException {
        Table table = newTable();
        Thread.sleep(PAST_THE_FRAME_MS);

        table.setSelection(0);
        FlutterBridge.updateFrame();
        int afterFirst = framesFor(table);
        assertThat(afterFirst).as("the first change goes out on the turn it happened").isPositive();

        for (int i = 1; i <= BURST; i++) {
            table.setSelection(i % 30);
            FlutterBridge.updateFrame();
        }
        // An order of magnitude, not an exact count: how many frames the burst spans depends on the
        // machine, so "no extra push at all" fails whenever the loop straddles a frame boundary.
        assertThat(framesFor(table) - afterFirst)
                .as("%d changes must cost a push per frame, nowhere near %d pushes", BURST, BURST)
                .isLessThan(BURST / 10);

        int beforeHeld = framesFor(table);
        table.setSelection(7);
        FlutterBridge.updateFrame();
        Thread.sleep(PAST_THE_FRAME_MS);
        FlutterBridge.updateFrame();
        assertThat(framesFor(table))
                .as("a held-back change is never dropped")
                .isGreaterThan(beforeHeld);
    }

    @Test
    @DisplayName("a widget other than Table is never held back")
    void onlyTableIsCoalesced() throws InterruptedException {
        Label label = new Label(Mocks.swtShell(), 0);
        label.setData("dev.equo.swt.new", false);
        FlutterBridge.update();
        Thread.sleep(PAST_THE_FRAME_MS);

        label.setText("first");
        FlutterBridge.updateFrame();
        int afterFirst = framesFor(label);

        label.setText("second");
        FlutterBridge.updateFrame();

        assertThat(framesFor(label))
                .as("changing a Label twice inside one frame must still push twice: a test that "
                        + "pumps the loop and reads the result cannot wait for the next frame")
                .isGreaterThan(afterFirst);
    }

    @Test
    @DisplayName("an explicit update() flushes immediately, however recently the widget was pushed")
    void explicitUpdateIsNotHeldBack() throws InterruptedException {
        Table table = newTable();
        Thread.sleep(PAST_THE_FRAME_MS);

        table.setSelection(0);
        FlutterBridge.updateFrame();
        int afterFirst = framesFor(table);

        table.setSelection(1);
        FlutterBridge.update();

        assertThat(framesFor(table))
                .as("callers that block on update() need the state out on this call")
                .isGreaterThan(afterFirst);
    }

    /** A Table past its first send, so later changes are pushes on its own channel. */
    private static Table newTable() {
        Table table = new Table(Mocks.swtShell(), 0);
        for (int i = 0; i < 30; i++) new TableItem(table, 0);
        table.setData("dev.equo.swt.new", false);
        FlutterBridge.update();
        return table;
    }

    private int framesFor(Widget widget) {
        String channel = "/" + widget.hashCode();
        return (int) bridge.comm.sent.stream().filter(f -> f.event.contains(channel)).count();
    }
}
