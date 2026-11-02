package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Composite lays its children out from the bounds it finds on them, so a child whose bounds never
 * reach Dart is laid out at whatever was last known — at 0x0, invisible, unpainted, and with no
 * semantics node at all.
 *
 * <p>These tests cover the Java half of that: the bounds a child gets after its parent was already
 * sent must still reach Dart, and a widget marked dirty while a flush is running must not be
 * dropped by that flush's bookkeeping. Neither says by which route it arrives — on the child's own
 * channel or inside an ancestor's payload — because that is the delivery layer's business and has
 * changed more than once.
 */
@Tag("flutter-it")
class StaleChildBoundsFlutterTest {

    /** A {@link RecordingComm} that can run a test hook in the middle of a flush. */
    private static class HookComm extends RecordingComm {
        Runnable onSend;

        @Override
        public void send(String eventName) {
            super.send(eventName);
            fire();
        }

        @Override
        public void send(String eventName, byte[] payload) {
            super.send(eventName, payload);
            fire();
        }

        private void fire() {
            Runnable hook = onSend;
            onSend = null; // one-shot: the hook itself sends, and must not recurse
            if (hook != null) hook.run();
        }
    }

    private RecordingBridge bridge;
    private HookComm comm;
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
        comm = new HookComm();
        bridge = new RecordingBridge(comm);
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    private void pump() {
        for (int i = 0; i < 100 && display.readAndDispatch(); i++) {
            // drain
        }
        FlutterBridge.update();
    }

    /** The last frame whose payload has this widget's id at its root. */
    private Optional<Map<String, Object>> lastPayloadFor(Widget widget) {
        // Payload ids are the API widget's identity hash -- that is what FlutterBridge keys on.
        long id = widget.hashCode();
        Map<String, Object> found = null;
        for (RecordingComm.Frame frame : comm.sent) {
            if (!(frame.value instanceof Map)) continue;
            Map<String, Object> root = frame.state();
            if (hasId(root, id)) {
                found = root;
            }
        }
        return Optional.ofNullable(found);
    }

    /**
     * The last state Dart was told about [id], wherever it travelled: a widget is often delivered
     * INSIDE an ancestor's payload rather than on its own channel (the dirty-ancestor filter drops
     * the child's own send when its parent is dirty too), and Flutter reads it from there.
     */
    private Optional<Map<String, Object>> lastStateAnywhereFor(Widget widget) {
        long id = widget.hashCode();
        Map<String, Object> found = null;
        for (RecordingComm.Frame frame : comm.sent) {
            if (!(frame.value instanceof Map)) continue;
            Map<String, Object> hit = findById(frame.state(), id);
            if (hit != null) found = hit;
        }
        return Optional.ofNullable(found);
    }

    private static boolean hasId(Map<String, Object> node, long id) {
        return node.get("id") instanceof Number n && n.longValue() == id;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> findById(Map<String, Object> node, long id) {
        if (hasId(node, id)) return node;
        if (!(node.get("children") instanceof List<?> children)) return null;
        for (Object element : children) {
            Map<String, Object> hit = findById((Map<String, Object>) element, id);
            if (hit != null) return hit;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> childOf(Map<String, Object> parent, long childId) {
        if (!(parent.get("children") instanceof List<?> children)) return null;
        for (Object element : children) {
            Map<String, Object> child = (Map<String, Object>) element;
            if (hasId(child, childId)) return child;
        }
        return null;
    }

    @Test
    void childLaidOutAfterItsParentWasSent_reachesDart() {
        Shell shell = new Shell(display);
        shell.setSize(1000, 1000);
        Composite part = new Composite(shell, SWT.NONE);
        part.setBounds(0, 32, 970, 958);
        Canvas grid = new Canvas(part, SWT.NONE);
        pump(); // the part and its child are created and sent -- the child still 0x0

        grid.setBounds(5, 5, 960, 816); // what the example's own layout does, after that first send
        pump();

        Map<String, Object> state = lastStateAnywhereFor(grid)
                .orElseThrow(() -> new AssertionError("the child's bounds never reached Dart; frames: "
                        + comm.sent.stream().map(f -> f.event + "=" + f.value).toList()));
        Map<?, ?> bounds = (Map<?, ?>) state.get("bounds");
        assertThat(bounds).as("child bounds as Dart last saw them").isNotNull();
        assertThat(((Number) bounds.get("width")).intValue()).as("child width as Flutter will lay it out").isEqualTo(960);
        assertThat(((Number) bounds.get("height")).intValue()).as("child height as Flutter will lay it out").isEqualTo(816);
    }

    /**
     * {@code update()} snapshots the dirty set, sends, and then clears the WHOLE set — so a widget
     * marked dirty while that flush is running is discarded without ever being sent, and nothing
     * re-dirties it. A layout that runs while an earlier flush is in flight is exactly that case.
     */
    @Test
    void aWidgetDirtiedWhileTheFlushIsRunning_isStillSent() {
        Shell shell = new Shell(display);
        shell.setSize(1000, 1000);
        Composite first = new Composite(shell, SWT.NONE);
        Composite second = new Composite(shell, SWT.NONE);
        pump();
        comm.sent.clear();

        // Dirty `second` from inside the flush -- the window between update()'s snapshot and its clear.
        comm.onSend = () -> second.setBounds(0, 0, 300, 300);
        first.setBounds(0, 0, 100, 100);
        FlutterBridge.update();

        // A later turn of the event loop must still deliver `second`'s new bounds -- on its own
        // channel or inside an ancestor's payload, whichever the filter chose.
        FlutterBridge.update();
        Map<String, Object> state = lastStateAnywhereFor(second)
                .orElseThrow(() -> new AssertionError("`second` never reached Dart at all"));
        Map<?, ?> bounds = (Map<?, ?>) state.get("bounds");
        assertThat(bounds).as("`second` bounds as Dart last saw them").isNotNull();
        assertThat(((Number) bounds.get("width")).intValue())
                .as("a widget dirtied while a flush was running must still reach Dart")
                .isEqualTo(300);
    }
}
