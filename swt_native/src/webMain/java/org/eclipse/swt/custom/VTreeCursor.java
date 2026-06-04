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
public class VTreeCursor extends VCanvas {

    protected VTreeCursor() {
    }

    protected VTreeCursor(DartTreeCursor impl) {
        super(impl);
    }

    @JsonConverter(target = TreeCursor.class)
    public static class TreeCursorJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTreeCursor.class, (JsonWriter.WriteObject<DartTreeCursor>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTreeCursor.class, (JsonReader.ReadObject<DartTreeCursor>) reader -> {
                return null;
            });
        }

        public static TreeCursor read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, TreeCursor api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
