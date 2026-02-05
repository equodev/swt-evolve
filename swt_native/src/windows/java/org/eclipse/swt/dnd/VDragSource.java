package org.eclipse.swt.dnd;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VDragSource extends VWidget {

    protected VDragSource() {
    }

    protected VDragSource(DartDragSource impl) {
        super(impl);
    }

    public Control getControl() {
        Control val = ((DartDragSource) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartDragSource) impl).control = value;
    }

    @JsonAttribute(ignore = true)
    public Transfer[] getTransfer() {
        Transfer[] values = ((DartDragSource) impl).transferAgents;
        if (values == null)
            return null;
        ArrayList<Transfer> result = new ArrayList<>(values.length);
        for (Transfer v : values) if (v != null)
            result.add(v);
        return result.toArray(Transfer[]::new);
    }

    public void setTransfer(Transfer[] value) {
        ((DartDragSource) impl).transferAgents = value;
    }

    @JsonConverter(target = DragSource.class)
    public static class DragSourceJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartDragSource.class, (JsonWriter.WriteObject<DartDragSource>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartDragSource.class, (JsonReader.ReadObject<DartDragSource>) reader -> {
                return null;
            });
        }

        public static DragSource read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, DragSource api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
