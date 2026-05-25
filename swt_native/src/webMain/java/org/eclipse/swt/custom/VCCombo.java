package org.eclipse.swt.custom;

import java.util.function.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VCCombo extends VComposite {

    protected VCCombo() {
    }

    protected VCCombo(DartCCombo impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartCCombo) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartCCombo) impl).alignment = value;
    }

    public boolean getEditable() {
        return ((DartCCombo) impl).getEditable();
    }

    public void setEditable(boolean value) {
        ((DartCCombo) impl).editable = value;
    }

    public String[] getItems() {
        String[] values = ((DartCCombo) impl).items;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(String[]::new);
    }

    public void setItems(String[] value) {
        ((DartCCombo) impl).items = value;
    }

    public boolean getListVisible() {
        return ((DartCCombo) impl).getListVisible();
    }

    public void setListVisible(boolean value) {
        ((DartCCombo) impl).listVisible = value;
    }

    public Point getSelection() {
        return ((DartCCombo) impl).selection;
    }

    public void setSelection(Point value) {
        ((DartCCombo) impl).selection = value;
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartCCombo) impl).getText();
    }

    public void setText(String value) {
        ((DartCCombo) impl).setText(value);
    }

    public int getTextLimit() {
        return ((DartCCombo) impl).getTextLimit();
    }

    public void setTextLimit(int value) {
        ((DartCCombo) impl).textLimit = value;
    }

    public int getVisibleItemCount() {
        return ((DartCCombo) impl).getVisibleItemCount();
    }

    public void setVisibleItemCount(int value) {
        ((DartCCombo) impl).visibleItemCount = value;
    }

    @JsonConverter(target = CCombo.class)
    public static class CComboJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCCombo.class, (JsonWriter.WriteObject<DartCCombo>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartCCombo.class, (JsonReader.ReadObject<DartCCombo>) reader -> {
                return null;
            });
        }

        public static CCombo read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, CCombo api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
