package org.eclipse.swt.custom;

import java.util.function.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;
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

    @JsonAttribute(ignore = true)
    public boolean getEditable() {
        return ((DartCCombo) impl).getEditable();
    }

    public void setEditable(boolean value) {
        ((DartCCombo) impl).editable = value;
    }

    public String[] getItemTooltips() {
        String[] values = ((DartCCombo) impl).itemTooltips;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(String[]::new);
    }

    public void setItemTooltips(String[] value) {
        ((DartCCombo) impl).itemTooltips = value;
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

    @JsonAttribute(ignore = true)
    public int getVisibleItemCount() {
        return ((DartCCombo) impl).getVisibleItemCount();
    }

    public void setVisibleItemCount(int value) {
        ((DartCCombo) impl).visibleItemCount = value;
    }

    public static final String ALIGNMENT = "alignment";

    public static final String ITEM_TOOLTIPS = "itemTooltips";

    public static final String ITEMS = "items";

    public static final String LIST_VISIBLE = "listVisible";

    public static final String SELECTION = "selection";

    public static final String TEXT = "text";

    public static final String TEXT_LIMIT = "textLimit";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "alignment":
                Serializer.writeKeyValue(writer, "alignment", getAlignment());
                return;
            case "itemTooltips":
                Serializer.writeKeyValue(writer, "itemTooltips", getItemTooltips());
                return;
            case "items":
                Serializer.writeKeyValue(writer, "items", getItems());
                return;
            case "listVisible":
                Serializer.writeKeyValue(writer, "listVisible", getListVisible());
                return;
            case "selection":
                Serializer.writeKeyValue(writer, "selection", getSelection());
                return;
            case "text":
                Serializer.writeKeyValue(writer, "text", getText());
                return;
            case "textLimit":
                Serializer.writeKeyValue(writer, "textLimit", getTextLimit());
                return;
        }
        super.writeProperty(writer, key);
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

        public static CCombo read(JsonReader<?> reader) throws IOException {
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
