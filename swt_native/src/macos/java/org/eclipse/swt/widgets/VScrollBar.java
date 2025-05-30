package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VScrollBar extends VWidget {

    public boolean enabled;

    public int increment;

    public int maximum;

    public int minimum;

    public int pageIncrement;

    public int selection;

    public int thumb;

    public boolean visible;

    @JsonConverter(target = ScrollBar.class)
    public static class ScrollBarJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartScrollBar.class, (JsonWriter.WriteObject<DartScrollBar>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartScrollBar.class, (JsonReader.ReadObject<DartScrollBar>) reader -> {
                return null;
            });
        }

        public static ScrollBar read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, ScrollBar api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
