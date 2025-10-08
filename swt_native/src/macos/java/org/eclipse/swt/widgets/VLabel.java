package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VLabel extends VControl {

    protected VLabel() {
    }

    protected VLabel(DartLabel impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartLabel) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartLabel) impl).setAlignment(value);
    }

    @JsonAttribute(nullable = true)
    public Image getImage() {
        Image val = ((DartLabel) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartLabel) impl).image = value;
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartLabel) impl).text;
    }

    public void setText(String value) {
        ((DartLabel) impl).text = value;
    }

    @JsonConverter(target = Label.class)
    public static class LabelJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartLabel.class, (JsonWriter.WriteObject<DartLabel>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartLabel.class, (JsonReader.ReadObject<DartLabel>) reader -> {
                return null;
            });
        }

        public static Label read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Label api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
