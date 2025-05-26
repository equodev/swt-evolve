package org.eclipse.swt.graphics;

import java.io.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;

@CompiledJson()
public class VRectangle {

    public int x;

    public int y;

    public int width;

    public int height;

    @JsonConverter(target = Rectangle.class)
    public abstract static class RectangleJson {
        public static Rectangle read (JsonReader reader) {
            return null;
        }
        public static void write (JsonWriter writer, Rectangle value) {
            VRectangle valueObj = ((DartRectangle) value.getImpl()).getValue();
            writer.serializeObject(valueObj);
        }
    }
}
