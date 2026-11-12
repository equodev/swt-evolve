package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import dev.equo.swt.harness.WireFrames;
import org.eclipse.swt.SWT;
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

/**
 * What a change to a list of widgets costs: a child added to or removed from a composite, or a row
 * added to a table, travels as the change to the list the client holds rather than as the list.
 */
@ExtendWith(Mocks.class)
class ListChangeWireTest {

    private static final int CLIENT = 1;

    /** Bytes of a composite's change record when one child is added to its thirty. */
    private static final int CHILD_ADDED_BUDGET = 32;

    /**
     * Bytes of a table's change record when one text-only row is added to its three hundred, the row
     * included. Ids are hash codes, so their varints vary by a byte or two from run to run.
     */
    private static final int ROW_ADDED_BUDGET = 58;

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
        Serializer.enterConnection(CLIENT);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a child added to or removed from thirty travels as the change to the list held")
    void childAddedAndRemoved() {
        Composite composite = new Composite(Mocks.shell(), SWT.NONE);
        for (int i = 0; i < 30; i++) new Label(composite, SWT.NONE).setText("Label " + i);
        frameOf(composite);

        Label added = new Label(composite, SWT.NONE);
        added.setText("added");
        List<WireFrames.Record> addedFrame = frameOf(composite);
        WireFrames.Record change = recordOf(addedFrame, composite);
        assertThat(change.size()).as("bytes of the composite's record %s", change.state()).isLessThanOrEqualTo(CHILD_ADDED_BUDGET);
        Map<String, Object> children = listChange(change.state().get("children"));
        assertThat(children).containsEntry("from", 30).containsEntry("removed", 0);
        assertThat(ids(children.get("inserted"))).containsExactly((long) added.hashCode());
        assertThat(recordOf(addedFrame, added).state()).as("the new child is described").containsEntry("text", "added");

        composite.getChildren()[10].dispose();
        Map<String, Object> removed = listChange(recordOf(frameOf(composite), composite).state().get("children"));
        assertThat(removed).containsEntry("from", 10).containsEntry("removed", 1);
        assertThat(ids(removed.get("inserted"))).isEmpty();
    }

    @Test
    @DisplayName("a row added to three hundred is described in place inside the change to the rows")
    void rowAdded() {
        Table table = new Table(Mocks.shell(), SWT.NONE);
        new TableColumn(table, SWT.LEFT).setText("Name");
        for (int i = 0; i < 300; i++) new TableItem(table, SWT.NONE).setText("Row " + i);
        frameOf(table);

        TableItem row = new TableItem(table, SWT.NONE);
        row.setText("added");
        List<WireFrames.Record> frame = frameOf(table);

        WireFrames.Record change = recordOf(frame, table);
        assertThat(change.size()).as("bytes of the table's record, the new row in it: %s", change.state()).isLessThanOrEqualTo(ROW_ADDED_BUDGET);
        Map<String, Object> items = listChange(change.state().get("items"));
        assertThat(items).containsEntry("from", 300).containsEntry("removed", 0);
        assertThat(recordOf(frame, row).state().get("texts")).isEqualTo(List.of("added"));
    }

    @Test
    @DisplayName("a client that asks for the composite is given its whole list, and the next change is a change again")
    void askedForWholeAfterAChange() {
        Composite composite = new Composite(Mocks.shell(), SWT.NONE);
        for (int i = 0; i < 30; i++) new Label(composite, SWT.NONE).setText("Label " + i);
        frameOf(composite);
        new Label(composite, SWT.NONE).setText("added");
        listChange(recordOf(frameOf(composite), composite).state().get("children"));

        // Known to the client, as a widget a flush has delivered is; then asked for, as a client whose
        // list does not fit the change asks.
        composite.setData("dev.equo.swt.new", false);
        FlutterBridge.resendWidget(FlutterBridge.id(composite));
        bridge.comm.sent.clear();
        FlutterBridge.update();
        Map<String, Object> whole = bridge.comm.sent.stream().filter(f -> f.event.equals("Composite/" + composite.hashCode()))
                .map(RecordingComm.Frame::state).findFirst().orElseThrow();
        assertThat(whole).as("described whole").doesNotContainKey("_d");
        assertThat(ids(whole.get("children"))).hasSize(31);

        Label later = new Label(composite, SWT.NONE);
        Map<String, Object> next = listChange(recordOf(frameOf(composite), composite).state().get("children"));
        assertThat(next).containsEntry("from", 31).containsEntry("removed", 0);
        assertThat(ids(next.get("inserted"))).containsExactly((long) later.hashCode());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> listChange(Object list) {
        assertThat(list).as("a change to the list held").isInstanceOf(Map.class);
        return (Map<String, Object>) list;
    }

    @SuppressWarnings("unchecked")
    private static List<Long> ids(Object refs) {
        return ((List<Map<String, Object>>) refs).stream().map(r -> ((Number) r.get("id")).longValue()).toList();
    }

    private static WireFrames.Record recordOf(List<WireFrames.Record> records, Widget widget) {
        String id = "/" + widget.hashCode();
        for (WireFrames.Record record : records) if (record.channel().endsWith(id)) return record;
        throw new AssertionError("no record for " + widget + " in " + records);
    }

    /** The frame a flush writes for {@code widget}, credited as delivered. */
    private static List<WireFrames.Record> frameOf(Widget widget) {
        Serializer.Tape tape = new Serializer.Tape(CLIENT);
        try {
            tape.describe((DartWidget) widget.getImpl());
            return WireFrames.records(tape.frame().payload());
        } finally {
            tape.release();
        }
    }
}
