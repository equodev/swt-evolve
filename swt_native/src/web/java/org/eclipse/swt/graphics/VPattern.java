package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VPattern extends VResource {

    protected VPattern() {
    }

    protected VPattern(DartPattern impl) {
        super(impl);
    }

    @JsonConverter(target = Pattern.class)
    public static class PatternJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartPattern.class, (JsonWriter.WriteObject<DartPattern>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartPattern.class, (JsonReader.ReadObject<DartPattern>) reader -> {
                return null;
            });
        }

        public static Pattern read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Pattern api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
