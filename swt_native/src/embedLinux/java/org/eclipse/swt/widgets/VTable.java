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
public class VTable extends VComposite {

    protected VTable() {
    }

    protected VTable(DartTable impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public int[] getColumnOrder() {
        return ((DartTable) impl).columnOrder;
    }

    public void setColumnOrder(int[] value) {
        ((DartTable) impl).columnOrder = value;
    }

    public TableColumn[] getColumns() {
        TableColumn[] values = ((DartTable) impl).columns;
        if (values == null)
            return null;
        ArrayList<TableColumn> result = new ArrayList<>(values.length);
        for (TableColumn v : values) if (v != null)
            result.add(v);
        return result.toArray(TableColumn[]::new);
    }

    public void setColumns(TableColumn[] value) {
        ((DartTable) impl).columns = value;
    }

    public TableEditor[] getEditors() {
        return ((DartTable) impl)._editors();
    }

    public void setEditors(TableEditor[] value) {
        ((DartTable) impl).editors = value;
    }

    public Color getHeaderBackground() {
        return ((DartTable) impl).headerBackground;
    }

    public void setHeaderBackground(Color value) {
        ((DartTable) impl).headerBackground = value;
    }

    public Color getHeaderForeground() {
        return ((DartTable) impl).headerForeground;
    }

    public void setHeaderForeground(Color value) {
        ((DartTable) impl).headerForeground = value;
    }

    public boolean getHeaderVisible() {
        return ((DartTable) impl).getHeaderVisible();
    }

    public void setHeaderVisible(boolean value) {
        ((DartTable) impl).headerVisible = value;
    }

    public TableItem[] getItems() {
        TableItem[] values = ((DartTable) impl).items;
        if (values == null)
            return null;
        ArrayList<TableItem> result = new ArrayList<>(values.length);
        for (TableItem v : values) if (v != null)
            result.add(v);
        return result.toArray(TableItem[]::new);
    }

    public void setItems(TableItem[] value) {
        ((DartTable) impl).items = value;
    }

    public boolean getLinesVisible() {
        return ((DartTable) impl).getLinesVisible();
    }

    public void setLinesVisible(boolean value) {
        ((DartTable) impl).linesVisible = value;
    }

    public int[] getSelection() {
        return ((DartTable) impl).selection;
    }

    public void setSelection(int[] value) {
        ((DartTable) impl).selection = value;
    }

    @JsonAttribute(ignore = true)
    public TableColumn getSortColumn() {
        TableColumn val = ((DartTable) impl).sortColumn;
        if (val != null && !(val.getImpl() instanceof DartTableColumn))
            return null;
        return val;
    }

    public void setSortColumn(TableColumn value) {
        ((DartTable) impl).sortColumn = value;
    }

    @JsonAttribute(ignore = true)
    public int getSortDirection() {
        return ((DartTable) impl).getSortDirection();
    }

    public void setSortDirection(int value) {
        ((DartTable) impl).sortDirection = value;
    }

    @JsonAttribute(ignore = true)
    public int getTopIndex() {
        return ((DartTable) impl).getTopIndex();
    }

    public void setTopIndex(int value) {
        ((DartTable) impl).topIndex = value;
    }

    public int getItemCount() {
        return ((DartTable) impl).getItemCount();
    }

    public void setItemCount(int value) {
    }

    public static final String COLUMNS = "columns";

    public static final String EDITORS = "editors";

    public static final String HEADER_BACKGROUND = "headerBackground";

    public static final String HEADER_FOREGROUND = "headerForeground";

    public static final String HEADER_VISIBLE = "headerVisible";

    public static final String ITEM_COUNT = "itemCount";

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
            case "headerForeground":
                Serializer.writeKeyValue(writer, "headerForeground", getHeaderForeground());
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
            case "itemCount":
                Serializer.writeKeyValue(writer, "itemCount", getItemCount());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Table.class)
    public static class TableJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTable.class, (JsonWriter.WriteObject<DartTable>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTable.class, (JsonReader.ReadObject<DartTable>) reader -> {
                return null;
            });
        }

        public static Table read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Table api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
