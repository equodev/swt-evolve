package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VPopupList {

    protected VPopupList() {
    }

    protected VPopupList(DartPopupList impl) {
        this.impl = impl;
    }

    protected DartPopupList impl;

    public Font getFont() {
        Font val = ((DartPopupList) impl).font;
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartPopupList) impl).font = value;
    }

    public String[] getItems() {
        String[] values = ((DartPopupList) impl).items;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(String[]::new);
    }

    public void setItems(String[] value) {
        ((DartPopupList) impl).items = value;
    }

    public int getMinimumWidth() {
        return ((DartPopupList) impl).getMinimumWidth();
    }

    public void setMinimumWidth(int value) {
        ((DartPopupList) impl).minimumWidth = value;
    }

    public String getSelection() {
        return ((DartPopupList) impl).selection;
    }

    public void setSelection(String value) {
        ((DartPopupList) impl).selection = value;
    }

    @JsonConverter(target = PopupList.class)
    public static class PopupListJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartPopupList.class, (JsonWriter.WriteObject<DartPopupList>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartPopupList.class, (JsonReader.ReadObject<DartPopupList>) reader -> {
                return null;
            });
        }

        public static PopupList read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, PopupList api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
