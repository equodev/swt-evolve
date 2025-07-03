package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VToolBar extends VComposite {

    protected VToolBar() {
    }

    protected VToolBar(DartToolBar impl) {
        super(impl);
    }

    public ToolItem[] getItems() {
        ToolItem[] values = ((DartToolBar) impl).items;
        if (values == null)
            return null;
        ArrayList<ToolItem> result = new ArrayList<>(values.length);
        for (ToolItem v : values) if (v != null)
            result.add(v);
        return result.toArray(ToolItem[]::new);
    }

    public void setItems(ToolItem[] value) {
        ((DartToolBar) impl).items = value;
    }

    public int getRowCount() {
        return ((DartToolBar) impl).getRowCount();
    }

    public void setRowCount(int value) {
        ((DartToolBar) impl).rowCount = value;
    }

    @JsonConverter(target = ToolBar.class)
    public static class ToolBarJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartToolBar.class, (JsonWriter.WriteObject<DartToolBar>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartToolBar.class, (JsonReader.ReadObject<DartToolBar>) reader -> {
                return null;
            });
        }

        public static ToolBar read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, ToolBar api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
