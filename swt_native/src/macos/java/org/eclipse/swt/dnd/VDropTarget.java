package org.eclipse.swt.dnd;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VDropTarget extends VWidget {

    protected VDropTarget() {
    }

    protected VDropTarget(DartDropTarget impl) {
        super(impl);
    }

    public Control getControl() {
        Control val = ((DartDropTarget) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartDropTarget) impl).control = value;
    }

    @JsonAttribute(ignore = true)
    public Transfer[] getTransfer() {
        Transfer[] values = ((DartDropTarget) impl).transferAgents;
        if (values == null)
            return null;
        ArrayList<Transfer> result = new ArrayList<>(values.length);
        for (Transfer v : values) if (v != null)
            result.add(v);
        return result.toArray(Transfer[]::new);
    }

    public void setTransfer(Transfer[] value) {
        ((DartDropTarget) impl).transferAgents = value;
    }

    @JsonConverter(target = DropTarget.class)
    public static class DropTargetJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartDropTarget.class, (JsonWriter.WriteObject<DartDropTarget>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartDropTarget.class, (JsonReader.ReadObject<DartDropTarget>) reader -> {
                return null;
            });
        }

        public static DropTarget read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, DropTarget api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
