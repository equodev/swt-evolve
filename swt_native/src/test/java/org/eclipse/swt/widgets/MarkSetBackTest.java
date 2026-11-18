package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * A property is marked only while it differs from what the client holds: a selection that comes back
 * to the one last sent before the flush takes its mark back, a Link names its tab stop only when
 * the text gains or loses a link, and a setter given the value it already holds marks nothing.
 */
@ExtendWith(Mocks.class)
public class MarkSetBackTest extends SerializeTestBase {

    private static Table table() {
        Table table = new Table(shell(), SWT.SINGLE);
        for (int i = 0; i < 4; i++) new TableItem(table, SWT.NONE).setText("row " + i);
        table.setSelection(1);
        ((DartTable) table.getImpl()).getValue().clearDirty();
        return table;
    }

    private static Tree tree() {
        Tree tree = new Tree(shell(), SWT.SINGLE);
        for (int i = 0; i < 4; i++) new TreeItem(tree, SWT.NONE).setText("row " + i);
        tree.setSelection(tree.getItem(1));
        ((DartTree) tree.getImpl()).getValue().clearDirty();
        return tree;
    }

    @Test
    void aTableSelectionSetAgainIsNotSent() {
        Table table = table();
        table.setSelection(1);
        assertThat(((DartTable) table.getImpl()).getValue().changedKeys()).doesNotContain(VTable.SELECTION);
    }

    @Test
    void aTableSelectionChangedAndChangedBackIsNotSent() {
        Table table = table();
        table.setSelection(2);
        assertThat(((DartTable) table.getImpl()).getValue().changedKeys()).contains(VTable.SELECTION);
        table.setSelection(1);
        assertThat(((DartTable) table.getImpl()).getValue().changedKeys()).doesNotContain(VTable.SELECTION);
    }

    @Test
    void aTableSelectionThatEndsElsewhereIsSent() {
        Table table = table();
        table.deselectAll();
        table.select(3);
        assertThat(((DartTable) table.getImpl()).getValue().changedKeys()).contains(VTable.SELECTION);
    }

    @Test
    void aTreeSelectionSetAgainIsNotSent() {
        Tree tree = tree();
        tree.setSelection(tree.getItem(1));
        assertThat(((DartTree) tree.getImpl()).getValue().changedKeys()).doesNotContain(VTree.SELECTION);
    }

    @Test
    void aTreeSelectionChangedAndChangedBackIsNotSent() {
        Tree tree = tree();
        tree.setSelection(tree.getItem(2));
        assertThat(((DartTree) tree.getImpl()).getValue().changedKeys()).contains(VTree.SELECTION);
        tree.setSelection(tree.getItem(1));
        assertThat(((DartTree) tree.getImpl()).getValue().changedKeys()).doesNotContain(VTree.SELECTION);
    }

    @Test
    void aLinkNamesItsTabStopOnlyWhenTheTextGainsOrLosesALink() {
        Link link = new Link(shell(), SWT.NONE);
        link.setText("plain");
        VLink value = ((DartLink) link.getImpl()).getValue();
        value.clearDirty();

        link.setText("still plain");
        assertThat(value.changedKeys()).contains(VLink.TEXT).doesNotContain(VControl.TAB_STOP);

        link.setText("now <a>linked</a>");
        assertThat(value.changedKeys()).contains(VControl.TAB_STOP);
    }

    @Test
    void aShellMinimumSizeSetAgainIsNotSent() {
        Shell shell = new Shell(shell(), SWT.SHELL_TRIM);
        shell.setMinimumSize(120, 80);
        VShell value = ((DartShell) shell.getImpl()).getValue();
        value.clearDirty();

        shell.setMinimumSize(120, 80);
        assertThat(value.changedKeys()).doesNotContain(VShell.MINIMUM_SIZE);

        shell.setMinimumSize(140, 80);
        assertThat(value.changedKeys()).contains(VShell.MINIMUM_SIZE);
    }

    @Test
    void aFolderStyleIsSentOnlyWhenItsBitsChange() {
        CTabFolder folder = new CTabFolder(shell(), SWT.NONE);
        VWidget value = ((DartWidget) folder.getImpl()).getValue();
        value.clearDirty();

        folder.setSingle(folder.getSingle());
        assertThat(value.changedKeys()).doesNotContain(VWidget.STYLE);

        folder.setSingle(!folder.getSingle());
        assertThat(value.changedKeys()).contains(VWidget.STYLE);
    }

    @Test
    void aListItemSetToTheSameStringIsNotSent() {
        List list = new List(shell(), SWT.SINGLE);
        list.setItems(new String[] {"a", "b"});
        VList value = ((DartList) list.getImpl()).getValue();
        value.clearDirty();

        list.setItem(1, "b");
        assertThat(value.changedKeys()).doesNotContain(VList.ITEMS);

        list.setItem(1, "c");
        assertThat(value.changedKeys()).contains(VList.ITEMS);
    }

    @Test
    void aColumnAlignmentSetAgainIsNotSent() {
        Table table = table();
        new TableColumn(table, SWT.NONE);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setAlignment(SWT.RIGHT);
        VTableColumn value = ((DartTableColumn) column.getImpl()).getValue();
        value.clearDirty();

        column.setAlignment(SWT.RIGHT);
        assertThat(value.changedKeys()).doesNotContain(VTableColumn.ALIGNMENT);

        column.setAlignment(SWT.CENTER);
        assertThat(value.changedKeys()).contains(VTableColumn.ALIGNMENT);
    }

    @Test
    void aTreeColumnAlignmentSetAgainIsNotSent() {
        Tree tree = tree();
        new TreeColumn(tree, SWT.NONE);
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.setAlignment(SWT.RIGHT);
        VTreeColumn value = ((DartTreeColumn) column.getImpl()).getValue();
        value.clearDirty();

        column.setAlignment(SWT.RIGHT);
        assertThat(value.changedKeys()).doesNotContain(VTreeColumn.ALIGNMENT);

        column.setAlignment(SWT.CENTER);
        assertThat(value.changedKeys()).contains(VTreeColumn.ALIGNMENT);
    }

    @Test
    void aTableScrolledToTheRowAtItsTopIsNotSent() {
        Table table = table();
        table.setTopIndex(0);
        VTable value = ((DartTable) table.getImpl()).getValue();
        value.clearDirty();

        table.setTopIndex(0);
        assertThat(value.changedKeys()).doesNotContain(VTable.TOP_INDEX);
    }
}
