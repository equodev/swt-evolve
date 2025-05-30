package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

public class VScrollable extends VControl {

    public int scrollbarsMode;

    @JsonConverter(target = Scrollable.class)
    public static class ScrollableJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartScrollable.class, (JsonWriter.WriteObject<DartScrollable>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartScrollable.class, (JsonReader.ReadObject<DartScrollable>) reader -> {
                return null;
            });
        }

        public static Scrollable read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Scrollable api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
