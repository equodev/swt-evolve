package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VMessageBox extends VDialog {

    protected VMessageBox() {
    }

    protected VMessageBox(DartMessageBox impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public Map<Integer, String> getButtonLabels() {
        return ((DartMessageBox) impl).labels;
    }

    public void setButtonLabels(Map<Integer, String> value) {
        ((DartMessageBox) impl).labels = value;
    }

    public String getMessage() {
        return ((DartMessageBox) impl).getMessage();
    }

    public void setMessage(String value) {
        ((DartMessageBox) impl).message = value;
    }

    @JsonConverter(target = MessageBox.class)
    public static class MessageBoxJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartMessageBox.class, (JsonWriter.WriteObject<DartMessageBox>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartMessageBox.class, (JsonReader.ReadObject<DartMessageBox>) reader -> {
                return null;
            });
        }

        public static MessageBox read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, MessageBox api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
