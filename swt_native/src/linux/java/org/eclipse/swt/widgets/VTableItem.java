package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VTableItem extends VItem {

    protected VTableItem() {
    }

    protected VTableItem(DartTableItem impl) {
        super(impl);
    }

    public Color getBackground() {
        return ((DartTableItem) impl).background;
    }

    public void setBackground(Color value) {
        ((DartTableItem) impl).background = value;
    }

    public boolean getChecked() {
        return ((DartTableItem) impl).getChecked();
    }

    public void setChecked(boolean value) {
        ((DartTableItem) impl).checked = value;
    }

    public Font getFont() {
        Font val = ((DartTableItem) impl).font;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartTableItem) impl).font = value;
    }

    public Color getForeground() {
        return ((DartTableItem) impl).foreground;
    }

    public void setForeground(Color value) {
        ((DartTableItem) impl).foreground = value;
    }

    public boolean getGrayed() {
        return ((DartTableItem) impl).getGrayed();
    }

    public void setGrayed(boolean value) {
        ((DartTableItem) impl).grayed = value;
    }

    public int getImageIndent() {
        return ((DartTableItem) impl).getImageIndent();
    }

    public void setImageIndent(int value) {
        ((DartTableItem) impl).imageIndent = value;
    }

    public String[] getTexts() {
        String[] values = ((DartTableItem) impl).strings;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(String[]::new);
    }

    public void setTexts(String[] value) {
        ((DartTableItem) impl).strings = value;
    }

    @JsonConverter(target = TableItem.class)
    public static class TableItemJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTableItem.class, (JsonWriter.WriteObject<DartTableItem>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTableItem.class, (JsonReader.ReadObject<DartTableItem>) reader -> {
                return null;
            });
        }

        public static TableItem read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, TableItem api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
