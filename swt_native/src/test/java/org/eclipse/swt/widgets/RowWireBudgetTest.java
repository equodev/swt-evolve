package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
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
 * What new table and tree rows cost on the wire: a row the client lacks is described inside the list
 * that names it, and a text-only row carries its texts (and its first text, which tests read off the
 * client's state) and nothing the client never reads off a row, such as its style.
 */
@ExtendWith(Mocks.class)
class RowWireBudgetTest {

    private static final int CLIENT = 1;

    /** Bytes per new text-only row of a two-column table, its texts included. */
    private static final int TABLE_ROW_BUDGET = 38;

    /** Bytes per new text-only child of an expanded tree item, its text included. */
    private static final int TREE_ROW_BUDGET = 19;

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
        Serializer.enterConnection(CLIENT);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("400 new text-only rows of a two-column table stay within the per-row budget")
    void newTableRows() {
        Table table = new Table(Mocks.shell(), SWT.NONE);
        new TableColumn(table, SWT.LEFT).setText("Name");
        new TableColumn(table, SWT.LEFT).setText("Value");
        frameOf((DartWidget) table.getImpl());

        for (int i = 0; i < 400; i++) new TableItem(table, SWT.NONE).setText(new String[] {"Row " + i, "Value " + i});
        Frame filled = frameOf((DartWidget) table.getImpl());

        assertThat(filled.bytes / 400.0).isLessThanOrEqualTo(TABLE_ROW_BUDGET);
        TableItem row = table.getItem(123);
        Map<String, Object> described = filled.stateOf(row);
        assertThat(described).containsEntry("swt", "TableItem").doesNotContainKey("style");
        assertThat(described.get("texts")).isEqualTo(List.of("Row 123", "Value 123"));

        row.setText(1, "changed");
        Map<String, Object> change = WireFrames.record((DartWidget) row.getImpl(), CLIENT);
        assertThat(change).containsKey("_d");
        assertThat(change.get("texts")).isEqualTo(List.of("Row 123", "changed"));

        TableItem added = new TableItem(table, SWT.NONE);
        added.setText("added");
        Frame more = frameOf((DartWidget) table.getImpl());
        assertThat(more.records.stream().filter(r -> r.channel().startsWith("TableItem/")).map(WireFrames.Record::channel))
                .containsExactly("TableItem/" + added.hashCode());
    }

    @Test
    @DisplayName("200 new children of an expanded tree item stay within the per-row budget")
    void newTreeChildren() {
        Tree tree = new Tree(Mocks.shell(), SWT.NONE);
        TreeItem parent = new TreeItem(tree, SWT.NONE);
        parent.setText("parent");
        new TreeItem(parent, SWT.NONE).setText("placeholder");
        parent.setExpanded(true);
        frameOf((DartWidget) tree.getImpl());
        parent.getItem(0).dispose();
        frameOf((DartWidget) parent.getImpl());

        for (int i = 0; i < 200; i++) new TreeItem(parent, SWT.NONE).setText("Child " + i);
        Frame filled = frameOf((DartWidget) parent.getImpl());

        assertThat(filled.bytes / 200.0).isLessThanOrEqualTo(TREE_ROW_BUDGET);
        TreeItem child = parent.getItem(77);
        Map<String, Object> described = filled.stateOf(child);
        assertThat(described).containsEntry("swt", "TreeItem").containsEntry("text", "Child 77").doesNotContainKey("style");

        child.setText("changed");
        Map<String, Object> change = WireFrames.record((DartWidget) child.getImpl(), CLIENT);
        assertThat(change).containsKey("_d").containsEntry("text", "changed");
    }

    private record Frame(int bytes, List<WireFrames.Record> records) {
        Map<String, Object> stateOf(Widget widget) {
            String id = "/" + widget.hashCode();
            for (WireFrames.Record record : records) if (record.channel().endsWith(id)) return record.state();
            throw new AssertionError("no record for " + widget);
        }
    }

    /** The frame a flush writes for {@code impl}, credited as delivered. */
    private static Frame frameOf(DartWidget impl) {
        Serializer.Tape tape = new Serializer.Tape(CLIENT);
        try {
            tape.describe(impl);
            byte[] payload = tape.frame().payload();
            return new Frame(payload.length, WireFrames.records(payload));
        } finally {
            tape.release();
        }
    }
}
