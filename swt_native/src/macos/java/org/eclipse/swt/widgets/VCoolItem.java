package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VCoolItem extends VItem {

    protected VCoolItem() {
    }

    protected VCoolItem(DartCoolItem impl) {
        super(impl);
    }

    public Control getControl() {
        Control val = ((DartCoolItem) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartCoolItem) impl).control = value;
    }

    public Point getMinimumSize() {
        return ((DartCoolItem) impl).getMinimumSize();
    }

    public void setMinimumSize(Point value) {
        ((DartCoolItem) impl).setMinimumSize(value);
    }

    public int getMinimumWidth() {
        return ((DartCoolItem) impl).minimumWidth;
    }

    public void setMinimumWidth(int value) {
        ((DartCoolItem) impl).minimumWidth = value;
    }

    public Point getPreferredSize() {
        return ((DartCoolItem) impl).getPreferredSize();
    }

    public void setPreferredSize(Point value) {
        ((DartCoolItem) impl).setPreferredSize(value);
    }

    public int getPreferredWidth() {
        return ((DartCoolItem) impl).preferredWidth;
    }

    public void setPreferredWidth(int value) {
        ((DartCoolItem) impl).preferredWidth = value;
    }

    @JsonConverter(target = CoolItem.class)
    public static class CoolItemJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCoolItem.class, (JsonWriter.WriteObject<DartCoolItem>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartCoolItem.class, (JsonReader.ReadObject<DartCoolItem>) reader -> {
                return null;
            });
        }

        public static CoolItem read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, CoolItem api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
