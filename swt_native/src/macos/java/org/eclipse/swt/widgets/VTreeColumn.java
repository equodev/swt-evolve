package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VTreeColumn extends VItem {

    protected VTreeColumn() {
    }

    protected VTreeColumn(DartTreeColumn impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartTreeColumn) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartTreeColumn) impl).setAlignment(value);
    }

    public boolean getMoveable() {
        return ((DartTreeColumn) impl).getMoveable();
    }

    public void setMoveable(boolean value) {
        ((DartTreeColumn) impl).movable = value;
    }

    public boolean getResizable() {
        return ((DartTreeColumn) impl).getResizable();
    }

    public void setResizable(boolean value) {
        ((DartTreeColumn) impl).resizable = value;
    }

    public String getToolTipText() {
        return ((DartTreeColumn) impl).toolTipText;
    }

    public void setToolTipText(String value) {
        ((DartTreeColumn) impl).toolTipText = value;
    }

    public int getWidth() {
        return ((DartTreeColumn) impl).getWidth();
    }

    public void setWidth(int value) {
        ((DartTreeColumn) impl).width = value;
    }

    @JsonConverter(target = TreeColumn.class)
    public static class TreeColumnJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTreeColumn.class, (JsonWriter.WriteObject<DartTreeColumn>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTreeColumn.class, (JsonReader.ReadObject<DartTreeColumn>) reader -> {
                return null;
            });
        }

        public static TreeColumn read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, TreeColumn api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
