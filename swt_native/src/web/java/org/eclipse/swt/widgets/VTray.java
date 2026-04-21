package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VTray extends VWidget {

    protected VTray() {
    }

    protected VTray(DartTray impl) {
        super(impl);
    }

    public TrayItem[] getItems() {
        TrayItem[] values = ((DartTray) impl).items;
        if (values == null)
            return null;
        ArrayList<TrayItem> result = new ArrayList<>(values.length);
        for (TrayItem v : values) if (v != null)
            result.add(v);
        return result.toArray(TrayItem[]::new);
    }

    public void setItems(TrayItem[] value) {
        ((DartTray) impl).items = value;
    }

    @JsonConverter(target = Tray.class)
    public static class TrayJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTray.class, (JsonWriter.WriteObject<DartTray>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTray.class, (JsonReader.ReadObject<DartTray>) reader -> {
                return null;
            });
        }

        public static Tray read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Tray api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
