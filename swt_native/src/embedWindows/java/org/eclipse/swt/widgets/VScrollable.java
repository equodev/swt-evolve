package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

public class VScrollable extends VControl {

    protected VScrollable() {
    }

    protected VScrollable(IScrollable impl) {
        super(impl);
    }

    public ScrollBar getHorizontalBar() {
        ScrollBar val = ((DartScrollable) impl).horizontalBar;
        if (val != null && !(val.getImpl() instanceof DartScrollBar))
            return null;
        return val;
    }

    public void setHorizontalBar(ScrollBar value) {
        ((DartScrollable) impl).horizontalBar = value;
    }

    @JsonAttribute(ignore = true)
    public int getScrollbarsMode() {
        return ((DartScrollable) impl).getScrollbarsMode();
    }

    public void setScrollbarsMode(int value) {
        ((DartScrollable) impl).scrollbarsMode = value;
    }

    public ScrollBar getVerticalBar() {
        ScrollBar val = ((DartScrollable) impl).verticalBar;
        if (val != null && !(val.getImpl() instanceof DartScrollBar))
            return null;
        return val;
    }

    public void setVerticalBar(ScrollBar value) {
        ((DartScrollable) impl).verticalBar = value;
    }

    public static final String HORIZONTAL_BAR = "horizontalBar";

    public static final String VERTICAL_BAR = "verticalBar";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "horizontalBar":
                Serializer.writeKeyValue(writer, "horizontalBar", getHorizontalBar());
                return;
            case "verticalBar":
                Serializer.writeKeyValue(writer, "verticalBar", getVerticalBar());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Scrollable.class)
    public static class ScrollableJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartScrollable.class, (JsonWriter.WriteObject<DartScrollable>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartScrollable.class, (JsonReader.ReadObject<DartScrollable>) reader -> {
                return null;
            });
        }

        public static Scrollable read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Scrollable api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
