package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.List;
import java.util.ArrayList;

@CompiledJson()
public class VComposite extends VScrollable {

    public int backgroundMode;

    public List<Control> children = new ArrayList<>();

    @JsonAttribute(ignore = true)
    public Layout layout;

    public boolean layoutDeferred;

    public Control[] tabList;

    @JsonConverter(target = Composite.class)
    public static class CompositeJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartComposite.class, (JsonWriter.WriteObject<DartComposite>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartComposite.class, (JsonReader.ReadObject<DartComposite>) reader -> {
                return null;
            });
        }

        public static Composite read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Composite api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
