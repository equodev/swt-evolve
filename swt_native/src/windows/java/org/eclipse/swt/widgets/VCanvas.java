package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VCanvas extends VComposite {

    protected VCanvas() {
    }

    protected VCanvas(DartCanvas impl) {
        super(impl);
    }

    public Caret getCaret() {
        Caret val = ((DartCanvas) impl).caret;
        if (val != null && !(val.getImpl() instanceof DartCaret))
            return null;
        return val;
    }

    public void setCaret(Caret value) {
        ((DartCanvas) impl).caret = value;
    }

    @JsonAttribute(ignore = true)
    public IME getIme() {
        return ((DartCanvas) impl).ime;
    }

    public void setIme(IME value) {
        ((DartCanvas) impl).ime = value;
    }

    @JsonConverter(target = Canvas.class)
    public static class CanvasJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCanvas.class, (JsonWriter.WriteObject<DartCanvas>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartCanvas.class, (JsonReader.ReadObject<DartCanvas>) reader -> {
                return null;
            });
        }

        public static Canvas read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Canvas api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
