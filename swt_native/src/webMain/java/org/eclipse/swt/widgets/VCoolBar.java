package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VCoolBar extends VComposite {

    protected VCoolBar() {
    }

    protected VCoolBar(DartCoolBar impl) {
        super(impl);
    }

    public int[] getItemOrder() {
        return ((DartCoolBar) impl).itemOrder;
    }

    public void setItemOrder(int[] value) {
        ((DartCoolBar) impl).itemOrder = value;
    }

    public Point[] getItemSizes() {
        Point[] values = ((DartCoolBar) impl).itemSizes;
        if (values == null)
            return null;
        ArrayList<Point> result = new ArrayList<>(values.length);
        for (Point v : values) if (v != null)
            result.add(v);
        return result.toArray(Point[]::new);
    }

    public void setItemSizes(Point[] value) {
        ((DartCoolBar) impl).itemSizes = value;
    }

    public CoolItem[] getItems() {
        CoolItem[] values = ((DartCoolBar) impl).getItems();
        if (values == null)
            return null;
        ArrayList<CoolItem> result = new ArrayList<>(values.length);
        for (CoolItem v : values) if (v != null)
            result.add(v);
        return result.toArray(CoolItem[]::new);
    }

    public void setItems(CoolItem[] value) {
        ((DartCoolBar) impl).items = new CoolItem[][] { value };
    }

    public boolean getLocked() {
        return ((DartCoolBar) impl).getLocked();
    }

    public void setLocked(boolean value) {
        ((DartCoolBar) impl).isLocked = value;
    }

    public int[] getWrapIndices() {
        return ((DartCoolBar) impl).wrapIndices;
    }

    public void setWrapIndices(int[] value) {
        ((DartCoolBar) impl).wrapIndices = value;
    }

    @JsonConverter(target = CoolBar.class)
    public static class CoolBarJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCoolBar.class, (JsonWriter.WriteObject<DartCoolBar>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartCoolBar.class, (JsonReader.ReadObject<DartCoolBar>) reader -> {
                return null;
            });
        }

        public static CoolBar read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, CoolBar api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
