package org.eclipse.swt.graphics;

import java.util.*;
import java.util.function.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

public class VResource {

    protected VResource() {
    }

    protected VResource(IResource impl) {
        this.impl = impl;
    }

    protected IResource impl;

    @JsonConverter(target = Resource.class)
    public static class ResourceJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartResource.class, (JsonWriter.WriteObject<DartResource>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartResource.class, (JsonReader.ReadObject<DartResource>) reader -> {
                return null;
            });
        }

        public static Resource read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Resource api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
