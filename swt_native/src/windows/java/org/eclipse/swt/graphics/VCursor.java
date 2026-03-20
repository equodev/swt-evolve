package org.eclipse.swt.graphics;

import java.util.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

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

    @JsonConverter(target = Cursor.class)
    public static class CursorJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCursor.class, (JsonWriter.WriteObject<DartCursor>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartCursor.class, (JsonReader.ReadObject<DartCursor>) reader -> {
                return null;
            });
        }

        public static Cursor read(JsonReader<?> reader) {
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
