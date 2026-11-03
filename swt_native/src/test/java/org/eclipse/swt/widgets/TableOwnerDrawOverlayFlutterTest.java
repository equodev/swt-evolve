package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.BatchEntries;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An owner-drawn row is shown as its listeners drew it, not as the one string a capture can name.
 *
 * <p>The shape is a git client's branch list: each row's {@link SWT#PaintItem} listener draws a
 * status icon as a filled rounded rectangle, then the branch name in a bold font, at the cell
 * bounds it reads from {@link TableItem#getBounds(int)}. Once Flutter reports a row's overlay
 * listening, that drawing has to reach the row's own GC channel, in the row's coordinates, as a
 * complete paint — the icon and the font included.
 */
@Tag("flutter-it")
class TableOwnerDrawOverlayFlutterTest {

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
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    @Test
    void rowDrawing_reachesTheRowsOwnOverlay_onceItListens() {
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.NONE);
        table.setBounds(0, 0, 200, 200);
        Color green = new Color(display, 96, 160, 128);
        Color light = new Color(display, 223, 222, 222);
        Font bold = new Font(display, new FontData("system", 11, SWT.BOLD));
        table.addListener(SWT.EraseItem, event -> { });
        table.addListener(SWT.PaintItem, event -> {
            TableItem row = (TableItem) event.item;
            int x = row.getBounds(event.index).x;
            int y = row.getBounds(event.index).y;
            event.gc.setBackground(green);
            event.gc.fillRoundRectangle(x + 2, y + 4, 13, 17, 4, 4);
            event.gc.setFont(bold);
            // The label takes the color native SWT set on the GC: the row's foreground.
            event.gc.setBackground(table.getBackground());
            event.gc.drawString(row.getData().toString(), x + 20, y + 2, true);
        });

        TableItem first = new TableItem(table, SWT.NONE);
        first.setData("stable");
        TableItem second = new TableItem(table, SWT.NONE);
        second.setData("check_0710");
        second.setForeground(light);
        int rowY = second.getBounds(0).y;
        assertThat(rowY).as("the second row sits below the first").isPositive();

        reportOverlayListening(table, second);
        bridge.comm.sent.clear();
        String[] texts = ((DartTableItem) second.getImpl()).getTexts();

        String row = "GC/" + second.hashCode();
        List<GcFrames.Entry> entries = BatchEntries.of(bridge.comm.sent);
        List<String> rowChannels = entries.stream()
                .map(GcFrames.Entry::channel)
                .filter(c -> c.startsWith(row))
                .collect(Collectors.toList());

        assertThat(texts).as("the drawn name is still the row's text").containsExactly("check_0710");
        assertThat(rowChannels)
                .as("the icon is drawn on the row's own channel, and the paint is committed")
                // drawString(String, int, int, true) reaches the client as the flags form SWT defines it by.
                .contains(row + "/fillRoundRectangleintintintintintint", row + "/drawTextStringintintint",
                        row + "/gcDispose");
        assertThat(WireValues.find(body(entries, row + "/gcDispose"), m -> Boolean.TRUE.equals(m.get("fullRepaint"))))
                .as("each paint of the row replaces the last")
                .isNotEmpty();
        List<Object> state = entries.stream().filter(e -> e.channel().equals(row)).map(GcFrames.Entry::value)
                .collect(Collectors.toList());
        assertThat(WireValues.mentions(state, "transform")).as("the row is drawn in its own coordinates").isTrue();
        assertThat(WireValues.find(state, m -> m.containsKey("transform")).stream()
                .anyMatch(m -> holdsNumber(m.get("transform"), -rowY)))
                .as("the row is drawn in its own coordinates")
                .isTrue();
        assertThat(WireValues.find(state, m -> m.containsKey("style") && WireValues.same(m.get("style"), 1)))
                .as("the bold font the name is drawn in").isNotEmpty();
        assertThat(WireValues.find(state, m -> "#ffdfdede".equals(m.get("foreground"))))
                .as("the row's foreground, which native SWT sets on the GC it hands the listener")
                .isNotEmpty();
        assertThat(entries.stream().map(GcFrames.Entry::channel))
                .as("nothing the row draws lands on the Table's own channel")
                .noneMatch(c -> c.startsWith("GC/" + table.hashCode() + "/"));

        bridge.comm.sent.clear();
        ((DartTableItem) first.getImpl()).getTexts();
        assertThat(BatchEntries.of(bridge.comm.sent).stream().map(GcFrames.Entry::channel))
                .as("a row whose overlay never reported listening is not painted into it")
                .noneMatch(c -> c.startsWith("GC/" + first.hashCode()));

        bold.dispose();
        light.dispose();
        green.dispose();
    }

    /**
     * The overlay scrolls sideways with its row, while the cell bounds the listeners draw at are in
     * the table's client area: the row's GC puts the scrolled distance back.
     */
    @Test
    void rowDrawing_landsUnderItsColumn_whenTheTableIsScrolledSideways() {
        int scrolled = 300;
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.H_SCROLL | SWT.V_SCROLL);
        table.setBounds(0, 0, 200, 200);
        new TableColumn(table, SWT.NONE).setWidth(100);
        new TableColumn(table, SWT.NONE).setWidth(600);
        table.addListener(SWT.EraseItem, event -> { });
        table.addListener(SWT.PaintItem, event -> event.gc.drawString("x", event.x, event.y, true));
        TableItem row = new TableItem(table, SWT.NONE);
        int columnLeft = row.getBounds(1).x;

        Event scroll = new Event();
        scroll.x = scrolled;
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) table.getImpl(), "Scroll", "Scroll"), scroll);
        while (display.readAndDispatch()) {
            // run the handler's asyncExec
        }
        assertThat(row.getBounds(1).x).as("the cell in the client area").isEqualTo(columnLeft - scrolled);

        bridge.comm.sent.clear();
        reportOverlayListening(table, row);
        ((DartTableItem) row.getImpl()).getTexts();

        String channel = "GC/" + row.hashCode();
        List<Object> state = BatchEntries.of(bridge.comm.sent).stream().filter(e -> e.channel().equals(channel))
                .map(GcFrames.Entry::value).collect(Collectors.toList());
        assertThat(WireValues.find(state, m -> m.containsKey("transform")).stream()
                .anyMatch(m -> holdsNumber(m.get("transform"), scrolled)))
                .as("the row's GC is moved right by the scrolled distance, back to the overlay's origin")
                .isTrue();
    }

    private void reportOverlayListening(Table table, TableItem row) {
        Event e = new Event();
        e.segments = new int[] { row.hashCode() };
        bridge.comm.fireContaining("Table/" + table.hashCode() + "/PaintItem/PaintItem", e);
        while (display.readAndDispatch()) {
            // run the handler's asyncExec
        }
    }

    /** The last body sent on exactly {@code channel}. */
    private static Object body(List<GcFrames.Entry> entries, String channel) {
        Object found = null;
        for (GcFrames.Entry entry : entries) {
            if (entry.channel().equals(channel)) found = entry.value();
        }
        assertThat(found).as("a frame on " + channel).isNotNull();
        return found;
    }

    /** Whether {@code tree} holds the number {@code n} at any depth. */
    private static boolean holdsNumber(Object tree, double n) {
        if (tree instanceof Number) return ((Number) tree).doubleValue() == n;
        if (tree instanceof java.util.Map<?, ?> map) return map.values().stream().anyMatch(v -> holdsNumber(v, n));
        if (tree instanceof List<?> list) return list.stream().anyMatch(v -> holdsNumber(v, n));
        return false;
    }
}
