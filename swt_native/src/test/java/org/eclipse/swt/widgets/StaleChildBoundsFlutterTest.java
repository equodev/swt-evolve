package org.eclipse.swt.widgets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flutter lays a Composite's children out from the PARENT's copy of them — {@code NoLayout}'s
 * delegate constrains each child tightly to the {@code bounds} it finds in {@code state.children} —
 * so a parent payload that reaches Dart still carrying pre-layout children pins them at that size.
 * At 0x0 the child is invisible, unpainted, and has no semantics node at all.
 *
 * <p>These tests cover the Java half of that contract: the bounds a child gets after its parent was
 * already sent must still reach Dart, and a widget marked dirty while a flush is running must not
 * be dropped by that flush's bookkeeping.
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

    private static class HookBridge extends FlutterBridge {
        final HookComm comm = new HookComm();

        HookBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }

    private HookBridge bridge;
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
        bridge = new HookBridge();
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

    /** The last frame whose payload has this widget's id at its root, parsed. */
    private Optional<JsonObject> lastPayloadFor(Widget widget) {
        // Payload ids are the API widget's identity hash -- that is what FlutterBridge keys on.
        long id = widget.hashCode();
        JsonObject found = null;
        for (RecordingComm.Frame frame : bridge.comm.sent) {
            if (frame.json == null || frame.json.isEmpty()) continue;
            JsonObject root = JsonParser.parseString(frame.json).getAsJsonObject();
            if (root.has("id") && root.get("id").getAsLong() == id) {
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
    private Optional<JsonObject> lastStateAnywhereFor(Widget widget) {
        long id = widget.hashCode();
        JsonObject found = null;
        for (RecordingComm.Frame frame : bridge.comm.sent) {
            if (frame.json == null || frame.json.isEmpty()) continue;
            JsonObject hit = findById(JsonParser.parseString(frame.json).getAsJsonObject(), id);
            if (hit != null) found = hit;
        }
        return Optional.ofNullable(found);
    }

    private static JsonObject findById(JsonObject node, long id) {
        if (node.has("id") && node.get("id").getAsLong() == id) return node;
        if (!node.has("children")) return null;
        for (var element : node.getAsJsonArray("children")) {
            JsonObject hit = findById(element.getAsJsonObject(), id);
            if (hit != null) return hit;
        }
        return null;
    }

    private static JsonObject childOf(JsonObject parent, long childId) {
        if (!parent.has("children")) return null;
        for (var element : parent.getAsJsonArray("children")) {
            JsonObject child = element.getAsJsonObject();
            if (child.has("id") && child.get("id").getAsLong() == childId) return child;
        }
        return null;
    }

    @Test
    void childLaidOutAfterItsParentWasSent_reachesDartInsideTheParentPayload() {
        Shell shell = new Shell(display);
        shell.setSize(1000, 1000);
        Composite part = new Composite(shell, SWT.NONE);
        part.setBounds(0, 32, 970, 958);
        Canvas grid = new Canvas(part, SWT.NONE);
        pump(); // the part and its child are created and sent -- the child still 0x0

        grid.setBounds(5, 5, 960, 816); // what the example's own layout does, after that first send
        pump();

        JsonObject parentPayload = lastPayloadFor(part)
                .orElseThrow(() -> new AssertionError("the parent composite was never sent to Dart; frames: "
                        + bridge.comm.sent.stream().map(f -> f.event + "=" + f.json).toList()));
        JsonObject childCopy = childOf(parentPayload, grid.hashCode());
        assertThat(childCopy)
                .as("the parent's payload must carry its child -- Flutter lays children out from this copy")
                .isNotNull();
        JsonObject bounds = childCopy.getAsJsonObject("bounds");
        assertThat(bounds).as("child bounds inside the parent payload").isNotNull();
        assertThat(bounds.get("width").getAsInt()).as("child width as Flutter will lay it out").isEqualTo(960);
        assertThat(bounds.get("height").getAsInt()).as("child height as Flutter will lay it out").isEqualTo(816);
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
        bridge.comm.sent.clear();

        // Dirty `second` from inside the flush -- the window between update()'s snapshot and its clear.
        bridge.comm.onSend = () -> second.setBounds(0, 0, 300, 300);
        first.setBounds(0, 0, 100, 100);
        FlutterBridge.update();

        // A later turn of the event loop must still deliver `second`'s new bounds -- on its own
        // channel or inside an ancestor's payload, whichever the filter chose.
        FlutterBridge.update();
        JsonObject state = lastStateAnywhereFor(second)
                .orElseThrow(() -> new AssertionError("`second` never reached Dart at all"));
        JsonObject bounds = state.getAsJsonObject("bounds");
        assertThat(bounds).as("`second` bounds as Dart last saw them").isNotNull();
        assertThat(bounds.get("width").getAsInt())
                .as("a widget dirtied while a flush was running must still reach Dart")
                .isEqualTo(300);
    }
}
