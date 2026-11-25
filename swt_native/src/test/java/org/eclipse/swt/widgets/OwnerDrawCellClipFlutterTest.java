package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Native SWT clips the GC of an owner-draw cell event to the cell, so a listener that draws text
 * wider than its column (JFace's StyledCellLabelProvider draws a TextLayout with no width) is cut at
 * the column's edge instead of drawing over the next columns.
 */
@Tag("flutter-it")
class OwnerDrawCellClipFlutterTest {

    private static final int COLUMN_WIDTH = 120;
    private static final int COLUMNS = 3;

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
    void aTableCellsPaintItemGc_isClippedToTheCell() {
        Table table = new Table(new Shell(display), SWT.FULL_SELECTION);
        table.setBounds(0, 0, 400, 200);
        for (int i = 0; i < COLUMNS; i++) new TableColumn(table, SWT.NONE).setWidth(COLUMN_WIDTH);
        TableItem row = new TableItem(table, SWT.NONE);
        Map<Integer, Rectangle[]> seen = listen(table);

        paint(table, "Table", row);

        assertClippedToCells(seen);
    }

    @Test
    void aTreeCellsPaintItemGc_isClippedToTheCell() {
        Tree tree = new Tree(new Shell(display), SWT.FULL_SELECTION);
        tree.setBounds(0, 0, 400, 200);
        for (int i = 0; i < COLUMNS; i++) new TreeColumn(tree, SWT.NONE).setWidth(COLUMN_WIDTH);
        TreeItem row = new TreeItem(tree, SWT.NONE);
        Map<Integer, Rectangle[]> seen = listen(tree);

        paint(tree, "Tree", row);

        assertClippedToCells(seen);
    }

    /** Records, per column, the cell's bounds and the clipping of the GC PaintItem gets. */
    private static Map<Integer, Rectangle[]> listen(Control control) {
        Map<Integer, Rectangle[]> seen = new LinkedHashMap<>();
        control.addListener(SWT.PaintItem, event -> {
            Rectangle cell = event.item instanceof TableItem
                    ? ((TableItem) event.item).getBounds(event.index)
                    : ((TreeItem) event.item).getBounds(event.index);
            seen.put(event.index, new Rectangle[] { cell, event.gc.getClipping() });
            event.gc.drawText("a text much wider than the column it is drawn in", event.x + 2, event.y);
        });
        return seen;
    }

    /** Flutter reports the row's overlay listening, as it does once the overlay is laid out. */
    private void paint(Control parent, String type, Item row) {
        Event e = new Event();
        e.segments = new int[] { row.hashCode(), 0xffffff };
        bridge.comm.fireContaining(type + "/" + parent.hashCode() + "/PaintItem/PaintItem", e);
        while (display.readAndDispatch()) {
            // run the handler's asyncExec
        }
    }

    private static void assertClippedToCells(Map<Integer, Rectangle[]> seen) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(seen.keySet()).as("columns painted").containsExactly(0, 1, 2);
        seen.forEach((column, r) -> {
            Rectangle cell = r[0];
            Rectangle clip = r[1];
            // The first column's clip may also cover the indent and expander left of its text.
            Rectangle previous = column == 0 ? null : seen.get(column - 1)[0];
            int leftLimit = previous == null ? 0 : previous.x + previous.width;
            softly.assertThat(clip.x).as("column " + column + ": clip's left edge vs cell " + cell)
                    .isGreaterThanOrEqualTo(leftLimit).isLessThanOrEqualTo(cell.x);
            softly.assertThat(clip.x + clip.width).as("column " + column + ": clip's right edge vs cell " + cell)
                    .isEqualTo(cell.x + cell.width);
        });
        softly.assertAll();
    }

    // Cocoa and GTK clip a Table or Tree without columns to the whole row, out to the client area's right
    // edge; Win32 to its one column, as wide as the widest row. Evolve follows Cocoa and GTK.

    @Test
    void aColumnlessTablesPaintItemGc_reachesTheClientAreasRightEdgePastTheWidestRow() {
        Table table = new Table(new Shell(display), SWT.FULL_SELECTION);
        table.setBounds(0, 0, 400, 200);
        TableItem wide = new TableItem(table, SWT.NONE);
        wide.setText(WIDE_ROW);
        TableItem row = new TableItem(table, SWT.NONE);
        row.setText("short");
        Map<Integer, Rectangle[]> seen = listen(table);

        paint(table, "Table", row);

        assertReachesTheClientAreasRightEdge(table, seen, wide.getBounds(0));
    }

    @Test
    void aColumnlessTreesPaintItemGc_reachesTheClientAreasRightEdgePastTheWidestRow() {
        Tree tree = new Tree(new Shell(display), SWT.FULL_SELECTION);
        tree.setBounds(0, 0, 400, 200);
        TreeItem wide = new TreeItem(tree, SWT.NONE);
        wide.setText(WIDE_ROW);
        TreeItem row = new TreeItem(tree, SWT.NONE);
        row.setText("short");
        Map<Integer, Rectangle[]> seen = listen(tree);

        paint(tree, "Tree", row);

        assertReachesTheClientAreasRightEdge(tree, seen, wide.getBounds(0));
    }

    private static final String WIDE_ROW = "a row much wider than the other";

    private static void assertReachesTheClientAreasRightEdge(Scrollable control, Map<Integer, Rectangle[]> seen, Rectangle widest) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(seen.keySet()).as("cells painted").containsExactly(0);
        Rectangle[] r = seen.get(0);
        if (r != null) {
            Rectangle cell = r[0];
            Rectangle clip = r[1];
            Rectangle client = control.getClientArea();
            softly.assertThat(widest.x + widest.width).as("the widest row ends before the client edge " + client)
                    .isLessThan(client.x + client.width);
            softly.assertThat(clip.x).as("clip's left edge vs cell " + cell).isLessThanOrEqualTo(cell.x);
            softly.assertThat(clip.x + clip.width).as("clip's right edge vs client area " + client)
                    .isEqualTo(client.x + client.width);
        }
        softly.assertAll();
    }

    /** Win32 and GTK keep a Table's checkbox in column 0 wherever it is shown, and clip column 0 over it. */
    @Test
    void aCheckTablesColumnZeroClip_coversItsCheckboxWhereverItIsShown() {
        Table table = new Table(new Shell(display), SWT.CHECK | SWT.FULL_SELECTION);
        table.setBounds(0, 0, 400, 200);
        for (int i = 0; i < COLUMNS; i++) new TableColumn(table, SWT.NONE).setWidth(COLUMN_WIDTH);
        table.setColumnOrder(new int[] { 1, 0, 2 });
        TableItem row = new TableItem(table, SWT.NONE);
        Map<Integer, Rectangle[]> seen = listen(table);

        paint(table, "Table", row);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(seen.keySet()).as("columns painted").contains(0, 1, 2);
        if (seen.containsKey(0) && seen.containsKey(1)) {
            Rectangle shownBefore = seen.get(1)[0];
            Rectangle cell = seen.get(0)[0];
            Rectangle clip = seen.get(0)[1];
            Rectangle column = new Rectangle(shownBefore.x + shownBefore.width, cell.y, COLUMN_WIDTH, cell.height);
            softly.assertThat(cell.x).as("the text starts after the checkbox").isGreaterThan(column.x);
            softly.assertThat(clip).as("column 0's clip: its whole column").isEqualTo(column);
            softly.assertThat(seen.get(1)[1]).as("column 1's clip, shown first").isEqualTo(shownBefore);
        }
        softly.assertAll();
    }

    /** All three platforms clip a cell with an image to its whole column, image included. */
    @Test
    void aCellWithAnImage_isClippedToItsWholeColumn() {
        Table table = new Table(new Shell(display), SWT.FULL_SELECTION);
        table.setBounds(0, 0, 400, 200);
        for (int i = 0; i < COLUMNS; i++) new TableColumn(table, SWT.NONE).setWidth(COLUMN_WIDTH);
        TableItem row = new TableItem(table, SWT.NONE);
        org.eclipse.swt.graphics.Image image = new org.eclipse.swt.graphics.Image(display, 16, 16);
        row.setImage(1, image);
        row.setText(1, "text");
        Map<Integer, Rectangle[]> seen = listen(table);

        paint(table, "Table", row);

        SoftAssertions softly = new SoftAssertions();
        Rectangle[] r = seen.get(1);
        softly.assertThat(r).as("column 1 painted").isNotNull();
        if (r != null) {
            softly.assertThat(row.getImageBounds(1).x).as("the image starts the cell").isLessThan(row.getTextBounds(1).x);
            softly.assertThat(r[1]).as("column 1's clip").isEqualTo(new Rectangle(r[0].x, r[0].y, COLUMN_WIDTH, r[0].height));
        }
        softly.assertAll();
        image.dispose();
    }

    /** Win32 and GTK put a Tree's indent and expander in its leftmost column and clip that column over them. */
    @Test
    void aTreeChildsLeftmostClip_coversItsIndent() {
        Tree tree = new Tree(new Shell(display), SWT.FULL_SELECTION);
        tree.setBounds(0, 0, 400, 200);
        for (int i = 0; i < COLUMNS; i++) new TreeColumn(tree, SWT.NONE).setWidth(COLUMN_WIDTH);
        TreeItem parent = new TreeItem(tree, SWT.NONE);
        TreeItem child = new TreeItem(parent, SWT.NONE);
        parent.setExpanded(true);
        Map<Integer, Rectangle[]> seen = listen(tree);

        paint(tree, "Tree", child);

        SoftAssertions softly = new SoftAssertions();
        Rectangle[] r = seen.get(0);
        softly.assertThat(r).as("column 0 painted").isNotNull();
        if (r != null) {
            int rowLeft = (int) Math.round(dev.equo.swt.size.TreeSizes.getRowLeft((DartTree) tree.getImpl()));
            softly.assertThat(r[0].x).as("the child's text starts after its indent").isGreaterThan(parent.getBounds(0).x);
            softly.assertThat(r[1].x).as("column 0's clip starts at the row's start").isEqualTo(rowLeft);
            softly.assertThat(r[1].x + r[1].width).as("column 0's clip ends with the column").isEqualTo(r[0].x + r[0].width);
        }
        softly.assertAll();
    }

    /** Each platform gives PaintItem a GC of its own, clipped to the cell whatever EraseItem did to its GC. */
    @Test
    void aClipEraseItemSets_doesNotReachPaintItem() {
        Table table = new Table(new Shell(display), SWT.FULL_SELECTION);
        table.setBounds(0, 0, 400, 200);
        for (int i = 0; i < COLUMNS; i++) new TableColumn(table, SWT.NONE).setWidth(COLUMN_WIDTH);
        TableItem row = new TableItem(table, SWT.NONE);
        Map<Integer, Rectangle[]> erased = new LinkedHashMap<>();
        table.addListener(SWT.EraseItem, event -> {
            erased.put(event.index, new Rectangle[] { new Rectangle(event.x, event.y, event.width, event.height),
                    event.gc.getClipping() });
            event.gc.setClipping(event.x, event.y, 1, 1);
        });
        Map<Integer, Rectangle[]> seen = listen(table);

        paint(table, "Table", row);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(seen.keySet()).as("columns painted").containsExactly(0, 1, 2);
        seen.forEach((column, r) -> {
            Rectangle[] e = erased.get(column);
            softly.assertThat(e).as("column " + column + " erased").isNotNull();
            if (e != null) softly.assertThat(r[1]).as("column " + column + ": PaintItem's clip vs EraseItem's").isEqualTo(e[1]);
        });
        softly.assertAll();
    }
}
