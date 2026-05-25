package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

public class VDialog {

    protected VDialog() {
    }

    protected VDialog(IDialog impl) {
        this.impl = impl;
    }

    protected IDialog impl;

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getStyle() {
        return ((DartDialog) impl).getStyle();
    }

    public void setStyle(int value) {
        ((DartDialog) impl).style = value;
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartDialog) impl).getText();
    }

    public void setText(String value) {
        ((DartDialog) impl).title = value;
    }

    @JsonConverter(target = Dialog.class)
    public static class DialogJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartDialog.class, (JsonWriter.WriteObject<DartDialog>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartDialog.class, (JsonReader.ReadObject<DartDialog>) reader -> {
                return null;
            });
        }

        public static Dialog read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Dialog api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
