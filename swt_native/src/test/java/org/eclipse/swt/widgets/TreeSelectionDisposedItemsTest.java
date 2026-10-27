package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * Removing items must drop them from the selection. Native SWT reads the selection back from the
 * OS control, where a removed row no longer exists; applications rely on that and dereference
 * {@code getData()} of every selected item after rebuilding a tree.
 */
class TreeSelectionDisposedItemsTest extends SerializeTestBase {

    private Tree tree;
    private TreeItem parent;
    private TreeItem child;
    private TreeItem sibling;

    private void createTree() {
        tree = new Tree(shell(), SWT.MULTI);
        parent = new TreeItem(tree, SWT.NONE);
        parent.setText("Parent");
        child = new TreeItem(parent, SWT.NONE);
        child.setText("Child");
        parent.setExpanded(true);
        sibling = new TreeItem(tree, SWT.NONE);
        sibling.setText("Sibling");
    }

    @Test
    void disposing_the_parent_of_a_selected_item_drops_it_from_the_selection() {
        createTree();
        tree.setSelection(new TreeItem[] { child, sibling });

        parent.dispose();

        assertThat(tree.getSelection()).containsExactly(sibling);
        assertThat(tree.getSelectionCount()).isEqualTo(1);
    }

    @Test
    void removing_all_children_of_an_item_drops_them_from_the_selection() {
        createTree();
        tree.setSelection(new TreeItem[] { child, sibling });

        parent.removeAll();

        assertThat(tree.getSelection()).containsExactly(sibling);
    }

    @Test
    void shrinking_the_item_count_drops_removed_items_from_the_selection() {
        createTree();
        tree.setSelection(new TreeItem[] { parent, sibling });

        tree.setItemCount(1);

        assertThat(tree.getSelection()).containsExactly(parent);
    }

    @Test
    void disposing_a_selected_item_itself_drops_it_from_the_selection() {
        createTree();
        tree.setSelection(new TreeItem[] { child, sibling });

        sibling.dispose();

        assertThat(tree.getSelection()).containsExactly(child);
    }
}
