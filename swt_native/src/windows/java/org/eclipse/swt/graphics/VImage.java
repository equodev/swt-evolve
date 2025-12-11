package org.eclipse.swt.graphics;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.function.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VImage extends VResource {

    protected VImage() {
    }

    protected VImage(DartImage impl) {
        super(impl);
    }

    public Color getBackground() {
        return ((DartImage) impl).background;
    }

    public void setBackground(Color value) {
        ((DartImage) impl).background = value;
    }

    public String getFilename() {
        return ((DartImage) impl).filename;
    }

    public void setFilename(String value) {
        ((DartImage) impl).filename = value;
    }

    public ImageData getImageData() {
        return ((DartImage) impl).imageData;
    }

    public void setImageData(ImageData value) {
        ((DartImage) impl).imageData = value;
    }

    @JsonConverter(target = Image.class)
    public static class ImageJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartImage.class, (JsonWriter.WriteObject<DartImage>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartImage.class, (JsonReader.ReadObject<DartImage>) reader -> {
                return null;
            });
        }

        public static Image read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Image api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
