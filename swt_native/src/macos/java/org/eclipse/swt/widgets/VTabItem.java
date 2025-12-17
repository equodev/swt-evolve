package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VTabItem extends VItem {

    protected VTabItem() {
    }

    protected VTabItem(DartTabItem impl) {
        super(impl);
    }

    public Control getControl() {
        Control val = ((DartTabItem) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartTabItem) impl).control = value;
    }

    public String getToolTipText() {
        return ((DartTabItem) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartTabItem) impl).toolTipText = value;
    }

    @JsonConverter(target = TabItem.class)
    public static class TabItemJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTabItem.class, (JsonWriter.WriteObject<DartTabItem>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTabItem.class, (JsonReader.ReadObject<DartTabItem>) reader -> {
                return null;
            });
        }

        public static TabItem read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, TabItem api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
