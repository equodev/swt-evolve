package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import dev.equo.swt.harness.WireFrames;
import dev.equo.swt.harness.WireValues;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * When a widget travels as a name rather than as a description.
 *
 * <p>A record names the widgets it holds by id. One the client already holds, and that has not
 * changed, gets no record of its own: adding one child to a composite of five hundred describes the
 * one, not the five hundred.
 */
@ExtendWith(Mocks.class)
class WidgetReferenceTest {

    /** The client these frames are written for, and the one {@link RecordingComm} answers with. */
    private static final int CLIENT = 1;

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
        assumeTrue(Serializer.diffEnabled, "references are part of partial delivery");
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a delivered, unchanged child is named rather than described")
    void unchangedChildIsAReference() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label child = new Label(parent, SWT.NONE);
        child.setText("established");
        deliver(parent);

        List<WireFrames.Record> frame = write(parent);

        assertThat(WireValues.describing(recordOf(frame, parent), child.hashCode())).as("the parent names it").isNotEmpty();
        assertThat(recordOf(frame, child))
                .as("the child is on the far side already and has not moved since")
                .isNull();
    }

    @Test
    @DisplayName("a child that changed tells the client so in a record of its own")
    void changedChildIsWrittenWhole() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label child = new Label(parent, SWT.NONE);
        child.setText("before");
        deliver(parent);

        child.setText("after");
        FlutterBridge.update();

        assertThat(bridge.comm.sent)
                .as("its record is the mention of this change")
                .anyMatch(f -> f.event.equals("Label/" + child.hashCode()) && holds(f.value, "after"));
    }

    @Test
    @DisplayName("a child the far side has never seen is described in full")
    void undeliveredChildIsWrittenWhole() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        deliver(parent);

        Label added = new Label(parent, SWT.NONE);
        added.setText("added");

        Map<String, Object> record = recordOf(write(parent), added);
        assertThat(record).as("there is nothing on the far side for a name to resolve to").isNotNull();
        assertThat(holds(record, "added")).as("there is nothing on the far side for a name to resolve to").isTrue();
    }

    @Test
    @DisplayName("the widget a frame is about is always described")
    void theSubjectOfAFrameIsNeverAReference() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        label.setText("established");
        deliver(parent);

        Map<String, Object> record = recordOf(write(label), label);
        assertThat(record).as("a frame about a widget that says nothing of it says nothing at all").isNotNull();
        assertThat(holds(record, "established"))
                .as("a frame about a widget that says nothing of it says nothing at all")
                .isTrue();
    }

    @Test
    @DisplayName("naming the unchanged is what makes a structural change affordable")
    void addingAChildDoesNotRedescribeItsSiblings() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        for (int i = 0; i < 50; i++) new Label(parent, SWT.NONE).setText("row " + i);
        deliver(parent);

        Label added = new Label(parent, SWT.NONE);
        added.setText("the one that changed");
        List<WireFrames.Record> frame = write(parent);

        assertThat(WireValues.containsText(states(frame), "the one that changed")).isTrue();
        assertThat(frame)
                .as("the parent's record and the new child's: every sibling named, none re-described")
                .hasSize(2);
        assertThat(WireValues.find(states(frame), m -> m.values().stream()
                .anyMatch(v -> v instanceof String text && text.startsWith("row ")))).isEmpty();
    }

    // ---- harness ----

    /** Puts the whole subtree on the wire, so later frames have something to refer back to. */
    private void deliver(Widget widget) {
        FlutterBridge.update();
        markKnown(widget);
        bridge.dirty((DartWidget) widget.getImpl());
        FlutterBridge.update();
        bridge.comm.sent.clear();
    }

    private void markKnown(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (widget instanceof Composite composite)
            for (Widget child : composite.getChildren()) markKnown(child);
    }

    /** The frame describing [widget] to the client: its records, each with its channel. */
    private List<WireFrames.Record> write(Widget widget) {
        return WireFrames.frame((DartWidget) widget.getImpl(), CLIENT);
    }

    /** The record [frame] carries for [widget], or null when it has none. */
    private static Map<String, Object> recordOf(List<WireFrames.Record> frame, Widget widget) {
        String channel = "/" + widget.hashCode();
        for (WireFrames.Record record : frame) {
            if (record.channel().endsWith(channel)) return record.state();
        }
        return null;
    }

    private static List<Map<String, Object>> states(List<WireFrames.Record> frame) {
        return frame.stream().map(WireFrames.Record::state).toList();
    }

    /** Whether [tree] holds the string [value] as a property's value, at any depth. */
    private static boolean holds(Object tree, String value) {
        return !WireValues.find(tree, m -> m.containsValue(value)).isEmpty();
    }
}
