package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VScrollBar extends VWidget {

    protected VScrollBar() {
    }

    protected VScrollBar(DartScrollBar impl) {
        super(impl);
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public boolean getEnabled() {
        return ((DartScrollBar) impl).getEnabled();
    }

    public void setEnabled(boolean value) {
        ((DartScrollBar) impl).enabled = value;
    }

    public int getIncrement() {
        return ((DartScrollBar) impl).getIncrement();
    }

    public void setIncrement(int value) {
        ((DartScrollBar) impl).increment = value;
    }

    public int getMaximum() {
        return ((DartScrollBar) impl).getMaximum();
    }

    public void setMaximum(int value) {
        ((DartScrollBar) impl).maximum = value;
    }

    public int getMinimum() {
        return ((DartScrollBar) impl).getMinimum();
    }

    public void setMinimum(int value) {
        ((DartScrollBar) impl).minimum = value;
    }

    @JsonAttribute(ignore = true)
    public int getPageIncrement() {
        return ((DartScrollBar) impl).getPageIncrement();
    }

    public void setPageIncrement(int value) {
        ((DartScrollBar) impl).pageIncrement = value;
    }

    public int getSelection() {
        return ((DartScrollBar) impl).getSelection();
    }

    public void setSelection(int value) {
        ((DartScrollBar) impl).selection = value;
    }

    public int getThumb() {
        return ((DartScrollBar) impl).getThumb();
    }

    public void setThumb(int value) {
        ((DartScrollBar) impl).thumb = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public boolean getVisible() {
        return ((DartScrollBar) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartScrollBar) impl).visible = value;
    }

    public static final String ENABLED = "enabled";

    public static final String INCREMENT = "increment";

    public static final String MAXIMUM = "maximum";

    public static final String MINIMUM = "minimum";

    public static final String SELECTION = "selection";

    public static final String THUMB = "thumb";

    public static final String VISIBLE = "visible";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "enabled":
                Serializer.writeKeyValue(writer, "enabled", getEnabled());
                return;
            case "increment":
                Serializer.writeKeyValue(writer, "increment", getIncrement());
                return;
            case "maximum":
                Serializer.writeKeyValue(writer, "maximum", getMaximum());
                return;
            case "minimum":
                Serializer.writeKeyValue(writer, "minimum", getMinimum());
                return;
            case "selection":
                Serializer.writeKeyValue(writer, "selection", getSelection());
                return;
            case "thumb":
                Serializer.writeKeyValue(writer, "thumb", getThumb());
                return;
            case "visible":
                Serializer.writeKeyValue(writer, "visible", getVisible());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = ScrollBar.class)
    public static class ScrollBarJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartScrollBar.class, (JsonWriter.WriteObject<DartScrollBar>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartScrollBar.class, (JsonReader.ReadObject<DartScrollBar>) reader -> {
                return null;
            });
        }

        public static ScrollBar read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, ScrollBar api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
