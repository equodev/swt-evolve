package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

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

    @JsonAttribute(ignore = true)
    public boolean getMoveable() {
        return ((DartTableColumn) impl).getMoveable();
    }

    public void setMoveable(boolean value) {
        ((DartTableColumn) impl).moveable = value;
    }

    public boolean getResizable() {
        return ((DartTableColumn) impl).getResizable();
    }

    public void setResizable(boolean value) {
        ((DartTableColumn) impl).resizable = value;
    }

    @JsonAttribute(ignore = true)
    public String getToolTipText() {
        return ((DartTableColumn) impl).getToolTipText();
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

    public static final String ALIGNMENT = "alignment";

    public static final String RESIZABLE = "resizable";

    public static final String WIDTH = "width";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "alignment":
                Serializer.writeKeyValue(writer, "alignment", getAlignment());
                return;
            case "resizable":
                Serializer.writeKeyValue(writer, "resizable", getResizable());
                return;
            case "width":
                Serializer.writeKeyValue(writer, "width", getWidth());
                return;
        }
        super.writeProperty(writer, key);
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

        public static TableColumn read(JsonReader<?> reader) throws IOException {
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
