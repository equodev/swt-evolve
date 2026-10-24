package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How a control that changed parents travels.
 *
 * <p>The two parents are siblings, so their frames leave in an order nothing decides. The far side
 * reads a disposal out of what stops being carried, and tells it apart from a move by comparing
 * write stamps - so a moved control has to arrive described, with a stamp of its own. Named
 * instead, it carries no stamp, and the old parent's frame applied first reads as the control
 * having gone away: it is dropped, and the new parent is left pointing at nothing.
 */
@ExtendWith(Mocks.class)
class ReparentDeliveryTest {

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
    @DisplayName("a control that changed parents is described by its new parent, not named")
    void aMovedControlIsDescribed() {
        Shell shell = Mocks.shell();
        Composite from = new Composite(shell, SWT.NONE);
        Composite to = new Composite(shell, SWT.NONE);
        Composite moved = new Composite(from, SWT.NONE);
        settle(from, to);

        moved.setParent(to);
        FlutterBridge.update();

        assertThat(bridge.comm.sent).hasSize(1);
        String payload = bridge.comm.sent.get(0).json;
        assertThat(payload)
                .as("the old parent says it no longer holds the control")
                .contains("\"id\":" + from.hashCode());
        assertThat(payload)
                .as("a name carries no write stamp, so the far side cannot tell the move from a "
                        + "disposal and drops the control before the new parent's frame arrives")
                .doesNotContain("{\"id\":" + moved.hashCode() + ",\"swt\":\"Composite\",\"_r\":1}");
        assertThat(payload)
                .as("described, so it arrives with a stamp of its own")
                .contains("{\"id\":" + moved.hashCode() + ",\"swt\":\"Composite\",\"_s\":");
    }

    @Test
    @DisplayName("a control moved into a hidden shell and back arrives with everything under it described")
    void aControlBackFromAHiddenShellIsDescribedWhole() {
        Shell shell = Mocks.shell();
        Composite host = new Composite(shell, SWT.NONE);
        Shell hidden = new Shell(shell, SWT.NONE);
        Composite part = new Composite(host, SWT.NONE);
        Composite inner = new Composite(part, SWT.NONE);
        Label leaf = new Label(inner, SWT.NONE);
        leaf.setText("leaf");
        settle(host);

        // Nothing the client is shown holds the part any more, so it lets go of it and of
        // everything under it.
        part.setParent(hidden);
        FlutterBridge.update();
        bridge.comm.sent.clear();
        part.setParent(host);
        FlutterBridge.update();

        // Read in delivery order, as the client applies them: a name resolves only to something
        // described before it.
        Set<Long> described = new HashSet<>();
        Set<Long> unresolved = new LinkedHashSet<>();
        for (var frame : bridge.comm.sent) walk(JsonParser.parseString(frame.json), described, unresolved);
        for (Widget w : new Widget[]{part, inner, leaf}) {
            assertThat(described).as("%s is described", w).contains((long) w.hashCode());
            assertThat(unresolved).as("%s is never named before it is described", w)
                    .doesNotContain((long) w.hashCode());
        }
    }

    private static void walk(JsonElement element, Set<Long> described, Set<Long> unresolved) {
        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) walk(e, described, unresolved);
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("id") && object.has("_r")) {
                long id = object.get("id").getAsLong();
                if (!described.contains(id)) unresolved.add(id);
            } else if (object.has("id") && object.has("_s") && !object.has("_b")) {
                described.add(object.get("id").getAsLong());
            }
            for (var entry : object.entrySet()) walk(entry.getValue(), described, unresolved);
        }
    }

    /**
     * Two rounds. The first only clears the "new" flag - a widget the client has never been told
     * about is expected to travel inside its parent, so it is not sent on its own channel - and the
     * second delivers, which is what gives every widget the write stamp the diff path needs.
     */
    private void settle(Composite... roots) {
        FlutterBridge.update();
        for (Composite root : roots) bridge.dirty((DartWidget) root.getImpl());
        FlutterBridge.update();
        bridge.comm.sent.clear();
    }
}
