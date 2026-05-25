package org.eclipse.swt.dnd;

import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

public class VByteArrayTransfer extends VTransfer {

    protected VByteArrayTransfer() {
    }

    protected VByteArrayTransfer(IByteArrayTransfer impl) {
        super(impl);
    }

    @JsonConverter(target = ByteArrayTransfer.class)
    public static class ByteArrayTransferJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartByteArrayTransfer.class, (JsonWriter.WriteObject<DartByteArrayTransfer>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartByteArrayTransfer.class, (JsonReader.ReadObject<DartByteArrayTransfer>) reader -> {
                return null;
            });
        }

        public static ByteArrayTransfer read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, ByteArrayTransfer api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
