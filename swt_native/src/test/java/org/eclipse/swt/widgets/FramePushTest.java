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
 * The event loop's flush sends every change on the turn it happened. A push carries only what
 * changed, so holding one back to the next frame saves little and costs the client a frame of
 * latency: a Table changed twice in quick succession showed the second change a frame late.
 */
@ExtendWith(Mocks.class)
class FramePushTest {

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
    @DisplayName("a Table changed right after a push is pushed again on the same turn")
    void aTableChangeIsNotHeldToTheNextFrame() {
        Table table = newTable();

        table.setSelection(0);
        FlutterBridge.updateFrame();
        int afterFirst = framesFor(table);
        table.setSelection(1);
        FlutterBridge.updateFrame();

        assertThat(framesFor(table)).as("the second change, out on its own turn").isGreaterThan(afterFirst);
        assertThat(FlutterBridge.hasQueued()).as("nothing left for a later frame").isFalse();
    }

    @Test
    @DisplayName("a widget changed twice inside one frame is pushed twice")
    void everyWidgetPushesOnItsTurn() {
        Label label = new Label(Mocks.shell(), 0);
        label.setData("dev.equo.swt.new", false);
        FlutterBridge.update();

        label.setText("first");
        FlutterBridge.updateFrame();
        int afterFirst = framesFor(label);
        label.setText("second");
        FlutterBridge.updateFrame();

        assertThat(framesFor(label)).isGreaterThan(afterFirst);
    }

    /** A Table past its first send, so later changes are pushes on its own channel. */
    private static Table newTable() {
        Table table = new Table(Mocks.shell(), 0);
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
