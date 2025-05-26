package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;

@CompiledJson()
public class VColor extends VResource {

    public double[] handle;

    @JsonConverter(target = Color.class)
    public abstract static class ColorJson {

        public static Color read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Color api) {
            if (api == null)
                writer.writeNull();
            else {
                VColor value = ((DartColor) api.getImpl()).getValue();
                writer.serializeObject(value);
            }
        }
    }
}
