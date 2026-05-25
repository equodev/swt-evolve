package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VTransform extends VResource {

    protected VTransform() {
    }

    protected VTransform(DartTransform impl) {
        super(impl);
    }

    public float[] getElements() {
        float[] result = new float[6];
        ((DartTransform) impl).getElements(result);
        return result;
    }

    public void setElements(float[] value) {
        ((DartTransform) impl).setElements(value[0], value[1], value[2], value[3], value[4], value[5]);
    }

    @JsonConverter(target = Transform.class)
    public static class TransformJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTransform.class, (JsonWriter.WriteObject<DartTransform>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartTransform.class, (JsonReader.ReadObject<DartTransform>) reader -> {
                return null;
            });
        }

        public static Transform read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Transform api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
