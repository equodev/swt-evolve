package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VCombo extends VComposite {

    protected VCombo() {
    }

    protected VCombo(DartCombo impl) {
        super(impl);
    }

    public String[] getItems() {
        String[] values = ((DartCombo) impl).items;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(String[]::new);
    }

    public void setItems(String[] value) {
        ((DartCombo) impl).items = value;
    }

    public boolean getListVisible() {
        return ((DartCombo) impl).getListVisible();
    }

    public void setListVisible(boolean value) {
        ((DartCombo) impl).listVisible = value;
    }

    public Point getSelection() {
        return ((DartCombo) impl).selection;
    }

    public void setSelection(Point value) {
        ((DartCombo) impl).selection = value;
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartCombo) impl).text;
    }

    public void setText(String value) {
        ((DartCombo) impl).text = value;
    }

    public int getTextLimit() {
        return ((DartCombo) impl).getTextLimit();
    }

    public void setTextLimit(int value) {
        ((DartCombo) impl).textLimit = value;
    }

    public int getVisibleItemCount() {
        return ((DartCombo) impl).getVisibleItemCount();
    }

    public void setVisibleItemCount(int value) {
        ((DartCombo) impl).visibleCount = value;
    }

    @JsonConverter(target = Combo.class)
    public static class ComboJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCombo.class, (JsonWriter.WriteObject<DartCombo>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartCombo.class, (JsonReader.ReadObject<DartCombo>) reader -> {
                return null;
            });
        }

        public static Combo read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Combo api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
