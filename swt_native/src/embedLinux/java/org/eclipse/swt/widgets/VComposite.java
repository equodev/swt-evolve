package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VComposite extends VScrollable {

    protected VComposite() {
    }

    protected VComposite(DartComposite impl) {
        super(impl);
    }

    public int getBackgroundMode() {
        return ((DartComposite) impl).getBackgroundMode();
    }

    public void setBackgroundMode(int value) {
        ((DartComposite) impl).backgroundMode = value;
    }

    public Control[] getChildren() {
        Control[] values = ((DartComposite) impl)._getChildren();
        if (values == null)
            return null;
        ArrayList<Control> result = new ArrayList<>(values.length);
        for (Control v : values) if (v != null && v.getImpl() instanceof DartControl)
            result.add(v);
        return result.toArray(new Control[0]);
    }

    public void setChildren(Control[] value) {
        ((DartComposite) impl).children = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getLayoutDeferred() {
        return ((DartComposite) impl).getLayoutDeferred();
    }

    public void setLayoutDeferred(boolean value) {
        ((DartComposite) impl).layoutDeferred = value;
    }

    @JsonAttribute(ignore = true)
    public Control[] getTabList() {
        Control[] values = ((DartComposite) impl).tabList;
        if (values == null)
            return null;
        ArrayList<Control> result = new ArrayList<>(values.length);
        for (Control v : values) if (v != null && v.getImpl() instanceof DartControl)
            result.add(v);
        return result.toArray(new Control[0]);
    }

    public void setTabList(Control[] value) {
        ((DartComposite) impl).tabList = value;
    }

    public static final String BACKGROUND_MODE = "backgroundMode";

    public static final String CHILDREN = "children";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "backgroundMode":
                Serializer.writeKeyValue(writer, "backgroundMode", getBackgroundMode());
                return;
            case "children":
                Serializer.writeKeyValue(writer, "children", getChildren());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Composite.class)
    public static class CompositeJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartComposite.class, (JsonWriter.WriteObject<DartComposite>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartComposite.class, (JsonReader.ReadObject<DartComposite>) reader -> {
                return null;
            });
        }

        public static Composite read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Composite api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
