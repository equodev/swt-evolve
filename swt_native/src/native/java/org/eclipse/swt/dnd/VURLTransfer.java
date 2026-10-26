package org.eclipse.swt.dnd;

import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

public class VURLTransfer extends VByteArrayTransfer {

    protected VURLTransfer() {
    }

    protected VURLTransfer(DartURLTransfer impl) {
        super(impl);
    }

    @JsonConverter(target = URLTransfer.class)
    public static class URLTransferJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartURLTransfer.class, (JsonWriter.WriteObject<DartURLTransfer>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartURLTransfer.class, (JsonReader.ReadObject<DartURLTransfer>) reader -> {
                return null;
            });
        }

        public static URLTransfer read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, URLTransfer api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }

    @JsonConverter(target = VURLTransfer.class)
    public static class EmptyJson {

        public static VURLTransfer read(JsonReader<?> reader) throws IOException {
            if (reader.wasNull())
                return null;
            reader.skip();
            return new VURLTransfer();
        }

        public static void write(JsonWriter writer, VURLTransfer value) {
            if (value == null)
                writer.writeNull();
            else
                writer.writeAscii("{}");
        }
    }
}
