package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VRegion extends VResource {

    protected VRegion() {
    }

    protected VRegion(DartRegion impl) {
        super(impl);
    }

    @JsonConverter(target = Region.class)
    public static class RegionJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartRegion.class, (JsonWriter.WriteObject<DartRegion>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartRegion.class, (JsonReader.ReadObject<DartRegion>) reader -> {
                return null;
            });
        }

        public static Region read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Region api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
