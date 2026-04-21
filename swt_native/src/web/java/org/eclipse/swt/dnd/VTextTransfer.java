package org.eclipse.swt.dnd;

import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
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

        public static TextTransfer read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, TextTransfer api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
