package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VColor extends VResource {

    public double[] handle;

    @JsonConverter(target = Color.class)
    public static class ColorJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartColor.class, (JsonWriter.WriteObject<DartColor>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartColor.class, (JsonReader.ReadObject<DartColor>) reader -> {
                return null;
            });
        }

        public static Color read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Color api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
