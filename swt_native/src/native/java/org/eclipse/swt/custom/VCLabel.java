package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;
import java.util.ArrayList;

@CompiledJson()
public class VCLabel extends VCanvas {

    protected VCLabel() {
    }

    protected VCLabel(DartCLabel impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartCLabel) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartCLabel) impl).align = value;
    }

    public Image getBackgroundImage() {
        Image val = ((DartCLabel) impl).backgroundImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setBackgroundImage(Image value) {
        ((DartCLabel) impl).backgroundImage = value;
    }

    public int getBottomMargin() {
        return ((DartCLabel) impl).getBottomMargin();
    }

    public void setBottomMargin(int value) {
        ((DartCLabel) impl).bottomMargin = value;
    }

    @JsonAttribute(ignore = true)
    public Color[] getGradientColors() {
        Color[] values = ((DartCLabel) impl).gradientColors;
        if (values == null)
            return null;
        ArrayList<Color> result = new ArrayList<>(values.length);
        for (Color v : values) if (v != null)
            result.add(v);
        return result.toArray(new Color[0]);
    }

    public void setGradientColors(Color[] value) {
        ((DartCLabel) impl).gradientColors = value;
    }

    @JsonAttribute(ignore = true)
    public int[] getGradientPercents() {
        return ((DartCLabel) impl).gradientPercents;
    }

    public void setGradientPercents(int[] value) {
        ((DartCLabel) impl).gradientPercents = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getGradientVertical() {
        return ((DartCLabel) impl).gradientVertical;
    }

    public void setGradientVertical(boolean value) {
        ((DartCLabel) impl).gradientVertical = value;
    }

    @JsonAttribute(nullable = true)
    public Image getImage() {
        Image val = ((DartCLabel) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartCLabel) impl).image = value;
    }

    public int getLeftMargin() {
        return ((DartCLabel) impl).getLeftMargin();
    }

    public void setLeftMargin(int value) {
        ((DartCLabel) impl).leftMargin = value;
    }

    public int getRightMargin() {
        return ((DartCLabel) impl).getRightMargin();
    }

    public void setRightMargin(int value) {
        ((DartCLabel) impl).rightMargin = value;
    }

    public String getText() {
        return ((DartCLabel) impl).getText();
    }

    public void setText(String value) {
        ((DartCLabel) impl).text = value;
    }

    public int getTopMargin() {
        return ((DartCLabel) impl).getTopMargin();
    }

    public void setTopMargin(int value) {
        ((DartCLabel) impl).topMargin = value;
    }

    public static final String ALIGNMENT = "alignment";

    public static final String BACKGROUND_IMAGE = "backgroundImage";

    public static final String BOTTOM_MARGIN = "bottomMargin";

    public static final String IMAGE = "image";

    public static final String LEFT_MARGIN = "leftMargin";

    public static final String RIGHT_MARGIN = "rightMargin";

    public static final String TEXT = "text";

    public static final String TOP_MARGIN = "topMargin";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "alignment":
                Serializer.writeKeyValue(writer, "alignment", getAlignment());
                return;
            case "backgroundImage":
                Serializer.writeKeyValue(writer, "backgroundImage", getBackgroundImage());
                return;
            case "bottomMargin":
                Serializer.writeKeyValue(writer, "bottomMargin", getBottomMargin());
                return;
            case "image":
                Serializer.writeKeyValue(writer, "image", getImage());
                return;
            case "leftMargin":
                Serializer.writeKeyValue(writer, "leftMargin", getLeftMargin());
                return;
            case "rightMargin":
                Serializer.writeKeyValue(writer, "rightMargin", getRightMargin());
                return;
            case "text":
                Serializer.writeKeyValue(writer, "text", getText());
                return;
            case "topMargin":
                Serializer.writeKeyValue(writer, "topMargin", getTopMargin());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = CLabel.class)
    public static class CLabelJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCLabel.class, (JsonWriter.WriteObject<DartCLabel>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartCLabel.class, (JsonReader.ReadObject<DartCLabel>) reader -> {
                return null;
            });
        }

        public static CLabel read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, CLabel api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
