package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VTabFolder extends VComposite {

    protected VTabFolder() {
    }

    protected VTabFolder(DartTabFolder impl) {
        super(impl);
    }

    public TabItem[] getItems() {
        TabItem[] values = ((DartTabFolder) impl).items;
        if (values == null)
            return null;
        ArrayList<TabItem> result = new ArrayList<>(values.length);
        for (TabItem v : values) if (v != null)
            result.add(v);
        return result.toArray(TabItem[]::new);
    }

    public void setItems(TabItem[] value) {
        ((DartTabFolder) impl).items = value;
    }

    public TabItem[] getSelection() {
        TabItem[] values = ((DartTabFolder) impl).selection;
        if (values == null)
            return null;
        ArrayList<TabItem> result = new ArrayList<>(values.length);
        for (TabItem v : values) if (v != null)
            result.add(v);
        return result.toArray(TabItem[]::new);
    }

    public void setSelection(TabItem[] value) {
        ((DartTabFolder) impl).selection = value;
    }

    @JsonConverter(target = TabFolder.class)
    public static class TabFolderJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTabFolder.class, (JsonWriter.WriteObject<DartTabFolder>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTabFolder.class, (JsonReader.ReadObject<DartTabFolder>) reader -> {
                return null;
            });
        }

        public static TabFolder read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, TabFolder api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
