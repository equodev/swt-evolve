package org.eclipse.swt.graphics;

import java.util.*;
import java.util.function.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;

@CompiledJson()
public class VResource {

    @JsonConverter(target = Resource.class)
    public abstract static class ResourceJson {

        public static Resource read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Resource api) {
            if (api == null)
                writer.writeNull();
            else {
                VResource value = ((DartResource) api.getImpl()).getValue();
                writer.serializeObject(value);
            }
        }
    }
}
