package org.eclipse.swt.custom;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

public class VBusyIndicator {

    protected VBusyIndicator() {
    }

    protected VBusyIndicator(DartBusyIndicator impl) {
        this.impl = impl;
    }

    protected DartBusyIndicator impl;

    @JsonConverter(target = BusyIndicator.class)
    public static class BusyIndicatorJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartBusyIndicator.class, (JsonWriter.WriteObject<DartBusyIndicator>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartBusyIndicator.class, (JsonReader.ReadObject<DartBusyIndicator>) reader -> {
                return null;
            });
        }

        public static BusyIndicator read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, BusyIndicator api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }

    @JsonConverter(target = VBusyIndicator.class)
    public static class EmptyJson {

        public static VBusyIndicator read(JsonReader<?> reader) throws IOException {
            if (reader.wasNull())
                return null;
            reader.skip();
            return new VBusyIndicator();
        }

        public static void write(JsonWriter writer, VBusyIndicator value) {
            if (value == null)
                writer.writeNull();
            else
                writer.writeAscii("{}");
        }
    }
}
