package org.eclipse.swt.opengl;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VGLCanvas extends VCanvas {

    protected VGLCanvas() {
    }

    protected VGLCanvas(DartGLCanvas impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public GLData getGLData() {
        return ((DartGLCanvas) impl).GLData;
    }

    public void setGLData(GLData value) {
        ((DartGLCanvas) impl).GLData = value;
    }

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = GLCanvas.class)
    public static class GLCanvasJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartGLCanvas.class, (JsonWriter.WriteObject<DartGLCanvas>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartGLCanvas.class, (JsonReader.ReadObject<DartGLCanvas>) reader -> {
                return null;
            });
        }

        public static GLCanvas read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, GLCanvas api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
