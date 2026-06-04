package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VGCData {

    protected VGCData() {
    }

    protected VGCData(DartGCData impl) {
        this.impl = impl;
    }

    protected DartGCData impl;

    @JsonConverter(target = GCData.class)
    public static class GCDataJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartGCData.class, (JsonWriter.WriteObject<DartGCData>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartGCData.class, (JsonReader.ReadObject<DartGCData>) reader -> {
                return null;
            });
        }

        public static GCData read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, GCData api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
