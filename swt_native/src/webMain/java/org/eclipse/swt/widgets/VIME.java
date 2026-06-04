package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;
import java.util.ArrayList;

@CompiledJson()
public class VIME extends VWidget {

    protected VIME() {
    }

    protected VIME(DartIME impl) {
        super(impl);
    }

    public int getCompositionOffset() {
        return ((DartIME) impl).getCompositionOffset();
    }

    public void setCompositionOffset(int value) {
        ((DartIME) impl).startOffset = value;
    }

    public int[] getRanges() {
        return ((DartIME) impl).ranges;
    }

    public void setRanges(int[] value) {
        ((DartIME) impl).ranges = value;
    }

    public TextStyle[] getStyles() {
        TextStyle[] values = ((DartIME) impl).styles;
        if (values == null)
            return null;
        ArrayList<TextStyle> result = new ArrayList<>(values.length);
        for (TextStyle v : values) if (v != null)
            result.add(v);
        return result.toArray(TextStyle[]::new);
    }

    public void setStyles(TextStyle[] value) {
        ((DartIME) impl).styles = value;
    }

    @JsonConverter(target = IME.class)
    public static class IMEJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartIME.class, (JsonWriter.WriteObject<DartIME>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartIME.class, (JsonReader.ReadObject<DartIME>) reader -> {
                return null;
            });
        }

        public static IME read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, IME api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
