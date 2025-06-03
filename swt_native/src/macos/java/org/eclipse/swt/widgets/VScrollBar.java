package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VScrollBar extends VWidget {

    protected VScrollBar() {
    }

    protected VScrollBar(DartScrollBar impl) {
        super(impl);
    }

    public int getIncrement() {
        return ((DartScrollBar) impl).increment;
    }

    public void setIncrement(int value) {
        ((DartScrollBar) impl).increment = value;
    }

    public int getMaximum() {
        return ((DartScrollBar) impl).maximum;
    }

    public void setMaximum(int value) {
        ((DartScrollBar) impl).maximum = value;
    }

    public int getMinimum() {
        return ((DartScrollBar) impl).minimum;
    }

    public void setMinimum(int value) {
        ((DartScrollBar) impl).minimum = value;
    }

    public int getPageIncrement() {
        return ((DartScrollBar) impl).pageIncrement;
    }

    public void setPageIncrement(int value) {
        ((DartScrollBar) impl).pageIncrement = value;
    }

    public int getSelection() {
        return ((DartScrollBar) impl).selection;
    }

    public void setSelection(int value) {
        ((DartScrollBar) impl).selection = value;
    }

    public int getThumb() {
        return ((DartScrollBar) impl).thumb;
    }

    public void setThumb(int value) {
        ((DartScrollBar) impl).thumb = value;
    }

    public boolean getVisible() {
        return ((DartScrollBar) impl).visible;
    }

    public void setVisible(boolean value) {
        ((DartScrollBar) impl).visible = value;
    }

    public int getState() {
        return ((DartScrollBar) impl).state;
    }

    public void setState(int value) {
        ((DartScrollBar) impl).state = value;
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

        public static ScrollBar read(JsonReader<?> reader) {
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
