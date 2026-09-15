package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VSlider extends VControl {

    protected VSlider() {
    }

    protected VSlider(DartSlider impl) {
        super(impl);
    }

    public int getIncrement() {
        return ((DartSlider) impl).getIncrement();
    }

    public void setIncrement(int value) {
        ((DartSlider) impl).increment = value;
    }

    public int getMaximum() {
        return ((DartSlider) impl).getMaximum();
    }

    public void setMaximum(int value) {
        ((DartSlider) impl).maximum = value;
    }

    public int getMinimum() {
        return ((DartSlider) impl).getMinimum();
    }

    public void setMinimum(int value) {
        ((DartSlider) impl).minimum = value;
    }

    public int getPageIncrement() {
        return ((DartSlider) impl).getPageIncrement();
    }

    public void setPageIncrement(int value) {
        ((DartSlider) impl).pageIncrement = value;
    }

    public int getSelection() {
        return ((DartSlider) impl).getSelection();
    }

    public void setSelection(int value) {
        ((DartSlider) impl).selection = value;
    }

    public int getThumb() {
        return ((DartSlider) impl).getThumb();
    }

    public void setThumb(int value) {
        ((DartSlider) impl).thumb = value;
    }

    public static final String INCREMENT = "increment";

    public static final String MAXIMUM = "maximum";

    public static final String MINIMUM = "minimum";

    public static final String PAGE_INCREMENT = "pageIncrement";

    public static final String SELECTION = "selection";

    public static final String THUMB = "thumb";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "increment":
                Serializer.writeKeyValue(writer, "increment", getIncrement());
                return;
            case "maximum":
                Serializer.writeKeyValue(writer, "maximum", getMaximum());
                return;
            case "minimum":
                Serializer.writeKeyValue(writer, "minimum", getMinimum());
                return;
            case "pageIncrement":
                Serializer.writeKeyValue(writer, "pageIncrement", getPageIncrement());
                return;
            case "selection":
                Serializer.writeKeyValue(writer, "selection", getSelection());
                return;
            case "thumb":
                Serializer.writeKeyValue(writer, "thumb", getThumb());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Slider.class)
    public static class SliderJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartSlider.class, (JsonWriter.WriteObject<DartSlider>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartSlider.class, (JsonReader.ReadObject<DartSlider>) reader -> {
                return null;
            });
        }

        public static Slider read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Slider api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
