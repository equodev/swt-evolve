package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VExpandItem extends VItem {

    protected VExpandItem() {
    }

    protected VExpandItem(DartExpandItem impl) {
        super(impl);
    }

    public Control getControl() {
        Control val = ((DartExpandItem) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartExpandItem) impl).control = value;
    }

    public boolean getExpanded() {
        return ((DartExpandItem) impl).getExpanded();
    }

    public void setExpanded(boolean value) {
        ((DartExpandItem) impl).expanded = value;
    }

    public int getHeight() {
        return ((DartExpandItem) impl).getHeight();
    }

    public void setHeight(int value) {
        ((DartExpandItem) impl).height = value;
    }

    @JsonConverter(target = ExpandItem.class)
    public static class ExpandItemJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartExpandItem.class, (JsonWriter.WriteObject<DartExpandItem>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartExpandItem.class, (JsonReader.ReadObject<DartExpandItem>) reader -> {
                return null;
            });
        }

        public static ExpandItem read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, ExpandItem api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
