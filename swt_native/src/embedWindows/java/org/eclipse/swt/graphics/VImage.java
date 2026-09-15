package org.eclipse.swt.graphics;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;
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

    @JsonAttribute(ignore = true)
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

    public int getHeight() {
        return ((DartImage) impl)._wireHeight();
    }

    public void setHeight(int value) {
    }

    public ImageData getImageData() {
        return ((DartImage) impl)._imageDataForWire();
    }

    public void setImageData(ImageData value) {
        ((DartImage) impl).imageData = value;
    }

    @JsonAttribute(nullable = true)
    public Long getRemoteRef() {
        return ((DartImage) impl).remoteRef;
    }

    public void setRemoteRef(Long value) {
        ((DartImage) impl).remoteRef = value;
    }

    public String getSvgContent() {
        return ((DartImage) impl).svgContent;
    }

    public void setSvgContent(String value) {
        ((DartImage) impl).svgContent = value;
    }

    public int getWidth() {
        return ((DartImage) impl)._wireWidth();
    }

    public void setWidth(int value) {
    }

    public static final String FILENAME = "filename";

    public static final String HEIGHT = "height";

    public static final String IMAGE_DATA = "imageData";

    public static final String REMOTE_REF = "remoteRef";

    public static final String SVG_CONTENT = "svgContent";

    public static final String WIDTH = "width";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "filename":
                Serializer.writeKeyValue(writer, "filename", getFilename());
                return;
            case "height":
                Serializer.writeKeyValue(writer, "height", getHeight());
                return;
            case "imageData":
                Serializer.writeKeyValue(writer, "imageData", getImageData());
                return;
            case "remoteRef":
                Serializer.writeKeyValue(writer, "remoteRef", getRemoteRef());
                return;
            case "svgContent":
                Serializer.writeKeyValue(writer, "svgContent", getSvgContent());
                return;
            case "width":
                Serializer.writeKeyValue(writer, "width", getWidth());
                return;
        }
        super.writeProperty(writer, key);
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

        public static Image read(JsonReader<?> reader) throws IOException {
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
