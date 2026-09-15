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

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getAlpha() {
        return ((IColor) impl).getAlpha();
    }

    public void setAlpha(int value) {
    }

    public int getBlue() {
        return ((IColor) impl).getBlue();
    }

    public void setBlue(int value) {
    }

    public int getGreen() {
        return ((IColor) impl).getGreen();
    }

    public void setGreen(int value) {
    }

    public int getRed() {
        return ((IColor) impl).getRed();
    }

    public void setRed(int value) {
    }

    public static final String ALPHA = "alpha";

    public static final String BLUE = "blue";

    public static final String GREEN = "green";

    public static final String RED = "red";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "alpha":
                Serializer.writeKeyValue(writer, "alpha", getAlpha());
                return;
            case "blue":
                Serializer.writeKeyValue(writer, "blue", getBlue());
                return;
            case "green":
                Serializer.writeKeyValue(writer, "green", getGreen());
                return;
            case "red":
                Serializer.writeKeyValue(writer, "red", getRed());
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
