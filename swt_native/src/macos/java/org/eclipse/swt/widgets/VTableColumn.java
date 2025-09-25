package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VTableColumn extends VItem {

    protected VTableColumn() {
    }

    protected VTableColumn(DartTableColumn impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartTableColumn) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartTableColumn) impl).setAlignment(value);
    }

    public boolean getMoveable() {
        return ((DartTableColumn) impl).getMoveable();
    }

    public void setMoveable(boolean value) {
        ((DartTableColumn) impl).movable = value;
    }

    public boolean getResizable() {
        return ((DartTableColumn) impl).getResizable();
    }

    public void setResizable(boolean value) {
        ((DartTableColumn) impl).resizable = value;
    }

    public String getToolTipText() {
        return ((DartTableColumn) impl).toolTipText;
    }

    public void setToolTipText(String value) {
        ((DartTableColumn) impl).toolTipText = value;
    }

    public int getWidth() {
        return ((DartTableColumn) impl).getWidth();
    }

    public void setWidth(int value) {
        ((DartTableColumn) impl).width = value;
    }

    @JsonConverter(target = TableColumn.class)
    public static class TableColumnJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTableColumn.class, (JsonWriter.WriteObject<DartTableColumn>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTableColumn.class, (JsonReader.ReadObject<DartTableColumn>) reader -> {
                return null;
            });
        }

        public static TableColumn read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, TableColumn api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
