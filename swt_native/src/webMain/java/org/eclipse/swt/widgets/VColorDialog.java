package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;
import java.util.ArrayList;

@CompiledJson()
public class VColorDialog extends VDialog {

    protected VColorDialog() {
    }

    protected VColorDialog(DartColorDialog impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public RGB getRGB() {
        return ((DartColorDialog) impl).rgb;
    }

    public void setRGB(RGB value) {
        ((DartColorDialog) impl).rgb = value;
    }

    @JsonAttribute(ignore = true)
    public RGB[] getRGBs() {
        RGB[] values = ((DartColorDialog) impl).rgbs;
        if (values == null)
            return null;
        ArrayList<RGB> result = new ArrayList<>(values.length);
        for (RGB v : values) if (v != null)
            result.add(v);
        return result.toArray(RGB[]::new);
    }

    public void setRGBs(RGB[] value) {
        ((DartColorDialog) impl).rgbs = value;
    }

    @JsonConverter(target = ColorDialog.class)
    public static class ColorDialogJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartColorDialog.class, (JsonWriter.WriteObject<DartColorDialog>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartColorDialog.class, (JsonReader.ReadObject<DartColorDialog>) reader -> {
                return null;
            });
        }

        public static ColorDialog read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, ColorDialog api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
