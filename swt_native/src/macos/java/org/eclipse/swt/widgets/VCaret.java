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

    @JsonAttribute(ignore = true)
    public Font getFont() {
        return ((DartCaret) impl).font;
    }

    public void setFont(Font value) {
        ((DartCaret) impl).font = value;
    }

    @JsonAttribute(ignore = true)
    public Image getImage() {
        return ((DartCaret) impl).image;
    }

    public void setImage(Image value) {
        ((DartCaret) impl).image = value;
    }

    public boolean getIsVisible() {
        return ((DartCaret) impl).getVisible();
    }

    public void setIsVisible(boolean value) {
        ((DartCaret) impl).isVisible = value;
    }

    public int getHeight() {
        return ((DartCaret) impl).height;
    }

    public void setHeight(int value) {
        ((DartCaret) impl).height = value;
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
