package org.eclipse.swt.widgets;

import java.util.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

public class VWidget {

    protected VWidget() {
    }

    protected VWidget(IWidget impl) {
        this.impl = impl;
    }

    protected IWidget impl;

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getStyle() {
        return ((DartWidget) impl).getStyle();
    }

    public void setStyle(int value) {
        ((DartWidget) impl).getApi().style = value;
    }

    @JsonConverter(target = Widget.class)
    public static class WidgetJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartWidget.class, (JsonWriter.WriteObject<DartWidget>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartWidget.class, (JsonReader.ReadObject<DartWidget>) reader -> {
                return null;
            });
        }

        public static Widget read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Widget api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
