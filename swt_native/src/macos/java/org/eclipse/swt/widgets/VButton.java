package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VButton extends VControl {

    public int alignment;

    public boolean grayed;

    @JsonAttribute(ignore = true)
    public Image image;

    public boolean selection;

    public String text;

    @JsonConverter(target = Button.class)
    public static class ButtonJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartButton.class, (JsonWriter.WriteObject<DartButton>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartButton.class, (JsonReader.ReadObject<DartButton>) reader -> {
                return null;
            });
        }

        public static Button read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Button api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
