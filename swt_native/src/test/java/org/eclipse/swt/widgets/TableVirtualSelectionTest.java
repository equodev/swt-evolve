package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** A VIRTUAL table answers its selection with real items, creating the rows it has not shown yet. */
class TableVirtualSelectionTest extends KeyRoutingTestBase {

    @Test
    @DisplayName("getSelection after selectAll on a VIRTUAL table holds an item for every row")
    void selectionOfRowsNeverShownHoldsItems() {
        Table table = new Table(new Composite(shell(), SWT.NONE), SWT.MULTI | SWT.VIRTUAL);
        table.addListener(SWT.SetData, e -> ((TableItem) e.item).setText("row " + table.indexOf((TableItem) e.item)));
        table.setItemCount(40);

        table.selectAll();

        TableItem[] selection = table.getSelection();
        assertThat(selection).hasSize(40).doesNotContainNull();
        assertThat(selection[39].getText()).isEqualTo("row 39");
    }
}
