package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;
import org.eclipse.swt.custom.*;
import java.util.ArrayList;

@CompiledJson()
public class VTree extends VComposite {

    protected VTree() {
    }

    protected VTree(DartTree impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public int[] getColumnOrder() {
        return ((DartTree) impl).columnOrder;
    }

    public void setColumnOrder(int[] value) {
        ((DartTree) impl).columnOrder = value;
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

    public TreeEditor[] getEditors() {
        return ((DartTree) impl).editors;
    }

    public void setEditors(TreeEditor[] value) {
        ((DartTree) impl).editors = value;
    }

    public Color getHeaderBackground() {
        return ((DartTree) impl).getHeaderBackground();
    }

    public void setHeaderBackground(Color value) {
        ((DartTree) impl).setHeaderBackground(value);
    }

    @JsonAttribute(ignore = true)
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

    @JsonAttribute(ignore = true)
    public TreeColumn getSortColumn() {
        TreeColumn val = ((DartTree) impl).sortColumn;
        if (val != null && !(val.getImpl() instanceof DartTreeColumn))
            return null;
        return val;
    }

    public void setSortColumn(TreeColumn value) {
        ((DartTree) impl).sortColumn = value;
    }

    @JsonAttribute(ignore = true)
    public int getSortDirection() {
        return ((DartTree) impl).getSortDirection();
    }

    public void setSortDirection(int value) {
        ((DartTree) impl).sortDirection = value;
    }

    @JsonAttribute(ignore = true)
    public TreeItem getTopItem() {
        TreeItem val = ((DartTree) impl).topItem;
        if (val != null && !(val.getImpl() instanceof DartTreeItem))
            return null;
        return val;
    }

    public void setTopItem(TreeItem value) {
        ((DartTree) impl).topItem = value;
    }

    public static final String COLUMNS = "columns";

    public static final String EDITORS = "editors";

    public static final String HEADER_BACKGROUND = "headerBackground";

    public static final String HEADER_VISIBLE = "headerVisible";

    public static final String ITEMS = "items";

    public static final String LINES_VISIBLE = "linesVisible";

    public static final String SELECTION = "selection";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "columns":
                Serializer.writeKeyValue(writer, "columns", getColumns());
                return;
            case "editors":
                Serializer.writeKeyValue(writer, "editors", getEditors());
                return;
            case "headerBackground":
                Serializer.writeKeyValue(writer, "headerBackground", getHeaderBackground());
                return;
            case "headerVisible":
                Serializer.writeKeyValue(writer, "headerVisible", getHeaderVisible());
                return;
            case "items":
                Serializer.writeKeyValue(writer, "items", getItems());
                return;
            case "linesVisible":
                Serializer.writeKeyValue(writer, "linesVisible", getLinesVisible());
                return;
            case "selection":
                Serializer.writeKeyValue(writer, "selection", getSelection());
                return;
        }
        super.writeProperty(writer, key);
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

        public static Tree read(JsonReader<?> reader) throws IOException {
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
