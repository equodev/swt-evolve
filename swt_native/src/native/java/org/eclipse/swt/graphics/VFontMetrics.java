package org.eclipse.swt.graphics;

import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

public class VFontMetrics {

    protected VFontMetrics() {
    }

    protected VFontMetrics(DartFontMetrics impl) {
        this.impl = impl;
    }

    protected DartFontMetrics impl;

    @JsonConverter(target = FontMetrics.class)
    public static class FontMetricsJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartFontMetrics.class, (JsonWriter.WriteObject<DartFontMetrics>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartFontMetrics.class, (JsonReader.ReadObject<DartFontMetrics>) reader -> {
                return null;
            });
        }

        public static FontMetrics read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, FontMetrics api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }

    @JsonConverter(target = VFontMetrics.class)
    public static class EmptyJson {

        public static VFontMetrics read(JsonReader<?> reader) throws IOException {
            if (reader.wasNull())
                return null;
            reader.skip();
            return new VFontMetrics();
        }

        public static void write(JsonWriter writer, VFontMetrics value) {
            if (value == null)
                writer.writeNull();
            else
                writer.writeAscii("{}");
        }
    }
}
