package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/** selectAll selects the rows the tree shows, as every native does, and leaves its structure alone. */
class TreeSelectAllTest extends SerializeTestBase {

    @Test
    void selectAll_selects_the_shown_rows_and_keeps_the_roots() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        TreeItem alpha = new TreeItem(tree, SWT.NONE);
        TreeItem bravo = new TreeItem(tree, SWT.NONE);
        TreeItem child = new TreeItem(bravo, SWT.NONE);
        new TreeItem(child, SWT.NONE);
        TreeItem charlie = new TreeItem(tree, SWT.NONE);

        tree.selectAll();

        assertThat(tree.getSelection()).containsExactly(alpha, bravo, charlie);
        assertThat(tree.getItems()).containsExactly(alpha, bravo, charlie);

        bravo.setExpanded(true);
        tree.selectAll();

        assertThat(tree.getSelection()).containsExactly(alpha, bravo, child, charlie);
        assertThat(tree.getItemCount()).isEqualTo(3);
    }
}
