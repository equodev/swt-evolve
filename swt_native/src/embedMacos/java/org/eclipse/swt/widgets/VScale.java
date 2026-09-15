package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VScale extends VControl {

    protected VScale() {
    }

    protected VScale(DartScale impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public int getIncrement() {
        return ((DartScale) impl).getIncrement();
    }

    public void setIncrement(int value) {
        ((DartScale) impl).increment = value;
    }

    public int getMaximum() {
        return ((DartScale) impl).getMaximum();
    }

    public void setMaximum(int value) {
        ((DartScale) impl).maximum = value;
    }

    public int getMinimum() {
        return ((DartScale) impl).getMinimum();
    }

    public void setMinimum(int value) {
        ((DartScale) impl).minimum = value;
    }

    @JsonAttribute(ignore = true)
    public int getPageIncrement() {
        return ((DartScale) impl).getPageIncrement();
    }

    public void setPageIncrement(int value) {
        ((DartScale) impl).pageIncrement = value;
    }

    public int getSelection() {
        return ((DartScale) impl).getSelection();
    }

    public void setSelection(int value) {
        ((DartScale) impl).selection = value;
    }

    public static final String MAXIMUM = "maximum";

    public static final String MINIMUM = "minimum";

    public static final String SELECTION = "selection";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "maximum":
                Serializer.writeKeyValue(writer, "maximum", getMaximum());
                return;
            case "minimum":
                Serializer.writeKeyValue(writer, "minimum", getMinimum());
                return;
            case "selection":
                Serializer.writeKeyValue(writer, "selection", getSelection());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Scale.class)
    public static class ScaleJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartScale.class, (JsonWriter.WriteObject<DartScale>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartScale.class, (JsonReader.ReadObject<DartScale>) reader -> {
                return null;
            });
        }

        public static Scale read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Scale api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
