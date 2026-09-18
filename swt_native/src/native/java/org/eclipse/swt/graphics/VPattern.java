package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VPattern extends VResource {

    protected VPattern() {
    }

    protected VPattern(DartPattern impl) {
        super(impl);
    }

    public Color getColor1() {
        return ((DartPattern) impl).color1;
    }

    public void setColor1(Color value) {
    }

    public Color getColor2() {
        return ((DartPattern) impl).color2;
    }

    public void setColor2(Color value) {
    }

    public float getEndX() {
        return ((DartPattern) impl).endX;
    }

    public void setEndX(float value) {
        ((DartPattern) impl).endX = value;
    }

    public float getEndY() {
        return ((DartPattern) impl).endY;
    }

    public void setEndY(float value) {
        ((DartPattern) impl).endY = value;
    }

    @JsonAttribute(nullable = true)
    public Image getImage() {
        Image val = ((DartPattern) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartPattern) impl).image = value;
    }

    public float getStartX() {
        return ((DartPattern) impl).startX;
    }

    public void setStartX(float value) {
        ((DartPattern) impl).startX = value;
    }

    public float getStartY() {
        return ((DartPattern) impl).startY;
    }

    public void setStartY(float value) {
        ((DartPattern) impl).startY = value;
    }

    public static final String COLOR1 = "color1";

    public static final String COLOR2 = "color2";

    public static final String END_X = "endX";

    public static final String END_Y = "endY";

    public static final String IMAGE = "image";

    public static final String START_X = "startX";

    public static final String START_Y = "startY";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "color1":
                Serializer.writeKeyValue(writer, "color1", getColor1());
                return;
            case "color2":
                Serializer.writeKeyValue(writer, "color2", getColor2());
                return;
            case "endX":
                Serializer.writeKeyValue(writer, "endX", getEndX());
                return;
            case "endY":
                Serializer.writeKeyValue(writer, "endY", getEndY());
                return;
            case "image":
                Serializer.writeKeyValue(writer, "image", getImage());
                return;
            case "startX":
                Serializer.writeKeyValue(writer, "startX", getStartX());
                return;
            case "startY":
                Serializer.writeKeyValue(writer, "startY", getStartY());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Pattern.class)
    public static class PatternJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartPattern.class, (JsonWriter.WriteObject<DartPattern>) (writer, impl) -> {
                if (impl == null || impl.isDisposed())
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartPattern.class, (JsonReader.ReadObject<DartPattern>) reader -> {
                return null;
            });
        }

        public static Pattern read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Pattern api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
