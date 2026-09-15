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

    @JsonAttribute(nullable = false)
    public int[] getRects() {
        return org.eclipse.swt.graphics.RegionHelper.flatten(((DartRegion) impl).rects);
    }

    public void setRects(int[] value) {
    }

    public static final String RECTS = "rects";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "rects":
                Serializer.writeKeyValue(writer, "rects", getRects());
                return;
        }
        super.writeProperty(writer, key);
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
