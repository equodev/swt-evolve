package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VControlEditor {

    protected VControlEditor() {
    }

    protected VControlEditor(DartControlEditor impl) {
        this.impl = impl;
    }

    protected DartControlEditor impl;

    public Control getEditor() {
        Control val = ((DartControlEditor) impl).editor;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setEditor(Control value) {
        ((DartControlEditor) impl).editor = value;
    }

    @JsonConverter(target = ControlEditor.class)
    public static class ControlEditorJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartControlEditor.class, (JsonWriter.WriteObject<DartControlEditor>) (writer, impl) -> {
                Serializer.writeEditorWithId(json, writer, impl);
            });
            json.registerReader(DartControlEditor.class, (JsonReader.ReadObject<DartControlEditor>) reader -> {
                return null;
            });
        }

        public static ControlEditor read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, ControlEditor api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
