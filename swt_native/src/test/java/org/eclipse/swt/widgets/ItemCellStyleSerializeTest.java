package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.*;

/**
 * The colour and font an item sets for one column ({@code setForeground(int, Color)} and the like),
 * which JFace's {@code ViewerCell} uses for every colour and font provider. They travel as
 * {@code foregrounds}/{@code backgrounds}/{@code fonts}, one slot per column, and a cell setter
 * names the list it wrote so a partial update carries it.
 */
class ItemCellStyleSerializeTest extends SerializeTestBase {

    @Test
    void treeItem_sends_the_foreground_and_font_set_for_a_column() {
        Tree tree = tree();
        ((DartTree) tree.getImpl()).columnCount = 2;
        TreeItem item = new TreeItem(tree, SWT.NONE);
        Font bold = new Font(device(), "Arial", 10, SWT.BOLD);

        item.setForeground(1, new Color(device(), 10, 100, 200));
        item.setBackground(0, new Color(device(), 1, 2, 3));
        item.setFont(1, bold);

        Map<String, Object> state = serialize(item);
        assertThat(WireValues.at(state, "backgrounds[0]")).isInstanceOf(String.class).asString().endsWith("03");
        assertThatWire(state).node("backgrounds[1]").isNull();
        assertThatWire(state).node("foregrounds").hasSize(2);
        assertThatWire(state).node("foregrounds[0]").isNull();
        assertThat(WireValues.at(state, "foregrounds[1]")).isEqualTo("#ff0a64c8");
        assertThatWire(state).node("fonts[0]").isNull();
        assertThatWire(state).node("fonts[1]").isObject();
    }

    @Test
    void treeItem_cell_setters_name_the_list_they_write() {
        Tree tree = tree();
        TreeItem item = new TreeItem(tree, SWT.NONE);
        VTreeItem value = ((DartTreeItem) item.getImpl()).getValue();
        value.clearDirty();

        item.setForeground(0, new Color(device(), 10, 100, 200));
        item.setBackground(0, new Color(device(), 1, 2, 3));
        item.setFont(0, new Font(device(), "Arial", 10, SWT.ITALIC));

        assertThat(value.changedNames()).contains("foregrounds", "backgrounds", "fonts");
    }

    @Test
    void tableItem_sends_the_colours_and_font_set_for_a_column() {
        Table table = table();
        ((DartTable) table.getImpl()).columnCount = 2;
        TableItem item = new TableItem(table, SWT.NONE);

        item.setForeground(0, new Color(device(), 10, 100, 200));
        item.setBackground(1, new Color(device(), 1, 2, 3));
        item.setFont(1, new Font(device(), "Arial", 10, SWT.BOLD));

        Map<String, Object> state = serialize(item);
        assertThat(WireValues.at(state, "foregrounds[0]")).isInstanceOf(String.class).asString().endsWith("c8");
        assertThatWire(state).node("backgrounds[0]").isNull();
        assertThat(WireValues.at(state, "backgrounds[1]")).isInstanceOf(String.class).asString().endsWith("03");
        assertThatWire(state).node("fonts[1]").isObject();
    }

    @Test
    void an_item_with_no_cell_values_sends_no_cell_lists() {
        TreeItem item = new TreeItem(tree(), SWT.NONE);

        Map<String, Object> state = serialize(item);
        assertThatWire(state).isObject()
                .doesNotContainKey("foregrounds")
                .doesNotContainKey("backgrounds")
                .doesNotContainKey("fonts");
    }

    /** A viewer reusing a row clears it ({@code Table.clear(int)}), then sets only what differs from the cleared defaults. */
    @Test
    void clearing_a_tableItem_names_the_cell_fonts_it_resets() {
        Table table = table();
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText("old");
        item.setFont(0, new Font(device(), "Arial", 10, SWT.BOLD));
        VTableItem value = ((DartTableItem) item.getImpl()).getValue();
        value.clearDirty();

        ((DartTableItem) item.getImpl()).clear();
        item.setText("new");
        item.setFont(0, null);

        assertThat(value.changedKeys()).contains(VTableItem.FONTS);
    }

    /**
     * Without {@code images}, a row draws {@code image}: a cleared row that had one names it, or keeps
     * the old icon. Its text is its cells', which the client reads instead of the one it inherits.
     */
    @Test
    void clearing_a_tableItem_names_the_image_it_inherits_and_its_texts() {
        Table table = table();
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText("old");
        item.setImage(new Image(device(), 4, 4));
        VTableItem value = ((DartTableItem) item.getImpl()).getValue();
        value.clearDirty();

        ((DartTableItem) item.getImpl()).clear();
        item.setFont(0, null);

        assertThat(value.changedKeys()).contains(VTableItem.IMAGE, VTableItem.TEXTS);
    }

    @Test
    void clearing_a_treeItem_names_what_it_resets() {
        Tree tree = tree();
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText("old");
        item.setImage(new Image(device(), 4, 4));
        item.setFont(0, new Font(device(), "Arial", 10, SWT.BOLD));
        VTreeItem value = ((DartTreeItem) item.getImpl()).getValue();
        value.clearDirty();

        ((DartTreeItem) item.getImpl()).clear();
        item.setFont(0, null);

        assertThat(value.changedKeys()).contains(VTreeItem.IMAGE, VTreeItem.TEXT, VTreeItem.FONTS);
    }
}
