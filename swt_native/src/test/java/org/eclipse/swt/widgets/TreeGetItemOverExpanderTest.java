package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.swtShell;

/**
 * Regression: {@code Tree.getItem(Point)} used to derive the item from {@code point.y}
 * alone, so a click on the expand/collapse arrow resolved to the row it belongs to. Native SWT
 * resolves no item there, and applications lean on that: one that clears its selection whenever
 * {@code getItem} returns null kept the selection instead, because the arrow always resolved to a
 * row.
 *
 * <p>Coordinates below are tree-relative and follow the rendered geometry pinned by
 * {@code flutter-lib/test/tree_expander_geometry_test.dart}: with {@code TREE_WIDTH} the arrow of a
 * root item spans x in [16.25, 28.25) and, one level in, [32.25, 44.25).
 */
@DisabledOnOs(OS.LINUX)
class TreeGetItemOverExpanderTest extends SerializeTestBase {

    private static final int TREE_WIDTH = 300;
    private static final int ROOT_ARROW = 20;
    private static final int CHILD_ARROW = 36;
    private static final int LABEL = 100;

    private Tree tree;
    private TreeItem branch;
    private TreeItem child;
    private TreeItem leaf;
    private int rowHeight;

    private void createTree() {
        tree = new Tree(swtShell(), SWT.BORDER);
        tree.setBounds(0, 0, TREE_WIDTH, 400);

        branch = new TreeItem(tree, SWT.NONE);
        branch.setText("Branch");
        child = new TreeItem(branch, SWT.NONE);
        child.setText("Child");
        TreeItem grandChild = new TreeItem(child, SWT.NONE);
        grandChild.setText("Grand child");
        branch.setExpanded(true);
        child.setExpanded(true);

        leaf = new TreeItem(tree, SWT.NONE);
        leaf.setText("Leaf");

        rowHeight = tree.getItemHeight();
    }

    /** Middle of the row at {@code index} in the flattened, visible-item order. */
    private Point at(int x, int index) {
        return new Point(x, index * rowHeight + rowHeight / 2);
    }

    @Test
    void a_point_on_the_expander_arrow_resolves_no_item() {
        createTree();

        assertThat(tree.getItem(at(ROOT_ARROW, 0))).as("root branch arrow").isNull();
        assertThat(tree.getItem(at(CHILD_ARROW, 1))).as("nested branch arrow").isNull();
    }

    @Test
    void a_point_on_the_row_content_still_resolves_the_item() {
        createTree();

        assertThat(tree.getItem(at(LABEL, 0))).as("root branch label").isSameAs(branch);
        assertThat(tree.getItem(at(LABEL, 1))).as("nested branch label").isSameAs(child);
        // Left of its own (deeper) arrow, a nested row is content, not expander.
        assertThat(tree.getItem(at(ROOT_ARROW, 1))).as("nested row, root-arrow column").isSameAs(child);
    }

    @Test
    void an_item_without_children_has_no_expander_to_miss() {
        createTree();

        // Flattened order: Branch, Child, Grand child, Leaf.
        assertThat(tree.getItem(at(ROOT_ARROW, 3))).as("leaf, arrow column").isSameAs(leaf);
        assertThat(tree.getItem(at(LABEL, 3))).as("leaf, label").isSameAs(leaf);
    }

    @Test
    void the_expander_column_moves_right_with_nesting() {
        createTree();

        // The root arrow's x lands in the indent of a level-1 row, and vice versa — proving the
        // hit test is per-level and not a single fixed column.
        assertThat(tree.getItem(at(ROOT_ARROW, 0))).as("root row at root arrow").isNull();
        assertThat(tree.getItem(at(ROOT_ARROW, 1))).as("nested row at root arrow").isSameAs(child);
        assertThat(tree.getItem(at(CHILD_ARROW, 0))).as("root row at nested arrow").isSameAs(branch);
        assertThat(tree.getItem(at(CHILD_ARROW, 1))).as("nested row at nested arrow").isNull();
    }
}
