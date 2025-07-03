package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VCLabel extends VCanvas {

    protected VCLabel() {
    }

    protected VCLabel(DartCLabel impl) {
        super(impl);
    }

    public int getAlign() {
        return ((DartCLabel) impl).getAlignment();
    }

    public void setAlign(int value) {
        ((DartCLabel) impl).align = value;
    }

    public int getBottomMargin() {
        return ((DartCLabel) impl).getBottomMargin();
    }

    public void setBottomMargin(int value) {
        ((DartCLabel) impl).bottomMargin = value;
    }

    @JsonAttribute(ignore = true)
    public Image getImage() {
        return ((DartCLabel) impl).image;
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

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartCLabel) impl).text;
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

    public Color[] getGradientColors() {
        Color[] values = ((DartCLabel) impl).gradientColors;
        if (values == null)
            return null;
        ArrayList<Color> result = new ArrayList<>(values.length);
        for (Color v : values) if (v != null)
            result.add(v);
        return result.toArray(Color[]::new);
    }

    public void setGradientColors(Color[] value) {
        ((DartCLabel) impl).gradientColors = value;
    }

    public int[] getGradientPercents() {
        return ((DartCLabel) impl).gradientPercents;
    }

    public void setGradientPercents(int[] value) {
        ((DartCLabel) impl).gradientPercents = value;
    }

    public boolean getGradientVertical() {
        return ((DartCLabel) impl).gradientVertical;
    }

    public void setGradientVertical(boolean value) {
        ((DartCLabel) impl).gradientVertical = value;
    }

    @JsonAttribute(ignore = true)
    public Image getBackgroundImage() {
        return ((DartCLabel) impl).backgroundImage;
    }

    public void setBackgroundImage(Image value) {
        ((DartCLabel) impl).backgroundImage = value;
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

        public static CLabel read(JsonReader<?> reader) {
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
