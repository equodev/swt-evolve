package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Records what one state change actually costs on the wire, per scenario: how many frames leave
 * Java, how many bytes they carry, and how many widget nodes are serialized inside them.
 *
 * <p>All three are exact integers with no timing component, so they are a stable reference point
 * rather than a measurement: the same scenario on the same sources yields the same numbers on any
 * machine. Run it before changing the delivery mechanism and the result is the "before" column that
 * cannot be reconstructed afterwards; run it after and the difference is the change, with no
 * benchmark noise in between.
 *
 * <p>The scenarios cover both the cases a per-property protocol is meant to improve (one leaf
 * property, one child inside a large subtree) and the cases it must not make worse (a first
 * delivery, an everything-changed flush).
 *
 * <p>Numbers are printed and written to {@code build/delivery-census.json}. Nothing is asserted:
 * this records the baseline, it does not police it.
 */
@ExtendWith(Mocks.class)
class DeliveryCensusTest {

    /** Widget count of the "large subtree" scenarios — big enough that re-sending it is visible. */
    private static final int LARGE_SUBTREE_LEAVES = 500;
    private static final int TABLE_ROWS = 1000;
    private static final int NEW_SUBTREE_LEAVES = 200;

    private static final Map<String, Object> census = new LinkedHashMap<>();

    private RecordingBridge bridge;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() throws IOException {
        Config.defaultToEclipse();
        report();
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

    // ---------------- scenarios ----------------

    @Test
    @DisplayName("1 - one property on a leaf Label")
    void oneLeafProperty() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        label.setText("before");
        settle(parent);

        label.setText("after");
        record("leaf_property", flush());
    }

    @Test
    @DisplayName("2 - one child changed inside a large subtree")
    void oneChildInLargeSubtree() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        List<Label> leaves = leaves(parent, LARGE_SUBTREE_LEAVES);
        settle(parent);

        leaves.get(LARGE_SUBTREE_LEAVES / 2).setText("after");
        record("child_in_large_subtree", flush());
    }

    @Test
    @DisplayName("2b - one child added to an established subtree")
    void oneChildAddedToLargeSubtree() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        leaves(parent, LARGE_SUBTREE_LEAVES);
        settle(parent);

        Label added = new Label(parent, SWT.NONE);
        added.setText("added");
        record("child_added_to_large_subtree", flush());
    }

    @Test
    @DisplayName("2c - one child removed from an established subtree")
    void oneChildRemovedFromLargeSubtree() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        List<Label> leaves = leaves(parent, LARGE_SUBTREE_LEAVES);
        settle(parent);

        leaves.get(LARGE_SUBTREE_LEAVES / 2).dispose();
        record("child_removed_from_large_subtree", flush());
    }

    @Test
    @DisplayName("3 - a layout pass over a large subtree")
    void layoutPass() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        List<Label> leaves = leaves(parent, LARGE_SUBTREE_LEAVES);
        settle(parent);

        int y = 0;
        for (Label leaf : leaves) leaf.setBounds(new Rectangle(0, y += 20, 100, 18));
        record("layout_pass", flush());
    }

    @Test
    @DisplayName("4 - one cell changed in a 1000-row Table")
    void oneCellInLargeTable() {
        Shell shell = Mocks.swtShell();
        Table table = new Table(shell, SWT.NONE);
        List<TableItem> rows = new ArrayList<>();
        for (int i = 0; i < TABLE_ROWS; i++) {
            TableItem row = new TableItem(table, SWT.NONE);
            row.setText("row " + i);
            rows.add(row);
        }
        settle(table);

        rows.get(TABLE_ROWS / 2).setText("changed");
        record("cell_in_large_table", flush());
    }

    @Test
    @DisplayName("5 - first delivery of a new subtree")
    void firstDeliveryOfNewSubtree() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        settle(parent);

        // Created after the parent is established, so this measures a first delivery riding its
        // already-sent ancestor - the boot-shaped case reachable without the Display backend.
        leaves(parent, NEW_SUBTREE_LEAVES);
        record("first_delivery_new_subtree", flush());
    }

    @Test
    @DisplayName("6 - worst case: every leaf changed")
    void everythingChanged() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        List<Label> leaves = leaves(parent, LARGE_SUBTREE_LEAVES);
        settle(parent);

        for (int i = 0; i < leaves.size(); i++) leaves.get(i).setText("changed " + i);
        record("all_leaves_changed", flush());
    }


    @Test
    @DisplayName("7 - one property on a property-rich leaf, per widget type")
    void leafPropertyByWidgetType() {
        System.out.println("--- one setToolTipText() on an established leaf, by widget type ---");
        System.out.printf("%-14s %-10s %-8s %-12s %-10s%n",
                "widget", "full", "props", "diff (est)", "saving");
        for (String type : PROPERTY_RICH_LEAVES) {
            measureSingleProperty(type);
        }
    }

    /**
     * Leaf widgets spanning the property-count range, from the smallest thing SWT has to the
     * widest. A per-property protocol pays off in proportion to how much of a payload is NOT the
     * property that changed, so the spread across these is the actual answer to "is a leaf diff
     * worth it" - a Label alone would answer only for the cheapest case.
     */
    private static final List<String> PROPERTY_RICH_LEAVES =
            List.of("Label", "Button", "Text", "Combo", "Spinner", "Scale", "ProgressBar",
                    "List", "Canvas", "Group", "Table", "Tree", "ToolBar", "StyledText");

    private void measureSingleProperty(String type) {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        Control leaf = newLeaf(type, parent);
        if (leaf == null) {
            System.out.printf("%-14s (not constructible in this backend)%n", type);
            return;
        }
        settle(parent);

        leaf.setToolTipText("tip");
        List<RecordingComm.Frame> frames = flush();
        if (frames.isEmpty()) {
            System.out.printf("%-14s (no frame - setToolTipText does not mark this widget dirty)%n", type);
            return;
        }
        String payload = normalize(frames.get(0).json);
        int full = payload.getBytes(StandardCharsets.UTF_8).length;
        int props = topLevelPropertyCount(frames.get(0).json);

        // What the same update would cost keyed by name: identity, the changed-key list, and the
        // one property. Constructed rather than guessed, so the ratio below is arithmetic on real
        // payloads instead of an estimate.
        String asDiff = "{\"id\":0000000000,\"swt\":\"" + type + "\",\"_s\":0000000000,"
                + "\"_d\":[\"toolTipText\"],\"toolTipText\":\"tip\"}";
        int diff = asDiff.getBytes(StandardCharsets.UTF_8).length;

        System.out.printf("%-14s %-10d %-8d %-12d %.0f%%%n",
                type, full, props, diff, 100.0 * (full - diff) / full);

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("frames", 1);
        entry.put("bytes", full);
        entry.put("raw_bytes", frames.get(0).json.getBytes(StandardCharsets.UTF_8).length);
        entry.put("widget_nodes_serialized", countNodes(frames.get(0).json));
        entry.put("top_level_properties", props);
        entry.put("projected_diff_bytes", diff);
        census.put("leaf_property_" + type, entry);
    }

    private Control newLeaf(String type, Composite parent) {
        try {
            return switch (type) {
                case "Label" -> new Label(parent, SWT.NONE);
                case "Button" -> new Button(parent, SWT.PUSH);
                case "Text" -> new Text(parent, SWT.NONE);
                case "Combo" -> new Combo(parent, SWT.NONE);
                case "Spinner" -> new Spinner(parent, SWT.NONE);
                case "Scale" -> new Scale(parent, SWT.NONE);
                case "ProgressBar" -> new ProgressBar(parent, SWT.NONE);
                case "List" -> new org.eclipse.swt.widgets.List(parent, SWT.NONE);
                case "Canvas" -> new Canvas(parent, SWT.NONE);
                case "Group" -> new Group(parent, SWT.NONE);
                case "Table" -> new Table(parent, SWT.NONE);
                case "Tree" -> new Tree(parent, SWT.NONE);
                case "ToolBar" -> new ToolBar(parent, SWT.NONE);
                case "StyledText" -> new org.eclipse.swt.custom.StyledText(parent, SWT.NONE);
                default -> null;
            };
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Keys of the payload's own object, not counting nested ones - the width of this widget. */
    private static int topLevelPropertyCount(String json) {
        int depth = 0;
        int count = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) { escaped = false; continue; }
            if (c == '\\') { escaped = true; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (inString) continue;
            if (c == '{' || c == '[') depth++;
            else if (c == '}' || c == ']') depth--;
            else if (c == ':' && depth == 1) count++;
        }
        return count;
    }

    // ---------------- harness ----------------

    private List<Label> leaves(Composite parent, int count) {
        List<Label> leaves = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Label leaf = new Label(parent, SWT.NONE);
            leaf.setText("leaf " + i);
            leaves.add(leaf);
        }
        return leaves;
    }

    /**
     * Brings the tree to the state the scenarios measure from: everything already delivered once,
     * nothing pending. A widget is "new" until it has been through a flush, and a new widget is
     * carried by its parent rather than sent on its own channel, so measuring an update before
     * this point would measure a first delivery instead.
     */
    private void settle(Widget root) {
        FlutterBridge.update();
        markSent(root);
        // Actually deliver the tree once. A widget that has never been written out has nothing on
        // the far side for a later update to be relative to, so it would be sent whole however it
        // was flushed - and the measurement would compare a first delivery against a first
        // delivery. Writing the root walks its children, so the whole subtree counts as delivered.
        try {
            new dev.equo.swt.Serializer().to(root);
            dev.equo.swt.Serializer.markDelivered();
        } catch (java.io.IOException e) {
            throw new AssertionError("could not establish the delivered state for " + root, e);
        }
        bridge.comm.sent.clear();
    }

    private void markSent(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (widget instanceof Composite composite)
            for (Control child : composite.getChildren()) markSent(child);
        if (widget instanceof Table table)
            for (TableItem item : table.getItems()) markSent(item);
    }

    private List<RecordingComm.Frame> flush() {
        FlutterBridge.update();
        return new ArrayList<>(bridge.comm.sent);
    }

    // ---------------- measurement ----------------

    private static void record(String scenario, List<RecordingComm.Frame> frames) {
        int bytes = 0;
        int rawBytes = 0;
        int nodes = 0;
        List<String> channels = new ArrayList<>();
        for (RecordingComm.Frame frame : frames) {
            rawBytes += frame.json.getBytes(StandardCharsets.UTF_8).length;
            bytes += normalize(frame.json).getBytes(StandardCharsets.UTF_8).length;
            nodes += countNodes(frame.json);
            channels.add(frame.event);
        }
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("frames", frames.size());
        entry.put("bytes", bytes);
        entry.put("raw_bytes", rawBytes);
        entry.put("widget_nodes_serialized", nodes);
        entry.put("channels", channels);
        census.put(scenario, entry);

        System.out.printf("%-30s frames=%-4d bytes=%-8d nodes=%-5d%n",
                scenario, frames.size(), bytes, nodes);
        if (!frames.isEmpty()) {
            String sample = frames.get(0).json;
            System.out.println("    first frame on " + frames.get(0).event + ": "
                    + sample.substring(0, Math.min(300, sample.length())));
        }
    }

    /**
     * Pads the payload's fixture noise to a fixed width so a byte count measures the delivery
     * mechanism rather than this run's accidents. Three sources vary run to run without anything
     * changing: widget ids are {@code hashCode()}s, the write stamp grows over a run, and the mocked
     * colors are random in 1..255 - each of them 1 to 10 digits wide. Without this the same scenario
     * reports a different size on every run and no size can ever be asserted.
     */
    private static String normalize(String json) {
        return json.replaceAll("\"(id|seq|red|green|blue|alpha)\":\\d+", "\"$1\":0000000000");
    }

    /**
     * Widget nodes inside a payload, counted by the {@code "swt"} type key every serialized widget
     * carries. A nested child costs one, so this separates "one small frame" from "one frame that
     * happens to contain the whole subtree" - which frame and byte counts alone cannot.
     */
    private static int countNodes(String json) {
        int count = 0;
        for (int i = json.indexOf("\"swt\":"); i >= 0; i = json.indexOf("\"swt\":", i + 1)) count++;
        return count;
    }

    private static void report() throws IOException {
        StringBuilder json = new StringBuilder("{\n");
        json.append("  \"mode\": \"full-send baseline\",\n");
        json.append("  \"scenarios\": {\n");
        int remaining = census.size();
        for (Map.Entry<String, Object> scenario : census.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> values = (Map<String, Object>) scenario.getValue();
            json.append("    \"").append(scenario.getKey()).append("\": {")
                    .append("\"frames\": ").append(values.get("frames"))
                    .append(", \"bytes\": ").append(values.get("bytes"))
                    .append(", \"raw_bytes\": ").append(values.get("raw_bytes"))
                    .append(", \"widget_nodes_serialized\": ").append(values.get("widget_nodes_serialized"))
                    .append("}").append(--remaining > 0 ? "," : "").append("\n");
        }
        json.append("  }\n}\n");

        Path out = Paths.get("build", "delivery-census.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, json.toString(), StandardCharsets.UTF_8);
        System.out.println("delivery census written to " + out.toAbsolutePath());
    }
}
