package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson(objectFormatPolicy = CompiledJson.ObjectFormatPolicy.FULL)
public class VColor extends VResource {

    protected VColor() {
    }

    protected VColor(IColor impl) {
        super(impl);
    }

    @JsonAttribute(name = "a", includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getAlpha() {
        return ((IColor) impl).getAlpha();
    }

    public void setAlpha(int value) {
    }

    @JsonAttribute(name = "b")
    public int getBlue() {
        return ((IColor) impl).getBlue();
    }

    public void setBlue(int value) {
    }

    @JsonAttribute(name = "g")
    public int getGreen() {
        return ((IColor) impl).getGreen();
    }

    public void setGreen(int value) {
    }

    @JsonAttribute(name = "r")
    public int getRed() {
        return ((IColor) impl).getRed();
    }

    public void setRed(int value) {
    }

    public static final String A = "a";

    public static final String B = "b";

    public static final String G = "g";

    public static final String R = "r";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "a":
                Serializer.writeKeyValue(writer, "a", getAlpha());
                return;
            case "b":
                Serializer.writeKeyValue(writer, "b", getBlue());
                return;
            case "g":
                Serializer.writeKeyValue(writer, "g", getGreen());
                return;
            case "r":
                Serializer.writeKeyValue(writer, "r", getRed());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Color.class)
    public static class ColorJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartColor.class, (JsonWriter.WriteObject<DartColor>) (writer, impl) -> {
                if (impl == null || impl.isDisposed())
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartColor.class, (JsonReader.ReadObject<DartColor>) reader -> {
                return null;
            });
        }

        public static Color read(JsonReader<?> reader) throws IOException {
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
