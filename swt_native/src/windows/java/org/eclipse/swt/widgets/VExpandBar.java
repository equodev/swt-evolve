package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VExpandBar extends VComposite {

    protected VExpandBar() {
    }

    protected VExpandBar(DartExpandBar impl) {
        super(impl);
    }

    public ExpandItem[] getItems() {
        ExpandItem[] values = ((DartExpandBar) impl).items;
        if (values == null)
            return null;
        ArrayList<ExpandItem> result = new ArrayList<>(values.length);
        for (ExpandItem v : values) if (v != null)
            result.add(v);
        return result.toArray(ExpandItem[]::new);
    }

    public void setItems(ExpandItem[] value) {
        ((DartExpandBar) impl).items = value;
    }

    public int getSpacing() {
        return ((DartExpandBar) impl).getSpacing();
    }

    public void setSpacing(int value) {
        ((DartExpandBar) impl).spacing = value;
    }

    @JsonConverter(target = ExpandBar.class)
    public static class ExpandBarJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartExpandBar.class, (JsonWriter.WriteObject<DartExpandBar>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartExpandBar.class, (JsonReader.ReadObject<DartExpandBar>) reader -> {
                return null;
            });
        }

        public static ExpandBar read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, ExpandBar api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
