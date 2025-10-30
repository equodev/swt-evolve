package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

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

        public static ProgressBar read(JsonReader<?> reader) {
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
