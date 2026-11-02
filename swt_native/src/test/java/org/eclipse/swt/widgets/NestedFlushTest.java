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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A flush can start inside another: a getter read for the client can pump the event loop, which
 * flushes. The inner one must leave the outer one as it found it, still collecting into its frames.
 */
@ExtendWith(Mocks.class)
class NestedFlushTest {

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
    @DisplayName("a flush started while a record is written sends each record once, the outer frame included")
    void nestedFlushSendsEachRecordOnce() {
        Shell shell = Mocks.shell();
        Label inner = sentAlready(new Label(shell, SWT.NONE));
        Label after = sentAlready(new Label(shell, SWT.NONE));
        FlutterBridge.update();
        bridge.comm.sent.clear();

        // A record being written reads a getter that pumps the event loop, which flushes.
        FlutterBridge.dirty(once(() -> FlutterBridge.writeTape(bridge.comm, tape -> {
            tape.describeHolder("Display", 1, new int[0], (w, key) -> false);
            inner.setText("inner");
            FlutterBridge.update();
        })));
        after.setText("after");
        FlutterBridge.update();

        assertThat(sends("Display/1")).as("the outer frame's record").isEqualTo(1);
        assertThat(sends("Label/" + inner.hashCode())).as("the inner flush's record").isEqualTo(1);
        assertThat(sends("Label/" + after.hashCode())).as("a record the outer flush wrote after the inner one").isEqualTo(1);
    }

    @Test
    @DisplayName("what the outer flush writes after a flush inside it arrives after what it wrote before")
    void outerRecordsKeepTheirOrder() {
        Shell shell = Mocks.shell();
        Label inner = sentAlready(new Label(shell, SWT.NONE));
        FlutterBridge.update();
        bridge.comm.sent.clear();

        FlutterBridge.dirty(once(() -> FlutterBridge.writeTape(bridge.comm, tape -> {
            tape.describeHolder("Display", 1, new int[0], (w, key) -> false);
            inner.setText("inner");
            FlutterBridge.update();
        })));
        FlutterBridge.dirty(once(() -> FlutterBridge.writeTape(bridge.comm,
                tape -> tape.describeHolder("Display", 2, new int[0], (w, key) -> false))));
        FlutterBridge.update();

        java.util.List<String> displays = bridge.comm.sent.stream().map(f -> f.event)
                .filter(e -> e.startsWith("Display/")).collect(java.util.stream.Collectors.toList());
        assertThat(displays).containsExactly("Display/1", "Display/2");
    }

    @Test
    @DisplayName("without a flush inside it, a flush sends each changed widget's record once")
    void plainFlushSendsEachRecordOnce() {
        Label label = sentAlready(new Label(Mocks.shell(), SWT.NONE));
        FlutterBridge.update();
        bridge.comm.sent.clear();

        label.setText("text");
        FlutterBridge.update();

        assertThat(sends("Label/" + label.hashCode())).isEqualTo(1);
    }

    /** A widget past its first send, so a change to it is a record of its own. */
    private static <T extends Widget> T sentAlready(T widget) {
        widget.setData("dev.equo.swt.new", false);
        return widget;
    }

    /** How many sent records are about {@code channel}. */
    private long sends(String channel) {
        return bridge.comm.sent.stream().filter(f -> f.event.equals(channel)).count();
    }

    /** A state that flushes {@code flush} once, then drops out of the set. */
    private static FlutterBridge.DirtyState once(Runnable flush) {
        return new FlutterBridge.DirtyState() {
            private boolean done;

            @Override
            public boolean isStale() {
                return done;
            }

            @Override
            public void flush() {
                done = true;
                flush.run();
            }
        };
    }
}
