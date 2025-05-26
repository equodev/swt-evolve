package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;

@CompiledJson()
public class VButton extends VControl {

    public int alignment;

    public boolean grayed;

    @JsonAttribute(ignore = true)
    public Image image;

    public boolean selection;

    public String text;

    @JsonConverter(target = Button.class)
    public abstract static class ButtonJson {

        public static Button read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Button api) {
            if (api == null)
                writer.writeNull();
            else {
                VButton value = ((DartButton) api.getImpl()).getValue();
                writer.serializeObject(value);
            }
        }
    }
}
