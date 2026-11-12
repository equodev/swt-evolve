package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import dev.equo.swt.harness.WireFrames;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code column.setWidth(0)} is how an application hides a column: native SWT gives it no space.
 * A width of 0 is also the type default a whole description leaves out, so the client must still
 * receive it explicitly — otherwise it cannot tell a hidden column from one it never got a width for.
 */
@Tag("flutter-it")
class ZeroWidthColumnFlutterTest {

    private Display display;
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
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    @Test
    void aHiddenTableColumnIsDescribedAsZeroWideAndNotResizable() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        Table table = new Table(shell, SWT.BORDER);
        table.setHeaderVisible(true);
        TableColumn hidden = null;
        for (String name : new String[] { "Name", "Masked", "Value" }) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(name);
            column.setWidth(80);
            if (name.equals("Masked")) hidden = column;
        }
        hidden.setWidth(0);
        hidden.setResizable(false);

        Map<String, Object> described = WireFrames.describe(hidden);
        assertThat(described).as("a column hidden with setWidth(0)").containsEntry("width", 0);
        assertThat(described).as("a column pinned with setResizable(false)").containsEntry("resizable", false);
    }

    @Test
    void aTableColumnHiddenAfterItWasShownSendsZeroWidthAndNotResizable() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        Table table = new Table(shell, SWT.BORDER);
        table.setHeaderVisible(true);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText("Masked");
        column.setWidth(80);
        shell.setSize(400, 300);
        shell.open();
        FlutterBridge.update().join();
        bridge.comm.sent.clear();

        column.setWidth(0);
        column.setResizable(false);
        FlutterBridge.update().join();

        assertThat(lastSent("TableColumn/" + FlutterBridge.id(column)))
                .containsEntry("width", 0).containsEntry("resizable", false);
    }

    @Test
    void aTableRunsNoOwnerDrawListenerForAHiddenColumn() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        Table table = new Table(shell, SWT.BORDER);
        for (String name : new String[] { "Name", "Masked", "Value" }) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(name);
            column.setWidth(name.equals("Masked") ? 0 : 80);
        }
        Map<Integer, List<Integer>> cells = recordOwnerDraw(table);
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "host", "secret", "example" });
        shell.setSize(400, 300);
        shell.open();

        WireFrames.describe(item);
        OwnerDrawOverlay.paint(table, item, table.getColumnCount());

        assertNoListenerRanForColumn(cells, 1);
    }

    @Test
    void aTreeRunsNoOwnerDrawListenerForAHiddenColumn() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        Tree tree = new Tree(shell, SWT.BORDER);
        for (String name : new String[] { "Name", "Masked", "Value" }) {
            TreeColumn column = new TreeColumn(tree, SWT.NONE);
            column.setText(name);
            column.setWidth(name.equals("Masked") ? 0 : 80);
        }
        Map<Integer, List<Integer>> cells = recordOwnerDraw(tree);
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(new String[] { "host", "secret", "example" });
        shell.setSize(400, 300);
        shell.open();

        WireFrames.describe(item);
        OwnerDrawOverlay.paint(tree, item, tree.getColumnCount());

        assertNoListenerRanForColumn(cells, 1);
    }

    @Test
    void anOwnerDrawnRowIsDrawnAgainWhenAColumnGetsAWidth() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        Table table = new Table(shell, SWT.BORDER);
        new TableColumn(table, SWT.NONE).setWidth(80);
        TableColumn late = new TableColumn(table, SWT.NONE);
        List<Integer> painted = new ArrayList<>();
        table.addListener(SWT.PaintItem, e -> painted.add(e.index));
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "host", "example" });
        WireFrames.describe(item);
        // The row is on screen: captured, then painted ahead into its overlay, so column 0 runs twice.
        assertThat(painted).as("a column of width 0 is not painted").containsOnly(0);
        // Sent, as a row is by the time a layout sizes its columns: a row still queued is not queued again.
        FlutterBridge.update().join();
        bridge.dirtied.clear();

        late.setWidth(80);

        assertThat(bridge.dirtied).as("a column a layout sizes later has its rows drawn again")
                .contains(item.getImpl());
        painted.clear();
        WireFrames.describe(item);
        assertThat(painted).containsExactly(0, 1);
    }

    @Test
    void anOwnerDrawnTreeRowIsDrawnAgainWhenAColumnGetsAWidth() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        Tree tree = new Tree(shell, SWT.BORDER);
        new TreeColumn(tree, SWT.NONE).setWidth(80);
        TreeColumn late = new TreeColumn(tree, SWT.NONE);
        List<Integer> painted = new ArrayList<>();
        tree.addListener(SWT.PaintItem, e -> painted.add(e.index));
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(new String[] { "host", "example" });
        WireFrames.describe(item);
        // The row is on screen: captured, then painted ahead into its overlay, so column 0 runs twice.
        assertThat(painted).as("a column of width 0 is not painted").containsOnly(0);
        // Sent, as a row is by the time a layout sizes its columns: a row still queued is not queued again.
        FlutterBridge.update().join();
        bridge.dirtied.clear();

        late.setWidth(80);

        assertThat(bridge.dirtied).as("a column a layout sizes later has its rows drawn again")
                .contains(item.getImpl());
        painted.clear();
        WireFrames.describe(item);
        assertThat(painted).containsExactly(0, 1);
    }

    @Test
    void aTreeColumnHiddenAfterItWasShownSendsZeroWidthAndNotResizable() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        Tree tree = new Tree(shell, SWT.BORDER);
        tree.setHeaderVisible(true);
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.setText("Masked");
        column.setWidth(80);
        shell.setSize(400, 300);
        shell.open();
        FlutterBridge.update().join();
        bridge.comm.sent.clear();

        column.setWidth(0);
        column.setResizable(false);
        FlutterBridge.update().join();

        assertThat(lastSent("TreeColumn/" + FlutterBridge.id(column)))
                .containsEntry("width", 0).containsEntry("resizable", false);
    }

    /** The column index of every MeasureItem, EraseItem and PaintItem {@code parent} sends, by event type. */
    private static Map<Integer, List<Integer>> recordOwnerDraw(Control parent) {
        Map<Integer, List<Integer>> cells = new java.util.HashMap<>();
        for (int type : new int[] { SWT.MeasureItem, SWT.EraseItem, SWT.PaintItem }) {
            List<Integer> indexes = new ArrayList<>();
            cells.put(type, indexes);
            parent.addListener(type, e -> indexes.add(e.index));
        }
        return cells;
    }

    private static void assertNoListenerRanForColumn(Map<Integer, List<Integer>> cells, int hidden) {
        for (Map.Entry<Integer, List<Integer>> e : cells.entrySet()) {
            assertThat(e.getValue()).as("cells sent event type %d", e.getKey()).isNotEmpty().doesNotContain(hidden);
        }
    }

    private Map<String, Object> lastSent(String channel) {
        Map<String, Object> last = null;
        for (RecordingComm.Frame frame : bridge.comm.sent) {
            if (frame.event.equals(channel)) last = frame.state();
        }
        assertThat(last).as("the change to %s reaches the client: %s", channel, bridge.comm.sent).isNotNull();
        return last;
    }

    @Test
    void aHiddenTreeColumnIsDescribedAsZeroWideAndNotResizable() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        Tree tree = new Tree(shell, SWT.BORDER);
        tree.setHeaderVisible(true);
        TreeColumn hidden = null;
        for (String name : new String[] { "Name", "Masked", "Value" }) {
            TreeColumn column = new TreeColumn(tree, SWT.NONE);
            column.setText(name);
            column.setWidth(80);
            if (name.equals("Masked")) hidden = column;
        }
        hidden.setWidth(0);
        hidden.setResizable(false);

        Map<String, Object> described = WireFrames.describe(hidden);
        assertThat(described).as("a column hidden with setWidth(0)").containsEntry("width", 0);
        assertThat(described).as("a column pinned with setResizable(false)").containsEntry("resizable", false);
    }
}
