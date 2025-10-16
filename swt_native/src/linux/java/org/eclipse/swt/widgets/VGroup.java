package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VGroup extends VComposite {

    protected VGroup() {
    }

    protected VGroup(DartGroup impl) {
        super(impl);
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartGroup) impl).text;
    }

    public void setText(String value) {
        ((DartGroup) impl).text = value;
    }

    @JsonConverter(target = Group.class)
    public static class GroupJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartGroup.class, (JsonWriter.WriteObject<DartGroup>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartGroup.class, (JsonReader.ReadObject<DartGroup>) reader -> {
                return null;
            });
        }

        public static Group read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Group api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
