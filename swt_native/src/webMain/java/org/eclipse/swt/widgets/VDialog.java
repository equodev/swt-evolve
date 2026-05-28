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

    protected String swt = "";

    protected long id = 0L;

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public String getSwt() {
        return swt;
    }

    public void setSwt(String v) {
        this.swt = v;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public long getId() {
        return id;
    }

    public void setId(long v) {
        this.id = v;
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
            json.registerWriter(VDialog.class, (JsonWriter.WriteObject<VDialog>) (writer, vDialog) -> {
                if (vDialog == null) {
                    writer.writeNull();
                    return;
                }
                if (vDialog.getClass() == VDialog.class) {
                    writer.writeNull();
                    return;
                }
                @SuppressWarnings("unchecked")
                JsonWriter.WriteObject<VDialog> w = (JsonWriter.WriteObject<VDialog>) json.tryFindWriter(vDialog.getClass());
                if (w != null)
                    w.write(writer, vDialog);
                else
                    writer.writeNull();
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
