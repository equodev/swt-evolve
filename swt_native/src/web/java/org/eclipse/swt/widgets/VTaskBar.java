package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VTaskBar extends VWidget {

    protected VTaskBar() {
    }

    protected VTaskBar(DartTaskBar impl) {
        super(impl);
    }

    public TaskItem[] getItems() {
        TaskItem[] values = ((DartTaskBar) impl).items;
        if (values == null)
            return null;
        ArrayList<TaskItem> result = new ArrayList<>(values.length);
        for (TaskItem v : values) if (v != null)
            result.add(v);
        return result.toArray(TaskItem[]::new);
    }

    public void setItems(TaskItem[] value) {
        ((DartTaskBar) impl).items = value;
    }

    @JsonConverter(target = TaskBar.class)
    public static class TaskBarJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTaskBar.class, (JsonWriter.WriteObject<DartTaskBar>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTaskBar.class, (JsonReader.ReadObject<DartTaskBar>) reader -> {
                return null;
            });
        }

        public static TaskBar read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, TaskBar api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
