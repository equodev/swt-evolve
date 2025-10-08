package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VList extends VScrollable {

    protected VList() {
    }

    protected VList(DartList impl) {
        super(impl);
    }

    public String[] getItems() {
        String[] values = ((DartList) impl).items;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(String[]::new);
    }

    public void setItems(String[] value) {
        ((DartList) impl).items = value;
    }

    public int[] getSelection() {
        return ((DartList) impl).selection;
    }

    public void setSelection(int[] value) {
        ((DartList) impl).selection = value;
    }

    public int getTopIndex() {
        return ((DartList) impl).getTopIndex();
    }

    public void setTopIndex(int value) {
        ((DartList) impl).topIndex = value;
    }

    @JsonConverter(target = List.class)
    public static class ListJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartList.class, (JsonWriter.WriteObject<DartList>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartList.class, (JsonReader.ReadObject<DartList>) reader -> {
                return null;
            });
        }

        public static List read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, List api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
