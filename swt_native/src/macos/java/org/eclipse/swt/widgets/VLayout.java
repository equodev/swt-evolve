package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

public class VLayout {

    @JsonConverter(target = Layout.class)
    public static class LayoutJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartLayout.class, (JsonWriter.WriteObject<DartLayout>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartLayout.class, (JsonReader.ReadObject<DartLayout>) reader -> {
                return null;
            });
        }

        public static Layout read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Layout api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
