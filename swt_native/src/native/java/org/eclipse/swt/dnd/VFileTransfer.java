package org.eclipse.swt.dnd;

import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

public class VFileTransfer extends VByteArrayTransfer {

    protected VFileTransfer() {
    }

    protected VFileTransfer(DartFileTransfer impl) {
        super(impl);
    }

    @JsonConverter(target = FileTransfer.class)
    public static class FileTransferJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartFileTransfer.class, (JsonWriter.WriteObject<DartFileTransfer>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartFileTransfer.class, (JsonReader.ReadObject<DartFileTransfer>) reader -> {
                return null;
            });
        }

        public static FileTransfer read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, FileTransfer api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }

    @JsonConverter(target = VFileTransfer.class)
    public static class EmptyJson {

        public static VFileTransfer read(JsonReader<?> reader) throws IOException {
            if (reader.wasNull())
                return null;
            reader.skip();
            return new VFileTransfer();
        }

        public static void write(JsonWriter writer, VFileTransfer value) {
            if (value == null)
                writer.writeNull();
            else
                writer.writeAscii("{}");
        }
    }
}
