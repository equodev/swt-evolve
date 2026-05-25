package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VTableEditor extends VControlEditor {

    protected VTableEditor() {
    }

    protected VTableEditor(DartTableEditor impl) {
        super(impl);
    }

    public int getColumn() {
        return ((DartTableEditor) impl).getColumn();
    }

    public void setColumn(int value) {
        ((DartTableEditor) impl).column = value;
    }

    public TableItem getItem() {
        TableItem val = ((DartTableEditor) impl).item;
        if (val != null && !(val.getImpl() instanceof DartTableItem))
            return null;
        return val;
    }

    public void setItem(TableItem value) {
        ((DartTableEditor) impl).item = value;
    }

    @JsonConverter(target = TableEditor.class)
    public static class TableEditorJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTableEditor.class, (JsonWriter.WriteObject<DartTableEditor>) (writer, impl) -> {
                Serializer.writeEditorWithId(json, writer, impl);
            });
            json.registerReader(DartTableEditor.class, (JsonReader.ReadObject<DartTableEditor>) reader -> {
                return null;
            });
        }

        public static TableEditor read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, TableEditor api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
