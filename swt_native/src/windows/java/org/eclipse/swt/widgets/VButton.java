package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VButton extends VControl {

    protected VButton() {
    }

    protected VButton(DartButton impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartButton) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartButton) impl).setAlignment(value);
    }

    public boolean getGrayed() {
        return ((DartButton) impl).getGrayed();
    }

    public void setGrayed(boolean value) {
        ((DartButton) impl).grayed = value;
    }

    @JsonAttribute(nullable = true)
    public Image getImage() {
        Image val = ((DartButton) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartButton) impl).image = value;
    }

    public boolean getSelection() {
        return ((DartButton) impl).getSelection();
    }

    public void setSelection(boolean value) {
        ((DartButton) impl).selection = value;
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartButton) impl).getText();
    }

    public void setText(String value) {
        ((DartButton) impl).text = value;
    }

    @JsonConverter(target = Button.class)
    public static class ButtonJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartButton.class, (JsonWriter.WriteObject<DartButton>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartButton.class, (JsonReader.ReadObject<DartButton>) reader -> {
                return null;
            });
        }

        public static Button read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Button api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
