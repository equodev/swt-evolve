package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.WireOracle;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the client holds after any run of changes and flushes is what Java holds. An update carries
 * only what changed since what was sent, and leaves out what it remembers sending; a change that is
 * dropped there, or sent against a state the client was never given, leaves the client behind for
 * good, so every flush here is checked against each widget's state written out in full.
 *
 * <p>Seeded; {@code -Dstress.seed=N} and {@code -Dstress.rounds=N} replay or lengthen a run.
 */
@ExtendWith(Mocks.class)
class WireConvergenceTest {

    private static final long SEED = Long.getLong("stress.seed", 20261027L);
    private static final int ROUNDS = Integer.getInteger("stress.rounds", 400);
    private static final String[] TEXTS = {"", "a", "bb", "a longer text", "Ünïcödé ✓"};

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
    @DisplayName("after any run of changes and flushes, the client holds what Java holds")
    void theClientConvergesOnJavasState() {
        Random random = new Random(SEED);
        Shell shell = Mocks.shell();
        List<Control> widgets = List.of(
                sentAlready(new Label(shell, SWT.NONE)),
                sentAlready(new Label(shell, SWT.WRAP)),
                sentAlready(new Button(shell, SWT.CHECK)),
                sentAlready(new Button(shell, SWT.PUSH)),
                sentAlready(new Text(shell, SWT.SINGLE)),
                sentAlready(new Scale(shell, SWT.HORIZONTAL)));
        for (Control w : widgets) w.setToolTipText("start");
        FlutterBridge.update();

        Map<String, Map<String, Object>> client = new HashMap<>();
        List<String> log = new ArrayList<>();
        int seen = 0;
        for (int round = 0; round < ROUNDS; round++) {
            int changes = 1 + random.nextInt(5);
            for (int c = 0; c < changes; c++) {
                Control w = widgets.get(random.nextInt(widgets.size()));
                log.add(change(w, random));
            }
            FlutterBridge.update();
            List<RecordingComm.Frame> sent = bridge.comm.sent;
            for (; seen < sent.size(); seen++) apply(client, sent.get(seen));
            log.add("flush");
            for (Control w : widgets) {
                String channel = channelOf(w);
                Map<String, Object> held = client.get(channel);
                assertThat(held).as("seed %d round %d: %s was never described; changes so far %s",
                        SEED, round, channel, tail(log)).isNotNull();
                assertThat(normalized(held))
                        .as("seed %d round %d: what the client holds of %s; last changes %s", SEED, round, channel, tail(log))
                        .isEqualTo(normalized(full(w)));
            }
        }
    }

    @Test
    @DisplayName("after children come, go and move, and items are added, removed and changed, the client holds what Java holds")
    void theClientConvergesOnListsAndMoves() {
        Random random = new Random(SEED);
        Shell shell = Mocks.shell();
        List<Composite> parents = List.of(sentAlready(new Composite(shell, SWT.NONE)), sentAlready(new Composite(shell, SWT.NONE)));
        Table table = sentAlready(new Table(shell, SWT.MULTI));
        Tree tree = sentAlready(new Tree(shell, SWT.MULTI));
        for (Composite p : parents) p.setToolTipText("start");
        table.setToolTipText("start");
        tree.setToolTipText("start");
        FlutterBridge.update();

        Map<String, Map<String, Object>> client = new HashMap<>();
        List<String> log = new ArrayList<>();
        int seen = 0;
        for (int round = 0; round < ROUNDS; round++) {
            int changes = 1 + random.nextInt(4);
            for (int c = 0; c < changes; c++) {
                switch (random.nextInt(3)) {
                    case 0 -> log.add(changeChildren(parents, random));
                    case 1 -> log.add(changeItems(table, random));
                    default -> log.add(changeItems(tree, random));
                }
            }
            FlutterBridge.update();
            List<RecordingComm.Frame> sent = bridge.comm.sent;
            for (; seen < sent.size(); seen++) apply(client, sent.get(seen));
            log.add("flush");
            List<Widget> live = new ArrayList<>(List.of(table, tree));
            live.addAll(parents);
            for (Composite p : parents) live.addAll(List.of(p.getChildren()));
            live.addAll(List.of(table.getItems()));
            collect(tree.getItems(), live);
            for (Widget w : live) {
                String channel = channelOf(w);
                Map<String, Object> held = client.get(channel);
                assertThat(held).as("seed %d round %d: %s was never described; last changes %s",
                        SEED, round, channel, tail(log)).isNotNull();
                assertThat(normalized(held))
                        .as("seed %d round %d: what the client holds of %s; last changes %s", SEED, round, channel, tail(log))
                        .isEqualTo(normalized(full(w)));
            }
        }
    }

    /** Creates, disposes, moves or reorders a child of one of {@code parents}; returns what it did. */
    private static String changeChildren(List<Composite> parents, Random r) {
        Composite parent = parents.get(r.nextInt(parents.size()));
        Control[] children = parent.getChildren();
        int pick = children.length == 0 ? 0 : r.nextInt(5);
        switch (pick) {
            case 0 -> {
                Control child = r.nextBoolean() ? new Label(parent, SWT.NONE) : new Button(parent, SWT.PUSH);
                if (child instanceof Label l) l.setText(TEXTS[r.nextInt(TEXTS.length)]);
                return "new " + channelOf(child) + " in " + channelOf(parent);
            }
            case 1 -> {
                Control child = children[r.nextInt(children.length)];
                String name = channelOf(child);
                child.dispose();
                return "dispose " + name;
            }
            case 2 -> {
                Control child = children[r.nextInt(children.length)];
                Composite other = parents.get(0) == parent ? parents.get(1) : parents.get(0);
                child.setParent(other);
                return "move " + channelOf(child) + " to " + channelOf(other);
            }
            case 3 -> {
                Control child = children[r.nextInt(children.length)];
                child.moveAbove(null);
                return "raise " + channelOf(child);
            }
            default -> {
                Control child = children[r.nextInt(children.length)];
                child.setToolTipText(TEXTS[r.nextInt(TEXTS.length)]);
                return "toolTip of " + channelOf(child);
            }
        }
    }

    /** Adds, removes or changes a Table's items; returns what it did. */
    private static String changeItems(Table table, Random r) {
        int count = table.getItemCount();
        int pick = count == 0 ? 0 : r.nextInt(5);
        switch (pick) {
            case 0 -> {
                TableItem item = new TableItem(table, SWT.NONE, r.nextInt(count + 1));
                item.setText(TEXTS[r.nextInt(TEXTS.length)]);
                return "new " + channelOf(item);
            }
            case 1 -> {
                int index = r.nextInt(count);
                String name = channelOf(table.getItem(index));
                table.remove(index);
                return "remove " + name;
            }
            case 2 -> {
                TableItem item = table.getItem(r.nextInt(count));
                item.setText(TEXTS[r.nextInt(TEXTS.length)]);
                return "text of " + channelOf(item);
            }
            case 3 -> {
                int index = r.nextInt(count);
                table.setSelection(index);
                return "select " + index;
            }
            default -> {
                TableItem item = table.getItem(r.nextInt(count));
                item.setChecked(r.nextBoolean());
                return "checked of " + channelOf(item);
            }
        }
    }

    /** Adds, removes, expands or changes a Tree's items; returns what it did. */
    private static String changeItems(Tree tree, Random r) {
        List<Widget> items = new ArrayList<>();
        collect(tree.getItems(), items);
        int pick = items.isEmpty() ? 0 : r.nextInt(5);
        switch (pick) {
            case 0 -> {
                TreeItem item = items.isEmpty() || r.nextBoolean()
                        ? new TreeItem(tree, SWT.NONE)
                        : new TreeItem((TreeItem) items.get(r.nextInt(items.size())), SWT.NONE);
                item.setText(TEXTS[r.nextInt(TEXTS.length)]);
                return "new " + channelOf(item);
            }
            case 1 -> {
                TreeItem item = (TreeItem) items.get(r.nextInt(items.size()));
                String name = channelOf(item);
                item.dispose();
                return "dispose " + name;
            }
            case 2 -> {
                TreeItem item = (TreeItem) items.get(r.nextInt(items.size()));
                boolean v = r.nextBoolean();
                item.setExpanded(v);
                return "expanded " + v + " of " + channelOf(item);
            }
            case 3 -> {
                TreeItem item = (TreeItem) items.get(r.nextInt(items.size()));
                tree.setSelection(item);
                return "select " + channelOf(item);
            }
            default -> {
                TreeItem item = (TreeItem) items.get(r.nextInt(items.size()));
                item.setText(TEXTS[r.nextInt(TEXTS.length)]);
                return "text of " + channelOf(item);
            }
        }
    }

    private static void collect(TreeItem[] items, List<Widget> into) {
        for (TreeItem item : items) {
            into.add(item);
            collect(item.getItems(), into);
        }
    }

    /** Makes one random change to {@code w}; returns what it did. */
    private static String change(Control w, Random r) {
        int pick = r.nextInt(6);
        String what;
        switch (pick) {
            case 0 -> { boolean v = r.nextBoolean(); w.setEnabled(v); what = "enabled=" + v; }
            case 1 -> { String v = r.nextBoolean() ? null : TEXTS[r.nextInt(TEXTS.length)]; w.setToolTipText(v); what = "toolTip=" + v; }
            case 2 -> { int x = r.nextInt(3) * 10, y = r.nextInt(3) * 10, wd = 50 + r.nextInt(3) * 25; w.setBounds(x, y, wd, 20); what = "bounds=" + x + "," + y + "," + wd; }
            default -> what = changeOwn(w, r);
        }
        return w.getClass().getSimpleName() + "@" + w.hashCode() + " " + what;
    }

    /** A change to a property only {@code w}'s kind has. */
    private static String changeOwn(Control w, Random r) {
        if (w instanceof Label l) {
            if (r.nextBoolean()) { String v = TEXTS[r.nextInt(TEXTS.length)]; l.setText(v); return "text=" + v; }
            int[] alignments = {SWT.LEFT, SWT.CENTER, SWT.RIGHT};
            int v = alignments[r.nextInt(3)];
            l.setAlignment(v);
            return "alignment=" + v;
        }
        if (w instanceof Button b) {
            if (r.nextBoolean()) { boolean v = r.nextBoolean(); b.setSelection(v); return "selection=" + v; }
            String v = TEXTS[r.nextInt(TEXTS.length)];
            b.setText(v);
            return "text=" + v;
        }
        if (w instanceof Text t) {
            if (r.nextInt(3) == 0) { boolean v = r.nextBoolean(); t.setEditable(v); return "editable=" + v; }
            String v = TEXTS[r.nextInt(TEXTS.length)];
            t.setText(v);
            return "text=" + v;
        }
        if (w instanceof Scale s) {
            switch (r.nextInt(3)) {
                case 0 -> { int v = 50 + r.nextInt(3) * 50; s.setMaximum(v); return "maximum=" + v; }
                case 1 -> { int v = r.nextInt(4) * 10; s.setSelection(v); return "selection=" + v; }
                default -> { int v = 1 + r.nextInt(3); s.setIncrement(v); return "increment=" + v; }
            }
        }
        return "nothing";
    }

    /** What a record does to the client's copy of its widget: a whole one replaces it, a change sets what it names. */
    @SuppressWarnings("unchecked")
    private static void apply(Map<String, Map<String, Object>> client, RecordingComm.Frame frame) {
        if (frame.wire == 0 || !(frame.value instanceof Map)) return;
        Map<String, Object> record = (Map<String, Object>) frame.value;
        List<Object> named = (List<Object>) record.get("_d");
        if (named == null) {
            client.put(frame.event, new LinkedHashMap<>(record));
            return;
        }
        Map<String, Object> held = client.get(frame.event);
        assertThat(held).as("a change to %s, which the client was never described", frame.event).isNotNull();
        for (Object key : named) held.put((String) key, record.get(key));
    }

    /** Every property of {@code w}'s state, written out as the client reads them, defaults included. */
    private static Map<String, Object> full(Widget w) {
        return WireOracle.fields(((DartWidget) w.getImpl()).getValue());
    }

    /** The properties that say something: no bookkeeping, and none at the value the client starts from. */
    private static Map<String, Object> normalized(Map<String, Object> state) {
        Map<String, Object> out = new java.util.TreeMap<>();
        state.forEach((key, value) -> {
            if (key.startsWith("_") || key.equals("id") || key.equals("swt") || isDefault(value)) return;
            out.put(key, value);
        });
        return out;
    }

    private static boolean isDefault(Object v) {
        return v == null || Boolean.FALSE.equals(v) || "".equals(v)
                || (v instanceof Number n && n.doubleValue() == 0)
                || (v instanceof List<?> l && l.isEmpty()) || (v instanceof Map<?, ?> m && m.isEmpty());
    }

    private static String channelOf(Widget w) {
        return w.getClass().getSimpleName() + "/" + w.hashCode();
    }

    private static List<String> tail(List<String> log) {
        return log.subList(Math.max(0, log.size() - 20), log.size());
    }

    private static <T extends Widget> T sentAlready(T widget) {
        widget.setData("dev.equo.swt.new", false);
        return widget;
    }
}
