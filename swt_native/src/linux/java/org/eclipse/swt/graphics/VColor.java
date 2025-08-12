package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson(objectFormatPolicy = CompiledJson.ObjectFormatPolicy.FULL)
public class VColor extends VResource {

    protected VColor() {
    }

    protected VColor(IColor impl) {
        super(impl);
    }

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

    @JsonConverter(target = Color.class)
    public static class ColorJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartColor.class, (JsonWriter.WriteObject<DartColor>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartColor.class, (JsonReader.ReadObject<DartColor>) reader -> {
                return null;
            });
        }

        public static Color read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Color api) {
            if (api == null)
                writer.writeNull();
            else if (api.getImpl() instanceof SwtColor s)
                writer.serializeObject(new VColor(s));
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
