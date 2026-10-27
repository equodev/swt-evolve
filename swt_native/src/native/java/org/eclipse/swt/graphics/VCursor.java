package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VCursor extends VResource {

    protected VCursor() {
    }

    protected VCursor(DartCursor impl) {
        super(impl);
    }

    public int getCursorStyle() {
        return ((DartCursor) impl).cursorStyle;
    }

    public void setCursorStyle(int value) {
        ((DartCursor) impl).cursorStyle = value;
    }

    public int getHotspotX() {
        return ((DartCursor) impl).hotspotX;
    }

    public void setHotspotX(int value) {
        ((DartCursor) impl).hotspotX = value;
    }

    public int getHotspotY() {
        return ((DartCursor) impl).hotspotY;
    }

    public void setHotspotY(int value) {
        ((DartCursor) impl).hotspotY = value;
    }

    @JsonAttribute(nullable = true)
    public Image getImage() {
        Image val = ((DartCursor) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartCursor) impl).image = value;
    }

    public static final String CURSOR_STYLE = "cursorStyle";

    public static final String HOTSPOT_X = "hotspotX";

    public static final String HOTSPOT_Y = "hotspotY";

    public static final String IMAGE = "image";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "cursorStyle":
                Serializer.writeKeyValue(writer, "cursorStyle", getCursorStyle());
                return;
            case "hotspotX":
                Serializer.writeKeyValue(writer, "hotspotX", getHotspotX());
                return;
            case "hotspotY":
                Serializer.writeKeyValue(writer, "hotspotY", getHotspotY());
                return;
            case "image":
                Serializer.writeKeyValue(writer, "image", getImage());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Cursor.class)
    public static class CursorJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCursor.class, (JsonWriter.WriteObject<DartCursor>) (writer, impl) -> {
                if (impl == null || impl.isDisposed())
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartCursor.class, (JsonReader.ReadObject<DartCursor>) reader -> {
                return null;
            });
        }

        public static Cursor read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Cursor api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
