package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VCaret extends VWidget {

    protected VCaret() {
    }

    protected VCaret(DartCaret impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public Font getFont() {
        Font val = ((DartCaret) impl).font;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartCaret) impl).font = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public boolean getVisible() {
        return ((DartCaret) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartCaret) impl).isVisible = value;
    }

    public Rectangle getBounds() {
        return ((DartCaret) impl).getBounds();
    }

    public void setBounds(Rectangle value) {
    }

    public Image getImage() {
        return ((DartCaret) impl).image;
    }

    public void setImage(Image value) {
    }

    public boolean getFocused() {
        return ((DartCaret) impl).isFocusCaret();
    }

    public void setFocused(boolean value) {
    }

    public static final String BOUNDS = "bounds";

    public static final String FOCUSED = "focused";

    public static final String IMAGE = "image";

    public static final String VISIBLE = "visible";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "visible":
                Serializer.writeKeyValue(writer, "visible", getVisible());
                return;
            case "bounds":
                Serializer.writeKeyValue(writer, "bounds", getBounds());
                return;
            case "image":
                Serializer.writeKeyValue(writer, "image", getImage());
                return;
            case "focused":
                Serializer.writeKeyValue(writer, "focused", getFocused());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Caret.class)
    public static class CaretJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCaret.class, (JsonWriter.WriteObject<DartCaret>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartCaret.class, (JsonReader.ReadObject<DartCaret>) reader -> {
                return null;
            });
        }

        public static Caret read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Caret api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
