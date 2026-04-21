package org.eclipse.swt.dnd;

import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VTransferData {

    protected VTransferData() {
    }

    protected VTransferData(DartTransferData impl) {
        this.impl = impl;
    }

    protected DartTransferData impl;

    @JsonConverter(target = TransferData.class)
    public static class TransferDataJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTransferData.class, (JsonWriter.WriteObject<DartTransferData>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartTransferData.class, (JsonReader.ReadObject<DartTransferData>) reader -> {
                return null;
            });
        }

        public static TransferData read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, TransferData api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
