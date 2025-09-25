package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VTable extends VComposite {

    protected VTable() {
    }

    protected VTable(DartTable impl) {
        super(impl);
    }

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

    public TableColumn getSortColumn() {
        TableColumn val = ((DartTable) impl).sortColumn;
        if (val != null && !(val.getImpl() instanceof DartTableColumn))
            return null;
        return val;
    }

    public void setSortColumn(TableColumn value) {
        ((DartTable) impl).sortColumn = value;
    }

    public int getSortDirection() {
        return ((DartTable) impl).getSortDirection();
    }

    public void setSortDirection(int value) {
        ((DartTable) impl).sortDirection = value;
    }

    public int getTopIndex() {
        return ((DartTable) impl).getTopIndex();
    }

    public void setTopIndex(int value) {
        ((DartTable) impl).topIndex = value;
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

        public static Table read(JsonReader<?> reader) {
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
