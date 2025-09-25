package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VTree extends VComposite {

    protected VTree() {
    }

    protected VTree(DartTree impl) {
        super(impl);
    }

    public int[] getColumnOrder() {
        return ((DartTree) impl).cachedItemOrder;
    }

    public void setColumnOrder(int[] value) {
        ((DartTree) impl).cachedItemOrder = value;
    }

    public TreeColumn[] getColumns() {
        TreeColumn[] values = ((DartTree) impl).columns;
        if (values == null)
            return null;
        ArrayList<TreeColumn> result = new ArrayList<>(values.length);
        for (TreeColumn v : values) if (v != null)
            result.add(v);
        return result.toArray(TreeColumn[]::new);
    }

    public void setColumns(TreeColumn[] value) {
        ((DartTree) impl).columns = value;
    }

    public Color getHeaderBackground() {
        return ((DartTree) impl).getHeaderBackground();
    }

    public void setHeaderBackground(Color value) {
        ((DartTree) impl).setHeaderBackground(value);
    }

    public Color getHeaderForeground() {
        return ((DartTree) impl).getHeaderForeground();
    }

    public void setHeaderForeground(Color value) {
        ((DartTree) impl).setHeaderForeground(value);
    }

    public boolean getHeaderVisible() {
        return ((DartTree) impl).getHeaderVisible();
    }

    public void setHeaderVisible(boolean value) {
        ((DartTree) impl).headerVisible = value;
    }

    public TreeItem[] getItems() {
        TreeItem[] values = ((DartTree) impl).items;
        if (values == null)
            return null;
        ArrayList<TreeItem> result = new ArrayList<>(values.length);
        for (TreeItem v : values) if (v != null)
            result.add(v);
        return result.toArray(TreeItem[]::new);
    }

    public void setItems(TreeItem[] value) {
        ((DartTree) impl).items = value;
    }

    public boolean getLinesVisible() {
        return ((DartTree) impl).getLinesVisible();
    }

    public void setLinesVisible(boolean value) {
        ((DartTree) impl).linesVisible = value;
    }

    public TreeItem[] getSelection() {
        TreeItem[] values = ((DartTree) impl).selection;
        if (values == null)
            return null;
        ArrayList<TreeItem> result = new ArrayList<>(values.length);
        for (TreeItem v : values) if (v != null)
            result.add(v);
        return result.toArray(TreeItem[]::new);
    }

    public void setSelection(TreeItem[] value) {
        ((DartTree) impl).selection = value;
    }

    public TreeColumn getSortColumn() {
        TreeColumn val = ((DartTree) impl).sortColumn;
        if (val != null && !(val.getImpl() instanceof DartTreeColumn))
            return null;
        return val;
    }

    public void setSortColumn(TreeColumn value) {
        ((DartTree) impl).sortColumn = value;
    }

    public int getSortDirection() {
        return ((DartTree) impl).getSortDirection();
    }

    public void setSortDirection(int value) {
        ((DartTree) impl).sortDirection = value;
    }

    public TreeItem getTopItem() {
        TreeItem val = ((DartTree) impl).topItem;
        if (val != null && !(val.getImpl() instanceof DartTreeItem))
            return null;
        return val;
    }

    public void setTopItem(TreeItem value) {
        ((DartTree) impl).topItem = value;
    }

    @JsonConverter(target = Tree.class)
    public static class TreeJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTree.class, (JsonWriter.WriteObject<DartTree>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTree.class, (JsonReader.ReadObject<DartTree>) reader -> {
                return null;
            });
        }

        public static Tree read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Tree api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
