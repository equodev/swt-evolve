package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where Java says a multi-column Tree's cells are. An owner-drawing listener paints at these
 * bounds into an overlay laid over the row Flutter rendered, so they have to be where Flutter puts
 * the cells: after the tree's edge gap and the row's padding, at the widths Java set. The Flutter
 * side of the same numbers is pinned by tree_column_widths_from_java_test.dart.
 */
@ExtendWith(Mocks.class)
class TreeCellBoundsTest {

    private static final int TREE_WIDTH = 300;
    // A multi-column row's cells start after the edge gap (0.005 of 300, 1.5) and the row padding (12).
    private static final int CELLS_LEFT = 14;

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
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private TreeItem itemInThreeColumns() {
        Tree tree = new Tree(Mocks.shell(), SWT.NONE);
        tree.setBounds(0, 0, TREE_WIDTH, 200);
        int[] widths = {150, 120, 110};
        for (int width : widths) new TreeColumn(tree, SWT.NONE).setWidth(width);
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(new String[] {"alpha", "beta", "gamma"});
        return item;
    }

    @Test
    void cellsStartAfterTheRowPaddingAtTheWidthsJavaSet() {
        TreeItem item = itemInThreeColumns();

        assertThat(item.getBounds(1).x).isEqualTo(CELLS_LEFT + 150);
        assertThat(item.getBounds(1).width).isEqualTo(120);
        assertThat(item.getBounds(2).x - item.getBounds(1).x).isEqualTo(120);
    }

    @Test
    void packFitsTheWidestCellAndTheHeader() {
        Tree tree = new Tree(Mocks.shell(), SWT.NONE);
        tree.setBounds(0, 0, TREE_WIDTH, 200);
        TreeColumn name = new TreeColumn(tree, SWT.NONE);
        name.setText("Name");
        TreeColumn owner = new TreeColumn(tree, SWT.NONE);
        owner.setText("Owner");
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(new String[] {"a cell text much longer than its column header", "x"});

        name.pack();
        owner.pack();

        assertThat(name.getWidth())
                .as("the first column holds the indent and expander before its text")
                .isGreaterThan(dev.equo.swt.size.TreeSizes.getTextWidth(item, 0));
        assertThat(owner.getWidth())
                .as("a column whose header is wider than its cells fits the header")
                .isGreaterThan(dev.equo.swt.size.TreeSizes.getTextWidth(item, 1));
    }

    @Test
    void packNeverHidesAnEmptyColumn() {
        Tree tree = new Tree(Mocks.shell(), SWT.NONE);
        tree.setBounds(0, 0, TREE_WIDTH, 200);
        new TreeColumn(tree, SWT.NONE);
        TreeColumn empty = new TreeColumn(tree, SWT.NONE);

        empty.pack();

        assertThat(empty.getWidth()).as("a width of 0 is a hidden column").isPositive();
    }

    @Test
    void textBoundsAreTheTextLineCentredInTheRow() {
        TreeItem item = itemInThreeColumns();
        Rectangle cell = item.getBounds(1);
        Rectangle text = item.getTextBounds(1);

        assertThat(text.x).isEqualTo(cell.x);
        assertThat(text.height).isPositive().isLessThan(cell.height);
        assertThat(text.y - cell.y).isEqualTo((cell.height - text.height) / 2);
    }
}
