package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VFont extends VResource {

    protected VFont() {
    }

    protected VFont(DartFont impl) {
        super(impl);
    }

    public FontData[] getFontData() {
        FontData[] values = ((DartFont) impl).getFontData();
        if (values == null)
            return null;
        ArrayList<FontData> result = new ArrayList<>(values.length);
        for (FontData v : values) if (v != null)
            result.add(v);
        return result.toArray(FontData[]::new);
    }

    public void setFontData(FontData[] value) {
        ((DartFont) impl)._fontData = value;
    }

    @JsonConverter(target = Font.class)
    public static class FontJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartFont.class, (JsonWriter.WriteObject<DartFont>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartFont.class, (JsonReader.ReadObject<DartFont>) reader -> {
                return null;
            });
        }

        public static Font read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Font api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
