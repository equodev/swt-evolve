package org.eclipse.swt.widgets;

import java.util.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import com.dslplatform.json.*;

@CompiledJson()
public class VWidget {

    @JsonConverter(target = Widget.class)
    public abstract static class WidgetJson {

        public static Widget read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Widget api) {
            if (api == null)
                writer.writeNull();
            else {
                VWidget value = ((DartWidget) api.getImpl()).getValue();
                writer.serializeObject(value);
            }
        }
    }
}
