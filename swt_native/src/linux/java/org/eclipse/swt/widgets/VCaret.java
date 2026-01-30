package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VCaret extends VWidget {

    protected VCaret() {
    }

    protected VCaret(DartCaret impl) {
        super(impl);
    }

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

    @JsonAttribute(nullable = true)
    public Image getImage() {
        Image val = ((DartCaret) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartCaret) impl).image = value;
    }

    public boolean getVisible() {
        return ((DartCaret) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartCaret) impl).isVisible = value;
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

        public static Caret read(JsonReader<?> reader) {
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
