package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * The owner-draw listeners' GC background is the ground Evolve paints under the row (theme row,
 * band or selection) unless the row is painted in a colour the application set, and a selected row's events
 * carry {@code SWT.SELECTED}, as native SWT hands them its selection colour and that detail. An
 * application that fills with the GC's background, or draws opaque text, then lands on the row's
 * own colour.
 *
 * <p>The shape is a command palette whose PaintItem listener draws a right-aligned shortcut with
 * opaque text on a table that sets no colours.
 */
@Tag("flutter-it")
class OwnerDrawRowGroundFlutterTest {

    private static final RGB BASE = new RGB(0xee, 0xf8, 0xef);
    private static final RGB BAND = new RGB(0xdc, 0xef, 0xdf);
    private static final RGB SELECTION = new RGB(0xbe, 0xdf, 0xe4);

    private RecordingBridge bridge;
    private Display display;
    private ConfigFlags savedFlags;

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
        savedFlags = Config.getConfigFlags();
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = "light";
        Config.setConfigFlags(flags);
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
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void aTablesRows_giveTheGcTheGroundUnderThem_andTheSelectedRowSaysSo() {
        Table table = new Table(new Shell(display), SWT.FULL_SELECTION);
        table.setBounds(0, 0, 300, 200);
        Item[] rows = { new TableItem(table, SWT.NONE), new TableItem(table, SWT.NONE), new TableItem(table, SWT.NONE) };
        table.setSelection(2);
        Map<Item, Seen> seen = listen(table);

        paint(table, "Table", rows, BASE, BAND, SELECTION);

        assertGrounds(seen, rows);
    }

    @Test
    void aTreesRows_giveTheGcTheGroundUnderThem_andTheSelectedRowSaysSo() {
        Tree tree = new Tree(new Shell(display), SWT.FULL_SELECTION);
        tree.setBounds(0, 0, 300, 200);
        TreeItem[] items = { new TreeItem(tree, SWT.NONE), new TreeItem(tree, SWT.NONE), new TreeItem(tree, SWT.NONE) };
        tree.setSelection(items[2]);
        Map<Item, Seen> seen = listen(tree);

        paint(tree, "Tree", items, BASE, BAND, SELECTION);

        assertGrounds(seen, items);
    }

    @Test
    void inTheTheme_aBackgroundTheAppSet_isNotPainted_soTheGcGetsTheGround() {
        Table table = new Table(new Shell(display), SWT.FULL_SELECTION);
        table.setBounds(0, 0, 300, 200);
        Color lightBlue = new Color(display, 204, 229, 255);
        Color pink = new Color(display, 255, 204, 229);
        table.setBackground(lightBlue);
        TableItem onTable = new TableItem(table, SWT.NONE);
        TableItem onItem = new TableItem(table, SWT.NONE);
        onItem.setBackground(pink);
        Map<Item, Seen> seen = listen(table);

        paint(table, "Table", new Item[] { onTable, onItem }, BASE, BAND);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(seen.get(onTable).paintBackground).as("the table's colour is not painted")
                    .isEqualTo(BASE);
            softly.assertThat(seen.get(onItem).paintBackground).as("the item's colour is not painted")
                    .isEqualTo(BAND);
        });
        lightBlue.dispose();
        pink.dispose();
    }

    @Test
    void withSwtColors_aBackgroundTheAppSet_reachesTheGcAsSet_unlessTheRowIsSelected() {
        Config.getConfigFlags().use_swt_colors = true;
        Table table = new Table(new Shell(display), SWT.FULL_SELECTION);
        table.setBounds(0, 0, 300, 200);
        Color lightBlue = new Color(display, 204, 229, 255);
        TableItem plain = new TableItem(table, SWT.NONE);
        TableItem chosen = new TableItem(table, SWT.NONE);
        plain.setBackground(lightBlue);
        chosen.setBackground(lightBlue);
        table.setSelection(1);
        Map<Item, Seen> seen = listen(table);

        paint(table, "Table", new Item[] { plain, chosen }, BASE, SELECTION);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(seen.get(plain).paintBackground).as("an unselected row keeps the app's colour")
                    .isEqualTo(lightBlue.getRGB());
            softly.assertThat(seen.get(chosen).paintBackground).as("a selected row is on the selection")
                    .isEqualTo(SELECTION);
        });
        lightBlue.dispose();
    }

    // ---- harness ----------------------------------------------------------------------------------

    private static final class Seen {
        RGB eraseBackground;
        RGB paintBackground;
        boolean eraseSelected;
        boolean paintSelected;
    }

    private static Map<Item, Seen> listen(Control control) {
        Map<Item, Seen> seen = new HashMap<>();
        control.addListener(SWT.EraseItem, event -> {
            Seen s = seen.computeIfAbsent((Item) event.item, k -> new Seen());
            s.eraseBackground = event.gc.getBackground().getRGB();
            s.eraseSelected = (event.detail & SWT.SELECTED) != 0;
        });
        control.addListener(SWT.PaintItem, event -> {
            Seen s = seen.computeIfAbsent((Item) event.item, k -> new Seen());
            s.paintBackground = event.gc.getBackground().getRGB();
            s.paintSelected = (event.detail & SWT.SELECTED) != 0;
            event.gc.drawText("Shift T", event.x + 200, event.y);
        });
        return seen;
    }

    /** Flutter reports each row's overlay listening, with the ground it lays the row's overlay on. */
    private void paint(Control parent, String type, Item[] rows, RGB... grounds) {
        Event e = new Event();
        e.segments = new int[rows.length * 2];
        for (int i = 0; i < rows.length; i++) {
            e.segments[2 * i] = rows[i].hashCode();
            e.segments[2 * i + 1] = (grounds[i].red << 16) | (grounds[i].green << 8) | grounds[i].blue;
        }
        bridge.comm.fireContaining(type + "/" + parent.hashCode() + "/PaintItem/PaintItem", e);
        while (display.readAndDispatch()) {
            // run the handler's asyncExec
        }
        for (Item row : rows) {
            Object impl = row.getImpl();
            if (impl instanceof DartTableItem) ((DartTableItem) impl).getTexts();
            if (impl instanceof DartTreeItem) ((DartTreeItem) impl).getTexts();
        }
    }

    private static void assertGrounds(Map<Item, Seen> seen, Item[] rows) {
        SoftAssertions.assertSoftly(softly -> {
            RGB[] expected = { BASE, BAND, SELECTION };
            String[] names = { "even row", "banded row", "selected row" };
            for (int i = 0; i < rows.length; i++) {
                Seen s = seen.get(rows[i]);
                softly.assertThat(s).as(names[i] + " painted").isNotNull();
                if (s == null) continue;
                softly.assertThat(s.eraseBackground).as(names[i] + ": EraseItem's GC background").isEqualTo(expected[i]);
                softly.assertThat(s.paintBackground).as(names[i] + ": PaintItem's GC background").isEqualTo(expected[i]);
                softly.assertThat(s.eraseSelected).as(names[i] + ": EraseItem's SWT.SELECTED").isEqualTo(i == 2);
                softly.assertThat(s.paintSelected).as(names[i] + ": PaintItem's SWT.SELECTED").isEqualTo(i == 2);
            }
        });
    }
}
