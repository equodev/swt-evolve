package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VFontDialog extends VDialog {

    protected VFontDialog() {
    }

    protected VFontDialog(DartFontDialog impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public RGB getRGB() {
        return ((DartFontDialog) impl).rgb;
    }

    public void setRGB(RGB value) {
        ((DartFontDialog) impl).rgb = value;
    }

    public boolean getEffectsVisible() {
        return ((DartFontDialog) impl).getEffectsVisible();
    }

    public void setEffectsVisible(boolean value) {
        ((DartFontDialog) impl).effectsVisible = value;
    }

    public FontData getFontData() {
        FontData val = ((DartFontDialog) impl).fontData;
        if (val != null && !(val.getImpl() instanceof DartFontData))
            return null;
        return val;
    }

    public void setFontData(FontData value) {
        ((DartFontDialog) impl).fontData = value;
    }

    public FontData[] getFontList() {
        FontData[] values = ((DartFontDialog) impl).fontList;
        if (values == null)
            return null;
        ArrayList<FontData> result = new ArrayList<>(values.length);
        for (FontData v : values) if (v != null)
            result.add(v);
        return result.toArray(FontData[]::new);
    }

    public void setFontList(FontData[] value) {
        ((DartFontDialog) impl).fontList = value;
    }

    @JsonConverter(target = FontDialog.class)
    public static class FontDialogJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartFontDialog.class, (JsonWriter.WriteObject<DartFontDialog>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartFontDialog.class, (JsonReader.ReadObject<DartFontDialog>) reader -> {
                return null;
            });
        }

        public static FontDialog read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, FontDialog api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
