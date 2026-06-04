package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;
import java.util.ArrayList;

@CompiledJson()
public class VDecorations extends VCanvas {

    protected VDecorations() {
    }

    protected VDecorations(DartDecorations impl) {
        super(impl);
    }

    public Button getDefaultButton() {
        Button val = ((DartDecorations) impl).defaultButton;
        if (val != null && !(val.getImpl() instanceof DartButton))
            return null;
        return val;
    }

    public void setDefaultButton(Button value) {
        ((DartDecorations) impl).defaultButton = value;
    }

    @JsonAttribute(nullable = true)
    public Image getImage() {
        Image val = ((DartDecorations) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartDecorations) impl).image = value;
    }

    public Image[] getImages() {
        Image[] values = ((DartDecorations) impl).images;
        if (values == null)
            return null;
        ArrayList<Image> result = new ArrayList<>(values.length);
        for (Image v : values) if (v != null)
            result.add(v);
        return result.toArray(Image[]::new);
    }

    public void setImages(Image[] value) {
        ((DartDecorations) impl).images = value;
    }

    public boolean getMaximized() {
        return ((DartDecorations) impl).getMaximized();
    }

    public void setMaximized(boolean value) {
        ((DartDecorations) impl).maximized = value;
    }

    public Menu getMenuBar() {
        Menu val = ((DartDecorations) impl).menuBar;
        if (val != null && !(val.getImpl() instanceof DartMenu))
            return null;
        return val;
    }

    public void setMenuBar(Menu value) {
        ((DartDecorations) impl).menuBar = value;
    }

    public boolean getMinimized() {
        return ((DartDecorations) impl).getMinimized();
    }

    public void setMinimized(boolean value) {
        ((DartDecorations) impl).minimized = value;
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartDecorations) impl).getText();
    }

    public void setText(String value) {
        ((DartDecorations) impl).text = value;
    }

    @JsonConverter(target = Decorations.class)
    public static class DecorationsJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartDecorations.class, (JsonWriter.WriteObject<DartDecorations>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartDecorations.class, (JsonReader.ReadObject<DartDecorations>) reader -> {
                return null;
            });
        }

        public static Decorations read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Decorations api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
