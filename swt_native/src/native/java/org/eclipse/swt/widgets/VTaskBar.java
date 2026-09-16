package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;
import java.util.ArrayList;

@CompiledJson()
public class VTaskBar extends VWidget {

    protected VTaskBar() {
    }

    protected VTaskBar(DartTaskBar impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public TaskItem[] getItems() {
        TaskItem[] values = ((DartTaskBar) impl).items;
        if (values == null)
            return null;
        ArrayList<TaskItem> result = new ArrayList<>(values.length);
        for (TaskItem v : values) if (v != null)
            result.add(v);
        return result.toArray(new TaskItem[0]);
    }

    public void setItems(TaskItem[] value) {
        ((DartTaskBar) impl).items = value;
    }

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
        }
        super.writeProperty(writer, key);
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

        public static TaskBar read(JsonReader<?> reader) throws IOException {
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
