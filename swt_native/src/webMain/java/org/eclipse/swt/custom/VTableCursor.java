package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VTableCursor extends VCanvas {

    protected VTableCursor() {
    }

    protected VTableCursor(DartTableCursor impl) {
        super(impl);
    }

    @JsonConverter(target = TableCursor.class)
    public static class TableCursorJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTableCursor.class, (JsonWriter.WriteObject<DartTableCursor>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTableCursor.class, (JsonReader.ReadObject<DartTableCursor>) reader -> {
                return null;
            });
        }

        public static TableCursor read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, TableCursor api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
