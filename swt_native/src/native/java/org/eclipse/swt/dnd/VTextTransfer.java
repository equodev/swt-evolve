package org.eclipse.swt.dnd;

import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

public class VTextTransfer extends VByteArrayTransfer {

    protected VTextTransfer() {
    }

    protected VTextTransfer(DartTextTransfer impl) {
        super(impl);
    }

    @JsonConverter(target = TextTransfer.class)
    public static class TextTransferJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTextTransfer.class, (JsonWriter.WriteObject<DartTextTransfer>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartTextTransfer.class, (JsonReader.ReadObject<DartTextTransfer>) reader -> {
                return null;
            });
        }

        public static TextTransfer read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, TextTransfer api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }

    @JsonConverter(target = VTextTransfer.class)
    public static class EmptyJson {

        public static VTextTransfer read(JsonReader<?> reader) throws IOException {
            if (reader.wasNull())
                return null;
            reader.skip();
            return new VTextTransfer();
        }

        public static void write(JsonWriter writer, VTextTransfer value) {
            if (value == null)
                writer.writeNull();
            else
                writer.writeAscii("{}");
        }
    }
}
