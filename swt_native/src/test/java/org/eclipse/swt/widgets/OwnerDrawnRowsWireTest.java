package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.BatchEntries;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a step of a sash drag or a wheel notch sends for the owner-drawn rows of a Tree or Table, the
 * rows drawn by a PaintItem listener that lays its text out against the cell, as a styled label
 * provider does. A row repainted with a GC that differs from the one before it only in where the row
 * now starts sends that, not the GC whole; a row a scroll only moves keeps its overlay and is not
 * repainted.
 */
@Tag("flutter-it")
class OwnerDrawnRowsWireTest {

    private static final int ROWS = 80;
    /** Bytes a repainted row may take when only its transform changed: the paint itself, and the change. */
    private static final int ROW_REPAINT_BUDGET = 64;
    /** Bytes a row whose overlay just mounted may take: its paint, and the GC whole. */
    private static final int MOUNTED_ROW_BUDGET = 130;

    /** How many rows have an overlay on the client: the ones on screen and the two its list builds below them. */
    private int mounted;
    private RecordingBridge bridge;
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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void aSashStepSendsEachTreeRowsNewTransformAndNotItsWholeGc() {
        Tree tree = ownerDrawnTree();
        List<TreeItem> rows = List.of(tree.getItems());

        // The tree's edge gap is a share of its width, so each row starts somewhere else.
        tree.setBounds(0, 0, 310, 200);
        flush();

        Set<Integer> painted = painted(rows);
        assertThat(painted).as("the rows on screen are repainted").isNotEmpty();
        for (Map<String, Object> state : states()) {
            assertThat(WireValues.mentions(state, "_whole")).as("a change, not the GC whole: %s", state).isFalse();
            assertThat(WireValues.mentions(state, "transform")).as("%s", state).isTrue();
            assertThat(WireValues.mentions(state, "font")).as("%s", state).isFalse();
        }
        assertThat(gcBytes()).isLessThanOrEqualTo(ROW_REPAINT_BUDGET * painted.size());
    }

    @Test
    void aWheelNotchRepaintsOnlyTheTreeRowsItBringsIn() {
        Tree tree = ownerDrawnTree();
        List<TreeItem> rows = List.of(tree.getItems());
        // A repaint just before leaves rows to name, which a scroll names as it brings them on screen.
        tree.redraw();
        flush();
        bridge.comm.sent.clear();
        assertThat(((DartTree) tree.getImpl()).ownerDrawRowsLeft).isNotNull();

        tree.setTopItem(rows.get(3));
        flush();
        scrolled(tree, 3 * tree.getItemHeight());
        // The rows the scroll brought into the client's list mount their overlays.
        listening(tree, rows.subList(mounted, mounted + 3));
        flush();

        assertThat(painted(rows)).as("only the rows the client mounted, never a row the notch only moved")
                .containsOnly(mounted, mounted + 1, mounted + 2);
        assertThat(gcBytes()).isLessThanOrEqualTo(3 * MOUNTED_ROW_BUDGET);
    }

    @Test
    void aWheelNotchRepaintsOnlyTheTableRowsItBringsIn() {
        Table table = ownerDrawnTable();
        List<TableItem> rows = List.of(table.getItems());
        table.redraw();
        flush();
        bridge.comm.sent.clear();
        assertThat(((DartTable) table.getImpl()).ownerDrawRowsLeft).isNotNull();

        table.setTopIndex(3);
        flush();
        scrolled(table, 3 * table.getItemHeight());
        listening(table, rows.subList(mounted, mounted + 3));
        flush();

        assertThat(painted(rows)).as("only the rows the client mounted, never a row the notch only moved")
                .containsOnly(mounted, mounted + 1, mounted + 2);
        assertThat(gcBytes()).isLessThanOrEqualTo(3 * MOUNTED_ROW_BUDGET);
    }

    @Test
    void aRedrawAfterASidewaysScrollSendsEachTableRowsNewTransform() {
        Table table = ownerDrawnTable();
        List<TableItem> rows = List.of(table.getItems());
        DartTable impl = (DartTable) table.getImpl();
        while (impl.ownerDrawRowsLeft != null) ((OwnerDrawRowsLeft) impl.ownerDrawRowsLeft).run();
        flush();
        // The overlays scroll sideways with their rows: nothing is repainted until the table is.
        scrolled(table, 0, 40);
        assertThat(painted(rows)).isEmpty();
        bridge.comm.sent.clear();

        table.redraw();
        flush();

        Set<Integer> painted = painted(rows);
        assertThat(painted).as("the rows on screen are repainted").isNotEmpty();
        for (Map<String, Object> state : states()) {
            assertThat(WireValues.mentions(state, "_whole")).as("a change, not the GC whole: %s", state).isFalse();
            assertThat(WireValues.mentions(state, "transform")).as("%s", state).isTrue();
        }
        assertThat(gcBytes()).isLessThanOrEqualTo(ROW_REPAINT_BUDGET * painted.size());
    }

    @Test
    void aRowWhoseOverlayMountedAgainIsDescribedWhole() {
        Tree tree = ownerDrawnTree();
        List<TreeItem> rows = List.of(tree.getItems());

        listening(tree, rows.subList(2, 3));
        flush();

        assertThat(painted(rows)).containsOnly(2);
        assertThat(states()).singleElement()
                .satisfies(state -> assertThat(WireValues.mentions(state, "_whole")).isTrue());
    }

    /** An owner-drawn tree laid out and painted, the overlays of its first {@link #mounted} rows listening. */
    private Tree ownerDrawnTree() {
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.V_SCROLL);
        tree.setBounds(0, 0, 300, 200);
        tree.addListener(SWT.MeasureItem, e -> { });
        tree.addListener(SWT.PaintItem, e -> {
            TreeItem item = (TreeItem) e.item;
            Rectangle text = item.getTextBounds(e.index);
            Rectangle image = item.getImageBounds(e.index);
            e.gc.drawText(item.getText(e.index), text.x + image.width, text.y, true);
        });
        List<TreeItem> rows = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            TreeItem row = new TreeItem(tree, SWT.NONE);
            row.setText("row " + i);
            rows.add(row);
        }
        flush();
        mounted = tree.getClientArea().height / tree.getItemHeight() + 2;
        listening(tree, rows.subList(0, mounted));
        flush();
        bridge.comm.sent.clear();
        return tree;
    }

    private Table ownerDrawnTable() {
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.V_SCROLL | SWT.H_SCROLL);
        table.setBounds(0, 0, 300, 200);
        new TableColumn(table, SWT.NONE).setWidth(400);
        table.addListener(SWT.MeasureItem, e -> { });
        table.addListener(SWT.PaintItem, e -> {
            TableItem item = (TableItem) e.item;
            Rectangle text = item.getTextBounds(e.index);
            e.gc.drawText(item.getText(e.index), text.x, text.y, true);
        });
        List<TableItem> rows = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            TableItem row = new TableItem(table, SWT.NONE);
            row.setText("row " + i);
            rows.add(row);
        }
        flush();
        mounted = table.getClientArea().height / table.getItemHeight() + 2;
        listening(table, rows.subList(0, mounted));
        flush();
        bridge.comm.sent.clear();
        return table;
    }

    /** The indexes of the rows the frames sent so far paint. */
    private Set<Integer> painted(List<? extends Item> rows) {
        Set<Integer> painted = new LinkedHashSet<>();
        for (GcFrames.Entry entry : BatchEntries.of(bridge.comm.sent)) {
            for (int i = 0; i < rows.size(); i++) {
                if (entry.channel().startsWith("GC/" + rows.get(i).hashCode() + "/")) painted.add(i);
            }
        }
        return painted;
    }

    /** The GC state records sent so far. */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> states() {
        List<Map<String, Object>> states = new ArrayList<>();
        for (GcFrames.Entry entry : BatchEntries.of(bridge.comm.sent)) {
            if (entry.channel().startsWith("GC/") && entry.channel().indexOf('/', 3) < 0
                    && entry.value() instanceof Map) {
                states.add((Map<String, Object>) entry.value());
            }
        }
        return states;
    }

    /** What the GC frames sent so far take on the wire. */
    private int gcBytes() {
        int bytes = 0;
        for (RecordingComm.Frame frame : bridge.comm.sent) {
            if (frame.wire == 0) bytes += frame.size;
        }
        return bytes;
    }

    private void scrolled(Control parent, int y) {
        scrolled(parent, y, 0);
    }

    /** The client scrolled {@code parent} to {@code y} down and {@code x} sideways. */
    private void scrolled(Control parent, int y, int x) {
        Event e = new Event();
        e.y = y;
        e.x = x;
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) parent.getImpl(), "Scroll", "Scroll"), e);
        flush();
    }

    /** The client mounted the overlays of {@code rows}. */
    private void listening(Control parent, List<? extends Item> rows) {
        Event e = new Event();
        e.segments = rows.stream().mapToInt(Object::hashCode).toArray();
        String kind = parent instanceof Tree ? "Tree/" : "Table/";
        bridge.comm.fireContaining(kind + parent.hashCode() + "/PaintItem/PaintItem", e);
    }

    private void flush() {
        while (display.readAndDispatch()) {
            // run the handlers' asyncExec and flush
        }
    }
}
