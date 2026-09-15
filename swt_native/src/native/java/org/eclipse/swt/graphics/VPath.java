package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VPath extends VResource {

    protected VPath() {
    }

    protected VPath(DartPath impl) {
        super(impl);
    }

    public PathData getPathData() {
        return ((DartPath) impl).pathData;
    }

    public void setPathData(PathData value) {
        ((DartPath) impl).pathData = value;
    }

    public static final String PATH_DATA = "pathData";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "pathData":
                Serializer.writeKeyValue(writer, "pathData", getPathData());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Path.class)
    public static class PathJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartPath.class, (JsonWriter.WriteObject<DartPath>) (writer, impl) -> {
                if (impl == null || impl.isDisposed())
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartPath.class, (JsonReader.ReadObject<DartPath>) reader -> {
                return null;
            });
        }

        public static Path read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Path api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
