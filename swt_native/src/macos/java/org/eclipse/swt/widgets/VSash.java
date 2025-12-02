package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VSash extends VControl {

    protected VSash() {
    }

    protected VSash(DartSash impl) {
        super(impl);
    }

    @JsonConverter(target = Sash.class)
    public static class SashJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartSash.class, (JsonWriter.WriteObject<DartSash>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartSash.class, (JsonReader.ReadObject<DartSash>) reader -> {
                return null;
            });
        }

        public static Sash read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Sash api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
