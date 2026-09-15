package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VProgressBar extends VControl {

    protected VProgressBar() {
    }

    protected VProgressBar(DartProgressBar impl) {
        super(impl);
    }

    public int getMaximum() {
        return ((DartProgressBar) impl).getMaximum();
    }

    public void setMaximum(int value) {
        ((DartProgressBar) impl).maximum = value;
    }

    public int getMinimum() {
        return ((DartProgressBar) impl).getMinimum();
    }

    public void setMinimum(int value) {
        ((DartProgressBar) impl).minimum = value;
    }

    public int getSelection() {
        return ((DartProgressBar) impl).getSelection();
    }

    public void setSelection(int value) {
        ((DartProgressBar) impl).selection = value;
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

    @JsonConverter(target = ProgressBar.class)
    public static class ProgressBarJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartProgressBar.class, (JsonWriter.WriteObject<DartProgressBar>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartProgressBar.class, (JsonReader.ReadObject<DartProgressBar>) reader -> {
                return null;
            });
        }

        public static ProgressBar read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, ProgressBar api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
