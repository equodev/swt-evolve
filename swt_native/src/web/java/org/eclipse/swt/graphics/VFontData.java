package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VFontData {

    protected VFontData() {
    }

    protected VFontData(DartFontData impl) {
        this.impl = impl;
    }

    protected DartFontData impl;

    public int getHeight() {
        return ((DartFontData) impl).getHeight();
    }

    public void setHeight(int value) {
        ((DartFontData) impl).getApi().height = value;
    }

    public String getLocale() {
        return ((DartFontData) impl).getLocale();
    }

    public void setLocale(String value) {
        ((DartFontData) impl).lang = value;
    }

    public String getName() {
        return ((DartFontData) impl).getName();
    }

    public void setName(String value) {
        ((DartFontData) impl).getApi().name = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getStyle() {
        return ((DartFontData) impl).getStyle();
    }

    public void setStyle(int value) {
        ((DartFontData) impl).setStyle(value);
    }

    @JsonConverter(target = FontData.class)
    public static class FontDataJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartFontData.class, (JsonWriter.WriteObject<DartFontData>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartFontData.class, (JsonReader.ReadObject<DartFontData>) reader -> {
                return null;
            });
        }

        public static FontData read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, FontData api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
